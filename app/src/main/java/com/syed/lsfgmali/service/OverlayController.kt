package com.syed.lsfgmali.service

import android.app.Service
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.Gravity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import com.lsfg.android.session.NativeBridge
import com.syed.lsfgmali.R
import com.syed.lsfgmali.data.ForegroundState
import com.syed.lsfgmali.data.LogBus
import com.syed.lsfgmali.data.SettingsStore
import com.syed.lsfgmali.ui.OverlayPanelRoot
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Owns every overlay window:
 *  - the full-screen, never-touchable output SurfaceView
 *  - an optional touch bridge window (only when accessibility is enabled)
 *  - a draggable bubble and a Material 3 control panel
 */
class OverlayController(
    private val service: Service,
    private val config: SessionConfig,
    private val callbacks: Callbacks,
) {
    interface Callbacks {
        fun onStop()
        fun onBypassChange(enabled: Boolean)
        fun onTargetFps(fps: Int)
        fun onOpenApp()
        fun onTouchBridgeFailure(message: String)
    }

    private val windowManager = service.getSystemService(WindowManager::class.java)
    private val store = SettingsStore.get(service)
    private val density = service.resources.displayMetrics.density

    private var fullscreen: SurfaceView? = null
    private var bubble: View? = null
    private var panelView: ComposeView? = null
    private var panelHost: ComposeOverlayHost? = null
    private var bridge: View? = null
    private var forwarder: TouchForwarder? = null

    private var captureW = 0
    private var captureH = 0
    private var suppressed = ForegroundState.mainActivityForeground
    private var starved = false
    private var bypass = false
    private var destroyed = false

    private val overlayType: Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

    fun show(w: Int, h: Int) {
        captureW = w
        captureH = h
        addFullscreen()
        if (config.touchBridge) addTouchBridge()
        if (config.showBubble) addBubble()
        applyVisibility()
    }

    fun onGeometryChanged(w: Int, h: Int) {
        captureW = w
        captureH = h
        rebindOutputSurface()
    }

    fun rebindOutputSurface() {
        val holder = fullscreen?.holder ?: return
        if (captureW <= 0 || captureH <= 0) return
        if (holder.surface?.isValid == true) {
            runCatching { NativeBridge.setOutputSurface(holder.surface, captureW, captureH) }
                .onFailure { LogBus.log("setOutputSurface failed: ${it.message}") }
        }
    }

    fun setBypass(value: Boolean) {
        bypass = value
    }

    fun setSuppressed(value: Boolean) {
        if (suppressed == value) return
        suppressed = value
        applyVisibility()
    }

    fun setStarved(value: Boolean) {
        if (starved == value) return
        starved = value
        applyVisibility()
    }

    fun setBubbleVisible(visible: Boolean) {
        if (visible) {
            if (bubble == null) addBubble()
        } else {
            bubble?.let { runCatching { windowManager.removeView(it) } }
            bubble = null
        }
    }

    fun setTouchBridgeEnabled(enabled: Boolean) {
        if (enabled && bridge == null) addTouchBridge()
        if (!enabled) {
            bridge?.let { runCatching { windowManager.removeView(it) } }
            bridge = null
            forwarder = null
        }
    }

    fun destroy() {
        if (destroyed) return
        destroyed = true
        hidePanel()
        bridge?.let { runCatching { windowManager.removeView(it) } }
        bubble?.let { runCatching { windowManager.removeView(it) } }
        fullscreen?.let { runCatching { windowManager.removeView(it) } }
        bridge = null
        bubble = null
        fullscreen = null
        runCatching { NativeBridge.setOutputSurface(null, 0, 0) }
    }

    private fun addFullscreen() {
        if (fullscreen != null) return
        val view = SurfaceView(service)
        view.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                SkipScreenshot.apply(view)
                rebindOutputSurface()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                rebindOutputSurface()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                runCatching { NativeBridge.setOutputSurface(null, 0, 0) }
            }
        })
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        runCatching { windowManager.addView(view, params) }
            .onFailure { LogBus.log("overlay add failed: ${it.message}") }
        fullscreen = view
    }

    private fun addTouchBridge() {
        if (bridge != null) return
        if (!LsfgTouchService.isReady) {
            LogBus.log("Touch bridge is selected but the accessibility service is off — using native pass-through")
            return
        }
        val touch = TouchForwarder { message ->
            LogBus.log(message)
            setTouchBridgeEnabled(false)
            callbacks.onTouchBridgeFailure(message)
        }
        forwarder = touch
        val view = View(service).apply {
            isClickable = true
            isFocusable = false
            setBackgroundColor(Color.TRANSPARENT)
            setOnTouchListener { v, event -> touch.onTouch(v, event) }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        runCatching { windowManager.addView(view, params) }
            .onFailure { LogBus.log("touch bridge failed: ${it.message}") }
        bridge = view
        SkipScreenshot.apply(view)
        applyBridgeTouchable(!suppressed)
    }

    private fun applyBridgeTouchable(touchable: Boolean) {
        val view = bridge ?: return
        val params = view.layoutParams as? WindowManager.LayoutParams ?: return
        params.flags = if (touchable) {
            params.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        } else {
            params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }
        view.visibility = if (touchable) View.VISIBLE else View.GONE
        runCatching { windowManager.updateViewLayout(view, params) }
        if (!touchable) forwarder?.reset()
    }

    private fun addBubble() {
        if (bubble != null) return
        val size = dp(52)
        val view = ImageView(service).apply {
            setImageResource(R.drawable.ic_bubble)
            setPadding(dp(11), dp(11), dp(11), dp(11))
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xE60D1416.toInt())
                setStroke(dp(1), 0x9900D6C4.toInt())
            }
            contentDescription = "LSFG controls"
            elevation = dp(8).toFloat()
        }
        val screenW = service.resources.displayMetrics.widthPixels
        val savedX = store.settings.value.bubbleX
        val params = WindowManager.LayoutParams(
            size,
            size,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = if (savedX >= 0) savedX else screenW - size - dp(14)
            y = store.settings.value.bubbleY.coerceAtLeast(dp(24))
        }
        var downRawX = 0f
        var downRawY = 0f
        var startX = 0
        var startY = 0
        var moved = false
        val slop = dp(6)
        view.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startX = params.x
                    startY = params.y
                    moved = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - downRawX).roundToInt()
                    val dy = (event.rawY - downRawY).roundToInt()
                    if (abs(dx) > slop || abs(dy) > slop) moved = true
                    val maxX = screenW - size
                    val maxY = service.resources.displayMetrics.heightPixels - size
                    params.x = (startX + dx).coerceIn(0, maxX.coerceAtLeast(0))
                    params.y = (startY + dy).coerceIn(0, maxY.coerceAtLeast(0))
                    runCatching { windowManager.updateViewLayout(view, params) }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (moved) {
                        store.update { it.copy(bubbleX = params.x, bubbleY = params.y) }
                    } else {
                        togglePanel()
                    }
                    true
                }
                else -> false
            }
        }
        runCatching { windowManager.addView(view, params) }
            .onFailure { LogBus.log("bubble add failed: ${it.message}") }
        bubble = view
        SkipScreenshot.apply(view)
    }

    private fun togglePanel() {
        if (panelView != null) {
            hidePanel()
            return
        }
        if (suppressed || destroyed) return
        val host = ComposeOverlayHost().also { it.create() }
        val view = ComposeView(service).apply {
            ViewTreeLifecycleOwner.set(this, host)
            ViewTreeViewModelStoreOwner.set(this, host)
            ViewTreeSavedStateRegistryOwner.set(this, host)
            setContent {
                OverlayPanelRoot(
                    bypass = bypass,
                    onBypass = {
                        bypass = it
                        callbacks.onBypassChange(it)
                    },
                    onTarget = { callbacks.onTargetFps(it) },
                    onStop = { callbacks.onStop() },
                    onOpenApp = { callbacks.onOpenApp() },
                    onClose = { hidePanel() },
                )
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = dp(12)
            y = dp(14)
        }
        runCatching { windowManager.addView(view, params) }
            .onFailure {
                LogBus.log("panel add failed: ${it.message}")
                host.destroy()
                return
            }
        panelView = view
        panelHost = host
        SkipScreenshot.apply(view)
    }

    private fun hidePanel() {
        panelView?.let { runCatching { windowManager.removeView(it) } }
        panelView = null
        panelHost?.destroy()
        panelHost = null
    }

    private fun applyVisibility() {
        fullscreen?.alpha = if (suppressed || starved) 0f else 1f
        bubble?.visibility = if (suppressed) View.GONE else View.VISIBLE
        if (suppressed) hidePanel()
        applyBridgeTouchable(!suppressed)
    }

    private fun dp(value: Int): Int = (value * density).roundToInt()
}

private class ComposeOverlayHost : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore = ViewModelStore()

    private val savedStateController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    fun create() {
        savedStateController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun destroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStore.clear()
    }
}

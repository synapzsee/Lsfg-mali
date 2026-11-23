package com.syed.lsfgmali.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.Surface
import android.view.WindowManager
import com.lsfg.android.session.NativeBridge
import com.syed.lsfgmali.MainActivity
import com.syed.lsfgmali.R
import com.syed.lsfgmali.data.CaptureScale
import com.syed.lsfgmali.data.EngineState
import com.syed.lsfgmali.data.ForegroundState
import com.syed.lsfgmali.data.FpsMode
import com.syed.lsfgmali.data.LogBus
import com.syed.lsfgmali.data.LogSaver
import com.syed.lsfgmali.data.Paths
import com.syed.lsfgmali.data.SessionPhase
import com.syed.lsfgmali.data.SettingsStore
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToLong

class CaptureService : Service() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var projection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var ioThread: HandlerThread? = null
    private var ioHandler: Handler? = null
    private var displayListener: DisplayManager.DisplayListener? = null
    private var overlay: OverlayController? = null

    private var config = SessionConfig()
    private var multiplier = 2
    private var captureW = 0
    private var captureH = 0
    private var currentRotation = Surface.ROTATION_0
    private var densityDpi = 0

    private var sessionActive = false
    private var contextReady = false
    private var reconfiguring = false
    private var tornDown = false
    private var starved = false
    private var suppressed = false
    private var bypass = false
    private var nativeFp16 = false
    private var mirrorMode = false
    private var manualMultiplier = false

    private var lastUnique = -1L
    private var lastAdvance = 0L
    private var sessionStartedAt = 0L
    private var initRetried = false

    private var statsUnique = 0L
    private var statsPosted = 0L
    private var statsAt = 0L
    private var sourceFps = 0.0
    private var outputFps = 0.0
    private var notificationTick = 0

    private var lowFpsTicks = 0
    private var highFpsTicks = 0
    private var multiplierChanges = 0
    private var lastPushErrorLog = 0L

    private val projectionCallback = object : MediaProjection.Callback() {
        override fun onStop() {
            mainHandler.post {
                if (!tornDown) {
                    LogBus.log("screen capture revoked by the system — stopping")
                    teardown()
                    stopSelf()
                }
            }
        }

        /** Android 14+ tells us when the captured content changed its size. */
        override fun onCapturedContentResize(width: Int, height: Int) {
            mainHandler.post {
                if (!tornDown && sessionActive) {
                    LogBus.log("captured content resized to ${width}x$height")
                    onRotationPossiblyChanged("capturedContent")
                }
            }
        }
    }

    private val watchdog = object : Runnable {
        override fun run() {
            if (!sessionActive || tornDown) return
            tick()
            if (sessionActive && !tornDown) mainHandler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        LogSaver.start(this)
        LogBus.log("CaptureService created")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0)
                val data = projectionData(intent)
                val cfg = SessionConfig.fromIntent(intent, config)
                if (data == null) {
                    fail("No screen-capture consent data")
                    return START_NOT_STICKY
                }
                if (sessionActive) teardown()
                startForegroundNow()
                startSession(resultCode, data, cfg)
            }
            ACTION_STOP -> {
                teardown()
                stopSelf()
            }
            ACTION_UPDATE_TARGET -> {
                val fps = intent.getIntExtra(EXTRA_TARGET_FPS, config.targetFps)
                applyTargetFps(fps)
            }
            ACTION_SET_BYPASS -> {
                applyBypass(intent.getBooleanExtra(EXTRA_BYPASS, false))
            }
            ACTION_SET_BUBBLE -> {
                val show = intent.getBooleanExtra(EXTRA_BUBBLE, true)
                config = config.copy(showBubble = show)
                overlay?.setBubbleVisible(show)
            }
            ACTION_SET_SCALE -> {
                val name = intent.getStringExtra(EXTRA_SCALE) ?: return START_NOT_STICKY
                applyCaptureScale(name)
            }
            ACTION_SET_MULTIPLIER -> {
                val value = intent.getIntExtra(EXTRA_MULTIPLIER, config.multiplier)
                applyMultiplier(value)
            }
            null -> if (!sessionActive) stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        onRotationPossiblyChanged("configuration")
    }

    override fun onDestroy() {
        teardown()
        super.onDestroy()
    }

    private fun startSession(resultCode: Int, data: Intent, cfg: SessionConfig) {
        if (!Settings.canDrawOverlays(this)) {
            fail("Overlay permission is missing — grant 'Display over other apps'")
            return
        }
        config = cfg
        initRetried = false
        sessionStartedAt = SystemClock.elapsedRealtime()
        multiplier = cfg.multiplier
        bypass = false
        starved = false
        suppressed = ForegroundState.mainActivityForeground
        multiplierChanges = 0
        lowFpsTicks = 0
        highFpsTicks = 0
        tornDown = false
        reconfiguring = false

        EngineState.update {
            it.copy(
                phase = SessionPhase.STARTING,
                running = true,
                starved = false,
                message = "Starting…",
                error = null,
                driver = if (cfg.driver.name == "PANVK") "PanVK" else "Stock Mali",
                targetFps = cfg.targetCap,
                multiplier = multiplier,
                fp16 = cfg.fp16,
            )
        }

        val manager = getSystemService(MediaProjectionManager::class.java)
        val projectionResult = runCatching { manager.getMediaProjection(resultCode, data) }
        val p = projectionResult.getOrNull()
        if (p == null) {
            val reason = projectionResult.exceptionOrNull()?.message ?: "no projection"
            fail("Screen-capture permission was refused ($reason) — start the session again")
            return
        }
        projection = p
        runCatching { p.registerCallback(projectionCallback, mainHandler) }
            .onFailure { LogBus.log("projection callback registration failed: ${it.message}") }

        val geometry = computeGeometry()
        captureW = geometry.first
        captureH = geometry.second
        currentRotation = geometry.third
        LogBus.log(
            "session start: display ${resources.displayMetrics.widthPixels}x${resources.displayMetrics.heightPixels}, " +
                "capture ${captureW}x$captureH (scale ${config.scale}), rotation=$currentRotation, dpi=$densityDpi",
        )
        LogBus.log(
            "session config: multiplier=$multiplier flow=${config.flow} performance=${config.performance} " +
                "hdr=${config.hdr} antiArtifacts=${config.antiArtifacts} fp16Requested=${config.fp16} " +
                "fpsMode=${config.fpsMode} target=${config.targetFps} targetCap=${config.targetCap} " +
                "adaptive=${config.adaptive} emaAlpha=${config.emaAlpha} outlierRatio=${config.outlierRatio} " +
                "vsyncSlack=${config.vsyncSlackMs} queueDepth=${config.queueDepth}",
        )
        LogBus.log(
            "session paths: shaderCache=${Paths.shaderCache(this)} dll=${Paths.dllCopy(this).absolutePath} " +
                "nativeLog=${Paths.nativeLog(this)}",
        )

        ioThread = HandlerThread("lsfg-capture").also { it.start() }
        ioHandler = Handler(ioThread!!.looper)
        val worker = ioHandler!!
        // initContext() creates the Vulkan device and loads the framegen shaders, which
        // blocks for hundreds of milliseconds — keep it off the main thread.
        worker.post {
            val rc = initEngine()
            mainHandler.post { onEngineStarted(rc) }
        }
    }

    /** Continues the session once the engine finished starting on the worker thread. */
    private fun onEngineStarted(rc: Int) {
        if (tornDown) return
        if (rc < 0) {
            fail("INIT FAILED rc=$rc — engine did not start. Try Stock driver or a different capture scale.")
            return
        }
        mirrorMode = rc > 0
        if (mirrorMode) {
            LogBus.log("frame generation is unavailable on this device — running in mirror mode (raw capture only)")
        }
        val handler = ioHandler
        if (handler == null) {
            fail("Capture thread disappeared while starting")
            return
        }
        val reader = createReader(captureW, captureH, handler)
        imageReader = reader
        val vd = createVirtualDisplay(reader)
        if (vd == null) {
            fail("Virtual display could not be created")
            return
        }
        virtualDisplay = vd
        // The native bypass flag outlives a session, so reset it on every start.
        bypass = false
        runCatching { NativeBridge.setBypass(false) }
        applyVsync()
        registerDisplayListener()

        sessionActive = true
        contextReady = true
        lastUnique = -1L
        lastAdvance = SystemClock.elapsedRealtime()
        statsUnique = 0L
        statsPosted = 0L
        statsAt = lastAdvance
        sourceFps = 0.0
        outputFps = 0.0
        notificationTick = 0

        overlay = OverlayController(this, config, overlayCallbacks).also {
            it.setSuppressed(suppressed)
            it.show(captureW, captureH)
        }
        mainHandler.postDelayed(watchdog, 1000L)
        updateNotification()
        EngineState.update {
            it.copy(
                phase = SessionPhase.RUNNING,
                running = true,
                starved = false,
                message = if (mirrorMode) "Mirror mode — frame generation unavailable" else "Running",
                error = null,
                captureW = captureW,
                captureH = captureH,
                captureScale = config.scale,
                multiplier = multiplier,
                targetFps = config.targetCap,
                fp16 = nativeFp16,
                bypass = false,
            )
        }
        LogBus.log(
            "capture running — switch to your game. multiplier=$multiplier target=${config.targetCap} " +
                "fp16=$nativeFp16 quality=${config.performance} flow=${config.flow} mirror=$mirrorMode",
        )
    }

    private fun tick() {
        val ownApp = ForegroundState.mainActivityForeground
        if (ownApp != suppressed) {
            suppressed = ownApp
            overlay?.setSuppressed(ownApp)
            LogBus.log(if (ownApp) "overlay hidden while the app is in front" else "overlay may present again")
        }

        if (!contextReady || reconfiguring) return

        val now = SystemClock.elapsedRealtime()
        val unique = runCatching { NativeBridge.getUniqueCaptureCount() }.getOrDefault(lastUnique)
        if (unique != lastUnique) {
            lastUnique = unique
            lastAdvance = now
            if (starved) {
                starved = false
                overlay?.setStarved(false)
                LogBus.log("captures resumed (unique=$unique)")
            }
        } else if (!starved && now - lastAdvance >= STARVE_TIMEOUT_MS) {
            starved = true
            overlay?.setStarved(true)
            LogBus.log("overlay hidden: no new frames for ${(now - lastAdvance) / 1000}s (game closed, paused or static)")
        }

        val generated = runCatching { NativeBridge.getGeneratedFrameCount() }.getOrDefault(0L)
        val posted = runCatching { NativeBridge.getPostedFrameCount() }.getOrDefault(0L)
        val latency = runCatching { NativeBridge.getAverageLatencyMs() }.getOrDefault(0.0)
        val queue = runCatching { NativeBridge.getAverageQueueMs() }.getOrDefault(0.0)

        if (statsAt > 0L) {
            val dt = max(1L, now - statsAt) / 1000.0
            val rawSource = (unique - statsUnique).coerceAtLeast(0L) / dt
            val rawOutput = (posted - statsPosted).coerceAtLeast(0L) / dt
            sourceFps = smooth(sourceFps, rawSource)
            outputFps = smooth(outputFps, rawOutput)
        }
        statsUnique = unique
        statsPosted = posted
        statsAt = now

        maybeAdaptMultiplier()

        EngineState.update {
            it.copy(
                phase = if (starved) SessionPhase.STARVED else SessionPhase.RUNNING,
                running = true,
                starved = starved,
                message = when {
                    starved -> "Waiting for new frames"
                    mirrorMode -> "Mirror mode — frame generation unavailable"
                    else -> "Running"
                },
                captureW = captureW,
                captureH = captureH,
                multiplier = multiplier,
                targetFps = config.targetCap,
                fp16 = nativeFp16,
                generated = generated,
                posted = posted,
                uniqueCaptures = unique,
                sourceFps = (sourceFps * 10).roundToLong() / 10.0,
                outputFps = (outputFps * 10).roundToLong() / 10.0,
                latencyMs = (latency * 10).roundToLong() / 10.0,
                queueMs = (queue * 10).roundToLong() / 10.0,
            )
        }

        notificationTick++
        if (notificationTick % 5 == 0) updateNotification()
        if (notificationTick % 30 == 0) logPacingSample()
    }

    /**
     * Writes a frame-pacing sample into the session log: how the engine's
     * posted frames are actually spaced, which is where stutter shows up.
     */
    private fun logPacingSample() {
        val intervals = LongArray(64)
        val count = runCatching { NativeBridge.getRecentPostIntervalsNs(intervals) }.getOrDefault(0)
        if (count <= 0) return
        val values = intervals.take(count).filter { it > 0 }.sorted()
        if (values.isEmpty()) return
        val p50 = values[values.size / 2] / 1_000_000.0
        val p95 = values[(values.size * 95 / 100).coerceAtMost(values.size - 1)] / 1_000_000.0
        val worst = values.last() / 1_000_000.0
        LogBus.log(
            "pacing sample: source=${"%.1f".format(sourceFps)} fps output=${"%.1f".format(outputFps)} fps " +
                "post-interval p50=${"%.2f".format(p50)} ms p95=${"%.2f".format(p95)} ms max=${"%.2f".format(worst)} ms " +
                "latency=${"%.1f".format(runCatching { NativeBridge.getAverageLatencyMs() }.getOrDefault(0.0))} ms " +
                "queue=${"%.1f".format(runCatching { NativeBridge.getAverageQueueMs() }.getOrDefault(0.0))} ms",
        )
    }

    private fun maybeAdaptMultiplier() {
        if (manualMultiplier) return
        if (config.fpsMode != FpsMode.TARGET || !config.adaptive || starved) return
        if (multiplierChanges >= 3 || sourceFps < 20.0) return
        val needed = ceil(config.targetFps / sourceFps).toInt().coerceIn(2, 6)
        if (needed > multiplier) {
            highFpsTicks = 0
            lowFpsTicks++
            if (lowFpsTicks >= 3) {
                lowFpsTicks = 0
                restartEngineAt(max(multiplier + 1, needed))
            }
        } else if (needed < multiplier) {
            lowFpsTicks = 0
            highFpsTicks++
            if (highFpsTicks >= 10) {
                highFpsTicks = 0
                restartEngineAt(max(2, multiplier - 1))
            }
        } else {
            lowFpsTicks = 0
            highFpsTicks = 0
        }
    }

    private fun restartEngineAt(newMultiplier: Int) {
        if (!contextReady || reconfiguring || !sessionActive) return
        val previous = multiplier
        if (newMultiplier == previous) return
        reconfiguring = true
        contextReady = false
        LogBus.log("adaptive: source ${"%.1f".format(sourceFps)} fps, multiplier $previous -> $newMultiplier (target ${config.targetFps})")
        multiplier = newMultiplier
        val worker = ioHandler
        if (worker == null) {
            reconfiguring = false
            fail("Capture thread disappeared while adapting the multiplier")
            return
        }
        worker.post {
            runCatching { NativeBridge.destroyContext() }
            val rc = initEngine()
            mainHandler.post { onEngineRestarted(rc) }
        }
    }

    private fun onEngineRestarted(rc: Int) {
        if (tornDown) return
        if (rc < 0) {
            reconfiguring = false
            fail("Engine restart failed (rc=$rc) while adapting the multiplier")
            return
        }
        mirrorMode = rc > 0
        multiplierChanges++
        overlay?.rebindOutputSurface()
        lastUnique = -1L
        lastAdvance = SystemClock.elapsedRealtime()
        starved = false
        overlay?.setStarved(false)
        contextReady = true
        reconfiguring = false
        updateNotification()
    }

    private fun initEngine(): Int {
        val cache = Paths.shaderCache(this)
        val fp16 = config.fp16 && runCatching { NativeBridge.isFramegenFp16Supported(cache) }.getOrDefault(false)
        nativeFp16 = fp16
        var rc = initContextCall(cache, fp16, config.performance, config.targetCap, config.queueDepth)
        // Only a negative code means the engine failed to start. A positive code is
        // kRenderLoopFramegenDisabled: the render loop runs, but without frame
        // generation (capture-only mirror mode).
        if (rc < 0 && !initRetried) {
            initRetried = true
            LogBus.log("engine init failed rc=$rc — retrying with the conservative profile")
            runCatching { NativeBridge.destroyContext() }
            rc = initContextCall(cache, fp16, true, 0, 1)
        }
        if (rc == 0) {
            LogBus.log(
                "engine initialised: ${captureW}x$captureH ${multiplier}x flow=${config.flow} " +
                    "fp16=$fp16 target=${config.targetCap} queue=${config.queueDepth}",
            )
        } else if (rc > 0) {
            LogBus.log("initContext rc=$rc — frame generation disabled, capture-only mirror mode")
        } else {
            LogBus.log("initContext failed rc=$rc")
        }
        return rc
    }

    private fun initContextCall(
        cache: String,
        fp16: Boolean,
        performance: Boolean,
        targetCap: Int,
        queueDepth: Int,
    ): Int = runCatching {
        NativeBridge.initContext(
            cache,
            captureW,
            captureH,
            multiplier,
            config.flow,
            performance,
            config.hdr,
            config.antiArtifacts,
            fp16,
            false, 0, 0, 0f, 0f, 0f, false,
            false, 0, 0f, 0f, 0f, 0f,
            false, 0, 0, 0f, 0f, 0f,
            targetCap,
            config.emaAlpha,
            config.outlierRatio,
            config.vsyncSlackMs,
            queueDepth,
        )
    }.getOrElse {
        LogBus.log("initContext threw: ${it.message}")
        -999
    }

    private fun createReader(width: Int, height: Int, handler: Handler): ImageReader {
        val reader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 4)
        reader.setOnImageAvailableListener({ r ->
            val image = runCatching { r.acquireLatestImage() }.getOrNull() ?: return@setOnImageAvailableListener
            try {
                if (contextReady) {
                    val buffer = image.hardwareBuffer
                    if (buffer != null) {
                        // The producer's timestamp keeps the engine's pacing in sync with
                        // the display; System.nanoTime() would drift against it.
                        runCatching { NativeBridge.pushFrame(buffer, image.timestamp) }
                            .onFailure { error ->
                                val now = SystemClock.elapsedRealtime()
                                if (now - lastPushErrorLog > 5000L) {
                                    lastPushErrorLog = now
                                    LogBus.log("pushFrame failed: ${error.message}")
                                }
                            }
                    }
                }
            } finally {
                image.close()
            }
        }, handler)
        return reader
    }

    private fun createVirtualDisplay(reader: ImageReader): VirtualDisplay? = runCatching {
        projection?.createVirtualDisplay(
            DISPLAY_NAME,
            captureW,
            captureH,
            densityDpi,
            VIRTUAL_DISPLAY_FLAGS,
            reader.surface,
            null,
            ioHandler,
        )
    }.onFailure { LogBus.log("createVirtualDisplay failed: ${it.message}") }.getOrNull()

    private fun computeGeometry(): Triple<Int, Int, Int> {
        val metrics = resources.displayMetrics
        val scale = config.scale.coerceIn(0.25f, 1f)
        val w = ((metrics.widthPixels * scale).toInt() / 2 * 2).coerceAtLeast(2)
        val h = ((metrics.heightPixels * scale).toInt() / 2 * 2).coerceAtLeast(2)
        densityDpi = metrics.densityDpi
        val rotation = runCatching {
            @Suppress("DEPRECATION")
            getSystemService(WindowManager::class.java).defaultDisplay.rotation
        }.getOrDefault(Surface.ROTATION_0)
        return Triple(w, h, rotation)
    }

    private fun applyVsync() {
        val rate = runCatching {
            @Suppress("DEPRECATION")
            getSystemService(WindowManager::class.java).defaultDisplay.refreshRate
        }.getOrDefault(0f)
        if (rate > 1f) {
            runCatching { NativeBridge.setVsyncPeriodNs((1_000_000_000.0 / rate).toLong()) }
        }
    }

    private fun onRotationPossiblyChanged(source: String) {
        if (!sessionActive || !contextReady || reconfiguring || tornDown) return
        val (w, h, rotation) = computeGeometry()
        if (w == captureW && h == captureH) {
            if (rotation != currentRotation) {
                currentRotation = rotation
                LogBus.log("rotation event ($source): rotation=${rotationName(rotation)}, capture size unchanged ${captureW}x$captureH")
            }
            return
        }
        reconfigure(w, h, rotation, source)
    }

    private fun reconfigure(newW: Int, newH: Int, rotation: Int, source: String) {
        if (reconfiguring || !sessionActive) return
        reconfiguring = true
        contextReady = false
        LogBus.log(
            "capture reconfigure ($source): ${rotationName(currentRotation)} -> ${rotationName(rotation)}, " +
                "capture ${captureW}x$captureH -> ${newW}x$newH (overlay stays attached)",
        )
        val oldVd = virtualDisplay
        runCatching { oldVd?.setSurface(null) }
        runCatching { imageReader?.close() }
        imageReader = null

        captureW = newW
        captureH = newH
        currentRotation = rotation
        val worker = ioHandler
        if (worker == null) {
            reconfiguring = false
            fail("Capture thread disappeared during reconfigure")
            return
        }
        worker.post {
            runCatching { NativeBridge.destroyContext() }
            val rc = initEngine()
            mainHandler.post { onReconfigured(rc, worker) }
        }
    }

    private fun onReconfigured(rc: Int, worker: Handler) {
        if (tornDown) return
        if (rc < 0) {
            reconfiguring = false
            fail("INIT FAILED rc=$rc after rotation — engine did not restart")
            return
        }
        mirrorMode = rc > 0
        val reader = createReader(captureW, captureH, worker)
        imageReader = reader
        val oldVd = virtualDisplay
        // Android 14+ allows only one VirtualDisplay per MediaProjection consent:
        // a second createVirtualDisplay() throws SecurityException, so the existing
        // display is resized instead.
        val mayRecreate = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        val newVd = if (mayRecreate) createVirtualDisplay(reader) else null
        if (newVd != null) {
            runCatching { oldVd?.release() }
            virtualDisplay = newVd
            LogBus.log("virtual display recreated at ${captureW}x$captureH")
        } else {
            runCatching {
                oldVd?.resize(captureW, captureH, densityDpi)
                oldVd?.setSurface(reader.surface)
            }.onFailure {
                LogBus.log("virtual display resize failed: ${it.message}")
            }
            virtualDisplay = oldVd
            LogBus.log(
                "virtual display resized at ${captureW}x$captureH" +
                    if (mayRecreate) "" else " (Android 14+ allows one virtual display per consent)",
            )
        }
        applyVsync()
        overlay?.onGeometryChanged(captureW, captureH)
        overlay?.rebindOutputSurface()
        lastUnique = -1L
        lastAdvance = SystemClock.elapsedRealtime()
        starved = false
        overlay?.setStarved(false)
        contextReady = true
        reconfiguring = false
        EngineState.update {
            it.copy(captureW = captureW, captureH = captureH, phase = SessionPhase.RUNNING, starved = false)
        }
        LogBus.log("reconfigure complete: ${captureW}x$captureH overlay still attached")
    }

    private fun registerDisplayListener() {
        if (displayListener != null) return
        val listener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) = Unit
            override fun onDisplayRemoved(displayId: Int) = Unit
            override fun onDisplayChanged(displayId: Int) {
                if (displayId == android.view.Display.DEFAULT_DISPLAY) {
                    onRotationPossiblyChanged("displayListener")
                }
            }
        }
        displayListener = listener
        runCatching {
            getSystemService(DisplayManager::class.java).registerDisplayListener(listener, mainHandler)
        }
    }

    private fun unregisterDisplayListener() {
        val listener = displayListener ?: return
        displayListener = null
        runCatching { getSystemService(DisplayManager::class.java).unregisterDisplayListener(listener) }
    }

    private fun teardown() {
        if (tornDown) return
        tornDown = true
        val durationMs = if (sessionStartedAt > 0L) SystemClock.elapsedRealtime() - sessionStartedAt else 0L
        val generated = runCatching { NativeBridge.getGeneratedFrameCount() }.getOrDefault(0L)
        val posted = runCatching { NativeBridge.getPostedFrameCount() }.getOrDefault(0L)
        val unique = runCatching { NativeBridge.getUniqueCaptureCount() }.getOrDefault(0L)
        val latency = runCatching { NativeBridge.getAverageLatencyMs() }.getOrDefault(0.0)
        sessionActive = false
        contextReady = false
        mainHandler.removeCallbacks(watchdog)
        unregisterDisplayListener()
        runCatching { virtualDisplay?.setSurface(null) }
        runCatching { virtualDisplay?.release() }
        virtualDisplay = null
        runCatching { imageReader?.close() }
        imageReader = null
        // Drop the overlay before the engine so the output surface is detached
        // while the context is still alive.
        overlay?.destroy()
        overlay = null
        val worker = ioHandler
        if (worker != null) {
            worker.post { runCatching { NativeBridge.destroyContext() } }
        } else {
            runCatching { NativeBridge.destroyContext() }
        }
        ioThread?.quitSafely()
        ioThread = null
        ioHandler = null
        runCatching { projection?.unregisterCallback(projectionCallback) }
        runCatching { projection?.stop() }
        projection = null
        starved = false
        suppressed = false
        lastUnique = -1L
        sessionStartedAt = 0L
        stopForeground(STOP_FOREGROUND_REMOVE)
        EngineState.reset()
        LogBus.log(
            "session summary: duration=${durationMs / 1000}s generated=$generated posted=$posted unique=$unique " +
                "source=${"%.1f".format(sourceFps)} fps output=${"%.1f".format(outputFps)} fps " +
                "avgLatency=${"%.1f".format(latency)} ms " +
                "multiplier=$multiplier fp16=$nativeFp16 capture=${captureW}x$captureH mirror=$mirrorMode",
        )
        LogBus.log("session stopped")
    }

    private fun fail(message: String) {
        LogBus.log(message)
        teardown()
        EngineState.update {
            it.copy(
                phase = SessionPhase.ERROR,
                running = false,
                starved = false,
                message = message,
                error = message,
            )
        }
        runCatching { stopSelf() }
    }

    private fun applyTargetFps(fps: Int) {
        val value = fps.coerceIn(15, 144)
        // Keep the user's adaptive choice untouched — only the target changes.
        config = config.copy(fpsMode = FpsMode.TARGET, targetFps = value)
        SettingsStore.get(this).update { it.copy(fpsMode = FpsMode.TARGET, targetFps = value) }
        EngineState.update { it.copy(targetFps = value) }
        runCatching {
            NativeBridge.setPacingParams(
                value,
                config.emaAlpha,
                config.outlierRatio,
                config.vsyncSlackMs,
                config.queueDepth,
            )
        }
        LogBus.log("target fps updated live -> $value")
        updateNotification()
    }

    /** Switches the capture resolution while the session keeps running. */
    private fun applyCaptureScale(name: String) {
        val scale = runCatching { CaptureScale.valueOf(name) }.getOrNull()
        if (scale == null) {
            LogBus.log("unknown capture scale '$name'")
            return
        }
        if (scale.value == config.scale) return
        config = config.copy(scale = scale.value)
        SettingsStore.get(this).update { it.copy(captureScale = scale) }
        EngineState.update { it.copy(captureScale = scale.value) }
        LogBus.log("capture scale changed live -> ${scale.label}")
        val (w, h, rotation) = computeGeometry()
        if (w == captureW && h == captureH) {
            LogBus.log("capture size unchanged at ${w}x$h — nothing to reconfigure")
            updateNotification()
            return
        }
        reconfigure(w, h, rotation, "capture scale change")
    }

    /** Sets the internal multiplier by hand and stops the adaptive logic from fighting it. */
    private fun applyMultiplier(value: Int) {
        val wanted = value.coerceIn(2, 8)
        if (wanted == multiplier) return
        manualMultiplier = true
        config = config.copy(multiplier = wanted)
        SettingsStore.get(this).update { it.copy(multiplier = wanted) }
        LogBus.log("multiplier changed live -> ${wanted}x")
        restartEngineAt(wanted)
    }

    private fun applyBypass(enabled: Boolean) {
        bypass = enabled
        overlay?.setBypass(enabled)
        EngineState.update { it.copy(bypass = enabled) }
        runCatching { NativeBridge.setBypass(enabled) }
        LogBus.log(if (enabled) "frame generation bypassed" else "frame generation resumed")
        updateNotification()
    }

    private val overlayCallbacks = object : OverlayController.Callbacks {
        override fun onStop() {
            teardown()
            stopSelf()
        }

        override fun onBypassChange(enabled: Boolean) {
            applyBypass(enabled)
        }

        override fun onTargetFps(fps: Int) {
            applyTargetFps(fps)
        }

        override fun onScaleChange(scale: CaptureScale) {
            applyCaptureScale(scale.name)
        }

        override fun onMultiplierChange(multiplier: Int) {
            applyMultiplier(multiplier)
        }

        override fun onOpenApp() {
            runCatching {
                startActivity(
                    Intent(this@CaptureService, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                )
            }
        }

        override fun onTouchBridgeFailure(message: String) {
            config = config.copy(touchBridge = false)
            SettingsStore.get(this@CaptureService).update { it.copy(touchBridge = false) }
            overlay?.setTouchBridgeEnabled(false)
            LogBus.log("touch bridge disabled: $message")
        }
    }

    private fun startForegroundNow() {
        val notification = buildNotification("Starting…")
        runCatching {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION,
            )
        }.onFailure {
            LogBus.log("startForeground failed: ${it.message}")
        }
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Frame generation", NotificationManager.IMPORTANCE_LOW).apply {
                setShowBadge(false)
            },
        )
    }

    private fun updateNotification() {
        if (!sessionActive) return
        val text = if (config.targetCap > 0) {
            "Running · target ${config.targetCap} fps · ${multiplier}× · ${captureW}×$captureH"
        } else {
            "Running · ${multiplier}× · ${captureW}×$captureH"
        }
        val prefix = if (mirrorMode) "Mirror mode · " else ""
        runCatching {
            getSystemService(NotificationManager::class.java).notify(
                NOTIFICATION_ID,
                buildNotification(if (bypass) "Bypass on · $prefix$text" else "$prefix$text"),
            )
        }
    }

    private fun buildNotification(text: String): Notification {
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, CaptureService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val openIntent = PendingIntent.getActivity(
            this,
            2,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val bypassIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, CaptureService::class.java)
                .setAction(ACTION_SET_BYPASS)
                .putExtra(EXTRA_BYPASS, !bypass),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_lsfg)
            .setContentTitle("LSFG Mali")
            .setContentText(text)
            .setContentIntent(openIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(
                Notification.Action.Builder(
                    android.graphics.drawable.Icon.createWithResource(
                        this,
                        if (bypass) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                    ),
                    if (bypass) "Resume" else "Bypass",
                    bypassIntent,
                ).build(),
            )
            .addAction(
                Notification.Action.Builder(
                    android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
                    "Stop",
                    stopIntent,
                ).build(),
            )
            .build()
    }

    private fun smooth(previous: Double, sample: Double): Double =
        if (previous <= 0.0) sample else previous * 0.7 + sample * 0.3

    private fun rotationName(rotation: Int): String = when (rotation) {
        Surface.ROTATION_0 -> "portrait"
        Surface.ROTATION_90 -> "landscape"
        Surface.ROTATION_180 -> "reverse-portrait"
        Surface.ROTATION_270 -> "reverse-landscape"
        else -> rotation.toString()
    }

    @Suppress("DEPRECATION")
    private fun projectionData(intent: Intent): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_RESULT_DATA) as? Intent
        }

    companion object {
        private const val CHANNEL_ID = "lsfg_session"
        private const val NOTIFICATION_ID = 42
        private const val DISPLAY_NAME = "LSFG-Mali"
        private const val VIRTUAL_DISPLAY_FLAGS = 16
        private const val STARVE_TIMEOUT_MS = 6000L

        const val ACTION_START = "com.syed.lsfgmali.action.START"
        const val ACTION_STOP = "com.syed.lsfgmali.action.STOP"
        const val ACTION_UPDATE_TARGET = "com.syed.lsfgmali.action.UPDATE_TARGET"
        const val ACTION_SET_BYPASS = "com.syed.lsfgmali.action.SET_BYPASS"
        const val ACTION_SET_BUBBLE = "com.syed.lsfgmali.action.SET_BUBBLE"
        const val ACTION_SET_SCALE = "com.syed.lsfgmali.action.SET_SCALE"
        const val ACTION_SET_MULTIPLIER = "com.syed.lsfgmali.action.SET_MULTIPLIER"

        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        const val EXTRA_TARGET_FPS = "target_fps"
        const val EXTRA_BYPASS = "bypass"
        const val EXTRA_BUBBLE = "bubble"
        const val EXTRA_SCALE = "capture_scale"
        const val EXTRA_MULTIPLIER = "multiplier"

        fun start(context: Context, resultCode: Int, data: Intent, config: SessionConfig) {
            val intent = Intent(context, CaptureService::class.java).setAction(ACTION_START)
            intent.putExtra(EXTRA_RESULT_CODE, resultCode)
            intent.putExtra(EXTRA_RESULT_DATA, data)
            config.writeTo(intent)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, CaptureService::class.java).setAction(ACTION_STOP))
        }

        fun setTargetFps(context: Context, fps: Int) {
            context.startService(
                Intent(context, CaptureService::class.java)
                    .setAction(ACTION_UPDATE_TARGET)
                    .putExtra(EXTRA_TARGET_FPS, fps),
            )
        }

        fun setBypass(context: Context, bypass: Boolean) {
            runCatching {
                context.startService(
                    Intent(context, CaptureService::class.java)
                        .setAction(ACTION_SET_BYPASS)
                        .putExtra(EXTRA_BYPASS, bypass),
                )
            }.onFailure { LogBus.log("could not toggle bypass: ${it.message}") }
        }

        fun setBubble(context: Context, show: Boolean) {
            context.startService(
                Intent(context, CaptureService::class.java)
                    .setAction(ACTION_SET_BUBBLE)
                    .putExtra(EXTRA_BUBBLE, show),
            )
        }

        fun setCaptureScale(context: Context, scale: CaptureScale) {
            runCatching {
                context.startService(
                    Intent(context, CaptureService::class.java)
                        .setAction(ACTION_SET_SCALE)
                        .putExtra(EXTRA_SCALE, scale.name),
                )
            }.onFailure { LogBus.log("could not change the capture scale: ${it.message}") }
        }

        fun setMultiplier(context: Context, multiplier: Int) {
            runCatching {
                context.startService(
                    Intent(context, CaptureService::class.java)
                        .setAction(ACTION_SET_MULTIPLIER)
                        .putExtra(EXTRA_MULTIPLIER, multiplier),
                )
            }.onFailure { LogBus.log("could not change the multiplier: ${it.message}") }
        }
    }
}

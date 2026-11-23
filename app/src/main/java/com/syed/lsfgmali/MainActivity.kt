package com.syed.lsfgmali

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.syed.lsfgmali.data.ForegroundState
import com.syed.lsfgmali.data.LogBus
import com.syed.lsfgmali.data.LogSaver
import com.syed.lsfgmali.data.SettingsStore
import com.syed.lsfgmali.service.CaptureService
import com.syed.lsfgmali.service.SessionConfig
import com.syed.lsfgmali.ui.LsfgApp
import com.syed.lsfgmali.ui.theme.LsfgTheme

class MainActivity : ComponentActivity() {

    private enum class Pending { NONE, OVERLAY, NOTIFICATION }

    private var pending = Pending.NONE

    private val projectionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val settings = SettingsStore.get(this).settings.value
            val config = SessionConfig.fromSettings(settings, settings.fp16Auto)
            runCatching { CaptureService.start(this, result.resultCode, result.data!!, config) }
                .onFailure {
                    LogBus.log("could not start capture: ${it.message}")
                    toast("Could not start capture: ${it.message}")
                }
            moveTaskToBack(true)
        } else {
            LogBus.log("screen capture consent denied")
            toast("Screen capture was not granted")
        }
    }

    private val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (pending == Pending.OVERLAY) {
            pending = Pending.NONE
            if (Settings.canDrawOverlays(this)) startFlow() else toast("Overlay permission is still denied")
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (pending == Pending.NOTIFICATION) {
            pending = Pending.NONE
            if (granted) launchProjection() else toast("Notifications are required for the capture service")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LsfgTheme {
                LsfgApp(
                    onStart = ::startFlow,
                    onStop = { CaptureService.stop(this) },
                    onShareLog = { LogSaver.shareLatest(this) },
                    onOpenAccessibility = {
                        openSettings(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    onOpenOverlaySettings = { requestOverlay() },
                    onOpenNotificationSettings = { openNotificationSettings() },
                    onOpenBatterySettings = {
                        openSettings(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ForegroundState.mainActivityForeground = true
    }

    override fun onPause() {
        ForegroundState.mainActivityForeground = false
        super.onPause()
    }

    private fun startFlow() {
        val settings = SettingsStore.get(this).settings.value
        if (!settings.dllReady) {
            toast("Import your Lossless.dll first")
            return
        }
        if (!Settings.canDrawOverlays(this)) {
            pending = Pending.OVERLAY
            requestOverlay()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pending = Pending.NOTIFICATION
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        launchProjection()
    }

    private fun launchProjection() {
        val manager = getSystemService(MediaProjectionManager::class.java)
        runCatching { projectionLauncher.launch(manager.createScreenCaptureIntent()) }
            .onFailure {
                LogBus.log("screen capture intent failed: ${it.message}")
                toast("Screen capture is unavailable on this build")
            }
    }

    private fun requestOverlay() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        )
        runCatching { overlayPermissionLauncher.launch(intent) }
            .onFailure { openSettings(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) }
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        openSettings(intent)
    }

    private fun openSettings(intent: Intent) {
        runCatching { startActivity(intent) }
            .onFailure { toast("This ROM does not expose that settings page") }
    }

    private fun toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}

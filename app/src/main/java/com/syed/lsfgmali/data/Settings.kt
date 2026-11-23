package com.syed.lsfgmali.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class DriverMode { PANVK, STOCK }
enum class CaptureScale(val value: Float, val label: String) {
    FULL(1f, "100%"),
    BALANCED(0.75f, "75%"),
    PERFORMANCE(0.5f, "50%"),
}
enum class FpsMode { FIXED, TARGET }

data class Settings(
    val driver: DriverMode = DriverMode.PANVK,
    val captureScale: CaptureScale = CaptureScale.FULL,
    val flowScale: Float = 0.70f,
    val fpsMode: FpsMode = FpsMode.FIXED,
    val multiplier: Int = 2,
    val targetFps: Int = 60,
    val adaptive: Boolean = true,
    val performance: Boolean = true,
    val hdr: Boolean = false,
    val antiArtifacts: Boolean = true,
    val touchBridge: Boolean = false,
    val showBubble: Boolean = true,
    val autoDeviceProfile: Boolean = true,
    val deviceProfileApplied: Boolean = false,
    val fp16Auto: Boolean = true,
    val dllName: String? = null,
    val dllReady: Boolean = false,
    val dllSha: String? = null,
    val dllVersion: String? = null,
    val dllFp16: Boolean = false,
    val bubbleX: Int = -1,
    val bubbleY: Int = 300,
)

/**
 * SharedPreferences-backed settings, exposed as a StateFlow for Compose.
 */
class SettingsStore private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("settings_v2", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private fun read(): Settings {
        val def = Settings()
        return Settings(
            driver = enumValue(prefs.getString("driver", def.driver.name), def.driver),
            captureScale = enumValue(prefs.getString("capture_scale", def.captureScale.name), def.captureScale),
            flowScale = prefs.getFloat("flow", def.flowScale),
            fpsMode = enumValue(prefs.getString("fps_mode", def.fpsMode.name), def.fpsMode),
            multiplier = prefs.getInt("multiplier", def.multiplier).coerceIn(2, 8),
            targetFps = prefs.getInt("target_fps", def.targetFps).coerceIn(15, 144),
            adaptive = prefs.getBoolean("adaptive", def.adaptive),
            performance = prefs.getBoolean("performance", def.performance),
            hdr = prefs.getBoolean("hdr", def.hdr),
            antiArtifacts = prefs.getBoolean("anti_artifacts", def.antiArtifacts),
            touchBridge = prefs.getBoolean("touch_bridge", def.touchBridge),
            showBubble = prefs.getBoolean("show_bubble", def.showBubble),
            autoDeviceProfile = prefs.getBoolean("auto_device_profile", def.autoDeviceProfile),
            deviceProfileApplied = prefs.getBoolean("device_profile_applied", def.deviceProfileApplied),
            fp16Auto = prefs.getBoolean("fp16_auto", def.fp16Auto),
            dllName = prefs.getString("dll_name", null),
            dllReady = prefs.getBoolean("dll_ready", false),
            dllSha = prefs.getString("dll_sha", null),
            dllVersion = prefs.getString("dll_version", null),
            dllFp16 = prefs.getBoolean("dll_fp16", false),
            bubbleX = prefs.getInt("bubble_x", def.bubbleX),
            bubbleY = prefs.getInt("bubble_y", def.bubbleY),
        )
    }

    @Synchronized
    fun update(transform: (Settings) -> Settings): Settings {
        val next = transform(_settings.value)
        prefs.edit()
            .putString("driver", next.driver.name)
            .putString("capture_scale", next.captureScale.name)
            .putFloat("flow", next.flowScale)
            .putString("fps_mode", next.fpsMode.name)
            .putInt("multiplier", next.multiplier.coerceIn(2, 8))
            .putInt("target_fps", next.targetFps.coerceIn(15, 144))
            .putBoolean("adaptive", next.adaptive)
            .putBoolean("performance", next.performance)
            .putBoolean("hdr", next.hdr)
            .putBoolean("anti_artifacts", next.antiArtifacts)
            .putBoolean("touch_bridge", next.touchBridge)
            .putBoolean("show_bubble", next.showBubble)
            .putBoolean("auto_device_profile", next.autoDeviceProfile)
            .putBoolean("device_profile_applied", next.deviceProfileApplied)
            .putBoolean("fp16_auto", next.fp16Auto)
            .putString("dll_name", next.dllName)
            .putBoolean("dll_ready", next.dllReady)
            .putString("dll_sha", next.dllSha)
            .putString("dll_version", next.dllVersion)
            .putBoolean("dll_fp16", next.dllFp16)
            .putInt("bubble_x", next.bubbleX)
            .putInt("bubble_y", next.bubbleY)
            .apply()
        _settings.value = next
        return next
    }

    fun setDll(name: String?, sha: String?, ready: Boolean, version: String? = null, fp16: Boolean = false) = update {
        it.copy(
            dllName = name,
            dllSha = sha,
            dllReady = ready,
            dllVersion = version,
            dllFp16 = fp16,
        )
    }

    fun applyG85Profile() = update {
        it.copy(
            captureScale = CaptureScale.PERFORMANCE,
            flowScale = 0.50f,
            performance = true,
            hdr = false,
            antiArtifacts = true,
            fpsMode = FpsMode.TARGET,
            targetFps = 60,
            multiplier = 2,
            adaptive = true,
        )
    }

    private inline fun <reified T : Enum<T>> enumValue(name: String?, fallback: T): T =
        runCatching { enumValueOf<T>(name ?: fallback.name) }.getOrDefault(fallback)

    companion object {
        @Volatile private var instance: SettingsStore? = null
        fun get(context: Context): SettingsStore =
            instance ?: synchronized(this) {
                instance ?: SettingsStore(context).also { instance = it }
            }
    }
}

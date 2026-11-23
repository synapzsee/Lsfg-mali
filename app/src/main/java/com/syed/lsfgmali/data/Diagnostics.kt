package com.syed.lsfgmali.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import com.syed.lsfgmali.BuildConfig
import java.io.File
import com.lsfg.android.session.NativeBridge

/**
 * Builds a one-shot snapshot of everything that matters for a bug report:
 * app state, driver, DLL, shader cache and the live engine counters.
 */
object Diagnostics {
    fun report(context: Context): String {
        val store = SettingsStore.get(context)
        val settings = store.settings.value
        val device = DeviceInfo.detect(context)
        val engine = EngineState.snapshot.value
        val cache = Paths.shaderCacheDir(context)
        val cacheFiles = cache.listFiles()?.count { it.isFile } ?: 0
        val fp16Files = File(cache, "fp16").listFiles()?.count { it.isFile } ?: 0
        val fp32Files = File(cache, "fp32").listFiles()?.count { it.isFile } ?: 0
        val fp16 = runCatching {
            NativeBridge.isFramegenFp16Supported(cache.absolutePath)
        }.getOrDefault(false)
        val engineVersion = runCatching { NativeBridge.nativeVersion() }.getOrDefault("unavailable")
        val logFile = LogSaver.latest(context)?.absolutePath ?: "none yet"

        return buildString {
            appendLine("LSFG Mali diagnostics — ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("generated: ${java.util.Date()}")
            appendLine()
            appendLine("[device]")
            appendLine("  ${device.manufacturer} ${device.model}")
            appendLine("  soc: ${device.soc.ifBlank { "unknown" }}")
            appendLine("  gpu: ${device.gpuShort}")
            appendLine("  android: ${device.androidRelease} (SDK ${device.sdkInt}, API ${Build.VERSION.SDK_INT})")
            appendLine("  kernel: ${device.kernel}")
            appendLine("  abi: ${device.abi}")
            appendLine()
            appendLine("[engine]")
            appendLine("  version: $engineVersion")
            appendLine("  phase: ${engine.phase} — ${engine.message}")
            appendLine("  driver: ${settings.driver}")
            appendLine("  icd: ${Paths.panVkIcdJson(context).absolutePath}")
            appendLine("  generated=${engine.generated} posted=${engine.posted} unique=${engine.uniqueCaptures}")
            appendLine("  source=${"%.1f".format(engine.sourceFps)} fps output=${"%.1f".format(engine.outputFps)} fps")
            appendLine("  latency=${"%.1f".format(engine.latencyMs)} ms queue=${"%.1f".format(engine.queueMs)} ms")
            appendLine("  capture=${engine.captureW}x${engine.captureH} multiplier=${engine.multiplier} target=${engine.targetFps}")
            appendLine("  fp16: ${engine.fp16}")
            appendLine()
            appendLine("[dll]")
            appendLine("  name: ${settings.dllName ?: "not imported"}")
            appendLine("  version: ${settings.dllVersion ?: "unknown"}")
            appendLine("  sha256: ${settings.dllSha ?: "-"}")
            appendLine("  extracted: ${settings.dllReady}")
            appendLine("  fp16 shader set: ${settings.dllFp16} (engine probe: $fp16)")
            appendLine()
            appendLine("[shader cache]")
            appendLine("  path: ${cache.absolutePath}")
            appendLine("  top-level=$cacheFiles fp16=$fp16Files fp32=$fp32Files")
            appendLine()
            appendLine("[settings]")
            appendLine("  captureScale=${settings.captureScale} flowScale=${settings.flowScale}")
            appendLine("  fpsMode=${settings.fpsMode} target=${settings.targetFps} multiplier=${settings.multiplier}")
            appendLine("  adaptive=${settings.adaptive} performance=${settings.performance} hdr=${settings.hdr}")
            appendLine("  antiArtifacts=${settings.antiArtifacts} fp16Auto=${settings.fp16Auto}")
            appendLine("  touchBridge=${settings.touchBridge} showBubble=${settings.showBubble}")
            appendLine("  autoDeviceProfile=${settings.autoDeviceProfile} deviceProfileApplied=${settings.deviceProfileApplied}")
            appendLine()
            appendLine("[permissions]")
            appendLine("  overlay: ${Settings.canDrawOverlays(context)}")
            appendLine("  accessibility touch bridge: ${com.syed.lsfgmali.service.LsfgTouchService.isReady}")
            appendLine()
            appendLine("[files]")
            appendLine("  session log: $logFile")
            appendLine("  engine native log: ${Paths.nativeLog(context)}")
            appendLine("  crash file: ${Paths.crashFile(context)}")
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        manager.setPrimaryClip(ClipData.newPlainText("LSFG Mali diagnostics", text))
    }
}

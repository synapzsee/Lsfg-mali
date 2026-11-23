package com.lsfg.android.session

import android.hardware.HardwareBuffer
import android.view.Surface

/**
 * JNI entry points into the prebuilt liblsfg-android.so (lsfg-vk, MIT).
 *
 * The native library is name-matched to this exact package/class, so the
 * package and class name must never change.
 */
object NativeBridge {
    init {
        System.loadLibrary("lsfg-android")
    }

    external fun nativeVersion(): String
    external fun initCrashReporter(crashPath: String, logPath: String)
    external fun extractShaders(dllPath: String, dllSha256: String, cacheDir: String): Int
    external fun probeShaders(cacheDir: String): Int

    external fun initContext(
        cacheDir: String,
        width: Int,
        height: Int,
        multiplier: Int,
        flowScale: Float,
        performance: Boolean,
        hdr: Boolean,
        antiArtifacts: Boolean,
        framegenFp16: Boolean,
        npuPostProcessing: Boolean,
        npuPreset: Int,
        npuUpscaleFactor: Int,
        npuAmount: Float,
        npuRadius: Float,
        npuThreshold: Float,
        npuFp16: Boolean,
        cpuPostProcessing: Boolean,
        cpuPreset: Int,
        cpuStrength: Float,
        cpuSaturation: Float,
        cpuVibrance: Float,
        cpuVignette: Float,
        gpuPostProcessing: Boolean,
        gpuStage: Int,
        gpuMethod: Int,
        gpuUpscaleFactor: Float,
        gpuSharpness: Float,
        gpuStrength: Float,
        targetFpsCap: Int,
        emaAlpha: Float,
        outlierRatio: Float,
        vsyncSlackMs: Float,
        queueDepth: Int,
    ): Int

    external fun isNpuAvailable(): Boolean
    external fun isFramegenFp16Supported(cacheDir: String): Boolean
    external fun getNpuSummary(): String

    external fun setOutputSurface(surface: Surface?, w: Int, h: Int)
    external fun pushFrame(hardwareBuffer: HardwareBuffer, timestampNs: Long)

    external fun getGeneratedFrameCount(): Long
    external fun getPostedFrameCount(): Long
    external fun getUniqueCaptureCount(): Long
    external fun getAverageQueueMs(): Double
    external fun getAverageLatencyMs(): Double
    external fun getRecentPostIntervalsNs(outIntervalsNs: LongArray): Int
    external fun getProfileWindowNs(out: LongArray): Int

    external fun setBypass(bypass: Boolean)
    external fun setAntiArtifacts(enabled: Boolean)
    external fun setVsyncPeriodNs(periodNs: Long)

    external fun setPacingParams(
        targetFpsCap: Int,
        emaAlpha: Float,
        outlierRatio: Float,
        vsyncSlackMs: Float,
        queueDepth: Int,
    )

    external fun setShizukuTimingEnabled(enabled: Boolean)
    external fun reportShizukuTiming(timestampNs: Long, frameTimeNs: Long, pacingJitterNs: Long)
    external fun destroyContext()
}

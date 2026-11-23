package com.syed.lsfgmali.service

import android.content.Intent
import com.syed.lsfgmali.data.CaptureScale
import com.syed.lsfgmali.data.DriverMode
import com.syed.lsfgmali.data.FpsMode
import com.syed.lsfgmali.data.Settings

data class SessionConfig(
    val driver: DriverMode = DriverMode.PANVK,
    val scale: Float = 1f,
    val flow: Float = 0.7f,
    val fpsMode: FpsMode = FpsMode.FIXED,
    val multiplier: Int = 2,
    val targetFps: Int = 60,
    val adaptive: Boolean = true,
    val performance: Boolean = true,
    val hdr: Boolean = false,
    val antiArtifacts: Boolean = true,
    val fp16: Boolean = true,
    val touchBridge: Boolean = false,
    val showBubble: Boolean = true,
    val queueDepth: Int = 3,
    val emaAlpha: Float = 0.125f,
    val outlierRatio: Float = 4f,
    val vsyncSlackMs: Float = 2f,
) {
    val targetCap: Int get() = if (fpsMode == FpsMode.TARGET) targetFps else 0

    fun writeTo(intent: Intent): Intent = intent.apply {
        putExtra("driver", driver.name)
        putExtra("scale", scale)
        putExtra("flow", flow)
        putExtra("fps_mode", fpsMode.name)
        putExtra("multiplier", multiplier)
        putExtra("target_fps", targetFps)
        putExtra("adaptive", adaptive)
        putExtra("performance", performance)
        putExtra("hdr", hdr)
        putExtra("anti", antiArtifacts)
        putExtra("fp16", fp16)
        putExtra("touch_bridge", touchBridge)
        putExtra("show_bubble", showBubble)
        putExtra("queue_depth", queueDepth)
        putExtra("ema_alpha", emaAlpha)
        putExtra("outlier_ratio", outlierRatio)
        putExtra("vsync_slack", vsyncSlackMs)
    }

    companion object {
        fun fromIntent(intent: Intent, fallback: SessionConfig): SessionConfig = SessionConfig(
            driver = runCatching { DriverMode.valueOf(intent.getStringExtra("driver")!!) }.getOrDefault(fallback.driver),
            scale = intent.getFloatExtra("scale", fallback.scale),
            flow = intent.getFloatExtra("flow", fallback.flow),
            fpsMode = runCatching { FpsMode.valueOf(intent.getStringExtra("fps_mode")!!) }.getOrDefault(fallback.fpsMode),
            multiplier = intent.getIntExtra("multiplier", fallback.multiplier).coerceIn(2, 8),
            targetFps = intent.getIntExtra("target_fps", fallback.targetFps).coerceIn(15, 144),
            adaptive = intent.getBooleanExtra("adaptive", fallback.adaptive),
            performance = intent.getBooleanExtra("performance", fallback.performance),
            hdr = intent.getBooleanExtra("hdr", fallback.hdr),
            antiArtifacts = intent.getBooleanExtra("anti", fallback.antiArtifacts),
            fp16 = intent.getBooleanExtra("fp16", fallback.fp16),
            touchBridge = intent.getBooleanExtra("touch_bridge", fallback.touchBridge),
            showBubble = intent.getBooleanExtra("show_bubble", fallback.showBubble),
            queueDepth = intent.getIntExtra("queue_depth", fallback.queueDepth).coerceIn(2, 6),
            emaAlpha = intent.getFloatExtra("ema_alpha", fallback.emaAlpha),
            outlierRatio = intent.getFloatExtra("outlier_ratio", fallback.outlierRatio),
            vsyncSlackMs = intent.getFloatExtra("vsync_slack", fallback.vsyncSlackMs),
        )

        fun fromSettings(s: Settings, fp16: Boolean): SessionConfig = SessionConfig(
            driver = s.driver,
            scale = s.captureScale.value,
            flow = s.flowScale,
            fpsMode = s.fpsMode,
            multiplier = s.multiplier,
            targetFps = s.targetFps,
            adaptive = s.adaptive,
            performance = s.performance,
            hdr = s.hdr,
            antiArtifacts = s.antiArtifacts,
            fp16 = fp16 && s.fp16Auto,
            touchBridge = s.touchBridge,
            showBubble = s.showBubble,
            queueDepth = if (s.captureScale == CaptureScale.PERFORMANCE) 2 else 3,
        )
    }
}

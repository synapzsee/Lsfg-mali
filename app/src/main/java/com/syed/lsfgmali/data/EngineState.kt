package com.syed.lsfgmali.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class SessionPhase { IDLE, STARTING, RUNNING, STARVED, STOPPING, ERROR }

data class EngineSnapshot(
    val phase: SessionPhase = SessionPhase.IDLE,
    val running: Boolean = false,
    val starved: Boolean = false,
    val message: String = "Idle",
    val error: String? = null,
    val driver: String = "PanVK",
    val captureW: Int = 0,
    val captureH: Int = 0,
    val multiplier: Int = 2,
    val targetFps: Int = 0,
    val fp16: Boolean = false,
    val generated: Long = 0,
    val posted: Long = 0,
    val uniqueCaptures: Long = 0,
    val sourceFps: Double = 0.0,
    val outputFps: Double = 0.0,
    val latencyMs: Double = 0.0,
    val queueMs: Double = 0.0,
) {
    val resolution: String get() = if (captureW > 0) "${captureW}×$captureH" else "—"
    val modeLabel: String
        get() = when {
            targetFps > 0 -> "Target $targetFps fps · ${multiplier}× engine"
            multiplier > 0 -> "Fixed ${multiplier}×"
            else -> "—"
        }
}

object EngineState {
    private val _snapshot = MutableStateFlow(EngineSnapshot())
    val snapshot: StateFlow<EngineSnapshot> = _snapshot.asStateFlow()

    fun update(transform: (EngineSnapshot) -> EngineSnapshot) = _snapshot.update(transform)

    fun set(transform: (EngineSnapshot) -> EngineSnapshot) = _snapshot.update(transform)

    fun reset() = _snapshot.update { EngineSnapshot(driver = it.driver) }
}

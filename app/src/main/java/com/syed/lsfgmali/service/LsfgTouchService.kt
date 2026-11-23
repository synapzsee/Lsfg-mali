package com.syed.lsfgmali.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import com.syed.lsfgmali.data.LogBus

/**
 * Optional bridge for ROMs/OEMs that drop touches targeted at overlay windows.
 * It only replays gestures that originate from our own pass-through layer.
 *
 * Only one accessibility gesture can run at a time, so the newest gesture is
 * kept pending and dispatched as soon as the running one finishes. That keeps
 * drag input responsive instead of dropping every second move.
 */
class LsfgTouchService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        LogBus.log("Touch bridge connected — touches will be replayed to the game")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (instance === this) instance = null
        pending = null
        inFlight = false
        LogBus.log("Touch bridge disconnected")
        super.onDestroy()
    }

    /** Timestamp of the last gesture the system actually ran. */
    @Volatile
    var lastGestureAt: Long = 0L
        private set

    fun send(gesture: GestureDescription): Boolean = runCatching {
        pending = gesture
        if (inFlight) true else dispatchPending()
    }.getOrDefault(false)

    private fun dispatchPending(): Boolean {
        val gesture = pending ?: return true
        inFlight = true
        val accepted = runCatching {
            dispatchGesture(
                gesture,
                object : GestureResultCallback() {
                    override fun onCompleted(description: GestureDescription?) {
                        inFlight = false
                        lastGestureAt = SystemClock.uptimeMillis()
                        dispatchPending()
                    }

                    override fun onCancelled(description: GestureDescription?) {
                        inFlight = false
                        lastGestureAt = SystemClock.uptimeMillis()
                        dispatchPending()
                    }
                },
                null,
            )
        }.getOrDefault(false)
        if (!accepted) {
            inFlight = false
            pending = null
        }
        return accepted
    }

    companion object {
        @Volatile
        var instance: LsfgTouchService? = null
            private set

        @Volatile
        private var pending: GestureDescription? = null

        @Volatile
        private var inFlight = false

        val isReady: Boolean get() = instance != null
    }
}

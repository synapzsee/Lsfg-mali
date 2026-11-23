package com.syed.lsfgmali.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.view.accessibility.AccessibilityEvent
import com.syed.lsfgmali.data.LogBus

/**
 * Optional bridge for ROMs/OEMs that drop touches targeted at overlay windows.
 * It only replays gestures that originate from our own pass-through layer.
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
        LogBus.log("Touch bridge disconnected")
        super.onDestroy()
    }

    fun send(gesture: GestureDescription): Boolean =
        runCatching { dispatchGesture(gesture, null, null) }.getOrDefault(false)

    companion object {
        @Volatile
        var instance: LsfgTouchService? = null
            private set

        val isReady: Boolean get() = instance != null
    }
}

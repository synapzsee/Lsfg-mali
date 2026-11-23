package com.syed.lsfgmali.service

import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PointF
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import com.syed.lsfgmali.data.LogBus
import kotlin.math.max

/**
 * Replays the pointer stream from the transparent overlay layer as accessibility gestures.
 * This is only used when the user explicitly enables the touch bridge.
 */
class TouchForwarder(private val onFailure: (String) -> Unit) {
    private val lastPoint = HashMap<Int, PointF>()
    private val active = HashMap<Int, GestureDescription.StrokeDescription>()
    private var lastDispatch = 0L
    private var disabled = false

    fun reset() {
        lastPoint.clear()
        active.clear()
    }

    fun onTouch(view: View, event: MotionEvent): Boolean {
        if (disabled) return false
        val service = LsfgTouchService.instance
        if (service == null) {
            disabled = true
            onFailure("Touch bridge is enabled but the accessibility service is off")
            return false
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                reset()
                addPointer(0, event.x, event.y)
                dispatch()
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                addPointer(event.getPointerId(index), event.getX(index), event.getY(index))
                dispatch()
            }
            MotionEvent.ACTION_MOVE -> {
                if (SystemClock.uptimeMillis() - lastDispatch < 8) return true
                var changed = false
                for (i in 0 until event.pointerCount) {
                    changed = continuePointer(
                        event.getPointerId(i),
                        event.getX(i),
                        event.getY(i),
                        willContinue = true,
                    ) || changed
                }
                if (changed) dispatch()
            }
            MotionEvent.ACTION_POINTER_UP, MotionEvent.ACTION_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                finishPointer(id, event.getX(index), event.getY(index))
                dispatch()
            }
            MotionEvent.ACTION_CANCEL -> {
                disabled = true
                reset()
                onFailure("Touch bridge was cancelled by the system")
            }
        }
        return true
    }

    private fun addPointer(id: Int, x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 1, true)
        active[id] = stroke
        lastPoint[id] = PointF(x, y)
    }

    private fun continuePointer(id: Int, x: Float, y: Float, willContinue: Boolean): Boolean {
        val previous = active[id] ?: return false
        val last = lastPoint[id] ?: return false
        val path = Path().apply {
            moveTo(last.x, last.y)
            lineTo(x, y)
        }
        val next = runCatching {
            previous.continueStroke(path, 0, max(1, (SystemClock.uptimeMillis() - lastDispatch).toInt()), willContinue)
        }.getOrNull() ?: return false
        active[id] = next
        lastPoint[id] = PointF(x, y)
        return true
    }

    private fun finishPointer(id: Int, x: Float, y: Float) {
        val previous = active.remove(id)
        val last = lastPoint.remove(id)
        if (previous == null || last == null) return
        val path = Path().apply {
            moveTo(last.x, last.y)
            lineTo(x, y)
        }
        val final = runCatching { previous.continueStroke(path, 0, 1, false) }.getOrNull() ?: return
        active[id] = final
    }

    private fun dispatch() {
        if (active.isEmpty()) return
        val service = LsfgTouchService.instance ?: return
        val builder = GestureDescription.Builder()
        active.values.forEach { builder.addStroke(it) }
        lastDispatch = SystemClock.uptimeMillis()
        if (!service.send(builder.build())) {
            disabled = true
            reset()
            LogBus.log("Touch bridge dispatch failed — falling back to native pass-through")
            onFailure("Touch bridge failed; disable it in Settings")
        }
        active.keys.toList().forEach { id ->
            if (active[id]?.willContinue() != true) active.remove(id)
        }
    }
}

package com.syed.lsfgmali.service

import android.view.SurfaceControl
import android.view.SurfaceView
import android.view.View
import com.syed.lsfgmali.data.LogBus

/**
 * Best-effort exclusion of our overlay surfaces from screen capture.
 * Without this the generator can see its own output and feed back on itself.
 */
internal object SkipScreenshot {
    private val getRootSurfaceControl = runCatching {
        View::class.java.getMethod("getRootSurfaceControl")
    }.getOrNull()

    private val transactionClass = runCatching {
        Class.forName("android.view.SurfaceControl\$Transaction")
    }.getOrNull()

    private val setSkipScreenshot = runCatching {
        transactionClass?.getMethod(
            "setSkipScreenshot",
            SurfaceControl::class.java,
            Boolean::class.javaPrimitiveType,
        )
    }.getOrNull()

    private val applyTransaction = runCatching {
        transactionClass?.getMethod("apply")
    }.getOrNull()

    private val transactionCtor = runCatching {
        transactionClass?.getConstructor()
    }.getOrNull()

    fun apply(view: View) {
        val control = surfaceControlOf(view) ?: return
        val ctor = transactionCtor ?: return
        val setter = setSkipScreenshot ?: return
        val apply = applyTransaction ?: return
        runCatching {
            val transaction = ctor.newInstance()
            setter.invoke(transaction, control, true)
            apply.invoke(transaction)
        }.onFailure { LogBus.log("skipScreenshot unavailable: ${it.message}") }
    }

    private fun surfaceControlOf(view: View): SurfaceControl? = runCatching {
        if (view is SurfaceView) {
            view.surfaceControl
        } else {
            getRootSurfaceControl?.invoke(view) as? SurfaceControl
        }
    }.getOrNull()
}

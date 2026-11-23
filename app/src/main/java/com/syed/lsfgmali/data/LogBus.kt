package com.syed.lsfgmali.data

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** In-process log fan-out: UI state, session file writer, and logcat. */
object LogBus {
    private const val MAX_LINES = 600
    private val fmt = SimpleDateFormat("HH:mm:ss", Locale.US)
    private val listeners = CopyOnWriteArrayList<(String) -> Unit>()

    private val _lines = MutableStateFlow<List<String>>(emptyList())
    val lines: StateFlow<List<String>> = _lines.asStateFlow()

    fun log(msg: String) {
        val line = "${fmt.format(Date())}  $msg"
        _lines.update { old -> (old + line).takeLast(MAX_LINES) }
        listeners.forEach { l -> runCatching { l(line) } }
        Log.i(TAG, msg)
    }

    fun addListener(l: (String) -> Unit) {
        listeners.add(l)
        _lines.value.forEach { h -> runCatching { l(h) } }
    }

    fun removeListener(l: (String) -> Unit) = listeners.remove(l)

    fun clear() = _lines.update { emptyList() }

    const val TAG = "LSFGMali"
}

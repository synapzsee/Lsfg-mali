package com.syed.lsfgmali.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Saves this process's own logcat plus the in-app LogBus lines to a rotating
 * session file, so a user can send back one artifact after a playtest.
 */
object LogSaver {
    private const val MAX_PART_BYTES = 8L * 1024 * 1024
    private const val MAX_FILES = 6
    private const val TAG = "logsaver"

    private val running = AtomicBoolean(false)
    private var proc: Process? = null
    private var thread: Thread? = null
    private var current: File? = null
    private var partIndex = 0
    private var sessionStamp = ""
    private var listener: ((String) -> Unit)? = null

    val isRunning: Boolean get() = running.get()

    @Synchronized
    fun start(ctx: Context) {
        appContext = ctx.applicationContext
        if (!running.compareAndSet(false, true)) return
        sessionStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        partIndex = 0
        current = openPart(ctx)
        listener = { line -> write("SHELL  $line") }.also { LogBus.addListener(it) }
        thread = thread(name = TAG, isDaemon = true) { pump(ctx) }
        LogBus.log("logcat saving ON -> ${current?.absolutePath}")
        prune(ctx)
    }

    @Synchronized
    fun stop() {
        if (!running.compareAndSet(true, false)) return
        runCatching { proc?.destroy() }
        proc = null
        listener?.let { LogBus.removeListener(it) }
        listener = null
        thread?.interrupt()
        thread = null
        current = null
    }

    private fun pump(ctx: Context) {
        try {
            val p = ProcessBuilder("logcat", "-v", "threadtime", "--pid=${android.os.Process.myPid()}")
                .redirectErrorStream(true)
                .start()
            proc = p
            p.inputStream.bufferedReader().useLines { lines ->
                for (line in lines) {
                    if (!running.get()) break
                    write(line)
                }
            }
        } catch (t: Throwable) {
            LogBus.log("logcat reader unavailable: ${t.message} (shell log still saved)")
        }
    }

    private fun openPart(ctx: Context): File {
        val dir = Paths.logsDir(ctx)
        val f = File(dir, "lsfgmali-${sessionStamp}-p${partIndex}.log")
        f.writeText("")
        return f
    }

    @Synchronized
    private fun write(line: String) {
        var f = current ?: return
        runCatching {
            val ctx = appContext
            if (f.length() > MAX_PART_BYTES && ctx != null) {
                partIndex++
                f = openPart(ctx)
                current = f
            }
            f.appendText(line + "\n")
        }
    }

    @Volatile
    private var appContext: Context? = null

    fun latest(ctx: Context): File? =
        Paths.logsDir(ctx).listFiles()
            ?.filter { it.isFile && it.name.endsWith(".log") }
            ?.maxByOrNull { it.lastModified() }

    fun shareLatest(ctx: Context) {
        val f = latest(ctx) ?: run {
            LogBus.log("no session log to share")
            return
        }
        runCatching {
            val uri: Uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "LSFG Mali session log")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            ctx.startActivity(
                Intent.createChooser(send, "Share LSFG Mali log")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }.onFailure { LogBus.log("share failed: ${it.message}") }
    }

    private fun prune(ctx: Context) {
        runCatching {
            Paths.logsDir(ctx).listFiles()
                ?.filter { it.isFile && it.name.endsWith(".log") }
                ?.sortedByDescending { it.lastModified() }
                ?.drop(MAX_FILES)
                ?.forEach { it.delete() }
        }
    }

}

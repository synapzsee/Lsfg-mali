package com.syed.lsfgmali.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import com.syed.lsfgmali.BuildConfig
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Saves this process's own logcat plus the in-app LogBus lines to a rotating
 * session file, so a user can send back one artifact after a playtest.
 *
 * `logcat --pid=<pid>` is unreliable on some Android builds (the stream comes
 * back empty), so the whole buffer is read and filtered here instead: our own
 * pid, plus any line that mentions this app's package.
 */
object LogSaver {
    private const val MAX_PART_BYTES = 8L * 1024 * 1024
    private const val MAX_FILES = 6
    private const val TAG = "logsaver"

    private val running = AtomicBoolean(false)
    private var proc: java.lang.Process? = null
    private var thread: Thread? = null
    private var current: File? = null
    private var partIndex = 0
    private var sessionStamp = ""
    private var listener: ((String) -> Unit)? = null
    private var logcatMode = "none"

    val isRunning: Boolean get() = running.get()

    @Synchronized
    fun start(ctx: Context) {
        appContext = ctx.applicationContext
        if (!running.compareAndSet(false, true)) return
        sessionStamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        partIndex = 0
        current = openPart(ctx)
        writeHeader(ctx)
        listener = { line: String -> write("SHELL  $line") }.also { LogBus.addListener(it) }
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
        val pid = android.os.Process.myPid()
        val pkg = ctx.packageName
        val process = runCatching {
            ProcessBuilder("logcat", "-v", "threadtime", "-b", "all")
                .redirectErrorStream(true)
                .start()
        }.getOrElse {
            LogBus.log("logcat could not be started: ${it.message} — only shell lines are captured")
            return
        }
        proc = process
        logcatMode = "all buffers, filtered to pid $pid"
        write("LOGCAT  mode=$logcatMode")
        runCatching {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.useLines { lines ->
                for (line in lines) {
                    if (!running.get()) break
                    if (isInteresting(line, pid, pkg)) write(line)
                }
            }
        }.onFailure {
            write("LOGCAT  stream ended: ${it.message}")
        }
    }

    private fun isInteresting(line: String, pid: Int, pkg: String): Boolean {
        if (line.contains(pkg, ignoreCase = true)) return true
        val fields = line.split(' ', limit = 6)
        if (fields.size < 3) return false
        return fields[2].toIntOrNull() == pid
    }

    private fun writeHeader(ctx: Context) {
        val device = DeviceInfo.detect(ctx)
        write("=== LSFGMali session log $sessionStamp part $partIndex (pid ${android.os.Process.myPid()}, app ${BuildConfig.VERSION_NAME}) ===")
        write("=== device: ${device.manufacturer} ${device.model} · ${device.soc.ifBlank { "SoC?" }} · GPU ${device.gpuShort} · Android ${device.androidRelease} (SDK ${device.sdkInt}) · ${device.abi} ===")
        write("=== engine native log: ${Paths.nativeLog(ctx)} · crash file: ${Paths.crashFile(ctx)} ===")
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
                writeHeader(ctx)
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

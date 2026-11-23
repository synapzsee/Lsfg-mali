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
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.LinkedHashSet
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Saves this process's own logcat, the engine's native log file and the in-app
 * LogBus lines to a rotating session file, so a user can send back one
 * artifact after a playtest.
 *
 * Three sources are merged because no single one is reliable everywhere:
 *  - `logcat --pid=<pid>`  (the supported way to read our own logs)
 *  - `logcat -b all`       (fallback for ROMs where the pid filter returns nothing)
 *  - the engine's own ring log file written by initCrashReporter()
 */
object LogSaver {
    private const val MAX_PART_BYTES = 8L * 1024 * 1024
    private const val MAX_FILES = 6
    private const val TAG = "logsaver"
    private const val DEDUPE_WINDOW = 800

    private val running = AtomicBoolean(false)
    private var proc: java.lang.Process? = null
    private var thread: Thread? = null
    private val workers = mutableListOf<Thread>()
    private var current: File? = null
    private var partIndex = 0
    private var sessionStamp = ""
    private var listener: ((String) -> Unit)? = null
    private val seen = Collections.synchronizedSet(LinkedHashSet<String>())

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
        synchronized(workers) {
            workers.forEach { runCatching { it.interrupt() } }
            workers.clear()
        }
        current = null
    }

    private fun pump(ctx: Context) {
        val pid = android.os.Process.myPid()
        val pkg = ctx.packageName
        write("LOGCAT  sources: logcat --pid=$pid, logcat -b all (filtered), engine ring log")

        workers += thread(name = "logsaver-pid", isDaemon = true) {
            val stream = startLogcat(listOf("-v", "threadtime", "--pid=$pid"))
            if (stream == null) {
                write("LOGCAT  pid-filtered logcat could not be started")
            } else {
                readStream(stream) { line -> writeUnique(line) }
            }
        }
        workers += thread(name = "logsaver-full", isDaemon = true) {
            val stream = startLogcat(listOf("-v", "threadtime", "-b", "all"))
            if (stream == null) {
                write("LOGCAT  full-buffer logcat could not be started")
            } else {
                readStream(stream) { line ->
                    if (isInteresting(line, pid, pkg)) writeUnique(line)
                }
            }
        }
        workers += thread(name = "logsaver-native", isDaemon = true) { tailNativeLog(ctx) }
    }

    private fun startLogcat(args: List<String>): java.lang.Process? = runCatching {
        ProcessBuilder(listOf("logcat") + args)
            .redirectErrorStream(true)
            .start()
    }.getOrNull()

    private fun readStream(process: java.lang.Process, sink: (String) -> Unit) {
        runCatching {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            reader.useLines { lines ->
                for (line in lines) {
                    if (!running.get()) break
                    sink(line)
                }
            }
        }.onFailure {
            write("LOGCAT  stream ended: ${it.message}")
        }
    }

    /**
     * The engine keeps its own ring log on disk (initCrashReporter); tail it so
     * the session file contains the Vulkan/framegen lines even when logcat is
     * unavailable.
     */
    private fun tailNativeLog(ctx: Context) {
        val file = File(Paths.nativeLog(ctx))
        var offset = 0L
        while (running.get()) {
            try {
                Thread.sleep(1500)
            } catch (e: InterruptedException) {
                return
            }
            if (!file.exists()) continue
            runCatching {
                if (file.length() < offset) offset = 0
                if (file.length() <= offset) return@runCatching
                RandomAccessFile(file, "r").use { raf ->
                    raf.seek(offset)
                    val text = raf.readBytes().toString(Charsets.UTF_8)
                    offset = raf.filePointer
                    text.split('\n').filter { it.isNotBlank() }.forEach { write("NATIVE  $it") }
                }
            }
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
    private fun writeUnique(line: String) {
        if (!seen.add(line)) return
        if (seen.size > DEDUPE_WINDOW) {
            val iterator = seen.iterator()
            if (iterator.hasNext()) {
                iterator.next()
                iterator.remove()
            }
        }
        write(line)
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

    /** Copies the latest session log into the public Downloads folder. */
    fun exportToDownloads(ctx: Context): Boolean {
        val source = latest(ctx)
        if (source == null) {
            LogBus.log("no session log to export")
            return false
        }
        return runCatching {
            val values = android.content.ContentValues().apply {
                put("_display_name", source.name)
                put("mime_type", "text/plain")
                put("relative_path", "Download/LSFGMali")
            }
            val uri = ctx.contentResolver.insert(
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values,
            ) ?: error("MediaStore insert failed")
            ctx.contentResolver.openOutputStream(uri)?.use { out ->
                source.inputStream().use { input -> input.copyTo(out) }
            } ?: error("could not open the export stream")
            LogBus.log("session log exported to Download/LSFGMali/${source.name}")
            true
        }.getOrElse {
            LogBus.log("log export failed: ${it.message}")
            false
        }
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

package com.syed.lsfgmali;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Process;
import android.provider.MediaStore;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.ArraysKt;
import kotlin.collections.CollectionsKt;
import kotlin.comparisons.ComparisonsKt;
import kotlin.io.ByteStreamsKt;
import kotlin.io.CloseableKt;
import kotlin.io.ConstantsKt;
import kotlin.io.FilesKt;
import kotlin.io.TextStreamsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.text.Charsets;
import kotlin.text.StringsKt;

/* compiled from: LogSaver.kt */
@Metadata(d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010 \n\u0002\b\u0004\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\u0006\u0010\u001a\u001a\u00020\tJ\u000e\u0010\u001b\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0019J\u000e\u0010\u001c\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0019J\u0010\u0010\u001d\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\u0010\u0010\u001e\u001a\u00020\u00162\u0006\u0010\u001f\u001a\u00020\u0013H\u0002J\u0010\u0010 \u001a\u00020\u00162\u0006\u0010!\u001a\u00020\u000fH\u0002J\u0010\u0010\"\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0019H\u0002J\u0010\u0010#\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0018\u001a\u00020\u0019J\u0010\u0010&\u001a\u00020\t2\u0006\u0010'\u001a\u00020\u000fH\u0002J\u0010\u0010(\u001a\u0004\u0018\u00010\u000f2\u0006\u0010\u0018\u001a\u00020\u0019J\u000e\u0010)\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0019R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T¢\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e¢\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0005X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0007X\u0082\u000e¢\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e¢\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0013\u0012\u0004\u0012\u00020\u00160\u0015X\u0082\u0004¢\u0006\u0002\n\u0000R\u0014\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00130%X\u0082\u0004¢\u0006\u0002\n\u0000"}, d2 = {"Lcom/syed/lsfgmali/LogSaver;", "", "<init>", "()V", "MAX_PART_BYTES", "", "MAX_FILES", "", "running", "", "proc", "Ljava/lang/Process;", "thread", "Ljava/lang/Thread;", "currentFile", "Ljava/io/File;", "partBytes", "partIndex", "sessionStamp", "", "busListener", "Lkotlin/Function1;", "", "logDir", "ctx", "Landroid/content/Context;", "isRunning", "start", "stop", "openPart", "writeLine", "line", "openPartFrom", "dir", "prune", "latestFile", "SESSION_MARKERS", "", "containsSession", "f", "exportFile", "shareLatest"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class LogSaver {
    private static final int MAX_FILES = 5;
    private static final long MAX_PART_BYTES = 8388608;
    private static File currentFile;
    private static long partBytes;
    private static int partIndex;
    private static Process proc;
    private static volatile boolean running;
    private static Thread thread;
    public static final LogSaver INSTANCE = new LogSaver();
    private static String sessionStamp = "";
    private static final Function1<String, Unit> busListener = new Function1() { // from class: com.syed.lsfgmali.LogSaver$$ExternalSyntheticLambda1
        @Override // kotlin.jvm.functions.Function1
        public final Object invoke(Object obj) {
            Unit busListener$lambda$0;
            busListener$lambda$0 = LogSaver.busListener$lambda$0((String) obj);
            return busListener$lambda$0;
        }
    };
    private static final List<String> SESSION_MARKERS = CollectionsKt.listOf((Object[]) new String[]{"capture size ", "capture running"});

    private LogSaver() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final Unit busListener$lambda$0(String line) {
        Intrinsics.checkNotNullParameter(line, "line");
        INSTANCE.writeLine("SHELL  " + line);
        return Unit.INSTANCE;
    }

    private final File logDir(Context ctx) {
        File $this$logDir_u24lambda_u241 = new File(ctx.getFilesDir(), "logs");
        $this$logDir_u24lambda_u241.mkdirs();
        return $this$logDir_u24lambda_u241;
    }

    public final boolean isRunning() {
        return running;
    }

    public final synchronized void start(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        if (running) {
            return;
        }
        running = true;
        sessionStamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
        partIndex = 0;
        openPart(ctx);
        LogBus.INSTANCE.addListener(busListener);
        Thread $this$start_u24lambda_u244 = new Thread(new Runnable() { // from class: com.syed.lsfgmali.LogSaver$$ExternalSyntheticLambda3
            @Override // java.lang.Runnable
            public final void run() {
                LogSaver.start$lambda$3();
            }
        });
        $this$start_u24lambda_u244.setName("logsaver");
        $this$start_u24lambda_u244.setDaemon(true);
        $this$start_u24lambda_u244.start();
        thread = $this$start_u24lambda_u244;
        LogBus logBus = LogBus.INSTANCE;
        File file = currentFile;
        logBus.log("logcat saving ON -> " + (file != null ? file.getAbsolutePath() : null));
        prune(ctx);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final void start$lambda$3() {
        String line;
        try {
            Process p = new ProcessBuilder("logcat", "-v", "threadtime", "--pid=" + Process.myPid()).redirectErrorStream(true).start();
            proc = p;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            try {
                BufferedReader r = bufferedReader;
                while (running && (line = r.readLine()) != null) {
                    INSTANCE.writeLine(line);
                }
                Unit unit = Unit.INSTANCE;
                CloseableKt.closeFinally(bufferedReader, null);
            } finally {
            }
        } catch (Exception e) {
            INSTANCE.writeLine("LOGSAVER logcat stream failed: " + e.getMessage() + " (shell logs still captured)");
        }
    }

    public final synchronized void stop(Context ctx) {
        Unit unit;
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        if (running) {
            running = false;
            LogBus.INSTANCE.removeListener(busListener);
            try {
                Result.Companion companion = Result.INSTANCE;
                LogSaver logSaver = this;
                Process process = proc;
                if (process != null) {
                    process.destroy();
                    unit = Unit.INSTANCE;
                } else {
                    unit = null;
                }
                Result.m11constructorimpl(unit);
            } catch (Throwable th) {
                Result.Companion companion2 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th));
            }
            proc = null;
            LogBus logBus = LogBus.INSTANCE;
            File file = currentFile;
            logBus.log("logcat saving OFF (file kept: " + (file != null ? file.getName() : null) + ")");
        }
    }

    private final void openPart(Context ctx) {
        String name;
        if (partIndex == 0) {
            name = "lsfgmali-" + sessionStamp + ".log";
        } else {
            name = "lsfgmali-" + sessionStamp + "-part" + partIndex + ".log";
        }
        currentFile = new File(logDir(ctx), name);
        partBytes = 0L;
        File file = currentFile;
        if (file != null) {
            FilesKt.appendText$default(file, "=== LSFGMali session log " + sessionStamp + " part " + partIndex + " (pid " + Process.myPid() + ", own-process logcat + shell log) ===\n", null, 2, null);
        }
    }

    private final synchronized void writeLine(String line) {
        File f = currentFile;
        if (f == null) {
            return;
        }
        try {
            String s = line + "\n";
            FilesKt.appendText$default(f, s, null, 2, null);
            partBytes += s.length();
            if (partBytes > MAX_PART_BYTES) {
                partIndex++;
                File it = currentFile;
                if (it != null) {
                    LogSaver logSaver = INSTANCE;
                    File parentFile = it.getParentFile();
                    Intrinsics.checkNotNull(parentFile);
                    logSaver.openPartFrom(parentFile);
                }
            }
        } catch (Exception e) {
        }
    }

    private final void openPartFrom(File dir) {
        String name = "lsfgmali-" + sessionStamp + "-part" + partIndex + ".log";
        currentFile = new File(dir, name);
        partBytes = 0L;
        File file = currentFile;
        if (file != null) {
            FilesKt.appendText$default(file, "=== part " + partIndex + " ===\n", null, 2, null);
        }
    }

    private final void prune(Context ctx) {
        List files;
        Object[] $this$sortedByDescending$iv = logDir(ctx).listFiles(new FileFilter() { // from class: com.syed.lsfgmali.LogSaver$$ExternalSyntheticLambda0
            @Override // java.io.FileFilter
            public final boolean accept(File file) {
                boolean prune$lambda$7;
                prune$lambda$7 = LogSaver.prune$lambda$7(file);
                return prune$lambda$7;
            }
        });
        if ($this$sortedByDescending$iv == null || (files = ArraysKt.sortedWith($this$sortedByDescending$iv, new Comparator() { // from class: com.syed.lsfgmali.LogSaver$prune$$inlined$sortedByDescending$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                File it = (File) t2;
                File it2 = (File) t;
                return ComparisonsKt.compareValues(Long.valueOf(it.lastModified()), Long.valueOf(it2.lastModified()));
            }
        })) == null) {
            return;
        }
        Iterable $this$forEach$iv = CollectionsKt.drop(files, MAX_FILES);
        for (Object element$iv : $this$forEach$iv) {
            File it = (File) element$iv;
            LogSaver logSaver = INSTANCE;
            try {
                Result.Companion companion = Result.INSTANCE;
                Result.m11constructorimpl(Boolean.valueOf(it.delete()));
            } catch (Throwable th) {
                Result.Companion companion2 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean prune$lambda$7(File f) {
        String name = f.getName();
        Intrinsics.checkNotNullExpressionValue(name, "getName(...)");
        return StringsKt.startsWith$default(name, "lsfgmali-", false, 2, (Object) null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean latestFile$lambda$11(File f) {
        String name = f.getName();
        Intrinsics.checkNotNullExpressionValue(name, "getName(...)");
        return StringsKt.startsWith$default(name, "lsfgmali-", false, 2, (Object) null);
    }

    public final File latestFile(Context ctx) {
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        File[] listFiles = logDir(ctx).listFiles(new FileFilter() { // from class: com.syed.lsfgmali.LogSaver$$ExternalSyntheticLambda4
            @Override // java.io.FileFilter
            public final boolean accept(File file) {
                boolean latestFile$lambda$11;
                latestFile$lambda$11 = LogSaver.latestFile$lambda$11(file);
                return latestFile$lambda$11;
            }
        });
        File file = null;
        if (listFiles != null) {
            if (!(listFiles.length == 0)) {
                file = listFiles[0];
                int lastIndex$iv = ArraysKt.getLastIndex(listFiles);
                if (lastIndex$iv != 0) {
                    long maxValue$iv = file.lastModified();
                    int i$iv = 1;
                    if (1 <= lastIndex$iv) {
                        while (true) {
                            File file2 = listFiles[i$iv];
                            long v$iv = file2.lastModified();
                            if (maxValue$iv < v$iv) {
                                file = file2;
                                maxValue$iv = v$iv;
                            }
                            if (i$iv == lastIndex$iv) {
                                break;
                            }
                            i$iv++;
                        }
                    }
                }
            }
        }
        return file;
    }

    /* JADX WARN: Removed duplicated region for block: B:29:0x00c9 A[LOOP:0: B:12:0x0045->B:29:0x00c9, LOOP_END] */
    /* JADX WARN: Removed duplicated region for block: B:30:0x00dc A[EDGE_INSN: B:30:0x00dc->B:31:0x00dc BREAK  A[LOOP:0: B:12:0x0045->B:29:0x00c9], SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    private final boolean containsSession(File f) {
        boolean z;
        Reader $this$useLines$iv;
        Throwable th;
        boolean z2;
        BufferedReader it$iv;
        Reader $this$useLines$iv2;
        boolean z3;
        BufferedReader it$iv2;
        try {
            Reader inputStreamReader = new InputStreamReader(new FileInputStream(f), Charsets.UTF_8);
            $this$useLines$iv = inputStreamReader instanceof BufferedReader ? (BufferedReader) inputStreamReader : new BufferedReader(inputStreamReader, ConstantsKt.DEFAULT_BUFFER_SIZE);
            z = false;
        } catch (Exception e) {
            z = false;
        }
        try {
            BufferedReader bufferedReader = $this$useLines$iv instanceof BufferedReader ? (BufferedReader) $this$useLines$iv : new BufferedReader($this$useLines$iv, ConstantsKt.DEFAULT_BUFFER_SIZE);
            try {
                BufferedReader it$iv3 = bufferedReader;
                Sequence lines = TextStreamsKt.lineSequence(it$iv3);
                Iterator<String> it = lines.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        z2 = false;
                        break;
                    }
                    Object element$iv = it.next();
                    String line = (String) element$iv;
                    Iterable $this$any$iv = SESSION_MARKERS;
                    z2 = true;
                    if ($this$any$iv instanceof Collection) {
                        try {
                            if (((Collection) $this$any$iv).isEmpty()) {
                                it$iv = it$iv3;
                                $this$useLines$iv2 = $this$useLines$iv;
                                z3 = z;
                                it$iv2 = null;
                                if (it$iv2 == null) {
                                    break;
                                }
                                z = z3;
                                it$iv3 = it$iv;
                                $this$useLines$iv = $this$useLines$iv2;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            z = false;
                            try {
                                throw th;
                            } catch (Throwable th3) {
                                CloseableKt.closeFinally(bufferedReader, th);
                                throw th3;
                            }
                        }
                    }
                    Iterator it2 = $this$any$iv.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            it$iv = it$iv3;
                            $this$useLines$iv2 = $this$useLines$iv;
                            z3 = z;
                            it$iv2 = null;
                            break;
                        }
                        Object element$iv2 = it2.next();
                        String m = (String) element$iv2;
                        it$iv = it$iv3;
                        Iterable $this$any$iv2 = $this$any$iv;
                        $this$useLines$iv2 = $this$useLines$iv;
                        z3 = z;
                        z = false;
                        try {
                            if (StringsKt.contains$default((CharSequence) line, (CharSequence) m, false, 2, (Object) null)) {
                                it$iv2 = 1;
                                break;
                            }
                            z = z3;
                            it$iv3 = it$iv;
                            $this$any$iv = $this$any$iv2;
                            $this$useLines$iv = $this$useLines$iv2;
                        } catch (Throwable th4) {
                            th = th4;
                            throw th;
                        }
                    }
                    if (it$iv2 == null) {
                    }
                }
                CloseableKt.closeFinally(bufferedReader, null);
                return z2;
            } catch (Throwable th5) {
                z = false;
                th = th5;
            }
        } catch (Exception e2) {
            return z;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static final boolean exportFile$lambda$16(File f) {
        String name = f.getName();
        Intrinsics.checkNotNullExpressionValue(name, "getName(...)");
        return StringsKt.startsWith$default(name, "lsfgmali-", false, 2, (Object) null);
    }

    public final File exportFile(Context ctx) {
        List files;
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        Object[] $this$sortedByDescending$iv = logDir(ctx).listFiles(new FileFilter() { // from class: com.syed.lsfgmali.LogSaver$$ExternalSyntheticLambda2
            @Override // java.io.FileFilter
            public final boolean accept(File file) {
                boolean exportFile$lambda$16;
                exportFile$lambda$16 = LogSaver.exportFile$lambda$16(file);
                return exportFile$lambda$16;
            }
        });
        Object obj = null;
        if ($this$sortedByDescending$iv == null || (files = ArraysKt.sortedWith($this$sortedByDescending$iv, new Comparator() { // from class: com.syed.lsfgmali.LogSaver$exportFile$$inlined$sortedByDescending$1
            /* JADX WARN: Multi-variable type inference failed */
            @Override // java.util.Comparator
            public final int compare(T t, T t2) {
                File it = (File) t2;
                File it2 = (File) t;
                return ComparisonsKt.compareValues(Long.valueOf(it.lastModified()), Long.valueOf(it2.lastModified()));
            }
        })) == null) {
            return null;
        }
        List $this$firstOrNull$iv = files;
        Iterator it = $this$firstOrNull$iv.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Object element$iv = it.next();
            File it2 = (File) element$iv;
            LogSaver logSaver = INSTANCE;
            Intrinsics.checkNotNull(it2);
            if (logSaver.containsSession(it2)) {
                obj = element$iv;
                break;
            }
        }
        File file = (File) obj;
        return file == null ? (File) CollectionsKt.firstOrNull(files) : file;
    }

    public final void shareLatest(Context ctx) {
        Uri uri;
        Throwable th;
        Throwable th2;
        FileInputStream it;
        Intrinsics.checkNotNullParameter(ctx, "ctx");
        File src = exportFile(ctx);
        if (src == null) {
            LogBus.INSTANCE.log("share log: no log files captured yet");
            return;
        }
        File newest = latestFile(ctx);
        if (newest != null && !Intrinsics.areEqual(newest.getName(), src.getName())) {
            LogBus.INSTANCE.log("share log: exporting session log " + src.getName() + " (skipping current launch log " + newest.getName() + ")");
        } else {
            LogBus.INSTANCE.log("share log: exporting " + src.getName());
        }
        try {
            ContentValues values = new ContentValues();
            values.put("_display_name", src.getName());
            values.put("mime_type", "text/plain");
            values.put("relative_path", "Download");
            uri = ctx.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e) {
            e = e;
        }
        try {
            if (uri == null) {
                throw new IllegalStateException("MediaStore insert failed");
            }
            OutputStream openOutputStream = ctx.getContentResolver().openOutputStream(uri);
            if (openOutputStream != null) {
                FileInputStream fileInputStream = openOutputStream;
                try {
                    try {
                        OutputStream out = fileInputStream;
                        fileInputStream = new FileInputStream(src);
                        try {
                            it = fileInputStream;
                        } catch (Throwable th3) {
                            th2 = th3;
                        }
                        try {
                            long copyTo$default = ByteStreamsKt.copyTo$default(it, out, 0, 2, null);
                            CloseableKt.closeFinally(fileInputStream, null);
                            Long.valueOf(copyTo$default);
                            CloseableKt.closeFinally(fileInputStream, null);
                        } catch (Throwable th4) {
                            th2 = th4;
                            try {
                                throw th;
                            } finally {
                            }
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        try {
                            throw th;
                        } finally {
                        }
                    }
                } catch (Throwable th6) {
                    th = th6;
                    throw th;
                }
            }
            LogBus.INSTANCE.log("log copied to Downloads/" + src.getName() + " — sharing…");
            Intent $this$shareLatest_u24lambda_u2423 = new Intent("android.intent.action.SEND");
            $this$shareLatest_u24lambda_u2423.setType("text/plain");
            $this$shareLatest_u24lambda_u2423.putExtra("android.intent.extra.STREAM", uri);
            $this$shareLatest_u24lambda_u2423.addFlags(1);
            Intent $this$shareLatest_u24lambda_u2424 = Intent.createChooser($this$shareLatest_u24lambda_u2423, "Send LSFGMali log");
            $this$shareLatest_u24lambda_u2424.addFlags(268435456);
            ctx.startActivity($this$shareLatest_u24lambda_u2424);
        } catch (Exception e2) {
            e = e2;
            LogBus.INSTANCE.log("share log failed: " + e.getMessage() + " (file is at " + src.getAbsolutePath() + ")");
        }
    }
}

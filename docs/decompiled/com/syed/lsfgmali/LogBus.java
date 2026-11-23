package com.syed.lsfgmali;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import kotlin.Metadata;
import kotlin.Result;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: Shell.kt */
@Metadata(d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u000e\u001a\u00020\b2\u0006\u0010\u000f\u001a\u00020\u0007J\u001a\u0010\u0010\u001a\u00020\b2\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0006J\u001a\u0010\u0012\u001a\u00020\b2\u0012\u0010\u0011\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u0006R \u0010\u0004\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\b0\u00060\u0005X\u0082\u0004¢\u0006\u0002\n\u0000R\u001e\u0010\t\u001a\u0012\u0012\u0004\u0012\u00020\u00070\nj\b\u0012\u0004\u0012\u00020\u0007`\u000bX\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004¢\u0006\u0002\n\u0000"}, d2 = {"Lcom/syed/lsfgmali/LogBus;", "", "<init>", "()V", "listeners", "Ljava/util/concurrent/CopyOnWriteArrayList;", "Lkotlin/Function1;", "", "", "history", "Ljava/util/ArrayList;", "Lkotlin/collections/ArrayList;", "fmt", "Ljava/text/SimpleDateFormat;", "log", "msg", "addListener", "l", "removeListener"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class LogBus {
    public static final LogBus INSTANCE = new LogBus();
    private static final CopyOnWriteArrayList<Function1<String, Unit>> listeners = new CopyOnWriteArrayList<>();
    private static final ArrayList<String> history = new ArrayList<>();
    private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private LogBus() {
    }

    public final synchronized void log(String msg) {
        Intrinsics.checkNotNullParameter(msg, "msg");
        String line = fmt.format(new Date()) + "  " + msg;
        history.add(line);
        if (history.size() > 300) {
            history.remove(0);
        }
        Iterator<Function1<String, Unit>> it = listeners.iterator();
        Intrinsics.checkNotNullExpressionValue(it, "iterator(...)");
        while (it.hasNext()) {
            Function1 l = it.next();
            try {
                Result.Companion companion = Result.INSTANCE;
                LogBus logBus = this;
                l.invoke(line);
                Result.m11constructorimpl(Unit.INSTANCE);
            } catch (Throwable th) {
                Result.Companion companion2 = Result.INSTANCE;
                Result.m11constructorimpl(ResultKt.createFailure(th));
            }
        }
        Log.i("LSFGMaliShell", msg);
    }

    public final void addListener(Function1<? super String, Unit> l) {
        Intrinsics.checkNotNullParameter(l, "l");
        listeners.add(l);
        synchronized (this) {
            Iterator<String> it = history.iterator();
            Intrinsics.checkNotNullExpressionValue(it, "iterator(...)");
            while (it.hasNext()) {
                String next = it.next();
                Intrinsics.checkNotNullExpressionValue(next, "next(...)");
                String h = next;
                LogBus logBus = INSTANCE;
                try {
                    Result.Companion companion = Result.INSTANCE;
                    l.invoke(h);
                    Result.m11constructorimpl(Unit.INSTANCE);
                } catch (Throwable th) {
                    Result.Companion companion2 = Result.INSTANCE;
                    Result.m11constructorimpl(ResultKt.createFailure(th));
                }
            }
            Unit unit = Unit.INSTANCE;
        }
    }

    public final void removeListener(Function1<? super String, Unit> l) {
        Intrinsics.checkNotNullParameter(l, "l");
        listeners.remove(l);
    }
}

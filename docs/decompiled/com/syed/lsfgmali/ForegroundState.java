package com.syed.lsfgmali;

import kotlin.Metadata;

/* compiled from: Shell.kt */
@Metadata(d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0004\bÆ\u0002\u0018\u00002\u00020\u0001B\t\b\u0002¢\u0006\u0004\b\u0002\u0010\u0003R\u001a\u0010\u0004\u001a\u00020\u0005X\u0086\u000e¢\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t"}, d2 = {"Lcom/syed/lsfgmali/ForegroundState;", "", "<init>", "()V", "mainActivityForeground", "", "getMainActivityForeground", "()Z", "setMainActivityForeground", "(Z)V"}, k = 1, mv = {2, 0, 0}, xi = 48)
/* loaded from: classes.dex */
public final class ForegroundState {
    public static final ForegroundState INSTANCE = new ForegroundState();
    private static volatile boolean mainActivityForeground;

    private ForegroundState() {
    }

    public final boolean getMainActivityForeground() {
        return mainActivityForeground;
    }

    public final void setMainActivityForeground(boolean z) {
        mainActivityForeground = z;
    }
}

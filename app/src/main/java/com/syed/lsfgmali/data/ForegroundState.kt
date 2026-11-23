package com.syed.lsfgmali.data

/** Tracks whether our own MainActivity is in front, so the overlay never covers our UI. */
object ForegroundState {
    @Volatile
    var mainActivityForeground: Boolean = false
}

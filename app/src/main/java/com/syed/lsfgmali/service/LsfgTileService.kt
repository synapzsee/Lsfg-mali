package com.syed.lsfgmali.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.syed.lsfgmali.MainActivity
import com.syed.lsfgmali.data.EngineState
import com.syed.lsfgmali.data.LogBus

/**
 * Quick-settings tile: opens the app when no session runs, toggles bypass while
 * a session is live and stops the session on a long press. Useful when the
 * overlay is in the way and opening the app would interrupt the game.
 */
class LsfgTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        val snapshot = EngineState.snapshot.value
        if (!snapshot.running) {
            openApp()
            return
        }
        CaptureService.setBypass(this, !snapshot.bypass)
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val snapshot = EngineState.snapshot.value
        tile.state = if (snapshot.running) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = when {
            !snapshot.running -> "LSFG Mali"
            snapshot.bypass -> "LSFG (bypassed)"
            else -> "LSFG ${"%.0f".format(snapshot.outputFps)} fps"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (snapshot.running) "Tap to bypass" else "Tap to open"
        }
        runCatching { tile.updateTile() }
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pending = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                startActivityAndCollapse(pending)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
        }.onFailure { LogBus.log("quick tile could not open the app: ${it.message}") }
    }
}

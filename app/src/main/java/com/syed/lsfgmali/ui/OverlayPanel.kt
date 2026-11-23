package com.syed.lsfgmali.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syed.lsfgmali.data.EngineState
import com.syed.lsfgmali.data.EngineSnapshot
import com.syed.lsfgmali.data.SessionPhase
import com.syed.lsfgmali.service.CaptureService
import com.syed.lsfgmali.ui.theme.LsfgTheme

@Composable
fun OverlayPanel(
    snapshot: EngineSnapshot,
    bypass: Boolean,
    onBypass: (Boolean) -> Unit,
    onTarget: (Int) -> Unit,
    onStop: () -> Unit,
    onOpenApp: () -> Unit,
    onClose: () -> Unit,
) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = when (snapshot.phase) {
                        SessionPhase.RUNNING -> Color(0xFF2EE59D)
                        SessionPhase.STARVED -> Color(0xFFFFB020)
                        SessionPhase.ERROR -> Color(0xFFFF5252)
                        else -> MaterialTheme.colorScheme.outline
                    },
                    modifier = Modifier.size(10.dp),
                ) {}
                Spacer(Modifier.width(8.dp))
                Text(
                    "LSFG Mali",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close panel")
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                when (snapshot.phase) {
                    SessionPhase.RUNNING -> "Generating frames"
                    SessionPhase.STARVED -> "Waiting for new frames"
                    SessionPhase.STARTING -> "Starting…"
                    SessionPhase.STOPPING -> "Stopping…"
                    SessionPhase.ERROR -> snapshot.error ?: "Error"
                    SessionPhase.IDLE -> "Idle"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat("Output", "%.0f".format(snapshot.outputFps), "fps", Modifier.weight(1f))
                Stat("Source", "%.0f".format(snapshot.sourceFps), "fps", Modifier.weight(1f))
                Stat("Latency", "%.0f".format(snapshot.latencyMs), "ms", Modifier.weight(1f))
            }

            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Speed, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    buildString {
                        append(snapshot.resolution)
                        if (snapshot.captureW > 0) append(" · ")
                        append(if (snapshot.targetFps > 0) "target ${snapshot.targetFps}" else "${snapshot.multiplier}×")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                Text(snapshot.driver, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.height(12.dp))
            Text("Target FPS", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(30, 45, 60, 90).forEach { fps ->
                    FilterChip(
                        selected = snapshot.targetFps == fps,
                        onClick = { onTarget(fps) },
                        label = { Text("$fps") },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bypass frame gen", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                Switch(checked = bypass, onCheckedChange = onBypass)
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenApp, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("App")
                }
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(" $unit", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun OverlayPanelRoot(
    bypass: Boolean,
    onBypass: (Boolean) -> Unit,
    onTarget: (Int) -> Unit,
    onStop: () -> Unit,
    onOpenApp: () -> Unit,
    onClose: () -> Unit,
) {
    val snapshot by EngineState.snapshot.collectAsState()
    LsfgTheme {
        Surface(color = Color.Transparent) {
            OverlayPanel(
                snapshot = snapshot,
                bypass = bypass,
                onBypass = onBypass,
                onTarget = onTarget,
                onStop = onStop,
                onOpenApp = onOpenApp,
                onClose = onClose,
            )
        }
    }
}

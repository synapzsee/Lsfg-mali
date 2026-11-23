package com.syed.lsfgmali.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.syed.lsfgmali.data.Diagnostics
import com.syed.lsfgmali.data.LogBus
import com.syed.lsfgmali.data.LogSaver
import androidx.compose.ui.platform.LocalContext

/**
 * Live tail of the in-app log plus a one-shot diagnostics dump. Both exist so a
 * user can hand back a complete picture without leaving the app.
 */
@Composable
fun DebugCard(
    onShareLog: () -> Unit,
    onExportLog: () -> Unit,
    onCopied: (String) -> Unit,
) {
    val context = LocalContext.current
    val lines by LogBus.lines.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    SectionCard(icon = Icons.Filled.BugReport, title = "Debug log") {
        Text(
            "Live shell log — the session file also captures logcat and the engine's own counters.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))

        val visible = if (expanded) lines else lines.takeLast(12)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        ) {
            SelectionContainer {
                Column(
                    Modifier
                        .heightIn(max = if (expanded) 320.dp else 190.dp)
                        .padding(10.dp),
                ) {
                    if (visible.isEmpty()) {
                        Text("no log lines yet", style = MaterialTheme.typography.labelSmall)
                    } else {
                        visible.forEach { line ->
                            Text(
                                line,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                ),
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (expanded) "Collapse" else "Expand")
            }
            OutlinedButton(
                onClick = {
                    Diagnostics.copyToClipboard(context, lines.joinToString("\n"))
                    onCopied("Log copied to clipboard")
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Copy")
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onShareLog, modifier = Modifier.weight(1f)) {
                Text("Share session log")
            }
            OutlinedButton(onClick = onExportLog, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save to Downloads")
            }
        }
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                LogBus.clear()
                onCopied("In-app log cleared")
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Clear in-app log")
        }
    }
}

@Composable
fun DiagnosticsCard(onCopied: (String) -> Unit) {
    val context = LocalContext.current
    var report by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { report = Diagnostics.report(context) }

    SectionCard(icon = Icons.Filled.Info, title = "Diagnostics report") {
        Text(
            "One tap copies every setting the engine sees, plus driver, DLL and shader-cache state.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        ) {
            SelectionContainer {
                Text(
                    report.ifBlank { "collecting…" },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                    ),
                    modifier = Modifier
                        .heightIn(max = 260.dp)
                        .padding(10.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val fresh = Diagnostics.report(context)
                    report = fresh
                    Diagnostics.copyToClipboard(context, fresh)
                    onCopied("Diagnostics copied to clipboard")
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Copy report")
            }
            OutlinedButton(
                onClick = { report = Diagnostics.report(context) },
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Refresh")
            }
        }
    }
}

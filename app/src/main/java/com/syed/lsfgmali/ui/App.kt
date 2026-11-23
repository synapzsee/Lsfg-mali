package com.syed.lsfgmali.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.lsfg.android.session.NativeBridge
import com.syed.lsfgmali.BuildConfig
import com.syed.lsfgmali.data.CaptureScale
import com.syed.lsfgmali.data.DeviceInfo
import com.syed.lsfgmali.data.DllVersion
import com.syed.lsfgmali.data.DriverMode
import com.syed.lsfgmali.data.EngineState
import com.syed.lsfgmali.data.FpsMode
import com.syed.lsfgmali.data.Hash
import com.syed.lsfgmali.data.LogBus
import com.syed.lsfgmali.data.LogSaver
import com.syed.lsfgmali.data.Paths
import com.syed.lsfgmali.data.SessionPhase
import com.syed.lsfgmali.data.SettingsStore
import com.syed.lsfgmali.service.CaptureService
import com.syed.lsfgmali.service.LsfgTouchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LsfgApp(
    onStart: () -> Unit,
    onStop: () -> Unit,
    onShareLog: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenBatterySettings: () -> Unit,
) {
    val context = LocalContext.current
    val store = remember { SettingsStore.get(context) }
    val settings by store.settings.collectAsState()
    val engine by EngineState.snapshot.collectAsState()
    val device = remember { DeviceInfo.detect(context) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    var accessibilityReady by remember { mutableStateOf(LsfgTouchService.isReady) }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }
    var notificationsGranted by remember { mutableStateOf(notificationsAllowed(context)) }
    var importing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                accessibilityReady = LsfgTouchService.isReady
                overlayGranted = Settings.canDrawOverlays(context)
                notificationsGranted = notificationsAllowed(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(engine.error) {
        engine.error?.let { snackbar.showSnackbar(it) }
    }

    val dllPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            importing = true
            val imported = withContext(Dispatchers.IO) { importDll(context, uri) }
            importing = false
            if (imported != null) {
                store.setDll(imported.name, imported.sha, true, imported.version, imported.fp16)
                val version = imported.version?.let { " $it" } ?: ""
                val path = if (imported.fp16) {
                    "FP16 shader set available"
                } else {
                    "FP32 path (FP16 needs DLL ≥ 3.2.2.0)"
                }
                snackbar.showSnackbar("Lossless.dll$version imported — $path")
            } else {
                store.setDll(null, null, false)
                snackbar.showSnackbar("Import failed — see log for the exact reason")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("LSFG Mali", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (engine.running) "Session running" else "Frame generation for Mali GPUs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenAccessibility) {
                        Icon(Icons.Filled.AccessibilityNew, contentDescription = "Accessibility")
                    }
                    IconButton(onClick = onShareLog) {
                        Icon(Icons.Filled.Share, contentDescription = "Share log")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                HeroCard(
                    enginePhase = engine.phase,
                    message = engine.message,
                    driver = engine.driver,
                    gpu = device.gpuShort,
                    dllReady = settings.dllReady,
                    running = engine.running,
                    onStart = onStart,
                    onStop = onStop,
                )
            }
            if (engine.running) {
                item { LiveCard(engine) }
            }
            item {
                SetupCard(
                    dllName = settings.dllName,
                    dllSha = settings.dllSha,
                    dllVersion = settings.dllVersion,
                    dllFp16 = settings.dllFp16,
                    importing = importing,
                    onPick = { dllPicker.launch(arrayOf("*/*")) },
                )
            }
            item {
                PerformanceCard(
                    settings = settings,
                    deviceName = device.model,
                    onUpdate = { transform -> store.update(transform) },
                    onApplyG85 = {
                        store.applyG85Profile()
                        store.update { it.copy(deviceProfileApplied = true) }
                        scope.launch { snackbar.showSnackbar("Helio G85 profile applied") }
                    },
                )
            }
            item {
                ControlsCard(
                    settings = settings,
                    accessibilityReady = accessibilityReady,
                    overlayGranted = overlayGranted,
                    notificationsGranted = notificationsGranted,
                    onUpdate = { transform -> store.update(transform) },
                    onOpenAccessibility = onOpenAccessibility,
                    onOpenOverlay = onOpenOverlaySettings,
                    onOpenNotifications = onOpenNotificationSettings,
                    onOpenBattery = onOpenBatterySettings,
                    onBubbleChange = { show ->
                        store.update { it.copy(showBubble = show) }
                        if (engine.running) CaptureService.setBubble(context, show)
                    },
                    onTouchBridgeBlocked = {
                        scope.launch { snackbar.showSnackbar("Enable the LSFG Mali touch bridge in Accessibility, then try again") }
                    },
                )
            }
            item {
                DriverCard(
                    driver = settings.driver,
                    onSelect = { mode ->
                        store.update { it.copy(driver = mode) }
                        scope.launch { snackbar.showSnackbar("Driver saved — restart the app to apply it") }
                    },
                )
            }
            item {
                DeviceCard(
                    device = device,
                    autoProfile = settings.autoDeviceProfile,
                    onAutoProfile = { enabled -> store.update { it.copy(autoDeviceProfile = enabled) } },
                    onApplyG85 = {
                        store.applyG85Profile()
                        store.update { it.copy(deviceProfileApplied = true) }
                        scope.launch { snackbar.showSnackbar("Helio G85 profile applied") }
                    },
                )
            }
            item {
                LogCard(onShareLog = onShareLog)
            }
            item {
                AboutCard(device.gpuShort, device.androidRelease, device.sdkInt)
            }
        }
    }
}

@Composable
private fun HeroCard(
    enginePhase: SessionPhase,
    message: String,
    driver: String,
    gpu: String,
    dllReady: Boolean,
    running: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Speed, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Frame generation", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                StatusPill(enginePhase)
            }
            Spacer(Modifier.height(8.dp))
            Text("$driver · $gpu", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onStart,
                    enabled = dllReady && !running,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Start session")
                }
                OutlinedButton(onClick = onStop, enabled = running) {
                    Icon(Icons.Filled.Stop, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Stop")
                }
            }
            if (!dllReady) {
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Import your Lossless.dll to enable Start", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun LiveCard(engine: com.syed.lsfgmali.data.EngineSnapshot) {
    SectionCard(icon = Icons.Filled.Tune, title = "Live engine") {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metric("Output", "%.0f".format(engine.outputFps), "fps", Modifier.weight(1f))
            Metric("Source", "%.0f".format(engine.sourceFps), "fps", Modifier.weight(1f))
            Metric("Latency", "%.0f".format(engine.latencyMs), "ms", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "${engine.resolution} · ${engine.modeLabel} · queue ${"%.0f".format(engine.queueMs)} ms",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "generated ${engine.generated} · posted ${engine.posted} · captures ${engine.uniqueCaptures}" +
                if (engine.fp16) " · FP16" else " · FP32",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SetupCard(
    dllName: String?,
    dllSha: String?,
    dllVersion: String?,
    dllFp16: Boolean,
    importing: Boolean,
    onPick: () -> Unit,
) {
    SectionCard(icon = Icons.Filled.FileOpen, title = "Game files") {
        Text(
            "Lossless.dll is never bundled with this app. Import the copy you own; it stays in app-private storage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    dllName?.let { name -> dllVersion?.let { "$name ($it)" } ?: name } ?: "No DLL imported",
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    when {
                        dllSha == null ->
                            "Any version works — 3.2.2.0+ only adds the optional FP16 shader set"
                        dllFp16 -> "FP16 shader set available · SHA-256 ${dllSha.take(16)}…"
                        else -> "FP32 shader set · SHA-256 ${dllSha.take(16)}… · FP16 needs DLL ≥ 3.2.2.0"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = onPick,
                enabled = !importing,
                label = { Text(if (dllName != null) "Replace" else "Import") },
            )
        }
        if (importing) {
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text("Extracting shaders…", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun PerformanceCard(
    settings: com.syed.lsfgmali.data.Settings,
    deviceName: String,
    onUpdate: ((com.syed.lsfgmali.data.Settings) -> com.syed.lsfgmali.data.Settings) -> Unit,
    onApplyG85: () -> Unit,
) {
    var target by remember(settings.targetFps) { mutableIntStateOf(settings.targetFps) }
    var flow by remember(settings.flowScale) { mutableStateOf(settings.flowScale) }
    SectionCard(icon = Icons.Filled.Tune, title = "Performance") {
        Text("Capture scale", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CaptureScale.entries.forEach { scale ->
                FilterChip(
                    selected = settings.captureScale == scale,
                    onClick = { onUpdate { it.copy(captureScale = scale) } },
                    label = { Text("${scale.label}") },
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Frame pacing", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = settings.fpsMode == FpsMode.FIXED,
                onClick = { onUpdate { it.copy(fpsMode = FpsMode.FIXED) } },
                label = { Text("Fixed") },
            )
            listOf(2, 3, 4).forEach { multiplier ->
                FilterChip(
                    selected = settings.fpsMode == FpsMode.FIXED && settings.multiplier == multiplier,
                    onClick = { onUpdate { it.copy(fpsMode = FpsMode.FIXED, multiplier = multiplier) } },
                    label = { Text("${multiplier}×") },
                )
            }
            FilterChip(
                selected = settings.fpsMode == FpsMode.TARGET,
                onClick = { onUpdate { it.copy(fpsMode = FpsMode.TARGET) } },
                label = { Text("Target") },
            )
        }
        if (settings.fpsMode == FpsMode.TARGET) {
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Target ${target} fps", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Text("15–144", style = MaterialTheme.typography.labelSmall)
            }
            Slider(
                value = target.toFloat(),
                onValueChange = { target = it.roundToInt() },
                onValueChangeFinished = {
                    onUpdate { it.copy(targetFps = target, fpsMode = FpsMode.TARGET) }
                },
                valueRange = 15f..144f,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 45, 60, 90, 120).forEach { fps ->
                    FilterChip(
                        selected = target == fps,
                        onClick = {
                            target = fps
                            onUpdate { it.copy(targetFps = fps, fpsMode = FpsMode.TARGET) }
                        },
                        label = { Text("$fps") },
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Flow scale ${"%.2f".format(flow)}", style = MaterialTheme.typography.labelMedium)
        Slider(
            value = flow,
            onValueChange = { flow = it },
            onValueChangeFinished = { onUpdate { it.copy(flowScale = flow) } },
            valueRange = 0.3f..1f,
        )
        Spacer(Modifier.height(8.dp))
        ToggleRow(
            title = "Performance mode",
            subtitle = "Lighter shader path, best for Bifrost",
            checked = settings.performance,
            onChange = { v -> onUpdate { it.copy(performance = v) } },
        )
        ToggleRow(
            title = "Adaptive multiplier",
            subtitle = "Raise the internal multiplier when the source is slow",
            checked = settings.adaptive,
            onChange = { v -> onUpdate { it.copy(adaptive = v) } },
        )
        ToggleRow(
            title = "FP16 when supported",
            subtitle = "Auto-detected from the imported DLL",
            checked = settings.fp16Auto,
            onChange = { v -> onUpdate { it.copy(fp16Auto = v) } },
        )
        ToggleRow(
            title = "Anti-artifacts",
            subtitle = "Reduce halos around UI text",
            checked = settings.antiArtifacts,
            onChange = { v -> onUpdate { it.copy(antiArtifacts = v) } },
        )
        ToggleRow(
            title = "HDR",
            subtitle = "Only when the game and display are HDR",
            checked = settings.hdr,
            onChange = { v -> onUpdate { it.copy(hdr = v) } },
        )
        if (deviceName.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(Modifier.height(8.dp))
            Text("Detected device: $deviceName", style = MaterialTheme.typography.labelSmall)
            TextButtonRow(text = "Apply Helio G85 profile", onClick = onApplyG85)
        }
    }
}

@Composable
private fun ControlsCard(
    settings: com.syed.lsfgmali.data.Settings,
    accessibilityReady: Boolean,
    overlayGranted: Boolean,
    notificationsGranted: Boolean,
    onUpdate: ((com.syed.lsfgmali.data.Settings) -> com.syed.lsfgmali.data.Settings) -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenOverlay: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenBattery: () -> Unit,
    onBubbleChange: (Boolean) -> Unit,
    onTouchBridgeBlocked: () -> Unit,
) {
    SectionCard(icon = Icons.Filled.AccessibilityNew, title = "Touch & overlay") {
        ToggleRow(
            title = "Touch bridge",
            subtitle = if (accessibilityReady) {
                "Replays gestures for ROMs that filter touches over overlays"
            } else {
                "Accessibility service is off — enable it to use the bridge"
            },
            checked = settings.touchBridge,
            onChange = { enabled ->
                if (enabled && !accessibilityReady) {
                    onUpdate { it.copy(touchBridge = false) }
                    onTouchBridgeBlocked()
                    onOpenAccessibility()
                } else {
                    onUpdate { it.copy(touchBridge = enabled) }
                }
            },
        )
        if (settings.touchBridge && !accessibilityReady) {
            Text(
                "Touch bridge was saved but is inactive until the service is enabled.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(4.dp))
        ToggleRow(
            title = "Floating bubble",
            subtitle = "Draggable control bubble above the game",
            checked = settings.showBubble,
            onChange = onBubbleChange,
        )
        Spacer(Modifier.height(8.dp))
        PermissionRow(
            icon = Icons.Filled.Layers,
            title = "Display over other apps",
            granted = overlayGranted,
            action = "Grant",
            onAction = onOpenOverlay,
        )
        PermissionRow(
            icon = Icons.Filled.Notifications,
            title = "Notifications",
            granted = notificationsGranted,
            action = "Settings",
            onAction = onOpenNotifications,
        )
        TextButtonRow(text = "Ignore battery optimization", onClick = onOpenBattery)
    }
}

@Composable
private fun DriverCard(
    driver: DriverMode,
    onSelect: (DriverMode) -> Unit,
) {
    SectionCard(icon = Icons.Filled.Memory, title = "Graphics driver") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = driver == DriverMode.PANVK,
                onClick = { onSelect(DriverMode.PANVK) },
                label = { Text("PanVK (bundled)") },
            )
            FilterChip(
                selected = driver == DriverMode.STOCK,
                onClick = { onSelect(DriverMode.STOCK) },
                label = { Text("Stock Mali") },
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "PanVK is an in-app Vulkan ICD, no root required. Stock Mali uses the system driver and is the fallback if PanVK cannot initialise.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Restart the app after switching — the Vulkan loader reads this at process start.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun DeviceCard(
    device: com.syed.lsfgmali.data.DeviceProfile,
    autoProfile: Boolean,
    onAutoProfile: (Boolean) -> Unit,
    onApplyG85: () -> Unit,
) {
    SectionCard(icon = Icons.Filled.Info, title = "Device") {
        Text("${device.manufacturer} ${device.model}", fontWeight = FontWeight.Medium)
        Text(
            "${device.soc.ifBlank { "SoC unknown" }} · ${device.gpuShort} · Android ${device.androidRelease} (SDK ${device.sdkInt})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "kernel ${device.kernel} · ${device.abi}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        ToggleRow(
            title = "Auto device profile",
            subtitle = "Apply a safe profile once on first launch",
            checked = autoProfile,
            onChange = onAutoProfile,
        )
        if (device.isHelioG85) {
            TextButtonRow(text = "Apply Helio G85 profile", onClick = onApplyG85)
        }
    }
}

@Composable
private fun LogCard(onShareLog: () -> Unit) {
    val context = LocalContext.current
    SectionCard(icon = Icons.Filled.BugReport, title = "Diagnostics") {
        Text(
            "Every session writes a rotating log with the engine counters and device info. Share it if something fails.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onShareLog) {
                Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Share latest")
            }
            OutlinedButton(onClick = { LogSaver.shareLatest(context) }) {
                Text(if (LogSaver.isRunning) "Saving on" else "Saving off")
            }
        }
    }
}

@Composable
private fun AboutCard(gpu: String, android: String, sdk: Int) {
    SectionCard(icon = Icons.Filled.Info, title = "About") {
        Text("LSFG Mali ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})", fontWeight = FontWeight.Medium)
        Text(
            "Lossless Scaling frame generation for Mali GPUs. Engine: lsfg-vk (MIT). Driver: PanVK (MIT). Shell: GPL-3.0.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "$gpu · Android $android (SDK $sdk)",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun Metric(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(" $unit", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun PermissionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    granted: Boolean,
    action: String,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        if (granted) {
            Icon(Icons.Filled.CheckCircle, contentDescription = "Granted", tint = Color(0xFF2EE59D), modifier = Modifier.size(18.dp))
        } else {
            OutlinedButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
private fun TextButtonRow(text: String, onClick: () -> Unit) {
    Spacer(Modifier.height(6.dp))
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(text) }
}

@Composable
private fun StatusPill(phase: SessionPhase) {
    val (label, color) = when (phase) {
        SessionPhase.RUNNING -> "Running" to Color(0xFF2EE59D)
        SessionPhase.STARVED -> "Waiting" to Color(0xFFFFB020)
        SessionPhase.STARTING -> "Starting" to Color(0xFF6FA8FF)
        SessionPhase.ERROR -> "Error" to Color(0xFFFF5252)
        SessionPhase.STOPPING -> "Stopping" to MaterialTheme.colorScheme.outline
        SessionPhase.IDLE -> "Idle" to MaterialTheme.colorScheme.outline
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.16f),
        contentColor = color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .background(color, CircleShape),
            )
            Spacer(Modifier.width(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun notificationsAllowed(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private data class DllImport(
    val name: String,
    val sha: String,
    val version: String?,
    val fp16: Boolean,
    val probeRc: Int,
)

private fun importDll(context: Context, uri: Uri): DllImport? = runCatching {
    val destination = Paths.dllCopy(context)
    LogBus.log("DLL import: source uri=$uri")
    val input = context.contentResolver.openInputStream(uri) ?: error("Cannot open the selected file")
    input.use { src ->
        destination.outputStream().use { dst -> src.copyTo(dst) }
    }
    val bytes = destination.length()
    val sha = Hash.sha256(destination)
    val version = DllVersion.detect(destination)
    LogBus.log("DLL copied: ${destination.absolutePath} ($bytes bytes)")
    LogBus.log("DLL sha256: $sha")
    LogBus.log("DLL version: ${version ?: "unknown (not a PE image?)"}")

    val cacheDir = Paths.shaderCacheDir(context)
    val cache = cacheDir.absolutePath
    LogBus.log("shader cache: $cache (exists=${cacheDir.isDirectory}, writable=${cacheDir.canWrite()})")
    if (!cacheDir.isDirectory) error("shader cache directory could not be created")

    val extracted = NativeBridge.extractShaders(destination.absolutePath, sha, cache)
    LogBus.log("extractShaders rc=$extracted ${if (extracted == 0) "(ok)" else "(FAILED)"}")
    if (extracted != 0) {
        LogBus.log("cache after failure: ${describeCache(cacheDir)}")
        error("extractShaders rc=$extracted")
    }
    val probe = NativeBridge.probeShaders(cache)
    if (probe != 0) {
        LogBus.log("probeShaders rc=$probe — the driver rejected a shader variant; frame generation still runs on the FP32 path")
    } else {
        LogBus.log("probeShaders rc=0 — every frame-gen shader was accepted")
    }
    val fp16 = runCatching { NativeBridge.isFramegenFp16Supported(cache) }.getOrDefault(false)
    LogBus.log("cache contents: ${describeCache(cacheDir)}")
    LogBus.log("FP16 frame-gen shader set present: $fp16")
    val name = displayName(context, uri) ?: destination.name
    LogBus.log(
        "Lossless.dll imported: $name${version?.let { " ($it)" } ?: ""} sha=${sha.take(12)}… " +
            "extract=$extracted probe=$probe fp16=$fp16",
    )
    DllImport(name, sha, version, fp16, probe)
}.getOrElse {
    LogBus.log("Lossless.dll import failed: ${it.message}")
    null
}

private fun describeCache(dir: File): String {
    val top = dir.listFiles()?.count { it.isFile } ?: 0
    val fp16 = File(dir, "fp16").listFiles()?.count { it.isFile } ?: 0
    val fp32 = File(dir, "fp32").listFiles()?.count { it.isFile } ?: 0
    return "top=$top fp16=$fp16 fp32=$fp32"
}

private fun displayName(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    }
}.getOrNull()

package com.example.networkintelligence.presentation.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Sensors
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.NetworkMeasurement
import com.example.networkintelligence.presentation.common.rememberMonitoringPermissions
import com.example.networkintelligence.ui.theme.InterFamily
import com.example.networkintelligence.ui.theme.JetBrainsMonoFamily
import com.example.networkintelligence.ui.theme.KS_Outline
import com.example.networkintelligence.ui.theme.KS_OutlineVariant
import com.example.networkintelligence.ui.theme.KS_Primary
import com.example.networkintelligence.ui.theme.KS_Secondary
import com.example.networkintelligence.ui.theme.KS_Surface
import com.example.networkintelligence.ui.theme.KS_SurfaceContainer
import com.example.networkintelligence.ui.theme.KS_SurfaceContainerHigh
import com.example.networkintelligence.ui.theme.KS_SurfaceContainerHighest
import com.example.networkintelligence.ui.theme.KS_SurfaceContainerLow
import com.example.networkintelligence.ui.theme.KS_SurfaceContainerLowest
import com.example.networkintelligence.ui.theme.KS_Tertiary
import com.example.networkintelligence.ui.theme.SpaceGroteskFamily
import com.google.accompanist.permissions.ExperimentalPermissionsApi

private val MonoStyle @Composable get() = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    letterSpacing = 0.5.sp,
    color = KS_Outline,
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    onMeasurementDone: (NetworkMeasurement) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val permissions = rememberMonitoringPermissions()

    LaunchedEffect(state) {
        if (state is MeasureState.Done) {
            onMeasurementDone((state as MeasureState.Done).measurement)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KS_Surface)
            .padding(contentPadding),
    ) {
        if (state !is MeasureState.Done) {
            TechnicalBackground()
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(8.dp))
            KineticTopBar()

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (val s = state) {
                    is MeasureState.Idle -> IdleContent(
                        permissionsGranted = permissions.allPermissionsGranted,
                        onMeasure = viewModel::measure,
                        onRequestPermissions = { permissions.launchMultiplePermissionRequest() },
                    )
                    is MeasureState.Measuring -> MeasuringContent()
                    is MeasureState.Done -> DoneContent(s.measurement, onMeasureAgain = viewModel::measure)
                    is MeasureState.Error -> ErrorContent(s.message, viewModel::measure)
                }
            }
        }
    }
}

// ── Top Bar ─────────────────────────────────────────────────────────────────

@Composable
private fun KineticTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KS_Surface.copy(alpha = 0.85f))
            .border(width = 0.5.dp, color = KS_OutlineVariant.copy(alpha = 0.15f), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Outlined.Hub, contentDescription = null, tint = KS_Primary, modifier = Modifier.size(20.dp))
            Text(
                text = "KINETIC_SIGNAL",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 3.sp,
                ),
                color = KS_Primary,
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(KS_SurfaceContainerHighest)
                .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "R",
                style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = KS_Primary,
            )
        }
    }
}

// ── Technical Grid Background ────────────────────────────────────────────────

@Composable
private fun TechnicalBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridColor = KS_Outline.copy(alpha = 0.05f)
        val step = 40.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), 1f)
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1f)
            y += step
        }
    }
}

// ── IDLE STATE ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun IdleContent(
    permissionsGranted: Boolean,
    onMeasure: () -> Unit,
    onRequestPermissions: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idle")

    // Pulsing rings (ping animation)
    val ring1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "ring1a",
    )
    val ring1Scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "ring1s",
    )
    val ring2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "ring2a",
    )
    val ring2Scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "ring2s",
    )
    // Button glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow",
    )
    // Scan line
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "scan",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        // System status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(KS_Secondary, CircleShape),
                )
                Text(
                    text = "SYSTEM_ONLINE",
                    style = MonoStyle,
                    color = KS_Secondary,
                )
            }
            Box(
                modifier = Modifier
                    .background(KS_SurfaceContainerHigh, RoundedCornerShape(4.dp))
                    .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = if (permissionsGranted) "READY" else "NEEDS PERMISSIONS",
                    style = MonoStyle,
                    color = if (permissionsGranted) KS_Secondary else KS_Primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Node card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KS_SurfaceContainerLow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(KS_Secondary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .border(0.5.dp, KS_Secondary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Router, contentDescription = null, tint = KS_Secondary, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = "CURRENT_NODE", style = MonoStyle, color = KS_Outline)
                Text(
                    text = "TAP START TO MEASURE",
                    style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Center section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (permissionsGranted) "READY TO" else "PERMISSIONS",
                style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Light, fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = if (permissionsGranted) "ANALYZE" else "REQUIRED",
                style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
                color = KS_Secondary,
            )
            Text(
                text = "Protocol: 802.11ax // Layer-7 Inspection",
                style = MonoStyle,
                color = KS_Outline.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(32.dp))

        // Circular measure button with pulse rings + scan line
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(180.dp),
        ) {
            // Scan line overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = scanY * size.height
                val scanAlpha = if (scanY < 0.1f) scanY / 0.1f else if (scanY > 0.9f) (1f - scanY) / 0.1f else 1f
                drawLine(
                    brush = Brush.horizontalGradient(listOf(Color.Transparent, KS_Secondary.copy(alpha = 0.4f * scanAlpha), Color.Transparent)),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            // Pulse ring 2 (outer)
            Box(
                modifier = Modifier
                    .size(180.dp * ring2Scale)
                    .border(1.dp, KS_Secondary.copy(alpha = ring2Alpha * 0.5f), CircleShape),
            )
            // Pulse ring 1 (inner)
            Box(
                modifier = Modifier
                    .size(180.dp * ring1Scale)
                    .border(1.dp, KS_Secondary.copy(alpha = ring1Alpha), CircleShape),
            )

            // Main button
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(KS_SurfaceContainerLowest)
                    .border(2.dp, KS_Secondary.copy(alpha = glowAlpha), CircleShape)
                    .clickable { if (permissionsGranted) onMeasure() else onRequestPermissions() },
            ) {
                // Radial glow inside
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(KS_Secondary.copy(alpha = 0.08f), Color.Transparent),
                            center = center,
                            radius = size.minDimension / 2,
                        ),
                    )
                }

                // Crosshair decorations
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val tickLen = 8.dp.toPx()
                    val color = KS_Secondary.copy(alpha = 0.4f)
                    val stroke = 1.dp.toPx()
                    drawLine(color, Offset(center.x, 0f), Offset(center.x, tickLen), stroke)
                    drawLine(color, Offset(center.x, size.height - tickLen), Offset(center.x, size.height), stroke)
                    drawLine(color, Offset(0f, center.y), Offset(tickLen, center.y), stroke)
                    drawLine(color, Offset(size.width - tickLen, center.y), Offset(size.width, center.y), stroke)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PlayArrow,
                        contentDescription = "Measure",
                        tint = KS_Secondary,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = if (permissionsGranted) "START" else "ALLOW",
                        style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 3.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Deploying diagnostic packets to measure\ningress and egress latency variance.",
            style = TextStyle(fontFamily = JetBrainsMonoFamily, fontSize = 10.sp),
            color = KS_Outline.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))
    }
}

// ── MEASURING STATE ──────────────────────────────────────────────────────────

@Composable
private fun MeasuringContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "measuring")

    // Radar scan lines — 10s full rotation (from Stitch design)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotation",
    )
    // Middle ring pulse: scale 1→1.05, alpha 0.2→0.4 (from Stitch design)
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseScale",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulseAlpha",
    )
    // Waveform bars: all 10dp→30dp, staggered delays (matching Stitch: 0.1s, 0.3s, 0.2s, 0.4s)
    val w1 by infiniteTransition.animateFloat(10f, 30f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing, delayMillis = 100), RepeatMode.Reverse), label = "w1")
    val w2 by infiniteTransition.animateFloat(10f, 30f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing, delayMillis = 300), RepeatMode.Reverse), label = "w2")
    val w3 by infiniteTransition.animateFloat(10f, 30f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing, delayMillis = 200), RepeatMode.Reverse), label = "w3")
    val w4 by infiniteTransition.animateFloat(10f, 30f, infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing, delayMillis = 400), RepeatMode.Reverse), label = "w4")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))

        // Live scan header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = KS_Primary, size = androidx.compose.ui.geometry.Size(4f, size.height))
                }
                .padding(start = 10.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(KS_Tertiary, CircleShape))
                    Text(
                        text = "LIVE SCAN PATH: ACTIVE",
                        style = MonoStyle,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(text = "SOURCE: MULTI_THREADED_PROBE", style = MonoStyle, color = KS_Outline.copy(alpha = 0.6f))
            }
            Box(
                modifier = Modifier
                    .background(KS_SurfaceContainer, RoundedCornerShape(4.dp))
                    .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(text = "SCANNING...", style = MonoStyle, color = KS_Secondary)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Radar visualization
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(KS_SurfaceContainerLow, RoundedCornerShape(12.dp))
                .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2
                val cy = size.height / 2
                val halfMin = minOf(cx, cy)

                // Dot grid (radial dots, 20dp spacing — matching Stitch)
                val dotColor = KS_Outline.copy(alpha = 0.05f)
                val dotStep = 20.dp.toPx()
                var dx = dotStep / 2
                while (dx < size.width) {
                    var dy = dotStep / 2
                    while (dy < size.height) {
                        drawCircle(dotColor, 1.dp.toPx(), Offset(dx, dy))
                        dy += dotStep
                    }
                    dx += dotStep
                }

                val stroke = Stroke(1.dp.toPx())
                // Outer ring (inset-4 = 16dp from edge)
                drawCircle(KS_OutlineVariant.copy(alpha = 0.2f), halfMin - 16.dp.toPx(), Offset(cx, cy), style = stroke)
                // Middle ring (inset-8 = 32dp, pulsing scale + alpha)
                drawCircle(KS_OutlineVariant.copy(alpha = pulseAlpha), (halfMin - 32.dp.toPx()) * pulseScale, Offset(cx, cy), style = stroke)
                // Inner ring (inset-16 = 64dp)
                drawCircle(KS_OutlineVariant.copy(alpha = 0.4f), halfMin - 64.dp.toPx(), Offset(cx, cy), style = stroke)

                // Two rotating scan lines from center (like radio frequency scanner)
                rotate(rotation, Offset(cx, cy)) {
                    // Line 1: secondary (teal) → right
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, KS_Secondary.copy(alpha = 0.6f)),
                            start = Offset(cx, cy), end = Offset(cx + halfMin, cy),
                        ),
                        start = Offset(cx, cy), end = Offset(cx + halfMin, cy),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                    // Line 2: primary (blue) → down (90° from line 1)
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(Color.Transparent, KS_Primary.copy(alpha = 0.4f)),
                            start = Offset(cx, cy), end = Offset(cx, cy + halfMin),
                        ),
                        start = Offset(cx, cy), end = Offset(cx, cy + halfMin),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }
            }

            // Central metric display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(KS_Surface.copy(alpha = 0.85f))
                    .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.4f), CircleShape),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Downlink Velocity", style = MonoStyle, color = KS_Outline)
                    Text(
                        text = "—",
                        style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 40.sp),
                        color = KS_Primary,
                    )
                    // Waveform bars
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        listOf(w1, w2, w3, w4).forEach { height ->
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(height.dp)
                                    .background(KS_Secondary, RoundedCornerShape(2.dp)),
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(text = "MBPS", style = MonoStyle.copy(letterSpacing = 3.sp), color = KS_Outline)
                }
            }

            // Corner technical labels
            Text(
                text = "STABILITY: —\nERROR_RATE: —",
                style = MonoStyle.copy(fontSize = 8.sp),
                color = KS_Tertiary,
                modifier = Modifier.align(Alignment.TopStart).padding(12.dp),
            )
            Text(
                text = "PACKET_LOSS: —\nLATENCY: —",
                style = MonoStyle.copy(fontSize = 8.sp),
                color = KS_Outline,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
            )
        }

        Spacer(Modifier.height(16.dp))

        // Metrics grid (2x2)
        val metrics = listOf(
            Triple("Latency", "—", "ms"),
            Triple("Signal", "—", "dBm"),
            Triple("Upload", "—", "Mbps"),
            Triple("Download", "—", "Mbps"),
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            metrics.chunked(2).forEach { col ->
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    col.forEach { (label, value, unit) ->
                        MeasuringMetricCard(label = label, value = value, unit = unit)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun MeasuringMetricCard(label: String, value: String, unit: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KS_SurfaceContainer, RoundedCornerShape(4.dp))
            .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(12.dp),
    ) {
        Text(text = label.uppercase(), style = MonoStyle, color = KS_Outline)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = value,
                style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(text = unit, style = MonoStyle, color = KS_Outline, modifier = Modifier.padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(KS_SurfaceContainerHighest, RoundedCornerShape(1.dp)),
        )
    }
}

// ── DONE STATE ───────────────────────────────────────────────────────────────

private fun formatSpeed(mbps: Float): Pair<String, String> = if (mbps >= 1f) {
    "%.1f".format(mbps) to "MBPS"
} else {
    "%.0f".format(mbps * 1000) to "KBPS"
}

@Composable
private fun DoneContent(measurement: NetworkMeasurement, onMeasureAgain: () -> Unit) {
    val score = measurement.score
    val (scoreLabel, scoreColor) = when {
        score >= 80 -> "EXCELLENT" to KS_Tertiary
        score >= 60 -> "GOOD" to KS_Primary
        score >= 40 -> "FAIR" to KS_Secondary
        else -> "POOR" to MaterialTheme.colorScheme.error
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Speed hero card
        SpeedHeroCard(measurement)

        // Score + network info strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KS_SurfaceContainerLow, RoundedCornerShape(6.dp))
                .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "NETWORK_SCORE:", style = MonoStyle.copy(fontSize = 9.sp), color = KS_Outline)
                Text(
                    text = score.toString(),
                    style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp),
                    color = scoreColor,
                )
                Box(
                    modifier = Modifier
                        .background(scoreColor.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = scoreLabel,
                        style = TextStyle(fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 8.sp),
                        color = scoreColor,
                    )
                }
            }
            Text(
                text = "${measurement.networkType} · ${measurement.providerName ?: "?"}",
                style = MonoStyle.copy(fontSize = 9.sp),
                color = KS_Outline,
            )
        }

        // Secondary metrics
        Text(
            text = "LINK PARAMETERS",
            style = MonoStyle.copy(fontSize = 9.sp, letterSpacing = 2.sp),
            color = KS_Outline,
            modifier = Modifier.padding(horizontal = 4.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            measurement.latencyMs?.let { latency ->
                val badge = when { latency < 50 -> "OPTIMAL"; latency < 150 -> "GOOD"; else -> "HIGH" }
                FactorCard(Icons.Outlined.Timer, KS_Secondary, "Latency", "${latency}ms", badge, KS_Tertiary, "High", KS_Tertiary)
            }
            measurement.signalStrengthDbm?.let { dbm ->
                val badge = when { dbm >= -70 -> "STRONG"; dbm >= -85 -> "STABLE"; else -> "WEAK" }
                FactorCard(Icons.Outlined.SignalCellularAlt, KS_Primary, "Signal Strength", "${dbm} dBm", badge, KS_Tertiary, "Medium", KS_Secondary)
            }
            measurement.uploadMbps?.let { ul ->
                val (ulVal, ulUnit) = formatSpeed(ul)
                FactorCard(Icons.Outlined.Sensors, KS_Primary, "Upload Speed", "$ulVal $ulUnit", "ACTIVE", KS_Secondary, "Medium", KS_Secondary)
            }
        }

        // AI diagnosis card
        measurement.diagnosis?.let { diagnosis ->
            AiDiagnosisCard(summary = diagnosis.summary, detail = diagnosis.detail)
        }

        // Measure again button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, KS_Secondary.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .background(KS_Secondary.copy(alpha = 0.06f), RoundedCornerShape(6.dp))
                .clickable { onMeasureAgain() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Sensors,
                    contentDescription = null,
                    tint = KS_Secondary,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "[ RE-RUN_SCAN ]",
                    style = MonoStyle.copy(fontSize = 11.sp, letterSpacing = 2.sp),
                    color = KS_Secondary,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun SpeedHeroCard(measurement: NetworkMeasurement) {
    val (speedVal, speedUnit) = measurement.downloadMbps?.let { formatSpeed(it) } ?: ("—" to "MBPS")
    val latencyStr = measurement.latencyMs?.let { "${it}ms" } ?: "—"
    val uploadStr = measurement.uploadMbps?.let {
        val (v, u) = formatSpeed(it)
        "$v $u"
    } ?: "—"
    val signalStr = measurement.signalStrengthDbm?.let { "${it} dBm" } ?: "—"

    // Derive 4 bar heights (proportional to download speed, max ~1000 Mbps for display)
    val downloadMbps = measurement.downloadMbps ?: 0f
    val norm = (downloadMbps / 1000f).coerceIn(0.05f, 1f)
    val barHeights = listOf(norm * 0.7f, norm * 1.0f, norm * 0.85f, norm * 0.55f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KS_SurfaceContainerLowest, RoundedCornerShape(16.dp))
            .border(1.dp, KS_OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
    ) {
        // Inner decorative nested border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .background(KS_SurfaceContainer.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .padding(20.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Top corner info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "STABILITY: ${if (downloadMbps > 0) "%.1f%%".format(95 + (norm * 4.9f)) else "—"}",
                            style = MonoStyle.copy(fontSize = 8.sp),
                            color = KS_Tertiary,
                        )
                        Text(
                            text = "SIGNAL: $signalStr",
                            style = MonoStyle.copy(fontSize = 8.sp),
                            color = KS_Tertiary,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(KS_SurfaceContainerHigh, RoundedCornerShape(4.dp))
                            .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "CH: ${measurement.networkType}",
                            style = MonoStyle.copy(fontSize = 8.sp, letterSpacing = 0.8.sp),
                            color = KS_Secondary,
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Label
                Text(
                    text = "DOWNLINK VELOCITY",
                    style = MonoStyle.copy(fontSize = 9.sp, letterSpacing = 2.sp),
                    color = KS_Outline,
                )

                Spacer(Modifier.height(8.dp))

                // Big speed number
                Text(
                    text = speedVal,
                    style = TextStyle(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 72.sp,
                        lineHeight = 72.sp,
                        letterSpacing = (-1).sp,
                    ),
                    color = KS_Secondary,
                )

                Spacer(Modifier.height(8.dp))

                // 4 waveform bars
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.Bottom) {
                    barHeights.forEach { fraction ->
                        val h = (6 + fraction * 28).dp
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(h)
                                .background(KS_Secondary, RoundedCornerShape(3.dp)),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Unit
                Text(
                    text = speedUnit,
                    style = MonoStyle.copy(fontSize = 12.sp, letterSpacing = 3.sp),
                    color = KS_Outline,
                )

                Spacer(Modifier.height(20.dp))

                // Bottom stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "LATENCY_OFFSET: $latencyStr",
                        style = MonoStyle.copy(fontSize = 8.sp),
                        color = KS_Outline,
                    )
                    Text(
                        text = "UPLOAD: $uploadStr",
                        style = MonoStyle.copy(fontSize = 8.sp),
                        color = KS_Outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun FactorCard(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    badge: String,
    badgeColor: Color,
    impact: String,
    impactColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(KS_SurfaceContainer.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .border(0.5.dp, KS_OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconTint.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label.uppercase(),
                    style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = value, style = MonoStyle.copy(fontSize = 11.sp), color = KS_Outline)
                    Box(
                        modifier = Modifier
                            .background(badgeColor.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = badge,
                            style = TextStyle(fontFamily = JetBrainsMonoFamily, fontWeight = FontWeight.Bold, fontSize = 8.sp),
                            color = badgeColor,
                        )
                    }
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "Impact", style = MonoStyle.copy(fontSize = 8.sp), color = KS_Outline)
            Text(
                text = impact,
                style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = impactColor,
            )
        }
    }
}

@Composable
private fun AiDiagnosisCard(summary: String, detail: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(KS_Primary.copy(alpha = 0.4f), KS_Secondary.copy(alpha = 0.4f))),
                RoundedCornerShape(16.dp),
            )
            .background(KS_SurfaceContainer.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(KS_SurfaceContainerHighest, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = KS_Primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI SIGNAL DIAGNOSIS",
                    style = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp),
                    color = KS_Primary,
                )
                Text(
                    text = summary,
                    style = TextStyle(fontFamily = InterFamily, fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Text(
                    text = detail,
                    style = TextStyle(fontFamily = InterFamily, fontSize = 11.sp),
                    color = KS_Outline,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = KS_Outline, modifier = Modifier.size(20.dp))
        }
    }
}

// ── ERROR STATE ───────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "[ SCAN_FAILED ]", style = MonoStyle.copy(fontSize = 14.sp, letterSpacing = 2.sp), color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Text(text = message, style = TextStyle(fontFamily = InterFamily, fontSize = 12.sp), color = KS_Outline, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .border(1.dp, KS_Outline, RoundedCornerShape(4.dp))
                .clickable { onRetry() }
                .padding(horizontal = 24.dp, vertical = 10.dp),
        ) {
            Text(text = "[ RETRY_SCAN ]", style = MonoStyle.copy(fontSize = 11.sp, letterSpacing = 2.sp), color = KS_Outline)
        }
    }
}



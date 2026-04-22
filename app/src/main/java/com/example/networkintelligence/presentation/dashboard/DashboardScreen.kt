package com.example.networkintelligence.presentation.dashboard

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.networkintelligence.domain.model.Diagnosis
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.presentation.common.DiagnosisLine
import com.example.networkintelligence.presentation.common.ScoreTrendIndicator
import com.example.networkintelligence.presentation.common.rememberMonitoringPermissions
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DashboardScreen(
    contentPadding: PaddingValues,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions = rememberMonitoringPermissions()
    val permissionsGranted = permissions.allPermissionsGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        HeaderBlock(totalSamples = state.totalSamples)

        ScoreBlock(
            latest = state.latest,
            diagnosis = state.diagnosis,
            scoreDelta = state.scoreDeltaVsOneHourAgo,
        )

        if (!permissionsGranted) {
            PermissionCard(onRequest = { permissions.launchMultiplePermissionRequest() })
        }

        ControlsBlock(
            monitoring = state.monitoring,
            canStart = permissionsGranted,
            onStart = viewModel::startMonitoring,
            onStop = viewModel::stopMonitoring,
            onCaptureNow = viewModel::captureOnce,
        )
    }
}

@Composable
private fun HeaderBlock(totalSamples: Int) {
    Column {
        Text(
            text = "Network Intelligence",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Your signal, quantified.",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "$totalSamples samples collected",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ScoreBlock(
    latest: NetworkSample?,
    diagnosis: Diagnosis?,
    scoreDelta: Int?,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                ),
                shape = RoundedCornerShape(4.dp),
            )
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = if (latest == null) "AWAITING SIGNAL" else "LATEST SCORE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = latest?.score?.toString() ?: "—",
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (scoreDelta != null) {
                    Box(modifier = Modifier.padding(bottom = 16.dp)) {
                        ScoreTrendIndicator(delta = scoreDelta)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = latest?.let { meta(it) } ?: "Trigger a capture to see a reading.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (diagnosis != null) {
                Spacer(Modifier.height(12.dp))
                DiagnosisLine(diagnosis = diagnosis)
            }
        }
    }
}

@Composable
private fun PermissionCard(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(20.dp),
    ) {
        Text(
            text = "Permissions needed",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "We use location to bucket samples into ~1km zones (never raw coordinates) and phone state to read signal strength. Notifications let background work report status on Android 13+.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRequest) { Text("Grant permissions") }
    }
}

@Composable
private fun ControlsBlock(
    monitoring: Boolean,
    canStart: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCaptureNow: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = if (monitoring) "Monitoring active" else "Monitoring paused",
            style = MaterialTheme.typography.titleSmall,
            color = if (monitoring) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = if (monitoring) {
                "A sample is captured roughly every 15 minutes in the background."
            } else {
                "Start monitoring to build a history of your network quality."
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (monitoring) {
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Stop monitoring") }
        } else {
            Button(
                onClick = onStart,
                enabled = canStart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            ) { Text("Start monitoring") }
        }
        OutlinedButton(
            onClick = onCaptureNow,
            enabled = canStart,
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Capture now") }
    }
}

private fun meta(sample: NetworkSample): String {
    val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val when_ = timeFmt.format(Date(sample.timestamp))
    val latency = sample.latencyMs?.let { "${it}ms" } ?: "—"
    val provider = sample.providerName ?: "unknown"
    return "$when_ · ${sample.networkType} · $provider · $latency"
}

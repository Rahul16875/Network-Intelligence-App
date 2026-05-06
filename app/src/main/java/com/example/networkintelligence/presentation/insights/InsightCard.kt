package com.example.networkintelligence.presentation.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.presentation.common.causeLabel

@Composable
fun InsightCard(insight: Insight) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = insight.title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "${insight.evidenceSampleCount} samples",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = insight.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Confidence",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LinearProgressIndicator(
                progress = { insight.confidence },
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = confidenceColor(insight.confidence),
                trackColor = MaterialTheme.colorScheme.surfaceContainerLow,
            )
            Text(
                text = "${(insight.confidence * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        insight.diagnosisCause?.let { cause ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = cause.causeLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = causeColor(cause),
                )
            }
        }
    }
}

@Composable
private fun confidenceColor(confidence: Float) = when {
    confidence >= 0.7f -> MaterialTheme.colorScheme.tertiary
    confidence >= 0.4f -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.primary
}

@Composable
private fun causeColor(cause: DiagnosisCause) = when (cause) {
    DiagnosisCause.HEALTHY -> MaterialTheme.colorScheme.tertiary
    DiagnosisCause.MARGINAL -> MaterialTheme.colorScheme.secondary
    DiagnosisCause.WEAK_SIGNAL,
    DiagnosisCause.PACKET_LOSS,
    DiagnosisCause.DEGRADED_GENERATION,
    DiagnosisCause.NO_NETWORK -> MaterialTheme.colorScheme.error
    DiagnosisCause.CONGESTION -> MaterialTheme.colorScheme.primary
}

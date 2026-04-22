package com.example.networkintelligence.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.networkintelligence.domain.model.Diagnosis
import com.example.networkintelligence.domain.model.DiagnosisCause

@Composable
fun DiagnosisLine(
    diagnosis: Diagnosis,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(
            modifier = Modifier
                .size(8.dp)
                .background(color = diagnosis.cause.dotColor(), shape = CircleShape),
        )
        Text(
            text = diagnosis.summary,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = diagnosis.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DiagnosisCause.dotColor(): Color = when (this) {
    DiagnosisCause.HEALTHY -> MaterialTheme.colorScheme.tertiary
    DiagnosisCause.MARGINAL -> MaterialTheme.colorScheme.secondary
    DiagnosisCause.WEAK_SIGNAL,
    DiagnosisCause.PACKET_LOSS,
    DiagnosisCause.DEGRADED_GENERATION -> MaterialTheme.colorScheme.error
    DiagnosisCause.CONGESTION -> MaterialTheme.colorScheme.primary
    DiagnosisCause.NO_NETWORK -> MaterialTheme.colorScheme.error
}

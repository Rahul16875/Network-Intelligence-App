package com.example.networkintelligence.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.math.absoluteValue

@Composable
fun ScoreTrendIndicator(
    delta: Int?,
    modifier: Modifier = Modifier,
) {
    if (delta == null) return

    val icon = when {
        delta > 2 -> Icons.Outlined.ArrowUpward
        delta < -2 -> Icons.Outlined.ArrowDownward
        else -> Icons.Outlined.Remove
    }
    val label = when {
        delta > 2 -> "+$delta"
        delta < -2 -> "$delta"
        else -> "±${delta.absoluteValue}"
    }
    val tint = when {
        delta > 2 -> MaterialTheme.colorScheme.tertiary
        delta < -2 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .background(
                color = tint.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = " $label vs 1h ago",
            style = MaterialTheme.typography.labelSmall,
            color = tint,
        )
    }
}

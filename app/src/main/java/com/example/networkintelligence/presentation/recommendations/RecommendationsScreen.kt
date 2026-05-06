package com.example.networkintelligence.presentation.recommendations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RecommendationsScreen(
    contentPadding: PaddingValues,
    viewModel: RecommendationsViewModel = hiltViewModel(),
) {
    val measured by viewModel.measuredRecommendations.collectAsStateWithLifecycle()
    val predicted by viewModel.predictedRecommendations.collectAsStateWithLifecycle()
    val isPredictedLoading by viewModel.isPredictedLoading.collectAsStateWithLifecycle()
    val hasPredictedData by viewModel.hasPredictedData.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
    ) {
        item {
            Text(
                text = "Recommendations",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${measured.size + predicted.size} suggestion${if (measured.size + predicted.size == 1) "" else "s"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Measured section
        item {
            SectionHeader(
                title = "Based on your data",
                subtitle = "From ${measured.size} measured recommendation${if (measured.size == 1) "" else "s"}",
            )
        }

        if (measured.isEmpty()) {
            item {
                EmptyState(
                    text = "No recommendations yet.",
                    subtext = "Keep collecting samples — advice will appear once patterns emerge.",
                )
            }
        } else {
            items(measured, key = { it.id }) { rec ->
                RecommendationCard(rec)
            }
        }

        // Predicted section
        item { Spacer(Modifier.height(8.dp)) }

        item {
            SectionHeader(
                title = "Predicted for this area",
                subtitle = "From tower coverage data · Not personal measurements",
            )
        }

        when {
            isPredictedLoading -> {
                item {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }
            !hasPredictedData && predicted.isEmpty() -> {
                item {
                    EmptyState(
                        text = "No tower data available.",
                        subtext = "Enable location access or check internet connectivity to load tower coverage predictions.",
                    )
                }
            }
            else -> {
                items(predicted, key = { it.id }) { rec ->
                    RecommendationCard(rec)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(text: String, subtext: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

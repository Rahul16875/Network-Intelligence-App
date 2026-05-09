package com.example.networkintelligence.presentation.agent

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkintelligence.data.remote.GeminiApi
import com.example.networkintelligence.domain.model.ChatMessage
import com.example.networkintelligence.domain.model.NetworkMeasurement
import com.example.networkintelligence.domain.model.Role
import com.example.networkintelligence.util.APP_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgentUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val inputText: String = "",
)

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val geminiApi: GeminiApi,
) : ViewModel() {

    private val _state = MutableStateFlow(AgentUiState())
    val state: StateFlow<AgentUiState> = _state.asStateFlow()

    private var currentMeasurement: NetworkMeasurement? = null

    fun setMeasurement(measurement: NetworkMeasurement?) {
        currentMeasurement = measurement
    }

    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage(text: String = _state.value.inputText.trim()) {
        if (text.isBlank() || _state.value.isLoading) return

        val userMsg = ChatMessage(role = Role.USER, content = text)
        _state.update { it.copy(messages = it.messages + userMsg, inputText = "", isLoading = true) }

        viewModelScope.launch {
            Log.i(APP_TAG, "AgentVM: sending message: $text")
            val systemContext = buildSystemContext()
            val history = buildHistory(_state.value.messages)
            val reply = geminiApi.generate(systemContext, history)
            Log.i(APP_TAG, "AgentVM: received reply length=${reply.length}")
            val modelMsg = ChatMessage(role = Role.MODEL, content = reply)
            _state.update { it.copy(messages = it.messages + modelMsg, isLoading = false) }
        }
    }

    private fun buildSystemContext(): String {
        val m = currentMeasurement
        return buildString {
            appendLine("You are a helpful network quality assistant for the Network Intelligence Android app.")
            appendLine("You help users understand their internet connection, network providers, and signal quality.")
            appendLine("Keep answers concise (2-4 sentences) and practical. Use plain language, not technical jargon.")
            appendLine()
            if (m != null) {
                appendLine("Current network measurement:")
                appendLine("- Network type: ${m.networkType}")
                appendLine("- Provider: ${m.providerName ?: "unknown"}")
                appendLine("- Signal strength: ${m.signalStrengthDbm?.let { "$it dBm" } ?: "unknown"}")
                appendLine("- Latency: ${m.latencyMs?.let { "${it}ms" } ?: "unknown"}")
                appendLine("- Download speed: ${m.downloadMbps?.let { "%.1f Mbps".format(it) } ?: "unknown"}")
                appendLine("- Upload speed: ${m.uploadMbps?.let { "%.1f Mbps".format(it) } ?: "unknown"}")
                appendLine("- Quality score: ${m.score}/100")
                appendLine("- Diagnosis: ${m.diagnosis?.summary ?: "none"} — ${m.diagnosis?.detail ?: ""}")
            } else {
                appendLine("No network measurement has been taken yet. Encourage the user to tap 'Measure Network' on the Home tab first for personalized advice.")
            }
        }
    }

    private fun buildHistory(messages: List<ChatMessage>): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        var i = 0
        while (i < messages.size) {
            val msg = messages[i]
            if (msg.role == Role.USER) {
                val reply = messages.getOrNull(i + 1)?.takeIf { it.role == Role.MODEL }?.content ?: ""
                result.add(msg.content to reply)
                i += if (reply.isNotEmpty()) 2 else 1
            } else {
                i++
            }
        }
        return result
    }
}

package com.example.networkintelligence.presentation.agent

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.networkintelligence.domain.model.ChatMessage
import com.example.networkintelligence.domain.model.NetworkMeasurement
import com.example.networkintelligence.domain.model.Role
import com.example.networkintelligence.ui.theme.InterFamily
import com.example.networkintelligence.ui.theme.JetBrainsMonoFamily
import com.example.networkintelligence.ui.theme.KS_Outline
import com.example.networkintelligence.ui.theme.KS_OutlineVariant
import com.example.networkintelligence.ui.theme.KS_Primary
import com.example.networkintelligence.ui.theme.KS_Secondary
import com.example.networkintelligence.ui.theme.KS_Surface
import com.example.networkintelligence.ui.theme.KS_SurfaceContainer
import com.example.networkintelligence.ui.theme.KS_SurfaceContainerHigh
import com.example.networkintelligence.ui.theme.KS_SurfaceContainerLow
import com.example.networkintelligence.ui.theme.SpaceGroteskFamily

private val QUICK_QUESTIONS = listOf(
    "Why is my internet slow?",
    "Which network type is best?",
    "What does my score mean?",
    "How can I improve signal?",
    "What's causing high latency?",
    "Is my provider good here?",
)

private val MonoStyle = TextStyle(
    fontFamily = JetBrainsMonoFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 10.sp,
    letterSpacing = 0.5.sp,
    color = KS_Outline,
)

@Composable
fun AgentScreen(
    contentPadding: PaddingValues,
    currentMeasurement: NetworkMeasurement?,
    viewModel: AgentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(currentMeasurement) {
        viewModel.setMeasurement(currentMeasurement)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KS_Surface)
            .padding(contentPadding)
            .imePadding(),
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KS_Surface)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = KS_Primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "ASK_AI",
                    style = TextStyle(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        letterSpacing = 3.sp,
                    ),
                    color = KS_Primary,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (currentMeasurement != null)
                    "> CONTEXT: latest measurement loaded // score=${currentMeasurement.score}"
                else
                    "> CONTEXT: no measurement // run a scan for personalized analysis",
                style = MonoStyle.copy(fontSize = 9.sp, letterSpacing = 0.3.sp),
                color = if (currentMeasurement != null) KS_Secondary else KS_Outline,
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(KS_OutlineVariant.copy(alpha = 0.2f)),
        )

        // Quick questions (shown when no messages)
        if (state.messages.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(QUICK_QUESTIONS) { question ->
                    QuickChip(text = question, onClick = { viewModel.sendMessage(question) })
                }
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(KS_OutlineVariant.copy(alpha = 0.2f)),
            )
        }

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(state.messages) { message ->
                MessageBubble(message)
            }
            if (state.isLoading) {
                item { TypingIndicator() }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KS_SurfaceContainerLow)
                .border(width = 1.dp, color = KS_OutlineVariant.copy(alpha = 0.25f))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = ">",
                style = MonoStyle.copy(fontSize = 12.sp),
                color = KS_Secondary,
                modifier = Modifier.padding(end = 2.dp),
            )
            BasicTextField(
                value = state.inputText,
                onValueChange = viewModel::onInputChange,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFDFE3E9),
                    letterSpacing = 0.3.sp,
                ),
                cursorBrush = SolidColor(KS_Secondary),
                maxLines = 4,
                decorationBox = { innerTextField ->
                    Box {
                        if (state.inputText.isEmpty()) {
                            Text(
                                text = "enter command // ask about your network...",
                                style = MonoStyle.copy(fontSize = 12.sp),
                            )
                        }
                        innerTextField()
                    }
                },
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = if (state.inputText.isNotBlank() && !state.isLoading)
                            KS_Secondary.copy(alpha = 0.15f) else Color.Transparent,
                        shape = RoundedCornerShape(4.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = if (state.inputText.isNotBlank() && !state.isLoading)
                            KS_Secondary.copy(alpha = 0.4f) else KS_OutlineVariant.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = { viewModel.sendMessage() },
                    enabled = state.inputText.isNotBlank() && !state.isLoading,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (state.inputText.isNotBlank() && !state.isLoading) KS_Secondary else KS_Outline,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(KS_SurfaceContainer, RoundedCornerShape(4.dp))
            .border(1.dp, KS_OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text.uppercase(),
            style = MonoStyle.copy(fontSize = 9.sp, letterSpacing = 0.8.sp),
            color = KS_Outline,
        )
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "typing_alpha",
    )
    Row(
        modifier = Modifier
            .background(KS_SurfaceContainerHigh, RoundedCornerShape(4.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "AI_PROCESSING",
            style = MonoStyle.copy(fontSize = 10.sp, letterSpacing = 1.sp),
            color = KS_Secondary,
            modifier = Modifier.alpha(alpha),
        )
        Text(
            text = "█",
            style = MonoStyle.copy(fontSize = 10.sp),
            color = KS_Secondary,
            modifier = Modifier.alpha(alpha),
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == Role.USER
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        if (isUser) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(KS_Primary.copy(alpha = 0.12f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 2.dp))
                    .border(1.dp, KS_Primary.copy(alpha = 0.3f), RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 2.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Column {
                    Text(
                        text = "YOU",
                        style = MonoStyle.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                        color = KS_Primary.copy(alpha = 0.7f),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = message.content,
                        style = TextStyle(fontFamily = InterFamily, fontSize = 13.sp),
                        color = Color(0xFFDFE3E9),
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .background(KS_SurfaceContainerHigh, RoundedCornerShape(topStart = 2.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                    .border(1.dp, KS_OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(topStart = 2.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = KS_Secondary,
                            modifier = Modifier.size(10.dp),
                        )
                        Text(
                            text = "GEMINI",
                            style = MonoStyle.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                            color = KS_Secondary.copy(alpha = 0.7f),
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = message.content,
                        style = TextStyle(fontFamily = InterFamily, fontSize = 13.sp, lineHeight = 20.sp),
                        color = Color(0xFFDFE3E9),
                    )
                }
            }
        }
    }
}

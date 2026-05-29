package com.mroldl001.mimochat.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mroldl001.mimochat.domain.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MessageBubble(
    message: Message,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val hasThinking = !isUser && !message.reasoningContent.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        if (isUser) {
            UserBubble(message = message)
        } else {
            AssistantBubble(message = message, hasThinking = hasThinking)
        }

        MiuixText(
            text = formatTimestamp(message.timestamp),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(top = 4.dp, start = 6.dp, end = 6.dp)
        )
    }
}

@Composable
private fun UserBubble(message: Message) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.End
    ) {
        MiuixCard(
            modifier = Modifier.widthIn(max = 340.dp),
            cornerRadius = 24.dp,
            colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                color = MiuixTheme.colorScheme.primary,
                contentColor = MiuixTheme.colorScheme.onPrimary
            ),
            insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            MiuixText(
                text = "你",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            MiuixText(
                text = message.content,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onPrimary
            )
            if (message.isFailed) {
                Spacer(modifier = Modifier.height(8.dp))
                BasicComponent(
                    title = "发送失败",
                    summary = "请查看下方错误提示",
                    insideMargin = PaddingValues(0.dp),
                    titleColor = BasicComponentDefaults.titleColor(
                        color = MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                    ),
                    summaryColor = BasicComponentDefaults.summaryColor(
                        color = MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.68f)
                    ),
                )
            }
        }
    }
}

@Composable
private fun AssistantBubble(
    message: Message,
    hasThinking: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 720.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (hasThinking) {
            ThinkingCard(
                reasoningContent = message.reasoningContent!!,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (message.content.isNotBlank()) {
            MiuixCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface
                ),
                insideMargin = PaddingValues(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        MiuixText(
                            text = "助手",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        if (message.isAborted) {
                            MiuixText(
                                text = "已中断",
                                style = MiuixTheme.textStyles.body2,
                                color = Color(0xFFE59252)
                            )
                        } else if (message.isFailed) {
                            MiuixText(
                                text = "输出失败",
                                style = MiuixTheme.textStyles.body2,
                                color = Color(0xFFE05A5A)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                MixedMarkdownLatex(
                    text = message.content,
                    textColor = MiuixTheme.colorScheme.onSurface
                )
            }
        }

        if (message.isStreaming && message.content.isNotBlank()) {
            StreamingIndicator(
                modifier = Modifier.padding(top = 8.dp, start = 4.dp)
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

package com.mroldl001.mimochat.ui.chat.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun StreamingMessageBubble(
    content: String,
    reasoningContent: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        if (reasoningContent.isNotBlank()) {
            ThinkingCard(
                reasoningContent = reasoningContent,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (content.isNotBlank()) {
            MiuixCard(
                modifier = Modifier.widthIn(max = 720.dp),
                cornerRadius = 24.dp,
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface
                ),
                insideMargin = PaddingValues(16.dp)
            ) {
                MiuixText(
                    text = "助手",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
                Spacer(modifier = Modifier.height(8.dp))
                MixedMarkdownLatex(
                    text = content,
                    textColor = MiuixTheme.colorScheme.onSurface
                )
                StreamingIndicator(
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

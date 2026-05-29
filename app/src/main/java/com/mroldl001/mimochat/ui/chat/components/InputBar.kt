package com.mroldl001.mimochat.ui.chat.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.TextField as MiuixTextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun InputBar(
    onSendMessage: (String) -> Unit,
    onStopGenerating: () -> Unit,
    isGenerating: Boolean = false,
    highlighted: Boolean = false,
    accessoryContent: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    val canSend = messageText.isNotBlank() && !isGenerating

    MiuixSurface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (highlighted) {
            MiuixTheme.colorScheme.surfaceContainer
        } else {
            MiuixTheme.colorScheme.surface
        },
        border = BorderStroke(1.dp, MiuixTheme.colorScheme.dividerLine.copy(alpha = if (highlighted) 0.42f else 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            accessoryContent?.invoke()

            MiuixSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.28f))
            ) {
                MiuixTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    label = "说点什么",
                    useLabelAsPlaceholder = true,
                    enabled = !isGenerating,
                    singleLine = false,
                    minLines = 1,
                    maxLines = 4,
                    cornerRadius = 18.dp,
                    textStyle = MiuixTheme.textStyles.main.copy(fontWeight = FontWeight.Medium)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (messageText.isNotBlank() && !isGenerating) {
                        MiuixText(
                            text = "${messageText.length} 字",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                        )
                        Spacer(modifier = Modifier.size(10.dp))
                        MiuixButton(
                            onClick = { messageText = "" },
                            cornerRadius = 18.dp,
                            insideMargin = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            colors = MiuixButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MiuixTheme.colorScheme.onSurface
                            )
                        ) {
                            MiuixText(
                                text = "清空",
                                style = MiuixTheme.textStyles.body1
                            )
                        }
                    }
                }

                val sendColors = if (isGenerating) {
                    MiuixButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.errorContainer,
                        contentColor = MiuixTheme.colorScheme.onErrorContainer
                    )
                } else {
                    MiuixButtonDefaults.buttonColorsPrimary()
                }

                MiuixButton(
                    onClick = {
                        if (isGenerating) {
                            onStopGenerating()
                        } else if (canSend) {
                            onSendMessage(messageText.trim())
                            messageText = ""
                        }
                    },
                    enabled = isGenerating || canSend,
                    cornerRadius = 18.dp,
                    minHeight = 44.dp,
                    insideMargin = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    colors = sendColors,
                    modifier = Modifier.alpha(if (isGenerating || canSend) 1f else 0.64f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MiuixIcon(
                            imageVector = if (isGenerating) MiuixIcons.Pause else MiuixIcons.Send,
                            contentDescription = if (isGenerating) "停止生成" else "发送消息",
                            modifier = Modifier.size(18.dp)
                        )
                        MiuixText(
                            text = if (isGenerating) "停止" else "发送",
                            style = MiuixTheme.textStyles.body1
                        )
                    }
                }
            }
        }
    }
}

package com.mroldl001.mimochat.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MiuixConversationHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    leadingContent: @Composable (() -> Unit)? = null,
    selectorContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable RowScope.() -> Unit = {}
) {
    MiuixCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer,
            contentColor = MiuixTheme.colorScheme.onSurface
        ),
        insideMargin = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingContent?.let {
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        it()
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MiuixText(
                        text = title,
                        style = MiuixTheme.textStyles.title1,
                        color = MiuixTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    MiuixText(
                        text = subtitle,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = trailingContent
                )
            }

            selectorContent?.let {
                MiuixSurface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MiuixTheme.colorScheme.surfaceContainerHigh
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        it()
                    }
                }
            }
        }
    }
}

@Composable
fun MiuixConversationFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(28.dp)

    MiuixSurface(
        modifier = modifier.fillMaxSize(),
        shape = shape,
        color = MiuixTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MiuixTheme.colorScheme.dividerLine.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MiuixTheme.colorScheme.surfaceContainer),
            content = content
        )
    }
}

@Composable
fun MiuixComposerPanel(
    title: String,
    summary: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    MiuixCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer,
            contentColor = MiuixTheme.colorScheme.onSurface
        ),
        insideMargin = PaddingValues(top = 14.dp, bottom = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 5.dp)
                        .background(
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(999.dp)
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    MiuixText(
                        text = title,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    MiuixText(
                        text = summary,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content
            )
        }
    }
}

@Composable
fun MiuixFrostedSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape,
    tint: Color,
    borderColor: Color
) {
    MiuixSurface(
        modifier = modifier,
        shape = shape,
        color = tint,
        border = BorderStroke(1.dp, borderColor)
    ) {}
}

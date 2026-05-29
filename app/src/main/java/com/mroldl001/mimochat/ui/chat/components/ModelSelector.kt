
package com.mroldl001.mimochat.ui.chat.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mroldl001.mimochat.domain.model.AIModel
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.ArrowRight
import top.yukonga.miuix.kmp.icon.basic.Check
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ModelSelector(
    currentModel: AIModel?,
    models: List<AIModel>,
    onModelSelected: (AIModel) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val titleColor = MiuixTheme.colorScheme.onSurface
    val subtitleColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val rotation by animateFloatAsState(
        targetValue = if (showBottomSheet) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "arrow_rotation"
    )

    MiuixCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = if (compact) 22.dp else 24.dp,
        onClick = { showBottomSheet = true },
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer,
            contentColor = titleColor
        ),
        insideMargin = PaddingValues(
            horizontal = 18.dp,
            vertical = if (compact) 12.dp else 14.dp
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MiuixText(
                    text = if (compact) "模型" else "当前模型",
                    style = MiuixTheme.textStyles.body2,
                    color = subtitleColor
                )
                MiuixText(
                    text = currentModel?.name ?: "请选择模型",
                    style = if (compact) MiuixTheme.textStyles.body1 else MiuixTheme.textStyles.title3,
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
            MiuixIcon(
                imageVector = MiuixIcons.Basic.ArrowRight,
                contentDescription = "选择模型",
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                modifier = Modifier.rotate(rotation + 90f)
            )
        }
    }

    OverlayBottomSheet(
        show = showBottomSheet,
        title = "模型选择",
        onDismissRequest = { showBottomSheet = false },
        backgroundColor = MiuixTheme.colorScheme.surface,
        enableNestedScroll = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 24.dp)
        ) {
            MiuixCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 28.dp,
                colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    contentColor = titleColor
                ),
                insideMargin = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MiuixText(
                        text = "为当前会话切换模型",
                        style = MiuixTheme.textStyles.title2,
                        color = titleColor,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    MiuixText(
                        text = "不离开当前上下文，直接在宿主内切换可用模型。",
                        style = MiuixTheme.textStyles.body2,
                        color = subtitleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.height(350.dp)
            ) {
                items(models, key = { it.id }) { model ->
                    ModelItem(
                        model = model,
                        isSelected = model.id == currentModel?.id,
                        onClick = {
                            onModelSelected(model)
                            showBottomSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModelItem(
    model: AIModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300),
        label = "text_color"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MiuixTheme.colorScheme.surfaceContainerHighest
        } else {
            MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.72f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "container_color"
    )

    val fontWeight by animateIntAsState(
        targetValue = if (isSelected) 700 else 400,
        animationSpec = tween(durationMillis = 300),
        label = "font_weight"
    )

    val fontSize by animateFloatAsState(
        targetValue = if (isSelected) 20f else 16f,
        animationSpec = tween(durationMillis = 300),
        label = "font_size"
    )

    MiuixCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        cornerRadius = 24.dp,
        onClick = onClick,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = containerColor,
            contentColor = textColor
        ),
        insideMargin = PaddingValues(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                MiuixText(
                    text = model.name,
                    style = MiuixTheme.textStyles.title3.copy(fontSize = fontSize.sp),
                    fontWeight = FontWeight(fontWeight),
                    color = textColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                MiuixText(
                    text = if (isSelected) "当前正在使用" else "点按即可切换",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
            if (isSelected) {
                MiuixIcon(
                    imageVector = MiuixIcons.Basic.Check,
                    contentDescription = null,
                    tint = textColor
                )
            }
        }
    }
}

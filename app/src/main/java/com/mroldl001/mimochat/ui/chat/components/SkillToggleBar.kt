package com.mroldl001.mimochat.ui.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mroldl001.mimochat.ui.chat.viewmodel.SkillType
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SkillToggleBar(
    isThinkingMode: Boolean,
    activeSkill: SkillType?,
    isGenerating: Boolean,
    onThinkingModeToggle: (Boolean) -> Unit,
    onSkillToggle: (SkillType?) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkillModeCard(
            icon = MiuixIcons.Tune,
            title = "深度思考",
            summary = "拉长推理链，适合复杂问题",
            selected = activeSkill == null && isThinkingMode,
            enabled = !isGenerating,
            compact = compact,
            onClick = {
                if (isThinkingMode) {
                    onThinkingModeToggle(false)
                } else {
                    onThinkingModeToggle(true)
                    onSkillToggle(null)
                }
            }
        )
        SkillModeCard(
            icon = MiuixIcons.Edit,
            title = "文风润色",
            summary = "强化措辞、语气和表达质感",
            selected = activeSkill == SkillType.POET,
            enabled = !isGenerating,
            compact = compact,
            onClick = {
                if (activeSkill == SkillType.POET) {
                    onSkillToggle(null)
                } else {
                    onThinkingModeToggle(false)
                    onSkillToggle(SkillType.POET)
                }
            }
        )
        SkillModeCard(
            icon = MiuixIcons.Messages,
            title = "学习模式",
            summary = "更偏讲解和循序引导",
            selected = activeSkill == SkillType.LEARNING,
            enabled = !isGenerating,
            compact = compact,
            onClick = {
                if (activeSkill == SkillType.LEARNING) {
                    onSkillToggle(null)
                } else {
                    onThinkingModeToggle(false)
                    onSkillToggle(SkillType.LEARNING)
                }
            }
        )
    }
}

@Composable
private fun SkillModeCard(
    icon: ImageVector,
    title: String,
    summary: String,
    selected: Boolean,
    enabled: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    val cardColor = when {
        selected -> MiuixTheme.colorScheme.surfaceContainerHighest
        else -> MiuixTheme.colorScheme.surfaceContainer
    }
    val titleColor = when {
        !enabled -> MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.55f)
        selected -> MiuixTheme.colorScheme.primary
        else -> MiuixTheme.colorScheme.onSurface
    }

    MiuixCard(
        modifier = Modifier.widthIn(min = if (compact) 136.dp else 152.dp),
        cornerRadius = if (compact) 20.dp else 22.dp,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = cardColor,
            contentColor = titleColor
        ),
        onClick = if (enabled) onClick else null,
        insideMargin = PaddingValues(
            horizontal = 14.dp,
            vertical = if (compact) 12.dp else 14.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiuixSurface(
                    shape = CircleShape,
                    color = if (selected) {
                        MiuixTheme.colorScheme.primary.copy(alpha = 0.16f)
                    } else {
                        MiuixTheme.colorScheme.surfaceContainerHigh
                    }
                ) {
                    Row(
                        modifier = Modifier.size(34.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MiuixIcon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = titleColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    MiuixText(
                        text = title,
                        style = MiuixTheme.textStyles.body1,
                        color = titleColor
                    )
                    if (selected) {
                        MiuixText(
                            text = "已启用",
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (!compact) {
                MiuixText(
                    text = summary,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary
                )
            }
        }
    }
}

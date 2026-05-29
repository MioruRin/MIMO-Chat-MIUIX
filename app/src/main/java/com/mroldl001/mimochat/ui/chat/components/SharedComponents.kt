package com.mroldl001.mimochat.ui.chat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mroldl001.mimochat.domain.model.WebSearchResult
import dev.jeziellago.compose.markdowntext.MarkdownText
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import top.yukonga.miuix.kmp.basic.Surface as MiuixSurface
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.theme.MiuixTheme

enum class ContentType {
    MARKDOWN,
    CODE_BLOCK,
    INLINE_CODE,
    LATEX_INLINE,
    LATEX_BLOCK
}

data class ContentSegment(
    val type: ContentType,
    val content: String
)

fun splitContent(text: String): List<ContentSegment> {
    val segments = mutableListOf<ContentSegment>()
    val codeBlockRegex = Regex("```([\\s\\S]*?)```")
    val latexBlockRegex = Regex("""\$\$([\s\S]*?)\$\$""")
    val allMatches = mutableListOf<MatchInfo>()

    codeBlockRegex.findAll(text).forEach { match ->
        allMatches.add(MatchInfo(match.range, match, ContentType.CODE_BLOCK))
    }
    latexBlockRegex.findAll(text).forEach { match ->
        allMatches.add(MatchInfo(match.range, match, ContentType.LATEX_BLOCK))
    }

    allMatches.sortBy { it.range.first }
    var lastEnd = 0

    allMatches.forEach { info ->
        if (info.range.first < lastEnd) return@forEach

        if (info.range.first > lastEnd) {
            val mdText = text.substring(lastEnd, info.range.first)
            if (mdText.isNotBlank()) {
                segments.add(ContentSegment(ContentType.MARKDOWN, mdText))
            }
        }

        val groupValue = info.match.groupValues.getOrNull(1) ?: ""
        when (info.type) {
            ContentType.CODE_BLOCK -> {
                val code = groupValue.trim()
                if (code.isNotBlank()) segments.add(ContentSegment(ContentType.CODE_BLOCK, code))
            }

            ContentType.LATEX_BLOCK -> {
                val latex = groupValue.trim()
                if (latex.isNotBlank()) segments.add(ContentSegment(ContentType.LATEX_BLOCK, latex))
            }

            else -> Unit
        }

        lastEnd = info.range.last + 1
    }

    if (lastEnd < text.length) {
        val remaining = text.substring(lastEnd)
        if (remaining.isNotBlank()) {
            segments.add(ContentSegment(ContentType.MARKDOWN, remaining))
        }
    }

    return segments
}

private data class MatchInfo(
    val range: IntRange,
    val match: MatchResult,
    val type: ContentType
)

@Composable
fun ThinkingCard(
    reasoningContent: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "reasoning_expand"
    )

    MiuixCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        onClick = { isExpanded = !isExpanded },
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainerHigh,
            contentColor = MiuixTheme.colorScheme.onSurface
        ),
        insideMargin = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiuixIcon(
                imageVector = MiuixIcons.Tune,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            MiuixText(
                text = "深度思考",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.weight(1f)
            )
            MiuixIcon(
                imageVector = MiuixIcons.ExpandMore,
                contentDescription = if (isExpanded) "收起" else "展开",
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation)
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(10.dp))
            MiuixText(
                text = reasoningContent,
                style = MiuixTheme.textStyles.body1,
                color = MiuixTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun SearchResultsCard(
    searchResults: List<WebSearchResult>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "search_expand"
    )

    MiuixCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        onClick = { isExpanded = !isExpanded },
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainerHigh,
            contentColor = MiuixTheme.colorScheme.onSurface
        ),
        insideMargin = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiuixIcon(
                imageVector = MiuixIcons.Search,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            MiuixText(
                text = "联网结果",
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.weight(1f)
            )
            MiuixIcon(
                imageVector = MiuixIcons.ExpandMore,
                contentDescription = if (isExpanded) "收起" else "展开",
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(rotation)
            )
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                searchResults.forEach { result ->
                    SearchResultItem(result = result)
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    result: WebSearchResult
) {
    val context = LocalContext.current
    val openLink = {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.url))
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Unable to open link", Toast.LENGTH_SHORT).show()
        }
    }

    MiuixCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        onClick = openLink,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.surfaceContainer,
            contentColor = MiuixTheme.colorScheme.onSurface
        ),
        insideMargin = PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiuixSurface(
                shape = RoundedCornerShape(12.dp),
                color = MiuixTheme.colorScheme.surfaceContainerHighest
            ) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MiuixIcon(
                        imageVector = MiuixIcons.Link,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                result.siteName?.let { siteName ->
                    MiuixText(
                        text = siteName,
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                MiuixText(
                    text = result.title,
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MixedMarkdownLatex(
    text: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val segments by remember(text) {
        derivedStateOf { splitContent(text) }
    }

    Column(modifier = modifier.wrapContentWidth()) {
        segments.forEach { segment ->
            when (segment.type) {
                ContentType.MARKDOWN -> {
                    MarkdownText(
                        markdown = segment.content,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        ),
                        isTextSelectable = true
                    )
                }

                ContentType.CODE_BLOCK -> CodeBlockView(code = segment.content)
                ContentType.LATEX_BLOCK -> LatexView(
                    latex = segment.content,
                    textColor = textColor,
                    isBlock = true
                )

                else -> Unit
            }
        }
    }
}

@Composable
fun StreamingIndicator(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "streaming")
    val indicatorColor = MiuixTheme.colorScheme.primary.copy(alpha = 0.72f)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val dotAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = index * 150, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$index"
            )

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .alpha(dotAlpha)
                    .background(
                        color = indicatorColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun CodeBlockView(
    code: String
) {
    val context = LocalContext.current
    val lines = code.split("\n")
    val language = if (lines.isNotEmpty() && lines[0].isNotBlank() && !lines[0].startsWith(" ")) {
        lines[0]
    } else {
        null
    }
    val codeLines = if (language != null && lines.size > 1) lines.subList(1, lines.size) else lines
    val lineCount = codeLines.size
    val maxLineNumberWidth = lineCount.toString().length

    val copyCode = {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
    }

    MiuixCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        cornerRadius = 20.dp,
        colors = top.yukonga.miuix.kmp.basic.CardDefaults.defaultColors(
            color = Color(0xFF171A20),
            contentColor = Color(0xFFF5F7FB)
        ),
        insideMargin = PaddingValues(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MiuixText(
                text = language ?: "代码块",
                style = MiuixTheme.textStyles.body2,
                color = Color(0xFF9CA6B5)
            )
            MiuixIconButton(onClick = copyCode) {
                MiuixIcon(
                    imageVector = MiuixIcons.Copy,
                    contentDescription = "复制代码",
                    tint = Color(0xFFD7DEE8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            codeLines.forEachIndexed { index, line ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiuixText(
                        text = (index + 1).toString().padStart(maxLineNumberWidth, ' '),
                        modifier = Modifier.widthIn(min = (maxLineNumberWidth * 10).dp),
                        style = TextStyle(
                            color = Color(0xFF6F7B8C),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    MiuixText(
                        text = line.ifEmpty { " " },
                        style = TextStyle(
                            color = Color(0xFFF4F7FB),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LatexView(
    latex: String,
    textColor: Color,
    isBlock: Boolean
) {
    val hexColor = textColor.toHexString()
    val htmlContent = remember(latex, hexColor, isBlock) {
        buildKaTeXHtml(latex, hexColor, isBlock)
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                settings.apply {
                    javaScriptEnabled = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    setSupportZoom(false)
                    displayZoomControls = false
                }
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://cdn.jsdelivr.net/npm/katex@0.16.44/dist/",
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = if (isBlock) {
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        } else {
            Modifier
                .wrapContentSize()
                .padding(vertical = 2.dp)
        }
    )
}

private fun buildKaTeXHtml(latex: String, textColor: String, isBlock: Boolean): String {
    val displayMode = if (isBlock) "true" else "false"
    val escapedLatex = latex
        .replace("\\", "\\\\")
        .replace("'", "\\'")
        .replace("\n", " ")

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/katex@0.16.44/dist/katex.min.css">
            <script src="https://cdn.jsdelivr.net/npm/katex@0.16.44/dist/katex.min.js"></script>
            <style>
                body {
                    margin: 0;
                    padding: 0;
                    display: flex;
                    ${if (isBlock) "justify-content: center;" else "justify-content: flex-start;"}
                    align-items: center;
                    min-height: ${if (isBlock) "40px" else "24px"};
                    background: transparent;
                }
                #math {
                    color: $textColor;
                }
                .katex { color: $textColor !important; }
            </style>
        </head>
        <body>
            <div id="math"></div>
            <script>
                try {
                    katex.render('$escapedLatex', document.getElementById('math'), {
                        throwOnError: false,
                        displayMode: $displayMode,
                        color: '$textColor'
                    });
                } catch (e) {
                    document.getElementById('math').textContent = '$escapedLatex';
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}

private fun Color.toHexString(): String {
    val r = (this.red * 255).toInt()
    val g = (this.green * 255).toInt()
    val b = (this.blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}

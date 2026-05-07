package com.jurysim.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

@Immutable
enum class AppWindowSize {
    Compact,
    Medium,
    Expanded
}

fun mapWidthSizeClass(widthSizeClass: WindowWidthSizeClass): AppWindowSize {
    return when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> AppWindowSize.Compact
        WindowWidthSizeClass.Medium -> AppWindowSize.Medium
        else -> AppWindowSize.Expanded
    }
}

val LocalAppWindowSize = compositionLocalOf { AppWindowSize.Compact }

@Composable
fun AdaptiveCenteredContent(
    modifier: Modifier = Modifier,
    maxWidth: Dp = adaptiveMaxContentWidth(),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = maxWidth),
            content = content
        )
    }
}

@Composable
fun adaptiveMaxContentWidth(): Dp {
    return when (LocalAppWindowSize.current) {
        AppWindowSize.Compact -> Dp.Unspecified
        AppWindowSize.Medium -> 720.dp
        AppWindowSize.Expanded -> 960.dp
    }
}

package com.stechtricker.youplay.ui.components.themed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stechtricker.youplay.ui.styling.LocalAppearance
import com.stechtricker.youplay.utils.color
import com.stechtricker.youplay.utils.medium

@Composable
fun DialogTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    primary: Boolean = false,
) {
    val (colorPalette, typography) = LocalAppearance.current

    val textColor = when {
        !enabled -> colorPalette.textDisabled
        primary -> colorPalette.onAccent
        else -> colorPalette.text
    }

    BasicText(
        text = text,
        style = typography.xs.medium.color(textColor),
        modifier = modifier
            .clip(RoundedCornerShape(36.dp))
            .background(if (primary) colorPalette.accent else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    )
}

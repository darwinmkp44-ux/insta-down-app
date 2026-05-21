package com.instadown.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instadown.app.ui.theme.GlassBase
import com.instadown.app.ui.theme.GlassBorder
import com.instadown.app.ui.theme.NeonPink
import com.instadown.app.ui.theme.NeonViolet
import com.instadown.app.ui.theme.TextMuted
import com.instadown.app.ui.theme.TextPrimary

/**
 * A beautiful custom Card component simulating frosted glass (Glassmorphism).
 * Includes a subtle transparent background, high-contrast light borders, and rounded corners.
 */
@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(GlassBase)
            .border(
                width = borderWidth,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color.White.copy(alpha = 0.03f),
                        Color.Black.copy(alpha = 0.1f)
                    )
                ),
                shape = shape
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * A sleek button with a glowing background gradient (NeonPink to NeonViolet) and rounded corners.
 */
@Composable
fun GlowingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = if (enabled) listOf(NeonPink, NeonViolet) else listOf(Color(0xFF3A3555), Color(0xFF2C2844))
    )

    Box(
        modifier = modifier
            .clip(shape)
            .background(gradientBrush)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else Color.White.copy(alpha = 0.4f),
            style = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        )
    }
}

/**
 * A transparent text input field simulating glassmorphism.
 * Highlights with a glowing border when selected.
 */
@Composable
fun GlassmorphicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    isFocused: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    
    // Choose border colors based on focus
    val borderBrush = if (isFocused) {
        Brush.horizontalGradient(listOf(NeonPink, NeonViolet))
    } else {
        SolidColor(Color.White.copy(alpha = 0.12f))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassBase)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                color = TextMuted,
                fontSize = 14.sp
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 14.sp),
            cursorBrush = SolidColor(NeonPink),
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}

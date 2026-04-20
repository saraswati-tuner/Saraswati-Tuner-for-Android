package com.tuner.saraswati.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    // For the large Hertz/Pitch display
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Your original font
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        shadow = null
    ),
    // For Menu items and Axis labels
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        shadow = Shadow(
            color = Color.White.copy(alpha = 0.9f),
            offset = Offset(0f, 0f),
            blurRadius = 35f
        )
    )
)
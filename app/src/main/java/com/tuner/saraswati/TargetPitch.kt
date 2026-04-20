package com.tuner.saraswati

import androidx.compose.ui.graphics.Color

enum class TargetPitch(
    val label: String,
    val freq: Float,
    val color: Color
) {
    G_SHARP_2("G#2", 103.83f, Color(0xFFFF0000)), // Red (Root)
    A_2("A2", 110.00f, Color(0xFFFFA500)),       // Orange
    A_SHARP_2("A#2", 116.54f, Color(0xFFFFFF00)), // Yellow
    B_2("B2", 123.47f, Color(0xFF00FF00)),       // Green
    C_3("C3", 130.81f, Color(0xFF00FFFF)),       // Cyan
    C_SHARP_3("C#3",138.59f, Color(0xFF007FFF)), // Brighter, high-contrast Violet
    //C_SHARP_3("C#3", 138.59f, Color(0xFF4B0082)), // Indigo
    D_3("D3", 146.83f, Color(0xFF8F00FF));       // Violet
    //D_3("D3", 146.83f, Color(0xFF8F00FF));       // Violet

    companion object {
        fun fromLabel(label: String): TargetPitch =
            entries.find { it.label == label } ?: G_SHARP_2
    }
}

package com.tuner.saraswati

import androidx.compose.ui.graphics.Color

enum class TargetPitch(
    val label: String,
    val freq: Float,
    val color: Color
) {
    F_LO("F",       87.31f,  Color(0xC0FF3131)),    // Neon Red (Root)
    F_SHARP_LO("F#",92.50f,  Color(0xFFFF3131)),    // Neon Red (Root)
    G_LO("G",       98.00f,  Color(0xC0FFA500)),    // Orange
    G_SHARP_LO("G#",103.83f, Color(0xFFFFA500)),    // Orange
    A_LO("A",       110.00f, Color(0xC0FFFF00)),    // Orange
    A_SHARP_LO("A#",116.54f, Color(0xFFFFFF00)),    // Yellow
    B_LO("B",       123.47f, Color(0xFF00FF00)),    // Green
    C_HI("C",       130.81f, Color(0xF000FFFF)),    // Cyan
    C_SHARP_HI("C#",138.59f, Color(0xFF00FFFF)),    // Cyan
    D_HI("D",       146.83f, Color(0xC000C0FF)),    // Indigo
    D_SHARP_HI("D#",155.56f, Color(0xFF00C0FF)),    // Indigo
    E_HI("E",       164.81f, Color(0xFFFF00FF));    // Magenta/pink


    companion object {
        fun fromLabel(label: String): TargetPitch =
            entries.find { it.label == label } ?: G_SHARP_LO
    }
}

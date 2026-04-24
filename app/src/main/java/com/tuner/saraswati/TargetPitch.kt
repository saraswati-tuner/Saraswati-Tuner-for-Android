package com.tuner.saraswati

import androidx.compose.ui.graphics.Color

enum class TargetPitch(
    val label: String,
    val freq: Float,
    val color: Color
) {
    F2("F2",        87.31f,  Color(0xC0FF3131)),    // Neon Red (Root)
    F_SHARP_2("F#2",92.50f,  Color(0xFFFF3131)),    // Neon Red (Root)
    G("G2",         98.00f,  Color(0xC0FFA500)),    // Orange
    G_SHARP_2("G#2",103.83f, Color(0xFFFFA500)),    // Orange
    A_2("A2",       110.00f, Color(0xC0FFFF00)),    // Orange
    A_SHARP_2("A#2",116.54f, Color(0xFFFFFF00)),    // Yellow
    B_2("B2",       123.47f, Color(0xFF00FF00)),    // Green
    C_3("C3",       130.81f, Color(0xF000FFFF)),    // Cyan
    C_SHARP_3("C#3",138.59f, Color(0xFF00FFFF)),    // Cyan
    D_3("D3",       146.83f, Color(0xC000C0FF)),    // Indigo
    D_SHARP3("D#3", 155.56f, Color(0xFF00C0FF)),    // Indigo
    E_3("E3",       164.81f, Color(0xFFFF00FF));    // Magenta/pink


    companion object {
        fun fromLabel(label: String): TargetPitch =
            entries.find { it.label == label } ?: G_SHARP_2
    }
}

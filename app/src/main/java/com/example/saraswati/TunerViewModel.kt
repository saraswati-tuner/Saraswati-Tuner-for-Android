package com.example.saraswati

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TunerViewModel : ViewModel() {
    val splashDurationMillis = 1000L
    val kattaiMap = mapOf("G#2" to 103.83f, "C#3" to 138.59f)

    // UI State
    var selectedKattai by mutableStateOf("G#2"); private set
    var currentPitch by mutableFloatStateOf(0f); private set

    // Circular Buffer Data
    val maxPoints = 200
    val history = FloatArray(maxPoints) { -1f }
    var writeIndex by mutableIntStateOf(0)
    var triggerToggle by mutableStateOf(false)

    val targetFreq: Float
        get() = kattaiMap[selectedKattai] ?: 103.83f

    fun updateKattai(newKattai: String) {
        selectedKattai = newKattai
    }

    fun updatePitch(newPitch: Float) {
        currentPitch = newPitch
        history[writeIndex] = newPitch
        writeIndex = (writeIndex + 1) % maxPoints
        triggerToggle = !triggerToggle
    }

    fun resetPitch() {
        currentPitch = 0f
        history[writeIndex] = -1f
        writeIndex = (writeIndex + 1) % maxPoints
        triggerToggle = !triggerToggle
    }
}
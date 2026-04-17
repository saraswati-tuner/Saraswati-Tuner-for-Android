package com.example.saraswati

import androidx.compose.runtime.derivedStateOf
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

    // Controls when and how Saraswati bloom appears on sustaining within target zone
    var targetZoneCents by mutableStateOf(5.0f)
    var stabilityMillis by mutableStateOf(0L)
    var bloomThreshold = 5000L
    val isBlooming by derivedStateOf { stabilityMillis >= bloomThreshold } // 5 seconds threshold

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

    private var frameDurationMillis: Long = 0L
    init {
        // Calculate the ms duration of the non-overlapping part of the buffer
        val stepSamples = (AudioConfig.BUFFER_SIZE - AudioConfig.OVERLAP).toDouble()
        frameDurationMillis = ((stepSamples / AudioConfig.SAMPLE_RATE) * 1000).toLong()
    }

    fun updateStability(currentCents: Double) {
        if (kotlin.math.abs(currentCents) <= targetZoneCents) {
            stabilityMillis += frameDurationMillis
        } else {
            stabilityMillis = 0L
        }
    }
}
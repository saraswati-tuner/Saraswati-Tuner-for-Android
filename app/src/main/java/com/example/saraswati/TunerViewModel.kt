package com.example.saraswati

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class SkillLevel(val label: String, val goal: Float) {
    BEGINNER("Beginner", 15f),
    INTERMEDIATE("Intermediate", 5f),
    ADVANCED("Advanced", 2f),
    PROFESSIONAL("Professional", 1f)
}

class TunerViewModel : ViewModel() {
    var currentLevel by mutableStateOf(SkillLevel.BEGINNER) // Default

    val targetPitchOptions = linkedMapOf(
        "F2" to 87.31f,
        "F#2" to 92.50f,
        "G2" to 98.00f,
        "G#2" to 103.83f,
        "A2" to 110.00f,
        "A#2" to 116.54f,
        "B2" to 123.47f,
        "C3" to 130.81f,
        "C#3" to 138.59f,
        "D3" to 146.83f
    )
    var selectedTargetPitch by mutableStateOf("G#2") // Default
    var targetFreq by mutableStateOf(103.83f)

    fun updateTargetPitch(label: String) {
        selectedTargetPitch = label
        targetFreq = targetPitchOptions[label] ?: 103.83f
    }
    val splashDurationMillis = 1000L

    var currentPitch by mutableFloatStateOf(0f); private set

    // Circular Buffer Data
    val maxPoints = 200
    val history = FloatArray(maxPoints) { -1f }
    var writeIndex by mutableIntStateOf(0)
    var triggerToggle by mutableStateOf(false)

    // Controls when and how Saraswati bloom appears on sustaining within target zone
    var targetZoneCents by mutableStateOf(5.0f)
    var stabilityMillis by mutableStateOf(0L)
    var bloomThreshold = 5000L
    val isBlooming by derivedStateOf { stabilityMillis >= bloomThreshold }

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
            //Log.d("Saraswati Tuner", "stabilitymillis = $stabilityMillis")
        } else {
            stabilityMillis = 0L
        }
    }
}
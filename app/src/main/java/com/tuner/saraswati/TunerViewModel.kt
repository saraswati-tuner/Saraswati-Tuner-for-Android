package com.tuner.saraswati

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
    val splashDurationMillis = 1000L
    var currentPitch by mutableFloatStateOf(0f); private set // For display screen bottom

    var currentLevel by mutableStateOf(SkillLevel.BEGINNER)  // Default
    val targetZoneCents by derivedStateOf { currentLevel.goal }

    // Circular Buffer Data
    val maxPoints = 200
    val history = FloatArray(maxPoints) { -1f }
    var writeIndex by mutableIntStateOf(0)
    var triggerToggle by mutableStateOf(false)

    // Controls when and how Saraswati bloom appears on sustaining within target zone
    var stabilityMillis by mutableStateOf(0L)
    var bloomThreshold = 5000L // 5s of continuous hold within targetZone cents
    val isBlooming by derivedStateOf { stabilityMillis >= bloomThreshold }

    var activePitch by mutableStateOf(TargetPitch.G_SHARP_2) // default G#2
        private set

    val targetFreq: Float get() = activePitch.freq
    val selectedTargetPitchLabel: String get() = activePitch.label

    fun updateTargetPitch(newPitchLabel: String) {
        val newPitch = TargetPitch.fromLabel(newPitchLabel)
        if (activePitch != newPitch) {
            activePitch = newPitch
            clearPitchHistory()
            stabilityMillis = 0L // Reset stability on pitch change
        }
    }

    private fun clearPitchHistory() {
        history.fill(-1f)
        writeIndex = 0
        triggerToggle = !triggerToggle
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

    // How long to hold freq for bloom to appear
    private var _frameDurationMillis: Long = 0L

    fun updateStability(currentCents: Double) {
        if (kotlin.math.abs(currentCents) <= targetZoneCents) {
            stabilityMillis += _frameDurationMillis
            //Log.d("Saraswati Tuner", "stabilitymillis = $stabilityMillis, tz = ${targetZoneCents}")
        } else {
            stabilityMillis = 0L
        }
    }

    init {
        // Calculate the ms duration of the non-overlapping part of the buffer
        val stepSamples = (AudioConfig.BUFFER_SIZE - AudioConfig.OVERLAP).toDouble()
        _frameDurationMillis = ((stepSamples / AudioConfig.SAMPLE_RATE) * 1000).toLong()
    }
}
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

// Pre-compute this constant to replace '1200 / log(2)'
private const val LOG2_1200 = 1731.234f

class TunerViewModel : ViewModel() {
    val splashDurationMillis = 1000L

    var isPaused by mutableStateOf(false)
    fun togglePause() { isPaused = !isPaused }

    // 1. TARGET (Set by the User)
    var activePitch by mutableStateOf(TargetPitch.G_SHARP_2)
    val targetFreq: Float get() = activePitch.freq
    val selectedTargetPitchLabel: String get() = activePitch.label

    // 2. LIVE OUTPUTS (Updated by the Engine)
    var rawFrequency by mutableFloatStateOf(0f); private set // Always the gray legend
    var refinedPitch by mutableFloatStateOf(0f); private set // The colored display & trace
    val isPitchInRange by derivedStateOf {
        refinedPitch > 0f && refinedPitch >= lowerBound && refinedPitch <= upperBound
    }

    // Bounds for the "In Range" check, updated on init and on selection
    private var lowerBound = 0f
    private var upperBound = 0f
    private fun updateBounds() { // Called when target changes
        lowerBound = targetFreq * 0.9715f // -50 cents
        upperBound = targetFreq * 1.0293f // +50 cents
    }

    // Call this whenever activePitch changes
    fun updateTargetPitch(newPitchLabel: String) {
        val newPitch = TargetPitch.fromLabel(newPitchLabel)
        if (activePitch != newPitch) {
            activePitch = newPitch
            updateBounds() // Sync the +/- 50 cent walls
            clearPitchHistory()
            stabilityMillis = 0L
        }
    }

    var currentLevel by mutableStateOf(SkillLevel.BEGINNER)  // Default
    val targetZoneCents by derivedStateOf { currentLevel.goal }

    // Circular Buffer Data for drawing pitch history
    val maxPoints = 200
    val history = FloatArray(maxPoints) { -1f }
    var writeIndex by mutableIntStateOf(0)
    var triggerToggle by mutableStateOf(false)
    private val _centsHistory = FloatArray(maxPoints) { 0f }
    val centsHistory: FloatArray get() = _centsHistory

    // Circular buffer and pre-computations for smoothing
    private val pitchWindow = FloatArray(AudioConfig.SMA_WINDOW_SIZE) { 0f }
    private var nextPitchNdx = 0

    // Controls when and how Saraswati bloom appears on sustaining within target zone
    var stabilityMillis by mutableStateOf(0L)
    var bloomThreshold = 10000L // 5s of continuous hold within targetZone cents
    val isBlooming by derivedStateOf { stabilityMillis >= bloomThreshold }

    private fun clearPitchHistory() {
        history.fill(-1f)
        writeIndex = 0
        triggerToggle = !triggerToggle
    }

    // How long to hold freq for bloom to appear
    private var _frameDurationMillis: Long = 0L

    fun updateStability(currentCents: Double) {
        if (kotlin.math.abs(currentCents) <= targetZoneCents) {
            stabilityMillis += _frameDurationMillis
        } else {
            stabilityMillis = 0L
        }
    }

    private fun calculateRefinedMean(window: FloatArray): Float {
        val validPitches = window.filter { it > 0f }.sorted()
        if (validPitches.isEmpty()) return 0f

        // Drop the highest and lowest 10% to eliminate jitter
        val trimCount = (validPitches.size * 0.1).toInt()
        val filtered = validPitches.drop(trimCount).dropLast(trimCount)

        return if (filtered.isNotEmpty()) filtered.average().toFloat() else validPitches.average().toFloat()
    }

    fun processNewPitch(detectedPitch: Float, probability: Float) {
        if (isPaused) return

        var p = detectedPitch
        val target = targetFreq
        var finalSma = 0f
        var finalCents = 0f

        // STAGE 1: RAW HEAR-THROUGH (The Legend at the bottom)
        // If the signal is clear, show it in the legend regardless of cent distance
        if (probability > 0.6f && p > AudioConfig.HEARING_THRESHOLD) {
            while (p < target * 0.707f) p *= 2f
            while (p > target * 1.414f) p /= 2f
            rawFrequency = p
        } else {
            rawFrequency = 0f
        }

        // STAGE 2: REFINEMENT (The Plot & Trace)
        // Only if it's within 5% of our target, we smooth it for the history
        if (rawFrequency in (target * 0.95f)..(target * 1.05f)) {
            pitchWindow[nextPitchNdx] = rawFrequency
            nextPitchNdx = (nextPitchNdx + 1) % AudioConfig.SMA_WINDOW_SIZE

            val sma = calculateRefinedMean(pitchWindow)
            if (sma > 0f) {
                refinedPitch = sma
                val cents = (kotlin.math.log2(sma.toDouble() / target) * 1200).toFloat()

                // Only treat as a "valid" point if within the 50c display limit
                if (kotlin.math.abs(cents) <= 50f) {
                    finalSma = sma
                    finalCents = cents
                }
            }
        }

        history[writeIndex] = finalSma
        _centsHistory[writeIndex] = finalCents
        writeIndex = (writeIndex + 1) % maxPoints

        // Update stability: if finalSma is 0, it resets the 10s timer immediately
        if (finalSma > 0f) {
            updateStability(finalCents.toDouble())
        } else {
            refinedPitch = 0f
            updateStability(Double.MAX_VALUE)
        }
    }

    init {
        updateBounds()

        // Calculate the ms duration of the non-overlapping part of the buffer
        val stepSamples = (AudioConfig.BUFFER_SIZE - AudioConfig.OVERLAP).toDouble()
        _frameDurationMillis = ((stepSamples / AudioConfig.SAMPLE_RATE) * 1000).toLong()
    }
}
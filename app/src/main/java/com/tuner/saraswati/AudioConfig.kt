package com.tuner.saraswati

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.annotation.RequiresPermission

object AudioConfig {
    const val SAMPLE_RATE = 44100
    const val BUFFER_SIZE = 4096
    const val OVERLAP = 2048
    const val MAX_HISTORY_POINTS = 200
    const val SMA_WINDOW_SIZE = 50
    const val HEARING_THRESHOLD = 20f
}

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
fun initializeAudioEngine(): Boolean {
    return try {
        val tempRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            AudioConfig.SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            AudioConfig.BUFFER_SIZE
        )
        val isReady = tempRecord.state == AudioRecord.STATE_INITIALIZED
        tempRecord.release()
        isReady
    } catch (e: Exception) { false }
}
package com.tuner.saraswati

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import be.tarsos.dsp.filters.LowPassFS
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import com.tuner.saraswati.ui.theme.SaraswatiTheme
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContent {
            SaraswatiTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
                    var permissionGranted by remember {
                        mutableStateOf(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                    }
                    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted = it }

                    LaunchedEffect(Unit) {
                        if (!permissionGranted) launcher.launch(Manifest.permission.RECORD_AUDIO)
                    }

                    if (permissionGranted)
                        SaraswatiApp()
                    else Box(contentAlignment = Alignment.Center) {
                        Text("Microphone Permission Required", color = Color.White)
                    }
                }
            }
        }
    }
}

@RequiresPermission(Manifest.permission.RECORD_AUDIO)
@Composable
fun SaraswatiApp(tunerVM: TunerViewModel = viewModel()) {
    val context = LocalContext.current
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    var timerFinished by remember { mutableStateOf(false) }
    var micInitialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(tunerVM.splashDurationMillis); timerFinished = true }
    LaunchedEffect(Unit) { micInitialized = initializeAudioEngine() }

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val readyToProceed = timerFinished && micInitialized

    // Audio Engine Side-Effect (restarts when targetFreq/kattai changes)
    LaunchedEffect(readyToProceed, tunerVM.selectedTargetPitchLabel) {
        if (!readyToProceed) return@LaunchedEffect

        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
            AudioConfig.SAMPLE_RATE,AudioConfig.BUFFER_SIZE, AudioConfig.OVERLAP)

        // Offload all processing to the ViewModel
        val handler = PitchDetectionHandler { result, _ ->
            tunerVM.processNewPitch(result.pitch, result.probability)
        }

        // *2.5f allows up to targetFreq plus some in next octave
        dispatcher.addAudioProcessor(LowPassFS(tunerVM.targetFreq * 2.5f, AudioConfig.SAMPLE_RATE.toFloat()))
        dispatcher.addAudioProcessor(PitchProcessor(PitchEstimationAlgorithm.YIN,
            AudioConfig.SAMPLE_RATE.toFloat(), AudioConfig.BUFFER_SIZE, handler))

        val audioThread = Thread(dispatcher, "AudioEngine")
        audioThread.start()

        // Cleanup on dispose or restart
        try { awaitCancellation() } finally { dispatcher.stop(); audioThread.interrupt() }
    }

    AnimatedContent(
        targetState = readyToProceed,
        transitionSpec = {
            fadeIn(animationSpec = tween(500))
                .togetherWith(fadeOut(animationSpec = tween(500)))
                .apply { targetContentZIndex = 1f }
        },
        label = "உ" // Pillaiyar suzhi
    ) { targetReady ->
        if (targetReady) {
            TunerMainScreen(tunerVM)
        } else {
            GaneshaSplashScreen()
        }
    }
}


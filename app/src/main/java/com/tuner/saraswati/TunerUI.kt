package com.tuner.saraswati

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.log2

// --- UI LAYER: DRAWING & CONTROLS ---
@Composable
fun TunerMainScreen(viewModel: TunerViewModel) {
    // Saraswati Bloom animation state
    val bloomAlpha by animateFloatAsState(
        targetValue = if (viewModel.isBlooming) 0.9f else 0f,
        animationSpec = tween(durationMillis = 2000),
        label = "Saraswati"
    )

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp), // Minimal top padding
            horizontalArrangement = Arrangement.Center, // Center the whole row
            verticalAlignment = Alignment.CenterVertically
        ) {
            TargetPitchDropDown(viewModel)
            Spacer(Modifier.width(24.dp)) // Gap between the two menus
            LevelDropDown(viewModel)
        }
        Box( // Middle - Plot (and bloom) area
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            TunerCanvas(viewModel)

            Image(
                painter = painterResource(id = R.drawable.saraswati_bloom),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp)
                    .alpha(bloomAlpha),
                contentScale = ContentScale.Fit
            )
        }

        // Bottom - (smoothed) freq display
        Text(
            text = if (viewModel.currentPitch > 0) "%.2f Hz".format(viewModel.currentPitch) else "---- Hz",
            fontSize = 20.sp,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 32.dp)
        )
    }
}

@Composable
fun TunerCanvas(viewModel: TunerViewModel) {
    // Tuner background
    Image(
        painter = painterResource(id = R.drawable.nataraja_bg),
        contentDescription = "Nataraja background",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    viewModel.triggerToggle // Subscribes TunerCanvas to watch for changes in triggerToggle

    val targetFreq = viewModel.targetFreq

    Canvas(Modifier.fillMaxSize().padding(vertical = 60.dp)) {
        val centerY = size.height / 2f
        val plotScaleY = size.height / 100f
        val stepX = size.width / viewModel.maxPoints

        // Goal Zone
        val currentGoal = viewModel.currentLevel.goal
        drawRect(
            color = Color.Green.copy(0.25f),
            topLeft = Offset(0f, centerY - currentGoal * plotScaleY),
            size = Size(size.width, (currentGoal * 2) * plotScaleY)
        )

        // Horiz Grid Lines
        for (i in -50..50 step 5) {
            val textPaint = android.graphics.Paint().apply {
                color = if (i == 0) android.graphics.Color.WHITE else android.graphics.Color.LTGRAY
                textSize = 36f // grid labels
                isAntiAlias = true
            }
            val label = if (i == 0) viewModel.selectedTargetPitch else "${i}c"
            val lineStartX = if (i % 10 == 0) textPaint.measureText(label) + 30f else 0f

            val y = centerY - (i * plotScaleY)
            val lineColor = when {
                i == 0 -> Color.White.copy(alpha = 0.8f)
                i % 10 == 0 -> Color.Gray.copy(alpha = 0.7f)
                else -> Color.Gray.copy(alpha = 0.5f)
            }
            drawLine(lineColor, Offset(lineStartX, y), Offset(size.width, y), 1.dp.toPx())

            if (i % 10 == 0) {
                drawIntoCanvas { canvas ->
                    val label = if (i == 0) viewModel.selectedTargetPitch else "${i}c"
                    canvas.nativeCanvas.drawText(label, 10f, y + 10f, textPaint)                }
            }
        }

        // Vert Grid Lines - Matching the horizontal 5-cent step size for perfect squares
        val squareStep = 5 * plotScaleY
        var xPos = 0f
        while (xPos <= size.width) {
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(xPos, 0f),
                end = Offset(xPos, size.height),
                strokeWidth = 0.5.dp.toPx()
            )
            xPos += squareStep
        }

        // Circular Buffer Drawing Logic
        for (i in 0 until viewModel.maxPoints - 1) {
            val currIdx = (viewModel.writeIndex + i) % viewModel.maxPoints
            val nextIdx = (viewModel.writeIndex + i + 1) % viewModel.maxPoints

            val p1 = viewModel.history[currIdx]
            val p2 = viewModel.history[nextIdx]

            if (p1 > 0f && p2 > 0f) {
                val c1 = 1200 * log2(p1 / targetFreq)
                val c2 = 1200 * log2(p2 / targetFreq)
                drawLine(
                    color = Color.Green,
                    start = Offset(i * stepX, centerY - (c1 * plotScaleY)),
                    end = Offset((i + 1) * stepX, centerY - (c2 * plotScaleY)),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun GaneshaSplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splash_logo_ganesha),
            contentDescription = "Ganesha"
        )
    }
}
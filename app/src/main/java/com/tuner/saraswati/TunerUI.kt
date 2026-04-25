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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- UI LAYER: DRAWING & CONTROLS ---
@Composable
fun TunerMainScreen(viewModel: TunerViewModel) {
    // Saraswati Bloom animation state
    val bloomAlpha by animateFloatAsState(
        targetValue = if (viewModel.isBlooming) 0.95f else 0f,
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
            Box(Modifier.graphicsLayer {}) { TunerCanvas(viewModel) }

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

        // 2. NEW: Centered Target Frequency Footer (Gray)
        Text(
            text = "${viewModel.activePitch.freq} Hz",
            color = Color.Gray,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Bottom - (smoothed) freq display (Monospace prevents jumping around)
        Text(
            text = if (viewModel.currentPitch > 0) "%.2f Hz".format(viewModel.currentPitch) else "---- Hz",
            fontSize = 20.sp,
            color = viewModel.activePitch.color.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 32.dp),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun TunerCanvas(viewModel: TunerViewModel) {
    // Static Background
    Image(
        painter = painterResource(id = R.drawable.nataraja_bg),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )

    val activeColor = viewModel.activePitch.color

    // LAYER 1: Static Grid (only re-draws if level or target changes)
    Canvas(Modifier.fillMaxSize().padding(vertical = 60.dp)) {
        val centerY = size.height / 2f
        val plotScaleY = size.height / 100f
        drawGoalZone(centerY, plotScaleY, viewModel.currentLevel.goal)
        drawGrid(centerY, plotScaleY, viewModel.selectedTargetPitchLabel, activeColor)
    }

    // LAYER 2: Dynamic Pitch History (re-draws every (46)ms)
    Canvas(
        Modifier
            .fillMaxSize()
            .padding(vertical = 60.dp)
            // Use graphicsLayer here to ensure this layer is hardware-accelerated
            .graphicsLayer()
    ) {
        val centerY = size.height / 2f
        val plotScaleY = size.height / 100f
        val stepX = size.width / viewModel.maxPoints

        drawPitchHistory(
            vm = viewModel,
            color = activeColor,
            centerY = centerY,
            scaleY = plotScaleY,
            stepX = stepX
        )
    }
}

private fun DrawScope.drawGoalZone(centerY: Float, scaleY: Float, goal: Float) {
    drawRect(
        color = Color.Green.copy(0.25f),
        topLeft = Offset(0f, centerY - goal * scaleY),
        size = Size(size.width, (goal * 2) * scaleY)
    )
}

private fun DrawScope.drawGrid(centerY: Float, scaleY: Float, label: String, activeColor: Color) {
    val labelSizePx = with(density) { 12.sp.toPx() }
    val textPaint = android.graphics.Paint().apply {
        textSize = labelSizePx
        isAntiAlias = true
        setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
    }

    // Horizontal lines & Labels
    for (i in -50..50 step 5) {
        val y = centerY - (i * scaleY)
        val isMajor = i % 10 == 0

        textPaint.color = activeColor.toArgb()
        textPaint.alpha = if (i % 10 == 0) 200 else if (i==0) 255 else 140

        val lineLabel = if (i == 0) label else "${i}c"
        val lineStartX = if (isMajor) textPaint.measureText(lineLabel) + 30f else 0f

        drawLine(
            color = Color.Gray,
            alpha = if (i==0) 0.8f else 0.5f, // 0c line is the primary target

            start = Offset(lineStartX, y),
            end = Offset(size.width, y),
            strokeWidth = if (i == 0) 1.dp.toPx() else 0.5.dp.toPx()
        )

        if (isMajor) {
            drawIntoCanvas { it.nativeCanvas.drawText(lineLabel, 10f, y + 10f, textPaint) }
        }
    }

    // Vert Grid Lines - Matching the horizontal 5-cent step size for perfect squares
    val squareStep = 5 * scaleY
    var xPos = 0f
    while (xPos <= size.width) {
        drawLine(
            color = Color.Gray.copy(alpha = .5f),
            start = Offset(xPos, 0f),
            end = Offset(xPos, size.height),
            strokeWidth = 0.5.dp.toPx()
        )
        xPos += squareStep
    }
}

private fun DrawScope.drawPitchHistory(
    vm: TunerViewModel,
    color: Color,
    centerY: Float,
    scaleY: Float,
    stepX: Float
) {
    // Draw a tiny filled disc at the leading edge of the plot
    val leadIdx = (vm.writeIndex + vm.maxPoints - 1) % vm.maxPoints
    val leadP = vm.history[leadIdx]
    if (leadP > 0f) {
        // Access pre-calculated data instead of performing log2 here
        val leadCents = vm.centsHistory[leadIdx]
        val discRadius = 4.dp.toPx()

        val xOffset = size.width - discRadius - 4.dp.toPx()
        val centerPos = Offset(xOffset, centerY - (leadCents * scaleY))

        drawCircle( // Subtle dark glow behind the disc
            color = Color.Black.copy(alpha = 0.5f),
            radius = discRadius + 1.dp.toPx(),
            center = centerPos
        )
        drawCircle( // The tiny disc itself
            color = color,
            radius = discRadius,
            center = centerPos
        )
    }

    val path = Path()
    var firstPoint = true

    for (i in 0 until vm.maxPoints) {
        val idx = (vm.writeIndex + i) % vm.maxPoints
        val p = vm.history[idx]

        if (p > 0f) {
            val cents = vm.centsHistory[idx] // Data from background thread
            val x = i * stepX
            val y = centerY - (cents * scaleY)

            if (firstPoint) {
                path.moveTo(x, y)
                firstPoint = false
            } else {
                path.lineTo(x, y)
            }
        }
    }

    // Single-pass Shadow
    val shadowOffset = 6.dp.toPx()
    drawContext.canvas.translate(0f, shadowOffset)
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.3f),
        style = Stroke(width = 3.dp.toPx())
    )
    drawContext.canvas.translate(0f, -shadowOffset)

    // Single-pass Trace
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
    )
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
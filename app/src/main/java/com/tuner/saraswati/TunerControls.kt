// Dropdown menus

package com.tuner.saraswati

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TargetPitchDropDown(viewModel: TunerViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.wrapContentSize()) {
        // 1. The Label & Value (The trigger)
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TARGET: ",
                fontSize = 12.sp,
                color = Color.Cyan.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = viewModel.selectedTargetPitchLabel,
                fontSize = 14.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shadowElevation = 0.dp,
            modifier = Modifier
                .background(
                    Color(0xFF0A0A0A).copy(alpha = 0.95f), // Darker and more opaque
                    RoundedCornerShape(12.dp)
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.2f))
        ) {
            TargetPitch.entries.forEach { pitch ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = pitch.label,
                            color = pitch.color,
                            style = MaterialTheme.typography.bodyMedium, // Apply the bloom
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    onClick = {
                        viewModel.updateTargetPitch(pitch.label)
                        expanded = false
                    },
                    contentPadding = PaddingValues(vertical = 0.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun LevelDropDown(viewModel: TunerViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var rowWidth by remember { mutableIntStateOf(0) } // To store the width in pixels

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    // This captures the total width of Label + Selection + Icon
                    rowWidth = coordinates.size.width
                }
                .clickable { expanded = true }
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Text(
                text = "LEVEL: ",
                fontSize = 12.sp,
                color = Color.Cyan.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = viewModel.currentLevel.label.uppercase(),
                fontSize = 14.sp,
                color = Color.White,
                fontFamily = FontFamily.Monospace
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shadowElevation = 0.dp,
            modifier = Modifier
                .background(
                    Color(0xFF0A0A0A).copy(alpha = 0.95f), // Darker and more opaque
                    RoundedCornerShape(12.dp)
                )
                // Force the menu to match the row's width exactly
                .width(with(LocalDensity.current) { rowWidth.toDp() })
        ) {
            SkillLevel.entries.forEach { level ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = level.label,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium, // Apply the bloom
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    onClick = {
                        viewModel.currentLevel = level
                        expanded = false
                    },
                    contentPadding = PaddingValues(0.dp)
                )
            }
        }
    }
}

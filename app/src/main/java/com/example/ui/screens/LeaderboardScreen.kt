package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onNavigateBack: () -> Unit) {
    val scores = remember {
        listOf(
            Pair("Alice", 1200),
            Pair("You", 950),
            Pair("Bob", 800),
            Pair("Charlie", 450)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scoreboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)
        ) {
            Text("Weekly Fact-Check Score", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            
            val maxScore = scores.maxOf { it.second }.toFloat()
            val textMeasurer = rememberTextMeasurer()
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondaryContainer
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface

            Canvas(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = canvasWidth / (scores.size * 2)
                val spacing = barWidth

                scores.forEachIndexed { index, pair ->
                    val (name, score) = pair
                    val barHeight = (score / maxScore) * (canvasHeight - 50.dp.toPx())
                    val startX = (index * (barWidth + spacing)) + spacing / 2

                    drawRect(
                        color = if (name == "You") primaryColor else secondaryColor,
                        topLeft = Offset(x = startX, y = canvasHeight - barHeight - 20.dp.toPx()),
                        size = Size(width = barWidth, height = barHeight)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = name,
                        topLeft = Offset(x = startX, y = canvasHeight - 15.dp.toPx())
                    )
                    
                    drawText(
                        textMeasurer = textMeasurer,
                        text = score.toString(),
                        topLeft = Offset(x = startX, y = canvasHeight - barHeight - 40.dp.toPx())
                    )
                }
            }
        }
    }
}

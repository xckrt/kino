package com.example.kino.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CinemaScreenVisual() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val glowColor = Color(0xFF00E5FF)
        Canvas(modifier = Modifier.fillMaxWidth(0.8f).height(30.dp)) {
            val path = Path().apply {
                moveTo(0f, size.height)
                quadraticBezierTo(size.width / 2, 0f, size.width, size.height)
            }
            drawPath(path, color = glowColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
            drawPath(path, Brush.verticalGradient(listOf(glowColor.copy(0.3f), Color.Transparent)))
        }
        Text("ЭКРАН", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), fontSize = 10.sp, modifier = Modifier.padding(top = 8.dp))
    }
}
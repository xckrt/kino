package com.example.kino.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp

@Composable
fun TicketShapeBackground(
    modifier: Modifier,
    color: Color,
    dividerColor: Color
) {
    Canvas(modifier = modifier) {
        val cornerRadius = 12.dp.toPx()
        val holeRadius = 10.dp.toPx()
        val cutX = size.width - 110.dp.toPx()

        val path = Path().apply {
            moveTo(cornerRadius, 0f)
            lineTo(cutX - holeRadius, 0f)
            arcTo(
                rect = Rect(center = Offset(cutX, 0f), radius = holeRadius),
                startAngleDegrees = 180f, sweepAngleDegrees = -180f, forceMoveTo = false
            )
            lineTo(size.width - cornerRadius, 0f)
            arcTo(
                rect = Rect(left = size.width - 2 * cornerRadius, top = 0f, right = size.width, bottom = 2 * cornerRadius),
                startAngleDegrees = 270f, sweepAngleDegrees = 90f, forceMoveTo = false
            )
            lineTo(size.width, size.height - cornerRadius)
            arcTo(
                rect = Rect(left = size.width - 2 * cornerRadius, top = size.height - 2 * cornerRadius, right = size.width, bottom = size.height),
                startAngleDegrees = 0f, sweepAngleDegrees = 90f, forceMoveTo = false
            )
            lineTo(cutX + holeRadius, size.height)
            arcTo(
                rect = Rect(center = Offset(cutX, size.height), radius = holeRadius),
                startAngleDegrees = 0f, sweepAngleDegrees = -180f, forceMoveTo = false
            )
            lineTo(cornerRadius, size.height)
            arcTo(
                rect = Rect(left = 0f, top = size.height - 2 * cornerRadius, right = 2 * cornerRadius, bottom = size.height),
                startAngleDegrees = 90f, sweepAngleDegrees = 90f, forceMoveTo = false
            )
            lineTo(0f, cornerRadius)
            arcTo(
                rect = Rect(0f, 0f, 2 * cornerRadius, 2 * cornerRadius),
                startAngleDegrees = 180f, sweepAngleDegrees = 90f, forceMoveTo = false
            )
            close()
        }
        drawPath(path, color = color)
        drawLine(
            color = dividerColor,
            start = Offset(cutX, holeRadius + 5f),
            end = Offset(cutX, size.height - holeRadius - 5f),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}
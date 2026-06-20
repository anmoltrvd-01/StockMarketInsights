package com.example.stockmarketinsights.componentsUi

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockmarketinsights.domain.model.IntradayInfo
import kotlin.math.round
import kotlin.math.roundToInt

@Composable
fun StockChart(
    infos: List<IntradayInfo>,
    modifier: Modifier = Modifier,
    graphColor: Color = Color(0xFF00C853)
) {
    if (infos.isEmpty()) return

    val spacing = 100f
    val transparentGraphColor = remember(graphColor) { graphColor.copy(alpha = 0.5f) }

    val upperValue = remember(infos) {
        (infos.maxOf { it.close } + 1).roundToInt()
    }
    val lowerValue = remember(infos) {
        (infos.minOf { it.close } - 1).toInt()
    }

    val density = LocalDensity.current
    val textPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.argb(180, 200, 200, 200)
            textAlign = Paint.Align.CENTER
            textSize = density.run { 11.sp.toPx() }
        }
    }

    Canvas(modifier = modifier) {
        if (infos.size < 2) return@Canvas

        val spacePerHour = (size.width - spacing) / infos.size.toFloat()

        // Draw X-axis labels (hours)
        infos.forEachIndexed { i, info ->
            if (i % 2 == 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "${info.date.hour}h",
                    spacing + i * spacePerHour,
                    size.height - 5,
                    textPaint
                )
            }
        }

        // Draw Y-axis labels (prices)
        val priceStep = (upperValue - lowerValue) / 5f
        (0..4).forEach { i ->
            drawContext.canvas.nativeCanvas.drawText(
                round(lowerValue + priceStep * i).toInt().toString(),
                30f,
                size.height - spacing - i * (size.height - spacing) / 5f,
                textPaint
            )
        }

        var lastX = 0f

        // Build stroke path (the line)
        val strokePath = Path().apply {
            for (i in infos.indices) {
                val info = infos[i]
                val nextInfo = infos.getOrNull(i + 1) ?: infos.last()
                val leftRatio = (info.close - lowerValue) / (upperValue - lowerValue).toFloat()
                val rightRatio = (nextInfo.close - lowerValue) / (upperValue - lowerValue).toFloat()

                val x1 = spacing + i * spacePerHour
                val y1 = (size.height - spacing - (leftRatio * (size.height - spacing))).toFloat()  // ← toFloat()
                val x2 = spacing + (i + 1) * spacePerHour
                val y2 = (size.height - spacing - (rightRatio * (size.height - spacing))).toFloat() // ← toFloat()

                if (i == 0) moveTo(x1, y1)

                lastX = (x1 + x2) / 2f
                quadraticBezierTo(x1, y1, lastX, (y1 + y2) / 2f) // now Float ✓
            }
        }

        // Build fill path (gradient area under the line)
        val fillPath = android.graphics.Path(strokePath.asAndroidPath())
            .asComposePath()
            .apply {
                lineTo(lastX, size.height - spacing)
                lineTo(spacing, size.height - spacing)
                close()
            }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(transparentGraphColor, Color.Transparent),
                endY = size.height - spacing
            )
        )

        drawPath(
            path = strokePath,
            color = graphColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
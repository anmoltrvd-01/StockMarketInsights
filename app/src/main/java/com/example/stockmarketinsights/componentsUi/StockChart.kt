package com.example.stockmarketinsights.componentsUi

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
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
    graphColor: Color = Color(0xFF27AE60)   // your app's green
) {
    if (infos.isEmpty()) return

    val spacing            = 100f
    val transparentColor   = remember { graphColor.copy(alpha = 0.5f) }
    val upperValue         = remember(infos) {
        (infos.maxOf { it.close } + 1).roundToInt()
    }
    val lowerValue         = remember(infos) {
        infos.minOf { it.close }.toInt()
    }

    // Use M3 onSurface for axis labels so it respects light/dark theme
    val labelColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val density    = LocalDensity.current
    val textPaint  = remember(density, labelColor) {
        Paint().apply {
            color     = labelColor
            textAlign = Paint.Align.CENTER
            textSize  = density.run { 12.sp.toPx() }
        }
    }

    Canvas(modifier = modifier) {
        val spacePerHour = (size.width - spacing) / infos.size

        // X-axis: hour labels (every other point)
        (0 until infos.size - 1 step 2).forEach { i ->
            val info = infos[i]
            drawContext.canvas.nativeCanvas.drawText(
                info.date.hour.toString(),
                spacing + i * spacePerHour,
                size.height - 5,
                textPaint
            )
        }

        // Y-axis: price labels (5 steps)
        val priceStep = (upperValue - lowerValue) / 5f
        (0..4).forEach { i ->
            drawContext.canvas.nativeCanvas.drawText(
                round(lowerValue + priceStep * i).toString(),
                30f,
                size.height - spacing - i * size.height / 5f,
                textPaint
            )
        }

        // Build stroke path with quadratic bezier curves
        var lastX = 0f
        val strokePath = Path().apply {
            for (i in infos.indices) {
                val info      = infos[i]
                val nextInfo  = infos.getOrNull(i + 1) ?: infos.last()
                val leftRatio = (info.close  - lowerValue) / (upperValue - lowerValue)
                val rightRatio= (nextInfo.close - lowerValue) / (upperValue - lowerValue)

                val x1 = spacing + i * spacePerHour
                val y1 = size.height - spacing - (leftRatio  * size.height).toFloat()
                val x2 = spacing + (i + 1) * spacePerHour
                val y2 = size.height - spacing - (rightRatio * size.height).toFloat()

                if (i == 0) moveTo(x1, y1)
                lastX = (x1 + x2) / 2f
                quadraticBezierTo(x1, y1, lastX, (y1 + y2) / 2f)
            }
        }

        // Fill path (gradient under the line)
        val fillPath = android.graphics.Path(strokePath.asAndroidPath())
            .asComposePath()
            .apply {
                lineTo(lastX, size.height - spacing)
                lineTo(spacing, size.height - spacing)
                close()
            }

        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(transparentColor, Color.Transparent),
                endY   = size.height - spacing
            )
        )

        // Stroke line
        drawPath(
            path  = strokePath,
            color = graphColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

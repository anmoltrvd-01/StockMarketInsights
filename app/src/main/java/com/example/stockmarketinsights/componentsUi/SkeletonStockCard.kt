package com.example.stockmarketinsights.componentsUi

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.stockmarketinsights.ui.theme.SurfaceDark
import com.example.stockmarketinsights.ui.theme.SurfaceVariant

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        SurfaceVariant,
        SurfaceVariant.copy(alpha = 0.5f),
        SurfaceVariant
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

@Composable
fun SkeletonStockCard(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceDark)
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.5f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun SkeletonListItem(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(80.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(130.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .width(60.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .width(50.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

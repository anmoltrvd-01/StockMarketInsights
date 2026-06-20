package com.example.stockmarketinsights.componentsUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stockmarketinsights.dataModel.StockSummaryItem
import com.example.stockmarketinsights.ui.theme.GreenPrimary
import com.example.stockmarketinsights.ui.theme.RedAccent
import com.example.stockmarketinsights.ui.theme.SurfaceDark

@Composable
fun StockCard(
    stock: StockSummaryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = !stock.change.startsWith("-")
    val changeColor = if (isPositive) GreenPrimary else RedAccent

    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${stock.price}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = changeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = stock.changePercent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = changeColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StockListItem(
    stock: StockSummaryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = !stock.change.startsWith("-")
    val changeColor = if (isPositive) GreenPrimary else RedAccent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${stock.price}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stock.changePercent,
                style = MaterialTheme.typography.bodySmall,
                color = changeColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

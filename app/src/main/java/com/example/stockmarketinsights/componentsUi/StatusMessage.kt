package com.example.stockmarketinsights.componentsUi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.stockmarketinsights.ui.theme.SurfaceVariant

enum class StateType { NETWORK, GENERIC_ERROR, EMPTY, NO_RESULTS }

@Composable
fun StatusMessage(
    type: StateType,
    title: String,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val icon: ImageVector = when (type) {
        StateType.NETWORK -> Icons.Filled.CloudOff
        StateType.GENERIC_ERROR -> Icons.Filled.ErrorOutline
        StateType.NO_RESULTS -> Icons.Filled.SearchOff
        StateType.EMPTY -> Icons.Filled.ErrorOutline
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        if (!message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Try Again")
            }
        }
    }
}

fun friendlyErrorFor(rawMessage: String?): Pair<String, String> {
    val msg = rawMessage?.lowercase() ?: ""
    return when {
        msg.contains("unable to resolve host") || msg.contains("no address") ->
            "No internet connection" to "Check your network and try again."
        msg.contains("timeout") || msg.contains("timed out") ->
            "Request timed out" to "The server took too long to respond."
        msg.contains("rate") || msg.contains("limit") || msg.contains("note") ->
            "Too many requests" to "We've hit the API limit — please try again in a minute."
        msg.isBlank() ->
            "Something went wrong" to "Please try again."
        else ->
            "Something went wrong" to "Please try again."
    }
}
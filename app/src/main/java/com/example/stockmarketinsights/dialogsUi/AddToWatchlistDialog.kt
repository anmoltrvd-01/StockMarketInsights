package com.example.stockmarketinsights.dialogsUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.stockmarketinsights.roomdb.WatchlistEntity
import com.example.stockmarketinsights.ui.theme.SurfaceDark
import com.example.stockmarketinsights.ui.theme.GreenPrimary

@Composable
fun AddToWatchlistDialog(
    symbol: String,
    stockName: String,
    watchlists: List<WatchlistEntity>,
    onDismiss: () -> Unit,
    onAddToWatchlist: (watchlistName: String) -> Unit,
    onCreateNew: (name: String) -> Unit
) {
    var showCreateField by remember { mutableStateOf(false) }
    var newWatchlistName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add $symbol to Watchlist",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (watchlists.isEmpty() && !showCreateField) {
                    Text(
                        text = "No watchlists yet. Create one below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(watchlists) { watchlist ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAddToWatchlist(watchlist.watchlistName) }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = GreenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = watchlist.watchlistName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (showCreateField) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newWatchlistName,
                        onValueChange = { newWatchlistName = it },
                        placeholder = { Text("Watchlist name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCreateField = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newWatchlistName.isNotBlank()) {
                                    onCreateNew(newWatchlistName.trim())
                                    showCreateField = false
                                    newWatchlistName = ""
                                }
                            }
                        ) {
                            Text("Create & Add")
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showCreateField = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create new watchlist")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

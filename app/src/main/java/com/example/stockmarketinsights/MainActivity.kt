package com.example.stockmarketinsights

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.stockmarketinsights.navigation.NavGraph
import com.example.stockmarketinsights.ui.theme.StockMarketInsightsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockMarketInsightsTheme {
                NavGraph()
            }
        }
    }
}
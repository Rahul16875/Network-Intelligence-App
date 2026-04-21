package com.example.networkintelligence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.networkintelligence.presentation.navigation.NetworkIntelligenceNavHost
import com.example.networkintelligence.ui.theme.NetworkIntelligenceTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NetworkIntelligenceTheme {
                NetworkIntelligenceNavHost()
            }
        }
    }
}

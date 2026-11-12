package com.noorazan.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.noorazan.app.service.PlayerService
import com.noorazan.app.ui.QiblaScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NoorAzanApp()
                }
            }
        }
    }
}

@Composable
fun NoorAzanApp() {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Prayer & Azan") },
                    icon = {}
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Qibla Compass") },
                    icon = {}
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("NoorAzan - Ad-Free Prayer Companion", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        val intent = Intent(context, PlayerService::class.java).apply {
                            putExtra("PRAYER_NAME", "Test Azan")
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(intent)
                        } else {
                            context.startService(intent)
                        }
                    }) {
                        Text("Test Background Azan Service")
                    }
                }
            } else {
                // Default coordinates (e.g., Regina, SK) - replace with FusedLocationProviderClient result
                QiblaScreen(latitude = 50.4452, longitude = -104.6189)
            }
        }
    }
}

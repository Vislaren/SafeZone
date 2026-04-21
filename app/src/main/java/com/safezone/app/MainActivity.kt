package com.safezone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.safezone.app.ui.navigation.SafeZoneNavGraph
import com.safezone.app.ui.theme.SafeZoneTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Handle incoming deep link for an alert
        val deepAlertId = intent?.data?.getQueryParameter("id")

        setContent {
            SafeZoneTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SafeZoneNavGraph(initialAlertId = deepAlertId)
                }
            }
        }
    }
}

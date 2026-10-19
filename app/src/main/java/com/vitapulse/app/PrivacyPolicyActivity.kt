package com.vitapulse.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vitapulse.app.ui.theme.VitaTheme

/** Shown by Health Connect when the user asks why VitaPulse reads their health data. */
class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitaTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        Modifier.safeDrawingPadding().padding(24.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("How VitaPulse uses your health data", style = MaterialTheme.typography.headlineSmall)
                        Text("VitaPulse reads steps, active calories, exercise time, heart rate, sleep and weight from Health Connect to show your daily progress, trends and goal reminders.")
                        Text("Health data is read-only, processed on your device, and never sold, shared or uploaded. Your food, water and weight logs are stored in an encrypted database (AES-256) whose key is protected by the Android Keystore.")
                        Text("Food photos are analysed on-device. Only a barcode number or food name is sent to Open Food Facts to look up nutrition values.")
                        Text("You can revoke access at any time in Health Connect settings, and delete all VitaPulse data from Profile → Delete all my data.")
                    }
                }
            }
        }
    }
}

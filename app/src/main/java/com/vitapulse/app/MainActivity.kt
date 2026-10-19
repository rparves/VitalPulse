package com.vitapulse.app

import android.os.Bundle
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.vitapulse.app.data.AppDatabase
import com.vitapulse.app.ui.VitaApp
import com.vitapulse.app.ui.theme.VitaTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private enum class Gate { CHECKING, LOCKED, OPEN }

    private var gate by mutableStateOf(Gate.CHECKING)
    private var lockEnabled = false
    private var stoppedAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        val dao = AppDatabase.get(applicationContext).dao()

        lifecycleScope.launch {
            dao.profileFlow().collect { p ->
                lockEnabled = p?.biometricLock == true
                if (p?.secureScreen == true) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                if (gate == Gate.CHECKING) {
                    if (lockEnabled) { gate = Gate.LOCKED; authenticate() } else gate = Gate.OPEN
                }
            }
        }

        setContent {
            VitaTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (gate != Gate.CHECKING) {
                        Box(Modifier.fillMaxSize()) {
                            VitaApp()
                            if (gate == Gate.LOCKED) LockScreen(::authenticate)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        stoppedAt = SystemClock.elapsedRealtime()
    }

    override fun onStart() {
        super.onStart()
        // Re-lock after 30 s in the background.
        if (lockEnabled && gate == Gate.OPEN && stoppedAt > 0 &&
            SystemClock.elapsedRealtime() - stoppedAt > 30_000) {
            gate = Gate.LOCKED
            authenticate()
        }
    }

    private fun authenticate() {
        val authenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        if (BiometricManager.from(this).canAuthenticate(authenticators) != BiometricManager.BIOMETRIC_SUCCESS) {
            gate = Gate.OPEN   // device lock removed; nothing to verify against
            return
        }
        val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    gate = Gate.OPEN
                }
            })
        prompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock VitaPulse")
                .setSubtitle("Your health data is protected")
                .setAllowedAuthenticators(authenticators)
                .build()
        )
    }
}

@Composable
private fun LockScreen(onUnlock: () -> Unit) {
    Surface(
        Modifier.fillMaxSize().clickable(remember { MutableInteractionSource() }, indication = null) {},
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Rounded.Lock, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("VitaPulse is locked", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onUnlock) { Text("Unlock") }
        }
    }
}

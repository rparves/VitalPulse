@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitapulse.app.data.Nutrition
import com.vitapulse.app.ui.*
import com.vitapulse.app.ui.components.*
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(vm: AppViewModel, onEdit: () -> Unit, onConnectHealth: () -> Unit) {
    val loaded by vm.profile.collectAsStateWithLifecycle()
    val p = loaded?.profile ?: return
    val context = LocalContext.current
    var confirmWipe by remember { mutableStateOf(false) }
    val bmi = Nutrition.bmi(p)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(p.name.firstOrNull()?.uppercase() ?: "🙂", style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(p.name.ifBlank { "Your profile" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${Nutrition.age(p)} yrs · ${p.heightCm.roundToInt()} cm · ${fmt1(p.weightKg)} kg",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            VCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Stat("BMI", fmt1(bmi), Nutrition.bmiCategory(bmi))
                    Stat("BMR", "${Nutrition.bmr(p).roundToInt()}", "kcal at rest")
                    Stat("TDEE", "${Nutrition.tdee(p).roundToInt()}", "kcal / day")
                }
            }
        }
        item {
            VCard {
                CardHeader(Icons.Rounded.Flag, "Daily goals", MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                GoalLine("Steps", fmt(p.stepGoal))
                GoalLine("Calories", "${fmt(p.calorieGoal)} kcal")
                GoalLine("Water", "${fmt(p.waterGoalMl)} ml")
                GoalLine("Active minutes", "${p.activeMinutesGoal} min")
                GoalLine("Sleep", hm(p.sleepGoalMin.toLong()))
                GoalLine("Target weight", "${fmt1(p.targetWeightKg)} kg")
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = onEdit) { Text("Edit profile & goals") }
            }
        }
        item {
            VCard {
                CardHeader(Icons.Rounded.MonitorHeart, "Health Connect", MaterialTheme.colorScheme.primary)
                Text("Sync activity, heart rate and sleep from wearables and other apps.",
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onConnectHealth) { Text("Permissions") }
                    OutlinedButton(onClick = {
                        runCatching {
                            context.startActivity(Intent("android.health.connect.action.HEALTH_HOME_SETTINGS"))
                        }.onFailure { Toast.makeText(context, "Open Health Connect from Settings", Toast.LENGTH_SHORT).show() }
                    }) { Text("Open Health Connect") }
                }
            }
        }
        item {
            VCard {
                CardHeader(Icons.Rounded.Shield, "Privacy & security", MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                SettingSwitch(Icons.Rounded.Notifications, "Goal reminders", "Nudges and an evening recap", p.remindersEnabled) {
                    vm.saveProfile(p.copy(remindersEnabled = it))
                }
                SettingSwitch(Icons.Rounded.Fingerprint, "App lock", "Fingerprint, face or device PIN", p.biometricLock) { on ->
                    val ok = BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL) ==
                        BiometricManager.BIOMETRIC_SUCCESS
                    if (on && !ok) Toast.makeText(context, "Set up a screen lock on your phone first", Toast.LENGTH_LONG).show()
                    else vm.saveProfile(p.copy(biometricLock = on))
                }
                SettingSwitch(Icons.Rounded.VisibilityOff, "Private screen", "Block screenshots and hide in recent apps", p.secureScreen) {
                    vm.saveProfile(p.copy(secureScreen = it))
                }
                Text(
                    "Your data is stored only on this phone in an AES-256 encrypted database. The key is held by the Android Keystore and is never backed up.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
        item {
            VCard {
                CardHeader(Icons.Rounded.DeleteForever, "Your data", MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { confirmWipe = true }) {
                    Text("Delete all my data", color = MaterialTheme.colorScheme.error)
                }
            }
        }
        item {
            Text("VitaPulse 1.0 · Nutrition data from Open Food Facts (ODbL). Not a medical device.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    if (confirmWipe) {
        AlertDialog(
            onDismissRequest = { confirmWipe = false },
            title = { Text("Delete everything?") },
            text = { Text("Your profile, food, water and weight logs will be permanently erased from this device. Health Connect data is not affected.") },
            confirmButton = {
                Button(
                    onClick = { vm.wipeAllData(); confirmWipe = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmWipe = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun Stat(label: String, value: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(sub, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun GoalLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingSwitch(icon: ImageVector, title: String, sub: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

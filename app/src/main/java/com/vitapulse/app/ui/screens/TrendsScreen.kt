@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitapulse.app.ui.*
import com.vitapulse.app.ui.components.*
import com.vitapulse.app.ui.theme.VP
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.roundToLong

private fun dayLabel(d: LocalDate) = d.dayOfWeek.getDisplayName(JTextStyle.SHORT, Locale.getDefault()).take(3)

@Composable
fun TrendsScreen(vm: AppViewModel) {
    val loaded by vm.profile.collectAsStateWithLifecycle()
    val p = loaded?.profile ?: return
    val stepsWeek by vm.stepsWeek.collectAsStateWithLifecycle()
    val kcalWeek by vm.kcalWeek.collectAsStateWithLifecycle()
    val weights by vm.weights.collectAsStateWithLifecycle()
    var showWeight by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(Modifier.padding(top = 8.dp)) {
                Text("Trends", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Last 7 days", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            val avg = if (stepsWeek.isEmpty()) null else stepsWeek.map { it.second }.average()
            VCard {
                CardHeader(Icons.AutoMirrored.Rounded.DirectionsWalk, "Steps", VP.Steps,
                    avg?.let { "avg ${fmt(it.roundToLong())}/day" })
                Spacer(Modifier.height(12.dp))
                if (stepsWeek.isEmpty()) {
                    Text("Connect Health Connect on the Today tab to see step history.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    BarChart(stepsWeek.map { it.second.toFloat() }, stepsWeek.map { dayLabel(it.first) },
                        VP.Steps, goal = p.stepGoal.toFloat())
                }
            }
        }
        item {
            val logged = kcalWeek.filter { it.second > 0 }
            VCard {
                CardHeader(Icons.Rounded.Restaurant, "Calories eaten", VP.Fuel,
                    if (logged.isEmpty()) null else "avg ${fmt(logged.map { it.second }.average().roundToLong())} kcal")
                Spacer(Modifier.height(12.dp))
                BarChart(kcalWeek.map { it.second.toFloat() }, kcalWeek.map { dayLabel(it.first) },
                    VP.Fuel, goal = p.calorieGoal.toFloat())
            }
        }
        item {
            VCard {
                val latest = weights.firstOrNull()?.kg ?: p.weightKg
                CardHeader(Icons.Rounded.MonitorWeight, "Weight", VP.Weight, "${fmt1(latest)} kg now")
                Spacer(Modifier.height(12.dp))
                if (weights.size < 2) {
                    Text("Log at least two weigh-ins to see your trend.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LineChart(weights.reversed().map { it.kg.toFloat() }, VP.Weight)
                }
                val toGo = latest - p.targetWeightKg
                Text(
                    when {
                        kotlin.math.abs(toGo) < 0.2 -> "You're at your target weight."
                        toGo > 0 -> "${fmt1(toGo)} kg above your ${fmt1(p.targetWeightKg)} kg target"
                        else -> "${fmt1(-toGo)} kg below your ${fmt1(p.targetWeightKg)} kg target"
                    },
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = { showWeight = true }) { Text("Log weight") }
            }
        }
    }

    if (showWeight) {
        var v by remember { mutableStateOf("") }
        val kg = v.toDoubleOrNull()?.takeIf { it in 25.0..350.0 }
        AlertDialog(
            onDismissRequest = { showWeight = false },
            title = { Text("Log weight") },
            text = { NumberField("Weight (kg)", v, Modifier.fillMaxWidth(), decimal = true) { v = it } },
            confirmButton = {
                Button(onClick = { kg?.let { vm.logWeight(it) }; showWeight = false }, enabled = kg != null) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showWeight = false }) { Text("Cancel") } },
        )
    }
}

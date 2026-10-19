@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitapulse.app.ui.*
import com.vitapulse.app.ui.components.*
import com.vitapulse.app.ui.theme.VP
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun TodayScreen(vm: AppViewModel, onConnectHealth: () -> Unit, onOpenFood: () -> Unit, onOpenTrends: () -> Unit) {
    val loaded by vm.profile.collectAsStateWithLifecycle()
    val p = loaded?.profile ?: return
    val day by vm.today.collectAsStateWithLifecycle()
    val food by vm.foodToday.collectAsStateWithLifecycle()
    val water by vm.waterToday.collectAsStateWithLifecycle()
    val weights by vm.weights.collectAsStateWithLifecycle()
    val stepsWeek by vm.stepsWeek.collectAsStateWithLifecycle()
    val kcalWeek by vm.kcalWeek.collectAsStateWithLifecycle()
    val hcGranted by vm.hcStepsGranted.collectAsStateWithLifecycle()
    val eaten = food.sumOf { it.kcal }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Greeting(p.name) }
        if (!hcGranted) item { ConnectCard(onConnectHealth) }
        item {
            VCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ActivityRings(
                        listOf(
                            Ring(day.steps.toFloat() / p.stepGoal, VP.Steps),
                            Ring(day.activeMinutes.toFloat() / p.activeMinutesGoal, VP.Active),
                            Ring((eaten / p.calorieGoal).toFloat(), VP.Fuel),
                        ),
                        Modifier.size(150.dp)
                    )
                    Spacer(Modifier.width(20.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RingLegend("Steps", "${fmt(day.steps)} / ${fmt(p.stepGoal)}", VP.Steps)
                        RingLegend("Active", "${day.activeMinutes} / ${p.activeMinutesGoal} min", VP.Active)
                        RingLegend("Fuel", "${eaten.roundToInt()} / ${p.calorieGoal} kcal", VP.Fuel)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(Icons.AutoMirrored.Rounded.DirectionsWalk, "Steps", fmt(day.steps), "steps",
                    VP.Steps, day.steps.toFloat() / p.stepGoal, Modifier.weight(1f), onClick = onOpenTrends)
                MetricTile(Icons.Rounded.Restaurant, "Food", fmt(eaten.roundToLong()), "kcal",
                    VP.Fuel, (eaten / p.calorieGoal).toFloat(), Modifier.weight(1f), onClick = onOpenFood)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(Icons.Rounded.WaterDrop, "Water", fmt(water), "/ ${fmt(p.waterGoalMl)} ml",
                    VP.Water, water.toFloat() / p.waterGoalMl, Modifier.weight(1f),
                    action = {
                        FilledTonalButton(onClick = { vm.addWater(250) }, contentPadding = PaddingValues(horizontal = 12.dp)) {
                            Text("+ 250 ml")
                        }
                    })
                MetricTile(Icons.Rounded.Bedtime, "Sleep", if (day.sleepMinutes > 0) hm(day.sleepMinutes) else "—",
                    "", VP.Sleep, day.sleepMinutes.toFloat() / p.sleepGoalMin, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(Icons.Rounded.Favorite, "Heart rate", day.avgHeartRate?.toString() ?: "—",
                    "bpm avg", VP.Heart, null, Modifier.weight(1f))
                MetricTile(Icons.Rounded.LocalFireDepartment, "Burned", fmt(day.activeKcal.roundToLong()),
                    "active kcal", VP.Active, null, Modifier.weight(1f))
            }
        }
        item {
            val latest = weights.firstOrNull()?.kg ?: p.weightKg
            MetricTile(Icons.Rounded.MonitorWeight, "Weight", fmt1(latest), "kg · target ${fmt1(p.targetWeightKg)} kg",
                VP.Weight, null, onClick = onOpenTrends)
        }
        item { HighlightsCard(p.stepGoal, p.calorieGoal, p.waterGoalMl, water, stepsWeek, kcalWeek, weights.map { it.kg }) }
    }
}

@Composable
private fun Greeting(name: String) {
    val h = LocalTime.now().hour
    val g = when (h) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
    Column(Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(if (name.isBlank()) g else "$g, $name", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RingLegend(label: String, value: String, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ConnectCard(onConnect: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Connect Health Connect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text("See steps, workouts, heart rate and sleep from your phone, Fitbit, Galaxy Watch, Pixel Watch and other apps.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Button(onClick = onConnect) { Text("Connect") }
        }
    }
}

@Composable
private fun HighlightsCard(
    stepGoal: Int, kcalGoal: Int, waterGoal: Int, water: Int,
    stepsWeek: List<Pair<LocalDate, Long>>, kcalWeek: List<Pair<LocalDate, Double>>, weights: List<Double>,
) {
    val lines = buildList {
        if (stepsWeek.isNotEmpty()) {
            val avg = stepsWeek.map { it.second }.average()
            val met = stepsWeek.count { it.second >= stepGoal }
            add("You averaged ${fmt(avg.roundToLong())} steps a day this week — ${(avg / stepGoal * 100).roundToInt()}% of your goal. Goal met on $met of 7 days.")
        }
        val logged = kcalWeek.filter { it.second > 0 }
        if (logged.size >= 2) {
            add("On days you logged food, you averaged ${logged.map { it.second }.average().roundToInt()} kcal (target $kcalGoal).")
        }
        if (water < waterGoal) add("${fmt(waterGoal - water)} ml of water left to reach today's goal.")
        else add("Hydration goal reached today. 💧")
        if (weights.size >= 2) {
            val diff = weights.first() - weights.last()
            val dir = if (diff < 0) "down" else "up"
            add("Weight is $dir ${fmt1(kotlin.math.abs(diff))} kg across your last ${weights.size} weigh-ins.")
        }
    }
    VCard {
        CardHeader(Icons.Rounded.Lightbulb, "Highlights", MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(8.dp))
        lines.forEach {
            Text("• $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 3.dp))
        }
    }
}

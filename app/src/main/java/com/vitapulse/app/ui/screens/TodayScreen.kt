@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitapulse.app.data.Readiness
import com.vitapulse.app.ui.*
import com.vitapulse.app.ui.components.*
import com.vitapulse.app.ui.theme.VP
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@Composable
fun TodayScreen(
    vm: AppViewModel,
    onConnectHealth: () -> Unit,
    onOpenFood: () -> Unit,
    onOpenTrends: () -> Unit,
    onScan: () -> Unit,
    onOpenWorkouts: () -> Unit,
) {
    val loaded by vm.profile.collectAsStateWithLifecycle()
    val p = loaded?.profile ?: return
    val day by vm.today.collectAsStateWithLifecycle()
    val food by vm.foodToday.collectAsStateWithLifecycle()
    val water by vm.waterToday.collectAsStateWithLifecycle()
    val weights by vm.weights.collectAsStateWithLifecycle()
    val stepsWeek by vm.stepsWeek.collectAsStateWithLifecycle()
    val kcalWeek by vm.kcalWeek.collectAsStateWithLifecycle()
    val hcGranted by vm.hcStepsGranted.collectAsStateWithLifecycle()
    val workouts by vm.workoutsToday.collectAsStateWithLifecycle()
    val readiness by vm.readiness.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()
    val eaten = food.sumOf { it.kcal }
    // Use whichever is higher so a watch-tracked session logged here isn't counted twice.
    val activeMin = maxOf(day.activeMinutes, workouts.sumOf { it.durationMin }.toLong())
    val burned = maxOf(day.activeKcal, workouts.sumOf { it.kcal })

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { TopBrandBar(hcGranted) }
        item { Greeting(p.name) }
        item { ReadinessHero(readiness, streak, hcGranted, onConnectHealth) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickAction(Icons.Rounded.CameraAlt, "Scan food", VP.Fuel, Modifier.weight(1f), onScan)
                QuickAction(Icons.Rounded.WaterDrop, "+250 ml", VP.Water, Modifier.weight(1f)) { vm.addWater(250) }
                QuickAction(Icons.Rounded.FitnessCenter, "Workout", VP.Lime, Modifier.weight(1f), onOpenWorkouts)
            }
        }
        if (!hcGranted && readiness != null) item { ConnectBanner(onConnectHealth) }
        item {
            VCard(glow = VP.Cyan) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("Today's activity", VP.Cyan)
                    Spacer(Modifier.weight(1f))
                    Text("${(listOf(day.steps.toFloat() / p.stepGoal, activeMin.toFloat() / p.activeMinutesGoal, (eaten / p.calorieGoal).toFloat()).map { it.coerceIn(0f, 1f) }.average() * 100).roundToInt()}% complete",
                        style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ActivityRings(
                        listOf(
                            Ring(day.steps.toFloat() / p.stepGoal, VP.Steps, Color(0xFF0FA3FF)),
                            Ring(activeMin.toFloat() / p.activeMinutesGoal, VP.Lime, Color(0xFF3DDC84)),
                            Ring((eaten / p.calorieGoal).toFloat(), VP.Fuel, VP.Heart),
                        ),
                        Modifier.size(158.dp)
                    )
                    Spacer(Modifier.width(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        RingLegend("Steps", fmt(countUp(day.steps.toInt())), "/ ${fmt(p.stepGoal)}", VP.Steps)
                        RingLegend("Active", "${countUp(activeMin.toInt())}", "/ ${p.activeMinutesGoal} min", VP.Lime)
                        RingLegend("Fuel", fmt(countUp(eaten.roundToInt())), "/ ${fmt(p.calorieGoal)} kcal", VP.Fuel)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(Icons.AutoMirrored.Rounded.DirectionsWalk, "Steps", fmt(day.steps), "of ${fmt(p.stepGoal)}",
                    VP.Steps, day.steps.toFloat() / p.stepGoal, Modifier.weight(1f), onClick = onOpenTrends)
                MetricTile(Icons.Rounded.Restaurant, "Food", fmt(eaten.roundToLong()), "kcal eaten",
                    VP.Fuel, (eaten / p.calorieGoal).toFloat(), Modifier.weight(1f), onClick = onOpenFood)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(Icons.Rounded.WaterDrop, "Water", fmt(water), "of ${fmt(p.waterGoalMl)} ml",
                    VP.Water, water.toFloat() / p.waterGoalMl, Modifier.weight(1f))
                MetricTile(Icons.Rounded.Bedtime, "Sleep", if (day.sleepMinutes > 0) hm(day.sleepMinutes) else "—",
                    "goal ${hm(p.sleepGoalMin.toLong())}", VP.Sleep, day.sleepMinutes.toFloat() / p.sleepGoalMin, Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricTile(Icons.Rounded.Favorite, "Heart", day.avgHeartRate?.toString() ?: "—",
                    "bpm average", VP.Heart, null, Modifier.weight(1f))
                MetricTile(Icons.Rounded.LocalFireDepartment, "Burned", fmt(burned.roundToLong()),
                    "active kcal", VP.Fuel, null, Modifier.weight(1f))
            }
        }
        item {
            val latest = weights.firstOrNull()?.kg ?: p.weightKg
            MetricTile(Icons.Rounded.MonitorWeight, "Weight", "${fmt1(latest)} kg", "target ${fmt1(p.targetWeightKg)} kg",
                VP.Weight, null, onClick = onOpenTrends)
        }
        item { InsightsCard(p.stepGoal, p.calorieGoal, p.waterGoalMl, water, stepsWeek, kcalWeek, weights.map { it.kg }) }
    }
}

@Composable
private fun TopBrandBar(synced: Boolean) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(VP.brand),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Rounded.Favorite, null, tint = VP.OnLime, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(10.dp))
        Text("VITAPULSE", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))
        LiveBadge(if (synced) "Synced" else "On-device", if (synced) VP.Lime else VP.Cyan)
    }
}

@Composable
private fun Greeting(name: String) {
    val h = LocalTime.now().hour
    val g = when (h) { in 5..11 -> "Good morning"; in 12..16 -> "Good afternoon"; else -> "Good evening" }
    Column(Modifier.padding(top = 6.dp)) {
        Eyebrow(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE · d MMMM")))
        Spacer(Modifier.height(4.dp))
        Text(if (name.isBlank()) "$g." else "$g,", style = MaterialTheme.typography.headlineLarge)
        if (name.isNotBlank()) {
            Text("$name.", style = MaterialTheme.typography.headlineLarge, color = VP.Lime)
        }
    }
}

private fun scoreColor(s: Int) = when {
    s >= 80 -> VP.Lime
    s >= 60 -> VP.Cyan
    s >= 40 -> VP.Carbs
    else -> VP.Heart
}

@Composable
private fun ReadinessHero(r: Readiness?, streak: Pair<Int, Int>, hcGranted: Boolean, onConnect: () -> Unit) {
    if (r == null) {
        VCard(glow = VP.Lime) {
            Eyebrow("Readiness score", VP.Lime)
            Spacer(Modifier.height(8.dp))
            Text("Unlock your daily score", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(6.dp))
            Text(
                "VitaPulse rates how ready you are to train from your sleep, recovery, food and water. Log a day of meals and water" +
                    if (hcGranted) " to see it tomorrow." else ", or connect Health Connect for sleep and activity.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!hcGranted) {
                Spacer(Modifier.height(14.dp))
                GradientButton("Connect Health Connect", Modifier.fillMaxWidth(), Icons.Rounded.Link, onClick = onConnect)
            }
            if (streak.first > 0) {
                Spacer(Modifier.height(12.dp))
                StreakChip(streak)
            }
        }
        return
    }
    val color = scoreColor(r.score)
    VCard(glow = color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                ActivityRings(listOf(Ring(r.score / 100f, color, VP.Cyan)), Modifier.fillMaxSize(), strokeFraction = 0.085f)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${countUp(r.score)}", style = MaterialTheme.typography.displayMedium)
                    Text("/ 100", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Eyebrow("Readiness", color)
                Text(r.label, style = MaterialTheme.typography.headlineSmall, color = color)
                Spacer(Modifier.height(4.dp))
                Text(r.advice, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            r.parts.take(3).forEach { part ->
                StatPill(part.label, "${part.score}", scoreColor(part.score), Modifier.weight(1f))
            }
        }
        if (r.parts.size > 3) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                r.parts.drop(3).forEach { part ->
                    StatPill(part.label, "${part.score}", scoreColor(part.score), Modifier.weight(1f))
                }
                if (streak.first > 0) StatPill("Streak", "🔥 ${streak.first} days", VP.Fuel, Modifier.weight(2f))
            }
        } else if (streak.first > 0) {
            Spacer(Modifier.height(8.dp))
            StreakChip(streak)
        }
        r.tip?.let {
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(color.copy(alpha = 0.08f)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = color, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("A guide from your logs and wearable data, not a medical measurement.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StreakChip(streak: Pair<Int, Int>) {
    Row(
        Modifier.clip(CircleShape).background(VP.Fuel.copy(alpha = 0.12f))
            .border(1.dp, VP.Fuel.copy(alpha = 0.35f), CircleShape).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥 ${streak.first}-day streak", style = MaterialTheme.typography.labelLarge, color = VP.Fuel, fontWeight = FontWeight.Bold)
        Text("  ·  best ${streak.second}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier.clip(shape).background(Brush.verticalGradient(listOf(color.copy(alpha = 0.16f), color.copy(alpha = 0.04f))))
            .border(1.dp, color.copy(alpha = 0.25f), shape).clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RingLegend(label: String, value: String, of: String, color: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(8.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Eyebrow(label)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(4.dp))
            Text(of, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp))
        }
    }
}

@Composable
private fun ConnectBanner(onConnect: () -> Unit) {
    VCard(glow = VP.Cyan, onClick = onConnect) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.Rounded.Watch, VP.Cyan, 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Connect your watch & phone data", style = MaterialTheme.typography.titleMedium)
                Text("Steps, workouts, heart rate and sleep via Health Connect",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ChevronRight, null, tint = VP.Cyan)
        }
    }
}

@Composable
private fun InsightsCard(
    stepGoal: Int, kcalGoal: Int, waterGoal: Int, water: Int,
    stepsWeek: List<Pair<LocalDate, Long>>, kcalWeek: List<Pair<LocalDate, Double>>, weights: List<Double>,
) {
    val lines = buildList {
        if (stepsWeek.isNotEmpty()) {
            val avg = stepsWeek.map { it.second }.average()
            val met = stepsWeek.count { it.second >= stepGoal }
            add("You averaged ${fmt(avg.roundToLong())} steps a day this week — ${(avg / stepGoal * 100).roundToInt()}% of your goal, hit on $met of 7 days.")
        }
        val logged = kcalWeek.filter { it.second > 0 }
        if (logged.size >= 2) {
            add("On logged days you averaged ${logged.map { it.second }.average().roundToInt()} kcal (target $kcalGoal).")
        }
        if (water < waterGoal) add("${fmt(waterGoal - water)} ml of water left to hit today's goal.")
        else add("Hydration goal reached today. 💧")
        if (weights.size >= 2) {
            val diff = weights.first() - weights.last()
            add("Weight is ${if (diff < 0) "down" else "up"} ${fmt1(kotlin.math.abs(diff))} kg across your last ${weights.size} weigh-ins.")
        }
    }
    VCard(glow = VP.Weight) {
        CardHeader(Icons.Rounded.AutoAwesome, "Insights", VP.Weight)
        lines.forEach { line ->
            Row(Modifier.padding(top = 12.dp)) {
                Box(Modifier.padding(top = 4.dp).width(3.dp).height(32.dp).clip(CircleShape).background(VP.Weight.copy(alpha = 0.6f)))
                Spacer(Modifier.width(12.dp))
                Text(line, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

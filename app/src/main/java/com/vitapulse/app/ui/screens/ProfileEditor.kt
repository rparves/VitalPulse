@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitapulse.app.data.Nutrition
import com.vitapulse.app.data.Profile
import com.vitapulse.app.ui.AppViewModel
import com.vitapulse.app.ui.components.*
import com.vitapulse.app.ui.theme.VP
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(VP.brand),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Rounded.Favorite, null, tint = VP.OnLime) }
            Spacer(Modifier.width(12.dp))
            Text("VITAPULSE", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            LiveBadge("Private", VP.Cyan)
        }
        Spacer(Modifier.height(8.dp))
        Text("Move more.", style = MaterialTheme.typography.displaySmall)
        Text("Eat smarter.", style = MaterialTheme.typography.displaySmall, color = VP.Cyan)
        Text("Recover better.", style = MaterialTheme.typography.displaySmall, color = VP.Lime)
        Text(
            "Set up your profile in a minute. Everything stays encrypted on this phone.",
            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ProfileEditor(Profile(), "Get started") { vm.saveProfile(it.copy(onboarded = true)) }
    }
}

@Composable
fun EditProfileScreen(vm: AppViewModel, onDone: () -> Unit) {
    val loaded by vm.profile.collectAsStateWithLifecycle()
    val p = loaded?.profile ?: return
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Profile & goals") },
            navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } }
        )
        Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp)) {
            ProfileEditor(p, "Save") { vm.saveProfile(it); onDone() }
        }
    }
}

@Composable
fun ProfileEditor(initial: Profile, saveLabel: String, onSave: (Profile) -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var sex by remember { mutableStateOf(initial.sex) }
    var birthYear by remember { mutableStateOf(initial.birthYear.toString()) }
    var height by remember { mutableStateOf(initial.heightCm.roundToInt().toString()) }
    var weight by remember { mutableStateOf(initial.weightKg.toString()) }
    var activity by remember { mutableIntStateOf(initial.activityLevel) }
    var goalType by remember { mutableStateOf(initial.goalType) }
    var steps by remember { mutableStateOf(initial.stepGoal.toString()) }
    var kcal by remember { mutableStateOf(initial.calorieGoal.toString()) }
    var water by remember { mutableStateOf(initial.waterGoalMl.toString()) }
    var active by remember { mutableStateOf(initial.activeMinutesGoal.toString()) }
    var sleep by remember { mutableStateOf((initial.sleepGoalMin / 60.0).toString()) }
    var target by remember { mutableStateOf(initial.targetWeightKg.toString()) }

    val year = LocalDate.now().year
    val body: Profile? = run {
        val by = birthYear.toIntOrNull()?.takeIf { it in (year - 100)..(year - 13) }
        val h = height.toDoubleOrNull()?.takeIf { it in 100.0..250.0 }
        val w = weight.toDoubleOrNull()?.takeIf { it in 25.0..350.0 }
        if (by == null || h == null || w == null) null
        else initial.copy(
            name = name.trim().take(40), sex = sex, birthYear = by, heightCm = h, weightKg = w,
            activityLevel = activity, goalType = goalType
        )
    }
    val full: Profile? = body?.let {
        val s = steps.toIntOrNull()?.takeIf { v -> v in 1000..100000 }
        val k = kcal.toIntOrNull()?.takeIf { v -> v in 1200..6000 }
        val wa = water.toIntOrNull()?.takeIf { v -> v in 500..8000 }
        val a = active.toIntOrNull()?.takeIf { v -> v in 5..300 }
        val sl = sleep.toDoubleOrNull()?.takeIf { v -> v in 4.0..12.0 }
        val t = target.toDoubleOrNull()?.takeIf { v -> v in 25.0..350.0 }
        if (s == null || k == null || wa == null || a == null || sl == null || t == null) null
        else it.copy(stepGoal = s, calorieGoal = k, waterGoalMl = wa, activeMinutesGoal = a,
            sleepGoalMin = (sl * 60).roundToInt(), targetWeightKg = t)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("About you")
        OutlinedTextField(name, { name = it.take(40) }, Modifier.fillMaxWidth(), label = { Text("First name") }, singleLine = true)
        ChoiceRow(listOf("male" to "Male", "female" to "Female"), sex) { sex = it }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Birth year", birthYear, Modifier.weight(1f)) { birthYear = it }
            NumberField("Height (cm)", height, Modifier.weight(1f)) { height = it }
        }
        NumberField("Weight (kg)", weight, Modifier.fillMaxWidth(), decimal = true) { weight = it }
        Text("Activity level", style = MaterialTheme.typography.labelLarge)
        ChoiceRow(Nutrition.activityLabels.mapIndexed { i, l -> (i + 1) to l }, activity) { activity = it }
        Text("Goal", style = MaterialTheme.typography.labelLarge)
        ChoiceRow(listOf("lose" to "Lose weight", "maintain" to "Maintain", "gain" to "Build / gain"), goalType) { goalType = it }

        Spacer(Modifier.height(4.dp))
        SectionTitle("Daily targets")
        if (body != null) {
            val rec = Nutrition.recommendedCalories(body)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Suggested: $rec kcal/day", fontWeight = FontWeight.SemiBold)
                        Text(
                            "BMR ${Nutrition.bmr(body).roundToInt()} · TDEE ${Nutrition.tdee(body).roundToInt()} kcal",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { kcal = rec.toString() }) { Text("Use") }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Steps", steps, Modifier.weight(1f)) { steps = it }
            NumberField("Calories (kcal)", kcal, Modifier.weight(1f)) { kcal = it }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Water (ml)", water, Modifier.weight(1f)) { water = it }
            NumberField("Active (min)", active, Modifier.weight(1f)) { active = it }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField("Sleep (hours)", sleep, Modifier.weight(1f), decimal = true) { sleep = it }
            NumberField("Target weight (kg)", target, Modifier.weight(1f), decimal = true) { target = it }
        }
        if (full == null) {
            Text(
                "Check the values: age 13–100, height 100–250 cm, weight 25–350 kg, calories 1200–6000, steps 1,000–100,000, sleep 4–12 h.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error
            )
        }
        GradientButton(saveLabel, Modifier.fillMaxWidth(), enabled = full != null) { full?.let(onSave) }
        Spacer(Modifier.height(24.dp))
    }
}

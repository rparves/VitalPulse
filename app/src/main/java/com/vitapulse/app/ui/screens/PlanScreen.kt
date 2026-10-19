@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitapulse.app.data.BmiStandard
import com.vitapulse.app.data.BodyPlan
import com.vitapulse.app.data.BodyPlanner
import com.vitapulse.app.food.MealPlanner
import com.vitapulse.app.food.PlanMeal
import com.vitapulse.app.ui.*
import com.vitapulse.app.ui.components.*
import com.vitapulse.app.ui.theme.VP
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private fun bmiColor(bmi: Double, std: BmiStandard) = when {
    bmi < 18.5 -> VP.Carbs
    bmi <= std.healthyMax -> VP.Lime
    bmi <= std.overweightMax -> VP.Fuel
    else -> VP.Heart
}

@Composable
fun PlanScreen(vm: AppViewModel, onDone: () -> Unit) {
    val loaded by vm.profile.collectAsStateWithLifecycle()
    val p = loaded?.profile ?: return
    val context = LocalContext.current
    var std by rememberSaveable { mutableStateOf(BmiStandard.WHO) }
    var day by rememberSaveable { mutableIntStateOf(0) }
    val plan = remember(p.weightKg, p.heightCm, p.birthYear, p.sex, p.activityLevel, std) { BodyPlanner.build(p, std) }
    val meals = remember(plan.calories, day) { MealPlanner.day(day, plan.calories) }
    val applied = p.calorieGoal == plan.calories && p.stepGoal == plan.steps && p.targetWeightKg == plan.targetWeightKg
    val color = bmiColor(plan.bmi, std)

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Your body plan") },
            navigationIcon = { IconButton(onClick = onDone) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = VP.Bg),
        )
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ---------- BMI hero ----------
            item {
                VCard(glow = color) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Eyebrow("Body mass index", color)
                        Spacer(Modifier.weight(1f))
                        LiveBadge(plan.category, color)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(fmt1(plan.bmi), style = MaterialTheme.typography.displayMedium, color = color)
                        Spacer(Modifier.width(10.dp))
                        Text("${fmt1(p.weightKg)} kg · ${p.heightCm.roundToInt()} cm",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 10.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    BmiGauge(plan.bmi, std)
                    Spacer(Modifier.height(14.dp))
                    ChoiceRow(BmiStandard.entries.map { it to it.label }, std) { std = it }
                    Text(
                        if (std == BmiStandard.ASIAN)
                            "South Asian cut-offs: WHO notes health risks rise from BMI 23 in many Asian populations."
                        else "WHO standard: healthy range 18.5–24.9.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // ---------- Target ----------
            item {
                VCard(glow = VP.Cyan) {
                    Eyebrow("Your healthy target", VP.Cyan)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Healthy weight for your height: ${fmt1(plan.healthyMinKg)}–${fmt1(plan.healthyMaxKg)} kg",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("Target BMI", fmt1(plan.targetBmi), VP.Lime, Modifier.weight(1f))
                        StatPill("Target", "${fmt1(plan.targetWeightKg)} kg", VP.Cyan, Modifier.weight(1f))
                        StatPill(
                            "Change", when (plan.direction) {
                                "lose" -> "−${fmt1(p.weightKg - plan.targetWeightKg)} kg"
                                "gain" -> "+${fmt1(plan.targetWeightKg - p.weightKg)} kg"
                                else -> "Maintain"
                            }, VP.Fuel, Modifier.weight(1f)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(planSummary(plan), style = MaterialTheme.typography.bodyMedium)
                    if (plan.isMinor) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "For under-18s, BMI is judged with age-and-sex percentile charts, so VitaPulse suggests maintenance targets only. A doctor can advise on growth and weight.",
                            style = MaterialTheme.typography.bodySmall, color = VP.Carbs
                        )
                    } else if (plan.bmi >= 35 || plan.bmi < 17) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "At this BMI, it's worth checking in with a doctor before starting — they can tailor the plan and check for related health issues.",
                            style = MaterialTheme.typography.bodySmall, color = VP.Carbs
                        )
                    }
                }
            }

            // ---------- Daily targets ----------
            item {
                VCard(glow = VP.Lime) {
                    Eyebrow("Daily targets", VP.Lime)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("Calories", fmt(plan.calories), VP.Fuel, Modifier.weight(1f))
                        StatPill("Protein", "${plan.proteinG} g", VP.Protein, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("Carbs", "${plan.carbsG} g", VP.Carbs, Modifier.weight(1f))
                        StatPill("Fat", "${plan.fatG} g", VP.Fat, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("Steps", fmt(plan.steps), VP.Steps, Modifier.weight(1f))
                        StatPill("Active", "${plan.activeMin} min", VP.Lime, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatPill("Water", "${fmt1(plan.waterMl / 1000.0)} L", VP.Water, Modifier.weight(1f))
                        StatPill("Sleep", "${plan.sleepMin / 60} h", VP.Sleep, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        when (plan.direction) {
                            "lose" -> "Strength training 2–3× a week helps you keep muscle while losing fat."
                            "gain" -> "Pair the extra food with strength training 3× a week so the gain is mostly muscle."
                            else -> "Keep a mix of cardio and strength work to stay in your healthy range."
                        },
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    GradientButton(
                        if (applied) "Targets applied ✓" else "Apply these targets",
                        Modifier.fillMaxWidth(), Icons.Rounded.Flag, enabled = !applied
                    ) {
                        vm.applyPlan(plan)
                        Toast.makeText(context, "Daily targets updated", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // ---------- Diet plan ----------
            item {
                Column {
                    Eyebrow("Suggested diet plan", VP.Fuel)
                    Text("${fmt(plan.calories)} kcal a day", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (0..6).forEach { i ->
                            val d = LocalDate.now().plusDays(i.toLong())
                            FilterChip(
                                selected = day == i, onClick = { day = i },
                                label = { Text(if (i == 0) "Today" else d.format(DateTimeFormatter.ofPattern("EEE"))) },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = VP.Lime, selectedLabelColor = VP.OnLime),
                            )
                        }
                    }
                }
            }
            meals.forEach { m ->
                item(key = "meal_${day}_${m.meal}") {
                    MealPlanCard(m) {
                        vm.logMeal(m)
                        Toast.makeText(context, "${m.meal} added to today's food log", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            item {
                val total = meals.sumOf { it.kcal }.roundToInt()
                val protein = meals.sumOf { it.protein }.roundToInt()
                Text(
                    "Day total ≈ ${fmt(total)} kcal · ${protein} g protein. Swap any dish for one you like with similar calories — " +
                        "use Food → Search to compare. Cook with little oil: 1 tbsp adds about 120 kcal.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item {
                Text(
                    "BMI doesn't distinguish muscle from fat and isn't designed for pregnancy, athletes or older adults. " +
                        "This plan is general guidance, not medical advice — if you have a health condition, ask a doctor or dietitian.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(18.dp)) { Text("Done") } }
        }
    }
}

private fun planSummary(plan: BodyPlan): String {
    val date = LocalDate.now().plusWeeks(plan.weeks.toLong()).format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    return when (plan.direction) {
        "lose" -> "Losing about ${fmt1(plan.weeklyRateKg)} kg a week, you could reach ${fmt1(plan.targetWeightKg)} kg in about ${plan.weeks} weeks (around $date). Slow and steady keeps the weight off."
        "gain" -> "Gaining about ${fmt1(plan.weeklyRateKg)} kg a week, you could reach ${fmt1(plan.targetWeightKg)} kg in about ${plan.weeks} weeks (around $date)."
        else -> "You're in your healthy range — the goal is to stay here and build fitness."
    }
}

@Composable
private fun BmiGauge(bmi: Double, std: BmiStandard) {
    val lo = 15.0
    val hi = 40.0
    val segs = listOf(
        lo to 18.5 to VP.Carbs,
        18.5 to std.healthyMax to VP.Lime,
        std.healthyMax to std.overweightMax to VP.Fuel,
        std.overweightMax to hi to VP.Heart,
    )
    val markerColor = MaterialTheme.colorScheme.onSurface
    Column {
        Canvas(Modifier.fillMaxWidth().height(22.dp)) {
            val barH = 10.dp.toPx()
            val y = (size.height - barH) / 2
            fun x(v: Double) = ((v.coerceIn(lo, hi) - lo) / (hi - lo) * size.width).toFloat()
            segs.forEach { (range, c) ->
                val (a, b) = range
                drawRoundRect(c.copy(alpha = 0.85f), Offset(x(a) + 1.5f, y), Size(x(b) - x(a) - 3f, barH), CornerRadius(barH / 2))
            }
            val mx = x(bmi)
            drawCircle(Color.Black, 11.dp.toPx(), Offset(mx, size.height / 2))
            drawCircle(markerColor, 8.dp.toPx(), Offset(mx, size.height / 2))
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            listOf("Under", "Healthy", "Over", "Obese").forEach {
                Text(it, Modifier.weight(1f), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MealPlanCard(m: PlanMeal, onLog: () -> Unit) {
    val icon = when (m.meal) {
        "Breakfast" -> Icons.Rounded.WbSunny
        "Lunch" -> Icons.Rounded.LunchDining
        "Snack" -> Icons.Rounded.Cookie
        else -> Icons.Rounded.DinnerDining
    }
    VCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon, VP.Fuel)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(m.meal, style = MaterialTheme.typography.titleMedium)
                Text("${m.kcal.roundToInt()} kcal · ${m.protein.roundToInt()} g protein",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(onClick = onLog, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Icon(Icons.Rounded.Add, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Log")
            }
        }
        m.items.forEach { item ->
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(item.food.name, fontWeight = FontWeight.Medium)
                    Text(
                        listOfNotNull("${item.grams.roundToInt()} g", item.pieces).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text("${item.kcal.roundToInt()} kcal", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

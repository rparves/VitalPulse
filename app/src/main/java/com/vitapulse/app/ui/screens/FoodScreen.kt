@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vitapulse.app.data.FoodEntry
import com.vitapulse.app.food.FoodCandidate
import com.vitapulse.app.ui.*
import com.vitapulse.app.ui.components.*
import com.vitapulse.app.ui.theme.VP
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun FoodScreen(vm: AppViewModel, onScan: () -> Unit) {
    val loaded by vm.profile.collectAsStateWithLifecycle()
    val p = loaded?.profile ?: return
    val food by vm.foodToday.collectAsStateWithLifecycle()
    val day by vm.today.collectAsStateWithLifecycle()
    val workouts by vm.workoutsToday.collectAsStateWithLifecycle()
    var showSearch by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<FoodCandidate?>(null) }

    val eaten = food.sumOf { it.kcal }
    val exercise = maxOf(day.activeKcal, workouts.sumOf { it.kcal })
    val remaining = p.calorieGoal - eaten + exercise

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Food", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp))
        }
        item {
            VCard {
                CardHeader(Icons.Rounded.LocalFireDepartment, "Calorie budget", VP.Fuel)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BudgetCell("Goal", p.calorieGoal.toDouble())
                    BudgetCell("− Food", eaten)
                    BudgetCell("+ Exercise", exercise)
                    BudgetCell(if (remaining >= 0) "= Left" else "= Over", abs(remaining), highlight = true)
                }
                Spacer(Modifier.height(12.dp))
                ProgressLine((eaten / (p.calorieGoal + exercise)).toFloat(), VP.Fuel, 8.dp)
            }
        }
        item { MacroCard(food) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onScan, modifier = Modifier.weight(1f).height(52.dp)) {
                    Icon(Icons.Rounded.CameraAlt, null); Spacer(Modifier.width(8.dp)); Text("Scan food")
                }
                OutlinedButton(onClick = { showSearch = true }, modifier = Modifier.weight(1f).height(52.dp)) {
                    Icon(Icons.Rounded.Search, null); Spacer(Modifier.width(8.dp)); Text("Search")
                }
            }
        }
        MEALS.forEach { meal ->
            item(key = meal) { MealCard(meal, food.filter { it.meal == meal }) { vm.deleteFood(it) } }
        }
        item {
            Text(
                "Packaged food data © Open Food Facts contributors (ODbL). Photo estimates are approximate.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showSearch) FoodSearchDialog(onPick = { selected = it; showSearch = false }, onDismiss = { showSearch = false })
    selected?.let { c ->
        AddFoodDialog(c, onDismiss = { selected = null }) { g, meal -> vm.logFood(c, g, meal); selected = null }
    }
}

@Composable
private fun BudgetCell(label: String, value: Double, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.roundToInt().toString(), style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold, color = if (highlight) VP.Fuel else Color.Unspecified)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MacroCard(food: List<FoodEntry>) {
    val pr = food.sumOf { it.proteinG }
    val cb = food.sumOf { it.carbsG }
    val ft = food.sumOf { it.fatG }
    val totalKcal = (pr * 4 + cb * 4 + ft * 9).coerceAtLeast(1.0)
    VCard {
        CardHeader(Icons.Rounded.PieChart, "Macros", MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape)) {
            val parts = listOf(pr * 4 to VP.Protein, cb * 4 to VP.Carbs, ft * 9 to VP.Fat)
            if (food.isEmpty()) Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerHigh))
            else parts.forEach { (k, c) ->
                if (k > 0) Box(Modifier.weight((k / totalKcal).toFloat()).fillMaxHeight().background(c))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            MacroLabel("Protein", pr, VP.Protein)
            MacroLabel("Carbs", cb, VP.Carbs)
            MacroLabel("Fat", ft, VP.Fat)
        }
    }
}

@Composable
private fun MacroLabel(label: String, grams: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text("$label ${grams.roundToInt()} g", style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun MealCard(meal: String, items: List<FoodEntry>, onDelete: (FoodEntry) -> Unit) {
    VCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(meal, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${items.sumOf { it.kcal }.roundToInt()} kcal", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (items.isEmpty()) {
            Text("Nothing logged yet", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
        }
        items.forEach { e ->
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(e.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${e.grams.roundToInt()} g · ${e.source}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${e.kcal.roundToInt()}", fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { onDelete(e) }) { Icon(Icons.Rounded.DeleteOutline, "Delete") }
            }
        }
    }
}

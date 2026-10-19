@file:OptIn(ExperimentalMaterial3Api::class)

package com.vitapulse.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vitapulse.app.food.FoodCandidate
import com.vitapulse.app.food.FoodCatalog
import com.vitapulse.app.food.OpenFoodFacts
import com.vitapulse.app.ui.MEALS
import com.vitapulse.app.ui.components.ChoiceRow
import com.vitapulse.app.ui.defaultMeal
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CandidateRow(c: FoodCandidate, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(c.name, maxLines = 2, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
            Text(c.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("${c.kcal100.roundToInt()} kcal/100g", style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun FoodSearchDialog(onPick: (FoodCandidate) -> Unit, onDismiss: () -> Unit) {
    var q by remember { mutableStateOf("") }
    var remote by remember { mutableStateOf<List<FoodCandidate>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val local = remember(q) { FoodCatalog.search(q) }
    val runSearch = {
        if (q.isNotBlank()) {
            loading = true
            scope.launch {
                remote = OpenFoodFacts.search(q)
                loading = false
                searched = true
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("Search food") },
        text = {
            Column {
                OutlinedTextField(
                    q, { q = it.take(60) }, Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. dal, oats, brand name") },
                    singleLine = true,
                    trailingIcon = { IconButton(onClick = { runSearch() }) { Icon(Icons.Rounded.Search, "Search online") } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { runSearch() }),
                )
                if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
                if (searched && !loading && remote.isEmpty() && local.isEmpty()) {
                    Text("No matches. Try a simpler name.", Modifier.padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall)
                }
                LazyColumn(Modifier.heightIn(max = 380.dp)) {
                    items(local + remote) { c -> CandidateRow(c) { onPick(c) } }
                }
            }
        }
    )
}

@Composable
fun AddFoodDialog(c: FoodCandidate, onDismiss: () -> Unit, onLog: (Double, String) -> Unit) {
    var grams by remember(c) { mutableStateOf(c.servingG.roundToInt().toString()) }
    var meal by remember { mutableStateOf(defaultMeal()) }
    val g = grams.toDoubleOrNull()?.takeIf { it > 0 && it <= 5000 }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(c.name, maxLines = 3, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${c.source} · ${c.kcal100.roundToInt()} kcal per 100 g",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    grams, { grams = it.filter { ch -> ch.isDigit() || ch == '.' }.take(6) },
                    Modifier.fillMaxWidth(), label = { Text("Portion (grams)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                ChoiceRow(
                    listOf(0.5 to "½ serving", 1.0 to "1 serving", 2.0 to "2 servings"),
                    -1.0
                ) { mult -> grams = (c.servingG * mult).roundToInt().toString() }
                ChoiceRow(MEALS.map { it to it }, meal) { meal = it }
                if (g != null) {
                    val f = g / 100
                    Text("${(c.kcal100 * f).roundToInt()} kcal", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text(
                        "Protein ${(c.protein100 * f).roundToInt()} g · Carbs ${(c.carbs100 * f).roundToInt()} g · Fat ${(c.fat100 * f).roundToInt()} g",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (c.source != "Open Food Facts") {
                    Text("Estimate — adjust the portion to match what's on your plate.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { Button(onClick = { g?.let { onLog(it, meal) } }, enabled = g != null) { Text("Log food") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

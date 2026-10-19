@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.vitapulse.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.vitapulse.app.data.WorkoutLog
import com.vitapulse.app.ui.AppViewModel
import com.vitapulse.app.ui.components.CardHeader
import com.vitapulse.app.ui.components.VCard
import com.vitapulse.app.ui.fmt
import com.vitapulse.app.ui.fmt1
import com.vitapulse.app.ui.theme.VP
import kotlin.math.roundToInt
import com.vitapulse.app.exercise.Exercise
import com.vitapulse.app.exercise.ExerciseRepo
import kotlinx.coroutines.delay

private val categories = listOf(
    "featured" to "Featured", "cardio" to "Cardio", "strength" to "Strength", "stretching" to "Stretching",
    "plyometrics" to "Plyometrics", "powerlifting" to "Powerlifting", "olympic weightlifting" to "Olympic",
    "strongman" to "Strongman",
)

private val muscles = listOf(
    "chest", "shoulders", "biceps", "triceps", "lats", "middle back", "lower back", "abdominals",
    "quadriceps", "hamstrings", "glutes", "calves", "forearms", "traps",
)

fun String.titleCase() = split(' ').joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }

/** Plays the bundled GIF, or alternates the two online frames to animate the movement. */
@Composable
fun ExerciseAnimation(ex: Exercise, modifier: Modifier = Modifier) {
    val gif = ex.localGif
    if (gif != null) {
        AsyncImage(model = gif, contentDescription = ex.name, contentScale = ContentScale.Crop, modifier = modifier)
        return
    }
    val frames = ex.remoteFrames
    var second by remember(ex.id) { mutableStateOf(false) }
    LaunchedEffect(ex.id) {
        while (frames.size > 1) { delay(900); second = !second }
    }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)) {
        frames.getOrNull(0)?.let {
            AsyncImage(model = it, contentDescription = ex.name, contentScale = ContentScale.Crop, modifier = Modifier.matchParentSize())
        }
        frames.getOrNull(1)?.let {
            AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().alpha(if (second) 1f else 0f))
        }
    }
}

@Composable
fun WorkoutsScreen(vm: AppViewModel, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val todayLogs by vm.workoutsToday.collectAsStateWithLifecycle()
    var all by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    LaunchedEffect(Unit) { all = ExerciseRepo.all(context) }

    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("featured") }
    var muscle by rememberSaveable { mutableStateOf<String?>(null) }

    val byId = remember(all) { all.associateBy { it.id } }
    val filtered = remember(all, query, category, muscle) {
        val q = query.trim().lowercase()
        all.filter { e ->
            val categoryOk = if (category == "featured") {
                // "Featured" with no search/filter shows the curated set; with one, it searches everything.
                q.isNotEmpty() || muscle != null || e.id in ExerciseRepo.featuredIds
            } else e.category == category
            val queryOk = q.isEmpty() || e.name.lowercase().contains(q) ||
                e.primaryMuscles.any { it.contains(q) } || (e.equipment ?: "").contains(q)
            categoryOk && queryOk && (muscle == null || muscle in e.primaryMuscles)
        }.sortedByDescending { it.id in ExerciseRepo.featuredIds }
    }
    val showGroups = category == "featured" && query.isBlank() && muscle == null

    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Workouts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp))
                Text("${all.size} exercises with step-by-step form guides",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    query, { query = it.take(40) }, Modifier.fillMaxWidth(),
                    placeholder = { Text("Search: plank, squat, chest press…") },
                    leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Clear") } },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { (key, label) ->
                        FilterChip(selected = category == key, onClick = { category = key }, label = { Text(label) })
                    }
                }
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    muscles.forEach { m ->
                        FilterChip(selected = muscle == m, onClick = { muscle = if (muscle == m) null else m },
                            label = { Text(m.titleCase()) })
                    }
                }
            }
        }

        if (todayLogs.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) { TodayTraining(todayLogs, vm::deleteWorkout) }
        }

        if (all.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        } else if (showGroups) {
            ExerciseRepo.featuredGroups.forEach { (group, ids) ->
                item(span = { GridItemSpan(maxLineSpan) }, key = "h_$group") {
                    Text(group, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 8.dp))
                }
                val list = ids.mapNotNull { byId[it] }
                items(list, key = { "f_${it.id}" }) { ex -> ExerciseCard(ex) { onOpen(ex.id) } }
            }
        } else {
            if (filtered.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("No exercises match. Try another word or clear a filter.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(filtered.take(300), key = { it.id }) { ex -> ExerciseCard(ex) { onOpen(ex.id) } }
        }
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text("Exercise images and instructions: free-exercise-db (public domain). Non-featured exercises load images online.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun ExerciseCard(ex: Exercise, onClick: () -> Unit) {
    Column(
        Modifier.clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainer).clickable(onClick = onClick)
    ) {
        ExerciseAnimation(ex, Modifier.fillMaxWidth().aspectRatio(4f / 3f))
        Column(Modifier.padding(12.dp)) {
            Text(ex.name, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall)
            Text(
                listOfNotNull(ex.primaryMuscles.firstOrNull()?.titleCase(), ex.equipment?.titleCase()).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

fun WorkoutLog.summary(): String =
    if (sets > 0) {
        "$sets × $reps" + (if (weightKg > 0) " · ${fmt1(weightKg)} kg" else " · bodyweight")
    } else "$durationMin min"

@Composable
private fun TodayTraining(logs: List<WorkoutLog>, onDelete: (WorkoutLog) -> Unit) {
    val minutes = logs.sumOf { it.durationMin }
    val volume = logs.sumOf { it.sets * it.reps * it.weightKg }
    val kcal = logs.sumOf { it.kcal }
    VCard {
        CardHeader(Icons.Rounded.FitnessCenter, "Today's training", VP.Active, "$minutes min")
        logs.forEach { w ->
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(w.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                    Text(w.summary(), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onDelete(w) }) { Icon(Icons.Rounded.DeleteOutline, "Delete") }
            }
        }
        Text(
            buildString {
                append("$minutes min")
                if (volume > 0) append(" · ${fmt(volume.roundToInt())} kg lifted")
                append(" · ~${kcal.roundToInt()} kcal")
            },
            style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp)
        )
    }
}

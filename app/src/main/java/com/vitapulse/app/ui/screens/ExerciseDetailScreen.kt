@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.vitapulse.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vitapulse.app.exercise.Exercise
import com.vitapulse.app.exercise.ExerciseRepo
import com.vitapulse.app.ui.components.CardHeader
import com.vitapulse.app.ui.components.VCard

@Composable
fun ExerciseDetailScreen(id: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var ex by remember { mutableStateOf<Exercise?>(null) }
    LaunchedEffect(id) { ex = ExerciseRepo.all(context).firstOrNull { it.id == id } }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(ex?.name ?: "", maxLines = 1) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } }
        )
        val e = ex ?: return@Column
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ExerciseAnimation(e, Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(24.dp)))
            Text(e.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOfNotNull(e.category, e.level, e.equipment, e.mechanic, e.force?.let { "$it movement" })
                    .forEach { AssistChip(onClick = {}, label = { Text(it.titleCase()) }) }
            }
            VCard {
                Text("Muscles worked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Primary: " + e.primaryMuscles.joinToString { it.titleCase() })
                if (e.secondaryMuscles.isNotEmpty()) {
                    Text("Secondary: " + e.secondaryMuscles.joinToString { it.titleCase() },
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            VCard {
                Text("How to do it", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                e.instructions.forEachIndexed { i, step ->
                    Row(Modifier.padding(top = 10.dp)) {
                        Box(
                            Modifier.size(26.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${i + 1}", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                }
            }
            VCard {
                CardHeader(Icons.Rounded.TipsAndUpdates, "Suggested starting point", MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                Text(
                    when (e.category) {
                        "cardio" -> "20–30 minutes at a pace where you can still talk in short sentences. Build up gradually."
                        "stretching" -> "Hold 20–30 seconds, 2–3 times per side. Move slowly and never stretch into pain."
                        "plyometrics" -> "2–3 sets of 5–8 reps with full rest between sets. Land softly."
                        else -> if (e.id == "Plank") "3 holds of 20–45 seconds, keeping hips level and core braced."
                                else "3 sets of 8–12 reps, resting 60–90 seconds. Choose a weight that keeps your form clean."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("Stop if you feel sharp pain. Check with a doctor before starting a new program if you have a health condition.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

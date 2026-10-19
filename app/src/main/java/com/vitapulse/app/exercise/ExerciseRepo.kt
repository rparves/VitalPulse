package com.vitapulse.app.exercise

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray

data class Exercise(
    val id: String,
    val name: String,
    val category: String,
    val level: String,
    val equipment: String?,
    val force: String?,
    val mechanic: String?,
    val primaryMuscles: List<String>,
    val secondaryMuscles: List<String>,
    val instructions: List<String>,
    val images: List<String>,
) {
    /** Animated GIF bundled in the APK (works offline), or null if only online frames exist. */
    val localGif: String? get() = if (id in ExerciseRepo.featuredIds) "file:///android_asset/exercises/${gifName(id)}" else null

    /** Start/end position frames from the open dataset (public domain), loaded over HTTPS. */
    val remoteFrames: List<String> get() = images.map { ExerciseRepo.IMAGE_BASE + it }

    companion object {
        fun gifName(id: String) = id.lowercase().replace('-', '_') + ".gif"
    }
}

/**
 * Exercise library from free-exercise-db (https://github.com/yuhonas/free-exercise-db),
 * released into the public domain (Unlicense). 876 exercises; metadata is bundled offline.
 */
object ExerciseRepo {
    const val IMAGE_BASE = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

    /** Curated sets shown first, each with a bundled GIF. */
    val featuredGroups: List<Pair<String, List<String>>> = listOf(
        "Cardio" to listOf("Running_Treadmill", "Walking_Treadmill", "Bicycling_Stationary", "Elliptical_Trainer",
            "Rowing_Stationary", "Rope_Jumping", "Stairmaster", "Mountain_Climbers", "Front_Box_Jump"),
        "Core" to listOf("Plank", "Crunches", "Russian_Twist", "Push_Up_to_Side_Plank"),
        "Legs & glutes" to listOf("Bodyweight_Squat", "Barbell_Squat", "Leg_Press", "Dumbbell_Lunges",
            "Bodyweight_Walking_Lunge", "Barbell_Deadlift", "Barbell_Glute_Bridge", "Standing_Calf_Raises"),
        "Chest" to listOf("Leverage_Chest_Press", "Cable_Chest_Press", "Barbell_Bench_Press_-_Medium_Grip",
            "Dumbbell_Bench_Press", "Pushups", "Dumbbell_Flyes", "Butterfly", "Dips_-_Chest_Version"),
        "Back" to listOf("Pullups", "Wide-Grip_Lat_Pulldown", "Seated_Cable_Rows", "Bent_Over_Barbell_Row"),
        "Shoulders & arms" to listOf("Dumbbell_Shoulder_Press", "Side_Lateral_Raise", "Barbell_Curl",
            "Alternate_Hammer_Curl", "Triceps_Pushdown", "Bench_Dips", "One-Arm_Kettlebell_Swings"),
    )
    val featuredIds: Set<String> = featuredGroups.flatMap { it.second }.toSet()

    private val mutex = Mutex()
    @Volatile private var cache: List<Exercise>? = null

    suspend fun all(context: Context): List<Exercise> = cache ?: mutex.withLock {
        cache ?: withContext(Dispatchers.IO) {
            val text = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
            val arr = JSONArray(text)
            fun JSONArray?.strings(): List<String> =
                if (this == null) emptyList() else (0 until length()).map { getString(it) }
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Exercise(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    category = o.optString("category"),
                    level = o.optString("level"),
                    equipment = o.optString("equipment").takeIf { it.isNotBlank() && it != "null" },
                    force = o.optString("force").takeIf { it.isNotBlank() && it != "null" },
                    mechanic = o.optString("mechanic").takeIf { it.isNotBlank() && it != "null" },
                    primaryMuscles = o.optJSONArray("primaryMuscles").strings(),
                    secondaryMuscles = o.optJSONArray("secondaryMuscles").strings(),
                    instructions = o.optJSONArray("instructions").strings(),
                    images = o.optJSONArray("images").strings(),
                )
            }.sortedBy { it.name }.also { cache = it }
        }
    }
}

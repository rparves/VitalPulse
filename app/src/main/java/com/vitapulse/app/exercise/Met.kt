package com.vitapulse.app.exercise

import kotlin.math.ceil

/** Energy estimates from the Compendium of Physical Activities (MET values, approximate). */
object Met {
    fun of(ex: Exercise): Double = when (ex.id) {
        "Running_Treadmill" -> 9.8
        "Jogging_Treadmill" -> 7.0
        "Walking_Treadmill", "Trail_Running_Walking" -> 3.8
        "Bicycling", "Bicycling_Stationary", "Recumbent_Bike" -> 6.8
        "Elliptical_Trainer" -> 5.0
        "Rowing_Stationary" -> 7.0
        "Rope_Jumping" -> 11.8
        "Stairmaster", "Step_Mill" -> 9.0
        "Mountain_Climbers" -> 8.0
        "Plank", "Push_Up_to_Side_Plank" -> 3.8
        else -> when (ex.category) {
            "cardio" -> 7.0
            "plyometrics" -> 8.0
            "stretching" -> 2.3
            "powerlifting", "olympic weightlifting", "strongman" -> 6.0
            else -> 5.0
        }
    }

    /** Logged by duration instead of sets × reps × weight. */
    fun isTimed(ex: Exercise): Boolean =
        ex.category == "cardio" || ex.category == "stretching" ||
            ex.id in setOf("Plank", "Push_Up_to_Side_Plank", "Mountain_Climbers")

    /** Strength sets take roughly 2.5 minutes each including rest. */
    fun minutesForSets(sets: Int): Int = ceil(sets * 2.5).toInt().coerceAtLeast(1)

    fun kcal(ex: Exercise, minutes: Int, bodyKg: Double): Double = of(ex) * bodyKg * minutes / 60.0
}

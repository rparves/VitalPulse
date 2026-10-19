package com.vitapulse.app.data

import java.time.LocalDate
import kotlin.math.roundToInt

/** Energy math: Mifflin-St Jeor BMR × activity factor. */
object Nutrition {
    private val factors = listOf(1.2, 1.375, 1.55, 1.725, 1.9)
    val activityLabels = listOf("Sedentary", "Light", "Moderate", "Active", "Athlete")

    fun age(p: Profile) = LocalDate.now().year - p.birthYear

    fun bmr(p: Profile): Double =
        10 * p.weightKg + 6.25 * p.heightCm - 5 * age(p) + if (p.sex == "female") -161 else 5

    fun tdee(p: Profile) = bmr(p) * factors[(p.activityLevel - 1).coerceIn(0, 4)]

    /** Moderate deficit/surplus with a safe floor. */
    fun recommendedCalories(p: Profile): Int {
        val base = when (p.goalType) {
            "lose" -> tdee(p) - 500
            "gain" -> tdee(p) + 300
            else -> tdee(p)
        }
        val floor = if (p.sex == "female") 1200.0 else 1500.0
        return (base.coerceAtLeast(floor) / 10).roundToInt() * 10
    }

    fun bmi(p: Profile) = p.weightKg / ((p.heightCm / 100) * (p.heightCm / 100))

    fun bmiCategory(bmi: Double) = when {
        bmi < 18.5 -> "Underweight"
        bmi < 25 -> "Healthy range"
        bmi < 30 -> "Overweight"
        else -> "Obese range"
    }
}

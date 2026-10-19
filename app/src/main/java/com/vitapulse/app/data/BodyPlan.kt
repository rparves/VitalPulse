package com.vitapulse.app.data

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * BMI cut-offs. WHO's standard healthy range is 18.5–24.9. WHO also notes that for many
 * Asian populations, health risk rises from BMI 23, so a South Asian option is offered.
 */
enum class BmiStandard(val label: String, val healthyMax: Double, val overweightMax: Double) {
    WHO("Standard (WHO)", 24.9, 29.9),
    ASIAN("South Asian", 22.9, 24.9),
}

data class BodyPlan(
    val bmi: Double,
    val category: String,
    val standard: BmiStandard,
    val healthyMinKg: Double,
    val healthyMaxKg: Double,
    val targetBmi: Double,
    val targetWeightKg: Double,
    val direction: String,          // lose | gain | maintain (matches Profile.goalType)
    val weeklyRateKg: Double,
    val weeks: Int,
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val steps: Int,
    val activeMin: Int,
    val waterMl: Int,
    val sleepMin: Int,
    val isMinor: Boolean,
)

object BodyPlanner {
    fun bmi(weightKg: Double, heightCm: Double): Double {
        val h = heightCm / 100
        return weightKg / (h * h)
    }

    fun category(bmi: Double, std: BmiStandard): String = when {
        bmi < 18.5 -> "Underweight"
        bmi <= std.healthyMax -> "Healthy weight"
        bmi <= std.overweightMax -> "Overweight"
        else -> "Obesity range"
    }

    private fun round1(x: Double) = (x * 10).roundToInt() / 10.0

    fun build(p: Profile, std: BmiStandard): BodyPlan {
        val h2 = (p.heightCm / 100) * (p.heightCm / 100)
        val bmi = bmi(p.weightKg, p.heightCm)
        val minor = Nutrition.age(p) < 18

        // A realistic first target: just inside the healthy range (adults only).
        val targetBmi = when {
            minor -> bmi
            bmi > std.healthyMax -> std.healthyMax - 0.4
            bmi < 18.5 -> 20.0
            else -> bmi
        }
        val targetKg = round1(targetBmi * h2)
        val diff = targetKg - p.weightKg
        val direction = when {
            abs(diff) < 0.5 -> "maintain"
            diff < 0 -> "lose"
            else -> "gain"
        }
        // Safe pace: ~0.5 kg/week loss (0.75 if BMI ≥ 30), ~0.25 kg/week gain.
        val rate = when (direction) {
            "lose" -> if (bmi >= 30) 0.75 else 0.5
            "gain" -> 0.25
            else -> 0.0
        }
        val weeks = if (rate > 0) ceil(abs(diff) / rate).toInt() else 0

        // ~7,700 kcal per kg of body weight.
        val tdee = Nutrition.tdee(p)
        val raw = when (direction) {
            "lose" -> tdee - rate * 7700 / 7
            "gain" -> tdee + rate * 7700 / 7
            else -> tdee
        }
        val floor = if (p.sex == "female") 1200.0 else 1500.0
        val calories = ((raw.coerceAtLeast(floor)) / 10).roundToInt() * 10

        val proteinRef = if (direction == "lose") targetKg else p.weightKg
        val protein = ((if (direction == "maintain") 1.2 else 1.6) * proteinRef).roundToInt()
        val fat = (calories * 0.28 / 9).roundToInt()
        val carbs = ((calories - protein * 4 - fat * 9) / 4.0).roundToInt().coerceAtLeast(100)

        return BodyPlan(
            bmi = bmi,
            category = category(bmi, std),
            standard = std,
            healthyMinKg = round1(18.5 * h2),
            healthyMaxKg = round1(std.healthyMax * h2),
            targetBmi = targetBmi,
            targetWeightKg = targetKg,
            direction = direction,
            weeklyRateKg = rate,
            weeks = weeks,
            calories = calories,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat,
            steps = when (direction) { "lose" -> 10000; "gain" -> 7000; else -> 8000 },
            activeMin = when (direction) { "lose" -> 45; else -> 30 },
            waterMl = ((p.weightKg * 35) / 100).roundToInt().times(100).coerceIn(1500, 4000),
            sleepMin = 480,
            isMinor = minor,
        )
    }
}

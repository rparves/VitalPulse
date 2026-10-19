package com.vitapulse.app.data

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.roundToInt

data class ReadinessPart(val label: String, val score: Int, val detail: String)

data class Readiness(
    val score: Int,
    val label: String,
    val advice: String,
    val tip: String?,
    val parts: List<ReadinessPart>,
)

/**
 * Daily readiness (0–100): a weighted blend of last night's sleep and yesterday's
 * training load, fuel and hydration. Components without data are left out and the
 * remaining weights are rescaled; at least two components are required.
 * This is a motivational guide, not a medical measurement.
 */
object ReadinessCalc {
    fun compute(
        p: Profile,
        sleepMin: Long?,
        yesterdayActiveMin: Long?,
        yesterdayKcal: Double?,
        yesterdayWaterMl: Int?,
    ): Readiness? {
        val parts = mutableListOf<Pair<ReadinessPart, Double>>()

        sleepMin?.takeIf { it > 0 }?.let {
            val s = (min(it.toDouble() / p.sleepGoalMin, 1.0) * 100).roundToInt()
            parts += ReadinessPart("Sleep", s, "${it / 60}h ${it % 60}m last night") to 0.35
        }
        yesterdayActiveMin?.let {
            val r = it.toDouble() / p.activeMinutesGoal.coerceAtLeast(1)
            val s = when {
                r <= 1.5 -> 100
                r >= 3.5 -> 55
                else -> (100 - (r - 1.5) / 2.0 * 45).roundToInt()
            }
            parts += ReadinessPart("Recovery", s, "$it active min yesterday") to 0.25
        }
        yesterdayKcal?.takeIf { it > 0 }?.let {
            val r = it / p.calorieGoal
            val s = (100 - abs(1 - r) * 150).coerceIn(0.0, 100.0).roundToInt()
            parts += ReadinessPart("Fuel", s, "${it.roundToInt()} of ${p.calorieGoal} kcal yesterday") to 0.25
        }
        yesterdayWaterMl?.takeIf { it > 0 }?.let {
            val s = (min(it.toDouble() / p.waterGoalMl, 1.0) * 100).roundToInt()
            parts += ReadinessPart("Hydration", s, "$it ml water yesterday") to 0.15
        }
        if (parts.size < 2) return null

        val score = (parts.sumOf { it.first.score * it.second } / parts.sumOf { it.second }).roundToInt()
        val (label, advice) = when {
            score >= 80 -> "Ready to push" to "Good day for a hard session or a new personal best."
            score >= 60 -> "Train moderately" to "A normal workout is fine — keep the intensity steady."
            score >= 40 -> "Go easy" to "Choose light cardio, mobility or technique work today."
            else -> "Rest & recover" to "Prioritise rest, water, good food and an early night."
        }
        val weakest = parts.minBy { it.first.score }.first
        val tip = if (weakest.score >= 80) null else when (weakest.label) {
            "Sleep" -> "Sleep was the weakest factor — aim for ${p.sleepGoalMin / 60}h tonight."
            "Recovery" -> "Yesterday was a heavy training day — let your body catch up."
            "Fuel" -> "Yesterday's intake was far from your target — steady meals help recovery."
            else -> "You drank little yesterday — keep a bottle nearby today."
        }
        return Readiness(score, label, advice, tip, parts.map { it.first })
    }
}

object Streaks {
    /** Returns (current, best) streak of consecutive days with anything logged. */
    fun compute(daysDesc: List<Long>, today: Long): Pair<Int, Int> {
        if (daysDesc.isEmpty()) return 0 to 0
        val set = daysDesc.toHashSet()
        var d = if (today in set) today else today - 1
        var current = 0
        while (d in set) { current++; d-- }
        var best = 0
        var run = 0
        var prev: Long? = null
        for (day in daysDesc.sorted()) {
            run = if (prev != null && day == prev + 1) run + 1 else 1
            best = maxOf(best, run)
            prev = day
        }
        return current to maxOf(best, current)
    }
}

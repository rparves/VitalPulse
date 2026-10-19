package com.vitapulse.app.food

import kotlin.math.roundToInt

data class PlanItem(val food: FoodCandidate, val grams: Double) {
    val kcal get() = food.kcal100 * grams / 100
    val protein get() = food.protein100 * grams / 100
    /** Friendly count for foods eaten by the piece. */
    val pieces: String?
        get() = pieceUnits[food.name]?.let { (g, unit) ->
            val n = (grams / g * 2).roundToInt() / 2.0
            if (n <= 0) null else "≈ ${if (n % 1.0 == 0.0) n.toInt().toString() else n.toString()} $unit"
        }

    companion object {
        private val pieceUnits = mapOf(
            "Boiled egg" to (50.0 to "egg(s)"),
            "Roti / chapati" to (40.0 to "roti"),
            "Whole-wheat bread" to (30.0 to "slice(s)"),
            "Banana" to (118.0 to "banana(s)"),
            "Apple" to (182.0 to "apple(s)"),
            "Guava" to (100.0 to "guava(s)"),
            "Dim bhaji (omelette)" to (60.0 to "egg omelette(s)"),
            "Tea with milk & sugar" to (150.0 to "cup(s)"),
            "Whole milk" to (240.0 to "glass(es)"),
            "White rice, cooked" to (160.0 to "cup(s) rice"),
            "Brown rice, cooked" to (160.0 to "cup(s) rice"),
        )
    }
}

data class PlanMeal(val meal: String, val items: List<PlanItem>) {
    val kcal get() = items.sumOf { it.kcal }
    val protein get() = items.sumOf { it.protein }
}

/**
 * Simple, balanced Bangladeshi-friendly meal plans scaled to a calorie target.
 * Each template splits a meal's calories across foods from the offline catalogue.
 */
object MealPlanner {
    private val shares = listOf("Breakfast" to 0.25, "Lunch" to 0.35, "Snack" to 0.10, "Dinner" to 0.30)

    private val templates: Map<String, List<List<Pair<String, Double>>>> = mapOf(
        "Breakfast" to listOf(
            listOf("Roti / chapati" to 0.45, "Mixed vegetable (shobji)" to 0.25, "Boiled egg" to 0.30),
            listOf("Oatmeal, cooked" to 0.55, "Banana" to 0.25, "Whole milk" to 0.20),
            listOf("Whole-wheat bread" to 0.40, "Dim bhaji (omelette)" to 0.40, "Apple" to 0.20),
            listOf("Chira (flattened rice, dry)" to 0.35, "Plain yogurt" to 0.35, "Banana" to 0.30),
        ),
        "Lunch" to listOf(
            listOf("White rice, cooked" to 0.45, "Rui fish curry" to 0.25, "Masoor dal (thin)" to 0.15, "Shak bhaji (leafy greens)" to 0.15),
            listOf("Brown rice, cooked" to 0.45, "Chicken curry" to 0.30, "Mixed vegetable (shobji)" to 0.15, "Green salad" to 0.10),
            listOf("White rice, cooked" to 0.40, "Mug dal" to 0.20, "Pabda fish curry" to 0.25, "Begun bhorta" to 0.15),
            listOf("Roti / chapati" to 0.40, "Chola (chickpea curry)" to 0.30, "Green salad" to 0.10, "Plain yogurt" to 0.20),
        ),
        "Snack" to listOf(
            listOf("Guava" to 0.50, "Almonds" to 0.50),
            listOf("Plain yogurt" to 0.60, "Papaya" to 0.40),
            listOf("Muri (puffed rice)" to 0.50, "Chola (chickpea curry)" to 0.50),
            listOf("Tea with milk & sugar" to 0.40, "Peanuts" to 0.60),
        ),
        "Dinner" to listOf(
            listOf("Roti / chapati" to 0.40, "Chicken curry" to 0.35, "Mixed vegetable (shobji)" to 0.25),
            listOf("White rice, cooked" to 0.35, "Koi fish curry" to 0.30, "Masoor dal (thin)" to 0.15, "Shak bhaji (leafy greens)" to 0.20),
            listOf("Grilled chicken (deshi)" to 0.45, "Green salad" to 0.15, "Roti / chapati" to 0.40),
            listOf("Bhuna khichuri" to 0.65, "Egg curry (dim bhuna)" to 0.35),
        ),
    )

    /** dayIndex 0..6 rotates the templates so each day of the week is different. */
    fun day(dayIndex: Int, calories: Int): List<PlanMeal> =
        shares.mapIndexed { i, (meal, share) ->
            val options = templates.getValue(meal)
            val template = options[(dayIndex + i) % options.size]
            PlanMeal(meal, template.mapNotNull { (name, frac) ->
                FoodCatalog.byName(name)?.let { f ->
                    val grams = ((calories * share * frac / f.kcal100 * 100) / 10).roundToInt().coerceAtLeast(1) * 10.0
                    PlanItem(f, grams)
                }
            })
        }
}

package com.vitapulse.app.food

/** Offline nutrition entry: values per 100 g, typical serving in grams. Approximate. */
internal data class LocalFood(
    val name: String,
    val kcal: Double,
    val p: Double,
    val c: Double,
    val f: Double,
    val serving: Double,
    val cat: String,
    val keys: List<String>,
    val source: String = "VitaPulse estimate",
)

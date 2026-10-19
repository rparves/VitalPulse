package com.vitapulse.app.ui

import java.time.LocalTime
import java.util.Locale

fun fmt(n: Long): String = String.format(Locale.getDefault(), "%,d", n)
fun fmt(n: Int): String = fmt(n.toLong())
fun fmt1(d: Double): String = String.format(Locale.getDefault(), "%.1f", d)

val MEALS = listOf("Breakfast", "Lunch", "Dinner", "Snack")

fun defaultMeal(): String {
    val h = LocalTime.now().hour
    return when {
        h < 11 -> "Breakfast"
        h < 16 -> "Lunch"
        h < 18 -> "Snack"
        h < 22 -> "Dinner"
        else -> "Snack"
    }
}

fun hm(minutes: Long): String = "${minutes / 60}h ${minutes % 60}m"

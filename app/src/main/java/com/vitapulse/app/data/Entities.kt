package com.vitapulse.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val sex: String = "male",              // "male" | "female" (used for BMR formula)
    val birthYear: Int = 1995,
    val heightCm: Double = 170.0,
    val weightKg: Double = 70.0,
    val activityLevel: Int = 2,            // 1 sedentary … 5 athlete
    val goalType: String = "maintain",     // lose | maintain | gain
    val stepGoal: Int = 10000,
    val calorieGoal: Int = 2200,
    val waterGoalMl: Int = 2500,
    val activeMinutesGoal: Int = 30,
    val sleepGoalMin: Int = 480,
    val targetWeightKg: Double = 70.0,
    val remindersEnabled: Boolean = true,
    val biometricLock: Boolean = false,
    val secureScreen: Boolean = false,
    val onboarded: Boolean = false,
)

@Entity(tableName = "food_log")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val timestamp: Long,
    val meal: String,
    val name: String,
    val grams: Double,
    val kcal: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val source: String,
)

@Entity(tableName = "water_log")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val timestamp: Long,
    val ml: Int,
)

@Entity(tableName = "weight_log")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val timestamp: Long,
    val kg: Double,
)

@Entity(tableName = "workout_log")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val timestamp: Long,
    val exerciseId: String,
    val name: String,
    val category: String,
    val sets: Int,          // 0 for timed exercises
    val reps: Int,
    val weightKg: Double,   // 0 = bodyweight
    val durationMin: Int,
    val kcal: Double,       // MET-based estimate
)

data class DayTotal(val day: Long, val total: Double)

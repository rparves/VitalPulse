package com.vitapulse.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VitaDao {
    @Query("SELECT * FROM profile WHERE id = 1") fun profileFlow(): Flow<Profile?>
    @Query("SELECT * FROM profile WHERE id = 1") suspend fun profile(): Profile?
    @Upsert suspend fun saveProfile(p: Profile)

    @Insert suspend fun insertFood(e: FoodEntry)
    @Delete suspend fun deleteFood(e: FoodEntry)
    @Query("SELECT * FROM food_log WHERE epochDay = :day ORDER BY timestamp")
    fun foodForDay(day: Long): Flow<List<FoodEntry>>
    @Query("SELECT COALESCE(SUM(kcal), 0) FROM food_log WHERE epochDay = :day")
    suspend fun kcalForDay(day: Long): Double
    @Query("SELECT epochDay AS day, SUM(kcal) AS total FROM food_log WHERE epochDay >= :from GROUP BY epochDay")
    fun kcalByDay(from: Long): Flow<List<DayTotal>>

    @Insert suspend fun insertWater(e: WaterEntry)
    @Query("SELECT COALESCE(SUM(ml), 0) FROM water_log WHERE epochDay = :day")
    fun waterForDay(day: Long): Flow<Int>
    @Query("SELECT COALESCE(SUM(ml), 0) FROM water_log WHERE epochDay = :day")
    suspend fun waterForDayOnce(day: Long): Int

    @Insert suspend fun insertWorkout(w: WorkoutLog)
    @Delete suspend fun deleteWorkout(w: WorkoutLog)
    @Query("SELECT * FROM workout_log WHERE epochDay = :day ORDER BY timestamp")
    fun workoutsForDay(day: Long): Flow<List<WorkoutLog>>
    @Query("SELECT COALESCE(SUM(durationMin), 0) FROM workout_log WHERE epochDay = :day")
    suspend fun workoutMinutesForDay(day: Long): Int
    @Query("SELECT epochDay AS day, SUM(durationMin) AS total FROM workout_log WHERE epochDay >= :from GROUP BY epochDay")
    fun workoutMinutesByDay(from: Long): Flow<List<DayTotal>>

    /** Every day on which anything was logged (food, water or a workout), newest first. */
    @Query("SELECT epochDay FROM food_log UNION SELECT epochDay FROM water_log UNION SELECT epochDay FROM workout_log ORDER BY epochDay DESC")
    fun loggedDays(): Flow<List<Long>>

    @Insert suspend fun insertWeight(e: WeightEntry)
    @Query("SELECT * FROM weight_log ORDER BY timestamp DESC LIMIT :n")
    fun recentWeights(n: Int): Flow<List<WeightEntry>>
}

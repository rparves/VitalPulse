package com.vitapulse.app.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregateMetric
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import kotlin.reflect.KClass

data class DaySummary(
    val date: LocalDate = LocalDate.now(),
    val steps: Long = 0,
    val activeKcal: Double = 0.0,
    val activeMinutes: Long = 0,
    val avgHeartRate: Long? = null,
    val sleepMinutes: Long = 0,
)

/** Reads activity, heart, sleep data from Health Connect (Fitbit, Samsung Health, Google Fit, watches…). */
class HealthRepo(private val context: Context) {

    companion object {
        const val BACKGROUND_READ = "android.permission.health.READ_HEALTH_DATA_IN_BACKGROUND"
        private fun read(k: KClass<out Record>) = HealthPermission.getReadPermission(k)
        val STEPS = read(StepsRecord::class)
        val CORE: Set<String> = setOf(
            STEPS,
            read(ActiveCaloriesBurnedRecord::class),
            read(ExerciseSessionRecord::class),
            read(HeartRateRecord::class),
            read(SleepSessionRecord::class),
            read(WeightRecord::class),
        )
        val ALL: Set<String> = CORE + BACKGROUND_READ
    }

    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    fun isAvailable(): Boolean =
        HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    suspend fun granted(): Set<String> =
        if (!isAvailable()) emptySet()
        else runCatching { client.permissionController.getGrantedPermissions() }.getOrDefault(emptySet())

    suspend fun dailySummary(date: LocalDate): DaySummary {
        if (!isAvailable()) return DaySummary(date)
        val g = granted()
        val zone = ZoneId.systemDefault()
        val range = TimeRangeFilter.between(
            date.atStartOfDay(zone).toInstant(), date.plusDays(1).atStartOfDay(zone).toInstant()
        )
        val metrics = buildSet<AggregateMetric<*>> {
            if (STEPS in g) add(StepsRecord.COUNT_TOTAL)
            if (read(ActiveCaloriesBurnedRecord::class) in g) add(ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL)
            if (read(ExerciseSessionRecord::class) in g) add(ExerciseSessionRecord.EXERCISE_DURATION_TOTAL)
            if (read(HeartRateRecord::class) in g) add(HeartRateRecord.BPM_AVG)
        }
        var s = DaySummary(date)
        if (metrics.isNotEmpty()) {
            val r = client.aggregate(AggregateRequest(metrics, range))
            s = s.copy(
                steps = r[StepsRecord.COUNT_TOTAL] ?: 0L,
                activeKcal = r[ActiveCaloriesBurnedRecord.ACTIVE_CALORIES_TOTAL]?.inKilocalories ?: 0.0,
                activeMinutes = r[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL]?.toMinutes() ?: 0L,
                avgHeartRate = r[HeartRateRecord.BPM_AVG],
            )
        }
        if (read(SleepSessionRecord::class) in g) {
            // "Last night": 6 pm yesterday → 2 pm today
            val night = TimeRangeFilter.between(
                date.minusDays(1).atTime(18, 0).atZone(zone).toInstant(),
                date.atTime(14, 0).atZone(zone).toInstant()
            )
            val r = client.aggregate(AggregateRequest(setOf(SleepSessionRecord.SLEEP_DURATION_TOTAL), night))
            s = s.copy(sleepMinutes = r[SleepSessionRecord.SLEEP_DURATION_TOTAL]?.toMinutes() ?: 0L)
        }
        return s
    }

    suspend fun weeklySteps(end: LocalDate): List<Pair<LocalDate, Long>> {
        if (!isAvailable() || STEPS !in granted()) return emptyList()
        val res = client.aggregateGroupByPeriod(
            AggregateGroupByPeriodRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(
                    end.minusDays(6).atStartOfDay(), end.plusDays(1).atStartOfDay()
                ),
                timeRangeSlicer = Period.ofDays(1),
            )
        )
        val byDay = res.associate { it.startTime.toLocalDate() to (it.result[StepsRecord.COUNT_TOTAL] ?: 0L) }
        return (0..6).map { i -> val d = end.minusDays(6L - i); d to (byDay[d] ?: 0L) }
    }
}

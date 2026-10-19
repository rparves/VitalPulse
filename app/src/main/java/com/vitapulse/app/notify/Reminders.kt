package com.vitapulse.app.notify

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.vitapulse.app.MainActivity
import com.vitapulse.app.R
import com.vitapulse.app.data.AppDatabase
import com.vitapulse.app.health.HealthRepo
import com.vitapulse.app.ui.fmt
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object Reminders {
    const val CHANNEL = "goals"

    fun createChannel(ctx: Context) {
        val ch = NotificationChannel(CHANNEL, "Goals & reminders", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Progress updates and nudges toward your daily targets"
            lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    fun schedule(ctx: Context) {
        val req = PeriodicWorkRequestBuilder<GoalWorker>(2, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(ctx)
            .enqueueUniquePeriodicWork("goal_reminders", ExistingPeriodicWorkPolicy.KEEP, req)
    }

    @SuppressLint("MissingPermission")
    fun post(ctx: Context, id: Int, title: String, text: String) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return
        val pi = PendingIntent.getActivity(
            ctx, 0,
            Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // On the lock screen only a generic message is shown — health details stay private.
        val publicVersion = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle("VitaPulse")
            .setContentText("You have a new progress update")
            .build()
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_notify)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(publicVersion)
            .build()
        NotificationManagerCompat.from(ctx).notify(id, n)
    }
}

class GoalWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val dao = AppDatabase.get(ctx).dao()
        val p = dao.profile() ?: return Result.success()
        if (!p.remindersEnabled || !p.onboarded) return Result.success()

        val hour = LocalTime.now().hour
        if (hour < 8 || hour >= 22) return Result.success()   // quiet hours

        val today = LocalDate.now()
        val epochDay = today.toEpochDay()
        val water = dao.waterForDayOnce(epochDay)
        val eaten = dao.kcalForDay(epochDay)
        val summary = runCatching { HealthRepo(ctx).dailySummary(today) }.getOrNull()
        val steps = summary?.steps ?: 0L
        val prefs = ctx.getSharedPreferences("vp_reminders", Context.MODE_PRIVATE)
        val dayFraction = ((hour - 8) / 14.0).coerceIn(0.0, 1.0)

        when {
            summary != null && steps >= p.stepGoal && prefs.getLong("celebrated", -1) != epochDay -> {
                prefs.edit().putLong("celebrated", epochDay).apply()
                Reminders.post(ctx, 1, "Step goal reached 🎉", "${fmt(steps)} steps today. Great work — keep the streak alive tomorrow.")
            }
            water < p.waterGoalMl * dayFraction * 0.6 && prefs.getInt("water_nudge_hour", -1) != hour -> {
                prefs.edit().putInt("water_nudge_hour", hour).apply()
                Reminders.post(ctx, 2, "Time for some water 💧", "You're at ${water} of ${p.waterGoalMl} ml today. A glass now keeps you on track.")
            }
            summary != null && hour >= 17 && steps < p.stepGoal * 0.6 -> {
                Reminders.post(ctx, 3, "${fmt(p.stepGoal - steps)} steps to go", "A 15-minute walk adds roughly 1,500–2,000 steps.")
            }
            hour >= 20 && prefs.getLong("recap", -1) != epochDay -> {
                prefs.edit().putLong("recap", epochDay).apply()
                val stepsTxt = if (summary != null) "${fmt(steps)} steps · " else ""
                Reminders.post(ctx, 4, "Today's recap", "$stepsTxt${eaten.roundToInt()} kcal logged · ${water} ml water")
            }
        }
        return Result.success()
    }
}

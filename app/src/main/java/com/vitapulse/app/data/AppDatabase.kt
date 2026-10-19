package com.vitapulse.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vitapulse.app.security.DbKeyManager
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/** All personal data lives in this SQLCipher (AES-256) encrypted database. */
@Database(
    entities = [Profile::class, FoodEntry::class, WaterEntry::class, WeightEntry::class, WorkoutLog::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): VitaDao

    companion object {
        /** v1 → v2: adds workout logging without touching existing data. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `workout_log` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `epochDay` INTEGER NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, `exerciseId` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                        "`category` TEXT NOT NULL, `sets` INTEGER NOT NULL, `reps` INTEGER NOT NULL, " +
                        "`weightKg` REAL NOT NULL, `durationMin` INTEGER NOT NULL, `kcal` REAL NOT NULL)"
                )
            }
        }

        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(ctx: Context): AppDatabase {
            System.loadLibrary("sqlcipher")
            val factory = SupportOpenHelperFactory(DbKeyManager.getOrCreatePassphrase(ctx))
            return Room.databaseBuilder(ctx, AppDatabase::class.java, "vitapulse.db")
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}

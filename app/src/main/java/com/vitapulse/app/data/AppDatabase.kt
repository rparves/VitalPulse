package com.vitapulse.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vitapulse.app.security.DbKeyManager
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

/** All personal data lives in this SQLCipher (AES-256) encrypted database. */
@Database(
    entities = [Profile::class, FoodEntry::class, WaterEntry::class, WeightEntry::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): VitaDao

    companion object {
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
                .build()
        }
    }
}

package com.example.medicalcalculatorapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.medicalcalculatorapp.data.db.converters.DateConverter
import com.example.medicalcalculatorapp.data.db.converters.FieldTypeConverter
import com.example.medicalcalculatorapp.data.db.converters.MapConverter
import com.example.medicalcalculatorapp.data.db.converters.StringListConverter
import com.example.medicalcalculatorapp.data.db.dao.CalculatorDao
import com.example.medicalcalculatorapp.data.db.dao.CategoryDao
import com.example.medicalcalculatorapp.data.db.dao.FavoriteDao
import com.example.medicalcalculatorapp.data.db.dao.FieldDao
import com.example.medicalcalculatorapp.data.db.dao.HistoryDao
import com.example.medicalcalculatorapp.data.db.dao.UserProfileDao
import com.example.medicalcalculatorapp.data.db.dao.UserSettingsDao
import com.example.medicalcalculatorapp.data.db.entity.CalculatorEntity
import com.example.medicalcalculatorapp.data.db.entity.CategoryEntity
import com.example.medicalcalculatorapp.data.db.entity.FavoriteEntity
import com.example.medicalcalculatorapp.data.db.entity.FieldEntity
import com.example.medicalcalculatorapp.data.db.entity.HistoryEntity
import com.example.medicalcalculatorapp.data.db.entity.UserProfileEntity
import com.example.medicalcalculatorapp.data.db.entity.UserSettingsEntity
import com.example.medicalcalculatorapp.data.db.util.DatabasePrepopulateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CalculatorEntity::class,
        FieldEntity::class,
        CategoryEntity::class,
        FavoriteEntity::class,
        HistoryEntity::class,
        UserProfileEntity::class,
        UserSettingsEntity::class
    ],
    version = 2, // Incremented for new user tables
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    FieldTypeConverter::class,
    StringListConverter::class,
    MapConverter::class
)
abstract class MedicalCalculatorDatabase : RoomDatabase() {
    // Existing DAOs
    abstract fun calculatorDao(): CalculatorDao
    abstract fun fieldDao(): FieldDao
    abstract fun categoryDao(): CategoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao

    // New User DAOs
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: MedicalCalculatorDatabase? = null
        private const val DATABASE_NAME = "medical_calculator_db"

        // Migration from version 1 to 2 (adding user tables)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create user_profiles table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_profiles (
                        id TEXT NOT NULL PRIMARY KEY,
                        email TEXT NOT NULL,
                        fullName TEXT,
                        profession TEXT,
                        specialization TEXT,
                        institution TEXT,
                        licenseNumber TEXT,
                        country TEXT,
                        language TEXT NOT NULL DEFAULT 'es',
                        profileImageUrl TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create user_settings table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_settings (
                        userId TEXT NOT NULL PRIMARY KEY,
                        theme TEXT NOT NULL DEFAULT 'SYSTEM',
                        language TEXT NOT NULL DEFAULT 'es',
                        notificationsEnabled INTEGER NOT NULL DEFAULT 1,
                        biometricAuthEnabled INTEGER NOT NULL DEFAULT 0,
                        autoSaveCalculations INTEGER NOT NULL DEFAULT 1,
                        defaultUnits TEXT NOT NULL DEFAULT 'METRIC',
                        privacySettingsJson TEXT NOT NULL DEFAULT '{}',
                        calculatorPreferencesJson TEXT NOT NULL DEFAULT '{}',
                        lastSyncTime INTEGER,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): MedicalCalculatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalCalculatorDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2) // Add migration instead of destructive
                    // Keep fallback for development only - remove for production
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
//package com.example.medicalcalculatorapp.data.db
//
//import android.content.Context
//import androidx.room.Database
//import androidx.room.Room
//import androidx.room.RoomDatabase
//import androidx.room.TypeConverters
//import androidx.sqlite.db.SupportSQLiteDatabase
//import com.example.medicalcalculatorapp.data.db.converters.DateConverter
//import com.example.medicalcalculatorapp.data.db.converters.FieldTypeConverter
//import com.example.medicalcalculatorapp.data.db.converters.MapConverter
//import com.example.medicalcalculatorapp.data.db.converters.StringListConverter
//import com.example.medicalcalculatorapp.data.db.dao.CalculatorDao
//import com.example.medicalcalculatorapp.data.db.dao.CategoryDao
//import com.example.medicalcalculatorapp.data.db.dao.FavoriteDao
//import com.example.medicalcalculatorapp.data.db.dao.FieldDao
//import com.example.medicalcalculatorapp.data.db.dao.HistoryDao
//import com.example.medicalcalculatorapp.data.db.entity.CalculatorEntity
//import com.example.medicalcalculatorapp.data.db.entity.CategoryEntity
//import com.example.medicalcalculatorapp.data.db.entity.FavoriteEntity
//import com.example.medicalcalculatorapp.data.db.entity.FieldEntity
//import com.example.medicalcalculatorapp.data.db.entity.HistoryEntity
//import com.example.medicalcalculatorapp.data.db.util.DatabasePrepopulateUtil
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//
//@Database(
//    entities = [
//        CalculatorEntity::class,
//        FieldEntity::class,
//        CategoryEntity::class,
//        FavoriteEntity::class,
//        HistoryEntity::class
//    ],
//    version = 1,
//    exportSchema = true // Good practice for tracking schema changes
//)
//@TypeConverters(
//    DateConverter::class,
//    FieldTypeConverter::class,
//    StringListConverter::class,
//    MapConverter::class
//)
//abstract class MedicalCalculatorDatabase : RoomDatabase() {
//    abstract fun calculatorDao(): CalculatorDao
//    abstract fun fieldDao(): FieldDao
//    abstract fun categoryDao(): CategoryDao
//    abstract fun favoriteDao(): FavoriteDao
//    abstract fun historyDao(): HistoryDao
//
//    companion object {
//        @Volatile
//        private var INSTANCE: MedicalCalculatorDatabase? = null
//        private const val DATABASE_NAME = "medical_calculator_db"
//
//        fun getDatabase(context: Context): MedicalCalculatorDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    MedicalCalculatorDatabase::class.java,
//                    DATABASE_NAME
//                )
//                    .fallbackToDestructiveMigration() // For development - replace with proper migrations in production
//                    .addCallback(object : RoomDatabase.Callback() {
//                        override fun onCreate(db: SupportSQLiteDatabase) {
//                            super.onCreate(db)
//                        }
//                    })
//                    .build()
//
//                INSTANCE = instance
//                instance
//            }
//        }
//    }
//}
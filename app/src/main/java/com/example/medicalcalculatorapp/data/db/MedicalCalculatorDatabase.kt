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
import com.example.medicalcalculatorapp.data.db.dao.UserComplianceDao
import com.example.medicalcalculatorapp.data.db.entity.UserComplianceEntity

@Database(
    entities = [
        CalculatorEntity::class,
        FieldEntity::class,
        CategoryEntity::class,
        FavoriteEntity::class,
        HistoryEntity::class,
        UserProfileEntity::class,
        UserSettingsEntity::class,
        UserComplianceEntity::class  // ✅ ADDED: Include compliance entity
    ],
    version = 3, // ✅ FIXED: Updated to version 3 for compliance table
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

    // User DAOs
    abstract fun userProfileDao(): UserProfileDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun userComplianceDao(): UserComplianceDao  // ✅ ADDED: Compliance DAO

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

        // ✅ NEW: Migration from version 2 to 3 (adding compliance table)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create user_compliance table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS user_compliance (
                        userId TEXT NOT NULL PRIMARY KEY,
                        hasAcceptedBasicTerms INTEGER NOT NULL DEFAULT 0,
                        basicTermsAcceptedAt INTEGER,
                        basicTermsVersion TEXT,
                        hasAcceptedMedicalDisclaimer INTEGER NOT NULL DEFAULT 0,
                        medicalDisclaimerAcceptedAt INTEGER,
                        medicalDisclaimerVersion TEXT,
                        isProfessionalVerified INTEGER NOT NULL DEFAULT 0,
                        professionalVerifiedAt INTEGER,
                        professionalType TEXT,
                        professionalLicenseInfo TEXT,
                        hasAcceptedPrivacyPolicy INTEGER NOT NULL DEFAULT 0,
                        privacyPolicyAcceptedAt INTEGER,
                        privacyPolicyVersion TEXT,
                        complianceVersion TEXT NOT NULL DEFAULT '2024.1',
                        lastUpdated INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        ipAddress TEXT,
                        userAgent TEXT,
                        consentMethod TEXT NOT NULL DEFAULT 'APP_DIALOG',
                        isCompliant INTEGER NOT NULL DEFAULT 0,
                        needsReview INTEGER NOT NULL DEFAULT 0,
                        complianceNotes TEXT
                    )
                """.trimIndent())

                // Create indices for better query performance
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_user_compliance_userId ON user_compliance(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_compliance_complianceVersion ON user_compliance(complianceVersion)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_user_compliance_lastUpdated ON user_compliance(lastUpdated)")
            }
        }

        fun getDatabase(context: Context): MedicalCalculatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalCalculatorDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // ✅ FIXED: Add both migrations
                    // ✅ REMOVE THIS FOR PRODUCTION - Only for development
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            println("🔥 Database created with version 3")
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
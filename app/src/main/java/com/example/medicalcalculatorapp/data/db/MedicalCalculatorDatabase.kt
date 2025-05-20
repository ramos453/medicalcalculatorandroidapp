package com.example.medicalcalculatorapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.example.medicalcalculatorapp.data.db.entity.CalculatorEntity
import com.example.medicalcalculatorapp.data.db.entity.CategoryEntity
import com.example.medicalcalculatorapp.data.db.entity.FavoriteEntity
import com.example.medicalcalculatorapp.data.db.entity.FieldEntity
import com.example.medicalcalculatorapp.data.db.entity.HistoryEntity
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
        HistoryEntity::class
    ],
    version = 1,
    exportSchema = true // Good practice for tracking schema changes
)
@TypeConverters(
    DateConverter::class,
    FieldTypeConverter::class,
    StringListConverter::class,
    MapConverter::class
)
abstract class MedicalCalculatorDatabase : RoomDatabase() {
    abstract fun calculatorDao(): CalculatorDao
    abstract fun fieldDao(): FieldDao
    abstract fun categoryDao(): CategoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: MedicalCalculatorDatabase? = null
        private const val DATABASE_NAME = "medical_calculator_db"

        fun getDatabase(context: Context): MedicalCalculatorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicalCalculatorDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - replace with proper migrations in production
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate database when created
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                // Prepopulate database here - we'll implement this in Checkpoint 6
                                DatabasePrepopulateUtil.prepopulateDatabase(context, database)
                            }
                        }
                    })
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
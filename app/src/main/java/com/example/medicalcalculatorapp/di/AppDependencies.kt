package com.example.medicalcalculatorapp.di

import android.content.Context
import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.mapper.CalculatorMapper
import com.example.medicalcalculatorapp.data.db.mapper.CategoryMapper
import com.example.medicalcalculatorapp.data.db.mapper.HistoryMapper
import com.example.medicalcalculatorapp.data.repository.CalculatorRepository
import com.example.medicalcalculatorapp.data.repository.CategoryRepository
import com.example.medicalcalculatorapp.data.repository.HistoryRepository
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.repository.ICalculatorRepository
import com.example.medicalcalculatorapp.domain.repository.ICategoryRepository
import com.example.medicalcalculatorapp.domain.repository.IHistoryRepository
import com.google.gson.Gson
import com.example.medicalcalculatorapp.domain.service.CalculatorService

// Import your domain calculators here
import com.example.medicalcalculatorapp.domain.calculator.impl.MedicationDosageCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.HeparinDosageCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.UnitConverterCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.IVDripRateCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.FluidBalanceCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.ElectrolyteManagementCalculator


/**
 * A simple dependency provider for the application.
 * In a larger app, you might want to use a proper DI framework like Hilt or Koin.
 */
object AppDependencies {

    private var database: MedicalCalculatorDatabase? = null
    private var calculatorRepository: ICalculatorRepository? = null
    private var categoryRepository: ICategoryRepository? = null
    private var historyRepository: IHistoryRepository? = null
    private var userManager: UserManager? = null
    private val gson = Gson()
    private var calculatorService: CalculatorService? = null

    fun provideDatabase(context: Context): MedicalCalculatorDatabase {
        return database ?: synchronized(this) {
            database ?: MedicalCalculatorDatabase.getDatabase(context).also {
                database = it
            }
        }
    }

    fun provideUserManager(context: Context): UserManager {
        return userManager ?: synchronized(this) {
            userManager ?: UserManager(context).also {
                userManager = it
            }
        }
    }

    fun provideCalculatorRepository(context: Context): ICalculatorRepository {
        return calculatorRepository ?: synchronized(this) {
            calculatorRepository ?: createCalculatorRepository(context).also {
                calculatorRepository = it
            }
        }
    }

    fun provideCategoryRepository(context: Context): ICategoryRepository {
        return categoryRepository ?: synchronized(this) {
            categoryRepository ?: createCategoryRepository(context).also {
                categoryRepository = it
            }
        }
    }

    fun provideCalculatorService(): CalculatorService {
        return calculatorService ?: synchronized(this) {
            calculatorService ?: createCalculatorService().also {
                calculatorService = it
            }
        }
    }

    private fun createCalculatorService(): CalculatorService {
        val service = CalculatorService()

        // Register all calculators
        service.registerCalculator(MedicationDosageCalculator())
        service.registerCalculator(HeparinDosageCalculator())
        service.registerCalculator(UnitConverterCalculator())
        service.registerCalculator(IVDripRateCalculator())
        service.registerCalculator(FluidBalanceCalculator())
        service.registerCalculator(ElectrolyteManagementCalculator())
        // Future calculators will be registered here:
        // service.registerCalculator(BMICalculator())
        // service.registerCalculator(MAPCalculator())

        return service
    }

    fun provideHistoryRepository(context: Context): IHistoryRepository {
        return historyRepository ?: synchronized(this) {
            historyRepository ?: createHistoryRepository(context).also {
                historyRepository = it
            }
        }
    }

    private fun createCalculatorRepository(context: Context): CalculatorRepository {
        val database = provideDatabase(context)
        val userManager = provideUserManager(context)
        val calculatorMapper = CalculatorMapper(gson)
        return CalculatorRepository(database, calculatorMapper, userManager)
    }

    private fun createCategoryRepository(context: Context): CategoryRepository {
        val database = provideDatabase(context)
        val categoryMapper = CategoryMapper()
        return CategoryRepository(database, categoryMapper)
    }

    private fun createHistoryRepository(context: Context): HistoryRepository {
        val database = provideDatabase(context)
        val userManager = provideUserManager(context)
        val historyMapper = HistoryMapper(gson)
        return HistoryRepository(database, historyMapper, userManager)
    }
}
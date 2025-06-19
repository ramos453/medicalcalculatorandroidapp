package com.example.medicalcalculatorapp.di

import android.content.Context
import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.mapper.CalculatorMapper
import com.example.medicalcalculatorapp.data.db.mapper.CategoryMapper
import com.example.medicalcalculatorapp.data.db.mapper.HistoryMapper
import com.example.medicalcalculatorapp.data.db.mapper.UserProfileMapper
import com.example.medicalcalculatorapp.data.db.mapper.UserSettingsMapper
import com.example.medicalcalculatorapp.data.db.mapper.UserComplianceMapper
import com.example.medicalcalculatorapp.data.repository.CalculatorRepository
import com.example.medicalcalculatorapp.data.repository.CategoryRepository
import com.example.medicalcalculatorapp.data.repository.HistoryRepository
import com.example.medicalcalculatorapp.data.repository.UserRepository
import com.example.medicalcalculatorapp.data.repository.UserComplianceRepository
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.calculator.impl.ApgarScoreCalculator
import com.example.medicalcalculatorapp.domain.repository.ICalculatorRepository
import com.example.medicalcalculatorapp.domain.repository.ICategoryRepository
import com.example.medicalcalculatorapp.domain.repository.IHistoryRepository
import com.example.medicalcalculatorapp.domain.repository.IUserRepository
import com.example.medicalcalculatorapp.domain.repository.IUserComplianceRepository
import com.example.medicalcalculatorapp.domain.service.CalculatorService
import com.google.gson.Gson
import com.example.medicalcalculatorapp.domain.calculator.impl.BMICalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.BradenScaleCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.ElectrolyteManagementCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.GlasgowComaScaleCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.MedicationDosageCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.PediatricDosageCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.FluidBalanceCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.IVDripRateCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.MinuteVentilationCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.UnitConverterCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.HeparinDosageCalculator
import com.example.medicalcalculatorapp.domain.calculator.impl.MAPCalculator
import com.example.medicalcalculatorapp.presentation.theme.MedicalCalculatorAppTheme


object AppDependencies {

    private var database: MedicalCalculatorDatabase? = null
    private var calculatorRepository: ICalculatorRepository? = null
    private var categoryRepository: ICategoryRepository? = null
    private var historyRepository: IHistoryRepository? = null
    private var userRepository: IUserRepository? = null
    private var userComplianceRepository: IUserComplianceRepository? = null
    private var userManager: UserManager? = null
    private var calculatorService: CalculatorService? = null
    private var userComplianceMapper: UserComplianceMapper? = null
    private val gson = Gson()

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

    fun provideUserComplianceMapper(): UserComplianceMapper {
        return userComplianceMapper ?: synchronized(this) {
            userComplianceMapper ?: UserComplianceMapper().also {
                userComplianceMapper = it
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

    fun provideHistoryRepository(context: Context): IHistoryRepository {
        return historyRepository ?: synchronized(this) {
            historyRepository ?: createHistoryRepository(context).also {
                historyRepository = it
            }
        }
    }

    fun provideUserRepository(context: Context): IUserRepository {
        return userRepository ?: synchronized(this) {
            userRepository ?: createUserRepository(context).also {
                userRepository = it
            }
        }
    }

    // NEW: UserComplianceRepository provision
    fun provideUserComplianceRepository(context: Context): IUserComplianceRepository {
        return userComplianceRepository ?: synchronized(this) {
            userComplianceRepository ?: createUserComplianceRepository(context).also {
                userComplianceRepository = it
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

    private fun createCalculatorRepository(context: Context): CalculatorRepository {
        val database = provideDatabase(context)
        val userManager = provideUserManager(context)
        val calculatorMapper = CalculatorMapper(gson) // ✅ Pass gson parameter
        return CalculatorRepository(database, calculatorMapper, userManager)
    }

    private fun createCategoryRepository(context: Context): CategoryRepository {
        val database = provideDatabase(context)
        val categoryMapper = CategoryMapper() // ✅ No parameters needed
        return CategoryRepository(database, categoryMapper)
    }

    private fun createHistoryRepository(context: Context): HistoryRepository {
        val database = provideDatabase(context)
        val userManager = provideUserManager(context)
        val historyMapper = HistoryMapper(gson) // ✅ Pass gson parameter
        return HistoryRepository(database, historyMapper, userManager)
    }

    private fun createUserRepository(context: Context): UserRepository {
        val database = provideDatabase(context)
        val userProfileMapper = UserProfileMapper() // ✅ No parameters needed
        val userSettingsMapper = UserSettingsMapper(gson) // ✅ Pass gson parameter
        return UserRepository(database, userProfileMapper, userSettingsMapper)
    }

    // NEW: UserComplianceRepository creation
    private fun createUserComplianceRepository(context: Context): UserComplianceRepository {
        val database = provideDatabase(context)
        val mapper = provideUserComplianceMapper()
        return UserComplianceRepository(database, mapper)
    }

    private fun createCalculatorService(): CalculatorService {
        val service = CalculatorService()
        try {
            service.registerCalculator(BMICalculator())
            service.registerCalculator(ApgarScoreCalculator())
            service.registerCalculator(BradenScaleCalculator())
            service.registerCalculator(GlasgowComaScaleCalculator())
            service.registerCalculator(MedicationDosageCalculator())
            service.registerCalculator(PediatricDosageCalculator())
            service.registerCalculator(FluidBalanceCalculator())
            service.registerCalculator(HeparinDosageCalculator())
            service.registerCalculator(MAPCalculator())
            service.registerCalculator(ElectrolyteManagementCalculator())
            service.registerCalculator(UnitConverterCalculator())
            service.registerCalculator(MinuteVentilationCalculator())
            service.registerCalculator(IVDripRateCalculator())


        } catch (e: Exception) {
            println("⚠️ Some calculators not available: ${e.message}")
        }
        return service
    }
}
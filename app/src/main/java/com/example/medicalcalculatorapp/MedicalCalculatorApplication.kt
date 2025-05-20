package com.example.medicalcalculatorapp

import android.app.Application
import com.example.medicalcalculatorapp.data.db.util.DatabasePrepopulateUtil
import com.example.medicalcalculatorapp.di.AppDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MedicalCalculatorApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Initialize database and prepopulate if needed
        applicationScope.launch {
            val database = AppDependencies.provideDatabase(applicationContext)
            // Prepopulate the database with initial data
            DatabasePrepopulateUtil.prepopulateDatabase(applicationContext, database)
        }
    }
}
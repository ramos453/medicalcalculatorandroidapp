package com.example.medicalcalculatorapp

import android.app.Application
import com.example.medicalcalculatorapp.data.db.util.DatabasePrepopulateUtil
import com.example.medicalcalculatorapp.di.AppDependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import com.google.firebase.FirebaseApp  // ← NEW IMPORT

class MedicalCalculatorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)  // ← NEW LINE

        // Don't block startup - populate in background after delay
        GlobalScope.launch(Dispatchers.IO) {
            delay(2000) // Wait 2 seconds after app starts
            try {
                val database = AppDependencies.provideDatabase(applicationContext)
                DatabasePrepopulateUtil.prepopulateIfNeeded(applicationContext, database)
            } catch (e: Exception) {
                println("❌ Background database population failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}


//package com.example.medicalcalculatorapp
//
//import android.app.Application
//import com.example.medicalcalculatorapp.data.db.util.DatabasePrepopulateUtil
//import com.example.medicalcalculatorapp.di.AppDependencies
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.SupervisorJob
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.GlobalScope  // Add this import
//import kotlinx.coroutines.delay        // Add this import
//
//class MedicalCalculatorApplication : Application() {
//
//    // Remove this - we don't need applicationScope anymore
//    // private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//
//    override fun onCreate() {
//        super.onCreate()
//
//        // Don't block startup - populate in background after delay
//        GlobalScope.launch(Dispatchers.IO) {
//            delay(2000) // Wait 2 seconds after app starts
//            try {
//                val database = AppDependencies.provideDatabase(applicationContext)
//                DatabasePrepopulateUtil.prepopulateIfNeeded(applicationContext, database)
//            } catch (e: Exception) {
//                println("❌ Background database population failed: ${e.message}")
//                e.printStackTrace()
//            }
//        }
//    }
//}
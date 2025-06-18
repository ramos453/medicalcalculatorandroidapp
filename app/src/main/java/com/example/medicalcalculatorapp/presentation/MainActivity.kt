package com.example.medicalcalculatorapp.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.ActivityMainBinding
import com.example.medicalcalculatorapp.data.auth.FirebaseAuthService
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var firebaseAuthService: FirebaseAuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth Service
        firebaseAuthService = FirebaseAuthService()

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Set up top-level destinations (no back button)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.splashFragment, R.id.loginFragment, R.id.calculatorListFragment)
        )

        setupActionBarWithNavController(navController, appBarConfiguration)

        // Hide/show toolbar based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment -> binding.toolbar.visibility = android.view.View.GONE
                else -> binding.toolbar.visibility = android.view.View.VISIBLE
            }
        }

        // 🆕 HANDLE AUTHENTICATION LINKS
        handleAuthenticationIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle new intents when app is already running
        intent?.let {
            setIntent(it)
            handleAuthenticationIntent(it)
        }
    }

    // 🆕 AUTHENTICATION LINK HANDLER
    private fun handleAuthenticationIntent(intent: Intent?) {
        val data: Uri? = intent?.data

        if (data != null && data.host == "medicalcalculatorapp-39631.web.app") {
            println("🔗 Authentication link detected: $data")

            when {
                // Email verification link
                data.path?.contains("/verify") == true -> {
                    handleEmailVerificationLink(data)
                }

                // Password reset link
                data.path?.contains("/reset-password") == true -> {
                    handlePasswordResetLink(data)
                }

                // General auth link
                data.path?.contains("/auth") == true -> {
                    handleGeneralAuthLink(data)
                }

                else -> {
                    println("🔗 Unhandled auth link: $data")
                    // Navigate to main app anyway
                    navigateToMainApp()
                }
            }
        }
    }

    private fun handleEmailVerificationLink(data: Uri) {
        println("📧 Handling email verification link")

        val mode = data.getQueryParameter("mode")
        val oobCode = data.getQueryParameter("oobCode")
        val continueUrl = data.getQueryParameter("continueUrl")

        if (mode == "verifyEmail" && !oobCode.isNullOrEmpty()) {
            // Apply the email verification code
            FirebaseAuth.getInstance().applyActionCode(oobCode)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("✅ Email verification successful")
                        showVerificationSuccess()
                        navigateToMainApp()
                    } else {
                        println("❌ Email verification failed: ${task.exception?.message}")
                        showVerificationError("Verificación fallida: ${task.exception?.message}")
                        navigateToLogin()
                    }
                }
        } else {
            showVerificationError("Enlace de verificación inválido")
            navigateToLogin()
        }
    }

    private fun handlePasswordResetLink(data: Uri) {
        println("🔑 Handling password reset link")

        val mode = data.getQueryParameter("mode")
        val oobCode = data.getQueryParameter("oobCode")

        if (mode == "resetPassword" && !oobCode.isNullOrEmpty()) {
            // For password reset, we need to navigate to a password reset screen
            // For now, show a message and navigate to login
            showPasswordResetInfo()
            navigateToLogin()
        } else {
            showVerificationError("Enlace de restablecimiento inválido")
            navigateToLogin()
        }
    }

    private fun handleGeneralAuthLink(data: Uri) {
        println("🔗 Handling general auth link")

        // For general auth links, just navigate to the appropriate screen
        if (FirebaseAuth.getInstance().currentUser != null) {
            // User is signed in, go to main app
            navigateToMainApp()
        } else {
            // User needs to sign in
            navigateToLogin()
        }
    }

    private fun showVerificationSuccess() {
        Toast.makeText(
            this,
            "✅ Email verificado correctamente. ¡Bienvenido!",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showVerificationError(message: String) {
        Toast.makeText(
            this,
            "❌ $message",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showPasswordResetInfo() {
        Toast.makeText(
            this,
            "🔑 Enlace de restablecimiento recibido. Inicie sesión para continuar.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateToMainApp() {
        try {
            // Navigate to calculator list (main app screen)
            navController.navigate(R.id.calculatorListFragment)
        } catch (e: Exception) {
            println("❌ Navigation error to main app: ${e.message}")
            // Fallback: restart the app
            finishAffinity()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun navigateToLogin() {
        try {
            // Navigate to login screen
            navController.navigate(R.id.loginFragment)
        } catch (e: Exception) {
            println("❌ Navigation error to login: ${e.message}")
            // The app should naturally go to splash -> login flow
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
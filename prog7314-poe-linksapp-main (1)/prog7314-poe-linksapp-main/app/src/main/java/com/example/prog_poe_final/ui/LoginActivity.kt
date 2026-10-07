package com.example.prog_poe_final.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.prog_poe_final.MainActivity
import com.example.prog_poe_final.R
import com.example.prog_poe_final.api.ApiClient
import com.example.prog_poe_final.databinding.ActivityLoginBinding
import com.example.prog_poe_final.utils.BiometricUtils
import com.example.prog_poe_final.utils.CurrentUserHolder
import com.example.prog_poe_final.utils.OfflineManager
import com.example.prog_poe_final.viewmodel.AuthViewModel
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var biometricButton: ImageView
    private lateinit var binding: ActivityLoginBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        biometricButton = findViewById(R.id.biometricButton)

        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val biometricEnabled = sharedPref.getBoolean("biometricEnabled", false)

        // Show biometric login button if enabled
        if (BiometricUtils.canUseBiometrics(this) && biometricEnabled) {
            biometricButton.visibility = View.VISIBLE
            biometricButton.setOnClickListener {
                BiometricUtils.showBiometricPrompt(
                    this,
                    onSuccess = {
                        Toast.makeText(this, "Biometric Login Successful", Toast.LENGTH_SHORT).show()
                        biometricAutoLogin() // Logs in using saved credentials
                    },
                    onFailure = {
                        Toast.makeText(this, "Biometric Login Failed", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        } else {
            biometricButton.visibility = View.GONE
        }

        // Normal Login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.loginUser(email, password)
        }

        // Observe status messages
        authViewModel.statusMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // Handle successful login
        authViewModel.isSuccessful.observe(this) { success ->
            if (success) {
                authViewModel.currentUser.observe(this) { user ->
                    if (user != null) {
                        CurrentUserHolder.user = user

                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                val userId = user.user_id!!
                                val body = mapOf("userId" to userId, "fcmToken" to token)

                                ApiClient.api.updateFcmToken(body).enqueue(object :
                                    Callback<Map<String, Any>> {
                                    override fun onResponse(
                                        call: Call<Map<String, Any>>,

                                        response: Response<Map<String, Any>>
                                    ) {
                                        if (response.isSuccessful)
                                            Log.d("FCM", "Token saved successfully for user $userId")
                                        else
                                            Log.w("FCM", "Failed to save token: ${response.code()}")
                                    }

                                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                                        Log.e("FCM", "Error uploading token: ${t.message}")
                                    }
                                })
                            } else {
                                Log.e("FCM", "Failed to get FCM token: ${task.exception?.message}")
                            }
                        }

                        val editor = sharedPref.edit()
                        editor.putString("userId", user.user_id.toString())
                        editor.putString("name", user.name)
                        editor.putString("email", user.email)
                        editor.putString("password", binding.etPassword.text.toString()) // Save password for biometrics
                        editor.putBoolean("biometricEnabled", true) // Enable biometric login
                        editor.apply()

                        Toast.makeText(this, "Welcome ${user.name}", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                }
            }
        }

        // Go to Register page
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        showOfflineBanner(!OfflineManager.isOnline(this))
    }

    // Navigate to home activity
    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    // Use saved credentials for biometric re-login
    private fun biometricAutoLogin() {
        val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val email = sharedPref.getString("email", null)
        val password = sharedPref.getString("password", null)

        if (email != null && password != null) {
            authViewModel.loginUser(email, password)
        } else {
            Toast.makeText(this, "No saved credentials for biometric login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOfflineBanner(isOffline: Boolean) {
        val banner = findViewById<TextView>(R.id.offlineBanner)

        if (isOffline) {
            banner.visibility = View.VISIBLE
            banner.animate().translationY(0f).setDuration(250).start()
        } else {
            banner.animate().translationY(-40f).setDuration(250).withEndAction {
                banner.visibility = View.GONE
            }.start()
        }
    }

    override fun onResume() {
        super.onResume()
        showOfflineBanner(!OfflineManager.isOnline(this))
    }
}

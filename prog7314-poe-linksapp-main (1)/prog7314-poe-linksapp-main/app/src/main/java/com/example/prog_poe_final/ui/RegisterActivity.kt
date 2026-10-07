package com.example.prog_poe_final.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.prog_poe_final.api.ApiClient
import com.example.prog_poe_final.databinding.ActivityRegisterBinding
import com.example.prog_poe_final.models.User
import com.example.prog_poe_final.utils.BiometricUtils
import com.example.prog_poe_final.viewmodel.AuthViewModel
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(name = name, email = email, password = password)
            authViewModel.registerUser(newUser)
        }

        // 🔄 Observe registration result
        authViewModel.statusMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        // ✅ When registration is successful
        authViewModel.isSuccessful.observe(this) { success ->
            if (success) {
                authViewModel.currentUser.observe(this) { user ->
                    if (user != null) {
                        // 🔹 Upload FCM token for the newly registered user
                        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val token = task.result
                                val userId = user.user_id!!
                                val body = mapOf("userId" to userId, "fcmToken" to token)

                                ApiClient.api.updateFcmToken(body)
                                    .enqueue(object : Callback<Map<String, Any>> {
                                        override fun onResponse(
                                            call: Call<Map<String, Any>>,
                                            response: Response<Map<String, Any>>
                                        ) {
                                            if (response.isSuccessful)
                                                Log.d("FCM", "Token saved successfully for user $userId")
                                            else
                                                Log.w("FCM", "Failed to save token: ${response.code()}")
                                        }

                                        override fun onFailure(
                                            call: Call<Map<String, Any>>,
                                            t: Throwable
                                        ) {
                                            Log.e("FCM", "Error uploading token: ${t.message}")
                                        }
                                    })
                            } else {
                                Log.e("FCM", "Failed to get FCM token: ${task.exception?.message}")
                            }
                        }

                        // ✅ Ask to enable biometrics if supported
                        if (BiometricUtils.canUseBiometrics(this)) {
                            BiometricUtils.showBiometricPrompt(
                                this,
                                onSuccess = {
                                    val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                    sharedPref.edit().apply {
                                        putBoolean("biometricEnabled", true)
                                        apply()
                                    }
                                    Toast.makeText(
                                        this,
                                        "Biometric login enabled for next time!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navigateToLogin()
                                },
                                onFailure = {
                                    Toast.makeText(
                                        this,
                                        "Biometric setup skipped",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navigateToLogin()
                                }
                            )
                        } else {
                            navigateToLogin()
                        }
                    }
                }
            }
        }

        // 🔁 Go to Login screen
        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}

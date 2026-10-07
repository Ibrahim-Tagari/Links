package com.example.prog_poe_final.repository

import com.example.prog_poe_final.api.ApiClient
import com.example.prog_poe_final.api.ApiService
import com.example.prog_poe_final.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthRepository {

    private val apiService = ApiClient.instance.create(ApiService::class.java)

    fun registerUser(user: User, onResult: (Boolean, String) -> Unit) {
        apiService.register(user).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.isSuccessful) {
                    onResult(true, "User registered successfully")
                } else {
                    onResult(false, "Registration failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                onResult(false, "Error: ${t.message}")
            }
        })
    }

    fun loginUser(
        email: String,
        password: String,
        onResult: (Boolean, User?, String) -> Unit
    ) {
        val credentials = mapOf("email" to email, "password" to password)

        apiService.login(credentials).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                if (response.isSuccessful && response.body() != null) {

                    val userMap = response.body()?.get("user") as Map<*, *>

                    // Handle number safely (Double or Int)
                    val userId = when (val idValue = userMap["id"]) {
                        is Double -> idValue.toInt()
                        is Int -> idValue
                        is Long -> idValue.toInt()
                        else -> null
                    }

                    // Get bio safely
                    val bio = userMap["bio"]?.toString()

                    if (userId == null) {
                        onResult(false, null, "Invalid user data")
                        return
                    }

                    val user = User(
                        user_id = userId,
                        name = userMap["name"] as String,
                        email = userMap["email"] as String,
                        bio = bio
                    )

                    onResult(true, user, "Login successful")
                } else {
                    onResult(false, null, "Login failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                onResult(false, null, "Error: ${t.message}")
            }
        })
    }
}

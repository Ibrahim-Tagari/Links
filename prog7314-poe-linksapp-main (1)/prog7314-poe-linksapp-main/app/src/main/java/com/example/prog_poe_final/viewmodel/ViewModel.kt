package com.example.prog_poe_final.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.prog_poe_final.models.User
import com.example.prog_poe_final.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    private val _statusMessage = MutableLiveData<String>()
    val statusMessage: LiveData<String> get() = _statusMessage

    private val _isSuccessful = MutableLiveData<Boolean>()
    val isSuccessful: LiveData<Boolean> get() = _isSuccessful

    fun registerUser(user: User) {
        repository.registerUser(user) { success, message ->
            _isSuccessful.value = success
            _statusMessage.value = message
        }
    }

    fun loginUser(email: String, password: String) {
        repository.loginUser(email, password) { success, user, message ->
            _isSuccessful.value = success
            _statusMessage.value = message
            if (success && user != null) {
                _currentUser.value = user
            }
        }
    }
}

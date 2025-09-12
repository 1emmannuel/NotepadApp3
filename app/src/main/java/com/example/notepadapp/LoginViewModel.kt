package com.example.notepadapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application)
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val TAG = "LoginViewModel"

    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                val trimmedUsername = username.trim()
                val trimmedPassword = password.trim()
                Log.d(TAG, "Attempting login: username=$trimmedUsername, password=$trimmedPassword")
                val user = repository.getUserByUsernameAndPassword(trimmedUsername, trimmedPassword)
                _user.value = user
                if (user != null) {
                    Log.d(TAG, "Login successful: userId=${user.id}, username=${user.username}")
                } else {
                    Log.d(TAG, "Login failed: No user found for username=$trimmedUsername")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login: ${e.message}")
                _user.value = null
            }
        }
    }
}
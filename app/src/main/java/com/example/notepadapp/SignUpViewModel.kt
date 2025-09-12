package com.example.notepadapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SignUpViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository(application)
    private val _signupSuccess = MutableLiveData<Boolean>()
    val signupSuccess: LiveData<Boolean> = _signupSuccess
    private val TAG = "SignUpViewModel"

    fun signup(username: String, password: String) {
        viewModelScope.launch {
            try {
                val trimmedUsername = username.trim()
                val trimmedPassword = password.trim()
                Log.d(TAG, "Attempting signup: username=$trimmedUsername, password=$trimmedPassword")
                val existingUser = repository.getUserByUsername(trimmedUsername)
                if (existingUser == null) {
                    repository.insert(User(username = trimmedUsername, password = trimmedPassword))
                    _signupSuccess.value = true
                    Log.d(TAG, "Sign up successful: username=$trimmedUsername")
                } else {
                    _signupSuccess.value = false
                    Log.d(TAG, "Sign up failed: Username $trimmedUsername already exists")
                }
                val allUsers = repository.getAllUsers()
                Log.d(TAG, "Current users: ${allUsers.joinToString { "username=${it.username}, password=${it.password}, id=${it.id}" }}")
            } catch (e: Exception) {
                Log.e(TAG, "Error during signup: ${e.message}")
                _signupSuccess.value = false
            }
        }
    }
}
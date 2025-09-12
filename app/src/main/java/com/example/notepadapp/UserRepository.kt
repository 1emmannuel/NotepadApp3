package com.example.notepadapp

import android.app.Application
import android.util.Log

class UserRepository(application: Application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val TAG = "UserRepository"

    suspend fun insert(user: User) {
        try {
            userDao.insert(user)
            Log.d(TAG, "User inserted: username=${user.username}, password=${user.password}, id=${user.id}")
            val allUsers = getAllUsers()
            Log.d(TAG, "All users after insert: ${allUsers.joinToString { "username=${it.username}, password=${it.password}, id=${it.id}" }}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting user: ${e.message}")
        }
    }

    suspend fun getUserByUsernameAndPassword(username: String, password: String): User? {
        try {
            val trimmedUsername = username.trim()
            val trimmedPassword = password.trim()
            val user = userDao.getUserByUsernameAndPassword(trimmedUsername, trimmedPassword)
            Log.d(TAG, "Login query: username=$trimmedUsername, password=$trimmedPassword, result=${if (user != null) "found (id=${user.id}, username=${user.username}, password=${user.password})" else "not found"}")
            val allUsers = getAllUsers()
            Log.d(TAG, "All users after login: ${allUsers.joinToString { "username=${it.username}, password=${it.password}, id=${it.id}" }}")
            return user
        } catch (e: Exception) {
            Log.e(TAG, "Error during login query: ${e.message}")
            return null
        }
    }

    suspend fun getUserByUsername(username: String): User? {
        try {
            val trimmedUsername = username.trim()
            val user = userDao.getUserByUsername(trimmedUsername)
            Log.d(TAG, "Username query: username=$trimmedUsername, result=${if (user != null) "found (id=${user.id}, username=${user.username})" else "not found"}")
            return user
        } catch (e: Exception) {
            Log.e(TAG, "Error finding user by username: ${e.message}")
            return null
        }
    }

    suspend fun getAllUsers(): List<User> {
        try {
            val users = userDao.getAllUsers()
            Log.d(TAG, "All users: ${users.joinToString { "username=${it.username}, password=${it.password}, id=${it.id}" }}")
            return users
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching users: ${e.message}")
            return emptyList()
        }
    }
}
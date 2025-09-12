package com.example.notepadapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.notepadapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    private val loginViewModel: LoginViewModel by viewModels()
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityLoginBinding.inflate(layoutInflater)
            setContentView(binding?.root ?: return)
        } catch (e: Exception) {
            Log.e(TAG, "UI init error: ${e.message}")
            Toast.makeText(this, "Failed to load UI", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding?.btnLogin?.setOnClickListener {
            val username = binding?.etUsername?.text?.toString()?.trim() ?: ""
            val password = binding?.etPassword?.text?.toString()?.trim() ?: ""
            if (username.isNotEmpty() && password.isNotEmpty()) {
                Log.d(TAG, "Login button clicked: username=$username, password=$password")
                loginViewModel.login(username, password)
            } else {
                Toast.makeText(this, "Enter username and password", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Username or password empty")
            }
        }

        binding?.tvSignUp?.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            Log.d(TAG, "Navigating to SignUpActivity")
        }

        loginViewModel.user.observe(this) { user ->
            try {
                if (user != null) {
                    // Save user_id to SharedPreferences
                    val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
                    prefs.edit().putInt("user_id", user.id).apply()
                    Log.d(TAG, "Login successful: userId=${user.id}, username=${user.username}")
                    Log.d(TAG, "Starting MainActivity")
                    // Clear back stack and start MainActivity
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish() // Close LoginActivity
                    Log.d(TAG, "LoginActivity finished")
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Login failed: Invalid credentials")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling login result: ${e.message}")
                Toast.makeText(this, "Login error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        Log.d(TAG, "LoginActivity destroyed")
    }
}
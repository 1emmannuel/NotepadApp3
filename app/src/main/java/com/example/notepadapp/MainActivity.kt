package com.example.notepadapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.notepadapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding?.root ?: return)
            Log.d(TAG, "View binding initialized")
        } catch (e: Exception) {
            Log.e(TAG, "UI init error: ${e.message}", e)
            Toast.makeText(this, "Failed to load UI: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val userId = getSharedPreferences("prefs", MODE_PRIVATE).getInt("user_id", -1)
        if (userId == -1) {
            Log.e(TAG, "No user_id provided")
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
            return
        }
        Log.d(TAG, "Started with userId=$userId")

        binding?.btnNotes?.setOnClickListener {
            Log.d(TAG, "Navigating to NotesActivity with userId=$userId")
            val intent = Intent(this@MainActivity, NotesActivity::class.java).apply {
                putExtra("user_id", userId)
            }
            try {
                startActivity(intent)
                Log.d(TAG, "Successfully started NotesActivity")
            } catch (e: Exception) {
                Log.e(TAG, "Error navigating to NotesActivity: ${e.javaClass.name} - ${e.message}", e)
                Toast.makeText(this, "Error opening notes: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Log.e(TAG, "btnNotes is null")
            Toast.makeText(this, "Notes button not found", Toast.LENGTH_LONG).show()
        }

        binding?.btnSignOut?.setOnClickListener {
            Log.d(TAG, "Sign out clicked")
            try {
                getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this@MainActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error signing out: ${e.message}", e)
                Toast.makeText(this, "Error signing out", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e(TAG, "btnSignOut is null")
            Toast.makeText(this, "Sign out button not found", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        Log.d(TAG, "MainActivity destroyed")
    }
}
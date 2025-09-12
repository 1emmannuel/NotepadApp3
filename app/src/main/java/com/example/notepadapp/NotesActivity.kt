package com.example.notepadapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepadapp.databinding.ActivityNotesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesActivity : AppCompatActivity() {
    private var binding: ActivityNotesBinding? = null
    private val notesViewModel: NotesViewModel by viewModels()
    private val TAG = "NotesActivity"
    private var editingNote: Note? = null
    private lateinit var noteAdapter: NoteAdapter
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding?.ivImage?.setImageURI(it)
            Log.d(TAG, "Image selected: $it")
        } ?: run {
            Log.d(TAG, "No image selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivityNotesBinding.inflate(layoutInflater)
            setContentView(binding?.root ?: run {
                Log.e(TAG, "Binding root is null")
                Toast.makeText(this, "Failed to initialize UI", Toast.LENGTH_LONG).show()
                finish()
                return
            })
            Log.d(TAG, "View binding initialized")
        } catch (e: Exception) {
            Log.e(TAG, "UI initialization failed: ${e.message}", e)
            Toast.makeText(this, "UI error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            Log.e(TAG, "No user_id provided in intent")
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@NotesActivity, LoginActivity::class.java))
            finish()
            return
        }
        Log.d(TAG, "Started with userId=$userId")

        // Check for edit intent
        val noteId = intent.getIntExtra("note_id", -1)
        if (noteId != -1) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val note = notesViewModel.getNoteById(noteId)
                    if (note != null) {
                        editingNote = note
                        binding?.etTitle?.setText(note.title)
                        binding?.etBody?.setText(note.content)
                        binding?.btnSave?.text = "Update"
                        note.imageUri?.let { uri ->
                            try {
                                selectedImageUri = Uri.parse(uri)
                                binding?.ivImage?.setImageURI(selectedImageUri)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error setting image URI: ${e.message}")
                            }
                        }
                        Log.d(TAG, "Editing note: id=${note.id}, title=${note.title}")
                    } else {
                        Log.e(TAG, "Note not found for id=$noteId")
                        Toast.makeText(this@NotesActivity, "Note not found", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading note for edit: ${e.message}", e)
                    Toast.makeText(this@NotesActivity, "Error loading note", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up RecyclerView
        try {
            noteAdapter = NoteAdapter(
                onEditClick = { note ->
                    Log.d(TAG, "Edit clicked for note: id=${note.id}, title=${note.title}")
                    val intent = Intent(this@NotesActivity, NotesActivity::class.java).apply {
                        putExtra("user_id", userId)
                        putExtra("note_id", note.id)
                    }
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error navigating to edit note: ${e.message}", e)
                        Toast.makeText(this, "Error editing note", Toast.LENGTH_SHORT).show()
                    }
                },
                onDeleteClick = { note ->
                    Log.d(TAG, "Delete clicked for note: id=${note.id}, title=${note.title}")
                    AlertDialog.Builder(this)
                        .setTitle("Delete Note")
                        .setMessage("Are you sure you want to delete '${note.title}'?")
                        .setPositiveButton("Delete") { _, _ ->
                            notesViewModel.deleteNote(note)
                            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            )
            binding?.rvNotes?.apply {
                layoutManager = LinearLayoutManager(this@NotesActivity)
                adapter = noteAdapter
                Log.d(TAG, "RecyclerView initialized")
            } ?: run {
                Log.e(TAG, "rvNotes is null")
                Toast.makeText(this, "Notes list not initialized", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        } catch (e: Exception) {
            Log.e(TAG, "RecyclerView setup failed: ${e.message}", e)
            Toast.makeText(this, "Error setting up notes list", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Observe notes
        try {
            notesViewModel.getNotesByUserId(userId).observe(this) { notes ->
                Log.d(TAG, "Notes loaded: ${notes?.size ?: 0}")
                noteAdapter.submitList(notes)
                if (notes.isNullOrEmpty()) {
                    Toast.makeText(this, "No notes found", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error observing notes: ${e.message}", e)
            Toast.makeText(this, "Error loading notes", Toast.LENGTH_SHORT).show()
        }

        // Pick image
        binding?.btnPickImage?.setOnClickListener {
            Log.d(TAG, "Pick image clicked")
            try {
                pickImage.launch("image/*")
            } catch (e: Exception) {
                Log.e(TAG, "Error launching image picker: ${e.message}", e)
                Toast.makeText(this, "Error picking image", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Log.e(TAG, "btnPickImage is null")
            Toast.makeText(this, "Pick image button not found", Toast.LENGTH_LONG).show()
        }

        // Save/Update Note
        binding?.btnSave?.setOnClickListener {
            val title = binding?.etTitle?.text?.toString()?.trim() ?: ""
            val content = binding?.etBody?.text?.toString()?.trim() ?: ""
            if (title.isNotEmpty() && content.isNotEmpty()) {
                Log.d(TAG, "Save/Update button clicked: title=$title, content=$content")
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        if (editingNote != null) {
                            val updatedNote = editingNote!!.copy(
                                title = title,
                                content = content,
                                imageUri = selectedImageUri?.toString()
                            )
                            notesViewModel.updateNote(updatedNote)
                            Toast.makeText(this@NotesActivity, "Note updated", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Note updated: id=${updatedNote.id}, title=$title")
                        } else {
                            val note = Note(
                                userId = userId,
                                title = title,
                                content = content,
                                imageUri = selectedImageUri?.toString()
                            )
                            notesViewModel.insertNote(note)
                            Toast.makeText(this@NotesActivity, "Note saved", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Note saved: title=$title")
                        }
                        binding?.etTitle?.text?.clear()
                        binding?.etBody?.text?.clear()
                        binding?.ivImage?.setImageDrawable(null)
                        binding?.btnSave?.text = "Save"
                        selectedImageUri = null
                        editingNote = null
                    } catch (e: Exception) {
                        Log.e(TAG, "Error saving/updating note: ${e.message}", e)
                        Toast.makeText(this@NotesActivity, "Error saving note", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Enter title and content", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Save/Update failed: empty title or content")
            }
        } ?: run {
            Log.e(TAG, "btnSave is null")
            Toast.makeText(this, "Save button not found", Toast.LENGTH_LONG).show()
        }

        // Delete Note (for editing mode)
        binding?.btnDelete?.setOnClickListener {
            if (editingNote != null) {
                Log.d(TAG, "Delete button clicked for note: id=${editingNote!!.id}, title=${editingNote!!.title}")
                AlertDialog.Builder(this)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete '${editingNote!!.title}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        try {
                            notesViewModel.deleteNote(editingNote!!)
                            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
                            binding?.etTitle?.text?.clear()
                            binding?.etBody?.text?.clear()
                            binding?.ivImage?.setImageDrawable(null)
                            binding?.btnSave?.text = "Save"
                            selectedImageUri = null
                            editingNote = null
                        } catch (e: Exception) {
                            Log.e(TAG, "Error deleting note: ${e.message}", e)
                            Toast.makeText(this, "Error deleting note", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                Toast.makeText(this, "No note selected for deletion", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Delete button clicked but no note is being edited")
            }
        } ?: run {
            Log.e(TAG, "btnDelete is null")
            Toast.makeText(this, "Delete button not found", Toast.LENGTH_LONG).show()
        }

        // Sign out
        binding?.btnSignOut?.setOnClickListener {
            Log.d(TAG, "Sign out clicked")
            try {
                getSharedPreferences("prefs", MODE_PRIVATE).edit().clear().apply()
                val intent = Intent(this@NotesActivity, LoginActivity::class.java).apply {
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
        Log.d(TAG, "NotesActivity destroyed")
    }
}
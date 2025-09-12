package com.example.notepadapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NoteRepository(application) // Correct: Passes Application
    private val TAG = "NotesViewModel"

    fun getNotesByUserId(userId: Int): LiveData<List<Note>> {
        try {
            val notes = repository.getNotesByUserId(userId)
            Log.d(TAG, "Notes loaded for userId=$userId")
            return notes
        } catch (e: Exception) {
            Log.e(TAG, "Error loading notes: ${e.message}")
            return repository.getNotesByUserId(userId)
        }
    }

    fun insertNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.insert(note)
                Log.d(TAG, "Note inserted: id=${note.id}, title=${note.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting note: ${e.message}")
                throw e
            }
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.update(note)
                Log.d(TAG, "Note updated: id=${note.id}, title=${note.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating note: ${e.message}")
                throw e
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                repository.delete(note)
                Log.d(TAG, "Note deleted: id=${note.id}, title=${note.title}")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting note: ${e.message}")
            }
        }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return repository.getNoteById(noteId)
    }
}
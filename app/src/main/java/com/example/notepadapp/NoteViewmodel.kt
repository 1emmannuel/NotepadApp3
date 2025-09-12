package com.example.notepadapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val noteRepository: NoteRepository
    private val TAG = "NoteViewModel"

    init {
        try {
            val noteDao = AppDatabase.getDatabase(application).noteDao()
            noteRepository = NoteRepository(noteDao as Application)
        } catch (e: Exception) {
            Log.e(TAG, "Init failed: ${e.message}")
            throw RuntimeException("Cannot init NoteViewModel", e)
        }
    }

    fun insert(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.insert(note)
            } catch (e: Exception) {
                Log.e(TAG, "Insert failed: ${e.message}")
            }
        }
    }

    fun update(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.update(note)
            } catch (e: Exception) {
                Log.e(TAG, "Update failed: ${e.message}")
            }
        }
    }

    fun delete(note: Note) {
        viewModelScope.launch {
            try {
                noteRepository.delete(note)
            } catch (e: Exception) {
                Log.e(TAG, "Delete failed: ${e.message}")
            }
        }
    }
}
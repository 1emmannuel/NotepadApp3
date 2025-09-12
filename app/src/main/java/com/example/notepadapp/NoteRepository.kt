package com.example.notepadapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData

class NoteRepository(application: Application) {
    private val noteDao = AppDatabase.getDatabase(application).noteDao()
    private val TAG = "NoteRepository"

    suspend fun insert(note: Note) {
        try {
            noteDao.insert(note)
            Log.d(TAG, "Note inserted: id=${note.id}, userId=${note.userId}, title=${note.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting note: ${e.message}")
            throw e
        }
    }

    suspend fun update(note: Note) {
        try {
            noteDao.update(note)
            Log.d(TAG, "Note updated: id=${note.id}, title=${note.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating note: ${e.message}")
            throw e
        }
    }

    suspend fun delete(note: Note) {
        try {
            noteDao.delete(note)
            Log.d(TAG, "Note deleted: id=${note.id}, title=${note.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note: ${e.message}")
            throw e
        }
    }

    fun getNotesByUserId(userId: Int): LiveData<List<Note>> {
        try {
            val notes = noteDao.getNotesByUserId(userId)
            Log.d(TAG, "Notes fetched for userId=$userId")
            return notes
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notes: ${e.message}")
            throw e
        }
    }

    suspend fun getNoteById(noteId: Int): Note? {
        try {
            val note = noteDao.getNoteById(noteId)
            Log.d(TAG, "Note fetched: id=$noteId, title=${note?.title}")
            return note
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching note by id: ${e.message}")
            return null
        }
    }
}
package com.example.anchornotes.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import java.util.UUID;

/**
 * Room DAO for Note and Reminder entities.
 * Matches methods listed in design doc 5.1.2.
 */
@Dao
public interface NoteDao {

    // ---------- Notes ----------

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    Note getNoteById(UUID id);

    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    List<Note> getAllNotesPinnedFirst();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Note note);

    @Update
    void updateNote(Note note);

    @Delete
    void deleteNote(Note note);

    // ---------- Reminders ----------

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    Reminder getReminderById(UUID id);

    @Query("SELECT * FROM reminders WHERE noteId = :noteId LIMIT 1")
    Reminder getReminderForNote(UUID noteId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReminder(Reminder reminder);

    @Update
    void updateReminder(Reminder reminder);

    @Delete
    void deleteReminder(Reminder reminder);

}

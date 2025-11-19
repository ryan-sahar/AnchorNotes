package com.example.anchornotes.data;

import android.content.Context;

import java.util.List;
import java.util.UUID;

/**
 * Repository wrapping the Room DAO.
 * Implements the methods described in design doc 5.1.2.
 */
public class NoteRepository {

    private final NoteDao noteDao;

    public NoteRepository(Context context) {
        LocalDatabase db = LocalDatabase.getInstance(context.getApplicationContext());
        this.noteDao = db.noteDao();
    }

    // ---------- Notes ----------

    public List<Note> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public Note getNote(UUID id) {
        if (id == null) return null;
        return noteDao.getNoteById(id);
    }

    public void insertNote(Note note) {
        noteDao.insertNote(note);
    }

    public void updateNote(Note note) {
        noteDao.updateNote(note);
    }

    public void deleteNote(Note note) {
        noteDao.deleteNote(note);
    }

    // ---------- Reminders ----------

    public Reminder getReminderById(UUID id) {
        if (id == null) return null;
        return noteDao.getReminderById(id);
    }

    public Reminder getReminderForNote(UUID noteId) {
        if (noteId == null) return null;
        return noteDao.getReminderForNote(noteId);
    }

    public void insertReminder(Reminder reminder) {
        noteDao.insertReminder(reminder);
    }

    public void updateReminder(Reminder reminder) {
        noteDao.updateReminder(reminder);
    }

    public void deleteReminder(Reminder reminder) {
        noteDao.deleteReminder(reminder);
    }
}

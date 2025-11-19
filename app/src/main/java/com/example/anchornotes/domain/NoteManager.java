package com.example.anchornotes.domain;

import android.content.Context;
import android.util.Pair;

import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.NoteRepository;
import com.example.anchornotes.data.Reminder;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Domain-layer manager for note business logic.
 *
 * Matches design doc 5.1.3:
 *  - createNote
 *  - updateNote
 *  - deleteNote (with reminder cleanup)
 *  - getNoteWithReminder
 */
public class NoteManager {

    private final NoteRepository noteRepository;
    private final ReminderManager reminderManager;

    public NoteManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.noteRepository = new NoteRepository(appContext);
        this.reminderManager = new ReminderManager(appContext);
    }

    // ---------- Queries ----------

    public List<Note> getAllNotes() {
        return noteRepository.getAllNotes();
    }

    public Note getNote(UUID id) {
        if (id == null) return null;
        return noteRepository.getNote(id);
    }

    /**
     * Return (Note, Reminder?) pair as in the design doc.
     */
    public Pair<Note, Reminder> getNoteWithReminder(UUID noteId) {
        if (noteId == null) {
            return Pair.create(null, null);
        }

        Note note = noteRepository.getNote(noteId);
        Reminder reminder = null;
        if (note != null) {
            reminder = noteRepository.getReminderForNote(noteId);
        }

        return Pair.create(note, reminder);
    }

    // ---------- Mutations ----------

    public Note createNote(String title, String content) {
        Note note = new Note(title, content);
        noteRepository.insertNote(note);
        return note;
    }

    public void updateNote(Note note) {
        if (note == null) return;
        note.setUpdatedAt(new Date());
        noteRepository.updateNote(note);
    }

    /**
     * Delete a note by ID and clean up any attached reminders,
     * matching sequence diagram 5.3.7.
     */
    public void deleteNote(UUID id) {
        if (id == null) return;

        Note existing = noteRepository.getNote(id);
        if (existing != null) {
            // First remove any reminders tied to this note
            reminderManager.removeRemindersForNote(id);

            // Then delete the note itself
            noteRepository.deleteNote(existing);
        }
    }
}

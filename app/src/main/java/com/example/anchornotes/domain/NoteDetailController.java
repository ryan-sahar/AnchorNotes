package com.example.anchornotes.domain;

import android.content.Context;
import android.util.Pair;

import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.Reminder;

import java.util.UUID;

/**
 * Controller for the Note Detail screen.
 *
 * Matches the design document's NoteDetailController:
 * - Load note + reminder
 * - Save note
 * - Delete note
 * - Delegate reminder setup events
 */
public class NoteDetailController {

    public interface Listener {
        void onNoteLoaded(Note note, Reminder reminder);
        void onNoteSaved();
        void onNoteDeleted();
        void navigateToTimeReminder(UUID noteId);
        void navigateToLocationReminder(UUID noteId);
        void showError(String message);
    }

    private final NoteManager noteManager;
    private final ReminderManager reminderManager;
    private final Listener listener;

    public NoteDetailController(Context context, Listener listener) {
        Context app = context.getApplicationContext();
        this.noteManager = new NoteManager(app);
        this.reminderManager = new ReminderManager(app);
        this.listener = listener;
    }

    // ------------------------------------------------------------
    // Load existing note (with reminder)
    // ------------------------------------------------------------
    public void loadExistingNote(UUID noteId) {
        Pair<Note, Reminder> pair = noteManager.getNoteWithReminder(noteId);
        if (listener != null) {
            listener.onNoteLoaded(pair.first, pair.second);
        }
    }

    // ------------------------------------------------------------
    // Create or update note
    // ------------------------------------------------------------
    public void saveNote(UUID noteId, String title, String content) {
        if (title == null || title.trim().isEmpty()) {
            if (listener != null) {
                listener.showError("Title cannot be empty");
            }
            return;
        }

        if (noteId == null) {
            // NEW NOTE
            Note created = noteManager.createNote(title, content);
            // Caller will typically finish() and reopen for editing if needed.
            if (listener != null) {
                listener.onNoteSaved();
            }
        } else {
            // UPDATE NOTE
            Note existing = noteManager.getNote(noteId);
            if (existing == null) {
                if (listener != null) {
                    listener.showError("Error: Note not found");
                }
                return;
            }

            existing.setTitle(title);
            existing.setContent(content);
            noteManager.updateNote(existing);

            if (listener != null) {
                listener.onNoteSaved();
            }
        }
    }

    // ------------------------------------------------------------
    // Delete note
    // ------------------------------------------------------------
    public void deleteNote(UUID noteId) {
        if (noteId == null) return;
        noteManager.deleteNote(noteId);
        if (listener != null) {
            listener.onNoteDeleted();
        }
    }

    // ------------------------------------------------------------
    // Reminder-related UI navigation
    // ------------------------------------------------------------
    public void onAddTimeReminder(UUID noteId) {
        if (noteId == null) {
            if (listener != null) {
                listener.showError("Please save the note before adding a reminder.");
            }
            return;
        }
        if (listener != null) {
            listener.navigateToTimeReminder(noteId);
        }
    }

    public void onAddLocationReminder(UUID noteId) {
        if (noteId == null) {
            if (listener != null) {
                listener.showError("Please save the note before adding a reminder.");
            }
            return;
        }
        if (listener != null) {
            listener.navigateToLocationReminder(noteId);
        }
    }
}

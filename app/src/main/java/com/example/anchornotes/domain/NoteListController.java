package com.example.anchornotes.domain;

import android.content.Context;

import com.example.anchornotes.data.Note;

import java.util.List;
import java.util.UUID;

/**
 * Presentation-layer controller for the main note list screen.
 *
 * This matches the design document's NoteListController, keeping the
 * Activity focused on view logic while this class coordinates with
 * the domain NoteManager.
 */
public class NoteListController {

    public interface Listener {
        /** Called when notes have been loaded from the domain layer. */
        void onNotesLoaded(List<Note> notes);

        /** Navigate to the detail screen for the given note. */
        void navigateToNoteDetail(UUID noteId);

        /** Navigate to the screen for creating a brand new note. */
        void navigateToCreateNote();
    }

    private final NoteManager noteManager;
    private final Listener listener;

    public NoteListController(Context context, Listener listener) {
        this.noteManager = new NoteManager(context.getApplicationContext());
        this.listener = listener;
    }

    /** Load all notes and deliver them back to the UI layer. */
    public void loadNotes() {
        List<Note> notes = noteManager.getAllNotes();
        if (listener != null) {
            listener.onNotesLoaded(notes);
        }
    }

    /** User tapped an existing note in the list. */
    public void onNoteSelected(UUID noteId) {
        if (listener != null && noteId != null) {
            listener.navigateToNoteDetail(noteId);
        }
    }

    /** User tapped the "add note" button. */
    public void onAddNoteClicked() {
        if (listener != null) {
            listener.navigateToCreateNote();
        }
    }
}

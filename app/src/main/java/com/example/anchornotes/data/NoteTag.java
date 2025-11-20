package com.example.anchornotes.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.util.UUID;

/**
 * Join table between notes and tags (many-to-many).
 */
@Entity(
        tableName = "note_tags",
        primaryKeys = { "noteId", "tagId" }
)
public class NoteTag {

    @NonNull
    private UUID noteId;

    @NonNull
    private UUID tagId;

    public NoteTag(@NonNull UUID noteId, @NonNull UUID tagId) {
        this.noteId = noteId;
        this.tagId = tagId;
    }

    @NonNull
    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(@NonNull UUID noteId) {
        this.noteId = noteId;
    }

    @NonNull
    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(@NonNull UUID tagId) {
        this.tagId = tagId;
    }
}

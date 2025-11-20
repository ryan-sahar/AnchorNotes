package com.example.anchornotes.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;
import java.util.UUID;

/**
 * DAO for tags and note-tag mappings.
 * We’ll expand this later when wiring UI filters.
 */
@Dao
public interface TagDao {

    // ---------------- TAGS ----------------

    @Query("SELECT * FROM tags ORDER BY name ASC")
    List<Tag> getAllTags();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTag(Tag tag);

    @Delete
    void deleteTag(Tag tag);

    // ---------------- NOTE ↔ TAG MAPPINGS ----------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNoteTag(NoteTag noteTag);

    @Delete
    void deleteNoteTag(NoteTag noteTag);

    @Query("DELETE FROM note_tags WHERE noteId = :noteId")
    void deleteTagsForNote(UUID noteId);

    @Query("SELECT t.* FROM tags t " +
            "INNER JOIN note_tags nt ON t.id = nt.tagId " +
            "WHERE nt.noteId = :noteId " +
            "ORDER BY t.name ASC")
    List<Tag> getTagsForNote(UUID noteId);

    @Query("SELECT n.* FROM notes n " +
            "INNER JOIN note_tags nt ON n.id = nt.noteId " +
            "WHERE nt.tagId = :tagId " +
            "ORDER BY n.updatedAt DESC")
    List<Note> getNotesForTag(UUID tagId);
}

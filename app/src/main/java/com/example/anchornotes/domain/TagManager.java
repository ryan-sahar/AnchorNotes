package com.example.anchornotes.domain;

import android.content.Context;

import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.Tag;
import com.example.anchornotes.data.TagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain-layer manager for tag business logic.
 *
 * Responsibilities:
 *  - Create/delete tags.
 *  - Attach/detach tags to notes.
 *  - Query tags for a note, and notes for a tag.
 */
public class TagManager {

    private final TagRepository tagRepository;

    public TagManager(Context context) {
        this.tagRepository = new TagRepository(context.getApplicationContext());
    }

    // ------------------------------------------------------------------------
    // Tags CRUD
    // ------------------------------------------------------------------------

    /** Return all tags in alphabetical order. */
    public List<Tag> getAllTags() {
        return tagRepository.getAllTags();
    }

    /** Create a new tag with the given name (if non-empty). */
    public Tag createTag(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        Tag tag = new Tag(trimmed);
        tagRepository.insertTag(tag);
        return tag;
    }

    /** Delete a tag and implicitly removes all note-tag mappings (via foreign key or DAO). */
    public void deleteTag(Tag tag) {
        if (tag == null) return;
        tagRepository.deleteTag(tag);
        // Note: our DAO deletes mappings per-note; here we rely on logic where needed.
    }

    // ------------------------------------------------------------------------
    // Note ↔ Tag links
    // ------------------------------------------------------------------------

    /** Attach an existing tag to a note. */
    public void attachTagToNote(UUID noteId, UUID tagId) {
        if (noteId == null || tagId == null) return;
        tagRepository.attachTagToNote(noteId, tagId);
    }

    /** Detach an existing tag from a note. */
    public void detachTagFromNote(UUID noteId, UUID tagId) {
        if (noteId == null || tagId == null) return;
        tagRepository.detachTagFromNote(noteId, tagId);
    }

    /** Replace all tags for a note with a new set of tag IDs. */
    public void setTagsForNote(UUID noteId, List<UUID> tagIds) {
        if (noteId == null) return;

        // Clear existing
        tagRepository.clearTagsForNote(noteId);

        if (tagIds == null) {
            return;
        }

        for (UUID tagId : tagIds) {
            if (tagId != null) {
                tagRepository.attachTagToNote(noteId, tagId);
            }
        }
    }

    /** Get all tags associated with a given note. */
    public List<Tag> getTagsForNote(UUID noteId) {
        if (noteId == null) {
            return new ArrayList<>();
        }
        List<Tag> tags = tagRepository.getTagsForNote(noteId);
        return tags != null ? tags : new ArrayList<>();
    }

    /** Get all notes that have a given tag. */
    public List<Note> getNotesForTag(UUID tagId) {
        if (tagId == null) {
            return new ArrayList<>();
        }
        List<Note> notes = tagRepository.getNotesForTag(tagId);
        return notes != null ? notes : new ArrayList<>();
    }
}

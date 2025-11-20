package com.example.anchornotes.data;

import android.content.Context;

import java.util.List;
import java.util.UUID;

/**
 * Repository for tag data, wrapping TagDao.
 */
public class TagRepository {

    private final TagDao tagDao;

    public TagRepository(Context context) {
        LocalDatabase db = LocalDatabase.getInstance(context.getApplicationContext());
        this.tagDao = db.tagDao();
    }

    public List<Tag> getAllTags() {
        return tagDao.getAllTags();
    }

    public void insertTag(Tag tag) {
        tagDao.insertTag(tag);
    }

    public void deleteTag(Tag tag) {
        tagDao.deleteTag(tag);
    }

    public void attachTagToNote(UUID noteId, UUID tagId) {
        tagDao.insertNoteTag(new NoteTag(noteId, tagId));
    }

    public void detachTagFromNote(UUID noteId, UUID tagId) {
        tagDao.deleteNoteTag(new NoteTag(noteId, tagId));
    }

    public void clearTagsForNote(UUID noteId) {
        tagDao.deleteTagsForNote(noteId);
    }

    public List<Tag> getTagsForNote(UUID noteId) {
        return tagDao.getTagsForNote(noteId);
    }

    public List<Note> getNotesForTag(UUID tagId) {
        return tagDao.getNotesForTag(tagId);
    }
}

package com.example.anchornotes.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.UUID;

/**
 * Note entity (matches design doc 5.1.1).
 *
 * Attributes:
 *  - id: UUID
 *  - title: String
 *  - content: String
 *  - createdAt: Date
 *  - updatedAt: Date
 *  - reminderId: UUID? (nullable)
 */
@Entity(tableName = "notes")
public class Note {

    @PrimaryKey
    @NonNull
    private UUID id;

    private String title;
    private String content;

    private Date createdAt;
    private Date updatedAt;

    @Nullable
    private UUID reminderId;

    // Empty constructor required by Room
    public Note() {
    }

    // Convenience constructor used by NoteManager.createNote(...)
    public Note(String title, String content) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.content = content;
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // ----- Getters / Setters -----

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Nullable
    public UUID getReminderId() {
        return reminderId;
    }

    public void setReminderId(@Nullable UUID reminderId) {
        this.reminderId = reminderId;
    }
}

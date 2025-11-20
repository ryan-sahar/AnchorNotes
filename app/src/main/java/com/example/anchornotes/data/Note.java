package com.example.anchornotes.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "notes")
public class Note {

    @PrimaryKey
    @NonNull
    private UUID id;

    private String title;
    private String content;

    private Date createdAt;
    private Date updatedAt;

    // Existing reminder field
    @Nullable
    private UUID reminderId;

    // ----------------------------------------------------
    // NEW FIELDS FOR PROJECT D
    // ----------------------------------------------------

    // Pinned notes (Feature 2b)
    private boolean pinned = false;

    // Context location (Feature 2c)
    @Nullable
    private Double locationLat;
    @Nullable
    private Double locationLng;

    // Attachments (Feature 1)
    @Nullable
    private String photoUri;

    @Nullable
    private String audioUri;

    // Relevant Notes (Feature 4)
    @Nullable
    private Long lastRelevantTriggeredAt;

    @Nullable
    private String relevantType; // "TIME" or "GEOFENCE"

    // ----------------------------------------------------

    public Note() {
    }

    public Note(String title, String content) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.content = content;

        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // ----------------------------------------------------
    // GETTERS / SETTERS
    // ----------------------------------------------------

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    @Nullable
    public UUID getReminderId() { return reminderId; }
    public void setReminderId(@Nullable UUID reminderId) { this.reminderId = reminderId; }

    // ------------------ NEW FIELDS ----------------------

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    @Nullable
    public Double getLocationLat() { return locationLat; }
    public void setLocationLat(@Nullable Double locationLat) { this.locationLat = locationLat; }

    @Nullable
    public Double getLocationLng() { return locationLng; }
    public void setLocationLng(@Nullable Double locationLng) { this.locationLng = locationLng; }

    @Nullable
    public String getPhotoUri() { return photoUri; }
    public void setPhotoUri(@Nullable String photoUri) { this.photoUri = photoUri; }

    @Nullable
    public String getAudioUri() { return audioUri; }
    public void setAudioUri(@Nullable String audioUri) { this.audioUri = audioUri; }

    @Nullable
    public Long getLastRelevantTriggeredAt() { return lastRelevantTriggeredAt; }
    public void setLastRelevantTriggeredAt(@Nullable Long lastRelevantTriggeredAt) {
        this.lastRelevantTriggeredAt = lastRelevantTriggeredAt;
    }

    @Nullable
    public String getRelevantType() { return relevantType; }
    public void setRelevantType(@Nullable String relevantType) { this.relevantType = relevantType; }
}

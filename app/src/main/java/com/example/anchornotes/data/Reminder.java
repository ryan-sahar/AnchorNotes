package com.example.anchornotes.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.UUID;

@Entity(tableName = "reminders")
public class Reminder {

    @PrimaryKey
    @NonNull
    private UUID id;

    @NonNull
    private UUID noteId;

    private ReminderType type;

    // Time-based reminder field
    private Date triggerTime;

    // Location-based reminder fields
    private Double locationLat;
    private Double locationLng;
    private Float radiusMeters;

    private boolean isActive;
    private Date retiredAt;

    public Reminder(@NonNull UUID noteId, ReminderType type) {
        this.id = UUID.randomUUID();
        this.noteId = noteId;
        this.type = type;
        this.isActive = true;
    }

    // Empty constructor required by Room
    public Reminder() {
    }

    // -------- Getters and Setters --------

    @NonNull
    public UUID getId() {
        return id;
    }

    public void setId(@NonNull UUID id) {
        this.id = id;
    }

    @NonNull
    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(@NonNull UUID noteId) {
        this.noteId = noteId;
    }

    public ReminderType getType() {
        return type;
    }

    public void setType(ReminderType type) {
        this.type = type;
    }

    public Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }

    public Double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(Double locationLat) {
        this.locationLat = locationLat;
    }

    public Double getLocationLng() {
        return locationLng;
    }

    public void setLocationLng(Double locationLng) {
        this.locationLng = locationLng;
    }

    public Float getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(Float radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public boolean isActive() {
        return isActive;
    }

    // Required setter for Room
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public Date getRetiredAt() {
        return retiredAt;
    }

    // Required setter for Room
    public void setRetiredAt(Date retiredAt) {
        this.retiredAt = retiredAt;
    }

    // Business logic helper
    public void markRetired() {
        this.isActive = false;
        this.retiredAt = new Date();
    }
}

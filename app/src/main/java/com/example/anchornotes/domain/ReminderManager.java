package com.example.anchornotes.domain;

import android.content.Context;

import com.example.anchornotes.data.LocationReminderService;
import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.NoteRepository;
import com.example.anchornotes.data.Reminder;
import com.example.anchornotes.data.ReminderType;
import com.example.anchornotes.services.TimeReminderService;

import java.util.Date;
import java.util.UUID;

/**
 * Domain-layer class for reminder business logic.
 *
 * Responsibilities:
 *  - Enforce "one active reminder per note" rule
 *  - Create/update/delete Reminder entities in the repository
 *  - Delegate OS integration to TimeReminderService and LocationReminderService
 */
public class ReminderManager {

    private final NoteRepository noteRepository;
    private final TimeReminderService timeReminderService;
    private final LocationReminderService locationReminderService;

    public ReminderManager(Context context) {
        Context appContext = context.getApplicationContext();
        this.noteRepository = new NoteRepository(appContext);
        this.timeReminderService = new TimeReminderService(appContext);
        this.locationReminderService = new LocationReminderService(appContext);
    }

    /**
     * Check if we're allowed to add a reminder of this type to the note.
     * In our design, a note may have at most one active reminder at a time,
     * regardless of type.
     */
    public boolean canAddReminder(UUID noteId, ReminderType type) {
        if (noteId == null) {
            return false;
        }

        Reminder existing = noteRepository.getReminderForNote(noteId);
        return existing == null || !existing.isActive();
    }

    /**
     * Convenience method for the UI to fetch the current reminder (if any).
     */
    public Reminder getReminderForNote(UUID noteId) {
        if (noteId == null) {
            return null;
        }
        return noteRepository.getReminderForNote(noteId);
    }

    /**
     * Create and store a time-based reminder, and schedule an OS alarm.
     */
    public Reminder createTimeReminder(UUID noteId, Date triggerTime) {
        if (noteId == null || triggerTime == null) {
            return null;
        }

        Note note = noteRepository.getNote(noteId);
        if (note == null) {
            return null;
        }

        // Clear any existing reminder for this note first.
        removeRemindersForNote(noteId);

        Reminder reminder = new Reminder(noteId, ReminderType.TIME);
        reminder.setTriggerTime(triggerTime);
        noteRepository.insertReminder(reminder);

        // Attach reminder to note so we can look it up quickly later.
        note.setReminderId(reminder.getId());
        noteRepository.updateNote(note);

        timeReminderService.scheduleTimeReminder(
                noteId,
                note.getTitle(),
                reminder
        );

        return reminder;
    }

    /**
     * Create and store a location-based reminder, and register a geofence.
     */
    public Reminder createLocationReminder(UUID noteId,
                                           double lat,
                                           double lng,
                                           float radiusMeters) {
        if (noteId == null) {
            return null;
        }

        Note note = noteRepository.getNote(noteId);
        if (note == null) {
            return null;
        }

        // Clear any existing reminder for this note first.
        removeRemindersForNote(noteId);

        Reminder reminder = new Reminder(noteId, ReminderType.LOCATION);
        reminder.setLocationLat(lat);
        reminder.setLocationLng(lng);
        reminder.setRadiusMeters(radiusMeters);
        noteRepository.insertReminder(reminder);

        // Attach reminder to note so we can look it up quickly later.
        note.setReminderId(reminder.getId());
        noteRepository.updateNote(note);

        // Register geofence with Play Services.
        locationReminderService.registerGeofenceForReminder(reminder);

        return reminder;
    }

    /**
     * Remove any reminder(s) for this note:
     *  - Cancel OS alarms/geofences
     *  - Delete Reminder row
     *  - Clear reminderId on the Note
     *
     * Used when:
     *  - User clears reminder from the UI
     *  - Note is deleted
     */
    public void removeRemindersForNote(UUID noteId) {
        if (noteId == null) {
            return;
        }

        Reminder existing = noteRepository.getReminderForNote(noteId);
        if (existing == null) {
            return;
        }

        // Cancel underlying OS integration.
        if (existing.getType() == ReminderType.TIME) {
            timeReminderService.cancelTimeReminder(existing);
        } else if (existing.getType() == ReminderType.LOCATION) {
            locationReminderService.removeGeofenceForReminder(existing);
        }

        noteRepository.deleteReminder(existing);

        // Clear reminderId on the note, if it still exists.
        Note note = noteRepository.getNote(noteId);
        if (note != null) {
            note.setReminderId(null);
            noteRepository.updateNote(note);
        }
    }

    /**
     * Mark this note's reminder (if any) as retired.
     * Used when a time-based reminder alarm fires or a geofence fires.
     */
    public void retireReminderForNote(UUID noteId) {
        if (noteId == null) {
            return;
        }

        Reminder existing = noteRepository.getReminderForNote(noteId);
        if (existing == null) {
            return;
        }

        // Cancel the OS-level alarm/geofence
        if (existing.getType() == ReminderType.TIME) {
            timeReminderService.cancelTimeReminder(existing);
        } else if (existing.getType() == ReminderType.LOCATION) {
            locationReminderService.removeGeofenceForReminder(existing);
        }

        if (existing.isActive()) {
            existing.markRetired();
            noteRepository.updateReminder(existing);
        }
    }

    // Convenience wrappers that match the design document naming

    /**
     * Called by ReminderReceiver when a time-based alarm fires.
     */
    public void handleTimeReminderFired(UUID noteId) {
        retireReminderForNote(noteId);
    }

    /**
     * Called by GeofenceReceiver when a location-based geofence transition fires.
     */
    public void handleGeofenceEvent(UUID noteId) {
        retireReminderForNote(noteId);
    }
}

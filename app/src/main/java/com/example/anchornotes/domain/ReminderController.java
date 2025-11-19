package com.example.anchornotes.domain;

import android.content.Context;
import android.location.Location;

import com.example.anchornotes.data.LocationProviderService;
import com.example.anchornotes.data.Reminder;
import com.example.anchornotes.data.ReminderType;

import java.util.Date;
import java.util.UUID;

/**
 * Presentation-layer controller for reminder-related flows.
 * Activities/Fragments should talk to this instead of directly to
 * ReminderManager or location services.
 */
public class ReminderController {

    public enum LocationError {
        NONE,
        PERMISSION_MISSING,
        PROVIDER_DISABLED,
        LOCATION_UNAVAILABLE,
        NOTE_ALREADY_HAS_ACTIVE_REMINDER
    }

    public static class LocationReminderResult {
        public final boolean success;
        public final LocationError error;

        public LocationReminderResult(boolean success, LocationError error) {
            this.success = success;
            this.error = error;
        }

        public static LocationReminderResult ok() {
            return new LocationReminderResult(true, LocationError.NONE);
        }

        public static LocationReminderResult fail(LocationError error) {
            return new LocationReminderResult(false, error);
        }
    }

    private final ReminderManager reminderManager;
    private final LocationProviderService locationProviderService;

    public ReminderController(Context context) {
        this.reminderManager = new ReminderManager(context);
        this.locationProviderService = new LocationProviderService(context);
    }

    // ------------------------------------------------------------------------
    // TIME-BASED REMINDERS
    // ------------------------------------------------------------------------

    /**
     * Add a time-based reminder "minutesFromNow" minutes from the current time.
     */
    public boolean addTimeReminderMinutesFromNow(UUID noteId, int minutesFromNow) {
        long now = System.currentTimeMillis();
        long triggerMillis = now + minutesFromNow * 60L * 1000L;
        Date triggerTime = new Date(triggerMillis);
        return addTimeReminderAt(noteId, triggerTime);
    }

    /**
     * Add a time-based reminder at an exact date/time.
     */
    public boolean addTimeReminderAt(UUID noteId, Date triggerTime) {
        if (noteId == null || triggerTime == null) {
            return false;
        }

        // Business rule: note may have at most one active reminder of any type.
        if (!reminderManager.canAddReminder(noteId, ReminderType.TIME)) {
            return false;
        }

        Reminder reminder = reminderManager.createTimeReminder(noteId, triggerTime);
        return (reminder != null);
    }

    /**
     * Clear any reminder attached to this note.
     */
    public void clearReminder(UUID noteId) {
        if (noteId == null) return;
        reminderManager.removeRemindersForNote(noteId);
    }

    // ------------------------------------------------------------------------
    // LOCATION-BASED REMINDERS
    // ------------------------------------------------------------------------

    /**
     * Add a location-based reminder using the device's current last known location.
     */
    public LocationReminderResult addLocationReminderWithCurrentLocation(
            UUID noteId,
            float radiusMeters
    ) {
        if (noteId == null) {
            return LocationReminderResult.fail(LocationError.LOCATION_UNAVAILABLE);
        }

        if (!reminderManager.canAddReminder(noteId, ReminderType.LOCATION)) {
            return LocationReminderResult.fail(LocationError.NOTE_ALREADY_HAS_ACTIVE_REMINDER);
        }

        LocationProviderService.LocationError availability =
                locationProviderService.getAvailabilityStatus();

        if (availability == LocationProviderService.LocationError.PERMISSION_MISSING) {
            return LocationReminderResult.fail(LocationError.PERMISSION_MISSING);
        }

        if (availability == LocationProviderService.LocationError.PROVIDER_DISABLED) {
            return LocationReminderResult.fail(LocationError.PROVIDER_DISABLED);
        }

        Location loc = locationProviderService.getLastKnownLocation();
        if (loc == null) {
            return LocationReminderResult.fail(LocationError.LOCATION_UNAVAILABLE);
        }

        reminderManager.createLocationReminder(
                noteId,
                loc.getLatitude(),
                loc.getLongitude(),
                radiusMeters
        );

        return LocationReminderResult.ok();
    }

    /**
     * Add a location-based reminder using explicit coordinates (from map picker).
     */
    public LocationReminderResult addLocationReminderWithCoordinates(
            UUID noteId,
            double lat,
            double lng,
            float radiusMeters
    ) {
        if (noteId == null) {
            return LocationReminderResult.fail(LocationError.LOCATION_UNAVAILABLE);
        }

        if (!reminderManager.canAddReminder(noteId, ReminderType.LOCATION)) {
            return LocationReminderResult.fail(LocationError.NOTE_ALREADY_HAS_ACTIVE_REMINDER);
        }

        reminderManager.createLocationReminder(
                noteId,
                lat,
                lng,
                radiusMeters
        );

        return LocationReminderResult.ok();
    }
}

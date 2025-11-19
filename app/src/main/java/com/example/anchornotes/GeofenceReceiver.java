package com.example.anchornotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.NoteRepository;
import com.example.anchornotes.data.NotificationService;
import com.example.anchornotes.domain.ReminderManager;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;
import java.util.UUID;

/**
 * Receives geofence transition events for location-based reminders.
 * Delegates to ReminderManager and shows a notification via NotificationService.
 */
public class GeofenceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if (event == null || event.hasError()) {
            return;
        }

        int transition = event.getGeofenceTransition();
        if (transition != Geofence.GEOFENCE_TRANSITION_ENTER
                && transition != Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Only handle ENTER/EXIT for now
            return;
        }

        List<Geofence> triggering = event.getTriggeringGeofences();
        if (triggering == null || triggering.isEmpty()) {
            return;
        }

        // For simplicity, handle just the first triggered geofence
        Geofence geofence = triggering.get(0);
        String requestId = geofence.getRequestId();

        // Our LocationReminderService encodes requestId as "reminder_<noteUUID>"
        UUID noteIdFromReminder = parseNoteIdFromRequestId(context, requestId);
        if (noteIdFromReminder == null) {
            return;
        }

        // Let the domain layer handle this location-based reminder event
        ReminderManager reminderManager = new ReminderManager(context);
        // <-- changed to domain-level handler per design
        reminderManager.handleGeofenceEvent(noteIdFromReminder);

        // Look up the note title for notification display
        NoteRepository repo = new NoteRepository(context.getApplicationContext());
        Note note = repo.getNote(noteIdFromReminder);

        String noteTitle = (note != null && note.getTitle() != null)
                ? note.getTitle()
                : "Location reminder";

        // Show notification via NotificationService
        NotificationService notificationService = new NotificationService(context);
        notificationService.showReminderNotification(
                noteIdFromReminder,
                noteTitle,
                "Location-based reminder for this note"
        );
    }

    /**
     * Parse the noteId from our geofence request ID.
     * Currently, LocationReminderService uses "reminder_<noteUUID>" as requestId.
     */
    private UUID parseNoteIdFromRequestId(Context context, String requestId) {
        if (requestId == null) return null;

        final String prefix = "reminder_";
        if (!requestId.startsWith(prefix)) {
            return null;
        }

        String uuidStr = requestId.substring(prefix.length());
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

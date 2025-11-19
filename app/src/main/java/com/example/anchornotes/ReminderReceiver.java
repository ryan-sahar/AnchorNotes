package com.example.anchornotes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.anchornotes.data.NotificationService;
import com.example.anchornotes.domain.ReminderManager;

import java.util.UUID;

/**
 * Receives time-based reminder alarms.
 * Delegates to ReminderManager and shows a notification via NotificationService.
 */
public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTE_ID = "extra_note_id";
    public static final String EXTRA_NOTE_TITLE = "extra_note_title";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        // Extract note info from the alarm intent
        String noteIdStr = intent.getStringExtra(EXTRA_NOTE_ID);
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);

        UUID noteId = null;
        if (noteIdStr != null) {
            try {
                noteId = UUID.fromString(noteIdStr);
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Let the domain layer handle this time-based reminder event
        if (noteId != null) {
            ReminderManager reminderManager = new ReminderManager(context);
            // <-- changed to domain-level handler per design
            reminderManager.handleTimeReminderFired(noteId);
        }

        // Show notification via NotificationService
        NotificationService notificationService = new NotificationService(context);
        notificationService.showReminderNotification(
                noteId,
                noteTitle,
                "Reminder for this note"
        );
    }
}

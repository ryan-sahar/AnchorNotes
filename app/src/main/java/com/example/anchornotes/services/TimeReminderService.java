package com.example.anchornotes.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.anchornotes.ReminderReceiver;
import com.example.anchornotes.data.Reminder;

import java.util.Date;
import java.util.UUID;

/**
 * Wrapper around AlarmManager for time-based note reminders.
 *
 * Lives in the services layer and only knows how to talk to the OS.
 * The domain layer (ReminderManager) decides when a reminder should fire.
 */
public class TimeReminderService {

    private final Context appContext;
    private final AlarmManager alarmManager;

    public TimeReminderService(Context context) {
        this.appContext = context.getApplicationContext();
        this.alarmManager = (AlarmManager) appContext.getSystemService(Context.ALARM_SERVICE);
    }

    /**
     * Schedule an alarm for the given reminder.
     *
     * @param noteId    ID of the note this reminder belongs to
     * @param noteTitle Title of the note (for display in the notification)
     * @param reminder  Reminder entity with triggerTime and id
     */
    public void scheduleTimeReminder(UUID noteId, String noteTitle, Reminder reminder) {
        if (alarmManager == null || reminder == null) {
            return;
        }

        Date trigger = reminder.getTriggerTime();
        if (trigger == null) {
            return;
        }

        PendingIntent pendingIntent = buildPendingIntent(noteId, noteTitle, reminder);
        long triggerAtMillis = trigger.getTime();

        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
        );
    }

    /**
     * Cancel a previously scheduled alarm, if any.
     */
    public void cancelTimeReminder(Reminder reminder) {
        if (alarmManager == null || reminder == null) {
            return;
        }

        PendingIntent pendingIntent = buildPendingIntent(null, null, reminder);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * Build the PendingIntent used to schedule and cancel alarms
     * for a particular Reminder.
     */
    private PendingIntent buildPendingIntent(UUID noteId, String noteTitle, Reminder reminder) {
        Intent intent = new Intent(appContext, ReminderReceiver.class);

        if (noteId != null) {
            intent.putExtra(ReminderReceiver.EXTRA_NOTE_ID, noteId.toString());
        }
        if (noteTitle != null) {
            intent.putExtra(ReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        }

        int requestCode = (reminder.getId() != null)
                ? reminder.getId().hashCode()
                : 0;

        return PendingIntent.getBroadcast(
                appContext,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}

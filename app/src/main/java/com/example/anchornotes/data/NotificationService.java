package com.example.anchornotes.data;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.anchornotes.NoteDetailActivity;
import com.example.anchornotes.R;

import java.util.UUID;

/**
 * Service wrapper around NotificationManager.
 * Central place to build and show reminder notifications.
 */
public class NotificationService {

    public static final String CHANNEL_ID = "anchor_notes_reminders";

    private final Context appContext;

    public NotificationService(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Show a reminder notification for a note.
     *
     * @param noteId     UUID of the note (may be null)
     * @param noteTitle  Title to show (fallback if null/empty)
     * @param message    Body text for the notification
     */
    public void showReminderNotification(@Nullable UUID noteId,
                                         @Nullable String noteTitle,
                                         String message) {
        // Android 13+: respect POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        createNotificationChannelIfNeeded();

        String safeTitle = (noteTitle == null || noteTitle.isEmpty())
                ? "AnchorNotes Reminder"
                : noteTitle;

        String noteIdStr = (noteId != null) ? noteId.toString() : null;

        // When user taps the notification, open the note in NoteDetailActivity
        Intent openIntent = new Intent(appContext, NoteDetailActivity.class);
        openIntent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, noteIdStr);

        PendingIntent contentIntent = PendingIntent.getActivity(
                appContext,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(appContext, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(safeTitle)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(contentIntent);

        NotificationManagerCompat nm = NotificationManagerCompat.from(appContext);
        int notificationId =
                (noteId != null) ? noteId.hashCode() : (int) System.currentTimeMillis();
        nm.notify(notificationId, builder.build());
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager nm =
                (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }

        NotificationChannel existing = nm.getNotificationChannel(CHANNEL_ID);
        if (existing != null) {
            return;
        }

        String name = "AnchorNotes Reminders";
        String description = "Notifications for note reminders";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel channel =
                new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        nm.createNotificationChannel(channel);
    }
}
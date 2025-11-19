package com.example.anchornotes.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.example.anchornotes.GeofenceReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collections;
import java.util.UUID;

/**
 * Wrapper around GeofencingClient for location-based reminders.
 * The domain layer (ReminderManager) calls this; it doesn't know
 * about the OS geofencing details.
 */
public class LocationReminderService {

    private final Context appContext;
    private final GeofencingClient geofencingClient;

    private static final String GEOFENCE_REQUEST_ID_PREFIX = "reminder_";

    public LocationReminderService(Context context) {
        this.appContext = context.getApplicationContext();
        this.geofencingClient = LocationServices.getGeofencingClient(appContext);
    }

    /**
     * Register a geofence for the given location-based Reminder.
     * Callers must ensure location permission is already granted.
     */
    public void registerGeofenceForReminder(Reminder reminder) {
        if (reminder == null
                || reminder.getId() == null
                || reminder.getLocationLat() == null
                || reminder.getLocationLng() == null
                || reminder.getRadiusMeters() == null) {
            return;
        }

        String requestId = buildRequestId(reminder.getId());

        Geofence geofence = new Geofence.Builder()
                .setRequestId(requestId)
                .setCircularRegion(
                        reminder.getLocationLat(),
                        reminder.getLocationLng(),
                        reminder.getRadiusMeters()
                )
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                        Geofence.GEOFENCE_TRANSITION_ENTER
                                | Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build();

        GeofencingRequest request = new GeofencingRequest.Builder()
                // Fire immediately if already inside
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        PendingIntent pendingIntent = getGeofencePendingIntent();

        try {
            // Caller is responsible for having location permission.
            geofencingClient.addGeofences(request, pendingIntent);
        } catch (SecurityException ignored) {
            // If we don't have permission, just fail silently for now.
        }
    }

    /**
     * Remove the geofence associated with this reminder.
     */
    public void removeGeofenceForReminder(Reminder reminder) {
        if (reminder == null || reminder.getId() == null) {
            return;
        }
        String requestId = buildRequestId(reminder.getId());
        geofencingClient.removeGeofences(Collections.singletonList(requestId));
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(appContext, GeofenceReceiver.class);
        return PendingIntent.getBroadcast(
                appContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public static String buildRequestId(UUID reminderId) {
        return GEOFENCE_REQUEST_ID_PREFIX + reminderId.toString();
    }

    public static UUID parseReminderIdFromRequestId(String requestId) {
        if (requestId == null) return null;
        if (!requestId.startsWith(GEOFENCE_REQUEST_ID_PREFIX)) return null;

        String uuidStr = requestId.substring(GEOFENCE_REQUEST_ID_PREFIX.length());
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}

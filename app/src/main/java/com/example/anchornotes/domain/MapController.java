package com.example.anchornotes.domain;

import android.content.Context;
import android.location.Location;

import com.example.anchornotes.data.LocationProviderService;

/**
 * Presentation-layer controller for the MapPicker screen.
 * Responsibilities:
 *  - Ask the location provider for the current location to center the map.
 *  - Handle "user tapped on map" events and create a location reminder.
 *  - Notify the view (Activity) about success or errors via Listener.
 */
public class MapController {

    public interface Listener {
        /** Center the map on the given coordinates with a suggested zoom level. */
        void centerMap(double lat, double lng, double zoom);

        /** Show a generic "location unavailable" message in the UI. */
        void showLocationUnavailableMessage();

        /** Called when the location reminder has been created successfully. */
        void onLocationReminderCreated();

        /** Show an error message in the UI. */
        void showError(String message);
    }

    // Default geofence radius (in meters) when user taps on the map.
    // This matches the “fixed-radius” idea from the design.
    private static final float DEFAULT_RADIUS_METERS = 100.0f;

    private final Listener listener;
    private final ReminderController reminderController;
    private final LocationProviderService locationProviderService;

    // Note the reminder will be attached to
    private final java.util.UUID noteId;

    public MapController(Context context,
                         java.util.UUID noteId,
                         ReminderController reminderController,
                         Listener listener) {
        Context app = context.getApplicationContext();
        this.noteId = noteId;
        this.reminderController = reminderController;
        this.locationProviderService = new LocationProviderService(app);
        this.listener = listener;
    }

    /**
     * Initialize the map: try to center on the user's last known location.
     * If unavailable, notify the view so it can show a message.
     */
    public void initialize() {
        Location last = locationProviderService.getLastKnownLocation();
        if (last != null && listener != null) {
            listener.centerMap(last.getLatitude(), last.getLongitude(), 16.0);
        } else if (listener != null) {
            listener.showLocationUnavailableMessage();
        }
    }

    /**
     * User tapped on the map at the given coordinates.
     * Try to create a location reminder for this note.
     */
    public void onUserTappedLocation(double latitude, double longitude) {
        if (noteId == null) {
            if (listener != null) {
                listener.showError("Cannot add location reminder: note not found.");
            }
            return;
        }

        // Delegate to ReminderController (domain/presentation bridge).
        ReminderController.LocationReminderResult result =
                reminderController.addLocationReminderWithCoordinates(
                        noteId,
                        latitude,
                        longitude,
                        DEFAULT_RADIUS_METERS
                );

        if (result == null) {
            if (listener != null) {
                listener.showError("Failed to create location reminder.");
            }
            return;
        }

        // Any non-null result is treated as success here; finer-grained
        // error mapping (e.g., PERMISSION_MISSING) can be added later.
        if (listener != null) {
            listener.onLocationReminderCreated();
        }
    }
}
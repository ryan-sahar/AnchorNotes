package com.example.anchornotes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.anchornotes.domain.MapController;
import com.example.anchornotes.domain.ReminderController;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.events.MapEventsReceiver;

/**
 * MapPickerActivity: UI for picking a location for a location-based reminder.
 *
 * Now delegates business logic to MapController to better match the design doc:
 *  - MapController handles initial centering and reminder creation.
 *  - This Activity just displays the map and responds to controller callbacks.
 */
public class MapPickerActivity extends AppCompatActivity implements MapController.Listener {

    public static final String EXTRA_NOTE_ID = "note_id";

    private MapView mapView;
    private Marker currentMarker;

    private java.util.UUID noteId;
    private MapController mapController;
    private ReminderController reminderController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        // Read noteId from Intent
        String idString = getIntent().getStringExtra(EXTRA_NOTE_ID);
        if (idString == null) {
            Toast.makeText(this, "Missing note id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        noteId = java.util.UUID.fromString(idString);

        // Initialize osmdroid config
        Configuration.getInstance().setUserAgentValue(getPackageName());

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        // Center to a default value first (in case we can't get current location)
        mapView.getController().setZoom(3.0);
        mapView.getController().setCenter(new GeoPoint(0.0, 0.0));

        // Domain/controller objects
        reminderController = new ReminderController(this);
        mapController = new MapController(this, noteId, reminderController, this);

        // Ask controller to initialize (center on last known location if available)
        mapController.initialize();

        // Add tap listener for picking a point
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                onMapTapped(p);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                // We only care about single tap; ignore long press.
                return false;
            }
        });

        mapView.getOverlays().add(mapEventsOverlay);
    }

    // ------------------------------------------------------------------------
    // Map tap handling
    // ------------------------------------------------------------------------

    private void onMapTapped(GeoPoint p) {
        // Place or move a marker at the tapped location
        if (currentMarker == null) {
            currentMarker = new Marker(mapView);
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(currentMarker);
        }
        currentMarker.setPosition(p);
        currentMarker.setTitle("Reminder here");
        currentMarker.showInfoWindow();

        // Forward to controller to create the reminder
        mapController.onUserTappedLocation(p.getLatitude(), p.getLongitude());

        mapView.invalidate();
    }

    // ------------------------------------------------------------------------
    // MapController.Listener callbacks
    // ------------------------------------------------------------------------

    @Override
    public void centerMap(double lat, double lng, double zoom) {
        if (mapView == null) return;
        mapView.getController().setZoom(zoom);
        mapView.getController().setCenter(new GeoPoint(lat, lng));
    }

    @Override
    public void showLocationUnavailableMessage() {
        Toast.makeText(this,
                "Current location unavailable. You can still tap anywhere to pick a location.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationReminderCreated() {
        Toast.makeText(this, "Location reminder set.", Toast.LENGTH_SHORT).show();

        // Optionally return RESULT_OK to the caller, even if they don't handle it yet.
        Intent data = new Intent();
        setResult(RESULT_OK, data);

        finish();
    }

    @Override
    public void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message != null ? message : "Unknown error")
                .setPositiveButton("OK", null)
                .show();
    }
}

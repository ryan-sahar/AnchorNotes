package com.example.anchornotes;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.anchornotes.data.LocationProviderService;
import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.Reminder;
import com.example.anchornotes.data.ReminderType;
import com.example.anchornotes.data.Tag;
import com.example.anchornotes.domain.NoteDetailController;
import com.example.anchornotes.domain.NoteManager;
import com.example.anchornotes.domain.ReminderManager;
import com.example.anchornotes.domain.TagManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class NoteDetailActivity extends AppCompatActivity implements NoteDetailController.Listener {

    public static final String EXTRA_NOTE_ID = "note_id";
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private EditText edtTitle;
    private EditText edtContent;
    private TextView txtReminderInfo;
    private TextView txtTags;
    private TextView txtNoteLocation;

    private Button btnSave;
    private Button btnDelete;
    private Button btnAddTimeReminder;
    private Button btnAddLocationReminder;
    private Button btnClearReminder;
    private Button btnEditTags;
    private Button btnUpdateNoteLocation;
    private Button btnClearNoteLocation;
    private Button btnViewNoteLocation;

    private UUID noteId = null;
    private Note currentNote;

    private NoteDetailController controller;
    private ReminderManager reminderManager;
    private TagManager tagManager;
    private NoteManager noteManager;
    private LocationProviderService locationProviderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        controller = new NoteDetailController(this, this);
        reminderManager = new ReminderManager(this);
        tagManager = new TagManager(this);
        noteManager = new NoteManager(this);
        locationProviderService = new LocationProviderService(this);

        // UI
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        txtReminderInfo = findViewById(R.id.txtReminderInfo);
        txtTags = findViewById(R.id.txtTags);
        txtNoteLocation = findViewById(R.id.txtNoteLocation);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnAddTimeReminder = findViewById(R.id.btnAddTimeReminder);
        btnAddLocationReminder = findViewById(R.id.btnAddLocationReminder);
        btnClearReminder = findViewById(R.id.btnClearReminder);
        btnEditTags = findViewById(R.id.btnEditTags);
        btnUpdateNoteLocation = findViewById(R.id.btnUpdateNoteLocation);
        btnClearNoteLocation = findViewById(R.id.btnClearNoteLocation);
        btnViewNoteLocation = findViewById(R.id.btnViewNoteLocation);

        // Check if editing existing note
        String idString = getIntent().getStringExtra(EXTRA_NOTE_ID);
        if (idString != null) {
            noteId = UUID.fromString(idString);
            controller.loadExistingNote(noteId);
        } else {
            btnDelete.setEnabled(false); // Can't delete a new note
            txtReminderInfo.setText("No reminder set");
            txtTags.setText("Tags: none");
            txtNoteLocation.setText("Location: none");
        }

        // Save button
        btnSave.setOnClickListener(v -> controller.saveNote(
                noteId,
                edtTitle.getText().toString(),
                edtContent.getText().toString()
        ));

        // Delete
        btnDelete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Delete note?")
                .setMessage("This will permanently remove the note and any reminders.")
                .setPositiveButton("Delete", (d, w) -> controller.deleteNote(noteId))
                .setNegativeButton("Cancel", null)
                .show()
        );

        // Add time reminder
        btnAddTimeReminder.setOnClickListener(v -> controller.onAddTimeReminder(noteId));

        // Add location reminder (geofence)
        btnAddLocationReminder.setOnClickListener(v -> controller.onAddLocationReminder(noteId));

        // Clear reminder
        btnClearReminder.setOnClickListener(v -> {
            if (noteId != null) {
                reminderManager.removeRemindersForNote(noteId);
                txtReminderInfo.setText("No reminder set");
            } else {
                showError("Please save the note before clearing reminders.");
            }
        });

        // Edit tags
        btnEditTags.setOnClickListener(v -> {
            if (noteId == null) {
                showError("Please save the note before editing tags.");
            } else {
                showEditTagsDialog();
            }
        });

        // NOTE LOCATION BUTTONS
        btnUpdateNoteLocation.setOnClickListener(v -> updateNoteLocationToCurrent());
        btnClearNoteLocation.setOnClickListener(v -> clearNoteLocation());
        btnViewNoteLocation.setOnClickListener(v -> viewNoteLocationOnMap());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When returning from ReminderSetupActivity or MapPickerActivity,
        // reload the note + reminder to update UI.
        if (noteId != null) {
            controller.loadExistingNote(noteId);
            refreshTags();
            // location will be refreshed in onNoteLoaded()
        }
    }

    // -------------------------------------------------------------------------
    // Controller Callbacks
    // -------------------------------------------------------------------------

    @Override
    public void onNoteLoaded(Note note, Reminder reminder) {
        if (note == null) return;

        // Track the id and current note
        noteId = note.getId();
        currentNote = note;

        edtTitle.setText(note.getTitle());
        edtContent.setText(note.getContent());

        if (reminder == null || !reminder.isActive()) {
            txtReminderInfo.setText("No reminder set");
        } else if (reminder.getType() == ReminderType.TIME) {
            txtReminderInfo.setText("Time reminder: " + reminder.getTriggerTime());
        } else if (reminder.getType() == ReminderType.LOCATION) {
            txtReminderInfo.setText("Location reminder set");
        } else {
            txtReminderInfo.setText("Reminder set");
        }

        // Update tags + location display
        refreshTags();
        updateLocationUI();
    }

    @Override
    public void onNoteSaved() {
        // After saving a new note, just finish back to the list.
        finish();
    }

    @Override
    public void onNoteDeleted() {
        finish();
    }

    @Override
    public void navigateToTimeReminder(UUID noteId) {
        Intent intent = new Intent(this, ReminderSetupActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, noteId.toString());
        startActivity(intent);
    }

    @Override
    public void navigateToLocationReminder(UUID noteId) {
        Intent intent = new Intent(this, MapPickerActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, noteId.toString());
        startActivity(intent);
    }

    @Override
    public void showError(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message != null ? message : "Unknown error")
                .setPositiveButton("OK", null)
                .show();
    }

    // -------------------------------------------------------------------------
    // Tags helpers
    // -------------------------------------------------------------------------

    private void refreshTags() {
        if (noteId == null) {
            txtTags.setText("Tags: none");
            return;
        }

        List<Tag> tags = tagManager.getTagsForNote(noteId);
        if (tags == null || tags.isEmpty()) {
            txtTags.setText("Tags: none");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(tags.get(i).getName());
        }
        txtTags.setText("Tags: " + sb.toString());
    }

    private void showEditTagsDialog() {
        // Load all tags and the ones currently attached to this note
        List<Tag> allTags = tagManager.getAllTags();
        List<Tag> noteTags = tagManager.getTagsForNote(noteId);

        if (allTags == null) {
            allTags = new ArrayList<>();
        }
        if (noteTags == null) {
            noteTags = new ArrayList<>();
        }

        // If there are no tags, prompt user to create one first
        if (allTags.isEmpty()) {
            showCreateTagDialog(this::showEditTagsDialog);
            return;
        }

        String[] names = new String[allTags.size()];
        boolean[] checked = new boolean[allTags.size()];

        // Pre-check tags that are already on this note
        Set<UUID> noteTagIds = new HashSet<>();
        for (Tag t : noteTags) {
            noteTagIds.add(t.getId());
        }

        for (int i = 0; i < allTags.size(); i++) {
            Tag t = allTags.get(i);
            names[i] = t.getName();
            checked[i] = noteTagIds.contains(t.getId());
        }

        // Make effectively-final copies for listeners
        final List<Tag> allTagsFinal = allTags;
        final boolean[] checkedFinal = checked;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Tags");

        builder.setMultiChoiceItems(names, checkedFinal,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedFinal[which] = isChecked;
                    }
                });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<UUID> selectedIds = new ArrayList<>();
                for (int i = 0; i < allTagsFinal.size(); i++) {
                    if (checkedFinal[i]) {
                        selectedIds.add(allTagsFinal.get(i).getId());
                    }
                }
                tagManager.setTagsForNote(noteId, selectedIds);
                refreshTags();
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.setNeutralButton("New Tag", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Create a new tag, then reopen the edit dialog
                showCreateTagDialog(NoteDetailActivity.this::showEditTagsDialog);
            }
        });

        builder.show();
    }

    private interface TagCreatedCallback {
        void onTagCreated();
    }

    private void showCreateTagDialog(TagCreatedCallback cb) {
        final EditText input = new EditText(this);
        input.setHint("Tag name");

        new AlertDialog.Builder(this)
                .setTitle("New Tag")
                .setView(input)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = input.getText().toString();
                        Tag created = tagManager.createTag(name);
                        if (created != null && cb != null) {
                            cb.onTagCreated();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // -------------------------------------------------------------------------
    // Note location helpers
    // -------------------------------------------------------------------------

    private void updateLocationUI() {
        if (currentNote == null
                || currentNote.getLocationLat() == null
                || currentNote.getLocationLng() == null) {
            txtNoteLocation.setText("Location: none");
            return;
        }

        double lat = currentNote.getLocationLat();
        double lng = currentNote.getLocationLng();
        txtNoteLocation.setText(String.format("Location: %.5f, %.5f", lat, lng));
    }

    private void updateNoteLocationToCurrent() {
        if (noteId == null || currentNote == null) {
            showError("Please save the note before attaching a location.");
            return;
        }

        // Ask the service what the situation is
        LocationProviderService.LocationError status = locationProviderService.getAvailabilityStatus();

        if (status == LocationProviderService.LocationError.PERMISSION_MISSING) {
            // Request runtime permission instead of just failing
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
            return;
        } else if (status == LocationProviderService.LocationError.PROVIDER_DISABLED) {
            showError("Location services are disabled. Please turn on location.");
            return;
        }

        Location loc = locationProviderService.getLastKnownLocation();
        if (loc == null) {
            showError("Could not get your current location.");
            return;
        }

        currentNote.setLocationLat(loc.getLatitude());
        currentNote.setLocationLng(loc.getLongitude());
        noteManager.updateNote(currentNote);
        updateLocationUI();
    }

    private void clearNoteLocation() {
        if (noteId == null || currentNote == null
                || currentNote.getLocationLat() == null
                || currentNote.getLocationLng() == null) {
            showError("This note does not have a location attached.");
            return;
        }

        currentNote.setLocationLat(null);
        currentNote.setLocationLng(null);
        noteManager.updateNote(currentNote);
        updateLocationUI();
    }

    private void viewNoteLocationOnMap() {
        if (currentNote == null
                || currentNote.getLocationLat() == null
                || currentNote.getLocationLng() == null) {
            showError("This note does not have a location attached.");
            return;
        }

        double lat = currentNote.getLocationLat();
        double lng = currentNote.getLocationLng();

        // Open Google Maps in the browser instead of the Maps app
        String url = "https://www.google.com/maps/search/?api=1&query=" + lat + "," + lng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            showError("No browser available to show this location.");
        }
    }

    // -------------------------------------------------------------------------
    // Permission result
    // -------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Try again now that we have permission
                updateNoteLocationToCurrent();
            } else {
                showError("Location permission denied. Cannot attach note location.");
            }
        }
    }
}

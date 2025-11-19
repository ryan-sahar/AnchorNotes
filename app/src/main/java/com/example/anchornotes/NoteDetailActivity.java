package com.example.anchornotes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.Reminder;
import com.example.anchornotes.data.ReminderType;
import com.example.anchornotes.domain.NoteDetailController;
import com.example.anchornotes.domain.ReminderManager;

import java.util.UUID;

public class NoteDetailActivity extends AppCompatActivity implements NoteDetailController.Listener {

    public static final String EXTRA_NOTE_ID = "note_id";

    private EditText edtTitle;
    private EditText edtContent;
    private TextView txtReminderInfo;

    private Button btnSave;
    private Button btnDelete;
    private Button btnAddTimeReminder;
    private Button btnAddLocationReminder;
    private Button btnClearReminder;

    private UUID noteId = null;
    private NoteDetailController controller;
    private ReminderManager reminderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        controller = new NoteDetailController(this, this);
        reminderManager = new ReminderManager(this);

        // UI
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        txtReminderInfo = findViewById(R.id.txtReminderInfo);

        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnAddTimeReminder = findViewById(R.id.btnAddTimeReminder);
        btnAddLocationReminder = findViewById(R.id.btnAddLocationReminder);
        btnClearReminder = findViewById(R.id.btnClearReminder);

        // Check if editing existing note
        String idString = getIntent().getStringExtra(EXTRA_NOTE_ID);
        if (idString != null) {
            noteId = UUID.fromString(idString);
            controller.loadExistingNote(noteId);
        } else {
            btnDelete.setEnabled(false); // Can't delete a new note
            txtReminderInfo.setText("No reminder set");
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

        // Add location reminder
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When returning from ReminderSetupActivity or MapPickerActivity,
        // reload the note + reminder to update txtReminderInfo.
        if (noteId != null) {
            controller.loadExistingNote(noteId);
        }
    }

    // -------------------------------------------------------------------------
    // Controller Callbacks
    // -------------------------------------------------------------------------

    @Override
    public void onNoteLoaded(Note note, Reminder reminder) {
        if (note == null) return;

        // Track the id in case we were launched without one (defensive)
        noteId = note.getId();

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
    }

    @Override
    public void onNoteSaved() {
        // After saving a new note, we could finish and rely on the list to reopen it,
        // but for simplicity we just finish back to the list.
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
}

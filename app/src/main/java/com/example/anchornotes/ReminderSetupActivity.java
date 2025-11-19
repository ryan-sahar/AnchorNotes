package com.example.anchornotes;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.anchornotes.domain.ReminderController;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * ReminderSetupScreen:
 * Lets the user choose when a time-based reminder should fire.
 * - Quick presets: 5 min, 30 min, 1 hour from now
 * - Custom date/time picker
 *
 * Now directly uses ReminderController to create the reminder for a given note.
 */
public class ReminderSetupActivity extends AppCompatActivity {

    // Kept for compatibility, but we no longer rely on passing this back.
    public static final String EXTRA_TRIGGER_TIME_MILLIS = "extra_trigger_time_millis";

    private Button btnIn5Min;
    private Button btnIn30Min;
    private Button btnIn1Hour;
    private Button btnCustom;
    private Button btnCancel;

    private ReminderController reminderController;
    private UUID noteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_setup);

        // Read noteId from the launching intent (sent by NoteDetailActivity)
        String idString = getIntent().getStringExtra(NoteDetailActivity.EXTRA_NOTE_ID);
        if (idString == null) {
            Toast.makeText(this, "Missing note id for reminder", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        noteId = UUID.fromString(idString);

        reminderController = new ReminderController(this);

        btnIn5Min = findViewById(R.id.btnIn5Min);
        btnIn30Min = findViewById(R.id.btnIn30Min);
        btnIn1Hour = findViewById(R.id.btnIn1Hour);
        btnCustom = findViewById(R.id.btnCustomTime);
        btnCancel = findViewById(R.id.btnCancelReminderSetup);

        btnIn5Min.setOnClickListener(v -> finishWithOffsetMinutes(5));
        btnIn30Min.setOnClickListener(v -> finishWithOffsetMinutes(30));
        btnIn1Hour.setOnClickListener(v -> finishWithOffsetMinutes(60));

        btnCustom.setOnClickListener(v -> showCustomDateTimePicker());

        btnCancel.setOnClickListener(v -> {
            Toast.makeText(this, "Reminder not changed", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void finishWithOffsetMinutes(int minutes) {
        long now = System.currentTimeMillis();
        long triggerMillis = now + minutes * 60L * 1000L;
        finishWithTriggerTime(triggerMillis);
    }

    /**
     * Convert the chosen millis to a Date and ask ReminderController to
     * create the time-based reminder for this note.
     */
    private void finishWithTriggerTime(long triggerMillis) {
        if (noteId == null) {
            Toast.makeText(this, "Missing note for reminder", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Date triggerTime = new Date(triggerMillis);
        boolean success = reminderController.addTimeReminderAt(noteId, triggerTime);

        if (success) {
            Toast.makeText(this, "Time reminder set", Toast.LENGTH_SHORT).show();
            // Optionally signal success to caller
            Intent data = new Intent();
            data.putExtra(EXTRA_TRIGGER_TIME_MILLIS, triggerMillis);
            setResult(RESULT_OK, data);
        } else {
            Toast.makeText(
                    this,
                    "Unable to set reminder (note may already have an active reminder).",
                    Toast.LENGTH_LONG
            ).show();
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    /**
     * Show a date picker followed by a time picker.
     * When both are chosen, we compute the millis and call finishWithTriggerTime.
     */
    private void showCustomDateTimePicker() {
        Calendar cal = Calendar.getInstance();

        DatePickerDialog dateDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timeDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                cal.set(Calendar.MINUTE, minute);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);

                                long triggerMillis = cal.getTimeInMillis();
                                finishWithTriggerTime(triggerMillis);
                            },
                            cal.get(Calendar.HOUR_OF_DAY),
                            cal.get(Calendar.MINUTE),
                            false
                    );
                    timeDialog.show();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dateDialog.show();
    }
}

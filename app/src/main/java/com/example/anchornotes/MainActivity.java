package com.example.anchornotes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.Reminder;
import com.example.anchornotes.data.ReminderType;
import com.example.anchornotes.domain.ReminderManager;
import com.example.anchornotes.domain.NoteListController;
import com.example.anchornotes.ui.NoteListAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements
        NoteListAdapter.OnNoteClickListener,
        NoteListController.Listener {

    private Button btnAddNote;
    private Button btnSort;
    private Button btnFilter;
    private TextView txtNotes;
    private RecyclerView recyclerNotes;

    private ReminderManager reminderManager;
    private NoteListController noteListController;
    private NoteListAdapter adapter;

    // Sort + Filter modes
    private enum SortMode {
        NEWEST_FIRST,
        OLDEST_FIRST,
        TITLE_ASC,
        TITLE_DESC,
        REMINDERS_FIRST
    }

    private enum FilterMode {
        ALL,
        HAS_REMINDER,
        TIME_ONLY,
        LOCATION_ONLY,
        NO_REMINDER
    }

    private SortMode currentSortMode = SortMode.NEWEST_FIRST;
    private FilterMode currentFilterMode = FilterMode.ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init managers/controllers
        reminderManager = new ReminderManager(this);
        noteListController = new NoteListController(this, this);

        // Init views
        btnAddNote = findViewById(R.id.btnAddNote);
        btnSort = findViewById(R.id.btnSort);
        btnFilter = findViewById(R.id.btnFilter);
        txtNotes = findViewById(R.id.txtNotes);
        recyclerNotes = findViewById(R.id.recyclerNotes);

        // Set up RecyclerView
        recyclerNotes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteListAdapter(reminderManager, this);
        recyclerNotes.setAdapter(adapter);

        // Add note button
        btnAddNote.setOnClickListener(v -> openNewNote());

        // Sort button
        btnSort.setOnClickListener(v -> showSortDialog());

        // Filter button
        btnFilter.setOnClickListener(v -> showFilterDialog());

        // Initial load
        updateNotesUI();
    }

    private void openNewNote() {
        if (noteListController != null) {
            noteListController.onAddNoteClicked();
        } else {
            Intent intent = new Intent(this, NoteDetailActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from NoteDetailActivity
        updateNotesUI();
    }

    private void updateNotesUI() {
        if (noteListController != null) {
            noteListController.loadNotes();
        }
    }

    @Override
    public void onNoteClick(Note note) {
        if (note == null) return;

        if (noteListController != null) {
            noteListController.onNoteSelected(note.getId());
        } else {
            Intent intent = new Intent(this, NoteDetailActivity.class);
            intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, note.getId().toString());
            startActivity(intent);
        }
    }

    // ------------------------------------------------------------------------
    // NoteListController callbacks
    // ------------------------------------------------------------------------

    @Override
    public void onNotesLoaded(List<Note> allNotes) {
        if (allNotes == null) {
            adapter.setNotes(new ArrayList<>());
            txtNotes.setText("Total notes: 0");
            return;
        }

        List<Note> filtered = new ArrayList<>();

        // Apply filtering
        for (Note note : allNotes) {
            if (note == null) continue;
            if (passesFilter(note)) {
                filtered.add(note);
            }
        }

        // Apply sorting
        Collections.sort(filtered, getComparator());

        // Update count label with filter hint
        txtNotes.setText("Total notes: " + filtered.size());

        // Push to adapter
        adapter.setNotes(filtered);
    }

    @Override
    public void navigateToNoteDetail(UUID noteId) {
        if (noteId == null) return;
        Intent intent = new Intent(this, NoteDetailActivity.class);
        intent.putExtra(NoteDetailActivity.EXTRA_NOTE_ID, noteId.toString());
        startActivity(intent);
    }

    @Override
    public void navigateToCreateNote() {
        Intent intent = new Intent(this, NoteDetailActivity.class);
        startActivity(intent);
    }

    // ------------------------------------------------------------------------
    // Sorting + Filtering UI
    // ------------------------------------------------------------------------

    private void showSortDialog() {
        String[] options = new String[]{
                "Newest first",
                "Oldest first",
                "Title A–Z",
                "Title Z–A",
                "Reminders first"
        };

        new AlertDialog.Builder(this)
                .setTitle("Sort notes")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentSortMode = SortMode.NEWEST_FIRST;
                            break;
                        case 1:
                            currentSortMode = SortMode.OLDEST_FIRST;
                            break;
                        case 2:
                            currentSortMode = SortMode.TITLE_ASC;
                            break;
                        case 3:
                            currentSortMode = SortMode.TITLE_DESC;
                            break;
                        case 4:
                            currentSortMode = SortMode.REMINDERS_FIRST;
                            break;
                    }
                    updateNotesUI();
                })
                .show();
    }

    private void showFilterDialog() {
        String[] options = new String[]{
                "All notes",
                "Has any reminder",
                "Time reminders only",
                "Location reminders only",
                "No reminder"
        };

        new AlertDialog.Builder(this)
                .setTitle("Filter notes")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentFilterMode = FilterMode.ALL;
                            break;
                        case 1:
                            currentFilterMode = FilterMode.HAS_REMINDER;
                            break;
                        case 2:
                            currentFilterMode = FilterMode.TIME_ONLY;
                            break;
                        case 3:
                            currentFilterMode = FilterMode.LOCATION_ONLY;
                            break;
                        case 4:
                            currentFilterMode = FilterMode.NO_REMINDER;
                            break;
                    }
                    updateNotesUI();
                })
                .show();
    }

    // ------------------------------------------------------------------------
    // Filtering + sorting helpers
    // ------------------------------------------------------------------------

    private boolean passesFilter(Note note) {
        if (currentFilterMode == FilterMode.ALL) {
            return true;
        }

        UUID noteId = note.getId();
        if (noteId == null) {
            return false;
        }

        Reminder reminder = reminderManager.getReminderForNote(noteId);

        switch (currentFilterMode) {
            case HAS_REMINDER:
                return reminder != null && reminder.isActive();

            case TIME_ONLY:
                return reminder != null
                        && reminder.isActive()
                        && reminder.getType() == ReminderType.TIME;

            case LOCATION_ONLY:
                return reminder != null
                        && reminder.isActive()
                        && reminder.getType() == ReminderType.LOCATION;

            case NO_REMINDER:
                return reminder == null || !reminder.isActive();

            case ALL:
            default:
                return true;
        }
    }

    private Comparator<Note> getComparator() {
        return (a, b) -> {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;

            switch (currentSortMode) {
                case NEWEST_FIRST:
                    return Long.compare(
                            safeTime(b.getUpdatedAt()),
                            safeTime(a.getUpdatedAt())
                    );

                case OLDEST_FIRST:
                    return Long.compare(
                            safeTime(a.getUpdatedAt()),
                            safeTime(b.getUpdatedAt())
                    );

                case TITLE_ASC: {
                    String ta = safeTitle(a.getTitle());
                    String tb = safeTitle(b.getTitle());
                    return ta.compareToIgnoreCase(tb);
                }

                case TITLE_DESC: {
                    String ta = safeTitle(a.getTitle());
                    String tb = safeTitle(b.getTitle());
                    return tb.compareToIgnoreCase(ta);
                }

                case REMINDERS_FIRST: {
                    boolean aHasReminder = hasActiveReminder(a);
                    boolean bHasReminder = hasActiveReminder(b);

                    if (aHasReminder && !bHasReminder) return -1;
                    if (!aHasReminder && bHasReminder) return 1;

                    // Fallback to newest first
                    return Long.compare(
                            safeTime(b.getUpdatedAt()),
                            safeTime(a.getUpdatedAt())
                    );
                }

                default:
                    return 0;
            }
        };
    }

    private boolean hasActiveReminder(Note note) {
        if (note == null || note.getId() == null) {
            return false;
        }
        Reminder reminder = reminderManager.getReminderForNote(note.getId());
        return reminder != null && reminder.isActive();
    }

    private long safeTime(Date date) {
        return date != null ? date.getTime() : 0L;
    }

    private String safeTitle(String title) {
        return title != null ? title : "";
    }
}

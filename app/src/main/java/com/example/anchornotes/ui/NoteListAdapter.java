package com.example.anchornotes.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anchornotes.R;
import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.Reminder;
import com.example.anchornotes.data.ReminderType;
import com.example.anchornotes.domain.ReminderManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * RecyclerView adapter for the list of notes on the main screen.
 * Shows title, content preview, and (if present) a small indicator of
 * an active reminder type for that note.
 */
public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    private final ReminderManager reminderManager;
    private final OnNoteClickListener listener;
    private final List<Note> notes = new ArrayList<>();

    public NoteListAdapter(ReminderManager reminderManager, OnNoteClickListener listener) {
        this.reminderManager = reminderManager;
        this.listener = listener;
    }

    public void setNotes(List<Note> newNotes) {
        notes.clear();
        if (newNotes != null) {
            notes.addAll(newNotes);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.bind(note, reminderManager, listener);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtTitle;
        private final TextView txtContentPreview;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtItemTitle);
            txtContentPreview = itemView.findViewById(R.id.txtItemContentPreview);
        }

        public void bind(Note note,
                         ReminderManager reminderManager,
                         OnNoteClickListener listener) {

            if (note == null) {
                txtTitle.setText("(No title)");
                txtContentPreview.setText("(No content)");
                itemView.setOnClickListener(null);
                return;
            }

            String title = note.getTitle();
            String content = note.getContent();

            // Determine reminder indicator, if any
            String prefix = "";

            if (reminderManager != null) {
                UUID noteId = note.getId();
                if (noteId != null) {
                    Reminder reminder = reminderManager.getReminderForNote(noteId);
                    if (reminder != null && reminder.isActive()) {
                        if (reminder.getType() == ReminderType.TIME) {
                            // Clock icon for time reminder
                            prefix = "⏱ ";
                        } else if (reminder.getType() == ReminderType.LOCATION) {
                            // Pin icon for location reminder
                            prefix = "📍 ";
                        }
                    }
                }
            }

            if (title == null || title.isEmpty()) {
                txtTitle.setText(prefix + "(Untitled)");
            } else {
                txtTitle.setText(prefix + title);
            }

            if (content == null || content.isEmpty()) {
                txtContentPreview.setText("(No content)");
            } else {
                txtContentPreview.setText(content);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNoteClick(note);
                }
            });
        }
    }
}

package com.example.anchornotes.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anchornotes.R;
import com.example.anchornotes.data.Note;
import com.example.anchornotes.data.NoteRepository;

import java.util.List;

/**
 * Adapter for the note list on the home screen.
 * Now supports pinning notes and uses OnNoteClickListener
 * so MainActivity doesn't need to change.
 */
public class NoteListAdapter extends RecyclerView.Adapter<NoteListAdapter.NoteViewHolder> {

    // This matches what MainActivity expects.
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    private final Context context;
    private final OnNoteClickListener listener;
    private List<Note> notes;
    private final NoteRepository noteRepository;

    public NoteListAdapter(Context context, List<Note> notes, OnNoteClickListener listener) {
        this.context = context;
        this.notes = notes;
        this.listener = listener;
        this.noteRepository = new NoteRepository(context);
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.txtTitle.setText(note.getTitle());
        holder.txtPreview.setText(note.getContent());

        // Pinned icon color
        if (note.isPinned()) {
            holder.imgPin.setColorFilter(Color.YELLOW);
        } else {
            holder.imgPin.setColorFilter(Color.GRAY);
        }

        // Toggle pin when star is tapped
        holder.imgPin.setOnClickListener(v -> {
            note.setPinned(!note.isPinned());
            noteRepository.updateNote(note);
            notifyDataSetChanged();
        });

        // Open note on row tap
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNoteClick(note);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes != null ? notes.size() : 0;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle;
        TextView txtPreview;
        ImageView imgPin;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtNoteTitle);
            txtPreview = itemView.findViewById(R.id.txtNoteContentPreview);
            imgPin = itemView.findViewById(R.id.imgPin);
        }
    }
}

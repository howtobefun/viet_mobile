package com.example.doan.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doan.R;
import com.example.doan.data.Note;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
        void onNoteLongClick(View anchorView, Note note);
        void onPinClick(Note note);
    }

    private List<Note> notes = new ArrayList<>();
    private final OnNoteClickListener listener;
    private final SimpleDateFormat sdf =
            new SimpleDateFormat("HH:mm dd/MM/yyyy", new Locale("vi"));

    public NoteAdapter(OnNoteClickListener listener) {
        this.listener = listener;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public Note getNoteAt(int position) {
        return notes.get(position);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);

        holder.txtTitle.setText(note.getTitle());
        holder.txtCategory.setText(note.getCategory());
        holder.txtContent.setText(note.getContent());
        holder.txtTime.setText(sdf.format(note.getCreatedAt()));
        holder.cardNote.setCardBackgroundColor(note.getColor());
        holder.imgPin.setImageResource(
                note.isPinned() ?
                        android.R.drawable.btn_star_big_on :
                        android.R.drawable.btn_star_big_off
        );

        holder.itemView.setOnClickListener(v -> listener.onNoteClick(note));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onNoteLongClick(v, note);
            return true;
        });
        holder.imgPin.setOnClickListener(v -> listener.onPinClick(note));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtCategory, txtContent, txtTime;
        ImageView imgPin;
        MaterialCardView cardNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtContent = itemView.findViewById(R.id.txtContent);
            txtTime = itemView.findViewById(R.id.txtTime);
            imgPin = itemView.findViewById(R.id.imgPin);
            cardNote = itemView.findViewById(R.id.cardNote);
        }
    }
}

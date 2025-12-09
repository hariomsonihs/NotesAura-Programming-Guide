package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.PracticeExercise;

public class AdminPracticeExerciseManageAdapter extends RecyclerView.Adapter<AdminPracticeExerciseManageAdapter.ViewHolder> {
    public interface OnEditListener { void onEdit(PracticeExercise ex); }
    public interface OnDeleteListener { void onDelete(PracticeExercise ex); }
    private List<PracticeExercise> exercises;
    private OnEditListener editListener;
    private OnDeleteListener deleteListener;
    public AdminPracticeExerciseManageAdapter(List<PracticeExercise> exercises, OnEditListener editListener, OnDeleteListener deleteListener) {
        this.exercises = exercises;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_practice_exercise, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PracticeExercise ex = exercises.get(position);
        holder.title.setText(ex.getTitle());
        holder.name.setText(ex.getName());
        holder.link.setText(ex.getWebLink());
        holder.edit.setOnClickListener(v -> editListener.onEdit(ex));
        holder.delete.setOnClickListener(v -> deleteListener.onDelete(ex));
    }

    @Override
    public int getItemCount() { return exercises.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, name, link, edit, delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.exercise_title);
            name = itemView.findViewById(R.id.exercise_name);
            link = itemView.findViewById(R.id.exercise_link);
            edit = itemView.findViewById(R.id.edit_btn);
            delete = itemView.findViewById(R.id.delete_btn);
        }
    }
}

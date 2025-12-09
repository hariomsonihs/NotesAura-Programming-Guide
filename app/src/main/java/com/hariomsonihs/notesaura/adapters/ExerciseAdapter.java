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
import com.hariomsonihs.notesaura.models.Exercise;
import com.hariomsonihs.notesaura.interfaces.OnExerciseClickListener;
import com.hariomsonihs.notesaura.utils.OfflineManager;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
    private List<Exercise> exercises;
    private OnExerciseClickListener listener;
    private OfflineManager offlineManager;
    private String courseId;

    public ExerciseAdapter(List<Exercise> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }
    
    public void setOfflineManager(OfflineManager offlineManager, String courseId) {
        this.offlineManager = offlineManager;
        this.courseId = courseId;
    }
    
    public void updateExercises(List<Exercise> newExercises) {
        this.exercises = newExercises;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        holder.bind(exercise, listener);
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    class ExerciseViewHolder extends RecyclerView.ViewHolder {
        private TextView exerciseTitle, completedTick;
        private ImageView offlineIcon, deleteIcon;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            exerciseTitle = itemView.findViewById(R.id.exercise_title);
            completedTick = itemView.findViewById(R.id.exercise_completed_tick);
            offlineIcon = itemView.findViewById(R.id.offline_icon);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }

        public void bind(Exercise exercise, OnExerciseClickListener listener) {
            exerciseTitle.setText(exercise.getTitle());
            completedTick.setVisibility(exercise.isCompleted() ? View.VISIBLE : View.GONE);
            
            // Show offline indicators (eye icon and delete icon)
            boolean isOffline = offlineManager != null && offlineManager.isExerciseDownloaded(courseId, exercise.getId());
            if (offlineIcon != null) offlineIcon.setVisibility(isOffline ? View.VISIBLE : View.GONE);
            if (deleteIcon != null) {
                deleteIcon.setVisibility(isOffline ? View.VISIBLE : View.GONE);
                deleteIcon.setOnClickListener(v -> {
                    if (offlineManager != null) {
                        offlineManager.deleteExercise(courseId, exercise.getId());
                        notifyItemChanged(getAdapterPosition());
                    }
                });
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExerciseClick(exercise);
                }
            });
        }
    }
}
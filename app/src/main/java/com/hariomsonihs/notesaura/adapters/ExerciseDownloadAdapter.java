package com.hariomsonihs.notesaura.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.Exercise;
import com.hariomsonihs.notesaura.utils.OfflineManager;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExerciseDownloadAdapter extends RecyclerView.Adapter<ExerciseDownloadAdapter.ViewHolder> {
    private List<Exercise> exercises;
    private Set<String> selectedExercises;
    private String courseId;
    private OfflineManager offlineManager;
    private OnSelectionChangeListener listener;

    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
    }

    public ExerciseDownloadAdapter(List<Exercise> exercises, String courseId, OfflineManager offlineManager) {
        this.exercises = exercises;
        this.courseId = courseId;
        this.offlineManager = offlineManager;
        this.selectedExercises = new HashSet<>();
    }
    
    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_download, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Exercise exercise = exercises.get(position);
        
        holder.titleText.setText(exercise.getTitle());
        holder.descriptionText.setText(exercise.getDescription());
        
        boolean isDownloaded = offlineManager.isExerciseDownloaded(courseId, exercise.getId());
        holder.downloadedIcon.setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
        
        // Hide already downloaded exercises by making them non-selectable
        if (isDownloaded) {
            holder.checkbox.setVisibility(View.GONE);
            holder.itemView.setAlpha(0.5f);
        } else {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
            holder.checkbox.setChecked(selectedExercises.contains(exercise.getId()));
            holder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                android.util.Log.d("ExerciseDownloadAdapter", "Checkbox changed: " + exercise.getTitle() + ", checked: " + isChecked);
                if (isChecked) {
                    selectedExercises.add(exercise.getId());
                } else {
                    selectedExercises.remove(exercise.getId());
                }
                android.util.Log.d("ExerciseDownloadAdapter", "Selected count: " + selectedExercises.size());
                if (listener != null) {
                    listener.onSelectionChanged(selectedExercises.size());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public Set<String> getSelectedExercises() {
        return selectedExercises;
    }

    public void selectAll() {
        selectedExercises.clear();
        for (Exercise exercise : exercises) {
            // Only select exercises that are not already downloaded
            if (!offlineManager.isExerciseDownloaded(courseId, exercise.getId())) {
                selectedExercises.add(exercise.getId());
            }
        }
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedExercises.size());
        }
    }

    public void deselectAll() {
        selectedExercises.clear();
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedExercises.size());
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkbox;
        TextView titleText;
        TextView descriptionText;
        ImageView downloadedIcon;

        ViewHolder(View itemView) {
            super(itemView);
            checkbox = itemView.findViewById(R.id.checkbox_exercise);
            titleText = itemView.findViewById(R.id.text_exercise_title);
            descriptionText = itemView.findViewById(R.id.text_exercise_description);
            downloadedIcon = itemView.findViewById(R.id.icon_downloaded);
        }
    }
}
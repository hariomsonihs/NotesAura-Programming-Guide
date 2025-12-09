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

public class PracticeExerciseAdapter extends RecyclerView.Adapter<PracticeExerciseAdapter.ViewHolder> {
    public interface OnExerciseClickListener {
        void onExerciseClick(PracticeExercise exercise);
    }
    private List<PracticeExercise> exercises;
    private OnExerciseClickListener listener;
    public PracticeExerciseAdapter(List<PracticeExercise> exercises, OnExerciseClickListener listener) {
        this.exercises = exercises;
        this.listener = listener;
    }
    public void setExercises(List<PracticeExercise> exercises) {
        this.exercises = exercises;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_practice_exercise, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PracticeExercise exercise = exercises.get(position);
        holder.bind(exercise, listener, position + 1);
    }
    @Override
    public int getItemCount() {
        return exercises != null ? exercises.size() : 0;
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title, exerciseNumber, exerciseTypeBadge;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.practice_exercise_title);
            exerciseNumber = itemView.findViewById(R.id.exercise_number);
            exerciseTypeBadge = itemView.findViewById(R.id.exercise_type_badge);
        }
        public void bind(PracticeExercise exercise, OnExerciseClickListener listener, int number) {
            title.setText(exercise.getTitle());
            exerciseNumber.setText(String.valueOf(number));
            exerciseTypeBadge.setText("Code");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onExerciseClick(exercise);
            });
        }
    }
}

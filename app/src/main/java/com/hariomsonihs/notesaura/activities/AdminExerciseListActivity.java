package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.Exercise;
import com.hariomsonihs.notesaura.utils.CourseDataManager;

public class AdminExerciseListActivity extends AppCompatActivity {
    private RecyclerView exerciseRecyclerView;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exercises = new ArrayList<>();
    private String courseId;
    private CourseDataManager courseDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_exercise_list);

        courseId = getIntent().getStringExtra("course_id");
        courseDataManager = CourseDataManager.getInstance();

        initializeViews();
        setupToolbar();
        loadExercises();
    }

    private void initializeViews() {
        exerciseRecyclerView = findViewById(R.id.exercise_recycler_view);
        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseAdapter = new ExerciseAdapter(exercises, new OnExerciseDeleteListener() {
            @Override
            public void onDelete(Exercise exercise, int position) {
                exercises.remove(position);
                courseDataManager.updateExercises(courseId, exercises);
                exerciseAdapter.notifyItemRemoved(position);
                Toast.makeText(AdminExerciseListActivity.this, "Exercise deleted", Toast.LENGTH_SHORT).show();
            }
        });
        exerciseRecyclerView.setAdapter(exerciseAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Exercises");
        }
    }

    private void loadExercises() {
        List<Exercise> loaded = courseDataManager.getExercises(courseId);
        exercises.clear();
        if (loaded != null && !loaded.isEmpty()) {
            exercises.addAll(loaded);
        }
        exerciseAdapter.notifyDataSetChanged();
        Toast.makeText(this, "Loaded " + exercises.size() + " exercises", Toast.LENGTH_SHORT).show();
    }

    public interface OnExerciseDeleteListener {
        void onDelete(Exercise exercise, int position);
    }

    private static class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
        private List<Exercise> exerciseList;
        private OnExerciseDeleteListener deleteListener;
        ExerciseAdapter(List<Exercise> list, OnExerciseDeleteListener listener) {
            this.exerciseList = list;
            this.deleteListener = listener;
        }
        @NonNull
        @Override
        public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_admin, parent, false);
            return new ExerciseViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
            Exercise ex = exerciseList.get(position);
            holder.title.setText(ex.getTitle());
            holder.desc.setText(ex.getDescription());
            holder.htmlPath.setText(ex.getHtmlFilePath());
            holder.deleteBtn.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onDelete(ex, position);
            });
        }
        @Override
        public int getItemCount() { return exerciseList.size(); }
        static class ExerciseViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc, htmlPath;
            Button deleteBtn;
            ExerciseViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.exercise_title);
                desc = itemView.findViewById(R.id.exercise_desc);
                htmlPath = itemView.findViewById(R.id.exercise_html_path);
                deleteBtn = itemView.findViewById(R.id.delete_exercise_btn);
            }
        }
    }
}

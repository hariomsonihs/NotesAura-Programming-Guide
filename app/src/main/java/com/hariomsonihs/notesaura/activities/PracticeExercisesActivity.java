package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.PracticeExerciseAdapter;
import com.hariomsonihs.notesaura.models.PracticeExercise;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;
import android.view.View;

public class PracticeExercisesActivity extends AppCompatActivity implements PracticeDataManager.DataUpdateListener {
    private RecyclerView exercisesRecyclerView;
    private PracticeExerciseAdapter adapter;
    private PracticeDataManager dataManager;
    private String practiceListId;
    private String practiceListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_exercises);
        
        practiceListId = getIntent().getStringExtra("practice_list_id");
        practiceListName = getIntent().getStringExtra("practice_list_name");
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(practiceListName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Initialize data manager first
        dataManager = PracticeDataManager.getInstance();
        
        // Setup header info
        TextView listTitle = findViewById(R.id.list_title);
        TextView exercisesCountBadge = findViewById(R.id.exercises_count_badge);
        if (listTitle != null) listTitle.setText(practiceListName);
        
        List<PracticeExercise> exercises = dataManager.getExercisesByCategory(practiceListId);
        if (exercisesCountBadge != null) exercisesCountBadge.setText(String.valueOf(exercises.size()));
        
        exercisesRecyclerView = findViewById(R.id.practice_exercises_recycler_view);
        exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        if (exercises.isEmpty()) {
            showEmptyState();
        } else {
            adapter = new PracticeExerciseAdapter(exercises, exercise -> {
                Intent i = new Intent(this, PracticeContentActivity.class);
                i.putExtra("exercise_id", exercise.getId());
                i.putExtra("exercise_title", exercise.getTitle());
                startActivity(i);
            });
            exercisesRecyclerView.setAdapter(adapter);
        }
        
        dataManager.addDataUpdateListener(this);
    }
    @Override
    public void onPracticeExercisesUpdated() {
        runOnUiThread(() -> {
            List<PracticeExercise> exercises = dataManager.getExercisesByCategory(practiceListId);
            
            if (exercises.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
                if (adapter != null) {
                    adapter.setExercises(exercises);
                } else {
                    adapter = new PracticeExerciseAdapter(exercises, exercise -> {
                        Intent i = new Intent(this, PracticeContentActivity.class);
                        i.putExtra("exercise_id", exercise.getId());
                        i.putExtra("exercise_title", exercise.getTitle());
                        startActivity(i);
                    });
                    exercisesRecyclerView.setAdapter(adapter);
                }
            }
        });
    }
    
    private void showEmptyState() {
        exercisesRecyclerView.setVisibility(View.GONE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null) {
            EmptyStateHelper.showEmptyState(
                container,
                "We are working on it",
                "Exercises for this practice set will be available soon"
            );
        }
    }
    
    private void hideEmptyState() {
        exercisesRecyclerView.setVisibility(View.VISIBLE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null && container.getChildCount() > 1) {
            container.removeViewAt(1);
        }
    }
    @Override
    public void onPracticeListsUpdated() {}
    @Override
    public void onPracticeCategoriesUpdated() {}
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}

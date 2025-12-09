package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.InterviewCategoryAdapter;
import com.hariomsonihs.notesaura.utils.InterviewDataManager;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;
import android.view.View;

public class InterviewActivity extends AppCompatActivity implements InterviewDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView;
    private InterviewCategoryAdapter adapter;
    private InterviewDataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Interview Questions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        categoriesRecyclerView = findViewById(R.id.interview_categories_recycler_view);
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataManager = InterviewDataManager.getInstance();
        dataManager.addDataUpdateListener(this);
        
        // Initialize adapter with current data
        adapter = new InterviewCategoryAdapter(dataManager.getAllCategories(), category -> {
            Intent intent = new Intent(this, InterviewQuestionsActivity.class);
            intent.putExtra("category_id", category.getId());
            intent.putExtra("category_name", category.getName());
            startActivity(intent);
        });
        categoriesRecyclerView.setAdapter(adapter);
        
        // Update UI based on current data
        updateUI();
    }
    


    @Override
    public void onInterviewCategoriesUpdated() {
        runOnUiThread(() -> {
            updateUI();
        });
    }
    
    private void updateUI() {
        if (dataManager.getAllCategories().isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            if (adapter != null) {
                adapter.setCategories(dataManager.getAllCategories());
            }
        }
    }
    
    private void showEmptyState() {
        categoriesRecyclerView.setVisibility(View.GONE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null) {
            EmptyStateHelper.showEmptyState(
                container,
                "We are working on it",
                "Interview questions will be available soon"
            );
        }
    }
    
    private void hideEmptyState() {
        categoriesRecyclerView.setVisibility(View.VISIBLE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null && container.getChildCount() > 1) {
            container.removeViewAt(1);
        }
    }

    @Override
    public void onInterviewQuestionsUpdated() {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.removeDataUpdateListener(this);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
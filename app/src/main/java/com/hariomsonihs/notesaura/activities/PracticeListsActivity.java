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
import com.hariomsonihs.notesaura.adapters.PracticeListAdapter;
import com.hariomsonihs.notesaura.models.PracticeList;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;
import android.view.View;

public class PracticeListsActivity extends AppCompatActivity implements PracticeDataManager.DataUpdateListener {
    private RecyclerView recyclerView;
    private PracticeListAdapter adapter;
    private PracticeDataManager dataManager;
    private String categoryId;
    private String categoryName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_lists);
        
        categoryId = getIntent().getStringExtra("category_id");
        categoryName = getIntent().getStringExtra("category_name");
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(categoryName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Initialize data manager first
        dataManager = PracticeDataManager.getInstance();
        
        // Setup header info
        TextView categoryTitle = findViewById(R.id.category_title);
        TextView listsCountBadge = findViewById(R.id.lists_count_badge);
        if (categoryTitle != null) categoryTitle.setText(categoryName + " Lists");
        
        List<PracticeList> lists = dataManager.getPracticeListsByCategory(categoryId);
        if (listsCountBadge != null) listsCountBadge.setText(String.valueOf(lists.size()));
        
        recyclerView = findViewById(R.id.practice_lists_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        if (lists.isEmpty()) {
            showEmptyState();
        } else {
            adapter = new PracticeListAdapter(lists, list -> {
                Intent i = new Intent(this, PracticeExercisesActivity.class);
                i.putExtra("practice_list_id", list.getId());
                i.putExtra("practice_list_name", list.getName());
                startActivity(i);
            });
            recyclerView.setAdapter(adapter);
        }
        
        dataManager.addDataUpdateListener(this);
    }
    @Override
    public void onPracticeListsUpdated() {
        List<PracticeList> lists = dataManager.getPracticeListsByCategory(categoryId);
        
        if (lists.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            if (adapter != null) {
                adapter.setPracticeLists(lists);
            } else {
                adapter = new PracticeListAdapter(lists, list -> {
                    Intent i = new Intent(this, PracticeExercisesActivity.class);
                    i.putExtra("practice_list_id", list.getId());
                    i.putExtra("practice_list_name", list.getName());
                    startActivity(i);
                });
                recyclerView.setAdapter(adapter);
            }
        }
    }
    
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null) {
            EmptyStateHelper.showEmptyState(
                container,
                "We are working on it",
                "Practice lists for this category will be available soon"
            );
        }
    }
    
    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null && container.getChildCount() > 1) {
            container.removeViewAt(1);
        }
    }
    @Override public void onPracticeCategoriesUpdated() {}
    @Override public void onPracticeExercisesUpdated() {}
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

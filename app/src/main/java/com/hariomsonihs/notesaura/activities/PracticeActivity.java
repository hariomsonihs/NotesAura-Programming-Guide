package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.PracticeCategoryAdapter;
import com.hariomsonihs.notesaura.models.PracticeCategory;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;

public class PracticeActivity extends AppCompatActivity implements PracticeDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView;
    private PracticeCategoryAdapter adapter;
    private PracticeDataManager dataManager;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_practice);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            categoriesRecyclerView = findViewById(R.id.practice_categories_recycler_view);
            swipeRefresh = findViewById(R.id.swipe_refresh);
            categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            
            setupSwipeRefresh();
            dataManager = PracticeDataManager.getInstance();
            
            // Check if categories are empty
            if (dataManager.getAllCategories().isEmpty()) {
                showEmptyState();
            } else {
                adapter = new PracticeCategoryAdapter(dataManager.getAllCategories(), category -> {
                    if (category != null) {
                        Intent i = new Intent(this, PracticeListsActivity.class);
                        i.putExtra("category_id", category.getId());
                        i.putExtra("category_name", category.getName());
                        startActivity(i);
                    }
                });
                categoriesRecyclerView.setAdapter(adapter);
            }
            dataManager.addDataUpdateListener(this);
        } catch (Exception e) {
            // Handle initialization errors
            finish();
        }
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            if (com.hariomsonihs.notesaura.utils.NetworkUtil.isNetworkAvailable(this)) {
                dataManager.refreshData();
                // Stop refreshing after a delay
                new android.os.Handler().postDelayed(() -> {
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                }, 1000);
            } else {
                swipeRefresh.setRefreshing(false);
                com.hariomsonihs.notesaura.utils.OfflineHelper.showNoInternetMessage(this);
            }
        });
        swipeRefresh.setColorSchemeResources(
            R.color.primary_start,
            R.color.secondary_start,
            R.color.accent_green
        );
    }
    @Override
    public void onPracticeCategoriesUpdated() {
        if (!isFinishing()) {
            runOnUiThread(() -> {
                try {
                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                    
                    if (dataManager != null) {
                        if (dataManager.getAllCategories().isEmpty()) {
                            showEmptyState();
                        } else {
                            hideEmptyState();
                            if (adapter != null) {
                                adapter.setCategories(dataManager.getAllCategories());
                            } else {
                                adapter = new PracticeCategoryAdapter(dataManager.getAllCategories(), category -> {
                                    if (category != null) {
                                        Intent i = new Intent(this, PracticeListsActivity.class);
                                        i.putExtra("category_id", category.getId());
                                        i.putExtra("category_name", category.getName());
                                        startActivity(i);
                                    }
                                });
                                categoriesRecyclerView.setAdapter(adapter);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Handle update errors silently
                }
            });
        }
    }
    
    private void showEmptyState() {
        categoriesRecyclerView.setVisibility(View.GONE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null) {
            EmptyStateHelper.showEmptyState(
                container,
                "We are working on it",
                "Practice exercises will be available soon"
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
    public void onPracticeListsUpdated() {}
    @Override
    public void onPracticeExercisesUpdated() {}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}

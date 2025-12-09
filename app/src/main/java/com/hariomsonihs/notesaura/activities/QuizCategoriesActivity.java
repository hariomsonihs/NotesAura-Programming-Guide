package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.QuizCategoryAdapter;
import com.hariomsonihs.notesaura.models.QuizCategory;
import com.hariomsonihs.notesaura.utils.QuizDataManager;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;

public class QuizCategoriesActivity extends AppCompatActivity implements QuizDataManager.DataUpdateListener {
    private RecyclerView recyclerView;
    private QuizCategoryAdapter adapter;
    private List<QuizCategory> categories;
    private QuizDataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_categories);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Quiz Categories");
        
        recyclerView = findViewById(R.id.categories_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        dataManager = QuizDataManager.getInstance();
        categories = new ArrayList<>();
        
        adapter = new QuizCategoryAdapter(categories, this::openSubcategories);
        recyclerView.setAdapter(adapter);
        
        dataManager.addDataUpdateListener(this);
        
        // Load data from Firebase directly
        loadDataFromFirebase();
    }
    
    private void loadData() {
        categories.clear();
        List<QuizCategory> allCategories = dataManager.getAllCategories();
        
        if (allCategories.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            categories.addAll(allCategories);
        }
        
        adapter.notifyDataSetChanged();
    }
    
    private void loadDataFromFirebase() {
        android.util.Log.d("QuizCategories", "Loading quiz categories from Firebase...");
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("quiz_categories")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                android.util.Log.d("QuizCategories", "Firebase query successful. Found " + queryDocumentSnapshots.size() + " documents");
                
                categories.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        android.util.Log.d("QuizCategories", "Processing document: " + doc.getId() + ", data: " + doc.getData());
                        
                        QuizCategory category = new QuizCategory();
                        category.setId(doc.getId());
                        category.setName(doc.getString("name"));
                        category.setIconUrl(doc.getString("iconUrl"));
                        category.setColor(doc.getString("color"));
                        
                        Long orderLong = doc.getLong("order");
                        category.setOrder(orderLong != null ? orderLong.intValue() : 0);
                        
                        categories.add(category);
                        android.util.Log.d("QuizCategories", "Added category: " + category.getName());
                    } catch (Exception e) {
                        android.util.Log.e("QuizCategories", "Error parsing category: " + doc.getId(), e);
                    }
                }
                
                android.util.Log.d("QuizCategories", "Total categories loaded: " + categories.size());
                
                if (categories.isEmpty()) {
                    android.util.Log.d("QuizCategories", "No categories found, showing empty state");
                    showEmptyState();
                } else {
                    android.util.Log.d("QuizCategories", "Categories found, hiding empty state");
                    hideEmptyState();
                    categories.sort(java.util.Comparator.comparingInt(QuizCategory::getOrder));
                }
                
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("QuizCategories", "Failed to load categories from Firebase", e);
                showEmptyState();
            });
    }
    
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null) {
            EmptyStateHelper.showEmptyState(
                container,
                "We are working on it",
                "Quiz categories will be available soon"
            );
        }
    }
    
    private void hideEmptyState() {
        recyclerView.setVisibility(View.VISIBLE);
        LinearLayout container = findViewById(R.id.content_container);
        if (container != null) {
            // Remove any empty state views
            for (int i = container.getChildCount() - 1; i >= 0; i--) {
                View child = container.getChildAt(i);
                if (child != recyclerView) {
                    container.removeView(child);
                }
            }
        }
        android.util.Log.d("QuizCategories", "RecyclerView visibility set to VISIBLE, adapter item count: " + adapter.getItemCount());
    }
    
    private void openSubcategories(QuizCategory category) {
        Intent intent = new Intent(this, QuizSubcategoriesActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        startActivity(intent);
    }
    
    @Override
    public void onQuizCategoriesUpdated() {
        runOnUiThread(this::loadData);
    }
    
    @Override
    public void onQuizSubcategoriesUpdated() {}
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}
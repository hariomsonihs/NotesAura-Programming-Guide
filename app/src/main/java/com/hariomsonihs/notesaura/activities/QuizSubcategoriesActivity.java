package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.QuizSubcategoryAdapter;
import com.hariomsonihs.notesaura.models.QuizSubcategory;
import com.hariomsonihs.notesaura.utils.QuizDataManager;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;
import android.view.View;

public class QuizSubcategoriesActivity extends AppCompatActivity implements QuizDataManager.DataUpdateListener {
    private RecyclerView recyclerView;
    private QuizSubcategoryAdapter adapter;
    private List<QuizSubcategory> subcategories;
    private QuizDataManager dataManager;
    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_subcategories);
        
        categoryId = getIntent().getStringExtra("category_id");
        String categoryName = getIntent().getStringExtra("category_name");
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(categoryName + " Quizzes");
        
        recyclerView = findViewById(R.id.subcategories_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        dataManager = QuizDataManager.getInstance();
        subcategories = new ArrayList<>();
        
        adapter = new QuizSubcategoryAdapter(subcategories, this::openQuiz);
        recyclerView.setAdapter(adapter);
        
        dataManager.addDataUpdateListener(this);
        loadDataFromFirebase();
    }
    
    private void loadData() {
        subcategories.clear();
        List<QuizSubcategory> categorySubcategories = dataManager.getSubcategoriesByCategory(categoryId);
        
        if (categorySubcategories.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            subcategories.addAll(categorySubcategories);
        }
        
        adapter.notifyDataSetChanged();
    }
    
    private void loadDataFromFirebase() {
        android.util.Log.d("QuizSubcategories", "Loading subcategories for category: " + categoryId);
        
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("quiz_subcategories")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                android.util.Log.d("QuizSubcategories", "Firebase query successful. Found " + queryDocumentSnapshots.size() + " documents");
                
                subcategories.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        android.util.Log.d("QuizSubcategories", "Processing document: " + doc.getId() + ", data: " + doc.getData());
                        
                        QuizSubcategory subcategory = new QuizSubcategory();
                        subcategory.setId(doc.getId());
                        subcategory.setName(doc.getString("name"));
                        subcategory.setCategoryId(doc.getString("categoryId"));
                        subcategory.setWebUrl(doc.getString("webUrl"));
                        
                        Long orderLong = doc.getLong("order");
                        subcategory.setOrder(orderLong != null ? orderLong.intValue() : 0);
                        
                        subcategories.add(subcategory);
                        android.util.Log.d("QuizSubcategories", "Added subcategory: " + subcategory.getName());
                    } catch (Exception e) {
                        android.util.Log.e("QuizSubcategories", "Error parsing subcategory: " + doc.getId(), e);
                    }
                }
                
                android.util.Log.d("QuizSubcategories", "Total subcategories loaded: " + subcategories.size());
                
                if (subcategories.isEmpty()) {
                    android.util.Log.d("QuizSubcategories", "No subcategories found, showing empty state");
                    showEmptyState();
                } else {
                    android.util.Log.d("QuizSubcategories", "Subcategories found, hiding empty state");
                    hideEmptyState();
                    subcategories.sort(java.util.Comparator.comparingInt(QuizSubcategory::getOrder));
                }
                
                adapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("QuizSubcategories", "Failed to load subcategories from Firebase", e);
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
                "Quizzes for this category will be available soon"
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
        android.util.Log.d("QuizSubcategories", "RecyclerView visibility set to VISIBLE, adapter item count: " + adapter.getItemCount());
    }
    
    private void openQuiz(QuizSubcategory subcategory) {
        Intent intent = new Intent(this, QuizWebViewActivity.class);
        intent.putExtra("quiz_url", subcategory.getWebUrl());
        intent.putExtra("quiz_title", subcategory.getName());
        startActivity(intent);
    }
    
    @Override
    public void onQuizCategoriesUpdated() {}
    
    @Override
    public void onQuizSubcategoriesUpdated() {
        runOnUiThread(this::loadData);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}
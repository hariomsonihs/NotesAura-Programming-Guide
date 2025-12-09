package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.EbookCategoryAdapter;
import com.hariomsonihs.notesaura.models.EbookCategory;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class EbooksActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EbookCategoryAdapter adapter;
    private List<EbookCategory> categories;
    private FirebaseFirestore db;
    private SwipeRefreshLayout swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setDecorFitsSystemWindows(false);
        
        setContentView(R.layout.activity_ebooks);
        
        initViews();
        setupRecyclerView();
        loadCategories();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_ebook_categories);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        db = FirebaseFirestore.getInstance();
        
        setupSwipeRefresh();
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::refreshData);
        swipeRefresh.setColorSchemeResources(
            R.color.primary_start,
            R.color.secondary_start,
            R.color.accent_green
        );
    }
    
    private void refreshData() {
        if (com.hariomsonihs.notesaura.utils.NetworkUtil.isNetworkAvailable(this)) {
            loadCategories();
        } else {
            swipeRefresh.setRefreshing(false);
            com.hariomsonihs.notesaura.utils.OfflineHelper.showNoInternetMessage(this);
        }
    }

    private void setupRecyclerView() {
        categories = new ArrayList<>();
        adapter = new EbookCategoryAdapter(categories, this::openSubcategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadCategories() {
        db.collection("ebook_categories")
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categories.clear();
                    queryDocumentSnapshots.forEach(doc -> {
                        EbookCategory category = doc.toObject(EbookCategory.class);
                        category.setId(doc.getId());
                        categories.add(category);
                    });
                    
                    if (categories.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                    
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    swipeRefresh.setRefreshing(false);
                    if (!com.hariomsonihs.notesaura.utils.NetworkUtil.isNetworkAvailable(this)) {
                        com.hariomsonihs.notesaura.utils.OfflineHelper.showNoInternetMessage(this);
                    } else {
                        Toast.makeText(this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                    }
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
                "Ebooks will be available soon"
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

    private void openSubcategories(EbookCategory category) {
        Intent intent = new Intent(this, EbookSubcategoriesActivity.class);
        intent.putExtra("category_id", category.getId());
        intent.putExtra("category_name", category.getName());
        startActivity(intent);
    }
}
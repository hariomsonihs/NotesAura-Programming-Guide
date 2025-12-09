package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.EbookSubcategoryAdapter;
import com.hariomsonihs.notesaura.models.EbookSubcategory;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import android.widget.LinearLayout;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class EbookSubcategoriesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EbookSubcategoryAdapter adapter;
    private List<EbookSubcategory> subcategories;
    private FirebaseFirestore db;
    private String categoryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setDecorFitsSystemWindows(false);
        
        setContentView(R.layout.activity_ebook_subcategories);
        
        categoryId = getIntent().getStringExtra("category_id");
        String categoryName = getIntent().getStringExtra("category_name");
        
        initViews(categoryName);
        setupRecyclerView();
        loadSubcategories();
    }

    private void initViews(String categoryName) {
        recyclerView = findViewById(R.id.recycler_ebook_subcategories);
        TextView titleText = findViewById(R.id.title_text);
        titleText.setText(categoryName);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        db = FirebaseFirestore.getInstance();
    }

    private void setupRecyclerView() {
        subcategories = new ArrayList<>();
        adapter = new EbookSubcategoryAdapter(subcategories, this::openEbooksList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadSubcategories() {
        db.collection("ebook_subcategories")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    subcategories.clear();
                    queryDocumentSnapshots.forEach(doc -> {
                        EbookSubcategory subcategory = doc.toObject(EbookSubcategory.class);
                        subcategory.setId(doc.getId());
                        subcategories.add(subcategory);
                    });
                    
                    if (subcategories.isEmpty()) {
                        showEmptyState();
                    } else {
                        hideEmptyState();
                    }
                    
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load subcategories", Toast.LENGTH_SHORT).show();
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
                "Subcategories will be available soon"
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

    private void openEbooksList(EbookSubcategory subcategory) {
        Intent intent = new Intent(this, EbooksListActivity.class);
        intent.putExtra("subcategory_id", subcategory.getId());
        intent.putExtra("subcategory_name", subcategory.getName());
        startActivity(intent);
    }
}
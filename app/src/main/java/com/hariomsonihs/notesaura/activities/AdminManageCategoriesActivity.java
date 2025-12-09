package com.hariomsonihs.notesaura.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.CategoryManageAdapter;
import com.hariomsonihs.notesaura.utils.CourseDataManager;

public class AdminManageCategoriesActivity extends AppCompatActivity implements CourseDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView;
    private CategoryManageAdapter categoryAdapter;
    private List<CategoryItem> categories;
    private CourseDataManager courseDataManager;
    private Button addCategoryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_categories);
        
        courseDataManager = CourseDataManager.getInstance();
        courseDataManager.addDataUpdateListener(this);
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadCategories();
    }
    
    private void initializeViews() {
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        addCategoryBtn = findViewById(R.id.add_category_btn);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Categories");
        }
    }
    
    private void setupRecyclerView() {
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categories = new ArrayList<>();
        categoryAdapter = new CategoryManageAdapter(categories, new CategoryManageAdapter.OnCategoryActionListener() {
            @Override
            public void onDeleteCategory(CategoryItem category) {
                showDeleteConfirmation(category);
            }
        });
        categoriesRecyclerView.setAdapter(categoryAdapter);
        setupDragAndDrop();
    }
    
    private void setupClickListeners() {
        addCategoryBtn.setOnClickListener(v -> showAddCategoryDialog());
    }
    
    private void loadCategories() {
        categories.clear();
        Map<String, String> categoriesMap = courseDataManager.getOrderedCategoriesMap();
        int order = 0;
        for (Map.Entry<String, String> entry : categoriesMap.entrySet()) {
            categories.add(new CategoryItem(entry.getKey(), entry.getValue(), order++));
        }
        categoryAdapter.notifyDataSetChanged();
    }
    
    private void setupDragAndDrop() {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                
                if (from < to) {
                    for (int i = from; i < to; i++) {
                        Collections.swap(categories, i, i + 1);
                    }
                } else {
                    for (int i = from; i > to; i--) {
                        Collections.swap(categories, i, i - 1);
                    }
                }
                
                categoryAdapter.notifyItemMoved(from, to);
                
                // Update order for all categories and save to Firebase
                for (int i = 0; i < categories.size(); i++) {
                    categories.get(i).order = i;
                    courseDataManager.updateCategoryOrder(categories.get(i).id, i);
                }
                
                return true;
            }
            
            @Override
            public void onSwiped(RecyclerView.ViewHolder vh, int dir) {}
        });
        helper.attachToRecyclerView(categoriesRecyclerView);
    }
    
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Category");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText categoryIdEdit = new EditText(this);
        categoryIdEdit.setHint("Category ID (e.g., mobile_dev)");
        layout.addView(categoryIdEdit);
        
        EditText categoryNameEdit = new EditText(this);
        categoryNameEdit.setHint("Category Name (e.g., Mobile Development)");
        layout.addView(categoryNameEdit);
        
        builder.setView(layout);
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            String id = categoryIdEdit.getText().toString().trim();
            String name = categoryNameEdit.getText().toString().trim();
            
            if (id.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            courseDataManager.addNewCategory(id, name);
            Toast.makeText(this, "Category added successfully!", Toast.LENGTH_SHORT).show();
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showDeleteConfirmation(CategoryItem category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete '" + category.name + "'? This will also affect courses in this category.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    courseDataManager.deleteCategory(category.id);
                    Toast.makeText(this, "Category deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    @Override
    public void onCoursesUpdated() {
        // Not needed here
    }
    
    @Override
    public void onCategoriesUpdated() {
        runOnUiThread(this::loadCategories);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        courseDataManager.removeDataUpdateListener(this);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    public static class CategoryItem {
        public String id;
        public String name;
        public int order;
        
        public CategoryItem(String id, String name) {
            this.id = id;
            this.name = name;
            this.order = 0;
        }
        
        public CategoryItem(String id, String name, int order) {
            this.id = id;
            this.name = name;
            this.order = order;
        }
    }
}
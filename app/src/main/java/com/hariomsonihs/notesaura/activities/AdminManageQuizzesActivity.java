package com.hariomsonihs.notesaura.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminQuizCategoryAdapter;
import com.hariomsonihs.notesaura.adapters.AdminQuizSubcategoryAdapter;
import com.hariomsonihs.notesaura.models.*;
import com.hariomsonihs.notesaura.utils.QuizDataManager;

public class AdminManageQuizzesActivity extends AppCompatActivity implements QuizDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView, subcategoriesRecyclerView;
    private AdminQuizCategoryAdapter categoryAdapter;
    private AdminQuizSubcategoryAdapter subcategoryAdapter;
    private List<QuizCategory> categories;
    private List<QuizSubcategory> subcategories;
    private QuizDataManager dataManager;
    private Button addCategoryBtn, addSubcategoryBtn;
    private LinearLayout subcategorySection;
    private TextView subcategorySectionTitle;
    private QuizCategory selectedCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_quizzes);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Manage Quizzes");
        
        initViews();
        setupRecyclerViews();
        setupButtons();
        
        dataManager = QuizDataManager.getInstance();
        dataManager.addDataUpdateListener(this);
        loadData();
    }
    
    private void initViews() {
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        subcategoriesRecyclerView = findViewById(R.id.subcategories_recycler_view);
        addCategoryBtn = findViewById(R.id.add_category_btn);
        addSubcategoryBtn = findViewById(R.id.add_subcategory_btn);
        subcategorySection = findViewById(R.id.subcategory_section);
        subcategorySectionTitle = findViewById(R.id.subcategory_section_title);
        subcategorySection.setVisibility(View.GONE);
    }
    
    private void setupRecyclerViews() {
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        subcategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        categories = new ArrayList<>();
        subcategories = new ArrayList<>();
        
        categoryAdapter = new AdminQuizCategoryAdapter(categories, this::editCategory, this::deleteCategory, this::selectCategory);
        subcategoryAdapter = new AdminQuizSubcategoryAdapter(subcategories, this::editSubcategory, this::deleteSubcategory);
        
        categoriesRecyclerView.setAdapter(categoryAdapter);
        subcategoriesRecyclerView.setAdapter(subcategoryAdapter);
    }
    
    private void setupButtons() {
        addCategoryBtn.setOnClickListener(v -> showAddCategoryDialog());
        addSubcategoryBtn.setOnClickListener(v -> showAddSubcategoryDialog());
    }
    
    private void loadData() {
        categories.clear();
        categories.addAll(dataManager.getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }
    
    private void selectCategory(QuizCategory category) {
        selectedCategory = category;
        subcategorySectionTitle.setText("Subcategories for: " + category.getName());
        subcategorySection.setVisibility(View.VISIBLE);
        
        subcategories.clear();
        subcategories.addAll(dataManager.getSubcategoriesByCategory(category.getId()));
        subcategoryAdapter.notifyDataSetChanged();
    }
    
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Quiz Category");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText nameInput = new EditText(this);
        nameInput.setHint("Category Name");
        layout.addView(nameInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (d, w) -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = name.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis();
            QuizCategory category = new QuizCategory(id, name, "", "#2196F3", categories.size());
            dataManager.addQuizCategory(category);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showAddSubcategoryDialog() {
        if (selectedCategory == null) {
            Toast.makeText(this, "Select a category first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Quiz Subcategory");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText nameInput = new EditText(this);
        nameInput.setHint("Subcategory Name");
        layout.addView(nameInput);
        
        EditText urlInput = new EditText(this);
        urlInput.setHint("Web URL");
        layout.addView(urlInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (d, w) -> {
            String name = nameInput.getText().toString().trim();
            String url = urlInput.getText().toString().trim();
            if (name.isEmpty() || url.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = name.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis();
            QuizSubcategory subcategory = new QuizSubcategory(id, name, selectedCategory.getId(), url, subcategories.size());
            dataManager.addQuizSubcategory(subcategory);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void editCategory(QuizCategory category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Quiz Category");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText nameInput = new EditText(this);
        nameInput.setHint("Category Name");
        nameInput.setText(category.getName());
        layout.addView(nameInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Update", (d, w) -> {
            String name = nameInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            category.setName(name);
            dataManager.updateQuizCategory(category);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteCategory(QuizCategory category) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Delete " + category.getName() + "?")
            .setPositiveButton("Delete", (d, w) -> dataManager.deleteQuizCategory(category.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void editSubcategory(QuizSubcategory subcategory) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Quiz Subcategory");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText nameInput = new EditText(this);
        nameInput.setHint("Subcategory Name");
        nameInput.setText(subcategory.getName());
        layout.addView(nameInput);
        
        EditText urlInput = new EditText(this);
        urlInput.setHint("Web URL");
        urlInput.setText(subcategory.getWebUrl());
        layout.addView(urlInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Update", (d, w) -> {
            String name = nameInput.getText().toString().trim();
            String url = urlInput.getText().toString().trim();
            if (name.isEmpty() || url.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }
            subcategory.setName(name);
            subcategory.setWebUrl(url);
            dataManager.updateQuizSubcategory(subcategory);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteSubcategory(QuizSubcategory subcategory) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Subcategory")
            .setMessage("Delete " + subcategory.getName() + "?")
            .setPositiveButton("Delete", (d, w) -> dataManager.deleteQuizSubcategory(subcategory.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onQuizCategoriesUpdated() {
        runOnUiThread(this::loadData);
    }
    
    @Override
    public void onQuizSubcategoriesUpdated() {
        runOnUiThread(() -> {
            if (selectedCategory != null) {
                subcategories.clear();
                subcategories.addAll(dataManager.getSubcategoriesByCategory(selectedCategory.getId()));
                subcategoryAdapter.notifyDataSetChanged();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}
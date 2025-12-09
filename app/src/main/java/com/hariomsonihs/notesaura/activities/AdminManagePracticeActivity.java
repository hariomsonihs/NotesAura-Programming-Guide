package com.hariomsonihs.notesaura.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminPracticeCategoryManageAdapter;
import com.hariomsonihs.notesaura.adapters.AdminPracticeListManageAdapter;
import com.hariomsonihs.notesaura.models.*;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;

public class AdminManagePracticeActivity extends AppCompatActivity implements PracticeDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView, practiceListsRecyclerView;
    private AdminPracticeCategoryManageAdapter categoryAdapter;
    private AdminPracticeListManageAdapter listAdapter;
    private List<PracticeCategory> categories;
    private List<PracticeList> practiceLists;
    private PracticeDataManager dataManager;
    private PracticeCategory selectedCategory;
    private PracticeList selectedList;
    private List<PracticeExercise> exercises;
    private TextView selectedCategoryTitle;
    private LinearLayout practiceListsSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_practice);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Manage Practice");
        
        initViews();
        setupRecyclerViews();
        setupButtons();
        
        dataManager = PracticeDataManager.getInstance();
        loadData();
        dataManager.addDataUpdateListener(this);
    }
    
    private void initViews() {
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        practiceListsRecyclerView = findViewById(R.id.practice_lists_recycler_view);
        selectedCategoryTitle = findViewById(R.id.selected_category_title);
        practiceListsSection = findViewById(R.id.practice_lists_section);
        practiceListsSection.setVisibility(android.view.View.GONE);
    }
    
    private void setupRecyclerViews() {
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        practiceListsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        categories = new ArrayList<>();
        practiceLists = new ArrayList<>();
        exercises = new ArrayList<>();
        
        categoryAdapter = new AdminPracticeCategoryManageAdapter(categories, new AdminPracticeCategoryManageAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(PracticeCategory category) {
                selectCategory(category);
            }
            
            @Override
            public void onEditCategory(PracticeCategory category) {
                // Not used in this activity
            }
            
            @Override
            public void onDeleteCategory(PracticeCategory category) {
                // Not used in this activity
            }
        });
        listAdapter = new AdminPracticeListManageAdapter(practiceLists, this::editList, this::deleteList, this::openExercises);
        
        categoriesRecyclerView.setAdapter(categoryAdapter);
        practiceListsRecyclerView.setAdapter(listAdapter);
    }
    
    private void setupButtons() {
        // No buttons needed for this view
    }
    
    private void loadData() {
        categories.clear();
        categories.addAll(dataManager.getAllCategories());
        categoryAdapter.notifyDataSetChanged();
    }
    
    private void selectCategory(PracticeCategory category) {
        selectedCategory = category;
        selectedCategoryTitle.setText("Practice Lists for: " + category.getName());
        practiceListsSection.setVisibility(android.view.View.VISIBLE);
        
        practiceLists.clear();
        practiceLists.addAll(dataManager.getPracticeListsByCategory(category.getId()));
        listAdapter.notifyDataSetChanged();
    }
    
    private void openExercises(PracticeList list) {
        android.content.Intent intent = new android.content.Intent(this, AdminManagePracticeExercisesActivity.class);
        intent.putExtra("practice_list_id", list.getId());
        intent.putExtra("practice_list_name", list.getName());
        startActivity(intent);
    }
    
    private void showAddListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Practice List");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText inputName = new EditText(this);
        inputName.setHint("List Name");
        layout.addView(inputName);
        
        EditText inputDesc = new EditText(this);
        inputDesc.setHint("Description");
        layout.addView(inputDesc);
        
        Spinner categorySpinner = new Spinner(this);
        List<PracticeCategory> categories = dataManager.getAllCategories();
        List<String> categoryNames = new ArrayList<>();
        for (PracticeCategory cat : categories) {
            categoryNames.add(cat.getName());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(spinnerAdapter);
        layout.addView(categorySpinner);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            String selectedCategoryId = categories.get(categorySpinner.getSelectedItemPosition()).getId();
            String id = name.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis();
            PracticeList list = new PracticeList(id, name, desc, selectedCategoryId, practiceLists.size());
            dataManager.addPracticeList(list);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showAddExerciseDialog() {
        if (selectedList == null) {
            Toast.makeText(this, "Please select a practice list first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Exercise");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Exercise Title");
        layout.addView(inputTitle);
        
        EditText inputDesc = new EditText(this);
        inputDesc.setHint("Description");
        layout.addView(inputDesc);
        
        EditText inputLink = new EditText(this);
        inputLink.setHint("Web Link (URL)");
        layout.addView(inputLink);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            String link = inputLink.getText().toString().trim();
            if (title.isEmpty() || link.isEmpty()) {
                Toast.makeText(this, "Title and Web link required", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = title.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis();
            PracticeExercise ex = new PracticeExercise(id, title, desc, link, selectedList.getId(), exercises.size());
            dataManager.addPracticeExercise(ex);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void editList(PracticeList list) {
        // Edit list dialog similar to add but with pre-filled data
        showEditListDialog(list);
    }
    
    private void showEditListDialog(PracticeList list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Practice List");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText inputName = new EditText(this);
        inputName.setHint("List Name");
        inputName.setText(list.getName());
        layout.addView(inputName);
        
        EditText inputDesc = new EditText(this);
        inputDesc.setHint("Description");
        inputDesc.setText(list.getDescription());
        layout.addView(inputDesc);
        
        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            list.setName(name);
            list.setDescription(desc);
            dataManager.updatePracticeList(list);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteList(PracticeList list) {
        new AlertDialog.Builder(this)
            .setTitle("Delete List")
            .setMessage("Delete " + list.getName() + "?")
            .setPositiveButton("Delete", (d, w) -> dataManager.deletePracticeList(list.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void editExercise(PracticeExercise ex) {
        showEditExerciseDialog(ex);
    }
    
    private void showEditExerciseDialog(PracticeExercise ex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Exercise");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Exercise Title");
        inputTitle.setText(ex.getTitle());
        layout.addView(inputTitle);
        
        EditText inputDesc = new EditText(this);
        inputDesc.setHint("Description");
        inputDesc.setText(ex.getDescription());
        layout.addView(inputDesc);
        
        EditText inputLink = new EditText(this);
        inputLink.setHint("Web Link (URL)");
        inputLink.setText(ex.getFileLink());
        layout.addView(inputLink);
        
        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            String link = inputLink.getText().toString().trim();
            if (title.isEmpty() || link.isEmpty()) {
                Toast.makeText(this, "Title and Web link required", Toast.LENGTH_SHORT).show();
                return;
            }
            ex.setTitle(title);
            ex.setDescription(desc);
            ex.setFileLink(link);
            dataManager.updatePracticeExercise(ex);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteExercise(PracticeExercise ex) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Exercise")
            .setMessage("Delete " + ex.getTitle() + "?")
            .setPositiveButton("Delete", (d, w) -> dataManager.deletePracticeExercise(ex.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onPracticeCategoriesUpdated() {
        runOnUiThread(() -> {
            loadData();
        });
    }
    
    @Override
    public void onPracticeListsUpdated() {
        runOnUiThread(() -> {
            if (selectedCategory != null) {
                practiceLists.clear();
                practiceLists.addAll(dataManager.getPracticeListsByCategory(selectedCategory.getId()));
                listAdapter.notifyDataSetChanged();
            }
        });
    }
    
    @Override
    public void onPracticeExercisesUpdated() {}
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}
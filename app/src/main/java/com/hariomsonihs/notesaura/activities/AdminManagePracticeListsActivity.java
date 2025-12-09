package com.hariomsonihs.notesaura.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminPracticeListManageAdapter;
import com.hariomsonihs.notesaura.adapters.AdminPracticeExerciseManageAdapter;
import com.hariomsonihs.notesaura.models.*;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;

public class AdminManagePracticeListsActivity extends AppCompatActivity implements PracticeDataManager.DataUpdateListener {
    private RecyclerView recyclerView, exerciseRecyclerView;
    private AdminPracticeListManageAdapter adapter;
    private AdminPracticeExerciseManageAdapter exerciseAdapter;
    private List<PracticeList> practiceLists;
    private List<PracticeExercise> exercises;
    private PracticeDataManager dataManager;
    private Button addBtn, addExerciseBtn;
    private LinearLayout exerciseSection;
    private TextView exerciseTitle;
    private EditText searchInput;
    private PracticeList selectedList;
    private List<PracticeList> allPracticeLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_practice_lists);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Manage Practice Lists");
        
        recyclerView = findViewById(R.id.practice_lists_recycler_view);
        exerciseRecyclerView = findViewById(R.id.exercise_recycler_view);
        addBtn = findViewById(R.id.add_practice_list_btn);
        addExerciseBtn = findViewById(R.id.add_exercise_btn);
        exerciseSection = findViewById(R.id.exercise_section);
        exerciseTitle = findViewById(R.id.exercise_title);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        searchInput = findViewById(R.id.search_input);
        
        dataManager = PracticeDataManager.getInstance();
        practiceLists = new ArrayList<>();
        exercises = new ArrayList<>();
        allPracticeLists = new ArrayList<>();
        
        for (PracticeCategory cat : dataManager.getAllCategories()) {
            allPracticeLists.addAll(dataManager.getPracticeListsByCategory(cat.getId()));
        }
        practiceLists.addAll(allPracticeLists);
        
        adapter = new AdminPracticeListManageAdapter(practiceLists, this::showEditDialog, this::deletePracticeList, this::openExercises);
        exerciseAdapter = new AdminPracticeExerciseManageAdapter(exercises, this::editExercise, this::deleteExercise);
        recyclerView.setAdapter(adapter);
        exerciseRecyclerView.setAdapter(exerciseAdapter);
        
        exerciseSection.setVisibility(View.GONE);
        
        dataManager.addDataUpdateListener(this);
        addBtn.setOnClickListener(v -> showAddDialog());
        addExerciseBtn.setOnClickListener(v -> showAddExerciseDialog());
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterLists(s.toString());
            }
            public void afterTextChanged(android.text.Editable s) {}
        });
        setupDragAndDrop();
    }
    private void openExercises(PracticeList list) {
        selectedList = list;
        exerciseTitle.setText("Exercises for: " + list.getName());
        exerciseSection.setVisibility(View.VISIBLE);
        
        exercises.clear();
        exercises.addAll(dataManager.getExercisesByCategory(list.getId()));
        exerciseAdapter.notifyDataSetChanged();
    }
    
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Practice List");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText inputName = new EditText(this);
        inputName.setHint("List Name");
        layout.addView(inputName);
        
        EditText inputDesc = new EditText(this);
        inputDesc.setHint("Description");
        layout.addView(inputDesc);
        
        // Category Spinner
        android.widget.Spinner categorySpinner = new android.widget.Spinner(this);
        java.util.List<PracticeCategory> categories = dataManager.getAllCategories();
        java.util.List<String> categoryNames = new java.util.ArrayList<>();
        for (PracticeCategory cat : categories) {
            categoryNames.add(cat.getName());
        }
        android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoryNames);
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
    private void showEditDialog(PracticeList list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Practice List");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
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
    private void deletePracticeList(PracticeList list) {
        dataManager.deletePracticeList(list.getId());
    }

    private void setupDragAndDrop() {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(practiceLists, from, to);
                adapter.notifyItemMoved(from, to);
                for (int i = 0; i < practiceLists.size(); i++) practiceLists.get(i).setOrder(i);
                dataManager.updatePracticeList(practiceLists.get(from));
                dataManager.updatePracticeList(practiceLists.get(to));
                return true;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {}
        });
        helper.attachToRecyclerView(recyclerView);
    }
    @Override
    public void onPracticeCategoriesUpdated() {}
    private void filterLists(String query) {
        practiceLists.clear();
        if (query.isEmpty()) {
            practiceLists.addAll(allPracticeLists);
        } else {
            for (PracticeList list : allPracticeLists) {
                if (list.getName().toLowerCase().contains(query.toLowerCase()) ||
                    list.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    practiceLists.add(list);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onPracticeListsUpdated() {
        allPracticeLists.clear();
        for (PracticeCategory cat : dataManager.getAllCategories()) {
            allPracticeLists.addAll(dataManager.getPracticeListsByCategory(cat.getId()));
        }
        filterLists(searchInput.getText().toString());
    }
    private void showAddExerciseDialog() {
        if (selectedList == null) {
            Toast.makeText(this, "Select a practice list first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Exercise");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText titleInput = new EditText(this);
        titleInput.setHint("Title");
        layout.addView(titleInput);
        
        EditText nameInput = new EditText(this);
        nameInput.setHint("Name");
        layout.addView(nameInput);
        
        EditText linkInput = new EditText(this);
        linkInput.setHint("Web Link");
        layout.addView(linkInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (d, w) -> {
            String title = titleInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String link = linkInput.getText().toString().trim();
            
            if (title.isEmpty() || name.isEmpty() || link.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String id = title.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis();
            PracticeExercise exercise = new PracticeExercise(id, title, name, link, selectedList.getId(), exercises.size());
            dataManager.addPracticeExercise(exercise);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void editExercise(PracticeExercise exercise) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Exercise");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText titleInput = new EditText(this);
        titleInput.setHint("Title");
        titleInput.setText(exercise.getTitle());
        layout.addView(titleInput);
        
        EditText nameInput = new EditText(this);
        nameInput.setHint("Name");
        nameInput.setText(exercise.getName());
        layout.addView(nameInput);
        
        EditText linkInput = new EditText(this);
        linkInput.setHint("Web Link");
        linkInput.setText(exercise.getWebLink());
        layout.addView(linkInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Save", (d, w) -> {
            String title = titleInput.getText().toString().trim();
            String name = nameInput.getText().toString().trim();
            String link = linkInput.getText().toString().trim();
            
            if (title.isEmpty() || name.isEmpty() || link.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }
            
            exercise.setTitle(title);
            exercise.setName(name);
            exercise.setWebLink(link);
            dataManager.updatePracticeExercise(exercise);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteExercise(PracticeExercise exercise) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Exercise")
            .setMessage("Delete " + exercise.getTitle() + "?")
            .setPositiveButton("Delete", (d, w) -> dataManager.deletePracticeExercise(exercise.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    @Override
    public void onPracticeExercisesUpdated() {
        if (selectedList != null) {
            exercises.clear();
            exercises.addAll(dataManager.getExercisesByCategory(selectedList.getId()));
            exerciseAdapter.notifyDataSetChanged();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}

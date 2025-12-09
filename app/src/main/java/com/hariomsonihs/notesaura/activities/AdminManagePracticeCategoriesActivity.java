package com.hariomsonihs.notesaura.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminPracticeCategoryManageAdapter;
import com.hariomsonihs.notesaura.models.PracticeCategory;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;

public class AdminManagePracticeCategoriesActivity extends AppCompatActivity implements PracticeDataManager.DataUpdateListener {
    private RecyclerView recyclerView;
    private AdminPracticeCategoryManageAdapter adapter;
    private List<PracticeCategory> categories;
    private PracticeDataManager dataManager;
    private Button addBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_practice_categories);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.practice_categories_recycler_view);
        addBtn = findViewById(R.id.add_practice_category_btn);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataManager = PracticeDataManager.getInstance();
        categories = new ArrayList<>(dataManager.getAllCategories());
        adapter = new AdminPracticeCategoryManageAdapter(categories, new AdminPracticeCategoryManageAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(PracticeCategory category) {
                // Handle category selection if needed
            }
            
            @Override
            public void onEditCategory(PracticeCategory category) {
                showEditDialog(category);
            }
            
            @Override
            public void onDeleteCategory(PracticeCategory category) {
                deleteCategory(category);
            }
        });
        recyclerView.setAdapter(adapter);
        dataManager.addDataUpdateListener(this);
        addBtn.setOnClickListener(v -> showAddDialog());
        setupDragAndDrop();
    }
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Practice Category");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText inputId = new EditText(this);
        inputId.setHint("Category ID (e.g., java_practice)");
        layout.addView(inputId);
        
        EditText inputName = new EditText(this);
        inputName.setHint("Category Name (e.g., Java Practice)");
        layout.addView(inputName);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String id = inputId.getText().toString().trim();
            String name = inputName.getText().toString().trim();
            if (id.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Both ID and Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            PracticeCategory cat = new PracticeCategory(id, name, "", "#2196F3", categories.size());
            dataManager.addPracticeCategory(cat);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void showEditDialog(PracticeCategory cat) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Practice Category");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText inputId = new EditText(this);
        inputId.setHint("Category ID");
        inputId.setText(cat.getId());
        inputId.setEnabled(false); // ID cannot be changed
        layout.addView(inputId);
        
        EditText inputName = new EditText(this);
        inputName.setHint("Category Name");
        inputName.setText(cat.getName());
        layout.addView(inputName);
        
        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            cat.setName(name);
            dataManager.updatePracticeCategory(cat);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void deleteCategory(PracticeCategory cat) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage("Delete " + cat.getName() + "?")
            .setPositiveButton("Delete", (d, w) -> dataManager.deletePracticeCategory(cat.getId()))
            .setNegativeButton("Cancel", null)
            .show();
    }
    private void setupDragAndDrop() {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(categories, from, to);
                adapter.notifyItemMoved(from, to);
                for (int i = 0; i < categories.size(); i++) categories.get(i).setOrder(i);
                dataManager.updatePracticeCategory(categories.get(from));
                dataManager.updatePracticeCategory(categories.get(to));
                return true;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {}
        });
        helper.attachToRecyclerView(recyclerView);
    }
    @Override
    public void onPracticeCategoriesUpdated() {
        runOnUiThread(() -> {
            categories.clear();
            categories.addAll(dataManager.getAllCategories());
            adapter.notifyDataSetChanged();
        });
    }
    @Override public void onPracticeExercisesUpdated() {}
    @Override public void onPracticeListsUpdated() {}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}

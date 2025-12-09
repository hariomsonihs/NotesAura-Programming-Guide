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
import com.hariomsonihs.notesaura.adapters.AdminPracticeExerciseManageAdapter;
import com.hariomsonihs.notesaura.models.PracticeExercise;
import com.hariomsonihs.notesaura.utils.PracticeDataManager;

public class AdminManagePracticeExercisesActivity extends AppCompatActivity implements PracticeDataManager.DataUpdateListener {
    private RecyclerView recyclerView;
    private AdminPracticeExerciseManageAdapter adapter;
    private List<PracticeExercise> exercises;
    private PracticeDataManager dataManager;
    private Button addBtn;
    private String practiceListId;
    private String practiceListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_practice_exercises);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        practiceListId = getIntent().getStringExtra("practice_list_id");
        practiceListName = getIntent().getStringExtra("practice_list_name");
        if (getSupportActionBar() != null) getSupportActionBar().setTitle(practiceListName);
        recyclerView = findViewById(R.id.practice_exercises_recycler_view);
        addBtn = findViewById(R.id.add_practice_exercise_btn);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataManager = PracticeDataManager.getInstance();
        exercises = new ArrayList<>(dataManager.getExercisesByCategory(practiceListId));
        adapter = new AdminPracticeExerciseManageAdapter(exercises, this::showEditDialog, this::deleteExercise);
        recyclerView.setAdapter(adapter);
        dataManager.addDataUpdateListener(this);
        addBtn.setOnClickListener(v -> showAddDialog());
        setupDragAndDrop();
    }
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Practice Exercise");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
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
            if (title.isEmpty()) {
                Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (link.isEmpty()) {
                Toast.makeText(this, "Web link required", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = title.toLowerCase().replaceAll("[^a-z0-9]", "_") + "_" + System.currentTimeMillis();
            PracticeExercise ex = new PracticeExercise(id, title, desc, link, practiceListId, exercises.size());
            dataManager.addPracticeExercise(ex);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void showEditDialog(PracticeExercise ex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Practice Exercise");
        
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
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
            if (title.isEmpty()) {
                Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
                return;
            }
            if (link.isEmpty()) {
                Toast.makeText(this, "Web link required", Toast.LENGTH_SHORT).show();
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
    private void setupDragAndDrop() {
        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) {
                int from = vh.getAdapterPosition();
                int to = target.getAdapterPosition();
                Collections.swap(exercises, from, to);
                adapter.notifyItemMoved(from, to);
                for (int i = 0; i < exercises.size(); i++) exercises.get(i).setOrder(i);
                dataManager.updatePracticeExercise(exercises.get(from));
                dataManager.updatePracticeExercise(exercises.get(to));
                return true;
            }
            @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {}
        });
        helper.attachToRecyclerView(recyclerView);
    }
    @Override
    public void onPracticeExercisesUpdated() {
        runOnUiThread(() -> {
            exercises.clear();
            exercises.addAll(dataManager.getExercisesByCategory(practiceListId));
            adapter.notifyDataSetChanged();
        });
    }
    @Override public void onPracticeCategoriesUpdated() {}
    @Override public void onPracticeListsUpdated() {}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
}

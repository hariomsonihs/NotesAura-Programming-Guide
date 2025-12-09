package com.hariomsonihs.notesaura.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminInterviewCategoryAdapter;
import com.hariomsonihs.notesaura.adapters.AdminInterviewQuestionAdapter;
import com.hariomsonihs.notesaura.models.InterviewCategory;
import com.hariomsonihs.notesaura.models.InterviewQuestion;
import com.hariomsonihs.notesaura.utils.InterviewDataManager;
import com.hariomsonihs.notesaura.utils.NotificationHelper;

public class AdminManageInterviewActivity extends AppCompatActivity implements InterviewDataManager.DataUpdateListener {
    private RecyclerView categoriesRecyclerView, questionsRecyclerView;
    private AdminInterviewCategoryAdapter categoryAdapter;
    private AdminInterviewQuestionAdapter questionAdapter;
    private List<InterviewCategory> categories;
    private List<InterviewQuestion> questions;
    private InterviewDataManager dataManager;
    private InterviewCategory selectedCategory;
    private TextView selectedCategoryTitle;
    private LinearLayout questionsSection;
    private Button addCategoryBtn, addQuestionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_interview);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Interview Questions");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initViews();
        setupRecyclerViews();
        
        dataManager = InterviewDataManager.getInstance();
        dataManager.addDataUpdateListener(this);
        loadData();
    }
    
    private void initViews() {
        categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        selectedCategoryTitle = findViewById(R.id.selected_category_title);
        questionsSection = findViewById(R.id.questions_section);
        addCategoryBtn = findViewById(R.id.add_category_btn);
        addQuestionBtn = findViewById(R.id.add_question_btn);
        
        questionsSection.setVisibility(android.view.View.GONE);
        
        addCategoryBtn.setOnClickListener(v -> showAddCategoryDialog());
        if (addQuestionBtn != null) {
            addQuestionBtn.setOnClickListener(v -> showAddQuestionDialog());
        }
    }
    
    private void setupRecyclerViews() {
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        categories = new ArrayList<>();
        questions = new ArrayList<>();
        
        categoryAdapter = new AdminInterviewCategoryAdapter(categories, new AdminInterviewCategoryAdapter.OnCategoryActionListener() {
            @Override
            public void onCategoryClick(InterviewCategory category) {
                selectCategory(category);
            }
            
            @Override
            public void onEditCategory(InterviewCategory category) {
                editCategory(category);
            }
            
            @Override
            public void onDeleteCategory(InterviewCategory category) {
                deleteCategory(category);
            }
        });
        questionAdapter = new AdminInterviewQuestionAdapter(questions, this::editQuestion, this::deleteQuestion);
        
        categoriesRecyclerView.setAdapter(categoryAdapter);
        questionsRecyclerView.setAdapter(questionAdapter);
    }
    
    private void loadData() {
        if (dataManager != null) {
            categories.clear();
            List<InterviewCategory> allCategories = dataManager.getAllCategories();
            if (allCategories != null) {
                categories.addAll(allCategories);
            }
            categoryAdapter.notifyDataSetChanged();
        }
    }
    
    private void selectCategory(InterviewCategory category) {
        selectedCategory = category;
        selectedCategoryTitle.setText("Questions for: " + category.getName());
        questionsSection.setVisibility(android.view.View.VISIBLE);
        
        // Set up add question button click listener when section becomes visible
        if (addQuestionBtn != null) {
            addQuestionBtn.setOnClickListener(v -> showAddQuestionDialog());
        }
        
        questions.clear();
        if (dataManager != null) {
            List<InterviewQuestion> categoryQuestions = dataManager.getQuestionsByCategory(category.getId());
            if (categoryQuestions != null) {
                questions.addAll(categoryQuestions);
            }
        }
        questionAdapter.notifyDataSetChanged();
    }
    
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Interview Category");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText nameInput = new EditText(this);
        nameInput.setHint("Category Name");
        layout.addView(nameInput);
        
        EditText descInput = new EditText(this);
        descInput.setHint("Description");
        layout.addView(descInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = name.toLowerCase().replaceAll("[^a-z0-9]", "_");
            InterviewCategory category = new InterviewCategory(id, name, desc, "#2196F3", categories.size());
            if (dataManager != null) {
                dataManager.addInterviewCategory(category);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void editCategory(InterviewCategory category) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Category");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText nameInput = new EditText(this);
        nameInput.setText(category.getName());
        layout.addView(nameInput);
        
        EditText descInput = new EditText(this);
        descInput.setText(category.getDescription());
        layout.addView(descInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                return;
            }
            category.setName(name);
            category.setDescription(desc);
            if (dataManager != null) {
                dataManager.updateInterviewCategory(category);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteCategory(InterviewCategory category) {
        if (dataManager != null) {
            dataManager.deleteInterviewCategory(category.getId());
        }
    }
    
    private void showAddQuestionDialog() {
        if (selectedCategory == null) {
            Toast.makeText(this, "Please select a category first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Interview Question");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText titleInput = new EditText(this);
        titleInput.setHint("Question Title");
        layout.addView(titleInput);
        
        EditText descInput = new EditText(this);
        descInput.setHint("Description");
        layout.addView(descInput);
        
        EditText linkInput = new EditText(this);
        linkInput.setHint("Web Link (optional)");
        layout.addView(linkInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String link = linkInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
                return;
            }
            String id = System.currentTimeMillis() + "";
            InterviewQuestion question = new InterviewQuestion(id, title, desc, link, selectedCategory.getId(), questions.size());
            if (dataManager != null) {
                dataManager.addInterviewQuestion(question);
                
                // Send notification
                NotificationHelper.sendNotificationToAllUsers(
                    "New Interview Question!", 
                    "New question added in " + selectedCategory.getName() + ": " + title, 
                    "interview", 
                    selectedCategory.getId(), 
                    null
                );
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void editQuestion(InterviewQuestion question) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Question");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        
        EditText titleInput = new EditText(this);
        titleInput.setText(question.getTitle());
        layout.addView(titleInput);
        
        EditText descInput = new EditText(this);
        descInput.setText(question.getDescription());
        layout.addView(descInput);
        
        EditText linkInput = new EditText(this);
        linkInput.setText(question.getWebLink());
        layout.addView(linkInput);
        
        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String title = titleInput.getText().toString().trim();
            String desc = descInput.getText().toString().trim();
            String link = linkInput.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Title required", Toast.LENGTH_SHORT).show();
                return;
            }
            question.setTitle(title);
            question.setDescription(desc);
            question.setWebLink(link);
            if (dataManager != null) {
                dataManager.updateInterviewQuestion(question);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void deleteQuestion(InterviewQuestion question) {
        if (dataManager != null) {
            dataManager.deleteInterviewQuestion(question.getId());
        }
    }
    
    @Override
    public void onInterviewCategoriesUpdated() {
        runOnUiThread(this::loadData);
    }
    
    @Override
    public void onInterviewQuestionsUpdated() {
        runOnUiThread(() -> {
            if (selectedCategory != null && dataManager != null) {
                questions.clear();
                List<InterviewQuestion> categoryQuestions = dataManager.getQuestionsByCategory(selectedCategory.getId());
                if (categoryQuestions != null) {
                    questions.addAll(categoryQuestions);
                }
                questionAdapter.notifyDataSetChanged();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dataManager != null) {
            dataManager.removeDataUpdateListener(this);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
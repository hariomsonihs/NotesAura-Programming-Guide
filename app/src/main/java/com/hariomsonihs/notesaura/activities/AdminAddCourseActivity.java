package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.models.Exercise;
import com.hariomsonihs.notesaura.utils.CourseDataManager;
import com.hariomsonihs.notesaura.utils.NotificationHelper;

public class AdminAddCourseActivity extends AppCompatActivity {
    private EditText courseIdEdit, courseTitleEdit, courseDescEdit, coursePriceEdit, courseDurationEdit, courseRatingEdit;
    private EditText learningObjectivesEdit, targetAudienceEdit;
    private Spinner categorySpinner, difficultySpinner;
    private Button addCourseBtn;
    private CourseDataManager courseDataManager;

    // Exercise management
    private RecyclerView exerciseRecyclerView;
    private Button addExerciseBtn;
    private ExerciseAdapter exerciseAdapter;
    private List<Exercise> exercises = new ArrayList<>();
    
    // Description fields
    private List<String> learningObjectives = new ArrayList<>();
    private List<String> targetAudience = new ArrayList<>();

    private boolean isEditMode = false;
    private String editingCourseId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_course);

        courseDataManager = CourseDataManager.getInstance();

        initializeViews();
        setupToolbar();
        setupSpinners();
        setupClickListeners();

        // Check for edit mode
        if (getIntent().hasExtra("course_id")) {
            isEditMode = true;
            editingCourseId = getIntent().getStringExtra("course_id");
            loadCourseForEdit(editingCourseId);
        }

        setupExerciseSection();

        // Show asset file paths button logic
        Button showAssetsBtn = findViewById(R.id.show_assets_files_btn);
        showAssetsBtn.setOnClickListener(v -> showAssetsFileListDialog());
    }

    private void loadCourseForEdit(String courseId) {
        com.hariomsonihs.notesaura.models.Course course = courseDataManager.getCourse(courseId);
        if (course != null) {
            courseIdEdit.setText(course.getId());
            courseIdEdit.setEnabled(false); // Don't allow changing ID
            courseTitleEdit.setText(course.getTitle());
            courseDescEdit.setText(course.getDescription());
            coursePriceEdit.setText(String.valueOf(course.getPrice()));
            courseDurationEdit.setText(String.valueOf(course.getDuration()));
            courseRatingEdit.setText(String.valueOf(course.getRating()));
            // Set category spinner
            String categoryName = courseDataManager.getCategoryName(course.getCategory());
            ArrayAdapter<String> categoryAdapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
            int catPos = categoryAdapter.getPosition(categoryName);
            if (catPos >= 0) categorySpinner.setSelection(catPos);
            // Set difficulty spinner
            String difficulty = course.getDifficulty();
            ArrayAdapter<String> diffAdapter = (ArrayAdapter<String>) difficultySpinner.getAdapter();
            int diffPos = diffAdapter.getPosition(difficulty);
            if (diffPos >= 0) difficultySpinner.setSelection(diffPos);
            // Load exercises for this course
            exercises.clear();
            List<Exercise> existing = courseDataManager.getExercises(courseId);
            if (existing != null) exercises.addAll(existing);
            if (exerciseAdapter != null) exerciseAdapter.notifyDataSetChanged();
            
            // Load Firebase description data
            loadFirebaseDescriptionData(courseId);
            
            // Change button text
            addCourseBtn.setText("Update Course");
        }
    }
    
    private void loadFirebaseDescriptionData(String courseId) {
        com.hariomsonihs.notesaura.utils.FirebaseDBHelper.getInstance()
                .getCourseDocument(courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> objectives = (List<String>) documentSnapshot.get("learningObjectives");
                        List<String> audience = (List<String>) documentSnapshot.get("targetAudience");
                        
                        if (objectives != null) {
                            StringBuilder objText = new StringBuilder();
                            for (String obj : objectives) {
                                objText.append(obj).append("\n");
                            }
                            learningObjectivesEdit.setText(objText.toString().trim());
                        }
                        
                        if (audience != null) {
                            StringBuilder audText = new StringBuilder();
                            for (String aud : audience) {
                                audText.append(aud).append("\n");
                            }
                            targetAudienceEdit.setText(audText.toString().trim());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Ignore failure, fields will remain empty
                });
    }

    // Show a dialog with a WebView displaying the assets file list
    private void showAssetsFileListDialog() {
        android.webkit.WebView webView = new android.webkit.WebView(this);
        webView.loadUrl("file:///android_asset/assets_file_list.html");
        new android.app.AlertDialog.Builder(this)
                .setTitle("Available Asset File Paths")
                .setView(webView)
                .setPositiveButton("Close", null)
                .show();
    }
    
    private void initializeViews() {
        courseIdEdit = findViewById(R.id.course_id_edit);
        courseTitleEdit = findViewById(R.id.course_title_edit);
        courseDescEdit = findViewById(R.id.course_desc_edit);
        coursePriceEdit = findViewById(R.id.course_price_edit);
        courseDurationEdit = findViewById(R.id.course_duration_edit);
        courseRatingEdit = findViewById(R.id.course_rating_edit);
        learningObjectivesEdit = findViewById(R.id.learning_objectives_edit);
        targetAudienceEdit = findViewById(R.id.target_audience_edit);
        categorySpinner = findViewById(R.id.category_spinner);
        difficultySpinner = findViewById(R.id.difficulty_spinner);
        addCourseBtn = findViewById(R.id.add_course_btn);

        // Inflate and add exercise section
        ViewGroup rootLayout = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        LayoutInflater inflater = LayoutInflater.from(this);
        View exerciseSection = inflater.inflate(R.layout.layout_exercise_list, rootLayout, false);
        // Add after course details, before addCourseBtn
        ViewGroup scrollView = (ViewGroup) rootLayout.getChildAt(1); // ScrollView
        ViewGroup linearLayout = (ViewGroup) ((ViewGroup) scrollView.getChildAt(0));
        linearLayout.addView(exerciseSection, linearLayout.getChildCount() - 1);

        exerciseRecyclerView = exerciseSection.findViewById(R.id.exercise_recycler_view);
        addExerciseBtn = exerciseSection.findViewById(R.id.add_exercise_btn);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add New Course");
        }
    }
    
    private void setupSpinners() {
        // Category spinner
        List<String> categories = new ArrayList<>();
        for (String categoryId : courseDataManager.getAllCategories()) {
            categories.add(courseDataManager.getCategoryName(categoryId));
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        
        // Difficulty spinner
        String[] difficulties = {"Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, difficulties);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(difficultyAdapter);
    }
    
    private void setupClickListeners() {
        addCourseBtn.setOnClickListener(v -> {
            if (isEditMode) {
                updateCourse();
            } else {
                addCourse();
            }
        });
    }

    private void updateCourse() {
        // Use the same validation as addCourse
        String id = courseIdEdit.getText().toString().trim();
        String title = courseTitleEdit.getText().toString().trim();
        String description = courseDescEdit.getText().toString().trim();
        String priceStr = coursePriceEdit.getText().toString().trim();
        String durationStr = courseDurationEdit.getText().toString().trim();
        String ratingStr = courseRatingEdit.getText().toString().trim();

        if (id.isEmpty() || title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
            int duration = durationStr.isEmpty() ? 30 : Integer.parseInt(durationStr);
            float rating = ratingStr.isEmpty() ? 4.0f : Float.parseFloat(ratingStr);

            String selectedCategory = getSelectedCategoryId();
            String difficulty = difficultySpinner.getSelectedItem().toString();
            
            // Parse learning objectives and target audience
            parseLearningObjectives();
            parseTargetAudience();

            // Update course in data manager
            courseDataManager.updateCourse(id, title, description, selectedCategory, rating, duration, price, difficulty, new ArrayList<>(exercises));
            
            // Update description data in Firebase
            saveCourseDescriptionToFirebase(id);

            Toast.makeText(this, "Course updated successfully!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupExerciseSection() {
        // Load exercises if editing existing course, else empty or default
        String courseId = courseIdEdit.getText().toString().trim();
        if (!courseId.isEmpty()) {
            List<Exercise> existing = courseDataManager.getExercises(courseId);
            if (existing != null && !existing.isEmpty()) {
                exercises.clear();
                exercises.addAll(existing);
            }
        }
        exerciseAdapter = new ExerciseAdapter(exercises);
        exerciseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        exerciseRecyclerView.setAdapter(exerciseAdapter);

        addExerciseBtn.setOnClickListener(v -> showAddExerciseDialog());
    }

    private void showAddExerciseDialog() {
        // Simple dialog for adding exercise
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null);
        EditText titleEdit = dialogView.findViewById(R.id.exercise_title_edit);
        EditText descEdit = dialogView.findViewById(R.id.exercise_desc_edit);
        EditText htmlPathEdit = dialogView.findViewById(R.id.exercise_html_path_edit);
        builder.setView(dialogView)
                .setTitle("Add Exercise")
                .setPositiveButton("Add", (dialog, which) -> {
                    String id = "e" + (exercises.size() + 1);
                    String title = titleEdit.getText().toString().trim();
                    String desc = descEdit.getText().toString().trim();
                    String htmlPath = htmlPathEdit.getText().toString().trim();
                    String courseId = courseIdEdit.getText().toString().trim();
                    if (!title.isEmpty() && !htmlPath.isEmpty()) {
                        exercises.add(new Exercise(id, title, desc, htmlPath, courseId));
                        exerciseAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ExerciseAdapter for RecyclerView
    private class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {
        private List<Exercise> exerciseList;
        ExerciseAdapter(List<Exercise> list) { this.exerciseList = list; }
        @NonNull
        @Override
        public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise_admin, parent, false);
            return new ExerciseViewHolder(view);
        }
        @Override
        public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
            Exercise ex = exerciseList.get(position);
            holder.title.setText(ex.getTitle());
            holder.desc.setText(ex.getDescription());
            holder.htmlPath.setText(ex.getHtmlFilePath());
            holder.deleteBtn.setOnClickListener(v -> {
                exerciseList.remove(position);
                notifyDataSetChanged();
            });
            holder.editBtn.setOnClickListener(v -> showEditExerciseDialog(position));
        }
        @Override
        public int getItemCount() { return exerciseList.size(); }
        class ExerciseViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc, htmlPath;
            Button editBtn, deleteBtn;
            ExerciseViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.exercise_title);
                desc = itemView.findViewById(R.id.exercise_desc);
                htmlPath = itemView.findViewById(R.id.exercise_html_path);
                editBtn = itemView.findViewById(R.id.edit_exercise_btn);
                deleteBtn = itemView.findViewById(R.id.delete_exercise_btn);
            }
        }
    }

    private void showEditExerciseDialog(int pos) {
        Exercise ex = exercises.get(pos);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null);
        EditText titleEdit = dialogView.findViewById(R.id.exercise_title_edit);
        EditText descEdit = dialogView.findViewById(R.id.exercise_desc_edit);
        EditText htmlPathEdit = dialogView.findViewById(R.id.exercise_html_path_edit);
        titleEdit.setText(ex.getTitle());
        descEdit.setText(ex.getDescription());
        htmlPathEdit.setText(ex.getHtmlFilePath());
        builder.setView(dialogView)
                .setTitle("Edit Exercise")
                .setPositiveButton("Save", (dialog, which) -> {
                    ex.setTitle(titleEdit.getText().toString().trim());
                    ex.setDescription(descEdit.getText().toString().trim());
                    ex.setHtmlFilePath(htmlPathEdit.getText().toString().trim());
                    exerciseAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void addCourse() {
        String id = courseIdEdit.getText().toString().trim();
        String title = courseTitleEdit.getText().toString().trim();
        String description = courseDescEdit.getText().toString().trim();
        String priceStr = coursePriceEdit.getText().toString().trim();
        String durationStr = courseDurationEdit.getText().toString().trim();
        String ratingStr = courseRatingEdit.getText().toString().trim();

        if (id.isEmpty() || title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double price = priceStr.isEmpty() ? 0.0 : Double.parseDouble(priceStr);
            int duration = durationStr.isEmpty() ? 30 : Integer.parseInt(durationStr);
            float rating = ratingStr.isEmpty() ? 4.0f : Float.parseFloat(ratingStr);

            String selectedCategory = getSelectedCategoryId();
            String difficulty = difficultySpinner.getSelectedItem().toString();
            
            // Parse learning objectives and target audience
            parseLearningObjectives();
            parseTargetAudience();

            // Use exercises from UI
            courseDataManager.addNewCourse(id, title, description, selectedCategory, rating, duration, price, difficulty, new ArrayList<>(exercises));
            
            // Save description data to Firebase
            saveCourseDescriptionToFirebase(id);

            // Send notification for new course
            NotificationHelper.sendNotificationToAllUsers(
                "New Course Added!", 
                "Check out the new course: " + title, 
                "course", 
                id, 
                null
            );
            
            Toast.makeText(this, "Course added/updated successfully!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void parseLearningObjectives() {
        learningObjectives.clear();
        String text = learningObjectivesEdit.getText().toString().trim();
        if (!text.isEmpty()) {
            String[] lines = text.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    learningObjectives.add(line);
                }
            }
        }
    }
    
    private void parseTargetAudience() {
        targetAudience.clear();
        String text = targetAudienceEdit.getText().toString().trim();
        if (!text.isEmpty()) {
            String[] lines = text.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    targetAudience.add(line);
                }
            }
        }
    }
    
    private void saveCourseDescriptionToFirebase(String courseId) {
        java.util.Map<String, Object> descriptionData = new java.util.HashMap<>();
        descriptionData.put("learningObjectives", learningObjectives);
        descriptionData.put("targetAudience", targetAudience);
        
        com.hariomsonihs.notesaura.utils.FirebaseDBHelper.getInstance()
                .getCourseDocument(courseId)
                .update(descriptionData)
                .addOnSuccessListener(aVoid -> {
                    // Success - already showing toast in addCourse
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save course description", Toast.LENGTH_SHORT).show();
                });
    }
    
    private String getSelectedCategoryId() {
        String selectedCategoryName = categorySpinner.getSelectedItem().toString();
        for (String categoryId : courseDataManager.getAllCategories()) {
            if (courseDataManager.getCategoryName(categoryId).equals(selectedCategoryName)) {
                return categoryId;
            }
        }
        return "programming"; // default
    }
    
    // createDefaultExercises removed, not needed anymore
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
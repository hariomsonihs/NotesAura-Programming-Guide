package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminCourseGridAdapter;
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.utils.CourseDataManager;

public class AdminCoursesActivity extends AppCompatActivity {
    private RecyclerView coursesRecyclerView;
    private AdminCourseGridAdapter courseAdapter;
    private List<Course> courses;
    private List<Course> filteredCourses;
    private CourseDataManager courseDataManager;
    private android.widget.EditText searchCoursesEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_courses);

        courseDataManager = CourseDataManager.getInstance();

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        loadCourses();
    }
    
    private void initializeViews() {
        coursesRecyclerView = findViewById(R.id.courses_recycler_view);
        searchCoursesEditText = findViewById(R.id.search_courses);
    }
    
    private void updateStats() {
        android.widget.TextView totalCount = findViewById(R.id.total_courses_count);
        android.widget.TextView activeCount = findViewById(R.id.active_courses_count);
        
        if (totalCount != null) {
            totalCount.setText(String.valueOf(courses.size()));
        }
        if (activeCount != null) {
            activeCount.setText(String.valueOf(courses.size()));
        }
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Courses");
        }
    }
    
    private void setupRecyclerView() {
        coursesRecyclerView.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        courses = new ArrayList<>();
        filteredCourses = new ArrayList<>();
        courseAdapter = new com.hariomsonihs.notesaura.adapters.AdminCourseGridAdapter(filteredCourses, new com.hariomsonihs.notesaura.interfaces.OnCourseClickListener() {
            @Override
            public void onCourseClick(Course course) {
                // Open AdminAddCourseActivity in edit mode
                android.content.Intent intent = new android.content.Intent(AdminCoursesActivity.this, AdminAddCourseActivity.class);
                intent.putExtra("course_id", course.getId());
                startActivity(intent);
            }
            @Override
            public void onEnrollClick(Course course) {
                // Handle enroll click if needed
            }
        });
        coursesRecyclerView.setAdapter(courseAdapter);

        // Long press to delete
        courseAdapter.setOnItemLongClickListener((position, course) -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    courseDataManager.deleteCourse(course.getId());
                    loadCourses();
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
    }

    private void setupSearch() {
        searchCoursesEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void filterCourses(String query) {
        filteredCourses.clear();
        if (query.isEmpty()) {
            filteredCourses.addAll(courses);
        } else {
            String lower = query.toLowerCase();
            for (Course course : courses) {
                if ((course.getTitle() != null && course.getTitle().toLowerCase().contains(lower)) ||
                    (course.getDescription() != null && course.getDescription().toLowerCase().contains(lower)) ||
                    (course.getCategory() != null && course.getCategory().toLowerCase().contains(lower))) {
                    filteredCourses.add(course);
                }
            }
        }
        courseAdapter.notifyDataSetChanged();
    }
    
    private void loadCourses() {
        courses.clear();
        courses.addAll(courseDataManager.getAllCourses());
        filterCourses(searchCoursesEditText.getText().toString());
        updateStats();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
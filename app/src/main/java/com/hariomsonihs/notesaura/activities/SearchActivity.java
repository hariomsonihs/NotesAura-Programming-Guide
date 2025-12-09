package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.CourseGridAdapter;
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.interfaces.OnCourseClickListener;
import com.hariomsonihs.notesaura.utils.Constants;

public class SearchActivity extends AppCompatActivity implements OnCourseClickListener {
    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private TextView noResultsText;
    private CourseGridAdapter searchAdapter;
    private List<Course> allCourses;
    private List<Course> filteredCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        loadAllCourses();
        setupSearch();
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.search_edit_text);
        searchResultsRecyclerView = findViewById(R.id.search_results_recycler_view);
        noResultsText = findViewById(R.id.no_results_text);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Courses");
        }
    }

    private void setupRecyclerView() {
        searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        filteredCourses = new ArrayList<>();
        searchAdapter = new CourseGridAdapter(filteredCourses, this);
        searchResultsRecyclerView.setAdapter(searchAdapter);
    }

    private void loadAllCourses() {
        // Only load courses from CourseDataManager (admin panel/Firebase)
        allCourses = com.hariomsonihs.notesaura.utils.CourseDataManager.getInstance().getAllCourses();
    }
    
    private void setCourseProperties(String courseId, String difficulty, int duration, double price) {
        for (Course course : allCourses) {
            if (course.getId().equals(courseId)) {
                course.setDifficulty(difficulty);
                course.setDuration(duration);
                course.setPrice(price);
                course.setFree(false);
                course.setRating(4.5f);
                course.setEnrolledCount((int)(Math.random() * 1000) + 100);
                break;
            }
        }
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.equalsIgnoreCase("admin")) {
                    showAdminPasswordDialog();
                } else {
                    filterCourses(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Focus on search box
        searchEditText.requestFocus();
    }
    
    private void showAdminPasswordDialog() {
        android.widget.EditText passwordInput = new android.widget.EditText(this);
        passwordInput.setHint("Enter admin password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        passwordInput.setPadding(50, 30, 50, 30);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🔒 Admin Access")
            .setMessage("Enter password to access web admin panel")
            .setView(passwordInput)
            .setPositiveButton("Access", (dialog, which) -> {
                String password = passwordInput.getText().toString().trim();
                if (password.equals("0819")) {
                    // Open web admin panel in browser
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://notesaura-admin.vercel.app/"));
                    startActivity(browserIntent);
                    searchEditText.setText("");
                } else {
                    android.widget.Toast.makeText(this, "Incorrect password!", android.widget.Toast.LENGTH_SHORT).show();
                    searchEditText.setText("");
                }
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                searchEditText.setText("");
            })
            .setOnCancelListener(dialog -> {
                searchEditText.setText("");
            })
            .show();
    }

    private void filterCourses(String query) {
        filteredCourses.clear();
        
        if (query.trim().isEmpty()) {
            // Show all courses if search is empty
            filteredCourses.addAll(allCourses);
        } else {
            // Filter courses based on query
            String lowerQuery = query.toLowerCase().trim();
            
            for (Course course : allCourses) {
                if (course.getTitle().toLowerCase().contains(lowerQuery) ||
                    course.getDescription().toLowerCase().contains(lowerQuery) ||
                    course.getCategory().toLowerCase().contains(lowerQuery)) {
                    filteredCourses.add(course);
                }
            }
        }
        
        // Update UI
        if (filteredCourses.isEmpty() && !query.trim().isEmpty()) {
            searchResultsRecyclerView.setVisibility(View.GONE);
            noResultsText.setVisibility(View.VISIBLE);
            noResultsText.setText("No courses found for \"" + query + "\"");
        } else {
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
            noResultsText.setVisibility(View.GONE);
        }
        
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCourseClick(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE_ID, course.getId());
        startActivity(intent);
    }

    @Override
    public void onEnrollClick(Course course) {
        onCourseClick(course);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
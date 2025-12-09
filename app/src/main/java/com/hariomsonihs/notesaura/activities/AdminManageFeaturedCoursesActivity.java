package com.hariomsonihs.notesaura.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.AdminFeaturedCoursesAdapter;
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.utils.CourseDataManager;

public class AdminManageFeaturedCoursesActivity extends AppCompatActivity implements CourseDataManager.DataUpdateListener {
    private RecyclerView recyclerView;
    private AdminFeaturedCoursesAdapter adapter;
    private List<Course> allCourses;
    private CourseDataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_manage_featured_courses);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manage Featured Courses");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initViews();
        setupRecyclerView();
        loadData();
        
        dataManager = CourseDataManager.getInstance();
        dataManager.addDataUpdateListener(this);
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.courses_recycler_view);
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        allCourses = new ArrayList<>();
        adapter = new AdminFeaturedCoursesAdapter(allCourses, new AdminFeaturedCoursesAdapter.OnCourseActionListener() {
            @Override
            public void onToggleFeatured(Course course, boolean isFeatured) {
                if (isFeatured) {
                    dataManager.addToFeatured(course.getId());
                } else {
                    dataManager.removeFromFeatured(course.getId());
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }
    
    private void loadData() {
        allCourses.clear();
        allCourses.addAll(CourseDataManager.getInstance().getAllCourses());
        adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onCoursesUpdated() {
        runOnUiThread(this::loadData);
    }
    
    @Override
    public void onCategoriesUpdated() {
        // Not needed
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataManager.removeDataUpdateListener(this);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
package com.hariomsonihs.notesaura.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.CourseDetailActivity;
import com.hariomsonihs.notesaura.adapters.CourseGridAdapter;
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.interfaces.OnCourseClickListener;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.CourseDataManager;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;

public class CategoryCoursesFragment extends Fragment implements OnCourseClickListener, CourseDataManager.DataUpdateListener {
    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";
    
    private RecyclerView coursesRecyclerView;
    private TextView categoryTitle;
    private CourseGridAdapter courseAdapter;
    private List<Course> courses;
    private String categoryId;
    private String categoryName;

    public static CategoryCoursesFragment newInstance(String categoryId, String categoryName) {
        CategoryCoursesFragment fragment = new CategoryCoursesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_courses, container, false);

        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }

        initializeViews(view);
        setupRecyclerView();
        loadCourses();

        return view;
    }

    private void initializeViews(View view) {
        categoryTitle = view.findViewById(R.id.category_title);
        coursesRecyclerView = view.findViewById(R.id.courses_recycler_view);
        
        if (categoryName != null) {
            categoryTitle.setText(categoryName + " Courses");
        }
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        coursesRecyclerView.setLayoutManager(gridLayoutManager);
        coursesRecyclerView.setHasFixedSize(true);
        courses = new ArrayList<>();
        courseAdapter = new CourseGridAdapter(courses, this);
        coursesRecyclerView.setAdapter(courseAdapter);
    }

    private CourseDataManager dataManager;

    private void loadCourses() {
        courses.clear();
        if (dataManager == null) {
            dataManager = CourseDataManager.getInstance();
        }
        
        List<Course> categoryCourses = dataManager.getCoursesByCategory(categoryId);
        
        android.util.Log.d("CategoryCourses", "Category: " + categoryId + ", Courses found: " + categoryCourses.size());
        
        if (categoryCourses.isEmpty()) {
            // Show simple empty message
            android.util.Log.d("CategoryCourses", "Showing empty state for category: " + categoryId);
            coursesRecyclerView.setVisibility(View.GONE);
            
            // Post to ensure view is ready
            coursesRecyclerView.post(() -> {
                View rootView = getView();
                if (rootView != null && getContext() != null) {
                    ViewGroup container = (ViewGroup) rootView;
                    
                    // Remove any existing empty views
                    for (int i = container.getChildCount() - 1; i >= 0; i--) {
                        View child = container.getChildAt(i);
                        if (child != categoryTitle && child != coursesRecyclerView) {
                            container.removeView(child);
                        }
                    }
                    
                    // Create animated empty state view
                    View emptyView = EmptyStateHelper.createEmptyStateView(
                        getContext(),
                        "We are working on it",
                        "Courses under this category will be available soon"
                    );
                    
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    );
                    container.addView(emptyView, params);
                    android.util.Log.d("CategoryCourses", "Empty text added successfully with container children: " + container.getChildCount());
                }
            });
        } else {
            android.util.Log.d("CategoryCourses", "Showing courses for category: " + categoryId);
            coursesRecyclerView.setVisibility(View.VISIBLE);
            
            View rootView = getView();
            if (rootView != null) {
                ViewGroup container = (ViewGroup) rootView;
                // Remove any existing empty views
                for (int i = container.getChildCount() - 1; i >= 0; i--) {
                    View child = container.getChildAt(i);
                    if (child != categoryTitle && child != coursesRecyclerView) {
                        container.removeView(child);
                    }
                }
            }
            courses.addAll(categoryCourses);
        }
        
        courseAdapter.notifyDataSetChanged();
    }
    
    // Removed getCategoryName: now using real categoryId
    @Override
    public void onCategoriesUpdated() {
        // If category is deleted, pop fragment
        if (dataManager != null && !dataManager.getCategoriesMap().containsKey(categoryId)) {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onCoursesUpdated() {
        loadCourses();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dataManager == null) {
            dataManager = CourseDataManager.getInstance();
        }
        dataManager.addDataUpdateListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dataManager != null) {
            dataManager.removeDataUpdateListener(this);
        }
    }
    
    private Course createCourse(String id, String title, String description, String category, float rating, int duration, double price) {
        Course course = new Course(id, title, description, category);
        course.setRating(rating);
        course.setDuration(duration);
        course.setPrice(price);
        course.setFree(price == 0.0);
        course.setDifficulty("Beginner");
        course.setEnrolledCount((int)(Math.random() * 500) + 50);
        return course;
    }

    @Override
    public void onCourseClick(Course course) {
        Intent intent = new Intent(getContext(), CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE_ID, course.getId());
        startActivity(intent);
    }

    @Override
    public void onEnrollClick(Course course) {
        // Handle enrollment
    }
}
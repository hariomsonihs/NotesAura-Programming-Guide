package com.hariomsonihs.notesaura.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.CourseDetailActivity;
import com.hariomsonihs.notesaura.adapters.CourseGridAdapter;
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.interfaces.OnCourseClickListener;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.CourseDataManager;

public class CoursesFragment extends Fragment implements OnCourseClickListener {
    private RecyclerView coursesRecyclerView;
    private CourseGridAdapter courseAdapter;
    private List<Course> courses;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadCourses();

        return view;
    }

    private void initializeViews(View view) {
        coursesRecyclerView = view.findViewById(R.id.courses_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadCourses();
            swipeRefresh.setRefreshing(false);
        });
        swipeRefresh.setColorSchemeResources(
            R.color.primary_start,
            R.color.secondary_start,
            R.color.accent_green
        );
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        coursesRecyclerView.setLayoutManager(gridLayoutManager);
        coursesRecyclerView.setHasFixedSize(true);
        courses = new ArrayList<>();
        courseAdapter = new CourseGridAdapter(courses, this);
        coursesRecyclerView.setAdapter(courseAdapter);
    }

    private void loadCourses() {
        courses.clear();
        
        CourseDataManager dataManager = CourseDataManager.getInstance();
        courses.addAll(dataManager.getAllCourses());
        
        courseAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCourseClick(Course course) {
        Intent intent = new Intent(getContext(), CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE_ID, course.getId());
        startActivity(intent);
    }

    @Override
    public void onEnrollClick(Course course) {
        // Handle enrollment - navigate to course detail for enrollment
        Intent intent = new Intent(getContext(), CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE_ID, course.getId());
        startActivity(intent);
    }
}
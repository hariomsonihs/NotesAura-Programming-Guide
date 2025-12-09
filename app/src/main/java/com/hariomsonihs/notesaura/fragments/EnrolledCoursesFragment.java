package com.hariomsonihs.notesaura.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;
import java.util.List;
import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.activities.CourseDetailActivity;
import com.hariomsonihs.notesaura.adapters.EnrolledCourseAdapter;
import com.hariomsonihs.notesaura.models.EnrolledCourse;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.FirebaseDBHelper;

public class EnrolledCoursesFragment extends Fragment implements EnrolledCourseAdapter.OnEnrolledCourseClickListener {
    private RecyclerView coursesRecyclerView;
    private EnrolledCourseAdapter courseAdapter;
    private List<EnrolledCourse> enrolledCourses;
    private FirebaseDBHelper dbHelper;
    private SwipeRefreshLayout swipeRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_enrolled_courses, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        loadEnrolledCourses();

        return view;
    }

    private void initializeViews(View view) {
        coursesRecyclerView = view.findViewById(R.id.courses_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        dbHelper = FirebaseDBHelper.getInstance();
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadEnrolledCourses();
            swipeRefresh.setRefreshing(false);
        });
        swipeRefresh.setColorSchemeResources(
            R.color.primary_start,
            R.color.secondary_start,
            R.color.accent_green
        );
    }

    private void setupRecyclerView() {
        coursesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        enrolledCourses = new ArrayList<>();
        courseAdapter = new EnrolledCourseAdapter(enrolledCourses, this);
        coursesRecyclerView.setAdapter(courseAdapter);
    }

    private void loadEnrolledCourses() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            dbHelper.getUserEnrolledCourses(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        enrolledCourses.clear();
                        queryDocumentSnapshots.forEach(document -> {
                            String courseId = document.getId();
                            String courseName = document.getString("courseName");
                            String category = document.getString("category");
                            Long progressLong = document.getLong("progressPercentage");
                            int progress = progressLong != null ? progressLong.intValue() : 0;
                            
                            if (courseName != null) {
                                EnrolledCourse course = new EnrolledCourse(courseId, courseName, category != null ? category : "General");
                                course.setProgressPercentage(progress);
                                if (document.getDate("enrollmentDate") != null) {
                                    course.setEnrollmentDate(document.getDate("enrollmentDate"));
                                }
                                if (document.getDate("lastAccessed") != null) {
                                    course.setLastAccessed(document.getDate("lastAccessed"));
                                }
                                enrolledCourses.add(course);
                            }
                        });
                        courseAdapter.notifyDataSetChanged();
                    });
        }
    }

    @Override
    public void onEnrolledCourseClick(EnrolledCourse course) {
        Intent intent = new Intent(getContext(), CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE_ID, course.getCourseId());
        startActivity(intent);
    }
}
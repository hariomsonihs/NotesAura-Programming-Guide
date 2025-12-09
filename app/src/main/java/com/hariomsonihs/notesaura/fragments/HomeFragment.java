package com.hariomsonihs.notesaura.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.CourseAdapter;
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.interfaces.OnCourseClickListener;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.CourseDataManager;

public class HomeFragment extends Fragment implements OnCourseClickListener, CourseDataManager.DataUpdateListener {
    private RecyclerView featuredCoursesMainRecyclerView;
    private RecyclerView programmingCoursesRecyclerView;
    private RecyclerView webCoursesRecyclerView;
    private RecyclerView androidCoursesRecyclerView;
    private RecyclerView dataCoursesRecyclerView;
    private RecyclerView recentCoursesRecyclerView;

    private CourseAdapter featuredCoursesMainAdapter;
    private CourseAdapter programmingCoursesAdapter;
    private CourseAdapter webCoursesAdapter;
    private CourseAdapter androidCoursesAdapter;
    private CourseAdapter dataCoursesAdapter;
    private CourseAdapter recentCoursesAdapter;

    private List<Course> featuredCoursesMain;
    private List<Course> programmingCourses;
    private List<Course> webCourses;
    private List<Course> androidCourses;
    private List<Course> dataCourses;
    private List<Course> recentCourses;
    private SwipeRefreshLayout swipeRefresh;
    private CourseDataManager courseDataManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initializeViews(view);
        courseDataManager = CourseDataManager.getInstance();
        courseDataManager.addDataUpdateListener(this);
        setupRecyclerViews();
        setupSwipeRefresh();
        
        // Load data immediately with cached content
        loadDataImmediate();
        
        // Setup click listeners after initial load
        setupClickListeners(view);

        return view;
    }

    private void initializeViews(View view) {
        featuredCoursesMainRecyclerView = view.findViewById(R.id.featured_courses_main_recycler_view);
        programmingCoursesRecyclerView = view.findViewById(R.id.programming_courses_recycler_view);
        webCoursesRecyclerView = view.findViewById(R.id.web_courses_recycler_view);
        androidCoursesRecyclerView = view.findViewById(R.id.android_courses_recycler_view);
        dataCoursesRecyclerView = view.findViewById(R.id.data_courses_recycler_view);
        recentCoursesRecyclerView = view.findViewById(R.id.recent_courses_recycler_view);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        
        // Set personalized greeting
        setupPersonalizedGreeting(view);
    }
    
    private void setupPersonalizedGreeting(View view) {
        android.widget.TextView greetingText = view.findViewById(R.id.greeting_text);
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser != null && greetingText != null) {
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            
            String userName = "User";
            if (displayName != null && !displayName.isEmpty()) {
                userName = displayName.split(" ")[0]; // Get first name only
            } else if (email != null) {
                userName = email.split("@")[0]; // Use email prefix
            }
            
            greetingText.setText("Hello " + userName + "! 👋✨");
        }
    }

    private void setupClickListeners(View view) {
        // Featured courses view all
        View seeAllFeatured = view.findViewById(R.id.see_all_featured);
        if (seeAllFeatured != null) {
            seeAllFeatured.setOnClickListener(v -> {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CoursesFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Dynamic Explore buttons for categories
        Map<String, String> categoriesMap = courseDataManager.getCategoriesMap();

        // Helper to set up explore button
        setupExploreButton(view, R.id.explore_programming, "programming", categoriesMap);
        setupExploreButton(view, R.id.explore_web, "web_development", categoriesMap);
        setupExploreButton(view, R.id.explore_android, "app_development", categoriesMap);
        setupExploreButton(view, R.id.explore_data, "data_science", categoriesMap);
    }

    private void setupExploreButton(View view, int buttonId, String categoryId, Map<String, String> categoriesMap) {
        View button = view.findViewById(buttonId);
        if (button != null) {
            String categoryName = categoriesMap.get(categoryId);
            if (categoryName != null) {
                button.setOnClickListener(v -> {
                    CategoryCoursesFragment fragment = CategoryCoursesFragment.newInstance(categoryId, categoryName);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });
                button.setEnabled(true);
                button.setAlpha(1f);
            } else {
                // Hide or disable button if category doesn't exist
                button.setEnabled(false);
                button.setAlpha(0.5f);
            }
        }
    }

    private void setupRecyclerViews() {
        // Featured Courses Main RecyclerView
        featuredCoursesMainRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        featuredCoursesMain = new ArrayList<>();
        featuredCoursesMainAdapter = new CourseAdapter(featuredCoursesMain, this, false);
        featuredCoursesMainRecyclerView.setAdapter(featuredCoursesMainAdapter);

        // Programming Courses RecyclerView
        programmingCoursesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        programmingCourses = new ArrayList<>();
        programmingCoursesAdapter = new CourseAdapter(programmingCourses, this, false);
        programmingCoursesRecyclerView.setAdapter(programmingCoursesAdapter);

        // Web Courses RecyclerView
        webCoursesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        webCourses = new ArrayList<>();
        webCoursesAdapter = new CourseAdapter(webCourses, this, false);
        webCoursesRecyclerView.setAdapter(webCoursesAdapter);

        // Android Courses RecyclerView
        androidCoursesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        androidCourses = new ArrayList<>();
        androidCoursesAdapter = new CourseAdapter(androidCourses, this, false);
        androidCoursesRecyclerView.setAdapter(androidCoursesAdapter);

        // Data Science Courses RecyclerView
        dataCoursesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        dataCourses = new ArrayList<>();
        dataCoursesAdapter = new CourseAdapter(dataCourses, this, false);
        dataCoursesRecyclerView.setAdapter(dataCoursesAdapter);

        // Recent Courses RecyclerView
        recentCoursesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recentCourses = new ArrayList<>();
        recentCoursesAdapter = new CourseAdapter(recentCourses, this, false);
        recentCoursesRecyclerView.setAdapter(recentCoursesAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            loadData();
            swipeRefresh.setRefreshing(false);
        });
        swipeRefresh.setColorSchemeResources(
                R.color.primary_start,
                R.color.secondary_start,
                R.color.accent_green
        );
    }

    private void loadDataImmediate() {
        // Load with cached data first for instant display
        loadFeaturedCoursesMain();
        loadProgrammingCourses();
        loadWebCourses();
        loadAndroidCourses();
        loadDataScienceCourses();
        loadRecentCourses();
    }
    
    private void loadData() {
        loadDataImmediate();
    }

    private void loadFeaturedCoursesMain() {
        featuredCoursesMain.clear();
        List<Course> featured = courseDataManager.getFeaturedCourses();
        
        if (featured.isEmpty()) {
            // Fallback: show first 3 courses if no featured courses set
            List<Course> allCourses = courseDataManager.getAllCourses();
            for (int i = 0; i < Math.min(3, allCourses.size()); i++) {
                featuredCoursesMain.add(allCourses.get(i));
            }
        } else {
            featuredCoursesMain.addAll(featured);
        }

        featuredCoursesMainAdapter.notifyDataSetChanged();
    }

    private void loadProgrammingCourses() {
        programmingCourses.clear();
        programmingCourses.addAll(courseDataManager.getCoursesByCategory("programming"));
        programmingCoursesAdapter.notifyDataSetChanged();
    }

    private void loadWebCourses() {
        webCourses.clear();
        webCourses.addAll(courseDataManager.getCoursesByCategory("web_development"));
        webCoursesAdapter.notifyDataSetChanged();
    }

    private void loadAndroidCourses() {
        androidCourses.clear();
        androidCourses.addAll(courseDataManager.getCoursesByCategory("app_development"));
        androidCoursesAdapter.notifyDataSetChanged();
    }

    private void loadDataScienceCourses() {
        dataCourses.clear();
        dataCourses.addAll(courseDataManager.getCoursesByCategory("data_science"));
        dataCoursesAdapter.notifyDataSetChanged();
    }

    private void loadRecentCourses() {
        recentCourses.clear();
        
        // Get personalized continue learning courses
        List<Course> continueLearning = courseDataManager.getContinueLearningCourses();
        recentCourses.addAll(continueLearning);

        recentCoursesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCourseClick(Course course) {
        // Track course access for continue learning
        courseDataManager.trackCourseAccess(course.getId());
        
        Intent intent = new Intent(getContext(), com.hariomsonihs.notesaura.activities.CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE_ID, course.getId());
        startActivity(intent);
    }

    @Override
    public void onEnrollClick(Course course) {
        // Handle enroll click
    }

    @Override
    public void onCoursesUpdated() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(this::loadData);
        }
    }

    @Override
    public void onCategoriesUpdated() {
        // Not needed in HomeFragment
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (courseDataManager != null) {
            courseDataManager.removeDataUpdateListener(this);
        }
    }
}
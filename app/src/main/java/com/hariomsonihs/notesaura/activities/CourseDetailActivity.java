package com.hariomsonihs.notesaura.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.AppBarLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hariomsonihs.notesaura.R;
import com.hariomsonihs.notesaura.adapters.ExerciseAdapter;
import com.hariomsonihs.notesaura.models.Exercise;
import com.hariomsonihs.notesaura.interfaces.OnExerciseClickListener;
import com.hariomsonihs.notesaura.utils.Constants;
import com.hariomsonihs.notesaura.utils.CourseDataManager;
import com.hariomsonihs.notesaura.utils.FirebaseDBHelper;
import com.hariomsonihs.notesaura.utils.EmptyStateHelper;
import com.hariomsonihs.notesaura.utils.OfflineManager;
import com.hariomsonihs.notesaura.adapters.ExerciseDownloadAdapter;
import com.hariomsonihs.notesaura.models.Course;
import com.bumptech.glide.Glide;
import android.text.TextUtils;

public class CourseDetailActivity extends AppCompatActivity implements OnExerciseClickListener {

    private TextView courseTitle, tabDescription, tabIndex, progressText, progressPercentage;
    private Button enrollButton;
    private ImageView downloadButton;
    private View headerBackground;
    private ImageView backButton, courseIcon, rateCourseButton;
    private LinearLayout contentContainer, progressContainer;
    private ProgressBar courseProgressBar;
    private RecyclerView exercisesRecyclerView;
    private ExerciseAdapter exerciseAdapter;
    private AppBarLayout appBarLayout;
    private SwipeRefreshLayout swipeRefresh;
    private List<Exercise> exercises = new ArrayList<>();
    private boolean showingDescription = true;
    private String courseId;
    private FirebaseDBHelper dbHelper;
    private boolean isEnrolled = false;
    private boolean firestoreDataLoaded = false;
    private OfflineManager offlineManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setDecorFitsSystemWindows(false);
        
        setContentView(R.layout.activity_course_detail);

        courseId = getIntent().getStringExtra(Constants.KEY_COURSE_ID);
        if (courseId == null) {
            courseId = getIntent().getStringExtra("course_id"); // Try backup key
        }
        
        android.util.Log.d("CourseDetailActivity", "Received courseId: " + courseId);
        android.util.Log.d("CourseDetailActivity", "Intent extras: " + getIntent().getExtras());
        
        // Check if this came from notification
        boolean fromNotification = getIntent().getBooleanExtra("from_notification", false);
        android.util.Log.d("CourseDetailActivity", "From notification: " + fromNotification);
        
        if (courseId == null || courseId.isEmpty()) {
            android.util.Log.w("CourseDetailActivity", "No courseId found, using default");
            courseId = "java_programming";
            Toast.makeText(this, "Loading default course (no ID provided)", Toast.LENGTH_SHORT).show();
        } else {
            // Show toast to confirm we got the course ID
            android.util.Log.d("CourseDetailActivity", "Successfully received courseId: " + courseId);
        }

        dbHelper = FirebaseDBHelper.getInstance();
        offlineManager = OfflineManager.getInstance(this);
        
        // Track course access for continue learning
        CourseDataManager.getInstance().trackCourseAccess(courseId);

        initializeViews();
        setupWindowInsets();
        
        // Debug: List all available courses in Firestore
        debugListAllCourses();
        
        loadCourseData();
        checkEnrollmentStatus();
        setupClickListeners();
        
        setDescriptionTabActive();
        showDescriptionContent();
    }
    
    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                refreshCourseData();
            });
            swipeRefresh.setColorSchemeResources(
                R.color.primary_start,
                R.color.secondary_start,
                R.color.accent_green
            );
        }
    }
    
    private void refreshCourseData() {
        if (com.hariomsonihs.notesaura.utils.NetworkUtil.isNetworkAvailable(this)) {
            loadCourseData();
            checkEnrollmentStatus();
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
        } else {
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
            Toast.makeText(this, "Turn on your internet to refresh content", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void debugListAllCourses() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("courses")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                android.util.Log.d("CourseDetailActivity", "=== DEBUG: All courses in Firestore ===");
                android.util.Log.d("CourseDetailActivity", "Total courses found: " + queryDocumentSnapshots.size());
                
                for (com.google.firebase.firestore.QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    String title = doc.getString("title");
                    android.util.Log.d("CourseDetailActivity", "Course ID: " + doc.getId() + ", Title: " + title);
                }
                
                android.util.Log.d("CourseDetailActivity", "Looking for course ID: " + courseId);
                android.util.Log.d("CourseDetailActivity", "=== END DEBUG ===");
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("CourseDetailActivity", "Failed to list courses for debugging", e);
            });
    }

    private void initializeViews() {
        courseTitle = findViewById(R.id.course_title);
        enrollButton = findViewById(R.id.enroll_button);
        downloadButton = findViewById(R.id.download_button);
        headerBackground = findViewById(R.id.header_background);
        backButton = findViewById(R.id.back_button);
        courseIcon = findViewById(R.id.course_icon);
        rateCourseButton = findViewById(R.id.rate_course_button);
        tabDescription = findViewById(R.id.tab_description);
        tabIndex = findViewById(R.id.tab_index);
        contentContainer = findViewById(R.id.content_container);
        appBarLayout = findViewById(R.id.app_bar);
        progressContainer = findViewById(R.id.progress_container);
        progressText = findViewById(R.id.progress_text);
        progressPercentage = findViewById(R.id.progress_percentage);
        courseProgressBar = findViewById(R.id.course_progress_bar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        
        setupSwipeRefresh();
    }
    
    private void setupWindowInsets() {
        View bottomContainer = findViewById(R.id.bottom_button_container);
        
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(bottomContainer, (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                v.getPaddingStart(),
                v.getPaddingTop(),
                v.getPaddingEnd(),
                systemBars.bottom + 20
            );
            return androidx.core.view.WindowInsetsCompat.CONSUMED;
        });
    }

    private void loadCourseData() {
        android.util.Log.d("CourseDetailActivity", "Loading course data for ID: " + courseId);
        
        // Load course from Firestore using courses collection
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("courses")
            .document(courseId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                
                android.util.Log.d("CourseDetailActivity", "Firestore query completed. Document exists: " + documentSnapshot.exists());
                android.util.Log.d("CourseDetailActivity", "Document ID queried: " + documentSnapshot.getId());
                android.util.Log.d("CourseDetailActivity", "Document data: " + documentSnapshot.getData());
                
                if (documentSnapshot.exists()) {
                    String title = documentSnapshot.getString("title");
                    String description = documentSnapshot.getString("description");
                    String imageUrl = documentSnapshot.getString("imageUrl");
                    
                    android.util.Log.d("CourseDetailActivity", "Document data: title=" + title + ", description=" + description);
                    
                    if (title != null && !title.isEmpty()) {
                        courseTitle.setText(title);
                        android.util.Log.d("CourseDetailActivity", "Successfully loaded course from Firestore: " + title);
                        
                        firestoreDataLoaded = true;
                        
                        // Load exercises from Firestore
                        List<Map<String, Object>> exercisesList = (List<Map<String, Object>>) documentSnapshot.get("exercises");
                        if (exercisesList != null && !exercisesList.isEmpty()) {
                            exercises.clear();
                            for (Map<String, Object> exerciseData : exercisesList) {
                                Exercise exercise = new Exercise(
                                    (String) exerciseData.get("id"),
                                    (String) exerciseData.get("title"),
                                    (String) exerciseData.get("description"),
                                    (String) exerciseData.get("contentPath"),
                                    courseId
                                );
                                exercises.add(exercise);
                            }
                            android.util.Log.d("CourseDetailActivity", "Loaded " + exercises.size() + " exercises from Firestore");
                        }
                    } else {
                        android.util.Log.w("CourseDetailActivity", "Course title is null or empty, falling back to local data");
                        loadFromLocalData();
                    }
                } else {
                    android.util.Log.w("CourseDetailActivity", "Course document not found in Firestore for ID: " + courseId);
                    
                    // If from notification and course not found, try to load a popular course instead
                    boolean fromNotification = getIntent().getBooleanExtra("from_notification", false);
                    if (fromNotification) {
                        courseId = "java_programming"; // Fallback to a known course
                        loadCourseData(); // Retry with fallback course
                        return;
                    } else {
                        loadFromLocalData();
                    }
                }
                setCourseVisuals();
            })
            .addOnFailureListener(e -> {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
                
                android.util.Log.e("CourseDetailActivity", "Firestore query failed for course ID: " + courseId, e);
                
                if (!com.hariomsonihs.notesaura.utils.NetworkUtil.isNetworkAvailable(this)) {
                    Toast.makeText(this, "Turn on your internet to load latest content", Toast.LENGTH_SHORT).show();
                }
                
                loadFromLocalData();
                setCourseVisuals();
            });
    }
    
    private void loadFromLocalData() {
        if (!firestoreDataLoaded) {
            CourseDataManager dataManager = CourseDataManager.getInstance();
            courseTitle.setText(dataManager.getCourseTitle(courseId));
            exercises.clear();
            exercises.addAll(dataManager.getExercises(courseId));
            android.util.Log.d("CourseDetailActivity", "Loaded local data for courseId: " + courseId);
        } else {
            android.util.Log.d("CourseDetailActivity", "Skipping local data load - Firestore data already loaded");
        }
    }
    
    private void loadExercisesFromFirestore() {
        dbHelper.getCourseDocument(courseId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<Map<String, Object>> exercisesList = (List<Map<String, Object>>) documentSnapshot.get("exercises");
                    if (exercisesList != null && !exercisesList.isEmpty()) {
                        exercises.clear();
                        for (Map<String, Object> exerciseData : exercisesList) {
                            Exercise exercise = new Exercise(
                                (String) exerciseData.get("id"),
                                (String) exerciseData.get("title"),
                                (String) exerciseData.get("description"),
                                (String) exerciseData.get("contentPath"),
                                courseId
                            );
                            exercises.add(exercise);
                        }
                        android.util.Log.d("CourseDetailActivity", "Loaded " + exercises.size() + " exercises from Firestore");
                    } else {
                        // Fallback to local data
                        CourseDataManager dataManager = CourseDataManager.getInstance();
                        exercises.clear();
                        exercises.addAll(dataManager.getExercises(courseId));
                        android.util.Log.d("CourseDetailActivity", "No exercises in Firestore, loaded " + exercises.size() + " from local data");
                    }
                } else {
                    // Fallback to local data
                    CourseDataManager dataManager = CourseDataManager.getInstance();
                    exercises.clear();
                    exercises.addAll(dataManager.getExercises(courseId));
                }
            })
            .addOnFailureListener(e -> {
                // Fallback to local data
                CourseDataManager dataManager = CourseDataManager.getInstance();
                exercises.clear();
                exercises.addAll(dataManager.getExercises(courseId));
                android.util.Log.e("CourseDetailActivity", "Failed to load exercises from Firestore", e);
            });
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> onBackPressed());
        enrollButton.setOnClickListener(v -> {
            if (isEnrolled) {
                if (!exercises.isEmpty()) {
                    Exercise nextExercise = getNextIncompleteExercise();
                    if (nextExercise != null) {
                        onExerciseClick(nextExercise);
                    } else {
                        onExerciseClick(exercises.get(0)); // Fallback to first exercise
                    }
                }
            } else {
                enrollInCourse();
            }
        });
        downloadButton.setOnClickListener(v -> {
            if (!isEnrolled) {
                Toast.makeText(this, "Please enroll in the course first", Toast.LENGTH_SHORT).show();
                return;
            }
            showDownloadDialog();
        });
        if (rateCourseButton != null) {
            rateCourseButton.setOnClickListener(v -> showRatingDialog());
        }
        tabDescription.setOnClickListener(v -> showDescriptionTab());
        tabIndex.setOnClickListener(v -> showIndexTab());
    }

    private void checkEnrollmentStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            isEnrolled = false;
            updateEnrollButton();
            progressContainer.setVisibility(View.GONE);
            return;
        }
        dbHelper.getEnrollmentDocument(user.getUid(), courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isEnrolled = documentSnapshot.exists();
                    updateEnrollButton();
                    if (isEnrolled && !showingDescription) {
                        loadExerciseCompletionStatus();
                    } else if (!isEnrolled) {
                        progressContainer.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    isEnrolled = false;
                    updateEnrollButton();
                    progressContainer.setVisibility(View.GONE);
                });
    }

    private void setCourseVisuals() {
        Course course = CourseDataManager.getInstance().getCourse(courseId);
        String category = getCategoryFromCourseId(courseId).toLowerCase();
        int backgroundRes = R.drawable.bg_gradient_secondary;
        
        if (category.contains("java")) {
            backgroundRes = R.drawable.bg_gradient_java;
        } else if (category.contains("python")) {
            backgroundRes = R.drawable.bg_gradient_python;
        } else if (category.contains("web")) {
            backgroundRes = R.drawable.bg_gradient_web;
        } else if (category.contains("android") || category.contains("app")) {
            backgroundRes = R.drawable.bg_gradient_android;
        } else if (category.contains("data") || category.contains("machine")) {
            backgroundRes = R.drawable.bg_gradient_data;
        }

        headerBackground.setBackgroundResource(backgroundRes);
        
        // Set course image from URL or default background
        if (course != null && !TextUtils.isEmpty(course.getImageUrl())) {
            courseIcon.setImageTintList(null);
            Glide.with(this)
                .load(course.getImageUrl())
                .placeholder(getDefaultIcon(category))
                .error(getDefaultIcon(category))
                .centerCrop()
                .into(courseIcon);
            headerBackground.setAlpha(0.4f);
        } else {
            courseIcon.setImageResource(getDefaultIcon(category));
            headerBackground.setAlpha(1.0f);
        }
    }
    
    private int getDefaultIcon(String category) {
        if (category.contains("java") || category.contains("python")) {
            return R.drawable.ic_code;
        } else if (category.contains("web")) {
            return R.drawable.ic_web;
        } else if (category.contains("android") || category.contains("app")) {
            return R.drawable.ic_phone;
        } else if (category.contains("data") || category.contains("machine")) {
            return R.drawable.ic_analytics;
        } else {
            return R.drawable.ic_code;
        }
    }

    private void updateEnrollButton() {
        enrollButton.setText(isEnrolled ? "CONTINUE LEARNING" : "LET'S GO!");
        if (downloadButton != null) {
            // Show download button only when enrolled and exercises exist
            downloadButton.setVisibility(isEnrolled && !exercises.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void showDescriptionTab() {
        showingDescription = true;
        setDescriptionTabActive();
        setAppBarDraggable(false);
        showDescriptionContent();
    }
    
    private void setDescriptionTabActive() {
        tabDescription.setTextColor(getColor(R.color.text_primary_light));
        tabDescription.setTypeface(null, android.graphics.Typeface.BOLD);
        tabIndex.setTextColor(getColor(R.color.text_secondary_light));
        tabIndex.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void showDescriptionContent() {
        contentContainer.removeAllViews();
        loadCourseDescriptionFromFirebase();
    }

    private void loadCourseDescriptionFromFirebase() {
        dbHelper.getCourseDocument(courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> objectives = null;
                    List<String> audience = null;

                    if (documentSnapshot.exists()) {
                        objectives = (List<String>) documentSnapshot.get("learningObjectives");
                        audience = (List<String>) documentSnapshot.get("targetAudience");
                    }

                    if (objectives == null || objectives.isEmpty()) {
                        objectives = getDefaultLearningObjectives(courseId);
                    }
                    if (audience == null || audience.isEmpty()) {
                        audience = getDefaultTargetAudience(courseId);
                    }

                    createDescriptionLayout(objectives, audience);
                })
                .addOnFailureListener(e -> {
                    List<String> objectives = getDefaultLearningObjectives(courseId);
                    List<String> audience = getDefaultTargetAudience(courseId);
                    createDescriptionLayout(objectives, audience);
                });
    }

    private void createDescriptionLayout(List<String> learningObjectives, List<String> targetAudience) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 60, 60, 120);

        TextView learnTitle = new TextView(this);
        learnTitle.setText("What Will I Learn?");
        learnTitle.setTextSize(20);
        learnTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        learnTitle.setTextColor(getColor(R.color.text_primary_light));
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, 0, 0, 48);
        learnTitle.setLayoutParams(titleParams);
        layout.addView(learnTitle);

        for (String objective : learningObjectives) {
            TextView objText = new TextView(this);
            objText.setText("◆ " + objective);
            objText.setTextSize(16);
            objText.setTextColor(getColor(R.color.text_secondary_light));
            objText.setLineSpacing(12, 1.0f);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 24);
            objText.setLayoutParams(params);
            layout.addView(objText);
        }

        TextView audienceTitle = new TextView(this);
        audienceTitle.setText("Who is the target audience?");
        audienceTitle.setTextSize(20);
        audienceTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        audienceTitle.setTextColor(getColor(R.color.text_primary_light));
        LinearLayout.LayoutParams audTitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        audTitleParams.setMargins(0, 48, 0, 48);
        audienceTitle.setLayoutParams(audTitleParams);
        layout.addView(audienceTitle);

        for (String aud : targetAudience) {
            TextView audText = new TextView(this);
            audText.setText("◆ " + aud);
            audText.setTextSize(16);
            audText.setTextColor(getColor(R.color.text_secondary_light));
            audText.setLineSpacing(12, 1.0f);
            LinearLayout.LayoutParams audParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            audParams.setMargins(0, 0, 0, 24);
            audText.setLayoutParams(audParams);
            layout.addView(audText);
        }

        contentContainer.addView(layout);
    }

    private void showIndexTab() {
        showingDescription = false;
        tabIndex.setTextColor(getColor(R.color.text_primary_light));
        tabIndex.setTypeface(null, android.graphics.Typeface.BOLD);
        tabDescription.setTextColor(getColor(R.color.text_secondary_light));
        tabDescription.setTypeface(null, android.graphics.Typeface.NORMAL);
        setAppBarDraggable(true);
        showExercisesList();
        if (isEnrolled) {
            updateProgressDisplay();
        }
    }

    private void setAppBarDraggable(boolean draggable) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) appBarLayout.getChildAt(0).getLayoutParams();
        if (draggable) {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        } else {
            params.setScrollFlags(0);
            appBarLayout.setExpanded(true, true);
        }
    }

    private void showExercisesList() {
        contentContainer.removeAllViews();
        
        if (exercises.isEmpty()) {
            // Show empty state with animation
            EmptyStateHelper.showEmptyState(
                contentContainer,
                "We are working on it",
                "Exercises will be available soon"
            );
        } else {
            if (exercisesRecyclerView == null) {
                exercisesRecyclerView = new RecyclerView(this);
                exercisesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                exerciseAdapter = new ExerciseAdapter(exercises, this);
                exerciseAdapter.setOfflineManager(offlineManager, courseId);
                exercisesRecyclerView.setAdapter(exerciseAdapter);
                
                // Add extra padding to prevent last item from being hidden
                exercisesRecyclerView.setPadding(0, 0, 0, 100);
                exercisesRecyclerView.setClipToPadding(false);
            }
            contentContainer.addView(exercisesRecyclerView);
            if (isEnrolled) {
                loadExerciseCompletionStatus();
            }
            // Update download button visibility when exercises are loaded
            updateEnrollButton();
        }
    }

    private List<String> getDefaultLearningObjectives(String courseId) {
        List<String> objectives = new ArrayList<>();
        if (courseId.contains("python")) {
            objectives.add("Understand and implement basic Python Code.");
            objectives.add("Solid understanding of Python programming fundamentals.");
            objectives.add("Create small programs with Python.");
        } else if (courseId.contains("java")) {
            objectives.add("Master Java programming fundamentals and OOP concepts.");
            objectives.add("Build robust applications using Java libraries.");
            objectives.add("Understand exception handling and file operations.");
        } else {
            objectives.add("Master the fundamentals of programming concepts.");
            objectives.add("Build practical projects to strengthen skills.");
            objectives.add("Understand best practices and industry standards.");
        }
        return objectives;
    }

    private List<String> getDefaultTargetAudience(String courseId) {
        List<String> audience = new ArrayList<>();
        if (courseId.contains("python")) {
            audience.add("Complete Programming Beginners.");
            audience.add("People who want to learn Python for data science.");
        } else if (courseId.contains("java")) {
            audience.add("Beginners who want to learn object-oriented programming.");
            audience.add("Students preparing for technical interviews.");
        } else {
            audience.add("Beginners who want to start their programming journey.");
            audience.add("Students looking to enhance their technical skills.");
        }
        return audience;
    }

    private void enrollInCourse() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Check if user is premium first
            dbHelper.getUserDocument(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        boolean isPremium = documentSnapshot.getBoolean("premium") != null ? 
                                documentSnapshot.getBoolean("premium") : false;
                        
                        double coursePrice = getCoursePrice(courseId);
                        if (coursePrice == 0.0 || isPremium) {
                            // Free course or premium user - enroll directly
                            enrollUserDirectly();
                            if (isPremium && coursePrice > 0.0) {
                                Toast.makeText(this, "Premium access granted!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Paid course for non-premium user - open payment activity
                            Intent paymentIntent = new Intent(this, PaymentActivity.class);
                            paymentIntent.putExtra(Constants.KEY_COURSE_ID, courseId);
                            paymentIntent.putExtra("course_name", courseTitle.getText().toString());
                            paymentIntent.putExtra("course_price", coursePrice);
                            startActivity(paymentIntent);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If user data fetch fails, proceed with normal flow
                        double coursePrice = getCoursePrice(courseId);
                        if (coursePrice == 0.0) {
                            enrollUserDirectly();
                        } else {
                            Intent paymentIntent = new Intent(this, PaymentActivity.class);
                            paymentIntent.putExtra(Constants.KEY_COURSE_ID, courseId);
                            paymentIntent.putExtra("course_name", courseTitle.getText().toString());
                            paymentIntent.putExtra("course_price", coursePrice);
                            startActivity(paymentIntent);
                        }
                    });
        } else {
            Toast.makeText(this, "Please login to enroll", Toast.LENGTH_SHORT).show();
        }
    }

    private void enrollUserDirectly() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Check user premium status to set correct payment status
            dbHelper.getUserDocument(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        boolean isPremium = documentSnapshot.getBoolean("premium") != null ? 
                                documentSnapshot.getBoolean("premium") : false;
                        double coursePrice = getCoursePrice(courseId);
                        
                        Map<String, Object> enrollmentData = new HashMap<>();
                        enrollmentData.put("courseId", courseId);
                        enrollmentData.put("courseName", courseTitle.getText().toString());
                        enrollmentData.put("category", getCategoryFromCourseId(courseId));
                        enrollmentData.put("enrollmentDate", new Date());
                        enrollmentData.put("progressPercentage", 0);
                        
                        if (coursePrice == 0.0) {
                            enrollmentData.put("paymentStatus", "FREE");
                        } else if (isPremium) {
                            enrollmentData.put("paymentStatus", "PREMIUM");
                            enrollmentData.put("amountPaid", 0.0);
                        } else {
                            enrollmentData.put("paymentStatus", "FREE");
                        }

                        dbHelper.getEnrollmentDocument(user.getUid(), courseId)
                                .set(enrollmentData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Successfully enrolled!", Toast.LENGTH_LONG).show();
                                    checkEnrollmentStatus();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to enroll", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        // Fallback if user data fetch fails
                        Map<String, Object> enrollmentData = new HashMap<>();
                        enrollmentData.put("courseId", courseId);
                        enrollmentData.put("courseName", courseTitle.getText().toString());
                        enrollmentData.put("category", getCategoryFromCourseId(courseId));
                        enrollmentData.put("enrollmentDate", new Date());
                        enrollmentData.put("progressPercentage", 0);
                        enrollmentData.put("paymentStatus", "FREE");

                        dbHelper.getEnrollmentDocument(user.getUid(), courseId)
                                .set(enrollmentData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Successfully enrolled!", Toast.LENGTH_LONG).show();
                                    checkEnrollmentStatus();
                                })
                                .addOnFailureListener(ex -> {
                                    Toast.makeText(this, "Failed to enroll", Toast.LENGTH_SHORT).show();
                                });
                    });
        }
    }

    private double getCoursePrice(String courseId) {
        return CourseDataManager.getInstance().getCoursePrice(courseId);
    }

    private String getCategoryFromCourseId(String courseId) {
        if (courseId.contains("java") || courseId.contains("python") || courseId.contains("c_") || 
            courseId.contains("cpp") || courseId.contains("programming")) {
            return "Programming";
        } else if (courseId.contains("html") || courseId.contains("css") || courseId.contains("javascript") || 
                   courseId.contains("web") || courseId.contains("react")) {
            return "Web Development";
        } else if (courseId.contains("android") || courseId.contains("flutter") || courseId.contains("app")) {
            return "App Development";
        } else if (courseId.contains("machine") || courseId.contains("data") || courseId.contains("ml")) {
            return "Data Science";
        } else if (courseId.contains("cheat")) {
            return "Cheat Sheets";
        }
        return "Programming";
    }

    private void loadExerciseCompletionStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || !isEnrolled || exerciseAdapter == null) {
            return;
        }
        dbHelper.getUserEnrolledCourses(user.getUid())
                .document(courseId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> completedExercises = (List<String>) documentSnapshot.get("completedExercises");
                        if (completedExercises != null) {
                            for (Exercise exercise : exercises) {
                                boolean isCompleted = completedExercises.contains(exercise.getId());
                                exercise.setCompleted(isCompleted);
                            }
                        }
                    }
                    if (exerciseAdapter != null) {
                        exerciseAdapter.notifyDataSetChanged();
                    }
                    updateProgressDisplay();
                });
    }

    @Override
    public void onExerciseClick(Exercise exercise) {
        if (isEnrolled) {
            Intent intent = new Intent(this, com.hariomsonihs.notesaura.activities.ExerciseActivity.class);
            intent.putExtra(Constants.KEY_EXERCISE_ID, exercise.getId());
            intent.putExtra(Constants.KEY_HTML_PATH, exercise.getHtmlFilePath());
            intent.putExtra(Constants.KEY_COURSE_ID, courseId);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Please enroll in the course first", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateProgressDisplay() {
        if (!isEnrolled || exercises.isEmpty()) {
            progressContainer.setVisibility(View.GONE);
            return;
        }
        
        int completedCount = 0;
        for (Exercise exercise : exercises) {
            if (exercise.isCompleted()) {
                completedCount++;
            }
        }
        
        int totalExercises = exercises.size();
        int progressPercent = totalExercises > 0 ? (completedCount * 100) / totalExercises : 0;
        
        progressText.setText(completedCount + "/" + totalExercises + " exercises");
        progressPercentage.setText(progressPercent + "%");
        courseProgressBar.setProgress(progressPercent);
        progressContainer.setVisibility(View.VISIBLE);
    }
    
    private void showRatingDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        android.view.View dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_rate_course, null);
        
        ImageView[] stars = {
            dialogView.findViewById(R.id.star1),
            dialogView.findViewById(R.id.star2),
            dialogView.findViewById(R.id.star3),
            dialogView.findViewById(R.id.star4),
            dialogView.findViewById(R.id.star5)
        };
        
        android.widget.EditText commentEditText = dialogView.findViewById(R.id.comment_edit_text);
        final int[] selectedRating = {0};
        
        // Star click listeners
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> {
                selectedRating[0] = rating;
                updateStars(stars, rating);
            });
        }
        
        builder.setView(dialogView)
                .setTitle("Rate " + courseTitle.getText().toString())
                .setPositiveButton("Submit", (dialog, which) -> {
                    if (selectedRating[0] > 0) {
                        String comment = commentEditText.getText().toString().trim();
                        submitRating(selectedRating[0], comment);
                        Toast.makeText(this, "Rating submitted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void updateStars(ImageView[] stars, int rating) {
        for (int i = 0; i < stars.length; i++) {
            if (i < rating) {
                stars[i].setImageResource(R.drawable.ic_star_filled);
            } else {
                stars[i].setImageResource(R.drawable.ic_star_border);
            }
        }
    }
    
    private void submitRating(int rating, String comment) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Map<String, Object> ratingData = new HashMap<>();
            ratingData.put("courseId", courseId);
            ratingData.put("userId", currentUser.getUid());
            ratingData.put("userName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous");
            ratingData.put("rating", rating);
            ratingData.put("comment", comment);
            ratingData.put("timestamp", System.currentTimeMillis());
            
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("course_ratings")
                .document(currentUser.getUid() + "_" + courseId)
                .set(ratingData);
        }
    }
    
    private void showDownloadDialog() {
        if (exercises.isEmpty()) {
            Toast.makeText(this, "No exercises available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_download_exercises, null);
        
        RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_exercises);
        Button selectAllBtn = dialogView.findViewById(R.id.btn_select_all);
        Button deselectAllBtn = dialogView.findViewById(R.id.btn_deselect_all);
        Button cancelBtn = dialogView.findViewById(R.id.btn_cancel);
        Button downloadBtn = dialogView.findViewById(R.id.btn_download);
        
        // Initially show download button but disable it
        downloadBtn.setVisibility(View.VISIBLE);
        downloadBtn.setEnabled(false);
        downloadBtn.setAlpha(0.5f);
        
        ExerciseDownloadAdapter adapter = new ExerciseDownloadAdapter(exercises, courseId, offlineManager);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Set selection change listener
        adapter.setOnSelectionChangeListener(selectedCount -> {
            android.util.Log.d("DownloadDialog", "Selection changed: " + selectedCount);
            if (selectedCount > 0) {
                downloadBtn.setEnabled(true);
                downloadBtn.setAlpha(1.0f);
                downloadBtn.setText("Download (" + selectedCount + ")");
                android.util.Log.d("DownloadDialog", "Download button enabled");
            } else {
                downloadBtn.setEnabled(false);
                downloadBtn.setAlpha(0.5f);
                downloadBtn.setText("Download");
                android.util.Log.d("DownloadDialog", "Download button disabled");
            }
        });
        
        android.app.AlertDialog dialog = builder.setView(dialogView).create();
        
        // Debug logs
        android.util.Log.d("DownloadDialog", "Dialog created, download button found: " + (downloadBtn != null));
        android.util.Log.d("DownloadDialog", "Exercises count: " + exercises.size());
        
        selectAllBtn.setOnClickListener(v -> {
            android.util.Log.d("DownloadDialog", "Select all clicked");
            adapter.selectAll();
        });
        deselectAllBtn.setOnClickListener(v -> {
            android.util.Log.d("DownloadDialog", "Deselect all clicked");
            adapter.deselectAll();
        });
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        downloadBtn.setOnClickListener(v -> {
            java.util.Set<String> selected = adapter.getSelectedExercises();
            android.util.Log.d("DownloadDialog", "Download clicked, selected: " + selected.size());
            if (!selected.isEmpty()) {
                startDownload(selected);
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Please select exercises to download", Toast.LENGTH_SHORT).show();
            }
        });
        
        dialog.show();
        
        // Set dialog window size
        if (dialog.getWindow() != null) {
            android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            params.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(params);
        }
        
        android.util.Log.d("DownloadDialog", "Dialog shown");
    }
    
    private void startDownload(java.util.Set<String> selectedExercises) {
        if (selectedExercises.isEmpty()) {
            Toast.makeText(this, "Please select exercises", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setTitle("Downloading Exercises");
        progressDialog.setMessage("Starting download...");
        progressDialog.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();
        
        final int[] completedCount = {0};
        final int totalCount = selectedExercises.size();
        
        Toast.makeText(this, "Starting download of " + totalCount + " exercises", Toast.LENGTH_SHORT).show();
        
        for (String exerciseId : selectedExercises) {
            Exercise exercise = findExerciseById(exerciseId);
            if (exercise != null) {
                String content = "<html><head><title>" + exercise.getTitle() + "</title></head><body><h1>" + 
                               exercise.getTitle() + "</h1><p>" + exercise.getDescription() + "</p></body></html>";
                
                offlineManager.downloadExercise(courseId, exerciseId, content, new OfflineManager.DownloadListener() {
                    @Override
                    public void onProgress(String exerciseId, int progress) {
                        runOnUiThread(() -> {
                            int overallProgress = ((completedCount[0] * 100) + progress) / totalCount;
                            progressDialog.setProgress(overallProgress);
                            Exercise ex = findExerciseById(exerciseId);
                            progressDialog.setMessage("Downloading: " + (ex != null ? ex.getTitle() : exerciseId));
                        });
                    }
                    
                    @Override
                    public void onComplete(String exerciseId) {
                        runOnUiThread(() -> {
                            completedCount[0]++;
                            int overallProgress = (completedCount[0] * 100) / totalCount;
                            progressDialog.setProgress(overallProgress);
                            
                            if (completedCount[0] == totalCount) {
                                progressDialog.dismiss();
                                Toast.makeText(CourseDetailActivity.this, "All exercises downloaded successfully!", Toast.LENGTH_LONG).show();
                                if (exerciseAdapter != null) {
                                    exerciseAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                    
                    @Override
                    public void onError(String exerciseId, String error) {
                        runOnUiThread(() -> {
                            progressDialog.dismiss();
                            Toast.makeText(CourseDetailActivity.this, "Download failed: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
        }
    }
    
    private Exercise findExerciseById(String exerciseId) {
        for (Exercise exercise : exercises) {
            if (exercise.getId().equals(exerciseId)) {
                return exercise;
            }
        }
        return null;
    }
    
    private Exercise getNextIncompleteExercise() {
        for (Exercise exercise : exercises) {
            if (!exercise.isCompleted()) {
                return exercise;
            }
        }
        return null; // All exercises completed
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        checkEnrollmentStatus();
    }
}

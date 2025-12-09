package com.hariomsonihs.notesaura.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.hariomsonihs.notesaura.models.Course;
import com.hariomsonihs.notesaura.models.Exercise;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import android.content.Context;
import android.content.SharedPreferences;

public class CourseDataManager {
    // Update an existing course (admin edit)
    public void updateCourse(String id, String title, String description, String category, float rating, int duration, double price, String difficulty, List<Exercise> exercises) {
        CourseData courseData = allCourses.get(id);
        if (courseData != null) {
            courseData.title = title;
            courseData.description = description;
            courseData.category = category;
            courseData.rating = rating;
            courseData.duration = duration;
            courseData.price = price;
            courseData.difficulty = difficulty;
            courseData.isFree = price == 0.0;
            courseData.exercises = exercises != null ? exercises : new ArrayList<>();
            uploadCourseToFirebase(courseData);
            saveToCache();
            notifyCoursesUpdated();
        }
    }
    private static CourseDataManager instance;
    private Map<String, CourseData> allCourses;
    private java.util.LinkedHashMap<String, String> categories;
    private Map<String, String> categoryImageUrls;
    private List<String> featuredCourseIds;
    private FirebaseFirestore db;
    private Context context;
    private SharedPreferences prefs;
    private List<String> bookmarkedCourses;

    private List<DataUpdateListener> listeners;
    private ListenerRegistration coursesListener;
    private ListenerRegistration categoriesListener;

    public interface DataUpdateListener {
        void onCoursesUpdated();
        void onCategoriesUpdated();
    }


    // Update exercises for a course and sync to Firebase
    public void updateExercises(String courseId, List<Exercise> exercises) {
        CourseData data = allCourses.get(courseId);
        if (data != null) {
            data.exercises = new ArrayList<>(exercises);
            uploadCourseToFirebase(data);
            saveToCache();
            notifyCoursesUpdated();
        }
    }
    
    public static CourseDataManager getInstance() {
        if (instance == null) {
            instance = new CourseDataManager();
        }
        return instance;
    }
    
    private CourseDataManager() {
        db = FirebaseFirestore.getInstance();

        listeners = new ArrayList<>();
        allCourses = new HashMap<>();
        categories = new java.util.LinkedHashMap<>();
        categoryImageUrls = new HashMap<>();
        featuredCourseIds = new ArrayList<>();
        bookmarkedCourses = new ArrayList<>();
    }
    
    public void initialize(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences("course_cache", Context.MODE_PRIVATE);
        loadFromCache();
        loadBookmarks();
        setupFirebaseListeners();
        loadFeaturedFromFirebase();
    }
    
    public void addDataUpdateListener(DataUpdateListener listener) {
        listeners.add(listener);
    }
    
    public void removeDataUpdateListener(DataUpdateListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyCoursesUpdated() {
        for (DataUpdateListener listener : listeners) {
            listener.onCoursesUpdated();
        }
    }
    
    private void notifyCategoriesUpdated() {
        for (DataUpdateListener listener : listeners) {
            listener.onCategoriesUpdated();
        }
    }
    
    private void setupFirebaseListeners() {
        // Always load from cache first for instant display
        loadFromCache();
        
        // Notify immediately with cached data
        notifyCoursesUpdated();
        notifyCategoriesUpdated();
        
        // Check network availability before setting up listeners
        if (!NetworkUtil.isNetworkAvailable(context)) {
            android.util.Log.d("CourseDataManager", "No network - using cached data only");
            return;
        }
        
        // Listen to courses collection
        coursesListener = db.collection("courses")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Network error - load from cache
                        loadFromCache();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        loadCoursesFromFirebase(queryDocumentSnapshots.getDocuments());
                    }
                });
        
        // Listen to categories collection
        categoriesListener = db.collection("categories")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        // Network error - load from cache
                        loadCategoriesFromCache();
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        loadCategoriesFromFirebase(queryDocumentSnapshots.getDocuments());
                    }
                });
    }
    
    private void loadCoursesFromFirebase(List<DocumentSnapshot> documents) {
        Map<String, CourseData> newCourses = new HashMap<>();
        
        for (DocumentSnapshot doc : documents) {
            try {
                CourseData courseData = new CourseData();
                courseData.id = doc.getId();
                courseData.title = doc.getString("title");
                courseData.description = doc.getString("description");
                courseData.category = doc.getString("category");
                courseData.rating = doc.getDouble("rating").floatValue();
                courseData.duration = doc.getLong("duration").intValue();
                courseData.price = doc.getDouble("price");
                courseData.difficulty = doc.getString("difficulty");
                courseData.imageUrl = doc.getString("imageUrl");
                courseData.featured = doc.getBoolean("featured") != null ? doc.getBoolean("featured") : false;
                courseData.featuredOrder = doc.getLong("featuredOrder") != null ? doc.getLong("featuredOrder").intValue() : 0;
                // Handle backward compatibility with old 'order' field
                Long globalOrderLong = doc.getLong("globalOrder");
                Long categoryOrderLong = doc.getLong("categoryOrder");
                Long oldOrderLong = doc.getLong("order");
                
                courseData.globalOrder = globalOrderLong != null ? globalOrderLong.intValue() : 
                                        (oldOrderLong != null ? oldOrderLong.intValue() : 0);
                courseData.categoryOrder = categoryOrderLong != null ? categoryOrderLong.intValue() : 
                                          (oldOrderLong != null ? oldOrderLong.intValue() : 0);
                courseData.isFree = courseData.price == 0.0;
                
                // Load exercises
                List<Map<String, Object>> exercisesList = (List<Map<String, Object>>) doc.get("exercises");
                courseData.exercises = new ArrayList<>();
                if (exercisesList != null) {
                    for (Map<String, Object> exerciseMap : exercisesList) {
                        Exercise exercise = new Exercise(
                                (String) exerciseMap.get("id"),
                                (String) exerciseMap.get("title"),
                                (String) exerciseMap.get("description"),
                                (String) exerciseMap.get("contentPath"),
                                (String) exerciseMap.get("courseId")
                        );
                        courseData.exercises.add(exercise);
                    }
                }
                
                newCourses.put(courseData.id, courseData);
            } catch (Exception ex) {
                // Skip invalid course data
            }
        }
        
        allCourses = newCourses;
        
        // Debug logging
        android.util.Log.d("CourseDataManager", "Loaded " + newCourses.size() + " courses:");
        for (CourseData course : newCourses.values()) {
            android.util.Log.d("CourseDataManager", course.title + " - Global: " + course.globalOrder + ", Category: " + course.categoryOrder + ", Category: " + course.category);
        }
        
        saveToCache();
        notifyCoursesUpdated();
    }
    
    private void loadCategoriesFromFirebase(List<DocumentSnapshot> documents) {
        java.util.LinkedHashMap<String, String> newCategories = new java.util.LinkedHashMap<>();
        
        // Sort documents by order field
        documents.sort((doc1, doc2) -> {
            Long order1 = doc1.getLong("order");
            Long order2 = doc2.getLong("order");
            if (order1 == null) order1 = 0L;
            if (order2 == null) order2 = 0L;
            return order1.compareTo(order2);
        });
        
        for (DocumentSnapshot doc : documents) {
            String id = doc.getId();
            String name = doc.getString("name");
            String imageUrl = doc.getString("imageUrl");
            if (id != null && name != null) {
                newCategories.put(id, name);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    categoryImageUrls.put(id, imageUrl);
                }
            }
        }
        
        categories = newCategories;
        saveCategoriesCache();
        notifyCategoriesUpdated();
    }
    
    private void loadFromCache() {
        if (prefs == null) return;
        
        // Load cached courses
        String coursesJson = prefs.getString("cached_courses", "");
        if (!coursesJson.isEmpty()) {
            try {
                // Simple cache implementation - in production use proper JSON parsing
                android.util.Log.d("CourseDataManager", "Loading courses from cache");
                // For now, initialize with default data if cache exists
                initializeDefaultData();
            } catch (Exception e) {
                android.util.Log.e("CourseDataManager", "Error loading from cache", e);
                initializeDefaultData();
            }
        } else {
            initializeDefaultData();
        }
        
        loadCategoriesFromCache();
    }
    
    private void saveToCache() {
        if (prefs == null) return;
        
        try {
            // Save courses to cache (simplified)
            prefs.edit()
                .putBoolean("data_cached", true)
                .putLong("cache_timestamp", System.currentTimeMillis())
                .putString("cached_courses", "cached")
                .apply();
            
            android.util.Log.d("CourseDataManager", "Data saved to cache");
        } catch (Exception e) {
            android.util.Log.e("CourseDataManager", "Error saving to cache", e);
        }
    }
    
    private void saveCategoriesCache() {
        if (prefs == null) return;
        
        try {
            prefs.edit()
                .putBoolean("categories_cached", true)
                .putLong("categories_cache_timestamp", System.currentTimeMillis())
                .apply();
            
            android.util.Log.d("CourseDataManager", "Categories saved to cache");
        } catch (Exception e) {
            android.util.Log.e("CourseDataManager", "Error saving categories to cache", e);
        }
    }
    
    private void loadCategoriesFromCache() {
        if (prefs == null) return;
        
        boolean cached = prefs.getBoolean("categories_cached", false);
        if (cached) {
            android.util.Log.d("CourseDataManager", "Loading categories from cache");
            // Initialize default categories if cached
            initializeDefaultCategories();
        }
    }
    
    private void initializeDefaultData() {
        // Initialize default categories for offline use
        initializeDefaultCategories();
        
        // Initialize some default courses for offline use
        initializeDefaultCourses();
        
        android.util.Log.d("CourseDataManager", "Default data initialized for offline use");
    }
    
    private void initializeDefaultCategories() {
        if (categories.isEmpty()) {
            categories.put("programming", "Programming");
            categories.put("web_development", "Web Development");
            categories.put("app_development", "App Development");
            categories.put("data_science", "Data Science");
            categories.put("cheat_sheets", "Cheat Sheets");
        }
    }
    
    private void initializeDefaultCourses() {
        if (allCourses.isEmpty()) {
            // Programming courses
            addCourse("java_programming", "Java Programming", "Learn Java from basics to advanced", "programming", 4.5f, 120, 0.0, "Beginner", createJavaExercises());
            addCourse("python_programming", "Python Programming", "Master Python programming", "programming", 4.7f, 100, 0.0, "Beginner", createPythonExercises());
            addCourse("c_programming", "C Programming", "Learn C programming fundamentals", "programming", 4.4f, 90, 0.0, "Beginner", createCExercises());
            addCourse("cpp_programming", "C++ Programming", "Object-oriented programming with C++", "programming", 4.6f, 110, 0.0, "Intermediate", createCppExercises());
            
            // Web development courses
            addCourse("html_css", "HTML & CSS", "Web development fundamentals", "web_development", 4.3f, 80, 0.0, "Beginner", createHtmlCssExercises());
            addCourse("javascript", "JavaScript", "Dynamic web programming", "web_development", 4.5f, 95, 0.0, "Beginner", createJsExercises());
            addCourse("react", "React.js", "Modern frontend development", "web_development", 4.7f, 120, 0.0, "Intermediate", createReactExercises());
            
            // App development courses
            addCourse("android_dev", "Android Development", "Build Android apps", "app_development", 4.6f, 150, 0.0, "Intermediate", createAndroidExercises());
            addCourse("flutter", "Flutter", "Cross-platform app development", "app_development", 4.8f, 140, 0.0, "Intermediate", createFlutterExercises());
            
            // Data science courses
            addCourse("data_analysis", "Data Analysis", "Analyze data with Python", "data_science", 4.5f, 100, 0.0, "Beginner", createDataAnalysisExercises());
            addCourse("machine_learning", "Machine Learning", "AI and ML fundamentals", "data_science", 4.7f, 160, 0.0, "Advanced", createMlExercises());
        }
    }
    
    private void uploadDefaultDataToFirebase() {
        // Upload categories
        for (Map.Entry<String, String> entry : categories.entrySet()) {
            Map<String, Object> categoryData = new HashMap<>();
            categoryData.put("name", entry.getValue());
            db.collection("categories").document(entry.getKey()).set(categoryData);
        }
        
        // Upload courses
        for (CourseData courseData : allCourses.values()) {
            uploadCourseToFirebase(courseData);
        }
    }
    
    private void uploadCourseToFirebase(CourseData courseData) {
        Map<String, Object> data = new HashMap<>();
        data.put("title", courseData.title);
        data.put("description", courseData.description);
        data.put("category", courseData.category);
        data.put("rating", courseData.rating);
        data.put("duration", courseData.duration);
        data.put("price", courseData.price);
        data.put("difficulty", courseData.difficulty);
        data.put("imageUrl", courseData.imageUrl);
        data.put("featured", courseData.featured);
        data.put("featuredOrder", courseData.featuredOrder);
        data.put("globalOrder", courseData.globalOrder);
        data.put("categoryOrder", courseData.categoryOrder);
        // Keep old order field for backward compatibility
        data.put("order", courseData.categoryOrder);
        
        List<Map<String, Object>> exercisesList = new ArrayList<>();
        for (Exercise exercise : courseData.exercises) {
            Map<String, Object> exerciseMap = new HashMap<>();
            exerciseMap.put("id", exercise.getId());
            exerciseMap.put("title", exercise.getTitle());
            exerciseMap.put("description", exercise.getDescription());
            exerciseMap.put("contentPath", exercise.getHtmlFilePath());
            exerciseMap.put("courseId", exercise.getCourseId());
            exercisesList.add(exerciseMap);
        }
        data.put("exercises", exercisesList);
        
        db.collection("courses").document(courseData.id).set(data);
    }
    
    private void addCourse(String id, String title, String description, String category, float rating, int duration, double price, String difficulty, List<Exercise> exercises) {
        CourseData courseData = new CourseData();
        courseData.id = id;
        courseData.title = title;
        courseData.description = description;
        courseData.category = category;
        courseData.rating = rating;
        courseData.duration = duration;
        courseData.price = price;
        courseData.difficulty = difficulty;
        courseData.exercises = exercises;
        courseData.isFree = price == 0.0;
        allCourses.put(id, courseData);
    }
    
    public Course getCourse(String courseId) {
        CourseData data = allCourses.get(courseId);
        if (data == null) return null;
        
        Course course = new Course(data.id, data.title, data.description, data.category);
        course.setRating(data.rating);
        course.setDuration(data.duration);
        course.setPrice(data.price);
        course.setFree(data.isFree);
        course.setDifficulty(data.difficulty);
        course.setImageUrl(data.imageUrl);
        course.setEnrolledCount((int)(Math.random() * 500) + 50);
        return course;
    }
    
    public List<Course> getCoursesByCategory(String category) {
        List<Course> courses = new ArrayList<>();
        List<CourseData> categoryData = new ArrayList<>();
        
        for (CourseData data : allCourses.values()) {
            if (data.category.equals(category)) {
                categoryData.add(data);
            }
        }
        
        // Sort by category order, then by title
        categoryData.sort((a, b) -> {
            if (a.categoryOrder != b.categoryOrder) {
                return Integer.compare(a.categoryOrder, b.categoryOrder);
            }
            return a.title.compareTo(b.title);
        });
        
        // Debug logging for category ordering
        android.util.Log.d("CourseDataManager", "getCoursesByCategory(" + category + ") - Sorted by categoryOrder:");
        for (CourseData data : categoryData) {
            android.util.Log.d("CourseDataManager", data.title + " - Category: " + data.categoryOrder);
            courses.add(getCourse(data.id));
        }
        
        return courses;
    }
    
    public List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        List<CourseData> allData = new ArrayList<>();
        
        for (CourseData data : allCourses.values()) {
            allData.add(data);
        }
        
        // Sort by global order, then by title
        allData.sort((a, b) -> {
            if (a.globalOrder != b.globalOrder) {
                return Integer.compare(a.globalOrder, b.globalOrder);
            }
            return a.title.compareTo(b.title);
        });
        
        // Debug logging for global ordering
        android.util.Log.d("CourseDataManager", "getAllCourses - Sorted by globalOrder:");
        for (CourseData data : allData) {
            android.util.Log.d("CourseDataManager", data.title + " - Global: " + data.globalOrder);
            courses.add(getCourse(data.id));
        }
        
        return courses;
    }
    
    public List<String> getAllCategories() {
        return new ArrayList<>(categories.keySet());
    }
    
    public String getCategoryName(String categoryId) {
        return categories.getOrDefault(categoryId, categoryId);
    }
    
    public Map<String, String> getCategoriesMap() {
        return new java.util.LinkedHashMap<>(categories);
    }
    
    public java.util.LinkedHashMap<String, String> getOrderedCategoriesMap() {
        return new java.util.LinkedHashMap<>(categories);
    }
    
    // Admin methods for course management
    public void addNewCourse(String id, String title, String description, String category, 
                           float rating, int duration, double price, String difficulty, 
                           List<Exercise> exercises) {
        CourseData courseData = new CourseData();
        courseData.id = id;
        courseData.title = title;
        courseData.description = description;
        courseData.category = category;
        courseData.rating = rating;
        courseData.duration = duration;
        courseData.price = price;
        courseData.difficulty = difficulty;
        courseData.isFree = price == 0.0;
        courseData.exercises = exercises != null ? exercises : new ArrayList<>();
        
        allCourses.put(id, courseData);
        uploadCourseToFirebase(courseData);
    }
    
    public void deleteCourse(String courseId) {
        allCourses.remove(courseId);
        db.collection("courses").document(courseId).delete();
    }
    
    public void addNewCategory(String id, String name) {
        categories.put(id, name);
        Map<String, Object> categoryData = new HashMap<>();
        categoryData.put("name", name);
        categoryData.put("order", categories.size());
        db.collection("categories").document(id).set(categoryData);
    }
    
    public void updateCategoryOrder(String categoryId, int order) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("order", order);
        db.collection("categories").document(categoryId).update(updateData);
    }
    
    // Featured courses methods
    public List<Course> getFeaturedCourses() {
        List<Course> featured = new ArrayList<>();
        List<CourseData> featuredData = new ArrayList<>();
        
        // Get all featured courses
        for (CourseData data : allCourses.values()) {
            if (data.featured) {
                featuredData.add(data);
            }
        }
        
        // Sort by featured order
        featuredData.sort((a, b) -> Integer.compare(a.featuredOrder, b.featuredOrder));
        
        // Convert to Course objects
        for (CourseData data : featuredData) {
            Course course = getCourse(data.id);
            if (course != null) {
                featured.add(course);
            }
        }
        
        return featured;
    }
    
    public void addToFeatured(String courseId) {
        if (!featuredCourseIds.contains(courseId)) {
            featuredCourseIds.add(courseId);
            saveFeaturedToFirebase();
        }
    }
    
    public void removeFromFeatured(String courseId) {
        featuredCourseIds.remove(courseId);
        saveFeaturedToFirebase();
    }
    
    public boolean isFeatured(String courseId) {
        return featuredCourseIds.contains(courseId);
    }
    
    private void saveFeaturedToFirebase() {
        Map<String, Object> data = new HashMap<>();
        data.put("courseIds", featuredCourseIds);
        db.collection("settings").document("featured_courses").set(data);
    }
    
    private void loadFeaturedFromFirebase() {
        db.collection("settings").document("featured_courses")
            .addSnapshotListener((doc, e) -> {
                if (e != null || doc == null) return;
                List<String> ids = (List<String>) doc.get("courseIds");
                if (ids != null) {
                    featuredCourseIds = new ArrayList<>(ids);
                    notifyCoursesUpdated();
                }
            });
    }
    
    public void deleteCategory(String categoryId) {
        categories.remove(categoryId);
        categoryImageUrls.remove(categoryId);
        db.collection("categories").document(categoryId).delete();
    }
    
    public String getCategoryImageUrl(String categoryId) {
        return categoryImageUrls.get(categoryId);
    }
    
    // Track user course access
    public void trackCourseAccess(String courseId) {
        if (prefs != null) {
            long timestamp = System.currentTimeMillis();
            prefs.edit().putLong("course_access_" + courseId, timestamp).apply();
            
            // Also save to Firebase for cross-device sync
            com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                Map<String, Object> accessData = new HashMap<>();
                accessData.put("courseId", courseId);
                accessData.put("timestamp", timestamp);
                accessData.put("userId", currentUser.getUid());
                
                db.collection("user_course_access")
                    .document(currentUser.getUid() + "_" + courseId)
                    .set(accessData);
            }
        }
    }
    
    // Get continue learning courses (recently accessed + enrolled + latest)
    public List<Course> getContinueLearningCourses() {
        List<Course> continueCourses = new ArrayList<>();
        
        // Get recently accessed courses
        List<Course> recentlyAccessed = getRecentlyAccessedCourses();
        continueCourses.addAll(recentlyAccessed);
        
        // If not enough courses, add latest courses
        if (continueCourses.size() < 3) {
            List<Course> latestCourses = getLatestCourses();
            for (Course course : latestCourses) {
                if (!continueCourses.contains(course) && continueCourses.size() < 3) {
                    continueCourses.add(course);
                }
            }
        }
        
        return continueCourses;
    }
    
    // Get recently accessed courses
    private List<Course> getRecentlyAccessedCourses() {
        List<Course> recentCourses = new ArrayList<>();
        
        if (prefs != null) {
            Map<String, Long> courseAccess = new HashMap<>();
            
            // Get all course access timestamps from SharedPreferences
            for (String courseId : allCourses.keySet()) {
                long timestamp = prefs.getLong("course_access_" + courseId, 0);
                if (timestamp > 0) {
                    courseAccess.put(courseId, timestamp);
                }
            }
            
            // Sort by timestamp (most recent first)
            List<Map.Entry<String, Long>> sortedAccess = new ArrayList<>(courseAccess.entrySet());
            sortedAccess.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
            
            // Get top 3 recently accessed courses
            for (int i = 0; i < Math.min(3, sortedAccess.size()); i++) {
                String courseId = sortedAccess.get(i).getKey();
                Course course = getCourse(courseId);
                if (course != null) {
                    recentCourses.add(course);
                }
            }
        }
        
        return recentCourses;
    }
    
    // Get latest added courses
    private List<Course> getLatestCourses() {
        List<Course> latestCourses = new ArrayList<>();
        List<CourseData> allData = new ArrayList<>(allCourses.values());
        
        // Sort by global order (latest courses have higher order numbers)
        allData.sort((a, b) -> Integer.compare(b.globalOrder, a.globalOrder));
        
        // Get top 3 latest courses
        for (int i = 0; i < Math.min(3, allData.size()); i++) {
            Course course = getCourse(allData.get(i).id);
            if (course != null) {
                latestCourses.add(course);
            }
        }
        
        return latestCourses;
    }
    
    // Bookmark methods
    public void toggleBookmark(String courseId) {
        if (bookmarkedCourses.contains(courseId)) {
            bookmarkedCourses.remove(courseId);
        } else {
            bookmarkedCourses.add(courseId);
        }
        saveBookmarks();
        syncBookmarksToFirebase();
    }
    
    public boolean isBookmarked(String courseId) {
        return bookmarkedCourses.contains(courseId);
    }
    
    public List<Course> getBookmarkedCourses() {
        List<Course> bookmarked = new ArrayList<>();
        for (String courseId : bookmarkedCourses) {
            Course course = getCourse(courseId);
            if (course != null) {
                bookmarked.add(course);
            }
        }
        return bookmarked;
    }
    
    private void loadBookmarks() {
        if (prefs != null) {
            String bookmarksJson = prefs.getString("bookmarked_courses", "");
            if (!bookmarksJson.isEmpty()) {
                String[] courseIds = bookmarksJson.split(",");
                bookmarkedCourses = new ArrayList<>();
                for (String courseId : courseIds) {
                    if (!courseId.trim().isEmpty()) {
                        bookmarkedCourses.add(courseId.trim());
                    }
                }
            }
        }
    }
    
    private void saveBookmarks() {
        if (prefs != null) {
            String bookmarksJson = String.join(",", bookmarkedCourses);
            prefs.edit().putString("bookmarked_courses", bookmarksJson).apply();
        }
    }
    
    private void syncBookmarksToFirebase() {
        com.google.firebase.auth.FirebaseUser currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Map<String, Object> bookmarkData = new HashMap<>();
            bookmarkData.put("bookmarkedCourses", bookmarkedCourses);
            bookmarkData.put("userId", currentUser.getUid());
            bookmarkData.put("timestamp", System.currentTimeMillis());
            
            db.collection("user_bookmarks")
                .document(currentUser.getUid())
                .set(bookmarkData);
        }
    }
    
    public void cleanup() {
        if (coursesListener != null) {
            coursesListener.remove();
        }
        if (categoriesListener != null) {
            categoriesListener.remove();
        }
    }
    
    public List<Exercise> getExercises(String courseId) {
        CourseData data = allCourses.get(courseId);
        return data != null ? data.exercises : new ArrayList<>();
    }
    
    public double getCoursePrice(String courseId) {
        CourseData data = allCourses.get(courseId);
        return data != null ? data.price : 0.0;
    }
    
    public String getCourseTitle(String courseId) {
        CourseData data = allCourses.get(courseId);
        return data != null ? data.title : "Course";
    }
    
    public String getCourseDescription(String courseId) {
        CourseData data = allCourses.get(courseId);
        if (data != null) {
            String priceText = data.isFree ? "Free Course" : "Price: ₹" + (int)data.price;
            return data.description + ". " + priceText;
        }
        return "Course description not available";
    }
    
    // Exercise creation methods
    private List<Exercise> createJavaExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Java Basics", "Introduction to Java programming", "courses/programming/java_programming/basics.html", "java_programming"));
        exercises.add(new Exercise("e2", "Variables and Data Types", "Learn about Java variables", "courses/programming/java_programming/variables.html", "java_programming"));
        exercises.add(new Exercise("e3", "Control Structures", "If statements, loops, and more", "courses/programming/java_programming/control.html", "java_programming"));
        return exercises;
    }
    
    private List<Exercise> createPythonExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Python Basics", "Introduction to Python", "courses/programming/python/basics.html", "python_programming"));
        exercises.add(new Exercise("e2", "Data Types", "Python data types and variables", "courses/programming/python/datatypes.html", "python_programming"));
        exercises.add(new Exercise("e3", "Functions", "Creating and using functions", "courses/programming/python/functions.html", "python_programming"));
        return exercises;
    }
    
    private List<Exercise> createCppExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Introduction to C++", "Getting started with C++ programming", "courses/programming/cpp/basics.html", "cpp_programming"));
        exercises.add(new Exercise("e2", "OOP Concepts", "Object-Oriented Programming in C++", "courses/programming/cpp/oop.html", "cpp_programming"));
        exercises.add(new Exercise("e3", "Classes and Objects", "Understanding classes and objects", "courses/programming/cpp/classes.html", "cpp_programming"));
        return exercises;
    }
    
    private List<Exercise> createCExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Introduction to C", "Introduction to C programming", "courses/programming/c_programming/c1.html", "c_programming"));
        exercises.add(new Exercise("e2", "Features of C", "Understanding Features of C", "courses/programming/c_programming/c2.html", "c_programming"));
        exercises.add(new Exercise("e3", "Basic Syntax & Structure", "Learn the basic syntax and structure of C", "courses/programming/c_programming/c3.html", "c_programming"));
        return exercises;
    }
    
    private List<Exercise> createGoExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Go Basics", "Introduction to Go programming", "courses/programming/go/basics.html", "go_programming"));
        exercises.add(new Exercise("e2", "Go Syntax", "Go syntax and structure", "courses/programming/go/syntax.html", "go_programming"));
        exercises.add(new Exercise("e3", "Goroutines", "Concurrency in Go", "courses/programming/go/goroutines.html", "go_programming"));
        return exercises;
    }
    
    private List<Exercise> createKotlinExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Kotlin Basics", "Introduction to Kotlin", "courses/programming/kotlin/basics.html", "kotlin_programming"));
        exercises.add(new Exercise("e2", "Kotlin Syntax", "Kotlin syntax and features", "courses/programming/kotlin/syntax.html", "kotlin_programming"));
        exercises.add(new Exercise("e3", "Android with Kotlin", "Android development using Kotlin", "courses/programming/kotlin/android.html", "kotlin_programming"));
        return exercises;
    }
    
    private List<Exercise> createRustExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Rust Basics", "Introduction to Rust", "courses/programming/rust/basics.html", "rust_programming"));
        exercises.add(new Exercise("e2", "Memory Safety", "Rust memory management", "courses/programming/rust/memory.html", "rust_programming"));
        exercises.add(new Exercise("e3", "Ownership", "Rust ownership system", "courses/programming/rust/ownership.html", "rust_programming"));
        return exercises;
    }
    
    private List<Exercise> createSwiftExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Swift Basics", "Introduction to Swift", "courses/programming/swift/basics.html", "swift_programming"));
        exercises.add(new Exercise("e2", "Swift Syntax", "Swift language features", "courses/programming/swift/syntax.html", "swift_programming"));
        exercises.add(new Exercise("e3", "iOS Development", "Building iOS apps with Swift", "courses/programming/swift/ios.html", "swift_programming"));
        return exercises;
    }
    
    private List<Exercise> createDartExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Dart Basics", "Introduction to Dart programming", "courses/programming/dart/basics.html", "dart_programming"));
        exercises.add(new Exercise("e2", "Dart Syntax", "Dart language fundamentals", "courses/programming/dart/syntax.html", "dart_programming"));
        exercises.add(new Exercise("e3", "Object-Oriented Dart", "OOP concepts in Dart", "courses/programming/dart/oop.html", "dart_programming"));
        exercises.add(new Exercise("e4", "Async Programming", "Futures and Streams in Dart", "courses/programming/dart/async.html", "dart_programming"));
        return exercises;
    }
    
    private List<Exercise> createHtmlCssExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "HTML Basics", "Introduction to HTML", "courses/web/html/basics.html", "html_css"));
        exercises.add(new Exercise("e2", "CSS Styling", "Styling with CSS", "courses/web/css/styling.html", "html_css"));
        exercises.add(new Exercise("e3", "Responsive Design", "Creating responsive layouts", "courses/web/css/responsive.html", "html_css"));
        return exercises;
    }
    
    private List<Exercise> createJsExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "JS Basics", "JavaScript fundamentals", "courses/web/js/basics.html", "javascript"));
        exercises.add(new Exercise("e2", "DOM Manipulation", "Working with the DOM", "courses/web/js/dom.html", "javascript"));
        return exercises;
    }
    
    private List<Exercise> createReactExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "React Basics", "Introduction to React", "courses/web/react/basics.html", "react"));
        exercises.add(new Exercise("e2", "Components", "Creating React components", "courses/web/react/components.html", "react"));
        return exercises;
    }
    
    private List<Exercise> createVueExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Vue Basics", "Introduction to Vue.js", "courses/web/vue/basics.html", "vue_js"));
        exercises.add(new Exercise("e2", "Vue Components", "Creating Vue components", "courses/web/vue/components.html", "vue_js"));
        return exercises;
    }
    
    private List<Exercise> createAngularExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Angular Basics", "Introduction to Angular", "courses/web/angular/basics.html", "angular"));
        exercises.add(new Exercise("e2", "Components", "Angular components and services", "courses/web/angular/components.html", "angular"));
        return exercises;
    }
    
    private List<Exercise> createNodeExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Node.js Basics", "Introduction to Node.js", "courses/web/nodejs/basics.html", "nodejs"));
        exercises.add(new Exercise("e2", "Express Framework", "Building APIs with Express", "courses/web/nodejs/express.html", "nodejs"));
        return exercises;
    }
    
    private List<Exercise> createBootstrapExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Bootstrap Basics", "Introduction to Bootstrap", "courses/web/bootstrap/basics.html", "bootstrap"));
        exercises.add(new Exercise("e2", "Grid System", "Bootstrap grid and layout", "courses/web/bootstrap/grid.html", "bootstrap"));
        return exercises;
    }
    
    private List<Exercise> createPhpExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "PHP Basics", "Introduction to PHP", "courses/web/php/basics.html", "php"));
        exercises.add(new Exercise("e2", "PHP Syntax", "PHP syntax and features", "courses/web/php/syntax.html", "php"));
        return exercises;
    }
    
    private List<Exercise> createAndroidExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Android Basics", "Introduction to Android", "courses/app/android/basics.html", "android_dev"));
        exercises.add(new Exercise("e2", "Activities", "Working with activities", "courses/app/android/activities.html", "android_dev"));
        return exercises;
    }
    
    private List<Exercise> createFlutterExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Flutter Basics", "Introduction to Flutter", "courses/app/flutter/basics.html", "flutter"));
        exercises.add(new Exercise("e2", "Widgets", "Flutter widgets and layouts", "courses/app/flutter/widgets.html", "flutter"));
        return exercises;
    }
    
    private List<Exercise> createReactNativeExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "React Native Basics", "Introduction to React Native", "courses/app/react_native/basics.html", "react_native"));
        exercises.add(new Exercise("e2", "Navigation", "Navigation in React Native", "courses/app/react_native/navigation.html", "react_native"));
        return exercises;
    }
    
    private List<Exercise> createIosExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "iOS Basics", "Introduction to iOS development", "courses/app/ios/basics.html", "ios_swift"));
        exercises.add(new Exercise("e2", "UIKit", "Working with UIKit framework", "courses/app/ios/uikit.html", "ios_swift"));
        return exercises;
    }
    
    private List<Exercise> createDataAnalysisExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Data Basics", "Introduction to data analysis", "courses/data/analysis/basics.html", "data_analysis"));
        exercises.add(new Exercise("e2", "Pandas", "Data manipulation with pandas", "courses/data/analysis/pandas.html", "data_analysis"));
        return exercises;
    }
    
    private List<Exercise> createMlExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "ML Basics", "Introduction to Machine Learning", "courses/data/ml/basics.html", "machine_learning"));
        exercises.add(new Exercise("e2", "Algorithms", "ML algorithms and techniques", "courses/data/ml/algorithms.html", "machine_learning"));
        return exercises;
    }
    
    private List<Exercise> createDataVizExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Visualization Basics", "Introduction to data visualization", "courses/data/visualization/basics.html", "data_visualization"));
        exercises.add(new Exercise("e2", "Matplotlib", "Creating charts with Matplotlib", "courses/data/visualization/matplotlib.html", "data_visualization"));
        return exercises;
    }
    
    private List<Exercise> createDeepLearningExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Deep Learning Basics", "Introduction to deep learning", "courses/data/deep_learning/basics.html", "deep_learning"));
        exercises.add(new Exercise("e2", "Neural Networks", "Understanding neural networks", "courses/data/deep_learning/neural_networks.html", "deep_learning"));
        return exercises;
    }
    
    private List<Exercise> createRubyExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Ruby Basics", "Introduction to Ruby programming", "courses/programming/ruby/basics.html", "ruby_programming"));
        exercises.add(new Exercise("e2", "Ruby Syntax", "Ruby language features", "courses/programming/ruby/syntax.html", "ruby_programming"));
        exercises.add(new Exercise("e3", "Object-Oriented Ruby", "OOP concepts in Ruby", "courses/programming/ruby/oop.html", "ruby_programming"));
        return exercises;
    }
    
    private List<Exercise> createScalaExercises() {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", "Scala Basics", "Introduction to Scala programming", "courses/programming/scala/basics.html", "scala_programming"));
        exercises.add(new Exercise("e2", "Functional Programming", "Functional programming in Scala", "courses/programming/scala/functional.html", "scala_programming"));
        exercises.add(new Exercise("e3", "Scala Collections", "Working with Scala collections", "courses/programming/scala/collections.html", "scala_programming"));
        return exercises;
    }
    
    private List<Exercise> createCheatExercises(String language) {
        List<Exercise> exercises = new ArrayList<>();
        exercises.add(new Exercise("e1", language + " Syntax", language + " syntax reference", "courses/cheat/" + language.toLowerCase() + "/syntax.html", language.toLowerCase() + "_cheat"));
        return exercises;
    }
    
    public static class CourseData {
        public String id;
        public String title;
        public String description;
        public String category;
        public float rating;
        public int duration;
        public double price;
        public String difficulty;
        public String imageUrl;
        public boolean featured;
        public int featuredOrder;
        public int globalOrder;
        public int categoryOrder;
        public boolean isFree;
        public List<Exercise> exercises;
        
        public CourseData() {
            exercises = new ArrayList<>();
        }
    }
}
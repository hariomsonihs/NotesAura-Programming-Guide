package com.hariomsonihs.notesaura.utils;

public class Constants {
    // Firebase Collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_COURSES = "courses";
    public static final String COLLECTION_CATEGORIES = "categories";
    public static final String COLLECTION_PROGRESS = "progress";
    public static final String COLLECTION_PURCHASES = "purchases";
    public static final String COLLECTION_ENROLLMENTS = "enrollments";
    public static final String COLLECTION_INTERVIEW_CATEGORIES = "interview_categories";
    public static final String COLLECTION_INTERVIEW_QUESTIONS = "interview_questions";
    public static final String COLLECTION_PRACTICE_CATEGORIES = "practice_categories";
    public static final String COLLECTION_PRACTICE_LISTS = "practice_lists";
    public static final String COLLECTION_PRACTICE_EXERCISES = "practice_exercises";
    public static final String COLLECTION_QUIZ_CATEGORIES = "quiz_categories";
    public static final String COLLECTION_QUIZ_SUBCATEGORIES = "quiz_subcategories";
    public static final String COLLECTION_ADMIN = "admin";

    // SharedPreferences Keys
    public static final String PREF_NAME = "NotesAuraPrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";

    // Intent Keys
    public static final String KEY_COURSE_ID = "course_id";
    public static final String KEY_EXERCISE_ID = "exercise_id";
    public static final String KEY_CATEGORY_ID = "category_id";
    public static final String KEY_HTML_PATH = "html_path";

    // Categories
    public static final String CATEGORY_PROGRAMMING = "programming";
    public static final String CATEGORY_WEB_DEVELOPMENT = "web_development";
    public static final String CATEGORY_APP_DEVELOPMENT = "app_development";
    public static final String CATEGORY_DATA_SCIENCE = "data_science";
    public static final String CATEGORY_CHEAT_SHEETS = "cheat_sheets";

    // Asset Paths
    public static final String ASSETS_COURSES_PATH = "courses/";

    // Animation Durations
    public static final int ANIMATION_DURATION_SHORT = 300;
    public static final int ANIMATION_DURATION_MEDIUM = 500;
    public static final int ANIMATION_DURATION_LONG = 800;

    // Request Codes
    public static final int REQUEST_CODE_LOGIN = 1001;
    public static final int REQUEST_CODE_PROFILE = 1002;
}
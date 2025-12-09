package com.hariomsonihs.notesaura.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserData(String userId, String userName, String userEmail) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.KEY_USER_ID, userId);
        editor.putString(Constants.KEY_USER_NAME, userName);
        editor.putString(Constants.KEY_USER_EMAIL, userEmail);
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        return sharedPreferences.getString(Constants.KEY_USER_ID, "");
    }

    public String getUserName() {
        return sharedPreferences.getString(Constants.KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(Constants.KEY_USER_EMAIL, "");
    }

    // Dark mode methods removed
    
    public void clearCache() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Clear only cache-related data, keep user login data
        editor.remove("cached_courses");
        editor.remove("cached_categories");
        editor.remove("last_sync_time");
        editor.apply();
    }
}
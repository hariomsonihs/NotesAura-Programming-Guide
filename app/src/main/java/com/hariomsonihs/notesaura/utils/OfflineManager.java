package com.hariomsonihs.notesaura.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class OfflineManager {
    private static OfflineManager instance;
    private Context context;
    private SharedPreferences prefs;
    
    public interface DownloadListener {
        void onProgress(String exerciseId, int progress);
        void onComplete(String exerciseId);
        void onError(String exerciseId, String error);
    }
    
    private OfflineManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences("offline_courses", Context.MODE_PRIVATE);
    }
    
    public static OfflineManager getInstance(Context context) {
        if (instance == null) {
            instance = new OfflineManager(context);
        }
        return instance;
    }
    
    public void downloadExercise(String courseId, String exerciseId, String content, DownloadListener listener) {
        new Thread(() -> {
            try {
                File courseDir = new File(context.getFilesDir(), "offline_courses/" + courseId);
                if (!courseDir.exists()) courseDir.mkdirs();
                
                File exerciseFile = new File(courseDir, exerciseId + ".html");
                
                // Simulate download progress
                for (int i = 0; i <= 100; i += 10) {
                    Thread.sleep(100);
                    final int progress = i;
                    if (listener != null) {
                        android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                        mainHandler.post(() -> listener.onProgress(exerciseId, progress));
                    }
                }
                
                // Save content
                try (FileOutputStream fos = new FileOutputStream(exerciseFile)) {
                    fos.write(content.getBytes());
                }
                
                // Mark as downloaded
                Set<String> downloaded = getDownloadedExercises(courseId);
                downloaded.add(exerciseId);
                prefs.edit().putStringSet(courseId + "_exercises", downloaded).apply();
                
                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> listener.onComplete(exerciseId));
                }
                
            } catch (Exception e) {
                if (listener != null) {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> listener.onError(exerciseId, e.getMessage()));
                }
            }
        }).start();
    }
    
    public boolean isExerciseDownloaded(String courseId, String exerciseId) {
        return getDownloadedExercises(courseId).contains(exerciseId);
    }
    
    public Set<String> getDownloadedExercises(String courseId) {
        return prefs.getStringSet(courseId + "_exercises", new HashSet<>());
    }
    
    public void deleteExercise(String courseId, String exerciseId) {
        File exerciseFile = new File(context.getFilesDir(), "offline_courses/" + courseId + "/" + exerciseId + ".html");
        if (exerciseFile.exists()) {
            exerciseFile.delete();
        }
        
        Set<String> downloaded = new HashSet<>(getDownloadedExercises(courseId));
        downloaded.remove(exerciseId);
        prefs.edit().putStringSet(courseId + "_exercises", downloaded).apply();
    }
    
    public String getOfflineContent(String courseId, String exerciseId) {
        try {
            File exerciseFile = new File(context.getFilesDir(), "offline_courses/" + courseId + "/" + exerciseId + ".html");
            if (exerciseFile.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(exerciseFile);
                StringBuilder content = new StringBuilder();
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine()).append("\n");
                }
                scanner.close();
                return content.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
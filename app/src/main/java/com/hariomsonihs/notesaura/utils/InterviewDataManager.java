package com.hariomsonihs.notesaura.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.*;
import com.hariomsonihs.notesaura.models.InterviewCategory;
import com.hariomsonihs.notesaura.models.InterviewQuestion;

public class InterviewDataManager {
    private static InterviewDataManager instance;
    private FirebaseFirestore db;
    private List<InterviewCategory> categories;
    private List<InterviewQuestion> questions;
    private List<DataUpdateListener> listeners;
    private boolean isLoading = false;

    public interface DataUpdateListener {
        void onInterviewCategoriesUpdated();
        void onInterviewQuestionsUpdated();
    }

    private InterviewDataManager() {
        db = FirebaseFirestore.getInstance();
        categories = new ArrayList<>();
        questions = new ArrayList<>();
        listeners = new ArrayList<>();
        
        // Initialize with default data immediately
        createDefaultCategories();
        
        // Then try to load from Firebase
        loadData();
    }

    public static InterviewDataManager getInstance() {
        if (instance == null) {
            instance = new InterviewDataManager();
        }
        return instance;
    }

    private void loadData() {
        isLoading = true;
        // Load categories with fallback
        db.collection("interview_categories")
            .get()
            .addOnSuccessListener(snapshots -> {
                List<InterviewCategory> newCategories = new ArrayList<>();
                if (snapshots != null && !snapshots.isEmpty()) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            InterviewCategory category = doc.toObject(InterviewCategory.class);
                            if (category != null) {
                                category.setId(doc.getId());
                                newCategories.add(category);
                            }
                        } catch (Exception ex) {
                            // Skip invalid documents
                        }
                    }
                }
                
                if (!newCategories.isEmpty()) {
                    // Only update if we got data from Firebase
                    categories.clear();
                    categories.addAll(newCategories);
                }
                // If empty, keep existing default categories
                isLoading = false;
                notifyCategoriesUpdated();
            })
            .addOnFailureListener(e -> {
                // Keep existing data on failure, just notify
                isLoading = false;
                notifyCategoriesUpdated();
            });

        // Load questions with fallback
        db.collection("interview_questions")
            .get()
            .addOnSuccessListener(snapshots -> {
                List<InterviewQuestion> newQuestions = new ArrayList<>();
                if (snapshots != null && !snapshots.isEmpty()) {
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            InterviewQuestion question = doc.toObject(InterviewQuestion.class);
                            if (question != null) {
                                question.setId(doc.getId());
                                newQuestions.add(question);
                            }
                        } catch (Exception ex) {
                            // Skip invalid documents
                        }
                    }
                    questions.clear();
                    questions.addAll(newQuestions);
                }
                notifyQuestionsUpdated();
            })
            .addOnFailureListener(e -> {
                notifyQuestionsUpdated();
            });
    }
    
    private void createDefaultCategories() {
        categories.clear();
        categories.add(new InterviewCategory("java", "Java", "Java programming interview questions", "#FF5722", 0));
        categories.add(new InterviewCategory("android", "Android", "Android development interview questions", "#4CAF50", 1));
        categories.add(new InterviewCategory("web", "Web Development", "Web development interview questions", "#2196F3", 2));
        categories.add(new InterviewCategory("general", "General", "General programming interview questions", "#9C27B0", 3));
    }

    // Category methods
    public List<InterviewCategory> getAllCategories() {
        return new ArrayList<>(categories);
    }

    public void addInterviewCategory(InterviewCategory category) {
        db.collection("interview_categories").document(category.getId()).set(category);
    }

    public void updateInterviewCategory(InterviewCategory category) {
        db.collection("interview_categories").document(category.getId()).set(category);
    }

    public void deleteInterviewCategory(String categoryId) {
        db.collection("interview_categories").document(categoryId).delete();
    }

    // Question methods
    public List<InterviewQuestion> getQuestionsByCategory(String categoryId) {
        List<InterviewQuestion> result = new ArrayList<>();
        for (InterviewQuestion question : questions) {
            if (question.getCategoryId().equals(categoryId)) {
                result.add(question);
            }
        }
        return result;
    }
    
    public InterviewQuestion getQuestion(String questionId) {
        for (InterviewQuestion question : questions) {
            if (question.getId().equals(questionId)) {
                return question;
            }
        }
        return null;
    }

    public void addInterviewQuestion(InterviewQuestion question) {
        db.collection("interview_questions").document(question.getId()).set(question);
    }

    public void updateInterviewQuestion(InterviewQuestion question) {
        db.collection("interview_questions").document(question.getId()).set(question);
    }

    public void deleteInterviewQuestion(String questionId) {
        db.collection("interview_questions").document(questionId).delete();
    }

    // Listener methods
    public void addDataUpdateListener(DataUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeDataUpdateListener(DataUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyCategoriesUpdated() {
        for (DataUpdateListener listener : listeners) {
            listener.onInterviewCategoriesUpdated();
        }
    }

    private void notifyQuestionsUpdated() {
        for (DataUpdateListener listener : listeners) {
            listener.onInterviewQuestionsUpdated();
        }
    }
    
    public void refreshData() {
        loadData();
    }
}
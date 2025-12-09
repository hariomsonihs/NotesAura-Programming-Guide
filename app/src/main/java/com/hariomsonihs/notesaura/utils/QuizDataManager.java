package com.hariomsonihs.notesaura.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.*;
import com.hariomsonihs.notesaura.models.QuizCategory;
import com.hariomsonihs.notesaura.models.QuizSubcategory;

public class QuizDataManager {
    private static QuizDataManager instance;
    private Map<String, QuizCategory> categories;
    private Map<String, QuizSubcategory> subcategories;
    private FirebaseFirestore db;
    private List<DataUpdateListener> listeners;

    public interface DataUpdateListener {
        void onQuizCategoriesUpdated();
        void onQuizSubcategoriesUpdated();
    }

    public static QuizDataManager getInstance() {
        if (instance == null) instance = new QuizDataManager();
        return instance;
    }

    private QuizDataManager() {
        db = FirebaseFirestore.getInstance();
        categories = new HashMap<>();
        subcategories = new HashMap<>();
        listeners = new ArrayList<>();
        setupFirebaseListeners();
        addSampleDataIfEmpty();
    }

    private void addSampleDataIfEmpty() {
        db.collection("quiz_categories").get().addOnSuccessListener(querySnapshot -> {
            if (querySnapshot.isEmpty()) {
                addQuizCategory(new QuizCategory("java_quiz", "Java Quiz", "", "#FF6B8E", 0));
                addQuizCategory(new QuizCategory("python_quiz", "Python Quiz", "", "#4ECDC4", 1));
                addQuizCategory(new QuizCategory("web_quiz", "Web Development", "", "#FFE66D", 2));
            }
        });
    }

    private void setupFirebaseListeners() {
        db.collection("quiz_categories").addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;
            Map<String, QuizCategory> newCategories = new HashMap<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                try {
                    QuizCategory cat = doc.toObject(QuizCategory.class);
                    if (cat != null && cat.getId() != null) {
                        newCategories.put(cat.getId(), cat);
                    }
                } catch (Exception ex) {}
            }
            categories = newCategories;
            notifyCategoriesUpdated();
        });

        db.collection("quiz_subcategories").addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;
            Map<String, QuizSubcategory> newSubcategories = new HashMap<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                try {
                    QuizSubcategory sub = doc.toObject(QuizSubcategory.class);
                    if (sub != null && sub.getId() != null) {
                        newSubcategories.put(sub.getId(), sub);
                    }
                } catch (Exception ex) {}
            }
            subcategories = newSubcategories;
            notifySubcategoriesUpdated();
        });
    }

    public void addDataUpdateListener(DataUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeDataUpdateListener(DataUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyCategoriesUpdated() {
        for (DataUpdateListener l : listeners) l.onQuizCategoriesUpdated();
    }

    private void notifySubcategoriesUpdated() {
        for (DataUpdateListener l : listeners) l.onQuizSubcategoriesUpdated();
    }

    public void addQuizCategory(QuizCategory category) {
        db.collection("quiz_categories").document(category.getId()).set(category);
    }

    public void updateQuizCategory(QuizCategory category) {
        db.collection("quiz_categories").document(category.getId()).set(category);
    }

    public void deleteQuizCategory(String id) {
        db.collection("quiz_categories").document(id).delete();
    }

    public List<QuizCategory> getAllCategories() {
        List<QuizCategory> list = new ArrayList<>();
        for (QuizCategory cat : categories.values()) {
            if (cat != null) list.add(cat);
        }
        list.sort(Comparator.comparingInt(QuizCategory::getOrder));
        return list;
    }

    public void addQuizSubcategory(QuizSubcategory subcategory) {
        db.collection("quiz_subcategories").document(subcategory.getId()).set(subcategory);
    }

    public void updateQuizSubcategory(QuizSubcategory subcategory) {
        db.collection("quiz_subcategories").document(subcategory.getId()).set(subcategory);
    }

    public void deleteQuizSubcategory(String id) {
        db.collection("quiz_subcategories").document(id).delete();
    }

    public List<QuizSubcategory> getSubcategoriesByCategory(String categoryId) {
        List<QuizSubcategory> list = new ArrayList<>();
        for (QuizSubcategory sub : subcategories.values()) {
            if (sub != null && sub.getCategoryId() != null && sub.getCategoryId().equals(categoryId)) {
                list.add(sub);
            }
        }
        list.sort(Comparator.comparingInt(QuizSubcategory::getOrder));
        return list;
    }
}
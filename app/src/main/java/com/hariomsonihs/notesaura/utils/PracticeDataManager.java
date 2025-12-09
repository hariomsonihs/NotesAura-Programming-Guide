package com.hariomsonihs.notesaura.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.*;
import com.hariomsonihs.notesaura.models.PracticeCategory;
import com.hariomsonihs.notesaura.models.PracticeExercise;
import com.hariomsonihs.notesaura.models.PracticeList;

public class PracticeDataManager {
    private static PracticeDataManager instance;
    private Map<String, PracticeCategory> categories;
    private Map<String, PracticeList> practiceLists;
    private Map<String, PracticeExercise> exercises;
    private FirebaseFirestore db;
    private List<DataUpdateListener> listeners;
    private ListenerRegistration categoriesListener;
    private ListenerRegistration practiceListsListener;
    private ListenerRegistration exercisesListener;

    public interface DataUpdateListener {
        void onPracticeCategoriesUpdated();
        void onPracticeListsUpdated();
        void onPracticeExercisesUpdated();
    }

    public static PracticeDataManager getInstance() {
        if (instance == null) instance = new PracticeDataManager();
        return instance;
    }

    private PracticeDataManager() {
        db = FirebaseFirestore.getInstance();
        categories = new HashMap<>();
        practiceLists = new HashMap<>();
        exercises = new HashMap<>();
        listeners = new ArrayList<>();
        setupFirebaseListeners();
    }

    private void setupFirebaseListeners() {
        try {
            categoriesListener = db.collection(Constants.COLLECTION_PRACTICE_CATEGORIES)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    Map<String, PracticeCategory> newCategories = new HashMap<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            PracticeCategory cat = doc.toObject(PracticeCategory.class);
                            if (cat != null && cat.getId() != null) {
                                newCategories.put(cat.getId(), cat);
                            }
                        } catch (Exception ex) {
                            // Skip invalid documents
                        }
                    }
                    categories = newCategories;
                    notifyCategoriesUpdated();
                });
        } catch (Exception e) {
            // Handle Firebase initialization errors
        }
        try {
            practiceListsListener = db.collection(Constants.COLLECTION_PRACTICE_LISTS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    Map<String, PracticeList> newLists = new HashMap<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            PracticeList list = doc.toObject(PracticeList.class);
                            if (list != null && list.getId() != null) {
                                newLists.put(list.getId(), list);
                            }
                        } catch (Exception ex) {
                            // Skip invalid documents
                        }
                    }
                    practiceLists = newLists;
                    notifyPracticeListsUpdated();
                });
        } catch (Exception e) {
            // Handle Firebase initialization errors
        }
        try {
            exercisesListener = db.collection(Constants.COLLECTION_PRACTICE_EXERCISES)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    Map<String, PracticeExercise> newExercises = new HashMap<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        try {
                            PracticeExercise ex = doc.toObject(PracticeExercise.class);
                            if (ex != null && ex.getId() != null) {
                                newExercises.put(ex.getId(), ex);
                            }
                        } catch (Exception ex) {
                            // Skip invalid documents
                        }
                    }
                    exercises = newExercises;
                    notifyExercisesUpdated();
                });
        } catch (Exception e) {
            // Handle Firebase initialization errors
        }
    }

    public void addDataUpdateListener(DataUpdateListener listener) {
        listeners.add(listener);
    }
    public void removeDataUpdateListener(DataUpdateListener listener) {
        listeners.remove(listener);
    }
    private void notifyCategoriesUpdated() {
        for (DataUpdateListener l : listeners) l.onPracticeCategoriesUpdated();
    }
    private void notifyPracticeListsUpdated() {
        for (DataUpdateListener l : listeners) l.onPracticeListsUpdated();
    }
    private void notifyExercisesUpdated() {
        for (DataUpdateListener l : listeners) l.onPracticeExercisesUpdated();
    }
    // CRUD for practice lists
    public void addPracticeList(PracticeList list) {
        db.collection(Constants.COLLECTION_PRACTICE_LISTS).document(list.getId()).set(list);
    }
    public void updatePracticeList(PracticeList list) {
        db.collection(Constants.COLLECTION_PRACTICE_LISTS).document(list.getId()).set(list);
    }
    public void deletePracticeList(String id) {
        db.collection(Constants.COLLECTION_PRACTICE_LISTS).document(id).delete();
    }
    public List<PracticeList> getPracticeListsByCategory(String categoryId) {
        List<PracticeList> list = new ArrayList<>();
        if (categoryId == null || practiceLists == null) return list;
        for (PracticeList pl : practiceLists.values()) {
            if (pl != null && pl.getCategoryId() != null && pl.getCategoryId().equals(categoryId)) {
                list.add(pl);
            }
        }
        list.sort(Comparator.comparingInt(PracticeList::getOrder));
        return list;
    }
    public PracticeList getPracticeList(String id) {
        return practiceLists.get(id);
    }

    // CRUD for categories
    public void addPracticeCategory(PracticeCategory cat) {
        db.collection(Constants.COLLECTION_PRACTICE_CATEGORIES).document(cat.getId()).set(cat);
    }
    public void updatePracticeCategory(PracticeCategory cat) {
        db.collection(Constants.COLLECTION_PRACTICE_CATEGORIES).document(cat.getId()).set(cat);
    }
    public void deletePracticeCategory(String id) {
        db.collection(Constants.COLLECTION_PRACTICE_CATEGORIES).document(id).delete();
    }
    public List<PracticeCategory> getAllCategories() {
        List<PracticeCategory> list = new ArrayList<>();
        if (categories != null) {
            for (PracticeCategory cat : categories.values()) {
                if (cat != null) list.add(cat);
            }
        }
        list.sort(Comparator.comparingInt(PracticeCategory::getOrder));
        return list;
    }

    // CRUD for exercises
    public void addPracticeExercise(PracticeExercise ex) {
        db.collection(Constants.COLLECTION_PRACTICE_EXERCISES).document(ex.getId()).set(ex);
    }
    public void updatePracticeExercise(PracticeExercise ex) {
        db.collection(Constants.COLLECTION_PRACTICE_EXERCISES).document(ex.getId()).set(ex);
    }
    public void deletePracticeExercise(String id) {
        db.collection(Constants.COLLECTION_PRACTICE_EXERCISES).document(id).delete();
    }
    public List<PracticeExercise> getExercisesByCategory(String practiceListId) {
        List<PracticeExercise> list = new ArrayList<>();
        if (practiceListId == null || exercises == null) return list;
        for (PracticeExercise ex : exercises.values()) {
            if (ex != null && ex.getPracticeListId() != null && ex.getPracticeListId().equals(practiceListId)) {
                list.add(ex);
            }
        }
        list.sort(Comparator.comparingInt(PracticeExercise::getOrder));
        return list;
    }
    public PracticeExercise getExercise(String id) {
        return exercises.get(id);
    }
    
    // Method to get total exercises count by category
    public int getTotalExercisesByCategory(String categoryId) {
        int totalCount = 0;
        List<PracticeList> lists = getPracticeListsByCategory(categoryId);
        for (PracticeList list : lists) {
            totalCount += getExercisesByCategory(list.getId()).size();
        }
        return totalCount;
    }
    
    // Method to create default categories if none exist
    public void createDefaultCategoriesIfNeeded() {
        if (categories.isEmpty()) {
            // Create some default practice categories
            PracticeCategory javaCat = new PracticeCategory("java_practice", "Java Practice", "", "#FF5722", 0);
            PracticeCategory cCat = new PracticeCategory("c_practice", "C Practice", "", "#2196F3", 1);
            PracticeCategory pythonCat = new PracticeCategory("python_practice", "Python Practice", "", "#4CAF50", 2);
            
            addPracticeCategory(javaCat);
            addPracticeCategory(cCat);
            addPracticeCategory(pythonCat);
        }
    }
    
    public void refreshData() {
        setupFirebaseListeners();
    }
}

package com.hariomsonihs.notesaura.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseDBHelper {
    private static FirebaseDBHelper instance;
    private FirebaseFirestore firestore;

    private FirebaseDBHelper() {
        firestore = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseDBHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseDBHelper();
        }
        return instance;
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public CollectionReference getUsersCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_USERS);
    }

    public CollectionReference getCoursesCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_COURSES);
    }

    public CollectionReference getCategoriesCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_CATEGORIES);
    }

    public CollectionReference getProgressCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_PROGRESS);
    }

    public DocumentReference getUserDocument(String userId) {
        return getUsersCollection().document(userId);
    }

    public DocumentReference getCourseDocument(String courseId) {
        return getCoursesCollection().document(courseId);
    }

    public DocumentReference getProgressDocument(String userId, String courseId) {
        return getProgressCollection().document(userId + "_" + courseId);
    }

    public CollectionReference getEnrollmentsCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_ENROLLMENTS);
    }

    public CollectionReference getUserEnrolledCourses(String userId) {
        return firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection("enrolledCourses");
    }

    public DocumentReference getEnrollmentDocument(String userId, String courseId) {
        return getUserEnrolledCourses(userId).document(courseId);
    }
    
    public DocumentReference getPaymentDocument(String userId, String transactionId) {
        return firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection("payments")
                .document(transactionId);
    }
    
    public CollectionReference getUserPayments(String userId) {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .collection("payments");
    }
    
    public CollectionReference getInterviewCategoriesCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_INTERVIEW_CATEGORIES);
    }
    
    public CollectionReference getInterviewQuestionsCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_INTERVIEW_QUESTIONS);
    }
    
    public CollectionReference getPracticeCategoriesCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_PRACTICE_CATEGORIES);
    }
    
    public CollectionReference getPracticeListsCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_PRACTICE_LISTS);
    }
    
    public CollectionReference getPracticeExercisesCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_PRACTICE_EXERCISES);
    }
    
    public CollectionReference getQuizCategoriesCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_QUIZ_CATEGORIES);
    }
    
    public CollectionReference getQuizSubcategoriesCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_QUIZ_SUBCATEGORIES);
    }
    
    public CollectionReference getAdminCollection() {
        if (!isUserAuthenticated()) return null;
        return firestore.collection(Constants.COLLECTION_ADMIN);
    }
    
    private boolean isUserAuthenticated() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null;
    }
    
    public String getCurrentUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }
}
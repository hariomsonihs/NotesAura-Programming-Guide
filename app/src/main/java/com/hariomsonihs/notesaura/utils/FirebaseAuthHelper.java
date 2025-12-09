package com.hariomsonihs.notesaura.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthHelper {
    private static FirebaseAuthHelper instance;
    private FirebaseAuth firebaseAuth;

    private FirebaseAuthHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static synchronized FirebaseAuthHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthHelper();
        }
        return instance;
    }

    public FirebaseAuth getAuth() {
        return firebaseAuth;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public String getCurrentUserEmail() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    public void signOut() {
        firebaseAuth.signOut();
    }
}
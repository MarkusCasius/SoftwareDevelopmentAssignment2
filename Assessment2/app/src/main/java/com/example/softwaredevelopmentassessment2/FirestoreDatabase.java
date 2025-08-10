package com.example.softwaredevelopmentassessment2;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.*;

// A collection of methods for interacting with the firestore database. Majority of methods was planned for use with creating new courses for those
// with administrator privlidges, but wasn't able to be developed in time.
public class FirestoreDatabase {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface ResultCallback<T> {
        void onComplete(T result);
    }

    // Method for finding a user's account within the firestore's database
    public static void findAccount(String username, String password, ResultCallback<Boolean> callback) {
        db.collection("account")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        callback.onComplete(true);
                    } else {
                        callback.onComplete(false);
                    }
                });
    }

    // See if the user was an admin
    public static void findIsAdmin(String username, String password, ResultCallback<Integer> callback) {
        db.collection("account")
                .whereEqualTo("username", username)
                .whereEqualTo("password", password)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        Long isAdmin = doc.getLong("isAdministrator");
                        callback.onComplete(isAdmin != null ? isAdmin.intValue() : 0);
                    } else {
                        callback.onComplete(0);
                    }
                });
    }

    // Retrieve the course assigned to an account
    public static void findCourseFromAccount(String username, ResultCallback<String> callback) {
        db.collection("account")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        String course = doc.getString("accountCourse");
                        callback.onComplete(course != null ? course : "");
                    } else {
                        callback.onComplete("");
                    }
                });
    }

    // Check if a course is within the course collection
    public static void isCourse(String courseId, ResultCallback<Boolean> callback) {
        db.collection("course")
                .document(courseId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        callback.onComplete(true);
                    } else {
                        callback.onComplete(false);
                    }
                });
    }

    // Inserting data within a document
    public static void insertData(String collection, Map<String, Object> data, ResultCallback<String> callback) {
        db.collection(collection)
                .add(data)
                .addOnSuccessListener(docRef -> callback.onComplete(null))
                .addOnFailureListener(e -> callback.onComplete(e.getMessage()));
    }

    // Updating data within a document
    public static void updateData(String collection, String docId, String fieldName, Object newValue, ResultCallback<String> callback) {
        db.collection(collection)
                .document(docId)
                .update(fieldName, newValue)
                .addOnSuccessListener(unused -> callback.onComplete(null))
                .addOnFailureListener(e -> callback.onComplete(e.getMessage()));
    }

    // Deleting data within a document
    public static void deleteData(String collection, String docId, ResultCallback<String> callback) {
        db.collection(collection)
                .document(docId)
                .delete()
                .addOnSuccessListener(unused -> callback.onComplete(null))
                .addOnFailureListener(e -> callback.onComplete(e.getMessage()));
    }

    // Checking for whether an account is in the account collection. If not, one is made.
    public static void ensureUserAccountInFirestore(FirebaseUser user, FirestoreDatabase.ResultCallback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("account").document(user.getUid());

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    // No record — create one
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("uid", user.getUid());
                    userData.put("username", user.getEmail() != null ? user.getEmail() : user.getPhoneNumber());
                    userData.put("accountCourse", "");
                    userData.put("isAdministrator", 0);

                    userRef.set(userData).addOnCompleteListener(setTask -> {
                        if (setTask.isSuccessful()) {
                            Log.d("Firestore", "User account created.");
                            callback.onComplete(null);
                        } else {
                            Log.e("Firestore", "Failed to create user", setTask.getException());
                        }
                    });
                } else {
                    Log.d("Firestore", "User account already exists.");
                    callback.onComplete(null);
                }
            } else {
                Log.e("Firestore", "Error checking user document", task.getException());
            }
        });
    }
}



package com.example.softwaredevelopmentassessment2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView userNameTextView, userEmailTextView;
    private Spinner courseSpinner; // or EditText
    private Switch themeSwitch; // example for dark mode
    private Button saveButton;

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;
    private StorageReference storageRef;

    private Uri selectedImageUri;

    private static final int IMAGE_PICK_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImageView = findViewById(R.id.profileImageView);
        userNameTextView = findViewById(R.id.userNameTextView);
        userEmailTextView = findViewById(R.id.userEmailTextView);
        courseSpinner = findViewById(R.id.courseSpinner);
        themeSwitch = findViewById(R.id.themeSwitch);
        saveButton = findViewById(R.id.saveButton);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firestore = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();

        loadUserInfo();
        loadUserExtraData();

        profileImageView.setOnClickListener(v -> openImagePicker());

        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserInfo() {
        if (currentUser != null) {
            userNameTextView.setText(currentUser.getDisplayName());
            userEmailTextView.setText(currentUser.getEmail());

            // Load profile image URL from Firestore or use currentUser.getPhotoUrl()
            // Use Glide or Picasso to load into profileImageView
        }
    }

    private void loadUserExtraData() {
        firestore.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String course = document.getString("course");
                        boolean darkMode = document.getBoolean("darkMode") != null ? document.getBoolean("darkMode") : false;
                        // Set these to UI
                        // e.g. courseSpinner.setSelection(...) based on course
                        themeSwitch.setChecked(darkMode);
                    }
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            profileImageView.setImageURI(selectedImageUri);
            uploadProfilePicture(selectedImageUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfilePicture(Uri imageUri) {
        StorageReference profilePicRef = storageRef.child("profile_pictures/" + currentUser.getUid() + ".jpg");
        profilePicRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> profilePicRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // Save uri.toString() in Firestore under user's document
                            firestore.collection("users").document(currentUser.getUid())
                                    .update("profileImageUrl", uri.toString());
                        }))
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void saveUserProfile() {
        String selectedCourse = courseSpinner.getSelectedItem().toString();
        boolean darkModeEnabled = themeSwitch.isChecked();

        firestore.collection("users").document(currentUser.getUid())
                .update(
                        "course", selectedCourse,
                        "darkMode", darkModeEnabled
                )
                .addOnSuccessListener(aVoid -> {
                    // Maybe apply theme change immediately
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
    }
}
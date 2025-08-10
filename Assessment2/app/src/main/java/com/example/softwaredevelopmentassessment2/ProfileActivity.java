package com.example.softwaredevelopmentassessment2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

// Activity used to display the contents of the profile page, along with handling updates to a user's information.

public class ProfileActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_REQUEST_CODE = 1001;

    private ImageView profileImageView;
    private TextView userNameTextView, userEmailTextView;
    private Spinner courseSpinner;
    private Switch themeSwitch;
    private Button saveButton;

    private FirebaseUser currentUser;
    private FirebaseFirestore firestore;

    private Uri selectedImageUri;

    // For course mapping
    private List<String> courseIds = new ArrayList<>();
    private List<String> courseNames = new ArrayList<>();

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

        loadUserInfo();
        loadCourses();
        loadUserExtraData();

        profileImageView.setOnClickListener(v -> openImagePicker());
        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    // Loads a user's information used to parse through the variables displayed in activity_profile.xml.
    private void loadUserInfo() {
        if (currentUser != null) {
            userNameTextView.setText(currentUser.getDisplayName());
            userEmailTextView.setText(currentUser.getEmail());

            firestore.collection("account").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String localPath = document.getString("profileImagePath");
                            if (localPath != null) {
                                File imgFile = new File(localPath);
                                if (imgFile.exists()) {
                                    profileImageView.setImageURI(Uri.fromFile(imgFile));
                                }
                            }
                        }
                    });
        }
    }

    // Loads the available courses so users can change their desires courses if they wish to calculate
    // from the modules provided in that course.
    private void loadCourses() {
        firestore.collection("course")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    courseIds.clear();
                    courseNames.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getString("courseID");
                        String name = doc.getString("name");
                        if (id != null && name != null) {
                            courseIds.add(id);
                            courseNames.add(name);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, courseNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    courseSpinner.setAdapter(adapter);

                    // Once courses are loaded, select user's current course
                    loadUserExtraData();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load courses", Toast.LENGTH_SHORT).show()
                );
    }

    // Loads additional data from the user, being their course, and whether they have darkMode enabled.
    private void loadUserExtraData() {
        firestore.collection("account").document(currentUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String courseId = document.getString("course");
                        boolean darkMode = Boolean.TRUE.equals(document.getBoolean("darkMode"));
                        themeSwitch.setChecked(darkMode);

                        if (courseId != null && !courseIds.isEmpty()) {
                            int index = courseIds.indexOf(courseId);
                            if (index >= 0) {
                                courseSpinner.setSelection(index);
                            }
                        }
                    }
                });
    }

    // Module for beginning the activity to select a new image, then to save it to firebase and
    // local files.
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    String localPath = copyImageToInternalStorage(selectedImageUri);
                    profileImageView.setImageURI(Uri.parse(localPath));
                    saveProfileImagePath(localPath);

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

// Copies the picked image into app's private storage and returns the file path.
    private String copyImageToInternalStorage(Uri imageUri) throws IOException {
        // Create a unique file name
        String fileName = "profile_" + System.currentTimeMillis() + ".jpg";
        File file = new File(getFilesDir(), fileName);

        try (InputStream inputStream = getContentResolver().openInputStream(imageUri);
             OutputStream outputStream = new FileOutputStream(file)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        }

        return file.getAbsolutePath();
    }

    // Method for saving the local path of the image on the device
    private void saveProfileImagePath(String localPath) {
        firestore.collection("account").document(currentUser.getUid())
                .update("profileImagePath", localPath)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save profile picture path", Toast.LENGTH_SHORT).show()
                );
    }

    // Method for updating the information the user has altered to the firestore.
    private void saveUserProfile() {
        int selectedIndex = courseSpinner.getSelectedItemPosition();
        if (selectedIndex < 0 || selectedIndex >= courseIds.size()) {
            Toast.makeText(this, "Please select a course", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedCourseId = courseIds.get(selectedIndex);
        boolean darkModeEnabled = themeSwitch.isChecked();

        firestore.collection("account").document(currentUser.getUid())
                .update(
                        "accountCourse", selectedCourseId,
                        "darkMode", darkModeEnabled
                )
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show()
                );
    }
}
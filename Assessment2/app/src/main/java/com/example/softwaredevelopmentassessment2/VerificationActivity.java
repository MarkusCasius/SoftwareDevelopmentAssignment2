package com.example.softwaredevelopmentassessment2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);


        FirebaseApp.initializeApp(this);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance() // ✅ No argument here
        );

        Log.d("AppCheckDebug", "✅ App Check Debug provider installed");

        // Optional: Print debug token in Logcat
        firebaseAppCheck.getToken(true)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("AppCheckDebug", " Token: " + task.getResult().getToken());
                    } else {
                        Log.e("AppCheckDebug", "Token generation failed", task.getException());
                    }
                });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d("User", "User is signed in");
        } else {
            // No user is signed in
            Log.d("User", "User is not signed in");
        }

    }
}

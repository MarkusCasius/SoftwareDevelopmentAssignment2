package com.example.softwaredevelopmentassessment2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

    public class SplashActivity extends AppCompatActivity {

        private static final long SPLASH_DELAY = 1500;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Optional: show a splash screen layout
            setContentView(R.layout.activity_splash);

            new Handler().postDelayed(() -> {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // ✅ User is signed in
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    // ❌ Not signed in
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish(); // Close splash so user can't come back with back button
            }, SPLASH_DELAY);
        }
    }

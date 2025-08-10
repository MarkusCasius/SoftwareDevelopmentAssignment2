package com.example.softwaredevelopmentassessment2;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// A splash activity, being the first page opened. It then redirects to login if the user isn't locally logged in on the device
// Or goes to the MainActivity/home page if the user is logged in.

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
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                finish();
            }, SPLASH_DELAY);
        }
    }

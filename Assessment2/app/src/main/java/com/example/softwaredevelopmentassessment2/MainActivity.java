package com.example.softwaredevelopmentassessment2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AuthFlow", "onCreate: Activity started");
        FirebaseApp.initializeApp(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button GotoAuthButton = findViewById(R.id.GotoAuthButton);
        GotoAuthButton.setOnClickListener(v -> {
            Log.d("AuthFlow", "GotoAuthButton clicked → launching VerificationActivity");
            Intent intent = new Intent(MainActivity.this, VerificationActivity.class);
            startActivity(intent);
            finish();
        });

        Button SignOutButton = findViewById(R.id.SignOutButton);
        SignOutButton.setOnClickListener(v -> {
            Log.d("AuthFlow", "SignOutButton clicked → signing out");
            AuthUI.getInstance().signOut(MainActivity.this)
                    .addOnCompleteListener(task -> Log.d("AuthFlow", "Sign out complete"));
        });

        Button CalculatorNavigateButton = findViewById(R.id.CalculatorNavigateButton);
        CalculatorNavigateButton.setOnClickListener(v -> {
            Log.d("AuthFlow", "Calculator clicked → launching CalculatorActivity");
            Intent intent = new Intent(MainActivity.this, GradeCalculatorActivity.class);
            startActivity(intent);
        });

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not signed in
            Log.d("AuthFlow", "Launching sign-in intent");
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

}

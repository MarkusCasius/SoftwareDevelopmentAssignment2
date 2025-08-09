package com.example.softwaredevelopmentassessment2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupDrawer(R.layout.activity_main);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Load default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }

        // Handle navigation item selection
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.nav_home) {
                // Handle Home navigation
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_profile) {
                // Navigate to Profile
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_settings) {
                // Navigate to GradeCalculatorActivity
                startActivity(new Intent(MainActivity.this, GradeCalculatorActivity.class));
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_calculator) {
                // Navigate to GradeCalculatorActivity
                startActivity(new Intent(MainActivity.this, GradeCalculatorActivity.class));
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_sign_out) {
                // Handle sign out
                AuthUI.getInstance().signOut(MainActivity.this)
                        .addOnCompleteListener(task -> {
                            Log.d("AuthFlow", "Sign out complete");
                            drawerLayout.closeDrawers();
                        });
            }
            // Add more else-if cases as needed

            return true;
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

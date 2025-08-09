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

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("AuthFlow", "onCreate: Activity started");
        FirebaseApp.initializeApp(this);
        // FirestoreSeeder.seedDatabase();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.main);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,               // pass toolbar here
                R.string.drawer_open,
                R.string.drawer_close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }


        // Setup DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);

        // Setup ActionBarDrawerToggle to show hamburger icon and sync state
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout,
                R.string.drawer_open,  // You should add these strings in res/values/strings.xml
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        // Enable the hamburger icon in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Handle NavigationView item clicks
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.nav_home) {
                // Example: close drawer if already home
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_calculator) {
                startActivity(new Intent(MainActivity.this, GradeCalculatorActivity.class));
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_sign_out) {
                AuthUI.getInstance().signOut(MainActivity.this)
                        .addOnCompleteListener(task -> {
                            Log.d("AuthFlow", "Sign out complete");
                            drawerLayout.closeDrawers();
                        });
            }
            // Add more cases as needed

            return true;
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

    // Make sure the hamburger menu works by overriding onOptionsItemSelected
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

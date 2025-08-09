package com.example.softwaredevelopmentassessment2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    /**
     * Call this from child activities in onCreate()
     * Pass the layout resource that includes DrawerLayout + toolbar + navigation view
     */
    protected void setupDrawer(int layoutResId) {
        setContentView(layoutResId);

        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.nav_home) {
                // Handle Home navigation
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_profile) {
                // Navigate to Profile
                startActivity(new Intent(this, ProfileActivity.class));
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_settings) {
                // Navigate to GradeCalculatorActivity
                startActivity(new Intent(this, GradeCalculatorActivity.class));
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_calculator) {
                // Navigate to GradeCalculatorActivity
                Log.d("NavMenu", "Navigating to Calculator.");
                startActivity(new Intent(this, GradeCalculatorActivity.class));
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_sign_out) {
                // Handle sign out
                AuthUI.getInstance().signOut(this)
                        .addOnCompleteListener(task -> {
                            Log.d("AuthFlow", "Sign out complete");
                            drawerLayout.closeDrawers();
                        });
            }
            // Add more else-if cases as needed

            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle != null && drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

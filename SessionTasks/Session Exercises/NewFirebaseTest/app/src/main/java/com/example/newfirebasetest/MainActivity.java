package com.example.newfirebasetest;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private AppDatabase db;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize database
        db = AppDatabase.getInstance(this);
        userDao = db.userDao();

        // Insert users into the database
        User user1 = new User("Alice", "Brown");
        User user2 = new User("Bob", "Smith");

        userDao.insertAll(user1, user2);
        Log.d(TAG, "Users inserted!");

        // Fetch all users and log them
        List<User> users = userDao.getAllUsers();
        for (User user : users) {
            Log.d(TAG, "User: " + user.uid + ", " + user.firstName + " " + user.lastName);
        }
    }
}
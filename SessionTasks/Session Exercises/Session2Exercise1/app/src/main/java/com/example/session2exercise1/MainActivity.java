package com.example.session2exercise1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    Button sendButton;
    TextView email; TextView message; TextView subject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        sendButton = findViewById(R.id.sendButtonXML);
        email = findViewById(R.id.editTextTextEmailAddress); message = findViewById(R.id.editTextTextMessage); subject = findViewById(R.id.editTextTextSubject);
    }

    public void sendEvent(View view) {
        System.out.println(email + " test " + message + " " + subject);
        if (email != null && message != null && subject != null){
            Toast.makeText(MainActivity.this, "Message sent", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
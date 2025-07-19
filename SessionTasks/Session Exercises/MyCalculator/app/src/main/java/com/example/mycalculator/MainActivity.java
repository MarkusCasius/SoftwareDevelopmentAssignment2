package com.example.mycalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    TextView result; TextView firstNum; TextView secNum;
    Button divide; Button multiply; Button subtract; Button add;

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
        result = findViewById(R.id.resultView); firstNum = findViewById(R.id.editTextNumber); secNum = findViewById(R.id.editTextNumber2);
        divide = findViewById(R.id.divideButton); multiply = findViewById(R.id.multiplyButton); subtract = findViewById(R.id.minusButton); add = findViewById(R.id.plusButton);

    }

    public void plusButtonEvent(View view) {
        double finalResult;
        try {
            finalResult = Double.parseDouble(secNum.getText().toString()) + Double.parseDouble(firstNum.getText().toString());
            result.setText(Double.toString(finalResult));
        } catch (NumberFormatException e) {
            result.setText("Error: Input numbers");
        }
    }

    public void multiplyButtonEvent(View view) {
        double finalResult;
        try {
            finalResult = Double.parseDouble(secNum.getText().toString()) * Double.parseDouble(firstNum.getText().toString());
            result.setText(Double.toString(finalResult));
        } catch (NumberFormatException e) {
            result.setText("Error: Input numbers");
        }
    }

    public void divideButtonEvent(View view) {
        double finalResult;
        try {
            finalResult = Double.parseDouble(secNum.getText().toString()) / Double.parseDouble(firstNum.getText().toString());
            result.setText(Double.toString(finalResult));
        } catch (NumberFormatException e) {
            result.setText("Error: Input numbers");
        }
    }

    public void minusButtonEvent(View view) {
        double finalResult;
        try {
            finalResult = Double.parseDouble(secNum.getText().toString()) - Double.parseDouble(firstNum.getText().toString());
            result.setText(Double.toString(finalResult));
        } catch (NumberFormatException e) {
            result.setText("Error: Input numbers");
        }
    }
}
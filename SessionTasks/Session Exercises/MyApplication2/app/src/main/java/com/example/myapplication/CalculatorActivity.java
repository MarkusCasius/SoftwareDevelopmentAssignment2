package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {

    TextView editTextNumber;
    TextView editTextNumber2;
    TextView outputText;

    Button plusButton;
    Button minusButton;
    Button multiplyButton;
    Button divideButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        editTextNumber = findViewById(R.id.editTextNumber);
        editTextNumber2 = findViewById(R.id.editTextNumber2);
        outputText = findViewById(R.id.outputText);

        plusButton = findViewById(R.id.plusButton);
        minusButton = findViewById(R.id.minusButton);
        multiplyButton = findViewById(R.id.multiplyButton);
        divideButton = findViewById(R.id.divideButton);
    }

    public float convertTextToNumber(TextView input) {
        String convertedInput = input.getText().toString();
        if (!input.equals("")) {
            try {
                return Float.parseFloat(convertedInput);
            } catch (NumberFormatException nfe) {
            return 0;
            }
        }
        return 0;
    }

    public String convertNumberToText(float input) {
        return String.valueOf(input);
    }

    public void updateOutputText(String input) {
        outputText.setText(input);
    }
    public void plus(View view) {
        float outputFloat = convertTextToNumber(editTextNumber) + convertTextToNumber(editTextNumber2);
        updateOutputText(convertNumberToText(outputFloat));
    }

    public void minus(View view) {
        float outputFloat = convertTextToNumber(editTextNumber) - convertTextToNumber(editTextNumber2);
        updateOutputText(convertNumberToText(outputFloat));
    }

    public void divide(View view) {
        float outputFloat = convertTextToNumber(editTextNumber) / convertTextToNumber(editTextNumber2);
        updateOutputText(convertNumberToText(outputFloat));
    }

    public void multiply(View view) {
        float outputFloat = convertTextToNumber(editTextNumber) * convertTextToNumber(editTextNumber2);
        updateOutputText(convertNumberToText(outputFloat));
    }
}

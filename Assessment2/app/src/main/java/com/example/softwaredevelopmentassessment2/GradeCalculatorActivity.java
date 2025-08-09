package com.example.softwaredevelopmentassessment2;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeCalculatorActivity extends AppCompatActivity {

    private TableLayout moduleTable;
    private Button btnCalculate;
    private TextView txtResult;
    private String username = "caseymark94@gmail.com"; // set from login session
    private String courseID;
    private boolean isPostGrad = false;
    private boolean isFoundation = false;

    private List<String> moduleIds = new ArrayList<>();
    private Map<String, Integer> creditsMap = new HashMap<>();
    private Map<String, EditText> markInputs = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private DocumentSnapshot docSnap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        moduleTable = findViewById(R.id.moduleTable);
        btnCalculate = findViewById(R.id.btnCalculate);
        txtResult = findViewById(R.id.txtResult);

        loadCourseAndModules();

        btnCalculate.setOnClickListener(v -> calculateGrades());
    }

    private void loadCourseAndModules() {
        FirestoreDatabase.findCourseFromAccount(username, course -> {
            if (!course.isEmpty()) {
                courseID = course;

                // ✅ Instead of document(courseID), query by a field in your "course" collection
                db.collection("course")
                        .whereEqualTo("courseID", courseID) // or "name" if accountCourse stores course name
                        .limit(1)
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

                                String type = doc.getString("courseType");
                                if ("Foundation".equalsIgnoreCase(type)) {
                                    isFoundation = true;
                                } else if ("Postgraduate".equalsIgnoreCase(type)) {
                                    isPostGrad = true;
                                }

                                // ✅ Now load modules for this course
                                loadModules();
                            } else {
                                Toast.makeText(this, "Course not found in database", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error loading course type", Toast.LENGTH_SHORT).show()
                        );
            }
        });
    }

    private void loadModules() {
        db.collection("modules")
                .whereEqualTo("courseID", courseID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String moduleId = doc.getId();
                        String moduleName = doc.getString("moduleName");
                        Long credits = doc.getLong("credits");

                        if (moduleName != null && credits != null) {
                            addModuleRow(moduleId, moduleName, credits.intValue());
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading modules", Toast.LENGTH_SHORT).show()
                );
    }

    private void addModuleRow(String moduleId, String name, int credits) {
        TableRow row = new TableRow(this);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setPadding(8, 8, 8, 8);

        TextView creditsView = new TextView(this);
        creditsView.setText(String.valueOf(credits));
        creditsView.setPadding(8, 8, 8, 8);

        EditText markInput = new EditText(this);
        markInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        markInput.setHint("Enter mark");
        markInput.setPadding(8, 8, 8, 8);

        row.addView(nameView);
        row.addView(creditsView);
        row.addView(markInput);

        moduleTable.addView(row);

        // Track for calculation
        moduleIds.add(moduleId);
        creditsMap.put(moduleId, credits);
        markInputs.put(moduleId, markInput);
    }

    private void calculateGrades() {
        Map<String, Integer> enteredMarks = new HashMap<>();
        for (String moduleId : moduleIds) {
            String input = markInputs.get(moduleId).getText().toString();
            if (!input.isEmpty()) {
                enteredMarks.put(moduleId, Integer.parseInt(input));
            }
        }

        GradeCalculatorService.calculateGrades(courseID, isPostGrad, isFoundation, enteredMarks,
                new GradeCalculatorService.GradeCallback() {
                    @Override
                    public void onComplete(Map<String, Object> results) {
                        runOnUiThread(() -> displayFormattedResults(results));
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(GradeCalculatorActivity.this, "Calculation error", Toast.LENGTH_SHORT).show()
                        );
                    }
                });
    }

    private void displayFormattedResults(Map<String, Object> results) {
        StringBuilder sb = new StringBuilder();

        if (isFoundation) {
            sb.append("Your Final Grade: ").append(results.get("finalGrade")).append("\n");
        } else if (isPostGrad) {
            sb.append("Your Final Grade: ").append(results.get("finalGrade")).append("\n");
            sb.append("Average Score: ").append(formatDecimal(results.get("average"))).append("\n");
        } else {
            sb.append("Method A: ").append(formatDecimal(results.get("avgA")))
                    .append(" → ").append(results.get("classA")).append("\n");
            sb.append("Method B: ").append(formatDecimal(results.get("avgB")))
                    .append(" → ").append(results.get("classB")).append("\n");
            sb.append("Method C: ").append(formatDecimal(results.get("avgC")))
                    .append(" → ").append(results.get("classC")).append("\n");

            String[] classifications = {
                    (String) results.get("classA"),
                    (String) results.get("classB"),
                    (String) results.get("classC")
            };
            String highest = AcademicCalculator.GetHighestResult(classifications);
            sb.append("\nHighest Achievable Classification: ").append(highest);
        }

        txtResult.setText(sb.toString());
    }

    private String formatDecimal(Object value) {
        if (value instanceof Number) {
            return String.format("%.1f", ((Number) value).doubleValue());
        }
        return value != null ? value.toString() : "";
    }
}

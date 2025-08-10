package com.example.softwaredevelopmentassessment2;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class GradeCalculatorActivity extends AppCompatActivity {

    private TableLayout moduleTable;
    private Button btnCalculate;
    private TextView txtResult;
    private EditText desiredGradeInput;
    private String username; // set from login session
    private String courseID;
    private boolean isPostGrad = false;
    private boolean isFoundation = false;

    private List<String> moduleIds = new ArrayList<>();
    private Map<String, Integer> creditsMap = new HashMap<>();
    private Map<String, EditText> markInputs = new HashMap<>();
    private Map<String, Boolean> isCoreMap = new HashMap<>();
    private Map<String, CheckBox> optionalModuleChecks = new HashMap<>();
    private Map<String, Integer> moduleLevelMap = new HashMap<>(); // new: store level per module

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;

    private static final double FOUNDATION_PASS_THRESHOLD = 39.50;
    private static final int FOUNDATION_PASS_CREDITS = 120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        moduleTable = findViewById(R.id.moduleTable);
        btnCalculate = findViewById(R.id.btnCalculate);
        txtResult = findViewById(R.id.txtResult);
        desiredGradeInput = findViewById(R.id.desiredGradeInput);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert currentUser != null;
        username = currentUser.getEmail();

        loadCourseAndModules();

        btnCalculate.setOnClickListener(v -> calculateGrades());
    }

    private void loadCourseAndModules() {
        FirestoreDatabase.findCourseFromAccount(username, course -> {
            if (!course.isEmpty()) {
                courseID = course;

                db.collection("course")
                        .whereEqualTo("courseID", courseID)
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
                        Boolean isCore = doc.getBoolean("isCore");
                        Long level = doc.getLong("level");

                        if (moduleName != null && credits != null) {
                            int lvl = level != null ? level.intValue() : -1;
                            addModuleRow(moduleId, moduleName, credits.intValue(), isCore != null && isCore, lvl);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading modules", Toast.LENGTH_SHORT).show()
                );
    }

    private void addModuleRow(String moduleId, String name, int credits, boolean isCore, int level) {
        TableRow row = new TableRow(this);

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setPadding(8, 8, 8, 8);

        TextView creditsView = new TextView(this);
        creditsView.setText(String.valueOf(credits));
        creditsView.setPadding(8, 8, 8, 8);

        // mark input
        EditText markInput = new EditText(this);
        markInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        markInput.setHint("Enter mark");
        markInput.setPadding(8, 8, 8, 8);

        row.addView(nameView);
        row.addView(creditsView);
        row.addView(markInput);

        if (!isCore) {
            CheckBox optionalCheck = new CheckBox(this);
            optionalCheck.setPadding(8, 8, 8, 8);
            row.addView(optionalCheck);
            optionalModuleChecks.put(moduleId, optionalCheck);
        } else {
            TextView placeholder = new TextView(this);
            placeholder.setText("Core");
            placeholder.setPadding(8, 8, 8, 8);
            row.addView(placeholder);
        }

        moduleTable.addView(row);

        // Tracking data
        moduleIds.add(moduleId);
        creditsMap.put(moduleId, credits);
        markInputs.put(moduleId, markInput);
        isCoreMap.put(moduleId, isCore);
        moduleLevelMap.put(moduleId, level);
    }

    private void calculateGrades() {
        // Check optional modules selected
        for (String moduleId : moduleIds) {
            if (!isCoreMap.get(moduleId)) {
                CheckBox cb = optionalModuleChecks.get(moduleId);
                if (cb != null && !cb.isChecked()) {
                    Toast.makeText(this, "Please select all your optional modules before calculating", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        // Parse entered marks
        Map<String, Integer> enteredMarks = new HashMap<>();
        for (String moduleId : moduleIds) {
            String input = markInputs.get(moduleId).getText().toString();
            if (!input.isEmpty()) {
                try {
                    enteredMarks.put(moduleId, Integer.parseInt(input));
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid mark for a module", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        // Grade predictions
        String desiredGradeStr = desiredGradeInput.getText().toString();
        if (!desiredGradeStr.isEmpty()) {
            try {
                double desiredGrade = Double.parseDouble(desiredGradeStr);
                if (desiredGrade < 0 || desiredGrade > 100) {
                    Toast.makeText(this, "Desired grade must be between 0 and 100", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Double> predicted = calculateRequiredMarks(enteredMarks, desiredGrade);
                    if (predicted != null && !predicted.isEmpty()) {
                        highlightPredictedMarks(predicted);
                    }
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid desired grade", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Clear previous hints
            clearPredictedHints();
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

// Method for the grade tracker to provide statistics on how to achieve the desired overall mark
    private Map<String, Double> calculateRequiredMarks(Map<String, Integer> enteredMarks, double desiredGrade) {
        double totalCredits = 0;
        double knownWeightedSumTotal = 0;
        double missingCreditsTotal = 0;

        double knownWeightedSumL5 = 0, knownWeightedSumL6 = 0;
        double totalCreditsL5 = 0, totalCreditsL6 = 0;
        double knownCreditsL5 = 0, knownCreditsL6 = 0;
        double missingCreditsL5 = 0, missingCreditsL6 = 0;

        List<String> missingModuleIds = new ArrayList<>();

        for (String mid : moduleIds) {
            int credits = creditsMap.get(mid);
            totalCredits += credits;

            Integer m = enteredMarks.get(mid);
            Integer level = moduleLevelMap.get(mid) != null ? moduleLevelMap.get(mid) : -1;
            if (level == 5) totalCreditsL5 += credits;
            if (level == 6) totalCreditsL6 += credits;

            if (m != null) {
                knownWeightedSumTotal += credits * m;
                if (level == 5) { knownWeightedSumL5 += credits * m; knownCreditsL5 += credits; }
                if (level == 6) { knownWeightedSumL6 += credits * m; knownCreditsL6 += credits; }
            } else {
                missingModuleIds.add(mid);
                missingCreditsTotal += credits;
                if (level == 5) missingCreditsL5 += credits;
                if (level == 6) missingCreditsL6 += credits;
            }
        }

        // check feasibility, return candidate map or null
        Map<String, Double> bestCandidate = null;
        double bestScore = Double.MAX_VALUE; // pick method with lowest required mark (easiest)

        // FOUNDATION
        if (isFoundation) {
            // foundation uses credit-pool of modules with mark >= FOUNDATION_PASS_THRESHOLD
            int creditPool = 0;
            for (String mid : moduleIds) {
                Integer m = enteredMarks.get(mid);
                int cr = creditsMap.get(mid);
                if (m != null && m >= FOUNDATION_PASS_THRESHOLD) creditPool += cr;
            }
            if (creditPool >= FOUNDATION_PASS_CREDITS) {
                // already passing -> nothing required
                Toast.makeText(this, "Foundation pass already satisfied.", Toast.LENGTH_SHORT).show();
                return Collections.emptyMap();
            } else {
                // we need to set some missing modules to pass threshold until we reach 120 credits (greedy by credits)
                // collect missing modules sorted by credits desc
                List<String> missingSorted = new ArrayList<>(missingModuleIds);
                missingSorted.sort((a, b) -> Integer.compare(creditsMap.get(b), creditsMap.get(a)));

                Map<String, Double> candidate = new HashMap<>();
                int pool = creditPool;
                for (String mid : missingSorted) {
                    int cr = creditsMap.get(mid);
                    candidate.put(mid, FOUNDATION_PASS_THRESHOLD); // need >= threshold
                    pool += cr;
                    if (pool >= FOUNDATION_PASS_CREDITS) break;
                }

                if (pool >= FOUNDATION_PASS_CREDITS) {
                    // feasible -> but make sure threshold is in 0..100
                    if (FOUNDATION_PASS_THRESHOLD < 0 || FOUNDATION_PASS_THRESHOLD > 100) {
                        Toast.makeText(this, "Impossible target for foundation.", Toast.LENGTH_LONG).show();
                        return Collections.emptyMap();
                    }
                    // choose this candidate (only if it's better — here we choose foundation directly)
                    return candidate;
                } else {
                    Toast.makeText(this, "Impossible: not enough credits to pass foundation even if you pass all missing modules.", Toast.LENGTH_LONG).show();
                    return Collections.emptyMap();
                }
            }
        }

        // POSTGRAD
        if (isPostGrad) {
            if (missingCreditsTotal == 0) {
                double currentAvg = knownWeightedSumTotal / totalCredits;
                if (currentAvg >= desiredGrade) {
                    Toast.makeText(this, "Desired postgraduate average already achieved.", Toast.LENGTH_SHORT).show();
                    return Collections.emptyMap();
                } else {
                    Toast.makeText(this, "Desired postgraduate average cannot be reached (no missing marks).", Toast.LENGTH_LONG).show();
                    return Collections.emptyMap();
                }
            } else {
                double requiredMarkAllMissing = (desiredGrade * totalCredits - knownWeightedSumTotal) / missingCreditsTotal;
                if (requiredMarkAllMissing < 0 || requiredMarkAllMissing > 100) {
                    Toast.makeText(this, "Impossible target for postgraduate: required mark out of 0-100 range.", Toast.LENGTH_LONG).show();
                } else {
                    Map<String, Double> cand = new HashMap<>();
                    for (String mid : missingModuleIds) cand.put(mid, requiredMarkAllMissing);
                    bestCandidate = cand;
                    bestScore = requiredMarkAllMissing;
                }
            }
        }

        // ---------- UNDERGRAD: try Method A, B, C ----------
        if (!isPostGrad && !isFoundation) {
            // compute known averages (use totals, avoid divide-by-zero)
            double knownPartL5 = (totalCreditsL5 > 0) ? knownWeightedSumL5 / totalCreditsL5 : 0.0;
            double knownPartL6 = (totalCreditsL6 > 0) ? knownWeightedSumL6 / totalCreditsL6 : 0.0;

            // METHOD A: (L5avg + L6avg) / 2 = desired  => L5avg + L6avg = 2*desired
            double denomA = ( (missingCreditsL5 > 0 && totalCreditsL5>0) ? (missingCreditsL5 / totalCreditsL5) : 0.0 )
                    + ( (missingCreditsL6 > 0 && totalCreditsL6>0) ? (missingCreditsL6 / totalCreditsL6) : 0.0 );

            if (denomA > 0) {
                double numeratorA = 2.0 * desiredGrade - ((totalCreditsL5>0 ? knownWeightedSumL5 / totalCreditsL5 : 0.0) + (totalCreditsL6>0 ? knownWeightedSumL6 / totalCreditsL6 : 0.0));
                double xA = numeratorA / denomA;
                if (xA >= 0 && xA <= 100) {
                    Map<String, Double> cand = new HashMap<>();
                    for (String mid : missingModuleIds) cand.put(mid, xA);
                    if (xA < bestScore) { bestCandidate = cand; bestScore = xA; }
                }
            } else {
                // no missing credits in either level -> check if current meets desired
                double curL5 = (totalCreditsL5>0) ? knownWeightedSumL5 / totalCreditsL5 : 0.0;
                double curL6 = (totalCreditsL6>0) ? knownWeightedSumL6 / totalCreditsL6 : 0.0;
                double finalA = (curL5 + curL6) / 2.0;
                if (finalA >= desiredGrade) {
                    Toast.makeText(this, "Desired achieved already by Method A", Toast.LENGTH_SHORT).show();
                    return Collections.emptyMap();
                } // else impossible for A because no missing marks
            }

            // METHOD B: (L5avg + 2*L6avg) / 3 = desired  => L5avg + 2*L6avg = 3*desired
            double denomB = ( (missingCreditsL5 > 0 && totalCreditsL5>0) ? (missingCreditsL5 / totalCreditsL5) : 0.0 )
                    + 2.0 * ( (missingCreditsL6 > 0 && totalCreditsL6>0) ? (missingCreditsL6 / totalCreditsL6) : 0.0 );

            if (denomB > 0) {
                double numeratorB = 3.0 * desiredGrade - ((totalCreditsL5>0 ? knownWeightedSumL5 / totalCreditsL5 : 0.0) + 2.0 * (totalCreditsL6>0 ? knownWeightedSumL6 / totalCreditsL6 : 0.0));
                double xB = numeratorB / denomB;
                if (xB >= 0 && xB <= 100) {
                    Map<String, Double> cand = new HashMap<>();
                    for (String mid : missingModuleIds) cand.put(mid, xB);
                    if (xB < bestScore) { bestCandidate = cand; bestScore = xB; }
                }
            } else {
                double curL5 = (totalCreditsL5>0) ? knownWeightedSumL5 / totalCreditsL5 : 0.0;
                double curL6 = (totalCreditsL6>0) ? knownWeightedSumL6 / totalCreditsL6 : 0.0;
                double finalB = (curL5 + 2.0*curL6) / 3.0;
                if (finalB >= desiredGrade) {
                    Toast.makeText(this, "Desired achieved already by Method B", Toast.LENGTH_SHORT).show();
                    return Collections.emptyMap();
                }
            }

            // METHOD C: L6avg = desired  -> only L6 matters
            if (totalCreditsL6 > 0) {
                if (missingCreditsL6 == 0) {
                    double currentL6avg = knownWeightedSumL6 / totalCreditsL6;
                    if (currentL6avg >= desiredGrade) {
                        // already achieved
                        Toast.makeText(this, "Desired achieved already by Method C", Toast.LENGTH_SHORT).show();
                        return Collections.emptyMap();
                    } // else impossible for C because no missing L6 marks
                } else {
                    double xC = (desiredGrade * totalCreditsL6 - knownWeightedSumL6) / missingCreditsL6;
                    if (xC >= 0 && xC <= 100) {
                        // Only L6 missing modules matter; set predictions only for missing L6 modules
                        Map<String, Double> cand = new HashMap<>();
                        for (String mid : missingModuleIds) {
                            Integer lvl = moduleLevelMap.get(mid);
                            if (lvl != null && lvl == 6) {
                                cand.put(mid, xC);
                            }
                        }
                        if (!cand.isEmpty() && xC < bestScore) { bestCandidate = cand; bestScore = xC; }
                    }
                }
            }
        }

        // If no feasible candidate found
        if (bestCandidate == null || bestCandidate.isEmpty()) {
            Toast.makeText(this, "No feasible method found to reach the desired target within 0–100 range.", Toast.LENGTH_LONG).show();
            return Collections.emptyMap();
        }

        // Final validation: ensure all predicted values in 0-100
        for (double v : bestCandidate.values()) {
            if (Double.isNaN(v) || v < 0 || v > 100) {
                Toast.makeText(this, "Calculated required marks are out of bounds (0–100).", Toast.LENGTH_LONG).show();
                return Collections.emptyMap();
            }
        }

        return bestCandidate;
    }

    private void highlightPredictedMarks(Map<String, Double> predictedMarks) {
        clearPredictedHints();

        for (Map.Entry<String, Double> entry : predictedMarks.entrySet()) {
            String moduleId = entry.getKey();
            double required = entry.getValue();
            EditText input = markInputs.get(moduleId);
            if (input != null) {
                input.setHint(String.format(Locale.getDefault(), "%.1f%% needed", required));
                input.setHintTextColor(ContextCompat.getColor(this, R.color.teal));
            }
        }
    }

    private void clearPredictedHints() {
        for (EditText et : markInputs.values()) {
            et.setHint("Enter mark");
            et.setHintTextColor(Color.GRAY);
        }
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

            ArrayList<Integer> marks = new ArrayList<>();
            for (String moduleId : moduleIds) {
                String input = markInputs.get(moduleId).getText().toString();
                if (!input.isEmpty()) {
                    try {
                        marks.add(Integer.parseInt(input));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            String methodDClass = marks.isEmpty() ? "No Data" : AcademicCalculator.MethodD(marks);
            sb.append("Method D: Mode → ").append(methodDClass).append("\n");

            String[] classifications = {
                    (String) results.get("classA"),
                    (String) results.get("classB"),
                    (String) results.get("classC"),
                    methodDClass
            };
            String highest = AcademicCalculator.GetHighestResult(classifications);
            sb.append("\nHighest Achievable Classification: ").append(highest);
        }

        txtResult.setText(sb.toString());
    }

    private String formatDecimal(Object value) {
        if (value instanceof Number) {
            return String.format(Locale.getDefault(), "%.1f", ((Number) value).doubleValue());
        }
        return value != null ? value.toString() : "";
    }
}

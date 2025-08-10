package com.example.softwaredevelopmentassessment2;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeCalculatorService {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface GradeCallback {
        void onComplete(Map<String, Object> results);
        void onError(Exception e);
    }

    public static void calculateGrades(String courseID, boolean isPostGrad, boolean isFoundation, Map<String, Integer> localMarks, GradeCallback callback) {
        Query base = db.collection("modules").whereEqualTo("courseID", courseID);

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        ArrayList<Integer> creditsAll = new ArrayList<>();
        ArrayList<Integer> marksAll = new ArrayList<>();
        boolean altGrading;

        if (isPostGrad || isFoundation) {
            altGrading = false;
            tasks.add(base.get());
        } else {
            altGrading = true;
            tasks.add(base.whereEqualTo("level", 5).get());
            tasks.add(base.whereEqualTo("level", 6).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            if (isPostGrad || isFoundation) {
                QuerySnapshot snap = (QuerySnapshot) results.get(0);
                for (QueryDocumentSnapshot doc : snap) {
                    Long cr = doc.getLong("credits");
                    String moduleId = doc.getId();
                    if (cr != null && localMarks.containsKey(moduleId)) {
                        creditsAll.add(cr.intValue());
                        marksAll.add(localMarks.get(moduleId));
                    }
                }

                if (creditsAll.isEmpty()) {
                    callback.onError(new IllegalStateException("No marks entered"));
                    return;
                }

                String finalGrade = isFoundation
                        ? AcademicCalculator.FoundationMethod(creditsAll, marksAll)
                        : AcademicCalculator.PostgraduateMethod(creditsAll, marksAll);

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("finalGrade", finalGrade);

                if (!isFoundation) {
                    resultMap.put("average", AcademicCalculator.GetCourseAverage(creditsAll, marksAll));
                }

                callback.onComplete(resultMap);

            } else {
                // Undergrad path
                QuerySnapshot snap5 = (QuerySnapshot) results.get(0);
                ArrayList<Integer> credits5 = new ArrayList<>(), marks5 = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snap5) {
                    Long cr = doc.getLong("credits");
                    String moduleId = doc.getId();
                    if (cr != null && localMarks.containsKey(moduleId)) {
                        credits5.add(cr.intValue());
                        marks5.add(localMarks.get(moduleId));
                    }
                }

                QuerySnapshot snap6 = (QuerySnapshot) results.get(1);
                ArrayList<Integer> credits6 = new ArrayList<>(), marks6 = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snap6) {
                    Long cr = doc.getLong("credits");
                    String moduleId = doc.getId();
                    if (cr != null && localMarks.containsKey(moduleId)) {
                        credits6.add(cr.intValue());
                        marks6.add(localMarks.get(moduleId));
                    }
                }

                if (credits5.isEmpty() || credits6.isEmpty()) {
                    callback.onError(new IllegalStateException("No marks entered for one or more levels"));
                    return;
                }

                double L5avg = AcademicCalculator.GetCourseAverage(credits5, marks5);
                double L6avg = AcademicCalculator.GetCourseAverage(credits6, marks6);

                double avgA = AcademicCalculator.MethodA(L5avg, L6avg);
                double avgB = AcademicCalculator.MethodB(L5avg, L6avg);
                double avgC = AcademicCalculator.MethodC(L6avg);

                String classA = AcademicCalculator.CheckClassification(avgA, altGrading);
                String classB = AcademicCalculator.CheckClassification(avgB, altGrading);
                String classC = AcademicCalculator.CheckClassification(avgC, altGrading);

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("avgA", avgA);
                resultMap.put("classA", classA);
                resultMap.put("avgB", avgB);
                resultMap.put("classB", classB);
                resultMap.put("avgC", avgC);
                resultMap.put("classC", classC);

                callback.onComplete(resultMap);
            }
        }).addOnFailureListener(callback::onError);
    }
}

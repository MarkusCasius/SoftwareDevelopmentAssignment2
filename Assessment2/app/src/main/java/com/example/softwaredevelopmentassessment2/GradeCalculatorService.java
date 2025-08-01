package com.example.softwaredevelopmentassessment2;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GradeCalculatorService {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface GradeCallback {
        void onComplete(Map<String, Object> results);
        void onError(Exception e);
    }

    public static void calculateGrades(String courseID, boolean isPostGrad, boolean isFoundation, Map<String, Integer> localMarks, GradeCallback callback) {
        // Base Firestore query builder
        Query base = db.collection("modules").whereEqualTo("courseID", courseID);

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        ArrayList<Integer> creditsAll = new ArrayList<>();
        ArrayList<Integer> marksAll = new ArrayList<>();

        if (isPostGrad || isFoundation) {
            // Single query: all levels
            tasks.add(base.get());
        } else {
            // Two level queries for undergrad: L5 and L6
            tasks.add(base.whereEqualTo("level", 5).get());
            tasks.add(base.whereEqualTo("level", 6).get());
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            if (isPostGrad || isFoundation) {
                QuerySnapshot snap = (QuerySnapshot) results.get(0);
                for (QueryDocumentSnapshot doc : snap) {
                    Long cr = doc.getLong("credits");
                    Long mk = doc.getLong("mark");
                    if (cr != null && mk != null) {
                        creditsAll.add(cr.intValue());
                        marksAll.add(mk.intValue());
                    }
                }
                String finalGrade = isFoundation
                        ? AcademicCalculator.FoundationMethod(creditsAll, marksAll)
                        : AcademicCalculator.PostgraduateMethod(creditsAll, marksAll);

                callback.onComplete(Map.of(
                        "finalGrade", finalGrade,
                        "average", isFoundation ? null : AcademicCalculator.GetCourseAverage(creditsAll, marksAll)
                ));
            } else {
                // Undergrad path
                // first Snapshot L5
                QuerySnapshot snap5 = (QuerySnapshot) results.get(0);
                ArrayList<Integer> credits5 = new ArrayList<>(), marks5 = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snap5) {
                    Long cr = doc.getLong("credits"), mk = doc.getLong("mark");
                    if (cr!=null && mk!=null) {
                        credits5.add(cr.intValue());
                        marks5.add(mk.intValue());
                    }
                }
                double L5avg = AcademicCalculator.GetCourseAverage(credits5, marks5);

                // second Snapshot L6
                QuerySnapshot snap6 = (QuerySnapshot) results.get(1);
                ArrayList<Integer> credits6 = new ArrayList<>(), marks6 = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snap6) {
                    Long cr = doc.getLong("credits"), mk = doc.getLong("mark");
                    if (cr!=null && mk!=null) {
                        credits6.add(cr.intValue());
                        marks6.add(mk.intValue());
                    }
                }
                double L6avg = AcademicCalculator.GetCourseAverage(credits6, marks6);

                double avgA = AcademicCalculator.MethodA(L5avg, L6avg);
                double avgB = AcademicCalculator.MethodB(L5avg, L6avg);
                double avgC = AcademicCalculator.MethodC(L6avg);

                String classA = AcademicCalculator.CheckClassification(avgA, false);
                String classB = AcademicCalculator.CheckClassification(avgB, false);
                String classC = AcademicCalculator.CheckClassification(avgC, false);

                callback.onComplete(Map.of(
                        "avgA", avgA, "classA", classA,
                        "avgB", avgB, "classB", classB,
                        "avgC", avgC, "classC", classC
                ));
            }
        }).addOnFailureListener(callback::onError);
    }
}

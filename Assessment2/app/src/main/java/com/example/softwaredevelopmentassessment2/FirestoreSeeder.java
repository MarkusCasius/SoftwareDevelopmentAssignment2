//package com.example.softwaredevelopmentassessment2;

//import android.util.Log;
//
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.HashMap;
//import java.util.Map;

//public class FirestoreSeeder {
//
//    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
//    private static int moduleCounter = 1000; // Used for moduleID generation
//
//    public static void seedDatabase() {
//        seedFoundationCourse();
//        seedPostgradCourse();
//        expandCS101();
//    }
//
//    private static void seedFoundationCourse() {
//        String foundationCourseId = db.collection("course").document().getId();
//        Map<String, Object> foundationCourse = new HashMap<>();
//        foundationCourse.put("courseID", "FOUND001");
//        foundationCourse.put("name", "Foundation in Computing");
//        foundationCourse.put("courseType", "Foundation");
//
//        db.collection("course").document(foundationCourseId).set(foundationCourse)
//                .addOnSuccessListener(aVoid -> Log.d("SEED", "Foundation course added"))
//                .addOnFailureListener(e -> Log.e("SEED", "Error adding foundation course", e));
//
//        // 6 modules × 20 credits = 120 credits
//        for (int i = 1; i <= 6; i++) {
//            addModule("FOUND001", "Foundation Module " + i, 20, 4, i % 2 == 0);
//        }
//    }
//
//    private static void seedPostgradCourse() {
//        String postgradCourseId = db.collection("course").document().getId();
//        Map<String, Object> postgradCourse = new HashMap<>();
//        postgradCourse.put("courseID", "PG001");
//        postgradCourse.put("name", "MSc Advanced Computing");
//        postgradCourse.put("courseType", "Postgraduate");
//
//        db.collection("course").document(postgradCourseId).set(postgradCourse)
//                .addOnSuccessListener(aVoid -> Log.d("SEED", "Postgrad course added"))
//                .addOnFailureListener(e -> Log.e("SEED", "Error adding postgrad course", e));
//
//        // 3 modules × 20 credits = 60 credits
//        for (int i = 1; i <= 3; i++) {
//            addModule("PG001", "Postgrad Module " + i, 20, 7, i % 2 != 0);
//        }
//    }
//
//    private static void expandCS101() {
//        // Level 5 modules — enough to total 120 credits
//        for (int i = 1; i <= 6; i++) {
//            addModule("CS101", "CS101 Level 5 Module " + i, 20, 5, i % 2 == 0);
//        }
//
//        // Level 6 modules — another 120 credits
//        for (int i = 1; i <= 6; i++) {
//            addModule("CS101", "CS101 Level 6 Module " + i, 20, 6, i % 2 != 0);
//        }
//    }
//
//    private static void addModule(String courseID, String name, int credits, int level, boolean isCore) {
//        Map<String, Object> module = new HashMap<>();
//        module.put("courseID", courseID);
//        module.put("moduleName", name); // Make sure GradeCalculatorActivity matches this field name
//        module.put("credits", credits);
//        module.put("level", level);
//        module.put("isCore", isCore);
//        module.put("moduleID", String.format("%04d", ++moduleCounter));
//
//        db.collection("modules").add(module)
//                .addOnSuccessListener(ref -> Log.d("SEED", "Module added: " + ref.getId() + " " + module))
//                .addOnFailureListener(e -> Log.e("SEED", "Error adding module", e));
//    }
//}

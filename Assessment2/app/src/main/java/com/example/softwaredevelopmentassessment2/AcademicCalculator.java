package com.example.softwaredevelopmentassessment2;

import java.util.ArrayList;

/*
This class is used to handle the calculations related to getting the results for the modules of the academic calculator.
*/

/*

On the assignment 1 doc, there is pdf for taught programme and regulations. There is the details for
Post-grad and foundation calculation methods

Post-grad: 60 Credits, 1 method - Calculate Mean
Mark range Award classification
Below 49.49 Fail
49.5 – 59.49 Pass
59.5 – 69.49 Merit
69.5 – 100 Distinction

Foundation: 120 Credits, 1 Method - Passed Assessments to get 120 total Credits to go into next year
Pass mark is 40.
 */

public class AcademicCalculator {

    // Method gets the average of the methods inputted which is used by the academic calculator methods
    public static double GetCourseAverage(ArrayList<Integer> credits, ArrayList<Integer> marks) {
        int creditsTotal = 0;
        int combinedTotal = 0;
        double courseAverage = 0;
        for (int i = 0; i < credits.size(); ++i) {
            creditsTotal += credits.get(i);
            combinedTotal += (credits.get(i) * marks.get(i));
        }
        courseAverage += combinedTotal / creditsTotal;
        return courseAverage;
    }

    // Returns the highest grade achieved between the methods
    public static String GetHighestResult(String[] methodResults) {
        String first = "1st";
        String secondFirst = "2:1";
        String secondSecond = "2:2";
        String third = "3rd";
        String fail = "Fail";
        String highestResult = fail;
        // The for-loop searches through the entities within methodresults, and updates highest result and returning back the found result.
        for (int i = 0; i < methodResults.length; i++) {
            if (methodResults[i].equals(first)) {
                highestResult = first;
                break;
            } else if (methodResults[i].equals(secondFirst)) {
                highestResult = secondFirst;
            } else if (methodResults[i].equals(secondSecond) && !(highestResult.equals(secondFirst))) {
                highestResult = secondSecond;
            } else if ((methodResults[i].equals(third) && !(highestResult.equals(secondFirst)) && !(highestResult.equals(secondSecond)))) {
                highestResult = third;
            }
        }
        return highestResult;
    }

    // Returns the grade/classification of the average from the module/course.
    public static String CheckClassification(double achievedAverage, boolean isUnderGrad) {
        double first = 69.50;
        double twoOne = 59.50;
        double twoTwo = 49.50;
        double third = 39.50;

        if (isUnderGrad) {
            if (achievedAverage >= first) {
                return ("1st");
            } else if (achievedAverage >= twoOne) {
                return ("2:1");
            } else if (achievedAverage >= twoTwo) {
                return ("2:2");
            } else if (achievedAverage >= third) {
                return ("3rd");
            } else {
                return ("Fail");
            }
        } else {
            if (achievedAverage >= first) {
                return ("Distinction");
            } else if (achievedAverage >= twoOne) {
                return ("Merit");
            } else if (achievedAverage >= twoTwo) {
                return ("Pass");
            } else {
                return ("Fail");
            }
        }
    }

    // Methods A through C utilise averages from the levels and finds the overall average, then returns it
    public static double MethodA(double L5Average, double L6Average) {
        int NoOfAverages = 2;
        double overallAverage = (L5Average + L6Average) / NoOfAverages;
        return overallAverage;
    }

    public static double MethodB(double L5Average, double L6Average) {
        int NoOfAverages = 3;
        double overallAverage = (L5Average + L6Average + L6Average) / NoOfAverages;
        return overallAverage;
    }

    public static double MethodC(double L6Average) {
        int NoOfAverages = 1;
        double overallAverage = L6Average / NoOfAverages;
        return overallAverage;
    }

    // Method D uses the mode to find the most common grade and returning it.
    public static String MethodD(ArrayList<Integer> marks) {
        String first = "1st"; int firstMode = 0;
        String secondFirst = "2:1"; int secondFirstMode = 0;
        String secondSecond = "2:2"; int secondSecondMode = 0;
        String third = "3rd"; int thirdMode = 0;
        String fail = "Fail"; int failMode = 0;

        // For loop iterates through the array list, finding what marks are, their grade, then increasing the respective grade's mode
        // and returning back the highest mode grade found.
        for (int i = 0; i < marks.size(); ++i) {
            int courseMarks = marks.get(i);
            String grade = CheckClassification(courseMarks, false);
            if (grade.equals(first)) {
                firstMode ++;
            }
            else if (grade.equals(secondFirst)) {
                secondFirstMode ++;
            }
            else if (grade.equals(secondSecond)) {
                secondSecondMode ++;
            }
            else if (grade.equals(third)) {
                thirdMode ++;
            }
            else if (grade.equals(fail)) {
                failMode ++;
            }
        }

        if (firstMode >= secondFirstMode && firstMode >= secondSecondMode && firstMode >= thirdMode && firstMode >= failMode) {
            return first;
        }
        else if (secondFirstMode >= secondSecondMode && secondFirstMode >= thirdMode && secondFirstMode >= failMode) {
            return secondFirst;
        }
        else if (secondSecondMode >= thirdMode && secondSecondMode >= failMode) {
            return secondSecond;
        }
        else if (thirdMode >= failMode) {
            return third;
        }
        else {
            return fail;
        }
    }

    public static String FoundationMethod(ArrayList<Integer> credits, ArrayList<Integer> marks) {
        String fail = "Fail";
        String pass = "Pass";
        int creditPool = 0;

        for (int i = 0; i < credits.size(); i++) {
            if (marks.get(i) >= 39.50) {
                creditPool += credits.get(i);
            }
        }

        if (creditPool >= 120) {
            return pass;
        } else {
            return fail;
        }
    }

    public static String PostgraduateMethod(ArrayList<Integer> credits, ArrayList<Integer> marks) {
        return CheckClassification(GetCourseAverage(credits, marks), true);
    }
}



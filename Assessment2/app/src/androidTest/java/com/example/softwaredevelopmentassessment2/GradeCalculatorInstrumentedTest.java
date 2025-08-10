package com.example.softwaredevelopmentassessment2;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class GradeCalculatorInstrumentedTest {

    @Test
    public void testMethodA_returnsExpectedClassification() {
        double L5avg = 75.0;
        double L6avg = 72.0;

        double overall = AcademicCalculator.MethodA(L5avg, L6avg); // should be 73.5
        String classification = AcademicCalculator.CheckClassification(overall, true);

        assertEquals("1st", classification);
    }

    @Test
    public void testMethodB_returnsExpectedClassification() {
        double L5avg = 72.0;
        double L6avg = 75.0;

        double overall = AcademicCalculator.MethodB(L5avg, L6avg); // (72 + 75 + 75) / 3 = 74.0
        String classification = AcademicCalculator.CheckClassification(overall, true);

        assertEquals("1st", classification);
    }

    @Test
    public void testMethodC_returnsExpectedClassification() {
        double L6avg = 80.0;

        double overall = AcademicCalculator.MethodC(L6avg); // 80.0
        String classification = AcademicCalculator.CheckClassification(overall, true);

        assertEquals("1st", classification);
    }

    @Test
    public void testMethodD_modeClassification_firstsDominate() {
        ArrayList<Integer> marks = new ArrayList<>(Arrays.asList(75, 72, 68, 80, 78));
        String result = AcademicCalculator.MethodD(marks);
        assertEquals("1st", result);
    }

    @Test
    public void testMethodD_modeClassification_twoOneDominates() {
        ArrayList<Integer> marks = new ArrayList<>(Arrays.asList(65, 68, 62, 55, 59));
        String result = AcademicCalculator.MethodD(marks);
        assertEquals("2:1", result);
    }

    @Test
    public void testGetHighestResult_includesMethodD() {
        String highest = AcademicCalculator.GetHighestResult(
                new String[]{"2:1", "2:2", "1st", "2:1"}
        );
        assertEquals("1st", highest);
    }

    @Test
    public void testGetHighestResult_allFail() {
        String highest = AcademicCalculator.GetHighestResult(
                new String[]{"Fail", "Fail", "Fail", "Fail"}
        );
        assertEquals("Fail", highest);
    }
}

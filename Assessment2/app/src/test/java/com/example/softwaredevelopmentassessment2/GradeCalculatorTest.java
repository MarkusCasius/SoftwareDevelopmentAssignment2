package com.example.softwaredevelopmentassessment2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GradeCalculatorTest {

    private Map<String, Integer> creditsMap;
    private List<String> moduleIds;

    @Before
    public void setUp() {
        creditsMap = new HashMap<>();
        moduleIds = new ArrayList<>();

        creditsMap.put("M1", 20);
        creditsMap.put("M2", 40);
        creditsMap.put("M3", 60);

        moduleIds.addAll(creditsMap.keySet());
    }

    @Test
    public void testMethodD_ModeClassification() {
        ArrayList<Integer> marks = new ArrayList<>(Arrays.asList(72, 68, 65, 59, 61));
        String classification = AcademicCalculator.MethodD(marks);

        assertEquals("2:1", classification);
    }

    @Test
    public void testGetHighestResult_IncludesMethodD() {
        String[] classifications = { "2:1", "2:2", "3rd", "1st" };
        String highest = AcademicCalculator.GetHighestResult(classifications);

        assertEquals("1st", highest);
    }

    @Test
    public void testMethodD_AllFails() {
        ArrayList<Integer> marks = new ArrayList<>(Arrays.asList(20, 30, 35, 39, 10));
        String classification = AcademicCalculator.MethodD(marks);

        assertEquals("Fail", classification);
    }
}
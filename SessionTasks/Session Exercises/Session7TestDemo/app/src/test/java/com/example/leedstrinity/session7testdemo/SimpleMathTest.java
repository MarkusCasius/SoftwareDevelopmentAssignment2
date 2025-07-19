package com.example.leedstrinity.session7testdemo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SimpleMathTest {
    private SimpleMath simpleMath;

    @Before //This is executed before the @Test executes
    public void setUp(){
        simpleMath = new SimpleMath();
        System.out.println("Ready for testing");
    }
    @After //This is executed after the @Test
    public void tearDown(){
        System.out.println("Done with testing");
    }
    @Test
    public void testAdd() {
        int total = simpleMath.add(4, 5);
        assertEquals("Simple Math is not adding correctly", 9, total);
        //The message here is displayed if the test fails
    }
    @Test
    public void testDiff() {
        int total = simpleMath.diff(9, 2);
        assertEquals("Simple Math is not subtracting correctly", 7, total);
    }
    @Test
    public void testDiv(){
        double quotient = simpleMath.div(9,3);
        assertEquals("Simple math is not dividing correctly", 3.0, quotient, 0.0);
    }    //@Ignore //This ignores the test below
    @Test
    public void testDivWithZeroDivisor(){
        double quotient = simpleMath.div(9,0);
        assertEquals("Simple math is not handling division by zero correctly", 0.0, quotient, 0.0);
    }
}

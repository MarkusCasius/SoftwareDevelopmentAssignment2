package com.example.leedstrinity.session8expressotest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static java.util.regex.Pattern.matches;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.Test;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HelloWorldEspressoTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void textViewTest() {
        onView(withText("Hello World Espresso!")).check(matches(isDisplayed()));

    }

    @Test
    public void editTextTest() {
        onView(withId(R.id.editTextFirstName)).perform(typeText("First Name"));
        onView(withId(R.id.editTextFirstName)).perform(click());
        onView(withId(R.id.editTextFirstName)).perform(clearText());
        onView(withId(R.id.editTextFirstName)).perform(typeText("Another first name"));
        pauseTestFor(500);

        onView(withId(R.id.editTextFirstName)).perform(ViewActions.clearText())
                .perform(ViewActions.typeText("My Name"),closeSoftKeyboard());
        onView(withId(R.id.button)).perform(click());
        onView(withId(R.id.button)).check(matches(isEnabled()));
    }

    private void pauseTestFor(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

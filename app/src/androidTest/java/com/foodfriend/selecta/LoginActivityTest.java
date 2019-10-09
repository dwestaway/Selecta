package com.foodfriend.selecta;


import android.support.test.rule.ActivityTestRule;

import com.foodfriend.selecta.AccountActivity.LoginActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


/**
 * Created by Dan on 22/04/2018.
 */
public class LoginActivityTest {

    @Rule
    public ActivityTestRule<LoginActivity> activityTestRule = new ActivityTestRule<LoginActivity>(LoginActivity.class);

    private LoginActivity activity = null;

    @Before
    public void setUp() throws Exception
    {
        activity = activityTestRule.getActivity();
    }

    @Test
    public void viewsTest() throws Exception
    {
        onView(withId(R.id.logo)).check(matches(isDisplayed()));
        onView(withId(R.id.email)).check(matches(isDisplayed()));
        onView(withId(R.id.password)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonResetPass)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonGoogle)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonSignUp)).check(matches(isDisplayed()));
    }
    @Test
    public void buttonsTest() throws Exception
    {
        onView(withId(R.id.buttonLogin)).perform(click());

    }
    @After
    public void tearDown() throws Exception
    {
        activity = null;
    }

}
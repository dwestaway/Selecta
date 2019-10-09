package com.foodfriend.selecta;

import android.support.test.rule.ActivityTestRule;

import com.foodfriend.selecta.AccountActivity.SignupActivity;
import com.foodfriend.selecta.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Dan on 16/05/2018.
 */
public class SignupActivityTest {

    @Rule
    public ActivityTestRule<SignupActivity> activityTestRule = new ActivityTestRule<>(SignupActivity.class);

    private SignupActivity activity = null;

    @Before
    public void setUp() throws Exception
    {
        activity = activityTestRule.getActivity();
    }

    @Test
    public void viewsTest() throws Exception
    {

        onView(withId(R.id.logo)).check(matches(isDisplayed()));
        onView(withId(R.id.firstName)).check(matches(isDisplayed()));
        onView(withId(R.id.email)).check(matches(isDisplayed()));
        onView(withId(R.id.password)).check(matches(isDisplayed()));
        onView(withId(R.id.signupButton)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonBack)).check(matches(isDisplayed()));
    }
    @Test
    public void buttonsTest() throws Exception
    {
        onView(withId(R.id.signupButton)).perform(click());

    }

    @After
    public void tearDown() throws Exception
    {
        activity = null;
    }
}
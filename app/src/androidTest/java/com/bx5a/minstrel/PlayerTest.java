package com.bx5a.minstrel;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class PlayerTest {
    private String searchKeyword;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() {
        searchKeyword = "red hot";
    }

    @Test
    public void enqueue_sameActivity() {
        Log.i("enqueue_sameActivity", "Test started");
        // pop side bar
        onView(withId(R.id.menuMain_search)).perform(click());
        Log.i("enqueue_sameActivity", "Side panel opened");
        // open search
        onView(withId(R.id.activityMain_searchButton)).perform(click());
        Log.i("enqueue_sameActivity", "Search fragment opened");
        // type keyword
        onView(withId(R.id.viewSearch_search)).perform(typeText(searchKeyword), closeSoftKeyboard());
        Log.i("enqueue_sameActivity", "Keyword entered");
    }
}

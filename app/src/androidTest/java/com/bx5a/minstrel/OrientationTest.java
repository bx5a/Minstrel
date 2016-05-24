package com.bx5a.minstrel;

import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.bx5a.minstrel.OrientationChangeAction.orientationLandscape;
import static com.bx5a.minstrel.OrientationChangeAction.orientationPortrait;

/**
 * Created by guillaume on 24/05/2016.
 */
public class OrientationTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() {

    }

    @After
    public void destroy() {

    }

    private void rotate() {
        onView(isRoot()).perform(orientationLandscape());
        onView(isRoot()).perform(orientationPortrait());
    }

    @Test
    public void orientation_sameActivity() {
        rotate();
        onView(withId(R.id.menuMain_search)).perform(click());
        rotate();
        onView(withId(R.id.activityMain_searchButton)).perform(click());
        rotate();
        onView(withId(R.id.menuMain_search)).perform(click());
        onView(withId(R.id.activityMain_historyButton)).perform(click());
        rotate();
        onView(withId(R.id.menuMain_search)).perform(click());
        onView(withId(R.id.activityMain_playlistButton)).perform(click());
        rotate();
        onView(withId(R.id.activityMain_playerControls)).perform(click());
        rotate();
    }
}

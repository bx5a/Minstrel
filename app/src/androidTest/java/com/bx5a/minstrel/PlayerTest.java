package com.bx5a.minstrel;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;


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

    @After
    public void destroy() {

    }

    @Test
    public void enqueue_sameActivity() {
        IdlingResource timeIdlingResource = new ElapsedTimeIdlingResource(5000);

        // pop side bar
        onView(withId(R.id.menuMain_search)).perform(click());
        // open search
        onView(withId(R.id.activityMain_searchButton)).perform(click());
        // type keyword
        onView(isAssignableFrom(EditText.class)).perform(typeText(searchKeyword));
        // fix: force wait for search to be completed
        Espresso.registerIdlingResources(timeIdlingResource);

        // fix: make the soft keyboard disappear since closeSoftKeyboard doesn't work
        pressBack();
        Espresso.unregisterIdlingResources(timeIdlingResource);

        // click on first and second item
        onData(anything()).inAdapterView(withId(R.id.viewSearch_resultList)).atPosition(0).perform(click());
        onData(anything()).inAdapterView(withId(R.id.viewSearch_resultList)).atPosition(1).perform(click());

        // fix again: Can't seem to access menu
        pressBack();

        onView(withId(R.id.menuMain_search)).perform(click());
        onView(withId(R.id.activityMain_playlistButton)).perform(click());
        // remove currently playing song
        onData(anything()).inAdapterView(withId(R.id.fragmentPlaylist_list)).atPosition(0).perform(longClick());
    }
}

package com.bx5a.minstrel;

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
        // pop side bar
        onView(withId(R.id.menuMain_search)).perform(click());
        // open search
        onView(withId(R.id.activityMain_searchButton)).perform(click());
        // type keyword
        onView(isAssignableFrom(EditText.class)).perform(typeText(searchKeyword));

        // fix: force wait for search to be completed
        IdlingResource timeIdlingResource = new ElapsedTimeIdlingResource(5000);
        Espresso.registerIdlingResources(timeIdlingResource);

        // click on first item
        onData(anything()).inAdapterView(withId(R.id.viewSearch_resultList)).atPosition(0).perform(click());
        // fix: force wait cleanup
        Espresso.unregisterIdlingResources(timeIdlingResource);

        // wait for the undo button to disappear
        IdlingResource undoButtonDisappearResource = new ElapsedTimeIdlingResource(1000);
        Espresso.registerIdlingResources(undoButtonDisappearResource);
        // click on second item
        onData(anything()).inAdapterView(withId(R.id.viewSearch_resultList)).atPosition(1).perform(click());
        Espresso.unregisterIdlingResources(undoButtonDisappearResource);

        // close keyboard
        pressBack();

        onView(withId(R.id.menuMain_search)).perform(click());
        onView(withId(R.id.activityMain_playlistButton)).perform(click());
        // remove currently playing song
        onData(anything()).inAdapterView(withId(R.id.fragmentPlaylist_list)).atPosition(0).perform(longClick());
    }
}

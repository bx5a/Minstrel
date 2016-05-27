package com.bx5a.minstrel;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
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
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.not;

/**
 * Created by guillaume on 27/05/2016.
 */
@RunWith(AndroidJUnit4.class)
public class PlayerControlsTest {
    private String searchKeyword;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() {
        searchKeyword = "nirvana - smells like teen spirit";
    }

    @After
    public void destroy() {

    }

    @Test
    public void controls_sameActivity() {
        // move to player controls
        onView(withId(R.id.activityMain_playerControls)).perform(click());

        // add three video to the list
        onView(withId(R.id.menuMain_search)).perform(click());
        onView(withId(R.id.activityMain_searchButton)).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(typeText(searchKeyword));

        // force wait for search to be completed
        IdlingResource timeIdlingResource = new ElapsedTimeIdlingResource(5000);
        Espresso.registerIdlingResources(timeIdlingResource);

        // press first
        onData(anything()).inAdapterView(withId(R.id.viewSearch_resultList)).atPosition(0).perform(click());
        Espresso.unregisterIdlingResources(timeIdlingResource);
        IdlingResource firstWaitResource = new ElapsedTimeIdlingResource(1500);
        Espresso.registerIdlingResources(firstWaitResource);

        // press second
        onData(anything()).inAdapterView(withId(R.id.viewSearch_resultList)).atPosition(1).perform(click());
        Espresso.unregisterIdlingResources(firstWaitResource);
        IdlingResource secondWaitResource = new ElapsedTimeIdlingResource(1500);
        Espresso.registerIdlingResources(secondWaitResource);

        // press third
        onData(anything()).inAdapterView(withId(R.id.viewSearch_resultList)).atPosition(2).perform(click());
        Espresso.unregisterIdlingResources(secondWaitResource);
        IdlingResource thirdWaitResource = new ElapsedTimeIdlingResource(1500);
        Espresso.registerIdlingResources(thirdWaitResource);

        // move back to previous screen
        pressBack();  // close keyboard
        pressBack();  // move back
        Espresso.unregisterIdlingResources(thirdWaitResource);

        // control bar test
        onView(withId(R.id.viewPlayer_currentSong)).check(matches(withText(equalToIgnoringCase(searchKeyword))));
        onView(withId(R.id.viewPlayer_nextSong)).check(matches(not(withText(""))));

        // move to previous should do nothing
        onView(withId(R.id.fragmentPlayer_previous)).perform(click());
        onView(withId(R.id.viewPlayer_currentSong)).check(matches(withText(equalToIgnoringCase(searchKeyword))));

        // swipe to next changes current song but since we have two in the list current and next should display smtg
        onView(withId(R.id.fragmentPlayer_next)).perform(click());
        onView(withId(R.id.viewPlayer_currentSong)).check(matches(not(withText(equalToIgnoringCase(searchKeyword)))));
        onView(withId(R.id.viewPlayer_currentSong)).check(matches(not(withText(""))));
        onView(withId(R.id.viewPlayer_nextSong)).check(matches(not(withText(""))));

        // swipe to previous moves back to the desired song
        onView(withId(R.id.fragmentPlayer_previous)).perform(click());
        onView(withId(R.id.viewPlayer_currentSong)).check(matches(withText(equalToIgnoringCase(searchKeyword))));
    }
}

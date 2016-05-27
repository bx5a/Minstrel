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
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;

/**
 * Created by guillaume on 27/05/2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchTest {
    private String searchKeyword;
    private String expectedResult;

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void init() {
        searchKeyword = "C2C - Happy Ft.";
        expectedResult = "C2C - Happy Ft. D.Martin";
    }

    @After
    public void destroy() {

    }

    @Test
    public void search_sameActivity() {
        // pop side bar
        onView(withId(R.id.menuMain_search)).perform(click());
        // open search
        onView(withId(R.id.activityMain_searchButton)).perform(click());
        // type keyword
        onView(isAssignableFrom(EditText.class)).perform(typeText(searchKeyword));

        // force wait for search to be completed
        IdlingResource timeIdlingResource = new ElapsedTimeIdlingResource(5000);
        Espresso.registerIdlingResources(timeIdlingResource);

        pressBack();

        // check first item content
        onData(anything())
                .inAdapterView(withId(R.id.viewSearch_resultList))
                .atPosition(0)
                .check(matches(hasDescendant(
                        allOf(withId(R.id.listItemVideo_title),
                                withText(containsString(expectedResult))))));

        // cleanup
        Espresso.unregisterIdlingResources(timeIdlingResource);
    }
}

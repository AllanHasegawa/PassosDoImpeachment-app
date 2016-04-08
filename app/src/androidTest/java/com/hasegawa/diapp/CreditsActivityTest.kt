package com.hasegawa.diapp

import android.support.test.espresso.Espresso
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withContentDescription
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.hasegawa.diapp.activities.CreditsActivity
import org.junit.Assume
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CreditsActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(CreditsActivity::class.java)


    fun tabletMode() {
        Assume.assumeTrue(TestUtils.isScreenSw720dp(activityRule.activity))
    }

    fun phoneMode() {
        Assume.assumeTrue(!TestUtils.isScreenSw720dp(activityRule.activity))
    }

    @Test
    fun checkIfActionBarnHasUpArrow() {
        phoneMode()
        onView(withContentDescription(R.string.abc_action_bar_up_description)).
                check(matches(isDisplayed()))
    }

    @Test
    fun openDrawerAndPressBackButtonToCloseIt() {
        phoneMode()
        onView(withId(R.id.credits_drawer_layout)).perform(DrawerActions.open())
        Espresso.pressBack()
        onView(withId(R.id.credits_toolbar)).check(matches(isDisplayed()))
    }
}

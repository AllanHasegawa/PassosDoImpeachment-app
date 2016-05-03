/*******************************************************************************
 * Copyright 2016 Allan Yoshio Hasegawa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.hasegawa.diapp

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class CreditsScreenTest : BaseTest() {

    @Test
    fun checkIfActionBarnHasUpArrow() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        onView(withContentDescription(R.string.abc_action_bar_up_description)).
                check(matches(isDisplayed()))
    }

    @Test
    fun openDrawerAndPressBackButtonToCloseIt() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())

        pressBack()

        onView(withId(R.id.credits_toolbar)).check(matches(isDisplayed()))
    }


    @Test
    fun fromCreditsToStepsList() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_step_list)).perform(ViewActions.click())

        onView(allOf(withId(R.id.step_title_tv), withText(`is`("title3"))))
                .check(matches(isDisplayed()))
    }

    @Test
    fun fromCreditsToNewsList() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_news_list)).perform(ViewActions.click())

        onView(allOf(withId(R.id.important_news_title_tv),
        withText(`is`("ntitle1"))))
                .check(matches(isDisplayed()))
    }
}

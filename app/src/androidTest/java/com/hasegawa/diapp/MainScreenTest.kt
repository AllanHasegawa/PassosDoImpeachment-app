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
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.CardView
import android.widget.TextView
import com.hasegawa.diapp.activities.MainActivity
import com.hasegawa.diapp.controllers.ListStepsController
import com.hasegawa.diapp.di.AppModule
import com.hasegawa.diapp.di.DaggerAppMemComponent
import org.hamcrest.Matchers.*
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainScreenTest {

    @get:Rule
    val activityRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        DiApp.appComponent = DaggerAppMemComponent.builder()
                .appModule(AppModule(activityRule.activity.application))
                .build()
        MemMockGen.resetStepsRepo()
        MemMockGen.resetSyncsRepo()
        MemMockGen.genRegistered()
        MemMockGen.genSynced()
        MemMockGen.genSteps()
        MemMockGen.genStepLinks()
    }


    fun tabletMode() {
        Assume.assumeTrue(TestUtils.isScreenSw720dp(activityRule.activity))
    }

    fun phoneMode() {
        Assume.assumeTrue(!TestUtils.isScreenSw720dp(activityRule.activity))
    }

    @Test
    fun titleText() {
        phoneMode()
        onView(withId(R.id.main_toolbar_expanded_tv))
                .check(matches(withText(R.string.main_toolbar_expanded_title)))
    }

    @Test
    fun tmTitleText() {
        tabletMode()
        onView(withId(R.id.main_toolbar_expanded_tv))
                .check(matches(withText(R.string.main_toolbar_tablet_title)))
    }

    @Test
    fun titleShrunkText() {
        phoneMode()
        onView(withId(R.id.main_toolbar_shrunk_tv))
                .check(matches(withText(R.string.main_toolbar_shrunk_title)))
    }

    @Test
    fun tabs() {
        phoneMode()
        val tabStrip = allOf(withParent(withId(R.id.main_tablayout)),
                withClassName(containsString("SlidingTabStrip")))
        onView(tabStrip).check(matches(TestUtils.hasNumberOfChildren(`is`(2))))
    }

    @Test
    fun swipeTabs() {
        phoneMode()
        onView(withId(R.id.main_steps_rv)).check(matches(isDisplayed()))
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeLeft())
        onView(withId(R.id.main_news_rv)).check(matches(isDisplayed()))
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeLeft())
        onView(withId(R.id.main_news_rv)).check(matches(isDisplayed()))
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeRight())
        onView(withId(R.id.main_steps_rv)).check(matches(isDisplayed()))
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeRight())
        onView(withId(R.id.main_steps_rv)).check(matches(isDisplayed()))
    }


    @Test
    fun navBarNewsBt() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_news_list)).perform(ViewActions.click())
        onView(withId(R.id.main_news_rv)).check(matches(isDisplayed()))
    }

    @Test
    fun tmNavBarNewsBt() {
        tabletMode()
        onView(withText(R.string.nav_drawer_news_list)).perform(ViewActions.click())
        Thread.sleep(300)
        onView(withId(R.id.main_news_rv)).check(matches(isDisplayed()))
    }

    @Test
    fun navBarStepsBt() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_step_list)).perform(ViewActions.click())
        onView(withId(R.id.main_steps_rv)).check(matches(isDisplayed()))
    }

    @Test
    fun tmNavBarStepsBt() {
        tabletMode()
        onView(withText(R.string.nav_drawer_step_list)).perform(ViewActions.click())
        Thread.sleep(300)
        onView(withId(R.id.main_steps_rv)).check(matches(isDisplayed()))
    }


    @Test
    fun tmNavBarCreditsBt() {
        tabletMode()
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())
        Thread.sleep(300)
        onView(withId(R.id.credits_hase_email_bt)).check(matches(isDisplayed()))
    }

    @Test
    fun tmStepItemClick() {
        tabletMode()
        onView(withText(R.string.nav_drawer_step_list)).perform(ViewActions.click())
        Thread.sleep(300)
        onView(RecyclerViewMatcher(R.id.main_steps_rv).atPosition(1))
                .perform(ViewActions.click())
        var stepPosition = ""
        onView(RecyclerViewMatcher(R.id.main_steps_rv).atPositionOnView(1,
                R.id.view_position_tv)).perform(TestUtils.customAction {
            stepPosition = (it as TextView).text.toString()
        })
        Thread.sleep(100)
        onView(allOf(withId(R.id.detail_step_number_tv), isDisplayed()))
                .check(matches(withText(stepPosition)))
    }

    @Test
    fun checkHamburgerIconIsShowing() {
        phoneMode()
        onView(withContentDescription(R.string.nav_drawer_open)).check(matches(isDisplayed()))
    }

    @Test
    fun checkHamburgerIconOpensNavDrawer() {
        phoneMode()
        onView(withContentDescription(R.string.nav_drawer_open)).perform(ViewActions.click())
        onView(withText(R.string.nav_drawer_news_list)).check(matches(isDisplayed()))
    }


    @Test
    fun goToCreditsAndComeBackWithUpButton() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(
                ViewActions.click())

        onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun goToCreditsAndComeBackWithBackButton() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        pressBack()

        onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun goToDetailAndComeBackWithUpButton() {
        phoneMode()
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeRight())
        Thread.sleep(300) // add animation time
        onView(withId(R.id.main_steps_rv)).perform(
                RecyclerViewActions.scrollToPosition<ListStepsController.Adapter.ViewHolder>(2))
        onView(RecyclerViewMatcher(R.id.main_steps_rv).atPosition(2))
                .perform(ViewActions.click())

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(
                ViewActions.click())

        onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun goToDetailAndComeBackWithBackButton() {
        phoneMode()
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeRight())
        Thread.sleep(300) // add animation time
        onView(withId(R.id.main_steps_rv)).perform(
                RecyclerViewActions.scrollToPosition<ListStepsController.Adapter.ViewHolder>(2))
        onView(RecyclerViewMatcher(R.id.main_steps_rv).atPosition(2))
                .perform(ViewActions.click())

        pressBack()

        onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()))
    }

    @Test
    fun goToTheRightDetail() {
        onView(allOf(withText(`is`("title3")), withId(R.id.step_title_tv)))
                .perform(ViewActions.click())
        Thread.sleep(300) // add animation time
        onView(withText(`is`("description3"))).check(matches(isDisplayed()))
    }


    @Test
    fun navBarCreditsBt() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        onView(withText(`is`("Allan Yoshio Hasegawa"))).check(matches(isDisplayed()))
    }

    @Test
    fun goToCreditsThenToStepList() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())


        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_step_list)).perform(ViewActions.click())
        onView(withId(R.id.main_steps_rv)).check(matches(isDisplayed()))
    }

    @Test
    fun goToCreditsThenToNews() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_news_list)).perform(ViewActions.click())
        onView(withId(R.id.main_news_rv)).check(matches(isDisplayed()))
    }

    @Test
    fun openDrawerAndPressBackButtonToCloseIt() {
        phoneMode()
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        pressBack()
        onView(withId(R.id.main_toolbar)).check(matches(isDisplayed()))
    }
}

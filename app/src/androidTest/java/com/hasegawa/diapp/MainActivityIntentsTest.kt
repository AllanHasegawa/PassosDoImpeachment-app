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

import android.app.Activity
import android.app.Instrumentation
import android.app.Instrumentation.ActivityResult
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import android.widget.TextView
import com.hasegawa.diapp.activities.CreditsActivity
import com.hasegawa.diapp.activities.MainActivity
import com.hasegawa.diapp.activities.StepDetailActivity
import com.hasegawa.diapp.adapters.NewsRvAdapter
import com.hasegawa.diapp.adapters.StepsRvAdapter
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityIntentsTest {
    @get:Rule
    val activityRule = IntentsTestRule(MainActivity::class.java)

    fun tabletMode() {
        Assume.assumeTrue(TestUtils.isScreenSw720dp(activityRule.activity))
    }

    fun phoneMode() {
        Assume.assumeTrue(!TestUtils.isScreenSw720dp(activityRule.activity))
    }

    @Before
    fun stubAllExternalIntents() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        Intents.intending(not(IntentMatchers.isInternal()))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    @Test
    fun creditsToStepList() {
        phoneMode()
        val resultIntent = Intent()
        resultIntent.putExtra(MainActivity.INTENT_VIEW_NUMBER_KEY,
                MainActivity.VIEW_PAGER_STEPS_LIST)
        Intents.intending(IntentMatchers.toPackage("com.hasegawa.diapp"))
                .respondWith(ActivityResult(Activity.RESULT_OK, resultIntent))
        onView(withId(R.id.main_drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())
        onView(withId(R.id.main_steps_rv)).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun creditsToNews() {
        phoneMode()
        val resultIntent = Intent()
        resultIntent.putExtra(MainActivity.INTENT_VIEW_NUMBER_KEY,
                MainActivity.VIEW_PAGER_NEWS_LIST)
        Intents.intending(IntentMatchers.toPackage("com.hasegawa.diapp"))
                .respondWith(ActivityResult(Activity.RESULT_OK, resultIntent))
        onView(withId(R.id.main_drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())
        onView(withId(R.id.main_news_rv)).check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun mainShareFab() {
        onView(withId(R.id.main_fab)).perform(ViewActions.click())
        Intents.intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
                IntentMatchers.hasExtra(`is`(Intent.EXTRA_INTENT), allOf(
                        IntentMatchers.hasAction(Intent.ACTION_SEND),
                        IntentMatchers.hasType("text/plain")))))
    }

    @Test
    fun navBarFeedbackBt() {
        phoneMode()
        onView(withId(R.id.main_drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_feedback)).perform(ViewActions.click())

        val feedbackUrl = activityRule.activity.getString(R.string.app_feedback_url)
        Intents.intended(
                allOf(
                        IntentMatchers.hasAction(Intent.ACTION_VIEW),
                        IntentMatchers.hasData(feedbackUrl)
                )
        )
    }

    @Test
    fun tmNavBarFeedbackBt() {
        tabletMode()
        onView(withText(R.string.nav_drawer_feedback)).perform(ViewActions.click())

        val feedbackUrl = activityRule.activity.getString(R.string.app_feedback_url)
        Intents.intended(
                allOf(
                        IntentMatchers.hasAction(Intent.ACTION_VIEW),
                        IntentMatchers.hasData(feedbackUrl)
                )
        )
    }

    @Test
    fun navBarOpensourceBt() {
        phoneMode()
        onView(withId(R.id.main_drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_opensource)).perform(ViewActions.click())

        val opensourceUrl = activityRule.activity.getString(R.string.app_opensource_url)
        Intents.intended(
                allOf(
                        IntentMatchers.hasAction(Intent.ACTION_VIEW),
                        IntentMatchers.hasData(opensourceUrl)
                )
        )
    }

    @Test
    fun tmNavBarOpensourceBt() {
        tabletMode()
        onView(withText(R.string.nav_drawer_opensource)).perform(ViewActions.click())

        val opensourceUrl = activityRule.activity.getString(R.string.app_opensource_url)
        Intents.intended(
                allOf(
                        IntentMatchers.hasAction(Intent.ACTION_VIEW),
                        IntentMatchers.hasData(opensourceUrl)
                )
        )
    }

    @Test
    fun navBarCreditsBt() {
        phoneMode()
        onView(withId(R.id.main_drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_credits)).perform(ViewActions.click())

        Intents.intended(
                IntentMatchers.hasComponent(ComponentName(activityRule.activity,
                        CreditsActivity::class.java))
        )
    }

    @Test
    fun newsShareBt() {
        phoneMode()
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeLeft())
        Thread.sleep(300) // add animation time
        onView(withId(R.id.main_news_rv)).perform(
                RecyclerViewActions.scrollToPosition<NewsRvAdapter.NewsViewHolder>(3))
        onView(RecyclerViewMatcher(R.id.main_news_rv).atPositionOnView(3,
                R.id.important_news_share_bt)).perform(ViewActions.click())

        Intents.intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
                IntentMatchers.hasExtra(`is`(Intent.EXTRA_INTENT), allOf(
                        IntentMatchers.hasAction(Intent.ACTION_SEND),
                        IntentMatchers.hasType("text/plain")))))
    }

    @Test
    fun tmNewsShareBt() {
        tabletMode()
        onView(withText(R.string.nav_drawer_news_list)).perform(ViewActions.click())
        Thread.sleep(300) // add animation time
        onView(withId(R.id.main_news_rv)).perform(
                RecyclerViewActions.scrollToPosition<NewsRvAdapter.NewsViewHolder>(1))
        onView(RecyclerViewMatcher(R.id.main_news_rv).atPositionOnView(1,
                R.id.important_news_share_bt)).perform(ViewActions.click())

        Intents.intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
                IntentMatchers.hasExtra(`is`(Intent.EXTRA_INTENT), allOf(
                        IntentMatchers.hasAction(Intent.ACTION_SEND),
                        IntentMatchers.hasType("text/plain")))))
    }

    @Test
    fun newsLinkBt() {
        phoneMode()
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeLeft())
        Thread.sleep(300) // add animation time
        onView(withId(R.id.main_news_rv)).perform(
                RecyclerViewActions.scrollToPosition<NewsRvAdapter.NewsViewHolder>(3))
        var url: String = ""
        onView(RecyclerViewMatcher(R.id.main_news_rv).atPositionOnView(3,
                R.id.important_news_url_tv)).perform(TestUtils.customAction {
            url = (it!! as TextView).text.toString()
        })
        onView(RecyclerViewMatcher(R.id.main_news_rv).atPositionOnView(3,
                R.id.important_news_open_bt)).perform(ViewActions.click())

        Intents.intended(
                allOf(
                        IntentMatchers.hasAction(Intent.ACTION_VIEW),
                        IntentMatchers.hasData(Uri.parse(url))
                )
        )
    }

    @Test
    fun tmNewsLinkBt() {
        tabletMode()
        onView(withText(R.string.nav_drawer_news_list)).perform(ViewActions.click())
        Thread.sleep(300) // add animation time
        onView(withId(R.id.main_news_rv)).perform(
                RecyclerViewActions.scrollToPosition<NewsRvAdapter.NewsViewHolder>(1))
        var url: String = ""
        onView(RecyclerViewMatcher(R.id.main_news_rv).atPositionOnView(1,
                R.id.important_news_url_tv)).perform(TestUtils.customAction {
            url = (it!! as TextView).text.toString()
        })
        onView(RecyclerViewMatcher(R.id.main_news_rv).atPositionOnView(1,
                R.id.important_news_open_bt)).perform(ViewActions.click())

        Intents.intended(
                allOf(
                        IntentMatchers.hasAction(Intent.ACTION_VIEW),
                        IntentMatchers.hasData(Uri.parse(url))
                )
        )
    }

    @Test
    fun stepItemClick() {
        phoneMode()
        onView(withId(R.id.main_view_pager)).perform(ViewActions.swipeRight())
        Thread.sleep(300) // add animation time
        onView(withId(R.id.main_steps_rv)).perform(
                RecyclerViewActions.scrollToPosition<StepsRvAdapter.StepViewHolder>(2))
        onView(RecyclerViewMatcher(R.id.main_steps_rv).atPosition(2))
                .perform(ViewActions.click())

        Intents.intended(
                IntentMatchers.hasComponent(
                        ComponentName(activityRule.activity,
                                StepDetailActivity::class.java)
                )
        )
    }
}

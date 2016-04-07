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
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.contrib.DrawerActions
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.hasegawa.diapp.activities.CreditsActivity
import com.hasegawa.diapp.activities.MainActivity
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
class CreditsActivityIntentsTest {
    @get:Rule
    val activityRule = IntentsTestRule(CreditsActivity::class.java)

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
    fun goToMainActivityStepLists() {
        phoneMode()
        onView(withId(R.id.credits_drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_step_list)).perform(ViewActions.click())

        Intents.intended(
                allOf(
                        IntentMatchers.hasComponent(ComponentName(
                                activityRule.activity, MainActivity::class.java)
                        ),
                        IntentMatchers.hasExtra(MainActivity.INTENT_VIEW_NUMBER_KEY,
                                MainActivity.VIEW_PAGER_STEPS_LIST))
        )
    }

    @Test
    fun goToMainActivityNewsLists() {
        phoneMode()
        onView(withId(R.id.credits_drawer_layout)).perform(DrawerActions.open())
        onView(withText(R.string.nav_drawer_news_list)).perform(ViewActions.click())

        Intents.intended(
                allOf(
                        IntentMatchers.hasComponent(ComponentName(
                                activityRule.activity, MainActivity::class.java)
                        ),
                        IntentMatchers.hasExtra(MainActivity.INTENT_VIEW_NUMBER_KEY,
                                MainActivity.VIEW_PAGER_NEWS_LIST))
        )
    }

    @Test
    fun creditsEmailFab() {
        phoneMode()
        val haseEmail = Uri.fromParts("mailto",
                activityRule.activity.getString(R.string.credits_hase_email), null)
        onView(withId(R.id.credits_fab)).perform(ViewActions.click())
        Intents.intended(IntentMatchers
                .hasExtra(`is`(Intent.EXTRA_INTENT),
                        allOf(
                                IntentMatchers.hasAction(Intent.ACTION_SENDTO),
                                IntentMatchers.hasData(haseEmail))))
    }

    @Test
    fun creditsHaseGithubBt() {
        phoneMode()
        onView(withId(R.id.credits_hase_github_bt)).perform(ViewActions.click())
        val haseGithubUrl = activityRule.activity.getString(R.string.credits_hase_github_url)
        Intents.intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(haseGithubUrl)))
    }

    @Test
    fun creditsHaseEmailBt() {
        phoneMode()
        onView(withId(R.id.credits_hase_email_bt)).perform(ViewActions.click())
        val haseEmail = Uri.fromParts("mailto",
                activityRule.activity.getString(R.string.credits_hase_email), null)
        Intents.intended(IntentMatchers
                .hasExtra(`is`(Intent.EXTRA_INTENT),
                        allOf(
                                IntentMatchers.hasAction(Intent.ACTION_SENDTO),
                                IntentMatchers.hasData(haseEmail))))
    }
}


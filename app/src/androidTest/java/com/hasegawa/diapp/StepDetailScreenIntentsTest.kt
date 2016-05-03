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
import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.intent.Intents
import android.support.test.espresso.intent.matcher.IntentMatchers
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.hasegawa.diapp.not_tests.BaseTest
import org.hamcrest.Matchers.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StepDetailScreenIntentsTest : BaseTest() {

    @Before
    fun stubAllExternalIntents() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        Intents.intending(not(IntentMatchers.isInternal()))
                .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    @Test
    fun shareFab() {
        phoneMode()
        onView(allOf(withText(`is`("title3")), withId(R.id.step_title_tv)))
                .perform(ViewActions.click())


        onView(withId(R.id.detail_fab)).perform(ViewActions.click())
        Intents.intended(allOf(
                IntentMatchers.hasAction(Intent.ACTION_CHOOSER),
                IntentMatchers.hasExtra(`is`(Intent.EXTRA_INTENT), allOf(
                        IntentMatchers.hasAction(Intent.ACTION_SEND),
                        IntentMatchers.hasType("text/plain")))))
    }
}

/*
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
 */

package com.hasegawa.diapp

import android.support.test.espresso.intent.rule.IntentsTestRule
import com.hasegawa.diapp.activities.MainActivity
import com.hasegawa.diapp.di.AppModule
import com.hasegawa.diapp.di.DaggerAppMemComponent
import org.junit.Assume
import org.junit.Before
import org.junit.Rule

/**
 * Created by hasegawa on 5/3/2016.
 */
open class BaseTest {
    @get:Rule
    val activityRule = IntentsTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        DiApp.appComponent = DaggerAppMemComponent.builder()
                .appModule(AppModule(activityRule.activity.application))
                .build()
        MemMockGen.setupAll()
    }

    fun tabletMode() {
        Assume.assumeTrue(TestUtils.isScreenSw720dp(activityRule.activity))
    }

    fun phoneMode() {
        Assume.assumeTrue(!TestUtils.isScreenSw720dp(activityRule.activity))
    }
}

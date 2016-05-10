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

package com.hasegawa.diapp.not_tests

import android.app.Application
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.activities.MainActivity
import com.hasegawa.diapp.di.AppModule
import com.hasegawa.diapp.di.DaggerAppMemComponent
import com.hasegawa.diapp.di.MemServicesModule
import org.junit.Assume
import org.junit.Before
import org.junit.Rule

open class BaseTest {
    @get:Rule
    val activityRule =
            DaggerActivityTestRule(MainActivity::class.java,
                    object : DaggerActivityTestRule.OnBeforeActivityLaunchedListener<MainActivity> {
                        override fun beforeActivityLaunched(application: Application, activity: MainActivity?) {
                            MemMockGen.setupAll()
                            DiApp.appComponent = DaggerAppMemComponent.builder()
                                    .appModule(AppModule(application))
                                    .memServicesModule(MemServicesModule())
                                    .build()
                        }
                    })

    @Before
    fun setUp() {
        MemMockGen.setupAll()
    }

    fun tabletMode() {
        Assume.assumeTrue(TestUtils.isScreenSw720dp(activityRule.activity))
    }

    fun phoneMode() {
        Assume.assumeTrue(!TestUtils.isScreenSw720dp(activityRule.activity))
    }
}

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

import android.app.Activity
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.matcher.ViewMatchers
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

object TestUtils {
    fun hasNumberOfChildren(numChildrenMatcher: Matcher<Int>): Matcher<View> = object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description) {
            description.appendText(" a view with # children is ")
            numChildrenMatcher.describeTo(description)
        }

        override fun matchesSafely(item: View): Boolean {
            return (item is ViewGroup) && (numChildrenMatcher.matches((item as ViewGroup).childCount))
        }
    }

    fun clickChildViewWithId(childId: Int) = object : ViewAction {
        override fun getConstraints(): Matcher<View>? {
            return null;//ViewMatchers.withChild(ViewMatchers.withId(id))
        }

        override fun getDescription(): String? {
            return "Click on a child view with specified id."
        }

        override fun perform(uiController: UiController, view: View) {
            val child = view.findViewById(childId)
            child.performClick()
        }
    }

    fun customAction(f: (View?) -> Unit) = object : ViewAction {
        override fun getConstraints(): Matcher<View>? {
            return ViewMatchers.isDisplayingAtLeast(90)
        }

        override fun getDescription(): String? {
            return "Custom action on view"
        }

        override fun perform(uiController: UiController?, view: View?) {
            f(view)
        }
    }

    fun isScreenSw720dp(activity: Activity): Boolean {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val widthDp = displayMetrics.widthPixels / displayMetrics.density
        val heightDp = displayMetrics.heightPixels / displayMetrics.density
        val screenSw = Math.min(widthDp, heightDp)
        return screenSw >= 720
    }
}

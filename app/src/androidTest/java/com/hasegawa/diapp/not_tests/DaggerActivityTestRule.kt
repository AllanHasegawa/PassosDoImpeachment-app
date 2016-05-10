package com.hasegawa.diapp.not_tests

import android.app.Activity
import android.app.Application
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.intent.rule.IntentsTestRule

/**
 * [ActivityTestRule] which provides hook for
 * [ActivityTestRule.beforeActivityLaunched] method. Can be used for test dependency
 * injection especially in Espresso based tests.

 * @author Tomasz Rozbicki (modified/destroyed by Allan Yoshio Hasegawa)
 */
class DaggerActivityTestRule<T : Activity>(
        activityClass: Class<T>,
        private val mListener: DaggerActivityTestRule.OnBeforeActivityLaunchedListener<T>) :
        IntentsTestRule<T>(activityClass) {

    override fun beforeActivityLaunched() {
        super.beforeActivityLaunched()
        mListener.beforeActivityLaunched(InstrumentationRegistry.getInstrumentation()
                .targetContext.applicationContext as Application, activity)
    }

    interface OnBeforeActivityLaunchedListener<T> {
        fun beforeActivityLaunched(application: Application, activity: T?)
    }
}

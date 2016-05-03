package com.hasegawa.diapp.not_tests;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;

/**
 * {@link ActivityTestRule} which provides hook for
 * {@link ActivityTestRule#beforeActivityLaunched()} method. Can be used for test dependency
 * injection especially in Espresso based tests.
 *
 * @author Tomasz Rozbicki (modified/destroyed by Allan Yoshio Hasegawa)
 */
public class DaggerActivityTestRule<T extends Activity> extends IntentsTestRule<T> {

    private final OnBeforeActivityLaunchedListener<T> mListener;

    public DaggerActivityTestRule(Class<T> activityClass,
                                  @NonNull OnBeforeActivityLaunchedListener<T> listener) {
        super(activityClass);
        mListener = listener;
    }

    @Override
    protected void beforeActivityLaunched() {
        super.beforeActivityLaunched();
        mListener.beforeActivityLaunched((Application) InstrumentationRegistry.getInstrumentation()
                .getTargetContext().getApplicationContext(), getActivity());
    }

    public interface OnBeforeActivityLaunchedListener<T> {

        void beforeActivityLaunched(@NonNull Application application, T activity);
    }
}

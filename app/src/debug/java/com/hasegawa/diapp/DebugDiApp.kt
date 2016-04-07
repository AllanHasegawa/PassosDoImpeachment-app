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

import android.os.Build
import android.os.Handler
import android.os.StrictMode
import com.facebook.stetho.Stetho
import timber.log.Timber

class DebugDiApp : DiApp() {
    override fun onCreate() {
        super.onCreate()

//        tryToEnableStrictMode()

        Stetho.initializeWithDefaults(this)

        Timber.d("App initiated in DEBUG mode.")
    }

    companion object {
        private fun tryToEnableStrictMode() {
            if (Build.VERSION.SDK_INT >= 9) {
                enableStrictMode()
            }

            if (Build.VERSION.SDK_INT >= 16) {
                Handler().postAtFrontOfQueue { enableStrictMode() }
            }
        }

        private fun enableStrictMode() {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build())
        }
    }
}

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

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.hasegawa.diapp.cloud.RestInfo
import com.hasegawa.diapp.di.*
import com.hasegawa.diapp.utils.L
import net.danlew.android.joda.JodaTimeAndroid

open class DiApp : Application() {
    override fun onCreate() {
        super.onCreate()

        JodaTimeAndroid.init(this)

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .restServiceModule(RestServiceModule(RestInfo.API_URL))
                .build()
        L.log = appComponent.logDevice()
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        @JvmStatic lateinit var appComponent: AppComponent
        // The app uses a single activity, so I guess it is fine to put it here?
        @JvmStatic lateinit var activityComponent: ActivityComponent
    }
}

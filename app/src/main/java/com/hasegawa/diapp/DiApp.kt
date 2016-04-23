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
import com.hasegawa.diapp.cloud.RestInfo
import com.hasegawa.diapp.cloud.restservices.retrofit.RetrofitRestService
import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderNewsRepository
import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderStepsRepository
import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderSyncsRepository
import com.hasegawa.diapp.domain.devices.SyncScheduler
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.restservices.RestService
import com.hasegawa.diapp.syncadapters.SyncAdapterScheduler
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

open class DiApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        JodaTimeAndroid.init(this)

        stepsRepository = ContentProviderStepsRepository(applicationContext.contentResolver)
        newsRepository = ContentProviderNewsRepository(applicationContext.contentResolver)
        syncsRepository = ContentProviderSyncsRepository(applicationContext.contentResolver)
        restServices = RetrofitRestService(RestInfo.API_URL)
        syncScheduler = SyncAdapterScheduler(applicationContext)

        Timber.d("App initiated")
    }

    override fun onTerminate() {
        super.onTerminate()
    }

    companion object {

        lateinit var stepsRepository: StepsRepository
        lateinit var newsRepository: NewsRepository
        lateinit var syncsRepository: SyncsRepository
        lateinit var restServices: RestService
        lateinit var syncScheduler: SyncScheduler

        const val PREFS_KEY_SENT_TOKEN_TO_SERVER = "sent_token_to_server"
        const val PREFS_KEY_LAST_UPDATE = "last_update"
        const val PREFS_KEY_SYNC_PENDING = "sync_pending"
    }
}

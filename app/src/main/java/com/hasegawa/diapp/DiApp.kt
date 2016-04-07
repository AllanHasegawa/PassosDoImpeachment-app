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
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.hasegawa.diapp.models.DiDbHelper
import com.hasegawa.diapp.models.ImportantNews
import com.hasegawa.diapp.models.Step
import com.hasegawa.diapp.models.StepLink
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber

open class DiApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())

        JodaTimeAndroid.init(this)

        dbHelper = DiDbHelper(applicationContext)
        db = dbHelper.writableDatabase

        diProvider = DefaultStorIOContentResolver.builder()
                .contentResolver(applicationContext.contentResolver)
                .addTypeMapping(Step::class.java, Step.typeMapping())
                .addTypeMapping(StepLink::class.java, StepLink.typeMapping())
                .addTypeMapping(ImportantNews::class.java, ImportantNews.typeMapping())
                .build()

        Timber.d("App initiated")
    }

    override fun onTerminate() {
        super.onTerminate()
        db.close()
    }

    companion object {
        lateinit var diProvider: StorIOContentResolver
        lateinit var db: SQLiteDatabase
        lateinit var dbHelper: SQLiteOpenHelper

        const val PREFS_KEY_SENT_TOKEN_TO_SERVER = "sent_token_to_server"
        const val PREFS_KEY_LAST_UPDATE = "last_update"
        const val PREFS_KEY_SYNC_PENDING = "sync_pending"
    }
}

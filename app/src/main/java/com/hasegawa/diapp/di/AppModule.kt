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

package com.hasegawa.diapp.di

import android.app.Application
import android.content.Context
import com.hasegawa.diapp.devices.AppLogDevice
import com.hasegawa.diapp.devices.SyncAdapterScheduler
import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.devices.LogDevice
import com.hasegawa.diapp.domain.devices.SyncScheduler
import dagger.Module
import dagger.Provides
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Singleton

@Module
class AppModule(val context: Application) {
    @Provides @Singleton fun getApplicationContext(): Context = context

    @Provides @Singleton fun providesLogDevice(d: AppLogDevice): LogDevice = d
    @Provides @Singleton fun provideSyncScheduler(d: SyncAdapterScheduler): SyncScheduler = d

    @Provides @Singleton
    fun getExecutionThread(): ExecutionThread = ExecutionThread(Schedulers.io())

    @Provides @Singleton
    fun getPostExecutionThread(): PostExecutionThread =
            PostExecutionThread(AndroidSchedulers.mainThread())
}

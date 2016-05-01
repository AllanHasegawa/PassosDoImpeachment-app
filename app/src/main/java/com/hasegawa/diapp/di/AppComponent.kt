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

import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.devices.LogDevice
import com.hasegawa.diapp.domain.devices.SyncScheduler
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.restservices.RestService
import com.hasegawa.diapp.services.GCMListenerService
import com.hasegawa.diapp.services.GCMRegistrationService
import com.hasegawa.diapp.syncadapters.SyncAdapter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AppModule::class, RepositoriesModule::class, RestServiceModule::class))
interface AppComponent {
    fun inject(g: GCMListenerService)
    fun inject(g: GCMRegistrationService)
    fun inject(g: SyncAdapter)

    fun logDevice(): LogDevice
    fun syncScheduler(): SyncScheduler

    fun executionThread(): ExecutionThread
    fun postExecutionThread(): PostExecutionThread

    fun syncsRepository(): SyncsRepository
    fun stepsRepository(): StepsRepository
    fun newsRepository(): NewsRepository
    fun restService(): RestService
}

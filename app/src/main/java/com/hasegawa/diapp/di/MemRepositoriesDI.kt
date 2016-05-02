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

import com.hasegawa.diapp.cloud.restservices.mock.mem.MemRestService
import com.hasegawa.diapp.db.repositories.mocks.mem.MemNewsRepository
import com.hasegawa.diapp.db.repositories.mocks.mem.MemStepsRepository
import com.hasegawa.diapp.db.repositories.mocks.mem.MemSyncsRepository
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.restservices.RestService
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class MemServicesModule {
    @Provides @Singleton
    fun provideStepsRepo(): StepsRepository = MemStepsRepository()

    @Provides @Singleton
    fun provideSyncsRepo(): SyncsRepository = MemSyncsRepository()

    @Provides @Singleton
    fun provideNewsRepo(): NewsRepository = MemNewsRepository()

    @Provides @Singleton
    fun provideRestServices(): RestService = MemRestService()
}

@Singleton
@Component(modules = arrayOf(AppModule::class, MemServicesModule::class))
interface AppMemComponent : AppComponent {

}
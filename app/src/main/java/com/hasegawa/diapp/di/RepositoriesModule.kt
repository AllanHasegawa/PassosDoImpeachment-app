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

import android.content.ContentResolver
import android.content.Context
import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderNewsRepository
import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderStepsRepository
import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderSyncsRepository
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoriesModule {
    @Provides @Singleton
    fun contentResolver(context: Context): ContentResolver = context.contentResolver

    @Provides @Singleton
    fun syncsRepository(resolver: ContentResolver): SyncsRepository {
        return ContentProviderSyncsRepository(resolver)
    }

    @Provides @Singleton
    fun stepsRepository(resolver: ContentResolver): StepsRepository {
        return ContentProviderStepsRepository(resolver)
    }

    @Provides @Singleton
    fun newsRepository(resolver: ContentResolver): NewsRepository {
        return ContentProviderNewsRepository(resolver)
    }
}

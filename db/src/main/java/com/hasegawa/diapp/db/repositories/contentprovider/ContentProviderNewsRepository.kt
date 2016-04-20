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

package com.hasegawa.diapp.db.repositories.contentprovider

import android.content.ContentResolver
import android.net.Uri
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.NewsContract
import com.hasegawa.diapp.db.repositories.contentprovider.mappings.NewsEntityMapping
import com.hasegawa.diapp.db.utils.IdUtils
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery
import com.pushtorefresh.storio.contentresolver.queries.Query
import rx.Observable

class ContentProviderNewsRepository : NewsRepository {
    private val provider: DefaultStorIOContentResolver

    constructor(contentResolver: ContentResolver) {
        provider = DefaultStorIOContentResolver.builder()
                .contentResolver(contentResolver)
                .addTypeMapping(NewsEntity::class.java, NewsEntityMapping.typeMapping())
                .build()
    }


    override fun addAllNews(news: List<NewsEntity>): Observable<List<NewsEntity>> {
        return provider.put()
                .objects(news.map {
                    it.id = IdUtils.genIdIfNull(it.id)
                    it
                })
                .prepare()
                .asRxObservable()
                .map { it.results().keys.toList() }
    }

    override fun clearNews(): Observable<Int> {
        return provider.delete()
                .byQuery(DeleteQuery.builder().uri(NewsContract.URI).build())
                .prepare()
                .asRxObservable()
                .map { it.numberOfRowsDeleted() }
    }

    override fun getNews(): Observable<List<NewsEntity>> {
        return provider.get()
                .listOfObjects(NewsEntity::class.java)
                .withQuery(Query.builder()
                        .uri(NewsContract.URI)
                        .sortOrder("${NewsContract.COL_DATE} desc")
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun notifyChange() {
        provider.internal().contentResolver().notifyChange(Uri.parse(NewsContract.URI), null)
    }
}


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
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.LinksContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.StepsContract
import com.hasegawa.diapp.db.repositories.contentprovider.mappings.StepEntityMapping
import com.hasegawa.diapp.db.repositories.contentprovider.mappings.StepLinkEntityMapping
import com.hasegawa.diapp.db.utils.IdUtils
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.entities.StepLinkEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import com.pushtorefresh.storio.contentresolver.operations.put.PutResults
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery
import com.pushtorefresh.storio.contentresolver.queries.Query
import rx.Observable

class ContentProviderStepsRepository : StepsRepository {
    private val provider: StorIOContentResolver

    constructor(contentResolver: ContentResolver) {
        this.provider = DefaultStorIOContentResolver.builder().contentResolver(contentResolver)
                .addTypeMapping(StepEntity::class.java, StepEntityMapping.typeMapping())
                .addTypeMapping(StepLinkEntity::class.java, StepLinkEntityMapping.typeMapping())
                .build()
    }

    override fun addStepLinks(links: List<StepLinkEntity>): Observable<List<StepLinkEntity>> {
        return provider.put()
                .objects(links.map {
                    it.id = IdUtils.genIdIfNull(it.id)
                    it
                })
                .prepare()
                .asRxObservable()
                .map { r: PutResults<StepLinkEntity> ->
                    r.results().keys.filter {
                        r.results()[it]!!.wasInserted()
                    }.toList()
                }
    }

    override fun addSteps(steps: List<StepEntity>): Observable<List<StepEntity>> {
        return provider.put()
                .objects(steps.map {
                    it.id = IdUtils.genIdIfNull(it.id)
                    it
                })
                .prepare()
                .asRxObservable()
                .map { r: PutResults<StepEntity> ->
                    r.results().keys.filter {
                        r.results()[it]!!.wasInserted()
                    }
                }
    }

    override fun clearStepLinks(): Observable<Int> {
        return provider.delete()
                .byQuery(DeleteQuery.builder().uri(LinksContract.URI).build())
                .prepare()
                .asRxObservable()
                .map { it.numberOfRowsDeleted() }
    }

    override fun clearSteps(): Observable<Int> {
        return provider.delete()
                .byQuery(DeleteQuery.builder().uri(StepsContract.URI).build())
                .prepare()
                .asRxObservable()
                .map { it.numberOfRowsDeleted() }
    }

    override fun getNumberOfCompletedSteps(): Observable<Int> {
        return provider.get()
                .numberOfResults()
                .withQuery(Query.builder().uri(StepsContract.URI)
                        .where("${StepsContract.COL_COMPLETED}=?").whereArgs(1)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun getStepById(id: String): Observable<StepEntity> {
        return provider.get()
                .`object`(StepEntity::class.java)
                .withQuery(Query.builder()
                        .uri(StepsContract.URI)
                        .where("${StepsContract.COL_ID}=?")
                        .whereArgs(id)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun getStepByPosition(position: Int): Observable<StepEntity> {
        return provider.get()
                .`object`(StepEntity::class.java)
                .withQuery(Query.builder()
                        .uri(StepsContract.URI)
                        .where("${StepsContract.COL_POSITION}=?")
                        .whereArgs(position)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun getStepLinks(): Observable<List<StepLinkEntity>> {
        return provider.get()
                .listOfObjects(StepLinkEntity::class.java)
                .withQuery(Query.builder()
                        .uri(LinksContract.URI)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun getStepLinksByStepId(stepId: String): Observable<List<StepLinkEntity>> {
        return provider.get()
                .listOfObjects(StepLinkEntity::class.java)
                .withQuery(Query.builder()
                        .uri(LinksContract.URI)
                        .where("${LinksContract.COL_STEPS_ID}=?")
                        .whereArgs(stepId)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun getSteps(): Observable<List<StepEntity>> {
        return provider.get()
                .listOfObjects(StepEntity::class.java)
                .withQuery(Query.builder()
                        .uri(StepsContract.URI)
                        .sortOrder("${StepsContract.COL_POSITION}")
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun notifyChange() {
        provider.internal().contentResolver().notifyChange(Uri.parse(StepsContract.URI), null)
        provider.internal().contentResolver().notifyChange(Uri.parse(LinksContract.URI), null)
    }
}

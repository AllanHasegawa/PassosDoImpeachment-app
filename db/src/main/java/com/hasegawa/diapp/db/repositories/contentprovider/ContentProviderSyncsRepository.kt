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
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.GCMMessagesContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.GCMRegistrationsContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.SyncsContract
import com.hasegawa.diapp.db.repositories.contentprovider.mappings.GCMMessageEntityMapping
import com.hasegawa.diapp.db.repositories.contentprovider.mappings.GCMRegistrationEntityMapping
import com.hasegawa.diapp.db.repositories.contentprovider.mappings.SyncEntityMapping
import com.hasegawa.diapp.db.repositories.copyWithId
import com.hasegawa.diapp.db.repositories.copyWithTime
import com.hasegawa.diapp.domain.entities.GCMMessageEntity
import com.hasegawa.diapp.domain.entities.GCMRegistrationEntity
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.pushtorefresh.storio.StorIOException
import com.pushtorefresh.storio.contentresolver.impl.DefaultStorIOContentResolver
import com.pushtorefresh.storio.contentresolver.queries.Query
import rx.Observable

class ContentProviderSyncsRepository(resolver: ContentResolver) : SyncsRepository {
    private val provider: DefaultStorIOContentResolver

    init {
        provider = DefaultStorIOContentResolver.builder()
                .contentResolver(resolver)
                .addTypeMapping(GCMMessageEntity::class.java, GCMMessageEntityMapping.typeMapping())
                .addTypeMapping(GCMRegistrationEntity::class.java, GCMRegistrationEntityMapping.typeMapping())
                .addTypeMapping(SyncEntity::class.java, SyncEntityMapping.typeMapping())
                .build()
    }

    override fun getGCMRegistrations(): Observable<List<GCMRegistrationEntity>> {
        return provider.get()
                .listOfObjects(GCMRegistrationEntity::class.java)
                .withQuery(Query.builder().uri(GCMRegistrationsContract.URI).build())
                .prepare()
                .asRxObservable()
    }

    override fun getMessages(): Observable<List<GCMMessageEntity>> {
        return provider.get()
                .listOfObjects(GCMMessageEntity::class.java)
                .withQuery(Query.builder().uri(GCMMessagesContract.URI).build())
                .prepare()
                .asRxObservable()
    }

    override fun getMessagesByType(type: Int): Observable<List<GCMMessageEntity>> {
        return provider.get()
                .listOfObjects(GCMMessageEntity::class.java)
                .withQuery(Query.builder().uri(GCMMessagesContract.URI)
                        .where("${GCMMessagesContract.COL_TYPE}=?")
                        .whereArgs(type)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun getGCMRegistrationByToken(token: String): Observable<GCMRegistrationEntity?> {
        return provider.get()
                .`object`(GCMRegistrationEntity::class.java)
                .withQuery(Query.builder().uri(GCMRegistrationsContract.URI)
                        .where("${GCMRegistrationsContract.COL_TOKEN}=?")
                        .whereArgs(token)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun getPendingSyncs(): Observable<List<SyncEntity>> {
        return provider.get()
                .listOfObjects(SyncEntity::class.java)
                .withQuery(Query.builder().uri(SyncsContract.URI)
                        .where("${SyncsContract.COL_PENDING}>?")
                        .whereArgs(0)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun getSuccessfulSyncs(): Observable<List<SyncEntity>> {
        return provider.get()
                .listOfObjects(SyncEntity::class.java)
                .withQuery(Query.builder().uri(SyncsContract.URI)
                        .where("${SyncsContract.COL_PENDING}=?")
                        .whereArgs(0)
                        .build())
                .prepare()
                .asRxObservable()
    }

    override fun addGCMRegistration(registration: GCMRegistrationEntity): Observable<GCMRegistrationEntity?> {
        return getGCMRegistrationByToken(registration.token)
                .flatMap {
                    if (it == null) {
                        provider.put()
                                .`object`(registration.copyWithTime())
                                .prepare()
                                .asRxObservable()
                                .flatMap {
                                    val uri = if (it.wasInserted()) {
                                        it.insertedUri()!!
                                    } else if (it.wasUpdated()) {
                                        it.affectedUri()
                                    } else {
                                        throw StorIOException("GCMRegistration was not updated or inserted.")
                                    }
                                    provider.get().`object`(GCMRegistrationEntity::class.java)
                                            .withQuery(Query.builder().uri(uri).build())
                                            .prepare()
                                            .asRxObservable()
                                            .take(1)
                                }
                    } else {
                        Observable.just(it)
                    }
                }
    }

    override fun upsertMessage(message: GCMMessageEntity): Observable<GCMMessageEntity?> {
        return provider.put()
                .`object`(message.copyWithId())
                .prepare()
                .asRxObservable()
                .flatMap {
                    val uri = if (it.wasInserted()) {
                        it.insertedUri()!!
                    } else if (it.wasUpdated()) {
                        it.affectedUri()
                    } else {
                        throw StorIOException("GCMMessage was not updated or inserted.")
                    }
                    provider.get().`object`(GCMMessageEntity::class.java)
                            .withQuery(Query.builder().uri(uri).build())
                            .prepare()
                            .asRxObservable()
                            .take(1)
                }
    }

    override fun upsertSync(sync: SyncEntity): Observable<SyncEntity?> {
        return provider.put()
                .`object`(sync.copyWithId())
                .prepare()
                .asRxObservable()
                .flatMap {
                    val uri = if (it.wasInserted()) {
                        it.insertedUri()!!
                    } else if (it.wasUpdated()) {
                        it.affectedUri()
                    } else {
                        throw StorIOException("Sync was not updated or inserted.")
                    }
                    provider.get().`object`(SyncEntity::class.java)
                            .withQuery(Query.builder().uri(uri).build())
                            .prepare()
                            .asRxObservable()
                            .take(1)
                }
    }

    override fun upsertSyncs(syncs: List<SyncEntity>): Observable<List<SyncEntity>> {
        return provider.put()
                .objects(syncs.map { it.copyWithId() })
                .prepare()
                .asRxObservable()
                .map { syncs ->
                    syncs.results().keys.filter {
                        syncs.results()[it]!!.wasInserted() || syncs.results()[it]!!.wasUpdated()
                    }
                }
    }

    override fun notifyChange() {
        provider.internal().contentResolver().notifyChange(Uri.parse(SyncsContract.URI), null)
    }
}


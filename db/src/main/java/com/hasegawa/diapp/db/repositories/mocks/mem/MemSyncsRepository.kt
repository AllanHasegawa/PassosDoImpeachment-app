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

package com.hasegawa.diapp.db.repositories.mocks.mem

import com.hasegawa.diapp.db.repositories.copyWithId
import com.hasegawa.diapp.db.repositories.copyWithTime
import com.hasegawa.diapp.domain.entities.GCMMessageEntity
import com.hasegawa.diapp.domain.entities.GCMMessageType
import com.hasegawa.diapp.domain.entities.GCMRegistrationEntity
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import rx.Observable
import rx.lang.kotlin.BehaviorSubject
import rx.subjects.BehaviorSubject
import java.util.*

class MemSyncsRepository : SyncsRepository {

    private fun newRegSubject(): Observable<List<GCMRegistrationEntity>> {
        val s = BehaviorSubject(registrations.values.filterNotNull())
        registrationsSubjects.add(s)
        return s
    }

    private fun newMsgSubject(): Observable<List<GCMMessageEntity>> {
        val s = BehaviorSubject(messages.values.filterNotNull())
        messagesSubjects.add(s)
        return s
    }

    private fun newSyncSubject(): Observable<List<SyncEntity>> {
        val s = BehaviorSubject(syncs.values.filterNotNull())
        syncsSubjects.add(s)
        return s
    }

    override fun addGCMRegistration(registration: GCMRegistrationEntity): Observable<GCMRegistrationEntity?> {
        val hasIt = registrations[registration.token]
        if (hasIt != null) return Observable.just(hasIt)
        val newReg = registration.copyWithTime()
        registrations.put(newReg.token, newReg)
        return Observable.just(newReg)
    }

    override fun getGCMRegistrationByToken(token: String): Observable<GCMRegistrationEntity?> {
        return Observable.just(registrations[token])
    }

    override fun getGCMRegistrations(): Observable<List<GCMRegistrationEntity>> {
        return newRegSubject()
    }

    override fun getMessages(): Observable<List<GCMMessageEntity>> {
        return newMsgSubject()
    }

    override fun getMessagesByType(type: Int): Observable<List<GCMMessageEntity>> {
        return newMsgSubject().map { it.filter { it.type == GCMMessageType.fromValue(type) } }
    }

    override fun getPendingSyncs(): Observable<List<SyncEntity>> {
        return newSyncSubject().map { it.filter { it.pending } }
    }

    override fun getSuccessfulSyncs(): Observable<List<SyncEntity>> {
        return newSyncSubject().map { it.filter { !it.pending } }
    }

    override fun notifyChange() {
        messagesSubjects.forEach { it.onNext(messages.values.filterNotNull()) }
        registrationsSubjects.forEach { it.onNext(registrations.values.filterNotNull()) }
        syncsSubjects.forEach { it.onNext(syncs.values.toList()) }
    }

    override fun upsertMessage(message: GCMMessageEntity): Observable<GCMMessageEntity?> {
        val newMsg = message.copyWithId()
        messages.put(newMsg.id!!, newMsg)
        return Observable.just(newMsg)
    }

    override fun upsertSync(sync: SyncEntity): Observable<SyncEntity?> {
        val newSync = sync.copyWithId()
        syncs.put(newSync.id!!, newSync)
        return Observable.just(newSync)
    }

    override fun upsertSyncs(syncs: List<SyncEntity>): Observable<List<SyncEntity>> {
        val listOfInsertions = mutableListOf<Observable<SyncEntity?>>()
        syncs.forEach { listOfInsertions.add(upsertSync(it)) }
        return Observable.just(listOfInsertions.map { it.toBlocking().first()!! })
    }

    companion object {
        var registrations = HashMap<String, GCMRegistrationEntity>()
        var messages = HashMap<String, GCMMessageEntity>()
        var syncs = HashMap<String, SyncEntity>()

        private val registrationsSubjects = mutableListOf<BehaviorSubject<List<GCMRegistrationEntity>>>()
        private val messagesSubjects = mutableListOf<BehaviorSubject<List<GCMMessageEntity>>>()
        private val syncsSubjects = mutableListOf<BehaviorSubject<List<SyncEntity>>>()

        fun reset() {
            registrations.clear()
            messages.clear()
            syncs.clear()
            registrationsSubjects.clear()
            messagesSubjects.clear()
            syncsSubjects.clear()
        }
    }
}

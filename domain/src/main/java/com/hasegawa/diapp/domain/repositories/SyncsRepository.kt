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

package com.hasegawa.diapp.domain.repositories

import com.hasegawa.diapp.domain.entities.GCMMessageEntity
import com.hasegawa.diapp.domain.entities.GCMRegistrationEntity
import com.hasegawa.diapp.domain.entities.SyncEntity
import rx.Observable

interface SyncsRepository {
    fun getMessages(): Observable<List<GCMMessageEntity>>
    fun getMessagesByType(type: Int): Observable<List<GCMMessageEntity>>
    fun getPendingSyncs(): Observable<List<SyncEntity>>
    fun getSuccessfullySyncs(): Observable<List<SyncEntity>>
    fun getGCMRegistrations(): Observable<List<GCMRegistrationEntity>>
    fun getGCMRegistrationByToken(token: String): Observable<GCMRegistrationEntity?>

    fun upsertMessage(message: GCMMessageEntity): Observable<GCMMessageEntity?>
    fun upsertSyncs(syncs: List<SyncEntity>): Observable<List<SyncEntity>>
    fun upsertSync(sync: SyncEntity): Observable<SyncEntity?>
    fun addGCMRegistration(registration: GCMRegistrationEntity): Observable<GCMRegistrationEntity?>

    fun notifyChange()
}

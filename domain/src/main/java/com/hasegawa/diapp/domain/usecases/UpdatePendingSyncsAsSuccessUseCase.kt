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

package com.hasegawa.diapp.domain.usecases

import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import rx.Observable

class UpdatePendingSyncsAsSuccessUseCase(val syncsRepository: SyncsRepository,
                                         executionThread: ExecutionThread,
                                         postExecutionThread: PostExecutionThread) :
        UseCase<List<SyncEntity>>(executionThread, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<List<SyncEntity>> {
        return syncsRepository.getPendingSyncs()
                .take(1)
                .flatMap {
                    if (it.isEmpty()) {
                        syncsRepository.upsertSync(SyncEntity(null, false, null, null))
                                .map { listOf(it!!) }
                    } else {
                        Observable.just(it)
                    }
                }
                .map { it.map { it.copy(pending = false, timeSynced = null) } }
                .flatMap { syncsRepository.upsertSyncs(it) }
                .map { syncsRepository.notifyChange(); it }
    }
}

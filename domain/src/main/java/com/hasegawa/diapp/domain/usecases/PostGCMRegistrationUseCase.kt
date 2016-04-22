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

import com.hasegawa.diapp.domain.entities.GCMRegistrationEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.restservices.RestService
import rx.Observable
import rx.Scheduler

class PostGCMRegistrationUseCase(
        val gcmToken: String,
        val syncsRepository: SyncsRepository,
        val restService: RestService,
        executionThread: Scheduler,
        postExecutionThread: Scheduler) :
        UseCase<Boolean>(executionThread, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<Boolean> {
        return syncsRepository.getGCMRegistrationByToken(gcmToken)
                .take(1)
                .flatMap {
                    if (it == null) {
                        restService.postGCMToken(gcmToken)
                                .flatMap {
                                    syncsRepository
                                            .addGCMRegistration(
                                                    GCMRegistrationEntity(gcmToken, null)
                                            ).map { it != null }
                                }
                    } else {
                        Observable.just(false)
                    }
                }
    }
}

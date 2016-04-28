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
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.restservices.responses.StepResponse
import com.hasegawa.diapp.domain.restservices.responses.toEntity
import rx.Observable
import java.util.concurrent.TimeUnit


class AddStepResponsesToRepoUseCase(
        val stepResponses: List<StepResponse>,
        val stepsRepository: StepsRepository,
        executionThread: ExecutionThread, postExecutionThread: PostExecutionThread) :
        UseCase<List<StepEntity>>(executionThread, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<List<StepEntity>> {
        return Observable
                .just(stepResponses)
                .zipWith(stepsRepository.clearSteps(),
                        { a: List<StepResponse>, b: Int -> a })
                .map {
                    it.map { response ->
                        stepsRepository.addStep(response.toEntity(null))
                                .flatMap { entity ->
                                    stepsRepository.addStepLinks(
                                            response.links!!.map { it.toEntity(null, entity.id!!) }
                                    ).map { entity }
                                }
                    }
                }.flatMapIterable { it }
                .concatMap { it }
                .buffer(stepResponses.size)
                .timeout(10, TimeUnit.SECONDS)
                .map { stepsRepository.notifyChange(); it }
    }
}

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

import com.hasegawa.diapp.domain.repositories.StepsRepository
import rx.Observable
import rx.Scheduler

data class NumCompletedAndTotal(val completed: Int, val total: Int)

class GetNumStepsTotalCompletedUseCase(val stepsRepository: StepsRepository,
                                       executionThread: Scheduler, postExecutionThread: Scheduler) :
        UseCase<NumCompletedAndTotal>(executionThread, postExecutionThread) {

    override fun buildUseCaseObservable(): Observable<NumCompletedAndTotal> {
        return stepsRepository.getNumberOfSteps()
                .zipWith(stepsRepository.getNumberOfCompletedSteps(), { numSteps, numCompleted ->
                    NumCompletedAndTotal(numCompleted, numSteps)
                })
    }
}


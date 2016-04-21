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

package com.hasegawa.diapp.domain

import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetNumStepsTotalCompletedUseCase
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

class GetNumStepsTotalCompletedUseCaseTest {
    @Mock
    var stepsRepository: StepsRepository? = null

    init {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun execute() {
        val expected = NumCompletedAndTotal(1, 42)

        `when`(stepsRepository!!.getNumberOfSteps())
                .thenReturn(Observable.just(expected.total))
        `when`(stepsRepository!!.getNumberOfCompletedSteps())
                .thenReturn(Observable.just(expected.completed))


        val useCase = GetNumStepsTotalCompletedUseCase(stepsRepository!!,
                Schedulers.io(), Schedulers.newThread())

        var result: NumCompletedAndTotal? = null
        val barrier = CyclicBarrier(2)
        useCase.execute(object : Subscriber<NumCompletedAndTotal>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
            }

            override fun onNext(t: NumCompletedAndTotal?) {
                result = t
                barrier.await()
            }
        })

        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(expected))

        verify(stepsRepository!!).getNumberOfCompletedSteps()
        verify(stepsRepository!!).getNumberOfSteps()
        verifyNoMoreInteractions(stepsRepository!!)
    }
}

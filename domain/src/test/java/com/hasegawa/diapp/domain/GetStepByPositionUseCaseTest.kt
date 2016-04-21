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

import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetStepByPositionUseCase
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

class GetStepByPositionUseCaseTest {
    @Mock
    var stepsRepository: StepsRepository? = null

    val steps = mapOf(
            Pair(1, StepEntity("1", 1, "titleA", "descA", true, "possA")),
            Pair(2, StepEntity("2", 2, "titleB", "descB", false, "possB"))
    )

    init {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun execute() {
        `when`(stepsRepository!!.getStepByPosition(1))
                .thenReturn(Observable.just(steps[1]))
        `when`(stepsRepository!!.getStepByPosition(2))
                .thenReturn(Observable.just(steps[2]))
        `when`(stepsRepository!!.getStepByPosition(3))
                .thenReturn(Observable.just(null))

        val useCase = GetStepByPositionUseCase(1, stepsRepository!!,
                Schedulers.io(), Schedulers.newThread())

        var result: StepEntity? = null
        var completed = false
        val barrier = CyclicBarrier(2)
        useCase.execute(object : Subscriber<StepEntity>() {
            override fun onCompleted() {
                completed = true
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: StepEntity?) {
                result = t
                barrier.await()
            }
        })

        barrier.await(10, TimeUnit.SECONDS)
        assertThat(completed, `is`(false))
        assertThat(result, `is`(steps[1]))

        barrier.reset()
        useCase.position = 2
        barrier.await(10, TimeUnit.SECONDS)
        assertThat(completed, `is`(false))
        assertThat(result, `is`(steps[2]))

        barrier.reset()
        useCase.position = 3
        barrier.await(10, TimeUnit.SECONDS)
        useCase.unsubscribe()
        assertThat(completed, `is`(false))
        assertThat(result, nullValue())

        verify(stepsRepository!!).getStepByPosition(1)
        verify(stepsRepository!!).getStepByPosition(2)
        verify(stepsRepository!!).getStepByPosition(3)
        verifyNoMoreInteractions(stepsRepository!!)
    }
}

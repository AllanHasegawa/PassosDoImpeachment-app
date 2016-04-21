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
import com.hasegawa.diapp.domain.usecases.GetNumStepsCompletedUseCase
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import rx.Observable
import rx.Subscriber
import rx.lang.kotlin.BehaviorSubject
import rx.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

class GetNumStepsCompletedUseCaseTest {
    @Mock
    var stepsRepository: StepsRepository? = null

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val subject = BehaviorSubject(42)
        `when`(stepsRepository!!.getNumberOfCompletedSteps()).thenReturn(subject)
        `when`(stepsRepository!!.notifyChange()).then { subject.onNext(0) }
    }

    @Test
    fun execute() {
        val useCase = GetNumStepsCompletedUseCase(stepsRepository!!, Schedulers.io(),
                Schedulers.newThread())

        var completed = false
        var numCompleted = -1
        var calls = 0
        val barrier = CyclicBarrier(2)
        useCase.execute(object : Subscriber<Int>() {
            override fun onCompleted() {
                completed = true
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: Int?) {
                calls++
                numCompleted = t ?: -1
                barrier.await()
            }
        })

        barrier.await(10, TimeUnit.SECONDS)
        assertThat(numCompleted, `is`(42))

        barrier.reset()
        stepsRepository!!.notifyChange()
        barrier.await(10, TimeUnit.SECONDS)
        assertThat(numCompleted, `is`(0))

        useCase.unsubscribe()
        assertThat(completed, `is`(false))
        assertThat(calls, `is`(2))

        verify(stepsRepository!!).getNumberOfCompletedSteps()
        verify(stepsRepository!!).notifyChange()
        verifyNoMoreInteractions(stepsRepository!!)
    }
}

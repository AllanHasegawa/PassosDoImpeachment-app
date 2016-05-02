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
import com.hasegawa.diapp.domain.usecases.GetStepsUseCase
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import rx.Subscriber
import rx.lang.kotlin.BehaviorSubject
import rx.schedulers.Schedulers
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

class GetStepsUseCaseTest {
    val et = ExecutionThread(Schedulers.io())
    val pet = PostExecutionThread(Schedulers.newThread())

    @Mock
    var stepsRepository: StepsRepository? = null

    val stepsList = listOf(
            StepEntity("A", 1, "titleA", "descA", true, "possA"),
            StepEntity("B", 3, "titleA", "descA", false, "possB"),
            StepEntity("C", 2, "titleA", "descA", false, "possC"),
            StepEntity("D", 4, "titleA", "descA", false, "possD")
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val subject = BehaviorSubject(stepsList)
        `when`(stepsRepository!!.getSteps()).thenReturn(subject)
        `when`(stepsRepository!!.notifyChange()).then { subject.onNext(emptyList()) }
    }

    @Test
    fun execute() {
        val useCase = GetStepsUseCase(stepsRepository!!, et, pet)

        var completed = false
        var result: List<StepEntity>? = null
        var calls = 0
        val barrier = CyclicBarrier(2)
        useCase.execute(object : Subscriber<List<StepEntity>>() {
            override fun onCompleted() {
                completed = true
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: List<StepEntity>?) {
                calls++
                result = t
                barrier.await()
            }
        })

        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(stepsList.sortedBy { it.position }))

        barrier.reset()
        stepsRepository!!.notifyChange()
        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(emptyList()))

        useCase.unsubscribe()
        assertThat(completed, `is`(false))
        assertThat(calls, `is`(2))

        verify(stepsRepository!!).getSteps()
        verify(stepsRepository!!).notifyChange()
        verifyNoMoreInteractions(stepsRepository!!)
    }
}

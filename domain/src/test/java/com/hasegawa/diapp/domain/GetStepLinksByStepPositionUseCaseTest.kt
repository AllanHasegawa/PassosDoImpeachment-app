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
import com.hasegawa.diapp.domain.entities.StepLinkEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetStepLinksByStepPositionUseCase
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
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

class GetStepLinksByStepPositionUseCaseTest {
    val et = ExecutionThread(Schedulers.io())
    val pet = PostExecutionThread(Schedulers.newThread())

    @Mock
    var stepsRepository: StepsRepository? = null

    val step = StepEntity("stepA", 1, "titleA", "descA", true, "possA")

    val stepLinks = listOf(
            StepLinkEntity("A", step.id!!, "titleA", "urlA"),
            StepLinkEntity("B", step.id!!, "titleB", "urlB"),
            StepLinkEntity("C", step.id!!, "titleC", "urlC")
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val subjectLinks = BehaviorSubject(stepLinks)
        `when`(stepsRepository!!.getStepLinksByStepPosition(step.position))
                .thenReturn(subjectLinks)
        `when`(stepsRepository!!.notifyChange()).then {
            subjectLinks.onNext(emptyList())
        }
    }

    @Test
    fun execute() {
        val useCase = GetStepLinksByStepPositionUseCase(step.position, stepsRepository!!, et, pet)

        var completed = false
        var result: List<StepLinkEntity>? = null
        var calls = 0
        val barrier = CyclicBarrier(2)
        useCase.execute(object : Subscriber<List<StepLinkEntity>>() {
            override fun onCompleted() {
                completed = true
            }

            override fun onError(e: Throwable?) {
                throw e!!
            }

            override fun onNext(t: List<StepLinkEntity>?) {
                calls++
                result = t
                barrier.await()
            }
        })

        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(stepLinks))

        barrier.reset()
        stepsRepository!!.notifyChange()
        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(emptyList()))

        useCase.unsubscribe()
        assertThat(completed, `is`(false))
        assertThat(calls, `is`(2))

        verify(stepsRepository!!).getStepLinksByStepPosition(step.position)
        verify(stepsRepository!!).notifyChange()
        verifyNoMoreInteractions(stepsRepository!!)
    }
}

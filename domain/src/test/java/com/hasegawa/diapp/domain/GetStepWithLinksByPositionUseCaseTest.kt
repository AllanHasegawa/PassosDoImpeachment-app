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
import com.hasegawa.diapp.domain.entities.StepWithLinksEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetStepWithLinksByPositionUseCase
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

class GetStepWithLinksByPositionUseCaseTest {
    val et = ExecutionThread(Schedulers.io())
    val pet = PostExecutionThread(Schedulers.newThread())

    @Mock
    var stepsRepository: StepsRepository? = null

    val steps = mapOf(
            Pair(1, StepEntity("1", 1, "titleA", "descA", true, "possA")),
            Pair(2, StepEntity("2", 2, "titleB", "descB", false, "possB"))
    )

    val links = mapOf(
            Pair(1, StepLinkEntity("1", "1", "linkA", "urlA"))
    )

    init {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun execute() {

        val expected1 = StepWithLinksEntity(steps[1], listOf(links[1]!!))
        val expected2 = StepWithLinksEntity(steps[2], emptyList())
        val expected3 = StepWithLinksEntity(null, emptyList())

        `when`(stepsRepository!!.getStepByPosition(1))
                .thenReturn(Observable.just(steps[1]))
        `when`(stepsRepository!!.getStepByPosition(2))
                .thenReturn(Observable.just(steps[2]))
        `when`(stepsRepository!!.getStepByPosition(3))
                .thenReturn(Observable.just(null))
        `when`(stepsRepository!!.getStepLinksByStepPosition(anyInt()))
                .thenReturn(Observable.just(emptyList()))
        `when`(stepsRepository!!.getStepLinksByStepPosition(1))
                .thenReturn(Observable.just(listOf(links[1]!!)))

        val useCase = GetStepWithLinksByPositionUseCase(1, stepsRepository!!, et, pet)

        var result: StepWithLinksEntity? = null
        val barrier = CyclicBarrier(2)
        useCase.execute(object : Subscriber<StepWithLinksEntity>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: StepWithLinksEntity?) {
                result = t
                barrier.await()
            }
        })

        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(expected1))

        barrier.reset()
        useCase.position = 2
        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(expected2))

        barrier.reset()
        useCase.position = 3
        barrier.await(10, TimeUnit.SECONDS)
        useCase.unsubscribe()
        assertThat(result, `is`(expected3))

        verify(stepsRepository!!).getStepByPosition(1)
        verify(stepsRepository!!).getStepByPosition(2)
        verify(stepsRepository!!).getStepByPosition(3)
        verify(stepsRepository!!).getStepLinksByStepPosition(1)
        verify(stepsRepository!!).getStepLinksByStepPosition(2)
        verify(stepsRepository!!).getStepLinksByStepPosition(3)
        verifyNoMoreInteractions(stepsRepository!!)
    }
}

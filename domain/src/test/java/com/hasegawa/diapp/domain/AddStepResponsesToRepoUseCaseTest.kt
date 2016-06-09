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
import com.hasegawa.diapp.domain.restservices.responses.StepLinkResponse
import com.hasegawa.diapp.domain.restservices.responses.StepResponse
import com.hasegawa.diapp.domain.restservices.responses.toEntity
import com.hasegawa.diapp.domain.usecases.AddStepResponsesToRepoUseCase
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

class AddStepResponsesToRepoUseCaseTest {
    val et = ExecutionThread(Schedulers.io())
    val pet = PostExecutionThread(Schedulers.newThread())


    @Mock
    var stepsRepository: StepsRepository? = null


    val responses = listOf(
            StepResponse("titleA", "descA", "possA", 1, true, listOf(
                    StepLinkResponse("linkA", "urlA"),
                    StepLinkResponse("linkB", "urlB")
            )),
            StepResponse("titleB", "descB", "possB", 2, false, emptyList()
            ))

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun onClearRepo() {
        `when`(stepsRepository!!.clearStepLinks()).thenReturn(Observable.just(0))
        `when`(stepsRepository!!.clearSteps()).thenReturn(Observable.just(0))
        `when`(stepsRepository!!.addStep(responses[0].toEntity(null)))
                .thenReturn(Observable.just(responses[0].toEntity("id0")))
        `when`(stepsRepository!!.addStep(responses[1].toEntity(null)))
                .thenReturn(Observable.just(responses[1].toEntity("id1")))

        `when`(stepsRepository!!.addStepLinks(responses[0].links!!.map { it.toEntity(null, "id0") }))
                .thenReturn(Observable.just(responses[0].links!!.mapIndexed {
                    i, link ->
                    link.toEntity("id$i", "id0")
                }))
        `when`(stepsRepository!!.addStepLinks(emptyList()))
                .thenReturn(Observable.just(emptyList()))

        val useCase = AddStepResponsesToRepoUseCase(responses, stepsRepository!!, et, pet)

        var completed = false
        var results: List<StepEntity>? = null
        val barrier = CyclicBarrier(2)
        useCase.execute(object : Subscriber<List<StepEntity>>() {
            override fun onCompleted() {
                completed = true
                barrier.await()
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: List<StepEntity>?) {
                assertThat(t, notNullValue())
                results = t
            }
        })

        barrier.await(10, TimeUnit.SECONDS)
        assertThat(results, `is`(responses.mapIndexed { i, response ->
            response.toEntity("id$i")
        }))
        assertThat(completed, `is`(true))

        verify(stepsRepository!!).clearSteps()
        verify(stepsRepository!!).clearStepLinks()
        verify(stepsRepository!!, times(2)).addStep(anyObject())
        verify(stepsRepository!!).addStepLinks(responses[0].links!!.map { it.toEntity(null, "id0") })
        verify(stepsRepository!!).addStepLinks(emptyList())
        verify(stepsRepository!!).notifyChange()
        verifyNoMoreInteractions(stepsRepository!!)
    }
}

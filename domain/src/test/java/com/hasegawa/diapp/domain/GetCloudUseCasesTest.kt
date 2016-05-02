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

import com.hasegawa.diapp.domain.restservices.RestService
import com.hasegawa.diapp.domain.restservices.responses.NewsResponse
import com.hasegawa.diapp.domain.restservices.responses.StepLinkResponse
import com.hasegawa.diapp.domain.restservices.responses.StepResponse
import com.hasegawa.diapp.domain.usecases.GetCloudNewsUseCase
import com.hasegawa.diapp.domain.usecases.GetCloudStepsUseCase
import org.hamcrest.Matchers.*
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class GetCloudUseCasesTest {
    val et = ExecutionThread(Schedulers.io())
    val pet = PostExecutionThread(Schedulers.newThread())

    @Mock
    var restService: RestService? = null

    val stepResponses = listOf(
            StepResponse("titleA", "descA", "possA", 1, true, listOf(
                    StepLinkResponse("linkA", "urlA"),
                    StepLinkResponse("linkB", "urlB")
            )),
            StepResponse("titleB", "descB", "possB", 2, false, emptyList()
            ))

    val newsResponses = listOf(
            NewsResponse("titleA", "urlA", "tldrA", 0),
            NewsResponse("titleB", "urlB", "tldrB", 1),
            NewsResponse("titleC", "urlC", "tldrC", 2)
    )

    init {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun getSteps() {
        `when`(restService!!.getSteps()).thenReturn(Observable.just(stepResponses))

        val useCase = GetCloudStepsUseCase(restService!!, et, pet)
        var result: List<StepResponse>? = null
        val lock = CountDownLatch(1)
        useCase.execute(object : Subscriber<List<StepResponse>>() {
            override fun onCompleted() {
                lock.countDown()
            }

            override fun onError(e: Throwable?) {
                throw e!!
            }

            override fun onNext(t: List<StepResponse>?) {
                result = t
            }
        })
        lock.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(stepResponses))

        verify(restService!!).getSteps()
        verifyNoMoreInteractions(restService!!)
    }

    @Test
    fun getNews() {
        `when`(restService!!.getNews()).thenReturn(Observable.just(newsResponses))

        val useCase = GetCloudNewsUseCase(restService!!, et, pet)
        var result: List<NewsResponse>? = null
        val lock = CountDownLatch(1)
        useCase.execute(object : Subscriber<List<NewsResponse>>() {
            override fun onCompleted() {
                lock.countDown()
            }

            override fun onError(e: Throwable?) {
                throw e!!
            }

            override fun onNext(t: List<NewsResponse>?) {
                result = t
            }
        })
        lock.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(newsResponses))

        verify(restService!!).getNews()
        verifyNoMoreInteractions(restService!!)
    }
}


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

import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.domain.restservices.responses.NewsResponse
import com.hasegawa.diapp.domain.restservices.responses.toEntity
import com.hasegawa.diapp.domain.usecases.AddNewsResponsesToRepoUseCase
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

class AddNewsResponsesToRepoUseCaseTest {

    val et = ExecutionThread(Schedulers.io())
    val pet = PostExecutionThread(Schedulers.newThread())

    @Mock
    var newsRepository: NewsRepository? = null


    val responses = listOf(
            NewsResponse("titleA", "urlA", "tldrA", 0),
            NewsResponse("titleB", "urlB", "tldrB", 1),
            NewsResponse("titleC", "urlC", "tldrC", 2)
    )

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun onClearRepo() {
        `when`(newsRepository!!.clearNews()).thenReturn(Observable.just(0))
        `when`(newsRepository!!.addAllNews(responses.map { it.toEntity(null) }))
                .thenReturn(Observable.just(responses.mapIndexed { i, it -> it.toEntity("id$i") }))

        val useCase = AddNewsResponsesToRepoUseCase(responses, newsRepository!!, et, pet)

        var completed = false
        var results: List<NewsEntity>? = null
        val barrier = CyclicBarrier(2)
        useCase.execute(object : Subscriber<List<NewsEntity>>() {
            override fun onCompleted() {
                completed = true
                barrier.await()
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: List<NewsEntity>?) {
                assertThat(t, notNullValue())
                results = t
            }
        })

        barrier.await(10, TimeUnit.SECONDS)
        assertThat(results, `is`(responses.mapIndexed { i, response ->
            response.toEntity("id$i")
        }))
        assertThat(completed, `is`(true))

        verify(newsRepository!!).clearNews()
        verify(newsRepository!!).addAllNews(responses.map { it.toEntity(null) })
        verify(newsRepository!!).notifyChange()
        verifyNoMoreInteractions(newsRepository!!)
    }
}

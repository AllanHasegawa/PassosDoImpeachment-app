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
import com.hasegawa.diapp.domain.usecases.GetNewsUseCase
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
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

class GetNewsUseCaseTest {

    @Mock
    var newsRepository: NewsRepository? = null

    val newsList = listOf(
            NewsEntity("A", "NewsA", "UrlA", 0, "tldrA"),
            NewsEntity("B", "NewsB", "UrlA", 1, null),
            NewsEntity("C", "NewsC", "UrlA", 10, "tldrC"),
            NewsEntity("D", "NewsD", "UrlA", 2, "tldrD")
    )

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        val subject = BehaviorSubject(newsList)
        `when`(newsRepository!!.getNews()).thenReturn(subject)
        `when`(newsRepository!!.notifyChange()).then { subject.onNext(emptyList()) }
    }

    @Test
    fun execute() {
        val useCase = GetNewsUseCase(newsRepository!!, Schedulers.io(), Schedulers.newThread())

        val barrier = CyclicBarrier(2)
        var completed = false
        var result: List<NewsEntity>? = null
        var calls = 0
        useCase.execute(object : Subscriber<List<NewsEntity>>() {
            override fun onCompleted() {
                completed = true
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: List<NewsEntity>?) {
                calls++
                result = t
                barrier.await()
            }
        })
        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(newsList.sortedByDescending { it.date }))

        barrier.reset()
        newsRepository!!.notifyChange()
        barrier.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(emptyList()))


        useCase.unsubscribe()
        assertThat(completed, `is`(false))
        assertThat(calls, `is`(2))

        verify(newsRepository)!!.getNews()
        verify(newsRepository)!!.notifyChange()
        verifyNoMoreInteractions(newsRepository)
    }
}


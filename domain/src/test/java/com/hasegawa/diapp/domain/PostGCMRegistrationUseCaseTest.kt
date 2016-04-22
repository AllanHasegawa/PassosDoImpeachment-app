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

import com.hasegawa.diapp.domain.entities.GCMRegistrationEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.restservices.RestService
import com.hasegawa.diapp.domain.usecases.PostGCMRegistrationUseCase
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PostGCMRegistrationUseCaseTest {
    @Mock
    var syncsRepository: SyncsRepository? = null
    @Mock
    var restService: RestService? = null

    val token = "token :)"
    val gcmRegistration = GCMRegistrationEntity(token, null)

    private fun <T> anyObject(): T {
        return Mockito.anyObject<T>()
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun newToken() {
        `when`(syncsRepository!!.getGCMRegistrationByToken(token))
                .thenReturn(Observable.just(null))
                .thenReturn(Observable.just(gcmRegistration))
        `when`(syncsRepository!!.addGCMRegistration(anyObject<GCMRegistrationEntity>()))
                .thenReturn(Observable.just(gcmRegistration))
        `when`(restService!!.postGCMToken(token)).thenReturn(Observable.just(true))

        val useCase = PostGCMRegistrationUseCase(token, syncsRepository!!, restService!!,
                Schedulers.io(), Schedulers.newThread())

        var completed = false
        var calls = 0
        val lock = CountDownLatch(1)
        var result: Boolean? = null
        useCase.execute(object : Subscriber<Boolean>() {
            override fun onCompleted() {
                completed = true
                lock.countDown()
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: Boolean?) {
                calls++
                result = t
            }
        })

        lock.await(10, TimeUnit.SECONDS)
        useCase.unsubscribe()

        assertThat(completed, `is`(true))
        assertThat(calls, `is`(1))
        assertThat(result, `is`(true))

        verify(syncsRepository!!).getGCMRegistrationByToken(token)
        verify(syncsRepository!!).addGCMRegistration(anyObject())
        verify(restService!!).postGCMToken(token)
        verifyNoMoreInteractions(restService!!)
        verifyNoMoreInteractions(syncsRepository!!)
    }

    @Test
    fun tokenAlreadyExists() {
        `when`(syncsRepository!!.getGCMRegistrationByToken(token))
                .thenReturn(Observable.just(gcmRegistration))

        val useCase = PostGCMRegistrationUseCase(token, syncsRepository!!, restService!!,
                Schedulers.io(), Schedulers.newThread())

        var completed = false
        var calls = 0
        var result: Boolean? = null
        val lock = CountDownLatch(1)
        useCase.execute(object : Subscriber<Boolean>() {
            override fun onCompleted() {
                completed = true
                lock.countDown()
            }

            override fun onError(e: Throwable?) {
                throw UnsupportedOperationException()
            }

            override fun onNext(t: Boolean?) {
                calls++
                result = t
            }
        })

        lock.await(10, TimeUnit.SECONDS)
        useCase.unsubscribe()

        assertThat(completed, `is`(true))
        assertThat(calls, `is`(1))
        assertThat(result, `is`(false))

        verify(syncsRepository!!).getGCMRegistrationByToken(token)
        verifyNoMoreInteractions(syncsRepository!!)
        verifyNoMoreInteractions(restService!!)
    }

    @Test
    fun postRestFails() {
        val postErrorMsg = "Some error with rest service :("
        `when`(syncsRepository!!.getGCMRegistrationByToken(token))
                .thenReturn(Observable.just(null))
                .thenReturn(Observable.just(gcmRegistration))
        `when`(syncsRepository!!.addGCMRegistration(anyObject<GCMRegistrationEntity>()))
                .thenReturn(Observable.just(gcmRegistration))
        `when`(restService!!.postGCMToken(token)).thenReturn(Observable.error(
                RuntimeException(postErrorMsg)
        ))


        val useCase = PostGCMRegistrationUseCase(token, syncsRepository!!, restService!!,
                Schedulers.io(), Schedulers.newThread())

        var completed = false
        var foundError = false
        var calls = 0
        var result: Boolean? = null
        val lock = CountDownLatch(1)
        useCase.execute(object : Subscriber<Boolean>() {
            override fun onCompleted() {
                completed = true
            }

            override fun onError(e: Throwable?) {
                assertThat(e!!.message, `is`(postErrorMsg))
                foundError = true
                lock.countDown()
            }

            override fun onNext(t: Boolean?) {
                calls++
                result = t
            }
        })

        lock.await(10, TimeUnit.SECONDS)
        useCase.unsubscribe()

        assertThat(completed, `is`(false))
        assertThat(foundError, `is`(true))
        assertThat(calls, `is`(0))
        assertThat(result, nullValue())

        verify(syncsRepository!!).getGCMRegistrationByToken(token)
        verify(syncsRepository!!, never()).addGCMRegistration(anyObject())
        verify(restService!!).postGCMToken(token)
        verifyNoMoreInteractions(restService!!)
        verifyNoMoreInteractions(syncsRepository!!)
    }
}
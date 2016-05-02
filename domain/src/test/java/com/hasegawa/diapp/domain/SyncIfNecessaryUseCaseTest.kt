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

import com.hasegawa.diapp.domain.devices.SyncScheduler
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.usecases.SyncIfNecessaryUseCase
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import rx.Observable
import rx.Subscriber
import rx.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SyncIfNecessaryUseCaseTest {
    val et = ExecutionThread(Schedulers.io())
    val pet = PostExecutionThread(Schedulers.newThread())

    @Mock
    var syncsRepository: SyncsRepository? = null

    @Mock
    var syncScheduler: SyncScheduler? = null

    val pendingSyncs = listOf(
            SyncEntity("A", true, 10, 10),
            SyncEntity("B", true, 20, 20)
    )

    val successSyncs = listOf(
            SyncEntity("A", false, 11, 10),
            SyncEntity("B", false, 22, 20)
    )

    init {
        MockitoAnnotations.initMocks(this)
    }

    @Test
    fun syncWhenPending() {
        `when`(syncsRepository!!.getPendingSyncs()).thenReturn(
                Observable.just(pendingSyncs)
        )

        val useCase = SyncIfNecessaryUseCase(syncScheduler!!, syncsRepository!!, et, pet)

        var result: Boolean? = null
        val lock = CountDownLatch(1)
        useCase.execute(object : Subscriber<Boolean>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
            }

            override fun onNext(t: Boolean?) {
                result = t
                lock.countDown()
            }
        })

        lock.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(true))

        verify(syncScheduler!!).enqueueSync(true)
        verifyNoMoreInteractions(syncScheduler!!)
        verify(syncsRepository!!).getPendingSyncs()
        verifyNoMoreInteractions(syncsRepository!!)
    }

    @Test
    fun syncWhenNotPendingButSuccess() {
        `when`(syncsRepository!!.getPendingSyncs()).thenReturn(Observable.just(emptyList()))
        `when`(syncsRepository!!.getSuccessfulSyncs()).thenReturn(Observable.just(successSyncs))

        val useCase = SyncIfNecessaryUseCase(syncScheduler!!, syncsRepository!!, et, pet)

        var result: Boolean? = null
        val lock = CountDownLatch(1)
        useCase.execute(object : Subscriber<Boolean>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
            }

            override fun onNext(t: Boolean?) {
                result = t
                lock.countDown()
            }
        })

        lock.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(false))

        verifyNoMoreInteractions(syncScheduler!!)
        verify(syncsRepository!!).getPendingSyncs()
        verify(syncsRepository!!).getSuccessfulSyncs()
        verifyNoMoreInteractions(syncsRepository!!)
    }

    @Test
    fun syncWhenNotPendingAndNoSuccess() {
        `when`(syncsRepository!!.getPendingSyncs()).thenReturn(Observable.just(emptyList()))
        `when`(syncsRepository!!.getSuccessfulSyncs()).thenReturn(Observable.just(emptyList()))

        val useCase = SyncIfNecessaryUseCase(syncScheduler!!, syncsRepository!!, et, pet)

        var result: Boolean? = null
        val lock = CountDownLatch(1)
        useCase.execute(object : Subscriber<Boolean>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
            }

            override fun onNext(t: Boolean?) {
                result = t
                lock.countDown()
            }
        })

        lock.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(true))

        verify(syncScheduler!!).enqueueSync(false)
        verifyNoMoreInteractions(syncScheduler!!)
        verify(syncsRepository!!).getPendingSyncs()
        verify(syncsRepository!!).getSuccessfulSyncs()
        verifyNoMoreInteractions(syncsRepository!!)
    }
}

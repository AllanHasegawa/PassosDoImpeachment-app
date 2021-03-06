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

import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.usecases.GetLastSuccessfulSyncUseCase
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

class GetLastSuccessfulSyncUseCaseTest {
    val et = ExecutionThread(Schedulers.io())
    val pet = PostExecutionThread(Schedulers.newThread())

    @Mock
    var syncsRepository: SyncsRepository? = null

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
    fun execute() {
        `when`(syncsRepository!!.getSuccessfulSyncs()).thenReturn(
                Observable.just(successSyncs)
        )

        val useCase = GetLastSuccessfulSyncUseCase(syncsRepository!!, et, pet)

        var result: SyncEntity? = null
        val lock = CountDownLatch(1)
        useCase.execute(object : Subscriber<SyncEntity?>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
            }

            override fun onNext(t: SyncEntity?) {
                result = t
                lock.countDown()
            }
        })

        lock.await(10, TimeUnit.SECONDS)
        assertThat(result, `is`(successSyncs.takeLast(1)[0]))

        verify(syncsRepository!!).getSuccessfulSyncs()
        verifyNoMoreInteractions(syncsRepository!!)
    }
}

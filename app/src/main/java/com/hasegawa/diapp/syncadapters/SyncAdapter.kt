/*******************************************************************************
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
 ******************************************************************************/
package com.hasegawa.diapp.syncadapters

import android.accounts.Account
import android.content.*
import android.os.Bundle
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.devices.LogDevice
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.restservices.RestService
import com.hasegawa.diapp.domain.restservices.responses.NewsResponse
import com.hasegawa.diapp.domain.restservices.responses.StepResponse
import com.hasegawa.diapp.domain.usecases.*
import rx.Subscriber
import javax.inject.Inject

class SyncAdapter : AbstractThreadedSyncAdapter {
    private lateinit var contentResolver: ContentResolver


    @Inject lateinit var restService: RestService
    @Inject lateinit var stepsRepository: StepsRepository
    @Inject lateinit var newsRepository: NewsRepository
    @Inject lateinit var syncsRepository: SyncsRepository
    @Inject lateinit var executionThread: ExecutionThread
    @Inject lateinit var postExecutionThread: PostExecutionThread
    @Inject lateinit var logDevice: LogDevice

    constructor(ctx: Context, autoInitialize: Boolean) : super(ctx, autoInitialize) {
        contentResolver = context.contentResolver
    }

    constructor(ctx: Context, autoInitialize: Boolean, allowParallelSyncs: Boolean)
    : super(ctx, autoInitialize, allowParallelSyncs) {
        contentResolver = context.contentResolver
    }

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?,
                               provider: ContentProviderClient?, syncResult: SyncResult?) {
        DiApp.appComponent.inject(this)

        logDevice.d("Initiating sync!")

        syncSteps(syncResult)
        syncNews(syncResult)

        if (!syncResult!!.hasError()) {
            UpdatePendingSyncsAsSuccessUseCase(syncsRepository, executionThread, postExecutionThread)
                    .execute(object : Subscriber<List<SyncEntity>>() {
                        override fun onCompleted() {
                        }

                        override fun onError(e: Throwable?) {
                            logDevice.d(e, "Error updating pending syncs.")
                        }

                        override fun onNext(t: List<SyncEntity>?) {
                        }
                    })
        }
    }


    private fun syncNews(syncResult: SyncResult?) {
        val getNewsUc = GetCloudNewsUseCase(restService, executionThread, postExecutionThread)
        getNewsUc.executeBlocking(object : Subscriber<List<NewsResponse>>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.d(e, "Network Response error: ${e!!.message}")
                syncResult!!.delayUntil = DELAY_RETRY
                syncResult.stats.numIoExceptions++
            }

            override fun onNext(t: List<NewsResponse>?) {
                if (t == null) {
                    logDevice.d("Response error")
                    syncResult!!.stats.numIoExceptions++
                    syncResult.delayUntil = DELAY_RETRY
                }
                try {
                    saveNewsResponseList(t!!)
                } catch (e: Exception) {
                    syncResult!!.databaseError = true
                    logDevice.d(e, "Database error: ${e.message}")
                }
            }
        })
    }

    private fun syncSteps(syncResult: SyncResult?) {
        val getStepsUc = GetCloudStepsUseCase(restService, executionThread, postExecutionThread)
        getStepsUc.executeBlocking(object : Subscriber<List<StepResponse>>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.d(e, "Network Response error: ${e!!.message}")
                syncResult!!.delayUntil = DELAY_RETRY
                syncResult.stats.numIoExceptions++
            }

            override fun onNext(t: List<StepResponse>?) {
                if (t == null) {
                    logDevice.d("Response error")
                    syncResult!!.stats.numIoExceptions++
                    syncResult.delayUntil = DELAY_RETRY
                }
                try {
                    saveStepsResponsesList(t!!)
                } catch (e: Exception) {
                    syncResult!!.databaseError = true
                    logDevice.d(e, "Database error: ${e.message}")
                }
            }
        })
    }

    private fun saveNewsResponseList(newsResponses: List<NewsResponse>) {
        try {
            val useCase = AddNewsResponsesToRepoUseCase(newsResponses,
                    newsRepository, executionThread, postExecutionThread)
            useCase.executeBlocking(object : Subscriber<List<NewsEntity>>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable?) {
                    logDevice.d(e, "Error trying to add news responses.")
                    throw e!!
                }

                override fun onNext(t: List<NewsEntity>?) {
                }
            })
            logDevice.d("Saved list of news successfully.")
        } catch (e: Exception) {
            logDevice.d(e, "Error while saving list of important news.")
            throw e
        }
    }

    private fun saveStepsResponsesList(stepResponsesList: List<StepResponse>) {
        try {
            val stepsToRepoUc = AddStepResponsesToRepoUseCase(stepResponsesList,
                    stepsRepository, executionThread, postExecutionThread)
            stepsToRepoUc.executeBlocking(object : Subscriber<List<StepEntity>>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable?) {
                }

                override fun onNext(t: List<StepEntity>?) {
                }
            })
            logDevice.d("List of steps saved successfully.")
        } catch (e: Exception) {
            logDevice.d(e, "Error while saving list of steps.")
            throw e
        }
    }

    companion object {
        private const val DELAY_RETRY = 30L
    }
}

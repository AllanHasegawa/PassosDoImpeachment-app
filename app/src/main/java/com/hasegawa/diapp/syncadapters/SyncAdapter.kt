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
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SyncResult
import android.net.Uri
import android.os.Bundle
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.activities.BaseNavDrawerActivity
import com.hasegawa.diapp.models.DiContract
import com.hasegawa.diapp.models.DiContract.ImportantNewsContract
import com.hasegawa.diapp.models.DiContract.LinksContract
import com.hasegawa.diapp.models.DiContract.StepsContract
import com.hasegawa.diapp.models.ImportantNews
import com.hasegawa.diapp.models.Step
import com.hasegawa.diapp.models.StepLink
import com.hasegawa.diapp.models.toContentValues
import com.hasegawa.diapp.restservices.ImportantNewsRest
import com.hasegawa.diapp.restservices.ImportantNewsRest.ImportantNewsResponse
import com.hasegawa.diapp.restservices.RestConfig
import com.hasegawa.diapp.restservices.StepsRest
import com.hasegawa.diapp.restservices.StepsRest.StepResponse
import com.hasegawa.diapp.syncadapters.authenticators.StubAuthenticator
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.ArrayList
import java.util.Random
import java.util.UUID
import java.util.concurrent.TimeUnit

class SyncAdapter : AbstractThreadedSyncAdapter {
    private lateinit var contentResolver: ContentResolver

    constructor(ctx: Context, autoInitialize: Boolean) : super(ctx, autoInitialize) {
        contentResolver = context.contentResolver
    }

    constructor(ctx: Context, autoInitialize: Boolean, allowParallelSyncs: Boolean)
    : super(ctx, autoInitialize, allowParallelSyncs) {
        contentResolver = context.contentResolver
    }

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?,
                               provider: ContentProviderClient?, syncResult: SyncResult?) {

        Timber.d("Initiating sync!")

        val retrofit = Retrofit.Builder()
                .baseUrl(RestConfig.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val stepsRest = retrofit.create(StepsRest::class.java)
        val importantNewsRest = retrofit.create(ImportantNewsRest::class.java)

        syncSteps(stepsRest, syncResult)
        syncImportantNews(importantNewsRest, syncResult)
        if (!syncResult!!.hasError()) {
            val lastUpdateDate = DateTime.now().toLocalDateTime().toString(
                    DateTimeFormat.forPattern("d/M/yyyy H:m:s")
            )
            val lastUpdateIntent = Intent()
            lastUpdateIntent.action = BaseNavDrawerActivity.BROADCAST_LAST_UPDATE_ACTION
            lastUpdateIntent.putExtra(
                    BaseNavDrawerActivity.BROADCAST_LAST_UPDATE_DATE_KEY,
                    lastUpdateDate)

            context.sendBroadcast(lastUpdateIntent)
        }
    }


    private fun syncImportantNews(importantNewsRest: ImportantNewsRest, syncResult: SyncResult?) {
        val newsCall = importantNewsRest.listImportantNews()
        try {
            val newsResponse = newsCall.execute()
            if (newsResponse.isSuccessful) {
                val listNews = newsResponse.body()
                try {
                    saveImportantNewsResponseList(listNews)
                } catch (e: Exception) {
                    syncResult!!.databaseError = true
                    Timber.d(e, "Database error: ${e.message}")
                }
            } else {
                Timber.d("Response error: ${newsResponse.code()} // ${newsResponse.errorBody().string()}")
                syncResult!!.stats.numIoExceptions++
                syncResult.delayUntil = 10
            }
        } catch (e: Exception) {
            Timber.d(e, "Network Response error: ${e.message}")
            syncResult!!.delayUntil = 10
            syncResult.stats.numIoExceptions++
        }
    }

    private fun syncSteps(stepsRest: StepsRest, syncResult: SyncResult?) {
        val stepsCall = stepsRest.listSteps()
        try {
            val stepsResponse = stepsCall.execute()
            if (stepsResponse.isSuccessful) {
                val listSteps = stepsResponse.body()
                try {
                    saveStepsResponsesList(listSteps)
                } catch (e: Exception) {
                    syncResult!!.databaseError = true
                    Timber.d(e, "Database error: ${e.message}")
                }
            } else {
                Timber.d("Response error: ${stepsResponse.code()} // ${stepsResponse.errorBody().string()}")
                syncResult!!.stats.numIoExceptions++
                syncResult.delayUntil = DELAY_RETRY
            }
        } catch (e: Exception) {
            Timber.d(e, "Network Response error: ${e.message}")
            syncResult!!.delayUntil = DELAY_RETRY
            syncResult.stats.numIoExceptions++
        }
    }

    private fun saveImportantNewsResponseList(newsResponses: List<ImportantNewsResponse>) {
        try {
            val uri = Uri.parse(ImportantNewsContract.URI)
            val newsOperations = ArrayList<ContentProviderOperation>(newsResponses.size + 1)
            newsOperations += ContentProviderOperation.newDelete(uri).build()

            newsResponses.map {
                ImportantNews(
                        UUID.randomUUID().toString(),
                        it.title!!,
                        it.url!!,
                        it.date!!,
                        it.tldr
                )
            }.foldRight(newsOperations) {
                news, array ->
                val op = ContentProviderOperation.newInsert(uri)
                        .withValues(news.toContentValues()).build()
                newsOperations += op
                newsOperations
            }
            DiApp.diProvider.internal().contentResolver().applyBatch(
                    DiContract.AUTHORITY, newsOperations)
            DiApp.diProvider.internal().contentResolver().notifyChange(
                    uri, null)
            Timber.d("Saved list of news successfully.")
        } catch (e: Exception) {
            Timber.d(e, "Error while saving list of important news.")
            throw e
        }
    }

    private fun saveStepsResponsesList(stepResponsesList: List<StepResponse>) {
        try {
            data class StepResponseAndId(val stepResponse: StepResponse, val id: String)

            val stepsResponsesWithId = stepResponsesList.map {
                StepResponseAndId(it, UUID.randomUUID().toString())
            }

            val steps = stepsResponsesWithId.map {
                Step(
                        it.id,
                        it.stepResponse.position!!,
                        it.stepResponse.title!!,
                        it.stepResponse.description!!,
                        it.stepResponse.completed!!,
                        it.stepResponse.possibleDate!!
                )
            }

            val stepLinks = stepsResponsesWithId
                    .filter { (it.stepResponse.links?.size ?: 0) > 0 }
                    .flatMap {
                        it.stepResponse.links!!.map {
                            link ->
                            StepLink(
                                    UUID.randomUUID().toString(),
                                    it.id,
                                    link.title!!,
                                    link.url!!)
                        }
                    }
                    .groupBy { it.stepsId }

            val stepUri = Uri.parse(StepsContract.URI)
            val linkUri = Uri.parse(LinksContract.URI)
            val stepOperations = ArrayList<ContentProviderOperation>(
                    2 + steps.size + stepLinks.size)
            stepOperations +=
                    ContentProviderOperation.newDelete(stepUri).build()
            stepOperations +=
                    ContentProviderOperation.newDelete(linkUri).build()
            steps.foldRight(stepOperations) {
                step, arr ->
                val op = ContentProviderOperation
                        .newInsert(stepUri)
                        .withValues(step.toContentValues())
                        .build()
                arr += op
                if (stepLinks.containsKey(step.id)) {
                    stepLinks[step.id]!!.foldRight(arr) {
                        link, arr ->
                        val linkOp = ContentProviderOperation
                                .newInsert(linkUri)
                                .withValues(link.toContentValues())
                                .build()
                        arr += linkOp
                        arr
                    }
                }
                arr
            }
            DiApp.diProvider.internal().contentResolver().applyBatch(
                    DiContract.AUTHORITY,
                    stepOperations)

            DiApp.diProvider.internal().contentResolver().notifyChange(stepUri, null)
            DiApp.diProvider.internal().contentResolver().notifyChange(linkUri, null)
            Timber.d("List of steps saved successfully.")
        } catch (e: Exception) {
            Timber.d(e, "Error while saving list of steps.")
            throw e
        }
    }

    companion object {
        private const val DELAY_RETRY = 30L
        private const val FULL_SYNC_DELAY_FROM_REQUEST = 10 * 60 * 1000

        private var requestSubscription: Subscription? = null
        private var requestLock = Any()
        fun requestFullSync(ctx: Context, forceImmediateSync: Boolean = false,
                            skipDelay: Boolean = false) {
            synchronized(requestLock) {
                if (skipDelay) {
                    if (requestSubscription != null && !requestSubscription!!.isUnsubscribed) {
                        requestFullSync(ctx, true)
                    }
                } else if (forceImmediateSync) {
                    Timber.d("Force syncing.")
                    requestSubscription?.unsubscribeIfSubscribed()
                    val bundle = Bundle()
                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
                    ContentResolver.requestSync(StubAuthenticator.createSyncAccount(ctx),
                            DiContract.AUTHORITY, bundle)
                } else {
                    if (requestSubscription != null && !requestSubscription!!.isUnsubscribed) {
                        Timber.d("Skipping delayed sync.")
                        return
                    }
                    val r = Random()
                    val timeToDelay = r.nextInt(FULL_SYNC_DELAY_FROM_REQUEST)
                    Timber.d("Initiating delayed sync in $timeToDelay ms.")
                    requestSubscription =
                            Observable.timer(timeToDelay.toLong(), TimeUnit.MILLISECONDS)
                                    .subscribeOn(AndroidSchedulers.mainThread())
                                    .take(1)
                                    .subscribe(object : Observer<Any> {
                                        override fun onCompleted() {
                                        }

                                        override fun onError(e: Throwable?) {
                                            Timber.d(e, "Error trying a delayed sync.")
                                        }

                                        override fun onNext(t: Any?) {
                                            val bundle = Bundle()
                                            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true)
                                            ContentResolver.requestSync(
                                                    StubAuthenticator.createSyncAccount(ctx),
                                                    DiContract.AUTHORITY,
                                                    bundle
                                            )
                                        }
                                    })
                }
            }
        }
    }
}

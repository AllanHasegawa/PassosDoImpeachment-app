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

package com.hasegawa.diapp.syncadapters

import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract
import com.hasegawa.diapp.domain.devices.SyncScheduler
import com.hasegawa.diapp.syncadapters.authenticators.StubAuthenticator
import rx.Observable
import rx.Subscriber
import rx.Subscription
import timber.log.Timber
import java.util.Random
import java.util.concurrent.TimeUnit

class SyncAdapterScheduler : SyncScheduler {
    var subscription: Subscription? = null
    val context: Context

    constructor(ctx: Context) {
        context = ctx
    }

    override fun enqueueSync(delayed: Boolean) {
        if (delayed && subscription != null && !subscription!!.isUnsubscribed) {
            // do nothing when request a delayed action and it is already queued
        } else if (delayed) {
            val delay = Random().nextInt(FULL_SYNC_DELAY_FROM_REQUEST)
            subscription = Observable.timer(delay.toLong(), TimeUnit.MILLISECONDS)
                    .subscribe(object : Subscriber<Long>() {
                        override fun onCompleted() {
                        }

                        override fun onError(e: Throwable?) {
                            Timber.d(e, "Error enqueuing sync")
                        }

                        override fun onNext(t: Long?) {
                            requestSync(false)
                        }
                    })
        } else {
            requestSync(true)
        }
    }

    private fun requestSync(forceNow: Boolean) {
        val bundle = Bundle()
        if (forceNow) {
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
        } else {
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS, true)
        }
        ContentResolver.requestSync(
                StubAuthenticator.createSyncAccount(context),
                DiContract.AUTHORITY,
                bundle
        )
    }

    companion object {
        private const val FULL_SYNC_DELAY_FROM_REQUEST = 10 * 60 * 1000
    }
}

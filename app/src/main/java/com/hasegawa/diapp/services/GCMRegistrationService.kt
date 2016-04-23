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
package com.hasegawa.diapp.services

import android.app.IntentService
import android.content.Intent
import com.google.android.gms.gcm.GcmPubSub
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.usecases.PostGCMRegistrationUseCase
import rx.Subscriber
import rx.schedulers.Schedulers
import timber.log.Timber

class GCMRegistrationService : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        try {
            val instanceId = InstanceID.getInstance(this)
            val token = instanceId.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)


            Timber.d("GCM Registration token: $token")
            val postGcmUc = PostGCMRegistrationUseCase(token, DiApp.syncsRepository,
                    DiApp.restServices, Schedulers.io(), Schedulers.io())
            postGcmUc.executeBlocking(object : Subscriber<Boolean>() {
                override fun onCompleted() {
                }

                override fun onError(e: Throwable?) {
                    Timber.d(e, "Error posting GCM Token.")
                    throw e!!
                }

                override fun onNext(t: Boolean?) {
                    Timber.d("Posted token? $t")
                    if (t != null && t) {
                        subscribeTopics(token)
                    }
                }
            })
        } catch (e: Exception) {
            Timber.d(e, "Failed to complete token refresh")
        }
    }

    private fun subscribeTopics(token: String) {
        val pubSub = GcmPubSub.getInstance(this)
        for (s in TOPICS) {
            pubSub.subscribe(token, "/topics/$s", null)
        }
    }

    companion object {
        const val TAG = "GCMRegistractionService"
        val TOPICS = arrayOf("sync", "news")

        const val INTENT_KEY_FORCE_REGISTER = "force_register"
    }
}

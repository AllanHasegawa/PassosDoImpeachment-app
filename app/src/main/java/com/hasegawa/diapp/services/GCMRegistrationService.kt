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
import android.preference.PreferenceManager
import com.google.android.gms.gcm.GcmPubSub
import com.google.android.gms.gcm.GoogleCloudMessaging
import com.google.android.gms.iid.InstanceID
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.restservices.GCMRest
import com.hasegawa.diapp.restservices.GCMRest.TokenPost
import com.hasegawa.diapp.restservices.RestConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class GCMRegistrationService : IntentService(TAG) {

    override fun onHandleIntent(intent: Intent?) {
        val forceUpdate = intent!!.getBooleanExtra(INTENT_KEY_FORCE_REGISTER, false)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (!forceUpdate && sharedPrefs.getBoolean(DiApp.PREFS_KEY_SENT_TOKEN_TO_SERVER, false)) {
            Timber.d("No need to register GCM token to server.")
            return;
        }

        try {
            val instanceId = InstanceID.getInstance(this)
            val token = instanceId.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null)
            Timber.d("GCM Registration token: $token")

            sendTokenToServer(token)

            subscribeTopics(token)

            sharedPrefs.edit().putBoolean(DiApp.PREFS_KEY_SENT_TOKEN_TO_SERVER, true).apply()
        } catch (e: Exception) {
            Timber.d(e, "Failed to complete token refresh")
            sharedPrefs.edit().putBoolean(DiApp.PREFS_KEY_SENT_TOKEN_TO_SERVER, false).apply()
        }
    }

    private fun sendTokenToServer(token: String) {
        val retrofit = Retrofit.Builder()
                .baseUrl(RestConfig.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val gcmRest = retrofit.create(GCMRest::class.java)
        val tokenCall = gcmRest.gcmTokenPost(TokenPost(token))
        val response = tokenCall.execute()
        if (response == null || !response.isSuccessful) {
            throw RuntimeException("Error sending token to server." +
                    "${response.code()}: ${response.body()}")
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

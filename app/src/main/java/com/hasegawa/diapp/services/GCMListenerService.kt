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

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.NotificationCompat
import com.google.android.gms.gcm.GcmListenerService
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.activities.MainActivity
import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.devices.LogDevice
import com.hasegawa.diapp.domain.devices.SyncScheduler
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.usecases.AddPendingSyncUseCase
import rx.Subscriber
import java.util.*
import javax.inject.Inject

class GCMListenerService : GcmListenerService() {

    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var syncsRepository: SyncsRepository
    @Inject lateinit var executionThread: ExecutionThread
    @Inject lateinit var postExecutionThread: PostExecutionThread
    @Inject lateinit var logDevice: LogDevice

    override fun onMessageReceived(from: String?, data: Bundle?) {
        DiApp.appComponent.inject(this)

        var delayed = true
        if (from!!.startsWith("/topics/sync")) {
            logDevice.d("GCM full sync request received.")
        } else if (from.startsWith("/topics/news")) {
            logDevice.d("GCM news notification request received.")
            val title = applicationContext.getString(R.string.notification_default_title)
            val message =
                    data!!.getString("notificationMessage",
                            applicationContext.getString(R.string.notification_default_message))
            appMessageNotification(title, message)
            delayed = false
        }

        AddPendingSyncUseCase(delayed, syncScheduler, syncsRepository,
                executionThread, postExecutionThread)
                .executeBlocking(
                        object : Subscriber<SyncEntity?>() {
                            override fun onCompleted() {
                            }

                            override fun onError(e: Throwable?) {
                                logDevice.d(e, "Error trying to add pending sync.")
                            }

                            override fun onNext(t: SyncEntity?) {
                            }
                        }
                )
    }

    private fun appMessageNotification(title: String, message: String) {
        val resultIntent = Intent(applicationContext, MainActivity::class.java)
        resultIntent.putExtra(MainActivity.INTENT_VIEW_NUMBER_KEY, 1)

        val stackBuilder = TaskStackBuilder.create(applicationContext)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)

        val pendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(applicationContext)
                .setSmallIcon(R.drawable.app_icon_plain)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        val notificationMgr = applicationContext
                .getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
        notificationMgr.notify(Random().nextInt(), notification)
    }
}

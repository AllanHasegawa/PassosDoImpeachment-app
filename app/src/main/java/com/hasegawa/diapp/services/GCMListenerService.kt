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
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.NotificationCompat
import com.google.android.gms.gcm.GcmListenerService
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.activities.BaseNavDrawerActivity
import com.hasegawa.diapp.activities.MainActivity
import com.hasegawa.diapp.syncadapters.SyncAdapter
import timber.log.Timber
import java.util.Random

class GCMListenerService : GcmListenerService() {

    override fun onMessageReceived(from: String?, data: Bundle?) {
        if (from!!.startsWith("/topics/sync")) {
            Timber.d("GCM full sync request received.")
        } else if (from.startsWith("/topics/news")) {
            Timber.d("GCM news notification request received.")
            val title = applicationContext.getString(R.string.notification_default_title)
            val message =
                    data!!.getString("notificationMessage",
                            applicationContext.getString(R.string.notification_default_message))
            appMessageNotification(title, message)
        }

        SyncAdapter.requestFullSync(applicationContext)
        sendSyncRequestToActivityAlive()
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        prefs.edit().putBoolean(DiApp.PREFS_KEY_SYNC_PENDING, true).commit()
    }

    private fun sendSyncRequestToActivityAlive() {
        // The receiver is registered in the BaseNavDrawerActivity, where,
        // if the activity is on screen to the user, the sync will happen
        // immediately.
        val intent = Intent()
        intent.action = BaseNavDrawerActivity.BROADCAST_REQUEST_SYNC_ACTION
        applicationContext.sendBroadcast(intent)
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

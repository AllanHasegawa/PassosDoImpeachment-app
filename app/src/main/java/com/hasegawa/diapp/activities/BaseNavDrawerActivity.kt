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

package com.hasegawa.diapp.activities

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.syncadapters.SyncAdapter

abstract class BaseNavDrawerActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener {

    protected var lastUpdateDate: String? = null
    private var lastUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val date = intent!!.getStringExtra(BROADCAST_LAST_UPDATE_DATE_KEY)
            lastUpdateDate = date

            Snackbar.make(getSnackBarAnchorView(),
                    getString(R.string.sync_done), Snackbar.LENGTH_SHORT)
                    .show()
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putString(DiApp.PREFS_KEY_LAST_UPDATE, lastUpdateDate).commit()
            prefs.edit().putBoolean(DiApp.PREFS_KEY_SYNC_PENDING, false).commit()

            updateNavLastUpdateTitle(date)
        }
    }

    private var syncRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            SyncAdapter.requestFullSync(this@BaseNavDrawerActivity, true)
            Snackbar.make(getSnackBarAnchorView(), getString(R.string.sync_on_going),
                    Snackbar.LENGTH_INDEFINITE).show()
        }
    }

    override fun onResume() {
        super.onResume()
        updateNavLastUpdateTitle()

        val lastUpdateFilter = IntentFilter()
        lastUpdateFilter.addAction(BROADCAST_LAST_UPDATE_ACTION)
        registerReceiver(lastUpdateReceiver, lastUpdateFilter)

        val syncRequestFilter = IntentFilter()
        syncRequestFilter.addAction(BROADCAST_REQUEST_SYNC_ACTION)
        registerReceiver(syncRequestReceiver, syncRequestFilter)


        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (prefs.getBoolean(DiApp.PREFS_KEY_SYNC_PENDING, false)) {
            SyncAdapter.requestFullSync(this, true)
            prefs.edit().putBoolean(DiApp.PREFS_KEY_SYNC_PENDING, false).commit()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(lastUpdateReceiver)
        unregisterReceiver(syncRequestReceiver)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.nav_last_update -> {
                Snackbar.make(getSnackBarAnchorView(),
                        R.string.sync_manual_request_started,
                        Snackbar.LENGTH_LONG).show()
            }
            R.id.nav_send_feedback -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(getString(R.string.app_feedback_url))
                try {
                    startActivity(i)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, getString(R.string.error_invalid_link),
                            Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_opensource_link -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(getString(R.string.app_opensource_url))
                try {
                    startActivity(i)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, getString(R.string.error_invalid_link),
                            Toast.LENGTH_SHORT).show()
                }
            }
        }

        return true
    }

    fun updateNavLastUpdateTitle(date: String? = null) {
        val newDate: String
        if (date == null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            newDate = prefs.getString(DiApp.PREFS_KEY_LAST_UPDATE,
                    getString(R.string.sync_never_updated))
        } else {
            newDate = date
        }
        val lastUpdateItem = getNavigationView().menu.findItem(R.id.nav_last_update)
        lastUpdateItem.title = newDate
    }

    fun forceSyncIfFirstTime() {
        if (lastUpdateDate == null) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            if (!prefs.contains(DiApp.PREFS_KEY_LAST_UPDATE)) {
                SyncAdapter.requestFullSync(this, true)
                Snackbar.make(getSnackBarAnchorView(),
                        R.string.sync_first, Snackbar.LENGTH_INDEFINITE)
                        .show()
            }
        }
    }

    abstract fun getNavigationView(): NavigationView
    abstract fun getSnackBarAnchorView(): View


    companion object {
        const val BROADCAST_LAST_UPDATE_ACTION = "com.hasegawa.diapp.main.last_update"
        const val BROADCAST_LAST_UPDATE_DATE_KEY = "last_update_date"
        const val BROADCAST_REQUEST_SYNC_ACTION = "com.hasegawa.diapp.main.request_sync"
    }
}

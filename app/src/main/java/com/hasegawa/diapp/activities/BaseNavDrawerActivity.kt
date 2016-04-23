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
import android.content.Intent
import android.net.Uri
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.usecases.GetLastSuccessfulSyncUseCase
import com.hasegawa.diapp.domain.usecases.SyncIfNecessaryUseCase
import com.hasegawa.diapp.utils.DateTimeExtensions
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

abstract class BaseNavDrawerActivity : AppCompatActivity(),
        NavigationView.OnNavigationItemSelectedListener {
    private var getLastSuccessfulSyncUc: GetLastSuccessfulSyncUseCase? = null
    private var syncIfNeededUc: SyncIfNecessaryUseCase? = null
    private var initialSyncSnackbarOn = false

    override fun onResume() {
        super.onResume()
        setupNavLastUpdateTitle()
        syncIfNeeded()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        syncIfNeededUc?.unsubscribe()
        getLastSuccessfulSyncUc?.unsubscribe()
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

    fun setupNavLastUpdateTitle() {
        val lastUpdateItem = getNavigationView().menu.findItem(R.id.nav_last_update)
        getLastSuccessfulSyncUc?.unsubscribe()

        getLastSuccessfulSyncUc = GetLastSuccessfulSyncUseCase(DiApp.syncsRepository,
                Schedulers.io(), AndroidSchedulers.mainThread())

        getLastSuccessfulSyncUc?.execute(object : Subscriber<SyncEntity?>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
            }

            override fun onNext(t: SyncEntity?) {
                Timber.d("get last sync: $t")
                if (t != null) {
                    val millis = DateTimeExtensions.fromUnixTimestamp(t.timeSynced!!).millis
                    val date = DateUtils.formatDateTime(applicationContext, millis,
                            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME)
                    lastUpdateItem.title = date
                    if (initialSyncSnackbarOn) {
                        initialSyncSnackbarOn = false
                        Snackbar.make(getSnackBarAnchorView(), getString(R.string.sync_done),
                                Snackbar.LENGTH_LONG).show()
                    }
                } else {
                    Timber.d("SyncEntity in last sync was NULL")
                    lastUpdateItem.title = getString(R.string.sync_never_updated)
                    Snackbar.make(getSnackBarAnchorView(), getString(R.string.sync_on_going),
                            Snackbar.LENGTH_LONG).show()
                }
            }
        })

    }

    fun syncIfNeeded() {
        syncIfNeededUc?.unsubscribe()

        syncIfNeededUc = SyncIfNecessaryUseCase(DiApp.syncScheduler, DiApp.syncsRepository,
                Schedulers.io(), Schedulers.io())

        syncIfNeededUc?.execute(object : Subscriber<Boolean>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Timber.d(e, "Error trying to sync if needed.")
            }

            override fun onNext(t: Boolean?) {
                if (t != null && t) {
                    initialSyncSnackbarOn = true
                    Snackbar.make(getSnackBarAnchorView(),
                            R.string.sync_first, Snackbar.LENGTH_INDEFINITE)
                            .show()
                }
            }
        })
    }

    abstract fun getNavigationView(): NavigationView
    abstract fun getSnackBarAnchorView(): View
}

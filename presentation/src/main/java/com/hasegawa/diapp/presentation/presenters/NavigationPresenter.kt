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

package com.hasegawa.diapp.presentation.presenters

import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.devices.DateDevice
import com.hasegawa.diapp.domain.devices.LogDevice
import com.hasegawa.diapp.domain.devices.UrlOpener
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.usecases.GetLastSuccessfulSyncUseCase
import com.hasegawa.diapp.presentation.ConstStrings
import com.hasegawa.diapp.presentation.views.NavigationMvpView
import rx.Subscriber
import javax.inject.Inject

class NavigationPresenter @Inject constructor(
        private val urlOpener: UrlOpener,
        private val dateDevice: DateDevice,
        private val constStrings: ConstStrings,
        private val logDevice: LogDevice,
        private val syncsRepository: SyncsRepository,
        private val executionThread: ExecutionThread,
        private val postExecutionThread: PostExecutionThread) :
        Presenter<NavigationMvpView>() {

    private var getLastUpdateUc: GetLastSuccessfulSyncUseCase? = null

    override fun onPause() {
        getLastUpdateUc?.unsubscribe()
        getLastUpdateUc = null
    }

    override fun onResume() {
        getLastUpdateUc = GetLastSuccessfulSyncUseCase(syncsRepository,
                executionThread, postExecutionThread)
        getLastUpdateUc?.execute(object : Subscriber<SyncEntity?>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error getting last sync.")
            }

            override fun onNext(t: SyncEntity?) {
                logDevice.d("Hey :3 $t")
                if (t != null) {
                    setUpdateDate(t.timeSynced)
                } else {
                    setUpdateDate(null)
                }
            }
        })
    }

    override fun onViewBound() {
        view.itemTouchListener = {
            view.renderItemSelected(it)
            when (it) {
                NavigationMvpView.Item.Feedback -> urlOpener.openUrl(constStrings.navFeedbackUrl)
                NavigationMvpView.Item.OpenSource -> urlOpener.openUrl(constStrings.navOpenSourceUrl)
                else -> Unit
            }
            view.actItemTouched(it)
        }

        view.drawerStateListener = {
            when (it) {
                NavigationMvpView.DrawerState.Opened -> view.renderOpenedNavView()
                NavigationMvpView.DrawerState.Closed -> view.renderClosedNavView()
            }
        }

        view.itemSelectionListener = { item ->
            view.renderItemSelected(item)
        }
    }

    private fun setUpdateDate(time: Long?) {
        val str = if (time == null) {
            constStrings.navSyncNeverUpdated
        } else {
            dateDevice.timestampToFormattedDateTime(time)
        }
        view.renderUpdateDateText(str)
    }
}

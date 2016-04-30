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
import com.hasegawa.diapp.domain.devices.ScreenDevice
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetNumStepsTotalCompletedUseCase
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.presentation.views.MainMvpView
import rx.Subscriber
import rx.schedulers.Timestamped
import java.util.*
import javax.inject.Inject

class MainPresenter @Inject constructor(
        private val screenDevice: ScreenDevice,
        private val logDevice: LogDevice,
        private val dateDevice: DateDevice,
        private val stepsRepository: StepsRepository,
        private val executionThread: ExecutionThread,
        private val postExecutionThread: PostExecutionThread) :
        Presenter<MainMvpView>() {

    var selectionListener: (selection: MainMvpView.Selection) -> Unit = {}

    private var getNumTotalsCompletedUc: GetNumStepsTotalCompletedUseCase? = null

    override fun onPause() {
        getNumTotalsCompletedUc?.unsubscribe()
        getNumTotalsCompletedUc = null
    }

    override fun onResume() {
        getNumTotalsCompletedUc = GetNumStepsTotalCompletedUseCase(stepsRepository,
                executionThread, postExecutionThread)
        getNumTotalsCompletedUc?.execute(object : Subscriber<NumCompletedAndTotal>() {

            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error loading num of completed and total.")
            }

            override fun onNext(t: NumCompletedAndTotal?) {
                if (t != null) {
                    view.renderNumStepsCompletedAndTotal(t)
                }
            }
        })
    }

    override fun onViewBound() {
        if (!screenDevice.isTablet()) {
            view.selectionListener = {
                selectionListener(it)
                view.renderSelection(it)
                when (it) {
                    MainMvpView.Selection.Steps -> view.renderSizeState(MainMvpView.SizeState.Expanded)
                    MainMvpView.Selection.News -> view.renderSizeState(MainMvpView.SizeState.Shrunk)
                }
            }
            view.listStepsScrollListener = { handleListStepsScroll(it) }
        }
    }

    private var dyHistory: List<Timestamped<Int>> = ArrayList(100)
    private fun handleListStepsScroll(dy: Int) {
        if (screenDevice.isTablet()) return
        if (dy != 0) {
            val useMoreComplexChangeDetection = false
            if (useMoreComplexChangeDetection) {
                val now = dateDevice.nowInMillis()
                (dyHistory as ArrayList).add(Timestamped(now, dy))
                dyHistory = dyHistory.takeLast(20).filter { (now - it.timestampMillis) < 500 }
                val dySum = dyHistory.sumBy { it.value }

                val dyDp = screenDevice.pxToDp(dySum)
                if (Math.abs(dyDp) > 32) {
                    if (dyDp < 0) {
                        view.renderSizeState(MainMvpView.SizeState.Expanded)
                    } else {
                        view.renderSizeState(MainMvpView.SizeState.Shrunk)
                    }
                }
            } else {
                if (dy < 0) {
                    view.renderSizeState(MainMvpView.SizeState.Expanded)
                } else {
                    view.renderSizeState(MainMvpView.SizeState.Shrunk)
                }
            }
        }
    }
}

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
import com.hasegawa.diapp.presentation.mvps.MainMvp
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
        MainMvp.Presenter() {

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

    override fun handleRouteChange(route: MainMvp.Route) {
        view.renderRouteSelection(route)
        when (route) {
            MainMvp.Route.Steps,
            MainMvp.Route.News -> {
                view.renderSizeState(routeToSize(route))
                view.renderMode(routeToMode(route))
            }
            MainMvp.Route.Credits -> {
                view.renderMode(routeToMode(route))
            }
            else -> Unit
        }
        view.actRouteChange(route)
    }

    override fun handleStepSelectionChange(position: Int) {
        view.renderStepSelectedByPosition(position)
    }

    override fun handleTabSelectionChange(route: MainMvp.Route) {
        view.renderRouteSelection(route)
        view.renderSizeState(routeToSize(route))
        view.actRouteChange(route)
    }

    override fun handleViewPagerChange(route: MainMvp.Route) {
        view.renderRouteSelection(route)
        view.renderSizeState(routeToSize(route))
    }


    private var dyHistory: List<Timestamped<Int>> = ArrayList(100)
    override fun handleStepsListScroll(dy: Int) {
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
                        view.renderSizeState(MainMvp.SizeState.Expanded)
                    } else {
                        view.renderSizeState(MainMvp.SizeState.Shrunk)
                    }
                }
            } else {
                if (dy < 0) {
                    view.renderSizeState(MainMvp.SizeState.Expanded)
                } else {
                    view.renderSizeState(MainMvp.SizeState.Shrunk)
                }
            }
        }
    }


    private fun routeToSize(route: MainMvp.Route): MainMvp.SizeState = when (route) {
        MainMvp.Route.Steps -> MainMvp.SizeState.Expanded
        MainMvp.Route.News -> MainMvp.SizeState.Shrunk
        else -> throw RuntimeException("Can't convert route $route to SizeState.")
    }

    private fun routeToMode(route: MainMvp.Route): MainMvp.Mode = when (route) {
        MainMvp.Route.Steps -> MainMvp.Mode.TwoPane
        MainMvp.Route.News -> MainMvp.Mode.OnePane
        MainMvp.Route.Credits -> MainMvp.Mode.OnePane
        else -> throw RuntimeException("Can't convert route $route to Mode.")
    }
}

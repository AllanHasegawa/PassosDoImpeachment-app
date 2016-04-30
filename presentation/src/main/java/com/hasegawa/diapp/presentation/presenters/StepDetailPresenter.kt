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
import com.hasegawa.diapp.domain.devices.LogDevice
import com.hasegawa.diapp.domain.devices.UrlOpener
import com.hasegawa.diapp.domain.entities.StepWithLinksEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetNumStepsTotalCompletedUseCase
import com.hasegawa.diapp.domain.usecases.GetStepWithLinksByPositionUseCase
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.presentation.views.StepDetailMvpView
import rx.Subscriber
import javax.inject.Inject

class StepDetailPresenter @Inject constructor(
        private val urlOpener: UrlOpener,
        private val logDevice: LogDevice,
        private val stepsRepository: StepsRepository,
        private val executionThread: ExecutionThread,
        private val postExecutionThread: PostExecutionThread) :
        Presenter<StepDetailMvpView>() {

    private var getStepUc: GetStepWithLinksByPositionUseCase? = null
    private var getTotalStepsUc: GetNumStepsTotalCompletedUseCase? = null

    override fun onResume() {
        getStepUc = GetStepWithLinksByPositionUseCase(
                -1, stepsRepository,
                executionThread, postExecutionThread)
        getStepUc?.execute(object : Subscriber<StepWithLinksEntity>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error getting step with links.")
            }

            override fun onNext(t: StepWithLinksEntity?) {
                if (t != null && t.step != null) {
                    view.renderStepAndLinks(t)
                }
            }
        })

        getTotalStepsUc = GetNumStepsTotalCompletedUseCase(stepsRepository,
                executionThread, postExecutionThread)
        getTotalStepsUc?.execute(object : Subscriber<NumCompletedAndTotal>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error getting num steps completed and total.")
            }

            override fun onNext(t: NumCompletedAndTotal?) {
                if (t != null) {
                    view.renderNumStepsCompletedAndTotal(t)
                }
            }
        })
    }

    override fun onPause() {
        getStepUc?.unsubscribe()
        getStepUc = null
        getTotalStepsUc?.unsubscribe()
        getTotalStepsUc = null
    }

    override fun onViewBound() {
        view.viewLinkTouchListener = { urlOpener.openUrl(it) }
    }

    fun setStepPosition(pos: Int) {
        getStepUc?.position = pos
    }
}

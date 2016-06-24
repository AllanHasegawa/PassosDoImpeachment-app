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
import com.hasegawa.diapp.domain.devices.TextSharer
import com.hasegawa.diapp.domain.devices.UrlOpener
import com.hasegawa.diapp.domain.entities.StepWithLinksEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetNumStepsTotalCompletedUseCase
import com.hasegawa.diapp.domain.usecases.GetStepWithLinksByPositionUseCase
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.presentation.ConstStrings
import com.hasegawa.diapp.presentation.mvpview.StepDetailMvpView
import rx.Subscriber
import javax.inject.Inject
import javax.inject.Named

class StepDetailPresenter @Inject constructor(
        @Named("stepPosition") private val stepPosition: Int,
        private val urlOpener: UrlOpener,
        private val logDevice: LogDevice,
        private val textSharer: TextSharer,
        private val constStrings: ConstStrings,
        private val stepsRepository: StepsRepository,
        private val executionThread: ExecutionThread,
        private val postExecutionThread: PostExecutionThread) :
        BasePresenter<StepDetailMvpView>() {

    private var getStepUc: GetStepWithLinksByPositionUseCase? = null
    private var getTotalStepsUc: GetNumStepsTotalCompletedUseCase? = null
    private var stepCache: StepWithLinksEntity? = null
    private var totalCache: Int = 0

    override fun onResume() {
        getStepUc = GetStepWithLinksByPositionUseCase(
                stepPosition, stepsRepository,
                executionThread, postExecutionThread)
        getStepUc?.execute(object : Subscriber<StepWithLinksEntity>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error getting step with links.")
            }

            override fun onNext(t: StepWithLinksEntity?) {
                if (t != null && t.step != null) {
                    stepCache = t
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
                    totalCache = t.total
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
        super.onViewBound()
        view.listenLinkBtTouch = { linkUrl ->
            urlOpener.openUrl(linkUrl)
        }

        view.listenShareFabTouch = {
            if (stepCache?.step != null) {
                val s = stepCache!!.step!!
                val body = constStrings.stepDetailShareBody(s.position, totalCache, s.completed,
                        s.possibleDate, s.title)
                textSharer.shareText(body, constStrings.stepDetailShareHeader)
            }
        }
    }
}

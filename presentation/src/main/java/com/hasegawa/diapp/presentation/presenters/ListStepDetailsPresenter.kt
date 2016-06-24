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
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetNumStepsTotalCompletedUseCase
import com.hasegawa.diapp.domain.usecases.GetStepsUseCase
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.presentation.ConstStrings
import com.hasegawa.diapp.presentation.mvpview.ListStepDetailsMvpView
import rx.Subscriber
import javax.inject.Inject

class ListStepDetailsPresenter @Inject constructor(
        private val logDevice: LogDevice,
        private val stepsRepository: StepsRepository,
        private val constStrings: ConstStrings,
        private val textSharer: TextSharer,
        private val executionThread: ExecutionThread,
        private val postExecutionThread: PostExecutionThread) :
        BasePresenter<ListStepDetailsMvpView>() {

    private var getNumStepsUc: GetNumStepsTotalCompletedUseCase? = null
    private var getSteps: GetStepsUseCase? = null

    private var numSteps = NumCompletedAndTotal(0, 0)
    private var stepsCache: List<StepEntity> = emptyList()
    private var currentStepPosition = 1

    override fun onPause() {
        getNumStepsUc?.unsubscribe()
        getNumStepsUc = null
        getSteps?.unsubscribe()
        getSteps = null
    }

    override fun onResume() {
        getNumStepsUc = GetNumStepsTotalCompletedUseCase(stepsRepository,
                executionThread, postExecutionThread)
        getNumStepsUc?.execute(object : Subscriber<NumCompletedAndTotal>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error getting num steps.")
            }

            override fun onNext(t: NumCompletedAndTotal?) {
                if (t != null) {
                    numSteps = t
                    view.renderNumSteps(t)
                }
            }
        })

        getSteps = GetStepsUseCase(stepsRepository, executionThread, postExecutionThread)
        getSteps?.execute(object : Subscriber<List<StepEntity>>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error getting steps.")
            }

            override fun onNext(t: List<StepEntity>?) {
                if (t != null) {
                    stepsCache = t
                    view.renderStepsByPosition(t.map { it.position }.distinct())
                    view.renderStepPosition(currentStepPosition)
                }
            }
        })
    }

    override fun onViewBound() {
        super.onViewBound()
        view.listenCurrentStepChange = { position -> handleCurrentStepChange(position) }
        view.listenShareBtTouch = { position -> handleShareBtTouch(position) }
    }

    private fun handleCurrentStepChange(position: Int) {
        currentStepPosition = position
        view.renderStepCompleted(findStepByPosition(position)?.completed ?: false)
        view.renderStepPosition(position)
    }

    private fun handleShareBtTouch(position: Int) {
        val step = findStepByPosition(position)
        if (step != null) {
            val body = constStrings.stepDetailShareBody(position, numSteps.total, step.completed,
                    step.possibleDate, step.title)
            textSharer.shareText(body, constStrings.stepDetailShareHeader)
        }
    }

    private fun findStepByPosition(pos: Int): StepEntity? =
            stepsCache.firstOrNull { it.position == pos }
}

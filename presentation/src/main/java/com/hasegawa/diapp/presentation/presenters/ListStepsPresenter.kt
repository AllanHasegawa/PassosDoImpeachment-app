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
import com.hasegawa.diapp.domain.devices.ScreenDevice
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.hasegawa.diapp.domain.usecases.GetStepsUseCase
import com.hasegawa.diapp.presentation.views.ListStepsMvpView
import rx.Subscriber
import java.util.*
import javax.inject.Inject

class ListStepsPresenter @Inject constructor(
        private val screenDevice: ScreenDevice,
        private val logDevice: LogDevice,
        private val stepsRepository: StepsRepository,
        private val executionThread: ExecutionThread,
        private val postExecutionThread: PostExecutionThread) :
        Presenter<ListStepsMvpView>() {

    private var getStepsUc: GetStepsUseCase? = null
    private var stepByPosSelected: Int = -1

    override fun onResume() {
        getStepsUc = GetStepsUseCase(stepsRepository, executionThread, postExecutionThread)
        getStepsUc?.execute(object : Subscriber<List<StepEntity>>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error getting list of steps.")
            }

            override fun onNext(t: List<StepEntity>?) {
                if (t == null) {
                    view.renderSteps(emptyList())
                } else {
                    view.renderSteps(makeStepList(t))
                    if (screenDevice.isTablet()) {
                        view.renderStepByPosSelected(stepByPosSelected)
                    }
                }
            }
        })
    }

    override fun onPause() {
        getStepsUc?.unsubscribe()
        getStepsUc = null
    }

    override fun onViewBound() {
        view.stepTouchListener = { step ->
            stepByPosSelected = step.position
            view.renderStepByPosSelected(step.position)
            view.actStepByPosTouched(step.position)
        }

        view.scrollListener = { dy: Int -> view.actListScrolled(dy) }

        view.stepSelectionListener = { position: Int ->
            stepByPosSelected = position
            view.renderStepByPosSelected(position)
        }
    }

    private fun makeStepList(source: List<StepEntity>): List<ListStepsMvpView.Item> {
        val ret = ArrayList<ListStepsMvpView.Item>(source.size + 1)
        if (!screenDevice.isTablet()) {
            ret.add(ListStepsMvpView.Item(ListStepsMvpView.ITEM_TYPE_SPACE))
        }
        ret.addAll(source.map { ListStepsMvpView.Item(ListStepsMvpView.ITEM_TYPE_STEP, it) })
        return ret
    }
}

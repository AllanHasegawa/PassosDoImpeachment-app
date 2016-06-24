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

package com.hasegawa.diapp.presentation.mvpview

import com.hasegawa.diapp.domain.entities.StepEntity


abstract class ListStepsMvpView : MvpView {
    companion object {
        val ITEM_TYPE_SPACE = 2
        val ITEM_TYPE_STEP = 3
    }

    data class Item(val type: Int, val step: StepEntity? = null)

    var listenScrollChange: (dy: Int) -> Unit = {}
    /**
     * Listen for when a Step is selected without a touch (from saved instance for example)
     * @param[position] Step's position.
     * */
    var listenStepSelectionChange: (position: Int) -> Unit = {}
    /**
     * Listen for user touch event on a step.
     * @param[step] The Step touched.
     * */
    var listenStepTouch: (step: StepEntity) -> Unit = {}

    abstract fun renderSteps(steps: List<Item>)

    /**
     * Highlight the Step.
     * @param[position] Step's position.
     */
    abstract fun renderStepByPosSelected(position: Int)

}

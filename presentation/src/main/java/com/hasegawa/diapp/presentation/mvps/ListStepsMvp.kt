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

package com.hasegawa.diapp.presentation.mvps

import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.presentation.presenters.BasePresenter

object ListStepsMvp {
    const val ITEM_TYPE_SPACE = 2
    const val ITEM_TYPE_STEP = 3

    data class Item(val type: Int, val step: StepEntity? = null)

    interface View {
        fun renderSteps(steps: List<Item>)

        /** Highlight the Step.
         *
         * @param[position] Step's position.
         * */
        fun renderStepByPosSelected(position: Int)
    }

    abstract class Presenter : BasePresenter<View>() {
        abstract fun handleScrollChanged(dy: Int)

        /** For when a Step is selected without a touch (from saved instance for example)
         *
         * @param[position] Step's position.
         * */
        abstract fun handleStepSelectionChanged(position: Int)

        /** User actively touched a Step
         *
         * @param[step] The Step touched.
         * */
        abstract fun handleStepTouched(step: StepEntity)
    }
}

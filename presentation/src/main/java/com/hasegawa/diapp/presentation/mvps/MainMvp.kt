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

import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.presentation.presenters.BasePresenter

object MainMvp {
    enum class Route {
        Steps,
        News,
        StepDetail,
        Credits
    }

    enum class SizeState {
        Shrunk,
        Expanded
    }

    enum class Mode {
        OnePane,
        TwoPane
    }


    interface View {
        /** Triggers a route change. */
        fun actRouteChange(route: MainMvp.Route)

        /** Only mark a route change visually. */
        fun renderRouteSelection(route: Route)


        /** Controls the size of the toolbar. */
        fun renderSizeState(state: SizeState)

        /** Show one or two panels. Used on tablets only. */
        fun renderMode(mode: Mode)

        fun renderNumStepsCompletedAndTotal(numbers: NumCompletedAndTotal)
        fun renderStepSelectedByPosition(position: Int)
    }

    abstract class Presenter : BasePresenter<View>() {
        /** User initiated route change. */
        abstract fun handleRouteChange(route: Route)

        abstract fun handleStepSelectionChange(position: Int)
        abstract fun handleTabSelectionChange(route: Route)
        abstract fun handleViewPagerChange(route: Route)
        abstract fun handleStepsListScroll(dy: Int)
    }
}

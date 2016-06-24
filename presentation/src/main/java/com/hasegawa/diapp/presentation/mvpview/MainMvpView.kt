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

import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal


abstract class MainMvpView : MvpView {
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

    var listenRouteChange: (route: Route) -> Unit = {}
    var listenStepSelectionChange: (position: Int) -> Unit = {}
    var listenTabSelectionChange: (route: Route) -> Unit = {}
    var listenViewPagerChange: (route: Route) -> Unit = {}
    var listenStepsListScroll: (dy: Int) -> Unit = {}

    /**
     * Triggers a route change.
     * TODO[hase] this should be in the presenter somehow.
     */
    abstract fun actRouteChange(route: Route)

    /** Only mark a route change visually. */
    abstract fun renderRouteSelection(route: Route)

    /** Controls the size of the toolbar. */
    abstract fun renderSizeState(state: SizeState)

    /** Show one or two panels. Used on tablets only. */
    abstract fun renderMode(mode: Mode)

    abstract fun renderNumStepsCompletedAndTotal(numbers: NumCompletedAndTotal)
    abstract fun renderStepSelectedByPosition(position: Int)
}

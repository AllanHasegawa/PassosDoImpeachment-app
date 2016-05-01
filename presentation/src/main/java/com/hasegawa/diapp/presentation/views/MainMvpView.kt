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

package com.hasegawa.diapp.presentation.views

import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal

abstract class MainMvpView : MvpView {
    enum class SizeState {
        Shrunk,
        Expanded
    }

    enum class Route {
        Steps,
        News,
        StepDetail,
        Credits
    }

    enum class Mode {
        OnePane,
        TwoPane
    }

    var routeListener: (route: Route) -> Unit = {}
    var listStepsScrollListener: (dy: Int) -> Unit = {}
    var stepSelectedByPosListener: (position: Int) -> Unit = {}

    abstract fun actRouteChange(route: Route)

    abstract fun renderNumStepsCompletedAndTotal(numbers: NumCompletedAndTotal)
    abstract fun renderSizeState(state: SizeState)
    abstract fun renderRouteChange(route: Route)
    abstract fun renderMode(mode: Mode)
    abstract fun renderStepSelectedByPosition(position: Int)
}


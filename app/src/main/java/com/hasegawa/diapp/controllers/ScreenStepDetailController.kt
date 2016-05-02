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

package com.hasegawa.diapp.controllers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.ChildControllerTransaction
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.hasegawa.diapp.R
import com.hasegawa.diapp.presentation.views.MainMvpView
import com.hasegawa.diapp.utils.BundleBuilder

class ScreenStepDetailController : BaseNavigationController {
    interface StepDetailTargetListener {
        fun onRouteFromStepDetail(route: MainMvpView.Route)
    }

    private var stepPosition: Int

    constructor(initialStepPosition: Int, target: Controller? = null) :
    this(BundleBuilder(Bundle()).putInt(BKEY_INITIAL_STEP_POSITION, initialStepPosition)
            .build(), target)

    constructor(bundle: Bundle, target: Controller? = null) : super(bundle) {
        stepPosition = bundle.getInt(BKEY_INITIAL_STEP_POSITION)
        if (target != null) {
            if (target is StepDetailTargetListener) {
                this.targetController = target
            } else {
                // Kotlin does not support non-generic classes with generic constructor :(
                throw RuntimeException("Target must be StepDetailTargetListener")
            }
        }
    }

    constructor(bundle: Bundle) : super(bundle) {
        stepPosition = bundle.getInt(BKEY_INITIAL_STEP_POSITION)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val baseView = super.onCreateView(inflater, container)
        val root = inflater.inflate(R.layout.screen_step_detail, baseContainer, true)

        if (childControllers.isEmpty()) {
            addChildController(ChildControllerTransaction.builder(
                    ListStepDetailsController(stepPosition), R.id.detail_child_container).build())
        }

        return baseView
    }

    override fun onNavigationRouteRequested(route: MainMvpView.Route) {
        val setTargetRoute = {
            if (targetController != null) {
                (targetController as StepDetailTargetListener?)?.onRouteFromStepDetail(route)
            }
        }
        when (route) {
            MainMvpView.Route.Steps -> {
                childControllers.map { router.popController(it) }
                setTargetRoute()
                router.popCurrentController()
            }
            MainMvpView.Route.News -> {
                childControllers.map { router.popController(it) }
                setTargetRoute()
                router.popCurrentController()
            }
            MainMvpView.Route.Credits -> {
                childControllers.map { router.popController(it) }
                router.popCurrentController()
                router.pushController(RouterTransaction.builder(ScreenCreditsController()).build())
            }
            else -> Unit
        }
    }

    companion object {
        const val BKEY_INITIAL_STEP_POSITION = "initial_step_position"
    }
}

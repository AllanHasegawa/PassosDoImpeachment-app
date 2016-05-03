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

import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.bluelinelabs.conductor.ChildControllerTransaction
import com.bluelinelabs.conductor.Controller
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.devices.TextSharer
import com.hasegawa.diapp.presentation.views.MainMvpView
import com.hasegawa.diapp.presentation.views.NavigationMvpView
import javax.inject.Inject

class ScreenCreditsController : BaseNavigationController {

    interface CreditsTargetListener {
        fun onRouteFromCredits(route: MainMvpView.Route)
    }

    @Inject lateinit var textSharer: TextSharer

    @BindView(R.id.credits_toolbar) lateinit var creditsToolbar: Toolbar

    constructor() : this(null)

    constructor(target: Controller?) : super(NavigationMvpView.Item.Credits) {
        if (target != null) {
            if (target is CreditsTargetListener) {
                this.targetController = target
            } else {
                // Kotlin does not support non-generic classes with generic constructor :(
                throw RuntimeException("Target must be of type CreditsTargetListener")
            }
        }

        DiApp.activityComponent.inject(this)
    }

    private lateinit var unbinder: Unbinder

    private lateinit var childController: Controller

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val baseView = super.onCreateView(inflater, container)

        val screen = inflater.inflate(R.layout.screen_credits, baseContainer, true)

        unbinder = ButterKnife.bind(this, screen)

        setupToolbar(creditsToolbar, true, true, { router.handleBack() })

        childController = CreditsController()
        addChildController(ChildControllerTransaction.builder(childController,
                R.id.credits_container).build())

        return baseView
    }

    override fun onDestroyView(view: View?) {
        super.onDestroyView(view)
        removeChildController(childController)
        unbinder.unbind()
    }

    override fun onNavigationRouteRequested(route: MainMvpView.Route) {
        val target = targetController
        when (route) {
            MainMvpView.Route.Steps -> {
                removeChildController(childController)
                (target as CreditsTargetListener?)?.onRouteFromCredits(MainMvpView.Route.Steps)
                router.popCurrentController()
            }
            MainMvpView.Route.News -> {
                removeChildController(childController)
                (target as CreditsTargetListener?)?.onRouteFromCredits(MainMvpView.Route.News)
                router.popCurrentController()
            }
            else -> return
        }
    }

    @OnClick(R.id.credits_fab)
    fun fabClick() {
        textSharer.shareTextByEmail(
                activity.getString(R.string.credits_hase_email),
                activity.getString(R.string.credits_hase_email_subject),
                activity.getString(R.string.credits_email_chooser_header)
        )
    }
}

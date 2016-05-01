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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.bluelinelabs.conductor.ChildControllerTransaction
import com.bluelinelabs.conductor.Controller
import com.hasegawa.diapp.R
import com.hasegawa.diapp.presentation.views.MainMvpView
import com.hasegawa.diapp.presentation.views.NavigationMvpView

class ScreenCreditsController : BaseNavigationController(NavigationMvpView.Item.Credits) {

    private lateinit var unbinder: Unbinder

    private lateinit var childController: Controller

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val baseView = super.onCreateView(inflater, container)

        val screen = inflater.inflate(R.layout.screen_credits, baseContainer, true)

        unbinder = ButterKnife.bind(this, screen)

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
        when (route) {
            MainMvpView.Route.Steps -> {
                removeChildController(childController)
                router.popCurrentController()
            }
            MainMvpView.Route.News -> {
                removeChildController(childController)
                router.popCurrentController()
            }
            else -> Unit
        }
    }

    @OnClick(R.id.credits_fab)
    fun fabClick() {
        Toast.makeText(activity, "Fab Not Working Yet! [TODO]", Toast.LENGTH_SHORT).show()
    }


}

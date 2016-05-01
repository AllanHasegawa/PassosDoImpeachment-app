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

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.bluelinelabs.conductor.Controller
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.presentation.presenters.CreditsPresenter
import com.hasegawa.diapp.presentation.views.CreditsMvpView
import javax.inject.Inject

class CreditsController : Controller() {
    @Inject lateinit var creditsPresenter: CreditsPresenter

    private lateinit var unbinder: Unbinder

    init {
        DiApp.activityComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.view_credits, container, false)
        unbinder = ButterKnife.bind(this, root)

        creditsPresenter.bindView(mvpView)
        creditsPresenter.onResume()

        Log.d("HH", "Starting credits onCreateView")

        return root
    }

    override fun onDestroyView(view: View?) {
        super.onDestroyView(view)
        creditsPresenter.onPause()
        unbinder.unbind()
        Log.d("HH", "Stopping Credits :)")
    }

    private val mvpView = object : CreditsMvpView() {}

    @OnClick(R.id.credits_hase_email_bt)
    fun haseEmailBtClick() = mvpView.haseEmailTouchListener()

    @OnClick(R.id.credits_hase_github_bt)
    fun haseGitHubClick() = mvpView.haseGitHubTouchListener()
}

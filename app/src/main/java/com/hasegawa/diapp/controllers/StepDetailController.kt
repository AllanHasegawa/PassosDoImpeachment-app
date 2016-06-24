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
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.Controller
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.di.DaggerStepDetailComponent
import com.hasegawa.diapp.di.StepDetailModule
import com.hasegawa.diapp.domain.entities.StepWithLinksEntity
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.presentation.mvpview.StepDetailMvpView
import com.hasegawa.diapp.presentation.presenters.StepDetailPresenter
import com.hasegawa.diapp.utils.BundleBuilder
import com.hasegawa.diapp.views.ItemDetailLinkView
import javax.inject.Inject


class StepDetailController : Controller {

    @Inject lateinit var stepDetailPresenter: StepDetailPresenter

    @BindView(R.id.detail_step_number_tv) lateinit var numberTv: TextView
    @BindView(R.id.detail_step_total_numbers_tv) lateinit var totalNumberTv: TextView
    @BindView(R.id.detail_title_tv) lateinit var titleTv: TextView
    @BindView(R.id.detail_date_tv) lateinit var dateTv: TextView
    @BindView(R.id.detail_description_tv) lateinit var descriptionTv: TextView
    @BindView(R.id.detail_links_ll) lateinit var linksLl: LinearLayout

    private lateinit var unbinder: Unbinder

    private var stepPosition: Int

    constructor(stepPosition: Int) : this(BundleBuilder(Bundle())
            .putInt(BKEY_STEP_POSITION, stepPosition).build())

    constructor(bundle: Bundle) : super(bundle) {
        stepPosition = bundle.getInt(BKEY_STEP_POSITION)

        val component = DaggerStepDetailComponent.builder()
                .activityComponent(DiApp.activityComponent)
                .stepDetailModule(StepDetailModule(stepPosition))
                .build()
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.view_step_detail, container, false)

        unbinder = ButterKnife.bind(this, root)

        stepDetailPresenter.bindView(mvpView)
        stepDetailPresenter.onResume()

        return root
    }

    override fun onDestroyView(view: View?) {
        super.onDestroyView(view)
        stepDetailPresenter.onPause()
        unbinder.unbind()
    }

    fun share() {
        mvpView.listenShareFabTouch()
    }

    private val mvpView = object : StepDetailMvpView() {
        override fun renderStepAndLinks(stepWithLinks: StepWithLinksEntity) {
            val step = stepWithLinks.step!!
            numberTv.text = step.position.toString()
            titleTv.text = step.title
            dateTv.text = step.possibleDate
            descriptionTv.text = step.description

            linksLl.removeAllViews()
            stepWithLinks.links.forEach { linkEntity ->
                val linkView = ItemDetailLinkView(activity, null)
                linkView.setOnClickListener {
                    listenLinkBtTouch(linkEntity.url)
                }
                linkView.stepLink = linkEntity
                linksLl.addView(linkView)
            }
        }

        override fun renderNumStepsCompletedAndTotal(numbers: NumCompletedAndTotal) {
            totalNumberTv.text = numbers.total.toString()
        }

    }

    companion object {
        const val BKEY_STEP_POSITION = "step_position"
    }
}

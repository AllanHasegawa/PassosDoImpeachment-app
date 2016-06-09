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
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.Controller
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.devices.LogDevice
import com.hasegawa.diapp.domain.devices.ScreenDevice
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.presentation.mvps.ListStepsMvp
import com.hasegawa.diapp.presentation.presenters.ListStepsPresenter
import com.hasegawa.diapp.utils.BundleBuilder
import com.hasegawa.diapp.views.ItemStepView
import javax.inject.Inject

class ListStepsController : Controller {

    var stepTouchListener: (position: Int) -> Unit = {}
    var listStepsScrollListener: (dy: Int) -> Unit = {}

    @Inject lateinit var screenDevice: ScreenDevice
    @Inject lateinit var logDevice: LogDevice
    @Inject lateinit var listStepsPresenter: ListStepsPresenter

    @BindView(R.id.main_steps_rv)
    lateinit var stepsRv: RecyclerView

    private var adapter: Adapter? = null
    private lateinit var unbinder: Unbinder

    private var stepByPosSelected: Int

    constructor(stepPosition: Int) : this(BundleBuilder(Bundle())
            .putInt(BKEY_STEP_POSITION, stepPosition).build())

    constructor(bundle: Bundle) : super(bundle) {
        stepByPosSelected = bundle.getInt(BKEY_STEP_POSITION)

        DiApp.activityComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.view_list_steps, container, false)

        unbinder = ButterKnife.bind(this, root)

        adapter = Adapter(listStepsPresenter, this)
        stepsRv.layoutManager = LinearLayoutManager(activity)
        stepsRv.adapter = adapter
        stepsRv.addOnScrollListener(onScrollListener)

        listStepsPresenter.bindView(mvpView)
        listStepsPresenter.onResume()
        listStepsPresenter.handleStepSelectionChanged(stepByPosSelected)

        return root
    }

    override fun onDestroyView(view: View?) {
        super.onDestroyView(view)
        adapter = null
        listStepsPresenter.onPause()
        unbinder.unbind()
    }

    fun renderSelectedStepByPosition(position: Int) {
        listStepsPresenter.handleStepSelectionChanged(position)
    }

    private val mvpView = object : ListStepsMvp.View {
        override fun renderStepByPosSelected(position: Int) {
            stepByPosSelected = position
            val newSelectionIndex = adapter?.items?.indexOfFirst { it.step?.position == position }
            if ((newSelectionIndex ?: -1) >= 0) {
                val oldSelected = adapter!!.selected
                adapter!!.selected = newSelectionIndex!!
                adapter!!.notifyItemChanged(oldSelected)
                adapter!!.notifyItemChanged(newSelectionIndex)
            }
        }

        override fun renderSteps(steps: List<ListStepsMvp.Item>) {
            adapter?.items = steps
            adapter?.notifyDataSetChanged()
        }
    }


    private val onScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            listStepsPresenter.handleScrollChanged(dy)
            listStepsScrollListener(dy)
        }
    }

    class Adapter(val presenter: ListStepsMvp.Presenter, val controller: ListStepsController) :
            RecyclerView.Adapter<Adapter.ViewHolder>() {
        class ViewHolder(item: View, val presenter: ListStepsMvp.Presenter,
                         val controller: ListStepsController) : RecyclerView.ViewHolder(item) {
            fun setStep(step: StepEntity?, selected: Boolean) {
                if (step == null) return
                if (itemView is ItemStepView) {
                    itemView.step = step
                    itemView.isSelected = selected
                    itemView.setOnClickListener {
                        presenter.handleStepTouched(step)
                        controller.stepTouchListener(step.position)
                    }
                }
            }
        }

        var items = emptyList<ListStepsMvp.Item>()
        var selected = -1

        override fun getItemViewType(position: Int): Int = items[position].type
        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val type = items[position].type
            when (type) {
                ListStepsMvp.ITEM_TYPE_STEP ->
                    holder?.setStep(items[position].step, selected == position)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
            val ctx = parent!!.context
            val inf = { id: Int -> LayoutInflater.from(ctx).inflate(id, parent, false) }
            val view = when (viewType) {
                ListStepsMvp.ITEM_TYPE_SPACE -> inf(R.layout.item_step_space)
                ListStepsMvp.ITEM_TYPE_STEP -> ItemStepView(ctx, null)
                else -> throw RuntimeException("Unknown view type $viewType")
            }
            return ViewHolder(view, presenter, controller)
        }
    }

    companion object {
        private const val BKEY_STEP_POSITION = "step_position"
    }
}

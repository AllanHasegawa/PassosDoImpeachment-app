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

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.support.ControllerPagerAdapter
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.devices.ScreenDevice
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.presentation.presenters.ListStepDetailsPresenter
import com.hasegawa.diapp.presentation.views.ListStepDetailsMvpView
import com.hasegawa.diapp.utils.BundleBuilder
import com.hasegawa.diapp.views.MaybeSwipeViewPager
import javax.inject.Inject

class ListStepDetailsController : Controller, ViewPager.OnPageChangeListener {

    @Inject lateinit var screenDevice: ScreenDevice
    @Inject lateinit var listStepDetailsPresenter: ListStepDetailsPresenter

    @BindView(R.id.detail_is_completed_fl) lateinit var isCompletedFl: FrameLayout
    @BindView(R.id.detail_view_pager) lateinit var viewPager: MaybeSwipeViewPager
    @BindView(R.id.detail_pb) lateinit var progressBar: ProgressBar

    private lateinit var unbinder: Unbinder
    private lateinit var adapter: StepDetailsAdapter

    protected var numTotalSteps: Int = 1
    protected var stepPosition: Int = 1

    private var isTablet: Boolean = true

    constructor(initialStepPosition: Int) : this(BundleBuilder(Bundle())
            .putInt(BKEY_INITIAL_STEP_POSITION, initialStepPosition).build())

    constructor(bundle: Bundle) : super(bundle) {
        stepPosition = bundle.getInt(BKEY_INITIAL_STEP_POSITION)
        DiApp.activityComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.view_list_step_details, container, false)

        isTablet = screenDevice.isTablet()

        unbinder = ButterKnife.bind(this, root)

        adapter = StepDetailsAdapter(this)
        viewPager.addOnPageChangeListener(this)
        viewPager.enableSwipe = !isTablet
        viewPager.adapter = adapter

        listStepDetailsPresenter.bindView(mvpView)
        listStepDetailsPresenter.onResume()
        mvpView.currentStepListener(stepPosition)

        return root
    }

    override fun onDestroyView(view: View?) {
        super.onDestroyView(view)
        listStepDetailsPresenter.onPause()
        unbinder.unbind()
    }

    fun renderStepByPosition(position: Int) {
        mvpView.currentStepListener(position)
    }

    private val mvpView = object : ListStepDetailsMvpView() {
        override fun renderNumSteps(numbers: NumCompletedAndTotal) {
            numTotalSteps = numbers.total
            updateProgressBar()
        }

        override fun renderStepPosition(position: Int) {
            stepPosition = position
            updateProgressBar()
            viewPager.setCurrentItem(adapter.stepPositions.indexOfFirst { it == position }, !isTablet)
        }

        private var progressAnim: ObjectAnimator? = null
        private fun updateProgressBar() {
            progressBar.max = numTotalSteps * 100
            val newProgress = stepPosition * 100
            if (progressAnim != null) {
                progressAnim!!.cancel()
            }
            progressAnim = ObjectAnimator.ofInt(progressBar,
                    "progress", progressBar.progress, newProgress)
            progressAnim?.duration = 300
            progressAnim?.start()
        }

        override fun renderStepCompleted(completed: Boolean) {
            val positionDrawable = when (completed) {
                true -> R.drawable.border_item_step_number_completed
                false -> R.drawable.border_item_step_number_incomplete
            }
            if (Build.VERSION.SDK_INT >= 16) {
                isCompletedFl.background = ContextCompat.getDrawable(
                        activity, positionDrawable)
            } else {
                isCompletedFl.setBackgroundDrawable(ContextCompat.getDrawable(
                        activity, positionDrawable))
            }
        }

        override fun renderStepsByPosition(positions: List<Int>) {
            adapter.stepPositions = positions
            adapter.notifyDataSetChanged()
            this.currentStepListener(stepPosition)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageSelected(position: Int) {
        mvpView.currentStepListener(adapter.stepPositions[position])
    }

    class StepDetailsAdapter(host: Controller) : ControllerPagerAdapter(host) {
        var stepPositions: List<Int> = emptyList()
        override fun getItem(position: Int) = StepDetailController(stepPositions[position])
        override fun getCount() = stepPositions.size
    }


    companion object {
        const val BKEY_INITIAL_STEP_POSITION = "initial_step_position"
    }
}

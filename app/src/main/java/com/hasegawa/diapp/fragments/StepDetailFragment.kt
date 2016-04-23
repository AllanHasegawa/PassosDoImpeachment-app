/*******************************************************************************
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
 ******************************************************************************/
package com.hasegawa.diapp.fragments

import android.animation.ObjectAnimator
import android.os.Build.VERSION
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.R.drawable
import com.hasegawa.diapp.adapters.StepDetailFragmentAdapter
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.usecases.GetNumStepsTotalCompletedUseCase
import com.hasegawa.diapp.domain.usecases.GetStepByPositionUseCase
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import com.hasegawa.diapp.views.MaybeSwipeViewPager
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

class StepDetailFragment : Fragment(), ViewPager.OnPageChangeListener {

    private lateinit var stepPb: ProgressBar
    private lateinit var isCompletedFl: FrameLayout
    private lateinit var viewPager: ViewPager

    var totalSteps = 0
        private set
    var stepPosition: Int = -1
        set(value) {
            field = value
            getStepByPositionUc?.position = value
        }

    private var getStepByPositionUc: GetStepByPositionUseCase? = null
    private var getNumStepsUc: GetNumStepsTotalCompletedUseCase? = null

    private var isTablet = false

    private lateinit var adapter: StepDetailFragmentAdapter
    var step: StepEntity? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isTablet = arguments.getBoolean(ARG_IS_TABLET, false)
            stepPosition = arguments.getInt(ARG_STEP_POSITION, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_step_detail, container, false)

        isCompletedFl = root.findViewById(R.id.detail_is_completed_fl) as FrameLayout
        stepPb = root.findViewById(R.id.detail_pb) as ProgressBar
        viewPager = root.findViewById(R.id.detail_view_pager) as ViewPager

        if (savedInstanceState != null) {
            stepPosition = savedInstanceState.getInt(ARG_STEP_POSITION, stepPosition)
        }
        Timber.d("Started with position $stepPosition")
        getStepByPositionUc = GetStepByPositionUseCase(stepPosition, DiApp.stepsRepository,
                Schedulers.io(), AndroidSchedulers.mainThread())
        getNumStepsUc = GetNumStepsTotalCompletedUseCase(DiApp.stepsRepository,
                Schedulers.io(), AndroidSchedulers.mainThread())

        adapter = StepDetailFragmentAdapter(activity.supportFragmentManager)
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(this)

        (viewPager as MaybeSwipeViewPager).enableSwipe = !isTablet

        loadGetStepUseCase()
        loadTotalStepsUc()
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        getStepByPositionUc?.unsubscribe()
        getNumStepsUc?.unsubscribe()
        adapter.close()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        if (step != null) {
            outState?.putInt(ARG_STEP_POSITION, step!!.position)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageSelected(position: Int) {
        step = adapter.stepsCache[position]
        loadStepIntoViews()
    }

    private var progressAnim: ObjectAnimator? = null
    private fun updateProgressBar() {
        stepPb.max = totalSteps * 100
        val newProgress = (step?.position ?: 0) * 100
        if (progressAnim != null) {
            progressAnim!!.cancel()
        }
        progressAnim = ObjectAnimator.ofInt(stepPb, "progress", stepPb.progress, newProgress)
        progressAnim?.duration = 300
        progressAnim?.start()
    }

    private fun loadGetStepUseCase() {
        getStepByPositionUc?.execute(object : Subscriber<StepEntity>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Timber.d(e, "Error getting step by position")
            }

            override fun onNext(t: StepEntity?) {
                step = t
                if (t != null) {
                    loadStepIntoViews()
                    val index = adapter.stepsCache
                            .indexOfFirst { it.position == t.position }
                    if (index != -1) {
                        viewPager.setCurrentItem(index, false)
                    }
                }
            }
        })
    }

    private fun loadTotalStepsUc() {

        getNumStepsUc?.execute(object : Subscriber<NumCompletedAndTotal>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Timber.d(e, "Error while trying to find total steps.")
            }

            override fun onNext(t: NumCompletedAndTotal?) {
                if (t != null) {
                    totalSteps = t.total
                    updateProgressBar()
                }
            }
        })
    }


    private fun loadStepIntoViews() {
        if (step == null) return
        val positionDrawable = when (step!!.completed) {
            true -> drawable.border_item_step_number_completed
            false -> drawable.border_item_step_number_incomplete
        }
        if (VERSION.SDK_INT >= 16) {
            isCompletedFl.background = ContextCompat.getDrawable(
                    context, positionDrawable)
        } else {
            isCompletedFl.setBackgroundDrawable(ContextCompat.getDrawable(
                    context, positionDrawable))
        }
        updateProgressBar()
    }


    companion object {
        const val ARG_IS_TABLET = "is_tablet"
        const val ARG_STEP_POSITION = "step_position"
        fun newInstance(isTablet: Boolean, stepPosition: Int): StepDetailFragment {
            val args = Bundle()
            args.putBoolean(ARG_IS_TABLET, isTablet)
            args.putInt(ARG_STEP_POSITION, stepPosition)
            val fragment = StepDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

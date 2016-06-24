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
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.bluelinelabs.conductor.ChildControllerTransaction
import com.bluelinelabs.conductor.Controller
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler
import com.bluelinelabs.conductor.support.ControllerPagerAdapter
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.devices.TextSharer
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.presentation.ConstStrings
import com.hasegawa.diapp.presentation.mvpview.MainMvpView
import com.hasegawa.diapp.presentation.mvpview.NavDrawerMvpView
import com.hasegawa.diapp.presentation.presenters.MainPresenter
import com.hasegawa.diapp.utils.ResourcesUtils
import javax.inject.Inject

class ScreenMainController : BaseNavigationController,
        ScreenCreditsController.CreditsTargetListener,
        ScreenStepDetailController.StepDetailTargetListener {

    @Inject lateinit var mainPresenter: MainPresenter
    @Inject lateinit var constStrings: ConstStrings
    @Inject lateinit var textSharer: TextSharer

    @BindView(R.id.main_toolbar_expanded_pb) lateinit var toolbarPb: ProgressBar
    @BindView(R.id.main_toolbar_expanded_tv) lateinit var toolbarExpandedTv: TextView
    @BindView(R.id.main_toolbar) lateinit var toolbar: Toolbar

    // Phone Mode Views
    private var viewPager: ViewPager? = null // ButterKnife + Kotlin + @Nullable == problem :(
    private var tabLayout: TabLayout? = null
    private var toolbarFl: FrameLayout? = null
    private var toolbarShrunkTv: TextView? = null

    // Tablet Mode Views
    private var mainContainer: FrameLayout? = null
    private var detailContainer: FrameLayout? = null
    private var containersLl: LinearLayout? = null


    private var listStepsController: ListStepsController? = null
    private var listNewsController: ListNewsController? = null
    private var creditsController: CreditsController? = null
    private var listStepDetailsController: ListStepDetailsController? = null

    private var numSteps: NumCompletedAndTotal? = null

    private var stepSelectedByPosition: Int = 1
    private var currentRoute = MainMvpView.Route.Steps
    var routeFromOthersScreens: MainMvpView.Route? = null


    private var unbinder: Unbinder? = null

    constructor(viewNumber: Int) : super(
            when (viewNumber) {
                0 -> NavDrawerMvpView.Item.StepsList
                1 -> NavDrawerMvpView.Item.NewsList
                else -> NavDrawerMvpView.Item.StepsList
            }) {
        when (viewNumber) {
            0 -> currentRoute = MainMvpView.Route.Steps
            1 -> currentRoute = MainMvpView.Route.News
        }
        DiApp.activityComponent.inject(this)
    }

    constructor(bundle: Bundle) : super(bundle) {
        DiApp.activityComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val baseView = super.onCreateView(inflater, container)

        val root: View
        if (!screenDevice.isTablet()) {
            root = inflater.inflate(R.layout.screen_main, baseContainer, true)
            unbinder = ButterKnife.bind(this, root)
        } else {
            root = baseView
            unbinder = ButterKnife.bind(this, root)
        }

        toolbar = root.findViewById(R.id.main_toolbar) as Toolbar

        setupToolbar(toolbar)

        listStepsController = ListStepsController(stepSelectedByPosition)
        listNewsController = ListNewsController()

        if (!screenDevice.isTablet()) {
            viewPager = ButterKnife.findById(root, R.id.main_view_pager)
            tabLayout = ButterKnife.findById(root, R.id.main_tablayout)
            toolbarFl = ButterKnife.findById(root, R.id.main_toolbar_fl)
            toolbarShrunkTv = ButterKnife.findById(root, R.id.main_toolbar_shrunk_tv)
            viewPager?.adapter =
                    MainControllerAdapter(this, listStepsController!!, listNewsController!!)
            setupTabLayout()
        } else {
            creditsController = CreditsController()
            listStepDetailsController = ListStepDetailsController(stepSelectedByPosition)

            mainContainer = ButterKnife.findById(root, R.id.base_container)
            detailContainer = ButterKnife.findById(root, R.id.detail_container)
            containersLl = ButterKnife.findById(root, R.id.main_containers_ll)
        }


        mainPresenter.bindView(mvpView)
        mainPresenter.onResume()

        listStepsController?.stepTouchListener = { mvpView.listenStepSelectionChange(it) }
        listStepsController?.listStepsScrollListener = { mvpView.listenStepsListScroll(it) }

        if (screenDevice.isTablet()) {
            mvpView.listenRouteChange(currentRoute)
        }

        return baseView
    }

    override fun onDestroyView(view: View?) {
        super.onDestroyView(view)
        mainPresenter.onPause()
        unbinder?.unbind()
        listStepsController = null
        listNewsController = null
        creditsController = null
        listStepDetailsController = null
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        outState.putInt(BKEY_STEP_SELECTED, stepSelectedByPosition)
        outState.putString(BKEY_CURRENT_ROUTE, currentRoute.toString())
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)
        stepSelectedByPosition = savedViewState.getInt(BKEY_STEP_SELECTED)
        if (screenDevice.isTablet()) {
            mvpView.listenStepSelectionChange(stepSelectedByPosition)
        }


        currentRoute = MainMvpView.Route.valueOf(savedViewState.getString(BKEY_CURRENT_ROUTE))
        currentRoute = routeFromOthersScreens ?: currentRoute
        routeFromOthersScreens = null
        mvpView.listenRouteChange(currentRoute)
    }

    override fun onRouteFromCredits(route: MainMvpView.Route) {
        routeFromOthersScreens = route
    }

    override fun onRouteFromStepDetail(route: MainMvpView.Route) {
        routeFromOthersScreens = route
    }

    @OnClick(R.id.main_fab)
    fun fabClicked() {
        val body = activity.getString(R.string.share_main_text,
                numSteps?.completed ?: 0, numSteps?.total ?: 0,
                activity.getString(R.string.app_play_store_short_url))
        val chooserTitle = activity.getString(R.string.share_main_chooser_header)
        textSharer.shareText(body, chooserTitle)
    }

    private val mvpView = object : MainMvpView() {
        override fun actRouteChange(route: MainMvpView.Route) {
            logDevice.d("Act Route Change $route")
            if (!screenDevice.isTablet()) {
                val viewPage: Int
                when (route) {
                    MainMvpView.Route.Steps -> {
                        viewPage = VIEW_PAGER_STEPS_LIST
                        currentRoute = route
                    }
                    MainMvpView.Route.News -> {
                        viewPage = VIEW_PAGER_NEWS_LIST
                        currentRoute = route
                    }
                    MainMvpView.Route.Credits -> {
                        if (router.getControllerWithTag(TAG_CREDITS) == null) {
                            router.pushController(
                                    RouterTransaction.builder(ScreenCreditsController(
                                            this@ScreenMainController))
                                            .popChangeHandler(VerticalChangeHandler())
                                            .pushChangeHandler(VerticalChangeHandler())
                                            .tag(TAG_CREDITS)
                                            .build())
                        }
                        return
                    }
                    else -> return
                }
                viewPager?.setCurrentItem(viewPage, true)
            } else {
                when (route) {
                    MainMvpView.Route.Steps -> if (childControllers.contains(listStepsController)) return
                    MainMvpView.Route.News -> if (childControllers.contains(listNewsController)) return
                    MainMvpView.Route.Credits -> if (childControllers.contains(creditsController)) return
                    else -> return
                }
                currentRoute = route
                childControllers.forEach { this@ScreenMainController.removeChildController(it) }
                when (route) {
                    MainMvpView.Route.Steps -> {
                        addChildController(ChildControllerTransaction.builder(
                                listStepsController!!, R.id.base_container)
                                .popChangeHandler(HorizontalChangeHandler())
                                .pushChangeHandler(HorizontalChangeHandler())
                                .build())
                        addChildController(ChildControllerTransaction.builder(
                                listStepDetailsController!!, R.id.detail_container)
                                .popChangeHandler(HorizontalChangeHandler())
                                .pushChangeHandler(HorizontalChangeHandler())
                                .build())
                    }
                    MainMvpView.Route.News -> {
                        addChildController(ChildControllerTransaction.builder(
                                listNewsController!!, R.id.base_container)
                                .popChangeHandler(HorizontalChangeHandler())
                                .pushChangeHandler(HorizontalChangeHandler())
                                .build())
                    }
                    MainMvpView.Route.Credits -> {
                        addChildController(ChildControllerTransaction.builder(
                                creditsController!!, R.id.base_container)
                                .popChangeHandler(HorizontalChangeHandler())
                                .pushChangeHandler(HorizontalChangeHandler())
                                .build())
                    }
                    else -> Unit
                }
            }
        }

        override fun renderMode(mode: MainMvpView.Mode) {
            if (screenDevice.isTablet()) {
                when (mode) {
                    MainMvpView.Mode.OnePane -> tmAdjustPanes(false)
                    MainMvpView.Mode.TwoPane -> tmAdjustPanes(true)
                }
            }
        }

        override fun renderStepSelectedByPosition(position: Int) {
            stepSelectedByPosition = position
            if (!screenDevice.isTablet()) {
                router.pushController(RouterTransaction.builder(
                        ScreenStepDetailController(position, this@ScreenMainController))
                        .popChangeHandler(VerticalChangeHandler())
                        .pushChangeHandler(VerticalChangeHandler())
                        .build())
            } else {
                listStepDetailsController?.renderStepByPosition(position)
                listStepsController?.renderSelectedStepByPosition(position)
            }
        }

        override fun renderNumStepsCompletedAndTotal(numbers: NumCompletedAndTotal) {
            toolbarPb.max = numbers.total
            toolbarPb.progress = numbers.completed
            numSteps = numbers
        }

        override fun renderRouteSelection(route: MainMvpView.Route) {
            when (route) {
                MainMvpView.Route.Steps -> navDrawerPresenter.setItemSelection(NavDrawerMvpView.Item.StepsList)
                MainMvpView.Route.News -> navDrawerPresenter.setItemSelection(NavDrawerMvpView.Item.NewsList)
                MainMvpView.Route.Credits -> navDrawerPresenter.setItemSelection(NavDrawerMvpView.Item.Credits)
                else -> Unit
            }

            if (!screenDevice.isTablet()) {
                val toolbarShrunkTitle = when (route) {
                    MainMvpView.Route.Steps -> constStrings.stepsToolbarShrunkTitle
                    MainMvpView.Route.News -> constStrings.newsToolbarShrunkTitle
                    else -> return
                }
                toolbarShrunkTv?.text = toolbarShrunkTitle
            }
        }

        override fun renderSizeState(state: MainMvpView.SizeState) {
            if (!screenDevice.isTablet()) {
                when (state) {
                    MainMvpView.SizeState.Expanded -> expandToolbar()
                    MainMvpView.SizeState.Shrunk -> shrinkToolbar()
                }
            }
        }

    }

    override fun onNavigationRouteRequested(route: MainMvpView.Route) {
        mvpView.listenRouteChange(route)
    }

    private fun tmAdjustPanes(twoPanes: Boolean) {
        val maxContainerSize = containersLl!!.measuredWidth - navView.measuredWidth
        if (maxContainerSize == 0) {
            // if the view is not ready yet, queue it for later :)
            Handler().post { tmAdjustPanes(twoPanes) }
            return
        }
        val mainContainerTarget = if (twoPanes) maxContainerSize / 2 else maxContainerSize
        val initialWidth = mainContainer!!.layoutParams.width
        val anim = ValueAnimator.ofInt(initialWidth, mainContainerTarget)
        anim.addUpdateListener {
            val oldLayoutParams = mainContainer!!.layoutParams as LinearLayout.LayoutParams
            oldLayoutParams.width = it.animatedValue as Int
            mainContainer!!.layoutParams = oldLayoutParams
        }
        anim.startDelay = 200
        anim.duration = 150
        anim.start()
    }

    private fun setupTabLayout() {
        if (!screenDevice.isTablet()) {
            tabLayout?.addTab(tabLayout?.newTab()!!.setIcon(R.drawable.ic_numbered_list))
            tabLayout?.addTab(tabLayout?.newTab()!!.setIcon(R.drawable.ic_newspaper))
            viewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
            viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float,
                                            positionOffsetPixels: Int) {
                }

                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageSelected(position: Int) {
                    if (position == VIEW_PAGER_STEPS_LIST) {
                        mvpView.listenRouteChange(MainMvpView.Route.Steps)
                    } else {
                        mvpView.listenRouteChange(MainMvpView.Route.News)
                    }
                }
            })
            tabLayout?.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    if (tab!!.position == VIEW_PAGER_STEPS_LIST) {
                        mvpView.listenRouteChange(MainMvpView.Route.Steps)
                    } else {
                        mvpView.listenRouteChange(MainMvpView.Route.News)
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })
        }
    }

    private val toolbarMaxHeightDp = 128
    private val toolbarMinHeightDp = 56
    private fun expandToolbar() {
        if (screenDevice.isTablet()) return
        if (toolbarAnimation != null && toolbarAnimation!!.isRunning) {
            return
        }
        val toolbarMaxHeight = ResourcesUtils.dpToPx(activity, toolbarMaxHeightDp)
        expandOrShrinkToolbar(toolbarMaxHeight)
        changeToolbarExpandedContentAlpha(false)
    }

    private fun shrinkToolbar() {
        if (screenDevice.isTablet()) return
        if (toolbarAnimation != null && toolbarAnimation!!.isRunning) {
            return
        }
        val toolbarMinHeight = ResourcesUtils.dpToPx(activity, toolbarMinHeightDp)
        expandOrShrinkToolbar(toolbarMinHeight)
        changeToolbarExpandedContentAlpha(true)
    }

    private var toolbarAnimation: ValueAnimator? = null
    private fun expandOrShrinkToolbar(targetHeight: Int) {
        if (toolbarAnimation != null && toolbarAnimation!!.isRunning) {
            toolbarAnimation!!.cancel()
        }
        val oldParams = toolbarFl!!.layoutParams
        toolbarAnimation = ValueAnimator.ofInt(oldParams.height, targetHeight)
        toolbarAnimation!!.duration = 200
        toolbarAnimation!!.addUpdateListener {
            val newHeight = it.animatedValue as Int
            oldParams.height = newHeight
            toolbarFl!!.layoutParams = oldParams
        }
        toolbarAnimation!!.interpolator = AccelerateDecelerateInterpolator()
        toolbarAnimation!!.start()
    }

    private var textFadeOutAnim0: ObjectAnimator? = null
    private var textFadeOutAnim1: ObjectAnimator? = null
    private fun changeToolbarExpandedContentAlpha(fadeOut: Boolean) {
        if (screenDevice.isTablet()) return
        val end = when (fadeOut) {
            true -> 0.0f
            false -> 1.0f
        }
        if (textFadeOutAnim0 != null && textFadeOutAnim0!!.isRunning) {
            textFadeOutAnim0!!.cancel()
        }
        if (textFadeOutAnim1 != null && textFadeOutAnim1!!.isRunning) {
            textFadeOutAnim1!!.cancel()
        }

        textFadeOutAnim0 = ObjectAnimator.ofFloat(toolbarExpandedTv, "alpha",
                toolbarExpandedTv.alpha, end)
        textFadeOutAnim1 = ObjectAnimator.ofFloat(toolbarShrunkTv!!, "alpha",
                toolbarShrunkTv!!.alpha, 1.0f - end)

        textFadeOutAnim0!!.interpolator = AccelerateDecelerateInterpolator()
        textFadeOutAnim1!!.interpolator = AccelerateDecelerateInterpolator()

        textFadeOutAnim0!!.duration = 200
        textFadeOutAnim1!!.duration = 200

        textFadeOutAnim0!!.start()
        textFadeOutAnim1!!.start()
    }

    private class MainControllerAdapter(
            host: Controller,
            val listStepsController: ListStepsController,
            val listNewsController: ListNewsController) : ControllerPagerAdapter(host) {
        private val controllers = listOf(listStepsController, listNewsController)
        override fun getItem(position: Int): Controller? = controllers[position]
        override fun getCount(): Int = 2
    }

    companion object {
        const val VIEW_PAGER_STEPS_LIST = 0
        const val VIEW_PAGER_NEWS_LIST = 1

        private const val BKEY_STEP_SELECTED = "step_selected"
        private const val BKEY_CURRENT_ROUTE = "main_current_route"

        private const val TAG_CREDITS = "credits_controller"
    }
}


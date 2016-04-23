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
package com.hasegawa.diapp.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.design.widget.TabLayout.OnTabSelectedListener
import android.support.design.widget.TabLayout.Tab
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.R.drawable
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.usecases.GetNumStepsTotalCompletedUseCase
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.domain.usecases.SyncIfNecessaryUseCase
import com.hasegawa.diapp.fragments.CreditsFragment
import com.hasegawa.diapp.fragments.MainFragment
import com.hasegawa.diapp.fragments.MainFragment.OnMainFragmentListener
import com.hasegawa.diapp.fragments.NewsFragment
import com.hasegawa.diapp.fragments.StepDetailFragment
import com.hasegawa.diapp.services.GCMRegistrationService
import com.hasegawa.diapp.utils.ResourcesUtils
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import org.joda.time.DateTime
import rx.Subscriber
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.schedulers.Timestamped
import timber.log.Timber
import java.util.ArrayList

class MainActivity : BaseNavDrawerActivity(), OnMainFragmentListener {

    // Common to tablet and phones. Must have.
    private lateinit var toolbar: Toolbar
    private lateinit var fab: FloatingActionButton
    private lateinit var navView: NavigationView
    private lateinit var toolbarFl: FrameLayout
    private lateinit var toolbarExpandedTv: TextView
    private lateinit var toolbarProgressBar: ProgressBar


    // Only used in phones
    private var drawer: DrawerLayout? = null
    private var mainViewPager: ViewPager? = null
    private var mainTabLayout: TabLayout? = null
    private var toolbarShrunkTv: TextView? = null

    // Only used in tablets
    private var detailFl: FrameLayout? = null


    private var getNumStepsUc: GetNumStepsTotalCompletedUseCase? = null

    private var numberOfSteps = 0
    private var numberOfCompletedSteps = 0

    private var isTablet = false
    private var detailFragment: StepDetailFragment? = null
    private var mainFragment: MainFragment? = null

    private var selectedStepPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.main_toolbar) as Toolbar
        fab = findViewById(R.id.main_fab) as FloatingActionButton
        navView = findViewById(R.id.main_nav_view) as NavigationView
        toolbarFl = findViewById(R.id.main_toolbar_fl) as FrameLayout
        toolbarExpandedTv = findViewById(R.id.main_toolbar_expanded_tv) as TextView
        toolbarProgressBar = findViewById(R.id.main_toolbar_expanded_pb) as ProgressBar

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        if (findViewById(R.id.main_fragment_container) != null) {
            Timber.d("Tablet mode on!")
            isTablet = true
            detailFl = findViewById(R.id.detail_fragment_container) as FrameLayout
        } else {
            Timber.d("Tablet mode off!")
            drawer = findViewById(R.id.main_drawer_layout) as DrawerLayout
            mainViewPager = findViewById(R.id.main_view_pager) as ViewPager
            mainTabLayout = findViewById(R.id.main_tablayout) as TabLayout
            toolbarShrunkTv = findViewById(R.id.main_toolbar_shrunk_tv) as TextView
            val toggle = ActionBarDrawerToggle(
                    this, drawer, toolbar,
                    R.string.nav_drawer_open, R.string.nav_drawer_close)
            drawer?.setDrawerListener(toggle)
            toggle.syncState()

            toolbar.setNavigationOnClickListener { drawer?.openDrawer(GravityCompat.START) }
        }

        navView.setNavigationItemSelectedListener(this)

        fab.setOnClickListener({ launchShareIntent() })

        setupNumberOfStepsUc()

        val gcmRegistrationIntent = Intent(this, GCMRegistrationService::class.java)
        startService(gcmRegistrationIntent)

        if (!isTablet) {
            setupViewPager()
        } else {
            tmSetupMainFragment(savedInstanceState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        getNumStepsUc?.unsubscribe()
    }

    override fun onResume() {
        super.onResume()
        if (!isTablet) {
            updateNavSelection()
        } else {
            tmAdjustPanes(tmShouldBeTwoPanes())
        }
    }

    override fun onBackPressed() {
        if (!isTablet) {
            if (drawer!!.isDrawerOpen(GravityCompat.START)) {
                drawer!!.closeDrawer(GravityCompat.START)
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(SAVED_STATE_SELECTED_STEP_POSITION, selectedStepPosition)
    }


    override fun onItemStepClicked(step: StepEntity) {
        if (!isTablet) {
            StepDetailActivity.launch(this, step)
        } else {
            detailFragment?.stepPosition = step.position
            selectedStepPosition = step.position
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null) {
            mainViewPager?.currentItem = data.getIntExtra(INTENT_VIEW_NUMBER_KEY,
                    VIEW_PAGER_STEPS_LIST)
        }
    }

    private var dyHistory: List<Timestamped<Int>> = ArrayList(100)
    override fun onMainFragmentScroll(dx: Int, dy: Int) {
        if (isTablet) return
        if (dy != 0) {
            val useMoreComplexChangeDetection = false
            if (useMoreComplexChangeDetection) {
                val now = DateTime.now().millis
                (dyHistory as ArrayList).add(Timestamped(now, dy))
                dyHistory = dyHistory.takeLast(20).filter { (now - it.timestampMillis) < 500 }
                val dySum = dyHistory.sumBy { it.value }

                val dyDp = ResourcesUtils.pxToDp(this, dySum)
                if (Math.abs(dyDp) > 32) {
                    if (dyDp < 0) {
                        expandToolbar()
                    } else {
                        shrinkToolbar()
                    }
                }
            } else {
                if (dy < 0) {
                    expandToolbar()
                } else {
                    shrinkToolbar()
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        super.onNavigationItemSelected(item)
        val id = item.itemId
        when (id) {
            R.id.nav_steps_list -> {
                if (!isTablet) {
                    mainViewPager?.setCurrentItem(VIEW_PAGER_STEPS_LIST, true)
                    expandToolbar()
                } else {
                    mainFragment = MainFragment.newInstance(true)
                    detailFragment = StepDetailFragment.newInstance(true, 1)
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.main_fragment_container, mainFragment)
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.detail_fragment_container, detailFragment)
                            .commit()
                    tmAdjustPanes(true)
                }
            }
            R.id.nav_news_list -> {
                if (!isTablet) {
                    mainViewPager?.setCurrentItem(VIEW_PAGER_NEWS_LIST, true)
                    shrinkToolbar()
                } else {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.main_fragment_container, NewsFragment.newInstance(true))
                            .commit()
                    tmAdjustPanes(false)
                    detailFragment = null
                    mainFragment = null
                }
            }
            R.id.nav_credits -> {
                if (!isTablet) {
                    CreditsActivity.launch(this)
                } else {
                    supportFragmentManager.beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.main_fragment_container, CreditsFragment.newInstance())
                            .commit()
                    tmAdjustPanes(false)
                    detailFragment = null
                    mainFragment = null
                }
            }
            R.id.nav_last_update -> {
                if (mainViewPager?.currentItem == VIEW_PAGER_STEPS_LIST) {
                    expandToolbar()
                }
            }
        }

        drawer?.closeDrawer(GravityCompat.START)
        return true
    }


    override fun getNavigationView(): NavigationView {
        return navView
    }

    override fun getSnackBarAnchorView(): View {
        return fab
    }

    private fun tmSetupMainFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            navView.setCheckedItem(R.id.nav_steps_list)
            mainFragment = MainFragment.newInstance(isTablet)
            detailFragment = StepDetailFragment.newInstance(isTablet, -1)
            supportFragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container, mainFragment, TM_MAIN_FRAG_TAG)
                    .add(R.id.detail_fragment_container, detailFragment, TM_DETAIL_FRAG_TAG)
                    .commit()
            tmAdjustPanes(true)
        } else {
            mainFragment = supportFragmentManager.findFragmentById(
                    R.id.main_fragment_container) as MainFragment
            detailFragment = supportFragmentManager.findFragmentById(
                    R.id.detail_fragment_container) as StepDetailFragment

            selectedStepPosition = savedInstanceState.getInt(SAVED_STATE_SELECTED_STEP_POSITION, -2)
            if (selectedStepPosition != -1) {
                mainFragment?.stepPositionToSelect = selectedStepPosition
            }
        }
    }

    private fun setupNumberOfStepsUc() {
        getNumStepsUc = GetNumStepsTotalCompletedUseCase(DiApp.stepsRepository,
                Schedulers.io(), AndroidSchedulers.mainThread())

        getNumStepsUc?.execute(object : Subscriber<NumCompletedAndTotal>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Timber.d(e, "Error fetching number of steps.")
            }

            override fun onNext(t: NumCompletedAndTotal?) {
                if (t != null) {
                    numberOfSteps = t.total
                    numberOfCompletedSteps = t.completed
                    updateToolbarProgressbar()
                }
            }
        })
    }

    private fun launchShareIntent() {
        val shareText = getString(R.string.share_main_text,
                numberOfCompletedSteps, numberOfSteps,
                getString(R.string.app_play_store_short_url))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        intent.type = "text/plain"
        startActivity(
                Intent.createChooser(intent, getString(R.string.share_main_chooser_header))
        )
    }

    private fun setupViewPager() {
        mainViewPager?.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            private val fragmentList = ArrayList<Fragment>()

            init {
                fragmentList.add(MainFragment.newInstance(false))
                fragmentList.add(NewsFragment.newInstance(false))
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getItem(position: Int): Fragment? {
                return fragmentList[position]
            }
        }
        mainTabLayout?.addTab(mainTabLayout?.newTab()!!.setIcon(drawable.ic_numbered_list))
        mainTabLayout?.addTab(mainTabLayout?.newTab()!!.setIcon(drawable.ic_newspaper))

        val viewNumber = intent.getIntExtra(INTENT_VIEW_NUMBER_KEY, -1)
        if (viewNumber != -1) {
            mainViewPager?.setCurrentItem(viewNumber, true)
        } else {
            updateNavSelection()
        }

        mainViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageSelected(position: Int) {
                updateNavSelection()
            }
        })
        mainViewPager?.addOnPageChangeListener(TabLayoutOnPageChangeListener(mainTabLayout))
        mainTabLayout?.setOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabUnselected(tab: Tab?) {
            }

            override fun onTabSelected(tab: Tab?) {
                if (tab!!.position == VIEW_PAGER_STEPS_LIST) {
                    toolbarShrunkTv?.text = getString(R.string.main_toolbar_shrunk_title)
                    expandToolbar()
                } else {
                    toolbarShrunkTv?.text = getString(R.string.news_toolbar_shrunk_title)
                    shrinkToolbar()
                }
                mainViewPager?.setCurrentItem(tab.position, true)
            }

            override fun onTabReselected(tab: Tab?) {
                if (tab!!.position == VIEW_PAGER_STEPS_LIST) {
                    expandToolbar()
                }
            }
        })
    }

    private fun updateNavSelection() {
        if (mainViewPager != null) {
            when (mainViewPager!!.currentItem) {
                VIEW_PAGER_STEPS_LIST -> navView.setCheckedItem(R.id.nav_steps_list)
                VIEW_PAGER_NEWS_LIST -> navView.setCheckedItem(R.id.nav_news_list)
            }
        }
    }

    private fun updateToolbarProgressbar() {
        toolbarProgressBar.max = numberOfSteps
        toolbarProgressBar.progress = numberOfCompletedSteps
    }

    fun tmAdjustPanes(twoPanes: Boolean) {
        val ll = findViewById(R.id.main_containers_ll) as LinearLayout
        val mainContainer = findViewById(R.id.main_fragment_container) as FrameLayout
        val maxContainerSize = ll.measuredWidth - navView.measuredWidth
        if (maxContainerSize == 0) {
            // if the view is not ready yet, queue it for later :)
            Handler().post { tmAdjustPanes(true) }
        }
        val mainContainerTarget = if (twoPanes) maxContainerSize / 2 else maxContainerSize
        val initialWidth = mainContainer.layoutParams.width
        val anim = ValueAnimator.ofInt(initialWidth, mainContainerTarget)
        anim.addUpdateListener {
            val oldLayoutParams = mainContainer.layoutParams as LinearLayout.LayoutParams
            oldLayoutParams.width = it.animatedValue as Int
            mainContainer.layoutParams = oldLayoutParams
        }
        anim.startDelay = 200
        anim.duration = 150
        anim.start()
    }

    fun tmShouldBeTwoPanes(): Boolean {
        val fragment = supportFragmentManager.findFragmentById(R.id.main_fragment_container)
        if (fragment != null) {
            return fragment is MainFragment
        }
        return false
    }

    private val toolbarMaxHeightDp = 128
    private val toolbarMinHeightDp = 56
    private fun expandToolbar() {
        if (isTablet) return
        if (toolbarAnimation != null && toolbarAnimation!!.isRunning) {
            return
        }
        val toolbarMaxHeight = ResourcesUtils.dpToPx(this, toolbarMaxHeightDp)
        expandOrShrinkToolbar(toolbarMaxHeight)
        changeToolbarExpandedContentAlpha(false)
    }

    private fun shrinkToolbar() {
        if (isTablet) return
        if (toolbarAnimation != null && toolbarAnimation!!.isRunning) {
            return
        }
        val toolbarMinHeight = ResourcesUtils.dpToPx(this, toolbarMinHeightDp)
        expandOrShrinkToolbar(toolbarMinHeight)
        changeToolbarExpandedContentAlpha(true)
    }

    private var toolbarAnimation: ValueAnimator? = null
    private fun expandOrShrinkToolbar(targetHeight: Int) {
        if (toolbarAnimation != null && toolbarAnimation!!.isRunning) {
            toolbarAnimation!!.cancel()
        }
        val oldParams = toolbarFl.layoutParams
        toolbarAnimation = ValueAnimator.ofInt(oldParams.height, targetHeight)
        toolbarAnimation!!.duration = 200
        toolbarAnimation!!.addUpdateListener {
            val newHeight = it.animatedValue as Int
            oldParams.height = newHeight
            toolbarFl.layoutParams = oldParams
        }
        toolbarAnimation!!.interpolator = AccelerateDecelerateInterpolator()
        toolbarAnimation!!.start()
    }

    private var textFadeOutAnim0: ObjectAnimator? = null
    private var textFadeOutAnim1: ObjectAnimator? = null
    private fun changeToolbarExpandedContentAlpha(fadeOut: Boolean) {
        if (isTablet) return
        var end = when (fadeOut) {
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

    companion object {
        const val INTENT_VIEW_NUMBER_KEY = "view_number_to_scroll"

        const val VIEW_PAGER_STEPS_LIST = 0
        const val VIEW_PAGER_NEWS_LIST = 1

        const val TM_MAIN_FRAG_TAG = "main_frag_tag"
        const val TM_DETAIL_FRAG_TAG = "detail_frag_tag"
        const val TM_NEWS_FRAG_TAG = "news_frag_tag"

        const val SAVED_STATE_SELECTED_STEP_POSITION = "selected_step_position"
    }
}

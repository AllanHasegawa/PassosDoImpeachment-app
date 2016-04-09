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

import android.app.Activity
import android.content.Intent
import android.database.DataSetObserver
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.adapters.StepDetailFragmentAdapter
import com.hasegawa.diapp.models.DiContract.StepsContract
import com.hasegawa.diapp.models.Step
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import com.pushtorefresh.storio.contentresolver.queries.Query
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

class StepDetailActivity : BaseNavDrawerActivity(), ViewPager.OnPageChangeListener {
    lateinit var drawer: DrawerLayout

    lateinit var positionFl: FrameLayout
    lateinit var positionTv: TextView

    lateinit var toolbarExpandedDateTv: TextView
    lateinit var toolbarExpandedTv: TextView
    lateinit var toolbarExpandedPb: ProgressBar
    lateinit var toolbar: Toolbar
    lateinit var viewPager: ViewPager

    lateinit var fab: FloatingActionButton

    lateinit var navView: NavigationView

    private var numberOfSteps = 0
    private var thisStepNumber = 0
    private var numberOfStepsSubscription: Subscription? = null
    private lateinit var fragmentAdapter: StepDetailFragmentAdapter

    private var step: Step? = null
    private var stepPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_step_detail)

        drawer = findViewById(R.id.detail_drawer_layout) as DrawerLayout
        fab = findViewById(R.id.detail_fab) as FloatingActionButton
        toolbar = findViewById(R.id.detail_toolbar) as Toolbar
        positionFl = findViewById(R.id.view_position_fl) as FrameLayout
        positionTv = findViewById(R.id.view_position_tv) as TextView
        toolbarExpandedDateTv = findViewById(R.id.detail_toolbar_expanded_date_tv) as TextView
        toolbarExpandedTv = findViewById(R.id.detail_toolbar_expanded_tv) as TextView
        toolbarExpandedPb = findViewById(R.id.detail_toolbar_expanded_pb) as ProgressBar
        navView = findViewById(R.id.detail_nav_view) as NavigationView
        viewPager = findViewById(R.id.detail_view_pager) as ViewPager

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.nav_drawer_open, R.string.nav_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        fab.setOnClickListener({ launchShareIntent() })

        if (savedInstanceState != null) {
            stepPosition = savedInstanceState.getInt(INTENT_STEP_POSITION_KEY, 1)
        } else {
            stepPosition = intent.getIntExtra(INTENT_STEP_POSITION_KEY, 1)
        }

        fragmentAdapter = StepDetailFragmentAdapter(supportFragmentManager)
        fragmentAdapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                fragmentAdapter.stepsCache.forEachIndexed {
                    i, step ->
                    if (step.position == stepPosition) {
                        if (viewPager.currentItem == i) {
                            onPageSelected(i)
                        } else {
                            viewPager.currentItem = i
                        }
                    }
                }
            }
        })
        fragmentAdapter.notifyDataSetChanged()

        viewPager.adapter = fragmentAdapter
        viewPager.addOnPageChangeListener(this)


        loadNumberOfSteps()
        updateNavLastUpdateTitle()
    }

    override fun onDestroy() {
        super.onDestroy()
        numberOfStepsSubscription?.unsubscribeIfSubscribed()
        fragmentAdapter.close()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(INTENT_STEP_POSITION_KEY, stepPosition)
    }

    override fun getNavigationView(): NavigationView {
        return navView
    }

    override fun getSnackBarAnchorView(): View {
        return fab
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        super.onNavigationItemSelected(item)

        val id = item.itemId
        when (id) {
            R.id.nav_steps_list -> {
                launchMainActivityViewPage(MainActivity.VIEW_PAGER_STEPS_LIST)
            }
            R.id.nav_news_list -> {
                launchMainActivityViewPage(MainActivity.VIEW_PAGER_NEWS_LIST)
            }
            R.id.nav_credits -> {
                CreditsActivity.launch(this)
            }
        }

        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun launchMainActivityViewPage(page: Int) {
        val data = Intent()
        data.putExtra(MainActivity.INTENT_VIEW_NUMBER_KEY, page)
        setResult(Activity.RESULT_OK, data)
        super.onBackPressed()
    }

    private fun launchShareIntent() {
        val state = when (step!!.completed) {
            true -> getString(R.string.share_step_detail_state_completed)
            false -> getString(R.string.share_step_detail_state_incomplete)
        }
        val shareText =
                getString(R.string.share_step_detail_text,
                        thisStepNumber, numberOfSteps, state,
                        step!!.possibleDate, step!!.title, getString(R.string.app_play_store_short_url))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        intent.type = "text/plain"
        startActivity(
                Intent.createChooser(intent, getString(R.string.share_step_detail_chooser_header))
        )
    }

    private fun updateProgressDisplay() {
        toolbarExpandedPb.max = numberOfSteps
        toolbarExpandedPb.progress = thisStepNumber
        toolbarExpandedTv.text = getString(R.string.step_detail_step_from_to,
                thisStepNumber, numberOfSteps)
    }

    private fun loadNumberOfSteps() {
        numberOfStepsSubscription =
                DiApp.diProvider.get()
                        .numberOfResults()
                        .withQuery(Query.builder().uri(StepsContract.URI).build())
                        .prepare()
                        .asRxObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Int> {
                            override fun onCompleted() {
                            }

                            override fun onNext(t: Int) {
                                numberOfSteps = t
                                updateProgressDisplay()
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error loading number of steps.")
                            }
                        })
    }

    private fun loadStep(t: Step?) {
        step = t
        if (t != null) {
            thisStepNumber = t.position
            toolbarExpandedDateTv.text = t.possibleDate
            val borderId = when (t.completed) {
                true -> R.drawable.border_item_step_number_completed
                false -> R.drawable.border_item_step_number_incomplete
            }
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                positionFl.background = ContextCompat.getDrawable(
                        this@StepDetailActivity, borderId)
            } else {
                positionFl.setBackgroundDrawable(ContextCompat.getDrawable(
                        this@StepDetailActivity, borderId))
            }
            positionTv.text = t.position.toString()
        }
        updateProgressDisplay()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onPageSelected(position: Int) {
        val step = fragmentAdapter.stepFromCache(position)
        loadStep(step)
        stepPosition = step?.position ?: 1
    }

    companion object {
        private val INTENT_STEP_POSITION_KEY = "step_position"

        fun launch(activity: Activity, step: Step) {
            val intent = Intent(activity, StepDetailActivity::class.java)
            if (activity is MainActivity) {
                intent.putExtra(INTENT_STEP_POSITION_KEY, step.position)
                activity.startActivityForResult(intent, 0x42)
            } else {
                throw RuntimeException("Only MainActivity should call this function.")
            }
        }
    }
}

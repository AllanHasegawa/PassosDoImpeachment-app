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
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
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
import com.hasegawa.diapp.fragments.StepDetailFragment
import com.hasegawa.diapp.models.DiContract.StepsContract
import com.hasegawa.diapp.models.Step
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import com.pushtorefresh.storio.contentresolver.queries.Query
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

class StepDetailActivity : BaseNavDrawerActivity() {
    lateinit var drawer: DrawerLayout

    lateinit var positionFl: FrameLayout
    lateinit var positionTv: TextView

    lateinit var toolbarExpandedDateTv: TextView
    lateinit var toolbarExpandedTv: TextView
    lateinit var toolbarExpandedPb: ProgressBar
    lateinit var toolbar: Toolbar

    lateinit var fab: FloatingActionButton

    lateinit var navView: NavigationView

    private var stepDetailFragment: StepDetailFragment? = null

    private var numberOfSteps = 0
    private var thisStepNumber = 0
    private var numberOfStepsSubscription: Subscription? = null

    private var stepSubscription: Subscription? = null

    private var step: Step? = null

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

        if (stepDetailFragment == null) {
            stepDetailFragment = StepDetailFragment.newInstance(false)
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, stepDetailFragment)
                    .commit()
        }

        loadStep()
        loadNumberOfSteps()
        updateNavLastUpdateTitle()
    }

    override fun onDestroy() {
        super.onDestroy()
        stepSubscription?.unsubscribeIfSubscribed()
        numberOfStepsSubscription?.unsubscribeIfSubscribed()
        stepDetailFragment = null
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
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
                        .listOfObjects(Step::class.java)
                        .withQuery(Query.builder().uri(StepsContract.URI).build())
                        .prepare()
                        .asRxObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<List<Step>> {
                            override fun onCompleted() {
                            }

                            override fun onNext(t: List<Step>) {
                                numberOfSteps = t.size
                                updateProgressDisplay()
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error loading number of steps.")
                            }
                        })
    }

    private fun loadStep() {
        val stepId = intent.getStringExtra(INTENT_STEP_ID_KEY)
        stepSubscription =
                DiApp.diProvider.get()
                        .`object`(Step::class.java)
                        .withQuery(Query.builder()
                                .uri(StepsContract.URI)
                                .where("${StepsContract.COL_ID}=?")
                                .whereArgs(stepId).build())
                        .prepare()
                        .asRxObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Step> {
                            override fun onNext(t: Step) {
                                stepDetailFragment?.step = t
                                step = t
                                thisStepNumber = step?.position!!
                                toolbarExpandedDateTv.text = step?.possibleDate
                                val borderId = when (step!!.completed) {
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
                                positionTv.text = step!!.position.toString()
                                updateProgressDisplay()
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error loading step with id $stepId")
                            }

                            override fun onCompleted() {
                            }
                        })
    }

    companion object {
        private val INTENT_STEP_ID_KEY = "stepId"

        fun launch(activity: Activity, step: Step) {
            val intent = Intent(activity, StepDetailActivity::class.java)
            if (activity is MainActivity) {
                intent.putExtra(INTENT_STEP_ID_KEY, step.id)
                activity.startActivityForResult(intent, 0x42)
            } else {
                activity.startActivity(intent)
            }
        }
    }
}

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
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.hasegawa.diapp.R
import com.hasegawa.diapp.fragments.StepDetailFragment
import com.hasegawa.diapp.models.Step
import timber.log.Timber

class StepDetailActivity : BaseNavDrawerActivity() {

    lateinit var drawer: DrawerLayout
    lateinit var toolbar: Toolbar
    lateinit var fab: FloatingActionButton
    lateinit var navView: NavigationView

    private var stepPosition: Int = -1

    private var detailFragment: StepDetailFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_step_detail)

        drawer = findViewById(R.id.detail_drawer_layout) as DrawerLayout
        fab = findViewById(R.id.detail_fab) as FloatingActionButton
        toolbar = findViewById(R.id.detail_toolbar) as Toolbar
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

        if (savedInstanceState == null) {
            stepPosition = intent.getIntExtra(INTENT_STEP_POSITION_KEY, 1)
            Timber.d("Start detail with position $stepPosition")
            detailFragment = StepDetailFragment.newInstance(false, stepPosition)
            supportFragmentManager.beginTransaction()
                    .add(R.id.detail_fragment_container, detailFragment, FRAG_TAG)
                    .commit()
        } else {
            Timber.d("Resume detail with position $stepPosition")
            detailFragment = supportFragmentManager.getFragment(savedInstanceState, FRAG_TAG)
                    as StepDetailFragment
        }

        updateNavLastUpdateTitle()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(INTENT_STEP_POSITION_KEY, stepPosition)
        supportFragmentManager.putFragment(outState, FRAG_TAG, detailFragment)
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
        if (detailFragment?.step == null) return
        val step = detailFragment!!.step!!
        val state = when (step.completed) {
            true -> getString(R.string.share_step_detail_state_completed)
            false -> getString(R.string.share_step_detail_state_incomplete)
        }
        val shareText =
                getString(R.string.share_step_detail_text,
                        step.position, detailFragment!!.totalSteps, state,
                        step.possibleDate, step.title, getString(R.string.app_play_store_short_url))
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        intent.type = "text/plain"
        startActivity(
                Intent.createChooser(intent, getString(R.string.share_step_detail_chooser_header))
        )
    }

    companion object {
        private const val FRAG_TAG = "detail_fragment"

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

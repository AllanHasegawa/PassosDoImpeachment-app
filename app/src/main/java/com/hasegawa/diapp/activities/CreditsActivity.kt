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
import android.net.Uri
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
import com.hasegawa.diapp.R.string

class CreditsActivity : BaseNavDrawerActivity() {

    lateinit var fab: FloatingActionButton
    lateinit var navView: NavigationView
    lateinit var drawer: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        fab = findViewById(R.id.credits_fab) as FloatingActionButton
        navView = findViewById(R.id.credits_nav_view) as NavigationView
        drawer = findViewById(R.id.credits_drawer_layout) as DrawerLayout

        val toolbar = findViewById(R.id.credits_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        fab.setOnClickListener {
            val i = Intent(Intent.ACTION_SENDTO,
                    Uri.fromParts("mailto",
                            getString(R.string.credits_hase_email),
                            null))
            i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.credits_hase_email_subject))
            startActivity(Intent.createChooser(i, getString(R.string.credits_email_chooser_header)))
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, string.nav_drawer_open, string.nav_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        navView.setNavigationItemSelectedListener(this)
        navView.setCheckedItem(R.id.nav_credits)

        updateNavLastUpdateTitle()
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
                launchMainActivity(MainActivity.VIEW_PAGER_STEPS_LIST)
            }
            R.id.nav_news_list -> {
                launchMainActivity(MainActivity.VIEW_PAGER_NEWS_LIST)
            }
        }
        val drawer = findViewById(R.id.credits_drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun launchMainActivity(viewPage: Int) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(MainActivity.INTENT_VIEW_NUMBER_KEY,
                viewPage)
        startActivity(intent)
    }

    companion object {
        fun launch(activity: Activity) {
            val intent = Intent(activity, CreditsActivity::class.java)
            activity.startActivity(intent)
        }
    }
}

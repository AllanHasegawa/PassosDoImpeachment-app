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

import android.app.Activity
import android.app.NotificationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.hasegawa.diapp.R
import com.hasegawa.diapp.adapters.NewsRvAdapter
import com.hasegawa.diapp.syncadapters.SyncAdapter
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator

class NewsFragment : Fragment() {
    lateinit var newsRv: RecyclerView

    private lateinit var newsRvAdapter: NewsRvAdapter

    private var isTablet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isTablet = arguments.getBoolean(ARG_IS_TABLET, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_news, container, false)

        newsRv = root.findViewById(R.id.main_news_rv) as RecyclerView

        newsRv.layoutManager = LinearLayoutManager(context)
        newsRvAdapter = NewsRvAdapter(isTablet)
        newsRv.adapter = newsRvAdapter
        val itemAnimator = SlideInLeftAnimator()
        itemAnimator.setInterpolator(AccelerateDecelerateInterpolator())
        newsRv.itemAnimator = itemAnimator
        newsRv.itemAnimator.addDuration = 300
        newsRv.itemAnimator.removeDuration = 300
        newsRv.itemAnimator.changeDuration = 300
        newsRv.itemAnimator.moveDuration = 300


        val notificationMrg = context
                .getSystemService(Activity.NOTIFICATION_SERVICE) as NotificationManager
        notificationMrg.cancelAll()
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        newsRvAdapter.close()
    }

    override fun onResume() {
        super.onResume()
        SyncAdapter.requestFullSync(activity, false, true)
    }

    companion object {
        const val ARG_IS_TABLET = "is_tablet"
        fun newInstance(isTablet: Boolean): NewsFragment {
            val args = Bundle()
            args.putBoolean(ARG_IS_TABLET, isTablet)
            val fragment = NewsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

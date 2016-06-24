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

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import com.bluelinelabs.conductor.Controller
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.presentation.mvpview.ListNewsMvpView
import com.hasegawa.diapp.presentation.presenters.ListNewsPresenter
import com.hasegawa.diapp.views.ItemImportantNewsView
import javax.inject.Inject

class ListNewsController : Controller() {
    @Inject lateinit var listNewsPresenter: ListNewsPresenter

    @BindView(R.id.main_news_rv)
    lateinit var newsRv: RecyclerView

    lateinit var unbinder: Unbinder

    private var adapter: Adapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        val root = inflater.inflate(R.layout.view_list_news, container, false)

        unbinder = ButterKnife.bind(this, root)

        DiApp.activityComponent.inject(this)
        listNewsPresenter.bindView(mvpView)
        listNewsPresenter.onResume()

        adapter = Adapter(mvpView)
        newsRv.layoutManager = LinearLayoutManager(activity)
        newsRv.adapter = adapter

        return root
    }

    override fun onDestroyView(view: View?) {
        super.onDestroyView(view)
        listNewsPresenter.onPause()
        unbinder.unbind()
        adapter = null
    }

    private val mvpView = object : ListNewsMvpView() {
        override fun renderNews(news: List<ListNewsMvpView.Item>) {
            adapter?.items = news
            adapter?.notifyDataSetChanged()
        }
    }

    class Adapter(val mvpView: ListNewsMvpView) :
            RecyclerView.Adapter<Adapter.ViewHolder>() {
        class ViewHolder(item: View, val mvpView: ListNewsMvpView) :
                RecyclerView.ViewHolder(item) {
            var dateTv: TextView? = null

            init {
                dateTv = item.findViewById(R.id.important_news_date_tv) as TextView?
            }

            fun setNews(news: NewsEntity?) {
                if (news != null && itemView is ItemImportantNewsView) {
                    itemView.news = news
                    itemView.shareBt.setOnClickListener { mvpView.listenShareBtTouch(news) }
                    itemView.linkBt.setOnClickListener { mvpView.listenOpenBtTouch(news) }
                }
            }

            fun setDate(date: String?) {
                dateTv?.text = date ?: ""
            }
        }

        var items = emptyList<ListNewsMvpView.Item>()

        override fun getItemViewType(position: Int): Int {
            return items[position].type
        }

        override fun getItemCount(): Int {
            return items.count()
        }

        override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
            val type = getItemViewType(position)
            when (type) {
                ListNewsMvpView.ITEM_NEWS_TYPE -> holder?.setNews(items[position].news)
                ListNewsMvpView.ITEM_DATE_TYPE -> holder?.setDate(items[position].date)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
            val ctx = parent!!.context
            val inf = { i: Int -> LayoutInflater.from(ctx).inflate(i, parent, false) }
            val view: View
            view = when (viewType) {
                ListNewsMvpView.ITEM_NEWS_TYPE -> ItemImportantNewsView(ctx, null)
                ListNewsMvpView.ITEM_SPACE_TYPE -> inf(R.layout.item_important_news_space)
                ListNewsMvpView.ITEM_MID_SPACE_TYPE -> inf(R.layout.item_important_news_before_date_space)
                ListNewsMvpView.ITEM_DATE_TYPE -> inf(R.layout.item_important_news_date)
                else -> inf(R.layout.item_important_news_before_date_space)
            }
            return ViewHolder(view, mvpView)
        }

    }
}

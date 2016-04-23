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
package com.hasegawa.diapp.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.adapters.NewsRvAdapter.NewsViewHolder
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.domain.usecases.GetNewsUseCase
import com.hasegawa.diapp.utils.DateTimeExtensions
import com.hasegawa.diapp.views.ItemImportantNewsView
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.ArrayList

class NewsRvAdapter(val ctx: Context, val isTablet: Boolean) :
        RecyclerView.Adapter<NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var dateTv: TextView? = null

        init {
            dateTv = view.findViewById(R.id.important_news_date_tv) as TextView?
        }

        fun setNews(news: NewsEntity) {
            (itemView!! as ItemImportantNewsView).importantNews = news
        }

        fun setDate(date: String) {
            dateTv?.text = date
        }
    }

    data class Item(val type: Int, val news: NewsEntity?, val date: String?)

    private var news = ArrayList<Item>()
    private lateinit var getNewsUc: GetNewsUseCase

    init {
        getNewsUc = GetNewsUseCase(DiApp.newsRepository, Schedulers.io(),
                AndroidSchedulers.mainThread())

        getNewsUc.execute(object : Subscriber<List<NewsEntity>>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Timber.d(e, "Error while fetching News from db.")
            }

            override fun onNext(t: List<NewsEntity>?) {
                if (t != null) {
                    val grouped = t.groupBy {
                        val millis = DateTimeExtensions.fromUnixTimestamp(it.date).millis
                        DateUtils.formatDateTime(ctx, millis,
                                DateUtils.FORMAT_SHOW_DATE)
                    }

                    val arr = ArrayList<Item>(t.size + 1)
                    if (!isTablet) {
                        arr.add(Item(TYPE_SPACE, null, null))
                    }

                    grouped.keys.forEachIndexed { i, date ->
                        if (i != 0) {
                            arr.add(Item(TYPE_SPACE_BEFORE_DATE, null, null))
                        }
                        arr.add(Item(TYPE_DATE, null, date))
                        arr.addAll(grouped[date]!!.map { Item(TYPE_NEWS, it, null) })
                    }

                    val items = news.size
                    news.clear()
                    notifyItemRangeRemoved(0, items - 1)

                    news.addAll(arr)
                    notifyItemRangeInserted(0, news.size - 1)
                }
            }
        })
    }

    fun close() {
        getNewsUc.unsubscribe()
        news.clear()
    }

    override fun getItemViewType(position: Int): Int {
        return if (news.size > 0) news[position].type else TYPE_SPACE
    }

    override fun getItemCount(): Int {
        return news.size
    }

    override fun onBindViewHolder(holder: NewsViewHolder?, position: Int) {
        when (getItemViewType(position)) {
            TYPE_NEWS -> holder?.setNews(news[position].news!!)
            TYPE_DATE -> holder?.setDate(news[position].date!!)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): NewsViewHolder? {
        return when (viewType) {
            TYPE_NEWS -> NewsViewHolder(ItemImportantNewsView(parent!!.context, null))
            TYPE_SPACE ->
                NewsViewHolder(LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.item_important_news_space, parent, false))
            TYPE_DATE ->
                NewsViewHolder(LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.item_important_news_date, parent, false))
            TYPE_SPACE_BEFORE_DATE ->
                NewsViewHolder(LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.item_important_news_before_date_space, parent, false))
            else -> throw RuntimeException("Unknown type $viewType")
        }
    }

    companion object {
        const val TYPE_NEWS = 5
        const val TYPE_SPACE = 6
        const val TYPE_DATE = 8
        const val TYPE_SPACE_BEFORE_DATE = 9
    }
}

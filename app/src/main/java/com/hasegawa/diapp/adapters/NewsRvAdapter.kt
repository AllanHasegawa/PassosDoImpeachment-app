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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.adapters.NewsRvAdapter.NewsViewHolder
import com.hasegawa.diapp.models.DiContract.ImportantNewsContract
import com.hasegawa.diapp.models.ImportantNews
import com.hasegawa.diapp.utils.DateTimeExtensions
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import com.hasegawa.diapp.views.ItemImportantNewsView
import com.pushtorefresh.storio.contentresolver.queries.Query
import org.joda.time.format.DateTimeFormat
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.ArrayList

class NewsRvAdapter(val isTablet: Boolean) : RecyclerView.Adapter<NewsViewHolder>() {

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var dateTv: TextView? = null

        init {
            dateTv = view.findViewById(R.id.important_news_date_tv) as TextView?
        }

        fun setNews(news: ImportantNews) {
            (itemView!! as ItemImportantNewsView).importantNews = news
        }

        fun setDate(date: String) {
            dateTv?.text = date
        }
    }

    data class Item(val type: Int, val news: ImportantNews?, val date: String?)

    private var news = ArrayList<Item>()
    private var newsSubscription: Subscription? = null

    init {
        newsSubscription =
                DiApp.diProvider.get()
                        .listOfObjects(ImportantNews::class.java)
                        .withQuery(Query.builder().uri(ImportantNewsContract.URI).build())
                        .prepare()
                        .asRxObservable()
                        .map {
                            it.sortedByDescending { it.date }
                                    .groupBy {
                                        DateTimeExtensions.fromUnixTimestamp(it.date).toString(
                                                DateTimeFormat.forPattern("dd/MM/yyyy")
                                        )
                                    }
                        }
                        .map {
                            val arr = ArrayList<Item>(it.size + 2)
                            if (!isTablet) {
                                arr.add(Item(TYPE_SPACE, null, null))
                            }
                            it.keys.forEachIndexed { i, date ->
                                if (i != 0) {
                                    arr.add(Item(TYPE_SPACE_BEFORE_DATE, null, null))
                                }
                                arr.add(Item(TYPE_DATE, null, date))
                                arr.addAll(it[date]!!.map { Item(TYPE_NEWS, it, null) })
                            }
                            arr
                        }
                        .observeOn(AndroidSchedulers.mainThread())
                        .map {
                            val items = news.size
                            news.clear()
                            notifyItemRangeRemoved(0, items)
                            it
                        }
                        .flatMapIterable { it }
                        .subscribe(object : Observer<Item> {
                            override fun onCompleted() {
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error while fetching News from db.")
                            }

                            override fun onNext(t: Item) {
                                news.add(t)
                                notifyItemInserted(news.size - 1)
                            }
                        })
    }

    fun close() {
        newsSubscription?.unsubscribeIfSubscribed()
        newsSubscription = null
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

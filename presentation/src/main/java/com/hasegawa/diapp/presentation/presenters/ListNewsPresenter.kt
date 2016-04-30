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

package com.hasegawa.diapp.presentation.presenters

import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.devices.*
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.domain.repositories.NewsRepository
import com.hasegawa.diapp.domain.usecases.GetNewsUseCase
import com.hasegawa.diapp.presentation.views.ListNewsMvpView
import rx.Subscriber
import java.util.*
import javax.inject.Inject

class ListNewsPresenter @Inject constructor(
        private val screenDevice: ScreenDevice,
        private val dateDevice: DateDevice,
        private val urlOpener: UrlOpener,
        private val newsSharer: NewsSharer,
        private val logDevice: LogDevice,
        private val newsRepository: NewsRepository,
        private val executionThread: ExecutionThread,
        private val postExecutionThread: PostExecutionThread) :
        Presenter<ListNewsMvpView>() {

    private var getNewsUc: GetNewsUseCase? = null

    override fun onResume() {
        getNewsUc = GetNewsUseCase(newsRepository, executionThread, postExecutionThread)
        getNewsUc?.execute(object : Subscriber<List<NewsEntity>>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.e(e, "Error getting news list.")
            }

            override fun onNext(t: List<NewsEntity>?) {
                if (t == null) {
                    view.renderNews(emptyList())
                } else {
                    view.renderNews(makeNewsList(t))
                }
            }
        })
    }

    override fun onPause() {
        getNewsUc?.unsubscribe()
        getNewsUc = null
    }

    override fun onViewBound() {
        view.viewShareBtTouchListener = { newsSharer.shareNews(it) }
        view.viewOpenBtTouchListener = { urlOpener.openUrl(it.url) }
    }

    private fun makeNewsList(source: List<NewsEntity>): List<ListNewsMvpView.Item> {
        val ret = ArrayList<ListNewsMvpView.Item>(source.size)

        val groups = source.groupBy { dateDevice.timestampToFormattedDate(it.date) }

        if (!screenDevice.isTablet()) {
            ret.add(ListNewsMvpView.Item(ListNewsMvpView.ITEM_SPACE_TYPE))
        }

        groups.keys.forEachIndexed { i, date ->
            if (i != 0) {
                ret.add(ListNewsMvpView.Item(ListNewsMvpView.ITEM_MID_SPACE_TYPE))
            }
            ret.add(ListNewsMvpView.Item(ListNewsMvpView.ITEM_DATE_TYPE, date = date))
            ret.addAll(groups[date]!!.map {
                ListNewsMvpView.Item(ListNewsMvpView.ITEM_NEWS_TYPE, it)
            })
        }

        return ret
    }
}

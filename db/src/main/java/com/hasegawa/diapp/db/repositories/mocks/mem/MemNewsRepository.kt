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

package com.hasegawa.diapp.db.repositories.mocks.mem

import com.hasegawa.diapp.db.utils.IdUtils
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.domain.repositories.NewsRepository
import rx.Observable
import rx.lang.kotlin.BehaviorSubject
import rx.subjects.BehaviorSubject
import java.util.*

class MemNewsRepository : NewsRepository {

    override fun addAllNews(news: List<NewsEntity>): Observable<List<NewsEntity>> {
        news.forEach { it.id = IdUtils.genIdIfNull(it.id) }
        MemNewsRepository.news.putAll(news.map { Pair(it.id!!, it) })
        return Observable.just(news)
    }

    override fun clearNews(): Observable<Int> {
        val s = news.size
        news.clear()
        return Observable.just(s)
    }

    override fun getNews(): Observable<List<NewsEntity>> {
        val s = BehaviorSubject(news.values.filterNotNull())
        subjects.add(s)
        return s.map { it.sortedByDescending { it.date } }
    }

    override fun notifyChange() {
        subjects.forEach { it.onNext(news.values.filterNotNull()) }
    }

    companion object {
        var news = HashMap<String, NewsEntity>()

        private val subjects = mutableListOf<BehaviorSubject<List<NewsEntity>>>()

        fun reset() {
            news.clear()
            subjects.clear()
        }
    }
}

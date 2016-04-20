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

package com.hasegawa.diapp.db

import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderNewsRepository
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.domain.entities.equalsNoId
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.Observable
import rx.schedulers.Schedulers
import java.util.ArrayList
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class ContentProviderNewsRepositoryTest {

    val contentResolver = RuntimeEnvironment.application.contentResolver

    fun db() = ContentProviderNewsRepository(contentResolver)

    fun newsList() = listOf(
            NewsEntity("A", "NewsA", "UrlA", 0, null),
            NewsEntity("B", "NewsB", "UrlB", 2, null),
            NewsEntity("C", "NewsC", "UrlC", 1, null),
            NewsEntity("D", "NewsD", "UrlD", 3, null),
            NewsEntity("E", "NewsE", "UrlE", 4, null),
            NewsEntity("F", "NewsF", "UrlF", 5, null)
    )

    @Test
    fun testGetNewsEmpty() {
        val n = db().getNews().toBlocking().first().size
        Assert.assertEquals(0, n)
    }

    @Test
    fun testAddNews() {
        val inserted = db().addAllNews(newsList()).toBlocking().first()
        Assert.assertThat(inserted, Matchers.containsInAnyOrder(*newsList().toTypedArray()))
    }

    @Test
    fun testAddNewsNoId() {
        val inserted = db().addAllNews(
                newsList().map { it.id = null; it }
        ).toBlocking().first()
        val n = inserted.map { news -> newsList().find { it.equalsNoId(news) } != null }
                .sumBy { if (it) 1 else 0 }
        Assert.assertEquals(newsList().size, n)
    }

    @Test
    fun testGetNewsSorted() {
        db().addAllNews(newsList()).toBlocking().first()
        val news = db().getNews().toBlocking().first()
        Assert.assertEquals(newsList().size, news.size)
        Assert.assertThat(news,
                Matchers.contains(*newsList().sortedByDescending { it.date }.toTypedArray()))
    }

    @Test
    fun testClearNews() {
        db().addAllNews(newsList()).toBlocking().first()

        val cleared = db().clearNews().toBlocking().first()
        Assert.assertEquals(newsList().size, cleared)

        val n = db().getNews().toBlocking().first().size
        Assert.assertEquals(0, n)
    }

    @Test
    fun testNotifyChange() {
        var barrier = CyclicBarrier(2)
        var results = ArrayList<Int>()
        val subscription = db().getNews()
                .take(3)
                .subscribe({
                    results.add(it.size)
                    barrier.await()
                })
        barrier.await(15, TimeUnit.SECONDS)
        barrier.reset()

        // notifyChange will call "onNext" on the same thread calling notifyChange, yeks.
        Observable.fromCallable { db().notifyChange() }
                .delay(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation()).subscribe()
        barrier.await(15, TimeUnit.SECONDS)
        barrier.reset()

        db().addAllNews(newsList()).toBlocking().first()

        // notifyChange will call "onNext" on the same thread calling notifyChange, yeks.
        Observable.fromCallable { db().notifyChange() }
                .delay(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation()).subscribe()
        barrier.await(15, TimeUnit.SECONDS)

        val expected = listOf(0, 0, newsList().size)
        Assert.assertEquals(expected, results)
        Assert.assertEquals(true, subscription.isUnsubscribed)
    }
}


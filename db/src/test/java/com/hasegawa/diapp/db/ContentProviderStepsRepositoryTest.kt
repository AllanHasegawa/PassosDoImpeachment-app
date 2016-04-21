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

import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderStepsRepository
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.entities.StepLinkEntity
import com.hasegawa.diapp.domain.entities.equalsNotId
import com.pushtorefresh.storio.StorIOException
import junit.framework.Assert
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
class ContentProviderStepsRepositoryTest {

    val contentResolver = RuntimeEnvironment.application.contentResolver

    fun db() = ContentProviderStepsRepository(contentResolver)

    fun stepsList() = listOf(
            StepEntity("A", 1, "A", "asdA", true, "1"),
            StepEntity("C", 3, "C", "asdC", true, "3"),
            StepEntity("B", 2, "B", "asdB", true, "2"),
            StepEntity("D", 4, "D", "asdD", true, "4"),
            StepEntity("E", 5, "E", "asdE", true, "5"),
            StepEntity("F", 6, "F", "asdF", false, "6"),
            StepEntity("G", 7, "G", "asdG", false, "7"),
            StepEntity("H", 8, "H", "asdH", false, "8")
    )

    fun stepLinksList() = listOf(
            StepLinkEntity("A", "A", "LinkAA", "UrlAA"),
            StepLinkEntity("B", "A", "LinkAB", "UrlAB"),
            StepLinkEntity("C", "A", "LinkAC", "UrlAC"),
            StepLinkEntity("D", "B", "LinkB", "UrlB"),
            StepLinkEntity("E", "C", "LinkC", "UrlC"),
            StepLinkEntity("F", "H", "LinkHF", "UrlHF"),
            StepLinkEntity("G", "H", "LinkHG", "UrlHG")
    )

    fun stepLinksWithInvalidStepIds() = stepLinksList() +
            listOf(
                    StepLinkEntity("Y", "Y", "Should Be Ignored", "Url Should Be Ignored"),
                    StepLinkEntity("Z", "Z", "Should Be Ignored2", "Url Should Be Ignored2")
            )

    @Test
    fun testEmptyDb() {
        val n = db().getSteps().toBlocking().first().size
        Assert.assertEquals("Empty steps", 0, n)

        val nLinks = db().getStepLinks().toBlocking().first().size
        Assert.assertEquals("Empty links", 0, nLinks)
    }

    @Test
    fun testGetSteps() {
        val stepsAdded = db().addSteps(stepsList()).toBlocking().first()
        Assert.assertNotNull(stepsAdded)

        val steps = db().getSteps().toBlocking().first()
        Assert.assertEquals("Get same size", stepsList().size, steps.size)

        val equals = stepsList().map { mock -> steps.find { it == mock } != null }
                .sumBy { if (it) 1 else 0 }
        Assert.assertEquals("Get same content", stepsList().size, equals)
    }

    @Test
    fun testGetStepById() {
        db().addSteps(stepsList()).toBlocking().first()
        val step = db().getStepById("H").toBlocking().first()
        Assert.assertEquals(stepsList().first { it.id == "H" }, step)
    }

    @Test
    fun testGetStepByPosition() {
        db().addSteps(stepsList()).toBlocking().first()
        val step = db().getStepByPosition(5).toBlocking().first()
        Assert.assertEquals(stepsList().first { it.position == 5 }, step)
    }

    @Test(expected = StorIOException::class)
    fun testAddStepLinksWithNoSteps() {
        db().addStepLinks(stepLinksList()).toBlocking().first()
    }

    @Test
    fun testAddStepLinks() {
        db().addSteps(stepsList()).toBlocking().first()
        val n = db().addStepLinks(stepLinksList()).toBlocking().first()
        Assert.assertEquals(stepLinksList().size, n.size)
    }

    @Test(expected = StorIOException::class)
    fun testAddStepLinksWithInvalidStepId() {
        db().addSteps(stepsList()).toBlocking().first()
        val n = db().addStepLinks(stepLinksWithInvalidStepIds()).toBlocking().first()
        Assert.assertEquals(5, n.size)
    }

    @Test
    fun testGetStepLinks() {
        db().addSteps(stepsList()).toBlocking().first()
        db().addStepLinks(stepLinksList()).toBlocking().first()

        val stepLinks = db().getStepLinks().toBlocking().first()
        Assert.assertEquals("Get same size", stepLinksList().size, stepLinks.size)

        val equals = stepLinksList().map { mock -> stepLinks.find { it == mock } != null }
                .sumBy { if (it) 1 else 0 }
        Assert.assertEquals("Get same content", stepLinksList().size, equals)
    }

    @Test
    fun testGetStepsEmpty() {
        val steps = db().getSteps().toBlocking().first()
        Assert.assertEquals(emptyList<StepEntity>(), steps)
    }

    @Test
    fun testGetStepLinksByStepPosition() {
        db().addSteps(stepsList()).toBlocking().first()
        db().addStepLinks(stepLinksList()).toBlocking().first()

        val stepLinks = db().getStepLinksByStepPosition(1).toBlocking().first()
        Assert.assertEquals(3, stepLinks.size)

        val equals = stepLinks.map { link -> stepLinksList().find { it == link } != null }
                .sumBy { if (it) 1 else 0 }
        Assert.assertEquals(3, equals)
    }

    @Test
    fun testGetStepLinksWithInvalidStepPosition() {
        val links = db().getStepLinksByStepPosition(55).toBlocking().first()
        Assert.assertEquals(emptyList<StepLinkEntity>(), links)
    }

    @Test
    fun testGetStepWithInvalidId() {
        val step = db().getStepById("who are you? :)").toBlocking().first()
        Assert.assertEquals(null, step)
    }

    @Test
    fun testGetStepWithInvalidPosition() {
        val step = db().getStepByPosition(42).toBlocking().first()
        Assert.assertEquals(null, step)
    }

    @Test
    fun testGetNumberOfCompletedSteps() {
        db().addSteps(stepsList()).toBlocking().first()
        val n = db().getNumberOfCompletedSteps().toBlocking().first()
        Assert.assertEquals(5, n)
    }

    @Test
    fun testClearSteps() {
        val initialSize = db().addSteps(stepsList()).toBlocking().first().size
        Assert.assertEquals("initial size", stepsList().size, initialSize)

        val removed = db().clearSteps().toBlocking().first()
        Assert.assertEquals("number removed", initialSize, removed)

        val afterSize = db().getSteps().toBlocking().first().size
        Assert.assertEquals("after size", 0, afterSize)
    }

    @Test
    fun testClearStepLinks() {
        db().addSteps(stepsList()).toBlocking().first()
        val initialSize = db().addStepLinks(stepLinksList()).toBlocking().first().size
        Assert.assertEquals("initial size", stepLinksList().size, initialSize)

        val removed = db().clearStepLinks().toBlocking().first()
        Assert.assertEquals("number removed", initialSize, removed)

        val afterSize = db().getStepLinks().toBlocking().first().size
        Assert.assertEquals("after size", 0, afterSize)
    }

    fun <T> doNotifyChangeTestResults(obs: Observable<List<T>>,
                                      changes: (() -> Unit)): List<Int> {
        var barrier = CyclicBarrier(2)
        var results = ArrayList<Int>()
        obs
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

        changes()

        // notifyChange will call "onNext" on the same thread calling notifyChange, yeks.
        Observable.fromCallable { db().notifyChange() }
                .delay(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation()).subscribe()
        barrier.await(15, TimeUnit.SECONDS)
        return results
    }

    @Test
    fun testNotifyChangeSteps() {
        val results = doNotifyChangeTestResults(db().getSteps(),
                { db().addSteps(stepsList()).toBlocking().first() })
        Assert.assertEquals(listOf(0, 0, stepsList().size), results)
    }

    @Test
    fun testNotifyChangeStepLinks() {
        val results = doNotifyChangeTestResults(db().getStepLinks(),
                {
                    db().addSteps(stepsList()).toBlocking().first();
                    db().addStepLinks(stepLinksList()).toBlocking().first()
                })
        Assert.assertEquals(listOf(0, 0, stepLinksList().size), results)
    }

    @Test
    fun testGetStepsOrdering() {
        db().addSteps(stepsList()).toBlocking().first()
        val steps = db().getSteps().toBlocking().first()
        val equals = stepsList().sortedBy { it.position }
                .mapIndexed { i, stepEntity -> steps[i] == stepEntity }
                .sumBy { if (it) 1 else 0 }
        Assert.assertEquals(stepsList().size, equals)
    }

    @Test
    fun testAddStepsIdCreation() {
        val steps = stepsList().map { it.id = null; it }
        db().addSteps(steps).toBlocking().first()
        val n = db().getSteps().toBlocking().first().map { s ->
            steps.find { it.equalsNotId(s) }
        }.sumBy { if (it != null) 1 else 0 }
        Assert.assertEquals(steps.size, n)
    }

    @Test
    fun testAddStepLinksIdCreation() {
        val links = stepLinksList().map { it.id = null; it }
        db().addSteps(stepsList()).toBlocking().first()
        db().addStepLinks(links).toBlocking().first()
        val n = db().getStepLinks().toBlocking().first().map { s ->
            links.find { it.equalsNotId(s) }
        }.sumBy { if (it != null) 1 else 0 }
        Assert.assertEquals(links.size, n)
    }
}

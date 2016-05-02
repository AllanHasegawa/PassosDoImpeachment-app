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
import com.hasegawa.diapp.domain.repositories.StepsRepository
import com.pushtorefresh.storio.StorIOException
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import rx.Observable
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
open class ContentProviderStepsRepositoryTest {

    val contentResolver = RuntimeEnvironment.application.contentResolver

    open fun db(): StepsRepository = ContentProviderStepsRepository(contentResolver)

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


    fun <T> doNotifyChangeTestResults(obs: Observable<List<T>>,
                                      changes: (() -> Unit)): List<Int> {
        var barrier = CyclicBarrier(2)
        var results = ArrayList<Int>()
        obs.subscribeOn(Schedulers.io())
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
    fun testEmptyDb() {
        val n = db().getSteps().toBlocking().first().size
        assertThat(n, `is`(0))

        val nLinks = db().getStepLinks().toBlocking().first().size
        assertThat(nLinks, `is`(0))
    }

    @Test
    fun testGetSteps() {
        val stepsAdded = db().addSteps(stepsList()).toBlocking().first()
        assertThat(stepsAdded, notNullValue())

        val steps = db().getSteps().toBlocking().first()
        assertThat(steps.size, `is`(stepsList().size))
        assertThat(steps, containsInAnyOrder(*stepsList().toTypedArray()))
    }

    @Test
    fun testGetStepById() {
        db().addSteps(stepsList()).toBlocking().first()
        val step = db().getStepById(stepsList()[2].id!!).toBlocking().first()
        assertThat(step, `is`(stepsList()[2]))
    }

    @Test
    fun testGetStepByPosition() {
        db().addSteps(stepsList()).toBlocking().first()
        val step = db().getStepByPosition(5).toBlocking().first()
        assertThat(step, `is`(stepsList().first { it.position == 5 }))
    }

    @Test(expected = StorIOException::class)
    fun testAddStepLinksWithNoSteps() {
        db().addStepLinks(stepLinksList()).toBlocking().first()
    }

    @Test
    fun addStep() {
        val step = db().addStep(stepsList()[0]).toBlocking().first()
        assertThat(step, `is`(stepsList()[0]))

        val steps = db().getSteps().toBlocking().first()
        assertThat(steps[0], `is`(stepsList()[0]))
    }

    @Test
    fun testAddStepLinks() {
        db().addSteps(stepsList()).toBlocking().first()
        val links = db().addStepLinks(stepLinksList()).toBlocking().first()
        assertThat(links, containsInAnyOrder(*stepLinksList().toTypedArray()))
    }

    @Test(expected = StorIOException::class)
    fun testAddStepLinksWithInvalidStepId() {
        db().addSteps(stepsList()).toBlocking().first()
        db().addStepLinks(stepLinksWithInvalidStepIds()).toBlocking().first()
    }

    @Test
    fun testGetStepLinks() {
        db().addSteps(stepsList()).toBlocking().first()
        db().addStepLinks(stepLinksList()).toBlocking().first()

        val stepLinks = db().getStepLinks().toBlocking().first()
        assertThat(stepLinks.size, `is`(stepLinksList().size))
        assertThat(stepLinks, containsInAnyOrder(*stepLinksList().toTypedArray()))
    }

    @Test
    fun testGetStepsEmpty() {
        val steps = db().getSteps().toBlocking().first()
        assertThat(steps, `is`(emptyList()))
    }

    @Test
    fun testGetStepLinksByStepPosition() {
        db().addSteps(stepsList()).toBlocking().first()
        db().addStepLinks(stepLinksList()).toBlocking().first()

        val stepLinks = db().getStepLinksByStepPosition(1).toBlocking().first()
        assertThat(stepLinks.size, `is`(3))
        assertThat(
                stepLinks, containsInAnyOrder(
                *stepLinksList().filter { it.stepsId == "A" }.toTypedArray()))
    }

    @Test
    fun testGetStepLinksWithInvalidStepPosition() {
        val links = db().getStepLinksByStepPosition(55).toBlocking().first()
        assertThat(links, `is`(emptyList()))
    }

    @Test
    fun testGetStepWithInvalidId() {
        val step = db().getStepById("who are you? :)").toBlocking().first()
        assertThat(step, nullValue())
    }

    @Test
    fun testGetStepWithInvalidPosition() {
        val step = db().getStepByPosition(42).toBlocking().first()
        assertThat(step, nullValue())
    }

    @Test
    fun testGetNumberOfCompletedSteps() {
        db().addSteps(stepsList()).toBlocking().first()
        val n = db().getNumberOfCompletedSteps().toBlocking().first()
        assertThat(n, `is`(5))
    }

    @Test
    fun testGetNumberOfSteps() {
        db().addSteps(stepsList()).toBlocking().first()
        val n = db().getNumberOfSteps().toBlocking().first()
        assertThat(n, `is`(stepsList().size))
    }

    @Test
    fun testClearSteps() {
        val initialSize = db().addSteps(stepsList()).toBlocking().first().size
        assertThat(initialSize, `is`(stepsList().size))

        val removed = db().clearSteps().toBlocking().first()
        assertThat(removed, `is`(initialSize))

        val afterSize = db().getSteps().toBlocking().first().size
        assertThat(afterSize, `is`(0))
    }

    @Test
    fun testClearStepLinks() {
        db().addSteps(stepsList()).toBlocking().first()
        val initialSize = db().addStepLinks(stepLinksList()).toBlocking().first().size
        assertThat(initialSize, `is`(stepLinksList().size))

        val removed = db().clearStepLinks().toBlocking().first()
        assertThat(removed, `is`(initialSize))

        val afterSize = db().getStepLinks().toBlocking().first().size
        assertThat(afterSize, `is`(0))
    }

    @Test
    fun testNotifyChangeSteps() {
        val results = doNotifyChangeTestResults(db().getSteps(),
                { db().addSteps(stepsList()).toBlocking().first() })
        assertThat(results, `is`(listOf(0, 0, stepsList().size)))
    }

    @Test
    fun testNotifyChangeStepLinks() {
        val results = doNotifyChangeTestResults(db().getStepLinks(),
                {
                    db().addSteps(stepsList()).toBlocking().first();
                    db().addStepLinks(stepLinksList()).toBlocking().first()
                })
        assertThat(results, `is`(listOf(0, 0, stepLinksList().size)))
    }

    @Test
    fun testGetStepsOrdering() {
        db().addSteps(stepsList()).toBlocking().first()
        val steps = db().getSteps().toBlocking().first()
        assertThat(steps, `is`(stepsList().sortedBy { it.position }))
    }

    @Test
    fun testAddStepsIdCreation() {
        val steps = stepsList().map { it.id = null; it }
        db().addSteps(steps).toBlocking().first()
        val n = db().getSteps().toBlocking().first().map { s ->
            steps.find { it.equalsNotId(s) }
        }.sumBy { if (it != null) 1 else 0 }
        assertThat(n, `is`(steps.size))
    }

    @Test
    fun testAddStepIdCreation() {
        val step = stepsList()[0]
        step.id = null
        db().addStep(step).toBlocking().first()
        val steps = db().getSteps().toBlocking().first()
        assertThat(steps[0].id, notNullValue())
        assertThat(steps[0].equalsNotId(stepsList()[0]), `is`(true))
    }

    @Test
    fun testAddStepLinksIdCreation() {
        val links = stepLinksList().map { it.id = null; it }
        db().addSteps(stepsList()).toBlocking().first()
        db().addStepLinks(links).toBlocking().first()
        val n = db().getStepLinks().toBlocking().first().map { s ->
            links.find { it.equalsNotId(s) }
        }.sumBy { if (it != null) 1 else 0 }
        assertThat(n, `is`(links.size))
    }
}

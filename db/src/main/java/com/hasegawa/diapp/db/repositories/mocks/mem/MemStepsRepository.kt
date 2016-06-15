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

import com.hasegawa.diapp.db.repositories.copyWithId
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.entities.StepLinkEntity
import com.hasegawa.diapp.domain.repositories.StepsRepository
import rx.Observable
import rx.lang.kotlin.BehaviorSubject
import rx.subjects.BehaviorSubject
import java.util.*

class MemStepsRepository : StepsRepository {

    private fun newStepSubject(): Observable<List<StepEntity>> {
        val s = BehaviorSubject(steps.values.filterNotNull())
        stepsSubjects.add(s)
        return s
    }

    private fun newStepLinksSubject(): Observable<List<StepLinkEntity>> {
        val s = BehaviorSubject(stepLinks.values.filterNotNull())
        stepLinksSubjects.add(s)
        return s
    }

    override fun addStep(step: StepEntity): Observable<StepEntity> {
        val newStep = step.copyWithId()
        steps.put(newStep.id!!, newStep)
        return Observable.just(newStep)
    }

    override fun addStepLinks(links: List<StepLinkEntity>): Observable<List<StepLinkEntity>> {
        val newLinks = links.map { it.copyWithId() }
        stepLinks.putAll(newLinks.map { Pair(it.id!!, it) })
        return Observable.just(newLinks)
    }

    override fun addSteps(steps: List<StepEntity>): Observable<List<StepEntity>> {
        return Observable.just(steps.map { addStep(it) }.map { it.toBlocking().first() })
    }

    override fun clearStepLinks(): Observable<Int> {
        val s = stepLinks.size
        stepLinks.clear()
        return Observable.just(s)
    }

    override fun clearSteps(): Observable<Int> {
        val s = steps.size
        steps.clear()
        return Observable.just(s)
    }

    override fun getNumberOfCompletedSteps(): Observable<Int> {
        return newStepSubject().map { it.filter { it.completed }.size }
    }

    override fun getNumberOfSteps(): Observable<Int> {
        return newStepSubject().map { it.size }
    }

    override fun getStepById(id: String): Observable<StepEntity?> {
        return newStepSubject().map { it.firstOrNull { it.id == id } }
    }

    override fun getStepByPosition(position: Int): Observable<StepEntity?> {
        return newStepSubject().map { it.firstOrNull { it.position == position } }
    }

    override fun getStepLinks(): Observable<List<StepLinkEntity>> {
        return newStepLinksSubject()
    }

    override fun getStepLinksByStepPosition(position: Int): Observable<List<StepLinkEntity>> {
        val stepsByPos = steps.values.filter { it.position == position }
        return newStepLinksSubject().map {
            it.filter {
                link ->
                stepsByPos.filter { it.id == link.stepsId }.isNotEmpty()
            }
        }
    }

    override fun getSteps(): Observable<List<StepEntity>> {
        return newStepSubject()
    }

    override fun notifyChange() {
        stepsSubjects.forEach { it.onNext(steps.values.mapNotNull { it }) }
        stepLinksSubjects.forEach { it.onNext(stepLinks.values.mapNotNull { it }) }
    }

    companion object {
        var steps = HashMap<String, StepEntity>()
        var stepLinks = HashMap<String, StepLinkEntity>()

        private val stepsSubjects = mutableListOf<BehaviorSubject<List<StepEntity>>>()
        private val stepLinksSubjects = mutableListOf<BehaviorSubject<List<StepLinkEntity>>>()

        fun reset() {
            steps.clear()
            stepLinks.clear()
            stepsSubjects.clear()
            stepLinksSubjects.clear()
        }
    }
}

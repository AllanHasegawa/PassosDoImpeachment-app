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

package com.hasegawa.diapp

import com.hasegawa.diapp.db.repositories.mocks.mem.MemStepsRepository
import com.hasegawa.diapp.db.repositories.mocks.mem.MemSyncsRepository
import com.hasegawa.diapp.domain.entities.GCMRegistrationEntity
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.entities.StepLinkEntity
import com.hasegawa.diapp.domain.entities.SyncEntity

object MemMockGen {

    fun resetStepsRepo() {
        MemStepsRepository.reset()
    }

    fun genSteps() {
        val steps = (1..20).map {
            StepEntity("id$it", it, "title$it", "description$it", it < 15, "possible$it")
        }
        MemStepsRepository.steps.putAll(steps.map { Pair(it.id!!, it) })
    }

    fun genStepLinks() {
        val links = (1..15).map {
            StepLinkEntity("lid$it", "id${it % 3}", "ltitle$it", "http://google.com/$it")
        }
        MemStepsRepository.stepLinks.putAll(links.map { Pair(it.id!!, it) })
    }

    fun resetSyncsRepo() {
        MemSyncsRepository.reset()
    }

    fun genSynced() {
        val successSync = SyncEntity("sid1", false, 10, 10)
        MemSyncsRepository.syncs.put(successSync.id!!, successSync)
    }

    fun genRegistered() {
        val successRegistration = GCMRegistrationEntity("tokenABC:)", 10)
        MemSyncsRepository.registrations.put(successRegistration.token, successRegistration)
    }
}

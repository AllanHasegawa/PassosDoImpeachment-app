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

package com.hasegawa.diapp.db.repositories

import com.hasegawa.diapp.db.utils.DateTimeUtils
import com.hasegawa.diapp.db.utils.IdUtils
import com.hasegawa.diapp.domain.entities.*

internal fun GCMMessageEntity.copyWithId() = this.copy(
        id = IdUtils.genIdIfNull(this.id),
        timeCreated = DateTimeUtils.nowIfNull(this.timeCreated))

internal fun GCMRegistrationEntity.copyWithTime() = this.copy(
        timeCreated = DateTimeUtils.nowIfNull(this.timeCreated))

internal fun NewsEntity.copyWithId() = this.copy(
        id = IdUtils.genIdIfNull(this.id))

internal fun StepEntity.copyWithId() = this.copy(
        id = IdUtils.genIdIfNull(this.id))

internal fun StepLinkEntity.copyWithId() = this.copy(
        id = IdUtils.genIdIfNull(this.id))

internal fun SyncEntity.copyWithId() = this.copy(
        id = IdUtils.genIdIfNull(this.id),
        timeCreated = DateTimeUtils.nowIfNull(this.timeCreated),
        timeSynced = DateTimeUtils.nowIfNull(this.timeSynced))


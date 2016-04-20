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

package com.hasegawa.diapp.domain.entities

enum class GCMMessageType(val value: Int) {
    Sync(1),
    NewsNotification(2);

    companion object {
        fun fromValue(value: Int) =
                when (value) {
                    1 -> Sync
                    2 -> NewsNotification
                    else -> throw RuntimeException("Unknown GCMMessageType $value")
                }
    }
}

data class GCMMessageEntity(var id: String? = null,
                            var syncsId: String?,
                            var type: GCMMessageType,
                            var data: String,
                            var timeCreated: Long? = null)

fun GCMMessageEntity.equalsNoId(m: GCMMessageEntity): Boolean {
    return this.syncsId == m.syncsId &&
            this.type == m.type &&
            this.data == m.data &&
            this.timeCreated == m.timeCreated
}

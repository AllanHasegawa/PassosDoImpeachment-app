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


data class StepEntity(val id: String? = null,
                      val position: Int,
                      val title: String,
                      val description: String,
                      val completed: Boolean,
                      val possibleDate: String
)


data class StepWithLinksEntity(val step: StepEntity?, val links: List<StepLinkEntity>)


fun StepEntity.equalsNotId(s: StepEntity): Boolean {
    return this.position == s.position &&
            this.title == s.title &&
            this.description == s.description &&
            this.completed == s.completed &&
            this.possibleDate == s.possibleDate
}


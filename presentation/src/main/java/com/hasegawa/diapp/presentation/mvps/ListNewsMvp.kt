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

package com.hasegawa.diapp.presentation.mvps

import com.hasegawa.diapp.domain.entities.NewsEntity
import com.hasegawa.diapp.presentation.presenters.BasePresenter

object ListNewsMvp {
    const val ITEM_SPACE_TYPE = 2
    const val ITEM_NEWS_TYPE = 3
    const val ITEM_DATE_TYPE = 4
    const val ITEM_MID_SPACE_TYPE = 5

    data class Item(val type: Int, val news: NewsEntity? = null, val date: String? = null)

    interface View {
        fun renderNews(news: List<Item>)
    }

    abstract class Presenter : BasePresenter<View>() {
        abstract fun handleShareBtTouch(newsEntity: NewsEntity)
        abstract fun handleOpenBtTouch(newsEntity: NewsEntity)
    }
}

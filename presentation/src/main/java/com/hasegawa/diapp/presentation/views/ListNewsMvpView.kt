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

package com.hasegawa.diapp.presentation.views

import com.hasegawa.diapp.domain.entities.NewsEntity

abstract class ListNewsMvpView : MvpView {
    var shareBtTouchListener: (NewsEntity) -> Unit = {}
    var openBtTouchListener: (NewsEntity) -> Unit = {}

    data class Item(val type: Int, val news: NewsEntity? = null, val date: String? = null)

    abstract fun renderNews(news: List<Item>)

    companion object {
        const val ITEM_SPACE_TYPE = 2
        const val ITEM_NEWS_TYPE = 3
        const val ITEM_DATE_TYPE = 4
        const val ITEM_MID_SPACE_TYPE = 5
    }
}

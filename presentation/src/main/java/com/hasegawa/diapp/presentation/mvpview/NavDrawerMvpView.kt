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

package com.hasegawa.diapp.presentation.mvpview

abstract class NavDrawerMvpView : MvpView {
    enum class Item {
        StepsList,
        NewsList,
        Credits,
        Feedback,
        OpenSource,
        Update
    }

    enum class DrawerState {
        Opened,
        Closed
    }

    /** Listen for touch events on items. */
    open var listenItemTouch: (item: Item) -> Unit = {}
    /** Listen for drawer state change. */
    open var listenDrawerStateChange: (state: DrawerState) -> Unit = {}


    abstract fun actItemTouched(item: Item)
    abstract fun renderUpdateDateText(text: String)
    abstract fun renderItemSelected(item: Item)
    abstract fun renderOpenedNavView()
    abstract fun renderClosedNavView()
}


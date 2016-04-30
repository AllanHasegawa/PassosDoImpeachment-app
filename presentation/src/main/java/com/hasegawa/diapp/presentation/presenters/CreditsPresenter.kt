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

package com.hasegawa.diapp.presentation.presenters

import com.hasegawa.diapp.domain.devices.TextSharer
import com.hasegawa.diapp.domain.devices.UrlOpener
import com.hasegawa.diapp.presentation.ConstStrings
import com.hasegawa.diapp.presentation.views.CreditsMvpView
import javax.inject.Inject

class CreditsPresenter @Inject constructor(
        private val textSharer: TextSharer,
        private val constStrings: ConstStrings,
        private val urlOpener: UrlOpener) :
        Presenter<CreditsMvpView>() {
    override fun onViewBound() {
        view.haseEmailTouchListener = {
            textSharer.shareTextByEmail(
                    constStrings.creditsHaseEmail,
                    constStrings.creditsHaseEmailSubject,
                    constStrings.creditsEmailChooserTitle)
        }
        view.haseGitHubTouchListener = {
            urlOpener.openUrl(constStrings.creditsHaseGitHubUrl)
        }
    }
}

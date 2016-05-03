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

package com.hasegawa.diapp.di

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.hasegawa.diapp.R
import com.hasegawa.diapp.devices.*
import com.hasegawa.diapp.domain.devices.*
import com.hasegawa.diapp.presentation.ConstStrings
import dagger.Module
import dagger.Provides

@Module
class ActivityModule(val context: AppCompatActivity) {
    @Provides @PerActivity fun activityContext(): Context = context

    @Provides @PerActivity fun activity(): Activity = context

    @Provides @PerActivity fun urlOpener(d: AppUrlOpener): UrlOpener = d

    @Provides @PerActivity fun dateDevice(d: AppDateDevice): DateDevice = d

    @Provides @PerActivity fun textSharer(d: AppTextSharer): TextSharer = d

    @Provides @PerActivity fun newsSharer(d: AppNewsSharer): NewsSharer = d

    @Provides @PerActivity fun screenDevice(d: AppScreenDevice): ScreenDevice = d

    @Provides @PerActivity
    fun constStrings(): ConstStrings {
        val s = { id: Int -> context.getString(id) }
        val cs = object : ConstStrings() {

            override var syncDone: String = s(R.string.sync_done)

            override var appPlayUrl: String = s(R.string.app_play_store_short_url)

            override var newsToolbarShrunkTitle: String = s(R.string.news_toolbar_shrunk_title)
            override var stepsToolbarShrunkTitle: String = s(R.string.main_toolbar_shrunk_title)

            override var navFeedbackUrl: String = s(R.string.app_feedback_url)
            override var navOpenSourceUrl: String = s(R.string.app_opensource_url)
            override var navSyncNeverUpdated: String = s(R.string.sync_never_updated)

            override var creditsEmailChooserTitle: String = s(R.string.credits_email_chooser_header)
            override var creditsHaseEmail: String = s(R.string.credits_hase_email)
            override var creditsHaseEmailSubject: String = s(R.string.credits_hase_email_subject)
            override var creditsHaseGitHubUrl: String = s(R.string.credits_hase_github_url)

            override var stepDetailShareHeader: String = s(R.string.share_step_detail_chooser_header)
            override var stepDetailShareStateCompleted: String = s(R.string.share_step_detail_state_completed)
            override var stepDetailShareStateIncomplete: String = s(R.string.share_step_detail_state_incomplete)

            override fun stepDetailShareBody(position: Int, total: Int, completed: Boolean,
                                             date: String, stepTitle: String): String {
                val state = if (completed) stepDetailShareStateCompleted else stepDetailShareStateIncomplete
                val body = context.getString(R.string.share_step_detail_text, position,
                        total, state, date, stepTitle, appPlayUrl)
                return body
            }
        }
        return cs
    }
}

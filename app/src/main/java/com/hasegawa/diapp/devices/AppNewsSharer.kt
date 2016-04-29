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

package com.hasegawa.diapp.devices

import android.content.Context
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.devices.NewsSharer
import com.hasegawa.diapp.domain.devices.TextSharer
import com.hasegawa.diapp.domain.entities.NewsEntity
import javax.inject.Inject

class AppNewsSharer @Inject constructor(val context: Context, val textSharer: TextSharer) :
        NewsSharer {

    override fun shareNews(news: NewsEntity) {
        val body = context.getString(R.string.share_news_item_text, news.title, news.url)
        textSharer.shareText(body,
                context.getString(R.string.share_news_item_chooser_header))
    }

}

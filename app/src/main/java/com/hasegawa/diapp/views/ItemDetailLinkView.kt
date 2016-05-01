/*******************************************************************************
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
 ******************************************************************************/
package com.hasegawa.diapp.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.entities.StepLinkEntity

class ItemDetailLinkView(ctx: Context, attrSet: AttributeSet?) : FrameLayout(ctx, attrSet) {
    lateinit var titleTv: TextView
    lateinit var urlTv: TextView

    var stepLink: StepLinkEntity? = null
        set(value) {
            field = value
            if (value != null) {
                titleTv.text = value.title
                urlTv.text = value.url
            }
        }

    init {
        inflate(ctx, R.layout.item_details_link, this)

        titleTv = this.findViewById(R.id.detail_link_title_tv) as TextView
        urlTv = this.findViewById(R.id.detail_link_url_tv) as TextView
    }
}

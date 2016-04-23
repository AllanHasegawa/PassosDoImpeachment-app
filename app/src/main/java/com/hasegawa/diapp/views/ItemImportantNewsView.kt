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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.entities.NewsEntity

class ItemImportantNewsView(ctx: Context, attrSet: AttributeSet?) : FrameLayout(ctx, attrSet) {

    lateinit var titleTv: TextView
    lateinit var urlTv: TextView
    lateinit var tldrTv: TextView
    lateinit var shareBt: Button
    lateinit var linkBt: Button

    init {
        inflate(context, R.layout.item_important_news, this)
        onFinishInflate()
        layoutParams = RecyclerView.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        titleTv = this.findViewById(R.id.important_news_title_tv) as TextView
        urlTv = this.findViewById(R.id.important_news_url_tv) as TextView
        tldrTv = this.findViewById(R.id.important_news_tldr_tv) as TextView
        shareBt = this.findViewById(R.id.important_news_share_bt) as Button
        linkBt = this.findViewById(R.id.important_news_open_bt) as Button

        shareBt.setOnClickListener({ launchShareIntent() })

        linkBt.setOnClickListener({ launchLinkIntent() })
    }

    var importantNews: NewsEntity? = null
        set(i) {
            field = i
            if (i != null) {
                titleTv.text = i.title
                urlTv.text = i.url
                if (i.tldr != null) {
                    tldrTv.visibility = VISIBLE
                    tldrTv.text = i.tldr
                } else {
                    tldrTv.visibility = View.GONE
                    tldrTv.text = ""
                }
            }
        }

    private fun launchLinkIntent() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(importantNews!!.url)
        try {
            context.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_invalid_link),
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchShareIntent() {
        val shareText = context.getString(R.string.share_news_item_text,
                importantNews!!.title, importantNews!!.url)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        intent.type = "text/plain"
        try {
            context.startActivity(
                    Intent.createChooser(intent,
                            context.getString(R.string.share_news_item_chooser_header))
            )
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_no_app_to_share),
                    Toast.LENGTH_SHORT).show()
        }
    }
}

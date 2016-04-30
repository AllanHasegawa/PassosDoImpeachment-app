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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.devices.TextSharer
import javax.inject.Inject

class AppTextSharer @Inject constructor(val context: Context) : TextSharer {

    override fun shareText(body: String, chooserTitle: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, body)
        intent.type = "text/plain"
        try {
            if (chooserTitle != null) {
                context.startActivity(Intent.createChooser(intent, chooserTitle))
            } else {
                context.startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_no_app_to_share),
                    Toast.LENGTH_SHORT).show()
        }
    }

    override fun shareTextByEmail(email: String, subject: String, chooserTitle: String?) {
        val i = Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null))
        i.putExtra(Intent.EXTRA_SUBJECT, subject)
        try {
            if (chooserTitle != null) {
                context.startActivity(Intent.createChooser(i, chooserTitle))
            } else {
                context.startActivity(i)
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_no_app_to_share),
                    Toast.LENGTH_SHORT).show()
        }
    }
}

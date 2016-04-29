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
import com.hasegawa.diapp.domain.devices.UrlOpener
import javax.inject.Inject

class AppUrlOpener @Inject constructor(val context: Context) : UrlOpener {
    override fun openUrl(url: String) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        try {
            context.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_invalid_link),
                    Toast.LENGTH_SHORT).show()
        }
    }
}

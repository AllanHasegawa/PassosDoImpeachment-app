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
import android.text.format.DateUtils
import com.hasegawa.diapp.domain.devices.DateDevice
import com.hasegawa.diapp.utils.DateTimeExtensions
import com.hasegawa.diapp.utils.toUnixTimestamp
import org.joda.time.DateTime
import javax.inject.Inject

class AppDateDevice @Inject constructor(val context: Context) : DateDevice {

    override fun nowInTimestamp(): Long {
        return DateTime.now().toUnixTimestamp()
    }

    override fun timestampToFormattedDate(time: Long): String {
        return DateUtils.formatDateTime(context, timestampToMillis(time),
                DateUtils.FORMAT_SHOW_DATE)
    }

    override fun timestampToFormattedDateTime(time: Long): String {
        return DateUtils.formatDateTime(context, timestampToMillis(time),
                DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME)
    }

    private fun timestampToMillis(ts: Long): Long {
        return DateTimeExtensions.fromUnixTimestamp(ts).millis
    }
}

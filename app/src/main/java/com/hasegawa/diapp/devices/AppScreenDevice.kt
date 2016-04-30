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

import android.app.Activity
import android.util.DisplayMetrics
import com.hasegawa.diapp.domain.devices.ScreenDevice
import com.hasegawa.diapp.utils.ResourcesUtils
import javax.inject.Inject

class AppScreenDevice @Inject constructor(val activity: Activity) : ScreenDevice {
    override fun isTablet(): Boolean {
        return getScreenSwDp() >= TABLET_SW_DP
    }

    override fun dpToPx(dp: Int): Int = ResourcesUtils.dpToPx(activity, dp)
    override fun pxToDp(px: Int): Int = ResourcesUtils.pxToDp(activity, px)
    override fun fDpToPx(dp: Float): Float = ResourcesUtils.fDpToPx(activity, dp)
    override fun fPxToDp(px: Float): Float = ResourcesUtils.fPxToDp(activity, px)

    private fun getScreenSwDp(): Float {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val widthDp = displayMetrics.widthPixels / displayMetrics.density
        val heightDp = displayMetrics.heightPixels / displayMetrics.density
        val screenSw = Math.min(widthDp, heightDp)
        return screenSw
    }

    companion object {
        private const val TABLET_SW_DP = 720
    }
}

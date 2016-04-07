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
package com.hasegawa.diapp.utils

import android.content.Context
import java.util.*

object ResourcesUtils {
    fun openRawResourceAsString(context: Context, id: Int): String {
        val str = context.resources.openRawResource(id)
        val s = Scanner(str).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    fun pxToDp(context: Context, px: Int): Int {
        return (px / context.resources.displayMetrics.density + 0.5f).toInt()
    }

    fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density + 0.5f).toInt()
    }

    fun fPxToDp(context: Context, px: Float): Float {
        return px / context.resources.displayMetrics.density
    }

    fun fDpToPx(context: Context, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}


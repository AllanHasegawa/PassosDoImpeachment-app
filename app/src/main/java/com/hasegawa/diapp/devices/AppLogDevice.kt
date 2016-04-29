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

import android.util.Log
import com.hasegawa.diapp.domain.devices.LogDevice
import java.util.regex.Pattern
import javax.inject.Inject

class AppLogDevice @Inject constructor() : LogDevice {

    override fun d(message: String) {
        Log.d(getTag(), message)
    }

    override fun d(t: Throwable?, message: String) {
        Log.d(getTag(), message, t)
    }

    override fun e(message: String) {
        Log.e(getTag(), message)
    }

    override fun e(t: Throwable?, message: String) {
        Log.e(getTag(), message, t)
    }

    override fun i(message: String) {
        Log.i(getTag(), message)
    }

    override fun i(t: Throwable?, message: String) {
        Log.i(getTag(), message, t)
    }

    override fun v(message: String) {
        Log.v(getTag(), message)
    }

    override fun v(t: Throwable?, message: String) {
        Log.v(getTag(), message, t)
    }

    companion object {
        private const val STACK_INDEX = 3
        private val CLASS_NAME_PATTERN = Pattern.compile("(\\$\\d+)+$")

        private fun getTag(): String {
            val stackElements = Throwable().stackTrace
            if (stackElements.size < STACK_INDEX) {
                if (stackElements.isEmpty()) {
                    return "NoClassNameFound"
                } else {
                    return extractClassName(stackElements.last())
                }
            }
            return extractClassName(stackElements[STACK_INDEX])
        }

        private fun extractClassName(se: StackTraceElement): String {
            var className = se.className
            val matcher = CLASS_NAME_PATTERN.matcher(className)
            if (matcher.find()) {
                className = matcher.replaceAll("")
            }
            return className.substringAfterLast(".")
        }
    }
}

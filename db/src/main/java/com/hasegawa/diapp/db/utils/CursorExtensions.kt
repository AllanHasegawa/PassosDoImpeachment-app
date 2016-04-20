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
package com.hasegawa.diapp.db.utils

import android.database.Cursor

fun Cursor.isNullByColmnName(colName: String): Boolean {
    return isNull(getColumnIndexOrThrow(colName))
}

fun Cursor.getStringByColumnName(colName: String): String {
    return getString(getColumnIndexOrThrow(colName))
}

fun Cursor.getIntByColumnName(colName: String): Int {
    return getInt(getColumnIndexOrThrow(colName))
}

fun Cursor.getLongByColumnName(colName: String): Long {
    return getLong(getColumnIndexOrThrow(colName))
}

fun Cursor.getFloatByColumnName(colName: String): Float {
    return getFloat(getColumnIndexOrThrow(colName))
}

fun Cursor.getDoubleByColumnName(colName: String): Double {
    return getDouble(getColumnIndexOrThrow(colName))
}

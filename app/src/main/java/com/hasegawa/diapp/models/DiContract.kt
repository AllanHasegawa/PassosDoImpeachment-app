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
package com.hasegawa.diapp.models

object DiContract {
    const val AUTHORITY = "com.hasegawa.diapp.provider"

    fun buildMimeType(singleRow: Boolean, type: String): String {
        val firstPart = when (singleRow) {
            true -> "vnd.android.cursor.item"
            false -> "vnd.android.cursor.dir"
        }
        val secondPart = "vnd.$AUTHORITY.$type"
        return firstPart + "/" + secondPart
    }

    const val CODE_STEPS_URI_ITEM = 2
    const val CODE_STEPS_URI_DIR = 3
    const val CODE_LINKS_URI_ITEM = 4
    const val CODE_LINKS_URI_DIR = 5
    const val CODE_IMPORTANT_NEWS_URI_ITEM = 6
    const val CODE_IMPORTANT_NEWS_URI_DIR = 7

    object StepsContract {
        const val TABLE_NAME = "steps"

        const val PATH = "steps"
        const val URI = "content://$AUTHORITY/$PATH"

        const val MIME_TYPE = "step"

        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_DESCRIPTION = "description"
        const val COL_POSITION = "position"
        const val COL_COMPLETED = "completed"
        const val COL_POSSIBLE_DATE = "possible_date"

        const val SQL_CREATE_TABLE =
                "create table $TABLE_NAME (" +
                        "$COL_ID text primary key," +
                        "$COL_TITLE text not null," +
                        "$COL_DESCRIPTION text not null," +
                        "$COL_POSITION integer not null," +
                        "$COL_COMPLETED integer not null," +
                        "$COL_POSSIBLE_DATE text not null" +
                        ")"

        const val SQL_DROP_TABLE = "drop table if exists $TABLE_NAME"
    }

    object LinksContract {
        const val TABLE_NAME = "links"

        const val PATH = "links"
        const val URI = "content://$AUTHORITY/$PATH"

        const val MIME_TYPE = "link"

        const val COL_ID = "id"
        const val COL_STEPS_ID = "steps_id"
        const val COL_TITLE = "title"
        const val COL_URL = "url"

        const val SQL_CREATE_TABLE =
                "create table $TABLE_NAME (" +
                        "$COL_ID text primary key," +
                        "$COL_STEPS_ID text not null," +
                        "$COL_TITLE text not null," +
                        "$COL_URL text not null," +
                        "foreign key($COL_STEPS_ID) " +
                        "references ${StepsContract.TABLE_NAME}(${StepsContract.COL_ID})" +
                        ")"

        const val SQL_DROP_TABLE = "drop table if exists $TABLE_NAME"
    }

    object ImportantNewsContract {
        const val TABLE_NAME = "important_news"

        const val PATH = "important_news"
        const val URI = "content://$AUTHORITY/$PATH"

        const val MIME_TYPE = "important_news"

        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_URL = "url"
        const val COL_DATE = "date"
        const val COL_TLDR = "tldr"

        const val SQL_CREATE_TABLE =
                "create table $TABLE_NAME (" +
                        "$COL_ID text primary key," +
                        "$COL_TITLE text not null," +
                        "$COL_URL text not null," +
                        "$COL_DATE integer not null," +
                        "$COL_TLDR text" +
                        ")"

        const val SQL_DROP_TABLE = "drop table if exists $TABLE_NAME"
    }
}

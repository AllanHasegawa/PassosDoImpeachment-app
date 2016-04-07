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

import android.content.ContentValues
import android.database.Cursor
import com.hasegawa.diapp.models.DiContract.ImportantNewsContract
import com.hasegawa.diapp.utils.getLongByColumnName
import com.hasegawa.diapp.utils.getStringByColumnName
import com.hasegawa.diapp.utils.isNullByColmnName
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping
import com.pushtorefresh.storio.contentresolver.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.contentresolver.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery
import com.pushtorefresh.storio.contentresolver.queries.InsertQuery
import com.pushtorefresh.storio.contentresolver.queries.UpdateQuery

class ImportantNews(
        val id: String?,
        val title: String?,
        val url: String?,
        val date: Long,
        val tldr: String? = null
) {
    companion object {
        fun typeMapping() = ContentResolverTypeMapping.builder<ImportantNews>()
                .putResolver(putResolver())
                .getResolver(getResolver())
                .deleteResolver(deleteResolver())
                .build()

        fun getResolver() = object : DefaultGetResolver<ImportantNews>() {
            override fun mapFromCursor(c: Cursor): ImportantNews {
                var tldr: String? =
                        if (c.isNullByColmnName(ImportantNewsContract.COL_TLDR)) null else
                            c.getStringByColumnName(ImportantNewsContract.COL_TLDR)
                return ImportantNews(
                        c.getStringByColumnName(ImportantNewsContract.COL_ID),
                        c.getStringByColumnName(ImportantNewsContract.COL_TITLE),
                        c.getStringByColumnName(ImportantNewsContract.COL_URL),
                        c.getLongByColumnName(ImportantNewsContract.COL_DATE),
                        tldr
                )
            }
        }

        fun putResolver() = object : DefaultPutResolver<ImportantNews>() {
            override fun mapToContentValues(i: ImportantNews): ContentValues {
                return i.toContentValues()
            }

            override fun mapToUpdateQuery(i: ImportantNews): UpdateQuery {
                return UpdateQuery.builder()
                        .uri(ImportantNewsContract.URI)
                        .where("${ImportantNewsContract.COL_ID} = ?")
                        .whereArgs(i.id)
                        .build()
            }

            override fun mapToInsertQuery(`object`: ImportantNews): InsertQuery {
                return InsertQuery.builder()
                        .uri(ImportantNewsContract.URI)
                        .build()
            }
        }

        fun deleteResolver() = object : DefaultDeleteResolver<ImportantNews>() {
            override fun mapToDeleteQuery(i: ImportantNews): DeleteQuery {
                return DeleteQuery.builder()
                        .uri(ImportantNewsContract.URI)
                        .where("${ImportantNewsContract.COL_ID} = ?")
                        .whereArgs(i.id)
                        .build()
            }
        }
    }
}

fun ImportantNews.toContentValues(): ContentValues {
    val contentValues = ContentValues()
    contentValues.put(ImportantNewsContract.COL_ID, this.id)
    contentValues.put(ImportantNewsContract.COL_TITLE, this.title)
    contentValues.put(ImportantNewsContract.COL_URL, this.url)
    contentValues.put(ImportantNewsContract.COL_DATE, this.date)
    contentValues.put(ImportantNewsContract.COL_TLDR, this.tldr)
    return contentValues
}
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

package com.hasegawa.diapp.db.repositories.contentprovider.mappings

import android.content.ContentValues
import android.database.Cursor
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.NewsContract
import com.hasegawa.diapp.db.utils.getLongByColumnName
import com.hasegawa.diapp.db.utils.getStringByColumnName
import com.hasegawa.diapp.db.utils.isNullByColmnName
import com.hasegawa.diapp.domain.entities.NewsEntity
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping
import com.pushtorefresh.storio.contentresolver.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.contentresolver.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery
import com.pushtorefresh.storio.contentresolver.queries.InsertQuery
import com.pushtorefresh.storio.contentresolver.queries.UpdateQuery

object NewsEntityMapping {
    fun typeMapping() = ContentResolverTypeMapping.builder<NewsEntity>()
            .putResolver(putResolver())
            .getResolver(getResolver())
            .deleteResolver(deleteResolver())
            .build()

    fun getResolver() = object : DefaultGetResolver<NewsEntity>() {
        override fun mapFromCursor(c: Cursor): NewsEntity {
            var tldr: String? =
                    if (c.isNullByColmnName(NewsContract.COL_TLDR)) null else
                        c.getStringByColumnName(NewsContract.COL_TLDR)
            return NewsEntity(
                    c.getStringByColumnName(NewsContract.COL_ID),
                    c.getStringByColumnName(NewsContract.COL_TITLE),
                    c.getStringByColumnName(NewsContract.COL_URL),
                    c.getLongByColumnName(NewsContract.COL_DATE),
                    tldr
            )
        }
    }

    fun putResolver() = object : DefaultPutResolver<NewsEntity>() {
        override fun mapToContentValues(i: NewsEntity): ContentValues {
            return i.toContentValues()
        }

        override fun mapToUpdateQuery(i: NewsEntity): UpdateQuery {
            return UpdateQuery.builder()
                    .uri(NewsContract.URI)
                    .where("${NewsContract.COL_ID} = ?")
                    .whereArgs(i.id)
                    .build()
        }

        override fun mapToInsertQuery(`object`: NewsEntity): InsertQuery {
            return InsertQuery.builder()
                    .uri(NewsContract.URI)
                    .build()
        }
    }

    fun deleteResolver() = object : DefaultDeleteResolver<NewsEntity>() {
        override fun mapToDeleteQuery(i: NewsEntity): DeleteQuery {
            return DeleteQuery.builder()
                    .uri(NewsContract.URI)
                    .where("${NewsContract.COL_ID} = ?")
                    .whereArgs(i.id)
                    .build()
        }
    }
}

fun NewsEntity.toContentValues(): ContentValues {
    val contentValues = ContentValues()
    contentValues.put(NewsContract.COL_ID, this.id)
    contentValues.put(NewsContract.COL_TITLE, this.title)
    contentValues.put(NewsContract.COL_URL, this.url)
    contentValues.put(NewsContract.COL_DATE, this.date)
    contentValues.put(NewsContract.COL_TLDR, this.tldr)
    return contentValues
}

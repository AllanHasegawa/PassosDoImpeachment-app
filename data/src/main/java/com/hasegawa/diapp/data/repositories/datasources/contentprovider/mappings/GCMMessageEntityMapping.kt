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

package com.hasegawa.diapp.data.repositories.datasources.contentprovider.mappings


import android.content.ContentValues
import android.database.Cursor
import com.hasegawa.diapp.data.models.GCMMessageEntity
import com.hasegawa.diapp.data.repositories.datasources.contentprovider.DiContract.GCMMessagesContract
import com.hasegawa.diapp.data.utils.getIntByColumnName
import com.hasegawa.diapp.data.utils.getLongByColumnName
import com.hasegawa.diapp.data.utils.getStringByColumnName
import com.hasegawa.diapp.data.utils.isNullByColmnName
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping
import com.pushtorefresh.storio.contentresolver.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.contentresolver.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery
import com.pushtorefresh.storio.contentresolver.queries.InsertQuery
import com.pushtorefresh.storio.contentresolver.queries.UpdateQuery

object GCMMessageEntityMapping {
    fun typeMapping() = ContentResolverTypeMapping.builder<GCMMessageEntity>()
            .putResolver(putResolver())
            .getResolver(getResolver())
            .deleteResolver(deleteResolver())
            .build()

    fun getResolver() = object : DefaultGetResolver<GCMMessageEntity>() {
        override fun mapFromCursor(c: Cursor): GCMMessageEntity {
            val syncsId = if (c.isNullByColmnName(GCMMessagesContract.COL_SYNC_ID)) null else
                c.getStringByColumnName(GCMMessagesContract.COL_SYNC_ID)
            return GCMMessageEntity(
                    c.getStringByColumnName(GCMMessagesContract.COL_ID),
                    syncsId,
                    c.getIntByColumnName(GCMMessagesContract.COL_TYPE),
                    c.getStringByColumnName(GCMMessagesContract.COL_DATA),
                    c.getLongByColumnName(GCMMessagesContract.COL_TIME_CREATED)
            )
        }
    }

    fun putResolver() = object : DefaultPutResolver<GCMMessageEntity>() {
        override fun mapToContentValues(s: GCMMessageEntity): ContentValues {
            return s.toContentValues()
        }

        override fun mapToUpdateQuery(s: GCMMessageEntity): UpdateQuery {
            return UpdateQuery.builder()
                    .uri(GCMMessagesContract.URI)
                    .where("${GCMMessagesContract.COL_ID} = ?")
                    .whereArgs(s.id)
                    .build()
        }

        override fun mapToInsertQuery(`object`: GCMMessageEntity): InsertQuery {
            return InsertQuery.builder()
                    .uri(GCMMessagesContract.URI)
                    .build()
        }
    }

    fun deleteResolver() = object : DefaultDeleteResolver<GCMMessageEntity>() {
        override fun mapToDeleteQuery(s: GCMMessageEntity): DeleteQuery {
            return DeleteQuery.builder()
                    .uri(GCMMessagesContract.URI)
                    .where("${GCMMessagesContract.COL_ID} = ?")
                    .whereArgs(s.id)
                    .build()
        }
    }
}

fun GCMMessageEntity.toContentValues(): ContentValues {
    val c = ContentValues()
    c.put(GCMMessagesContract.COL_ID, this.id)
    c.put(GCMMessagesContract.COL_SYNC_ID, this.syncsId)
    c.put(GCMMessagesContract.COL_TYPE, this.type)
    c.put(GCMMessagesContract.COL_DATA, this.data)
    c.put(GCMMessagesContract.COL_TIME_CREATED, this.timeCreated)
    return c
}

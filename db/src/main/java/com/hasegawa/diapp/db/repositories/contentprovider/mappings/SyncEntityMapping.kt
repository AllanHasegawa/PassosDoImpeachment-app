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
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.SyncsContract
import com.hasegawa.diapp.db.utils.getIntByColumnName
import com.hasegawa.diapp.db.utils.getLongByColumnName
import com.hasegawa.diapp.db.utils.getStringByColumnName
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping
import com.pushtorefresh.storio.contentresolver.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.contentresolver.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery
import com.pushtorefresh.storio.contentresolver.queries.InsertQuery
import com.pushtorefresh.storio.contentresolver.queries.UpdateQuery

object SyncEntityMapping {
    fun typeMapping() = ContentResolverTypeMapping.builder<SyncEntity>()
            .putResolver(putResolver())
            .getResolver(getResolver())
            .deleteResolver(deleteResolver())
            .build()

    fun getResolver() = object : DefaultGetResolver<SyncEntity>() {
        override fun mapFromCursor(c: Cursor): SyncEntity {
            return SyncEntity(
                    c.getStringByColumnName(SyncsContract.COL_ID),
                    c.getIntByColumnName(SyncsContract.COL_PENDING) > 0,
                    c.getLongByColumnName(SyncsContract.COL_TIME_SYNCED),
                    c.getLongByColumnName(SyncsContract.COL_TIME_CREATED)
            )
        }
    }

    fun putResolver() = object : DefaultPutResolver<SyncEntity>() {
        override fun mapToContentValues(s: SyncEntity): ContentValues {
            return s.toContentValues()
        }

        override fun mapToUpdateQuery(s: SyncEntity): UpdateQuery {
            return UpdateQuery.builder()
                    .uri(SyncsContract.URI)
                    .where("${SyncsContract.COL_ID} = ?")
                    .whereArgs(s.id)
                    .build()
        }

        override fun mapToInsertQuery(`object`: SyncEntity): InsertQuery {
            return InsertQuery.builder()
                    .uri(SyncsContract.URI)
                    .build()
        }
    }

    fun deleteResolver() = object : DefaultDeleteResolver<SyncEntity>() {
        override fun mapToDeleteQuery(s: SyncEntity): DeleteQuery {
            return DeleteQuery.builder()
                    .uri(SyncsContract.URI)
                    .where("${SyncsContract.COL_ID} = ?")
                    .whereArgs(s.id)
                    .build()
        }
    }
}

fun SyncEntity.toContentValues(): ContentValues {
    val c = ContentValues()
    c.put(SyncsContract.COL_ID, this.id)
    c.put(SyncsContract.COL_PENDING, if (this.pending) 1 else 0)
    c.put(SyncsContract.COL_TIME_SYNCED, this.timeSynced)
    c.put(SyncsContract.COL_TIME_CREATED, this.timeCreated)
    return c
}

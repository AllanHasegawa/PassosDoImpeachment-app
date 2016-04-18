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
import com.hasegawa.diapp.data.models.StepLinkEntity
import com.hasegawa.diapp.data.repositories.datasources.contentprovider.DiContract.LinksContract
import com.hasegawa.diapp.data.utils.getStringByColumnName
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping
import com.pushtorefresh.storio.contentresolver.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.contentresolver.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery
import com.pushtorefresh.storio.contentresolver.queries.InsertQuery
import com.pushtorefresh.storio.contentresolver.queries.UpdateQuery


object StepLinkEntityMapping {
    fun typeMapping() = ContentResolverTypeMapping.builder<StepLinkEntity>()
            .putResolver(putResolver())
            .getResolver(getResolver())
            .deleteResolver(deleteResolver())
            .build()

    fun getResolver() = object : DefaultGetResolver<StepLinkEntity>() {
        override fun mapFromCursor(c: Cursor): StepLinkEntity {
            return StepLinkEntity(
                    c.getStringByColumnName(LinksContract.COL_ID),
                    c.getStringByColumnName(LinksContract.COL_STEPS_ID),
                    c.getStringByColumnName(LinksContract.COL_TITLE),
                    c.getStringByColumnName(LinksContract.COL_URL)
            )
        }
    }

    fun putResolver() = object : DefaultPutResolver<StepLinkEntity>() {
        override fun mapToContentValues(s: StepLinkEntity): ContentValues {
            return s.toContentValues()
        }

        override fun mapToUpdateQuery(s: StepLinkEntity): UpdateQuery {
            return UpdateQuery.builder()
                    .uri(LinksContract.URI)
                    .where("${LinksContract.COL_ID} = ?")
                    .whereArgs(s.id)
                    .build()
        }

        override fun mapToInsertQuery(`object`: StepLinkEntity): InsertQuery {
            return InsertQuery.builder()
                    .uri(LinksContract.URI)
                    .build()
        }
    }

    fun deleteResolver() = object : DefaultDeleteResolver<StepLinkEntity>() {
        override fun mapToDeleteQuery(s: StepLinkEntity): DeleteQuery {
            return DeleteQuery.builder()
                    .uri(LinksContract.URI)
                    .where("${LinksContract.COL_ID} = ?")
                    .whereArgs(s.id)
                    .build()
        }
    }
}

fun StepLinkEntity.toContentValues(): ContentValues {
    val contentValues = ContentValues()
    contentValues.put(LinksContract.COL_ID, this.id)
    contentValues.put(LinksContract.COL_STEPS_ID, this.stepsId)
    contentValues.put(LinksContract.COL_TITLE, this.title)
    contentValues.put(LinksContract.COL_URL, this.url)
    return contentValues
}


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
import com.hasegawa.diapp.models.DiContract.StepsContract
import com.hasegawa.diapp.utils.getIntByColumnName
import com.hasegawa.diapp.utils.getStringByColumnName
import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping
import com.pushtorefresh.storio.contentresolver.operations.delete.DefaultDeleteResolver
import com.pushtorefresh.storio.contentresolver.operations.get.DefaultGetResolver
import com.pushtorefresh.storio.contentresolver.operations.put.DefaultPutResolver
import com.pushtorefresh.storio.contentresolver.queries.DeleteQuery
import com.pushtorefresh.storio.contentresolver.queries.InsertQuery
import com.pushtorefresh.storio.contentresolver.queries.UpdateQuery

class Step(
        var id: String,
        var position: Int,
        var title: String,
        var description: String,
        var completed: Boolean,
        var possibleDate: String
) {
    companion object {
        fun typeMapping() = ContentResolverTypeMapping.builder<Step>()
                .putResolver(putResolver())
                .getResolver(getResolver())
                .deleteResolver(deleteResolver())
                .build()

        fun getResolver() = object : DefaultGetResolver<Step>() {
            override fun mapFromCursor(c: Cursor): Step {
                return Step(
                        c.getStringByColumnName(StepsContract.COL_ID),
                        c.getIntByColumnName(StepsContract.COL_POSITION),
                        c.getStringByColumnName(StepsContract.COL_TITLE),
                        c.getStringByColumnName(StepsContract.COL_DESCRIPTION),
                        c.getIntByColumnName(StepsContract.COL_COMPLETED) > 0,
                        c.getStringByColumnName(StepsContract.COL_POSSIBLE_DATE)
                )
            }
        }

        fun putResolver() = object : DefaultPutResolver<Step>() {
            override fun mapToContentValues(s: Step): ContentValues {
                return s.toContentValues()
            }

            override fun mapToUpdateQuery(s: Step): UpdateQuery {
                return UpdateQuery.builder()
                        .uri(StepsContract.URI)
                        .where("${StepsContract.COL_ID} = ?")
                        .whereArgs(s.id)
                        .build()
            }

            override fun mapToInsertQuery(`object`: Step): InsertQuery {
                return InsertQuery.builder()
                        .uri(StepsContract.URI)
                        .build()
            }
        }

        fun deleteResolver() = object : DefaultDeleteResolver<Step>() {
            override fun mapToDeleteQuery(s: Step): DeleteQuery {
                return DeleteQuery.builder()
                        .uri(StepsContract.URI)
                        .where("${StepsContract.COL_ID} = ?")
                        .whereArgs(s.id)
                        .build()
            }
        }
    }
}

fun Step.toContentValues(): ContentValues {
    val contentValues = ContentValues()
    contentValues.put(StepsContract.COL_ID, this.id)
    contentValues.put(StepsContract.COL_TITLE, this.title)
    contentValues.put(StepsContract.COL_DESCRIPTION, this.description)
    contentValues.put(StepsContract.COL_POSITION, this.position)
    contentValues.put(StepsContract.COL_COMPLETED, this.completed)
    contentValues.put(StepsContract.COL_POSSIBLE_DATE, this.possibleDate)
    return contentValues
}


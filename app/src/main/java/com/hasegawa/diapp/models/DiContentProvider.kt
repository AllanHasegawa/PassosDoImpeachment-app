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

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.models.DiContract.ImportantNewsContract
import com.hasegawa.diapp.models.DiContract.LinksContract
import com.hasegawa.diapp.models.DiContract.StepsContract

class DiContentProvider : ContentProvider() {
    var notifyOnChange = false
    override fun onCreate(): Boolean {
        return true
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val tableName = findTableName(uri!!)
        if (tableName != null) {
            val numberOfAffectedRows = DiApp.db.delete(tableName, selection, selectionArgs)
            if (notifyOnChange && numberOfAffectedRows > 0) {
                context.contentResolver.notifyChange(uri, null)
            }
            return numberOfAffectedRows
        }

        return 0
    }

    override fun query(uri: Uri?,
                       projection: Array<out String>?,
                       selection: String?,
                       selectionArgs: Array<out String>?,
                       sortOrder: String?): Cursor? {
        val tableName = findTableName(uri!!)
        if (tableName != null) {
            return DiApp.db.query(tableName, projection,
                    selection, selectionArgs, null, null, sortOrder)
        }
        return null
    }

    override fun getType(uri: Uri?): String? {
        return when (uriMatcher.match(uri)) {
            DiContract.CODE_STEPS_URI_DIR -> DiContract.buildMimeType(false, StepsContract.MIME_TYPE)
            DiContract.CODE_STEPS_URI_ITEM -> DiContract.buildMimeType(true, StepsContract.MIME_TYPE)
            DiContract.CODE_LINKS_URI_DIR -> DiContract.buildMimeType(false, LinksContract.MIME_TYPE)
            DiContract.CODE_LINKS_URI_ITEM -> DiContract.buildMimeType(true, LinksContract.MIME_TYPE)
            DiContract.CODE_IMPORTANT_NEWS_URI_DIR -> DiContract.buildMimeType(false, ImportantNewsContract.MIME_TYPE)
            DiContract.CODE_IMPORTANT_NEWS_URI_ITEM -> DiContract.buildMimeType(true, ImportantNewsContract.MIME_TYPE)
            else -> null
        }
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        val tableName = findTableName(uri!!)
        if (tableName != null) {
            val insertedId = DiApp.db.insert(tableName, null, values)

            if (notifyOnChange && insertedId != -1L) {
                context.contentResolver.notifyChange(uri, null)
            }

            return ContentUris.withAppendedId(uri, insertedId)
        }
        return null
    }

    override fun update(uri: Uri?, values: ContentValues?,
                        selection: String?, selectionArgs: Array<out String>?): Int {
        val tableName = findTableName(uri!!)
        if (tableName != null) {
            val numberAffectedRows = DiApp.db.update(tableName, values, selection, selectionArgs)
            if (notifyOnChange && numberAffectedRows > 0) {
                context.contentResolver.notifyChange(uri, null)
            }
            return numberAffectedRows
        }
        return 0
    }

    private fun findTableName(uri: Uri): String? {
        val type = uriMatcher.match(uri)

        return when (type) {
            DiContract.CODE_STEPS_URI_DIR -> StepsContract.TABLE_NAME
            DiContract.CODE_STEPS_URI_ITEM -> StepsContract.TABLE_NAME
            DiContract.CODE_LINKS_URI_DIR -> LinksContract.TABLE_NAME
            DiContract.CODE_LINKS_URI_ITEM -> LinksContract.TABLE_NAME
            DiContract.CODE_IMPORTANT_NEWS_URI_DIR -> ImportantNewsContract.TABLE_NAME
            DiContract.CODE_IMPORTANT_NEWS_URI_ITEM -> ImportantNewsContract.TABLE_NAME
            else -> null
        }
    }

    companion object {
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            uriMatcher.addURI(DiContract.AUTHORITY, StepsContract.PATH, DiContract.CODE_STEPS_URI_DIR)
            uriMatcher.addURI(DiContract.AUTHORITY, StepsContract.PATH + "/#", DiContract.CODE_STEPS_URI_ITEM)
            uriMatcher.addURI(DiContract.AUTHORITY, LinksContract.PATH, DiContract.CODE_LINKS_URI_DIR)
            uriMatcher.addURI(DiContract.AUTHORITY, LinksContract.PATH + "/#", DiContract.CODE_LINKS_URI_ITEM)
            uriMatcher.addURI(DiContract.AUTHORITY, ImportantNewsContract.PATH, DiContract.CODE_IMPORTANT_NEWS_URI_DIR)
            uriMatcher.addURI(DiContract.AUTHORITY, LinksContract.PATH + "/#", DiContract.CODE_IMPORTANT_NEWS_URI_ITEM)
        }

    }
}

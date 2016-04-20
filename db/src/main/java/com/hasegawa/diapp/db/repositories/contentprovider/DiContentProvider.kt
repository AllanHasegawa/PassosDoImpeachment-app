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

package com.hasegawa.diapp.db.repositories.contentprovider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.GCMMessagesContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.GCMRegistrationsContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.LinksContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.NewsContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.StepsContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.SyncsContract


class DiContentProvider : ContentProvider() {
    var notifyOnChange = false
    lateinit var dbHelper: DiDbHelper

    override fun onCreate(): Boolean {
        dbHelper = DiDbHelper(context)
        return true
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        val tableName = findTableName(uri!!)
        if (tableName != null) {
            val db = dbHelper.writableDatabase
            val numberOfAffectedRows = db.delete(tableName, selection, selectionArgs)
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
            val db = dbHelper.readableDatabase
            return db.query(tableName, projection,
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

            DiContract.CODE_NEWS_URI_DIR -> DiContract.buildMimeType(false, NewsContract.MIME_TYPE)
            DiContract.CODE_NEWS_URI_ITEM -> DiContract.buildMimeType(true, NewsContract.MIME_TYPE)

            DiContract.CODE_GCM_MESSAGES_URI_DIR -> DiContract.buildMimeType(false, GCMMessagesContract.MIME_TYPE)
            DiContract.CODE_GCM_MESSAGES_URI_ITEM -> DiContract.buildMimeType(true, GCMMessagesContract.MIME_TYPE)

            DiContract.CODE_SYNCS_URI_DIR -> DiContract.buildMimeType(false, SyncsContract.MIME_TYPE)
            DiContract.CODE_SYNCS_URI_ITEM -> DiContract.buildMimeType(true, SyncsContract.MIME_TYPE)

            DiContract.CODE_GCM_REGISTRATIONS_URI_DIR -> DiContract.buildMimeType(false, GCMRegistrationsContract.MIME_TYPE)
            DiContract.CODE_GCM_REGISTRATIONS_URI_ITEM -> DiContract.buildMimeType(true, GCMRegistrationsContract.MIME_TYPE)
            else -> null
        }
    }

    override fun bulkInsert(uri: Uri?, values: Array<out ContentValues>?): Int {
        val tableName = findTableName(uri!!)
        if (tableName != null) {
            var changes = 0
            val db = dbHelper.writableDatabase
            db.beginTransaction()
            for (value in values!!) {
                val insertedId = db.insert(tableName, null, value)
                if (insertedId != -1L) {
                    changes++
                }
            }
            db.endTransaction()

            if (notifyOnChange && changes > 0) {
                context.contentResolver.notifyChange(uri, null)
            }

            return changes
        }
        return 0
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        val tableName = findTableName(uri!!)
        if (tableName != null) {
            val db = dbHelper.writableDatabase
            val insertedId = db.insert(tableName, null, values)

            if (notifyOnChange && insertedId != -1L) {
                context.contentResolver.notifyChange(uri, null)
            }

            if (insertedId == -1L) {
                throw SQLiteException("Failed to insert row into $uri}")
            }

            return ContentUris.withAppendedId(uri, insertedId)
        }
        return null
    }

    override fun update(uri: Uri?, values: ContentValues?,
                        selection: String?, selectionArgs: Array<out String>?): Int {
        val tableName = findTableName(uri!!)
        if (tableName != null) {
            val db = dbHelper.writableDatabase
            val numberAffectedRows = db.update(tableName, values, selection, selectionArgs)
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

            DiContract.CODE_NEWS_URI_DIR -> NewsContract.TABLE_NAME
            DiContract.CODE_NEWS_URI_ITEM -> NewsContract.TABLE_NAME

            DiContract.CODE_GCM_MESSAGES_URI_DIR -> GCMMessagesContract.TABLE_NAME
            DiContract.CODE_GCM_MESSAGES_URI_ITEM -> GCMMessagesContract.TABLE_NAME

            DiContract.CODE_SYNCS_URI_DIR -> SyncsContract.TABLE_NAME
            DiContract.CODE_SYNCS_URI_ITEM -> SyncsContract.TABLE_NAME

            DiContract.CODE_GCM_REGISTRATIONS_URI_DIR -> GCMRegistrationsContract.TABLE_NAME
            DiContract.CODE_GCM_REGISTRATIONS_URI_ITEM -> GCMRegistrationsContract.TABLE_NAME
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

            uriMatcher.addURI(DiContract.AUTHORITY, NewsContract.PATH, DiContract.CODE_NEWS_URI_DIR)
            uriMatcher.addURI(DiContract.AUTHORITY, NewsContract.PATH + "/#", DiContract.CODE_NEWS_URI_ITEM)

            uriMatcher.addURI(DiContract.AUTHORITY, GCMMessagesContract.PATH, DiContract.CODE_GCM_MESSAGES_URI_DIR)
            uriMatcher.addURI(DiContract.AUTHORITY, GCMMessagesContract.PATH + "/#", DiContract.CODE_GCM_MESSAGES_URI_ITEM)

            uriMatcher.addURI(DiContract.AUTHORITY, SyncsContract.PATH, DiContract.CODE_SYNCS_URI_DIR)
            uriMatcher.addURI(DiContract.AUTHORITY, SyncsContract.PATH + "/#", DiContract.CODE_SYNCS_URI_ITEM)

            uriMatcher.addURI(DiContract.AUTHORITY, GCMRegistrationsContract.PATH, DiContract.CODE_GCM_REGISTRATIONS_URI_DIR)
            uriMatcher.addURI(DiContract.AUTHORITY, GCMRegistrationsContract.PATH + "/#", DiContract.CODE_GCM_REGISTRATIONS_URI_ITEM)
        }

    }
}


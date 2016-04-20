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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.GCMMessagesContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.GCMRegistrationsContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.LinksContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.NewsContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.StepsContract
import com.hasegawa.diapp.db.repositories.contentprovider.DiContract.SyncsContract

class DiDbHelper(val ctx: Context) :
        SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(StepsContract.SQL_CREATE_TABLE)
        db.execSQL(LinksContract.SQL_CREATE_TABLE)
        db.execSQL(NewsContract.SQL_CREATE_TABLE)
        db.execSQL(GCMMessagesContract.SQL_CREATE_TABLE)
        db.execSQL(SyncsContract.SQL_CREATE_TABLE)
        db.execSQL(GCMRegistrationsContract.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL(StepsContract.SQL_DROP_TABLE)
        db.execSQL(LinksContract.SQL_DROP_TABLE)
        db.execSQL(NewsContract.SQL_DROP_TABLE)
        db.execSQL(GCMMessagesContract.SQL_DROP_TABLE)
        db.execSQL(SyncsContract.SQL_DROP_TABLE)
        db.execSQL(GCMRegistrationsContract.SQL_DROP_TABLE)
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        if (!db!!.isReadOnly) {
            db.execSQL("PRAGMA foreign_keys=ON;")
        }
    }

    companion object {
        const val DATABASE_NAME = "didb.sqlite"
        const val DATABASE_VERSION = 7
    }
}

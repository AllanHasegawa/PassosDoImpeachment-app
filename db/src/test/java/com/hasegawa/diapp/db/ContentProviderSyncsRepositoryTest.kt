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

package com.hasegawa.diapp.db


import com.hasegawa.diapp.db.repositories.contentprovider.ContentProviderSyncsRepository
import com.hasegawa.diapp.domain.entities.GCMMessageEntity
import com.hasegawa.diapp.domain.entities.GCMMessageType
import com.hasegawa.diapp.domain.entities.GCMRegistrationEntity
import com.hasegawa.diapp.domain.entities.SyncEntity
import com.hasegawa.diapp.domain.entities.equalsNoId
import com.pushtorefresh.storio.StorIOException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Random
import java.util.UUID

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class)
class ContentProviderSyncsRepositoryTest {
    val contentResolver = RuntimeEnvironment.application.contentResolver

    fun db() = ContentProviderSyncsRepository(contentResolver)

    fun gcmRegistrationsList() = listOf(
            GCMRegistrationEntity("tokenA", 0),
            GCMRegistrationEntity("tokenB", 1),
            GCMRegistrationEntity("tokenC", 2)
    )

    fun syncsList() = listOf(
            SyncEntity("A", false, 0, false, 0),
            SyncEntity("B", false, 0, true, 1),
            SyncEntity("C", true, 0, false, 2),
            SyncEntity("D", true, 0, false, 3),
            SyncEntity("E", false, 0, false, 4)
    )

    fun gcmMessagesList() = listOf(
            GCMMessageEntity("A", null, GCMMessageType.NewsNotification, "dataA", 0),
            GCMMessageEntity("B", "A", GCMMessageType.Sync, "dataB", 0),
            GCMMessageEntity("C", "B", GCMMessageType.Sync, "dataC", 0),
            GCMMessageEntity("D", null, GCMMessageType.NewsNotification, "dataD", 0)
    )

    @Test
    fun testGetRegistrationsEmpty() {
        val n = db().getGCMRegistrations().toBlocking().first().size
        Assert.assertEquals(0, n)
    }

    @Test
    fun testUpsertSyncWithNullTimeCreated() {
        var reg = db().upsertSync(SyncEntity(null, false, 10, true, null)).toBlocking().first()
        Assert.assertEquals(true, reg != null)
    }

    @Test
    fun testUpsertSyncsWithNullTimeCreated() {
        val syncs = syncsList().map { it.timeCreated = null; it }
        var reg = db().upsertSyncs(syncs).toBlocking().first()
        Assert.assertEquals(syncsList().size, reg.sumBy { if (it.timeCreated != null) 1 else 0 })
    }

    @Test
    fun testAddRegistration() {
        val reg = gcmRegistrationsList()[0]
        val retReg = db().addGCMRegistration(reg).toBlocking().first()
        Assert.assertEquals(reg, retReg)
    }

    @Test
    fun testAddRegistrationSameToken() {
        val reg = db().addGCMRegistration(gcmRegistrationsList()[0]).toBlocking().first()
        Assert.assertEquals(gcmRegistrationsList()[0], reg)

        val modifiedReg = gcmRegistrationsList()[0]
        modifiedReg.timeCreated = Random().nextLong()
        val newReg = db().addGCMRegistration(modifiedReg).toBlocking().first()
        Assert.assertEquals(reg, newReg)
    }

    @Test
    fun testGetRegistrationByToken() {
        val reg = gcmRegistrationsList()[0]
        db().addGCMRegistration(reg).toBlocking().first()
        val retReg = db().getGCMRegistrationsByToken(reg.token).toBlocking().first()
        Assert.assertEquals(reg, retReg)
    }

    @Test
    fun testGetRegistrationByTokenNotInDb() {
        val reg = gcmRegistrationsList()[0]
        val retReg = db().getGCMRegistrationsByToken(reg.token).toBlocking().first()
        Assert.assertEquals(null, retReg)
    }

    @Test
    fun testGetRegistrations() {
        gcmRegistrationsList().forEach {
            db().addGCMRegistration(it).toBlocking().first()
        }
        val regs = db().getGCMRegistrations().toBlocking().first()
        Assert.assertEquals(gcmRegistrationsList(), regs)
    }

    @Test
    fun testAddRegistrationWithNullTimeCreated() {
        val reg = gcmRegistrationsList()[0]
        reg.timeCreated = null
        val retReg = db().addGCMRegistration(reg).toBlocking().first()
        Assert.assertEquals(true, retReg!!.timeCreated != null)
    }

    @Test
    fun testGetMessagesEmpty() {
        val n = db().getMessages().toBlocking().first().size
        Assert.assertEquals(0, n)
    }

    @Test
    fun testUpsertMessages() {
        val msg = db().upsertMessage(gcmMessagesList()[0]).toBlocking().first()
        Assert.assertEquals(gcmMessagesList()[0], msg)

        val modifiedMsg = gcmMessagesList()[0]
        modifiedMsg.data = UUID.randomUUID().toString()
        modifiedMsg.timeCreated = Random().nextLong()
        modifiedMsg.type = GCMMessageType.fromValue(Random().nextInt(1) + 1)
        val newMsg = db().upsertMessage(modifiedMsg).toBlocking().first()
        Assert.assertEquals(modifiedMsg, newMsg)
    }

    @Test
    fun testInsertMessage() {
        val msg = gcmMessagesList()[0]
        msg.id = null
        val retMsg = db().upsertMessage(msg).toBlocking().first()
        Assert.assertEquals(true, retMsg!!.equalsNoId(msg))
    }

    @Test
    fun testInsertMessageWithNullTimeCreated() {
        val msg = gcmMessagesList()[0]
        msg.timeCreated = null
        val retMsg = db().upsertMessage(msg).toBlocking().first()
        Assert.assertEquals(true, retMsg!!.timeCreated != null)
    }

    @Test(expected = StorIOException::class)
    fun testAddMessagesWithInvalidSyncsId() {
        gcmMessagesList().forEach {
            db().upsertMessage(it).toBlocking().first()
        }
        val msgs = db().getMessages().toBlocking().first()
        Assert.assertEquals(gcmMessagesList(), msgs)
    }

    @Test
    fun testGetMessages() {
        db().upsertSyncs(syncsList()).toBlocking().first()
        gcmMessagesList().forEach {
            db().upsertMessage(it).toBlocking().first()
        }
        val msgs = db().getMessages().toBlocking().first()
        Assert.assertEquals(gcmMessagesList(), msgs)
    }

    @Test
    fun testUpsertSync() {
        val sync = db().upsertSync(syncsList()[0]).toBlocking().first()
        Assert.assertEquals(syncsList()[0], sync)

        val modifiedSync = sync
        modifiedSync!!.pending = Random().nextBoolean()
        modifiedSync.pendingTime = Random().nextLong()
        modifiedSync.success = Random().nextBoolean()
        modifiedSync.timeCreated = Random().nextLong()
        val newSync = db().upsertSync(modifiedSync).toBlocking().first()
        Assert.assertEquals(modifiedSync, newSync)
    }

    @Test
    fun testUpsertSyncs() {
        val syncs = db().upsertSyncs(syncsList()).toBlocking().first()
        assertThat(syncs,
                Matchers.containsInAnyOrder(*syncsList().toTypedArray()))
    }

    @Test
    fun testInsertSync() {
        val sync = syncsList()[0]
        sync.id = null
        val retSync = db().upsertSync(sync).toBlocking().first()
        Assert.assertEquals(true, sync.equalsNoId(retSync!!))
    }

    @Test
    fun testGetPendingSyncs() {
        db().upsertSyncs(syncsList()).toBlocking().first()
        val syncs = db().getPendingSyncs().toBlocking().first()
        Assert.assertEquals(syncsList().filter { it.pending }, syncs)
    }
}

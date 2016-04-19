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

package com.hasegawa.diapp.data


import com.hasegawa.diapp.data.models.GCMMessageEntity
import com.hasegawa.diapp.data.models.GCMRegistrationEntity
import com.hasegawa.diapp.data.models.SyncEntity
import com.hasegawa.diapp.data.models.equalsNoId
import com.hasegawa.diapp.data.repositories.datasources.contentprovider.ContentProviderSyncsRepository
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
            GCMRegistrationEntity("A", "tokenA", false, 0),
            GCMRegistrationEntity("B", null, false, 1),
            GCMRegistrationEntity("C", "tokenC", true, 2)
    )

    fun syncsList() = listOf(
            SyncEntity("A", false, 0, false, 0),
            SyncEntity("B", false, 0, true, 1),
            SyncEntity("C", true, 0, false, 2),
            SyncEntity("D", true, 0, false, 3),
            SyncEntity("E", false, 0, false, 4)
    )

    fun gcmMessagesList() = listOf(
            GCMMessageEntity("A", null, 0, "dataA", 0),
            GCMMessageEntity("B", "A", 0, "dataB", 0),
            GCMMessageEntity("C", "B", 0, "dataC", 0),
            GCMMessageEntity("D", null, 0, "dataD", 0)
    )

    @Test
    fun testGetRegistrationsEmpty() {
        val n = db().getGCMRegistrations().toBlocking().first().size
        Assert.assertEquals(0, n)
    }

    @Test
    fun testUpsertRegistration() {
        val reg = db().upsertGCMRegistration(gcmRegistrationsList()[0]).toBlocking().first()
        Assert.assertEquals(gcmRegistrationsList()[0], reg)

        val modifiedReg = gcmRegistrationsList()[0]
        modifiedReg.success = Random().nextBoolean()
        modifiedReg.timeCreated = Random().nextLong()
        modifiedReg.token = UUID.randomUUID().toString()
        modifiedReg.timeCreated = Random().nextLong()
        val newReg = db().upsertGCMRegistration(modifiedReg).toBlocking().first()
        Assert.assertEquals(modifiedReg, newReg)
    }

    @Test
    fun testAddRegistration() {
        val reg = gcmRegistrationsList()[0]
        reg.id = null
        val retReg = db().upsertGCMRegistration(reg).toBlocking().first()
        Assert.assertEquals(true, reg.equalsNoId(retReg))
    }

    @Test
    fun testGetRegistrations() {
        gcmRegistrationsList().forEach {
            db().upsertGCMRegistration(it).toBlocking().first()
        }
        val regs = db().getGCMRegistrations().toBlocking().first()
        Assert.assertEquals(gcmRegistrationsList(), regs)
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
        modifiedMsg.type = Random().nextInt()
        val newMsg = db().upsertMessage(modifiedMsg).toBlocking().first()
        Assert.assertEquals(modifiedMsg, newMsg)
    }

    @Test
    fun testInsertMessage() {
        val msg = gcmMessagesList()[0]
        msg.id = null
        val retMsg = db().upsertMessage(msg).toBlocking().first()
        Assert.assertEquals(true, retMsg.equalsNoId(msg))
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
        modifiedSync.pending = Random().nextBoolean()
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
        Assert.assertEquals(true, sync.equalsNoId(retSync))
    }

    @Test
    fun testNumberGCMRegistrationSuccessfully() {
        gcmRegistrationsList().forEach {
            db().upsertGCMRegistration(it).toBlocking().first()
        }
        val n = db().getNumberGCMRegistrationsSuccessfully().toBlocking().first()
        Assert.assertEquals(gcmRegistrationsList().filter { it.success }.size, n)
    }

    @Test
    fun testGetPendingSyncs() {
        db().upsertSyncs(syncsList()).toBlocking().first()
        val syncs = db().getPendingSyncs().toBlocking().first()
        Assert.assertEquals(syncsList().filter { it.pending }, syncs)
    }
}

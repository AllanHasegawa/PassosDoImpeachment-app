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

package com.hasegawa.diapp.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout
import android.widget.Toast
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.controllers.ScreenMainController
import com.hasegawa.diapp.di.ActivityModule
import com.hasegawa.diapp.di.DaggerActivityComponent
import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.devices.LogDevice
import com.hasegawa.diapp.domain.devices.SyncScheduler
import com.hasegawa.diapp.domain.repositories.SyncsRepository
import com.hasegawa.diapp.domain.usecases.SyncIfNecessaryUseCase
import com.hasegawa.diapp.presentation.ConstStrings
import com.hasegawa.diapp.services.GCMRegistrationService
import rx.Subscriber
import javax.inject.Inject

class ConductorActivity : AppCompatActivity() {

    private lateinit var router: Router

    @Inject lateinit var logDevice: LogDevice
    @Inject lateinit var syncScheduler: SyncScheduler
    @Inject lateinit var constStrings: ConstStrings
    @Inject lateinit var syncsRepository: SyncsRepository
    @Inject lateinit var executionThread: ExecutionThread
    @Inject lateinit var postExecutionThread: PostExecutionThread

    private var syncIfNeededUc: SyncIfNecessaryUseCase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conductor)

        DiApp.activityComponent = DaggerActivityComponent.builder()
                .activityModule(ActivityModule(this))
                .appComponent(DiApp.appComponent)
                .build()
        DiApp.activityComponent.inject(this)

        val container = findViewById(R.id.container) as FrameLayout

        router = Conductor.attachRouter(this, container, savedInstanceState)
        if (!router.hasRootController()) {
            router.setRoot(ScreenMainController())
        }

        val gcmRegistrationIntent = Intent(this, GCMRegistrationService::class.java)
        startService(gcmRegistrationIntent)

        syncIfNeeded()
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }


    private fun syncIfNeeded() {
        syncIfNeededUc?.unsubscribe()

        syncIfNeededUc = SyncIfNecessaryUseCase(syncScheduler, syncsRepository,
                executionThread, postExecutionThread)

        syncIfNeededUc?.execute(object : Subscriber<Boolean>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                logDevice.d(e, "Error trying to sync if needed.")
            }

            override fun onNext(t: Boolean?) {
                if (t != null && t) {
                    Toast.makeText(this@ConductorActivity, constStrings.syncDone,
                            Toast.LENGTH_LONG).show()
                }
            }
        })
    }
}

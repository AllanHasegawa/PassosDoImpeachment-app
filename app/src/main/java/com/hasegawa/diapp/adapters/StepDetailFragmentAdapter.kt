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
package com.hasegawa.diapp.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.domain.ExecutionThread
import com.hasegawa.diapp.domain.PostExecutionThread
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.usecases.GetStepsUseCase
import com.hasegawa.diapp.fragments.StepDetailSubFragment
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber
import java.util.*

class StepDetailFragmentAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {
    val stepsCache = ArrayList<StepEntity>()
    val getStepsUseCase: GetStepsUseCase

    init {
        getStepsUseCase = GetStepsUseCase(DiApp.stepsRepository,
                ExecutionThread(Schedulers.io()), PostExecutionThread(AndroidSchedulers.mainThread()))
        getStepsUseCase.execute(object : Subscriber<List<StepEntity>>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Timber.d(e, "Problem getting steps")
            }

            override fun onNext(t: List<StepEntity>?) {
                stepsCache.clear()
                stepsCache.addAll(t!!)
                notifyDataSetChanged()
            }
        })
    }

    override fun getItem(position: Int): Fragment? {
        val fragment = StepDetailSubFragment.newInstance(stepsCache[position].position)
        return fragment
    }

    override fun getCount(): Int {
        return stepsCache.size
    }

    fun stepFromCache(position: Int): StepEntity? {
        return stepsCache.getOrNull(position)
    }

    fun close() {
        stepsCache.clear()
        getStepsUseCase.unsubscribe()
    }
}

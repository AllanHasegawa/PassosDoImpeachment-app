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
import com.hasegawa.diapp.fragments.StepDetailSubFragment
import com.hasegawa.diapp.models.DiContract.StepsContract
import com.hasegawa.diapp.models.Step
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import com.pushtorefresh.storio.contentresolver.queries.Query
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.ArrayList

class StepDetailFragmentAdapter(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {
    val stepsCache = ArrayList<Step>()
    private var stepsSubscription: Subscription? = null

    init {
        stepsSubscription =
                DiApp.diProvider.get().listOfObjects(Step::class.java)
                        .withQuery(Query.builder()
                                .uri(StepsContract.URI)
                                .sortOrder("${StepsContract.COL_POSITION}")
                                .build())
                        .prepare()
                        .asRxObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<List<Step>> {
                            override fun onCompleted() {
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error getting list of steps")
                            }

                            override fun onNext(t: List<Step>) {
                                stepsCache.clear()
                                stepsCache.addAll(t)
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

    fun stepFromCache(position: Int): Step? {
        return stepsCache.getOrNull(position)
    }

    fun close() {
        stepsSubscription?.unsubscribeIfSubscribed()
        stepsCache.clear()
    }
}

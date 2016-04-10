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
package com.hasegawa.diapp.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.R.string
import com.hasegawa.diapp.models.DiContract.LinksContract
import com.hasegawa.diapp.models.DiContract.StepsContract
import com.hasegawa.diapp.models.Step
import com.hasegawa.diapp.models.StepLink
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import com.hasegawa.diapp.views.ItemDetailLinkView
import com.pushtorefresh.storio.contentresolver.queries.Query
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber

class StepDetailSubFragment : Fragment() {

    private lateinit var titleTv: TextView
    private lateinit var descriptionTv: TextView
    private lateinit var linksLl: LinearLayout

    private lateinit var positionTv: TextView
    private lateinit var totalStepsTv: TextView
    private lateinit var dateTv: TextView

    private var totalSteps = 0
    private var totalStepsSubscription: Subscription? = null

    var stepPosition: Int = -1
        set(value) {
            field = value
            loadStep()
        }
    private var stepSubscription: Subscription? = null
    private var stepLinkSubscription: Subscription? = null

    private var isTablet = false

    private var step: Step? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            stepPosition = arguments.getInt(ARG_STEP_POSITION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_step_detail_sub, container, false)

        titleTv = root.findViewById(R.id.detail_title_tv) as TextView
        descriptionTv = root.findViewById(R.id.detail_description_tv) as TextView
        positionTv = root.findViewById(R.id.detail_step_number_tv) as TextView
        totalStepsTv = root.findViewById(R.id.detail_step_total_numbers_tv) as TextView
        dateTv = root.findViewById(R.id.detail_date_tv) as TextView
        linksLl = root.findViewById(R.id.detail_links_ll) as LinearLayout

        loadStep()
        loadTotalStepsSubscription()
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        stepLinkSubscription?.unsubscribeIfSubscribed()
        totalStepsSubscription?.unsubscribeIfSubscribed()
        stepSubscription?.unsubscribeIfSubscribed()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(ARG_STEP_POSITION, stepPosition)
    }

    private fun loadTotalStepsSubscription() {
        totalStepsSubscription =
                DiApp.diProvider
                        .get()
                        .numberOfResults()
                        .withQuery(Query.builder().uri(StepsContract.URI).build())
                        .prepare()
                        .asRxObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Int> {
                            override fun onCompleted() {
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error while trying to find total steps.")
                            }

                            override fun onNext(t: Int) {
                                totalSteps = t
                                totalStepsTv.text = t.toString()
                            }
                        })
    }

    private fun loadStep() {
        stepSubscription?.unsubscribeIfSubscribed()
        stepSubscription =
                DiApp.diProvider.get().`object`(Step::class.java)
                        .withQuery(Query.builder().uri(StepsContract.URI)
                                .where("${StepsContract.COL_POSITION}=?")
                                .whereArgs(stepPosition).build())
                        .prepare()
                        .asRxObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Step> {
                            override fun onCompleted() {
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error loading step")
                            }

                            override fun onNext(t: Step?) {
                                step = t
                                loadStepIntoViews()
                            }
                        })
    }

    private fun loadStepIntoViews() {
        if (step == null) return
        positionTv.text = step!!.position.toString()
        dateTv.text = step!!.possibleDate

        titleTv.text = step!!.title
        if (step!!.description.length == 0) {
            descriptionTv.text = getString(string.step_detail_description_empty)
        } else {
            descriptionTv.text = step!!.description
        }

        stepLinkSubscription?.unsubscribeIfSubscribed()
        stepLinkSubscription = DiApp.diProvider.get()
                .listOfObjects(StepLink::class.java)
                .withQuery(Query.builder().uri(LinksContract.URI)
                        .where("${LinksContract.COL_STEPS_ID}=?")
                        .whereArgs(step!!.id)
                        .build())
                .prepare()
                .asRxObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        object : Observer<List<StepLink>> {
                            override fun onCompleted() {
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error getting the step links")
                            }

                            override fun onNext(t: List<StepLink>) {
                                linksLl.removeAllViews()
                                t.map {
                                    val view = ItemDetailLinkView(context, null)
                                    view.stepLink = it
                                    linksLl.addView(view)
                                }
                            }
                        }
                )
    }

    companion object {
        const val ARG_STEP_POSITION = "step_position"
        fun newInstance(stepPosition: Int): StepDetailSubFragment {
            val args = Bundle()
            args.putInt(ARG_STEP_POSITION, stepPosition)
            val fragment = StepDetailSubFragment()
            fragment.arguments = args
            return fragment
        }
    }

}

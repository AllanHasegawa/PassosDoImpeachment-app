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
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.models.DiContract
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

class StepDetailFragment : Fragment() {

    lateinit var titleTv: TextView
    lateinit var descriptionTv: TextView
    lateinit var linksLl: LinearLayout

    var positionFl: FrameLayout? = null
    var positionTv: TextView? = null
    var dateTv: TextView? = null
    var stepPb: ProgressBar? = null
    var stepNumberTv: TextView? = null

    private var totalSteps = 0
    private var totalStepsSubscription: Subscription? = null

    private var stepLinkSubscription: Subscription? = null
    var step: Step? = null
        set(value) {
            field = value
            if (step != null) {

                if (isTablet) {
                    val positionDrawable = when (step!!.completed) {
                        true -> R.drawable.border_item_step_number_completed
                        false -> R.drawable.border_item_step_number_incomplete
                    }
                    if (android.os.Build.VERSION.SDK_INT >= 16) {
                        positionFl?.background = ContextCompat.getDrawable(
                                context, positionDrawable)
                    } else {
                        positionFl?.setBackgroundDrawable(ContextCompat.getDrawable(
                                context, positionDrawable))
                    }
                    positionTv?.text = step!!.position.toString()

                    dateTv?.text = step!!.possibleDate
                    updateProgressBar()
                }

                titleTv.text = step!!.title
                if (step!!.description.length == 0) {
                    descriptionTv.text = getString(R.string.step_detail_description_empty)
                } else {
                    descriptionTv.text = step!!.description
                }

                stepLinkSubscription?.unsubscribeIfSubscribed()
                stepLinkSubscription = DiApp.diProvider.get()
                        .listOfObjects(StepLink::class.java)
                        .withQuery(Query.builder().uri(DiContract.LinksContract.URI)
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
                                        if (t.size == 0) {
                                            val emptyView =
                                                    LayoutInflater.from(context)
                                                            .inflate(R.layout.item_details_link_empty,
                                                                    linksLl, false)
                                            linksLl.addView(emptyView)
                                        }
                                    }
                                }
                        )
            }
        }

    private var isTablet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isTablet = arguments.getBoolean(ARG_IS_TABLET, false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_step_detail, container, false)
        titleTv = root.findViewById(R.id.detail_title_tv) as TextView
        descriptionTv = root.findViewById(R.id.detail_description_tv) as TextView
        linksLl = root.findViewById(R.id.detail_links_ll) as LinearLayout


        if (isTablet) {
            positionFl = root.findViewById(R.id.view_position_fl) as FrameLayout
            positionTv = root.findViewById(R.id.view_position_tv) as TextView
            dateTv = root.findViewById(R.id.detail_date_tv) as TextView
            stepNumberTv = root.findViewById(R.id.detail_step_number_tv) as TextView
            stepPb = root.findViewById(R.id.detail_pb) as ProgressBar

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
                                    updateProgressBar()
                                }
                            })
        }

        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        stepLinkSubscription?.unsubscribeIfSubscribed()
        totalStepsSubscription?.unsubscribeIfSubscribed()
    }

    private fun updateProgressBar() {
        stepPb?.max = totalSteps
        stepPb?.progress = step?.position ?: 0
        stepNumberTv?.text = "${step?.position ?: 0} de $totalSteps"
    }

    companion object {
        const val ARG_IS_TABLET = "is_tablet"
        fun newInstance(isTablet: Boolean): StepDetailFragment {
            val args = Bundle()
            args.putBoolean(ARG_IS_TABLET, isTablet)
            val fragment = StepDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

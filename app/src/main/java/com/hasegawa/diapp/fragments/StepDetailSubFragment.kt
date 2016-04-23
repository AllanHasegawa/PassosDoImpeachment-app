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
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.domain.entities.StepLinkEntity
import com.hasegawa.diapp.domain.entities.StepWithLinksEntity
import com.hasegawa.diapp.domain.usecases.GetNumStepsTotalCompletedUseCase
import com.hasegawa.diapp.domain.usecases.GetStepWithLinksByPositionUseCase
import com.hasegawa.diapp.domain.usecases.NumCompletedAndTotal
import com.hasegawa.diapp.views.ItemDetailLinkView
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import timber.log.Timber

class StepDetailSubFragment : Fragment() {

    private lateinit var titleTv: TextView
    private lateinit var descriptionTv: TextView
    private lateinit var linksLl: LinearLayout

    private lateinit var positionTv: TextView
    private lateinit var totalStepsTv: TextView
    private lateinit var dateTv: TextView

    private var totalSteps = 0

    var stepPosition: Int = -1
        set(value) {
            field = value
            getStepByPositionUc?.position = value
        }

    private var getStepByPositionUc: GetStepWithLinksByPositionUseCase? = null
    private var getNumStepsUc: GetNumStepsTotalCompletedUseCase? = null

    private var isTablet = false

    private var step: StepEntity? = null
    private var links: List<StepLinkEntity>? = null

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

        getStepByPositionUc = GetStepWithLinksByPositionUseCase(stepPosition, DiApp.stepsRepository,
                Schedulers.io(), AndroidSchedulers.mainThread())
        getNumStepsUc = GetNumStepsTotalCompletedUseCase(DiApp.stepsRepository,
                Schedulers.io(), AndroidSchedulers.mainThread())

        loadGetStepUc()
        loadTotalStepsUc()
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        getStepByPositionUc?.unsubscribe()
        getNumStepsUc?.unsubscribe()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(ARG_STEP_POSITION, stepPosition)
    }

    private fun loadTotalStepsUc() {
        getNumStepsUc?.execute(object : Subscriber<NumCompletedAndTotal>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Timber.d(e, "Error while trying to find total steps.")
            }

            override fun onNext(t: NumCompletedAndTotal?) {
                if (t != null) {
                    totalSteps = t.total
                    totalStepsTv.text = t.total.toString()
                }
            }
        })
    }

    private fun loadGetStepUc() {
        getStepByPositionUc?.execute(object : Subscriber<StepWithLinksEntity>() {
            override fun onCompleted() {
            }

            override fun onError(e: Throwable?) {
                Timber.d(e, "Error loading step")
            }

            override fun onNext(t: StepWithLinksEntity?) {
                if (t != null) {
                    step = t.step
                    links = t.links
                    loadStepIntoViews()
                }
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

        linksLl.removeAllViews()
        links?.map {
            val view = ItemDetailLinkView(context, null)
            view.stepLink = it
            linksLl.addView(view)
        }
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

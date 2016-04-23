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

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.hasegawa.diapp.R
import com.hasegawa.diapp.adapters.StepsRvAdapter
import com.hasegawa.diapp.domain.entities.StepEntity
import com.hasegawa.diapp.syncadapters.SyncAdapter
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import timber.log.Timber


class MainFragment : Fragment() {

    interface OnMainFragmentListener {
        fun onMainFragmentScroll(dx: Int, dy: Int)
        fun onItemStepClicked(step: StepEntity)
    }

    lateinit var stepsRv: RecyclerView
    var stepsAdapter: StepsRvAdapter? = null
    var stepPositionToSelect = -1

    private var mainListener: OnMainFragmentListener? = null
    private var isTablet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            isTablet = arguments.getBoolean(ARG_IS_TABLET, false)
        }
        Timber.d("MainFragment created. Tablet mode: $isTablet")
    }

    override fun onDestroy() {
        super.onDestroy()
        stepsAdapter?.close()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(R.layout.fragment_main, container, false)

        stepsRv = root.findViewById(R.id.main_steps_rv) as RecyclerView

        stepsRv.layoutManager = LinearLayoutManager(activity)

        stepsAdapter = StepsRvAdapter(mainListener!!, isTablet, stepPositionToSelect)
        stepsRv.adapter = stepsAdapter
        val itemAnimator = SlideInLeftAnimator()
        itemAnimator.setInterpolator(AccelerateDecelerateInterpolator())
        stepsRv.itemAnimator = itemAnimator
        stepsRv.itemAnimator.addDuration = 300
        stepsRv.itemAnimator.removeDuration = 300
        stepsRv.itemAnimator.changeDuration = 300
        stepsRv.itemAnimator.moveDuration = 300

        stepsRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                mainListener?.onMainFragmentScroll(dx, dy)
            }
        })

        return root
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnMainFragmentListener) {
            mainListener = context as OnMainFragmentListener?
        } else {
            throw RuntimeException(context.toString()
                    + " must implement OnMainFragmentListener");
        }
    }

    override fun onDetach() {
        super.onDetach()
        mainListener = null


    }

    companion object {
        const val ARG_IS_TABLET = "is_tablet"

        fun newInstance(isTablet: Boolean): MainFragment {
            val args = Bundle()
            args.putBoolean(ARG_IS_TABLET, isTablet)
            val fragment = MainFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

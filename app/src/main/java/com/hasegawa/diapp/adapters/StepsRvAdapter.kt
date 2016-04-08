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

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hasegawa.diapp.DiApp
import com.hasegawa.diapp.R
import com.hasegawa.diapp.adapters.StepsRvAdapter.StepViewHolder
import com.hasegawa.diapp.fragments.MainFragment.OnMainFragmentListener
import com.hasegawa.diapp.models.DiContract.StepsContract
import com.hasegawa.diapp.models.Step
import com.hasegawa.diapp.utils.unsubscribeIfSubscribed
import com.hasegawa.diapp.views.ItemStepView
import com.pushtorefresh.storio.contentresolver.queries.Query
import rx.Observer
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.ArrayList

class StepsRvAdapter(val mainFragmentListener: OnMainFragmentListener,
                     val isTablet: Boolean) :
        RecyclerView.Adapter<StepViewHolder>() {
    interface RvClickListener {
        fun onItemSelect(position: Int)
    }

    class Item(var type: Int, var step: Step?)

    class StepViewHolder(view: View,
                         val mainFragmentListener: OnMainFragmentListener? = null,
                         val clickListener: RvClickListener? = null) :
            RecyclerView.ViewHolder(view) {

        private var step: Step? = null

        fun setStep(step: Step, selected: Boolean) {
            this.step = step
            val itemStepView = itemView as ItemStepView
            itemStepView.step = step
            itemStepView.isSelected = selected
            itemStepView.setOnClickListener({
                clickListener?.onItemSelect(adapterPosition)
                mainFragmentListener?.onItemStepClicked(step)
            })
        }
    }

    private var stepsCache: ArrayList<Item> = ArrayList()

    private var stepsSubscription: Subscription? = null

    private var selectedItem = -1
    private val rvClickListener =
            object : RvClickListener {
                override fun onItemSelect(position: Int) {
                    selectItem(position)
                }
            }


    init {
        if (isTablet) {
            selectedItem = 0
        }
        stepsSubscription =
                DiApp.diProvider
                        .get()
                        .listOfObjects(Step::class.java)
                        .withQuery(Query.builder().uri(StepsContract.URI)
                                .sortOrder(StepsContract.COL_POSITION).build())
                        .prepare()
                        .asRxObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .map {
                            val arr = ArrayList<Item>(it.size + 2)
                            if (!isTablet) {
                                arr.add(Item(TYPE_SPACE, null))
//                                arr.add(Item(TYPE_BEGINNING, null))
                            }
                            arr.addAll(it.map { Item(TYPE_STEP, it) })

                            if (isTablet) {
                                selectedItem = 0
                            }
                            val items = stepsCache.size
                            stepsCache.clear()
                            notifyItemRangeRemoved(0, items)
                            arr
                        }
                        .flatMapIterable { it }
                        .subscribe(object : Observer<Item> {
                            override fun onCompleted() {
                            }

                            override fun onError(e: Throwable?) {
                                Timber.d(e, "Error updating StepsRvAdapter.")
                            }

                            override fun onNext(t: Item) {
                                stepsCache.add(t)
                                notifyItemChanged(stepsCache.size - 1)
                            }
                        })
    }

    fun close() {
        stepsSubscription?.unsubscribeIfSubscribed()
        stepsCache.clear()
    }

    fun selectItem(position: Int) {
        if (isTablet) {
            val oldSelectedItem = selectedItem
            selectedItem = position
            notifyItemChanged(oldSelectedItem)
            notifyItemChanged(selectedItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return stepsCache[position].type
    }

    override fun onBindViewHolder(holder: StepViewHolder?, position: Int) {
        val v = stepsCache[position]
        when (v.type) {
            TYPE_STEP -> {
                holder!!.setStep(stepsCache[position].step!!, position == selectedItem)
            }
        }
    }

    override fun getItemCount(): Int {
        return stepsCache.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): StepViewHolder? {
        return when (viewType) {
            TYPE_STEP -> StepViewHolder(ItemStepView(parent!!.context, null),
                    mainFragmentListener, rvClickListener)
            TYPE_SPACE -> {
                val spaceView = LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.item_step_space, parent, false)
                StepViewHolder(spaceView)
            }
            TYPE_BEGINNING -> {
                val view = LayoutInflater.from(parent!!.context)
                        .inflate(R.layout.item_step_beginning, parent, false)
                StepViewHolder(view)
            }
            else -> throw RuntimeException("Unknown viewType $viewType")
        }
    }

    companion object {
        val TYPE_STEP = 1
        val TYPE_SPACE = 4
        val TYPE_BEGINNING = 6
    }
}

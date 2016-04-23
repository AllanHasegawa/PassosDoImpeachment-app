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
package com.hasegawa.diapp.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import com.hasegawa.diapp.R
import com.hasegawa.diapp.domain.entities.StepEntity

class ItemStepView(ctx: Context, val attrs: AttributeSet?) : FrameLayout(ctx, attrs) {
    private lateinit var positionTv: TextView
    private lateinit var positionFl: FrameLayout
    private lateinit var possibleDateTv: TextView
    private lateinit var titleTv: TextView

    private var colorSelected: Int = 0
    private var colorUnselected: Int = 0

    init {
        inflate(ctx, R.layout.item_step, this)
        this.layoutParams = RecyclerView.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT
        )
        onFinishInflate()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        positionFl = this.findViewById(R.id.view_position_fl) as FrameLayout
        positionTv = this.findViewById(R.id.view_position_tv) as TextView
        possibleDateTv = this.findViewById(R.id.step_possible_date_tv) as TextView
        titleTv = this.findViewById(R.id.step_title_tv) as TextView

        val colorStateList =
                ContextCompat.getColorStateList(context, R.color.selector_step_selection_color)
        colorSelected = colorStateList.getColorForState(View.SELECTED_STATE_SET, 0)
        colorUnselected = colorStateList.defaultColor
    }

    var step: StepEntity? = null
        set(s: StepEntity?) {
            field = s
            if (s != null) {
                positionTv.text = s.position.toString()
                titleTv.text = s.title
                possibleDateTv.text = s.possibleDate
                val positionDrawable = when (s.completed) {
                    true -> R.drawable.border_item_step_number_completed
                    false -> R.drawable.border_item_step_number_incomplete
                }
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    positionFl.background = ContextCompat.getDrawable(context, positionDrawable)
                } else {
                    positionFl.setBackgroundDrawable(ContextCompat.getDrawable(context,
                            positionDrawable))
                }
            }
        }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        (getChildAt(0) as CardView).setCardBackgroundColor(
                if (selected) colorSelected else colorUnselected)
        if (selected) {
            shakeShake()
        }
    }

    fun shakeShake() {
        // http://frario.deviantart.com/art/Marinaaaa-356092658
        val anim = AnimationUtils.loadAnimation(context, R.anim.shake_shake)
        this.startAnimation(anim)
    }
}

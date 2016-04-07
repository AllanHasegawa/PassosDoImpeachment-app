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


import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.hasegawa.diapp.R
import com.hasegawa.diapp.R.layout
import timber.log.Timber

class CreditsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.d("Credits fragment created.")
        super.onCreate(savedInstanceState)
        if (arguments != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val root = inflater!!.inflate(layout.fragment_credits, container, false)

        root.findViewById(R.id.credits_hase_email_bt).setOnClickListener({ sendHaseEmail() })
        root.findViewById(R.id.credits_hase_github_bt).setOnClickListener({ openHaseGithub() })

        return root
    }

    fun sendHaseEmail() {
        val i = Intent(Intent.ACTION_SENDTO,
                Uri.fromParts("mailto",
                        getString(R.string.credits_hase_email),
                        null))
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.credits_hase_email_subject))
        startActivity(Intent.createChooser(i, getString(R.string.credits_email_chooser_header)))
    }

    fun openHaseGithub() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(getString(R.string.credits_hase_github_url))
        try {
            context.startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_invalid_link),
                    Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        fun newInstance(): CreditsFragment {
            val fragment = CreditsFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

}

package com.allattentionhere.fabulousfiltersample

import android.app.Dialog
import androidx.core.content.ContextCompat

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment
import com.allattentionhere.fabulousfiltersample.R.id.*
import com.google.android.flexbox.FlexboxLayout


/**
 * Created by krupenghetiya on 23/06/17.
 */

class MySampleFabFragment : AAH_FabulousFragment() {

    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.filter_sample_view, null)

        val rl_content = contentView.findViewById<View>(R.id.rl_content) as RelativeLayout
        val ll_buttons = contentView.findViewById<View>(R.id.ll_buttons) as LinearLayout
        contentView.findViewById<View>(R.id.btn_close).setOnClickListener { closeFilter("closed") }

        //params to set
        setAnimationDuration(600) //optional; default 500ms
        setPeekHeight(600) // optional; default 400dp
        setCallbacks((activity as AAH_FabulousFragment.Callbacks?)!!) //optional; to get back result
        setViewgroupStatic(ll_buttons) // optional; layout to stick at bottom on slide
        //        setViewPager(vp_types); //optional; if you use viewpager that has scrollview
        setViewMain(rl_content) //necessary; main bottomsheet view
        setMainContentView(contentView) // necessary; call at end before super
        super.setupDialog(dialog, style) //call super at last
    }

    companion object {

        fun newInstance(): MySampleFabFragment {
            return MySampleFabFragment()
        }
    }

}

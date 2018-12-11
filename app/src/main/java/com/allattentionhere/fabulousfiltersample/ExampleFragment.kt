package com.allattentionhere.fabulousfiltersample

import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment


class ExampleFragment : Fragment(), AAH_FabulousFragment.Callbacks {
    private lateinit var dialogFrag: MySampleFabFragment

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_example, container, false)
        // Log.d("k9crash", "result not null: ");
        val fab = rootView.findViewById<View>(R.id.fab) as FloatingActionButton
        dialogFrag = MySampleFabFragment.newInstance()
        dialogFrag.setParentFab(fab)
        fab.setOnClickListener {
            dialogFrag.setCallbacks(this@ExampleFragment)
            dialogFrag.show(activity!!.supportFragmentManager, dialogFrag.tag)
        }
        return rootView
    }

    override fun onResult(result: Any) {
        Log.d("k9res", "onResult: " + result.toString())
        if (result.toString().equals("swiped_down", ignoreCase = true)) {
            //do something or nothing
        } else {
            //handle result
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (dialogFrag.isAdded) {
            dialogFrag.dismiss()
            dialogFrag.show(activity!!.supportFragmentManager, dialogFrag.tag)
        }

    }

    companion object {

        fun newInstance(): ExampleFragment {
            return ExampleFragment()
        }
    }
}// Required empty public constructor

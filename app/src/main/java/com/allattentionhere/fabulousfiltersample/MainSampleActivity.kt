package com.allattentionhere.fabulousfiltersample

import android.content.res.Configuration
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton

import androidx.appcompat.app.AppCompatActivity

import android.util.Log
import android.view.View

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment

import com.allattentionhere.fabulousfiltersample.R.id.fab2
import com.allattentionhere.fabulousfiltersample.R.id.ll
import com.allattentionhere.fabulousfiltersample.R.id.recyclerView


class MainSampleActivity : AppCompatActivity(), AAH_FabulousFragment.Callbacks {

    private lateinit var fab: FloatingActionButton
    private lateinit var dialogFrag: MySampleFabFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_sample)
        fab = findViewById<View>(R.id.fab) as FloatingActionButton

        dialogFrag = MySampleFabFragment.newInstance()
        dialogFrag.setParentFab(fab)
        fab.setOnClickListener { dialogFrag.show(supportFragmentManager, dialogFrag.tag) }

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
            dialogFrag.show(supportFragmentManager, dialogFrag.tag)
        }

    }


}

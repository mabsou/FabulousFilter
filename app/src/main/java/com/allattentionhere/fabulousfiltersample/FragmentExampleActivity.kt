package com.allattentionhere.fabulousfiltersample

import android.os.Bundle

import androidx.fragment.app.FragmentTransaction
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.FrameLayout

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment

import com.allattentionhere.fabulousfiltersample.R.id.fab


class FragmentExampleActivity : AppCompatActivity(), AAH_FabulousFragment.Callbacks {

    private lateinit var fl: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_example)
        fl = findViewById<FrameLayout>(R.id.fl)

        val f = ExampleFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl, f, "tag")
        transaction.commitAllowingStateLoss()
    }


    override fun onResult(result: Any) {
        Log.d("k9res", "onResult: " + result.toString())
        if (result.toString().equals("swiped_down", ignoreCase = true)) {
            //do something or nothing
        } else {
            //handle result
        }
    }


}

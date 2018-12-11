package com.allattentionhere.fabulousfiltersample

import android.annotation.SuppressLint
import android.content.res.Configuration
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.collection.ArrayMap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.LinearLayout

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment
import com.squareup.picasso.Picasso

import java.util.ArrayList


class MainActivity : AppCompatActivity(), AAH_FabulousFragment.Callbacks, AAH_FabulousFragment.AnimationListener {

    private lateinit var fab: FloatingActionButton
    private lateinit var fab2: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var mData: MovieData
    private lateinit var mAdapter: MoviesAdapter
    private lateinit var p: Picasso
    private lateinit var ll: LinearLayout
    private var mList: MutableList<SingleMovie> = ArrayList()
    val applied_filters = ArrayMap<String, MutableList<String>>()
    private lateinit var dialogFrag: MyFabFragment
    private lateinit var dialogFrag1: MyFabFragment

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab2 = findViewById<View>(R.id.fab2) as FloatingActionButton
        recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        ll = findViewById<View>(R.id.ll) as LinearLayout

        mData = Util.movies
        p = Picasso.with(this)
        mList.addAll(mData.allMovies)
        mAdapter = MoviesAdapter(mList, p, this@MainActivity)
        val mLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter

        if (intent.getIntExtra("fab", 1) == 2) {
            fab2.visibility = View.VISIBLE
            fab.visibility = View.GONE
            ll.visibility = View.VISIBLE
        } else {
            fab2.visibility = View.GONE
            fab.visibility = View.VISIBLE
            ll.visibility = View.GONE
        }

        dialogFrag1 = MyFabFragment.newInstance()
        dialogFrag1.setParentFab(fab)
        fab.setOnClickListener { dialogFrag1.show(supportFragmentManager, dialogFrag1.tag) }

        dialogFrag = MyFabFragment.newInstance()
        dialogFrag.setParentFab(fab2)
        fab2.setOnClickListener { dialogFrag.show(supportFragmentManager, dialogFrag.tag) }
    }


    override fun onResult(result: Any) {
        Log.d("k9res", "onResult: " + result.toString())
        if (result.toString().equals("swiped_down", ignoreCase = true)) {
            //do something or nothing
        } else {
            if (result != null) {
                val applied_filters = result as ArrayMap<String, MutableList<String>>
                if (applied_filters!!.size != 0) {
                    var filteredList = mData.allMovies
                    //iterate over arraymap
                    for ((key, value) in applied_filters) {
                        Log.d("k9res", "entry.key: $key")
                        when (key) {
                            "genre" -> filteredList = mData.getGenreFilteredMovies(value, filteredList)
                            "rating" -> filteredList = mData.getRatingFilteredMovies(value, filteredList)
                            "year" -> filteredList = mData.getYearFilteredMovies(value, filteredList)
                            "quality" -> filteredList = mData.getQualityFilteredMovies(value, filteredList)
                        }
                    }
                    Log.d("k9res", "new size: " + filteredList.size)
                    mList.clear()
                    mList.addAll(filteredList)
                    mAdapter.notifyDataSetChanged()

                } else {
                    mList.addAll(mData.allMovies)
                    mAdapter.notifyDataSetChanged()
                }
            }
            //handle result
        }
    }

    fun getmData(): MovieData {
        return mData
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (dialogFrag.isAdded) {
            dialogFrag.dismiss()
            dialogFrag.show(supportFragmentManager, dialogFrag.tag)
        }
        if (dialogFrag1.isAdded) {
            dialogFrag1.dismiss()
            dialogFrag1.show(supportFragmentManager, dialogFrag1.tag)
        }

    }

    override fun onOpenAnimationStart() {
        Log.d("aah_animation", "onOpenAnimationStart: ")
    }

    override fun onOpenAnimationEnd() {
        Log.d("aah_animation", "onOpenAnimationEnd: ")

    }

    override fun onCloseAnimationStart() {
        Log.d("aah_animation", "onCloseAnimationStart: ")

    }

    override fun onCloseAnimationEnd() {
        Log.d("aah_animation", "onCloseAnimationEnd: ")

    }
}

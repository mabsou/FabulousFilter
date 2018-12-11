package com.allattentionhere.fabulousfilter

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by krupenghetiya on 26/06/17.
 */

class AAH_FilterView : FrameLayout {
    private lateinit var fl: FrameLayout
    private lateinit var fab: FloatingActionButton

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()

    }

    fun init() {
        fl = FrameLayout(context)
        fl.tag = "aah_fl"
        fab = FloatingActionButton(context)
        fab.tag = "aah_fab"
        fab.compatElevation = 0f
        fl.addView(fab)
        this.addView(fl)

    }
}

package com.allattentionhere.fabulousfilter

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.viewpager.widget.ViewPager
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout

import com.allattentionhere.fabulousfilter.viewpagerbottomsheet.BottomSheetUtils
import com.allattentionhere.fabulousfilter.viewpagerbottomsheet.ViewPagerBottomSheetBehavior
import com.allattentionhere.fabulousfilter.viewpagerbottomsheet.ViewPagerBottomSheetDialog
import com.allattentionhere.fabulousfilter.viewpagerbottomsheet.ViewPagerBottomSheetDialogFragment


/**
 * Created by krupenghetiya on 05/10/16.
 */

open class AAH_FabulousFragment : ViewPagerBottomSheetDialogFragment() {

    private var parent_fab: FloatingActionButton? = null
    private var metrics: DisplayMetrics? = null
    private var fab_size = 56
    private var fab_pos_y: Int = 0
    private var fab_pos_x: Int = 0
    private var scale_by = 12f
    private var bottomSheet: FrameLayout? = null
    private var mBottomSheetBehavior: ViewPagerBottomSheetBehavior<*>? = null
    private var fab_outside_y_offest = 0
    private var is_fab_outside_peekheight: Boolean = false

    //user params
    private var peek_height = 400
    private var anim_duration = 500
    private var fabulous_fab: FloatingActionButton? = null
    private var fl: FrameLayout? = null
    private var view_main: View? = null
    private var viewgroup_static: View? = null
    private var fab_icon_resource: Drawable? = null
    private var fab_background_color_resource: ColorStateList? = null
    private var contentView: View? = null
    private var callbacks: Callbacks? = null
    private var animationListener: AnimationListener? = null
    private var viewPager: ViewPager? = null


    private val mBottomSheetBehaviorCallback = object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                ViewPagerBottomSheetBehavior.STATE_HIDDEN -> {
                    if (callbacks != null) {
                        callbacks!!.onResult("swiped_down")
                    }
                    dismiss()
                }
                ViewPagerBottomSheetBehavior.STATE_COLLAPSED -> {
                    val params = view_main!!.layoutParams
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    view_main!!.layoutParams = params
                }
                ViewPagerBottomSheetBehavior.STATE_EXPANDED -> {
                    val params1 = view_main!!.layoutParams
                    params1.height = ViewGroup.LayoutParams.MATCH_PARENT
                    view_main!!.layoutParams = params1
                }
            }

        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            if (viewgroup_static != null) {
                val range = (metrics!!.heightPixels.toFloat() - metrics!!.density * peek_height - getStatusBarHeight(context!!).toFloat()).toInt()
                viewgroup_static!!.animate().translationY(-range + range * slideOffset).setDuration(0).start()
            }
        }
    }


    override fun onStart() {
        super.onStart()
        dialog!!.window!!.setWindowAnimations(R.style.dialog_animation_fade)

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        metrics = this.resources.displayMetrics

    }


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        if (viewPager != null) {
            BottomSheetUtils.setupViewPager(viewPager!!)
        }

        dialog.setContentView(contentView!!)

        val location = IntArray(2)
        parent_fab!!.getLocationInWindow(location)
        val x = location[0]
        val y = location[1]

        fab_size = parent_fab!!.height
        fab_pos_y = y
        fab_pos_x = x
        fab_icon_resource = parent_fab!!.drawable
        fab_background_color_resource = parent_fab!!.backgroundTintList

        (contentView!!.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))

        mBottomSheetBehavior = ViewPagerBottomSheetBehavior.from(contentView!!.parent as View)
        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior!!.setBottomSheetCallback(mBottomSheetBehaviorCallback)
            if (fab_pos_y - (metrics!!.heightPixels - metrics!!.density * peek_height) + fab_size * metrics!!.density - fab_size * metrics!!.density <= 0) {
                is_fab_outside_peekheight = true
                mBottomSheetBehavior!!.peekHeight = metrics!!.heightPixels - fab_pos_y
                fab_outside_y_offest = (metrics!!.heightPixels.toFloat() - fab_pos_y.toFloat() - metrics!!.density * peek_height).toInt()
            } else {
                mBottomSheetBehavior!!.peekHeight = (metrics!!.density * peek_height).toInt()
            }
            contentView!!.requestLayout()
        }
        dialog.setOnShowListener { paramDialog ->
            val d = paramDialog as ViewPagerBottomSheetDialog
            bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
            ViewPagerBottomSheetBehavior.from(bottomSheet!!).state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
            if (viewgroup_static != null) {
                val range = (metrics!!.heightPixels.toFloat() - metrics!!.density * peek_height - getStatusBarHeight(context!!).toFloat()).toInt()
                viewgroup_static!!.animate().translationY((-range).toFloat()).setDuration(0).start()
            }
            val fab_range_y = (fab_pos_y - (metrics!!.heightPixels - metrics!!.density * peek_height)).toInt()
            fabulous_fab!!.y = (fab_range_y + fab_outside_y_offest).toFloat()
            fabulous_fab!!.x = fab_pos_x.toFloat()
            view_main!!.visibility = View.INVISIBLE
            fabAnim()
        }

        val params = (contentView!!.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior

        if (behavior != null && behavior is ViewPagerBottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }

        scale_by = (peek_height * 1.6 / fab_size).toFloat() * metrics!!.density
        fabulous_fab = contentView!!.findViewWithTag<View>("aah_fab") as FloatingActionButton
        fl = contentView!!.findViewWithTag<View>("aah_fl") as FrameLayout
        var newfabsize = fab_size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val ele = Math.floor((parent_fab!!.compatElevation / 2).toDouble()).toInt()
            newfabsize = (fab_size - metrics!!.density * (18 + 6 * ele)).toInt()
            scale_by = (peek_height * 2 / newfabsize).toFloat() * metrics!!.density

        }

        val lp = FrameLayout.LayoutParams(newfabsize, newfabsize)
        lp.gravity = Gravity.CENTER
        fabulous_fab!!.layoutParams = lp
        fabulous_fab!!.setImageDrawable(fab_icon_resource)
        fabulous_fab!!.backgroundTintList = fab_background_color_resource


    }


    private fun fabAnim() {
        if (animationListener != null) animationListener!!.onOpenAnimationStart()
        val anim = AAH_ArcTranslateAnimation(0f, (metrics!!.widthPixels / 2 - fab_pos_x - fab_size / 2).toFloat(), 0f, -(metrics!!.density * (peek_height / 2 - (metrics!!.heightPixels - fab_pos_y - fab_size) / metrics!!.density)))
        anim.duration = anim_duration.toLong()
        fl!!.startAnimation(anim)
        anim.setAnimationListener(object : Animation.AnimationListener {
            @SuppressLint("RestrictedApi")
            override fun onAnimationStart(animation: Animation) {
                if (activity != null && !activity!!.isFinishing) {
                    parent_fab!!.visibility = View.GONE
                }
            }

            @SuppressLint("RestrictedApi")
            override fun onAnimationEnd(animation: Animation) {
                fabulous_fab!!.setImageResource(android.R.color.transparent)
                fabulous_fab!!.animate().setListener(null)
                fabulous_fab!!.visibility = View.INVISIBLE
                //Do something after 100ms
                val handler = Handler()
                handler.postDelayed({
                    mBottomSheetBehavior!!.peekHeight = (metrics!!.density * peek_height).toInt()
                    ViewPagerBottomSheetBehavior.from(bottomSheet!!).state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                    if (is_fab_outside_peekheight) {
                        bottomSheet!!.requestLayout()
                    }

                    fabulous_fab!!.animate().translationXBy((metrics!!.widthPixels / 2 - fab_pos_x - fab_size / 2).toFloat())
                            .translationYBy(-(metrics!!.density * (peek_height / 2 - (metrics!!.heightPixels - fab_pos_y - fab_size) / metrics!!.density)) - fab_outside_y_offest).setDuration(0)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationStart(animation: Animator) {
                                    super.onAnimationStart(animation)
                                    fabulous_fab!!.visibility = View.VISIBLE

                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation)
                                    fabulous_fab!!.animate().setListener(null)
                                    fabulous_fab!!.animate().scaleXBy(scale_by)
                                            .scaleYBy(scale_by)
                                            .setDuration(anim_duration.toLong())
                                            .setListener(object : AnimatorListenerAdapter() {
                                                override fun onAnimationEnd(animation: Animator) {
                                                    super.onAnimationEnd(animation)
                                                    fabulous_fab!!.animate().setListener(null)
                                                    fabulous_fab!!.visibility = View.GONE
                                                    view_main!!.visibility = View.VISIBLE
                                                    if (animationListener != null)
                                                        animationListener!!.onOpenAnimationEnd()

                                                }
                                            })


                                }
                            })
                }, 10)

            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })

    }

    @SuppressLint("RestrictedApi")
    fun closeFilter(o: Any) {
        if (animationListener != null) animationListener!!.onCloseAnimationStart()
        if (ViewPagerBottomSheetBehavior.from(bottomSheet!!).state == ViewPagerBottomSheetBehavior.STATE_EXPANDED) {
            ViewPagerBottomSheetBehavior.from(bottomSheet!!).state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
        }
        fabulous_fab!!.visibility = View.VISIBLE
        view_main!!.visibility = View.INVISIBLE
        fabulous_fab!!.animate().scaleXBy(-scale_by)
                .scaleYBy(-scale_by)
                .setDuration(anim_duration.toLong())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        fabulous_fab!!.animate().setListener(null)
                        fabulous_fab!!.setImageDrawable(fab_icon_resource)
                        if (is_fab_outside_peekheight) {
                            mBottomSheetBehavior!!.peekHeight = metrics!!.heightPixels - fab_pos_y
                            ViewPagerBottomSheetBehavior.from(bottomSheet!!).state = ViewPagerBottomSheetBehavior.STATE_COLLAPSED
                            bottomSheet!!.requestLayout()
                            //                            fabulous_fab.setY(fab_outside_y_offest - fab_pos_y + getStatusBarHeight(getContext()));
                        } else {
                            mBottomSheetBehavior!!.peekHeight = (metrics!!.density * peek_height).toInt()
                        }
                        val from_y: Float
                        val to_y: Float

                        from_y = fab_outside_y_offest.toFloat()
                        to_y = metrics!!.density * (peek_height / 2 - (metrics!!.heightPixels - fab_pos_y - fab_size) / metrics!!.density) + fab_outside_y_offest
                        val anim = AAH_ArcTranslateAnimation(0f, (-(metrics!!.widthPixels / 2 - fab_pos_x - fab_size / 2)).toFloat(), from_y, to_y)
                        anim.duration = anim_duration.toLong()
                        fl!!.startAnimation(anim)
                        anim.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation) {

                            }

                            override fun onAnimationEnd(animation: Animation) {
                                fabulous_fab!!.animate().setListener(null)
                                fabulous_fab!!.visibility = View.INVISIBLE
                                val handler = Handler()
                                handler.postDelayed({
                                    //Do something after 100ms
                                    if (animationListener != null)
                                        animationListener!!.onCloseAnimationEnd()
                                    if (callbacks != null) {
                                        callbacks!!.onResult(o)
                                    }
                                    dismiss()
                                }, 50)
                            }

                            override fun onAnimationRepeat(animation: Animation) {

                            }
                        })

                    }
                })
    }

    @SuppressLint("RestrictedApi")
    override fun onStop() {
        parent_fab!!.visibility = View.VISIBLE
        super.onStop()
    }


    interface Callbacks {
        fun onResult(result: Any)
    }

    interface AnimationListener {
        fun onOpenAnimationStart()

        fun onOpenAnimationEnd()

        fun onCloseAnimationStart()

        fun onCloseAnimationEnd()
    }

    fun setPeekHeight(peek_height: Int) {
        this.peek_height = peek_height
    }

    fun setViewMain(view_main: View) {
        this.view_main = view_main
    }

    fun setViewgroupStatic(viewgroup_static: View) {
        this.viewgroup_static = viewgroup_static
    }

    fun setMainContentView(contentView: View) {
        this.contentView = contentView
    }


    fun setCallbacks(callbacks: Callbacks) {
        this.callbacks = callbacks
    }

    fun setAnimationListener(animationListener: AnimationListener) {
        this.animationListener = animationListener
    }

    fun setParentFab(parent_fab: FloatingActionButton) {
        this.parent_fab = parent_fab
    }

    fun setAnimationDuration(anim_duration: Int) {
        this.anim_duration = anim_duration
    }

    fun setViewPager(viewPager: ViewPager) {
        this.viewPager = viewPager
    }

    companion object {

        fun getStatusBarHeight(context: Context): Int {
            val resources = context.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0)
                resources.getDimensionPixelSize(resourceId)
            else
                Math.ceil(((if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) 24 else 25) * resources.displayMetrics.density).toDouble()).toInt()
        }
    }
}

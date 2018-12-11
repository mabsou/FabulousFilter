package com.allattentionhere.fabulousfilter.viewpagerbottomsheet

import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.appcompat.app.AppCompatDialog
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout

import com.allattentionhere.fabulousfilter.R


class ViewPagerBottomSheetDialog : AppCompatDialog {

    private var mBehavior: ViewPagerBottomSheetBehavior<FrameLayout>? = null

    private var mCancelable = true
    private var mCanceledOnTouchOutside = true
    private var mCanceledOnTouchOutsideSet: Boolean = false

    private val mBottomSheetCallback = object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View,
                                    @ViewPagerBottomSheetBehavior.State newState: Int) {
            if (newState == ViewPagerBottomSheetBehavior.STATE_HIDDEN) {
                cancel()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    @JvmOverloads constructor(context: Context, theme: Int = 0) : super(context, theme) {
        // We hide the title bar for any style configuration. Otherwise, there will be a gap
        // above the bottom sheet when it is expanded.
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    protected constructor(context: Context, cancelable: Boolean,
                          cancelListener: DialogInterface.OnCancelListener) : super(context, cancelable, cancelListener) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        mCancelable = cancelable
    }

    override fun setContentView(layoutResId: Int) {
        super.setContentView(wrapInBottomSheet(layoutResId, null, null))
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun setContentView(view: View) {
        super.setContentView(wrapInBottomSheet(0, view, null))
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(wrapInBottomSheet(0, view, params))
    }

    override fun setCancelable(cancelable: Boolean) {
        super.setCancelable(cancelable)
        if (mCancelable != cancelable) {
            mCancelable = cancelable
            if (mBehavior != null) {
                mBehavior!!.isHideable = cancelable
            }
        }
    }

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        super.setCanceledOnTouchOutside(cancel)
        if (cancel && !mCancelable) {
            mCancelable = true
        }
        mCanceledOnTouchOutside = cancel
        mCanceledOnTouchOutsideSet = true
    }

    private fun wrapInBottomSheet(layoutResId: Int, view: View?, params: ViewGroup.LayoutParams?): View {
        var newView = view
        val coordinator = View.inflate(context,
                R.layout.design_view_pager_bottom_sheet_dialog, null) as CoordinatorLayout
        if (layoutResId != 0 && newView == null) {
            newView = layoutInflater.inflate(layoutResId, coordinator, false)
        }
        val bottomSheet = coordinator.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
        mBehavior = ViewPagerBottomSheetBehavior.from(bottomSheet)
        mBehavior!!.setBottomSheetCallback(mBottomSheetCallback)
        mBehavior!!.isHideable = mCancelable
        if (params == null) {
            bottomSheet.addView(newView)
        } else {
            bottomSheet.addView(newView, params)
        }
        // We treat the CoordinatorLayout as outside the dialog though it is technically inside
        coordinator.findViewById<View>(R.id.touch_outside).setOnClickListener {
            if (mCancelable && isShowing && shouldWindowCloseOnTouchOutside()) {
                cancel()
            }
        }
        // Handle accessibility events
        ViewCompat.setAccessibilityDelegate(bottomSheet, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View,
                                                           info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                if (mCancelable) {
                    info.addAction(AccessibilityNodeInfoCompat.ACTION_DISMISS)
                    info.isDismissable = true
                } else {
                    info.isDismissable = false
                }
            }

            override fun performAccessibilityAction(host: View, action: Int, args: Bundle): Boolean {
                if (action == AccessibilityNodeInfoCompat.ACTION_DISMISS && mCancelable) {
                    cancel()
                    return true
                }
                return super.performAccessibilityAction(host, action, args)
            }
        })
        return coordinator
    }

    internal fun shouldWindowCloseOnTouchOutside(): Boolean {
        if (!mCanceledOnTouchOutsideSet) {
            if (Build.VERSION.SDK_INT < 11) {
                mCanceledOnTouchOutside = true
            } else {
                val a = context.obtainStyledAttributes(
                        intArrayOf(android.R.attr.windowCloseOnTouchOutside))
                mCanceledOnTouchOutside = a.getBoolean(0, true)
                a.recycle()
            }
            mCanceledOnTouchOutsideSet = true
        }
        return mCanceledOnTouchOutside
    }

    private fun getThemeResId(context: Context, themeId: Int): Int {
        var newthemeId = themeId
        if (themeId == 0) {
            // If the provided theme is 0, then retrieve the dialogTheme from our theme
            val outValue = TypedValue()
            if (context.theme.resolveAttribute(
                            R.attr.bottomSheetDialogTheme, outValue, true)) {
                newthemeId = outValue.resourceId
            } else {
                // bottomSheetDialogTheme is not provided; we default to our light theme
                newthemeId = R.style.Theme_Design_Light_BottomSheetDialog
            }
        }
        return newthemeId
    }

}

/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.allattentionhere.fabulousfilter.viewpagerbottomsheet

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.ParcelableCompat
import androidx.core.os.ParcelableCompatCreatorCallbacks
import androidx.customview.view.AbsSavedState
import androidx.core.view.MotionEventCompat
import androidx.core.view.NestedScrollingChild
import androidx.core.view.VelocityTrackerCompat
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import androidx.customview.widget.ViewDragHelper
import androidx.viewpager.widget.ViewPagerUtils

import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewParent


import com.allattentionhere.fabulousfilter.R

import java.lang.ref.WeakReference


class ViewPagerBottomSheetBehavior<V : View> : CoordinatorLayout.Behavior<V> {

    private val mMaximumVelocity: Float

    private var mPeekHeight: Int = 0

    private var mPeekHeightAuto: Boolean = false

    private var peekHeightMin: Int = 0
        private set

    private var mMinOffset: Int = 0

    private var mMaxOffset: Int = 0

    var isHideable: Boolean = false

    var skipCollapsed: Boolean = false


    private var mState = STATE_COLLAPSED

    private var mViewDragHelper: ViewDragHelper? = null

    private var mIgnoreEvents: Boolean = false

    private var mLastNestedScrollDy: Int = 0

    private var mNestedScrolled: Boolean = false

    private var mParentHeight: Int = 0

    private var mViewRef: WeakReference<V>? = null

    private lateinit var mNestedScrollingChildRef: WeakReference<View>

    private var mCallback: BottomSheetCallback? = null

    private var mVelocityTracker: VelocityTracker? = null

    internal var mActivePointerId: Int = 0

    private var mInitialY: Int = 0

    private var mTouchingScrollingChild: Boolean = false


    var peekHeight: Int
        get() = if (mPeekHeightAuto) PEEK_HEIGHT_AUTO else mPeekHeight
        set(peekHeight) {
            var layout = false
            if (peekHeight == PEEK_HEIGHT_AUTO) {
                if (!mPeekHeightAuto) {
                    mPeekHeightAuto = true
                    layout = true
                }
            } else if (mPeekHeightAuto || mPeekHeight != peekHeight) {
                mPeekHeightAuto = false
                mPeekHeight = Math.max(0, peekHeight)
                mMaxOffset = mParentHeight - peekHeight
                layout = true
            }
            if (layout && mState == STATE_COLLAPSED && mViewRef != null) {
                val view = mViewRef!!.get()
                view?.requestLayout()
            }
        }


    // The view is not laid out yet; modify mState and let onLayoutChild handle it later
    // Start the animation; wait until a pending layout if there is one.
    var state: Int
        get() = mState
        set(state) {
            if (state == mState) {
                return
            }
            if (mViewRef == null) {
                if (state == STATE_COLLAPSED || state == STATE_EXPANDED ||
                        isHideable && state == STATE_HIDDEN) {
                    mState = state
                }
                return
            }
            val child = mViewRef!!.get() ?: return
            val parent = child.parent
            if (parent != null && parent.isLayoutRequested && ViewCompat.isAttachedToWindow(child)) {
                child.post { startSettlingAnimation(child, state) }
            } else {
                startSettlingAnimation(child, state)
            }
        }

    private val yVelocity: Float
        get() {
            mVelocityTracker!!.computeCurrentVelocity(1000, mMaximumVelocity)
            return VelocityTrackerCompat.getYVelocity(mVelocityTracker!!, mActivePointerId)
        }

    private val mDragCallback = object : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            if (mState == STATE_DRAGGING) {
                return false
            }
            if (mTouchingScrollingChild) {
                return false
            }
            if (mState == STATE_EXPANDED && mActivePointerId == pointerId) {
                val scroll = mNestedScrollingChildRef.get()
                if (scroll != null && ViewCompat.canScrollVertically(scroll, -1)) {
                    // Let the content scroll up
                    return false
                }
            }
            return mViewRef != null && mViewRef!!.get() === child
        }

        override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
            dispatchOnSlide(top)
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
                setStateInternal(STATE_DRAGGING)
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val top: Int
            val targetState: Int
            if (yvel < 0) { // Moving up
                top = mMinOffset
                targetState = STATE_EXPANDED
            } else if (isHideable && shouldHide(releasedChild, yvel)) {
                top = mParentHeight
                targetState = STATE_HIDDEN
            } else if (yvel == 0f) {
                val currentTop = releasedChild.top
                if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
                    top = mMinOffset
                    targetState = STATE_EXPANDED
                } else {
                    top = mMaxOffset
                    targetState = STATE_COLLAPSED
                }
            } else {
                top = mMaxOffset
                targetState = STATE_COLLAPSED
            }
            if (mViewDragHelper!!.settleCapturedViewAt(releasedChild.left, top)) {
                setStateInternal(STATE_SETTLING)
                ViewCompat.postOnAnimation(releasedChild,
                        SettleRunnable(releasedChild, targetState))
            } else {
                setStateInternal(targetState)
            }
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return constrain(top, mMinOffset, if (isHideable) mParentHeight else mMaxOffset)
        }

        private fun constrain(amount: Int, low: Int, high: Int): Int {
            return if (amount < low) low else if (amount > high) high else amount
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return child.left
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return if (isHideable) {
                mParentHeight - mMinOffset
            } else {
                mMaxOffset - mMinOffset
            }
        }
    }

    /**
     * Callback for monitoring events about bottom sheets.
     */
    abstract class BottomSheetCallback {

        abstract fun onStateChanged(bottomSheet: View, newState: Int)


        abstract fun onSlide(bottomSheet: View, slideOffset: Float)
    }

    annotation class State
    constructor() {}


    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs,
                R.styleable.BottomSheetBehavior_Layout)
        val value = a.peekValue(R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight)
        if (value != null && value.data == PEEK_HEIGHT_AUTO) {
            peekHeight = value.data
        } else {
            peekHeight = a.getDimensionPixelSize(
                    R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, PEEK_HEIGHT_AUTO)
        }
        isHideable = a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false)
        skipCollapsed = a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed,
                false)
        a.recycle()
        val configuration = ViewConfiguration.get(context)
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity.toFloat()
    }

    override fun onSaveInstanceState(parent: CoordinatorLayout, child: V): Parcelable? {
        return SavedState(super.onSaveInstanceState(parent, child)!!, mState)
    }

    override fun onRestoreInstanceState(parent: CoordinatorLayout, child: V, state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(parent, child, ss.superState!!)
        // Intermediate states are restored as collapsed state
        if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
            mState = STATE_COLLAPSED
        } else {
            mState = ss.state
        }
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
            ViewCompat.setFitsSystemWindows(child, true)
        }
        val savedTop = child.top
        // First let the parent lay it out
        parent.onLayoutChild(child, layoutDirection)
        // Offset the bottom sheet
        mParentHeight = parent.height
        val peekHeight: Int
        if (mPeekHeightAuto) {
            if (peekHeightMin == 0) {
                peekHeightMin = parent.resources.getDimensionPixelSize(
                        R.dimen.design_bottom_sheet_peek_height_min)
            }
            peekHeight = Math.max(peekHeightMin, mParentHeight - parent.width * 9 / 16)
        } else {
            peekHeight = mPeekHeight
        }
        mMinOffset = Math.max(0, mParentHeight - child.height)
        mMaxOffset = Math.max(mParentHeight - peekHeight, mMinOffset)
        if (mState == STATE_EXPANDED) {
            ViewCompat.offsetTopAndBottom(child, mMinOffset)
        } else if (isHideable && mState == STATE_HIDDEN) {
            ViewCompat.offsetTopAndBottom(child, mParentHeight)
        } else if (mState == STATE_COLLAPSED) {
            ViewCompat.offsetTopAndBottom(child, mMaxOffset)
        } else if (mState == STATE_DRAGGING || mState == STATE_SETTLING) {
            ViewCompat.offsetTopAndBottom(child, savedTop - child.top)
        }
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)
        }
        mViewRef = WeakReference(child)
        mNestedScrollingChildRef = WeakReference<View>(findScrollingChild(child))
        return true
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            mIgnoreEvents = true
            return false
        }
        val action = MotionEventCompat.getActionMasked(event)
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        when (action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mTouchingScrollingChild = false
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                // Reset the ignore flag
                if (mIgnoreEvents) {
                    mIgnoreEvents = false
                    return false
                }
            }
            MotionEvent.ACTION_DOWN -> {
                val initialX = event.x.toInt()
                mInitialY = event.y.toInt()
                val scroll = mNestedScrollingChildRef.get()
                if (scroll != null && parent.isPointInChildBounds(scroll, initialX, mInitialY)) {
                    mActivePointerId = event.getPointerId(event.actionIndex)
                    mTouchingScrollingChild = true
                }
                mIgnoreEvents = mActivePointerId == MotionEvent.INVALID_POINTER_ID && !parent.isPointInChildBounds(child, initialX, mInitialY)
            }
        }
        if (!mIgnoreEvents && mViewDragHelper!!.shouldInterceptTouchEvent(event)) {
            return true
        }
        // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
        // it is not the top most view of its parent. This is not necessary when the touch event is
        // happening over the scrolling content as nested scrolling logic handles that case.
        val scroll = mNestedScrollingChildRef.get()
        return action == MotionEvent.ACTION_MOVE && scroll != null &&
                !mIgnoreEvents && mState != STATE_DRAGGING &&
                !parent.isPointInChildBounds(scroll, event.x.toInt(), event.y.toInt()) &&
                Math.abs(mInitialY - event.y) > mViewDragHelper!!.touchSlop
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!child.isShown) {
            return false
        }
        val action = MotionEventCompat.getActionMasked(event)
        if (mState == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
            return true
        }
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, mDragCallback)
        }
        mViewDragHelper!!.processTouchEvent(event)
        // Record the velocity
        if (action == MotionEvent.ACTION_DOWN) {
            reset()
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
        // to capture the bottom sheet in case it is not captured and the touch slop is passed.
        if (action == MotionEvent.ACTION_MOVE && !mIgnoreEvents) {
            if (Math.abs(mInitialY - event.y) > mViewDragHelper!!.touchSlop) {
                mViewDragHelper!!.captureChildView(child, event.getPointerId(event.actionIndex))
            }
        }
        return !mIgnoreEvents
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: V,
                                     directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        mLastNestedScrollDy = 0
        mNestedScrolled = false
        return nestedScrollAxes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View, dx: Int,
                                   dy: Int, consumed: IntArray) {
        val scrollingChild = mNestedScrollingChildRef.get()
        if (target !== scrollingChild) {
            return
        }
        val currentTop = child.top
        val newTop = currentTop - dy
        if (dy > 0) { // Upward
            if (newTop < mMinOffset) {
                consumed[1] = currentTop - mMinOffset
                ViewCompat.offsetTopAndBottom(child, -consumed[1])
                setStateInternal(STATE_EXPANDED)
            } else {
                consumed[1] = dy
                ViewCompat.offsetTopAndBottom(child, -dy)
                setStateInternal(STATE_DRAGGING)
            }
        } else if (dy < 0) { // Downward
            if (!ViewCompat.canScrollVertically(target, -1)) {
                if (newTop <= mMaxOffset || isHideable) {
                    consumed[1] = dy
                    ViewCompat.offsetTopAndBottom(child, -dy)
                    setStateInternal(STATE_DRAGGING)
                } else {
                    consumed[1] = currentTop - mMaxOffset
                    ViewCompat.offsetTopAndBottom(child, -consumed[1])
                    setStateInternal(STATE_COLLAPSED)
                }
            }
        }
        dispatchOnSlide(child.top)
        mLastNestedScrollDy = dy
        mNestedScrolled = true
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, child: V, target: View) {
        if (child.top == mMinOffset) {
            setStateInternal(STATE_EXPANDED)
            return
        }
        if (target !== mNestedScrollingChildRef.get() || !mNestedScrolled) {
            return
        }
        val top: Int
        val targetState: Int
        if (mLastNestedScrollDy > 0) {
            top = mMinOffset
            targetState = STATE_EXPANDED
        } else if (isHideable && shouldHide(child, yVelocity)) {
            top = mParentHeight
            targetState = STATE_HIDDEN
        } else if (mLastNestedScrollDy == 0) {
            val currentTop = child.top
            if (Math.abs(currentTop - mMinOffset) < Math.abs(currentTop - mMaxOffset)) {
                top = mMinOffset
                targetState = STATE_EXPANDED
            } else {
                top = mMaxOffset
                targetState = STATE_COLLAPSED
            }
        } else {
            top = mMaxOffset
            targetState = STATE_COLLAPSED
        }
        if (mViewDragHelper!!.smoothSlideViewTo(child, child.left, top)) {
            setStateInternal(STATE_SETTLING)
            ViewCompat.postOnAnimation(child, SettleRunnable(child, targetState))
        } else {
            setStateInternal(targetState)
        }
        mNestedScrolled = false
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, target: View,
                                  velocityX: Float, velocityY: Float): Boolean {
        return target === mNestedScrollingChildRef.get() && (mState != STATE_EXPANDED || super.onNestedPreFling(coordinatorLayout, child, target,
                velocityX, velocityY))
    }

    internal fun invalidateScrollingChild() {
        val scrollingChild = findScrollingChild(mViewRef!!.get())
        mNestedScrollingChildRef = WeakReference<View>(scrollingChild)
    }


    fun setBottomSheetCallback(callback: BottomSheetCallback) {
        mCallback = callback
    }

    internal fun setStateInternal(state: Int) {
        if (mState == state) {
            return
        }
        mState = state
        val bottomSheet = mViewRef!!.get()
        if (bottomSheet != null && mCallback != null) {
            mCallback!!.onStateChanged(bottomSheet, state)
        }
    }

    private fun reset() {
        mActivePointerId = ViewDragHelper.INVALID_POINTER
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    internal fun shouldHide(child: View, yvel: Float): Boolean {
        if (skipCollapsed) {
            return true
        }
        if (child.top < mMaxOffset) {
            // It should not hide, but collapse.
            return false
        }
        val newTop = child.top + yvel * HIDE_FRICTION
        return Math.abs(newTop - mMaxOffset) / mPeekHeight.toFloat() > HIDE_THRESHOLD
    }

    private fun findScrollingChild(view: View?): View? {
        if (view is NestedScrollingChild) {
            return view
        }
        if (view is ViewPager) {
            val viewPager = view as ViewPager?
            val currentViewPagerChild = ViewPagerUtils.getCurrentView(viewPager!!)
            val scrollingChild = findScrollingChild(currentViewPagerChild)
            if (scrollingChild != null) {
                return scrollingChild
            }
        } else if (view is ViewGroup) {
            val group = view as ViewGroup?
            var i = 0
            val count = group!!.childCount
            while (i < count) {
                val scrollingChild = findScrollingChild(group.getChildAt(i))
                if (scrollingChild != null) {
                    return scrollingChild
                }
                i++
            }
        }
        return null
    }

    internal fun startSettlingAnimation(child: View?, state: Int) {
        val top: Int
        if (state == STATE_COLLAPSED) {
            top = mMaxOffset
        } else if (state == STATE_EXPANDED) {
            top = mMinOffset
        } else if (isHideable && state == STATE_HIDDEN) {
            top = mParentHeight
        } else {
            throw IllegalArgumentException("Illegal state argument: $state")
        }
        setStateInternal(STATE_SETTLING)
        if (mViewDragHelper!!.smoothSlideViewTo(child!!, child.left, top)) {
            ViewCompat.postOnAnimation(child, SettleRunnable(child, state))
        }
    }

    internal fun dispatchOnSlide(top: Int) {
        val bottomSheet = mViewRef!!.get()
        if (bottomSheet != null && mCallback != null) {
            if (top > mMaxOffset) {
                mCallback!!.onSlide(bottomSheet, (mMaxOffset - top).toFloat() / (mParentHeight - mMaxOffset))
            } else {
                mCallback!!.onSlide(bottomSheet,
                        (mMaxOffset - top).toFloat() / (mMaxOffset - mMinOffset))
            }
        }
    }

    private inner class SettleRunnable internal constructor(private val mView: View, private val mTargetState: Int) : Runnable {

        override fun run() {
            if (mViewDragHelper != null && mViewDragHelper!!.continueSettling(true)) {
                ViewCompat.postOnAnimation(mView, this)
            } else {
                setStateInternal(mTargetState)
            }
        }
    }

    protected class SavedState : AbsSavedState {

        internal val state: Int

        @JvmOverloads constructor(source: Parcel, loader: ClassLoader? = null) : super(source, loader) {

            state = source.readInt()
        }

        constructor(superState: Parcelable, state: Int) : super(superState) {
            this.state = state
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(state)
        }

    }

    companion object {

        /**
         * The bottom sheet is dragging.
         */
        val STATE_DRAGGING = 1

        /**
         * The bottom sheet is settling.
         */
        val STATE_SETTLING = 2


        val STATE_EXPANDED = 3


        val STATE_COLLAPSED = 4


        val STATE_HIDDEN = 5


        val PEEK_HEIGHT_AUTO = -1

        private val HIDE_THRESHOLD = 0.5f

        private val HIDE_FRICTION = 0.1f


        fun <V : View> from(view: V): ViewPagerBottomSheetBehavior<V> {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
                    ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            val behavior = params
                    .behavior as? ViewPagerBottomSheetBehavior<*> ?: throw IllegalArgumentException(
                    "The view is not associated with ViewPagerBottomSheetBehavior")
            return behavior as ViewPagerBottomSheetBehavior<V>
        }
    }

}

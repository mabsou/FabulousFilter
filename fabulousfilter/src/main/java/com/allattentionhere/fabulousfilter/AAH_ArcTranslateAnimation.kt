package com.allattentionhere.fabulousfilter

import android.graphics.PointF
import android.view.animation.Animation
import android.view.animation.Transformation

/**
 * Created by krupenghetiya on 19/06/17.
 */

class AAH_ArcTranslateAnimation : Animation {
    private var mFromXType = Animation.ABSOLUTE
    private var mToXType = Animation.ABSOLUTE

    private var mFromYType = Animation.ABSOLUTE
    private var mToYType = Animation.ABSOLUTE

    private var mFromXValue = 0.0f
    private var mToXValue = 0.0f

    private var mFromYValue = 0.0f
    private var mToYValue = 0.0f

    private var mFromXDelta: Float = 0.toFloat()
    private var mToXDelta: Float = 0.toFloat()
    private var mFromYDelta: Float = 0.toFloat()
    private var mToYDelta: Float = 0.toFloat()

    private var mStart: PointF? = null
    private var mControl: PointF? = null
    private var mEnd: PointF? = null

    /**
     * Constructor to use when building a ArcTranslateAnimation from code
     *
     * @param fromXDelta
     * Change in X coordinate to apply at the start of the animation
     * @param toXDelta
     * Change in X coordinate to apply at the end of the animation
     * @param fromYDelta
     * Change in Y coordinate to apply at the start of the animation
     * @param toYDelta
     * Change in Y coordinate to apply at the end of the animation
     */
    constructor(fromXDelta: Float, toXDelta: Float,
                fromYDelta: Float, toYDelta: Float) {
        mFromXValue = fromXDelta
        mToXValue = toXDelta
        mFromYValue = fromYDelta
        mToYValue = toYDelta

        mFromXType = Animation.ABSOLUTE
        mToXType = Animation.ABSOLUTE
        mFromYType = Animation.ABSOLUTE
        mToYType = Animation.ABSOLUTE
    }

    /**
     * Constructor to use when building a ArcTranslateAnimation from code
     *
     * @param fromXType
     * Specifies how fromXValue should be interpreted. One of
     * Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     * Animation.RELATIVE_TO_PARENT.
     * @param fromXValue
     * Change in X coordinate to apply at the start of the animation.
     * This value can either be an absolute number if fromXType is
     * ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     * @param toXType
     * Specifies how toXValue should be interpreted. One of
     * Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     * Animation.RELATIVE_TO_PARENT.
     * @param toXValue
     * Change in X coordinate to apply at the end of the animation.
     * This value can either be an absolute number if toXType is
     * ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     * @param fromYType
     * Specifies how fromYValue should be interpreted. One of
     * Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     * Animation.RELATIVE_TO_PARENT.
     * @param fromYValue
     * Change in Y coordinate to apply at the start of the animation.
     * This value can either be an absolute number if fromYType is
     * ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     * @param toYType
     * Specifies how toYValue should be interpreted. One of
     * Animation.ABSOLUTE, Animation.RELATIVE_TO_SELF, or
     * Animation.RELATIVE_TO_PARENT.
     * @param toYValue
     * Change in Y coordinate to apply at the end of the animation.
     * This value can either be an absolute number if toYType is
     * ABSOLUTE, or a percentage (where 1.0 is 100%) otherwise.
     */
    constructor(fromXType: Int, fromXValue: Float, toXType: Int,
                toXValue: Float, fromYType: Int, fromYValue: Float, toYType: Int,
                toYValue: Float) {

        mFromXValue = fromXValue
        mToXValue = toXValue
        mFromYValue = fromYValue
        mToYValue = toYValue

        mFromXType = fromXType
        mToXType = toXType
        mFromYType = fromYType
        mToYType = toYType
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val dx = calcBezier(interpolatedTime, mStart!!.x, mControl!!.x, mEnd!!.x).toFloat()
        val dy = calcBezier(interpolatedTime, mStart!!.y, mControl!!.y, mEnd!!.y).toFloat()

        t.matrix.setTranslate(dx, dy)
    }

    override fun initialize(width: Int, height: Int, parentWidth: Int,
                            parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        mFromXDelta = resolveSize(mFromXType, mFromXValue, width, parentWidth)
        mToXDelta = resolveSize(mToXType, mToXValue, width, parentWidth)
        mFromYDelta = resolveSize(mFromYType, mFromYValue, height, parentHeight)
        mToYDelta = resolveSize(mToYType, mToYValue, height, parentHeight)

        mStart = PointF(mFromXDelta, mFromYDelta)
        mEnd = PointF(mToXDelta, mToYDelta)
        mControl = PointF(mFromXDelta, mToYDelta) // How to choose the
        // Control point(we can
        // use the cross of the
        // two tangents from p0,
        // p1)
    }

    /**
     * Calculate the position on a quadratic bezier curve by given three points
     * and the percentage of time passed.
     *
     * from http://en.wikipedia.org/wiki/B%C3%A9zier_curve
     *
     * @param interpolatedTime
     * the fraction of the duration that has passed where 0 <= time
     * <= 1
     * @param p0
     * a single dimension of the starting point
     * @param p1
     * a single dimension of the control point
     * @param p2
     * a single dimension of the ending point
     */
    private fun calcBezier(interpolatedTime: Float, p0: Float, p1: Float, p2: Float): Long {
        return Math.round(Math.pow((1 - interpolatedTime).toDouble(), 2.0) * p0
                + (2f * (1 - interpolatedTime) * interpolatedTime * p1).toDouble()
                + Math.pow(interpolatedTime.toDouble(), 2.0) * p2)
    }

}
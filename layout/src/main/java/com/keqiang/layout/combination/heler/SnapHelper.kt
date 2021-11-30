package com.keqiang.layout.combination.heler

import android.util.DisplayMetrics
import android.view.View
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnFlingListener
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import kotlin.math.abs

/**
 * 辅助实现[RecyclerView] fling，与[androidx.recyclerview.widget.SnapHelper]差异在于支持设置差值器，
 * 不在滑动结束时展示完整的Item
 */
abstract class SnapHelper : OnFlingListener() {
    var mRecyclerView: RecyclerView? = null
    private var mGravityScroller: Scroller? = null
    private var mInterpolator: Interpolator = sQuinticInterpolator

    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
        mRecyclerView?.adapter ?: return false
        val layoutManager = mRecyclerView?.layoutManager ?: return false
        val minFlingVelocity = mRecyclerView?.minFlingVelocity ?: 50
        return ((abs(velocityY) > minFlingVelocity || abs(velocityX) > minFlingVelocity)
            && snapFromFling(layoutManager, velocityX, velocityY))
    }

    /**
     * Attaches the [androidx.recyclerview.widget.SnapHelper] to the provided RecyclerView, by calling
     * [RecyclerView.setOnFlingListener].
     * You can call this method with `null` to detach it from the current RecyclerView.
     *
     * @param recyclerView The RecyclerView instance to which you want to add this helper or
     * `null` if you want to remove SnapHelper from the current
     * RecyclerView.
     * @throws IllegalArgumentException if there is already a [RecyclerView.OnFlingListener]
     * attached to the provided [RecyclerView].
     */
    @Throws(IllegalStateException::class)
    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        if (mRecyclerView === recyclerView) {
            return  // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks()
        }
        mRecyclerView = recyclerView
        if (mRecyclerView != null) {
            setupCallbacks()
            mGravityScroller = Scroller(mRecyclerView!!.context,
                mInterpolator)
            snapToTargetExistingView()
        }
    }

    /**
     * Called when an instance of a [RecyclerView] is attached.
     */
    @Throws(IllegalStateException::class)
    private fun setupCallbacks() {
        check(mRecyclerView!!.onFlingListener == null) { "An instance of OnFlingListener already set." }
        mRecyclerView!!.onFlingListener = this
    }

    /**
     * Called when the instance of a [RecyclerView] is detached.
     */
    private fun destroyCallbacks() {
        mRecyclerView!!.onFlingListener = null
    }

    /**
     * Calculated the estimated scroll distance in each direction given velocities on both axes.
     *
     * @param velocityX Fling velocity on the horizontal axis.
     * @param velocityY Fling velocity on the vertical axis.
     * @return array holding the calculated distances in x and y directions
     * respectively.
     */
    fun calculateScrollDistance(velocityX: Int, velocityY: Int): IntArray {
        val outDist = IntArray(2)
        mGravityScroller!!.fling(0, 0, velocityX, velocityY, Int.MIN_VALUE, Int.MAX_VALUE, Int.MIN_VALUE, Int.MAX_VALUE)
        outDist[0] = mGravityScroller!!.finalX
        outDist[1] = mGravityScroller!!.finalY
        return outDist
    }

    /**
     * Helper method to facilitate for snapping triggered by a fling.
     *
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView].
     * @param velocityX     Fling velocity on the horizontal axis.
     * @param velocityY     Fling velocity on the vertical axis.
     * @return true if it is handled, false otherwise.
     */
    private fun snapFromFling(layoutManager: RecyclerView.LayoutManager, velocityX: Int,
                              velocityY: Int): Boolean {
        if (layoutManager !is ScrollVectorProvider) {
            return false
        }
        val smoothScroller = createScroller(layoutManager) ?: return false
        val targetPosition = findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (targetPosition == RecyclerView.NO_POSITION) {
            return false
        }
        smoothScroller.targetPosition = targetPosition
        layoutManager.startSmoothScroll(smoothScroller)
        return true
    }

    /**
     * Snaps to a target view which currently exists in the attached [RecyclerView]. This
     * method is used to snap the view when the [RecyclerView] is first attached; when
     * snapping was triggered by a scroll and when the fling is at its final stages.
     */
    fun snapToTargetExistingView() {
        if (mRecyclerView == null) {
            return
        }
        val layoutManager = mRecyclerView!!.layoutManager ?: return
        val snapView = findSnapView(layoutManager) ?: return
        val snapDistance = calculateDistanceToFinalSnap(layoutManager, snapView)
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            mRecyclerView!!.smoothScrollBy(snapDistance[0], snapDistance[1])
        }
    }

    /**
     * Creates a scroller to be used in the snapping implementation.
     *
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView].
     * @return a [RecyclerView.SmoothScroller] which will handle the scrolling.
     */
    protected fun createScroller(layoutManager: RecyclerView.LayoutManager): SmoothScroller? {
        return if (layoutManager !is ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView!!.context) {
            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
                if (mRecyclerView == null) {
                    // The associated RecyclerView has been removed so there is no action to take.
                    return
                }
                val snapDistances = calculateDistanceToFinalSnap(layoutManager, targetView)
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }
    }

    fun setInterpolator(interpolator: Interpolator?) {
        mInterpolator = interpolator ?: sQuinticInterpolator
    }

    /**
     * Override this method to snap to a particular point within the target view or the container
     * view on any axis.
     *
     *
     * This method is called when the [androidx.recyclerview.widget.SnapHelper] has intercepted a fling and it needs
     * to know the exact distance required to scroll by in order to snap to the target view.
     *
     * @param layoutManager the [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView]
     * @param targetView    the target view that is chosen as the view to snap
     * @return the output coordinates the put the result into. out[0] is the distance
     * on horizontal axis and out[1] is the distance on vertical axis.
     */
    abstract fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray

    /**
     * Override this method to provide a particular target view for snapping.
     *
     *
     * This method is called when the [androidx.recyclerview.widget.SnapHelper] is ready to start snapping and requires
     * a target view to snap to. It will be explicitly called when the scroll state becomes idle
     * after a scroll. It will also be called when the [androidx.recyclerview.widget.SnapHelper] is preparing to snap
     * after a fling and requires a reference view from the current set of child views.
     *
     *
     * If this method returns `null`, SnapHelper will not snap to any view.
     *
     * @param layoutManager the [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView]
     * @return the target view to which to snap on fling or end of scroll
     */
    abstract fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View?

    /**
     * Override to provide a particular adapter target position for snapping.
     *
     * @param layoutManager the [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView]
     * @param velocityX     fling velocity on the horizontal axis
     * @param velocityY     fling velocity on the vertical axis
     * @return the target adapter position to you want to snap or [RecyclerView.NO_POSITION]
     * if no snapping should happen
     */
    abstract fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int

    companion object {
        const val MILLISECONDS_PER_INCH = 100f

        /**
         * 时间五次方差值器，实现fling操作减速效果
         */
        val sQuinticInterpolator = Interpolator { t: Float ->
            val time = t - 1.0f
            time * time * time * time * time + 1.0f
        }
    }
}
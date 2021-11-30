package com.keqiang.layout.combination.heler

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 逻辑与[androidx.recyclerview.widget.LinearSnapHelper]基本一致，唯一差别就是查找目标view时与[RecyclerView]底部比较，而不是中间
 */
internal class LinearSnapHelper : SnapHelper() {
    // Orientation helpers are lazily created per LayoutManager.
    private var mVerticalHelper: OrientationHelper? = null
    private var mHorizontalHelper: OrientationHelper? = null
    private var mUseOriginal = true

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager, targetView: View): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToEnd(targetView,
                getHorizontalHelper(layoutManager))
        }
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToEnd(targetView,
                getVerticalHelper(layoutManager))
        }
        return out
    }

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int {
        if (layoutManager !is ScrollVectorProvider) {
            return RecyclerView.NO_POSITION
        }
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val currentPosition = layoutManager.getPosition(currentView)
        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }
        val vectorProvider = layoutManager as ScrollVectorProvider
        // deltaJumps sign comes from the velocity which may not match the order of children in
        // the LayoutManager. To overcome this, we ask for a vector from the LayoutManager to
        // get the direction.
        val vectorForEnd = vectorProvider.computeScrollVectorForPosition(itemCount - 1)
            ?: // cannot get a vector for the given position.
            return RecyclerView.NO_POSITION
        var vDeltaJump: Int
        var hDeltaJump: Int
        if (layoutManager.canScrollHorizontally()) {
            hDeltaJump = estimateNextPositionDiffForFling(layoutManager,
                getHorizontalHelper(layoutManager), velocityX, 0)
            if (vectorForEnd.x < 0) {
                hDeltaJump = -hDeltaJump
            }
        } else {
            hDeltaJump = 0
        }
        if (layoutManager.canScrollVertically()) {
            vDeltaJump = estimateNextPositionDiffForFling(layoutManager,
                getVerticalHelper(layoutManager), 0, velocityY)
            if (vectorForEnd.y < 0) {
                vDeltaJump = -vDeltaJump
            }
        } else {
            vDeltaJump = 0
        }
        val deltaJump = if (layoutManager.canScrollVertically()) vDeltaJump else hDeltaJump
        if (deltaJump == 0) {
            return RecyclerView.NO_POSITION
        }
        var targetPos = currentPosition + deltaJump
        if (targetPos < 0) {
            targetPos = 0
        }
        if (targetPos >= itemCount) {
            targetPos = itemCount - 1
        }
        return targetPos
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        layoutManager ?: return null
        if (layoutManager.canScrollVertically()) {
            return findEndView(layoutManager, getVerticalHelper(layoutManager))
        } else if (layoutManager.canScrollHorizontally()) {
            return findEndView(layoutManager, getHorizontalHelper(layoutManager))
        }
        return null
    }

    private fun distanceToEnd(targetView: View, helper: OrientationHelper): Int {
        val childCenter = helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView)
        val containerCenter = helper.startAfterPadding + helper.totalSpace
        return childCenter - containerCenter
    }

    /**
     * Estimates a position to which SnapHelper will try to scroll to in response to a fling.
     *
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView].
     * @param helper        The [OrientationHelper] that is created from the LayoutManager.
     * @param velocityX     The velocity on the x axis.
     * @param velocityY     The velocity on the y axis.
     * @return The diff between the target scroll position and the current position.
     */
    private fun estimateNextPositionDiffForFling(layoutManager: RecyclerView.LayoutManager,
                                                 helper: OrientationHelper, velocityX: Int, velocityY: Int): Int {
        val distances = calculateScrollDistance(velocityX, velocityY)
        val distancePerChild = computeDistancePerChild(layoutManager, helper)
        if (distancePerChild <= 0) {
            return 0
        }
        val distance = if (abs(distances[0]) > abs(distances[1])) distances[0] else distances[1]
        return (distance / distancePerChild).roundToInt()
    }

    /**
     * Return the child view that is currently closest to the end of this parent.
     *
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView].
     * @param helper        The relevant [OrientationHelper] for the attached [RecyclerView].
     * @return the child view that is currently closest to the end of this parent.
     */
    private fun findEndView(layoutManager: RecyclerView.LayoutManager, helper: OrientationHelper): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }
        var closestChild: View? = null
        val end = helper.startAfterPadding + helper.totalSpace
        var absClosest = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childEnd = helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child)
            val absDistance = abs(childEnd - end)

            /* if child end is closer than previous closest, set it as closest  */
            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    /**
     * Computes an average pixel value to pass a single child.
     *
     *
     * Returns a negative value if it cannot be calculated.
     *
     * @param layoutManager The [RecyclerView.LayoutManager] associated with the attached
     * [RecyclerView].
     * @param helper        The relevant [OrientationHelper] for the attached
     * [RecyclerView.LayoutManager].
     * @return A float value that is the average number of pixels needed to scroll by one view in
     * the relevant direction.
     */
    private fun computeDistancePerChild(layoutManager: RecyclerView.LayoutManager,
                                        helper: OrientationHelper): Float {
        var minPosView: View? = null
        var maxPosView: View? = null
        var minPos = Int.MAX_VALUE
        var maxPos = Int.MIN_VALUE
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return INVALID_DISTANCE
        }
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val pos = layoutManager.getPosition(child)
            if (pos == RecyclerView.NO_POSITION) {
                continue
            }
            if (pos < minPos) {
                minPos = pos
                minPosView = child
            }
            if (pos > maxPos) {
                maxPos = pos
                maxPosView = child
            }
        }
        if (minPosView == null || maxPosView == null) {
            return INVALID_DISTANCE
        }
        val start = helper.getDecoratedStart(minPosView).coerceAtMost(helper.getDecoratedStart(maxPosView))
        val end = helper.getDecoratedEnd(minPosView).coerceAtLeast(helper.getDecoratedEnd(maxPosView))
        val distance = end - start
        return if (distance == 0) {
            INVALID_DISTANCE
        } else 1f * distance / (maxPos - minPos + 1)
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mVerticalHelper == null || mVerticalHelper!!.layoutManager !== layoutManager) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return mVerticalHelper!!
    }

    private fun getHorizontalHelper(
        layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (mHorizontalHelper == null || mHorizontalHelper!!.layoutManager !== layoutManager) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return mHorizontalHelper!!
    }

    override fun onFling(velocityX: Int, velocityY: Int): Boolean {
        if (mUseOriginal) {
            return false
        }
        val fling = super.onFling(velocityX, velocityY)
        mUseOriginal = true
        return fling
    }

    /**
     * 如果[useOriginal] 是否使用[RecyclerView]自己的fling逻辑
     */
    fun onFling(velocityX: Int, velocityY: Int, useOriginal: Boolean): Boolean {
        mUseOriginal = useOriginal
        return onFling(velocityX, velocityY)
    }

    companion object {
        private const val INVALID_DISTANCE = 1f
    }
}
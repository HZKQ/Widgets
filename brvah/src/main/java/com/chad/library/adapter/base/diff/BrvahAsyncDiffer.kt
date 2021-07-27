package com.chad.library.adapter.base.diff

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.ListUpdateCallback
import com.chad.library.adapter.base.BaseQuickAdapter
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executor

/**
 * Helper for computing the difference between two lists via [DiffUtil] on a background thread.
 * 该类参考[AsyncListDiffer]定制
 */
class BrvahAsyncDiffer<T>(private val adapter: BaseQuickAdapter<T, *>,
                          private val config: BrvahAsyncDifferConfig<T>) {

    private val mUpdateCallback: ListUpdateCallback = BrvahListUpdateCallback(adapter)

    private class MainThreadExecutor : Executor {
        val mHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mHandler.post(command)
        }
    }

    private var mMainThreadExecutor: Executor = config.mainThreadExecutor ?: sMainThreadExecutor

    private val mListeners: MutableList<ListChangeListener<T>> = CopyOnWriteArrayList()

    private var mMaxScheduledGeneration = 0

    fun addData(index: Int, data: T) {
        val previousList: List<T> = adapter.data
        adapter.data.add(index, data)
        mUpdateCallback.onInserted(index, 1)
        onCurrentListChanged(previousList, null)
    }

    fun addData(data: T) {
        val previousList: List<T> = adapter.data
        adapter.data.add(data)
        mUpdateCallback.onInserted(previousList.size, 1)
        onCurrentListChanged(previousList, null)
    }

    fun addList(list: List<T>?) {
        if (list == null) return
        val previousList: List<T> = adapter.data
        adapter.data.addAll(list)
        mUpdateCallback.onInserted(previousList.size, list.size)
        onCurrentListChanged(previousList, null)
    }

    /**
     * 改变某一个数据
     */
    fun changeData(index: Int, newData: T, payload: T?) {
        val previousList: List<T> = adapter.data
        adapter.data[index] = newData
        mUpdateCallback.onChanged(index, 1, payload)
        onCurrentListChanged(previousList, null)
    }

    /**
     * 移除某一个数据
     */
    fun removeAt(index: Int) {
        val previousList: List<T> = adapter.data
        adapter.data.removeAt(index)
        mUpdateCallback.onRemoved(index, 1)
        onCurrentListChanged(previousList, null)
    }

    fun remove(t: T) {
        val previousList: List<T> = adapter.data
        val index = adapter.data.indexOf(t)
        if (index == -1) return
        adapter.data.removeAt(index)
        mUpdateCallback.onRemoved(index, 1)
        onCurrentListChanged(previousList, null)
    }


    @JvmOverloads
    fun submitList(newList: List<T>?, commitCallback: Runnable? = null) {
        // incrementing generation means any currently-running diffs are discarded when they finish
        val runGeneration: Int = ++mMaxScheduledGeneration
        if (newList === adapter.data) {
            // nothing to do (Note - still had to inc generation, since may have ongoing work)
            commitCallback?.run()
            return
        }

        val oldList: List<T> = adapter.data
        // fast simple remove all
        if (newList == null) {
            val countRemoved: Int = adapter.data.size
            adapter.data = arrayListOf()
            // notify last, after list is updated
            mUpdateCallback.onRemoved(0, countRemoved)
            onCurrentListChanged(oldList, commitCallback)
            return
        }
        // fast simple first insert
        if (adapter.data.isEmpty()) {
            adapter.data = newList.toMutableList()
            // notify last, after list is updated
            mUpdateCallback.onInserted(0, newList.size)
            onCurrentListChanged(oldList, commitCallback)
            return
        }

        config.backgroundThreadExecutor.execute {
            val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    return oldList.size
                }

                override fun getNewListSize(): Int {
                    return newList.size
                }

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    return if (oldItem != null && newItem != null) {
                        config.diffCallback.areItemsTheSame(oldItem, newItem)
                    } else oldItem == null && newItem == null
                    // If both items are null we consider them the same.
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    if (oldItem != null && newItem != null) {
                        return config.diffCallback.areContentsTheSame(oldItem, newItem)
                    }
                    if (oldItem == null && newItem == null) {
                        return true
                    }
                    throw AssertionError()
                }

                override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                    val oldItem: T? = oldList[oldItemPosition]
                    val newItem: T? = newList[newItemPosition]
                    if (oldItem != null && newItem != null) {
                        return config.diffCallback.getChangePayload(oldItem, newItem)
                    }
                    throw AssertionError()
                }
            })
            mMainThreadExecutor.execute {
                if (mMaxScheduledGeneration == runGeneration) {
                    latchList(newList, result, commitCallback)
                }
            }
        }
    }

    private fun latchList(
        newList: List<T>,
        diffResult: DiffResult,
        commitCallback: Runnable?) {
        val previousList: List<T> = adapter.data
        adapter.data = newList.toMutableList()

        diffResult.dispatchUpdatesTo(mUpdateCallback)
        onCurrentListChanged(previousList, commitCallback)
    }

    private fun onCurrentListChanged(previousList: List<T>,
                                     commitCallback: Runnable?) {
        for (listener in mListeners) {
            listener.onCurrentListChanged(previousList, adapter.data)
        }
        commitCallback?.run()
    }

    /**
     * Add a ListListener to receive updates when the current List changes.
     *
     * @param listener Listener to receive updates.
     *
     * @see [removeListListener]
     */
    fun addListListener(listener: ListChangeListener<T>) {
        mListeners.add(listener)
    }

    /**
     * Remove a previously registered ListListener.
     *
     * @param listener Previously registered listener.
     * @see [addListListener]
     */
    fun removeListListener(listener: ListChangeListener<T>) {
        mListeners.remove(listener)
    }

    /**
     * Remove all previously registered ListListener.
     * @see [addListListener]
     * @see [removeListListener]
     */
    fun clearAllListListener() {
        mListeners.clear()
    }

    companion object {
        // TODO: use MainThreadExecutor from supportlib once one exists
        private val sMainThreadExecutor: Executor = MainThreadExecutor()
    }
}
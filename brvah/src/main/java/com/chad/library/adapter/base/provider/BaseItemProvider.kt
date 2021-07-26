package com.chad.library.adapter.base.provider

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.chad.library.adapter.base.BaseProviderMultiAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.util.getItemView
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.lang.ref.WeakReference
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * [BaseProviderMultiAdapter] 的Provider基类
 * @param T 数据类型
 */
abstract class BaseItemProvider<T, BH : BaseViewHolder> {

    lateinit var context: Context

    private var weakAdapter: WeakReference<BaseProviderMultiAdapter<BH>>? = null
    private val clickViewIds by lazy(LazyThreadSafetyMode.NONE) { ArrayList<Int>() }
    private val longClickViewIds by lazy(LazyThreadSafetyMode.NONE) { ArrayList<Int>() }

    internal fun setAdapter(adapter: BaseProviderMultiAdapter<BH>) {
        weakAdapter = WeakReference(adapter)
    }

    open fun getAdapter(): BaseProviderMultiAdapter<BH>? {
        return weakAdapter?.get()
    }

    abstract val itemViewType: Int

    abstract val layoutId: Int
        @LayoutRes
        get

    /**
     * 当前Item是否可以点击
     */
    var itemCouldClick: Boolean = false

    /**
     * 当前Item是否可以长按
     */
    var itemCouldLongClick: Boolean = false

    abstract fun convert(helper: BH, item: T)

    open fun convert(helper: BH, item: T, payloads: List<Any>) {}

    /**
     * （可选重写）创建 ViewHolder。
     * 默认实现返回[BaseViewHolder]，可重写返回自定义 ViewHolder
     *
     * @param parent
     */
    @Suppress("UNCHECKED_CAST")
    open fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BH {
        val view = parent.getItemView(layoutId)
        var temp: Class<*>? = javaClass
        var z: Class<*>? = null
        while (z == null && null != temp) {
            z = getInstancedGenericKClass(temp)
            temp = temp.superclass
        }
        // 泛型擦除会导致z为null
        val vh: BH? = if (z == null) {
            BaseViewHolder(view) as BH
        } else {
            createBaseGenericKInstance(z, view)
        }
        return vh ?: BaseViewHolder(view) as BH
    }

    /**
     * try to create Generic VH instance
     *
     * @param z
     * @param view
     * @return
     */
    @Suppress("UNCHECKED_CAST")
    private fun createBaseGenericKInstance(z: Class<*>, view: View): BH? {
        try {
            val constructor: Constructor<*>
            // inner and unstatic class
            return if (z.isMemberClass && !Modifier.isStatic(z.modifiers)) {
                constructor = z.getDeclaredConstructor(javaClass, View::class.java)
                constructor.isAccessible = true
                constructor.newInstance(this, view) as BH
            } else {
                constructor = z.getDeclaredConstructor(View::class.java)
                constructor.isAccessible = true
                constructor.newInstance(view) as BH
            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * get generic parameter VH
     *
     * @param z
     * @return
     */
    private fun getInstancedGenericKClass(z: Class<*>): Class<*>? {
        try {
            val type = z.genericSuperclass
            if (type is ParameterizedType) {
                val types = type.actualTypeArguments
                for (temp in types) {
                    if (temp is Class<*>) {
                        if (BaseViewHolder::class.java.isAssignableFrom(temp)) {
                            return temp
                        }
                    } else if (temp is ParameterizedType) {
                        val rawType = temp.rawType
                        if (rawType is Class<*> && BaseViewHolder::class.java.isAssignableFrom(rawType)) {
                            return rawType
                        }
                    }
                }
            }
        } catch (e: java.lang.reflect.GenericSignatureFormatError) {
            e.printStackTrace()
        } catch (e: TypeNotPresentException) {
            e.printStackTrace()
        } catch (e: java.lang.reflect.MalformedParameterizedTypeException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * （可选重写）ViewHolder创建完毕以后的回掉方法。
     * @param viewHolder VH
     */
    open fun onViewHolderCreated(viewHolder: BH, viewType: Int) {}

    /**
     * Called when a view created by this [BaseItemProvider] has been attached to a window.
     * 当此[BaseItemProvider]出现在屏幕上的时候，会调用此方法
     *
     * This can be used as a reasonable signal that the view is about to be seen
     * by the user. If the [BaseItemProvider] previously freed any resources in
     * [onViewDetachedFromWindow][.onViewDetachedFromWindow]
     * those resources should be restored here.
     *
     * @param holder Holder of the view being attached
     */
    open fun onViewAttachedToWindow(holder: BH) {}

    /**
     * Called when a view created by this [BaseItemProvider] has been detached from its
     * window.
     * 当此[BaseItemProvider]从屏幕上移除的时候，会调用此方法
     *
     * Becoming detached from the window is not necessarily a permanent condition;
     * the consumer of an Adapter's views may choose to cache views offscreen while they
     * are not visible, attaching and detaching them as appropriate.
     *
     * @param holder Holder of the view being detached
     */
    open fun onViewDetachedFromWindow(holder: BH) {}

    /**
     * item 若想实现条目点击事件则重写该方法。如果设置[itemCouldClick]为false或[weakAdapter]设置了
     * [BaseQuickAdapter.setOnItemChildClick],则此点击事件无效
     */
    open fun onClick(helper: BH, view: View, data: T, position: Int) {}

    /**
     * item 若想实现条目长按事件则重写该方法，如果不消耗此事件，则返回false,此时[onClick]接收此事件。
     * 如果设置[itemCouldLongClick]为false或[weakAdapter]设置了[BaseQuickAdapter.setOnItemLongClickListener],则此长按事件无效
     */
    open fun onLongClick(helper: BH, view: View, data: T, position: Int): Boolean {
        return false
    }

    /**
     * item 子视图若想实现条目点击事件则重写该方法。如果[weakAdapter]设置了[BaseQuickAdapter.setOnItemChildClickListener],
     * 则此点击事件无效
     */
    open fun onChildClick(helper: BH, view: View, data: T, position: Int) {}

    /**
     * item 子视图若想实现条目长按事件则重写该方法，如果不消耗此事件，则返回false,此时[onChildClick]接收此事件。
     * 如果[weakAdapter]设置了[BaseQuickAdapter.setOnItemLongClickListener],则此长按事件无效
     */
    open fun onChildLongClick(helper: BH, view: View, data: T, position: Int): Boolean {
        return false
    }

    /**
     * 新增需要实现点击事件子View Id
     */
    fun addChildClickViewIds(@IdRes vararg ids: Int) {
        ids.forEach {
            this.clickViewIds.add(it)
        }
    }

    fun getChildClickViewIds() = this.clickViewIds

    /**
     * 新增需要实现长按事件子View Id
     */
    fun addChildLongClickViewIds(@IdRes vararg ids: Int) {
        ids.forEach {
            this.longClickViewIds.add(it)
        }
    }

    fun getChildLongClickViewIds() = this.longClickViewIds
}
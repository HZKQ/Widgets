package com.chad.library.adapter.base.viewholder

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.SparseArray
import android.util.TypedValue
import android.view.View
import android.widget.Checkable
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import me.zhouzhuo810.magpiex.utils.ColorUtil

/**
 * ViewHolder 基类
 */
@Keep
open class BaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    /**
     * Views indexed with their IDs
     */
    private val views: SparseArray<View> = SparseArray()

    /**
     * 如果使用了 DataBinding 绑定 View，可调用此方法获取 [ViewDataBinding]
     *
     * Deprecated, Please use [BaseDataBindingHolder]
     *
     * @return B?
     */
    @Deprecated("Please use BaseDataBindingHolder class", ReplaceWith("DataBindingUtil.getBinding(itemView)", "androidx.databinding.DataBindingUtil"))
    open fun <B : ViewDataBinding> getBinding(): B? = DataBindingUtil.getBinding(itemView)


    open fun <T : View> getView(@IdRes viewId: Int): T {
        val view = getViewOrNull<T>(viewId)
        checkNotNull(view) { "No view found with id $viewId" }
        return view
    }

    @Suppress("UNCHECKED_CAST")
    open fun <T : View> getViewOrNull(@IdRes viewId: Int): T? {
        val view = views.get(viewId)
        if (view == null) {
            itemView.findViewById<T>(viewId)?.let {
                views.put(viewId, it)
                return it
            }
        }
        return view as? T
    }

    open fun <T : View> Int.findView(): T? {
        return itemView.findViewById(this)
    }

    open fun setText(@IdRes viewId: Int, value: CharSequence?): BaseViewHolder {
        getView<TextView>(viewId).text = value
        return this
    }

    open fun setText(@IdRes viewId: Int, @StringRes strId: Int): BaseViewHolder? {
        getView<TextView>(viewId).setText(strId)
        return this
    }

    open fun setTextColor(@IdRes viewId: Int, @ColorInt color: Int): BaseViewHolder {
        getView<TextView>(viewId).setTextColor(color)
        return this
    }

    open fun setTextColorRes(@IdRes viewId: Int, @ColorRes colorRes: Int): BaseViewHolder {
        getView<TextView>(viewId).setTextColor(itemView.resources.getColor(colorRes))
        return this
    }

    open fun setImageResource(@IdRes viewId: Int, @DrawableRes imageResId: Int): BaseViewHolder {
        getView<ImageView>(viewId).setImageResource(imageResId)
        return this
    }

    open fun setImageDrawable(@IdRes viewId: Int, drawable: Drawable?): BaseViewHolder {
        getView<ImageView>(viewId).setImageDrawable(drawable)
        return this
    }

    open fun setImageBitmap(@IdRes viewId: Int, bitmap: Bitmap?): BaseViewHolder {
        getView<ImageView>(viewId).setImageBitmap(bitmap)
        return this
    }

    open fun setIconColor(@IdRes viewId: Int, color: Int): BaseViewHolder {
        ColorUtil.setIconColor(getView<ImageView>(viewId), color)
        return this
    }

    open fun setBackgroundColor(@IdRes viewId: Int, @ColorInt color: Int): BaseViewHolder {
        getView<View>(viewId).setBackgroundColor(color)
        return this
    }

    open fun setBackgroundResource(@IdRes viewId: Int, @DrawableRes backgroundRes: Int): BaseViewHolder {
        getView<View>(viewId).setBackgroundResource(backgroundRes)
        return this
    }

    open fun setAlpha(@IdRes viewId: Int, alpha: Float): BaseViewHolder {
        val view = getView<View>(viewId)
        view.alpha = alpha
        return this
    }

    open fun setVisible(@IdRes viewId: Int, isVisible: Boolean): BaseViewHolder {
        val view = getView<View>(viewId)
        view.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        return this
    }

    open fun setGone(@IdRes viewId: Int, isVisible: Boolean): BaseViewHolder {
        val view = getView<View>(viewId)
        view.visibility = if (!isVisible) View.GONE else View.VISIBLE
        return this
    }

    open fun setEnabled(@IdRes viewId: Int, isEnabled: Boolean): BaseViewHolder {
        getView<View>(viewId).isEnabled = isEnabled
        return this
    }

    open fun setChecked(@IdRes viewId: Int, isChecked: Boolean): BaseViewHolder {
        val view = getView<View>(viewId);
        if (view is Checkable) {
            view.isChecked = isChecked
        }
        return this
    }

    open fun setOnTouchListener(@IdRes viewId: Int, onTouchListener: View.OnTouchListener): BaseViewHolder {
        getView<View>(viewId).setOnTouchListener(onTouchListener)
        return this
    }

    open fun setOnCheckedChangeListener(@IdRes viewId: Int, onCheckedChangeListener: CompoundButton.OnCheckedChangeListener): BaseViewHolder {
        getView<CompoundButton>(viewId).setOnCheckedChangeListener(onCheckedChangeListener)
        return this
    }

    open fun setTextViewMinWidth(@IdRes viewId: Int, sizePx: Int): BaseViewHolder {
        val tv = getView<TextView>(viewId)
        tv.minWidth = sizePx
        return this
    }

    open fun setHintText(@IdRes viewId: Int, hint: CharSequence): BaseViewHolder {
        getView<TextView>(viewId).hint = hint
        return this
    }

    open fun setTextSizePx(@IdRes viewId: Int, sizePx: Float): BaseViewHolder {
        getView<TextView>(viewId).setTextSize(TypedValue.COMPLEX_UNIT_PX, sizePx)
        return this
    }

    open fun setTextSizeDp(@IdRes viewId: Int, sizeDp: Float): BaseViewHolder {
        getView<TextView>(viewId).setTextSize(TypedValue.COMPLEX_UNIT_DIP, sizeDp)
        return this
    }

    open fun setTextViewMinHeight(@IdRes viewId: Int, sizePx: Int): BaseViewHolder {
        val tv = getView<TextView>(viewId)
        tv.minHeight = sizePx
        return this
    }

    open fun setBackgroundDrawableColor(@IdRes viewId: Int, @ColorInt color: Int): BaseViewHolder {
        val view = getView<View>(viewId)
        val bg = view.background
        if (bg is GradientDrawable) {
            val mutate = bg.mutate() as GradientDrawable
            mutate.setColor(color)
        }
        return this
    }

    open fun setWidthAndHeight(@IdRes viewId: Int, widthPx: Int, heightPx: Int): BaseViewHolder {
        val view = getView<View>(viewId)
        val lp = view.layoutParams
        var hasChange = false
        if (lp.width != widthPx) {
            lp.width = widthPx
            hasChange = true
        }
        if (lp.height != heightPx) {
            lp.height = heightPx
            hasChange = true
        }
        if (hasChange) {
            view.layoutParams = lp
        }
        return this
    }


}


package com.bigkoo.pickerview.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.bigkoo.pickerview.R;
import com.bigkoo.pickerview.listener.OnDismissListener;
import com.bigkoo.pickerview.utils.PickerViewAnimateUtil;

/**
 * 精仿iOSPickerViewController控件
 *
 * @author Created by Sai on 15/11/22.
 */
public abstract class BasePickerView {
    
    protected final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER
    );
    
    private final Context context;
    protected ViewGroup contentContainer;
    // 显示PickerView的根View,默认是activity的根view
    public ViewGroup decorView;
    // 附加View 的 根View
    private ViewGroup rootView;
    // 附加Dialog 的 根View
    private ViewGroup dialogView;
    
    protected int pickerview_timebtn_nor = 0xFF057dff;
    protected int pickerview_timebtn_pre = 0xFFc2daf5;
    protected int pickerview_bg_topbar = 0xFFf5f5f5;
    protected int pickerview_topbar_title = 0xFF000000;
    protected int bgColor_default = 0xFFFFFFFF;
    
    private OnDismissListener onDismissListener;
    private boolean dismissing;
    
    private Animation outAnim;
    private Animation inAnim;
    private boolean isShowing;
    private int gravity = Gravity.CENTER;
    
    private Dialog mDialog;
    // 是否能取消
    private boolean cancelable;
    private boolean isAnim = true;
    
    public BasePickerView(Context context) {
        this.context = context;
    }
    
    @SuppressLint("InflateParams")
    protected void initViews(int backgroundId) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (isDialog()) {
            // 如果是对话框模式
            dialogView = (ViewGroup) layoutInflater.inflate(R.layout.layout_basepickerview, null, false);
            // 设置界面的背景为透明
            dialogView.setBackgroundColor(Color.TRANSPARENT);
            // 这个是真正要加载时间选取器的父布局
            contentContainer = (ViewGroup) dialogView.findViewById(R.id.content_container);
            contentContainer.setLayoutParams(this.params);
            // 创建对话框
            createDialog();
            // 给背景设置点击事件,这样当点击内容以外的地方会关闭界面
            dialogView.setOnClickListener(view -> dismiss());
        } else {
            // 如果只是要显示在屏幕的下方
            // decorView是activity的根View
            if (decorView == null) {
                decorView = (ViewGroup) ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
            }
            // 将控件添加到decorView中
            rootView = (ViewGroup) layoutInflater.inflate(R.layout.layout_basepickerview, decorView, false);
            rootView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ));
            if (backgroundId != 0) {
                rootView.setBackgroundColor(backgroundId);
            }
            // rootView.setBackgroundColor(ContextCompat.getColor(context,backgroundId));
            // 这个是真正要加载时间选取器的父布局
            contentContainer = (ViewGroup) rootView.findViewById(R.id.content_container);
            contentContainer.setLayoutParams(params);
        }
        setKeyBackCancelable(true);
    }
    
    protected void init() {
        inAnim = getInAnimation();
        outAnim = getOutAnimation();
    }
    
    protected void initEvents() {
    
    }
    
    public Context getContext() {
        return context;
    }
    
    public void show(boolean isAnim) {
        this.isAnim = isAnim;
        show();
    }
    
    /**
     * 添加View到根视图
     */
    public void show() {
        if (isDialog()) {
            showDialog();
        } else {
            if (isAttachToParent()) {
                return;
            }
            
            isShowing = true;
            onAttached(rootView);
            rootView.requestFocus();
        }
    }
    
    /**
     * show的时候调用
     *
     * @param view 这个View
     */
    private void onAttached(View view) {
        decorView.addView(view);
        if (isAnim) {
            contentContainer.startAnimation(inAnim);
        }
    }
    
    /**
     * 检测非Dialog时，该View是不是已经添加到根视图
     *
     * @return 如果视图已经存在该View返回true
     */
    private boolean isAttachToParent() {
        if (isDialog()) {
            return false;
        } else {
            return rootView.getParent() != null || isShowing;
        }
    }
    
    public boolean isShowing() {
        if (isDialog()) {
            return mDialog.isShowing();
        } else {
            return rootView.getParent() != null || isShowing;
        }
    }
    
    public void dismiss() {
        if (isDialog()) {
            dismissDialog();
        } else {
            if (dismissing) {
                return;
            }
            
            if (isAnim) {
                //消失动画
                outAnim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    
                    }
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        dismissImmediately();
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    
                    }
                });
                contentContainer.startAnimation(outAnim);
            } else {
                dismissImmediately();
            }
            dismissing = true;
        }
    }
    
    public void dismissImmediately() {
        decorView.post(() -> {
            //从根视图移除
            decorView.removeView(rootView);
            isShowing = false;
            dismissing = false;
            if (onDismissListener != null) {
                onDismissListener.onDismiss(BasePickerView.this);
            }
        });
    }
    
    public Animation getInAnimation() {
        int res = PickerViewAnimateUtil.getAnimationResource(this.gravity, true);
        return AnimationUtils.loadAnimation(context, res);
    }
    
    public Animation getOutAnimation() {
        int res = PickerViewAnimateUtil.getAnimationResource(this.gravity, false);
        return AnimationUtils.loadAnimation(context, res);
    }
    
    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }
    
    public void setKeyBackCancelable(boolean isCancelable) {
        ViewGroup View;
        if (isDialog()) {
            View = dialogView;
        } else {
            View = rootView;
        }
        
        View.setFocusable(isCancelable);
        View.setFocusableInTouchMode(isCancelable);
        if (isCancelable) {
            View.setOnKeyListener(onKeyBackListener);
        } else {
            View.setOnKeyListener(null);
        }
    }
    
    private final View.OnKeyListener onKeyBackListener = (v, keyCode, event) -> {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == MotionEvent.ACTION_DOWN
            && isAttachToParent()) {
            dismiss();
            return true;
        }
        return false;
    };
    
    protected void setOutSideCancelable(boolean isCancelable) {
        if (rootView != null) {
            View view = rootView.findViewById(R.id.outmost_container);
            
            if (isCancelable) {
                view.setOnTouchListener(onCancelableTouchListener);
            } else {
                view.setOnTouchListener(null);
            }
        }
    }
    
    /**
     * 设置对话框模式是否可以点击外部取消
     */
    public void setDialogOutSideCancelable(boolean cancelable) {
        this.cancelable = cancelable;
        if (mDialog != null) {
            mDialog.setCancelable(cancelable);
        }
    }
    
    /**
     * Called when the user touch on black overlay in order to dismiss the dialog
     */
    @SuppressLint("ClickableViewAccessibility")
    private final View.OnTouchListener onCancelableTouchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            dismiss();
        }
        return false;
    };
    
    public View findViewById(int id) {
        return contentContainer.findViewById(id);
    }
    
    private void createDialog() {
        if (dialogView != null) {
            mDialog = new Dialog(context, R.style.custom_dialog2);
            mDialog.setCancelable(cancelable);//不能点外面取消,也不 能点back取消
            mDialog.setContentView(dialogView);
            
            Window window = mDialog.getWindow();
            View decorView = window.getDecorView();
            decorView.setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(layoutParams);
            window.setWindowAnimations(R.style.pickerview_dialogAnim);
            mDialog.setOnDismissListener(dialog -> {
                if (onDismissListener != null) {
                    onDismissListener.onDismiss(BasePickerView.this);
                }
            });
        }
    }
    
    private void showDialog() {
        if (mDialog != null) {
            mDialog.show();
        }
    }
    
    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
    
    /**
     * 设置控件显示位置，默认显示在屏幕中间
     *
     * @param gravity 只支持{@link Gravity#CENTER}和{@link Gravity#BOTTOM},如果设置其它值，则使用默认值
     */
    public void setGravity(int gravity) {
        if (gravity == Gravity.CENTER || gravity == Gravity.BOTTOM) {
            params.gravity = gravity;
            this.gravity = gravity;
        } else {
            params.gravity = Gravity.CENTER;
            this.gravity = Gravity.CENTER;
        }
    }
    
    public boolean isDialog() {
        return true;
    }
    
    /**
     * 回调用户选中的数据
     */
    public abstract void returnData();
}

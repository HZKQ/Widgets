package com.keqiang.breadcrumb;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.zhouzhuo810.magpiex.utils.SimpleUtil;

/**
 * 面包屑
 *
 * @author Created by 汪高皖 on 2020/1/15 16:22
 */
public class Breadcrumb extends LinearLayout {
    
    /**
     * 文件夹名称后缀
     */
    public static final String FOLDER_SUFFIX = " > ";
    
    private TextView mTvRootFolder;
    private TextView mTvFolders;
    private MyHorizontalScrollView mScrollView;
    
    private com.keqiang.breadcrumb.OnClickListener mOnClickListener;
    private FolderChangeListener mFolderChangeListener;
    
    private boolean mInitFirstFolder;
    private boolean mFixFirstFolder;
    private String mFirstFolderId;
    private String mFirstFolderName;
    
    private List<FolderSpan> mFolderSpans;
    private String mFolders;
    
    /**
     * 当前文件夹位置，-2表示没有任何文件夹，-1表示当前当前目录是固定的第一个目录，>=0表示可其它位置目录
     */
    private int mCurFolderPosition;
    
    /**
     * 可点击节点文本颜色
     */
    private int mClickableTextColor;
    
    /**
     * 普通文本颜色
     */
    private int mTextColor;
    private int mTextSize;
    
    
    public Breadcrumb(Context context) {
        this(context, null);
    }
    
    public Breadcrumb(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public Breadcrumb(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Breadcrumb(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
        setHorizontalGravity(VERTICAL);
        if (attrs != null) {
            TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.Breadcrumb);
            mTextColor = t.getColor(R.styleable.Breadcrumb_android_textColor, Color.BLACK);
            mTextSize = t.getDimensionPixelSize(R.styleable.Breadcrumb_android_textSize, 16);
            mClickableTextColor = t.getColor(R.styleable.Breadcrumb_bc_clickable_textColor, Color.BLUE);
            t.recycle();
        } else {
            mTextColor = Color.BLACK;
            mClickableTextColor = Color.BLUE;
            mTextSize = 16;
        }
        
        if (!isInEditMode()) {
            mTextSize = SimpleUtil.getScaledValue(mTextSize);
        }
        
        mTvRootFolder = new TextView(context);
        mTvRootFolder.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mTvRootFolder.setTextColor(mTextColor);
        mTvRootFolder.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTvRootFolder.setLayoutParams(params);
        addView(mTvRootFolder);
        
        mScrollView = new MyHorizontalScrollView(context);
        mScrollView.setOverScrollMode(OVER_SCROLL_NEVER);
        mScrollView.setHorizontalScrollBarEnabled(false);
        mScrollView.setVerticalScrollBarEnabled(false);
        params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mScrollView.setLayoutParams(params);
        addView(mScrollView);
        
        mTvFolders = new TextView(context);
        mTvFolders.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mTvFolders.setTextColor(mTextColor);
        mTvFolders.setMovementMethod(LinkMovementMethod.getInstance());
        mTvFolders.setGravity(Gravity.CENTER_VERTICAL);
        params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTvFolders.setLayoutParams(params);
        mScrollView.addView(mTvFolders);
        
        setFixFirstFolder(true);
        mFolders = "";
        mFolderSpans = new ArrayList<>();
        mCurFolderPosition = -2;
    }
    
    /**
     * 文件导航移除当前被点击文件夹之后所有节点
     *
     * @param isClickFixFolder 是否是点击固定的目录
     * @param span             当前被点击文件夹
     */
    private void clickFolderSpan(boolean isClickFixFolder, FolderSpan span) {
        if (isClickFixFolder) {
            if (mCurFolderPosition == -1) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(mFirstFolderId, mFirstFolderName);
                }
                return;
            }
            
            mFolderSpans.clear();
            mFolders = "";
            mCurFolderPosition = -1;
            mTvFolders.setText(null);
            mTvRootFolder.setTextColor(mTextColor);
            if (mOnClickListener != null) {
                mOnClickListener.onClick(mFirstFolderId, mFirstFolderName);
            }
            
            if (mFolderChangeListener != null) {
                mFolderChangeListener.onChange(mFirstFolderId, mFirstFolderName);
            }
            return;
        }
        
        if (span.position == mCurFolderPosition) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(span.folderId, span.folderName);
            }
            return;
        }
        
        Iterator<FolderSpan> iterator = mFolderSpans.iterator();
        while (iterator.hasNext()) {
            FolderSpan next = iterator.next();
            if (next.position > span.position) {
                iterator.remove();
            }
        }
        
        mFolders = mFolders.subSequence(0, span.end).toString();
        SpannableString ss = new SpannableString(mFolders);
        int size = mFolderSpans.size();
        for (int i = 0; i < size; i++) {
            FolderSpan folderSpan = mFolderSpans.get(i);
            folderSpan.setColor(i == size - 1 ? mTextColor : mClickableTextColor);
            ss.setSpan(folderSpan, folderSpan.start, folderSpan.end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        mTvFolders.setText(ss);
        mScrollView.scrollToEnd();
        mCurFolderPosition = span.position;
        
        if (mOnClickListener != null) {
            mOnClickListener.onClick(span.folderId, span.folderName);
        }
        
        if (mFolderChangeListener != null) {
            mFolderChangeListener.onChange(span.folderId, span.folderName);
        }
    }
    
    /**
     * 文件导航增加文件夹节点，将触发{@link FolderChangeListener}监听
     *
     * @param folderId   节点Id
     * @param folderName 节点名称
     */
    public void addFolderSpan(String folderId, String folderName) {
        String folder = folderName;
        if (folder == null) {
            folder = "";
        }
        folder += FOLDER_SUFFIX;
        if (mFixFirstFolder && !mInitFirstFolder) {
            mTvRootFolder.setTextColor(mTextColor);
            mTvRootFolder.setText(folder);
            mFirstFolderId = folderId;
            mFirstFolderName = folderName;
            mCurFolderPosition = -1;
            mInitFirstFolder = true;
            
            if (mFolderChangeListener != null) {
                mFolderChangeListener.onChange(folderId, folderName);
            }
            return;
        }
        
        if (mFixFirstFolder) {
            mTvRootFolder.setTextColor(mClickableTextColor);
        }
        
        int start = mFolders.length();
        int end = start + folder.length();
        mFolders += folder;
        FolderSpan span = new FolderSpan(folderId, folderName, mFolderSpans.size(), start, end);
        mFolderSpans.add(span);
        SpannableString ss = new SpannableString(mFolders);
        int size = mFolderSpans.size();
        for (int i = 0; i < size; i++) {
            FolderSpan folderSpan = mFolderSpans.get(i);
            folderSpan.setColor(i == size - 1 ? mTextColor : mClickableTextColor);
            ss.setSpan(folderSpan, folderSpan.start, folderSpan.end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        mTvFolders.setText(ss);
        mScrollView.scrollToEnd();
        mCurFolderPosition = span.position;
        if (mFolderChangeListener != null) {
            mFolderChangeListener.onChange(folderId, folderName);
        }
    }
    
    /**
     * 返回到上一层目录，如果返回值不为null将触发{@link FolderChangeListener}监听
     *
     * @return String[], 上一层目录name和Id，如果为null，则说明已经无法回退
     */
    public String[] back() {
        if (mFixFirstFolder) {
            if (mFolderSpans.size() > 1) {
                FolderSpan span = mFolderSpans.get(mFolderSpans.size() - 2);
                clickFolderSpan(false, span);
                return new String[]{span.folderName, span.folderId};
            } else if (mFolderSpans.size() == 1) {
                clickFolderSpan(true, null);
                return new String[]{mFirstFolderName, mFirstFolderId};
            } else {
                return null;
            }
        } else {
            if (mFolderSpans.size() > 1) {
                FolderSpan span = mFolderSpans.get(mFolderSpans.size() - 2);
                clickFolderSpan(false, span);
                return new String[]{span.folderName, span.folderId};
            } else {
                return null;
            }
        }
    }
    
    /**
     * 重置数据
     */
    public void reset() {
        reset(mFixFirstFolder);
    }
    
    /**
     * 重置数据
     *
     * @param fixFirstFolder 是否固定第一个目录不让其左右滑动
     */
    public void reset(boolean fixFirstFolder) {
        mFolders = "";
        mFolderSpans.clear();
        mTvRootFolder.setText("");
        mTvFolders.setText("");
        mCurFolderPosition = -2;
        mInitFirstFolder = false;
        mFirstFolderId = null;
        mFirstFolderName = null;
        setFixFirstFolder(fixFirstFolder);
    }
    
    public void setOnClickListener(com.keqiang.breadcrumb.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
    
    public void setFolderChangeListener(FolderChangeListener folderChangeListener) {
        mFolderChangeListener = folderChangeListener;
    }
    
    private void setFixFirstFolder(boolean fixFirstFolder) {
        mFixFirstFolder = fixFirstFolder;
        if (mFixFirstFolder) {
            mTvRootFolder.setOnClickListener(v -> clickFolderSpan(true, null));
        } else {
            mTvRootFolder.setOnClickListener(null);
        }
    }
    
    public void setClickableTextColor(int clickableTextColor) {
        mClickableTextColor = clickableTextColor;
        
        if (mCurFolderPosition >= 0 && mFixFirstFolder) {
            mTvRootFolder.setTextColor(mClickableTextColor);
        }
        
        if (!TextUtils.isEmpty(mFolders)) {
            SpannableString ss = new SpannableString(mFolders);
            int size = mFolderSpans.size();
            for (int i = 0; i < size; i++) {
                FolderSpan folderSpan = mFolderSpans.get(i);
                folderSpan.setColor(i == size - 1 ? mTextColor : mClickableTextColor);
                ss.setSpan(folderSpan, folderSpan.start, folderSpan.end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            mTvFolders.setText(ss);
        }
    }
    
    public void setTextColor(int textColor) {
        mTextColor = textColor;
        
        if (mCurFolderPosition < 0 && mFixFirstFolder) {
            mTvRootFolder.setTextColor(mTextColor);
        }
        
        if (!TextUtils.isEmpty(mFolders)) {
            SpannableString ss = new SpannableString(mFolders);
            int size = mFolderSpans.size();
            for (int i = 0; i < size; i++) {
                FolderSpan folderSpan = mFolderSpans.get(i);
                folderSpan.setColor(i == size - 1 ? mTextColor : mClickableTextColor);
                ss.setSpan(folderSpan, folderSpan.start, folderSpan.end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            mTvFolders.setText(ss);
        }
    }
    
    public void setTextSize(int textSize) {
        mTextSize = textSize;
        mTvRootFolder.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        mTvFolders.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
    }
    
    /**
     * 可点击目录Span
     */
    private class FolderSpan extends ClickableSpan {
        /**
         * 文件夹Id
         */
        String folderId;
        
        /**
         * 原始文件夹名称，不包含">"
         */
        String folderName;
        
        /**
         * 当前FolderSpan在{@link #mFolderSpans}中的位置
         */
        int position;
        
        /**
         * 当前目录名称在{@link #mFolders}中的开始位置
         */
        int start;
        
        /**
         * start+原始文件夹名称长度+{@link #FOLDER_SUFFIX}长度
         */
        int end;
        
        int color;
        
        FolderSpan(String folderId, String folderName, int position, int start, int end) {
            this.folderId = folderId;
            this.folderName = folderName;
            this.position = position;
            this.start = start;
            this.end = end;
        }
        
        @Override
        public void onClick(@NonNull View widget) {
            clickFolderSpan(false, this);
        }
        
        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            ds.setColor(color);
            ds.setUnderlineText(false);
        }
        
        public void setColor(int color) {
            this.color = color;
        }
    }
}

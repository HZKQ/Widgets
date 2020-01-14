package com.keqiang.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * 处理自动换行，修复原生自动换行由于全角半角符号问题导致换行位置错误Bug<br>
 * 注意：只支持{@link String}字符类型，设置了其它复杂类型，最后也是转换为String处理
 */
public class AutoSplitTextView extends AppCompatTextView {
    /**
     * 是否自动处理自动换行
     */
    private boolean mAutoSplit = true;
    
    /**
     * 是否需要再次绘制
     */
    private boolean mNeedDraw = true;
    
    /**
     * 当前展示的文本
     */
    private String mText;
    private MyTask mMyTask;
    
    public AutoSplitTextView(Context context) {this(context, null);}
    
    public AutoSplitTextView(Context context, AttributeSet attrs) {this(context, attrs, 0); }
    
    public AutoSplitTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setAutoSplitEnabled(boolean enabled) {
        mAutoSplit = enabled;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 每次重新测绘时需要重新布局文本，如果界面大小发送变更，在此方法调用后即调用onSizeChanged，
        // 因此无需在onSizeChanged方法中调用reDrawText()，只需在此处调用即可
        reDrawText();
    }
    
    @Override
    public void setText(CharSequence text, TextView.BufferType type) {
        // 判断字符是否发生变更或变更后的内容是否已经绘制到界面
        // 从而减少处理字符换行逻辑的次数，提高性能
        if (text == null) {
            mNeedDraw = !TextUtils.isEmpty(mText);
        } else {
            mNeedDraw = !text.toString().equals(mText);
        }
        
        if (mNeedDraw) {
            mText = text == null ? null : text.toString();
            // 必须先设置一次，否则崩溃
            super.setText(text, type);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mMyTask != null) {
            mMyTask.cancel(true);
        }
    }
    
    /**
     * 重新绘制字符
     */
    private void reDrawText() {
        if (mAutoSplit && mNeedDraw) {
            mNeedDraw = false;
            if (TextUtils.isEmpty(mText)) {
                return;
            }
            
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();
            if (width > 0 && height > 0) {
                if (mMyTask == null) {
                    mMyTask = new MyTask(this);
                }
                mMyTask.execute(mText, width);
            }
        }
    }
    
    static class MyTask extends AsyncTask<Object, Void, String> {
        
        @SuppressLint("StaticFieldLeak")
        private AutoSplitTextView mAutoSplitTextView;
        
        MyTask(AutoSplitTextView autoSplitTextView) {
            mAutoSplitTextView = autoSplitTextView;
        }
        
        // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
        @Override
        protected String doInBackground(Object... params) {
            return autoSplitText((String) params[0], (int) params[1]);
        }
        
        // 方法4：onPostExecute（）
        // 作用：接收线程任务执行结果、将执行结果显示到UI组件
        @Override
        protected void onPostExecute(String result) {
            // 执行完毕后，则更新UI
            mAutoSplitTextView.setText(result, TextView.BufferType.NORMAL);
        }
        
        @Override
        protected void onCancelled() {
            mAutoSplitTextView = null;
        }
        
        /**
         * 在超出View宽度的文本后面加上"\n"符
         *
         * @param rawText 需要处理的文本
         * @param width   当前View的宽度
         * @return 处理后的文本
         */
        private String autoSplitText(String rawText, int width) {
            //原始文本
            final Paint tvPaint = mAutoSplitTextView.getPaint();
            //paint,包含字体等信息
            final float tvWidth = width - mAutoSplitTextView.getPaddingLeft() - mAutoSplitTextView.getPaddingRight();
            //控件可用宽度
            // 将原始文本按行拆分
            String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
            StringBuilder sbNewText = new StringBuilder();
            
            for (int i = 0; i < rawTextLines.length; i++) {
                if (i > 0) {
                    sbNewText.append("\n");
                }
                
                String rawTextLine = rawTextLines[i];
                if (tvPaint.measureText(rawTextLine) < tvWidth) {
                    //如果整行宽度在控件可用宽度之内,就不处理了
                    sbNewText.append(rawTextLine);
                } else {
                    //如果整行宽度超过控件可用宽度,则按字符测量,在超过可用宽度的前一个字符处手动换行
                    float lineWidth = 0;
                    for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
                        char ch = rawTextLine.charAt(cnt);
                        lineWidth += tvPaint.measureText(String.valueOf(ch));
                        if (lineWidth < tvWidth) {
                            sbNewText.append(ch);
                        } else {
                            sbNewText.append("\n");
                            lineWidth = 0;
                            --cnt;
                        }
                    }
                }
            }
            
            return sbNewText.toString();
        }
    }
}

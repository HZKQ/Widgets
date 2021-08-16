package com.keqiang.views;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.ReplacementSpan;
import android.text.style.TextAppearanceSpan;

import com.keqiang.views.SpannableString.AppendSpan;
import com.keqiang.views.SpannableString.TextSpan;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * TextView工具类
 *
 * @author Created by wanggaowan on 2021/8/13 21:01
 */
public class TextViewUtils {
    
    /**
     * 自动裁剪文本，在文本超出给定宽度后自动加上"\n"换行，支持富文本
     *
     * @param text  需要处理的文本
     * @param width 当前文本可绘制宽度
     * @return 处理后的文本
     */
    public static CharSequence autoSplitText(@NonNull CharSequence text, @NonNull TextPaint paint, int width, int maxLines) {
        if (maxLines == 0) {
            return text;
        }
        
        String rawText = text.toString();
        if (text instanceof String && paint.measureText(rawText) <= width) {
            return text;
        }
        
        TextPaint textPaint = new TextPaint();
        textPaint.set(paint);
        
        List<SpanInfo> spanInfoList = prepareSpanSizeInfo(text, textPaint);
        String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
        StringBuilder sbNewText = new StringBuilder();
        // 行数
        int appendCount = 0;
        // 文本当前已经处理字符长度
        int textOffset = 0;
        // 增加的换行符位置
        List<Integer> addLineFeedPos = new ArrayList<>();
        label:
        for (int i = 0; i < rawTextLines.length; i++) {
            if (i > 0) {
                // 增加的字符
                sbNewText.append("\n");
                addLineFeedPos.add(sbNewText.length());
            }
            
            String rawTextLine = rawTextLines[i];
            if (spanInfoList.size() == 0 && textPaint.measureText(rawTextLine) <= width) {
                // 如果整行宽度在控件可用宽度之内,就不处理了
                sbNewText.append(rawTextLine);
                textOffset = rawTextLine.length();
                
                appendCount++;
                if (appendCount >= maxLines) {
                    // 裁剪的文本已超出最大行数，无需再进行裁剪,直接拼接剩余内容
                    for (int j = i + 1; j < rawTextLines.length; j++) {
                        sbNewText.append("\n").append(rawTextLines[j]);
                    }
                    break;
                }
            } else {
                // 如果整行宽度超过控件可用宽度,则按字符测量,在超过可用宽度的前一个字符处手动换行
                float lineWidth = 0;
                for (int cnt = 0; cnt < rawTextLine.length(); ++cnt) {
                    String ch = rawTextLine.substring(cnt, cnt + 1);
                    int indexOf = rawText.indexOf(ch, textOffset);
                    SpanInfo replacementSpan = getReplacementSpan(spanInfoList, indexOf);
                    if (replacementSpan != null) {
                        ch = rawTextLine.substring(cnt, cnt + (replacementSpan.end - replacementSpan.start));
                        lineWidth += replacementSpan.replacementSize;
                    } else {
                        if (isEmojiCharacter(rawTextLine.charAt(cnt))
                            && cnt + 1 < rawTextLine.length()
                            && isEmojiCharacter(rawTextLine.charAt(cnt + 1))) {
                            // 处理表情符号，必须连续两个字符均是emoji表情
                            ch = rawTextLine.substring(cnt, cnt + 2);
                        }
                        
                        float textSize = getTextSize(spanInfoList, indexOf, paint.getTextSize());
                        textPaint.setTextSize(textSize);
                        lineWidth += textPaint.measureText(ch);
                    }
                    
                    textOffset += ch.length();
                    if (lineWidth == width) {
                        sbNewText.append(ch);
                        appendCount++;
                        if (appendCount >= maxLines) {
                            // 裁剪的文本已超出最大行数，无需再进行裁剪,直接拼接剩余内容
                            if (cnt + ch.length() < rawTextLine.length()) {
                                sbNewText.append(rawTextLine.substring(cnt + ch.length()));
                            }
                            
                            for (int j = i + 1; j < rawTextLines.length; j++) {
                                sbNewText.append("\n").append(rawTextLines[j]);
                            }
                            break label;
                        }
                        
                        // 增加的字符
                        sbNewText.append("\n");
                        addLineFeedPos.add(sbNewText.length());
                        lineWidth = 0;
                        cnt += ch.length() - 1;
                    } else if (lineWidth < width) {
                        sbNewText.append(ch);
                        cnt += ch.length() - 1;
                    } else {
                        appendCount++;
                        if (appendCount >= maxLines) {
                            // 裁剪的文本已超出最大行数，无需再进行裁剪,直接拼接剩余内容
                            sbNewText.append(rawTextLine.substring(cnt));
                            
                            for (int j = i + 1; j < rawTextLines.length; j++) {
                                sbNewText.append("\n").append(rawTextLines[j]);
                            }
                            break label;
                        }
                        
                        // 增加的字符
                        if (replacementSpan == null) {
                            // 只有非图片时，才需要在图片之前换行
                            sbNewText.append("\n");
                            addLineFeedPos.add(sbNewText.length());
                        }
                        
                        lineWidth = 0;
                        --cnt;
                        textOffset -= ch.length();
                    }
                }
            }
        }
        
        if (text instanceof SpannableString) {
            return buildRichText((SpannableString) text, sbNewText.toString(), addLineFeedPos);
        } else if (text instanceof SpannableStringBuilder) {
            return buildRichTextForBuilder((SpannableStringBuilder) text, sbNewText.toString(), addLineFeedPos);
        }
        
        return sbNewText.toString();
    }
    
    /**
     * 准备富文本的文字大小信息
     */
    private static List<SpanInfo> prepareSpanSizeInfo(CharSequence text, TextPaint textPaint) {
        List<SpanInfo> spanInfoList = new ArrayList<>();
        if (text instanceof SpannableString) {
            SpannableString ss = (SpannableString) text;
            CharacterStyle[] spans = ss.getSpans(0, ss.length(), CharacterStyle.class);
            for (CharacterStyle span : spans) {
                
                if (span instanceof TextSpan
                    || span instanceof AbsoluteSizeSpan
                    || span instanceof RelativeSizeSpan
                    || span instanceof TextAppearanceSpan
                    || span instanceof ReplacementSpan) {
                    
                    int startIndex = ss.getSpanStart(span);
                    int endIndex = ss.getSpanEnd(span);
                    int spanFlags = ss.getSpanFlags(span);
                    
                    float textSize;
                    if (span instanceof TextSpan) {
                        textSize = ((TextSpan) span).getTextSize();
                        if (textSize == AppendSpan.INVALID_VALUE) {
                            continue;
                        }
                    } else if (span instanceof AbsoluteSizeSpan) {
                        if (((AbsoluteSizeSpan) span).getDip()) {
                            textSize = ((AbsoluteSizeSpan) span).getSize() * textPaint.density;
                        } else {
                            textSize = ((AbsoluteSizeSpan) span).getSize();
                        }
                    } else if (span instanceof RelativeSizeSpan) {
                        textSize = ((RelativeSizeSpan) span).getSizeChange() * textPaint.getTextSize();
                    } else if (span instanceof TextAppearanceSpan) {
                        textSize = ((TextAppearanceSpan) span).getTextSize();
                        if (textSize == 0) {
                            continue;
                        }
                    } else {
                        int size = ((ReplacementSpan) span).getSize(textPaint, "", startIndex, endIndex, null);
                        spanInfoList.add(new SpanInfo(startIndex, endIndex, spanFlags, size));
                        continue;
                    }
                    
                    spanInfoList.add(new SpanInfo(textSize, startIndex, endIndex, spanFlags));
                }
            }
        } else if (text instanceof SpannableStringBuilder) {
            prepareSpanSizeInfoForBuilder(spanInfoList, (SpannableStringBuilder) text, textPaint);
        }
        return spanInfoList;
    }
    
    private static void prepareSpanSizeInfoForBuilder(List<SpanInfo> spanInfoList, SpannableStringBuilder builder, TextPaint textPaint) {
        CharacterStyle[] spans = builder.getSpans(0, builder.length(), CharacterStyle.class);
        for (CharacterStyle span : spans) {
            
            if (span instanceof TextSpan
                || span instanceof AbsoluteSizeSpan
                || span instanceof RelativeSizeSpan
                || span instanceof TextAppearanceSpan
                || span instanceof ReplacementSpan) {
                
                int startIndex = builder.getSpanStart(span);
                int endIndex = builder.getSpanEnd(span);
                int spanFlags = builder.getSpanFlags(span);
                
                float textSize;
                if (span instanceof TextSpan) {
                    textSize = ((TextSpan) span).getTextSize();
                    if (textSize == AppendSpan.INVALID_VALUE) {
                        continue;
                    }
                } else if (span instanceof AbsoluteSizeSpan) {
                    if (((AbsoluteSizeSpan) span).getDip()) {
                        textSize = ((AbsoluteSizeSpan) span).getSize() * textPaint.density;
                    } else {
                        textSize = ((AbsoluteSizeSpan) span).getSize();
                    }
                } else if (span instanceof RelativeSizeSpan) {
                    textSize = ((RelativeSizeSpan) span).getSizeChange() * textPaint.getTextSize();
                } else if (span instanceof TextAppearanceSpan) {
                    textSize = ((TextAppearanceSpan) span).getTextSize();
                    if (textSize == 0) {
                        continue;
                    }
                } else {
                    int size = ((ReplacementSpan) span).getSize(textPaint, "", startIndex, endIndex, null);
                    spanInfoList.add(new SpanInfo(startIndex, endIndex, spanFlags, size));
                    continue;
                }
                
                spanInfoList.add(new SpanInfo(textSize, startIndex, endIndex, spanFlags));
            }
        }
    }
    
    private static float getTextSize(List<SpanInfo> spanInfoList, int pos, float defValue) {
        if (spanInfoList.size() == 0) {
            return defValue;
        }
        
        for (SpanInfo spanInfo : spanInfoList) {
            if (spanInfo.start <= pos && pos < spanInfo.end) {
                return spanInfo.textSize;
            }
        }
        
        return defValue;
    }
    
    private static SpanInfo getReplacementSpan(List<SpanInfo> spanInfoList, int pos) {
        if (spanInfoList.size() == 0) {
            return null;
        }
        
        SpanInfo info = null;
        for (SpanInfo spanInfo : spanInfoList) {
            if (spanInfo.start <= pos && pos < spanInfo.end && spanInfo.isReplacement) {
                info = spanInfo;
                break;
            }
        }
        
        return info;
    }
    
    private static SpannableString buildRichText(SpannableString original, String str, List<Integer> addLineFeedPos) {
        SpannableString ssNew = new SpannableString(str);
        
        CharacterStyle[] spans = original.getSpans(0, original.length(), CharacterStyle.class);
        for (CharacterStyle span : spans) {
            int startIndex = original.getSpanStart(span);
            int endIndex = original.getSpanEnd(span);
            int spanFlags = original.getSpanFlags(span);
            
            // 默认span开始和结束位置都超出新增最后一个"\n"下标
            int size = addLineFeedPos.size();
            int statInListPos = size;
            int endInListPos = size;
            for (int i = 0; i < size; i++) {
                if (statInListPos == size && startIndex < addLineFeedPos.get(i)) {
                    statInListPos = i;
                }
                
                if (endInListPos == size && endIndex < addLineFeedPos.get(i)) {
                    endInListPos = i;
                }
                
                if (statInListPos != size && endInListPos != size) {
                    break;
                }
            }
            
            ssNew.setSpan(span, startIndex + statInListPos, endIndex + endInListPos, spanFlags);
        }
        return ssNew;
    }
    
    private static SpannableStringBuilder buildRichTextForBuilder(SpannableStringBuilder original, String str, List<Integer> addLineFeedPos) {
        SpannableStringBuilder ssNew = new SpannableStringBuilder(str);
        
        CharacterStyle[] spans = original.getSpans(0, original.length(), CharacterStyle.class);
        for (CharacterStyle span : spans) {
            int startIndex = original.getSpanStart(span);
            int endIndex = original.getSpanEnd(span);
            int spanFlags = original.getSpanFlags(span);
            
            // 默认span开始和结束位置都超出新增最后一个"\n"下标
            int size = addLineFeedPos.size();
            int statInListPos = size;
            int endInListPos = size;
            for (int i = 0; i < size; i++) {
                if (statInListPos == size && startIndex < addLineFeedPos.get(i)) {
                    statInListPos = i;
                }
                
                if (endInListPos == size && endIndex < addLineFeedPos.get(i)) {
                    endInListPos = i;
                }
                
                if (statInListPos != size && endInListPos != size) {
                    break;
                }
            }
            
            ssNew.setSpan(span, startIndex + statInListPos, endIndex + endInListPos, spanFlags);
        }
        return ssNew;
    }
    
    /**
     * 是否是表情符号
     */
    private static boolean isEmojiCharacter(char codePoint) {
        return !((codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA)
            || (codePoint == 0xD)
            || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
            || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)));
    }
    
    /**
     * 富文本信息
     */
    private static class SpanInfo {
        private final float textSize;
        private final int start;
        private final int end;
        // 该属性不管配置啥，最终都是仅包含start不包含end坐标数据应用span数据
        // spanFlags仅在SpannableStringBuilder insert,replace时有效
        private final int spanFlags;
        private final boolean isReplacement;
        private final int replacementSize;
        
        public SpanInfo(float textSize, int start, int end, int spanFlags) {
            this.textSize = textSize;
            this.start = start;
            this.end = end;
            this.spanFlags = spanFlags;
            isReplacement = false;
            replacementSize = 0;
        }
        
        public SpanInfo(int start, int end, int spanFlags, int replacementSize) {
            this.start = start;
            this.end = end;
            this.spanFlags = spanFlags;
            this.isReplacement = true;
            this.replacementSize = replacementSize;
            this.textSize = 0;
        }
    }
    
}

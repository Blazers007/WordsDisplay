package com.question.blazers.wordsdisplay.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;

/**
 * Created by blazers on 2017/8/17.
 * 文字展示View
 */

public class WordsDisplayView extends View {

    private static final String TAG = WordsDisplayView.class.getSimpleName();

    // Setting
    private static int DEFAULT_TEXT_SIZE_SP = 16;

    private static int DEFAULT_LINE_SPACE_HEIGHT_DP = 4;

    private static int DEFAULT_TEXT_COLOR = Color.BLACK;

    private static int HIGHLIGHT_TEXT_COLOR = Color.RED;

    // Var
    private TextPaint mTextPaint;

    private List<WordSegment> mWordSegmentList = new ArrayList<>(); // 单词列表

    private Rect mTextRect = new Rect();

    private int mLineSpaceHeight; // 行间距

    private int mWordSegmentSpaceWidth; // 单词间距

    private int mWordHeight; // 字高

    private int mDefaultTextColor;

    private int mHighlightTextColor;

    private Rect mContentRect = new Rect();

    private Comparator<WordSegment> mComparator;


    public WordsDisplayView(Context context) {
        super(context);
        init(context, null);
    }

    public WordsDisplayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WordsDisplayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attributeSet) {
        // 比较器
        mComparator = new Comparator<WordSegment>() {
            @Override
            public int compare(WordSegment wordSegment, WordSegment t1) {
                return wordSegment.mStartIndex - t1.mStartIndex;
            }
        };

        // 文字行间距
        mLineSpaceHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_LINE_SPACE_HEIGHT_DP, getResources().getDisplayMetrics());

        // 初始化文字画笔
        mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE_SP, getResources().getDisplayMetrics()));
        mTextPaint.getTextBounds("A", 0, 1, mTextRect);
        mWordSegmentSpaceWidth = mTextRect.width(); // 单个文字宽度
        mWordHeight = mTextRect.height(); // 文字高度

        // 默认颜色
        mDefaultTextColor = DEFAULT_TEXT_COLOR;
        mHighlightTextColor = HIGHLIGHT_TEXT_COLOR;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mWordSegmentList.isEmpty()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            // 1 宽度由计算 直接取最大值即可
            int assumedWidthSize = MeasureSpec.getSize(widthMeasureSpec);
            int assumedWithMode = MeasureSpec.getMode(widthMeasureSpec);
            int width = 0;
            if (assumedWithMode == EXACTLY || assumedWithMode == AT_MOST) {
                width = assumedWidthSize;
            }
            // 基本参数
            int canvasLeftBorder = getPaddingLeft();
            int canvasRightBorder = width - getPaddingRight();
            int canvasTopBorder = getPaddingTop();
            int maxContentWidth = canvasRightBorder - canvasLeftBorder;

            // 布局起始位置
            int currentX = canvasLeftBorder;
            int currentY = canvasTopBorder + mWordHeight; // 文字区域的左下角坐标
            for (int i = 0 ; i < mWordSegmentList.size() ; i ++) {
                WordSegment wordSegment = mWordSegmentList.get(i);
                mTextPaint.getTextBounds(wordSegment.mValue, 0, wordSegment.mValue.length(), mTextRect); // 测量高宽
                if (mTextRect.width() > maxContentWidth) {
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                    Log.e(TAG, "View max width cannot afford for word [" + wordSegment.mValue + "] length, this will make new lines forever");
                    return;
                }
                if (currentX + mTextRect.width() > canvasRightBorder) {  // 需要换行
                    currentX = canvasLeftBorder;
                    currentY += mWordHeight + mLineSpaceHeight;
                }
                // 赋值
                wordSegment.mX = currentX;
                wordSegment.mY = currentY;
                // 移动
                if (i + 1 < mWordSegmentList.size() && mWordSegmentList.get(i + 1).isNotACharacter()) {
                    currentX += mTextRect.width();
                } else {
                    currentX += mTextRect.width() + mWordSegmentSpaceWidth;
                }
            }

            // 计算当前所需高度
            int height = 0;
            int heightRequestForNow = currentY + getPaddingBottom();
            int assumedHeightSize = MeasureSpec.getSize(heightMeasureSpec);
            int assumedHeightMode = MeasureSpec.getMode(heightMeasureSpec);
            if (assumedHeightMode == EXACTLY) {
                height = assumedHeightSize;
            } else if (assumedHeightMode == AT_MOST) {
                height = Math.min(assumedHeightSize, heightRequestForNow);
            }

            // 测量完成
            setMeasuredDimension(width, height);
            mContentRect.set(canvasLeftBorder, canvasTopBorder, canvasRightBorder, height - getPaddingBottom());
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.clipRect(mContentRect);
        for (WordSegment wordSegment : mWordSegmentList) {
            mTextPaint.setColor(wordSegment.mIsHighlighted ? mHighlightTextColor : mDefaultTextColor);
            wordSegment.onDraw(canvas, mTextPaint);
        }
        canvas.restore();
    }


    /**
     * 设置默认文字颜色
     *
     * @param color 颜色
     */
    public void setDefaultColor(int color) {
        mDefaultTextColor = color;
        invalidate();
    }

    /**
     * 设置高亮文字颜色
     *
     * @param color 颜色
     */
    public void setHighlightColor(int color) {
        mHighlightTextColor = color;
        invalidate();
    }

    /**
     * 设置内容
     *
     * @param text        全部文本
     * @param highlighted 需要高亮的词组列表
     */
    public void setDisplayedText(String text, List<String> highlighted) {
        mWordSegmentList.clear();
        if (!TextUtils.isEmpty(text)) {
            // 解析
            StringBuilder searchStringBuilder = new StringBuilder(text);
            for (String highlightString : highlighted) {
                int startIndex = 0;
                do {
                    startIndex = text.indexOf(highlightString, startIndex);
                    if (startIndex != -1) {
                        // 搜索到的必须是一个单词 而不是某个长单词的一部分
                        if ((startIndex == 0 || !isThisCharInAZaz(text.charAt(startIndex - 1))) &&
                                (startIndex + highlightString.length() == text.length() || !isThisCharInAZaz(text.charAt(startIndex + highlightString.length())))) {
                            Log.i("====", startIndex + " -> " + highlightString);
                            char[] spaces = new char[highlightString.length()];
                            Arrays.fill(spaces, ' ');
                            searchStringBuilder.replace(startIndex, startIndex + highlightString.length(), new String(spaces));
                            mWordSegmentList.add(new WordSegment(startIndex, highlightString, true));
                            Log.i("====", "Curr: " + searchStringBuilder.toString());
                        }
                        // 偏移
                        startIndex += highlightString.length();
                    }
                } while (startIndex != -1);
            }
            // 解析剩下的单词
            String restString = searchStringBuilder.toString();
            Log.i("====", "Rest: " + restString);
            boolean wordCatchingNow = false;
            int startIndex = 0;
            for (int currentCharIndex = 0; currentCharIndex < restString.length(); currentCharIndex++) {
                if (!wordCatchingNow) {
                    // 还没开始新的单词
                    if (restString.charAt(currentCharIndex) != ' ') {
                        wordCatchingNow = true;
                        startIndex = currentCharIndex;
                    }
                } else {
                    if (currentCharIndex == restString.length() - 1) {
                        // 全文结束
                        wordCatchingNow = false;
                        mWordSegmentList.add(new WordSegment(startIndex, restString.substring(startIndex, currentCharIndex + 1), false));
                    } else  if (restString.charAt(currentCharIndex) == ' ') {
                        // 单词结束
                        wordCatchingNow = false;
                        mWordSegmentList.add(new WordSegment(startIndex, restString.substring(startIndex, currentCharIndex), false));
                    }
                }
            }
        }
        // 排序
        Collections.sort(mWordSegmentList, mComparator);
        Log.i("====", "Result: " + mWordSegmentList);
        requestLayout();
    }

    /**
     * 文字片段
     */
    private class WordSegment {
        // 在原文字中的序号
        private int mStartIndex;
        // 文字值
        private String mValue;
        // 是否需要高亮
        private boolean mIsHighlighted;

        // 绘制的X坐标 不需要考虑基线问题
        private int mX;
        // 绘制的Y坐标
        private int mY;

        public WordSegment(int mStartIndex, String mValue, boolean mIsHighlighted) {
            this.mStartIndex = mStartIndex;
            this.mValue = mValue;
            this.mIsHighlighted = mIsHighlighted;
        }

        /**
         * 绘制自身
         *
         * @param canvas    画布
         * @param textPaint 画笔
         */
        private void onDraw(Canvas canvas, TextPaint textPaint) {
            canvas.drawText(mValue, mX, mY, textPaint);
        }

        /**
         * 这个片段是否是符号
         * @return
         */
        private boolean isNotACharacter() {
            return mValue.length() == 1 && !isThisCharInAZaz(mValue.charAt(0));
        }

        @Override
        public String toString() {
            return mValue;
        }
    }

    /**
     * 判断字符是否是英文字母
     * @param target 所比较的
     * @return 是否为英文字母
     */
    private boolean isThisCharInAZaz(char target) {
        return (target >= 'A' && target <= 'Z') || (target >= 'a' && target <= 'z');
    }
}
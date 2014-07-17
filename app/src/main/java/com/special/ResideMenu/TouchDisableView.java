/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 SpecialCyCi
 * Modifications Copyright (c) 2014 Alexander "Evisceration" Martinz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.special.ResideMenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

class TouchDisableView extends ViewGroup {

    private View mContent;

    private boolean mTouchDisabled = false;

    public TouchDisableView(final Context context) { this(context, null); }

    public TouchDisableView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setContent(final View v) {
        if (mContent != null) removeView(mContent);
        mContent = v;
        addView(mContent);
    }

    public View getContent() { return mContent; }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int width = getDefaultSize(0, widthMeasureSpec);
        final int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);

        final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width);
        final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height);
        mContent.measure(contentWidth, contentHeight);
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top,
            final int right, final int bottom) {
        mContent.layout(0, 0, right - left, bottom - top);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent ev) { return mTouchDisabled; }

    void setTouchDisable(final boolean disableTouch) { mTouchDisabled = disableTouch; }

    boolean isTouchDisabled() { return mTouchDisabled; }
}

package org.digitalcampus.oppia.utils.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class WebViewPager extends ViewPager {
    public WebViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ExtendedWebView) {
            return ((ExtendedWebView) v).canScrollHor(-dx);
        } else {
            return super.canScroll(v, checkV, dx, x, y);
        }
    }

    // ViewPager has a known bug where multi-touch MOVE events can carry an invalid pointer
    // index (-1), causing an IllegalArgumentException in MotionEvent.getX(). Swallow it.
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        try {
            return super.onTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
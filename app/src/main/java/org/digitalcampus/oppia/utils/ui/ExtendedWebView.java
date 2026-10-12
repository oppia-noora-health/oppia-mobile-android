package org.digitalcampus.oppia.utils.ui;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.view.AbsSavedState;

public class ExtendedWebView extends WebView {

    private boolean canScroll = false;

    public ExtendedWebView(Context context) {
        super(context);
    }

    public ExtendedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }

    public boolean canScrollHor(int direction) {
        final int offset = computeHorizontalScrollOffset();
        final int range = computeHorizontalScrollRange() - computeHorizontalScrollExtent();
        if (range == 0) return false;
        if (direction < 0) {
            return offset > 0;
        } else {
            return offset < range - 1;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(this.canScroll);
        return super.onTouchEvent(event);
    }

    // WebView saves its full browsing state (history, scroll, form data) which can easily
    // exceed 500KB and trigger TransactionTooLargeException. We reload from file on resume anyway.
    // super must still be called to satisfy the View hierarchy contract (dispatchSaveInstanceState).
    @Override
    public Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        return AbsSavedState.EMPTY_STATE;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(AbsSavedState.EMPTY_STATE);
    }
}

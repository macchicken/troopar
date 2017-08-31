package com.troopar.trooparapp.myview;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Barry on 11/08/2016.
 * implicitly disable swipe page changing gesture of view page
 */
public class NonSwipeAbleViewPager extends ViewPager {

    public NonSwipeAbleViewPager(Context context) {
        super(context);
    }

    public NonSwipeAbleViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Never allow swiping to switch between pages
        return false;
    }


}

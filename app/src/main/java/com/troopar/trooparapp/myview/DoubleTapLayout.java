package com.troopar.trooparapp.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by Barry on 26/02/2016.
 */
public class DoubleTapLayout extends RelativeLayout {

    private GestureDetector gestureDetector;
    private DoubleTapAction doubleTapAction;


    public DoubleTapLayout(Context context) {
        super(context);
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public DoubleTapLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public DoubleTapLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        //Single Tap
        return gestureDetector.onTouchEvent(e);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            Log.d("DoubleTapLayout", "Double Tapped at: (" + x + "," + y + ")");
            doubleTapAction.action();
            return true;
        }
    }

    public interface DoubleTapAction{
        void action();
    }

    public void setDoubleTapAction(DoubleTapAction doubleTapAction) {
        this.doubleTapAction = doubleTapAction;
    }


}

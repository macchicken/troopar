package com.troopar.trooparapp.myview;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Barry on 18/06/2016.
 */
public class TouchableWrapper extends FrameLayout {

    private boolean touchUp;


    public TouchableWrapper(Context context) {
        super(context);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                Log.d("TouchableWrapper","TouchableWrapper action up");
                touchUp=true;
                break;
            default:touchUp=false;break;
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean isTouchUp() {
        return touchUp;
    }

    public void setTouchUp(boolean touchUp) {
        this.touchUp = touchUp;
    }


}

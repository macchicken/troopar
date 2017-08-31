package com.troopar.trooparapp.myfragment;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Barry on 2016/1/19.
 */
public class MyEditTextBackEvent extends EditText {

    private EditTextImeBackListener mOnImeBack;


    public MyEditTextBackEvent(Context context) {
        super(context);
    }

    public MyEditTextBackEvent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyEditTextBackEvent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null) mOnImeBack.onImeBack(this, this.getText().toString());
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
        mOnImeBack = listener;
    }

    public interface EditTextImeBackListener {
        void onImeBack(MyEditTextBackEvent ctrl, String text);
    }


}



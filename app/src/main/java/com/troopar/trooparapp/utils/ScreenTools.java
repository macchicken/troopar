
package com.troopar.trooparapp.utils;

import android.content.Context;

public class ScreenTools {

    private static ScreenTools mScreenTools;
    private Context mContext;

    private ScreenTools() {
    }

    public static ScreenTools getInstance() {
        if (mScreenTools == null)
            mScreenTools = new ScreenTools();
        return mScreenTools;
    }

    public int dip2px(int i) {
        return (int) (0.5D + (double) (getDensity(mContext) * (float) i));
    }

    public float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public int getScreenWidth() {
        return mContext.getResources().getDisplayMetrics().widthPixels;
    }

    public void setMContext(Context mContext) {
        this.mContext = mContext;
    }


}

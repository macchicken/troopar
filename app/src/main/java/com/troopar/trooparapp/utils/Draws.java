package com.troopar.trooparapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.File;

/**
 * Created by Barry on 22/02/2016.
 */
public class Draws {

    public static Bitmap getRoundedCornerBitmap(int w , int h , boolean squareTL, boolean squareTR, boolean squareBL, boolean squareBR,int color) {
        Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, w, h);
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, 10, 10, paint);
        //draw rectangles over the corners we want to be square
        if (squareTL){
            canvas.drawRect(0, 0, w/2, h/2, paint);
        }
        if (squareTR){
            canvas.drawRect(w/2, 0, w, h/2, paint);
        }
        if (squareBL){
            canvas.drawRect(0, h/2, w/2, h, paint);
        }
        if (squareBR){
            canvas.drawRect(w/2, h/2, w, h, paint);
        }
        return output;
    }

    public static RoundedRectDrawable createNewRoundedRectDrawable(Bitmap bitmap,float cornerRadius){
        return new RoundedRectDrawable(bitmap,cornerRadius);
    }

    private static class RoundedRectDrawable extends Drawable {

        private float mCornerRadius;
        private RectF mRect = new RectF();
        private BitmapShader mBitmapShader;
        private Paint mPaint;


        RoundedRectDrawable(Bitmap bitmap, float cornerRadius) {
            mCornerRadius = cornerRadius;
            mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(mBitmapShader);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            super.onBoundsChange(bounds);
            mRect.set(0, 0, bounds.width(), bounds.height());
//            RadialGradient vignette = new RadialGradient(mRect.centerX(), mRect.centerY() * 1.0f / 0.7f, mRect.centerX() * 1.3f,
//                    new int[] { 0, 0, 0x7f000000 }, new float[] { 0.0f, 0.7f, 1.0f }, Shader.TileMode.CLAMP);
//            Matrix oval = new Matrix();
//            oval.setScale(1.0f, 0.7f);
//            vignette.setLocalMatrix(oval);
//            mPaint.setShader(new ComposeShader(mBitmapShader, vignette, PorterDuff.Mode.SRC_OVER));
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mPaint.setColorFilter(cf);
        }
    }

    /**
     * render the marker color with specified color
     * @param color hex value
     * @return
     */
    public static BitmapDescriptor getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    public static Uri getAppImageFileUri(String photoFileName){
        return Uri.fromFile(new File(Tools.checkAppImageDirectory() + File.separator + photoFileName));
    }

    public static Bitmap loadImage(String photoFileName){
        BitmapFactory.Options sampleOptions = new BitmapFactory.Options();
        sampleOptions.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(photoFileName,sampleOptions);
        int inSampleSize=Tools.calculateInSampleSize(sampleOptions, 300, 300);
        sampleOptions.inJustDecodeBounds=false;
        sampleOptions.inSampleSize=inSampleSize;
        return BitmapFactory.decodeFile(photoFileName, sampleOptions);
    }


}

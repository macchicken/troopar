package com.troopar.trooparapp.myview;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.troopar.trooparapp.model.UploadPhoto;
import com.troopar.trooparapp.utils.ImageDownloader;
import com.troopar.trooparapp.utils.ScreenTools;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;


/**
 * Created by Barry on 26/02/2016.
 * nine grid of displaying image
 */
public class NineGridlayout extends ViewGroup {

    private int gap = 15;
    private int columns;
    private int rows;
    private List listData;
    private int totalWidth;


    public NineGridlayout(Context context) {
        super(context);
    }

    public NineGridlayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        ScreenTools screenTools=ScreenTools.getInstance();
        totalWidth=screenTools.getScreenWidth()-screenTools.dip2px(80);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
    private void layoutChildrenView(){
        int childrenCount = listData.size();
        int singleWidth = (totalWidth - gap * (3 - 1)) / 3;
        ViewGroup.LayoutParams params = getLayoutParams();//calculate height with number of images
        params.height = singleWidth * rows + gap * (rows - 1);
        setLayoutParams(params);
        for (int i = 0; i < childrenCount; i++) {
            ImageView childrenView = (ImageView) getChildAt(i);
            int[] position = findPosition(i);
            int left = (singleWidth + gap) * position[1];
            int top = (singleWidth + gap) * position[0];
            int right = left + singleWidth;
            int bottom = top + singleWidth;
            childrenView.layout(left, top, right, bottom);
        }

    }

    private int[] findPosition(int childNum) {
        int[] position = new int[2];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if ((i * columns + j) == childNum) {
                    position[0] = i;//row
                    position[1] = j;//column
                    break;
                }
            }
        }
        return position;
    }

    public void setImagesData(List<UploadPhoto> lists) {
        if (lists == null || lists.isEmpty()) {
            return;
        }
        generateChildrenLayout(lists.size());
        if (listData == null) {
            int i = 0;
            while (i < lists.size()) {
                ImageView iv = generateImageView(lists.get(i));
                addView(iv,generateDefaultLayoutParams());
                i++;
            }
        } else {
            int oldViewCount = listData.size();
            int newViewCount = lists.size();
            if (oldViewCount > newViewCount) {
                removeViews(newViewCount - 1, oldViewCount - newViewCount);
            } else if (oldViewCount < newViewCount) {
                for (int i = 0; i < newViewCount - oldViewCount; i++) {
                    ImageView iv = generateImageView(lists.get(i));
                    addView(iv,generateDefaultLayoutParams());
                }
            }
        }
        listData = lists;
        layoutChildrenView();
    }


    /**
     * num	row	column
     * 1	   1	1
     * 2	   1	2
     * 3	   1	3
     * 4	   2	2
     * 5	   2	3
     * 6	   2	3
     * 7	   3	3
     * 8	   3	3
     * 9	   3	3
     *
     */
    private void generateChildrenLayout(int length) {
        if (length <= 3) {
            rows = 1;
            columns = length;
        } else if (length <= 6) {
            rows = 2;
            columns = 3;
            if (length == 4) {
                columns = 2;
            }
        } else {
            rows = 3;
            columns = 3;
        }
    }

    private ImageView generateImageView(UploadPhoto uploadPhoto) {
        ImageView iv = new ImageView(getContext());
        iv.setOnClickListener(new SmallImageClickListener(uploadPhoto.getPhotoPath()));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(Color.parseColor("#f5f5f5"));
        ImageDownloader.getInstance().getRequestManager().load(uploadPhoto.getSmallImagePath()).diskCacheStrategy(DiskCacheStrategy.ALL).into(iv);
        return iv;
    }


}

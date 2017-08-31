package com.troopar.trooparapp.myview;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.troopar.trooparapp.R;

/**
 * Created by Barry on 25/06/2016.
 * render the customize pop up window
 */
public class EventSharePopupWindow extends PopupWindow {

    private View btn_weibo_share, btn_tecentqq_share,btn_moreoptions_share,btn_troopar_share,btn_facebook_share,btn_twitter_share,btn_instagram_share;
    private TextView btn_cancel;
    private View mMenuView;
    private int margin;


    public EventSharePopupWindow(Context context, View.OnClickListener itemsOnClick) {
        super(context);
        float d = context.getResources().getDisplayMetrics().density;
        margin = (int)(20 * d);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.event_share_layout, null);
        btn_weibo_share =  mMenuView.findViewById(R.id.btn_weibo_share);
        btn_tecentqq_share =  mMenuView.findViewById(R.id.btn_tecentqq_share);
        btn_moreoptions_share =  mMenuView.findViewById(R.id.btn_moreoptions_share);
        btn_troopar_share = mMenuView.findViewById(R.id.btn_troopar_share);
        btn_facebook_share =  mMenuView.findViewById(R.id.btn_facebook_share);
        btn_twitter_share =  mMenuView.findViewById(R.id.btn_twitter_share);
        btn_instagram_share =  mMenuView.findViewById(R.id.btn_instagram_share);
        btn_cancel = (TextView) mMenuView.findViewById(R.id.btn_cancel);
        //取消按钮
        btn_cancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });
        //设置按钮监听
        btn_weibo_share.setOnClickListener(itemsOnClick);
        btn_tecentqq_share.setOnClickListener(itemsOnClick);
        btn_moreoptions_share.setOnClickListener(itemsOnClick);
        btn_troopar_share.setOnClickListener(itemsOnClick);
        btn_facebook_share.setOnClickListener(itemsOnClick);
        btn_twitter_share.setOnClickListener(itemsOnClick);
        btn_instagram_share.setOnClickListener(itemsOnClick);
        //设置EventSharePopupWindow的View
        this.setContentView(mMenuView);
        //设置EventSharePopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        //设置EventSharePopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        //设置EventSharePopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置EventSharePopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置EventSharePopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y=(int) event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP){
                    if(y<height){
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    public void enableShareApp(String name){
        switch (name){
            case "com.sina.weibo":
                btn_weibo_share.setVisibility(View.VISIBLE);
                mMenuView.findViewById(R.id.btn_weibo_share_text).setVisibility(View.VISIBLE);
                break;
            case "com.tencent.mobileqq":
                btn_tecentqq_share.setVisibility(View.VISIBLE);
                mMenuView.findViewById(R.id.btn_tecentqq_share_text).setVisibility(View.VISIBLE);
                break;
            case "com.facebook.katana":
                btn_facebook_share.setVisibility(View.VISIBLE);
                mMenuView.findViewById(R.id.btn_facebook_share_text).setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams params= (ViewGroup.MarginLayoutParams) btn_moreoptions_share.getLayoutParams();
                params.leftMargin=margin;
                break;
            case "com.twitter.android":
                btn_twitter_share.setVisibility(View.VISIBLE);
                mMenuView.findViewById(R.id.btn_twitter_share_text).setVisibility(View.VISIBLE);
                break;
            case "com.instagram.android":
                btn_instagram_share.setVisibility(View.VISIBLE);
                mMenuView.findViewById(R.id.btn_instagram_share_text).setVisibility(View.VISIBLE);
                break;
            default:break;
        }
    }


}

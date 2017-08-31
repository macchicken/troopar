package com.troopar.trooparapp.utils;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Owen on 8/04/2016.
 */
public class RecyclerViewDecoration extends RecyclerView.ItemDecoration {

    private Drawable m_Margin;

    public RecyclerViewDecoration(Drawable margin) {
        m_Margin = margin;
    }

    @Override
    public void getItemOffsets(Rect outRect,View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }

        outRect.left = m_Margin.getIntrinsicWidth();
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int dividerTop = parent.getPaddingTop();
        int dividerBottom = parent.getPaddingBottom();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerLeft = child.getRight() + params.rightMargin;
            int dividerRight = dividerLeft + m_Margin.getIntrinsicWidth();

            m_Margin.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            m_Margin.draw(canvas);
        }
    }
}

package com.xue.douyin.common.view.roundrect;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;

import com.xue.douyin.R;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class RoundRectLayout extends ConstraintLayout {

    private Path mPath;

    private float mRadius;

    public RoundRectLayout(Context context) {
        super(context);
    }

    public RoundRectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RoundRectLayout);
        try {
            mRadius = ta.getDimensionPixelSize(R.styleable.RoundRectLayout_radius, 0);
        } finally {
            ta.recycle();
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        ensurePath();
        canvas.save();
        canvas.clipPath(mPath);
        super.dispatchDraw(canvas);
        canvas.restore();
    }

    private void ensurePath() {
        if (mPath != null) {
            return;
        }
        mPath = new Path();
        mPath.addRoundRect(new RectF(0,0,getMeasuredWidth(),getMeasuredHeight()),mRadius,mRadius, Path.Direction.CW);
    }
}

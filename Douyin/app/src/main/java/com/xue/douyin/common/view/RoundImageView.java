package com.xue.douyin.common.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by 薛贤俊 on 2018/3/6.
 */

@SuppressLint("AppCompatCustomView")
public class RoundImageView extends ImageView {

    private Path mCirclePath;

    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        ensurePath();
        canvas.save();
        canvas.clipPath(mCirclePath);
        super.onDraw(canvas);
        canvas.restore();
    }

    private void ensurePath() {
        if (mCirclePath != null) {
            return;
        }
        mCirclePath = new Path();
        mCirclePath.addCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2,
                Math.min(getMeasuredWidth(), getMeasuredHeight()) / 2, Path.Direction.CW);
    }
}

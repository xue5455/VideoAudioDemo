package com.xue.douyin.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class RecordButton extends TextView {

    private static final int BACKGROUND_COLOR = Color.parseColor("#fe2c55");

    private int mBackgroundColor = BACKGROUND_COLOR;

    private Paint mPaint;

    private OnRecordListener mListener;

    public RecordButton(Context context) {
        super(context);
        init();
    }

    public RecordButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setBackground(null);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(mBackgroundColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2,
                getMeasuredWidth() / 2, mPaint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mListener == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mListener.onRecordStart();
                startAnimation();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mListener.onRecordStop();
                stopAnimation();
                break;
        }
        return true;
    }

    private void startAnimation() {

    }

    private void stopAnimation() {

    }

    public void setOnRecordListener(OnRecordListener listener) {
        mListener = listener;
    }

    public interface OnRecordListener {
        void onRecordStart();

        void onRecordStop();
    }
}

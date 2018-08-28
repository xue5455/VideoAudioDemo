package com.xue.douyin.common.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.xue.douyin.R;

/**
 * Created by 薛贤俊 on 2018/4/24.
 */

public class ProgressBar extends View {

    private boolean thumbFocused;

    private Rect thumbArea = new Rect();

    private RectF thumbRect = new RectF();

    private int thumbLeft;

    private int thumbTop;

    private int maxThumbWidth;

    private int maxThumbHeight;

    private int thumbWidth;

    private int thumbHeight;

    private int progressHeight;

    private Paint bgPaint;

    private Paint thumbPaint;

    private Paint passedPaint;

    private int thumbColor;

    private int backgroundColor;

    private int passedColor;

    private float bgRadius = 10;

    private float thumbRadius;

    private RectF bgRect = new RectF();

    private ValueAnimator widthAnimator;
    private ValueAnimator heightAnimator;

    private boolean initialized = false;

    public ProgressBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setup(context, attrs);
    }

    private void setup(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar);
        try {
            thumbColor = ta.getColor(R.styleable.ProgressBar_pb_thumb_color,
                    Color.parseColor("#face15"));
            backgroundColor = ta.getColor(R.styleable.ProgressBar_pb_progress_color,
                    Color.parseColor("#ffffff"));
            passedColor = ta.getColor(R.styleable.ProgressBar_pb_passed_color,
                    Color.parseColor("#ffffff"));
            thumbRadius = ta.getDimensionPixelSize(R.styleable.ProgressBar_pb_thumb_radius, 0);
            bgRadius = ta.getDimensionPixelSize(R.styleable.ProgressBar_pb_progress_radius, 0);
            progressHeight = ta.getDimensionPixelSize(R.styleable.ProgressBar_pb_progress_height, 10);
        } finally {
            ta.recycle();
        }
        thumbPaint = newPaint(thumbColor);
        bgPaint = newPaint(backgroundColor);
        passedPaint = newPaint(passedColor);

        thumbWidth = 20;
        thumbHeight = 60;
        maxThumbWidth = 30;
        maxThumbHeight = 80;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (initialized) {
            return;
        }
        initialized = true;
        int top = (getMeasuredHeight() - progressHeight) / 2;
        bgRect.set(maxThumbWidth - thumbWidth, top, getMeasuredWidth() - (maxThumbWidth - thumbWidth), top + progressHeight);
        top = (getMeasuredHeight() - thumbHeight) / 2;
        thumbLeft = maxThumbWidth - thumbWidth;
        thumbTop = (getMeasuredHeight()-thumbHeight)/2;
        thumbRect.set(0, top, thumbWidth, top + thumbHeight);
        thumbArea.set(thumbLeft, 0, thumbLeft + thumbWidth, getMeasuredHeight());
    }

    private Paint newPaint(int color) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        return paint;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return onActionDown(event);
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return super.onTouchEvent(event);
    }


    private boolean onActionDown(MotionEvent event) {
        if (thumbArea.contains((int) event.getX(), (int) event.getY())) {

            return true;
        }
        return false;
    }

    /**
     * @param enlarge 是否放大
     */
    private void startAnimator(boolean enlarge) {
        if (widthAnimator == null) {
            widthAnimator = ValueAnimator.ofInt(0, 0);
            widthAnimator.setDuration(300);
            widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
//                    int width =
                }
            });
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRoundRect(bgRect, bgRadius, bgRadius, bgPaint);
        canvas.drawRoundRect(thumbRect, thumbRadius, thumbRadius, thumbPaint);
    }
}

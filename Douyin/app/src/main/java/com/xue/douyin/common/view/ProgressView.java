package com.xue.douyin.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;


import com.xue.douyin.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 薛贤俊 on 2018/2/25.
 */

public class ProgressView extends View {

    private static final int RADIUS = 4;

    private static final int DIVIDER_WIDTH = 2;

    private static final int BACKGROUND_COLOR = Color.parseColor("#22000000");

    private static final int CONTENT_COLOR = Color.parseColor("#face15");

    private static final int DIVIDER_COLOR = Color.WHITE;

    private Paint mPaint;

    private float mRadius = RADIUS;

    private int mBackgroundColor = BACKGROUND_COLOR;

    private int mContentColor = CONTENT_COLOR;

    private int mDividerColor = DIVIDER_COLOR;

    private int mDividerWidth = DIVIDER_WIDTH;

    private float mLoadingProgress;

    private List<Float> mProgressList = new ArrayList<>();

    public ProgressView(Context context) {
        super(context);
        init();
    }

    public ProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ProgressView);
        try {
            mRadius = ta.getDimensionPixelSize(R.styleable.ProgressView_pv_radius, RADIUS);
            mBackgroundColor = ta.getColor(R.styleable.ProgressView_pv_bg_color, BACKGROUND_COLOR);
            mContentColor = ta.getColor(R.styleable.ProgressView_pv_content_color, CONTENT_COLOR);
            mDividerColor = ta.getColor(R.styleable.ProgressView_pv_divider_color, DIVIDER_COLOR);
            mDividerWidth = ta.getDimensionPixelSize(R.styleable.ProgressView_pv_divider_width, DIVIDER_WIDTH);
        } finally {
            ta.recycle();
        }
    }


    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBackground(canvas);
        drawContent(canvas);
        drawDivider(canvas);
    }

    private void drawBackground(Canvas canvas) {
        mPaint.setColor(mBackgroundColor);
        canvas.drawRoundRect(new RectF(0, 0, getMeasuredWidth(), getMeasuredHeight()),
                mRadius, mRadius, mPaint);
    }

    private void drawContent(Canvas canvas) {
        float total = 0;
        for (float progress : mProgressList) {
            total += progress;
        }
        total += mLoadingProgress;
        int width = (int) (total * getMeasuredWidth());
        mPaint.setColor(mContentColor);
        canvas.drawRoundRect(new RectF(0, 0, width, getMeasuredHeight()),
                mRadius, mRadius, mPaint);
        if (width < mRadius) {
            return;
        }
        canvas.drawRect(new RectF(mRadius, 0, width, getMeasuredHeight()), mPaint);
    }

    private void drawDivider(Canvas canvas) {
        mPaint.setColor(mDividerColor);
        int left = 0;
        for (float progress : mProgressList) {
            left += progress * getMeasuredWidth();
            canvas.drawRect(left - mDividerWidth, 0, left, getMeasuredHeight(), mPaint);
        }
    }

    public void setLoadingProgress(float loadingProgress) {
        mLoadingProgress = loadingProgress;
        invalidate();
    }

    public void addProgress(float progress) {
        mLoadingProgress = 0;
        mProgressList.add(progress);
        invalidate();
    }

    public void deleteProgress() {
        mProgressList.remove(mProgressList.size() - 1);
        invalidate();
    }

    public void clear() {
        mProgressList.clear();
        invalidate();
    }
}

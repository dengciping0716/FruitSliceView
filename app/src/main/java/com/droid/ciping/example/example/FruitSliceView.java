package com.droid.ciping.example.example;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * 模仿水果忍者，刀光效果
 */
public class FruitSliceView extends View {

    private int maxLen = 15;    //最大轨迹长度
    private float addWidth = 3f;    //刀光增量宽度

    private Deque<PointF> pointFS = new ArrayDeque<>(maxLen);   //刀光上边框点集合
    private Deque<PointF> pointFSClose = new ArrayDeque<>(maxLen);  //刀光下边框点集合

    private Paint mPaint;
    private Shader mShader;//刀光填充颜色

    private boolean isDiff = false;
    //刀光减少
    Runnable diff = new Runnable() {
        @Override
        public void run() {
            PointF pointF = pointFS.pollFirst();
            int delayMillis = 50;
            if (null != pointF) {
                postInvalidate();
                postDelayed(diff, delayMillis);
                return;
            }

            if (isDiff) {
                postDelayed(diff, delayMillis);
            }
        }
    };

    //刀光清空
    Runnable clearP = new Runnable() {
        @Override
        public void run() {
            pointFS.clear();
            postInvalidate();
        }
    };

    public FruitSliceView(Context context) {
        super(context);
        init();
    }

    public FruitSliceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FruitSliceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);

        mPaint = new Paint();

        mPaint.setAntiAlias(true);
        mPaint.setPathEffect(new CornerPathEffect(5));

        mShader = new LinearGradient(0, 0, 40, 60,
                new int[]{
                        Color.parseColor("#f8f8f8"),
                        Color.parseColor("#C0C0C0"),
                        Color.parseColor("#f8f8f8"),
                },
                null,
                Shader.TileMode.CLAMP);

        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        if (widthPixels > 1080) {
            addWidth *= 2;
        }
    }

    public void onTouch(MotionEvent event) {
        Rect outRect = new Rect();
        getGlobalVisibleRect(outRect);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isDiff = true;
                removeCallbacks(diff);
                removeCallbacks(clearP);
                postDelayed(diff, 80);
                pointFS.clear();

                pointFS.addLast(new PointF(event.getX() - outRect.left, event.getY() - outRect.top));
                postInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                onMove(event.getX() - outRect.left, event.getY() - outRect.top);
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                isDiff = false;

                postDelayed(clearP, 400);
                break;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(diff);
        removeCallbacks(clearP);
        super.onDetachedFromWindow();
    }

    private void onMove(float x, float y) {
        if (pointFS.size() >= maxLen - 1) {
            pointFS.pollFirst();
        }
        pointFS.addLast(new PointF(x, y));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        PointF start = pointFS.peek();
        if (start == null) return;

        Path path = creatPath();
        //边框
        mPaint.setColor(Color.BLACK);
        mPaint.setStrokeWidth(1);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setShader(null);
        canvas.drawPath(path, mPaint);

        //填充
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setShader(mShader);
        canvas.drawPath(path, mPaint);

    }

    private Path creatPath() {
        PointF start = pointFS.peek();

        Path path = new Path();
        path.moveTo(start.x, start.y);

        int width = 1;

        PointF pre = null;
        PointF next = null;

        for (Iterator<PointF> iter = pointFS.iterator(); iter.hasNext(); ) {
            next = iter.next();
            if (iter.hasNext()) {

                float v = width / 2;
                float k = 0; //计算斜率，解决45°角为一条线的BUG
                if (pre != null) k = (next.y - pre.y) / (next.x - pre.x);
                if (Math.abs(1 - k) < 0.9) {
                    path.lineTo(next.x, next.y - v);
                    pointFSClose.addFirst(new PointF(next.x, next.y + v));
                } else {
                    path.lineTo(next.x - v, next.y - v);
                    pointFSClose.addFirst(new PointF(next.x + v, next.y + v));
                }

                pre = next;
            } else {
                path.lineTo(next.x, next.y);
            }

            width += addWidth;
        }

        for (; pointFSClose.peekFirst() != null; ) {
            PointF pf = pointFSClose.pollFirst();
            path.lineTo(pf.x, pf.y);
        }
        path.close();

        return path;
    }
}

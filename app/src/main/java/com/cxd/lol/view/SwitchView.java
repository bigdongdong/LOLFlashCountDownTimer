package com.cxd.lol.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class SwitchView extends View {
    private final float mInnerPaddingRadio = 0.07f ;
    private final int mOpenFillColor = Color.WHITE ;
    private final int mCloseFillColor = Color.GRAY ;
    private final int mBackgroundColor = Color.parseColor("#CCCCCC");
    @NonNull private final GradientDrawable mBackground ;
    @NonNull private final Paint mCirclePaint ;
    @NonNull private final Rect mRect ;
    private boolean mState = true; //true-open  false-close
    @Nullable private Callback mCallback ;

    public SwitchView(Context context) {
        this(context,null,0,0);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0,0);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mBackground = new GradientDrawable();
        mRect = new Rect();
        mCirclePaint = new Paint();
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setAntiAlias(true);

        this.setClickable(true);
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mState = !mState ;
                postInvalidate();
                if(mCallback != null){
                    mCallback.onStateChanged(mState);
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(getBackground() == null){
            final int radius = getMeasuredHeight() >> 1;
            mBackground.setColor(mBackgroundColor);
            mBackground.setCornerRadius(radius);
            this.setBackground(mBackground);
        }
    }

    public void initialize(boolean state){
        mState = state ;
        invalidate();
    }

    public void observe(Callback callback){
        this.mCallback = callback;
    }

    @Override
    @Deprecated
    public void setOnClickListener(@Nullable OnClickListener l) {
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        final int innerPadding = (int) (getMeasuredHeight()*mInnerPaddingRadio);
        if(mState){
            mRect.set(0,0,getMeasuredHeight(),getMeasuredHeight());
        }else{
            mRect.set(getMeasuredWidth()-getMeasuredHeight(),0,getMeasuredWidth(),getMeasuredHeight());
        }
        mRect.inset(innerPadding,innerPadding);

        mCirclePaint.setColor(mState?mOpenFillColor:mCloseFillColor);
        canvas.drawCircle(mRect.centerX(),mRect.centerY(),mRect.width()>>1,mCirclePaint);
    }

    public interface Callback{
        void onStateChanged(boolean state);
    }
}

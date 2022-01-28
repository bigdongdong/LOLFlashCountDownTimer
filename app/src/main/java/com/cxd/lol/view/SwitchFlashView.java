package com.cxd.lol.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class SwitchFlashView extends LinearLayout {
    private final String TAG = "SwitchFlashViewTAG";
    private final float mSwitchWidthRadioOfParentWidth = 0.8f ;
    private final float mSwitchHeightRadioOfSelfWidth = 0.45f ;
    private final int mOffsetVerticalPx = 15 ;
    private boolean mIsCountingDown = false ;

    private SwitchView mSwitchView ;
    private FlashView mFlashView ;

    public SwitchFlashView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SwitchFlashView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public SwitchFlashView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        this.setOrientation(LinearLayout.VERTICAL);
        this.setGravity(Gravity.CENTER);

        this.removeAllViews();
        mSwitchView = new SwitchView(context);
        mSwitchView.initialize(true);
        this.addView(mSwitchView);

        mFlashView = new FlashView(context);
        this.addView(mFlashView);

        mSwitchView.observe(new SwitchView.Callback() {
            @Override
            public void onStateChanged(boolean state) {
                /*TODO ……*/
                mFlashView.setImageDrawable(new ColorDrawable(state?Color.YELLOW:Color.GRAY));
            }
        });

        mFlashView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*判断是否锁定*/
                if(mIsCountingDown){
                    return;
                }

                mIsCountingDown = true ;
                final int totalMs = 5 * 60 * 1000 ;
                new CountDownTimer(totalMs,1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                        final float progress = (1.0f - millisUntilFinished * 1.0f/totalMs) * 100;
                        mFlashView.setProgress(progress);
                    }

                    @Override
                    public void onFinish() {
                        mIsCountingDown = false ;
                        mFlashView.setProgress(100);
                        /*TODO trigger more*/
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int switchWidth = (int) (getMeasuredWidth()* mSwitchWidthRadioOfParentWidth) ;
        final int height = (int) (switchWidth * mSwitchHeightRadioOfSelfWidth + mOffsetVerticalPx + getMeasuredWidth());
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height,MeasureSpec.AT_MOST));
        getChildAt(0).measure(MeasureSpec.makeMeasureSpec(switchWidth,MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (switchWidth*mSwitchHeightRadioOfSelfWidth),MeasureSpec.EXACTLY));
        getChildAt(1).measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(),MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(),MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        mFlashView.offsetTopAndBottom(mOffsetVerticalPx);
    }

    private class FlashView extends androidx.appcompat.widget.AppCompatImageView {
        private float mAngle ; // 0 ~ 360
        private final Path mClipPath;
        private final Path mDrawPath;
        private final Paint mProgressPaint ;
        private RectF mRectF ;
        private final float mRoundRadiusRadioOfParentWidth = 0.1f;
        private final int mProgressColor = Color.parseColor("#33111111");

        public FlashView(Context context) {
            super(context);
            mClipPath = new Path();
            mDrawPath = new Path();
            mRectF = new RectF();
            mProgressPaint = new Paint();
            mProgressPaint.setColor(mProgressColor);
            mProgressPaint.setStyle(Paint.Style.FILL);
            mProgressPaint.setAntiAlias(true);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mRectF.set(0,0,getMeasuredWidth(),getMeasuredHeight());
            final float radius = getMeasuredWidth() * mRoundRadiusRadioOfParentWidth ;
            mClipPath.addRoundRect(mRectF,radius,radius, Path.Direction.CCW);
        }

        void setProgress(float progress){
            if(progress < 0.0f){
                progress = 0.0f;
            }else if(progress > 100.0f){
                progress = 100.0f ;
            }
            mAngle = progress/100*360;
            postInvalidate();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.clipPath(mClipPath);
            super.draw(canvas);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            final int ew /*edge-width*/ = getMeasuredWidth();
            final int r /*radius*/ = ew >> 1 ;
            float tAngle /*保证始终是锐角，确保Math.tan仅对锐角求正切值*/;
            int tDist /*基于锐角的偏移量*/;

            mDrawPath.reset();
            mDrawPath.moveTo(r,0);
            mDrawPath.lineTo(r,r);

            if(mAngle == 360){
                return;
            } else if(mAngle == 0){
                canvas.drawColor(mProgressColor);
                return;
            }

            if(mAngle > 0 && mAngle < 90){
                tAngle = mAngle ;
                tDist = (int) (r * Math.tan(Math.toRadians(tAngle)));
                mDrawPath.lineTo(r+tDist,0);
                mDrawPath.lineTo(ew,0);
                mDrawPath.lineTo(ew,ew);
                mDrawPath.lineTo(0,ew);
                mDrawPath.lineTo(0,0);
            }else if(mAngle == 90){
                mDrawPath.lineTo(ew,r);
                mDrawPath.lineTo(ew,ew);
                mDrawPath.lineTo(0,ew);
                mDrawPath.lineTo(0,0);
            }else if(mAngle > 90 && mAngle < 180){
                tAngle = 180 - mAngle ;
                tDist = (int) (r / Math.tan(Math.toRadians(tAngle)));
                mDrawPath.lineTo(ew,r+tDist);
                mDrawPath.lineTo(ew,ew);
                mDrawPath.lineTo(0,ew);
                mDrawPath.lineTo(0,0);
            }else if(mAngle == 180){
                mDrawPath.lineTo(r,ew);
                mDrawPath.lineTo(0,ew);
                mDrawPath.lineTo(0,0);
            }else if(mAngle > 180 && mAngle < 270){
                tAngle = mAngle - 180;
                tDist = (int) (r * Math.tan(Math.toRadians(tAngle)));
                mDrawPath.lineTo(r-tDist,ew);
                mDrawPath.lineTo(0,ew);
                mDrawPath.lineTo(0,0);
            }else if(mAngle == 270){
                mDrawPath.lineTo(0,r);
                mDrawPath.lineTo(0,0);
            }else if(mAngle > 270){
                tAngle = 360 - mAngle;
                tDist = (int) (r * Math.tan(Math.toRadians(tAngle)));
                mDrawPath.lineTo(r - tDist,0);
            }

            mDrawPath.close();
            canvas.drawPath(mDrawPath,mProgressPaint);
        }
    }
}

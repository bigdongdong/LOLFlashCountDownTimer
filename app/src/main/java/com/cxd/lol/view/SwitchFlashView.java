package com.cxd.lol.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SwitchFlashView extends LinearLayout {
    private final String TAG = "SwitchFlashViewTAG";
    private final float mSwitchWidthRatioOfParentWidth = 0.7f ;
    private final float mSwitchHeightRatioOfSelfWidth = 0.45f ;
    private final float mResetBtnWidthRatioOfParentWidth = 0.4f ;
    private final int mOffsetVerticalPx = 15 ;
    private boolean mIsCountingDown = false ;

    private boolean mSwitchState = true;
    private SwitchView mSwitchView ;
    private FlashView mFlashView ;
    private CountDownTimer mCountDownTimer ;

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

        PressureView pv = new PressureView(context);
        this.addView(pv);

        ImageView resetIV = new ImageView(context);
        resetIV.setBackgroundColor(Color.parseColor("#dddddd"));
        this.addView(resetIV);
        resetIV.setVisibility(INVISIBLE);

        mFlashView = new FlashView(context);
        FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(-1,-1);
        mFlashView.setLayoutParams(fp);
        pv.addView(mFlashView);


        mFlashView.setImageDrawable(new ColorDrawable(Color.YELLOW));
        mSwitchView.observe(new SwitchView.Callback() {
            @Override
            public void onStateChanged(boolean state) {
                /*TODO ……*/
                mSwitchState = state ;
                mFlashView.setImageDrawable(new ColorDrawable(state?Color.YELLOW:Color.GRAY));
            }
        });

        pv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*判断是否锁定*/
                if(mIsCountingDown || !mSwitchState){
                    return;
                }else if(mCountDownTimer == null){
                    final int totalMs = 5 * 60 * 1000 ; /*TODO */
                    mCountDownTimer = new CountDownTimer(totalMs,100){
                        @Override
                        public void onTick(long millisUntilFinished) {
                            float progress = (1.0f - millisUntilFinished * 1.0f/totalMs) * 100;
                            mFlashView.setProgress(progress);
                        }

                        @Override
                        public void onFinish() {
                            mIsCountingDown = false ;
                            mFlashView.setProgress(100);
                            /*TODO trigger more*/
                            resetIV.setVisibility(INVISIBLE);
                        }
                    };
                }

                mIsCountingDown = true ;
                mCountDownTimer.start();
                resetIV.setVisibility(VISIBLE);
            }
        });

        resetIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*reset*/
                if(mCountDownTimer != null){
                    mCountDownTimer.cancel();
                    mIsCountingDown = false ;
                    mFlashView.setProgress(100);
                    resetIV.setVisibility(INVISIBLE);
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        getChildAt(1).offsetTopAndBottom(mOffsetVerticalPx);
        getChildAt(2).offsetTopAndBottom(mOffsetVerticalPx*2);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int switchWidth = (int) (getMeasuredWidth()* mSwitchWidthRatioOfParentWidth) ;
        final int height = (int) (switchWidth * mSwitchHeightRatioOfSelfWidth + mOffsetVerticalPx + getMeasuredWidth()
                + mOffsetVerticalPx + getMeasuredWidth() * mResetBtnWidthRatioOfParentWidth) ;
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height,MeasureSpec.AT_MOST));
        getChildAt(0).measure(MeasureSpec.makeMeasureSpec(switchWidth,MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (switchWidth* mSwitchHeightRatioOfSelfWidth),MeasureSpec.EXACTLY));
        getChildAt(1).measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth(),MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(),MeasureSpec.EXACTLY));
        getChildAt(2).measure(MeasureSpec.makeMeasureSpec((int) (getMeasuredWidth() * mResetBtnWidthRatioOfParentWidth),MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec((int) (getMeasuredWidth()*mResetBtnWidthRatioOfParentWidth),MeasureSpec.EXACTLY));
    }

    public void lock(boolean lock /*是否锁定*/){
       mSwitchView.setVisibility(lock?INVISIBLE:VISIBLE);
    }

    private class FlashView extends androidx.appcompat.widget.AppCompatImageView {
        private float mAngle ; // 0 ~ 360
        private final Path mClipPath;
        private final Path mDrawPath;
        private final Paint mProgressPaint ;
        private RectF mRectF ;
        private final float mRoundRadiusRatioOfParentWidth = 0.1f;
        private final int mProgressColor = Color.parseColor("#55111111");

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
            final float radius = getMeasuredWidth() * mRoundRadiusRatioOfParentWidth;
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

    /**
     * create by cxd on 2020/12/29
     * 压力View 手指按压会有阻尼缩放以及回弹动画
     */
    private class PressureView extends FrameLayout {
        private float mScale = 1.0f; //0.75 ~ 0.9 ~ 1.0
        private boolean isFingerOut = true;
        private ValueAnimator autoAnimator ;
        private ValueAnimator manualAnimator ;
        private OnClickListener mOnClickListener ;

        private final float MIN_RATIO = 0.85f ;  //最小比例
        private final float CENTER_RATIO = 0.92f ; //中间比例

        public PressureView(@NonNull Context context) {
            this(context,null);
        }

        public PressureView(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);

            this.setWillNotDraw(false);
            this.setClickable(true);
        }

        private long lastDownTime ;
        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if(isAnimating()){
                return true ;
            }

            final int action = ev.getAction();
            switch(action){
                case MotionEvent.ACTION_DOWN:
                    isFingerOut = false ;
                    lastDownTime = System.currentTimeMillis();
                    autoAnimate(0);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isFingerOut = true ;
                    if(manualAnimator != null && manualAnimator.isRunning()){
                        manualAnimator.cancel();
                    }

                    final long nowTime = System.currentTimeMillis();
                    if(nowTime - lastDownTime < 200){
                        autoAnimate(-1);
                    }else{
                        autoAnimate(1);
                    }

                    break;
            }
            return true;
        }

        @Override
        public void setOnClickListener(@Nullable OnClickListener l) {
            this.mOnClickListener = l ;
        }

        private boolean isAnimating(){
            return (autoAnimator != null && autoAnimator.isRunning())
                    && (manualAnimator != null && manualAnimator.isRunning()) ;
        }

        /**
         *
         * @param action 0-down  1-up&cancel -1-quick
         */
        private void autoAnimate(int action){
            if(autoAnimator == null){
                autoAnimator = new ValueAnimator();
                autoAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mScale = (float) animation.getAnimatedValue();
                        PressureView.this.setScaleX(mScale);
                        PressureView.this.setScaleY(mScale);
                    }
                });
            }

            switch(action){
                case 0:
                    autoAnimator.setFloatValues(1.0f,CENTER_RATIO);
                    autoAnimator.setDuration(100);
                    break;
                case 1:
                    autoAnimator.setFloatValues(mScale,1.0f);
                    autoAnimator.setDuration(100);
                    break;
                case -1:
                    autoAnimator.setFloatValues(mScale,CENTER_RATIO,1.0f);
                    autoAnimator.setDuration(200);
                    break;
            }

            autoAnimator.removeAllListeners();
            switch(action){
                case 0:
                    autoAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //判断手指还在不在上面
                            if(!isFingerOut){
                                //继续手动动画
                                manualAnimate();
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
                    break;
                case 1:
                case -1:
                    if(mOnClickListener != null){
                        autoAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                mOnClickListener.onClick(PressureView.this);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                    }
                    break;
            }

            autoAnimator.start();

        }

        private void manualAnimate(){
            if(manualAnimator == null){
                manualAnimator = new ValueAnimator();
                manualAnimator.setDuration(1500);
                manualAnimator.setInterpolator(new DecelerateInterpolator(2)); //先快后慢，阻尼效果
                manualAnimator.setFloatValues(CENTER_RATIO,MIN_RATIO);
                manualAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mScale = (float) animation.getAnimatedValue();
                        PressureView.this.setScaleX(mScale);
                        PressureView.this.setScaleY(mScale);
                    }
                });
            }
            manualAnimator.start();
        }
    }
}

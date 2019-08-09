package com.baidu.aip.excep.widget;
/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.common.system.DisplayUtil;

import demo.face.aip.baidu.com.facesdk.R;

/**
 * 人脸检测区域View
 */
public class FaceRoundView extends View{

        public static final float SURFACE_HEIGHT = 1000f;
        public static final float SURFACE_RATIO = 0.75f;
        public static final float WIDTH_SPACE_RATIO = 0.33f;
        public static final float HEIGHT_RATIO = 0.1f;
        public static final float HEIGHT_EXT_RATIO = 0.2f;
        public static final int CIRCLE_SPACE = 2;
        public static final int PATH_SPACE = 16;
        public static final int PATH_SMALL_SPACE = 12;
        public static final int PATH_WIDTH = 6;

        public final int COLOR_BG = Color.parseColor("#FFFFFF");
        public final int COLOR_RECT = COLOR_BG;
        public final int COLOR_ROUND = Color.parseColor("#DCDCDC");
        public final int COLOR_ROUND_PASS = Color.parseColor("#10B0D9");


        private Paint mBGPaint;//全局背景
        private Paint mPathPaint;//扫描区外框，就是那个圈外面的
        private Paint mFaceRoundPaint;

        private Paint mTextTopPaint;//画上面的提示语
        private Paint mTextBottomPaint;//画下面的提示语
        private Paint mTextTipPaint;//画提示语
        private Paint mArcTipPaint;//画提示语的背景

        private RectF mOvalRect;
        private Rect mFaceRect;
        private Rect mFaceDetectRect;

        private float mX;//原点
        private float mY;//原点
        private float mR;//半径
        private boolean mIsSuccess = false;

        private float mTipY;
        private float mTipX;
        private String mTipMessage;
        private float mTopTextX;//
        private float mTopTextY;//
        private float mBottomTextX;//
        private float mBottomTextY;//
        private final Bitmap mSuccessBitmap;

        public FaceRoundView(Context context, AttributeSet attrs) {
            super(context, attrs);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            float pathWidth = DensityUtils.dip2px(context, PATH_WIDTH);

            mBGPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBGPaint.setColor(COLOR_BG);
            mBGPaint.setStyle(Paint.Style.STROKE);
            mBGPaint.setAntiAlias(true);
            mBGPaint.setDither(true);

            mPathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPathPaint.setColor(COLOR_ROUND);
            mPathPaint.setStrokeWidth(pathWidth);
            mPathPaint.setStyle(Paint.Style.STROKE);
            mPathPaint.setAntiAlias(true);
            mPathPaint.setDither(true);


            mFaceRoundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mFaceRoundPaint.setColor(COLOR_ROUND);
            mFaceRoundPaint.setStyle(Paint.Style.FILL);
            mFaceRoundPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mFaceRoundPaint.setAntiAlias(true);
            mFaceRoundPaint.setDither(true);


            mTextBottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mTextBottomPaint.setColor(Color.parseColor("#FF10B0D9"));
            mTextBottomPaint.setTextSize(DensityUtils.dip2px(context, 17));

            mTextTopPaint = new Paint(mTextBottomPaint);
            mTextTopPaint.setColor(Color.parseColor("#FF616161"));
            mTextTopPaint.setTypeface(Typeface.DEFAULT_BOLD);
            mTextTopPaint.setTextSize(DensityUtils.dip2px(context, 20));

            mTextTipPaint = new Paint(mTextBottomPaint);
            mTextTipPaint.setColor(Color.WHITE);
            mTextTipPaint.setTextSize(DensityUtils.dip2px(context, 12));

            mArcTipPaint = new Paint();
            mArcTipPaint.setColor(Color.parseColor("#646464"));
            mArcTipPaint.setColor(Color.BLACK);
            mArcTipPaint.setAntiAlias(true);//取消锯齿
            mArcTipPaint.setStyle(Paint.Style.FILL);//设置画圆弧的画笔的属性为描边(空心)，个人喜欢叫它描边，叫空心有点会引起歧义
            mArcTipPaint.setStrokeWidth(pathWidth);

            mSuccessBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_success);

        }


        public void onRefreshTipsView(String message) {
            mTipMessage=message;
            if (!TextUtils.isEmpty(message)) {
                if (mTipY == 0) {
                    float mTipHeight = mTextTipPaint.getFontMetrics().top - mTextTipPaint.getFontMetrics().bottom + DensityUtils.dip2px(getContext(), 10);
                    mTipY = (mY - mR) + mTipHeight + DisplayUtil.dip2px(getContext(), 40);
                }
                float mTipWidth = mTextTipPaint.measureText(message);
                mTipX = mX - mTipWidth / 2;
                postInvalidate();
            }
        }

        public void onRefreshSuccessView(boolean mIsSuccess) {
            this.mIsSuccess = mIsSuccess;
            mPathPaint.setColor(mIsSuccess ? COLOR_ROUND_PASS : COLOR_ROUND);
            postInvalidate();
        }



        public float getRound() {
            return mR;
        }

        public Rect getFaceRoundRect() {
            return mFaceRect;
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            float canvasWidth = right - left;
            float canvasHeight = bottom - top;

            float x = canvasWidth / 2;
            float y = (canvasHeight / 2) - ((canvasHeight / 2) * HEIGHT_RATIO);
            float r = (canvasWidth / 2) - ((canvasWidth / 2) * WIDTH_SPACE_RATIO);
            if (mFaceRect == null) {
                mFaceRect = new Rect((int) (x - r),
                        (int) (y - r),
                        (int) (x + r),
                        (int) (y + r));
            }
            if (mFaceDetectRect == null) {
                float hr = r + (r * HEIGHT_EXT_RATIO);
                mFaceDetectRect = new Rect((int) (x - r),
                        (int) (y - hr),
                        (int) (x + r),
                        (int) (y + hr));
            }

            if (mOvalRect == null) {
                mOvalRect = new RectF(mX - mR, mY - mR,
                        mX + mR, mY + mR);
            }

            mX = x;
            mY = y;
            mR = r;
            mBGPaint.setStrokeWidth((top - bottom) / 2 - mR);

            mTopTextX = mX - mTextTopPaint.measureText("赏个脸呗") / 2;
            mTopTextY = mY - mR - DensityUtils.dip2px(getContext(), 20);
            mBottomTextX = mX - mTextBottomPaint.measureText("拿起手机，眨眨眼") / 2;
            mBottomTextY = mY + mR + DensityUtils.dip2px(getContext(), 40);


        }

        @Override
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            //画背景
//        canvas.drawColor(Color.WHITE);
//        canvas.drawARGB();
            canvas.drawPaint(mBGPaint);
//        canvas.drawCircle(mX, mY, getHeight() / 2, mBGPaint);
            //画外框
            canvas.drawCircle(mX, mY, mR - CIRCLE_SPACE + mPathPaint.getStrokeWidth(), mPathPaint);
            canvas.drawCircle(mX, mY, mR, mFaceRoundPaint);

            //画文字
            canvas.drawText("赏个脸呗！", mTopTextX, mTopTextY, mTextTopPaint);
            canvas.drawText("拿起手机，眨眨眼", mBottomTextX, mBottomTextY, mTextBottomPaint);

            //画提示语
//        canvas.drawArc(new RectF(mX, mY, 200, 200), -90, 120, true, mArcTipPaint);
            if (!TextUtils.isEmpty(mTipMessage)){
                canvas.drawText(mTipMessage, mTipX,mTipY,mTextTipPaint);
            }
            //画成功标识
            if (mIsSuccess) {
                canvas.drawBitmap(mSuccessBitmap, mX - mSuccessBitmap.getWidth() / 2, mY - mSuccessBitmap.getHeight() / 2, mPathPaint);
            }
        }


        public static Rect getPreviewDetectRect(int w, int pw, int ph) {
            float round = (w / 2) - ((w / 2) * WIDTH_SPACE_RATIO);
            float x = pw / 2;
            float y = (ph / 2) - ((ph / 2) * HEIGHT_RATIO);
            float r = (pw / 2) > round ? round : (pw / 2);
            float hr = r + (r * HEIGHT_EXT_RATIO);
            Rect rect = new Rect((int) (x - r),
                    (int) (y - hr),
                    (int) (x + r),
                    (int) (y + hr));
            return rect;
        }

}
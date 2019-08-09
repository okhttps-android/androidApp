package com.modular.apputils.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.common.system.DisplayUtil;
import com.core.app.ActionBackActivity;
import com.core.app.AppConstant;
import com.core.utils.CommonUtil;
import com.me.imageloader.ImageLoaderUtil;
import com.modular.apputils.R;
import com.modular.apputils.utils.Scheme;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.File;

import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * 单张图片预览
 *
 * @author Dean Tao
 * @version 1.0
 */
public class SingleImagePreviewActivity extends ActionBackActivity {

    private String mImageUri;
    private FrameLayout imageFl;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    @SuppressWarnings("unused")
    private TextView mProgressTextTv;
    PhotoViewAttacher mAttacher;
    private View mMoreMenuView;
    private PopupWindow mMoreWindow;
    private TextView sava_picture_tv;
    private TextView cancel_picture_tv;
    private String mSessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mImageUri = getIntent().getStringExtra(AppConstant.EXTRA_IMAGE_URI);
            mSessionId = getIntent().getStringExtra(AppConstant.EXTRA_IMAGE_SESSION);
        }
        getSupportActionBar().hide();
        setContentView(R.layout.activity_single_image_preview);
        initView();
    }

    private void initView() {
        mImageView = (ImageView) findViewById(R.id.image_view);
        imageFl = (FrameLayout) findViewById(R.id.image_fl);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressTextTv = (TextView) findViewById(R.id.progress_text_tv);

        boolean showProgress = false;
        // init status
        Log.i("wang", "mImageUri:" + mImageUri);
        Scheme scheme = Scheme.ofUri(mImageUri);
        Log.i("wang", "scheme:" + scheme);
        switch (scheme) {
            case HTTP:
            case HTTPS:// 需要网络加载的

                Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(mImageUri);
                if (bitmap == null || bitmap.isRecycled()) {
                    File file = ImageLoader.getInstance().getDiscCache().get(mImageUri);
                    if (file == null || !file.exists()) {// 文件不存在，那么就表示需要重新下载
                        showProgress = true;
                    }
                }
                break;
            case UNKNOWN:// 如果不知道什么类型，且不为空，就当做是一个本地文件的路径来加载
                if (TextUtils.isEmpty(mImageUri)) {
                    mImageUri = "";
                } else {
                    mImageUri = Uri.fromFile(new File(mImageUri)).toString();
                }
                break;
            default:
                // 其他 drawable asset类型不处理
                break;
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .displayer(new FadeInBitmapDisplayer(100))
                .showImageOnFail(R.drawable.image_download_fail_icon)
                .build();
        if (showProgress) {
            if (mSessionId == null) {
                ImageLoader.getInstance().displayImage(mImageUri, mImageView, options, mImageLoadingListener);
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
                ImageLoaderUtil.getInstance().loadImageWithCookie(mImageUri, mSessionId, mImageView, new RequestListener() {
                    @Override
                    public boolean onException(Exception e, Object o, Target target, boolean b) {
                        mProgressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Object o, Object o2, Target target, boolean b, boolean b1) {
                        mProgressBar.setVisibility(View.GONE);
                        mAttacher = new PhotoViewAttacher(mImageView);
                        mAttacher.update();

                        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                            @Override
                            public void onPhotoTap(View view, float x, float y) {
                                finish();
                                overridePendingTransition(0, R.anim.alpha_scale_out);
                            }

                            @Override
                            public void onOutsidePhotoTap() {

                            }
                        });

                        mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                longclickshowppw(mImageUri);
                                return true;
                            }
                        });
                        return false;
                    }
                });
            }
        } else {
            ImageLoader.getInstance().displayImage(mImageUri, mImageView, options, mImageLoadingListener);
        }
    }


    /**
     * @param ：长按点击弹出PopupWindow事件，
     * @author: FANGlh 2016-12-6
     */
    public void longclickshowppw(final String mImageUri) {

        mMoreMenuView = View.inflate(mContext, R.layout.layout_menu_common_save_picture, null);
        sava_picture_tv = (TextView) mMoreMenuView.findViewById(R.id.save_tv);
        cancel_picture_tv = (TextView) mMoreMenuView.findViewById(R.id.cancel_tv);

        mMoreWindow = new PopupWindow(mMoreMenuView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        mMoreWindow.setAnimationStyle(R.style.MenuAnimationFade);
        mMoreWindow.setBackgroundDrawable(new BitmapDrawable());
        mMoreWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closeMorePopupWindow();
            }
        });

        mMoreWindow.showAtLocation(getWindow().getDecorView(), Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(mContext, 0.5f);

        sava_picture_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.saveImageToLocal(getApplicationContext(), ImageLoader.getInstance().loadImageSync(mImageUri));
                closeMorePopupWindow();
            }
        });

        cancel_picture_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeMorePopupWindow();
            }
        });


    }

    private void closeMorePopupWindow() {
        if (mMoreWindow != null) {
            mMoreWindow.dismiss();
            DisplayUtil.backgroundAlpha(mContext, 1f);
        }

    }


    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, R.anim.alpha_scale_out);
    }

    @Override
    protected boolean onHomeAsUp() {
        finish();
        overridePendingTransition(0, R.anim.alpha_scale_out);
        return true;
    }

    private ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String arg0, View arg1) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
            mProgressBar.setVisibility(View.GONE);
            mAttacher = new PhotoViewAttacher((ImageView) arg1);
            mAttacher.update();

            mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float x, float y) {
                    finish();
                    overridePendingTransition(0, R.anim.alpha_scale_out);
                }

                @Override
                public void onOutsidePhotoTap() {

                }
            });

            mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longclickshowppw(mImageUri);
                    return true;
                }
            });
        }

        @Override
        public void onLoadingCancelled(String arg0, View arg1) {
            mProgressBar.setVisibility(View.GONE);
        }
    };

}

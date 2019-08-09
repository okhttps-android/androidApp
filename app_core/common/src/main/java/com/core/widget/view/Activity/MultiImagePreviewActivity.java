package com.core.widget.view.Activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.system.DisplayUtil;
import com.core.app.ActionBackActivity;
import com.core.app.R;
import com.core.app.AppConstant;
import com.core.net.http.Scheme;
import com.core.utils.CommonUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoViewAttacher;


/**
 * 图片集的预览
 * 
 * @author Dean Tao
 * @version 1.0
 */
public class MultiImagePreviewActivity extends ActionBackActivity {

	private ArrayList<String> mImages;
	private int mPosition;
	private boolean mChangeSelected;
	private PhotoViewAttacher mAttacher;
	private ViewPager mViewPager;
	private CheckBox mCheckBox;
	private TextView mIndexCountTv;
	private List<Integer> mRemovePosition = new ArrayList<Integer>();
	private View mMoreMenuView;
	private PopupWindow mMoreWindow;
	private TextView sava_picture_tv;
	private TextView cancel_picture_tv;
	private String mImageUri;
	private String imageUrl;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);//竖屏
		if (getIntent() != null) {
			mImages = (ArrayList<String>) getIntent().getSerializableExtra(AppConstant.EXTRA_IMAGES);
			mPosition = getIntent().getIntExtra(AppConstant.EXTRA_POSITION, 0);
			mChangeSelected = getIntent().getBooleanExtra(AppConstant.EXTRA_CHANGE_SELECTED, false);
		}
		if (mImages == null) {
			mImages = new ArrayList<String>();
		}
		getSupportActionBar().hide();
		setContentView(R.layout.activity_images_preview);
		initView();
	}

	@Override
	public void onBackPressed() {
		doFinish();
	}

	@Override
	protected boolean onHomeAsUp() {
		doFinish();
		return true;
	}

	private void doFinish() {
		if (mChangeSelected) {
			Intent intent = new Intent();
			ArrayList<String> resultImages = null;
			if (mRemovePosition.size() == 0) {
				resultImages = mImages;
			} else {
				resultImages = new ArrayList<String>();
				for (int i = 0; i < mImages.size(); i++) {
					if (!isInRemoveList(i)) {
						resultImages.add(mImages.get(i));
					}
				}
			}
			intent.putExtra(AppConstant.EXTRA_IMAGES, resultImages);
			setResult(RESULT_OK, intent);
		}
		finish();
	}

	private void initView() {
		mViewPager = (ViewPager) findViewById(R.id.view_pager);
		mIndexCountTv = (TextView) findViewById(R.id.index_count_tv);
		mCheckBox = (CheckBox) findViewById(R.id.check_box);
		mViewPager.setPageMargin(10);

		mViewPager.setAdapter(new ImagesAdapter());

		updateSelectIndex(mPosition);

		if (mPosition < mImages.size()) {
			mViewPager.setCurrentItem(mPosition);
		}

		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {
				updateSelectIndex(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	public void updateSelectIndex(final int index) {
		if (mPosition >= mImages.size()) {
			mIndexCountTv.setText(null);
		} else {
			mIndexCountTv.setText((index + 1) + "/" + mImages.size());
		}

		if (!mChangeSelected) {
			mCheckBox.setVisibility(View.GONE);
			return;
		}

		mCheckBox.setOnCheckedChangeListener(null);
		boolean removed = isInRemoveList(index);
		if (removed) {
			mCheckBox.setChecked(false);
		} else {
			mCheckBox.setChecked(true);
		}
		mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					removeFromRemoveList(index);
				} else {
					addInRemoveList(index);
				}
			}
		});
	}

	SparseArray<View> mViews = new SparseArray<View>();

	void addInRemoveList(int position) {
		if (!isInRemoveList(position)) {
			mRemovePosition.add(Integer.valueOf(position));
		}
	}

	void removeFromRemoveList(int position) {
		if (isInRemoveList(position)) {
			mRemovePosition.remove(Integer.valueOf(position));
		}
	}

	boolean isInRemoveList(int position) {
		return mRemovePosition.indexOf(Integer.valueOf(position)) != -1;
	}

	class ImagesAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return mImages.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View view = mViews.get(position );
			// init status
			imageUrl = mImages.get(position);
			Scheme scheme = Scheme.ofUri(imageUrl);
			if (view == null) {
				view = new ImageView(MultiImagePreviewActivity.this);
				mViews.put(position, view);
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						doFinish();
					}
				});
//				view.setOnLongClickListener(new View.OnLongClickListener() {
//					@Override
//					public boolean onLongClick(View v) {
//						longclickshowppw();
//						return true;
//					}
//				});
			}


			switch (scheme) {
			case HTTP:
			case HTTPS:// 需要网络加载的
				ImageLoader.getInstance().displayImage(imageUrl, (ImageView) view, mImageLoadingListener);
				break;
			case UNKNOWN:// 如果不知道什么类型，且不为空，就当做是一个本地文件的路径来加载
				if (!TextUtils.isEmpty(imageUrl)) {
					ImageLoader.getInstance().displayImage(Uri.fromFile(new File(imageUrl)).toString(), (ImageView) view,mImageLoadingListener);
//					ImageLoader.getInstance().displayImage(imageUrl, (ImageView) view, mImageLoadingListener);
				}
				break;
			default:
				// 其他 drawable asset类型不处理
				break;
			}
			mImageUri = Uri.fromFile(new File(imageUrl)).toString();
			container.addView(view);


			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = mViews.get(position);
			if (view == null) {
				super.destroyItem(container, position, object);
			} else {
				container.removeView(view);
			}
		}

	}

	/**
	 * @param ：长按点击弹出PopupWindow事件，
	 * @author: FANGlh 2016-12-6
	 */
	public void longclickshowppw(final String imageUrl){

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
				CommonUtil.saveImageToLocal(mContext, ImageLoader.getInstance().loadImageSync(imageUrl));
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


	private ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {
		@Override
		public void onLoadingStarted(String arg0, View arg1) {

		}

		@Override
		public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {

		}

		@Override
		public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
			mAttacher=new PhotoViewAttacher((ImageView) arg1);
			Log.i("Arison", "" + mAttacher.getMidScale());
			Log.i("Arison", "" + mAttacher.getMinimumScale());
			mAttacher.setMinimumScale(0.5f);
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
					longclickshowppw(imageUrl);
					return true;
				}
			});
		}

		@Override
		public void onLoadingCancelled(String arg0, View arg1) {

		}
	};
}

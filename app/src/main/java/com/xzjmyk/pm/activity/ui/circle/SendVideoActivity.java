package com.xzjmyk.pm.activity.ui.circle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.system.SystemUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.UploadFileResult;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.utils.helper.LoginHelper;
import com.core.xmpp.model.Area;
import com.modular.login.activity.LoginActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.applocation.UasLocationHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.LocationMapActivity;
import com.xzjmyk.pm.activity.ui.me.LocalVideoActivity;
import com.xzjmyk.pm.activity.util.im.helper.UploadService;
import com.xzjmyk.pm.activity.util.imageloader.BitmapUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendVideoActivity extends BaseActivity {

	private EditText mTextEdit;
	// Video Item
	private ImageView mImageView;
	private ImageView mIconImageView;
	private TextView mVideoTextTv;
	private View mFloatLayout;
	private Button mReleaseBtn;
	// data
	private int mSelectedId;
	private String mVideoFilePath;
	private Bitmap mThumbBmp;
	private long mTimeLen;
	private RelativeLayout mlocationrl;
	private TextView mlocationtv;
	private String baseAddr;
	private String last_address;
	private String last_location;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_videohi);
		mProgressDialog = ProgressDialogUtil.init(this, null, getString(R.string.please_wait));
		initView();
	}

	private void initView() {
		setTitle(R.string.qzone_send_video);
		// find view
		mTextEdit = (EditText) findViewById(R.id.text_edit);
		mImageView = (ImageView) findViewById(R.id.image_view);
		mIconImageView = (ImageView) findViewById(R.id.icon_image_view);
		mVideoTextTv = (TextView) findViewById(R.id.text_tv);
		mFloatLayout = findViewById(R.id.float_layout);
		mReleaseBtn = (Button) findViewById(R.id.release_btn);
		mlocationrl = (RelativeLayout) findViewById(R.id.location_rl);
		mlocationtv = (TextView) findViewById(R.id.location_tv);

//        if (!TextUtils.isEmpty(UasLocationHelper.getInstance().getUASLocation().getCityName())
//                && !TextUtils.isEmpty((UasLocationHelper.getInstance().getUASLocation().getName()))){
//            mlocationtv.setText((UasLocationHelper.getInstance().getUASLocation().getCityName()+"•"+
//                    UasLocationHelper.getInstance().getUASLocation().getName()));
//        }

		// init status
		mIconImageView.setBackgroundResource(R.drawable.icon_shipin_nor);
		mVideoTextTv.setText(R.string.circle_add_video);

		// set event
		mFloatLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SendVideoActivity.this, LocalVideoActivity.class);
				intent.putExtra(AppConstant.EXTRA_ACTION, AppConstant.ACTION_SELECT);
				if (mSelectedId != 0) {
					intent.putExtra(AppConstant.EXTRA_SELECT_ID, mSelectedId);
				}
				startActivityForResult(intent, 1);
			}
		});
//        mReleaseBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (TextUtils.isEmpty(mVideoFilePath) || mTimeLen <= 0) {
//                    return;
//                }
//                if (Build.VERSION.SDK_INT >= 11) {
//                    new UploadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                } else {
//                    new UploadTask().execute();
//                }
//            }
//        });

		mlocationrl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name = UasLocationHelper.getInstance().getUASLocation().getName();
				String address = UasLocationHelper.getInstance().getUASLocation().getAddress();
				baseAddr = StringUtil.isEmail(name) ? address : name;
				Intent i = new Intent(activity, LocationMapActivity.class);
				i.putExtra("qzoneaddr", baseAddr == null ? "" : baseAddr);
				i.putExtra("qzone_select_add", "qzone_select_add");
//                Log.i("baseAddr",baseAddr) ;
				startActivityForResult(i, 0x21);
			}
		});

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.send_qzone, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.send_qzone) {
			if (TextUtils.isEmpty(mVideoFilePath) || mTimeLen <= 0) {
				ToastMessage(getString(R.string.qzone_addd_nothing_video));
			} else if (Build.VERSION.SDK_INT >= 11) {
				new UploadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} else {
				new UploadTask().execute();
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == RESULT_OK && data != null) {// 选择视频的返回
			String filePath = data.getStringExtra(AppConstant.EXTRA_FILE_PATH);
			if (TextUtils.isEmpty(filePath)) {
				ToastUtil.showToast(this, R.string.qzone_select_failed);
				return;
			}
			File file = new File(filePath);
			if (!file.exists()) {
				ToastUtil.showToast(this, R.string.qzone_select_failed);
				return;
			}
			mVideoFilePath = filePath;
		    /* 获取缩略图显示 */

			Bitmap bitmap = ImageLoader.getInstance().getMemoryCache().get(filePath);
			if (bitmap == null || bitmap.isRecycled()) {
				bitmap = ThumbnailUtils.createVideoThumbnail(filePath, Thumbnails.MINI_KIND);
				if (bitmap != null) {
					try {
						ImageLoader.getInstance().getMemoryCache().put(filePath, bitmap);
					} catch (Exception e) {

					}
				}
			}

			if (bitmap != null && !bitmap.isRecycled()) {
				mThumbBmp = bitmap;
				mImageView.setImageBitmap(mThumbBmp);
			} else {
				mImageView.setImageBitmap(null);
			}

			mTimeLen = data.getLongExtra(AppConstant.EXTRA_TIME_LEN, 0);
			mSelectedId = data.getIntExtra(AppConstant.EXTRA_SELECT_ID, 0);
		} else if (resultCode == LocationMapActivity.REQUCODE) {
			PoiInfo info = data.getParcelableExtra(LocationMapActivity.REQUESTNAME);
			if (info != null) {
//                out_add_name_tv.setText(info.name);
				mlocationtv.setText(UasLocationHelper.getInstance().getUASLocation().getCityName() + "•" + info.name);
				last_address = info.address;
				last_location = info.name;
			}
		} else if (resultCode == LocationMapActivity.HIDDEN_REQUCODE) {
			mlocationtv.setText(getString(R.string.qzone_notshow_location));
			last_address = "";
			last_location = "";
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageView.setImageBitmap(null);
		mThumbBmp = null;
		mProgressDialog = null;
	}

	private ProgressDialog mProgressDialog;
	private String mVideoData;
	private String mImageData;

	private class UploadTask extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ProgressDialogUtil.show(mProgressDialog);
		}

		/**
		 * 上传的结果： <br/>
		 * return 1 Token过期，请重新登陆 <br/>
		 * return 2 视频为空，请重新录制 <br/>
		 * return 3 上传出错<br/>
		 * return 4 上传成功<br/>
		 */
		@Override
		protected Integer doInBackground(Void... params) {
			if (!LoginHelper.isTokenValidation()) {
				return 1;
			}
			if (TextUtils.isEmpty(mVideoFilePath)) {
				return 2;
			}

			// 保存视频缩略图至sd卡
			String imageSavePsth = CameraUtil.getOutputMediaFileUri(SendVideoActivity.this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE).getPath();
			if (!BitmapUtil.saveBitmapToSDCard(mThumbBmp, imageSavePsth)) {// 保存缩略图失败
				return 3;
			}

			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("userId", MyApplication.getInstance().mLoginUser.getUserId() + "");
			mapParams.put("access_token", MyApplication.getInstance().mAccessToken);

			List<String> dataList = new ArrayList<String>();
			dataList.add(mVideoFilePath);
			if (!TextUtils.isEmpty(imageSavePsth)) {
				dataList.add(imageSavePsth);
			}

			String result = new UploadService().uploadFile(mConfig.UPLOAD_URL, mapParams, dataList);

			Log.d("roamer", "UploadRecordResult:" + result);

			if (TextUtils.isEmpty(result)) {
				return 3;
			}

			UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
			boolean success = Result.defaultParser(SendVideoActivity.this, recordResult, true);
			if (success) {
				if (recordResult.getSuccess() != recordResult.getTotal()) {// 上传丢失了某些文件
					return 3;
				}
				if (recordResult.getData() != null) {
					UploadFileResult.Data data = recordResult.getData();
					if (data.getVideos() != null && data.getVideos().size() > 0) {
						while (data.getVideos().size() > 1) {// 因为正确情况下只有一个视频，所以要保证只有一个视频
							data.getVideos().remove(data.getVideos().size() - 1);
						}
						data.getVideos().get(0).setSize(new File(mVideoFilePath).length());
						data.getVideos().get(0).setLength(mTimeLen);
						mVideoData = JSON.toJSONString(data.getVideos(), UploadFileResult.sAudioVideosFilter);
					} else {
						return 3;
					}
					if (data.getImages() != null && data.getImages().size() > 0) {
						mImageData = JSON.toJSONString(data.getImages(), UploadFileResult.sImagesFilter);
					}

					Log.d("roamer", "mVideoData:" + mVideoData);
					Log.d("roamer", "mImageData:" + mImageData);
					return 4;
				} else {// 没有文件数据源，失败
					return 3;
				}
			} else {
				return 3;
			}

		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == 1) {
				ProgressDialogUtil.dismiss(mProgressDialog);
				startActivity(new Intent(SendVideoActivity.this, LoginActivity.class));
			} else if (result == 2) {
				ProgressDialogUtil.dismiss(mProgressDialog);
				ToastUtil.showToast(SendVideoActivity.this, R.string.qzone_video_file_not_exist);
			} else if (result == 3) {
				ProgressDialogUtil.dismiss(mProgressDialog);
				ToastUtil.showToast(SendVideoActivity.this, R.string.qzone_upload_failed);
			} else {
				sendAudio();
			}

		}
	}

	// 发布一条说说
	public void sendAudio() {
		Map<String, String> params = new HashMap<String, String>();

		params.put("access_token", MyApplication.getInstance().mAccessToken);

		// 消息类型：1=文字消息；2=图文消息；3=语音消息；4=视频消息；
		params.put("type", "4");

		// 消息标记：1：求职消息；2：招聘消息；3：普通消息；
		params.put("flag", "3");

		// 消息隐私范围 0=不可见；1=朋友可见；2=粉丝可见；3=广场
		params.put("visible", "3");

		params.put("text", mTextEdit.getText().toString());// 消息内容

		params.put("videos", mVideoData);// 消息内容

		if (!TextUtils.isEmpty(mImageData) && !mImageData.equals("{}") && !mImageData.equals("[{}]")) {
			params.put("images", mImageData);
		}
		ProgressDialogUtil.show(mProgressDialog);
		// 附加信息
		params.put("model", SystemUtil.getModel());
		params.put("osVersion", SystemUtil.getOsVersion());
		params.put("serialNumber", SystemUtil.getDeviceId(getApplicationContext()));

		double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
		double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();

		if (latitude != 0)
			params.put("latitude", String.valueOf(latitude));
		if (longitude != 0)
			params.put("longitude", String.valueOf(longitude));

		String cityname = UasLocationHelper.getInstance().getUASLocation().getCityName();
		String name = UasLocationHelper.getInstance().getUASLocation().getName();
		String address = UasLocationHelper.getInstance().getUASLocation().getAddress();
		String location = UasLocationHelper.getInstance().getUASLocation().getName();
		baseAddr = StringUtil.isEmail(name) ? address : name;

		// 是否显示定位
//        if (mlocationtv.getText().equals("不显示位置")){
//            // params.put("location", "");
//        }else
		if (!TextUtils.isEmpty(last_location) && !TextUtils.isEmpty(cityname)) {
			params.put("location", cityname + "•" + last_location);
		}
//        else if (!TextUtils.isEmpty(location)&& !TextUtils.isEmpty(cityname)){
//            params.put("location",cityname+ "•"+ location);
//        }
		Area area = Area.getDefaultCity();
		if (area != null) {
			params.put("cityId", String.valueOf(area.getId()));// 城市Id
		} else {
			params.put("cityId", "0");
		}
//        Lg.d(mConfig.MSG_ADD_URL);
		LogUtil.d(JSON.toJSONString(params));
		StringJsonObjectRequest<String> request = new StringJsonObjectRequest<String>(mConfig.MSG_ADD_URL, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError arg0) {
				ProgressDialogUtil.dismiss(mProgressDialog);
				ToastUtil.showErrorNet(SendVideoActivity.this);
			}
		}, new StringJsonObjectRequest.Listener<String>() {
			@Override
			public void onResponse(ObjectResult<String> result) {
				boolean parserResult = Result.defaultParser(SendVideoActivity.this, result, true);
				if (parserResult) {
					Intent intent = new Intent();
					intent.putExtra(AppConstant.EXTRA_MSG_ID, result.getData());
					setResult(RESULT_OK, intent);
					ProgressDialogUtil.dismiss(mProgressDialog);
					finish();
				} else
					ProgressDialogUtil.dismiss(mProgressDialog);
			}
		}, String.class, params);
		addDefaultRequest(request);
	}

}

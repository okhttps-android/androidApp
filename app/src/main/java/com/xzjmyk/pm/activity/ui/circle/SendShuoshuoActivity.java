
package com.xzjmyk.pm.activity.ui.circle;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.core.utils.pictureselector.ComPictureAdapter;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.MultiImagePreviewActivity;
import com.core.widget.view.MyGridView;
import com.core.xmpp.model.Area;
import com.modular.login.activity.LoginActivity;
import com.uas.applocation.UasLocationHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.LocationMapActivity;
import com.xzjmyk.pm.activity.util.im.helper.UploadService;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 发布一条说说的Activity
 */
public class SendShuoshuoActivity extends BaseActivity implements View.OnClickListener {
	private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
	private static final int REQUEST_CODE_PICK_PHOTO = 2;// 图库
	private static final int SENFSUCCESS = 1228;
	private static Uri mNewPhotoUri;// 拍照和图库 获得图片的URI

	private EditText mTextEdit;
	private View mSelectImgLayout;
	private MyGridView mGridView;
	private ArrayList<String> mPhotoList;
	private ComPictureAdapter mAdapter;
	private String mImageData;

	//添加位置和查阅人员选择
	private TextView location_tv, display_tv;
	public static final int LOCATION = 0x00a, DISPLAY = 0x00b;

	private int mType;
	private String dailyexperience = null;
	private String baseAddr;
	private String last_address;
	private String last_location;
	private String bitmap_url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_send_shuoshuo);
		if (getIntent() != null) {
			mType = getIntent().getIntExtra("type", 0);
		}
		mPhotoList = new ArrayList<String>();
		mAdapter = new ComPictureAdapter(this);
		mAdapter.setmPhotoList(mPhotoList);
		mAdapter.setMaxSiz(9);
		mProgressDialog = ProgressDialogUtil.init(this, null, getString(R.string.please_waitting));
		initView();
	}

	private void initView() {
		location_tv = (TextView) findViewById(R.id.location_tv);
		display_tv = (TextView) findViewById(R.id.display_tv);
		findViewById(R.id.release_btn).setOnClickListener(this);
		findViewById(R.id.location_rl).setOnClickListener(this);
		findViewById(R.id.display_rl).setOnClickListener(this);


		if (mType == 0) {
			setTitle(R.string.qzone_send_word);
		} else {
			setTitle(R.string.qzone_send_picture);
		}

		mTextEdit = (EditText) findViewById(R.id.text_edit);
		mSelectImgLayout = findViewById(R.id.select_img_layout);
		mGridView = (MyGridView) findViewById(R.id.grid_view);
		mGridView.setAdapter(mAdapter);


		// 判是否有从工作日报心得传来数据
		final Intent intent = getIntent();
		dailyexperience = intent.getStringExtra("Experience");
		bitmap_url = intent.getStringExtra("bitmap_url");

		if (!TextUtils.isEmpty(dailyexperience)) {
			mTextEdit.setText(dailyexperience);
		}
		if (mType == 0) {
			mSelectImgLayout.setVisibility(View.GONE);
		}
		if (!StringUtil.isEmpty(bitmap_url) && mType == 1) {
			mPhotoList.add(bitmap_url);
			mAdapter.notifyDataSetChanged();
		}
		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				int viewType = mAdapter.getItemViewType(position);
				if (viewType == 1) {
					showSelectPictureDialog();//添加
				} else {
					showPictureActionDialog(position); //删除
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.send_qzone, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.send_qzone) {
			if (mPhotoList.size() <= 0 && TextUtils.isEmpty(mTextEdit.getText().toString())) {// 没有照片，也没有说说，直接返回
				ToastMessage(getString(R.string.qzone_send_notice1));
			} else if (mPhotoList.size() <= 0 && !TextUtils.isEmpty(mTextEdit.getText().toString())) {// 发文字
				sendShuoshuo();
			} else if (mPhotoList.size() > 0) {//  图片+文字
				if (Build.VERSION.SDK_INT >= 11)
					new UploadPhpto().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				else
					new UploadPhpto().execute();
			}
		} else {
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	private void showPictureActionDialog(final int position) {
		String[] items = new String[]{getString(R.string.look_over), getString(R.string.common_delete)};
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.pictures)
				.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {// 查看
							Intent intent = new Intent(SendShuoshuoActivity.this, MultiImagePreviewActivity.class);
							intent.putExtra(AppConstant.EXTRA_IMAGES, mPhotoList);
							intent.putExtra(AppConstant.EXTRA_POSITION, position);
							intent.putExtra(AppConstant.EXTRA_CHANGE_SELECTED, false);
							startActivity(intent);
						} else {// 删除
							deletePhoto(position);
						}
						dialog.dismiss();
					}
				});
		builder.show();
	}

	private void deletePhoto(final int position) {
		mPhotoList.remove(position);
		mAdapter.notifyDataSetInvalidated();
	}

	private void showSelectPictureDialog() {
		String[] items = new String[]{getString(R.string.c_take_picture), getString(R.string.c_photo_album)};
		AlertDialog.Builder builder = new AlertDialog.Builder(this).setSingleChoiceItems(items, 0,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							requestPermission(Manifest.permission.CAMERA, new Runnable() {
								@Override
								public void run() {
									try {
										takePhoto();
									} catch (Exception e) {
										String message = e.getMessage();
										if (!StringUtil.isEmpty(message) && message.contains("Permission")) {
											ToastUtil.showToast(ct, R.string.not_system_permission);
										}
									}
								}
							}, new Runnable() {
								@Override
								public void run() {
									ToastUtil.showToast(ct, R.string.not_system_permission);
								}
							});
						} else {
							requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new Runnable() {
								@Override
								public void run() {
									Intent intent = new Intent();
									intent.putExtra("MAX_SIZE", 9);
									intent.putExtra("CURRENT_SIZE", mPhotoList == null ? 0 : mPhotoList.size());
									intent.setClass(ct, ImgFileListActivity.class);
									startActivityForResult(intent, 0x01);
								}
							}, new Runnable() {
								@Override
								public void run() {
									ToastUtil.showToast(ct, R.string.not_system_permission);
								}
							});
						}
						dialog.dismiss();
					}
				});
		builder.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable("takePhotoUri", mNewPhotoUri);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mNewPhotoUri = savedInstanceState.getParcelable("takePhotoUri");

	}

	private void takePhoto() throws Exception {
		mNewPhotoUri = CameraUtil.getOutputMediaFileUri(mContext, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
		LogUtil.d("uri:" + mNewPhotoUri);
		if (mNewPhotoUri != null) {
			CameraUtil.captureImage(SendShuoshuoActivity.this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
		} else {
			ToastUtil.showToast(this, "uri is null");
		}
	}

	private void selectPhoto() {
		CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);
	}

	private void doImageFiltering(ArrayList<String> mPhotoList) {
		for (int i = 0; i < mPhotoList.size(); i++) {
			File file = new File(mPhotoList.get(i).toString());
			if (!file.isFile()) {
//                mPhotoList.remove(i);
				Toast.makeText(ct, "第" + (i + 1) + "张图片格式不对，可能会上传失败，建议更换", Toast.LENGTH_LONG).show();
			}
			if (i == mPhotoList.size() - 1) {
				mAdapter.notifyDataSetInvalidated();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0x01 && resultCode == 0x02 && data != null) {
			mPhotoList.addAll(data.getStringArrayListExtra("files"));
			Log.i("files0x01", data.getStringArrayListExtra("files").toString());
			Log.i("mPhotoList", mPhotoList.toString());
			doImageFiltering(mPhotoList);
//            mAdapter.notifyDataSetInvalidated();
		}
		if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
			if (resultCode == Activity.RESULT_OK) {
				if (mNewPhotoUri != null) {
					mPhotoList.add(mNewPhotoUri.getPath());
					mAdapter.notifyDataSetInvalidated();
				} else {
					ToastUtil.showToast(this, R.string.c_take_picture_failed);
				}
			}
		} else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 选择一张图片,然后立即调用裁减
			if (resultCode == Activity.RESULT_OK) {
				if (data != null && data.getData() != null) {
					LogUtil.d("uri:", JSON.toJSONString(data.getData()));
					String path = CameraUtil.getImagePathFromUri(this, data.getData());
					mPhotoList.add(path);
					mAdapter.notifyDataSetInvalidated();
				} else {
					ToastUtil.showToast(this, R.string.c_photo_album_failed);
				}
			}
		} else if (resultCode == LocationMapActivity.REQUCODE) {
			PoiInfo info = data.getParcelableExtra(LocationMapActivity.REQUESTNAME);
			if (info != null) {
//                out_add_name_tv.setText(info.name);
				location_tv.setText(UasLocationHelper.getInstance().getUASLocation().getCityName() + "•" + info.name);
				last_address = info.address;
				last_location = info.name;
			}
		} else if (resultCode == LocationMapActivity.HIDDEN_REQUCODE) {
			location_tv.setText(getString(R.string.qzone_notshow_location));
			last_address = "";
			last_location = "";
		}

	}

	/**
	 * 在原先基础上：修改，添加定位功能，样式修改，默认请求
	 */
	// 发布一条说说
	public void sendShuoshuo() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("access_token", MyApplication.getInstance().mAccessToken);

		// 消息类型：1=文字消息；2=图文消息；3=语音消息；4=视频消息；
		if (TextUtils.isEmpty(mImageData)) {
			params.put("type", "1");
		} else {
			params.put("type", "2");
		}

		// 消息标记：1：求职消息；2：招聘消息；3：普通消息；
		params.put("flag", "3");
		// 消息隐私范围 0=不可见；1=朋友可见；2=粉丝可见；3=广场
		params.put("visible", "3");

		// 判是否有从工作日报心得传来数据
		if (!TextUtils.isEmpty(dailyexperience)) {
			params.put("text", mTextEdit.getText().toString() + "\t(" + getString(R.string.qzone_ishare_too) + ")\n\n");
		} else {
			params.put("text", mTextEdit.getText().toString());// 消息内容
		}

		if (!TextUtils.isEmpty(mImageData)) {
			params.put("images", mImageData);
		}

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

		Log.i("flhname", name);
		Log.i("flhaddress", address);


		// 是否显示定位
//        if (location_tv.getText().equals("不显示位置") || location_tv.getText().equals("所在位置")){
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
			params.put("cityId", String.valueOf(area.getId()));//城市Id
		} else {
			params.put("cityId", "0");
		}
		ProgressDialogUtil.show(mProgressDialog);
		StringJsonObjectRequest<String> request = new StringJsonObjectRequest<String>(mConfig.MSG_ADD_URL,
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						ToastUtil.showErrorNet(SendShuoshuoActivity.this);
						ProgressDialogUtil.dismiss(mProgressDialog);
					}
				}, new StringJsonObjectRequest.Listener<String>() {
			@Override
			public void onResponse(ObjectResult<String> result) {
				boolean parserResult = Result.defaultParser(SendShuoshuoActivity.this, result, true);
				if (parserResult) {
					final Intent intent = new Intent();
					intent.putExtra(AppConstant.EXTRA_MSG_ID, result.getData());
					setResult(RESULT_OK, intent);

					if (!TextUtils.isEmpty(dailyexperience)) {
						new AlertDialog
								.Builder(mContext)
								.setCancelable(false)  //设置点击对话框之外的对话框不消失
								.setTitle(getString(R.string.qzone_share_success))
								.setMessage(getString(R.string.qzone_enter_qzone_now))
								.setNegativeButton(getString(R.string.qzone_edit_daily), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										finish();
									}
								})
								.setPositiveButton(getString(R.string.qzone_enter_atnow), new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Intent intent1 = new Intent(ct, BusinessCircleActivity.class);
										startActivity(intent1);
										finish();
									}
								}).show();
					} else {
						finish();
					}
				}
				ProgressDialogUtil.dismiss(mProgressDialog);
			}
		}, String.class, params);
		addDefaultRequest(request);
	}

	private ProgressDialog mProgressDialog;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.location_rl:
				String name = UasLocationHelper.getInstance().getUASLocation().getName();
				String address = UasLocationHelper.getInstance().getUASLocation().getAddress();
				baseAddr = StringUtil.isEmail(name) ? address : name;

				Intent i = new Intent(activity, LocationMapActivity.class);
				i.putExtra("qzoneaddr", baseAddr == null ? "" : baseAddr);
				i.putExtra("qzone_select_add", "qzone_select_add");
//                Log.i("baseAddr",baseAddr) ;
				startActivityForResult(i, 0x21);
				break;
			case R.id.display_rl:
//                startActivityForResult(new Intent(), LOCATION);
				ToastMessage("该功能还未完善");
				break;
			case R.id.release_btn:
				if (mPhotoList.size() <= 0 && TextUtils.isEmpty(mTextEdit.getText().toString())) {// 没有照片，也没有说说，直接返回
					return;
				}
				if (mPhotoList.size() <= 0) {// 发文字
					sendShuoshuo();
				} else {//  图片+文字
					if (Build.VERSION.SDK_INT >= 11)
						new UploadPhpto().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					else
						new UploadPhpto().execute();
				}
				break;
		}


	}

	private class UploadPhpto extends AsyncTask<Void, Integer, Integer> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			ProgressDialogUtil.show(mProgressDialog);
		}

		/**
		 * 上传的结果： <br/>
		 * return 1 Token过期，请重新登陆 <br/>
		 * return 2 上传出错<br/>
		 * return 3 上传成功<br/>
		 */
		@Override
		protected Integer doInBackground(Void... params) {
			if (!LoginHelper.isTokenValidation()) {
				return 1;
			}
			Map<String, String> mapParams = new HashMap<String, String>();
			mapParams.put("userId", MyApplication.getInstance().mLoginUser.getUserId() + "");
			mapParams.put("access_token", MyApplication.getInstance().mAccessToken);
			String result = new UploadService().uploadFile(mConfig.UPLOAD_URL, mapParams, mPhotoList);
			Log.d("roamer", "上传图片消息：" + result);
			if (TextUtils.isEmpty(result)) {
				return 2;
			}

			UploadFileResult recordResult = JSON.parseObject(result, UploadFileResult.class);
			boolean success = Result.defaultParser(SendShuoshuoActivity.this, recordResult, true);
			if (success) {
				if (recordResult.getSuccess() != recordResult.getTotal()) {// 上传丢失了某些文件
					return 2;
				}
				if (recordResult.getData() != null) {
					UploadFileResult.Data data = recordResult.getData();
					if (data.getImages() != null && data.getImages().size() > 0) {
						mImageData = JSON.toJSONString(data.getImages(), UploadFileResult.sImagesFilter);
					}

					Log.d("roamer", "mImageData:" + mImageData);
					return 3;
				} else {// 没有文件数据源，失败
					return 2;
				}
			} else {
				return 2;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == 1) {
				ProgressDialogUtil.dismiss(mProgressDialog);
				startActivity(new Intent(SendShuoshuoActivity.this, LoginActivity.class));
			} else if (result == 2) {
				ProgressDialogUtil.dismiss(mProgressDialog);
				ToastUtil.showToast(SendShuoshuoActivity.this, getString(R.string.qzone_upload_failed));
			} else {
				sendShuoshuo();
			}
		}

	}


}

package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.system.InputMethodUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ImageUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonInterface;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.widget.listener.EditChangeListener;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//外勤签到
public class OutSigninOKActivity extends BaseActivity {
	@ViewInject(R.id.time_tv)//签到时间
	private TextView time_tv;
	@ViewInject(R.id.addr_tv)//签到地址
	private TextView addr_tv;
	@ViewInject(R.id.com_tv)//签到对应公司
	private TextView com_tv;
	@ViewInject(R.id.text_num)//填写备注字数
	private TextView text_num;
	@ViewInject(R.id.text_edit)
	private EditText text_edit;
	@ViewInject(R.id.image_tag)
	private ImageView image_tag;
	@ViewInject(R.id.image)
	private ImageView image;
	private String baseUrl;
	private Uri mNewPhotoUri;
	private int mo_id = 0;
	private boolean isSubmit = false;
	private String netDate;
	private File waterBitmapToFile = null;
	private PopupWindow mPopupWindow;
	private File mCurrentFile;
	private boolean isB2b;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_out_signin_ok);
		ViewUtils.inject(this);
		isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
		Intent intent = getIntent();
		String addr = intent.getStringExtra("addr");
		String com = intent.getStringExtra("com");
		JSONArray json = intent.getParcelableExtra("list");
		initView(addr, com, json);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.push) {
			if (StringUtil.isEmpty(text_edit.getText().toString())) {
				showToast( "请填写备注");
				return true;
			}
			if (!StringUtil.isEmpty(path) && new File(path).isFile())
				uploadFile(path);
			else {
				progressDialog.show();
				doSigninn();
			}
		} else if (item.getItemId() == android.R.id.home) {
			InputMethodUtil.hideInput(ct, text_edit);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_push, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (task != null || !task.isCancelled()) {
			task.cancel(true);
		}
	}

	private void initView(String addr, String com, JSONArray json) {
		baseUrl = CommonUtil.getSharedPreferences(ct, "erp_baseurl");
		com_tv.setText(com);
		if (Build.VERSION.SDK_INT >= 11)
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		else
			task.execute();
		time_tv.setText(TimeUtils.f_long_2_str(System.currentTimeMillis()));
		addr_tv.setText(addr);
		image_tag.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				InputMethodUtil.hideInput(ct, text_edit);
				showPopup();
			}
		});
		text_edit.addTextChangedListener(new EditChangeListener() {
			@Override
			public void afterTextChanged(Editable editable) {
				int num = editable.length();
				text_num.setText(num + "/" + 60);
				if (num > 60) {
					text_edit.setText(editable.subSequence(0, 60));
				}
			}
		});
	}

	private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
	private static final int REQUEST_CODE_PICK_PHOTO = 2;// 图库
	private static final int REQUEST_CODE_CROP_PHOTO = 3;//裁剪
	private String path = null;

	private void showPopup() {
		View headSelectView = View.inflate(OutSigninOKActivity.this, R.layout.layout_select_head, null);

		mPopupWindow = new PopupWindow(headSelectView,
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setAnimationStyle(R.style.MenuAnimationFade);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		DisplayUtil.backgroundAlpha(this, 0.5f);
		mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				closePopupWindow();
			}
		});
		mPopupWindow.showAtLocation(View.inflate(this, R.layout.activity_base_info, null), Gravity.BOTTOM, 0, 0);
		TextView takePicTv = (TextView) headSelectView.findViewById(R.id.head_take_picture);
		TextView selectPicTv = (TextView) headSelectView.findViewById(R.id.head_select_photos);
		TextView cancelTv = (TextView) headSelectView.findViewById(R.id.head_cancel);

		takePicTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				requestPermission(Manifest.permission.CAMERA, new Runnable() {
					@Override
					public void run() {
						mNewPhotoUri = CameraUtil.getOutputMediaFileUri(OutSigninOKActivity.this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
						CameraUtil.captureImage(OutSigninOKActivity.this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
					}
				}, new Runnable() {
					@Override
					public void run() {
						ToastUtil.showToast(ct, R.string.not_system_permission);
					}
				});
				closePopupWindow();
			}
		});

		selectPicTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				boolean is = CommonUtil.getSharedPreferencesBoolean(ct, "isImage", false);
				if (!is) {
					showToast(  "您当前未被允许使用相册，请使用拍照");
					return;
				}
				requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE, new Runnable() {
					@Override
					public void run() {
						mNewPhotoUri = CameraUtil.getOutputMediaFileUri(OutSigninOKActivity.this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
						CameraUtil.pickImageSimple(OutSigninOKActivity.this, REQUEST_CODE_PICK_PHOTO);
					}
				}, new Runnable() {
					@Override
					public void run() {
						ToastUtil.showToast(ct, R.string.not_system_permission);
					}
				});
				closePopupWindow();
			}
		});
		cancelTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				closePopupWindow();
			}
		});

	}

	private void closePopupWindow() {
		if (mPopupWindow != null) {
			mPopupWindow.dismiss();
			mPopupWindow = null;
			DisplayUtil.backgroundAlpha(this, 1f);
		}
	}

	private int getID(String chche) {
		if (StringUtil.isEmpty(chche)) return 0;
		Pattern p = Pattern.compile("(\\d+)");
		Matcher m = p.matcher(chche);
		if (m.find()) {
			return Integer.parseInt(m.group(0));
		}
		return -1;
	}

	private int id = 0;
	private boolean isTask = false;

	private void uploadFile(String path) {
		waterBitmapToFile = new File(path);
//        if (StringUtil.isEmpty(path)) return;
//        File waterBitmapToFile = ImageUtil.compressWaterBitmapToFile(path, 100, 300, 300
//                , time_tv.getText().toString().trim(), addr_tv.getText().toString().trim(), 0);
		if (!waterBitmapToFile.isFile()) return;
		RequestParams params = new RequestParams();
		params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(ct, "erp_master"));
		params.addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		params.addBodyParameter("em_code", CommonUtil.getSharedPreferences(ct, "erp_username"));
		params.addBodyParameter("type", "common");
		params.addBodyParameter("img", waterBitmapToFile);
		String url = CommonUtil.getAppBaseUrl(ct) + "mobile/uploadEmployeeAttach.action";
		final HttpUtils http = new HttpUtils();
		http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
			@Override
			public void onStart() {
				progressDialog.show();
				ViewUtil.ToastMessage(ct, "正在上传图片...");
			}

			@Override
			public void onLoading(long total, long current, boolean isUploading) {
				if (isUploading) {
				} else {
				}
			}

			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				progressDialog.dismiss();
				ViewUtil.ToastMessage(ct, "上传成功");
				if (JSONUtil.validate(responseInfo.result) && JSON.parseObject(responseInfo.result).getBoolean("success")) {
					id = getID(JSON.parseObject(responseInfo.result).getString("id"));
				}
				if (ct != null) {
					//这个为空
					Log.i("gongengming", "为空");
					progressDialog.show();
				}
				doSigninn();
			}


			@Override
			public void onFailure(HttpException error, String msg) {
				ViewUtil.ToastMessage(ct, "上传失败：" + msg);
				progressDialog.show();
				doSigninn();
			}
		});
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
			if (resultCode == Activity.RESULT_OK) {
				if (mNewPhotoUri != null) {
					path = mNewPhotoUri.getPath();
//                    path = CameraUtil.getImagePathFromUri(ct, mNewPhotoUri);
					if (StringUtil.isEmpty(path)) return;
					progressDialog.show();
					CommonUtil.getNetTime(handler);

				} else {
					ToastUtil.showToast(this, R.string.c_take_picture_failed);
				}
			}
		}
		/*else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 选择一张图片,然后立即调用裁减
		    if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    String path = CameraUtil.getImagePathFromUri(this, data.getData());
                    Uri o = Uri.fromFile(new File(path));
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, CameraUtil.MEDIA_TYPE_IMAGE);
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    CameraUtil.cropImage(this, o, mNewPhotoUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }*/
		else if (requestCode == REQUEST_CODE_PICK_PHOTO) {
			if (resultCode == Activity.RESULT_OK) {
				if (data != null && data.getData() != null) {
//                    path = mNewPhotoUri.getPath();
					path = CameraUtil.getImagePathFromUri(ct, data.getData());
					LogUtil.i("path=" + path);
					if (StringUtil.isEmpty(path)) return;
					progressDialog.show();
					CommonUtil.getNetTime(handler);

                   /* waterBitmapToFile = ImageUtil.compressWaterBitmapToFile(path, 100, 300, 300
                            , CommonUtil.getSharedPreferences(getApplicationContext(), "erp_emname")
                            , netDate, addr_tv.getText().toString().trim(), 0);
                    if (StringUtil.isEmpty(path)) return;
                    ImageLoader.getInstance().displayImage(Uri.fromFile(waterBitmapToFile).toString(), image);
                    image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Intent intent = new Intent(OutSigninOKActivity.this, SingleImagePreviewActivity.class);
                                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, waterBitmapToFile.getCanonicalPath());
                                startActivity(intent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });*/
				} else {
					ToastUtil.showToast(this, R.string.c_photo_album_failed);
				}
			}
		}
	}

	private final int whatSignin = 0x12;
	private boolean isSigninOK = false;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			progressDialog.dismiss();
			String message = (String) msg.getData().get("result");
			switch (msg.what) {
				case whatSignin:
					isSubmit = false;
					JSONObject json = JSON.parseObject(message);
					if (json.containsKey("success") && json.getBoolean("success")) {
						isSigninOK = true;
						if (!isB2b)
							doNextNet();
						else
							signinOk();
					} else {
						ToastUtil.showToast(ct, "签到失败");
					}
					break;
				case 0x13:
					if (isSigninOK || JSON.parseObject(message).containsKey("success") && JSON.parseObject(message).getBoolean("success")) {
						signinOk();
					} else {
						ToastUtil.showToast(ct, "签到失败");
					}
					break;
				case CommonUtil.GET_NET_TIME:
					progressDialog.dismiss();
					if (msg.obj != null) {
						netDate = (String) msg.obj;
					} else {
						netDate = time_tv.getText().toString().trim();
					}
					waterBitmapToFile = ImageUtil.compressWaterBitmapToFile(path, 100, 300, 300
							, CommonUtil.getSharedPreferences(getApplicationContext(), "erp_emname")
							, netDate, addr_tv.getText().toString().trim(), 0);
					ImageLoader.getInstance().displayImage(Uri.fromFile(waterBitmapToFile).toString(), image);
					image.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							try {
								Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
								intent.putExtra(AppConstant.EXTRA_IMAGE_URI, waterBitmapToFile.getCanonicalPath());
								startActivity(intent);
							} catch (IOException e) {
								e.printStackTrace();
							}
                            /*LayoutInflater inflater = LayoutInflater.from(OutSigninOKActivity.this);
                            final View largeImageView = inflater.inflate(R.layout.layout_large_image, null);
                            PhotoView photoView = (PhotoView) largeImageView.findViewById(R.id.large_image_iv);
                            ImageLoader.getInstance().displayImage(Uri.fromFile(waterBitmapToFile).toString(), photoView);

                            PhotoViewAttacher viewAttacher = new PhotoViewAttacher(photoView, true);
                            final PopupWindow largeImageWindow = new PopupWindow(largeImageView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                            largeImageWindow.setAnimationStyle(R.style.Animation_CustomPopup);
                            largeImageWindow.setFocusable(true);
                            largeImageWindow.setOutsideTouchable(true);
                            largeImageWindow.showAtLocation(View.inflate(OutSigninOKActivity.this, R.layout.activity_out_signin_ok, null), Gravity.CENTER, 0, 0);

                            photoView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (largeImageWindow.isShowing()) {
                                        largeImageWindow.dismiss();
                                    }
                                }
                            });

                            viewAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                                @Override
                                public void onPhotoTap(View view, float x, float y) {
                                    if (largeImageWindow.isShowing()) {
                                        largeImageWindow.dismiss();
                                    }
                                }

                                @Override
                                public void onOutsidePhotoTap() {

                                }
                            });*/
						}
					});
					break;
				case Constants.APP_SOCKETIMEOUTEXCEPTION:
					showToast(  message);
					break;
			}
		}


	};


	AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
		@Override
		protected Void doInBackground(Void... voids) {
			try {
				while (true) {
					publishProgress();
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			time_tv.setText(TimeUtils.f_long_2_str(System.currentTimeMillis()));
		}
	};

	private void doSigninn() {
		Context ct = MyApplication.getInstance();
		if (!CommonUtil.isNetWorkConnected(ct)) {
			showToast(  "当前网络不可用，请检查网络连接");
			return;
		}
		if (isSubmit) {
			showToast(  "当前正在提交");
			return;
		}
		isSubmit = true;

		if (isB2b) {
			signinByB2b();
		} else
			CommonInterface.getInstance().getIdByNet("MOBILE_OUTSIGN_SEQ", new CommonInterface.OnResultListener() {
				@Override
				public void result(boolean isOk, int what, String message) {
					try {
						if (isOk && !StringUtil.isEmpty(message) && isNum(message))
							mo_id = Integer.valueOf(message);
						CommonInterface.getInstance().getCodeByNet("MOBILE_OUTSIGN", new CommonInterface.OnResultListener() {
							@Override
							public void result(boolean isOk, int what, String message) {
								doSignin(message, mo_id, id);
							}
						});
					} catch (ClassCastException e) {

					} catch (Exception e) {

					}
				}
			});

	}

	private boolean isNum(String chche) {
		Pattern pattern = Pattern.compile("[0-9]+");
		Matcher matcher = pattern.matcher(chche);
		return matcher.matches();
	}


	private void doNextNet() {
		progressDialog.show();
		//获取网络数据
		String url = baseUrl + "common/submitCommon.action";
		final Map<String, Object> param = new HashMap<>();
		param.put("caller", "Mobile_outsign");
		param.put("id", mo_id);
		param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
		LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x13, null, null, "post");
	}

	/**
	 * 签到
	 */
	private void doSignin(String code, int mo_id, int id) {
		//获取网络数据
		String url = baseUrl + "mobile/oa/saveOutSign.action";
		final Map<String, Object> param = new HashMap<>();
		Map<String, Object> formStore = new HashMap<>();
		if (mo_id > 0)
			formStore.put("mo_id", mo_id);
		formStore.put("mo_code", code);
		formStore.put("mo_address", addr_tv.getText().toString().trim());
		String emconde = CommonUtil.getSharedPreferences(this, "erp_username");
		formStore.put("mo_mancode", emconde);
		String name = CommonUtil.getSharedPreferences(ct, "erp_emname");
		if (StringUtil.isEmpty(name))
			name = MyApplication.getInstance().mLoginUser.getNickName();
		formStore.put("mo_man", name);
		formStore.put("mo_remark", text_edit.getText().toString());
		formStore.put("mo_attachid", id);
		formStore.put("mo_company", com_tv.getText().toString().trim());
		String caller = "Mobile_outsign";
		param.put("caller", caller);
		param.put("formStore", JSONUtil.map2JSON(formStore));
		param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
		LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
		headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
		ViewUtil.httpSendRequest(ct, url, param, handler, headers, whatSignin, null, null, "post");
	}


	private void signinByB2b() {
		String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().saveOutSign;
		Map<String, Object> formStore = new HashMap<>();
		formStore.put("mo_remark", text_edit.getText().toString());
		formStore.put("mo_company", com_tv.getText().toString());
		formStore.put("mo_address", addr_tv.getText().toString());
		formStore.put("mo_mancode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
		String name = MyApplication.getInstance().mLoginUser.getNickName();
		formStore.put("mo_man", name);
		Map<String, Object> param = new HashMap<>();
		param.put("formStore", JSONUtil.map2JSON(formStore));
		Request request = new Request.Bulider()
				.setParam(param)
				.setUrl(url)
				.setMode(Request.Mode.POST)
				.bulid();
		OAHttpHelper.getInstance().requestHttp(request, new OnHttpResultListener() {
			@Override
			public void result(int what, boolean isJSON, String message, Bundle bundle) {
				isSubmit = false;
				JSONObject json = JSON.parseObject(message);
				if (json.containsKey("success") && json.getBoolean("success")) {
					isSigninOK = true;
					signinOk();
				} else {
					ToastUtil.showToast(ct, R.string.signin_error);
				}
			}

			@Override
			public void error(int what, String message, Bundle bundle) {

			}
		});

	}

	private void signinOk() {
		ToastUtil.showToast(ct, "签到成功");
		Intent intent = new Intent();
		intent.putExtra("result", true);
		setResult(0x12, intent);
		finish();
	}

}

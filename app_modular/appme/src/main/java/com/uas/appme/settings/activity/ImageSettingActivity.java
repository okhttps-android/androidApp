package com.uas.appme.settings.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.PermissionUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appme.R;
import com.umeng.socialize.utils.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by FANGlh on 2017/10/10.
 * function:
 */

public class ImageSettingActivity extends SupportToolBarActivity implements View.OnClickListener{
    private ImageView mImageIm;
    private Uri mNewPhotoUri;// 拍照和图库 获得图片的URI
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;//单选照片
    private int Max_Size = 1;
    private String photoselect=null;
    private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_setting_activity);
        initView();
        initData();
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
		for (String permission : permissions) {
			if (PermissionUtil.lacksPermissions(ct, permission)) {
				PermissionUtil.requestPermission(this, PermissionUtil.DEFAULT_REQUEST, permission);
			}else
				return;
		}
    }

    private void initData() {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appCompanyAdmin")
                .add("companyid",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("userid", MyApplication.getInstance().getLoginUserId())
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("appCompanyAdmin", o.toString()+"");
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                try {
                    //{"result":"1","url":"http://113.105.74.140:8081/u/0/0/201710/o/48fda5af663f40f795f2dd49e2d8801f.jpg"}
                    if (o.toString().contains("result")) {
                            if (o.toString().contains("url")) {
                                photoselect = JSON.parseObject(o.toString()).getString("url");
                                ImageLoader.getInstance().displayImage(photoselect.toString(),mImageIm);
                            }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

    }

    private void initView() {
        mImageIm = (ImageView) findViewById(R.id.image_im);
        findViewById(R.id.image_tv).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        mProgressDialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait));
        mImageIm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.image_tv || v.getId() == R.id.image_im){
            if (PermissionUtil.lacksPermissions(ct, Manifest.permission.CAMERA)) {
                PermissionUtil.requestPermission(ImageSettingActivity.this,PermissionUtil.DEFAULT_REQUEST,Manifest.permission.CAMERA);
                ToastUtil.showToast(ct,R.string.not_camera_permission);
            } else {
                showSelectPictureDialog();//添加
            }
        }else if (v.getId() == R.id.btn_save){
            if (!CommonUtil.isNetWorkConnected(ct)){
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }else {
                doSaveImage();
            }
        }

    }
    private void doSaveImage() {
        if (StringUtil.isEmpty(photoselect)) return;
        File waterBitmapToFile=  new File(photoselect);
        if (!waterBitmapToFile.isFile()){
            return;
        }else {
            com.lidroid.xutils.http.RequestParams params = new com.lidroid.xutils.http.RequestParams();
            params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(ct, "erp_master"));
            params.addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            params.addBodyParameter("userid", MyApplication.getInstance().mLoginUser.getUserId());
            params.addBodyParameter("file1", waterBitmapToFile);
            String url = "http://113.105.74.140:8080/upload/UploadServlet";
            final HttpUtils http = new HttpUtils();
            http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
                @Override
                public void onStart() {
                    progressDialog.show();
                    showToast( getString(com.uas.appworks.R.string.sending_picture)+"...");
                }
                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    if (isUploading) {
                    } else {
                    }
                }

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    if (JSONUtil.validate(responseInfo.result) && JSON.parseObject(responseInfo.result).getBoolean("success")) {
                        LogUtil.prinlnLongMsg("UploadServlet", responseInfo.result + "");

                        try {
                            JSONObject object = JSON.parseObject(responseInfo.result);
                            JSONObject dataobject = object.getJSONObject("data");
                            if (dataobject == null) return;
                            JSONArray imagearray = dataobject.getJSONArray("images");
                            if (ListUtils.isEmpty(imagearray)) return;
                            String oUrl = imagearray.getJSONObject(0).getString("oUrl");
                            if (!StringUtil.isEmpty(oUrl))
                                doUpdateUrl(oUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    showToast( getString(com.uas.appworks.R.string.common_save_failed) + msg);
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void doUpdateUrl(String oUrl) {
        Map<String, Object> params = new HashMap<>();
        params.put("sc_imageurl",oUrl);
        params.put("sc_uu",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.getHeaders().remove("Content-Type");
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStorurl")
                .add("map",JSONUtil.map2JSON(params))
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("hi/appStorurl", o.toString()+"ddd");
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBoolean("result")){
                    Toast.makeText(ct,getString(R.string.common_save_success),Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    finish();
                }
        }
    }));
	}


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0x01 && resultCode == 0x02 && data != null){  //新的多选方式
            photoselect = data.getStringArrayListExtra("files").get(0);
            mImageIm.setImageURI(Uri.fromFile(new File(photoselect)));
        }else if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    photoselect = mNewPhotoUri.getPath();
//                    mImageIm.setImageURI(mNewPhotoUri);
                    ImageLoader.getInstance().displayImage(mNewPhotoUri.toString(),mImageIm);
                    Log.i("files0x01",photoselect);
                } else {
                    ToastUtil.showToast(this, com.uas.appworks.R.string.c_take_picture_failed);
                }
            }
        }else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 传统单选方式，选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK){
                if (data != null && data.getData() != null){
                    photoselect= CameraUtil.getImagePathFromUri(this, data.getData());
                    mImageIm.setImageURI(Uri.fromFile(new File(photoselect)));
//                    ImageLoader.getInstance().displayImage(photoselect,mImageIm);
                    Log.i("files0x01",photoselect);
                }
            }
        }
    }

    private void showSelectPictureDialog() {
        String[] items = new String[]{getString(com.uas.appworks.R.string.c_take_picture), getString(com.uas.appworks.R.string.c_photo_album)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            try {
                                takePhoto();
                            } catch (Exception e) {
                                String message=e.getMessage();
                                if (!StringUtil.isEmpty(message)&&message.contains("Permission")){
                                    ToastUtil.showToast(ct,R.string.not_system_permission);
                                }
                            }
                        } else {
                            selectPhoto();
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void takePhoto() {
        try {
            mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().getLoginUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
            CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectPhoto() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);  //传统单选方式
        //新式多选方式
//        Intent intent = new Intent();
//        intent.putExtra("MAX_SIZE",Max_Size);
//        intent.putExtra("CURRENT_SIZE",0);
//        intent.setClass(ct,ImgFileListActivity.class);
//        startActivityForResult(intent,0x01);
    }

}

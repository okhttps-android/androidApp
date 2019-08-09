package com.uas.appme.settings.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.PermissionUtil;
import com.common.system.SystemUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ImageUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.UploadFileResult;
import com.core.net.http.ViewUtil;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.service.UploadService;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.LoginHelper;
import com.core.widget.SquareCenterImageView;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.MultiImagePreviewActivity;
import com.core.widget.view.MyGridView;
import com.core.xmpp.model.Area;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.modular.login.activity.LoginActivity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.applocation.UasLocationHelper;
import com.uas.appme.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @desc:用户反馈界面
 * @author：Arison on 2016/9/27
 * update : FANGlh on 2017-5-23 关于问题反馈上传文字新增字段，发送图片及更新更新附件ID接口
 */
public class FeedbackActivity extends SupportToolBarActivity implements View.OnClickListener {
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;// 图库
    private Uri mNewPhotoUri;// 拍照和图库 获得图片的URI

    private EditText mTextEdit;
    private TextView mSelectImagePromptTv;
    private View mSelectImgLayout;
    private MyGridView mGridView;
    private ArrayList<String> mPhotoList;
    private GridViewAdapter mAdapter;
    private String mImageData;
    private String cb_emcode;

    //添加位置和查阅人员选择
    private TextView display_tv;
    public static final int LOCATION = 0x00a, DISPLAY = 0x00b;

    private int mType;
    private final int http_commit = 0;
    private final int send_picture_request = 3;
    private int sended_p = 0;
    private final int common_id_request = 4;
    private final int update_ID = 5;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case http_commit:
                    if (msg.getData() != null) {
                        String word_result = msg.getData().getString("result");
                        Log.i(word_result, word_result);
                        //{"cb_id":14590,"success":true,"cb_code":"2017060066"}
                        try {
                            if (JSON.parseObject(word_result).containsKey("success")
                                    && JSON.parseObject(word_result).getBoolean("success")) {
                                cb_id = JSON.parseObject(word_result).getInteger("cb_id");
                                cb_code = JSON.parseObject(word_result).getString("cb_code");
                                Log.i("cb_id,cb_code", +cb_id + "," + cb_code);
                                Toast.makeText(ct, getString(R.string.fangkui_success), Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case Constants.HTTP_SUCCESS_INIT:
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    cb_code = JSON.parseObject(msg.getData().getString("result")).getString("code");
                    progressDialog.dismiss();
                    break;
                case common_id_request:
                    if (msg.getData() != null) {
                        String resultStr = msg.getData().getString("result");
                        JSONObject resultJsonObject = JSON.parseObject(resultStr);
                        cb_id = resultJsonObject.getInteger("id");
                        Log.i("resultStr", resultStr + "");
                    }
                    break;

                case update_ID:
                    if (msg.getData() != null) {
                        String updateID_result = msg.getData().getString("result");
                        Log.i("updateID_result", updateID_result);
                        progressDialog.dismiss();
                        release_btn.setEnabled(true);
                        Toast.makeText(ct, getString(R.string.fangkui_success), Toast.LENGTH_LONG).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 1000);
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    ToastMessage(msg.getData().getString("result"));
                    progressDialog.dismiss();
                    release_btn.setEnabled(true);
                    break;
            }
        }
    };
    private Button release_btn;

    private void doUpdateId(String update) {
        String url = "http://218.18.115.198:8888/ERP/common/attach/change.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", "Commentsback_mobile");
        params.put("table", "Commentsback_mobile");
        params.put("update", "cb_attch = '" + update + "'");
        params.put("condition", "cb_id = '" + cb_id + "'");
        params.put("type", "添加附件");
        params.put("master", "USOFTSYS");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        if (platform) {
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        } else {
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        }
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, update_ID, null, null, "post");
    }


    private Boolean platform;
    private int cb_id;
    private String update = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (getIntent() != null) {
            mType = getIntent().getIntExtra("type", 0);
        }
        mPhotoList = new ArrayList<String>();
        mAdapter = new GridViewAdapter();
        mProgressDialog = ProgressDialogUtil.init(this, null, getString(R.string.please_waitting));
        initView();
    }

    private void initView() {
        display_tv = (TextView) findViewById(R.id.display_tv);
        release_btn = (Button) findViewById(R.id.release_btn);
        findViewById(R.id.release_btn).setOnClickListener(this);
        //findViewById(R.id.location_rl).setOnClickListener(this);
        findViewById(R.id.display_rl).setOnClickListener(this);

        if (mType == 0) {
          setTitle(getString(R.string.send_words));
        } else {
            setTitle(getString(R.string.Rated_suggest));
        }
        mTextEdit = (EditText) findViewById(R.id.text_edit);
        mSelectImagePromptTv = (TextView) findViewById(R.id.select_img_prompt_tv);
        mSelectImgLayout = findViewById(R.id.select_img_layout);
        mGridView = (MyGridView) findViewById(R.id.grid_view);

//       ToastUtil.addEditTextNumChanged(ct, mTextEdit, 200);//这里复制粘贴过多字数会在有些机型上出现bug
        mGridView.setAdapter(mAdapter);

        if (mType == 0) {
            mSelectImagePromptTv.setVisibility(View.GONE);
            mSelectImgLayout.setVisibility(View.GONE);
        }

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewType = mAdapter.getItemViewType(position);

                if (viewType == 1) {
                    if (PermissionUtil.lacksPermissions(ct, Manifest.permission.CAMERA)) {
//                        ToastUtil.showToast(ct, com.uas.appworks.R.string.not_camera_permission);
                        PermissionUtil.requestPermission(FeedbackActivity.this, PermissionUtil.DEFAULT_REQUEST, Manifest.permission.CAMERA);
                    } else {
                        showSelectPictureDialog();//添加
                    }
                    //TODO 选择图片页面
//                    startActivityForResult(new Intent(ct, PhoneSelectActivity.class), 222);

                } else {
                    showPictureActionDialog(position);
                }
            }
        });

        findViewById(R.id.feedback_voice_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CommonUtil.getVoiceText(FeedbackActivity.this, mTextEdit, null);
            }
        });

        if (!platform) {
//            getCode();
        }

        getCommonId();
    }

    private void getCommonId() {
//        CommonUtil.getCommonId("http://218.18.115.198:8888/ERP/",ct, "Commentsback_mobile_SEQ", mHandler, common_id_request);
        cb_emcode = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
    }

    private void getCode() {
        String url = CommonUtil.getAppBaseUrl(ct) + "common/getCodeString.action";
//        String url = "http://218.18.115.198:8888/ERP/common/getCodeString.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", "Commentsback_mobile");
        params.put("type", 2);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
    }

    private void getPlatCode() {
//        String url = CommonUtil.getAppBaseUrl(ct) + "common/getCodeString.action";
        String url = " http://218.18.115.198:8888/ERP/common/getCodeString.action";
        Map<String, Object> params = new HashMap<>();
//        params.put("emuu", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu"));
//        params.put("enuu", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getEnuu());
        params.put("caller", "Commentsback_mobile");
        params.put("type", 2);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
    }

    private void showPictureActionDialog(final int position) {
        String[] items = new String[]{getString(R.string.look_over), getString(R.string.common_delete)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.pictures)
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {// 查看
                            Intent intent = new Intent(ct, MultiImagePreviewActivity.class);
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
                            try {
                                takePhoto();
                            } catch (Exception e) {
                                String message = e.getMessage();
                                if (!StringUtil.isEmpty(message) && message.contains("Permission")) {
                                    ToastUtil.showToast(ct, com.uas.appworks.R.string.not_system_permission);
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
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
    }

    private int Max_Size = 3;

    private void selectPhoto() {
//        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_PHOTO);
        Intent intent = new Intent();
        intent.putExtra("MAX_SIZE", Max_Size);
        intent.putExtra("CURRENT_SIZE", mPhotoList == null ? 0 : mPhotoList.size());
        intent.setClass(ct, ImgFileListActivity.class);
        startActivityForResult(intent, 0x01);
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
                    doImageFiltering(mPhotoList);
                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    String path = CameraUtil.getImagePathFromUri(this, data.getData());
                    mPhotoList.add(path);
                    mAdapter.notifyDataSetInvalidated();
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }

    }

    /**
     * 某些图片路径获取失败处理方案
     *
     * @param mPhotoList
     */
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
        params.put("text", mTextEdit.getText().toString());// 消息内容
        if (!TextUtils.isEmpty(mImageData)) {
            params.put("images", mImageData);
        }

        // 附加信息
        params.put("model", SystemUtil.getModel());
        params.put("osVersion", SystemUtil.getOsVersion());
        params.put("serialNumber", SystemUtil.getDeviceId(mContext));

        double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();

        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        String address = UasLocationHelper.getInstance().getUASLocation().getAddress();

        if (!TextUtils.isEmpty(address))
            params.put("location", address);

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
                        ToastUtil.showErrorNet(ct);
                        ProgressDialogUtil.dismiss(mProgressDialog);
                    }
                }, new StringJsonObjectRequest.Listener<String>() {
            @Override
            public void onResponse(ObjectResult<String> result) {
                boolean parserResult = Result.defaultParser(ct, result, true);
                if (parserResult) {
                    Intent intent = new Intent();
                    intent.putExtra(AppConstant.EXTRA_MSG_ID, result.getData());
                    setResult(RESULT_OK, intent);
                    finish();
                }
                ProgressDialogUtil.dismiss(mProgressDialog);
            }
        }, String.class, params);
        addDefaultRequest(request);
    }

    private ProgressDialog mProgressDialog;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.release_btn) {
            if (ListUtils.isEmpty(mPhotoList)) {
                ToastMessage(getString(R.string.please_add_image));
            } else {
                newSendWord();
            }
            LogUtil.prinlnLongMsg("mPhotoList", JSON.toJSONString(mPhotoList) + "hi");
        }
    }

    private void sendWord() {
        if (!StringUtil.isEmpty(mTextEdit.getText().toString())) {
            String url = "http://192.168.253.252:8080/ERP/mobile/Commentsback_mobile.action";
            String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
            if (StringUtil.isEmpty(emname)) {
                emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
            }
            String formStore = "{\n" +
                    " \"cb_emcode\":\"" + cb_emcode + "\",\n" +
                    " \"cb_company\":\"" + CommonUtil.getSharedPreferences(ct, "erp_commpany") + "\",\n" +
                    " \"cb_whichsys\":\"" + CommonUtil.getSharedPreferences(ct, "erp_master") + "\",\n" +
                    " \"cb_kind\":\"问题\",\n" +
                    " \"cb_text\":\"" + mTextEdit.getText().toString() + "（来自ANDROID）" + "\",\n" +
                    " \"cb_date\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss") + "\",\n" +
                    " \"cb_person\":\"" + emname + "\",\n" +
                    " \"cb_tel\":\"" + MyApplication.getInstance().mLoginUser.getTelephone() + "\",\n" +
                    "}\n";
            Map<String, Object> params = new HashMap<>();
            params.put("formStore", formStore);
            params.put("master", "USOFTSYS");
            params.put("caller", "Commentsback_mobile");
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, http_commit, null, null, "post");
            release_btn.setEnabled(false);
        } else {
            ToastMessage(getString(R.string.suggest_hink));
        }
    }

    private void sendPlatWord() {
        if (!StringUtil.isEmpty(mTextEdit.getText().toString())) {
            String url = "http://218.18.115.198:8888/ERP/mobile/Commentsback_mobile.action";
            String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
            if (StringUtil.isEmpty(emname)) {
                emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
            }
            String formStore = "{\n" +
                    " \"cb_emcode\":\"" + cb_emcode + "\",\n" +
                    " \"cb_company\":\"" + CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyName") + "\",\n" +
                    " \"cb_whichsys\":\"" + "USOFTSYS" + "\",\n" +
                    " \"cb_kind\":\"问题\",\n" +
                    " \"cb_text\":\"" + mTextEdit.getText().toString() + "（来自ANDROID）" + "\",\n" +
                    " \"cb_date\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss") + "\",\n" +
                    " \"cb_person\":\"" + emname + "\",\n" +
                    " \"cb_tel\":\"" + MyApplication.getInstance().mLoginUser.getTelephone() + "\",\n" +
                    "}\n";
            Map<String, Object> params = new HashMap<>();
            params.put("formStore", formStore);
            params.put("master", "USOFTSYS");
            params.put("caller", "Commentsback_mobile");
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            ViewUtil.httpSendRequest(mContext, url, params, mHandler, headers, http_commit, null, null, "post");
        } else {
            ToastMessage(getString(R.string.suggest_hink));
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
            boolean success = Result.defaultParser(ct, recordResult, true);
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
                startActivity(new Intent(ct, LoginActivity.class));
            } else if (result == 2) {
                ProgressDialogUtil.dismiss(mProgressDialog);
                ToastUtil.showToast(ct, getString(R.string.qzone_upload_failed));
            } else {
                sendShuoshuo();
            }
        }

    }

    private class GridViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mPhotoList.size() >= 3) {
                return 3;
            }
            return mPhotoList.size() + 1;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (mPhotoList.size() == 0) {
                return 1;// View Type 1代表添加更多的视图
            } else if (mPhotoList.size() < 9) {
                if (position < mPhotoList.size()) {
                    return 0;// View Type 0代表普通的ImageView视图
                } else {
                    return 1;
                }
            } else {
                return 0;
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (getItemViewType(position) == 0) {// 普通的视图
                SquareCenterImageView imageView = new SquareCenterImageView(ct);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                String url = mPhotoList.get(position);
                if (url == null) {
                    url = "";
                }
                ImageLoader.getInstance().displayImage(Uri.fromFile(new File(url)).toString(), imageView);
                return imageView;
            } else {
                View view = LayoutInflater.from(ct).inflate(R.layout.layout_circle_add_more_item,
                        parent, false);
                ImageView iconImageView = (ImageView) view.findViewById(R.id.icon_image_view);
                TextView voiceTextTv = (TextView) view.findViewById(R.id.text_tv);
                iconImageView.setBackgroundResource(R.drawable.add_picture);
                voiceTextTv.setText(R.string.qzone_add_picture);
                return view;
            }
        }

    }


    String cb_code;


    private void sendPicture() {
        if (ListUtils.isEmpty(mPhotoList)) {
            Toast.makeText(ct, getString(R.string.fangkui_success), Toast.LENGTH_LONG).show();
            finish();
        }
        for (int i = 0; i < mPhotoList.size(); i++) {
            String path = mPhotoList.get(i);
            sendPictureRequest(path);
        }
    }

    File[] files = null;

    private void newSendWord() {
        if (!StringUtil.isEmpty(mTextEdit.getText().toString())) {
            if (!ListUtils.isEmpty(mPhotoList)) {
                files = new File[mPhotoList.size()];
                for (int i = 0; i < mPhotoList.size(); i++) {
                    if (new File(mPhotoList.get(i)).isFile()) {
                        files[i] = ImageUtil.compressBitmapToFile(mPhotoList.get(i), 100, 360, 480);// TODO 压缩
//                                files[i] = new File(mPhotoList.get(i));
                    }
                }
            }

//            String url = "http://192.168.253.252:8080/ERP/mobile/Commentsback_mobile.action";
            String url = "http://218.18.115.198:8888/ERP/mobile/Commentsback_mobile.action"; // 正式账号
            String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
            if (StringUtil.isEmpty(emname)) {
                emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
            }
            String formStore = "{\n" +
                    " \"cb_emcode\":\"" + "U0305" + "\",\n" +
//                    " \"em_code\":\"" + CommonUtil.getSharedPreferences(ct, "erp_username") + "\",\n" +
                    " \"em_code\":\"" + "U0305" + "\",\n" +
                    " \"cb_company\":\"" + CommonUtil.getSharedPreferences(ct, "erp_commpany") + "\",\n" +
                    " \"cb_whichsys\":\"" + CommonUtil.getSharedPreferences(ct, "erp_master") + "\",\n" +
                    " \"cb_kind\":\"问题反馈\",\n" +
                    " \"cb_text\":\"" + mTextEdit.getText().toString() + "（来自ANDROID）" + "\",\n" +
                    " \"cb_date\":\"" + DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss") + "\",\n" +
                    " \"cb_person\":\"" + emname + "\",\n" +
                    " \"cb_tel\":\"" + MyApplication.getInstance().mLoginUser.getTelephone() + "\",\n" +
                    "}\n";
            RequestParams params = new RequestParams();
            params.addQueryStringParameter("master", "USOFTSYS");
            if (!platform) {
                params.addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            } else {
                params.addHeader("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            }

            params.addBodyParameter("formStore", formStore);
            params.addBodyParameter("type", "common");
            params.addBodyParameter("caller", "Commentsback_mobile");
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i] != null) {
                        params.addBodyParameter("img" + (i + 1), files[i]);
                    }
                }
            }
            final HttpUtils http = new HttpUtils();
            Log.i("urlparams", url + params + "");
            http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
                @Override
                public void onStart() {
                    progressDialog.show();
                    release_btn.setEnabled(false);
                    ViewUtil.ToastMessage(ct, getString(R.string.sending_picture) + "...");
                }

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    ViewUtil.ToastMessage(ct, getString(R.string.Uploaded_successfully));
                    release_btn.setEnabled(true);
                    Log.i("newSendMes", JSON.parseObject(responseInfo.result).toJSONString());
                    if (JSONUtil.validate(responseInfo.result) && JSON.parseObject(responseInfo.result).getBoolean("success")) {

                    }
                    progressDialog.dismiss();
                    release_btn.setEnabled(true);
                    Toast.makeText(ct, getString(R.string.fangkui_success), Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    ViewUtil.ToastMessage(ct, getString(R.string.common_save_failed) + msg);
                    release_btn.setEnabled(true);
                    Log.i("newSendMes", error + "," + msg);
                    progressDialog.dismiss();
                }
            });
        } else {
            ToastMessage(getString(R.string.suggest_hink));
        }
    }

    private void sendPictureRequest(String path) {
        if (StringUtil.isEmpty(path)) return;
        if (!new File(path).isFile()) return;
        File file = ImageUtil.compressBitmapToFile(path, 100, 360, 480);
        RequestParams params = new RequestParams();
        if (platform) {
            params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
            params.addHeader("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            params.addBodyParameter("em_code", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        } else {
            params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(ct, "erp_master"));
            params.addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            params.addBodyParameter("em_code", CommonUtil.getSharedPreferences(ct, "erp_username"));
        }
        params.addBodyParameter("type", "common");
        params.addBodyParameter("img", file == null ? new File(path) : file);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/uploadEmployeeAttach.action";
        Log.i("urlparams", url);
        final HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                progressDialog.show();
                ViewUtil.ToastMessage(ct, getString(R.string.sending_picture) + "...");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                if (isUploading) {
                    if (sended_p == mPhotoList.size()) {
                        release_btn.setEnabled(true);
                        Toast.makeText(ct, getString(R.string.fangkui_success), Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    release_btn.setEnabled(true);
                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                ViewUtil.ToastMessage(ct, getString(R.string.Uploaded_successfully));
                if (JSONUtil.validate(responseInfo.result) && JSON.parseObject(responseInfo.result).getBoolean("success")) {
                    sended_p++;
                    update = update + getID(JSON.parseObject(responseInfo.result).getString("id")) + ";";
                    Log.i("update_cb_attch", update + "");
                    if (sended_p == mPhotoList.size()) {
                        //TODO 更新附件ID接口
                        doUpdateId(update);
                    }
                }
            }


            @Override
            public void onFailure(HttpException error, String msg) {
                ViewUtil.ToastMessage(ct, getString(R.string.common_save_failed) + msg);
                progressDialog.dismiss();
            }
        });
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
}

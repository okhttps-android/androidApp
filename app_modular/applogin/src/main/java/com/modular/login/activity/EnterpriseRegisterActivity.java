package com.modular.login.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.system.PermissionUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ImageUtil;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.core.widget.ClearEditText;
import com.core.widget.view.Activity.SearchLocationActivity;
import com.core.widget.view.model.SearchPoiParam;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.login.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by RaoMeng on 2017/9/21.
 * 企业信息注册页面
 */

public class EnterpriseRegisterActivity extends BaseActivity implements View.OnClickListener {
    private final int ENTERPRISE_REGISTER = 0x11;
    private final int INDUSTRY_CHOOSE = 0x12;
    private final int SELECT_ADDRESS_REQUEST = 0x13;
    private final int SELECT_ADDRESS_RESULT = 0x14;

    private Button mNextStepButton;
    private ClearEditText mEnterpriseCompanyEt;
    private ClearEditText mEnterpriseLicenseEt;
    private ClearEditText mEnterpriseRepresentEt;
    private TextView mEnterpriseAddressEt;
    private TextView mEnterpriseIndustryEt;
    private ClearEditText mEnterpriseBusinessEt;
    private ImageView mEnterpriseLicenseIv;
    private ImageView mTakePicImageView;
    private TextView mEnterpriseClauseTv;
    private CheckBox mClauseCheckBox;
    private TextView mCompanyErrorTextView, mLicenseErrorTextView, mRepresentErrorTextView, mAddressErrorTextView, mIndustryErrorTextView;

    private PopupWindow mPopupWindow;
    private Uri mNewPhotoUri;
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;// 图库
    private static final int REQUEST_CODE_CROP_PHOTO = 3;//裁剪
    private String path = null;
    private File mLicenseFile;
    private boolean isCompanyAdopt = false, isLicenseAdopt = false, isRepresentAdopt = false, isAddressAdopt = false, isIndustryAdopt = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String result = msg.getData().getString("result");
            Log.d("enterpriseresponse", result);
            switch (msg.what) {
                case ENTERPRISE_REGISTER:
                    progressDialog.dismiss();
                    if (result != null) {
                        try {
                            JSONObject resultObject = new JSONObject(result);
                            if (resultObject.optBoolean("success")) {
                                JSONObject contentObject = resultObject.optJSONObject("content");
                                if (contentObject != null) {
                                    String pageToken = contentObject.optString("pageToken");
                                    CommonUtil.setSharedPreferences(ct, "pageToken", pageToken);
                                }
                                Intent intent = new Intent();
                                intent.setClass(EnterpriseRegisterActivity.this, AdminRegisterActivity.class);
                                intent.putExtra("companyName", mEnterpriseCompanyEt.getText().toString());
                                intent.putExtra("industry", mEnterpriseIndustryEt.getText().toString());
                                intent.putExtra("address", mEnterpriseAddressEt.getText().toString());
                                intent.putExtra("latitude", mLatitude + "");
                                intent.putExtra("longitude", mLongitude + "");

                                startActivity(intent);
                            } else {
                                String errMsg = resultObject.optString("errMsg");
                                ToastUtil.showToast(EnterpriseRegisterActivity.this, errMsg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    if (result != null)
                        ToastMessage(result);
                    break;
            }
        }
    };
    private HttpClient mHttpClient;
    private double mLatitude;
    private double mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enterprise_register);
        setTitle("企业注册(1/2)");

        initViews();
//        initProfession();

        initEvents();
    }

    private void initEvents() {
        mNextStepButton.setOnClickListener(this);
        mEnterpriseLicenseIv.setOnClickListener(this);
        mTakePicImageView.setOnClickListener(this);

        mClauseCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isRegButtonEnable(isChecked);
            }
        });
        mEnterpriseIndustryEt.setOnClickListener(this);
        mEnterpriseAddressEt.setOnClickListener(this);
        mEnterpriseCompanyEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                companyAdopt(mEnterpriseCompanyEt.getText().toString());
            }
        });

        mEnterpriseLicenseEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                licenseAdopt(mEnterpriseLicenseEt.getText().toString());
            }
        });

        mEnterpriseRepresentEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                representAdopt(mEnterpriseRepresentEt.getText().toString());
            }
        });

        mEnterpriseAddressEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                addressAdopt(mEnterpriseAddressEt.getText().toString());
            }
        });

        mEnterpriseIndustryEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                industryAdopt(mEnterpriseIndustryEt.getText().toString());
            }
        });

        mEnterpriseCompanyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                companyAdopt(text);
            }
        });

        mEnterpriseLicenseEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                licenseAdopt(text);
            }
        });

        mEnterpriseRepresentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                representAdopt(text);
            }
        });

        mEnterpriseAddressEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                addressAdopt(text);
            }
        });

        mEnterpriseIndustryEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                industryAdopt(text);
            }
        });
    }

    private void industryAdopt(String text) {
        if (TextUtils.isEmpty(text)) {
            mIndustryErrorTextView.setVisibility(View.VISIBLE);
            isIndustryAdopt = false;
        } else {
            mIndustryErrorTextView.setVisibility(View.INVISIBLE);
            isIndustryAdopt = true;
        }
        isRegButtonEnable(mClauseCheckBox.isChecked());
    }

    private void addressAdopt(String text) {
        if (TextUtils.isEmpty(text)) {
            mAddressErrorTextView.setVisibility(View.VISIBLE);
            isAddressAdopt = false;
        } else {
            mAddressErrorTextView.setVisibility(View.INVISIBLE);
            isAddressAdopt = true;
        }
        isRegButtonEnable(mClauseCheckBox.isChecked());
    }

    private void representAdopt(String text) {
        if (TextUtils.isEmpty(text)) {
            mRepresentErrorTextView.setVisibility(View.VISIBLE);
            isRepresentAdopt = false;
        } else {
            mRepresentErrorTextView.setVisibility(View.INVISIBLE);
            isRepresentAdopt = true;
        }
        isRegButtonEnable(mClauseCheckBox.isChecked());
    }

    private void licenseAdopt(String text) {
        if (TextUtils.isEmpty(text)) {
            mLicenseErrorTextView.setVisibility(View.VISIBLE);
            isLicenseAdopt = false;
        } else {
            mLicenseErrorTextView.setVisibility(View.INVISIBLE);
            isLicenseAdopt = true;
        }
        isRegButtonEnable(mClauseCheckBox.isChecked());
    }

    private void companyAdopt(String text) {
        if (TextUtils.isEmpty(text) || text.length() < 2 || text.length() > 99) {
            mCompanyErrorTextView.setVisibility(View.VISIBLE);
            isCompanyAdopt = false;
        } else {
            mCompanyErrorTextView.setVisibility(View.INVISIBLE);
            isCompanyAdopt = true;
        }
        isRegButtonEnable(mClauseCheckBox.isChecked());
    }

    private void isRegButtonEnable(boolean isChecked) {
        if (isChecked && isCompanyAdopt && isLicenseAdopt
                && isRepresentAdopt && isAddressAdopt && isIndustryAdopt && mLicenseFile != null) {
            mNextStepButton.setEnabled(true);
        } else {
            mNextStepButton.setEnabled(false);
        }
    }

    private void initViews() {

        mNextStepButton = (Button) findViewById(R.id.enterprise_register_next_step_btn);
        mEnterpriseCompanyEt = (ClearEditText) findViewById(R.id.enterprise_register_company_et);
        mEnterpriseLicenseEt = (ClearEditText) findViewById(R.id.enterprise_register_license_et);
        mEnterpriseRepresentEt = (ClearEditText) findViewById(R.id.enterprise_register_represent_et);
        mEnterpriseAddressEt = (TextView) findViewById(R.id.enterprise_register_address_et);
        mEnterpriseIndustryEt = (TextView) findViewById(R.id.enterprise_register_industry_et);
        mEnterpriseBusinessEt = (ClearEditText) findViewById(R.id.enterprise_register_business_et);
        mEnterpriseLicenseIv = (ImageView) findViewById(R.id.enterprise_register_license_iv);
        mTakePicImageView = (ImageView) findViewById(R.id.enterprise_register_picture_iv);
        mEnterpriseClauseTv = (TextView) findViewById(R.id.enterprise_register_clause_tv);
        mCompanyErrorTextView = (TextView) findViewById(R.id.enterprise_register_company_error_tv);
        mLicenseErrorTextView = (TextView) findViewById(R.id.enterprise_register_license_error_tv);
        mRepresentErrorTextView = (TextView) findViewById(R.id.enterprise_register_represent_error_tv);
        mAddressErrorTextView = (TextView) findViewById(R.id.enterprise_register_address_error_tv);
        mIndustryErrorTextView = (TextView) findViewById(R.id.enterprise_register_industry_error_tv);

        mEnterpriseClauseTv.setText(getClickableSpan());
        mEnterpriseClauseTv.setMovementMethod(LinkMovementMethod.getInstance());

        mEnterpriseAddressEt.setKeyListener(null);
        mClauseCheckBox = (CheckBox) findViewById(R.id.enterprise_register_clause_cb);
        mHttpClient = new
                HttpClient.Builder("https://account.ubtob.com")
//                HttpClient.Builder("http://113.105.74.135:8092")
//                HttpClient.Builder("http://192.168.253.66:8082")
//                HttpClient.Builder("http://192.168.253.200:8080")
                .build();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.enterprise_register_next_step_btn) {
            if (mClauseCheckBox.isChecked()) {
                if (mLicenseFile == null) {
                    ToastUtil.showToast(this, "请选择您的营业执照");
                } else {
                    if (CommonUtil.isNetWorkConnected(this)) {
                        progressDialog.show();
                        obtainPageToken();
                    } else {
                        ToastUtil.showToast(this, R.string.networks_out);
                    }
                }

            } else {
                ToastUtil.showToast(this, "请阅读并同意必读协议");
            }
        } else if (i == R.id.enterprise_register_license_iv) {
            if (mLicenseFile != null) {
                try {
                    Intent intent = new Intent();
                    intent.setAction("com.modular.tool.SingleImagePreviewActivity");
                    intent.putExtra(AppConstant.EXTRA_IMAGE_URI, mLicenseFile.getCanonicalPath());
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (i == R.id.enterprise_register_picture_iv) {
            CommonUtil.closeKeybord(mEnterpriseCompanyEt, this);
            showPicturePopup();
        } else if (i == R.id.enterprise_register_industry_et) {
            Intent intent = new Intent();
            intent.setClass(this, IndustryChooseActivity.class);
            startActivityForResult(intent, INDUSTRY_CHOOSE);
        } else if (i == R.id.enterprise_register_address_et) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            if (PermissionUtil.lacksPermissions(ct, permissions)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, 222);
                }
            } else {
                Intent intent = new Intent(ct, SearchLocationActivity.class);
                SearchPoiParam poiParam = new SearchPoiParam();
                poiParam.setType(2);
                poiParam.setTitle("地图搜索");
                poiParam.setRadius(1000);
                //poiParam.setContrastLatLng(new LatLng(companyLocation.getLocation().mLongitude, companyLocation.getLocation().mLatitude));
                poiParam.setResultCode(SELECT_ADDRESS_RESULT);
                poiParam.setDistanceTag(MyApplication.getInstance().getResources().getString(R.string.rice));
                intent.putExtra("data", poiParam);
                startActivityForResult(intent, SELECT_ADDRESS_REQUEST);
            }
        }
    }

    private void obtainPageToken() {
        mHttpClient.Api().send(new HttpClient.Builder()
                .url("/sso/mobile/userspace/register")
//                .header("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"))
                .method(Method.GET).build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object s) {
                if (s != null) {
                    try {
                        Log.d("pagetokens", s.toString());
                        JSONObject resultObject = new JSONObject(s.toString());
                        if (resultObject.optBoolean("success")) {
                            JSONObject contentObject = resultObject.optJSONObject("content");
                            if (contentObject != null) {
                                String pageToken = contentObject.optString("pageToken");
                                String sessionId = contentObject.optString("sessionId");
                                CommonUtil.setSharedPreferences(ct, "sessionId", sessionId);
                                CommonUtil.setSharedPreferences(ct, "pageToken", pageToken);

                                enterpriseRegister(pageToken, sessionId);
                            } else {
                                progressDialog.dismiss();
                                ToastUtil.showToast(EnterpriseRegisterActivity.this
                                        , "注册失败，请重试");
                            }
                        } else {
                            progressDialog.dismiss();
                            String errMsg = resultObject.optString("errMsg");
                            ToastUtil.showToast(EnterpriseRegisterActivity.this
                                    , TextUtils.isEmpty(errMsg) ? "注册失败，请重试" : errMsg);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                        ToastUtil.showToast(EnterpriseRegisterActivity.this
                                , "注册失败，请重试");
                    }
                }
            }
        }));

    }

    private void enterpriseRegister(String pageToken, String sessionId) {

        String url = "https://account.ubtob.com/sso/mobile/userspace/register";
//        String url = "http://192.168.253.66:8082/sso/mobile/userspace/register";

        String fileBytes = getFileBytes(mLicenseFile);
        Map<String, Object> params = new HashMap<>();
        params.put("name", mEnterpriseCompanyEt.getText().toString());
        params.put("businessCode", mEnterpriseLicenseEt.getText().toString());
        params.put("corporation", mEnterpriseRepresentEt.getText().toString());
        params.put("address", mEnterpriseAddressEt.getText().toString());
        params.put("pageToken", pageToken);
        if (!TextUtils.isEmpty(mEnterpriseIndustryEt.getText())) {
            params.put("profession", mEnterpriseIndustryEt.getText().toString());
        }
        if (!TextUtils.isEmpty(mEnterpriseBusinessEt.getText())) {
            params.put("tags", mEnterpriseBusinessEt.getText().toString());
        }
        params.put("businessImage", fileBytes);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + sessionId);
        ViewUtil.httpSendRequest(this, url, params, mHandler, headers, ENTERPRISE_REGISTER, null, null, "post");
    }

    private String getFileBytes(File licenseFile) {
        String fileBytes = "";
        FileInputStream inputStream = null;
        ByteArrayOutputStream bos = null;
        try {
            inputStream = new FileInputStream(licenseFile);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(b)) != -1) {
                bos.write(b, 0, len);
            }
            byte[] bytes = bos.toByteArray();
//            fileBytes = new String(bytes, "UTF-8");
            fileBytes = Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileBytes;
    }

    private void showPicturePopup() {
        View headSelectView = View.inflate(this, R.layout.layout_select_head, null);

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
                String[] permissions = new String[]{Manifest.permission.CAMERA};
                if (PermissionUtil.lacksPermissions(ct, permissions)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, PermissionUtil.DEFAULT_REQUEST);
                    }
                } else {
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(EnterpriseRegisterActivity.this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                    CameraUtil.captureImage(EnterpriseRegisterActivity.this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
                    closePopupWindow();
                }

            }
        });

        selectPicTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
                if (PermissionUtil.lacksPermissions(ct, permissions)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, 111);
                    }
                } else {
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(EnterpriseRegisterActivity.this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                    CameraUtil.pickImageSimple(EnterpriseRegisterActivity.this, REQUEST_CODE_PICK_PHOTO);
                    closePopupWindow();
                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.DEFAULT_REQUEST) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastUtil.showToast(this, "相机权限获取失败，请进入设置页面获取相机权限");
            } else {
                mNewPhotoUri = CameraUtil.getOutputMediaFileUri(EnterpriseRegisterActivity.this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                CameraUtil.captureImage(EnterpriseRegisterActivity.this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
                closePopupWindow();
            }
        } else if (requestCode == 111) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ToastUtil.showToast(this, "存储权限获取失败，请进入设置页面获取存储权限");
            } else {
                mNewPhotoUri = CameraUtil.getOutputMediaFileUri(EnterpriseRegisterActivity.this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                CameraUtil.pickImageSimple(EnterpriseRegisterActivity.this, REQUEST_CODE_PICK_PHOTO);
                closePopupWindow();
            }
        } else if (requestCode == 222) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                LogUtil.i("没有获取到权限");
                ToastUtil.showToast(this, "定位权限获取失败，请进入设置页面获取定位权限");
            } else {
                Intent intent = new Intent(ct, SearchLocationActivity.class);
                SearchPoiParam poiParam = new SearchPoiParam();
                poiParam.setType(2);
                poiParam.setTitle("地图搜索");
                poiParam.setRadius(1000);
//                poiParam.setContrastLatLng(MyApplication.getInstance().getBdLocationHelper().getLocation());
                poiParam.setResultCode(SELECT_ADDRESS_RESULT);
                poiParam.setDistanceTag(MyApplication.getInstance().getResources().getString(R.string.rice));
                intent.putExtra("data", poiParam);
                startActivityForResult(intent, SELECT_ADDRESS_REQUEST);
            }


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {
            // 拍照返回
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    path = mNewPhotoUri.getPath();
                    if (StringUtil.isEmpty(path)) {
                        return;
                    }

                    mLicenseFile = ImageUtil.compressBitmapToFile(path, 100, 300, 300);
                    if (StringUtil.isEmpty(path)) {
                        return;
                    }
                    ImageLoader.getInstance().displayImage(Uri.fromFile(mLicenseFile).toString(), mEnterpriseLicenseIv);

                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                }
            }
            isRegButtonEnable(mClauseCheckBox.isChecked());
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
                    path = CameraUtil.getImagePathFromUri(ct, data.getData());
                    LogUtil.i("path=" + path);
                    if (StringUtil.isEmpty(path)) {
                        return;
                    }

                    mLicenseFile = ImageUtil.compressBitmapToFile(path, 100, 300, 300);
                    if (StringUtil.isEmpty(path)) {
                        return;
                    }
                    ImageLoader.getInstance().displayImage(Uri.fromFile(mLicenseFile).toString(), mEnterpriseLicenseIv);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
            isRegButtonEnable(mClauseCheckBox.isChecked());
        } else if (requestCode == INDUSTRY_CHOOSE) {
            if (resultCode == Activity.RESULT_OK) {
                String industry = data.getStringExtra("industry");
                mEnterpriseIndustryEt.setText(industry);
                isRegButtonEnable(mClauseCheckBox.isChecked());
            }
        } else if (requestCode == SELECT_ADDRESS_REQUEST) {
            if (data != null && resultCode == SELECT_ADDRESS_RESULT) {
                PoiInfo poi = data.getParcelableExtra("resultKey");
                if (poi == null) {
                    return;
                }
                if (poi.address.contains(poi.city)) {
                    mEnterpriseAddressEt.setText(poi.address);
                } else {
                    mEnterpriseAddressEt.setText(poi.city + poi.address);
                }
                mLatitude = poi.location.latitude;
                mLongitude = poi.location.longitude;
            }
        }
    }

    /**
     * 获取可点击的SpannableString
     *
     * @return
     */
    private SpannableString getClickableSpan() {
        SpannableString spannableString = new SpannableString("我已阅读并同意《优软云服务协议》、《优软商城服务条款》、《优软商城买卖条款》");
        //设置下划线文字
        spannableString.setSpan(new UnderlineSpan(), 7, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字的单击事件
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                IntentUtils.webLinks(ct, "https://account.ubtob.com/common/rules", "优软云服务协议");
            }
        }, 7, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //设置文字的前景色
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 7, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new UnderlineSpan(), 17, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                IntentUtils.webLinks(ct, "https://mall.usoftchina.com/help#/issue/50", "优软商城服务条款");
            }
        }, 17, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 17, 27, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new UnderlineSpan(), 28, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                IntentUtils.webLinks(ct, "https://mall.usoftchina.com//help#/issue/16", "优软商城买卖条款");
            }
        }, 28, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), 28, 38, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}

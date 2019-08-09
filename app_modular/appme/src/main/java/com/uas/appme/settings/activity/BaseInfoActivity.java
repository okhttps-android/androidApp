package com.uas.appme.settings.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.dao.UserDao;
import com.core.model.EmployeesEntity;
import com.core.model.User;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.helper.LoginHelper;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.xmpp.model.Area;
import com.lidroid.xutils.ViewUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.uas.appme.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appme.other.activity.BasicInfoEditActivity;
import com.uas.appme.other.activity.CardcastActivity;
import com.uas.appme.other.activity.SelectAreaActivity;
import com.uas.appme.other.activity.UpdateSexActivity;
import com.uas.appme.other.activity.UpdateSingleTextActivity;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;


/**
  * @desc:用户个人资料界面
  * @author：Arison on 2018/1/23
  */
public class BaseInfoActivity extends BaseActivity implements View.OnClickListener {

    private ImageView iv_headImage;
    private TextView tv_nickname;
    private TextView tv_sex;
    private TextView tv_birthday;
    private TextView tv_address;
    private TextView tv_name;
    private TextView tv_tel;
    private TextView tv_depart;
    private TextView tv_position;
    private RelativeLayout rl_me_heard;
    private RelativeLayout rl_me_nickname;
    private RelativeLayout rl_me_sex;
    private RelativeLayout rl_me_birthday;
    private RelativeLayout rl_me_address;

    private User mUser;
    // Temp
    private User mTempData;
    // 选择头像的数据
    private File mCurrentFile;
    private boolean isError = false;

    private final int update_brithday = 0;
    private final int update_address = 1;

    private PopupWindow mHeadPopupWindow;
    private String mNickname, mSex;

    private ProgressDialog mProgressDialog;
    public static final String UPHEAD = "UPHEAD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_info);
        setTitle(getString(R.string.user_info_title));
        findViewById(R.id.my_data_rl).setOnClickListener(this);
        findViewById(R.id.my_friend_rl).setOnClickListener(this);
        findViewById(R.id.my_space_rl).setOnClickListener(this);
        findViewById(R.id.local_video_rl).setOnClickListener(this);
        ViewUtils.inject(this);
        initView();
    }

    private void initView() {
        iv_headImage = (ImageView) findViewById(R.id.me_heard_tv);
        tv_nickname = (TextView) findViewById(R.id.me_nickname_tv);
        tv_sex = (TextView) findViewById(R.id.me_sex_tv);
        tv_birthday = (TextView) findViewById(R.id.me_birthday_tv);
        tv_address = (TextView) findViewById(R.id.me_address_tv);
        tv_name = (TextView) findViewById(R.id.tv_name_value);
        tv_tel = (TextView) findViewById(R.id.tv_tel_value);
        tv_depart = (TextView) findViewById(R.id.tv_depart_value);
        tv_position = (TextView) findViewById(R.id.tv_position_value);
        rl_me_heard = (RelativeLayout) findViewById(R.id.me_heard);
        rl_me_nickname = (RelativeLayout) findViewById(R.id.me_nickname);
        rl_me_sex = (RelativeLayout) findViewById(R.id.me_birthday);
        rl_me_birthday = (RelativeLayout) findViewById(R.id.me_birthday);
        rl_me_address = (RelativeLayout) findViewById(R.id.me_address);
        findViewById(R.id.me_sex).setOnClickListener(this);
        mProgressDialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait));
        mUser = MyApplication.getInstance().mLoginUser;
        if (!LoginHelper.isUserValidation(mUser)) {
            isError = true;
            return;
        }
        initEvent();
        initData();

    }

    private void initEvent() {
        rl_me_heard.setOnClickListener(this);
        rl_me_nickname.setOnClickListener(this);
        rl_me_sex.setOnClickListener(this);
        rl_me_birthday.setOnClickListener(this);
        rl_me_address.setOnClickListener(this);
    }

    public void initData() {
        // clone一份临时数据，用来存数变化的值，返回的时候对比有无变化
        tv_nickname.setText(MyApplication.getInstance().mLoginUser.getNickName());
        try {
            mTempData = (User) mUser.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if (mTempData.getSex() == 1) {
//            rbBoy.setChecked(true);
            tv_sex.setText(getString(R.string.user_body));
            mSex = getString(R.string.user_body);
        } else {
            tv_sex.setText(getString(R.string.user_girl));
            mSex = getString(R.string.user_girl);
            //  rbGrid.setChecked(true);
        }
        tv_birthday.setText(DateFormatUtil.long2Str(mTempData.getBirthday() * 1000, DateFormatUtil.YMD));
        //查询数据库
        DBManager dbManager = new DBManager();
        String userId = mUser.getUserId();
        String whichsys = CommonUtil.getMaster();
        List<EmployeesEntity> entities = dbManager.select_getEmployee(new String[]{userId, whichsys}, "em_imid=? and whichsys=?");
        dbManager.closeDB();
        if (ListUtils.isEmpty(entities)) {
            mNickname = mTempData.getNickName();
        } else {
            mNickname = entities.get(0).getEM_NAME();
        }
        initEmployee(entities);
        if (!StringUtil.isEmpty(Area.getCityAreaString(0, mTempData.getAreaId()))) {
            tv_address.setText(Area.getProvinceCityString(mTempData.getProvinceId(), mTempData.getCityId())
                    + "-" + Area.getCityAreaString(0, mTempData.getAreaId()));
        } else {
            tv_address.setText(Area.getProvinceCityString(mTempData.getProvinceId(), mTempData.getCityId()));
        }
        if (Area.getProvinceCityString(mTempData.getProvinceId(), mTempData.getCityId()).contains("海外")) {
            tv_address.setText("海外");
        }
        AvatarHelper.getInstance().displayAvatar(mTempData.getUserId(), iv_headImage, true);
    }

    private void initEmployee(List<EmployeesEntity> entities) {
        String name = "";
        String phone = "";
        String depart = "";
        String position = "";
        if (!ListUtils.isEmpty(entities)) {
            EmployeesEntity employeesEntity = entities.get(0);
            if (employeesEntity != null) {
                name = employeesEntity.getEM_NAME();
                phone = employeesEntity.getEM_MOBILE();
                depart = employeesEntity.getEM_DEPART();
                position = employeesEntity.getEM_POSITION();
            }
        }
        if (isNull(name))
            name = CommonUtil.getName();
        if (isNull(phone))
            phone = CommonUtil.getSharedPreferences(ct, "user_phone");
        tv_name.setText(getNotNull(name));
        tv_tel.setText(getNotNull(phone));
        tv_depart.setText(getNotNull(depart));
        tv_position.setText(getNotNull(position));
    }

    private boolean isNull(String str) {
        if (StringUtil.isEmpty(str))
            return true;
        if ("未填写".equals(str.trim()))
            return true;
        return false;
    }

    private String getNotNull(String str) {
        if (StringUtil.isEmpty(str))
            return "未填写";
        return str;


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = null;
        if (id == R.id.my_data_rl) {
            startActivityForResult(new Intent(this, BasicInfoEditActivity.class), 1);
        } else if (id == R.id.my_friend_rl) {
            startActivity(new Intent(this, CardcastActivity.class));
        } else if (id == R.id.my_space_rl) {
        } else if (id == R.id.local_video_rl) {
            startActivity(new Intent("com.modular.im.LocalVideoActivity"));

        } else if (id == R.id.me_heard) {
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            View headSelectView = View.inflate(BaseInfoActivity.this, R.layout.layout_select_head, null);

            mHeadPopupWindow = new PopupWindow(headSelectView,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            mHeadPopupWindow.setAnimationStyle(R.style.MenuAnimationFade);
            mHeadPopupWindow.setFocusable(true);
            mHeadPopupWindow.setOutsideTouchable(true);
            DisplayUtil.backgroundAlpha(this, 0.5f);
            mHeadPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    closePopupWindow();
                }
            });
            mHeadPopupWindow.showAtLocation(View.inflate(this, R.layout.activity_base_info, null), Gravity.BOTTOM, 0, 0);
            TextView takePicTv = (TextView) headSelectView.findViewById(R.id.head_take_picture);
            TextView selectPicTv = (TextView) headSelectView.findViewById(R.id.head_select_photos);
            TextView cancelTv = (TextView) headSelectView.findViewById(R.id.head_cancel);

            takePicTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePhoto();
                    closePopupWindow();
                }
            });

            selectPicTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPhoto();
                    closePopupWindow();
                }
            });

            cancelTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closePopupWindow();
                }
            });
        } else if (id == R.id.me_nickname) {
            intent = new Intent(BaseInfoActivity.this, UpdateSingleTextActivity.class);
            intent.putExtra("nickname", tv_nickname.getText().toString().trim());
            startActivityForResult(intent, UPDATE_NICKNAME);
        } else if (id == R.id.me_sex) {
            intent = new Intent(BaseInfoActivity.this, UpdateSexActivity.class);
            intent.putExtra("sex", tv_sex.getText().toString().trim());
            startActivityForResult(intent, UPDATE_SEX);
        } else if (id == R.id.me_birthday) {
            showSelectBirthdayDialog();
        } else if (id == R.id.me_address) {
            intent = new Intent(BaseInfoActivity.this, SelectAreaActivity.class);
            intent.putExtra(SelectAreaActivity.EXTRA_AREA_TYPE, Area.AREA_TYPE_PROVINCE);
            intent.putExtra(SelectAreaActivity.EXTRA_AREA_PARENT_ID, Area.AREA_DATA_CHINA_ID);// 直接选择中国，
            intent.putExtra(SelectAreaActivity.EXTRA_AREA_DEEP, Area.AREA_TYPE_COUNTY);
            startActivityForResult(intent, SELECT_ADDRESS);
        }
    }

    private void closePopupWindow() {
        if (mHeadPopupWindow != null) {
            mHeadPopupWindow.dismiss();
            mHeadPopupWindow = null;
            DisplayUtil.backgroundAlpha(this, 1f);
        }
    }

    @SuppressWarnings("deprecation")
    private void showSelectBirthdayDialog() {
        Date date = new Date(mTempData.getBirthday() * 1000);
        DateTimePicker picker = new DateTimePicker(this, DateTimePicker.YEAR_MONTH_DAY);
        picker.setRange(1900, 2030);
        picker.setSelectedItem(date.getYear() + 1900,
                date.getMonth() + 1,
                date.getDate());
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                Log.i(TAG, "onDateTimePicked:" + year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
                GregorianCalendar calendar = new GregorianCalendar(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));

                long currentTime = System.currentTimeMillis() / 1000;
                long birthdayTime = calendar.getTime().getTime() / 1000;
                if (birthdayTime > currentTime) {
                    ToastUtil.showToast(mContext, "亲!您的出生日期已经超过现在了哦!");
                } else {
                    mTempData.setBirthday(TimeUtils.getSpecialBeginTime(tv_birthday, calendar.getTime().getTime() / 1000));
                    updateData();
                }
            }
        });
        picker.show();

    }

    private static final int REQUEST_CODE_CAPTURE_CROP_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_CROP_PHOTO = 2;
    private static final int REQUEST_CODE_CROP_PHOTO = 3;
    private static final int SELECT_ADDRESS = 4;
    private static final int UPDATE_NICKNAME = 5;
    private static final int UPDATE_SEX = 6;
    private Uri mNewPhotoUri;

    private void takePhoto() {
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_CROP_PHOTO);
    }


    private void selectPhoto() {
        CameraUtil.pickImageSimple(this, REQUEST_CODE_PICK_CROP_PHOTO);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CAPTURE_CROP_PHOTO) {// 拍照返回再去裁减
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    Uri o = mNewPhotoUri;
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    CameraUtil.cropImage(this, o, mNewPhotoUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_CROP_PHOTO) {// 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    String path = CameraUtil.getImagePathFromUri(this, data.getData());
                    Uri o = Uri.fromFile(new File(path));
                    mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    CameraUtil.cropImage(this, o, mNewPhotoUri, REQUEST_CODE_CROP_PHOTO, 1, 1, 300, 300);
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_CROP_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    mCurrentFile = new File(mNewPhotoUri.getPath());
                    if (mCurrentFile != null && mCurrentFile.exists()) {
                        Log.d("wang", "uploadAvatar");
                        uploadAvatar(mCurrentFile);
                    }
                    ImageLoader.getInstance().displayImage(mNewPhotoUri.toString(), iv_headImage,MyApplication.mAvatarRoundImageOptions);
                } else {
                    ToastUtil.showToast(this, R.string.c_crop_failed);
                }
            }

        } else if (requestCode == SELECT_ADDRESS) {// 选择城市
            if (resultCode == RESULT_OK && data != null) {
                int countryId = data.getIntExtra(SelectAreaActivity.EXTRA_COUNTRY_ID, 0);
                int provinceId = data.getIntExtra(SelectAreaActivity.EXTRA_PROVINCE_ID, 0);
                int cityId = data.getIntExtra(SelectAreaActivity.EXTRA_CITY_ID, 0);
                int countyId = data.getIntExtra(SelectAreaActivity.EXTRA_COUNTY_ID, 0);

                // String country_name = data.getStringExtra(Constant.EXTRA_COUNTRY_NAME);
                String province_name = data.getStringExtra(SelectAreaActivity.EXTRA_PROVINCE_NAME);
                String city_name = data.getStringExtra(SelectAreaActivity.EXTRA_CITY_NAME);
                String county_name = data.getStringExtra(SelectAreaActivity.EXTRA_COUNTY_NAME);
                if (StringUtil.isEmpty(county_name)) {
                    tv_address.setText(province_name + "-" + city_name);
                } else {
                    tv_address.setText(province_name + "-" + city_name + "-" + county_name);
                }
                if ("海外".equals(province_name)) {
                    tv_address.setText(province_name);
                }
                LogUtil.d("省：" + provinceId + "市：" + cityId + "县：" + countryId);
                mTempData.setCountryId(countryId);
                mTempData.setProvinceId(provinceId);
                mTempData.setCityId(cityId);
                mTempData.setAreaId(countyId);

                updateData();//更新数据
            }
        } else if (requestCode == UPDATE_NICKNAME && data != null) {
            mNickname = data.getStringExtra("newnickname");
            if (!StringUtil.isEmpty(mNickname)) {
                String name = StringUtil.toHttpString(mNickname);
                if (StringUtil.isEmpty(name)) {
                    ToastUtil.showToast(ct, R.string.limit_unno_zijie);
                    return;
                }
                tv_nickname.setText(name);
                mTempData.setNickName(mNickname);
                updateData();
            }
        } else if (requestCode == UPDATE_SEX && data != null) {
            mSex = data.getStringExtra("newsex");
            if (mSex != null) {
                tv_sex.setText(mSex);
                if (mSex.equals("男")) {
                    mTempData.setSex(1);
                } else if (mSex.equals("女")) {
                    mTempData.setSex(0);
                }
                updateData();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void updateData() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        if (!mUser.getNickName().equals(mTempData.getNickName())) {
            params.put("nickname", mTempData.getNickName());
        }
        if (mUser.getSex() != mTempData.getSex()) {
            params.put("sex", String.valueOf(mTempData.getSex()));
        }
        if (mUser.getBirthday() != mTempData.getBirthday()) {
            params.put("birthday", String.valueOf(mTempData.getBirthday()));
        }
        if (mUser.getCountryId() != mTempData.getCountryId()) {
            params.put("countryId", String.valueOf(mTempData.getCountryId()));
        }
        if (mUser.getProvinceId() != mTempData.getProvinceId()) {
            params.put("provinceId", String.valueOf(mTempData.getProvinceId()));
        }
        if (mUser.getCityId() != mTempData.getCityId()) {
            params.put("cityId", String.valueOf(mTempData.getCityId()));
        }
        if (mUser.getAreaId() != mTempData.getAreaId()) {
            params.put("areaId", String.valueOf(mTempData.getAreaId()));
        }
        ProgressDialogUtil.show(mProgressDialog);
        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.USER_UPDATE, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ProgressDialogUtil.dismiss(mProgressDialog);
                ToastUtil.showErrorNet(BaseInfoActivity.this);
            }
        }, new StringJsonObjectRequest.Listener<Void>() {

            @Override
            public void onResponse(ObjectResult<Void> result) {
                boolean success = Result.defaultParser(mContext, result, true);
                if (success) {
                    saveData();
                } else {
                    ProgressDialogUtil.dismiss(mProgressDialog);
                }
            }
        }, Void.class, params);
        addDefaultRequest(request);
    }

    private void saveData() {
        if (!mUser.getNickName().equals(mTempData.getNickName())) {
            MyApplication.getInstance().mLoginUser.setNickName(mTempData.getNickName());
            UserDao.getInstance().updateNickName(mTempData.getUserId(), mTempData.getNickName());// 更新数据库
        }
        if (mUser.getSex() != mTempData.getSex()) {
            MyApplication.getInstance().mLoginUser.setSex(mTempData.getSex());
            UserDao.getInstance().updateSex(mTempData.getUserId(), mTempData.getSex() + "");// 更新数据库
        }
        if (mUser.getBirthday() != mTempData.getBirthday()) {
            MyApplication.getInstance().mLoginUser.setBirthday(mTempData.getBirthday());
            UserDao.getInstance().updateBirthday(mTempData.getUserId(), mTempData.getBirthday() + "");// 更新数据库
        }

        if (mUser.getCountryId() != mTempData.getCountryId()) {
            MyApplication.getInstance().mLoginUser.setCountryId(mTempData.getCountryId());
            UserDao.getInstance().updateCountryId(mTempData.getUserId(), mTempData.getCountryId());
        }
        if (mUser.getProvinceId() != mTempData.getProvinceId()) {
            MyApplication.getInstance().mLoginUser.setProvinceId(mTempData.getProvinceId());
            UserDao.getInstance().updateProvinceId(mTempData.getUserId(), mTempData.getProvinceId());
        }
        if (mUser.getCityId() != mTempData.getCityId()) {
            MyApplication.getInstance().mLoginUser.setCityId(mTempData.getCityId());
            UserDao.getInstance().updateCityId(mTempData.getUserId(), mTempData.getCityId());
        }
        if (mUser.getAreaId() != mTempData.getAreaId()) {
            MyApplication.getInstance().mLoginUser.setAreaId(mTempData.getAreaId());
            UserDao.getInstance().updateAreaId(mTempData.getUserId(), mTempData.getAreaId());
        }
        if (mCurrentFile != null && mCurrentFile.exists()) {
            uploadAvatar(mCurrentFile);
        } else {
            ProgressDialogUtil.dismiss(mProgressDialog);
        }

    }


    private void uploadAvatar(File file) {
        if (!file.exists()) {// 文件不存在
            return;
        }
        // 显示正在上传的ProgressDialog
        ProgressDialogUtil.show(mProgressDialog, getString(R.string.upload_avataring));
        RequestParams params = new RequestParams();
        final String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        params.put("userId", loginUserId);
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        AsyncHttpClient client = new AsyncHttpClient();
        LogUtil.i("url=" + mConfig.AVATAR_UPLOAD_URL);
        LogUtil.i("params=" + params.toString());
        client.post(mConfig.AVATAR_UPLOAD_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                boolean success = false;
                if (arg0 == 200) {
                    Result result = null;
                    try {
                        result = JSON.parseObject(new String(arg2), Result.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }
                ProgressDialogUtil.dismiss(mProgressDialog);
                if (success) {
                    Intent intent = new Intent(UPHEAD);
                    intent.putExtra(UPHEAD, "updata");
                    BaseInfoActivity.this.sendBroadcast(intent);
                    AvatarHelper.getInstance().deleteAvatar(loginUserId);
                    ToastUtil.showToast(BaseInfoActivity.this, R.string.upload_avatar_success);
                    mCurrentFile = null;
                    mProgressDialog = null;
                } else {
                    ToastUtil.showToast(BaseInfoActivity.this, R.string.upload_avatar_failed);
                }

//                finish();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                ProgressDialogUtil.dismiss(mProgressDialog);
                ToastUtil.showToast(BaseInfoActivity.this, R.string.upload_avatar_failed);
            }
        });
    }

    @Override
    public void onBackPressed() {
        doBack();
    }


    @Override
    protected boolean onHomeAsUp() {
        doBack();
        return true;
    }

    private void doBack() {
        if (isError) {
            super.onBackPressed();
            return;
        }
        loadPageData();
        Log.d("raomeng", mUser.toString());
        Log.d("raomeng", mTempData.toString());

        if ((mUser != null && !mUser.equals(mTempData)) || (mCurrentFile != null && mCurrentFile.exists())) {
            //showBackDialog();
            finish();
        } else {
            finish();
        }

    }

    private void loadPageData() {
        mTempData.setNickName(tv_nickname.getText().toString().trim());
    }


/*    private void showBackDialog() {
        *//*AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.prompt_title).setMessage("个人资料发生改变，是否保存当前内容？")
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.create().show();*//*

        if (!MyApplication.getInstance().isNetworkActive()) {
            ToastUtil.showToast(BaseInfoActivity.this, "网络异常，更新失败");
            return;
        }
        loadPageData();
        if (TextUtils.isEmpty(mTempData.getNickName())) {
            tv_nickname.requestFocus();
            tv_nickname.setError(StringUtils.editTextHtmlErrorTip(BaseInfoActivity.this, R.string.name_empty_error));
            return;
        }

        if (mTempData.getCityId() <= 0) {
            ToastUtil.showToast(mContext, R.string.live_address_empty_error);
            return;
        }

        if (mUser != null && !mUser.equals(mTempData)) {// 数据改变了，提交数据
            Log.d("wang", "数据改变了，提交数据");
          //  updateData();
        }
        *//*if (mCurrentFile != null && mCurrentFile.exists()) {
            Log.d("wang", "uploadAvatar");
            uploadAvatar(mCurrentFile);
        }*//*
    }*/

}

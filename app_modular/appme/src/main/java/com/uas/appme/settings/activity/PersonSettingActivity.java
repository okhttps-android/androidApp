package com.uas.appme.settings.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.andreabaccega.widget.FormEditText;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.hmac.Md5Util;
import com.common.system.PermissionUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ImageUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.SelectBean;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.view.Activity.SelectActivity;
import com.core.xmpp.model.AddAttentionResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appme.R;
import com.uas.appme.settings.model.BSettingPlaceBean;
import com.uas.appme.settings.model.PersonSetingBean;
import com.umeng.socialize.utils.Log;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FANGlh on 2017/10/10.
 * function:
 */
public class PersonSettingActivity extends SupportToolBarActivity implements View.OnClickListener{
    private FormEditText mUserNameTv;
    private FormEditText mUserSexTv;
    private RelativeLayout mRySetStartTime;
    private FormEditText mUserDepartmentEv;
    private FormEditText mUserJobTv;
    private FormEditText mUserTelTv;
    private FormEditText mUserEmailTv;
    private Button mBtnSave;
    private String sm_id;
    private int sm_sex = -1;
    private RelativeLayout mDepartmentRl;
    private TextView user_department_tv;
    private List<String> departmentModel_list;
    private String result;
    private String sm_userid;
    private Button deleteBtn;
    private ImageView image_im;
    private Uri mNewPhotoUri;// 拍照和图库 获得图片的URI
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;//单选照片
    private static final int REQUEST_CODE_CAPTURE_CROP_PHOTO = 1;
    private static final int REQUEST_CODE_PICK_CROP_PHOTO = 2;
    private static final int REQUEST_CODE_CROP_PHOTO = 3;
    private String photoselect=null;
    private ProgressDialog mProgressDialog;
    private Boolean updateChangeP = false; //更新是否更换了头像，若没换
    private String imid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.person_setting_activity);
        initView();
        initData();
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        for (String permission : permissions) {
            if (PermissionUtil.lacksPermissions(ct, permission)) {
                PermissionUtil.requestPermission(this, PermissionUtil.DEFAULT_REQUEST, permission);
            }
        }
    }


    private void initView() {

        mUserNameTv = (FormEditText) findViewById(R.id.user_name_tv);
        mUserSexTv = (FormEditText) findViewById(R.id.user_sex_tv);
        mRySetStartTime = (RelativeLayout) findViewById(R.id.ry_set_startTime);
        mUserDepartmentEv = (FormEditText) findViewById(R.id.user_department_et);
        mUserJobTv = (FormEditText) findViewById(R.id.user_job_tv);
        mUserTelTv = (FormEditText) findViewById(R.id.user_tel_tv);
        mUserEmailTv = (FormEditText) findViewById(R.id.user_email_tv);
        mBtnSave = (Button) findViewById(R.id.btn_save);
        mDepartmentRl = (RelativeLayout) findViewById(R.id.user_department_rl);
        user_department_tv = (TextView) findViewById(R.id.user_department_tv);
        deleteBtn = (Button)findViewById(R.id.deleteBtn);
        image_im = (ImageView) findViewById(R.id.image_im);
        image_im.setOnClickListener(this);
        findViewById(R.id.image_tv_).setOnClickListener(this);

        deleteBtn.setOnClickListener(this);
        mUserSexTv.setKeyListener(null);
        mUserSexTv.setFocusable(false);
        mUserSexTv.setOnClickListener(this);
        mUserDepartmentEv.setKeyListener(null);
        mUserDepartmentEv.setFocusable(false);
        mUserDepartmentEv.setOnClickListener(this);

        mBtnSave.setOnClickListener(this);

        //接收商家类型
        sc_industry = getIntent().getStringExtra("sc_industry");
        sc_industrycode = getIntent().getStringExtra("sc_industrycode");

        mProgressDialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait));
    }
    private void initData() {

        //更新时的数据获取
        result = getIntent().getStringExtra("mdoel");
        int position = getIntent().getIntExtra("position",-1);
        if (!StringUtil.isEmpty(result) && position != -1)
            showUpdatedata(result,position);
        else
            deleteBtn.setVisibility(View.GONE);

        //接收商家类型
      String  sc_industry = getIntent().getStringExtra("sc_industry");
      String sc_industrycode = getIntent().getStringExtra("sc_industrycode");

      if (!"医疗".equals(sc_industry))
          user_department_tv.setText(getString(R.string.common_department));

        HttpClient httpClient =
//                new HttpClient.Builder(Constant.BASE_BOOKING_SETTING_URL).isDebug(true).build(true);\
                new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appPlaceList")
                .add("companyid",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appPlaceList", o.toString()+"");
                    try {
                        BSettingPlaceBean departmentModel = new BSettingPlaceBean();
                        departmentModel = JSON.parseObject(o.toString(),BSettingPlaceBean.class);
                        if (departmentModel == null || ListUtils.isEmpty(departmentModel.getResult())) return;
                        departmentModel_list = new ArrayList<>();
                        for (int i=0;i<departmentModel.getResult().size();i++)
                            departmentModel_list.add(departmentModel.getResult().get(i).getSt_name());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }));

    }

    private void showUpdatedata(String result,int position) {
        deleteBtn.setVisibility(View.VISIBLE);
        PersonSetingBean model = new PersonSetingBean();
        try {
            model = JSON.parseObject(result,PersonSetingBean.class);
            mUserNameTv.setText(model.getResult().get(position).getSm_username());
            mUserDepartmentEv.setText(model.getResult().get(position).getSm_stname());
            mUserJobTv.setText(model.getResult().get(position).getSm_level());
            mUserTelTv.setText(model.getResult().get(position).getSm_telephone());
            mUserEmailTv.setText(model.getResult().get(position).getSm_email());
            mUserSexTv.setText("1".equals(model.getResult().get(position).getSm_sex()) ? getString(R.string.user_body) : getString(R.string.user_girl));
            sm_id = model.getResult().get(position).getSm_id();
            mBtnSave.setText(getString(R.string.common_update_button));
          setTitle(getString(R.string.common_update_button));
            sm_userid = model.getResult().get(position).getSm_userid();
            AvatarHelper.getInstance().display(sm_userid, image_im, true, true); //显示头像照片
            photoselect = AvatarHelper.getAvatarUrl(sm_userid,true);
            Log.i("files0x01",photoselect);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.user_sex_tv){
            showSelectSexDialog();
        }else if (v.getId() == R.id.user_department_et){
//            String[] department = {"内科","外科","儿科","传染病科","妇产科","男科","精神心理科","皮肤性病科","中医科","肿瘤科","骨科","康复医学科","麻醉医学科","营养科","五官科","医学影像科","其他科室"};
            int requestCode = 0x01;
            if (ListUtils.isEmpty(departmentModel_list)) {
                ToastMessage(getString(R.string.cur_no_room));
                return;
            }else {
                doSelectDepartment(departmentModel_list, requestCode);
            }
        }else if (v.getId() == R.id.btn_save){
            if (!CommonUtil.isNetWorkConnected(ct)){
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }
            saveJudge();
        }else if (v.getId() == R.id.deleteBtn){
            if (!CommonUtil.isNetWorkConnected(ct)){
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }
            doDelete();
        }else if (v.getId() == R.id.image_im || v.getId() == R.id.image_tv_){
            if (PermissionUtil.lacksPermissions(ct, Manifest.permission.CAMERA)) {
                PermissionUtil.requestPermission(PersonSettingActivity.this,PermissionUtil.DEFAULT_REQUEST,Manifest.permission.CAMERA);
                ToastUtil.showToast(ct,R.string.not_camera_permission);
            } else {
                showSelectPictureDialog();//添加
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
//                            CameraUtil.pickImageSimple(PersonSettingActivity.this, REQUEST_CODE_PICK_PHOTO);  //传统单选方式
                            CameraUtil.pickImageSimple(PersonSettingActivity.this, REQUEST_CODE_PICK_CROP_PHOTO);
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
    private void takePhoto() {
//        try {
//            mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().getLoginUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
//            CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_CROP_PHOTO);
    }
    private void doDelete() {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreDel")
                .add("keyfield", "sm_id")
                .add("id",sm_id)
                .add("tablename","ServiceMan")
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("appStoreDel", o.toString() + "");
                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")){
                        Toast.makeText(ct,getString(R.string.delete_all_succ),Toast.LENGTH_LONG).show();
                        setResult(20);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
    private void doSelectDepartment(List<String> department, int requestCode) {
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
       /* for (String e : department) {
            bean = new SelectBean();
            bean.setName(e);
            bean.setClick(false);
            beans.add(bean);
        }*/
        LogUtil.prinlnLongMsg("department",JSON.toJSONString(department));

        for (int i = 0; i < department.size(); i++) {
            bean = new SelectBean();
            bean.setName(department.get(i));
            bean.setClick(false);
            beans.add(bean);
        }

        String title = null;
        if (!"医疗".equals(sc_industry))
            title = getString(R.string.select_department);
        else
            title = getString(R.string.select_room);

        LogUtil.prinlnLongMsg("department beans",JSON.toJSONString(beans));
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", title);
        startActivityForResult(intent, requestCode);
    }
    private File mCurrentFile;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0x20) {
            if (data == null) return;
            SelectBean b = data.getParcelableExtra("data");
            if (b == null) return;
            String department = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
            if (requestCode == 0x01) {
                mUserDepartmentEv.setText(department);
            }
        }
       /* if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    photoselect = mNewPhotoUri.getPath();
                    ImageLoader.getInstance().displayImage(mNewPhotoUri.toString(),image_im);
                    Log.i("files0x01",photoselect);
                    updateChangeP = true;
                } else {
                    ToastUtil.showToast(this, com.uas.appworks.R.string.c_take_picture_failed);
                }
            }
        }else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 传统单选方式，选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK){
                if (data != null && data.getData() != null){
                    photoselect= CameraUtil.getImagePathFromUri(this, data.getData());
                    image_im.setImageURI(Uri.fromFile(new File(photoselect)));
//                    ImageLoader.getInstance().displayImage(photoselect,image_im);
                    Log.i("files0x01",photoselect);
                    updateChangeP = true;
                }
            }
        }*/


        else  if (requestCode == REQUEST_CODE_CAPTURE_CROP_PHOTO) {// 拍照返回再去裁减
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
                    ImageLoader.getInstance().displayImage(mNewPhotoUri.toString(), image_im);
                    photoselect = mCurrentFile.getPath();
                    updateChangeP = true;
                } else {
                    ToastUtil.showToast(this, R.string.c_crop_failed);
                }
            }

        }
    }

    private void saveJudge() {
        if (StringUtil.isEmpty(photoselect)) {
            ToastMessage(getString(R.string.please_add_headerimg));
            return;
        }
        File file=  new File(photoselect);
        if (StringUtil.isEmpty(result)){
            if (!file.exists()) {// 录入时的，且图片文件不存在
                ToastMessage(getString(R.string.please_add_headerimg));
                return;
            } else {
                doSave();
            }
        }else
            doSave();

    }

    private void doSave() {
        if (mUserNameTv.testValidity() && mUserSexTv.testValidity()
                && mUserTelTv.testValidity() && mUserEmailTv.testValidity()){
            if (!StringUtil.isEmpty(mUserEmailTv.getText().toString())) {
                if (mUserEmailTv.getText().toString().contains("@")){
                    saveRequest();
                }else {
                    ToastMessage(getString(com.uas.appcontact.R.string.Please_imput_the_correct_email_format));
                }
            }else
                saveRequest();
        }
    }

    private void saveRequest() {

        Map<String, Object> params = new HashMap<>();
        if (!StringUtil.isEmpty(sm_id))
            params.put("sm_id",sm_id);

        params.put("sm_stid",0);
        params.put("sm_userid",StringUtil.isEmail(sm_userid) ? 0 : sm_userid);
        params.put("sm_username",mUserNameTv.getText().toString());
        params.put("sm_companyid",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
        params.put("sm_stname",mUserDepartmentEv.getText().toString());
        params.put("sm_companyname", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_commpany"));
        params.put("sm_level",mUserJobTv.getText().toString());
        params.put("sm_telephone",mUserTelTv.getText().toString());
        params.put("sm_sex",sm_sex);
        params.put("sm_email",mUserEmailTv.getText().toString());
        LogUtil.prinlnLongMsg("appPersonSet",JSONUtil.map2JSON(params));

        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appPersonSet")
                .add("map", JSONUtil.map2JSON(params))
                .add("token",MyApplication.getInstance().mAccessToken)
                .add("telephone",mUserTelTv.getText().toString())
                .add("password", Md5Util.toMD5(111111+""))
                .add("nickname",mUserNameTv.getText().toString())
                .add("description","UU互联")
                .add("sex",sm_sex)
                .add("birthday",94665600)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("appPersonSet", o.toString()+"");

                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBoolean("result")){
                        if (o.toString().contains("imid")){
                            imid = JSON.parseObject(o.toString()).getString("imid");
                            if (StringUtil.isEmpty(imid)) {
//                               ToastMessage("imid获取失败，请稍后再试");
                                return;
                            }
                            if (updateChangeP) {  //更换过照片
                                unLoadHeader(imid);
                            }else {
                                beforeSendMsg();  //只有在更新操作时且没换头像走这里
                            }
                        }else
                            beforeSendMsg();

                    }else if (!StringUtil.isEmpty(JSON.parseObject(o.toString()).getString("result"))){
                        ToastMessage(JSON.parseObject(o.toString()).getString("result"));
                    }else
                        ToastMessage(getString(R.string.save_failed));
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastMessage(getString(R.string.save_failed));
                }
            }
        }));

    }

    private void jumpTo() {
      /*  Map<String,Object> formStoreMap = new HashMap<>();
        formStoreMap.put("userName",mUserNameTv.getText().toString());
        formStoreMap.put("userSex","男".equals(mUserSexTv.getText().toString()) ? "M" : "F");
        formStoreMap.put("userTel",mUserTelTv.getText().toString());
        formStoreMap.put("userEmail",mUserEmailTv.getText().toString());

       HttpClient httpClient = new HttpClient.Builder("http://192.168.253.192:8088/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("platform-b2b/mobile/adduser/user \n")
                .add("formStore", JSONUtil.map2JSON(formStoreMap))
                .add("dept",mUserDepartmentEv.getText().toString())
                .add("role",mUserJobTv.getText().toString())
                .add("enuu",CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("emcode",imid)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                Log.d("fanglh",o.toString());
                try {
                    if (!StringUtil.isEmpty(o.toString()) && JSON.parseObject(o.toString()).containsKey("success") &&
                            "人员添加成功".equals(JSON.parseObject(o.toString()).getString("success"))){
                        if (!StringUtil.isEmpty(sm_id)){
                            Toast.makeText(ct,getString(R.string.update_success),Toast.LENGTH_LONG).show();
                            setResult(20);
                        }else {
                            Toast.makeText(ct,getString(R.string.common_save_success),Toast.LENGTH_LONG).show();
                            startActivity(new Intent(PersonSettingActivity.this,PersonSettingListActivity.class)
                                    .putExtra("sc_industry",sc_industry)
                                    .putExtra("sc_industrycode",sc_industrycode));
                        }
                        finish();
                    }else{

                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }));*/

       if (!StringUtil.isEmpty(sm_id)){
            Toast.makeText(ct,getString(R.string.update_success),Toast.LENGTH_LONG).show();
            setResult(20);
        }else {
            Toast.makeText(ct,getString(R.string.common_save_success),Toast.LENGTH_LONG).show();
            startActivity(new Intent(PersonSettingActivity.this,PersonSettingListActivity.class)
                    .putExtra("sc_industry",sc_industry)
                    .putExtra("sc_industrycode",sc_industrycode));
        }
        finish();
    }

    private void unLoadHeader(String imid) {
        File file=  new File(photoselect);
        if (!file.exists()) {// 文件不存在
            return;
        }

        file = ImageUtil.compressBitmapToFile(photoselect,100,360,480);  //压缩
        // 显示正在上传的ProgressDialog
        ProgressDialogUtil.show(mProgressDialog, getString(R.string.upload_avataring));
        RequestParams params = new RequestParams();
//        String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        params.put("userId", imid);
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
                    ToastUtil.showToast(PersonSettingActivity.this, R.string.upload_avatar_success);
                    beforeSendMsg();

                } else {
                    ToastUtil.showToast(PersonSettingActivity.this, R.string.upload_avatar_failed);
                }

            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                ProgressDialogUtil.dismiss(mProgressDialog);
                ToastUtil.showToast(PersonSettingActivity.this, R.string.upload_avatar_failed);
            }
        });
    }

    private void beforeSendMsg() {
        String name = CommonUtil.getName();
        String phone = mUserTelTv.getText().toString().trim().replaceAll(" ", "");
        String modeid1 = "0398e112-97a3-40b0-8430-0e871ef22524";

        String modeid2 = "fd4ac30e-b176-4410-ac0e-e39c8b71dfe0";

        if (!StringUtil.isEmpty(sm_id)) {
            jumpTo();
            return; //更新时不发短信
        }
        sendMessages(modeid1,name,phone);
        sendMessages(modeid2,name,phone);
    }

    private int sendM = 0;
    private void sendMessages( final String modeid, final String name, final String phone) {
        sendM++;
        StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
                Request.Method.POST, "http://message.ubtob.com/sms/send", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
//                dimssLoading();
            }
        }, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
            @Override
            public void onResponse(ObjectResult<AddAttentionResult> result) {
                if (sendM == 2){
                    Toast.makeText(ct,"短信发送成功",Toast.LENGTH_LONG).show();
                }
            }
        }, AddAttentionResult.class, null) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                String param = "{\"receiver\":\"" + phone + "\",\"params\":[\"" + name + "\"],\"templateId\":\"" + modeid + "\"}";
                LogUtil.i("param=" + param);
                return param.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        MyApplication.getInstance().getFastVolley().addDefaultRequest("Volley", request);

        if (sendM == 2)
            jumpTo();
    }

    private void showSelectSexDialog() {
        // 1是男，0是女，2是全部
        String[] sexs = new String[]{ getString(com.uas.appcontact.R.string.user_body), getString(com.uas.appcontact.R.string.user_girl)};
        int checkItem = 0;
        new AlertDialog.Builder(this).setTitle(getString(com.uas.appcontact.R.string.select_sex_title))
                .setSingleChoiceItems(sexs, checkItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            mUserSexTv.setText(R.string.user_body);
                            sm_sex = 1;
                        } else {
                            mUserSexTv.setText(R.string.user_girl);
                            sm_sex = 0;
                        }
                        dialog.dismiss();
                    }
                }).setCancelable(true).create().show();
    }
    private String sc_industry;
    private String sc_industrycode;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (!StringUtil.isEmpty(result)) {

        } else {
            getMenuInflater().inflate(R.menu.bsetting_more, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bsetting_more, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.booking_set_list){
            startActivity(new Intent(this,PersonSettingListActivity.class)
                    .putExtra("sc_industry",sc_industry)
                    .putExtra("sc_industrycode",sc_industrycode));
        }
        return super.onOptionsItemSelected(item);
    }
}

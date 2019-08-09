package com.uas.appme.settings.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.aip.excep.activity.FaceManageActivity;
import com.baidu.android.pushservice.PushManager;
import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPCheckUpdateCallback;
import com.baidu.autoupdatesdk.CPUpdateDownloadCallback;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.file.FileUtils;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.common.system.SystemUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.dao.work.VideoFileDao;
import com.core.dao.work.WorkModelDao;
import com.core.model.OAConfig;
import com.core.model.WorkModel;
import com.core.net.http.ViewUtil;
import com.core.net.utils.NetUtils;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.LoginHelper;
import com.core.utils.sp.UserSp;
import com.core.widget.view.SwitchView;
import com.hss01248.notifyutil.NotifyUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.utils.PopupWindowHelper;
import com.scwang.smartrefresh.layout.util.DensityUtil;
import com.uas.applocation.test.LocationTestSetActivity;
import com.uas.appme.R;
import com.uas.appme.pedometer.view.NewStepActivity;
import com.uas.appme.pedometer.view.StepSplashActivity;
import com.uas.appworks.OA.erp.activity.FlightsActivity;
import com.uas.appworks.OA.erp.activity.MissionSetActivity;
import com.uas.appworks.OA.erp.activity.MyRuleSetActivity;
import com.uas.appworks.OA.erp.activity.OfficeAddressSettingsActivity;
import com.uas.appworks.OA.erp.activity.SignSeniorSettingActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 设置
 */
public class SettingActivity extends SupportToolBarActivity implements View.OnClickListener {

    private TextView mExitBtn;
    private TextView mCacheTv;
    private TextView uas_website_tv;
    private TextView tv_menu_setting;
    private SwitchView cb_task_reply;
    private RelativeLayout uu_step_rl;
    private RelativeLayout sign_in_rl;
    private RelativeLayout sign_out_rl;
    private TextView tv_signauto_new;
    private TextView tv_sign_new;
    private TextView tv_language_new;
    private TextView tv_booking_new;
    private TextView tv_help_new;
    private RelativeLayout language_us_rl;
    private ImageView uu_step_im;
    private SwitchView uu_step_reply;
    private RelativeLayout rl_app_update;
    private TextView version_value;
    private RelativeLayout close_push_rl;
    private RelativeLayout speech_recognition_rl;
    private RelativeLayout new_step_rl;
    private RelativeLayout booking_set_rl;

    private ProgressDialog dialog;
    private RelativeLayout share_rl;
    private String newStep_service_name = "com.uas.appme.pedometer.service.StepService";

    private PopupWindow setWindow = null;//
    private RelativeLayout businessmen_setting_rl;
    private TextView bsettingRed;
    private RelativeLayout font_us_rl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        super.setTitle(getString(R.string.user_setting));
        initView();
    }


    private void initView() {
        font_us_rl = findViewById(R.id.font_us_rl);
        uas_website_tv = (TextView) findViewById(R.id.uas_website_tv);
        tv_menu_setting = (TextView) findViewById(R.id.tv_menu_setting);
        cb_task_reply = (SwitchView) findViewById(R.id.cb_task_reply);
        uu_step_rl = (RelativeLayout) findViewById(R.id.uu_step_rl);
        sign_in_rl = (RelativeLayout) findViewById(R.id.sign_in_rl);
        sign_out_rl = (RelativeLayout) findViewById(R.id.sign_out_rl);
        tv_signauto_new = (TextView) findViewById(R.id.tv_signauto_new);
        tv_sign_new = (TextView) findViewById(R.id.tv_sign_new);
        tv_language_new = (TextView) findViewById(R.id.tv_language_new);
        tv_booking_new = (TextView) findViewById(R.id.tv_booking_new);
        tv_help_new = (TextView) findViewById(R.id.tv_help_new);
        language_us_rl = (RelativeLayout) findViewById(R.id.language_us_rl);
        uu_step_im = (ImageView) findViewById(R.id.uu_step_im);
        uu_step_reply = (SwitchView) findViewById(R.id.uu_step_reply);
        rl_app_update = (RelativeLayout) findViewById(R.id.rl_app_update);
        version_value = (TextView) findViewById(R.id.version_value);
        close_push_rl = (RelativeLayout) findViewById(R.id.close_push_rl);
        speech_recognition_rl = (RelativeLayout) findViewById(R.id.speech_recognition_rl);
        new_step_rl = (RelativeLayout) findViewById(R.id.new_step_rl);
        booking_set_rl = (RelativeLayout) findViewById(R.id.booking_set_rl);
        businessmen_setting_rl = (RelativeLayout) findViewById(R.id.businessmen_setting_rl);
        businessmen_setting_rl.setOnClickListener(this);
        bsettingRed = (TextView) findViewById(R.id.businessmen_setting_new);
        if (PreferenceUtils.getBoolean(MyApplication.getInstance(), Constants.B_SETTINGRED)) //商家设置红点
            bsettingRed.setVisibility(View.GONE);
        new_step_rl.setOnClickListener(this);
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        share_rl = (RelativeLayout) findViewById(R.id.share_rl);
        mExitBtn = (TextView) findViewById(R.id.exit_btn);
        int isPush = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.BAIDU_PUSH);
        if (isPush == -1) {//第一次进入没有配置
            cb_task_reply.setChecked(true);
        } else if (isPush == 0) {//选择不推送
            cb_task_reply.setChecked(false);
        } else { //选择推送
            cb_task_reply.setChecked(true);
        }
        share_rl.setOnClickListener(this);
        cb_task_reply.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked) {
                    PreferenceUtils.putInt(Constants.BAIDU_PUSH, 1);
                    PushManager.resumeWork(MyApplication.getInstance());
                } else {
                    PushManager.stopWork(MyApplication.getInstance());
                    PreferenceUtils.putInt(Constants.BAIDU_PUSH, 0);
                }
            }
        });

        rl_app_update.setOnClickListener(this);
        close_push_rl.setOnClickListener(this);
        if (BaseConfig.isDebug()||"U0736".equals(CommonUtil.getEmcode())){
            close_push_rl.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    startActivity(new Intent(ct,SelectIpActivity.class));
                    return false;
                }
            });
        }
        version_value.setText(CommonUtil.ApkVersionCode(this));
        mExitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent("com.usoftchina.pay.MainMusicActivity"));
                PopupWindowHelper.showAlart(SettingActivity.this, getString(R.string.app_name), getString(R.string.exit_tips), new PopupWindowHelper.OnSelectListener() {
                    @Override
                    public void select(boolean selectOk) {
                        if (selectOk) {
                            UserSp.getInstance(mContext).clearUserInfo();
                            ViewUtil.clearAccount(mContext);
                            LoginHelper.broadcastLogout(mContext);
                            SettingActivity.this.finish();
                        }
                    }
                });
            }
        });
        mCacheTv = (TextView) findViewById(R.id.cache_tv);
        findViewById(R.id.clear_cache_rl).setOnClickListener(this);
        findViewById(R.id.use_help_rl).setOnClickListener(this);
        findViewById(R.id.about_us_rl).setOnClickListener(this);
        String path = MyApplication.getInstance().mAppDir;
        if (StringUtil.isEmpty(path)) {
            path = getCacheDir().getPath();
        }
        long cacheSize = FileUtils.getFileSize(new File(path));
        mCacheTv.setText(FileUtils.formatFileSize(cacheSize));
        uas_website_tv.setText(CommonUtil.getSharedPreferences(this, "erp_baseurl"));


        //UU开关的 根据StepService是否开启显示状态
        int isStep = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.UU_STEP);
        if (isServiceRunning(newStep_service_name) && (isStep == -1 || isStep == 1)) {
            uu_step_reply.setChecked(true);
        } else {
            speech_recognition_rl.setVisibility(View.GONE);
            uu_step_reply.setChecked(false);
        }

        uu_step_reply.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                tv_menu_setting.setVisibility(View.GONE);
                CommonUtil.putSharedPreferencesBoolean(SettingActivity.this, Constants.NEW_UURUN, true);
                if (isChecked) {
                    PreferenceUtils.putInt(Constants.UU_STEP, 1);
//                    PushManager.resumeWork(MyApplication.getInstance());
//
                    startActivity(new Intent(mContext, StepSplashActivity.class));
                } else {
//                    PushManager.stopWork(MyApplication.getInstance());
                    PreferenceUtils.putInt(Constants.UU_STEP, 0);
//                    stopService(intent);
                    Toast.makeText(mContext, getString(R.string.set_close_step_notice1), Toast.LENGTH_LONG).show();
                    speech_recognition_rl.setVisibility(View.GONE);
                }
            }
        });
        uu_step_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_menu_setting.setVisibility(View.GONE);
                CommonUtil.putSharedPreferencesBoolean(SettingActivity.this, Constants.NEW_UURUN, true);
                int last_isStep = PreferenceUtils.getInt(MyApplication.getInstance(), Constants.UU_STEP);
                if (isServiceRunning(newStep_service_name) && last_isStep == 1) {
                    startActivity(new Intent(mContext, NewStepActivity.class));
                } else {
                    Toast.makeText(mContext, getString(R.string.set_close_step_notice2), Toast.LENGTH_SHORT).show();
                }
            }
        });

        new_step_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_menu_setting.setVisibility(View.GONE);
                CommonUtil.putSharedPreferencesBoolean(SettingActivity.this, Constants.NEW_UURUN, true);
                startActivity(new Intent(mContext, NewStepActivity.class));
            }
        });

        booking_set_rl.setOnClickListener(this);

        tv_menu_setting.setVisibility(CommonUtil.getSharedPreferencesBoolean(this, Constants.NEW_UURUN, false) ? View.GONE : View.VISIBLE);
        tv_signauto_new.setVisibility(CommonUtil.getSharedPreferencesBoolean(this, Constants.SET_SIGN_AUTO, false) ? View.GONE : View.VISIBLE);
        tv_sign_new.setVisibility(CommonUtil.getSharedPreferencesBoolean(this, Constants.SET_SIGN_IN, false) ? View.GONE : View.VISIBLE);
        tv_language_new.setVisibility(CommonUtil.getSharedPreferencesBoolean(this, Constants.SET_SIGN_LANGUAGE, false) ? View.GONE : View.VISIBLE);
        tv_booking_new.setVisibility(CommonUtil.getSharedPreferencesBoolean(this, Constants.SET_BOOKING_TIME, false) ? View.GONE : View.VISIBLE);
        tv_help_new.setVisibility(CommonUtil.getSharedPreferencesBoolean(this, Constants.SET_CALL, false) ? View.GONE : View.VISIBLE);

        speech_recognition_rl.setOnClickListener(this);
        sign_in_rl.setOnClickListener(this);
        if (BaseConfig.isDebug()) {
            sign_in_rl.setOnLongClickListener(mTestOnLongClickListener);
        }
        sign_out_rl.setOnClickListener(this);
        language_us_rl.setOnClickListener(this);

        if (ApiUtils.getApiModel() instanceof ApiPlatform) {
            language_us_rl.setVisibility(View.GONE);
        } else {
            language_us_rl.setVisibility(View.VISIBLE);
        }
//        UserRoleUtils.checkUserRole(this,getRootView());
        doShowBSettingJudge();  //判断商家是不是管理员


        font_us_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingActivity.this, FontSizeActivity.class));
            }
        });
    }

    private View.OnLongClickListener mTestOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {

            if (BaseConfig.isDebug()) {
                startActivity(new Intent(ct, LocationTestSetActivity.class));
                return true;
            }
            return false;
        }
    };

    private void doShowBSettingJudge() {
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appCompanyAdmin")
//                .add("companyid",10043574)
//                .add("companyid",201)
//                .add("userid",100254)
//                .add("token",1)
                .add("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_uu"))
                .add("userid", MyApplication.getInstance().mLoginUser.getUserId())
                .add("token", MyApplication.getInstance().mAccessToken)


                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("appCompanyAdmin", o.toString() + "");
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                try {
                    //{"result":"1","url":"http://113.105.74.140:8081/u/0/0/201710/o/48fda5af663f40f795f2dd49e2d8801f.jpg"}
                    if (o.toString().contains("result")) {
//                        if (!CommonUtil.isReleaseVersion()) {
//                            businessmen_setting_rl.setVisibility(View.VISIBLE);
//                        } else

                        if ("1".equals(JSON.parseObject(o.toString()).getString("result"))) {
                            businessmen_setting_rl.setVisibility(View.VISIBLE);
                        } else {
                            businessmen_setting_rl.setVisibility(View.GONE);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

    }

    private boolean isServiceRunning(String servicename) { // 判断某个服务是否已经运行
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (servicename.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent("com.modular.main.MainActivity"));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent("com.modular.main.MainActivity"));
        super.onBackPressed();
    }


    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == R.id.booking_set_rl) {
            startActivity(new Intent(this, BookingSetActivity.class));
            CommonUtil.setSharedPreferences(this, Constants.SET_BOOKING_TIME, true);
            tv_booking_new.setVisibility(View.GONE);
        } else if (v.getId() == R.id.language_us_rl) {
            startActivity(new Intent(this, SelectLanguageActivity.class));
            CommonUtil.setSharedPreferences(this, Constants.SET_SIGN_LANGUAGE, true);
            tv_language_new.setVisibility(View.GONE);
        } else if (v.getId() == R.id.sign_in_rl) {
            showPopupWindow();
            CommonUtil.setSharedPreferences(this, Constants.SET_SIGN_IN, true);
            tv_sign_new.setVisibility(View.GONE);
        } else if (v.getId() == R.id.sign_out_rl) {
            intent = new Intent(mContext, MissionSetActivity.class);
            intent.putExtra(AppConfig.IS_ADMIN, PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false));
            startActivity(intent);
            CommonUtil.setSharedPreferences(this, Constants.SET_SIGN_AUTO, true);
            tv_signauto_new.setVisibility(View.GONE);
        } else if (v.getId() == R.id.clear_cache_rl) {
            showclearDialog();
        } else if (v.getId() == R.id.use_help_rl) {
            CommonUtil.setSharedPreferences(this, Constants.SET_CALL, true);
            tv_help_new.setVisibility(View.GONE);
            intent = new Intent(mContext, FeedbackActivity.class);
            intent.putExtra("type", 1);
            intent.putExtra(AppConstant.EXTRA_URL, mConfig.help_url);
            intent.putExtra(AppConstant.EXTRA_TITLE, getString(R.string.use_help));
            startActivity(intent);
        } else if (v.getId() == R.id.about_us_rl) {
            startActivity(new Intent(mContext, AboutActivity.class));
          // startActivity(new Intent("com.modular.work.ContactsListActivity"));
        } else if (v.getId() == R.id.share_rl) {
            share();
        } else if (v.getId() == R.id.rl_app_update) {
            if (!NetUtils.isNetWorkConnected(MyApplication.getInstance())) {
                ToastUtil.showToast(MyApplication.getInstance(), R.string.networks_out);
            } else {
                dialog.setMessage(getString(R.string.set_check_update_wait));
                dialog.show();

                BDAutoUpdateSDK.cpUpdateCheck(this, new MyCPCheckUpdateCallback());
//                BDAutoUpdateSDK.uiUpdateAction(this, new UICheckUpdateCallback() {
//                    @Override
//                    public void onNoUpdateFound() {
//                        PopupWindowHelper.showAlart(SettingActivity.this,
//                                getString(R.string.app_dialog_title), getString(R.string.set_isnewVersion)
//                                , new PopupWindowHelper.OnSelectListener() {
//                                    @Override
//                                    public void select(boolean selectOk) {
//                                      dialog.dismiss();
//                                    }
//                                });
//                    }
//
//                    @Override
//                    public void onCheckComplete() {
//                        if (DialogUtils.isDialogShowing(dialog)){
//                            dialog.dismiss();
//                        }
//                  BDAutoUpdateSDK.cpUpdateCheck(SettingActivity.this,
//                                new CPCheckUpdateCallback() {
//
//                                    @Override
//                                    public void onCheckUpdateCallback(
//                                            AppUpdateInfo info,
//                                            AppUpdateInfoForInstall infoForInstall) {
//                                        if (infoForInstall != null
//                                                && !TextUtils
//                                                .isEmpty(infoForInstall
//                                                        .getInstallPath())) {
//                                            
//                                        } else if (info != null) {
//                                           
//                                        } else {
//                                            PopupWindowHelper.showAlart(SettingActivity.this,
//                                                    getString(R.string.app_dialog_title), getString(R.string.set_isnewVersion)
//                                                    , new PopupWindowHelper.OnSelectListener() {
//                                                        @Override
//                                                        public void select(boolean selectOk) {
//
//                                                        }
//                                                    });
//                                        }
//                                      
//                                    }
//                                });
//                        
//                    }
//                });
            }

        } else if (v.getId() == R.id.super_setting_tv) {
            startActivityForResult(new Intent(mContext, SignSeniorSettingActivity.class), 0x12);
            closePopupWindow();
        } else if (v.getId() == R.id.work_setting_tv) {
            intent = new Intent(mContext, FlightsActivity.class);
            startActivityForResult(intent, 0x12);
            closePopupWindow();
        }else if (v.getId()==R.id.face_setting_tv){
            intent = new Intent(mContext, FaceManageActivity.class);
            startActivityForResult(intent, 0x13);
            closePopupWindow();
        }else if (v.getId() == R.id.office_addr_setting_tv) {
            startActivityForResult(new Intent(mContext, OfficeAddressSettingsActivity.class), 0x12);
            closePopupWindow();
        } else if (v.getId() == R.id.my_rule_setting_tv) {
            intent = new Intent(mContext, MyRuleSetActivity.class);
            List<WorkModel> models = WorkModelDao.getInstance().queryAuto();
            intent.putParcelableArrayListExtra("data", (ArrayList<WorkModel>) models);
            intent.putExtra("isFree", false);
            intent.putExtra("day", OAConfig.days);
            intent.putExtra("name", OAConfig.name);
            startActivity(intent);
            closePopupWindow();
        } else if (v.getId() == R.id.cancel_tv) {
            closePopupWindow();
        } else if (v.getId() == R.id.businessmen_setting_rl) {
            startActivity(new Intent(this, BSettingActivity.class));
            PreferenceUtils.putBoolean(Constants.B_SETTINGRED, true);
            bsettingRed.setVisibility(View.GONE);
        }
    }

    private void share() {
        shareSingleImage();
    }

    //分享单张图片
    public void shareSingleImage() {
        Uri imageUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.drawable.ic_uu_scan_code);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, getString(R.string.set_share_to)));
    }

    //确认是否清空本地缓存
    private void showclearDialog() {
        PopupWindowHelper.showAlart(this, getString(R.string.common_notice),
                getString(R.string.cache_msg), new PopupWindowHelper.OnSelectListener() {
                    @Override
                    public void select(boolean selectOk) {
                        if (selectOk) {
                            clearCache();
                        }
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void clearCache() {
        String filePath = MyApplication.getInstance().mAppDir;
        //删除文件
        VideoFileDao.getInstance().deleteAllVideoFile(
                VideoFileDao.getInstance().getVideoFiles(
                        MyApplication.getInstance().mLoginUser.getUserId()));
        if (Build.VERSION.SDK_INT >= 11)
            new ClearCacheAsyncTaska(filePath).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
        else
            new ClearCacheAsyncTaska(filePath).execute(true);
    }

    private class ClearCacheAsyncTaska extends AsyncTask<Boolean, String, Integer> {
        private File rootFile;
        private ProgressDialog progressDialog;
        private int filesNumber = 0;
        private boolean canceled = false;

        public ClearCacheAsyncTaska(String filePath) {
            this.rootFile = new File(filePath);
        }

        @Override
        protected void onPreExecute() {
            filesNumber = FileUtils.getFolderSubFilesNumber(rootFile);
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.deleteing));
            progressDialog.setMax(filesNumber);
            progressDialog.setProgress(0);
            // 设置取消按钮
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int i) {
                    canceled = true;
                }
            });
            progressDialog.show();
        }

        /**
         * 返回true代表删除完成，false表示取消了删除
         */
        @Override
        protected Integer doInBackground(Boolean... params) {
            if (filesNumber == 0) {
                return 0;
            }
            Log.i("gongpengming", "doInBackground");
            boolean deleteSubFolder = params[0];// 是否删除已清空的子文件夹
            return deleteFolder(rootFile, true, deleteSubFolder, 0);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // String filePath = values[0];
            int progress = Integer.parseInt(values[1]);
            // progressDialog.setMessage(filePath);
            progressDialog.setProgress(progress);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (!canceled && result == filesNumber) {
                ToastUtil.showToast(mContext, R.string.clear_completed);
            }
            long cacheSize = FileUtils.getFileSize(rootFile);
            mCacheTv.setText(FileUtils.formatFileSize(cacheSize));
        }

        private long notifyTime = 0;

        /**
         * 是否删除完毕
         *
         * @param file
         * @param deleteSubFolder
         * @return
         */
        private int deleteFolder(File file, boolean rootFolder, boolean deleteSubFolder, int progress) {
            if (file == null || !file.exists() || !file.isDirectory()) {
                return 0;
            }
            File flist[] = file.listFiles();
            for (File subFile : flist) {
                if (canceled) {
                    return progress;
                }
                if (subFile.isFile()) {
                    subFile.delete();
                    progress++;
                    long current = System.currentTimeMillis();
                    if (current - notifyTime > 200) {// 200毫秒更新一次界面
                        notifyTime = current;
                        publishProgress(subFile.getAbsolutePath(), String.valueOf(progress));
                    }
                } else {
                    progress = deleteFolder(subFile, false, deleteSubFolder, progress);
                    if (deleteSubFolder) {
                        subFile.delete();
                    }
                }
            }
            return progress;
        }
    }

    private void showPopupWindow() {
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.7f);
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(mContext).inflate(R.layout.pop_work_activity, null);
        if (!PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false)) {
            viewContext.findViewById(R.id.super_setting_tv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.work_setting_tv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.office_addr_setting_tv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.face_setting_tv).setVisibility(View.GONE);
            viewContext.findViewById(R.id.face_setting_line).setVisibility(View.GONE);
            viewContext.findViewById(R.id.super_setting_line).setVisibility(View.GONE);
            viewContext.findViewById(R.id.work_setting_line).setVisibility(View.GONE);
            viewContext.findViewById(R.id.office_addr_setting_line).setVisibility(View.GONE);
        } else {
            viewContext.findViewById(R.id.super_setting_tv).setOnClickListener(this);
            viewContext.findViewById(R.id.work_setting_tv).setOnClickListener(this);
            viewContext.findViewById(R.id.face_setting_tv).setOnClickListener(this);
            viewContext.findViewById(R.id.office_addr_setting_tv).setOnClickListener(this);
        }
        viewContext.findViewById(R.id.my_rule_setting_tv).setOnClickListener(this);
        viewContext.findViewById(R.id.cancel_tv).setOnClickListener(this);
        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }

    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }

    private PopupWindow popupWindow;

    public void showExitPop() {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_simple_dialog, null);
            TextView tv_title = view.findViewById(R.id.tv_title);
            TextView tv_content = view.findViewById(R.id.tv_content);
            TextView tv_cancel = view.findViewById(R.id.tv_cancel);
            TextView tv_sure = view.findViewById(R.id.tv_sure);
            tv_content.setText(R.string.exit_tips);
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() - DensityUtil.dp2px(50), LinearLayout.LayoutParams.MATCH_PARENT);
            tv_sure.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();

                }
            });
            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    popupWindow.dismiss();
                }
            });

        }

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(activity, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }


    /**
     * @desc:百度自动更新 下载回调
     * @author：Arison on 2018/8/9
     */
    private static class UpdateDownloadCallback implements CPUpdateDownloadCallback {

        @Override
        public void onDownloadComplete(String apkPath) {
            try {
                NotifyUtil.cancelAll();
                BDAutoUpdateSDK.cpUpdateInstall(MyApplication.getInstance(), apkPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onPercent(int percent, long rcvLen, long fileSize) {
            LogUtil.d("SettingActivity", "percent:" + percent + "fileSize:" + Integer.valueOf(String.valueOf(fileSize)) + " rcvLen：" + rcvLen);
            NotifyUtil.buildProgress(102, R.drawable.uuu, "正在下载,共" + byteToMb(fileSize), percent, 100).show();
        }

        @Override
        public void onFail(Throwable error, String content) {

        }

        @Override
        public void onStop() {

        }

    }

    private static String byteToMb(long fileSize) {
        float size = ((float) fileSize) / (1024f * 1024f);
        return String.format("%.2fMB", size);
    }


    /**
     * @desc:
     * @author：Arison on 2018/8/9
     */
    public class MyCPCheckUpdateCallback implements CPCheckUpdateCallback {

        @Override
        public void onCheckUpdateCallback(final AppUpdateInfo info, AppUpdateInfoForInstall infoForInstall) {
            if (infoForInstall != null && !TextUtils.isEmpty(infoForInstall.getInstallPath())) {
                BDAutoUpdateSDK.cpUpdateInstall(getApplicationContext(), infoForInstall.getInstallPath());
            } else if (info != null) {
                showUpdateVersionPopup(SettingActivity.this, info);
            } else {
                PopupWindowHelper.showAlart(SettingActivity.this,
                        getString(R.string.app_dialog_title), getString(R.string.set_isnewVersion)
                        , new PopupWindowHelper.OnSelectListener() {
                            @Override
                            public void select(boolean selectOk) {
                                dialog.dismiss();
                            }
                        });
            }
            dialog.dismiss();
        }
    }


    public static PopupWindow updatePopupWindow = null;

    public static PopupWindow showUpdateVersionPopup(final Activity activity, final AppUpdateInfo info) {
        updatePopupWindow = null;
        View view = null;
        Button bt_update;
        Button bt_noUpdate;
        TextView tv_apkVersion;
        TextView tv_update_content;
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if (updatePopupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_update_version, null);
            bt_update = view.findViewById(R.id.bt_update);
            bt_noUpdate = view.findViewById(R.id.bt_noUpdate);
            tv_update_content = view.findViewById(R.id.tv_update_content);
            tv_apkVersion = view.findViewById(R.id.tv_apkVersion);
            tv_apkVersion.setText(SystemUtil.getVersionName(activity) + "-->v" + info.getAppVersionName() + "/" + byteToMb(info.getAppSize()));
            tv_update_content.setText(info.getAppChangeLog());
            bt_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePopupWindow.dismiss();
                    ToastUtil.showToast(MyApplication.getInstance(), "开始下载...");
                    BDAutoUpdateSDK.cpUpdateDownload(activity, info, new UpdateDownloadCallback());
                }
            });

            bt_noUpdate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updatePopupWindow.dismiss();
                }
            });
            updatePopupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() - DensityUtil.dp2px(50), LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        updatePopupWindow.setFocusable(true);
        updatePopupWindow.setOutsideTouchable(true);
        updatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(activity, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(activity, 0.5f);
        updatePopupWindow.setBackgroundDrawable(new BitmapDrawable());
        updatePopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        updatePopupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.CENTER, 0, 0);

        return updatePopupWindow;
    }


}
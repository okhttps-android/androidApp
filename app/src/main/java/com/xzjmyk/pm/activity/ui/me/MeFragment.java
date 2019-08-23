package com.xzjmyk.pm.activity.ui.me;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.preferences.RedSpUtil;
import com.common.system.DisplayUtil;
import com.common.system.PermissionUtil;
import com.core.adapter.ItemPopListAdapter;
import com.core.adapter.ItemsSelectType1;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUAS;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.EasyFragment;
import com.core.broadcast.MsgBroadcast;
import com.core.net.http.ViewUtil;
import com.core.net.utils.NetUtils;
import com.core.utils.DialogUtils;
import com.core.utils.FlexJsonUtil;
import com.core.utils.StatusBarUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.view.Activity.CommonWebviewActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnPlayListener;
import com.modular.apputils.utils.playsdk.AliPlay;
import com.scwang.smartrefresh.layout.util.DensityUtil;
import com.uas.appme.other.activity.WorkCardActivity;
import com.uas.appme.other.model.Master;
import com.uas.appme.settings.activity.BaseInfoActivity;
import com.uas.appme.settings.activity.CheckWagesActivity;
import com.uas.appme.settings.activity.SystemAdminActivity;
import com.uas.appme.widget.MasterDialog;
import com.uas.appworks.crm3_0.activity.CustomerManageActivity;
import com.uas.appworks.crm3_0.activity.CustomerVisitActivity;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.xzjmyk.pm.activity.CaptureResultActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.MainActivity;
import com.xzjmyk.pm.activity.ui.circle.BusinessCircleActivity;
import com.xzjmyk.pm.activity.util.im.UserRoleUtils;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.xzjmyk.pm.im.audio.FaceRecognition.FaceView.OnlineFaceDemo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.xzjmyk.pm.activity.util.oa.CommonUtil.getSharedPreferencesBoolean;


public class MeFragment extends EasyFragment implements View.OnClickListener, OnPlayListener {
    private final String TAG = "MeFragment";

    private final int REQUEST_CODE = 11;
    private CircleImageView mAvatarImg;
    private TextView mNickNameTv;
    private TextView mPhoneNumTv;
    private RelativeLayout my_qr_code_rl;
    private RelativeLayout picture_selector_rl;
    private RelativeLayout app_friend_rl;
    private ImageView iv_desc_oa;
    private TextView tv_b2b_login;
    private RelativeLayout rl_company_change;
    private RelativeLayout rl_master_change;
    private RelativeLayout app_drafts_rl;
    private RelativeLayout mAdminLayout;
    private TextView company_tv;
    private ImageView iv_remain;
    private TextView master_tv;
    private TextView tv_menu_setting;
    private TextView tv_oa_desc;

    public MasterDialog mDialog;

    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    if (JSONUtil.validate(msg.getData().getString("result"))) {
                        showToact("系统内部错误！");
                    } else {
                        showToact(msg.getData().getString("result"));
                    }

                    break;
                case 1://获取中文账套
                    //账套信息需要做缓存 master list 数据  存 key  erp_masterlist
                    //公司信息已经做了缓存，存key  loginJson
                    String result = msg.getData().getString("result"); //解析账套信息
                    boolean isJsonStr = JSONUtil.validate(result);
                    if (isJsonStr) {
                        Map<Object, Object> resultsMap = FlexJsonUtil.fromJson(result);
                        List<Master> mList = FlexJsonUtil.fromJsonArray(
                                FlexJsonUtil.toJson(resultsMap.get("masters")),
                                Master.class);
                        getChMaster(mList);
                    } else {
                        showToact("获取账套信息失败!");
                    }
                    break;
                case 2://切换账套
                    result = msg.getData().getString("result");
                    Map<Object, Object> resultsMap = FlexJsonUtil.fromJson(result);
                    List<Master> mList = FlexJsonUtil.fromJsonArray(FlexJsonUtil.toJson(resultsMap.get("masters")), Master.class);
                    CommonUtil.setSharedPreferences(ct, "erp_masterlist", JSON.toJSONString(resultsMap.get("masters")));
                    List<ItemsSelectType1> selectType1s = new ArrayList<>();
                    if (!ListUtils.isEmpty(mList)) {
                        for (int i = 0; i < mList.size(); i++) {
                            ItemsSelectType1 itemsSelectType1 = new ItemsSelectType1();
                            itemsSelectType1.setName(mList.get(i).getMa_function());
                            itemsSelectType1.setEn_name(mList.get(i).getMa_user());
                            selectType1s.add(itemsSelectType1);
                        }
                    }
                    showPopDialog(getActivity(), selectType1s);
//                    if (CommonUtil.isDialogShowing(mDialog)) {
//                        return;
//                    }
//                    if (!((Activity) ct).isFinishing()) {
//                        mDialog = new MasterDialog(ct, "切换账套",
//                                new MasterDialog.PickDialogListener() {
//                                    @Override
//                                    public void onListItemClick(int position, final Master master) {
//                                        Login(master);
//                                    }
//                                });
//                        mDialog.show();
//                        mDialog.initViewData(mList);
//                    }
                    break;
                case 3://切换账套登录
                    String message = msg.getData().getString("result");
                    Map<String, Object> results = FlexJsonUtil.fromJson(message);
                    if ((Boolean) results.get("success")) {
                        showToact("账套切换成功！");
                        String sessionId = results.get("sessionId").toString();
                        String master = msg.getData().getString("master");
                        String master_ch = msg.getData().getString("master_ch");
                        //添加获取报表地址
                        String en_admin = JSONUtil.getText(message, "EN_ADMIN");
                        String extrajaSperurl = JSONUtil.getText(message,
                                "jasper".equals(en_admin) ? "EN_URL" : "EN_EXTRAJASPERURL");
                        CommonUtil.setSharedPreferences(ct, "extrajaSperurl", extrajaSperurl);
                        LogUtil.i("extrajaSperurl=" + extrajaSperurl);
                        CommonUtil.setSharedPreferences(ct, "erp_uu", String.valueOf(results.get("uu")));
                        CommonUtil.setSharedPreferences(ct, "sessionId", sessionId);
                        CommonUtil.setSharedPreferences(ct, "erp_master", master);
                        CommonUtil.setSharedPreferences(ct, "Master_ch", master_ch);
                        master_tv.setText(master_ch);
                        // 发送给主页刷新的广播
                        Intent intent = new Intent(MsgBroadcast.ACTION_MSG_COMPANY_UPDATE);
                        intent.putExtra("falg", "home");
                        LocalBroadcastManager.getInstance(ct).sendBroadcast(intent);
                    } else {
                        showToact(R.string.user_master_fai);
                    }
                    break;
            }
        }
    };
    private BroadcastReceiver receiverUpdataHead = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("updata".equals(intent.getStringExtra(com.core.app.AppConstant.UPHEAD))) {
                AvatarHelper.getInstance().displayAvatar(MyApplication.getInstance().mLoginUser.getUserId(), mAvatarImg, true);
            }
        }
    };
    private Boolean platform;
    private FrameLayout vg;
    private View workCardRedTv;

    public MeFragment() {
    }


    @Override
    public void onResume() {
        if (!mReceiverTag) {     //在注册广播接受者的时候 判断是否已被注册,避免重复多次注册广播
            IntentFilter inflate = new IntentFilter();
            mReceiverTag = true;
            inflate.addAction(com.core.app.AppConstant.UPHEAD);
            getActivity().registerReceiver(receiverUpdataHead, inflate);
        }
        super.onResume();
        if (mNickNameTv != null) {
            mNickNameTv.setText(MyApplication.getInstance().mLoginUser.getNickName());
        }
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        if (ApiUtils.getApiModel() instanceof ApiUAS) {
            LogUtil.d(TAG, "uas 模式！");
            //getChinaMaster();
            company_tv.setText(CommonUtil.getSharedPreferences(ct, "erp_commpany"));
            // 这样写的目的，是为了初始化本页面的时候，时时根据英文名字来查询当前的中文账套
            if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct, "Master_ch"))) {
                master_tv.setText(CommonUtil.getSharedPreferences(ct, "Master_ch"));
            } else {
                master_tv.setText(CommonUtil.getSharedPreferences(ct, "erp_master"));
            }
            rl_master_change.setVisibility(View.VISIBLE);
            mAdminLayout.setVisibility(View.VISIBLE);
        } else {
            LogUtil.d(TAG, "b2b 模式！");
            company_tv.setText(CommonUtil.getSharedPreferences(ct, "companyName"));
            master_tv.setText(CommonUtil.getSharedPreferences(ct, "spaceId"));
            rl_master_change.setVisibility(View.GONE);
            mAdminLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_me;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            setHasOptionsMenu(true);
            platform = ApiUtils.getApiModel() instanceof ApiPlatform;
            initViewRid();
            initView();

        }
    }


    private void initViewRid() {
        company_tv = findViewById(R.id.company_tv);
        iv_remain = findViewById(R.id.iv_remain);
        master_tv = findViewById(R.id.master_tv);
        tv_menu_setting = findViewById(R.id.tv_menu_setting);
        tv_oa_desc = findViewById(R.id.tv_oa_desc);
        my_qr_code_rl = findViewById(R.id.my_qr_code_rl);
        picture_selector_rl = findViewById(R.id.picture_selector_rl);
        app_friend_rl = findViewById(R.id.app_friend_rl);
        iv_desc_oa = findViewById(R.id.iv_desc_oa);
        tv_b2b_login = findViewById(R.id.tv_b2b_login);
        rl_company_change = findViewById(R.id.rl_company_change);
        rl_master_change = findViewById(R.id.rl_master_change);
        mAdminLayout = findViewById(R.id.me_admin_rl);
        app_drafts_rl = findViewById(R.id.app_drafts_rl);
        mAvatarImg = findViewById(R.id.avatar_img);
        mNickNameTv = findViewById(R.id.nick_name_tv);
        mPhoneNumTv = findViewById(R.id.phone_number_tv);
    }

    private void initView() {
        android.support.v7.widget.Toolbar meToolbar = findViewById(R.id.meToolbar);
        TextView meTitleTv = findViewById(R.id.meTitleTv);
        vg = findViewById(R.id.vg);

        if (meToolbar != null) {
            StatusBarUtil.immersive(getActivity(), 0x00000000, 0.0f);
            ((AppCompatActivity) ct).setSupportActionBar(meToolbar);
            ((AppCompatActivity) ct).getSupportActionBar().setDisplayShowTitleEnabled(false);
            StatusBarUtil.setPaddingSmart(ct, meToolbar);
        }
        meTitleTv.setText(R.string.me);
        //编辑
        findViewById(R.id.editInfoIv).setOnClickListener(this);
        findViewById(R.id.editInfoIv).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                startActivity(new Intent(ct, CustomerVisitActivity.class)
                        .putExtra("caller", "ProjectBusinessChance"));
                return false;
            }
        });
        findViewById(R.id.my_data_rl).setOnClickListener(this);
        View workCardRl = findViewById(R.id.workCardRl);
        workCardRl.setVisibility(View.VISIBLE);
        workCardRl.setOnClickListener(this);
        workCardRedTv = findViewById(R.id.workCardRedTv);
        workCardRedTv.setVisibility(RedSpUtil.api().getBoolean("workCardClicked",false)?View.GONE:View.VISIBLE);

        findViewById(R.id.my_friend_rl).setOnClickListener(this);
        findViewById(R.id.my_space_rl).setOnClickListener(this);
        findViewById(R.id.local_video_rl).setOnClickListener(this);
        findViewById(R.id.setting_rl).setOnClickListener(this);
        app_drafts_rl.setOnClickListener(this);
        app_friend_rl.setOnClickListener(this);
        rl_company_change.setOnClickListener(this);
        rl_master_change.setOnClickListener(this);
        mAdminLayout.setOnClickListener(this);
        IntentFilter loginFilter = new IntentFilter();
        loginFilter.addAction("com.app.home.update");
        LocalBroadcastManager.getInstance(ct).registerReceiver(updateLoginState, loginFilter);
        String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        AvatarHelper.getInstance().displayCircular(loginUserId, mAvatarImg, true, true);
        mNickNameTv.setText(MyApplication.getInstance().mLoginUser.getNickName());
        mPhoneNumTv.setText(MyApplication.getInstance().mLoginUser.getTelephone());

        mAvatarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, AvatarHelper.getAvatarUrl(loginUserId, false));
                ct.startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
            }
        });
        getChinaMaster();
        updateStatus();
        //TODO UU登入、登出、被杀死时间统计
        findViewById(R.id.time_statistics_rl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ct.startActivity(new Intent(ct, TimeStatisticsActivity.class));
            }
        });
        boolean seting = CommonUtil.getSharedPreferencesBoolean(ct, Constants.NEW_SETING);
        tv_menu_setting.setVisibility(seting ? View.GONE : View.VISIBLE);
        CommonUtil.clearSharedPreferences(ct, "seting");
        CommonUtil.clearSharedPreferences(ct, "seting_1");
        doHITask(); // TODO 测试按钮
        UserRoleUtils.checkUserRole(this, getmRootView());
        iv_remain.post(new Runnable() {
            @Override
            public void run() {
                iv_remain.setVisibility(MainActivity.isUnReadWork ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void doHITask() {
        if (CommonUtil.isReleaseVersion()) {
            my_qr_code_rl.setVisibility(View.GONE);
            picture_selector_rl.setVisibility(View.GONE);
        } else {
            my_qr_code_rl.setVisibility(View.GONE);
            picture_selector_rl.setVisibility(View.GONE);
        }

        my_qr_code_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ct.startActivity(new Intent(ct, OnlineFaceDemo.class));
            }
        });

        my_qr_code_rl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ct.startActivity(new Intent(ct, InfoCodeActivity.class));
                return true;
            }
        });
        picture_selector_rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new HttpClient.Builder("http://qq784602719.imwork.net:43386/")
//                        .isDebug(BaseConfig.isDebug())
//                        .build()
//                        .Api()
//                        .send(new HttpClient.Builder()
//                        .url("wxpay/appPay")
//                        .method(Method.POST)
//                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {
//
//                    @Override
//                    public void onResponse(Object o) {
//                        Log.i(TAG, "Success:" + o.toString());
//                        String message = o.toString();
//                        JSONObject data = JSON.parseObject(JSON.parseObject(o.toString()).getString("data"));
//                        message = data.toJSONString();
//                        Log.i(TAG, "message:" + message);
//                        WxPlay.api().wxPay(ct, message, MeFragment.this);
//                    }
//
//                    @Override
//                    public void onFailure(Object t) {
//                        Log.i(TAG, "Failure:" + t.toString());
//                    }
//                }));


                new HttpClient.Builder("http://qq784602719.imwork.net:43386/")
                        .isDebug(BaseConfig.isDebug())
                        .build()
                        .Api()
                        .send(new HttpClient.Builder()
                                .url("alipay/appPay")
                                .method(Method.POST)
                                .build(), new ResultSubscriber<>(new Result2Listener<Object>() {

                            @Override
                            public void onResponse(Object o) {
                                Log.i(TAG, "Success:" + o.toString());
                                String message = o.toString();
                                message = JSON.parseObject(o.toString()).getString("data");

                                Log.i(TAG, "message:" + message);
                                AliPlay.api().alipay(getActivity(), message, MeFragment.this);
                            }

                            @Override
                            public void onFailure(Object t) {
                                Log.i(TAG, "Failure:" + t.toString());
                            }
                        }));
                // ct.startActivity(new Intent(ct, DishMainActivity.class));

            }
        });
        picture_selector_rl.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ct.startActivity(new Intent(ct, CheckWagesActivity.class));
                return true;
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_me_scan, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.me_scan) {
            String[] permissions = new String[]{Manifest.permission.CAMERA};
            if (PermissionUtil.lacksPermissions(ct, permissions)) {
                requestPermissions(permissions, PermissionUtil.DEFAULT_REQUEST);
            } else {
                turn2CaptureActivity();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.DEFAULT_REQUEST) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                LogUtil.i("fragment没有获取到权限");
            } else {
                LogUtil.i("fragment已经用户赋予权限获取到权限");
                turn2CaptureActivity();
            }
        }
    }

    private void turn2CaptureActivity() {
        Intent intent = new Intent(getActivity(), CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.workCardRl:
                workCardRedTv.setVisibility(View.GONE);
                RedSpUtil.api().put("workCardClicked",true);
                startActivity(new Intent(ct, WorkCardActivity.class));
                break;
            case R.id.rl_company_change://公司切换
                if (!NetUtils.isNetWorkConnected(ct)) {
                    showToact(R.string.networks_out);
                    return;
                }
                if (DialogUtils.isDialogShowing(ViewUtil.popupWindow)) {
                    ViewUtil.popupWindow.dismiss();
                }
                if (DialogUtils.isDialogShowing(popupWindow)) {
                    popupWindow.dismiss();
                }
                ViewUtil.LoginTask(MyApplication.getInstance().mLoginUser.getTelephone()
                        , CommonUtil.getSharedPreferences(ct, "user_password"), ct);
                break;
            case R.id.editInfoIv://个人资料
                if (BaseConfig.isDebug()) {
                    startActivity(new Intent(ct, CustomerManageActivity.class)
                            .putExtra(Constants.Intents.CALLER, "Sale")
                            .putExtra(Constants.Intents.TITLE, "Sale")
                            .putExtra(Constants.Intents.ID, 0)
                    );
                } else {
                    ct.startActivity(new Intent(getActivity(), BaseInfoActivity.class));
                }
                break;
            case R.id.rl_master_change:
                if (!platform) {
                    changeMaster();
                }
                break;
            case R.id.my_friend_rl:
                setChangerRemain(true);
                MainActivity.isUnReadWork = false;
                Intent intent = new Intent(getActivity(), BusinessCircleActivity.class);
                intent.putExtra(AppConstant.EXTRA_CIRCLE_TYPE, AppConstant.CIRCLE_TYPE_MY_BUSINESS);
                PreferenceUtils.putLong(getActivity(), "TIMEMAIN", System.currentTimeMillis() / 1000);
                ct.startActivity(intent);
                break;
            case R.id.setting_rl:// 设置
                
               // ct.startActivity(new Intent(getActivity(), RnIndexActivity.class));
                
                ct.startActivity(new Intent(getActivity(), CommonWebviewActivity.class)
                .putExtra("scan_url","http://10.1.80.225:8000/#/dashboard/share_key/12dc375e7ced09941519edad44f8cd24b0f3e5286eb3976c7bffe0e04ed55ac6"));
                
//                ct.startActivity(new Intent(getActivity(), SettingActivity.class));
//                CommonUtil.setSharedPreferences(ct, Constants.NEW_SETING, true);
//                tv_menu_setting.setVisibility(View.GONE);
//                tv_oa_desc.setVisibility(View.GONE);
//                iv_desc_oa.setVisibility(View.GONE);
                break;
            case R.id.me_admin_rl://系统管理员
                startActivity(new Intent(getActivity(), SystemAdminActivity.class));
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {// 个人资料更新了
            AvatarHelper.getInstance().displayCircular(MyApplication.getInstance().mLoginUser.getUserId(), mAvatarImg, true);
            mNickNameTv.setText(MyApplication.getInstance().mLoginUser.getNickName());
        }
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            data.setClass(getActivity(), CaptureResultActivity.class);
            ct.startActivity(data);
        }

    }

    //isRead:true 已阅读   false: 未阅读
    public void setChangerRemain(boolean isRead) {
        iv_remain.setVisibility(isRead ? View.GONE : View.VISIBLE);
    }

    //获取中文账套
    public void getChinaMaster() {
        if (ApiUtils.getApiModel() instanceof ApiPlatform)
            return;
        String url = com.core.utils.CommonUtil.getAppBaseUrl(ct) + "mobile/getAllMasters.action";
        Map<String, Object> params = new HashMap<>();
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mhandler, headers, 1, null, null, "get");
    }

    private void getChMaster(List<Master> mList) {
        if (!mList.isEmpty()) {
            String en_master = CommonUtil.getSharedPreferences(ct, "erp_master");
            for (int i = 0; i < mList.size(); i++) {
                Master master = mList.get(i);
                if (master.getMa_user().equals(en_master)) {
                    master_tv.setText(master.getMa_function());
                    if (adapter != null) {
                        selectId = i;
                    }
                    CommonUtil.setSharedPreferences(ct, "Master_ch", master.getMa_function());
                }
            }
        } else {
            String erp_master = CommonUtil.getSharedPreferences(ct, "erp_master");
            master_tv.setText(erp_master);
            CommonUtil.setSharedPreferences(ct, "Master_ch", erp_master);
        }
    }

    //切换账套
    public void changeMaster() {
        if (StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct, "erp_masterlist"))) {
            String url = com.core.utils.CommonUtil.getAppBaseUrl(ct) + "mobile/getAllMasters.action";
            Map<String, Object> params = new HashMap<>();
            params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, params, mhandler, headers, 2, null, null, "get");
        } else {
            String masterlist = CommonUtil.getSharedPreferences(ct, "erp_masterlist");
            List<Master> mList = JSON.parseArray(masterlist, Master.class);
            List<ItemsSelectType1> selectType1s = new ArrayList<>();
            if (!ListUtils.isEmpty(mList)) {
                for (int i = 0; i < mList.size(); i++) {
                    String masterCN = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "Master_ch");
                    if (!StringUtil.isEmpty(masterCN)) {
                        if (masterCN.equals(mList.get(i).getMa_function())) {
                            selectId = i;
                        }
                    }
                    ItemsSelectType1 itemsSelectType1 = new ItemsSelectType1();
                    itemsSelectType1.setName(mList.get(i).getMa_function());
                    itemsSelectType1.setEn_name(mList.get(i).getMa_user());
                    selectType1s.add(itemsSelectType1);
                }
            }
            showPopDialog(getActivity(), selectType1s);
//            if (CommonUtil.isDialogShowing(mDialog)) {
//                return;
//            }
//            mDialog = new MasterDialog(ct, getString(R.string.user_dialog_master),
//                    new MasterDialog.PickDialogListener() {
//                        @Override
//                        public void onListItemClick(int position, final Master master) {
//                            Login(master);
//                        }
//                    });
//            mDialog.show();
//            mDialog.initViewData(mList);
        }
        ;

    }

    //切换登录
    public void Login(Master master) {
        String url = com.core.utils.CommonUtil.getAppBaseUrl(ct) + "mobile/login.action";
        Map<String, Object> params = new HashMap<String, Object>();
        String accountToken = CommonUtil.getSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN);
        params.put("token", accountToken);
        params.put("master", master.getMa_user());
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("master", master.getMa_user());
        bundle.putString("master_ch", master.getMa_function());
        message.setData(bundle);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mhandler, headers, 3, message, bundle, "get");
    }


    public void updateStatus() {
        if (!StringUtil.isEmpty(CommonUtil.getSharedPreferences(ct,
                "user_phone"))) {
            if (getSharedPreferencesBoolean(ct, "erp_login")) {
                company_tv.setText(CommonUtil.getSharedPreferences(ct, "erp_commpany"));
                // master_tv.setText(CommonUtil.getSharedPreferences(ct,"master"));
            }
            if (getSharedPreferencesBoolean(ct, "b2b_login")) {
                tv_b2b_login.setText("已登录");// 已登录
            } else {
                tv_b2b_login.setText("未登录");
            }
        } else {
            company_tv.setText("");
            master_tv.setText("");
            tv_b2b_login.setText("未登录");
        }
    }

    private boolean mReceiverTag = false;   //广播接受者标识

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {   //判断广播是否注册
            mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
            getActivity().unregisterReceiver(receiverUpdataHead);
        }
    }

    private BroadcastReceiver updateLoginState = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.app.home.update")) {
                String falg = intent.getStringExtra("falg");
                platform = ApiUtils.getApiModel() instanceof ApiPlatform;
                if (!"home".equals(falg)) {
                    updateStatus();
                }
                if ("ERP".equals(falg)) {
                    getChinaMaster();
                    company_tv.setText(CommonUtil.getSharedPreferences(ct, "erp_commpany"));
                    master_tv.setText(CommonUtil.getSharedPreferences(ct, "erp_master"));
                    rl_master_change.setVisibility(View.VISIBLE);
                    mAdminLayout.setVisibility(View.VISIBLE);
                }
                if ("B2B".equals(falg)) {
                    company_tv.setText(CommonUtil.getSharedPreferences(ct, "companyName"));
                    master_tv.setText(CommonUtil.getSharedPreferences(ct, "spaceId"));
                    rl_master_change.setVisibility(View.GONE);
                    mAdminLayout.setVisibility(View.GONE);
                }
            }
        }
    };

    @Override
    public void onSuccess(String resultStatus, String resultInfo) {
        showToact("支付成功");
    }

    @Override
    public void onFailure(String resultStatus, String resultInfo) {
        showToact("支付失败");
    }

    public void showToact(int resId) {
        ToastUtil.showToast(ct, resId, vg);
    }

    public void showToact(CharSequence message) {
        ToastUtil.showToast(ct, message, vg);
    }


    public PopupWindow popupWindow = null;
    public int selectId = 0;
    public ItemPopListAdapter adapter;

    public void showPopDialog(final Activity ct, List<ItemsSelectType1> itemsSelectType1s) {
        if (ct == null) return;
        View view = null;
        if (DialogUtils.isDialogShowing(ViewUtil.popupWindow)) {
            ViewUtil.popupWindow.dismiss();
        }
        if (DialogUtils.isDialogShowing(popupWindow)) {
            popupWindow.dismiss();
            popupWindow = null;
        }
        popupWindow = null;
        WindowManager windowManager = (WindowManager) ct.getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) ct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(com.core.app.R.layout.pop_dialog_list, null);
            ListView plist = view.findViewById(com.core.app.R.id.mList);
            TextView tv_title = view.findViewById(R.id.tv_title);
            tv_title.setText("账套选择");
            List<ItemsSelectType1> datas = itemsSelectType1s;
            adapter = new ItemPopListAdapter(ct, datas);
            adapter.setSelectId(selectId);
            plist.setAdapter(adapter);
            plist.setSelection(selectId);
            Drawable drawable = ct.getResources().getDrawable(com.core.app.R.drawable.selector_check_items);
            plist.setSelector(drawable);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    popupWindow.dismiss();
                    selectId = position;
                    adapter.setSelectId(selectId);
                    adapter.notifyDataSetChanged();
                    ItemPopListAdapter.ViewHolder viewHolder = (ItemPopListAdapter.ViewHolder) view.getTag();
                    ItemsSelectType1 model = viewHolder.model;
                    Master master = new Master();
                    master.setMa_user(model.getEn_name());
                    master.setMa_function(model.getName());
                    Login(master);
                }
            });
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() - DensityUtil.dp2px(50), LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(ct, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(ct, 0.5f);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(ct.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }


}

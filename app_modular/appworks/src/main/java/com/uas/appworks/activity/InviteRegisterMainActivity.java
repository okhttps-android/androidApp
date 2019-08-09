package com.uas.appworks.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.system.DisplayUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.activity.BaseMVPActivity;
import com.core.utils.CommonUtil;
import com.core.utils.SpanUtils;
import com.core.widget.ClearEditText;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.uas.appworks.R;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.uas.appworks.view.WorkPlatView;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.Log;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 新的邀请注册主页面
 * @date 2018/3/25 15:34
 */

public class InviteRegisterMainActivity extends BaseMVPActivity<WorkPlatPresenter> implements WorkPlatView, View.OnClickListener, UMShareListener {
    private LinearLayout mInviteCountLl;
    private TextView mInviteCountTv;
    private LinearLayout mRegisterCountLl;
    private TextView mRegisterCountTv;
    private LinearLayout mUnregisterCountLl;
    private TextView mUnregisterCountTv;
    private ClearEditText mEnterpriseEt;
    private ClearEditText mLinkmanEt;
    private ClearEditText mPhoneEt;
    private Button mInviteBtn;
    private String mRegisterUrl;
    private String mEnterpriseText, mLinkmanText, mPhoneText;
    private PopupWindow mEnterprisePopupWindow;
    private TextView mPopTextView, mCancelTextView, mAddPartnerTextView;
    private SpanUtils mSpanUtils;
    private boolean isNeedRefresh = false;

    @Override
    protected int getLayout() {
        return R.layout.activity_invite_register_main;
    }

    @Override
    protected void initView() {
        setTitle(R.string.str_work_invite_register);

        mInviteCountLl = (LinearLayout) $(R.id.invite_register_main_invite_ll);
        mInviteCountTv = (TextView) $(R.id.invite_register_main_invite_tv);
        mRegisterCountLl = (LinearLayout) $(R.id.invite_register_main_register_ll);
        mRegisterCountTv = (TextView) $(R.id.invite_register_main_register_tv);
        mUnregisterCountLl = (LinearLayout) $(R.id.invite_register_main_unregister_ll);
        mUnregisterCountTv = (TextView) $(R.id.invite_register_main_unregister_tv);
        mEnterpriseEt = (ClearEditText) $(R.id.invite_register_main_enterprise_et);
        mLinkmanEt = (ClearEditText) $(R.id.invite_register_main_linkman_et);
        mPhoneEt = (ClearEditText) $(R.id.invite_register_main_phone_et);
        mInviteBtn = (Button) $(R.id.invite_register_main_invite_btn);
        mInviteBtn.setEnabled(false);

        View popView = View.inflate(this, R.layout.pop_invite_register_add_partner, null);
        mPopTextView = (TextView) popView.findViewById(R.id.pop_invite_register_enterprise_tv);
        mCancelTextView = (TextView) popView.findViewById(R.id.pop_invite_register_cancel_tv);
        mAddPartnerTextView = (TextView) popView.findViewById(R.id.pop_invite_register_add_partner_tv);
        mEnterprisePopupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mEnterprisePopupWindow.setAnimationStyle(R.style.MenuAnimationFade);
        mEnterprisePopupWindow.setFocusable(true);
        mEnterprisePopupWindow.setOutsideTouchable(true);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return new WorkPlatPresenter();
    }

    @Override
    protected void initEvent() {
        mInviteCountLl.setOnClickListener(this);
        mRegisterCountLl.setOnClickListener(this);
        mUnregisterCountLl.setOnClickListener(this);
        mInviteBtn.setOnClickListener(this);
        mEnterprisePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(InviteRegisterMainActivity.this, 1f);
            }
        });

        mEnterpriseEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mEnterpriseText = editable.toString().trim();
                checkBtnEnable();
            }
        });

        mPhoneEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPhoneText = editable.toString().trim();
                checkBtnEnable();
            }
        });

        mLinkmanEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mLinkmanText = editable.toString().trim();
                checkBtnEnable();
            }
        });
    }

    private void checkBtnEnable() {
        if (!TextUtils.isEmpty(mEnterpriseText) && !TextUtils.isEmpty(mLinkmanText) && !TextUtils.isEmpty(mPhoneText)) {
            mInviteBtn.setEnabled(true);
        } else {
            mInviteBtn.setEnabled(false);
        }
    }

    @Override
    protected void initData() {
        mRegisterUrl = Constants.ACCOUNT_CENTER_HOST + "register/enterpriseRegistration?inviteUserUU="
                + CommonUtil.getUseruu(mContext)
                + "&inviteSpaceUU=" + CommonUtil.getEnuu(mContext)
                + "&invitationTime=" + DateFormatUtil.long2Str("yyyyMMdd");

        isNeedRefresh = true;
    }

    private void requestData() {
        HttpRequest.getInstance().sendRequest(new ApiPlatform().getBaseUrl(),
                new HttpParams.Builder()
                        .url("public/invitation/count")
                        .method(Method.GET)
                        .addParam("enUU", CommonUtil.getEnuuLong(mContext))
                        .addParam("userUU", CommonUtil.getUseruuLong(mContext))
                        .addParam("userTel", MyApplication.getInstance().mLoginUser.getTelephone())
                        .addParam("businessCode", CommonUtil.getSharedPreferences(mContext, Constants.CACHE.EN_BUSINESS_CODE))
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        hideLoading();
                        if (o != null) {
                            String result = o.toString();
                            if (JSONUtil.validate(result)) {
                                JSONObject resultObject = JSON.parseObject(result);
                                int inviteCount = JSONUtil.getInt(resultObject, "all");
                                int registerCount = JSONUtil.getInt(resultObject, "done");
                                int unregisterCount = JSONUtil.getInt(resultObject, "todo");

                                mInviteCountTv.setText(inviteCount + "");
                                mRegisterCountTv.setText(registerCount + "");
                                mUnregisterCountTv.setText(unregisterCount + "");
                            }
                        }
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        hideLoading();
                        toast(failStr);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (CommonUtil.isNetWorkConnected(mContext)) {
            if (isNeedRefresh) {
                showLoading("");
                requestData();
                isNeedRefresh = false;
            }
        } else {
            toast(R.string.networks_out);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite_register, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.invite_register_statistics) {
            startActivity(EnterpriseInviteStatisticsActivity.class);
        } else if (itemId == R.id.invite_register_share) {
            showSharePop(false);
        } else if (itemId == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void requestSuccess(int what, Object object) {

    }

    @Override
    public void requestError(int what, String errorMsg) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.invite_register_main_invite_btn) {
            if (CommonUtil.isNetWorkConnected(mContext)) {
                if (TextUtils.isEmpty(CommonUtil.getEnuu(this))) {
                    toast("您的企业还未注册优软云，请联系管理员注册企业优软云!");
                } else if (TextUtils.isEmpty(CommonUtil.getUseruu(this))) {
                    toast("您还不是优软云的个人用户，请联系管理员开通！");
                } else {
                    inviteClick();
                }
            } else {
                toast(R.string.networks_out);
            }
        } else {
            Intent intent = new Intent();
            intent.setClass(this, InviteRegisterListActivity.class);
            if (id == R.id.invite_register_main_invite_ll) {
                intent.putExtra(Constants.FLAG.INVITE_REGISTER_LIST_STATE, Constants.FLAG.STATE_INVITE);
            } else if (id == R.id.invite_register_main_register_ll) {
                intent.putExtra(Constants.FLAG.INVITE_REGISTER_LIST_STATE, Constants.FLAG.STATE_REGISTER);
            } else if (id == R.id.invite_register_main_unregister_ll) {
                intent.putExtra(Constants.FLAG.INVITE_REGISTER_LIST_STATE, Constants.FLAG.STATE_UNREGISTER);
            }
            startActivity(intent);
        }
    }

    private void inviteClick() {
        showLoading("");
        HttpRequest.getInstance().sendRequest(new ApiPlatform().getBaseUrl(),
                new HttpParams.Builder()
                        .url("public/invitation/checkEnName")
                        .method(Method.GET)
                        .addParam("name", mEnterpriseText)
                        .addParam("enUU", CommonUtil.getEnuuLong(mContext))
                        .addParam("userUU", CommonUtil.getUseruuLong(mContext))
                        .addParam("businessCode", CommonUtil.getSharedPreferences(mContext, Constants.CACHE.EN_BUSINESS_CODE))
                        .addParam("userTel", MyApplication.getInstance().mLoginUser.getTelephone())
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        hideLoading();
                        Log.d("checkname", o.toString());
                        if (o != null) {
                            String result = o.toString();
                            if (JSONUtil.validate(result)) {
                                JSONArray resultArray = JSON.parseArray(result);
                                if (resultArray != null && resultArray.size() > 0) {
                                    showAddPartnerPop(result);
                                } else {
                                    showSharePop(true);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        hideLoading();
                        Log.d("checkname", failStr);
                    }
                });

    }

    private void showAddPartnerPop(final String enterpriseInfo) {
        DisplayUtil.backgroundAlpha(this, 0.5f);

        String enName = "";
        long enUU = 0;
        if (JSONUtil.validate(enterpriseInfo)) {
            JSONArray resultArray = JSON.parseArray(enterpriseInfo);
            if (resultArray != null && resultArray.size() > 0) {
                JSONObject resultObject = resultArray.getJSONObject(0);
                if (resultObject != null) {
                    enName = JSONUtil.getText(resultObject, "enName");
                    enUU = resultObject.getLongValue("uu");
                }
            }
        }
        mSpanUtils = new SpanUtils();
        SpannableStringBuilder popContent = mSpanUtils.append(enName + "已注册优软云，").append("查看详情").setForegroundColor(Color.RED).setUnderline().create();
        mPopTextView.setText(popContent);

        mPopTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(InviteRegisterMainActivity.this, RegisterDetailActivity.class);
                intent.putExtra(Constants.FLAG.REGISTERED_ENTERPRISE_INFO, enterpriseInfo);
                intent.putExtra(Constants.FLAG.REGISTERED_ENTERPRISE_FLAG, Constants.FLAG.REGISTERED_DETAIL);
                startActivity(intent);
                mEnterprisePopupWindow.dismiss();
            }
        });

        mCancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEnterprisePopupWindow.dismiss();
                DisplayUtil.backgroundAlpha(InviteRegisterMainActivity.this, 1f);
            }
        });

        final long finalEnUU = enUU;
        mAddPartnerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPartner(finalEnUU);
            }
        });

        mEnterprisePopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.CENTER, 0, 0);
    }

    private void addPartner(long enUU) {
        progressDialog.show();
        HttpRequest.getInstance().sendRequest(new ApiPlatform().getBaseUrl(),
                new HttpParams.Builder()
                        .url("public/invitation/addpartner")
                        .method(Method.POST)
                        .addParam("userUU", CommonUtil.getUseruuLong(mContext))
                        .addParam("enUU", CommonUtil.getEnuuLong(mContext))
                        .addParam("userTel", MyApplication.getInstance().mLoginUser.getTelephone())
                        .addParam("businessCode", CommonUtil.getSharedPreferences(mContext, Constants.CACHE.EN_BUSINESS_CODE))
                        .addParam("inviteEnUU", enUU)
                        .addParam("inviteUserName", mLinkmanText)
                        .addParam("inviteUserTel", mPhoneText)
                        .addParam("app", "IM")
                        .build(), new HttpCallback() {
                    @Override
                    public void onSuccess(int flag, Object o) throws Exception {
                        Log.d("addpartnersuc", "success:" + o.toString());
                        progressDialog.dismiss();
                        mEnterprisePopupWindow.dismiss();
                        toast(getString(R.string.apply_hava_send));
                        DisplayUtil.backgroundAlpha(InviteRegisterMainActivity.this, 1f);
                    }

                    @Override
                    public void onFail(int flag, String failStr) throws Exception {
                        progressDialog.dismiss();
                        toast(failStr);
                        Log.d("addpartnerfai", "fail:" + failStr);
                    }
                });
    }

    private void showSharePop(final boolean add) {
        new ShareAction(activity).setDisplayList(
                SHARE_MEDIA.SINA,
                SHARE_MEDIA.QQ,
                SHARE_MEDIA.QZONE,
                SHARE_MEDIA.WEIXIN,
                SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.WEIXIN_FAVORITE)
                .setShareboardclickCallback(new ShareBoardlistener() {
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (share_media.name().equals("WEIXIN")
                                || share_media.name().equals("WEIXIN_CIRCLE")
                                || share_media.name().equals("WEIXIN_FAVORITE")) {
                            if (!isWeixinAvilible(InviteRegisterMainActivity.this)) {
                                Toast.makeText(InviteRegisterMainActivity.this, "您未安装微信", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        if (share_media.name().equals("QQ")
                                || share_media.name().equals("QZONE")) {
                            if (!isQQClientAvailable(InviteRegisterMainActivity.this)) {
                                Toast.makeText(InviteRegisterMainActivity.this, "您未安装QQ", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                        if (!CommonUtil.isNetWorkConnected(mContext)) {
                            toast(R.string.networks_out);
                            return;
                        }

                        if (add) {
                            isNeedRefresh = true;
                            Map<String, Object> params = new HashMap<>();

                            params.put("enuu", CommonUtil.getEnuuLong(mContext));
                            params.put("bussinesscode", CommonUtil.getSharedPreferences(mContext, Constants.CACHE.EN_BUSINESS_CODE));
                            params.put("useruu", CommonUtil.getUseruuLong(mContext));
                            params.put("userTel", MyApplication.getInstance().mLoginUser.getTelephone());
                            params.put("vendname", mEnterpriseText);
                            params.put("vendusertel", mPhoneText);
                            params.put("vendusername", mLinkmanText);
                            params.put("source", "IM");

                            String paramsJson = "";
                            try {
                                paramsJson = JSON.toJSONString(params);
                            } catch (Exception e) {
                                paramsJson = "";
                            }
                            Log.d("invitationaddpar", paramsJson);
                            HttpRequest.getInstance().sendRequest(new ApiPlatform().getBaseUrl(),
                                    new HttpParams.Builder()
                                            .url("public/invitation/add")
                                            .method(Method.POST)
                                            .addParam("jsonStr", paramsJson)
                                            .build(), new HttpCallback() {
                                        @Override
                                        public void onSuccess(int flag, Object o) throws Exception {
                                            Log.d("invitationaddsuc", o.toString());
                                        }

                                        @Override
                                        public void onFail(int flag, String failStr) throws Exception {
                                            Log.d("invitationaddfai", failStr);
                                        }
                                    });
                        }

                        new ShareAction(activity)
                                .setPlatform(share_media)
                                .withTitle("UU互联企业注册")
                                .withText("发现一款超好用的办公助手，邀请您注册使用！")
                                .withMedia(new UMImage(activity, "http://img.my.csdn.net/uploads/201609/30/1475204542_1365.png"))
                                .withTargetUrl(mRegisterUrl)
                                .setCallback(InviteRegisterMainActivity.this)
                                .share();
                    }
                }).open();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    public boolean isWeixinAvilible(Context context) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mm")) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isQQClientAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mobileqq")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onResult(SHARE_MEDIA share_media) {
//        toast(getString(R.string.str_share_success));
    }

    @Override
    public void onError(SHARE_MEDIA share_media, Throwable throwable) {
//        toast(getString(R.string.str_share_fail));

    }

    @Override
    public void onCancel(SHARE_MEDIA share_media) {
//        toast(getString(R.string.str_share_cancel));

    }
}

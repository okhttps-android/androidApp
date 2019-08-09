package com.xzjmyk.pm.activity.ui.erp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.common.preferences.RedSpUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.OA.erp.activity.StatisticsActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.base.EasyFragment;
import com.xzjmyk.pm.activity.ui.erp.activity.ErpMenActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.crm.ClientActivity;
import com.uas.appworks.datainquiry.activity.DataInquiryActivity;
import com.uas.appworks.datainquiry.activity.ReportStatisticsActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.OAActivity;
import com.xzjmyk.pm.activity.util.im.UserRoleUtils;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

/**
 * @desc:工作
 * @author：Administrator on 2016/1/30 16:15
 */
public class WorksFragment extends EasyFragment implements View.OnClickListener {
    private static final String HASHCODE = "WorksFragment";
    private static final String TIME_MILL = "TIMEMILL";
    private String TAG = "WorksFragment";
    private Context ct;

    @ViewInject(R.id.tv_business_num)
    private TextView tv_business_num;
    @ViewInject(R.id.tv_menu_oa)
    private TextView tv_menu_oa;

    @ViewInject(R.id.tv_oa_desc)
    private TextView tv_oa_desc;
    @ViewInject(R.id.tv_desc_business)
    private TextView tv_desc_business;

    @ViewInject(R.id.iv_business_desc)
    private ImageView iv_business_desc;
    @ViewInject(R.id.iv_desc_oa)
    private ImageView iv_desc_oa;

    @ViewInject(R.id.setting_rl)
    private RelativeLayout setting_rl;
    @ViewInject(R.id.rl_uas_sys)
    private RelativeLayout rl_uas_sys;
    @ViewInject(R.id.my_data_rl)
    private RelativeLayout my_data_rl;
    @ViewInject(R.id.goods_find)
    private RelativeLayout goods_find;
    @ViewInject(R.id.work_data_inquiry_rl)
    private RelativeLayout mDataInquiryRl;
    @ViewInject(R.id.work_report_statistics_rl)
    private RelativeLayout mReportStatisticsRl;
    @ViewInject(R.id.my_client_rl)
    private RelativeLayout my_client_rl;
    @ViewInject(R.id.tv_crm)
    private TextView tv_crm;
    @ViewInject(R.id.tv_oa)
    private TextView tv_oa;
    @ViewInject(R.id.show_new_function_ll)
    private LinearLayout show_new_function_ll;
    private Boolean platform;

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_works;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            ct = getActivity();
            initView();
        }
    }


    private void initView() {
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        rl_uas_sys.setOnClickListener(this);
        setting_rl.setOnClickListener(this);
        my_data_rl.setOnClickListener(this);
        my_client_rl.setOnClickListener(this);
        goods_find.setOnClickListener(this);
        mDataInquiryRl.setOnClickListener(this);
        mReportStatisticsRl.setOnClickListener(this);

        mDataInquiryRl.setVisibility(View.VISIBLE);
        mReportStatisticsRl.setVisibility(View.VISIBLE);


        if (CommonUtil.getSharedPreferencesBoolean(MyApplication.getInstance(), Constants.new_oa)) {
            tv_menu_oa.setVisibility(View.GONE);
            tv_oa_desc.setVisibility(View.GONE);
            iv_desc_oa.setVisibility(View.GONE);
        }
        if (CommonUtil.getSharedPreferencesBoolean(MyApplication.getInstance(), Constants.new_business)) {
            tv_business_num.setVisibility(View.GONE);
            tv_desc_business.setVisibility(View.GONE);
            iv_business_desc.setVisibility(View.GONE);
        }
        show_new_function_ll.setOnClickListener(this);
        if (platform) {
            my_client_rl.setVisibility(View.GONE);
        }
        setDataInquiryRed(!RedSpUtil.api().getDataInquiry());
        setReportStatisRed(!RedSpUtil.api().getReportStatis());
       
    }

    @Override
    public void onResume() {
        super.onResume();
        UserRoleUtils.checkUserRole(this, getmRootView());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_rl:
                String phone = CommonUtil.getSharedPreferences(getActivity(), "user_phone");
                String password = CommonUtil.getSharedPreferences(getActivity(), "user_password");
                String b_enuu = CommonUtil.getSharedPreferences(getActivity(), "erp_uu");
                String url = "";
                if (StringUtil.isEmpty(b_enuu)) {
                    url = "http://b2b.usoftchina.com/authen?b_username=" + phone + "&b_password=" + password;
                } else {
                    url = "http://b2b.usoftchina.com/authen?b_username=" + phone + "&b_password=" + password + "&b_enuu=" + b_enuu;
                }
                CommonUtil.setSharedPreferences(MyApplication.getInstance(), Constants.new_business, true);

                tv_business_num.setVisibility(View.GONE);
                tv_desc_business.setVisibility(View.GONE);
                iv_business_desc.setVisibility(View.GONE);
                IntentUtils.webLinks(ct, url, getString(R.string.work_business_me));
                break;
            case R.id.rl_uas_sys:
                if (falg.equals("1")) {
                    getActivity().startActivityForResult(new Intent(ct, OAActivity.class), 0x11);
                    CommonUtil.setSharedPreferences(MyApplication.getInstance(), Constants.new_oa, true);
                    tv_menu_oa.setVisibility(View.GONE);
                    tv_oa_desc.setVisibility(View.GONE);
                    iv_desc_oa.setVisibility(View.GONE);
                } else {
                    startActivity(new Intent(ct, ErpMenActivity.class));
                }
                break;
            case R.id.my_client_rl:
                if (falg.equals("1")) {
                    ct.startActivity(new Intent(ct, ClientActivity.class));
                } else {
                    ToastUtil.showToast(getActivity(), "抱歉，该功能未启用！");
                }
                break;
            case R.id.my_data_rl:
//                ToastUtil.showToast(getActivity(), "抱歉，该功能尚未完善");
                break;
            case R.id.goods_find:
                IntentUtils.webLinks(ct, "http://mall.ubtob.com", getString(R.string.work_business_find));
                break;
            case R.id.show_new_function_ll:
                startActivity(new Intent(getActivity(), StatisticsActivity.class));
                break;
            case R.id.work_data_inquiry_rl:
                RedSpUtil.api().putDataInquiry(true);
                setDataInquiryRed(false);
                startActivity(new Intent(ct, DataInquiryActivity.class));
                break;
            case R.id.work_report_statistics_rl:
                RedSpUtil.api().putReportStatis(true);
                setReportStatisRed(false);
                startActivity(new Intent(ct, ReportStatisticsActivity.class));
                break;
        }
    }

    private String falg = "1";
   /* private void isStartNewApp(int what){
        String url =CommonUtil.getAppBaseUrl(ct)+"mobile/crm/openNewVision.action";
        Map<String, Object> params = new HashMap<>();
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params,mhandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
    }*/
   /* private  String falg="1";
    private Handler mhandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case  Constants.HTTP_SUCCESS_INIT:
                    String result=   msg.getData().getString("result");
                    // {"isopen":[{"STATUS":"1"}],"success":true}
                    falg= JSON.parseObject(result).getJSONArray("isopen").getJSONObject(0).getString("STATUS");
                  if (falg.equals("1")){
                      tv_crm.setText("客户管理");
                      tv_oa.setText("办公自动化");
                  } else{
                      tv_crm.setText("客户管理");
                      tv_oa.setText("办公自动化");
                  }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    tv_crm.setText("客户管理");
                    tv_oa.setText("办公自动化");
                    falg="0";
                    break;
            }
        }
    };*/


    public void setDataInquiryRed(boolean showAble) {
        findViewById(R.id.work_data_inquiry_rv).setVisibility(showAble ? View.VISIBLE : View.GONE);
    }

    public void setReportStatisRed(boolean showAble) {
        findViewById(R.id.work_report_statistics_rv).setVisibility(showAble ? View.VISIBLE : View.GONE);
    }

}
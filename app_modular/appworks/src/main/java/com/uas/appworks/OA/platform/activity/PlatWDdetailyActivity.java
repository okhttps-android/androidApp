package com.uas.appworks.OA.platform.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.OAConfig;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.CustomerScrollView;
import com.core.widget.MyListView;
import com.uas.appworks.OA.erp.activity.WorkDailyAddActivity;
import com.uas.appworks.OA.platform.adapter.PlatComAfAdapter;
import com.uas.appworks.OA.platform.model.PlatComAfBean;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FANGlh on 2017/3/9.
 * function:
 */
public class PlatWDdetailyActivity extends BaseActivity {
    private static final int PLAT_DELETE_DAILY = 3091;
    private static final int RESUBMIT_OF_DELETEAPPFL = 3092;
    private static final int DELETE_OF_DELETEAPPFL = 3093;
    public static final int DELETE_SUCCEED = 1219;
    private static final int PLAT_APP_FLOW_NODES = 0x318;
    private static final int COMMON_DOC_DETAILY = 0x319;
    private static final int AGREE_OR_DISAGREE = 0x320;
    private static final int CHANGE_DEAL_MAN = 0x322;
    private static final int DOC_EM_DATA = 0x418;
    private TextView wdd_date;
    private TextView wdd_summary;
    private TextView wdd_plan;
    private TextView wdd_experience;
    private TextView wdd_status;
    private LinearLayout plan_ll;
    private LinearLayout experience_ll;
    private MyListView approval_flowlv;
    private ImageView unsubmit_iv;
    private LinearLayout resanddel_ll;
    private LinearLayout resubmit_ll;
    private LinearLayout delete_ll;
    private TextView resubmit_tv;
    private View hide_above_af;
    private LinearLayout done_task_ll;
    private LinearLayout undo_task_ll;
    private LinearLayout a_and_disa_ll;
    private LinearLayout agree_ll;
    private LinearLayout disagree_ll;
    private LinearLayout common_docui_change_dealman_ll;
    private CustomerScrollView csv_hide;
    private LinearLayout ly_head_display;
    private ImageView photo_im;
    private TextView name_tv;
    private TextView section_tv;
    private RelativeLayout common_docdata_rl;
    private String date;
    private String summary;
    private String wd_status;
    private int mkeyValue;
    private String fromwhere;
    private String mCaller = "WorkDaily";
    private String plan;
    private String experience;
    private String submittype;
    private PlatComAfBean mPlatComAfBean;
    private PlatComAfAdapter myAdapter;
    private List<String> afpeople_names;
    private List<String> af_status;
    private int deal_id;
    private int detail_id;
    private int deal_type = -1;
    private Boolean change = false;
    private DBManager manager = new DBManager(this);
    private String platem_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_platdaily_detail);
        initids();
        setTitle(getString(R.string.daily_detaily));
        initView();
        clickEvent();
    }

    private void initids() {
        wdd_date = (TextView) findViewById(R.id.work_daily_detail_time_tv);
        wdd_summary = (TextView) findViewById(R.id.work_daily_detail_summary_tv);
        wdd_plan = (TextView) findViewById(R.id.work_daily_detail_plan_tv);
        wdd_experience = (TextView) findViewById(R.id.work_daily_detail_experience_tv);
        wdd_date = (TextView) findViewById(R.id.work_daily_detail_status_tv);
        plan_ll = (LinearLayout) findViewById(R.id.work_daily_detail_plan_ll);
        experience_ll = (LinearLayout) findViewById(R.id.work_daily_detail_experience_ll);
        approval_flowlv = (MyListView) findViewById(R.id.work_daily_detail_approval_flow_lv);
        unsubmit_iv = (ImageView) findViewById(R.id.work_daily_unsubmit_iv);
        resubmit_ll = (LinearLayout) findViewById(R.id.common_docui_resubmit_ll);
        resanddel_ll = (LinearLayout) findViewById(R.id.item_common_docui_res_and_del_ll);
        delete_ll = (LinearLayout) findViewById(R.id.common_docui_delete_ll);
        resubmit_tv = (TextView) findViewById(R.id.common_docui_resubmit_tv);
        hide_above_af = findViewById(R.id.hide_above_af);
        done_task_ll = (LinearLayout) findViewById(R.id.work_daily_detail_donetask_ll);
        undo_task_ll = (LinearLayout) findViewById(R.id.work_daily_detail_undotask_ll);
        a_and_disa_ll = (LinearLayout) findViewById(R.id.common_docui_agree_and_change_ll);
        agree_ll = (LinearLayout) findViewById(R.id.common_docui_agree_ll);
        disagree_ll = (LinearLayout) findViewById(R.id.common_docui_disagree_ll);
        common_docui_change_dealman_ll = (LinearLayout) findViewById(R.id.common_docui_change_dealman_ll);
        csv_hide = (CustomerScrollView) findViewById(R.id.csv_hide);
        ly_head_display = (LinearLayout) findViewById(R.id.ly_head_display);
        photo_im = (ImageView) findViewById(R.id.common_docui_photo_img);
        name_tv = (TextView) findViewById(R.id.common_docui_name_tv);
        section_tv = (TextView) findViewById(R.id.common_docui_Section_tv);
        common_docdata_rl = (RelativeLayout) findViewById(R.id.common_docdata_rl);
        wdd_status = (TextView) findViewById(R.id.work_daily_detail_status_tv);

    }

    private void initView() {
        progressDialog.show();
        done_task_ll.setVisibility(View.GONE);
        undo_task_ll.setVisibility(View.GONE);

        afpeople_names = new ArrayList<>();
        af_status = new ArrayList<>();
        //TODO 加载审批流的适配器
        myAdapter = new PlatComAfAdapter(this);
        mPlatComAfBean = new PlatComAfBean();
        approval_flowlv.setAdapter(myAdapter);

        Intent intent = getIntent();
        fromwhere = intent.getStringExtra("fromwhere");
//        fromwhere = "examine_and_approve";
        if (!StringUtil.isEmpty(fromwhere) && "examine_and_approve".equals(fromwhere)) {
            deal_id = intent.getIntExtra("deal_id", -1); // TODO 用来操作：同意，不同意，变更
            detail_id = intent.getIntExtra("detail_id", -1); //
            deal_type = intent.getIntExtra("deal_type", -1);
            resanddel_ll.setVisibility(View.GONE);
            a_and_disa_ll.setVisibility(View.VISIBLE);
            Log.i("deal_id,detail_id", deal_id + "," + detail_id);

            if (deal_type == 1) {  //待审批界面进去传值
                a_and_disa_ll.setVisibility(View.VISIBLE);
            } else {
                a_and_disa_ll.setVisibility(View.GONE);
            }

            if (detail_id != -1) {
                if (MyApplication.getInstance().isNetworkActive()) {
                    loadeapData(detail_id);// 用来获取单据详情数据
                } else {
                    ToastMessage(getResources().getString(R.string.networks_out));
                    hide_above_af.setVisibility(View.GONE);
                    a_and_disa_ll.setVisibility(View.GONE);
                    csv_hide.setVisibility(View.GONE);
                    progressDialog.dismiss();
                }

            } else {
                approval_flowlv.setVisibility(View.GONE);
                hide_above_af.setVisibility(View.GONE);
                progressDialog.dismiss();
                csv_hide.setVisibility(View.GONE);
            }
        } else {
            csv_hide.setVisibility(View.VISIBLE);
            mkeyValue = intent.getIntExtra("ID", -1);
            a_and_disa_ll.setVisibility(View.GONE);
            Log.d("走到了mkeyValue", "重新提交/删除" + mkeyValue);
            initData();  // 个人从列表，录入跳转的详情界面 TODO ： 重新提交/删除
        }
    }

    private void initSureStatue(int mkeyValue) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_doc_detaily_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", mkeyValue);
        param.put("whichpage", 4);
        param.put("enuu", CommonUtil.getMaster());
        param.put("emcode", CommonUtil.getEmcode());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, DOC_EM_DATA, null, null, "get");
    }

    private void loadeapData(int mkeyValue) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_doc_detaily_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", mkeyValue);
        param.put("whichpage", 4);
        param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, COMMON_DOC_DETAILY, null, null, "get");
    }

    private void initData() {
        // 获取、处理、赋值从日报列表界面或者添加日报界面点击跳转传来的数据
        final Intent intent = getIntent();
        date = intent.getStringExtra("Date");
        summary = intent.getStringExtra("Content");
        if (!TextUtils.isEmpty(intent.getStringExtra("WD_Status"))) {
            wd_status = intent.getStringExtra("WD_Status");
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("Plan"))) {
            plan = intent.getStringExtra("Plan");
        } else {
            plan_ll.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(intent.getStringExtra("Experience"))) {
            experience = intent.getStringExtra("Experience");
        } else {
            experience_ll.setVisibility(View.GONE);
        }
        wdd_date.setText(date + "");
        wdd_summary.setText(summary);
        wdd_plan.setText(plan);
        wdd_experience.setText(experience);

        Log.i("platwd_status", wd_status+"");
        if ("已审核".equals(wd_status) || "已审批".equals(wd_status)) {
            wdd_status.setTextColor(getApplicationContext().getResources().getColor(R.color.approval));
            wdd_status.setText(mContext.getString(R.string.status_approved));
            resanddel_ll.setVisibility(View.GONE);
        } else if (wd_status.equals("已提交")) {
            wdd_status.setTextColor(getApplicationContext().getResources().getColor(R.color.no_approval));
            wdd_status.setText(mContext.getString(R.string.status_pending));
            resanddel_ll.setVisibility(View.VISIBLE);
        }

        initSureStatue(mkeyValue); // TODO 仅仅获取单据真正状态
//        else if (wd_status.equals("在录入")){
//            wdd_status.setTextColor(mContext.getResources().getColor(R.color.done_approval));
//            wdd_status.setText("未提交");
//            resanddel_ll.setVisibility(View.VISIBLE);
//            submittype = "unsubmit";
//            resubmit_tv.setText("提交");
//        }

        // 当获取到正确的单据caller和单据id时 请求获取审批流数据
        if (!TextUtils.isEmpty(mCaller) && mkeyValue != -1) {
            progressDialog.dismiss();
            //TODO 请求获取审批流数据
            if (MyApplication.getInstance().isNetworkActive()) {
                loadPlatAppFlow(mCaller, mkeyValue);
            } else {
                ToastMessage(getResources().getString(R.string.networks_out));
            }

        } else {
            approval_flowlv.setVisibility(View.GONE);
            hide_above_af.setVisibility(View.GONE);
        }

    }

    private void loadPlatAppFlow(String mCaller, int mkeyValue) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_app_flow_nodes_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", mkeyValue);
        param.put("caller", "工作日报");
        param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, PLAT_APP_FLOW_NODES, null, null, "get");

    }

    private int agree_type = 0;

    private void clickEvent() {
        resubmit_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("wd_status", wd_status+"");
                if ("已提交".equals(wd_status) || "待审批".equals(wd_status)) {
                    new AlertDialog
                            .Builder(PlatWDdetailyActivity.this)
                            .setTitle(mContext.getString(R.string.common_notice))
                            .setMessage(getString(R.string.daily_resubmit_notice1))
                            .setNegativeButton(mContext.getString(R.string.common_cancel), null)
                            .setPositiveButton(mContext.getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String retype = "reforsub";
                                    doresubmit(retype);
                                }
                            }).show();
                } else {
                    doJumpToAdd(mkeyValue);
                    finish();
                }


            }
        });
        delete_ll.setOnClickListener(new View.OnClickListener() {  //TODO 删除
            @Override
            public void onClick(View v) {

                if ("在录入".equals(wd_status)){
                    new AlertDialog
                            .Builder(PlatWDdetailyActivity.this)
                            .setTitle(mContext.getString(R.string.common_notice))
                            .setMessage(mContext.getString(R.string.delete_notice1))
                            .setNegativeButton(mContext.getString(R.string.common_cancel), null)
                            .setPositiveButton(mContext.getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doPlatDeleteByid(mkeyValue);
                                    progressDialog.show();
                                }
                            }).show();
                }else {
                    new AlertDialog
                            .Builder(PlatWDdetailyActivity.this)
                            .setTitle(mContext.getString(R.string.common_notice))
                            .setMessage(getString(R.string.delete_sumited_notice1))
                            .setNegativeButton(mContext.getString(R.string.common_cancel), null)
                            .setPositiveButton(mContext.getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String retype = "refordelete";
                                    doresubmit(retype);
                                    progressDialog.show();
                                }
                            }).show();
                }

            }
        });

        agree_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree_type = 202;
                doDealDescribe(agree_type, deal_id);

            }
        });

        disagree_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree_type = 203;
                doDealDescribe(agree_type, deal_id);
            }
        });


        common_docui_change_dealman_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSelectDealMan();
            }
        });

    }

    private void doJumpToAdd(int mkeyValue) {
        startActivity(new Intent(activity, WorkDailyAddActivity.class)
                .putExtra("caller", mCaller)
                .putExtra("id", mkeyValue)
                .putExtra("rs_summary", summary)
                .putExtra("rs_plan", plan)
                .putExtra("rs_experience", experience)
                .putExtra("resubmit", "resubmit"));
        Log.i("doresubmit_id", mkeyValue + "");
    }

    private PopupWindow popupWindow = null;

    private void doDealDescribe(final int agree_type, final int deal_id) {
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.common_edit_window, null);

        //设置按钮的点击事件
        final FormEditText deal_describe = (FormEditText) contentView.findViewById(R.id.deal_describe);
        LinearLayout jump_step_ll = (LinearLayout) contentView.findViewById(R.id.jump_step_ll);
        LinearLayout ok_ll = (LinearLayout) contentView.findViewById(R.id.ok_ll);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 300);
        h_screen = DisplayUtil.dip2px(this, 185);
        contentView.findViewById(R.id.jump_step_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String describe = "";
                doAgreeorDisAgree(agree_type, deal_id, describe);
                popupWindow.dismiss();
            }
        });

        contentView.findViewById(R.id.ok_ll).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!StringUtil.isEmpty(deal_describe.getText().toString())) {
                    String describe = deal_describe.getText().toString();
                    doAgreeorDisAgree(agree_type, deal_id, describe);
                    popupWindow.dismiss();
                } else {
                    ToastMessage(getString(R.string.daily_approve_notice1));
                }

            }
        });
        popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_main));
        // 设置好参数之后再show
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(0.4f);
    }

    private void setbg(float alpha) {
        setBackgroundAlpha(this, alpha);
        if (popupWindow == null) return;
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(PlatWDdetailyActivity.this, 1f);
            }
        });
    }

    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    private void doSelectDealMan() {
        Intent intent = new Intent("com.modular.common.SelectDepartmentActivity");
        SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                .setTitle(getString(R.string.select_doman))
                .setSingleAble(true);
        intent.putExtra(OAConfig.MODEL_DATA, bean);
        startActivityForResult(intent, 0x01);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            if (resultCode == 0x333) {
                loadPlatAppFlow(mCaller, detail_id);
                myAdapter.notifyDataSetChanged();
                a_and_disa_ll.setVisibility(View.GONE);
//                ToastMessage("变更成功，刷新审批节点");
                change = data.getBooleanExtra("change", false);
            } else {
                return;
            }
        } else if (requestCode == 0x01 && resultCode == 0x20) {
            SelectEmUser d = data.getParcelableExtra("data");
            if (d != null) {
                String single_man_name = d.getEmName();
                int single_man_emcode = -1;
                String emcode = d.getEmCode();
                try {
                    single_man_emcode = Integer.valueOf(emcode);
                } catch (ClassCastException e) {

                }
                startActivityForResult(new Intent("com.modular.changedealman.ChangeDealManActivit")
                                .putExtra("deal_id", deal_id)
                                .putExtra("single_man_emcode", single_man_emcode)
                                .putExtra("single_man_name", single_man_name)
                        , 0x325);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doAgreeorDisAgree(int agree_type, int deal_id, String describe) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_doc_examine_and_approve_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", deal_id);
        param.put("description", describe);
        param.put("dealtype", agree_type);
        param.put("whichpage", 4);
        param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, AGREE_OR_DISAGREE, null, null, "post");
    }

    // 反提交之操作：清除审批流和更改单据状态
    private void doresubmit(String retype) {
        progressDialog.show();
        HashMap<String, Object> params = new HashMap<>();
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_delete_approval_flow_url;//这个接口仅仅是反提交操作
        Map<String, Object> param = new HashMap<>();
        param.put("id", mkeyValue);
        param.put("whichpage", 4);
        param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        if (retype.equals("reforsub")) {  //反提交之重新提交请求
            ViewUtil.httpSendRequest(this, url, param, handler, headers, RESUBMIT_OF_DELETEAPPFL, null, null, "post");
        } else if (retype.equals("refordelete")) { //反提交之删除请求
            ViewUtil.httpSendRequest(this, url, param, handler, headers, DELETE_OF_DELETEAPPFL, null, null, "post");
        }
    }

    private void doPlatDeleteByid(int mkeyValue) {
        //删除
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().delete_work_daily;
        Map<String, Object> param = new HashMap<>();
        param.put("id", mkeyValue);
        param.put("whichpage", 4);
        param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, PLAT_DELETE_DAILY, null, null, "post");

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PLAT_APP_FLOW_NODES:
                    if (msg.getData() != null) {
                        String app_flow_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("app_flow_result", app_flow_result);

                        doPlatComAfShow(app_flow_result);
                    }
                    break;
                case RESUBMIT_OF_DELETEAPPFL:
                    if (msg.getData() != null) {
                        String r_for_s_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("r_for_s_result", r_for_s_result);
                        if (JSON.parseObject(r_for_s_result).containsKey("success") && JSON.parseObject(r_for_s_result).getBoolean("success")) {
//                            ToastMessage("反提交成功");
                            startActivity(new Intent(activity, WorkDailyAddActivity.class)
                                    .putExtra("caller", mCaller)
                                    .putExtra("id", mkeyValue)
                                    .putExtra("rs_summary", summary)
                                    .putExtra("rs_plan", plan)
                                    .putExtra("rs_experience", experience)
                                    .putExtra("resubmit", "resubmit"));
                            Log.i("doresubmit_id", mkeyValue + "");
                            finish();
                        } else {
                            String error = JSON.parseObject(r_for_s_result).getString("error");
                            ToastMessage(error == null ? "error" : error);
                        }

                    }
                    break;
                case DELETE_OF_DELETEAPPFL:
                    if (msg.getData() != null) {
                        String r_for_d_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("r_for_d_result", r_for_d_result);
//                        ToastMessage("反提交成功");
                        doPlatDeleteByid(mkeyValue);
                    }
                    break;

                case COMMON_DOC_DETAILY: // 审批人进去详情界面进行审批
                    if (msg.getData() != null) {
                        String com_doc_details = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("com_doc_details", com_doc_details);
                        JSONObject dataObject = JSON.parseObject(com_doc_details).getJSONObject("data");
                        if (dataObject != null) {
                            try {
                                mkeyValue = dataObject.getIntValue("wd_id");
                                long plat_date = dataObject.getLongValue("wd_date");
                                date = TimeUtils.s_long_2_str(plat_date);
                                summary = dataObject.getString("wd_comment");
                                plan = dataObject.getString("wd_plan");
                                experience = dataObject.getString("wd_experience");
                                wd_status = dataObject.getString("wd_status");
                                dodetailsShow(date, summary, plan, experience, wd_status);
                                getapplypeomsg(com_doc_details);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastMessage(getString(R.string.doc_detaily_deleted));
                            ly_head_display.setVisibility(View.GONE);
                            a_and_disa_ll.setVisibility(View.GONE);
                            csv_hide.setVisibility(View.GONE);
                        }
                    } else {
                        ly_head_display.setVisibility(View.GONE);
                        a_and_disa_ll.setVisibility(View.GONE);
                        ToastMessage(getString(R.string.doc_detaily_deleted));
                        csv_hide.setVisibility(View.GONE);
                    }
                    Log.d("DailyData", summary + "," + plan + "," + experience + "," + date + "," + wd_status);
                    loadPlatAppFlow(mCaller, mkeyValue);
                    break;
                case PLAT_DELETE_DAILY:
                    if (msg.getData() != null) {
                        String delete_result = msg.getData().getString("result");
                        if (JSON.parseObject(delete_result).containsKey("success") && JSON.parseObject(delete_result).getBoolean("success")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.delete_succeed_notice1), Toast.LENGTH_LONG);
                            if (!TextUtils.isEmpty(fromwhere) && "submitdaily".equals(fromwhere)) {
                                startActivity(new Intent(activity, PlatDailyShowActivity.class));
                                finish();
                            } else if (!TextUtils.isEmpty(fromwhere) && "dailylist".equals(fromwhere)) {
                                Intent intent = new Intent();
                                intent.putExtra("delete_succeed", "delete_succeed");
                                intent.putExtra("deleted_id", mkeyValue);
                                setResult(DELETE_SUCCEED, intent);
                                progressDialog.dismiss();
                                finish();
                            }
                            progressDialog.dismiss();
                            finish();
                        }
                    }
                    break;

                case AGREE_OR_DISAGREE:
                    if (msg.getData() != null) {
                        String agree_or_disagree_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("agree_or_disagree_result", agree_or_disagree_result);
                        Toast.makeText(getApplicationContext(), getString(R.string.make_adeal_success), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent();
                        intent.putExtra("isRemove", true);
                        setResult(0x41701, intent);
                        finish();
                    }
                    break;

                case CHANGE_DEAL_MAN:
                    if (msg.getData() != null) {
                        String change_deal_man_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("change_deal_man_result", change_deal_man_result);
                    }
                    break;

                case DOC_EM_DATA:
                    if (msg.getData() != null) {
                        String doc_em_data = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("doc_em_data", doc_em_data);
                        if (!StringUtil.isEmpty(doc_em_data)) {
                            getDailyStatue(doc_em_data);
//                            getapplypeomsg(doc_em_data);
                            name_tv.setText(platem_name + "");
                            String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                            AvatarHelper.getInstance().display(loginUserId, photo_im, true, false);
                            common_docdata_rl.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                            progressDialog.dismiss();
                        }
                    }
                    break;
            }
        }
    };

    private void getapplypeomsg(String doc_em_data) {
        try {
            JSONObject object = JSON.parseObject(doc_em_data);
            JSONObject dataObject = object.getJSONObject("data");
            String em_code = dataObject.getLongValue("emcode") + "";
            String em_name = dataObject.getString("wd_emp");
            name_tv.setText(em_name + "");
            if (!StringUtil.isEmpty((em_code))) {
                String whichsys = CommonUtil.getMaster();
                String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
                String selection = "em_code=? and whichsys=? ";
                try {
                    //获取数据库数据
                    EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                    if (bean != null) {
                        LogUtil.prinlnLongMsg("bean", JSON.toJSONString(bean));
                        String imId = String.valueOf(bean.getEm_IMID());
                        String imName = String.valueOf(bean.getEM_NAME());
                        String imDepartment = String.valueOf(bean.getEM_DEFAULTORNAME());
                        String imPosition = String.valueOf(bean.getEM_POSITION());

                        name_tv.setText(imName);
                        if (!StringUtil.isEmpty(imId)) {
                            AvatarHelper.getInstance().display(imId, photo_im, true, false);
                        } else {
                            String imageUri = "drawable://" + R.drawable.common_header_boy;
                            AvatarHelper.getInstance().display(imageUri, photo_im, true);
                        }//显示圆角图片
                        section_tv.setText(imDepartment + ">" + imPosition);

                        Log.i("aptodo", "imId=" + imId + "imName" + imName + "imDepartment" + imDepartment + "imPosition" + imPosition);
                    } else {
//                        ToastMessage("单据申请人数据获取异常");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Log.i("em_name,em_code", em_name + "," + em_code);
        } catch (Exception e) {
            e.printStackTrace();
        }

        common_docdata_rl.setVisibility(View.VISIBLE);
    }

    private void getDailyStatue(String doc_em_data) {
        JSONObject Object = JSON.parseObject(doc_em_data);
        JSONObject dataObject = Object.getJSONObject("data");

        if (dataObject != null) {
            if (!StringUtil.isEmpty(dataObject.getString("wd_status"))) {
                wd_status = dataObject.getString("wd_status");
                if ("已审核".equals(wd_status) || "已审批".equals(wd_status)) {
                    wdd_status.setTextColor(getApplicationContext().getResources().getColor(R.color.approval));
                    wdd_status.setText(mContext.getString(R.string.status_approved));
                    resanddel_ll.setVisibility(View.GONE);
                } else if ("已提交".equals(wd_status)) {
                    wdd_status.setTextColor(getApplicationContext().getResources().getColor(R.color.no_approval));
                    wdd_status.setText(mContext.getString(R.string.status_pending));
                    resanddel_ll.setVisibility(View.VISIBLE);
                } else if ("在录入".equals(wd_status)) {
                    wdd_status.setTextColor(getApplicationContext().getResources().getColor(R.color.done_approval));
                    wdd_status.setText(mContext.getString(R.string.status_unsubmit));
                    resanddel_ll.setVisibility(View.VISIBLE);
                }
            }
        }

    }

    private void dodetailsShow(String date, String summary, String plan, String experience, String wd_status) {
        csv_hide.setVisibility(View.VISIBLE);
        resanddel_ll.setVisibility(View.GONE);
        if (!StringUtil.isEmpty(plan)) {
            wdd_plan.setText(plan);
        } else {
            plan_ll.setVisibility(View.GONE);
        }

        if (!StringUtil.isEmpty(experience)) {
            wdd_experience.setText(experience);
        } else {
            experience_ll.setVisibility(View.GONE);
        }
        wdd_status.setText(wd_status);
        wdd_date.setText(date + "");
        wdd_summary.setText(summary);
    }

    private void doPlatComAfShow(String app_flow_result) {
        JSONObject resultJsonObject = JSON.parseObject(app_flow_result);
        if (resultJsonObject == null) {
            approval_flowlv.setVisibility(View.GONE);
            hide_above_af.setVisibility(View.GONE);
        } else {
            mPlatComAfBean = JSON.parseObject(resultJsonObject.toString(), PlatComAfBean.class);
            if (!mPlatComAfBean.isSuccess()) {
                return;
            }
            //TODO 获取数据审批人姓名，头像,审批状态
            if (!ListUtils.isEmpty(mPlatComAfBean.getData()) || !ListUtils.isEmpty(mPlatComAfBean.getNodes())) {
                int datas_size = mPlatComAfBean.getData().size();
                int nodes_size = mPlatComAfBean.getNodes().size();
                if (nodes_size == 0 && datas_size > 0) {
                    for (int i = 0; i < datas_size; i++) {
                        if (!StringUtil.isEmpty(mPlatComAfBean.getData().get(i).getJp_nodename())) {
                            afpeople_names.add(mPlatComAfBean.getData().get(i).getJp_nodename());
                        } else {
                            afpeople_names.add("");
                        }
                    }
                } else if (nodes_size > 0 && nodes_size <= datas_size) {
                    for (int i = 0; i < nodes_size; i++) {
                        if (!StringUtil.isEmpty(mPlatComAfBean.getNodes().get(i).getJn_dealManName())) {
                            afpeople_names.add(mPlatComAfBean.getNodes().get(i).getJn_dealManName());
                        } else {
                            afpeople_names.add("");
                        }
                    }

                    for (int i = nodes_size; i < datas_size; i++) {
                        if (!StringUtil.isEmpty(mPlatComAfBean.getData().get(i).getJp_nodename())) {
                            afpeople_names.add(mPlatComAfBean.getData().get(i).getJp_nodename());
                        } else {
                            afpeople_names.add("");
                        }
                    }
                }

                myAdapter.setAfpeople_names(afpeople_names);
                myAdapter.setmPlatComAfBean(mPlatComAfBean);
                myAdapter.notifyDataSetChanged();
            }

        }

        progressDialog.dismiss();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!TextUtils.isEmpty(fromwhere) && fromwhere.equals("submitdaily")) {
                startActivity(new Intent(activity, PlatDailyShowActivity.class)
                        .putExtra("fromwhere", "dodaily"));
                finish();
            } else {
                if (change) {
                    Intent intent = new Intent();
                    intent.putExtra("isRemove", true);
                    setResult(0x41701, intent);
                }
                finish();
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(fromwhere) && fromwhere.equals("submitdaily")) {
            startActivity(new Intent(activity, PlatDailyShowActivity.class)
                    .putExtra("fromwhere", "dodaily"));
            finish();
        } else {
            super.onBackPressed();
            if (change) {
                Intent intent = new Intent();
                intent.putExtra("isRemove", true);
                setResult(0x41701, intent);
            }
            finish();
        }

    }
}

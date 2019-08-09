package com.xzjmyk.pm.activity.ui.platform.pageforms;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
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
import com.core.net.http.ViewUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.CustomerScrollView;
import com.core.widget.MyListView;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.uas.appworks.OA.platform.adapter.PlatComAfAdapter;
import com.uas.appworks.OA.platform.model.PlatComAfBean;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.PlatLeaveAddActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.TravelActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.WorkExtraActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.SelectCollisionActivity;
import com.xzjmyk.pm.activity.ui.platform.activity.ChangeDealManActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FormDetailActivity extends BaseActivity {
    private static final int RESUBMIT_OF_DELETEAPPFL = 3092;
    private static final int DELETE_OF_DELETEAPPFL = 3093;
    private static final int PLAT_APP_FLOW_NODES = 0x318;
    private static final int COMMON_DOC_DETAILY = 0x319;
    private static final int DELETE_SUCCEED = 0x321;
    private static final int AGREE_OR_DISAGREE = 0x322;
    private static final int DOC_EM_DATA = 0x331;
    @ViewInject(R.id.list_form)
    private PullToRefreshListView mForm;
    @ViewInject(R.id.list_detail)
    private PullToRefreshListView mDetail;
    @ViewInject(R.id.common_approval_flow_lv)
    private MyListView approval_flowlv;
    @ViewInject(R.id.common_docui_resubmit_ll)
    private LinearLayout resubmit_ll;
    @ViewInject(R.id.common_docui_delete_ll)
    private LinearLayout delete_ll;
    @ViewInject(R.id.common_docui_agree_and_change_ll)
    private LinearLayout a_and_disa_ll;
    @ViewInject(R.id.common_docui_agree_ll)
    private LinearLayout agree_ll;
    @ViewInject(R.id.common_docui_disagree_ll)
    private LinearLayout disagree_ll;
    @ViewInject(R.id.item_common_docui_res_and_del_ll)
    private LinearLayout resanddel_ll;
    @ViewInject(R.id.common_docui_change_dealman_ll)
    private LinearLayout change_dealman_ll;
    @ViewInject(R.id.common_docui_main_msg_lv)
    private MyListView cdmain_lv;
    @ViewInject(R.id.common_docui_secondmsg_ll)
    private LinearLayout cdsecong_out_ll;
    @ViewInject(R.id.common_docui_second_msg_lv)
    private MyListView cdsecond_out_lv;
    //    @ViewInject(R.id.doc_msg_ll)
//    private LinearLayout doc_msg_ll;
    @ViewInject(R.id.common_docui_sv)
    private CustomerScrollView common_docui_sv;
    @ViewInject(R.id.hide_above_af)
    private View hide_above_af;
    @ViewInject(R.id.common_docui_status_tv)
    private TextView status_tv;
    @ViewInject(R.id.common_docui_name_tv)
    private TextView doc_em_name_tv;
    @ViewInject(R.id.common_docui_photo_img)
    private ImageView photo_im;
    @ViewInject(R.id.common_docui_Section_tv)
    private TextView section_tv;
    List<LinkedHashMap<String, String>> datas = new ArrayList<>();
    List<LinkedHashMap<String, String>> details = new ArrayList<>();
    private int mkeyValue = -1;
    private int whichpage = -1;
    private String doc_type = "";
    private PlatComAfBean mPlatComAfBean;
    private List<String> afpeople_names;
    private PlatComAfAdapter mPlatComAfAdapter;
    private String fromwhere;
    private String status;
    private int deal_id;
    private int agree_type;
    private List<String> doc_key;
    private List<String> doc_value;
    private List<String> doc_detail_key;
    private List<String> doc_detail_value;
    private PCDMainAdapter mymainAdapter;
    private String detailJson;
    private String dataJson;
    private int detail_id;
    private PlatSecondOutAdapter mySecondOutAdapter;
    private int deal_type = -1;
    private String formwhere;
    private String doc_em_name;
    private int fpd_id;
    private int wod_id;
    private Boolean change = false;
    private DBManager manager = new DBManager(this);
    private String platem_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_form_detail);
        setContentView(R.layout.plat_comdetail_docui);
        ViewUtils.inject(this);
        judgeFromWhere();
    }

    private void judgeFromWhere() {
        progressDialog.show();
        afpeople_names = new ArrayList<>();
        mPlatComAfBean = new PlatComAfBean();
        mPlatComAfAdapter = new PlatComAfAdapter(this);
        approval_flowlv.setAdapter(mPlatComAfAdapter);

        Intent intent = getIntent();
        fromwhere = intent.getStringExtra("fromwhere");
        doc_type = intent.getStringExtra("doc_type");
        whichpage = intent.getIntExtra("whichpage", -1);

        if (!StringUtil.isEmpty(doc_type)) {
            switch (doc_type) {
                case "请假单":
                    whichpage = 1;
                    break;
                case "出差单":
                    whichpage = 2;
                    break;
                case "加班单":
                    whichpage = 3;
                    break;
            }
        }
        String imageUri = "drawable://" + R.drawable.common_header_boy;
        AvatarHelper.getInstance().display(imageUri, photo_im, true);
        if (!StringUtil.isEmpty(fromwhere) && "examine_and_approve".equals(fromwhere)) {
            deal_id = intent.getIntExtra("deal_id", -1);
            detail_id = intent.getIntExtra("detail_id", -1);
            deal_type = intent.getIntExtra("deal_type", -1);
            initEAndAView();
            Log.d("走到了", " 同意/不同意");              //  领导审批时进入的界面   同意/不同意，
            LogUtil.d("deal_id,detail_id,whichpage", deal_id + "," + detail_id + "," + whichpage);
        } else {
            mkeyValue = intent.getIntExtra("ID", -1);
//            initView();  //todo 这个显示方法以后如果有多明细时再用，写的不好看 ：勿删
            initNewView();
            initData();
            initDocEmData(mkeyValue, whichpage);  // TODO 获取单据提交人姓名，职位，头像等，其实和从消息进来的审批界面请求获取详情一样
            Log.d("走到了", "重新提交/删除");  // 个人从列表，录入跳转的详情界面 重新提交/删除,数据从列表或录入界面传入 正常获取
        }

        initClickEvent(); //统一点击事件
    }

    private void initDocEmData(int mkeyValue, int whichpage) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_doc_detaily_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", mkeyValue);
        param.put("whichpage", whichpage);
        param.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
        param.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, DOC_EM_DATA, null, null, "get");
    }


    private void initEAndAView() {
        setTitle(doc_type + "详情");
        resanddel_ll.setVisibility(View.GONE);
        mForm.setVisibility(View.GONE);
        mDetail.setVisibility(View.GONE);
//        doc_msg_ll.setVisibility(View.VISIBLE);
        if (detail_id != -1 && whichpage != -1) {
            if (MyApplication.getInstance().isNetworkActive()) {
                if (deal_type == 1) {  //待审批界面进去传值
                    a_and_disa_ll.setVisibility(View.VISIBLE);
                } else {
                    a_and_disa_ll.setVisibility(View.GONE);
                }
                loadPlatDocDetails(detail_id, whichpage);
            } else {
                ToastMessage(getResources().getString(R.string.networks_out));
                hide_above_af.setVisibility(View.GONE);
                a_and_disa_ll.setVisibility(View.GONE);
                progressDialog.dismiss();
            }
        }
        doc_key = new ArrayList<>();
        doc_value = new ArrayList<>();
        doc_detail_key = new ArrayList<>();
        doc_detail_value = new ArrayList<>();

        mymainAdapter = new PCDMainAdapter();
        mySecondOutAdapter = new PlatSecondOutAdapter();
        cdmain_lv.setAdapter(mymainAdapter);
        cdsecond_out_lv.setAdapter(mySecondOutAdapter);
    }

    private void initClickEvent() {
        resubmit_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("status",status);
                if ("已提交".equals(status) || "待审批".equals(status)) {  // 已提交状态
                    new AlertDialog
                            .Builder(mContext)
                            .setTitle(getString(R.string.common_notice))
                            .setMessage(getString(R.string.daily_resubmit_notice1))
                            .setNegativeButton(getString(R.string.common_cancel), null)
                            .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String retype = "reforsub";
                                    doresubmit(retype);
                                }
                            }).show();
                } else {  // 在录入状态
                    doJumpToAdd(mkeyValue);
                    finish();
                }

            }
        });

        delete_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("在录入".equals(status)){
                    new AlertDialog
                            .Builder(mContext)
                            .setTitle(getString(R.string.common_notice))
                            .setMessage(getString(R.string.delete_notice1))
                            .setNegativeButton(getString(R.string.common_cancel), null)
                            .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    doPlatDeleteByid(mkeyValue);
                                    progressDialog.show();
                                }
                            }).show();
                }else {
                    new AlertDialog
                            .Builder(mContext)
                            .setTitle(getString(R.string.common_notice))
                            .setMessage(getString(R.string.delete_sumited_notice1))
                            .setNegativeButton(getString(R.string.common_cancel), null)
                            .setPositiveButton(getString(R.string.common_sure), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String retype = "refordelete";
                                    doresubmit(retype);
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

        change_dealman_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSelectDealMan();
            }
        });

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
                setBackgroundAlpha(FormDetailActivity.this, 1f);
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

        Intent intent = new Intent(ct, SelectCollisionActivity.class);
        SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                .setTitle("选择执行人")
                .setSingleAble(true);
        intent.putExtra(OAConfig.MODEL_DATA, bean);
        startActivityForResult(intent, 0x01);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            if (resultCode == 0x333) {
                loadPlatAppFlow(detail_id);
                mPlatComAfAdapter.notifyDataSetChanged();
                a_and_disa_ll.setVisibility(View.GONE);
//                ToastMessage("变更成功，刷新审批节点");
                change = data.getBooleanExtra("change",false);
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
                startActivityForResult(new Intent(getApplicationContext(), ChangeDealManActivity.class)
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
        param.put("whichpage", whichpage);
        param.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
        param.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, AGREE_OR_DISAGREE, null, null, "post");

    }

    // 反提交之操作：清除审批流和更改单据状态
    private void doresubmit(String retype) {

        HashMap<String, Object> params = new HashMap<>();
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_delete_approval_flow_url;//这个接口仅仅是反提交操作
        Map<String, Object> param = new HashMap<>();
        param.put("id", mkeyValue);
        param.put("whichpage", whichpage);
        param.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
        param.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        if ("reforsub".equals(retype)) {  //反提交之重新提交请求
            ViewUtil.httpSendRequest(this, url, param, handler, headers, RESUBMIT_OF_DELETEAPPFL, null, null, "post");
        } else if ("refordelete".equals(retype)) { //反提交之删除请求
            ViewUtil.httpSendRequest(this, url, param, handler, headers, DELETE_OF_DELETEAPPFL, null, null, "post");
        }
    }

    private void initData() {
        mForm.setAdapter(new SimpleAdapter(mContext, datas, R.layout.item_simple_text1,
                new String[]{"key", "value"}, new int[]{R.id.tv_key1, R.id.tv_value1}));
        mDetail.setAdapter(new SimpleAdapter(mContext, details, R.layout.item_simple_text14,
                new String[]{"title", "key1", "key2", "key3", "value1", "value2", "value3"},
                new int[]{R.id.tv_field1, R.id.tv_key1, R.id.tv_key2, R.id.tv_key3,
                        R.id.tv_value1, R.id.tv_value2, R.id.tv_value3}));
    }

    private void loadPlatDocDetails(int detail_id, int whichpage) {
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_doc_detaily_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", detail_id);
        param.put("whichpage", whichpage);
        param.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
        param.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, COMMON_DOC_DETAILY, null, null, "get");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!StringUtil.isEmpty(formwhere) && "ADDUI".equals(formwhere)) {
                switch (whichpage) {
                    case 1:
                        startActivity(new Intent(getApplicationContext(), LeavePageActivity.class)
                                .putExtra("ADDUI", "ADDUI"));
                        break;
                    case 2:
                        startActivity(new Intent(getApplicationContext(), TravelPageActivity.class)
                                .putExtra("ADDUI", "ADDUI"));
                        break;
                    case 3:
                        startActivity(new Intent(getApplicationContext(), WorkPageActivity.class)
                                .putExtra("ADDUI", "ADDUI"));
                        break;
                }
                finish();
            } else {
                if (change){
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
        if (!StringUtil.isEmpty(formwhere) && "ADDUI".equals(formwhere)) {
            switch (whichpage) {
                case 1:
                    startActivity(new Intent(getApplicationContext(), LeavePageActivity.class)
                            .putExtra("ADDUI", "ADDUI"));
                    break;
                case 2:
                    startActivity(new Intent(getApplicationContext(), TravelPageActivity.class)
                            .putExtra("ADDUI", "ADDUI"));
                    break;
                case 3:
                    startActivity(new Intent(getApplicationContext(), WorkPageActivity.class)
                            .putExtra("ADDUI", "ADDUI"));
                    break;
            }
            finish();
        } else {
            if (change){
                Intent intent = new Intent();
                intent.putExtra("isRemove", true);
                setResult(0x41701, intent);
            }
            finish();
        }
        super.onBackPressed();
    }

    private void initNewView() {

        doc_key = new ArrayList<>();
        doc_value = new ArrayList<>();
        doc_detail_key = new ArrayList<>();
        doc_detail_value = new ArrayList<>();

        mymainAdapter = new PCDMainAdapter();
        mySecondOutAdapter = new PlatSecondOutAdapter();
        cdmain_lv.setAdapter(mymainAdapter);
        cdsecond_out_lv.setAdapter(mySecondOutAdapter);

        Intent intent = getIntent();
        if (intent != null) {
            common_docui_sv.setVisibility(View.VISIBLE);
            dataJson = intent.getStringExtra("data");
            detailJson = intent.getStringExtra("detail");
            setTitle(intent.getStringExtra("title"));
            doc_type = intent.getStringExtra("title");
            mkeyValue = intent.getIntExtra("mkeyValue", -1);
            formwhere = intent.getStringExtra("ADDUI");
            doc_em_name = intent.getStringExtra("docemname");
            if (MyApplication.getInstance().isNetworkActive()) {
                if (mkeyValue != -1 || mkeyValue != 0) {
                    loadPlatAppFlow(mkeyValue);
                }
            } else {
                ToastMessage(getString(R.string.networks_out));
                hide_above_af.setVisibility(View.GONE);
            }

            status = intent.getStringExtra("status");
            doStatusShow(status, doc_em_name);
            Log.d("mkeyValue", mkeyValue + "");

            LinkedHashMap<String, Object> root = JSON.parseObject(dataJson, new TypeReference<LinkedHashMap<String, Object>>() {
            });
            Collection<Object> lists = root.values();
            for (int i = 0; i < lists.size(); i++) {
                doc_key.add(String.valueOf(root.keySet().toArray()[i]));
                doc_value.add(String.valueOf(root.values().toArray()[i]));
            }

            if (!StringUtil.isEmpty(detailJson)) {
                JSONArray array = JSON.parseArray(detailJson);
                for (int i = 0; i < array.size(); i++) {
                    String num = CommonUtil.numToCN(i + 1);
                    if ("出差单详情".equals(doc_type)) {
                        doc_detail_key.add(getString(R.string.travel_detail) + num);
                        doc_detail_value.add("");
                        doc_detail_key.add(array.getJSONObject(i).keySet().toArray()[0].toString());
                        doc_detail_value.add(array.getJSONObject(i).values().toArray()[0].toString());
                    } else if ("加班单详情".equals(doc_type)) {
                        doc_detail_key.add(getString(R.string.overwork_detail) + num);
                        doc_detail_value.add("");
                        /*doc_detail_key.add(array.getJSONObject(i).keySet().toArray()[0].toString());
                        doc_detail_value.add(array.getJSONObject(i).values().toArray()[0].toString());
                        doc_detail_key.add(array.getJSONObject(i).keySet().toArray()[1].toString());
                        doc_detail_value.add(array.getJSONObject(i).values().toArray()[1].toString());
                        doc_detail_key.add(array.getJSONObject(i).keySet().toArray()[2].toString());
                        doc_detail_value.add(array.getJSONObject(i).values().toArray()[2].toString());*/

                        doc_detail_key.add(getString(R.string.start_time));doc_detail_value.add(array.getJSONObject(i).getString(getString(R.string.start_time)));
                        doc_detail_key.add(getString(R.string.end_time));doc_detail_value.add(array.getJSONObject(i).getString(getString(R.string.end_time)));
                        doc_detail_key.add(getString(R.string.overwork_hours));doc_detail_value.add(array.getJSONObject(i).getString(getString(R.string.overwork_hours)));

                    }
                }
            }
            Log.i("flhdoc_detail_key",JSON.toJSONString(doc_detail_key));
            mymainAdapter.setDoc_key(doc_key);
            mymainAdapter.setDoc_value(doc_value);
            mymainAdapter.notifyDataSetChanged();

            if (!ListUtils.isEmpty(doc_detail_key) && !ListUtils.isEmpty(doc_detail_value)) {
                mySecondOutAdapter.setDoc_detail_key(doc_detail_key);
                mySecondOutAdapter.setDoc_detail_value(doc_detail_value);
                mySecondOutAdapter.notifyDataSetChanged();
            }
            LogUtil.prinlnLongMsg("doc_key,doc_value,mkeyValue,detail_id", doc_key + "," + doc_value + "," + mkeyValue + "," + detail_id);
            LogUtil.prinlnLongMsg("doc_detail_key,doc_detail_value", doc_detail_key + ":" + doc_detail_value);
        }
    }

    private void initView() {
        a_and_disa_ll.setVisibility(View.GONE);
//        doc_msg_ll.setVisibility(View.GONE);
        mForm.setVisibility(View.GONE);
        mDetail.setVisibility(View.GONE);
        Intent intent = getIntent();
        if (intent != null) {
            dataJson = intent.getStringExtra("data");
            detailJson = intent.getStringExtra("detail");
            setTitle(intent.getStringExtra("title"));
            mkeyValue = intent.getIntExtra("mkeyValue", -1);
            if (MyApplication.getInstance().isNetworkActive()) {
                if (mkeyValue != -1 || mkeyValue != 0) {
                    loadPlatAppFlow(mkeyValue);
                }
            } else {
                ToastMessage(getResources().getString(R.string.networks_out));
                hide_above_af.setVisibility(View.GONE);
            }

            status = intent.getStringExtra("status");
            doStatusShow(status, doc_em_name);
            Log.d("mkeyValue", mkeyValue + "");

            LinkedHashMap<String, Object> root = JSON.parseObject(dataJson, new TypeReference<LinkedHashMap<String, Object>>() {
            });
            Collection<Object> lists = root.values();
            for (int i = 0; i < lists.size(); i++) {
                LinkedHashMap<String, String> map = new LinkedHashMap<>();
                map.put("key", String.valueOf(root.keySet().toArray()[i]));
                map.put("value", String.valueOf(root.values().toArray()[i]));
                datas.add(map);
            }
            if (!StringUtil.isEmpty(detailJson)) {
                JSONArray array = JSON.parseArray(detailJson);
                for (int i = 0; i < array.size(); i++) {
                    LinkedHashMap<String, String> temp = new LinkedHashMap<>();
                    String num = CommonUtil.numToCN(i + 1);
                    temp.put("title", getString(R.string.ming_detail) + num);
                    switch (whichpage) {
                        case 2:
                            temp.put("key1", array.getJSONObject(i).keySet().toArray()[0].toString());
                            temp.put("value1", array.getJSONObject(i).values().toArray()[0].toString());
                            break;
                        case 3:
                            temp.put("key1", array.getJSONObject(i).keySet().toArray()[0].toString());
                            temp.put("key2", array.getJSONObject(i).keySet().toArray()[1].toString());
                            temp.put("key3", array.getJSONObject(i).keySet().toArray()[2].toString());
                            temp.put("value1", array.getJSONObject(i).values().toArray()[0].toString());
                            temp.put("value2", array.getJSONObject(i).values().toArray()[1].toString());
                            temp.put("value3", array.getJSONObject(i).values().toArray()[2].toString());
                            break;
                    }
                    details.add(temp);
                }
            }
        }
    }

    private void doStatusShow(String status, String doc_em_name) {
        if (!StringUtil.isEmpty(status)) {
            if ("已审批".equals(status) || "已审核".equals(status)) {
                status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.approval));
                status_tv.setText(getString(R.string.status_approved));
                resanddel_ll.setVisibility(View.GONE);
            } else if ("在录入".equals(status)){
                status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.done_approval));
                status_tv.setText(getString(R.string.status_unsubmit));
            }else if ("已提交".equals(status)){
                status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.no_approval));
                status_tv.setText(getString(R.string.status_pending));
                if (deal_type == 1 || deal_type == 2) {
                    resanddel_ll.setVisibility(View.GONE);
                } else {
                    resanddel_ll.setVisibility(View.VISIBLE);
                }
            }
        }

        if (!StringUtil.isEmpty(doc_em_name)) {
            doc_em_name_tv.setText(doc_em_name);
        }
    }

    private void loadPlatAppFlow(int af_id) {
        String caller = "";
        switch (whichpage) {
            case 1:
                caller = "请假单";
                break;
            case 2:
                caller = "出差单";
                break;
            case 3:
                caller = "加班单";
                break;
        }
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().common_app_flow_nodes_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", af_id);
        param.put("caller", caller);
        param.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
        param.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, PLAT_APP_FLOW_NODES, null, null, "get");
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
                        progressDialog.dismiss();
                    }
                    break;
                case COMMON_DOC_DETAILY:
                    if (msg.getData() != null) {
                        String com_doc_details = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("details_result", com_doc_details);
                        if (!StringUtil.isEmpty(com_doc_details)) {
                            doShowDocDetails(com_doc_details);
                        } else {
                            a_and_disa_ll.setVisibility(View.GONE);
                            hide_above_af.setVisibility(View.GONE);
                            ToastMessage(getString(R.string.doc_detaily_deleted));
                            progressDialog.dismiss();
                        }
                    } else {
                        a_and_disa_ll.setVisibility(View.GONE);
                        hide_above_af.setVisibility(View.GONE);
                        ToastMessage(getString(R.string.doc_detaily_deleted));
                        progressDialog.dismiss();
                    }
                    break;
                case DOC_EM_DATA:
                    if (msg.getData() != null) {
                        String doc_em_data = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("doc_em_data", doc_em_data);
                        if (!StringUtil.isEmpty(doc_em_data)) {
                            doShowDocEmData(doc_em_data);
                        }
                    }
                    break;
                case RESUBMIT_OF_DELETEAPPFL:
                    if (msg.getData() != null) {
                        String r_for_s_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("r_for_s_result", r_for_s_result);
                        if (JSON.parseObject(r_for_s_result).containsKey("success") && JSON.parseObject(r_for_s_result).getBoolean("success")){
//                            ToastMessage("反提交成功");
                            Log.i("doresubmit_id", mkeyValue + "");
                            // TODO 跳转至录入界面操作
                            doJumpToAdd(mkeyValue);
                            finish();
                        }else {
                            if (JSON.parseObject(r_for_s_result).containsKey("error") && !StringUtil.isEmpty(JSON.parseObject(r_for_s_result).getString("error"))){
                                ToastMessage(JSON.parseObject(r_for_s_result).getString("error"));
                            }
                        }
                    }
                    break;
                case DELETE_OF_DELETEAPPFL:
                    if (msg.getData() != null) {
                        String r_for_d_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("r_for_d_result", r_for_d_result);
//                        ToastMessage("反提交成功");
                        if (JSON.parseObject(r_for_d_result).containsKey("success") && JSON.parseObject(r_for_d_result).getBoolean("success")){
                            doPlatDeleteByid(mkeyValue);
                        }else {
                            if (JSON.parseObject(r_for_d_result).containsKey("error") && !StringUtil.isEmpty(JSON.parseObject(r_for_d_result).getString("error"))){
                                ToastMessage(JSON.parseObject(r_for_d_result).getString("error"));
                            }
                        }
                    }
                    break;

                case DELETE_SUCCEED:
                    if (msg.getData() != null) {
                        String delete_result = msg.getData().getString("result");
                        if (JSON.parseObject(delete_result).containsKey("success") && JSON.parseObject(delete_result).getBoolean("success")) {
                            Toast.makeText(MyApplication.getInstance(), getString(R.string.common_delete), Toast.LENGTH_LONG);
                            Intent intent = new Intent();
                            intent.putExtra("delete_succeed", "delete_succeed");
                            setResult(0x328, intent);
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
                        intent.putExtra("isRemove",true);
                        setResult(0x41701,intent);
                        finish();
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                    }
                    break;
            }
        }
    };


    private void doJumpToAdd(int mkeyValue) {
        if (whichpage == 1) {
            startActivity(new Intent(FormDetailActivity.this, PlatLeaveAddActivity.class)
                    .putExtra("data", dataJson)
                    .putExtra("mkeyValue", mkeyValue)
                    .putExtra("submittype", "resubmit"));

        }

        if (whichpage == 2) {
            startActivity(new Intent(FormDetailActivity.this, TravelActivity.class)
                    .putExtra("data", dataJson)
                    .putExtra("detailJson", detailJson)
                    .putExtra("mkeyValue", mkeyValue)
                    .putExtra("submittype", "resubmit")
                    .putExtra("fpd_id", fpd_id));

        }

        if (whichpage == 3) {
            startActivity(new Intent(FormDetailActivity.this, WorkExtraActivity.class)
                    .putExtra("data", dataJson)
                    .putExtra("detailJson", detailJson)
                    .putExtra("mkeyValue", mkeyValue)
                    .putExtra("submittype", "resubmit")
                    .putExtra("wod_id", wod_id));
        }
        finish();
    }

    private void doPlatDeleteByid(int mkeyValue) {
        //删除
        String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().delete_common_doc_url;
        Map<String, Object> param = new HashMap<>();
        param.put("id", mkeyValue);
        param.put("whichpage", whichpage);
        param.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
        param.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, DELETE_SUCCEED, null, null, "post");

    }

    private void doPlatComAfShow(String app_flow_result) {
        JSONObject resultJsonObject = JSON.parseObject(app_flow_result);
        if (resultJsonObject == null) {
            approval_flowlv.setVisibility(View.GONE);
            hide_above_af.setVisibility(View.GONE);
        } else {
            mPlatComAfBean = JSON.parseObject(resultJsonObject.toString(), PlatComAfBean.class);
            if (!ListUtils.isEmpty(mPlatComAfBean.getData()) || !ListUtils.isEmpty(mPlatComAfBean.getNodes())) {
                //TODO 获取数据审批人姓名，头像,审批状态
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
                } else if (nodes_size > 0) {
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

                mPlatComAfAdapter.setAfpeople_names(afpeople_names);
                mPlatComAfAdapter.setmPlatComAfBean(mPlatComAfBean);
                mPlatComAfAdapter.notifyDataSetChanged();

                Log.d("afpeople_names", afpeople_names.toString());
            }

        }
    }
    private String em_code;
    private void doShowDocEmData(String doc_em_data) {
        JSONObject Object = JSON.parseObject(doc_em_data);
        JSONObject dataObject = Object.getJSONObject("data");
        if (dataObject != null) {
            switch (whichpage) {
                case 1:
                    status = dataObject.getString("va_status");
                    doc_em_name = dataObject.getString("va_recorder");
                    break;
                case 2:
                    status = dataObject.getString("fp_status");
                    doc_em_name = dataObject.getString("fp_people2");
                    fpd_id = dataObject.getJSONArray("feePleaseDetails").getJSONObject(0).getInteger("fpd_id");
                    break;
                case 3:
                    status = dataObject.getString("wo_status");
                    doc_em_name = dataObject.getString("wo_recorder");
                    wod_id = dataObject.getJSONArray("workovertimedet").getJSONObject(0).getInteger("wod_id");
                    break;
            }
            em_code = dataObject.getLongValue("emcode")+"";
//            getapplypeomsg(em_code);
            doStatusShow(status, doc_em_name);

            doc_em_name_tv.setText(platem_name + "");
            String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
            AvatarHelper.getInstance().display(loginUserId, photo_im, true, false);
        }
    }

    private void getapplypeomsg(String em_code) {
        if (!StringUtil.isEmpty((em_code))){
            String whichsys = CommonUtil.getMaster();
            String[] selectionArgs = {em_code == null ? "" : em_code, whichsys};
            String selection = "em_code=? and whichsys=? ";
            try {
                //获取数据库数据
                EmployeesEntity bean = manager.selectForEmployee(selectionArgs, selection);
                if (bean != null) {
                    String imId = String.valueOf(bean.getEm_IMID());
                    String imName = String.valueOf(bean.getEM_NAME());
                    String imDepartment = String.valueOf(bean.getEM_DEFAULTORNAME());
                    String imPosition = String.valueOf(bean.getEM_POSITION());

                    if (!StringUtil.isEmpty(imId)) {
                        AvatarHelper.getInstance().display(imId, photo_im, true, false);
                    } else {
                        String imageUri = "drawable://" + R.drawable.common_header_boy;
                        AvatarHelper.getInstance().display(imageUri, photo_im, true);
                    }//显示圆角图片
                    section_tv.setText(imDepartment + ">" + imPosition);

                    Log.i("aptodo", "imId=" + imId + "imName" + imName + "imDepartment" + imDepartment + "imPosition" + imPosition);
                } else {
//                    ToastMessage("单据申请人数据获取异常");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doShowDocDetails(String com_doc_details) {
        JSONObject Object = JSON.parseObject(com_doc_details);
        JSONObject dataObject = Object.getJSONObject("data");
        if (dataObject != null) {
            common_docui_sv.setVisibility(View.VISIBLE);
            if (!ListUtils.isEmpty(doc_key)) doc_key.clear();
            if (!ListUtils.isEmpty(doc_value)) doc_value.clear();
            if (whichpage == 1) {
                cdsecong_out_ll.setVisibility(View.GONE);
                mkeyValue = dataObject.getIntValue("va_id");
//                doc_key.add("录入人");
//                doc_value.add(dataObject.getString("va_recorder"));
                doc_key.add(getString(R.string.doc_record_time));
                doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("va_date"), "yyyy-MM-dd HH:mm"));
                doc_key.add(getString(R.string.leave_type));
                doc_value.add(dataObject.getString("va_vacationtype"));
//                doc_key.add("单据状态");
//                doc_value.add(dataObject.getString("va_status"));
                doc_key.add(getString(R.string.start_time));
                doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("va_startime"), "yyyy-MM-dd HH:mm"));
                doc_key.add(getString(R.string.end_time));
                doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("va_endtime"), "yyyy-MM-dd HH:mm"));
                doc_key.add(getString(R.string.leave_reason));
                doc_value.add(dataObject.getString("va_remark"));

                status = dataObject.getString("va_status");
                doc_em_name = dataObject.getString("va_recorder");
            }
            if (whichpage == 3) {
                cdsecong_out_ll.setVisibility(View.VISIBLE);
                mkeyValue = dataObject.getIntValue("wo_id");
//                doc_key.add("录入人");
//                doc_value.add(dataObject.getString("wo_recorder"));
                doc_key.add(getString(R.string.doc_record_time));
                doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("wo_date"), "yyyy-MM-dd HH:mm"));
//                doc_key.add("单据状态");
//                doc_value.add(dataObject.getString("wo_status"));
                doc_key.add(getString(R.string.overwork_purpose));
                doc_value.add(dataObject.getString("wo_worktask"));
                //从表明细
                doc_detail_key.add(getString(R.string.overwork_detail1));
                doc_detail_value.add("");
                doc_detail_key.add(getString(R.string.start_time));
                doc_detail_value.add(
                        DateFormatUtil.long2Str(dataObject.getJSONArray("workovertimedet").getJSONObject(0).getLongValue("wod_startdate"), "yyyy-MM-dd HH:mm"));
                doc_detail_key.add(getString(R.string.end_time));
                doc_detail_value.add(
                        DateFormatUtil.long2Str(dataObject.getJSONArray("workovertimedet").getJSONObject(0).getLongValue("wod_enddate"), "yyyy-MM-dd HH:mm"));
                doc_detail_key.add(getString(R.string.overwork_hours));
                doc_detail_value.add(dataObject.getJSONArray("workovertimedet").getJSONObject(0).getDoubleValue("wod_count") + "");

                status = dataObject.getString("wo_status");
                doc_em_name = dataObject.getString("wo_recorder");
            }
            if (whichpage == 2) {
                cdsecong_out_ll.setVisibility(View.VISIBLE);
                mkeyValue = dataObject.getIntValue("fd_id");
//                doc_key.add("录入人");
//                doc_value.add(dataObject.getString("fp_people2"));
                doc_key.add(getString(R.string.doc_record_time));
                doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("fp_recorddate"), "yyyy-MM-dd HH:mm"));
//                doc_key.add("单据状态");
//                doc_value.add(dataObject.getString("fp_status"));
                doc_key.add(getString(R.string.start_time));
                doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("fp_prestartdate"), "yyyy-MM-dd HH:mm"));
                doc_key.add(getString(R.string.end_time));
                doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("fp_preenddate"), "yyyy-MM-dd HH:mm"));
                doc_key.add(getString(R.string.travel_reason));
                doc_value.add(dataObject.getString("fp_v3"));

                doc_detail_key.add(getString(R.string.travel_detail1));
                doc_detail_value.add("");
                doc_detail_key.add(getString(R.string.travel_destination));
                doc_detail_value.add(dataObject.getJSONArray("feePleaseDetails").getJSONObject(0).getString("fpd_location"));

                status = dataObject.getString("fp_status");
                doc_em_name = dataObject.getString("fp_people2");
            }

            doStatusShow(status, doc_em_name);
            mymainAdapter.setDoc_key(doc_key);
            mymainAdapter.setDoc_value(doc_value);
            mymainAdapter.notifyDataSetChanged();
            if (detail_id != -1 || detail_id != 0) {
                loadPlatAppFlow(detail_id);
            }


            if (!ListUtils.isEmpty(doc_detail_key) && !ListUtils.isEmpty(doc_detail_value)) {
                mySecondOutAdapter.setDoc_detail_key(doc_detail_key);
                mySecondOutAdapter.setDoc_detail_value(doc_detail_value);
                mySecondOutAdapter.notifyDataSetChanged();
            }
            LogUtil.prinlnLongMsg("doc_key,doc_value,mkeyValue,detail_id", doc_key + "," + doc_value + "," + mkeyValue + "," + detail_id);
            LogUtil.prinlnLongMsg("doc_detail_key,doc_detail_value", doc_detail_key + ":" + doc_detail_value);

            em_code = dataObject.getLongValue("emcode") + "";
            getapplypeomsg(em_code);
        } else {
            a_and_disa_ll.setVisibility(View.GONE);
            hide_above_af.setVisibility(View.GONE);
            ToastMessage(getString(R.string.doc_detaily_deleted));
            progressDialog.dismiss();
        }
    }


    // Plat Common Details Mian Adapter TODO 主表适配器
    private class PCDMainAdapter extends BaseAdapter {
        private List<String> doc_key;
        private List<String> doc_value;

        public List<String> getDoc_key() {
            return doc_key;
        }

        public void setDoc_key(List<String> doc_key) {
            this.doc_key = doc_key;
        }

        public List<String> getDoc_value() {
            return doc_value;
        }

        public void setDoc_value(List<String> doc_value) {
            this.doc_value = doc_value;
        }

        @Override
        public int getCount() {
            return doc_key == null ? 0 : doc_key.size();
        }

        @Override
        public Object getItem(int position) {
            return doc_key.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.item_comdoc_am, null);
                viewHolder.docmainmsg_list = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
                viewHolder.docmainmsg_value = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (!StringUtil.isEmpty(doc_value.get(position))) {
                viewHolder.docmainmsg_list.setText(doc_key.get(position));
            } else {
                viewHolder.docmainmsg_list.setText("");
            }

            if (!StringUtil.isEmpty(doc_key.get(position))) {
                viewHolder.docmainmsg_value.setText(doc_value.get(position));
            } else {
                viewHolder.docmainmsg_value.setText("");
            }
            return convertView;
        }

        class ViewHolder {
            TextView docmainmsg_list;
            TextView docmainmsg_value;
        }
    }

    //从表外面一层适配器
    private class PlatSecondOutAdapter extends BaseAdapter {
        private List<String> doc_detail_key;
        private List<String> doc_detail_value;

        public void setDoc_detail_key(List<String> doc_detail_key) {
            this.doc_detail_key = doc_detail_key;
        }

        public List<String> getDoc_detail_value() {
            return doc_detail_value;
        }

        public void setDoc_detail_value(List<String> doc_detail_value) {
            this.doc_detail_value = doc_detail_value;
        }

        @Override
        public int getCount() {
            return doc_detail_key == null ? 0 : doc_detail_key.size();
        }

        @Override
        public Object getItem(int position) {
            return doc_detail_key == null ? 0 : doc_detail_key.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(getApplicationContext(), R.layout.item_comdoc_am, null);
                viewHolder.list_tv = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
                viewHolder.value_tv = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                viewHolder.list_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.no_approval));
                viewHolder.list_tv.setText(doc_detail_key.get(position));
                viewHolder.value_tv.setText("");
            } else {
                if (!StringUtil.isEmpty(doc_detail_key.get(position))) {
                    viewHolder.list_tv.setText(doc_detail_key.get(position));
                } else {
                    viewHolder.list_tv.setText("");
                }

                if (!StringUtil.isEmpty(doc_detail_value.get(position))) {
                    viewHolder.value_tv.setText(doc_detail_value.get(position));
                } else {
                    viewHolder.value_tv.setText("");
                }

            }

            return convertView;
        }

        class ViewHolder {
            TextView list_tv;
            TextView value_tv;
        }
    }
}

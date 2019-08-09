package com.modular.appmessages.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.core.model.OAConfig;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.CustomerScrollView;
import com.core.widget.MyListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.modular.appmessages.R;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.uas.appworks.OA.platform.adapter.PlatComAfAdapter;
import com.uas.appworks.OA.platform.model.PlatComAfBean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FANGlh on 2017/4/18.
 * function:申诉单审批界面
 */
public class ComplaintDetailActivity extends BaseActivity {
    private static final int COMMON_DOC_DETAILY = 41801;
    private static final int PLAT_APP_FLOW_NODES = 41802;
    private static final int AGREE_OR_DISAGREE = 41803;

    private LinearLayout agree_ll;
    private LinearLayout disagree_ll;
    private View hide_above_af;
    private TextView status_tv;
    private TextView doc_em_name_tv;
    private CustomerScrollView common_docui_sv;
    private MyListView cdmain_lv;

    private List<String> afpeople_names;
    private PlatComAfAdapter mPlatComAfAdapter;
    private PlatComAfBean mPlatComAfBean;
    private String fromwhere;
    private int whichpage;
    private int deal_id;
    private int detail_id;
    private int deal_type;
    private String caller = "申诉单";
    private List<String> doc_key;
    private List<String> doc_value;
    private PCDMainAdapter mymainAdapter;
    private int agree_type;
    private Boolean change = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complaint_detail);
        initView();
        judgeFromWhere();
        initClickEvent();
    }

    private MyListView approval_flowlv;

    private LinearLayout resanddel_ll;
    private LinearLayout a_and_disa_ll;
    private LinearLayout change_dealman_ll;



    private void initView() {
        approval_flowlv = (MyListView) findViewById(R.id.common_approval_flow_lv);
        resanddel_ll = (LinearLayout) findViewById(R.id.item_common_docui_res_and_del_ll);
        a_and_disa_ll = (LinearLayout) findViewById(R.id.common_docui_agree_and_change_ll);
        change_dealman_ll = (LinearLayout) findViewById(R.id.common_docui_change_dealman_ll);
        agree_ll = (LinearLayout) findViewById(R.id.common_docui_agree_ll);
        disagree_ll = (LinearLayout) findViewById(R.id.common_docui_disagree_ll);
        hide_above_af = findViewById(R.id.hide_above_af);
        status_tv = (TextView) findViewById(R.id.common_docui_status_tv);
        doc_em_name_tv = (TextView) findViewById(R.id.common_docui_name_tv);
        common_docui_sv = (CustomerScrollView) findViewById(R.id.common_docui_sv);
        cdmain_lv = (MyListView) findViewById(R.id.common_docui_main_msg_lv);
    }

    private void initClickEvent() {
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

    private void doSelectDealMan() {
        Intent intent = new Intent("com.modular.main.SelectCollisionActivity");
        SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                .setTitle(getString(R.string.select_doman))
                .setSingleAble(true);
        intent.putExtra(OAConfig.MODEL_DATA, bean);
        startActivityForResult(intent, 0x01);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (resultCode == 0x333) {
            loadPlatAppFlow(detail_id);
            mPlatComAfAdapter.notifyDataSetChanged();
            a_and_disa_ll.setVisibility(View.GONE);
//            ToastMessage("变更成功，刷新审批节点");
            change = data.getBooleanExtra("change", false);
        }

        if (requestCode == 0x01 && resultCode == 0x20) {
            SelectEmUser d = data.getParcelableExtra("data");
            if (d != null) {
                String single_man_name = d.getEmName();
                int single_man_emcode = -1;
                String emcode = d.getEmCode();
                try {
                    single_man_emcode = Integer.valueOf(emcode);
                } catch (ClassCastException e) {

                }
                startActivityForResult(new Intent("com.modular.changedealman.ChangeDealManActivity")
                                .putExtra("deal_id", deal_id)
                                .putExtra("single_man_emcode", single_man_emcode)
                                .putExtra("single_man_name", single_man_name)
                        , 0x325);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private PopupWindow popupWindow = null;

    private void doDealDescribe(final int agree_type, final int deal_id) {
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.common_edit_window, null);

        //设置按钮的点击事件
        final FormEditText deal_describe = (FormEditText) contentView.findViewById(R.id.deal_describe);
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
                setBackgroundAlpha(ComplaintDetailActivity.this, 1f);
            }
        });
    }

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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
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
                case PLAT_APP_FLOW_NODES:
                    if (msg.getData() != null) {
                        String app_flow_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("app_flow_result", app_flow_result);
                        doPlatComAfShow(app_flow_result);
                        progressDialog.dismiss();
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

    private void judgeFromWhere() {
        progressDialog.show();
        afpeople_names = new ArrayList<>();
        mPlatComAfBean = new PlatComAfBean();
        mPlatComAfAdapter = new PlatComAfAdapter(this);
        approval_flowlv.setAdapter(mPlatComAfAdapter);

        Intent intent = getIntent();
        fromwhere = intent.getStringExtra("fromwhere");
        whichpage = intent.getIntExtra("whichpage", -1);

        if (!StringUtil.isEmpty(fromwhere) && "examine_and_approve".equals(fromwhere)) {
            //  领导审批时进入的界面   同意/不同意，
            deal_id = intent.getIntExtra("deal_id", -1);
            detail_id = intent.getIntExtra("detail_id", -1);
            deal_type = intent.getIntExtra("deal_type", -1);
            initEAndAView();
            LogUtil.d("deal_id,detail_id,whichpage", deal_id + "," + detail_id + "," + whichpage);
        }
    }

    private void initEAndAView() {
        resanddel_ll.setVisibility(View.GONE);
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
        mymainAdapter = new PCDMainAdapter();
        cdmain_lv.setAdapter(mymainAdapter);
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

    private void doShowDocDetails(String com_doc_details) {
        common_docui_sv.setVisibility(View.VISIBLE);
        JSONObject Object = JSON.parseObject(com_doc_details);
        JSONObject dataObject = Object.getJSONObject("data");
        if (dataObject != null) {
            if (!ListUtils.isEmpty(doc_key)) doc_key.clear();
            if (!ListUtils.isEmpty(doc_value)) doc_value.clear();

            doc_key.add(getString(R.string.doc_record_time));
            doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("recorderdate"), "yyyy-MM-dd HH:mm"));
            doc_key.add(getString(R.string.complaint_hour_T));
            doc_value.add(DateFormatUtil.long2Str(dataObject.getLongValue("signtime"), "yyyy-MM-dd HH:mm"));
            doc_key.add(getString(R.string.complaint_reson));
            doc_value.add(dataObject.getString("remark"));
            doc_key.add(getString(R.string.complaint_photo));
            doc_value.add(dataObject.getString("mobile"));
            doc_key.add(getString(R.string.complaint_addres));
            doc_value.add(dataObject.getString("address"));

            String status = dataObject.getString("status");
            String em_name = dataObject.getString("emname");

            doStatusShow(status, em_name);

            mymainAdapter.setDoc_key(doc_key);
            mymainAdapter.setDoc_value(doc_value);
            mymainAdapter.notifyDataSetChanged();
        } else {
            a_and_disa_ll.setVisibility(View.GONE);
            hide_above_af.setVisibility(View.GONE);
            ToastMessage(getString(R.string.doc_detaily_deleted));
            progressDialog.dismiss();
        }

        if (detail_id != -1 || detail_id != 0) {
            loadPlatAppFlow(detail_id);
        }
    }

    private void doStatusShow(String status, String em_name) {
        if (!StringUtil.isEmpty(status)) {
            if ("已审批".equals(status) || "已审核".equals(status)) {
                status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.approval));
                status_tv.setText(getString(R.string.status_approved));
            } else {
                status_tv.setTextColor(getApplicationContext().getResources().getColor(R.color.no_approval));
                status_tv.setText(getString(R.string.status_pending));
                if (deal_type == 1 || deal_type == 2) {
                    resanddel_ll.setVisibility(View.GONE);
                } else {
                    resanddel_ll.setVisibility(View.VISIBLE);
                }
            }
        }

        if (!StringUtil.isEmpty(em_name)) {
            doc_em_name_tv.setText(em_name);
        }
    }

    private void loadPlatAppFlow(int af_id) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (change) {
                Intent intent = new Intent();
                intent.putExtra("isRemove", true);
                setResult(0x41701, intent);
            }
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (change) {
            Intent intent = new Intent();
            intent.putExtra("isRemove", true);
            setResult(0x41701, intent);
        }
        super.onBackPressed();
    }
}

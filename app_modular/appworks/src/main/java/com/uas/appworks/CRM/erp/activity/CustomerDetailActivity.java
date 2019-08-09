package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.MyListView;
import com.core.widget.view.HorizontalStepsView;
import com.core.widget.view.steps.StepsView;
import com.uas.appworks.OA.erp.model.EmployeesModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @功能:客户详情
 * @author:Arisono
 * @param:
 * @return:
 */
public class CustomerDetailActivity extends BaseActivity {
    private TextView tv_company_name;
    private TextView tv_leader;
    private TextView tv_state;
    private TextView tv_lastDate;
    private TextView tv_address;
    private TextView tv_source;
    private TextView tv_industry;
    private TextView tv_type;
    private MyListView steps_lv;
    private TextView tv_diqu;
    private TextView tv_zhihzhao;
    private TextView tv_email;
    private TextView tv_name;
    private TextView tv_position;
    private TextView tv_mobile;
    private TextView tv_modifyRecord;
    private TextView tv_visitTimes;
    private TextView tv_feilv;
    private TextView tv_feiyonged;
    private StepsView mStepsView;
    private Button mAddPlanBtn;
    private TextView tv_money_kind;

    private String falg;
    private final int LOAD_BUSINESS_STATUES = 0x321;
    private Context ct;
    private String[] labels = {"初次沟通", "立项评估", "产品演示", "合同签约", "样品报价", "多次交易"
            , "商务谈判", "需求分析", "完成交易"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        tv_company_name = (TextView) findViewById(R.id.tv_company_name);
        tv_money_kind = (TextView) findViewById(R.id.tv_money_kind);
        mAddPlanBtn = (Button) findViewById(R.id.customer_detail_add_plan);
        mStepsView = (StepsView) findViewById(R.id.stepsView);
        tv_feiyonged = (TextView) findViewById(R.id.tv_feiyonged);
        tv_feilv = (TextView) findViewById(R.id.tv_feilv);
        tv_visitTimes = (TextView) findViewById(R.id.tv_visitTimes);
        tv_modifyRecord = (TextView) findViewById(R.id.tv_modifyRecord);
        tv_mobile = (TextView) findViewById(R.id.tv_mobile);
        tv_position = (TextView) findViewById(R.id.tv_position);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_email = (TextView) findViewById(R.id.tv_email);
        tv_zhihzhao = (TextView) findViewById(R.id.tv_zhihzhao);
        tv_diqu = (TextView) findViewById(R.id.tv_diqu);
        steps_lv = (MyListView) findViewById(R.id.steps_lv);
        tv_type = (TextView) findViewById(R.id.tv_type);
        tv_industry = (TextView) findViewById(R.id.tv_industry);
        tv_source = (TextView) findViewById(R.id.tv_source);
        tv_address = (TextView) findViewById(R.id.tv_address);
        tv_lastDate = (TextView) findViewById(R.id.tv_lastDate);
        tv_state = (TextView) findViewById(R.id.tv_state);
        tv_leader = (TextView) findViewById(R.id.tv_leader);


        ct = this;
       setTitle("客户详情");
    }

    private void initListener() {
//        mAddPlanBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(CustomerDetailActivity.this, VisitReportPlanActivity.class);
//                intent.putExtra("customer_name", mCustomerName);
//                intent.putExtra("customer_code",mCustomerCode);
//                intent.putExtra("contact_name", mContactName);
//                intent.putExtra("customer_address", mCustomerAddress);
//                startActivity(intent);
//            }
//        });
    }

    private void initData() {
        String code = getIntent().getStringExtra("code");
        falg = getIntent().getStringExtra("falg");
        sendHttpResquest(Constants.HTTP_SUCCESS_INIT, code);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item){
            startActivity(new Intent(this, CustomerAddActivity.class));
        }else if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }

    public String mCustomerCode;
    private String steps;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("result");
            if (!JSONUtil.validate(message)) return;
            JSONObject object = JSON.parseObject(msg.getData().getString("result"));
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    JSONObject jsonArray = object.getJSONObject("customer");
                    if (jsonArray != null) {
                        mCustomerCode = jsonArray.getString("cu_code");
                        tv_company_name.setText(jsonArray.getString("cu_name"));
                        tv_leader.setText(jsonArray.getString("cu_sellername"));
                        tv_state.setText(jsonArray.getString("cu_dealstatus") + "|普通客户");
                        tv_lastDate.setText(jsonArray.getString("cu_lastdate"));
                        tv_address.setText(jsonArray.getString("cu_add1"));
                        tv_source.setText(jsonArray.getString("cu_source"));
                        tv_industry.setText(jsonArray.getString("cu_kind"));
                        tv_diqu.setText(jsonArray.getString("cu_district"));
                        tv_type.setText(jsonArray.getString("cu_kind"));
                        tv_zhihzhao.setText(jsonArray.getString("cu_businesscode"));
                        tv_email.setText(jsonArray.getString("cu_email"));
                        tv_mobile.setText(jsonArray.getString("cu_mobile"));
                        tv_position.setText(jsonArray.getString("cu_degree"));
                        tv_name.setText(jsonArray.getString("cu_contact"));
                        steps = jsonArray.getString("cu_nichestep");
                        //TODO 测试先关闭
//                        tv_visitTimes.setText(jsonArray.getString("bfcount"));
                        tv_modifyRecord.setText(jsonArray.getString("cu_remark"));
                        tv_feilv.setText(jsonArray.getString("cu_taxrate"));
                        tv_feiyonged.setText(jsonArray.getString("bxamount"));
                        tv_money_kind.setText(jsonArray.getString("cu_currency"));
                    }
                    loadJieDuan();
                    break;
                case 0x21://获取商机阶段总数
                    progressDialog.dismiss();
                    try {
                        JSONArray array = object.getJSONArray("stages");
                        labels = new String[array.size()];
                        if (!array.isEmpty()) {
                            for (int i = 0; i < array.size(); i++) {
                                labels[i] = array.getJSONObject(i).getString("BS_NAME");
                            }
                        }
                        if (labels != null) {
                            List<EmployeesModel> models = new ArrayList<>();
                            models.add(new EmployeesModel().setEmployeeNames(getString(R.string.business_name) + 1).setEmployeecode(steps));
                            if (adapter == null) {
                                adapter = new StepsAdapter(models);
                                steps_lv.setAdapter(adapter);
                            } else
                                adapter.setModels(models);
                            loadBusinessStatues(getIntent().getStringExtra("code"));
                        }
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                        ToastMessage("扔出异常！");
                    }
                    break;
                case LOAD_BUSINESS_STATUES://获取商机阶段个数
                    progressDialog.dismiss();
                    if (JSONUtil.getBoolean(object, "success")) {
                        int visitcount = JSONUtil.getInt(object, "visitcount");
                        int nichecount = JSONUtil.getInt(object, "nichecount");//商机个数
                        tv_visitTimes.setText(String.valueOf(visitcount));

                        JSONArray nichecountItem = JSONUtil.getJSONArray(object, "businessProcess", "bccurrentprocess");
                        if (!ListUtils.isEmpty(nichecountItem)) {
                            List<EmployeesModel> models = new ArrayList<>();
                            for (int i = 0; i < nichecountItem.size(); i++) {
                                String name = JSONUtil.getText(nichecountItem.getJSONObject(i), "businessName");
                                String process = JSONUtil.getText(nichecountItem.getJSONObject(i), "businessProcess");
                                models.add(new EmployeesModel().setEmployeeNames(name).setEmployeecode(process));
                            }
                            if (adapter == null) {
                                adapter = new StepsAdapter(models);
                                steps_lv.setAdapter(adapter);
                            } else
                                adapter.setModels(models);
                        }
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    break;
                default:
                    progressDialog.dismiss();
                    break;
            }
        }
    };
    private StepsAdapter adapter = null;

    private int getPosition(String currentprocess) {
        if (labels == null || labels.length <= 0 || StringUtil.isEmpty(currentprocess))
            return 1;
        for (int i = 0; i < labels.length; i++)
            if (!StringUtil.isEmpty(labels[i]) && labels[i].equals(currentprocess))
                return (i + 1);
        return 1;
    }

    private void sendHttpResquest(int what, String code) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getCustomerbycode.action";
        Map<String, Object> params = new HashMap<>();
        params.put("cu_code", code);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private void loadBusinessStatues(String code) {
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getDatasbycode.action";
        Map<String, Object> params = new HashMap<>();
        params.put("custcode", code);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, LOAD_BUSINESS_STATUES, null, null, "post");
    }

    /**
     * @desc:加载阶段
     * @author：Arison on 2016/7/20
     */
    public void loadJieDuan() {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getBusinessChanceStage.action";
        Map<String, Object> params = new HashMap<>();
        params.put("condition", "1=1");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x21, null, null, "post");
    }

    private class StepsAdapter extends BaseAdapter {
        private List<EmployeesModel> models;

        public StepsAdapter(List<EmployeesModel> models) {
            this.models = models;
        }

        public void setModels(List<EmployeesModel> models) {
            this.models = models;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(models);
        }

        @Override
        public Object getItem(int position) {
            return models.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(ct).inflate(R.layout.item_steps, parent, false);
                holder.stepsView = (HorizontalStepsView) convertView.findViewById(R.id.stepsView);
                holder.business_stage_tv = (TextView) convertView.findViewById(R.id.business_stage_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (ListUtils.getSize(models) == 1)
                holder.business_stage_tv.setVisibility(View.GONE);
            EmployeesModel model = models.get(position);
            holder.business_stage_tv.setText(StringUtil.getMessage(model.getEmployeeNames()));

            holder.stepsView.setProgress(getPosition(model.getEmployeecode()), labels);
            holder.stepsView.setFocusable(false);
            holder.stepsView.setClickable(false);
            return convertView;
        }

        class ViewHolder {
            HorizontalStepsView stepsView;
            TextView business_stage_tv;
        }
    }


}

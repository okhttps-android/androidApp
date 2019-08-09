package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonInterface;
import com.core.utils.CommonUtil;
import com.core.widget.SingleDialog;
import com.core.widget.crouton.Style;
import com.core.widget.view.ListViewInScroller;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @功能:商机失效
 * @author:Arisono
 * @param:
 * @return:
 */
public class BusinessLessActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout ll_moment;
    private LinearLayout ll_leader;
    private LinearLayout ll_date;
    private EditText et_remark;
    private TextView tv_business_state;
    private TextView tv_leader;
    private TextView tv_date;
    private ArrayList<Data> datas = new ArrayList<>();
    private ListViewInScroller mList;

    private JSONArray array;//商机阶段全局变量
    private String bcd_code = "";
    //动态创建编辑框
    private HashMap<Integer, String> mEditText = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_less);
        initView();
        initData();
        initListener();
    }


    private void initView() {
        ll_moment = (LinearLayout) findViewById(R.id.ll_moment);
        ll_leader = (LinearLayout) findViewById(R.id.ll_leader);
        ll_date = (LinearLayout) findViewById(R.id.ll_date);
        et_remark = (EditText) findViewById(R.id.et_remark);
        tv_business_state = (TextView) findViewById(R.id.tv_business_state);
        tv_leader = (TextView) findViewById(R.id.tv_leader);
        tv_date = (TextView) findViewById(R.id.tv_date);
        mList = (ListViewInScroller) findViewById(R.id.lv_datas);
    }

    private void initListener() {
        tv_leader.setOnClickListener(this);
        tv_business_state.setOnClickListener(this);
    }

    int type = 1;
    private String code;
    private String process;
    private String doman;

    private void initData() {
        loadLeader(2);
        tv_date.setText(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getIntExtra("type", 1);
            code = intent.getStringExtra("code");
            process = intent.getStringExtra("process");
            doman = intent.getStringExtra("doman");
        }

        switch (type) {
            case 1:
               setTitle("商机失效");
                ll_leader.setVisibility(View.VISIBLE);
                ll_moment.setVisibility(View.GONE);
                ll_date.setVisibility(View.VISIBLE);
                break;
            case 2:
               setTitle("继续跟进");
                //tv_business_state.setText(process);
                ll_leader.setVisibility(View.GONE);
                ll_moment.setVisibility(View.VISIBLE);
                ll_date.setVisibility(View.GONE);
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_btn_certain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_save){
            if (type == 1) {
                CommonInterface.getInstance().getCodeByNet("BusinessChanceData", new CommonInterface.OnResultListener() {
                    @Override
                    public void result(boolean isOk, int result, String message) {
                        bcd_code = message;
                        sendHttpResquest(Constants.HTTP_SUCCESS_INIT, "失效");
                    }
                });

            }
            if (type == 2) {
                boolean falg = true;
                for (int i = 0; i < datas.size(); i++) {
                    if (datas.get(i).getIsNeed() == 1 && mEditText.get(i) == null) {
                        falg = false;
                        ToastMessage(datas.get(i).getName() + "为必填项");
                    } else {
                        if (mEditText.get(i) != null) {
                            if (StringUtil.isEmpty(mEditText.get(i).toString())) {
                                falg = false;
                                ToastMessage(datas.get(i).getName() + "为必填项");
                            }
                        }
                    }
                }
                if (falg) {
                    sendHttpResquest(Constants.HTTP_SUCCESS_INIT, "继续跟进");
                }
                for (int i = 0; i < mEditText.size(); i++) {
//                        Log.i(TAG, "onOptionsItemSelected:" + datas.get(i).getFormStoreKey() + ":" + mEditText.get(i));
                }
            }
        }else if (item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return true;
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result") + " type:" + type);
                    showToast("操作成功!" );
                    if (type == 2) {
                        updataSchedule(0x16);
                    } else {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                jumpToStateActivity();
                            }
                        }, 3000);
                    }
                    break;
                case 0x16:
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            jumpToStateActivity();
                        }
                    }, 2000);
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
//                    Crouton.makeText(activity,35, msg.getData().getString("result"),
//                            Style.holoRedLight, 2000).show();
                    ViewUtil.ToastMessage(mContext, msg.getData().getString("result"), Style.holoRedLight, 3000);
                    break;
                case 2:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    String leader = JSON.parseObject(msg.getData().getString("result"))
                            .getJSONObject("data").getString("em_name");
                    tv_leader.setText(leader);
                    break;
                case 5:
                    //商机阶段
                    progressDialog.dismiss();
                    lists.clear();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    String json = msg.getData().getString("result");
                    array = JSON.parseObject(json).getJSONArray("stages");
                    if (!array.isEmpty()) {
                        for (int i = 0; i < array.size(); i++) {
                            lists.add(array.getJSONObject(i).getString("BS_NAME"));
                        }
                    }
                    if (lists.isEmpty()) {
                        lists.add("无");
                    }
                    showSimpleDialog(tv_business_state, "商机阶段");
                    break;

            }
        }
    };

    private void jumpToStateActivity() {
        /*Intent intent = new Intent();
        intent.setClass(BusinessLessActivity.this, BusinessStateActivity.class);
        startActivity(intent);*/
        Intent intent = new Intent();
        setResult(1, intent);
        this.finish();
    }

    private void updataSchedule(int what) {
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/updateSchedule.action";
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private void sendHttpResquest(int what, String typeStr) {
        progressDialog.show();
        String remark = StringUtil.toHttpString(et_remark.getText().toString());
        String gridSoreData = "{\n";
        for (int i = 0; i < datas.size(); i++) {
            String value = mEditText.get(i) == null ? "" : mEditText.get(i).toString();
            gridSoreData = gridSoreData + "\"BCD_COLUMN" + datas.get(i).getFormStoreKey() + "\":\"" + value + "\",\n";
        }
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        if (StringUtil.isEmpty(tv_business_state.getText().toString()) && type == 2) {
            progressDialog.dismiss();
            ToastMessage("商机阶段为必填项");
            return;
        }
        String gridSore = gridSoreData +
                "\"bcd_bccode\":\"" + code + "\",\n" +
                "\"bcd_remark\":\"" + remark + "\",\n" +
                "\"bcd_leader\":\"" + tv_leader.getText().toString() + "\",\n" +
                "\"bcd_date\":\"" + tv_date.getText().toString() + "\",\n" +
                "\"bcd_bsname\":\"" + tv_business_state.getText() + "\",\n" +
                "\"bcd_type\":\"" + typeStr + "\",\n" +
                "\"bcd_man\":\"" + emname + "\"" + (this.type == 1 ? ",\n\"bcd_code\":\"" + bcd_code + "\"\n" : "") +
                "}";
        Log.i(TAG, "sendHttpResquest:" + gridSore);
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/updatebusinessChanceData.action";
        Map<String, Object> params = new HashMap<>();
        params.put("gridStore", gridSore);
        if (type == 1) {
            params.put("caller", "BusinessChanceData");//BusinessChance
        } else {
            params.put("caller", "BusinessChanceData");
        }

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }


    private void loadLeader(int what) {
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getheadmanmsg.action";
        Map<String, Object> params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    /**
     * @desc:加载阶段
     * @author：Arison on 2016/7/20
     */
    public void loadJieDuan(int what) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getBusinessChanceStage.action";
        Map<String, Object> params = new HashMap<>();
        params.put("condition", "1=1");
        params.put("master", CommonUtil.getSharedPreferences(ct, "erp_master"));
        params.put("sessionUser", CommonUtil.getSharedPreferences(ct, "erp_username"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    /**
     * @desc:动态加载编辑框
     * @author：Arison on 2016/10/19
     */
  /*  public void loadEditData(int what,String bs_code){
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "crm/chance/getpoint.action";
        Map<String, Object> params = new HashMap<>();
        params.put("bs_code", bs_code);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }*/
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_leader)
            loadLeader(2);
        else if (v.getId() ==  R.id.tv_business_state)
            loadJieDuan(5);
    }

    private List<String> lists = new ArrayList<String>();

    private SingleDialog singleDialog;

    public void showSimpleDialog(final TextView et, final String title) {
        if (singleDialog != null) {
            if (singleDialog.isShowing())
                return;
        }
        //                    "BS_POINT":"联系方式#沟通效果#天数#顺序#车型",
//                        "BS_POINTFLAG":"0#1#0#0#1",
//                        "BS_POINTDETNO":"3#4#7#8#9"
        datas.clear();
        singleDialog = new SingleDialog(ct, title,
                new SingleDialog.PickDialogListener() {
                    @Override
                    public void onListItemClick(int position, String value) {
                        et.setText(value);
                        if (!array.isEmpty()) {
                            for (int i = 0; i < array.size(); i++) {
                                if (value.equals(array.getJSONObject(i).getString("BS_NAME"))) {
                                    String bs_posint = array.getJSONObject(i).getString("BS_POINT");
                                    String bs_poiniflag = array.getJSONObject(i).getString("BS_POINTFLAG");
                                    String bs_pointdeino = array.getJSONObject(i).getString("BS_POINTDETNO");
                                    if (!StringUtil.isEmpty(bs_posint) && !StringUtil.isEmpty(bs_poiniflag) && !StringUtil.isEmpty(bs_pointdeino)) {
                                        String[] names = bs_posint.split("#");
                                        String[] falgs = bs_poiniflag.split("#");
                                        String[] keys = bs_pointdeino.split("#");
                                        for (int j = 0; j < names.length; j++) {
                                            Data data = new Data();
                                            data.setName(names[j]);
                                            data.setIsNeed(Integer.valueOf(falgs[j]));
                                            data.setFormStoreKey(keys[j]);
                                            datas.add(data);
                                        }
                                        //创建一个功能数组
                                        DataAdapter adapter = new DataAdapter(ct, datas);
                                        mList.setAdapter(adapter);

                                    } else {
                                        //创建一个功能数组
                                        datas.clear();
                                        DataAdapter adapter = new DataAdapter(ct, datas);
                                        mList.setAdapter(adapter);
                                    }

                                }

                            }
                        }
                    }
                });
        singleDialog.show();
        singleDialog.initViewData(lists);
       /* } else {
            singleDialog.show();
            singleDialog.initViewData(lists);
        }*/
    }

    public class Data {
        private String name;//字段名
        private int isNeed;//必填
        private String value;//编辑值
        private String formStoreKey;//formstore key

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIsNeed() {
            return isNeed;
        }

        public void setIsNeed(int isNeed) {
            this.isNeed = isNeed;
        }

        public String getFormStoreKey() {
            return formStoreKey;
        }

        public void setFormStoreKey(String formStoreKey) {
            this.formStoreKey = formStoreKey;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }


    public class DataAdapter extends BaseAdapter {
        private Context ct;
        private LayoutInflater inflater;
        private ArrayList<Data> mData;

        public DataAdapter(Context ct, ArrayList<Data> data) {
            this.ct = ct;
            this.mData = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewModel model = null;
            if (true) {
                convertView = inflater.inflate(R.layout.item_businessless_edit, parent, false);
                model = new ViewModel();
                model.text = (TextView) convertView.findViewById(R.id.tv_less_key);
                model.editText = (EditText) convertView.findViewById(R.id.tv_less_value);
                convertView.setTag(model);
            } else {
                //model= (ViewModel) convertView.getTag();
            }
            model.text.setText(mData.get(position).getName());
            if (mData.get(position).getIsNeed() == 0) {
                model.editText.setHint("");
            } else {
                model.editText.setHint(R.string.common_input);
            }

            model.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.i(TAG, "afterTextChanged:" + position);
                    mEditText.put(position, s.toString());
                    // datas.get(position).setValue(s.toString());
                }
            });
            //如果hashMap不为空，就设置的editText  
            if (mEditText.get(position) != null) {
                model.editText.setText(mEditText.get(position));
            }
            return convertView;
        }

        class ViewModel {
            TextView text;
            EditText editText;
        }
    }


}

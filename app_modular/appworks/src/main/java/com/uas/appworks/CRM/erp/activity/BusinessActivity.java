package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.config.VersionUtil;
import com.common.data.CalendarUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.view.ListViewInScroller;
import com.uas.appworks.CRM.erp.adapter.HListViewOneAdapter;
import com.uas.appworks.CRM.erp.view.FunnelView;
import com.uas.appworks.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商机页面
 */
public class BusinessActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = "BusinessActivity";
    private Button bt_qiang;
    private Button bt_create;
    private Button bt_manage;
    private ImageView mSwitchLeftIv, mSwitchRightIv;
    private TextView mTypeTextView;
    private ScrollView sv_funnel;
    private Button bt_go;
    private FunnelView funnelView;
    private HorizontalScrollView layout;
    private LinearLayout mBottomLayout;
    private ListViewInScroller lv_grid_dispaly;
    private HListViewOneAdapter hl_adapter;
    private ArrayList<ArrayList<String>> gridlists = new ArrayList<ArrayList<String>>();
    private String mWhichPage = "";
    private String gridData = "  [\n" +
            "[\"颜色\",\"商机阶段\",\"数量/金额\",\"转化率\"],\n" +
            "[\"#FF0000\",\"0\",\"0\",\"0\"],\n" +
            "[\"#00CCFF\",\"0\",\"0\",\"0\"],\n" +
            "[\"#FFFF00\",\"0\",\"0\",\"0\"],\n" +
            "[\"#00FF00\",\"0\",\"0\",\"0\"],\n" +
            "[\"#FF00FF\",\"0\",\"0\",\"0\"],\n" +
            "[\"#FF9900\",\"0\",\"0\",\"0\"],\n" +
            "[\"#993366\",\"0\",\"0\",\"0\"],\n" +
            "[\"#C0C0C0\",\"0\",\"0\",\"0\"],\n" +
            "[\"#FFCC99\",\"0\",\"0\",\"0\"]\n" +
            "]";
    private int mMenuPosition = 0;
    private String mBusinessType = "项目商机";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        initView();
        initData();
        initListener();
    }

    public void initView() {
        setTitle(getString(R.string.crmmain_business));

        Intent intent = getIntent();
        if (intent != null) {
            mWhichPage = intent.getStringExtra(Constants.FLAG.COMMON_WHICH_PAGE);
        }

        mBottomLayout = (LinearLayout) findViewById(R.id.ll_bottom);
        mSwitchLeftIv = (ImageView) findViewById(R.id.business_funnel_switch_left);
        mSwitchRightIv = (ImageView) findViewById(R.id.business_funnel_switch_right);
        mTypeTextView = (TextView) findViewById(R.id.business_funnel_type_tv);
        if ("businessManage".equals(mWhichPage)) {
            mBottomLayout.setVisibility(View.GONE);
            mSwitchLeftIv.setVisibility(View.GONE);
            mSwitchRightIv.setVisibility(View.VISIBLE);
            mTypeTextView.setVisibility(View.VISIBLE);
            if (intent != null) {
                mBusinessType = intent.getStringExtra(Constants.FLAG.BUSINESS_TYPE);
            }
            if (TextUtils.isEmpty(mBusinessType)) {
                mTypeTextView.setText("公司商机");
                mSwitchLeftIv.setVisibility(View.GONE);
                mSwitchRightIv.setVisibility(View.GONE);
            } else {
                mTypeTextView.setText(mBusinessType);
            }
        } else {
            mBottomLayout.setVisibility(View.VISIBLE);
            mSwitchLeftIv.setVisibility(View.GONE);
            mSwitchRightIv.setVisibility(View.GONE);
            mTypeTextView.setVisibility(View.GONE);
        }

        bt_qiang = (Button) findViewById(R.id.bt_qiang);
        bt_create = (Button) findViewById(R.id.bt_create);
        bt_manage = (Button) findViewById(R.id.bt_manage);
        bt_go = (Button) findViewById(R.id.bt_go);
        layout = (HorizontalScrollView) findViewById(R.id.layout);
        lv_grid_dispaly = (ListViewInScroller) findViewById(R.id.lv_grid_dispaly);
        sv_funnel = (ScrollView) findViewById(R.id.sv_funnel);
        funnelView = (FunnelView) findViewById(R.id.funnelview);
    }

    private void initListener() {
        bt_qiang.setOnClickListener(this);
        bt_manage.setOnClickListener(this);
        bt_go.setOnClickListener(this);
        bt_create.setOnClickListener(this);
        lv_grid_dispaly.setFocusable(false);
        lv_grid_dispaly.setOnItemClickListener(this);

        mSwitchLeftIv.setOnClickListener(this);
        mSwitchRightIv.setOnClickListener(this);
    }

    public void initData() {
        String month = new SimpleDateFormat("yyyyMM").format(CalendarUtil.getTimesMonthmorning());
        sendHttpResquest("to_char(bc_recorddate,'yyyymm')=" + month, mBusinessType);
        gridlists = (ArrayList) JSON.parseArray(gridData, ArrayList.class);
        hl_adapter = new HListViewOneAdapter(ct, gridlists);
        lv_grid_dispaly.setAdapter(hl_adapter);
        layout.setVisibility(View.VISIBLE);
    }

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crm_find, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.crm_data_find) {
            View view = getWindow().findViewById(item.getItemId());
            showPopupWindow(view);
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_create) {
            if (VersionUtil.canShowCrm2_0() && !CommonUtil.isBiteman())
                startActivity(new Intent(this, AddBusinessActivity.class));
            else startActivity(new Intent(this, BusinessAddActivity.class));
        } else if (v.getId() == R.id.bt_qiang) {
            startActivity(new Intent(this, BusinessDetailActivty.class).putExtra("bt_type", 1));
        } else if (v.getId() == R.id.bt_manage) {
            startActivity(new Intent(this, BusinessDetailActivty.class).putExtra("bt_type", 2));
        } else if (v.getId() == R.id.bt_go) {
            startActivity(new Intent(this, BusinessStateActivity.class).putExtra("bt_type", 0));
        } else if (v.getId() == R.id.business_funnel_switch_left) {
            mSwitchLeftIv.setVisibility(View.GONE);
            mSwitchRightIv.setVisibility(View.VISIBLE);
            mTypeTextView.setText(getString(R.string.project_business_chance));
            mBusinessType = "项目商机";
            requestByDate(mMenuPosition, mBusinessType, null);
        } else if (v.getId() == R.id.business_funnel_switch_right) {
            mSwitchLeftIv.setVisibility(View.VISIBLE);
            mSwitchRightIv.setVisibility(View.GONE);
            mTypeTextView.setText(getString(R.string.oem_business_chance));
            mBusinessType = "OEM商机";
            requestByDate(mMenuPosition, mBusinessType, null);
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    gridlists.clear();
                    Log.i(TAG, "json:" + msg.getData().getString("result"));
                    JSONArray jsonArray = JSON.parseObject(msg.getData().getString("result")).getJSONArray("chances");
                    if (jsonArray != null) {
                        if (jsonArray.size() > 0) {
                            showFunelView(msg.getData().getString("result"));
                        } else {
                            String json = "{\"sessionId\":\"A84EE897980B21770FD00FC69C47B27B\",\"chances\":[{\"detno\":1,\"percent\":\"0.00%\",\"count\":0,\"color\":\"485CC6\",\"currentprocess\":\"初次沟通\"},{\"detno\":2,\"percent\":\"0.00%\",\"count\":0,\"color\":\"4686CC\",\"currentprocess\":\"产品演示\"},{\"detno\":3,\"percent\":\"0.00%\",\"count\":0,\"color\":\"49B0C9\",\"currentprocess\":\"立项评估\"},{\"detno\":4,\"percent\":\"0.00%\",\"count\":0,\"color\":\"48C79E\",\"currentprocess\":\"需求分析\"},{\"detno\":5,\"percent\":\"0.00%\",\"count\":0,\"color\":\"55CC59\",\"currentprocess\":\"样品报价\"},{\"detno\":6,\"percent\":\"0.00%\",\"count\":0,\"color\":\"90BB42\",\"currentprocess\":\"商务谈判\"},{\"detno\":7,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BAA535\",\"currentprocess\":\"合同签约\"},{\"detno\":8,\"percent\":\"0.00%\",\"count\":0,\"color\":\"C7853F\",\"currentprocess\":\"完成交易\"},{\"detno\":9,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BB5743\",\"currentprocess\":\"多次交易\"}],\"success\":true}";
                            showFunelView(json);
                        }
                    } else {
                        String json = "{\"sessionId\":\"A84EE897980B21770FD00FC69C47B27B\",\"chances\":[{\"detno\":1,\"percent\":\"0.00%\",\"count\":0,\"color\":\"485CC6\",\"currentprocess\":\"初次沟通\"},{\"detno\":2,\"percent\":\"0.00%\",\"count\":0,\"color\":\"4686CC\",\"currentprocess\":\"产品演示\"},{\"detno\":3,\"percent\":\"0.00%\",\"count\":0,\"color\":\"49B0C9\",\"currentprocess\":\"立项评估\"},{\"detno\":4,\"percent\":\"0.00%\",\"count\":0,\"color\":\"48C79E\",\"currentprocess\":\"需求分析\"},{\"detno\":5,\"percent\":\"0.00%\",\"count\":0,\"color\":\"55CC59\",\"currentprocess\":\"样品报价\"},{\"detno\":6,\"percent\":\"0.00%\",\"count\":0,\"color\":\"90BB42\",\"currentprocess\":\"商务谈判\"},{\"detno\":7,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BAA535\",\"currentprocess\":\"合同签约\"},{\"detno\":8,\"percent\":\"0.00%\",\"count\":0,\"color\":\"C7853F\",\"currentprocess\":\"完成交易\"},{\"detno\":9,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BB5743\",\"currentprocess\":\"多次交易\"}],\"success\":true}";
                        showFunelView(json);
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "json:" + msg.getData().getString("result"));
                    String json = "{\"sessionId\":\"A84EE897980B21770FD00FC69C47B27B\",\"chances\":[{\"detno\":1,\"percent\":\"0.00%\",\"count\":0,\"color\":\"485CC6\",\"currentprocess\":\"初次沟通\"},{\"detno\":2,\"percent\":\"0.00%\",\"count\":0,\"color\":\"4686CC\",\"currentprocess\":\"产品演示\"},{\"detno\":3,\"percent\":\"0.00%\",\"count\":0,\"color\":\"49B0C9\",\"currentprocess\":\"立项评估\"},{\"detno\":4,\"percent\":\"0.00%\",\"count\":0,\"color\":\"48C79E\",\"currentprocess\":\"需求分析\"},{\"detno\":5,\"percent\":\"0.00%\",\"count\":0,\"color\":\"55CC59\",\"currentprocess\":\"样品报价\"},{\"detno\":6,\"percent\":\"0.00%\",\"count\":0,\"color\":\"90BB42\",\"currentprocess\":\"商务谈判\"},{\"detno\":7,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BAA535\",\"currentprocess\":\"合同签约\"},{\"detno\":8,\"percent\":\"0.00%\",\"count\":0,\"color\":\"C7853F\",\"currentprocess\":\"完成交易\"},{\"detno\":9,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BB5743\",\"currentprocess\":\"多次交易\"}],\"success\":true}";
                    showFunelView(json);
                    break;
                case Constants.APP_NOTNETWORK:
                    progressDialog.dismiss();
                    gridlists.clear();
                    ToastMessage(getString(R.string.networks_out));
                    json = "{\"sessionId\":\"A84EE897980B21770FD00FC69C47B27B\",\"chances\":[{\"detno\":1,\"percent\":\"0.00%\",\"count\":0,\"color\":\"485CC6\",\"currentprocess\":\"初次沟通\"},{\"detno\":2,\"percent\":\"0.00%\",\"count\":0,\"color\":\"4686CC\",\"currentprocess\":\"产品演示\"},{\"detno\":3,\"percent\":\"0.00%\",\"count\":0,\"color\":\"49B0C9\",\"currentprocess\":\"立项评估\"},{\"detno\":4,\"percent\":\"0.00%\",\"count\":0,\"color\":\"48C79E\",\"currentprocess\":\"需求分析\"},{\"detno\":5,\"percent\":\"0.00%\",\"count\":0,\"color\":\"55CC59\",\"currentprocess\":\"样品报价\"},{\"detno\":6,\"percent\":\"0.00%\",\"count\":0,\"color\":\"90BB42\",\"currentprocess\":\"商务谈判\"},{\"detno\":7,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BAA535\",\"currentprocess\":\"合同签约\"},{\"detno\":8,\"percent\":\"0.00%\",\"count\":0,\"color\":\"C7853F\",\"currentprocess\":\"完成交易\"},{\"detno\":9,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BB5743\",\"currentprocess\":\"多次交易\"}],\"success\":true}";
                    showFunelView(json);
                    break;
                default:
                    progressDialog.dismiss();
                    json = "{\"sessionId\":\"A84EE897980B21770FD00FC69C47B27B\",\"chances\":[{\"detno\":1,\"percent\":\"0.00%\",\"count\":0,\"color\":\"485CC6\",\"currentprocess\":\"初次沟通\"},{\"detno\":2,\"percent\":\"0.00%\",\"count\":0,\"color\":\"4686CC\",\"currentprocess\":\"产品演示\"},{\"detno\":3,\"percent\":\"0.00%\",\"count\":0,\"color\":\"49B0C9\",\"currentprocess\":\"立项评估\"},{\"detno\":4,\"percent\":\"0.00%\",\"count\":0,\"color\":\"48C79E\",\"currentprocess\":\"需求分析\"},{\"detno\":5,\"percent\":\"0.00%\",\"count\":0,\"color\":\"55CC59\",\"currentprocess\":\"样品报价\"},{\"detno\":6,\"percent\":\"0.00%\",\"count\":0,\"color\":\"90BB42\",\"currentprocess\":\"商务谈判\"},{\"detno\":7,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BAA535\",\"currentprocess\":\"合同签约\"},{\"detno\":8,\"percent\":\"0.00%\",\"count\":0,\"color\":\"C7853F\",\"currentprocess\":\"完成交易\"},{\"detno\":9,\"percent\":\"0.00%\",\"count\":0,\"color\":\"BB5743\",\"currentprocess\":\"多次交易\"}],\"success\":true}";
                    showFunelView(json);
                    break;
            }
        }
    };

    private void showFunelView(String result) {
        JSONObject root = JSON.parseObject(result);
        JSONArray chances = root.getJSONArray("chances");
        ArrayList<Integer> counts = new ArrayList<>();
        ArrayList<String> colors = new ArrayList<>();
        int countToatal = 0;
        if (!chances.isEmpty()) {
            ArrayList<String> items = new ArrayList<>();
            items.add(getString(R.string.color_text));
            // items.add("序号");
            items.add(getString(R.string.business_stage));
            items.add(getString(R.string.business_number));
            items.add(getString(R.string.business_Conversion_rate));
            gridlists.add(items);
            for (int i = 0; i < chances.size(); i++) {
                items = new ArrayList<>();
                JSONObject chanceObject = chances.getJSONObject(i);

                String currentprocess = JSONUtil.getText(chanceObject, "currentprocess");
                String percent = JSONUtil.getText(chanceObject, "percent");
                String color = JSONUtil.getText(chanceObject, "color");
                int detno = JSONUtil.getInt(chanceObject, "detno");
                int count = JSONUtil.getInt(chanceObject, "count");
                double amount = JSONUtil.getDouble(chanceObject,"amount");

                items.add("#" + color);
                colors.add("#" + color);
                // items.add(String.valueOf(i + 1));
                items.add(currentprocess);
                items.add(String.valueOf(count) + "/" + CommonUtil.doubleFormat(amount));
                counts.add(count);
                countToatal = countToatal + count;
                items.add(percent);
                gridlists.add(items);
            }
        }
        //gridlists = (ArrayList) JSON.parseArray(gridData, ArrayList.class);
        hl_adapter = new HListViewOneAdapter(ct, gridlists);
        lv_grid_dispaly.setAdapter(hl_adapter);
        layout.setVisibility(View.VISIBLE);
        Log.i(TAG, "handleMessage:colors:" + JSON.toJSONString(colors));
        Log.i(TAG, "handleMessage:counts:" + JSON.toJSONString(counts));
        Log.i(TAG, "handleMessage:countToatal:" + countToatal);
        if (countToatal == 0) {//数据为空，显示空视图
            for (int i = 0; i < counts.size(); i++) {
                counts.set(i, 50);
            }
            countToatal = counts.size() * 50;
        }
        funnelView.setData(counts, countToatal, colors);
        funnelView.animateY();
    }

    private String currentDate;

    private void sendHttpResquest(String currentdate, String type) {
        progressDialog.show();
        currentDate = currentdate;
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getBusinessChancebyMonth.action";
        Map<String, Object> params = new HashMap<>();
        if (StringUtil.isEmpty(currentdate)) {
            currentdate = "to_char(bc_recorddate,'yyyymm')=201607";
        }
        params.put("currentdate", currentdate);
        params.put("type", type);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
    }

    private PopupWindow popupWindow = null;

    public void showPopupWindow(final View menu) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = (ListView) view.findViewById(R.id.mList);
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    getPopData(),
                    R.layout.item_pop_list,
                    new String[]{"item_name"}, new int[]{R.id.tv_item_name});
            plist.setAdapter(adapter);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mMenuPosition = position;
                    requestByDate(position, mBusinessType, (TextView) menu);
                    popupWindow.dismiss();
                }
            });
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() / 3, windowManager.getDefaultDisplay().getHeight() / 3);
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        DisplayUtil.backgroundAlpha(this, 0.5f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(BusinessActivity.this, 1f);
            }
        });
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        popupWindow.showAsDropDown(menu, windowManager.getDefaultDisplay().getWidth(), 0);
    }

    private void requestByDate(int position, String type, TextView menu) {
        switch (position) {
        /*    case 0:
                String month = new SimpleDateFormat("yyyyMM").format(DateFormatUtil.getTimesMonthmorning());
                sendHttpResquest("to_char(bc_recorddate,'yyyymm')=" + month);

                break;*/
            case 0://本月
                String month = new SimpleDateFormat("yyyyMM").format(CalendarUtil.getTimesMonthmorning());
                sendHttpResquest("to_char(bc_recorddate,'yyyymm')=" + month, type);
                if (menu != null) {
                    menu.setText(getString(R.string.this_month));
                }
                break;
            case 1://上月
                String lastMonth = new SimpleDateFormat("yyyyMM").format(CalendarUtil.getLastMonthStartMorning());
                sendHttpResquest("to_char(bc_recorddate,'yyyymm')=" + lastMonth, type);
                if (menu != null) {
                    menu.setText(getString(R.string.last_month));
                }
                break;
            case 2://本季度
                String quarterStart = new SimpleDateFormat("yyyyMM").format(CalendarUtil.getCurrentQuarterStartTime());
                String quarterEnd = new SimpleDateFormat("yyyyMM").format(CalendarUtil.getCurrentQuarterEndTime());

                sendHttpResquest("to_char(bc_recorddate,'yyyymm')>=" + quarterStart + " and to_char(bc_recorddate,'yyyymm')< " + quarterEnd, type);
                if (menu != null) {
                    menu.setText(getString(R.string.this_Quarterly));
                }
                break;
            case 3://上季度
                String preStart = new SimpleDateFormat("yyyyMM").format(CalendarUtil.getPreQuarterStartTime());
                String preEnd = new SimpleDateFormat("yyyyMM").format(CalendarUtil.getPreQuarterEndTime());
                sendHttpResquest("to_char(bc_recorddate,'yyyymm')>=" + preStart + " and to_char(bc_recorddate,'yyyymm')< " + preEnd, type);
                if (menu != null) {
                    menu.setText(getString(R.string.last_Quarterly));
                }
                break;
            case 4://本年度
                String currentYear = new SimpleDateFormat("yyyy").format(CalendarUtil.getCurrentYearStartTime());
                sendHttpResquest("to_char(bc_recorddate,'yyyy')=" + currentYear, type);
                if (menu != null) {
                    menu.setText(getString(R.string.this_year));
                }
                break;
            case 5://上年度
                String preYear = new SimpleDateFormat("yyyy").format(CalendarUtil.getPreYearStartTime());
                sendHttpResquest("to_char(bc_recorddate,'yyyy')=" + preYear, type);
                if (menu != null) {
                    menu.setText(getString(R.string.last_year));
                }
                break;
        }
    }

    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();


        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.this_month));
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.last_month));
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.this_Quarterly));
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.last_Quarterly));
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.this_year));
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.last_year));
        list.add(map);

        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
//            HListViewOneAdapter.ViewHolder holder= (HListViewOneAdapter.ViewHolder) view.getTag();
            ArrayList<String> items = gridlists.get(position);
            startActivity(new Intent(ct, BusinessDetailActivty.class)
                    .putExtra("bt_type", 3)
                    .putExtra("steps", items.get(1))
                    .putExtra("currentdate", currentDate)
                    .putExtra("businessType", mBusinessType));
            // Crouton.makeText(this,"item "+items.get(2)+" is clicked",0xff99cc00,1000).show();
        }
    }


}

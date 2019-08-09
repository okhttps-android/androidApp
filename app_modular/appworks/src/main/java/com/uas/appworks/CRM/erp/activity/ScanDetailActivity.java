package com.uas.appworks.CRM.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.utils.NetUtils;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.widget.RecycleViewDivider;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.uas.appworks.CRM.erp.model.DeviceInfo;
import com.uas.appworks.R;
import com.uas.appworks.model.Device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Bitlike on 2017/11/22.
 */

public class ScanDetailActivity extends BaseActivity implements View.OnClickListener {
    private HttpClient httpClient = null;
    private RecyclerView mRecyclerView;
    private BaseRefreshLayout mRefreshLayout;

    private String decode;
    private String id;
    private DeviceInfoAdapter mAdapter;
    private List<DeviceInfo> baseInfoList;
    private List<DeviceInfo> moreInfoList;
    private List<DeviceInfo> historyInfoList;

    private int selectItem;
    private Device mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_refresh_recycler);
        initView();
        initData();
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            decode = intent.getStringExtra("decode");
            LogUtil.i("decode=" + decode);
        }
        baseInfoList = new ArrayList<>();
        moreInfoList = new ArrayList<>();
        historyInfoList = new ArrayList<>();
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        String emCode = CommonUtil.getSharedPreferences(ct, "sessionId");
        httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(MyApplication.getInstance()))
                .isDebug(true)
                .add("sessionId", sessionId)
                .add("master", CommonUtil.getSharedPreferences(ct, "erp_master"))
                .add("sessionUser", emCode)
                .add("sessionId", sessionId)
                .header("Cookie", "JSESSIONID=" + sessionId)
                .header("sessionUser", emCode)
                .build();
        selectItem = 1;
        loadData();
    }


    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.mRecyclerView);
        mRefreshLayout = (BaseRefreshLayout) findViewById(R.id.mRefreshLayout);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new RecycleViewDivider(ct, LinearLayoutManager.VERTICAL));
        initActionBar();
        mRefreshLayout.setEnabledPullUp(false);
        mRefreshLayout.setOnRefreshListener(new BaseRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }

            @Override
            public void onLoadMore() {

            }
        });
    }

    @Override
    public boolean needNavigation() {
        return false;
    }

    private void initActionBar() {
        setTitle("");
        View view = LayoutInflater.from(ct).inflate(R.layout.device_scan_head, null);
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(view);
        view.findViewById(R.id.backImg).setOnClickListener(this);
        view.findViewById(R.id.doneTv).setOnClickListener(this);
        RadioGroup selectRg = view.findViewById(R.id.selectRg);

        selectRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.baseInfoRb) {
                    setData2Adapter(1);
                } else if (checkedId == R.id.moreInfoRb) {
                    setData2Adapter(2);
                } else if (checkedId == R.id.historyRb) {
                    setData2Adapter(3);
                }
            }
        });
        RadioButton baseInfoRb = view.findViewById(R.id.baseInfoRb);
        baseInfoRb.setChecked(true);
    }

    private PopupWindow menuPop = null;

    private void showMuenPop(View v) {
        if (menuPop == null) {
            View view = LayoutInflater.from(ct).inflate(R.layout.menu_device_scan, null);
            menuPop = new PopupWindow(ct);
            menuPop.setContentView(view);
            menuPop.setBackgroundDrawable(ct.getResources().getDrawable(R.color.white));
            menuPop.setTouchable(true);
            DisplayUtil.backgroundAlpha(ct, 0.4f);
            menuPop.setOutsideTouchable(false);
            menuPop.setFocusable(true);
            menuPop.setWidth(DisplayUtil.dip2px(ct, 100));
            menuPop.setHeight(DisplayUtil.dip2px(ct, 200));
            menuPop.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    DisplayUtil.backgroundAlpha(ct, 1.0f);
                }
            });
//            view.findViewById(R.id.popFindBtn).setOnClickListener(this);
            view.findViewById(R.id.popUseApplyBtn).setOnClickListener(this);
            view.findViewById(R.id.popScrapApplyBtn).setOnClickListener(this);
            view.findViewById(R.id.popMaintenanceBtn).setOnClickListener(this);
            view.findViewById(R.id.popInspectBtn).setOnClickListener(this);
//            view.findViewById(R.id.popCycleCountBtn).setOnClickListener(this);
        }
        DisplayUtil.backgroundAlpha(ct, 0.4f);
        menuPop.showAsDropDown(v, 0, DisplayUtil.dip2px(ct, 10));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.backImg == id) {
            onBackPressed();
        } else if (R.id.doneTv == id) {
            showMuenPop(v);
        } else if (R.id.popUseApplyBtn == id) {
            startActivity(new Intent(ct, DeviceDataFormAddActivity.class).putExtra("title", StringUtil.getMessage(R.string.text_application_use)).putExtra("caller", "DeviceChange!Use").putExtra(Constants.FLAG.MODEL, mDevice));
            dismissMenuPop();
        } else if (R.id.popScrapApplyBtn == id) {
            startActivity(new Intent(ct, DeviceDataFormAddActivity.class).putExtra("title", StringUtil.getMessage(R.string.text_scrap_application)).putExtra("caller", "DeviceChange!Scrap").putExtra(Constants.FLAG.MODEL, mDevice));
            dismissMenuPop();
        } else if (R.id.popMaintenanceBtn == id) {
            startActivity(new Intent(ct, DeviceDataFormAddActivity.class).putExtra("title", StringUtil.getMessage(R.string.text_maintenance)).putExtra("caller", "DeviceChange!Maintain").putExtra(Constants.FLAG.MODEL, mDevice));
            dismissMenuPop();
        } else if (R.id.popInspectBtn == id) {
            startActivity(new Intent(ct, DeviceDataFormAddActivity.class).putExtra("title", StringUtil.getMessage(R.string.text_fault_inspection)).putExtra("caller", "DeviceChange!Inspect").putExtra(Constants.FLAG.MODEL, mDevice));
            dismissMenuPop();
        }

    }

    private void dismissMenuPop() {
        if (menuPop != null) {
            menuPop.dismiss();
        }
    }


    private void loadData() {

        if (NetUtils.isNetWorkConnected(ct)) {
            if (!mRefreshLayout.isRefreshing()) {
                progressDialog.show();
            }
            httpClient.Api().send(new HttpClient.Builder()
                    .url("mobile/device/getDeviceInfo.action")
                    .add("decode", decode)
                    .isDebug(BaseConfig.isDebug())
                    .method(Method.GET)
                    .build(), new ResultSubscriber<>(new ResultListener<Object>() {
                @Override
                public void onResponse(Object o) {
                    try {
                        handleData(o.toString());
                    } catch (Exception e) {
                        if (e != null) {
                            LogUtil.i("e=" + e.getMessage());
                        }
                    }
                    if (progressDialog != null) {
                        progressDialog.dismiss();

                    }
                    if (mRefreshLayout != null) {
                        mRefreshLayout.stopRefresh();
                    }

                }
            }));
        } else {
            if (!mRefreshLayout.isRefreshing()) {
                endOfError(getString(R.string.networks_out));
            }
        }
    }

    private void endOfError(String exceptionInfo) {
        setResult(0x22, new Intent().putExtra("data", exceptionInfo));
        finish();
    }

    private Comparator<Sort> comparator = new Comparator<Sort>() {
        @Override
        public int compare(Sort o1, Sort o2) {
            return o1.sort - o2.sort;
        }
    };

    private void handleData(String message) throws Exception {
        LogUtil.i(message);
        JSONObject json = JSON.parseObject(message);
        boolean success = JSONUtil.getBoolean(json, "success");
        JSONObject object = JSONUtil.getJSONObject(json, "data");
        if (success) {
            //处理主表
            JSONArray formItem = JSONUtil.getJSONArray(object, "formItem");
            JSONObject formData = JSONUtil.getJSONObject(object, "formData");
            List<DeviceInfo> formListData = handleFromGrid(true, null, formItem, formData);
            //详细配置
            JSONArray grid1Data = JSONUtil.getJSONArray(object, "grid1Data");
            List<DeviceInfo> moreListData = new ArrayList<>();
            JSONArray grid1Item = JSONUtil.getJSONArray(object, "grid1Item");
            for (int i = 0; i < grid1Data.size(); i++) {
                moreListData.addAll(handleFromGrid(false, "详细配置" + (i + 1), grid1Item, grid1Data.getJSONObject(i)));
            }
            //设备履历数据
            JSONArray grid2Item = JSONUtil.getJSONArray(object, "grid2Item");
            JSONArray grid2Data = JSONUtil.getJSONArray(object, "grid2Data");
            List<Sort> sorts = new ArrayList<>();
            for (int i = 0; i < grid2Data.size(); i++) {
                JSONObject o = grid2Data.getJSONObject(i);
                sorts.add(new Sort(JSONUtil.getInt(o, "DC_ID"), o));
            }
            Collections.sort(sorts, comparator);
            List<DeviceInfo> historyListData = new ArrayList<>();
            for (int i = 0; i < sorts.size(); i++) {
                historyListData.addAll(handleFromGrid(false, "设备履历" + (i + 1), grid2Item, sorts.get(i).object));
            }
            setData2Adapter(formListData, moreListData, historyListData);
        } else {
            String exceptionInfo = JSONUtil.getText(message, "exceptionInfo");
            if (!StringUtil.isEmpty(exceptionInfo)) {
                ToastUtil.showToast(ct, exceptionInfo);
            }
            endOfError(exceptionInfo);
        }

    }

    private void setData2Adapter(int selectItem) {
        this.selectItem = selectItem;
        List<DeviceInfo> listData = null;
        switch (selectItem) {
            case 1:
                listData = this.baseInfoList;
                break;
            case 2:
                listData = this.moreInfoList;
                break;
            case 3:
                listData = this.historyInfoList;
                break;
        }

        if (mAdapter == null) {
            mAdapter = new DeviceInfoAdapter(listData);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setModels(listData);
        }
    }

    private void setData2Adapter(List<DeviceInfo> baseInfoList, List<DeviceInfo> moreInfoList, List<DeviceInfo> historyInfoList) {
        this.baseInfoList = baseInfoList;
        this.moreInfoList = moreInfoList;
        this.historyInfoList = historyInfoList;
        setData2Adapter(selectItem);
    }


    private List<DeviceInfo> handleFromGrid(boolean isFrom, String title, JSONArray items, JSONObject data) throws Exception {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        mDevice = new Device();
        mDevice.setCode(decode);
        for (int i = 0; i < items.size(); i++) {
            JSONObject item = items.getJSONObject(i);
            String field = JSONUtil.getText(item, "field", "dataIndex");
            String caption = JSONUtil.getText(item, "caption");
            String type = JSONUtil.getText(item, "type");
            String values = getValues(type, JSONUtil.getText(data, field, field.toUpperCase()));
            if (isFrom && caption.equals("ID")) {
                id = values;
            }

            switch (caption) {
                case "使用车间":
                    mDevice.setWorkshop(values);
                    break;
                case "设备名称":
                    mDevice.setName(values);
                    break;
                case "设备编号":
                    if (!TextUtils.isEmpty(values)) {
                        mDevice.setCode(values);
                    }
                    break;
                case "当前线别":
                    mDevice.setLineCode(values);
                    break;
                case "当前使用部门":
                    mDevice.setCenterCode(values);
                    break;
                case "当前部门名称":
                    mDevice.setCenterName(values);
                    break;
            }

            deviceInfos.add(new DeviceInfo(isFrom, i == 0 ? title : null, caption, field, values));
        }

        return deviceInfos;
    }

    private String getValues(String type, String values) {
        if (type.equals("combo")) {
            switch (values) {
                case "UNUSED":
                    return "闲置中";
                case "USING":
                    return "正常使用";
                case "BREAKING":
                    return "故障中";
                case "SCRAPPED":
                    return "已报废";
                case "LOSSED":
                    return "已盘亏";
            }
        } else if ("datefield".equals(type)) {
            try {
                long time = Long.valueOf(values);
                return DateFormatUtil.long2Str(time, DateFormatUtil.YMD);
            } catch (Exception e) {
                if (values.length() > 8) {
                    values = values.substring(0, values.length() - 8);
                }

            }
        }
        return values;
    }

    private class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoAdapter.ViewHolder> {

        List<DeviceInfo> models;

        public DeviceInfoAdapter(List<DeviceInfo> models) {
            this.models = models;
        }

        public void setModels(List<DeviceInfo> models) {
            this.models = models;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DeviceInfo model = models.get(position);
            if (StringUtil.isEmpty(model.getTitle())) {
                holder.titleRl.setVisibility(View.GONE);
            } else {
                holder.titleRl.setVisibility(View.VISIBLE);
                holder.titleTv.setText(model.getTitle());
            }
            holder.captionTv.setText(model.getCaption());
            holder.valuesTv.setText(model.getValues());
        }

        @Override
        public int getItemCount() {
            return ListUtils.getSize(models);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout titleRl;
            TextView titleTv, captionTv, valuesTv;

            public ViewHolder(ViewGroup parent) {
                this(LayoutInflater.from(ct).inflate(R.layout.item_device_info, parent, false));
            }

            public ViewHolder(View itemView) {
                super(itemView);
                titleRl = itemView.findViewById(R.id.titleRl);
                titleTv = itemView.findViewById(R.id.titleTv);
                captionTv = itemView.findViewById(R.id.captionTv);
                valuesTv = itemView.findViewById(R.id.valuesTv);
            }
        }
    }


    private class Sort {
        int sort;
        JSONObject object;

        public Sort(int sort, JSONObject object) {
            this.sort = sort;
            this.object = object;
        }
    }
}

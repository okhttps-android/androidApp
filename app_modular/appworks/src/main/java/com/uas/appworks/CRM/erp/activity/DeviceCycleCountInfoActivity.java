package com.uas.appworks.CRM.erp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.module.recyclerlibrary.ui.refresh.BaseRefreshLayout;
import com.module.recyclerlibrary.ui.refresh.simlpe.SimpleRefreshLayout;
import com.uas.appworks.CRM.erp.adapter.CycleCountAdapter;
import com.uas.appworks.CRM.erp.model.CycleCount;
import com.uas.appworks.R;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitlike on 2017/12/19.
 */

public class DeviceCycleCountInfoActivity extends SupportToolBarActivity implements View.OnClickListener {
    private HttpClient httpClient = null;
    private String id;
    private TextView actionqtyTv;
    private TextView unactionqtyTv;
    private TextView codeTv, workshopTv;
    private ListView mListView;
    private TextView centerTv;
    private TextView lineTv;
    private TextView kindTv;
    private Button mObtainButton;
    private RecyclerView gridList;
    private SimpleRefreshLayout mSimpleRefreshLayout;
    private int page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_cycle_info);
        initView();
        initData();
    }

    private void initData() {
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        String emCode = CommonUtil.getEmcode();
        httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(MyApplication.getInstance()))
                .isDebug(true)
                .add("sessionId", sessionId)
                .add("master", CommonUtil.getSharedPreferences(ct, "erp_master"))
                .add("sessionUser", emCode)
                .add("sessionId", sessionId)
                .header("Cookie", "JSESSIONID=" + sessionId)
                .header("sessionUser", emCode)
                .build();
        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra("id");
            try {
                initJSONData(intent.getStringExtra("models"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadData(true);
        loadGridData(page = 1);
    }


    private void initJSONData(String message) throws Exception {
        JSONArray array = JSON.parseArray(message);
        JSONObject object;
        CycleCount.Data data;
        List<CycleCount.Data> models = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            String caption = JSONUtil.getText(object, "caption");
            boolean hasTwo = JSONUtil.getBoolean(object, "hasTwo");
            String values = JSONUtil.getText(object, "values");
            String caption2 = JSONUtil.getText(object, "caption2");
            String values2 = JSONUtil.getText(object, "values2");
            data = new CycleCount.Data(caption, values);
            data.setHasTwo(hasTwo);
            data.setCaption2(caption2);
            data.setValues2(values2);
            models.add(data);
        }
        mListView.setAdapter(new CycleCountAdapter.DataAdapter(ct, models));
    }

    private void initView() {
        codeTv = findViewById(R.id.codeTv);
        workshopTv = findViewById(R.id.workshopTv);
        gridList = findViewById(R.id.gridList);
        mSimpleRefreshLayout = findViewById(R.id.mSimpleRefreshLayout);
        centerTv = findViewById(R.id.centerTv);
        lineTv = findViewById(R.id.lineTv);
        kindTv = findViewById(R.id.kindTv);
        mListView = findViewById(R.id.mListView);
        actionqtyTv = findViewById(R.id.actionqtyTv);
        unactionqtyTv = findViewById(R.id.unactionqtyTv);
        findViewById(R.id.actionqtyTv).setOnClickListener(this);
        findViewById(R.id.unactionqtyTv).setOnClickListener(this);
        findViewById(R.id.cycleBtn).setOnClickListener(this);
        mObtainButton = findViewById(R.id.deviceAttributeBtn);
        if ("N_MALATA_ZZ".equals(CommonUtil.getMaster()) || "N_MALATA_ZZ_ZS".equals(CommonUtil.getMaster())) {
            mObtainButton.setText("刷新");
        } else {
            mObtainButton.setText("获取明细");
        }
        mObtainButton.setOnClickListener(this);
        findViewById(R.id.lossDeviceBtn).setOnClickListener(this);
        mSimpleRefreshLayout.setEnabledPullUp(true);
        mSimpleRefreshLayout.setEnablePullDown(false);
        gridList.setItemAnimator(new DefaultItemAnimator());
        gridList.setLayoutManager(new LinearLayoutManager(ct));
        mSimpleRefreshLayout.setOnRefreshListener(new BaseRefreshLayout.onRefreshListener() {
            @Override
            public void onRefresh() {

            }

            @Override
            public void onLoadMore() {
                page++;
                loadGridData(page);
            }
        });
    }


    private void loadData(boolean showDialog) {
        if (showDialog) {
            progressDialog.show();
        }
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/device/getCheckQty.action")
                .add("caller", "DeviceBatch!Stock")
                .add("id", id)
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
            }
        }));
    }

    private void submitCycle(String de_code) {
        progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/device/deviceStock.action")
                .add("caller", "DeviceBatch!Stock")
                .add("id", id)
                .add("de_code", de_code)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                try {
                    handleSubmitCycle(o.toString());
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }

            }
        }));
    }

    private void loadGridData(int page) {
        String condition = "dc_dbid=" + id;
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/common/getGridPanelandDataPage.action")
                .add("caller", "DeviceBatch!Stock")
                .add("condition", condition)
                .add("page", page)
                .add("pageSize", 50)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    handlerGridData(o.toString());
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }
                mSimpleRefreshLayout.stopRefresh();

            }
        }));
    }

    private void deviceAttribute() {
        progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/device/getDeviceAttribute.action")
                .add("caller", "DeviceBatch!Stock")
                .add("id", id)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                try {
                    handlerAttribute(o.toString());
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }

            }
        }));
    }

    private void lossDevice() {
        progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/device/lossDevice.action")
                .add("caller", "DeviceBatch!Stock")
                .add("id", id)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                try {
                    handlerLoss(o.toString());
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }

            }
        }));
    }

    private void handlerLoss(String message) {
        JSONObject object = JSON.parseObject(message);
        if (object.containsKey("exceptionInfo")) {
            ToastUtil.showToast(ct, JSONUtil.getText(object, "exceptionInfo"));
        } else {
            if (JSONUtil.getBoolean(object, "success")) {
                ToastUtil.showToast(ct, "盘亏成功");
                loadGridData(page = 1);
                loadData(false);
            }

        }
        LogUtil.i(message);
    }

    private void handlerAttribute(String message) {
        JSONObject object = JSON.parseObject(message);
        if (object.containsKey("exceptionInfo")) {
            ToastUtil.showToast(ct, JSONUtil.getText(object, "exceptionInfo"));
        } else {
            if (JSONUtil.getBoolean(object, "success")) {
                loadGridData(page = 1);
                loadData(false);
            }
        }
        LogUtil.i(message);
    }

    private GridAdapter mAdapter = null;

    private void handlerGridData(String message) {
        JSONObject object = JSON.parseObject(message);
        if (object.containsKey("exceptionInfo")) {
            ToastUtil.showToast(ct, JSONUtil.getText(object, "exceptionInfo"));
        } else {
            JSONArray array = JSONUtil.getJSONArray(object, "gridItem");
            JSONArray datas = JSONUtil.getJSONArray(object, "gridData");
            List<Grid> grids = new ArrayList<>();

            for (int j = 0; j < datas.size(); j++) {
                JSONObject data = datas.getJSONObject(j);
                boolean isTop = true;
                for (int i = 0; i < array.size(); i++) {
                    JSONObject config = array.getJSONObject(i);
                    String dataIndex = JSONUtil.getText(config, "dataIndex");
                    Grid grid = new Grid();
                    grid.caption = JSONUtil.getText(config, "caption");
                    grid.values = JSONUtil.getText(data, dataIndex.toUpperCase());
                    if (!TextUtils.isEmpty(grid.caption) && !TextUtils.isEmpty(grid.values)) {
                        if (isTop) {
                            grid.isTop = isTop;
                            isTop = false;
                        }
                        grids.add(grid);
                    }
                }
            }

            if (mAdapter == null) {
                mAdapter = new GridAdapter(grids);
                gridList.setAdapter(mAdapter);
            } else {
                if (page <= 1) {
                    mAdapter.setModels(grids);
                } else {
                    mAdapter.addModels(grids);
                }
            }
        }
        LogUtil.i(message);
    }

    private void handleData(String message) throws Exception {
        JSONObject object = JSONUtil.getJSONObject(message, "data");
        String code = JSONUtil.getText(object, "DB_CODE");
        String workShop = JSONUtil.getText(object, "DB_WORKSHOP");
        String centername = JSONUtil.getText(object, "DB_CENTERNAME");
        String linecode = JSONUtil.getText(object, "DB_LINECODE");
        String devkind = JSONUtil.getText(object, "DB_DEVTYPE");
        String actionqty = JSONUtil.getText(object, "DB_ACTIONQTY");
        String unactionqty = JSONUtil.getText(object, "DB_UNACTIONQTY");
        codeTv.setText(code);
        workshopTv.setText(workShop);
        centerTv.setText(centername);
        lineTv.setText(linecode);
        kindTv.setText(devkind);
        actionqtyTv.setText(Html.fromHtml("<u>" + actionqty + "</u>"));
        unactionqtyTv.setText(Html.fromHtml("<u>" + unactionqty + "</u>"));
        LogUtil.i("message=" + message);
    }

    private void handleSubmitCycle(String message) throws Exception {
        boolean success = JSONUtil.getBoolean(message, "success");
        if (success) {
            JSONObject object = JSONUtil.getJSONObject(message, "data");
            String popCode = JSONUtil.getText(object, "DE_CODE");
            String popName = JSONUtil.getText(object, "DE_NAME");
            String popGui = JSONUtil.getText(object, "DE_SPEC");
            String popUnActNum = String.valueOf(JSONUtil.getInt(object, "DB_UNACTIONQTY"));
            showMuenPop(popCode, popName, popGui, popUnActNum);
            loadData(false);
        } else {
            String exceptionInfo = JSONUtil.getText(message, "exceptionInfo");
            if (!StringUtil.isEmpty(exceptionInfo)) {
                showDialog(exceptionInfo);
            }
        }
    }

    private PopupWindow mPopupWindow;
    private TextView popCodeTv;
    private TextView popNameTv;
    private TextView popGuiTv;
    private TextView popUnActNumTv;

    private void showMuenPop(String popCode, String popName, String popGui, String popUnActNum) {
        View view = LayoutInflater.from(ct).inflate(R.layout.pop_submit_cycle, null);
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(ct);
            mPopupWindow.setContentView(view);
            mPopupWindow.setBackgroundDrawable(ct.getResources().getDrawable(R.color.white));
            mPopupWindow.setTouchable(true);
            DisplayUtil.backgroundAlpha(ct, 0.4f);
            mPopupWindow.setOutsideTouchable(false);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setWidth(DisplayUtil.dip2px(ct, 300));
            mPopupWindow.setHeight(DisplayUtil.dip2px(ct, 250));
            mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    DisplayUtil.backgroundAlpha(ct, 1.0f);
                }
            });
            popCodeTv = view.findViewById(R.id.popCodeTv);
            popNameTv = view.findViewById(R.id.popNameTv);
            popGuiTv = view.findViewById(R.id.popGuiTv);
            popUnActNumTv = view.findViewById(R.id.popUnActNumTv);
            view.findViewById(R.id.nextBtn).setOnClickListener(this);
            view.findViewById(R.id.backBtn).setOnClickListener(this);
        }
        if (popCodeTv != null) {
            popCodeTv.setText(popCode);
        }
        if (popNameTv != null) {
            popNameTv.setText(popName);
        }
        if (popGuiTv != null) {
            popGuiTv.setText(popGui);
        }
        if (popUnActNumTv != null) {
            popUnActNumTv.setText(popUnActNum);
        }
        DisplayUtil.backgroundAlpha(ct, 0.4f);
        if (!mPopupWindow.isShowing()) {
            mPopupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.actionqtyTv) {
            startActivity(new Intent(ct, DeviceCycleCountDetailsActivity.class).putExtra("isAct", true)
                    .putExtra("id", this.id));
        } else if (id == R.id.unactionqtyTv) {
            startActivity(new Intent(ct, DeviceCycleCountDetailsActivity.class).putExtra("isAct", false)
                    .putExtra("id", this.id));
        } else if (id == R.id.cycleBtn || R.id.nextBtn == id) {
            requestPermission(Manifest.permission.CAMERA, new Runnable() {
                @Override
                public void run() {
                    startActivityForResult(new Intent(ct, CaptureActivity.class), 0x21);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showToast(ct, R.string.not_camera_permission);
                }
            });
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        } else if (R.id.backBtn == id) {
            if (mPopupWindow != null && mPopupWindow.isShowing()) {
                mPopupWindow.dismiss();
            }
        } else if (R.id.deviceAttributeBtn == id) {
            if ("N_MALATA_ZZ".equals(CommonUtil.getMaster()) || "N_MALATA_ZZ_ZS".equals(CommonUtil.getMaster())) {
                loadData(true);
                loadGridData(page = 1);
            } else {
                deviceAttribute();
            }
        } else if (R.id.lossDeviceBtn == id) {
            lossDevice();
        }
    }

    private void showDialog(String message) {
        if (StringUtil.isEmpty(message)) return;
        new MaterialDialog.Builder(ct)
                .title(R.string.app_dialog_title)
                .content(message)
                .positiveText(MyApplication.getInstance().getString(R.string.app_dialog_ok))
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x21 && resultCode == Activity.RESULT_OK && data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    submitCycle(result);
                }
            }
        }
    }


    private class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

        List<Grid> models;

        public GridAdapter(List<Grid> models) {
            this.models = models;
        }

        public void setModels(List<Grid> models) {
            this.models = models;
            notifyDataSetChanged();
        }

        public void addModels(List<Grid> models) {
            if (this.models == null) {
                this.models = new ArrayList<>();
            }
            int sizeOld = this.models.size();
            this.models.addAll(models);
            notifyItemRangeChanged(sizeOld, this.models.size());
        }

        @Override
        public GridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new GridAdapter.ViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(GridAdapter.ViewHolder holder, int position) {
            Grid model = models.get(position);
            holder.line.setVisibility(model.isTop ? View.VISIBLE : View.GONE);
            holder.captionTv.setText(model.caption == null ? "" : model.caption);
            holder.valuesTv.setText(model.values == null ? "" : model.values);
        }

        @Override
        public int getItemCount() {
            return ListUtils.getSize(models);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            View line;
            TextView captionTv, valuesTv;

            public ViewHolder(ViewGroup parent) {
                this(LayoutInflater.from(ct).inflate(R.layout.item_cycle_grid, parent, false));
            }

            public ViewHolder(View itemView) {
                super(itemView);
                line = itemView.findViewById(R.id.line);
                captionTv = itemView.findViewById(R.id.captionTv);
                valuesTv = itemView.findViewById(R.id.valuesTv);
            }
        }
    }

    private class Grid {
        boolean isTop;
        String caption;
        String values;
    }
}

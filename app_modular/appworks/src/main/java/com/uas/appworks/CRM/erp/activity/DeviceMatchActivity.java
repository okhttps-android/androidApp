package com.uas.appworks.CRM.erp.activity;

import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.activity.BaseNetActivity;
import com.modular.apputils.activity.SelectNetAcitivty;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.uas.appworks.R;
import com.uas.appworks.model.DeviceMatch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bitlike on 2018/2/28.
 */

public class DeviceMatchActivity extends BaseNetActivity implements View.OnClickListener, OnSmartHttpListener {
    private final int LOAD_TO_DO = 1;

    private TextView centerCodeEd, centerNameEd;
    private TextView lineCodeEd;
    private TextView workShopEd;
    private TextView devModelEd;
    private RecyclerView mRecyclerView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_match;
    }

    @Override
    protected String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(this);
    }

    @Override
    protected void init() throws Exception {
        initView();
    }

    private void initView() {
        centerCodeEd = findViewById(R.id.centerCodeEd);
        centerNameEd = findViewById(R.id.centerNameEd);
        lineCodeEd = findViewById(R.id.lineCodeEd);
        workShopEd = findViewById(R.id.workShopEd);
        devModelEd = findViewById(R.id.devModelEd);

        findViewById(R.id.cancelTv).setOnClickListener(this);
        findViewById(R.id.resetTv).setOnClickListener(this);
        findViewById(R.id.confirmTv).setOnClickListener(this);
        centerCodeEd.setOnClickListener(this);
        centerNameEd.setOnClickListener(this);
        lineCodeEd.setOnClickListener(this);
        workShopEd.setOnClickListener(this);
        devModelEd.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ct, LinearLayout.VERTICAL));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    private void loadData(String centercode, String linecode, String workshop, String devmodel) {
        showProgress();
        Parameter.Builder builder = new Parameter.Builder();
        builder.url("mobile/device/getDevModelInfo.action")
                .record(LOAD_TO_DO)
                .addParams("centercode", centercode)
                .addParams("linecode", linecode)
                .addParams("workshop", workshop)
                .addParams("devmodel", devmodel)
                .showLog(true)
                .mode(Method.GET);
        requestCompanyHttp(builder, this);

    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.cancelTv) {

        } else if (id == R.id.resetTv) {
            centerCodeEd.setText("");
            centerNameEd.setText("");
            workShopEd.setText("");
            lineCodeEd.setText("");
            devModelEd.setText("");
        } else if (id == R.id.confirmTv) {
            String centercode = StringUtil.getText(centerCodeEd);
            String workshop = StringUtil.getText(workShopEd);
            String linecode = StringUtil.getText(lineCodeEd);
            String devmodel = StringUtil.getText(devModelEd);
            loadData(centercode, linecode, workshop, devmodel);
        } else {
            String name = "";
            if (id == R.id.centerCodeEd || id == R.id.centerNameEd) {
                name = "centercode";
            } else if (id == R.id.workShopEd) {
                name = "workshop";
            } else if (id == R.id.lineCodeEd) {
                name = "linecode";
            } else if (id == R.id.devModelEd) {
                name = "devmodel";
            }
            LogUtil.i("name=" + name);
            if (!TextUtils.isEmpty(name)) {
                startActivityForResult(new Intent(ct, SelectNetAcitivty.class)
                                .putExtra("fieldKey", name)
                                .putExtra("caller", "ModelRequire")
                                .putExtra("dataForm", true)
                                .putExtra("isForm", true)//是否是主表
                        , 90);
            }
        }
    }

    @Override
    public void onSuccess(int what, String message, Tags tag) throws Exception {
        JSONObject object = JSON.parseObject(message);
        switch (what) {
            case LOAD_TO_DO:
                if (JSONUtil.getBoolean(object, "success")) {
                    handerData(JSONUtil.getJSONArray(object, "data"));
                }
                break;
        }
    }

    @Override
    public void onFailure(int what, String message, Tags tag) throws Exception {
        if (JSONUtil.validateJSONObject(message)) {
            String exceptionInfo = JSONUtil.getText(message, "exceptionInfo");
            if (!TextUtils.isEmpty(exceptionInfo)) {
                ToastUtil.showToast(ct, exceptionInfo);
            }
        }
        handerData(new JSONArray());
    }

    private void handerData(JSONArray array) throws Exception {
        List<DeviceMatch> matches = new ArrayList<>();
        if (!ListUtils.isEmpty(array)) {
            JSONObject object = null;
            DeviceMatch matche = null;
            for (int i = 0; i < array.size(); i++) {
                object = array.getJSONObject(i);
                matche = new DeviceMatch();
                matche.setCode(JSONUtil.getText(object, "DM_CODE"));
                matche.setSpec(JSONUtil.getText(object, "DE_SPEC"));
                matche.setName(JSONUtil.getText(object, "DE_NAME"));
                matche.setExistqty(JSONUtil.getText(object, "EXISTQTY"));
                matche.setLackqty(JSONUtil.getText(object, "LACKQTY"));
                matche.setNeedqty(JSONUtil.getText(object, "NEEDQTY"));
                matches.add(matche);
            }
        }
        setData2View(matches);

    }

    private DeviceMatchAdapter mAdapter = null;

    private void setData2View(List<DeviceMatch> matches) {
        if (mAdapter == null) {
            mAdapter = new DeviceMatchAdapter(matches);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setMatches(matches);
        }
    }


    private class DeviceMatchAdapter extends RecyclerView.Adapter<DeviceMatchAdapter.ViewHolder> {
        private List<DeviceMatch> matches;

        public DeviceMatchAdapter(List<DeviceMatch> matches) {
            this.matches = matches;
        }

        public void setMatches(List<DeviceMatch> matches) {
            this.matches = matches;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(ct).inflate(R.layout.item_device_match, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            DeviceMatch match = matches.get(position);
            holder.specTv.setText(match.getSpec());
            holder.nameTv.setText(match.getName());
            holder.existqtyTv.setText(match.getExistqty());
            holder.lackqtyTv.setText(match.getLackqty());
            holder.needqtyTv.setText(match.getNeedqty());
        }

        @Override
        public int getItemCount() {
            return ListUtils.getSize(matches);
        }


        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView specTv;
            private TextView nameTv;
            private TextView existqtyTv;
            private TextView lackqtyTv;
            private TextView needqtyTv;

            public ViewHolder(View itemView) {
                super(itemView);
                specTv = itemView.findViewById(R.id.specTv);
                nameTv = itemView.findViewById(R.id.nameTv);
                existqtyTv = itemView.findViewById(R.id.existqtyTv);
                lackqtyTv = itemView.findViewById(R.id.lackqtyTv);
                needqtyTv = itemView.findViewById(R.id.needqtyTv);
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (90 == requestCode && data != null) {
            String json = data.getStringExtra("data");
            if (JSONUtil.validateJSONObject(json)) try {
                JSONObject object = JSON.parseObject(json);
                String centercode = JSONUtil.getText(object, "centercode");
                String centerName = JSONUtil.getText(object, "centername");
                String workshop = JSONUtil.getText(object, "workshop");
                String linecode = JSONUtil.getText(object, "linecode");
                String devmodel = JSONUtil.getText(object, "devmodel");

                if (!TextUtils.isEmpty(centercode)) {
                    centerCodeEd.setText(centercode);
                }
                if (!TextUtils.isEmpty(centerName)) {
                    centerNameEd.setText(centerName);
                }
                if (!TextUtils.isEmpty(workshop)) {
                    workShopEd.setText(workshop);
                }
                if (!TextUtils.isEmpty(linecode)) {
                    lineCodeEd.setText(linecode);
                }
                if (!TextUtils.isEmpty(devmodel)) {
                    devModelEd.setText(devmodel);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

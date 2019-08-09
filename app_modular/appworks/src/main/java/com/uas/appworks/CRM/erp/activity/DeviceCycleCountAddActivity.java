package com.uas.appworks.CRM.erp.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.Approval;
import com.core.model.SelectBean;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.listener.EditChangeListener;
import com.core.widget.view.Activity.SelectActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.activity.SelectNetAcitivty;
import com.uas.appworks.CRM.erp.model.CycleCountAdd;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bitlike on 2017/11/22.
 */

public class DeviceCycleCountAddActivity extends SupportToolBarActivity implements View.OnClickListener {
    private HttpClient httpClient = null;
    private RecyclerView popListView = null;
    private PopAdapter popAdapter = null;
    private List<CycleCountAdd> cycleCountAdds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_device_cyclecount);
        initView();
        initData();
    }

    private void initView() {
        popListView = findViewById(R.id.mRecyclerView);
        findViewById(R.id.cancelBtn).setOnClickListener(this);
        findViewById(R.id.sureBtn).setOnClickListener(this);
        findViewById(R.id.resetBtn).setOnClickListener(this);
        popListView.setLayoutManager(new LinearLayoutManager(ct));
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.cancelBtn == id) {
            finish();
        } else if (R.id.resetBtn == id) {
            if (popAdapter != null) {
                popAdapter.reset();
            }
        } else if (R.id.sureBtn == id) {
            if (popAdapter != null) {
                addCycle(popAdapter.getModels());
            }
        }
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent == null) {
            ToastUtil.showToast(ct, R.string.data_exception);
            finish();
        } else {
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
            loadPopData();
        }
    }


    private void loadPopData() {
        progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/common/getFormPanel.action")
                .add("caller", "DeviceBatch!Stock")
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    handlePopData(o.toString());
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

    private boolean isSubmitl;

    private void addCycle(List<CycleCountAdd> models) {
        Map<String, Object> formStore = new HashMap<>();
        for (CycleCountAdd e : models) {
            if (!StringUtil.isEmpty(e.getValues())) {
                formStore.put(e.getField(), e.getValues());
            } else if (!e.isAllowblank()) {
                ToastUtil.showToast(ct, e.getCaption() + "为必填项，请输入后提交");
                return;
            }
        }
        if (isSubmitl) return;
        isSubmitl = true;
        progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/device/saveAndSubmitDeviceStock.action")
                .add("caller", "DeviceBatch!Stock")
                .add("formStore", JSONUtil.map2JSON(formStore))
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    boolean success = JSONUtil.getBoolean(o.toString(), "success");
                    if (success) {
                        setResult(0x12);
                        ToastUtil.showToast(ct, "添加成功");
                        finish();
                    } else {
                        String exceptionInfo = JSONUtil.getText(o.toString(), "exceptionInfo");
                        if (!StringUtil.isEmpty(exceptionInfo)) {
                            ToastUtil.showToast(ct, exceptionInfo);
                        }
                    }
                    isSubmitl = false;

                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }
            }
        }));
    }

    private void handlePopData(String message) throws Exception {
        cycleCountAdds = new ArrayList<>();
        JSONObject data = JSONUtil.getJSONObject(message, "data");
        JSONArray items = JSONUtil.getJSONArray(data, "formdetail");
        for (int i = 0; i < items.size(); i++) {
            cycleCountAdds.add(new CycleCountAdd(items.getJSONObject(i)));
        }
        popAdapter = new PopAdapter(cycleCountAdds);
        popListView.setAdapter(popAdapter);
    }


    private class PopAdapter extends RecyclerView.Adapter<PopAdapter.ViewHodler> {
        private List<CycleCountAdd> models;
        private Drawable drawable;

        public List<CycleCountAdd> getModels() {
            return models;
        }

        public void updateValues(String message) {
            if (!StringUtil.isEmpty(message) && JSONUtil.validateJSONObject(message)) {
                LogUtil.i("message=" + message);
                JSONObject object = JSON.parseObject(message);
                if (models != null) {
                    for (int i = 0; i < models.size(); i++) {
                        LogUtil.i("e=" + JSON.toJSONString(models.get(i)));
                        if (object.containsKey(models.get(i).getField())) {
                            LogUtil.i("Field=" + models.get(i).getField());
                            models.get(i).setValues(JSONUtil.getText(object, models.get(i).getField()));
                            notifyItemChanged(i);
                        }
                    }
                    for (CycleCountAdd e : models) {
                        LogUtil.i(JSON.toJSONString(e));
                    }
                }
            }
        }

        public PopAdapter(List<CycleCountAdd> models) {
            this.models = models;
            drawable = getResources().getDrawable(R.drawable.ic_menu_retract);
        }

        private class TextChangListener extends EditChangeListener {
            ViewHodler hodler;
            private int position;

            public TextChangListener(ViewHodler hodler, int position) {
                this.hodler = hodler;
                this.position = position;
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (this.position >= 0) {
                    if (this.hodler.valuesEd != null) {
                        String valueEt = this.hodler.valuesEd.getText().toString();
                        models.get(this.position).setValues(valueEt == null ? "" : valueEt);
                    }
                }
            }
        }

        @Override
        public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHodler(parent);
        }

        @Override
        public void onBindViewHolder(ViewHodler holder, int position) {
            CycleCountAdd model = models.get(position);
            int hint = R.string.common_input;
            if (model.getType().equals("DBFIND") || model.getType().equals("C")) {
                holder.valuesEd.setFocusableInTouchMode(false);
                holder.valuesEd.setTag(R.id.tag_key, position);
                holder.valuesEd.setTag(model);
                holder.valuesEd.setOnClickListener(onClickListener);
                holder.valuesEd.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
                hint = model.isAllowblank() ? R.string.common_select_not_must : R.string.common_select;
            } else {
                holder.valuesEd.setFocusableInTouchMode(true);
                holder.valuesEd.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                holder.valuesEd.addTextChangedListener(new TextChangListener(holder, position));
                if (model.isAllowblank()) {
                    hint = R.string.common_input1;
                }
            }
            holder.valuesEd.setHint(hint);
            holder.captionTv.setText(model.getCaption());
            holder.valuesEd.setText(model.getValues());
        }

        @Override
        public int getItemCount() {
            return models == null ? 0 : models.size();
        }

        public void reset() {
            if (models != null) {
                for (CycleCountAdd e : models) {
                    e.setValues("");
                }
            }
            notifyDataSetChanged();
        }

        class ViewHodler extends RecyclerView.ViewHolder {
            TextView captionTv;
            EditText valuesEd;

            public ViewHodler(ViewGroup viewGroup) {
                this(LayoutInflater.from(ct).inflate(R.layout.item_pop_device_cyclecount, viewGroup, false));
            }

            public ViewHodler(View itemView) {
                super(itemView);
                captionTv = itemView.findViewById(R.id.captionTv);
                valuesEd = itemView.findViewById(R.id.valuesEd);
            }
        }

        private View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (view.getId() == R.id.valuesEd) {
                        CycleCountAdd model = (CycleCountAdd) view.getTag();
                        Intent intent = new Intent(ct, SelectActivity.class);
                        intent.putExtra("title", model.getCaption());
                        if (model.getType().equals("C")) {
                            selectField = model.getField();
                            LogUtil.i("selectField=" + selectField);
                            ArrayList<SelectBean> formBeaans = new ArrayList<>();
                            for (Approval.Data e : model.getDatas()) {
                                formBeaans.add(new SelectBean(e.display));
                            }
                            intent.putExtra("type", 2);
                            intent.putParcelableArrayListExtra("data", formBeaans);
                            startActivityForResult(intent, 0x21);
                        } else {
                            startActivityForResult(new Intent(ct, SelectNetAcitivty.class)
                                            .putExtra("fieldKey", model.getField())
                                            .putExtra("caller", "DeviceBatch!Stock")
                                            .putExtra("isForm", true)
                                            .putExtra("isDevice", true)
                                    , 90);
                        }

                    }
                } catch (Exception e) {

                }
            }
        };
    }

    private String selectField;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && popAdapter != null) {
            if (0x21 == requestCode && !StringUtil.isEmpty(selectField)) {
                SelectBean b = data.getParcelableExtra("data");
                LogUtil.d(JSON.toJSONString(b));
                if (b == null) return;
                Map<String, Object> map = new HashMap<>();
                LogUtil.i("selectField=" + selectField);
                map.put(selectField, b.getName());
                selectField = null;
                popAdapter.updateValues(JSONUtil.map2JSON(map));
            } else if (90 == requestCode) {
                String json = data.getStringExtra("data");
                LogUtil.i("json=" + json);
                popAdapter.updateValues(json);
            }
        }
    }
}

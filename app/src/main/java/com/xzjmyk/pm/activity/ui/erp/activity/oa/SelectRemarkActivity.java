package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.core.widget.listener.EditChangeListener;
import com.core.utils.ToastUtil;
import com.modular.apputils.utils.PopupWindowHelper;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.modular.apputils.widget.RecycleViewDivider;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SelectRemarkActivity extends BaseActivity implements OnHttpResultListener {

    private final int LOAD_NET = 0x11;
    private final int LOAD_COMTACT = 0x12;
    private RecyclerView recyclerview;
    private List<Bean> beanList;
    private RecycleAdapter adapter;
    private boolean isRemark = false;
    private String contact;
    private boolean isB2b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_remark);
        initView();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            showSaveCompany();
        }
        return super.onOptionsItemSelected(item);
    }


    private void showSaveCompany() {
        final PopupWindow window = new PopupWindow(ct);
        View view = LayoutInflater.from(ct).inflate(R.layout.item_select_remark_pop, null);
        window.setContentView(view);
        TextView title = (TextView) view.findViewById(R.id.title_tv);
        TextView company_tag = (TextView) view.findViewById(R.id.company_tag);
        title.setText(isRemark ? R.string.input_visit_remark : R.string.input_contact);
        company_tag.setText(isRemark ? R.string.visit_aim : R.string.common_Contact_person);
        final EditText company_et = (EditText) view.findViewById(R.id.company_et);
        view.findViewById(R.id.ok_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = company_et.getText().toString();
                if (StringUtil.isEmpty(message))
                    ToastUtil.showToast(ct, R.string.sure_input_valid);
                else if (message.length() > 20) {
                    ToastUtil.showToast(ct, R.string.too_long_edit_twenty);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("data", message);
                    setResult(0x20, intent);
                    window.dismiss();
                    finish();
                }
            }
        });
        view.findViewById(R.id.not_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                window.dismiss();
            }
        });
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(SelectRemarkActivity.this, 1f);
            }
        });
        window.setBackgroundDrawable(ct.getResources().getDrawable(R.drawable.pop_round_bg));
        window.setTouchable(true);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        PopupWindowHelper.setPopupWindowHW(this, window);
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }


    private void initView() {
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        RecycleViewDivider viewDivider = new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light));
        recyclerview.addItemDecoration(viewDivider);
        adapter = new RecycleAdapter(beanList);
        recyclerview.setAdapter(adapter);
        EditText search_edit = (EditText) findViewById(R.id.search_edit);
        search_edit.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (ListUtils.isEmpty(beanList)) return;
                List<Bean> beans = new ArrayList<>();
                for (Bean e : beanList) {
                    if (StringUtil.isInclude(e.name, input))
                        beans.add(e);
                }
                setBean2Adapter(beans);
            }
        });
    }

    private void initData() {
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        isRemark = getIntent() == null || !getIntent().getBooleanExtra("isContact", false);
        String title = getIntent() == null ? "" : getIntent().getStringExtra("title");
       setTitle(title);
        beanList = new ArrayList<>();
        if (isRemark) {
            loadLocalData();
            loadBusinessRemark();
        } else {
          setTitle(R.string.common_Contact_person);
            contact = getIntent().getStringExtra("contact");
            loadContact(getIntent().getStringExtra("cuname"));
        }
    }

    private void loadLocalData() {
        Bean bean = new Bean();
        bean.type = 1;
        bean.name = getResources().getString(R.string.maintain_customers);
        beanList.add(bean);
        bean = new Bean();
        bean.type = 1;
        bean.name = getResources().getString(R.string.business_process);
        beanList.add(bean);
        bean = new Bean();
        bean.type = 1;
        bean.name = getResources().getString(R.string.other_vivit);
        beanList.add(bean);
    }

    private void loadBusinessRemark() {
        if (isB2b) {
            handlerData(null);
            return;
        }
        progressDialog.show();
        Map<String, Object> param = new HashMap<>();
        param.put("condition", "1=1");
        Request request = new Request.Bulider()
                .setUrl("mobile/crm/getBusinessChanceStage.action")
                .setParam(param)
                .setWhat(LOAD_NET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    private void loadContact(String cuName) {
        if (isB2b) {
            handlerData(null);
            return;
        }
        progressDialog.show();
        String url = "mobile/crm/getContactPerson.action";
        Map<String, Object> param = new HashMap<>();
        param.put("page", 1);
        if (!StringUtil.isEmpty(cuName))
            param.put("condition", "ct_cuname='" + cuName + "'");
        param.put("size", 1000);
        Request request = new Request.Bulider()
                .setWhat(LOAD_COMTACT)
                .setUrl(url)
                .setParam(param)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }


    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        progressDialog.dismiss();
        if (!isJSON) return;
        JSONObject object = JSON.parseObject(message);
        switch (what) {
            case LOAD_NET:
            case LOAD_COMTACT:
                JSONArray array = JSONUtil.getJSONArray(object, "stages", "datalist");
                handlerData(array);
                break;
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        progressDialog.dismiss();
        handlerData(null);
        if (StringUtil.isEmpty(message)) return;
        ToastUtil.showToast(ct, message);
    }

    private void handlerData(JSONArray array) {
        Bean bean = null;
        if (!ListUtils.isEmpty(array)) {
            JSONObject object = null;
            for (int i = 0; i < array.size(); i++) {
                object = array.getJSONObject(i);
                bean = new Bean();
                bean.type = 1;
                bean.name = JSONUtil.getText(object, "BS_NAME", "ct_name");
                if (StringUtil.isEmpty(contact) || !contact.equals(bean.name))
                    beanList.add(beanList.size(), bean);
            }
        }
        if (!StringUtil.isEmpty(contact)) {
            bean = new Bean();
            bean.type = 1;
            bean.name = contact;
            beanList.add(beanList.size(), bean);
        }
        setBean2Adapter(beanList);
    }

    private void setBean2Adapter(List<Bean> beans) {
        if (adapter == null) {
            adapter = new RecycleAdapter(beans);
            recyclerview.setAdapter(adapter);
        } else {
            adapter.setBeanList(beans);
        }
    }

    class RecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<Bean> beanList;

        public RecycleAdapter(List<Bean> beanList) {
            this.beanList = beanList;
        }

        public List<Bean> getBeanList() {
            return beanList;
        }

        public void setBeanList(List<Bean> beanList) {
            if (ListUtils.isEmpty(beanList)) {
                this.beanList = new ArrayList<>();
                Bean bean = new Bean();
                bean.type = 3;
                bean.name = getString(R.string.not_message_and_add);
                this.beanList.add(bean);
            } else {
                this.beanList = beanList;
            }
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            return this.beanList.get(position).type;
        }


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 3) {
                View view = LayoutInflater.from(ct).inflate(R.layout.item_remark_empty, parent, false);
                return new EmptyHolder(view);
            } else {
                View view = LayoutInflater.from(ct).inflate(R.layout.item_remark, parent, false);
                return new ViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof ViewHolder) {
                ViewHolder h = (ViewHolder) holder;
                if (beanList.get(position).type == 2) {//输入
                    h.et.setVisibility(View.VISIBLE);
                    h.tv.setVisibility(View.GONE);
                } else if (beanList.get(position).type == 1) {//显示
                    h.tv.setVisibility(View.VISIBLE);
                    h.et.setVisibility(View.GONE);
                    h.tv.setText(StringUtil.isEmpty(beanList.get(position).name) ? "" : beanList.get(position).name);
                }
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String message = beanList.get(position).name;
                        Intent intent = new Intent();
                        intent.putExtra("data", message);
                        setResult(0x20, intent);
                        finish();
                    }
                });
            } else if (holder instanceof EmptyHolder) {

            }
        }


        @Override
        public int getItemCount() {
            return ListUtils.isEmpty(beanList) ? 0 : beanList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv;
            EditText et;

            public ViewHolder(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.tv);
                et = (EditText) itemView.findViewById(R.id.et);
            }
        }

        class EmptyHolder extends RecyclerView.ViewHolder {
            TextView empty_tv;

            public EmptyHolder(View itemView) {
                super(itemView);
                empty_tv = (TextView) itemView.findViewById(R.id.empty_tv);
            }
        }
    }

    class Bean {
        String name;
        int type;//1.显示  2.输入  3.空数据
    }
}

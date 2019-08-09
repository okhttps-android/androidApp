package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.dao.DBManager;
import com.core.model.HrorgsEntity;
import com.core.model.OAConfig;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CompanyHandlerInfoUtil;
import com.core.widget.ClearEditText;
import com.core.widget.listener.EditChangeListener;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.OA.erp.model.HrorgsModel;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.view.tree.MyTreeListViewAdapter;
import com.xzjmyk.pm.activity.ui.erp.view.tree.Node;
import com.xzjmyk.pm.activity.ui.erp.view.tree.TreeListViewAdapter;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectDepartmentActivity extends OABaseActivity {
    @ViewInject(R.id.context_lv)
    private ListView context_lv;
    @ViewInject(R.id.mumber_tv)
    private TextView mumber_tv;
    @ViewInject(R.id.all_sure_cb)
    private CheckBox all_sure_cb;

    @ViewInject(R.id.rl_empty)
    private RelativeLayout rl_empty;
    @ViewInject(R.id.select_rl)
    private RelativeLayout select_rl;
    @ViewInject(R.id.textViewMessage)
    private TextView textViewMessage;
    @ViewInject(R.id.search_lv)
    private ListView search_lv;
    @ViewInject(R.id.search_edit)
    private ClearEditText search_edit;

    private MyTreeListViewAdapter<HrorgsModel> adapter;
    private List<HrorgsModel> mDatas = new ArrayList<>();
    private String selectName = "";
    private SeachAdapter seachAdapter = null;

    boolean isClick = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_department);
        ViewUtils.inject(this);
        rl_empty.setVisibility(View.GONE);
        mumber_tv.setText(getString(R.string.selected)+" 0 "+getString(R.string.a_department));
        initData();
    }


    private void initData() {
        DBManager manager = new DBManager();
        String master = CommonUtil.getSharedPreferences(ct, "erp_master");
        List<HrorgsEntity> hrorgsEntities = manager.queryHrorgList(new String[]{master}, "whichsys=?");
        manager.closeDB();
        if (ListUtils.isEmpty(hrorgsEntities)) {
            loadHrorgsEntitiesByNet();
        } else {
            initView(hrorgsEntities);
        }
    }

    private void loadHrorgsEntitiesByNet() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            showToast(R.string.networks_out, R.color.load_error);
            return;
        }
        progressDialog.show();
        String master = CommonUtil.getSharedPreferences(ct, "erp_master");
        Map<String, Object> param = new HashMap<>();
        param.put("master", master);
        param.put("lastdate", "");
        Bundle bundle = new Bundle();
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        bundle.putBoolean("isB2b", isB2b);
        String url = isB2b ? ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getUsersInfo : "mobile/getAllHrorgEmps.action";
        Request request = new Request.Bulider()
                .setMode(Request.Mode.GET)
                .setParam(param)
                .setBundle(bundle)
                .setUrl(url).bulid();
        OAHttpHelper.getInstance().requestHttp(request, new OnHttpResultListener() {
            @Override
            public void result(int what, boolean isJSON, String message, Bundle bundle) {
                try {
                    progressDialog.dismiss();
                    JSONObject object = JSON.parseObject(message);
                    if (!isJSON) {
                        rl_empty.setVisibility(View.VISIBLE);
                        return;
                    }
                    boolean isB2b = bundle.getBoolean("isB2b");
                    if (isB2b) {

                    } else {
                        List<HrorgsEntity> hrorgsEntities = CompanyHandlerInfoUtil.getHrorgsByNet(object);
                        if (ListUtils.isEmpty(hrorgsEntities))
                            rl_empty.setVisibility(View.VISIBLE);
                        else
                            initView(hrorgsEntities);
                    }
                }catch (Exception e){

                }
            }

            @Override
            public void error(int what, String message, Bundle bundle) {
                progressDialog.dismiss();
            }
        });


    }

    private void initEvent() {
        all_sure_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (StringUtil.isEmpty(search_edit.getText().toString())) {
                    if (b) {
                        if (isClick) {
                            adapter.selectAll();
                        }
                        all_sure_cb.setText(R.string.cancel_select_all);
                    } else {
                        if (isClick) {
                            adapter.deleteAll();
                        }
                        all_sure_cb.setText(R.string.select_all);
                    }
                    isClick = true;
                } else {
                    if (!ListUtils.isEmpty(seachAdapter.getSearch())) {
                        seachAdapter.setClicked(b);
                        setMumberSelect(adapter.getClick().size());
                    }
                }
            }
        });
        findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endActivity();
            }
        });
        search_edit.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable s) {
                List<Node> datas = adapter.getAllNodes();
                if (ListUtils.isEmpty(datas)) return;
                String str = s.toString();
                if (StringUtil.isEmpty(str)) {
                    search_lv.setVisibility(View.GONE);
                    rl_empty.setVisibility(View.GONE);
                    select_rl.setVisibility(View.VISIBLE);
                    context_lv.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    return;
                }
                List<Node> search = new ArrayList<>();
                for (Node m : datas) {

                    if (StringUtil.isInclude(m.getName(), str))
                        search.add(m);
                }
                showSearch(search);
            }
        });

        search_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (seachAdapter == null || seachAdapter.getSearch().size() <= position) return;
                seachAdapter.getSearch().get(position).setChecked(!seachAdapter.getSearch().get(position).isChecked());
                seachAdapter.notifyDataSetChanged();
                int clickSize = adapter.getClick().size();
                setMumberSelect(clickSize);

            }
        });
    }

    private void showSearch(List<Node> search) {
        if (ListUtils.isEmpty(search)) {
            search_lv.setVisibility(View.GONE);
            context_lv.setVisibility(View.GONE);
            rl_empty.setVisibility(View.VISIBLE);
            select_rl.setVisibility(View.GONE);
        } else {
            search_lv.setVisibility(View.VISIBLE);
            context_lv.setVisibility(View.GONE);
            rl_empty.setVisibility(View.GONE);
            select_rl.setVisibility(View.VISIBLE);
            if (seachAdapter == null) {
                seachAdapter = new SeachAdapter(search);
                search_lv.setAdapter(seachAdapter);
            } else {
                seachAdapter.setSearch(search);
                seachAdapter.notifyDataSetChanged();
            }
        }
    }


    private void initView(List<HrorgsEntity> hrorgsEntities) {
        selectName = getIntent().getStringExtra(OAConfig.STRING_DATA);
        for (HrorgsEntity e : hrorgsEntities) {
            mDatas.add(new HrorgsModel(e.getOr_id(), e.getOr_subof(), e.getOr_name(), e.getOr_code()));
        }
        rl_empty.setVisibility(View.GONE);
        select_rl.setVisibility(View.VISIBLE);
        try {
            adapter = new MyTreeListViewAdapter<>(context_lv, this, mDatas, 0, false);
            adapter.setOnTreeNodeClickListener(new TreeListViewAdapter.OnTreeNodeClickListener() {
                @Override
                public void onClick(Node node, int position) {
                }

                @Override
                public void onCheckChange(Node node, int position, boolean isSelectAll,
                                          List<Node> checkedNodes) {
                    mumber_tv.setText(getString(R.string.selected)+" " + checkedNodes.size() + " "+getString(R.string.a_department));
                    if (isSelectAll && !all_sure_cb.isChecked()) {//如果已经全选并且当前的cb状态为
                        isClick = false;
                        all_sure_cb.setChecked(true);
                    } else if (!isSelectAll && all_sure_cb.isChecked()) {
                        isClick = false;
                        all_sure_cb.setChecked(false);
                    }
                }
            });
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        setMumberSelect(0);
        adapter.setClick(selectName);
        int clickSize = adapter.getClick().size();
        mumber_tv.setText((getString(R.string.selected)+" " + clickSize  + " "+getString(R.string.a_department)));
        if (clickSize == mDatas.size()) {
            all_sure_cb.setChecked(true);
            all_sure_cb.setText(R.string.cancel_select_all);
        }
        context_lv.setAdapter(adapter);
        if (ListUtils.isEmpty(mDatas)) {
            textViewMessage.setText(R.string.not_load_ok_fefresh);
            rl_empty.setVisibility(View.VISIBLE);
            select_rl.setVisibility(View.GONE);
        }
        initEvent();

    }

    private void setMumberSelect(int number) {
        mumber_tv.setText((getString(R.string.selected)+" " + number  + " "+getString(R.string.a_department)));
        if (number != adapter.getAllNodes().size()) {
            if (all_sure_cb.isChecked()) {
                isClick = false;
                all_sure_cb.setText(R.string.select_all);
            }
        } else {
            if (!all_sure_cb.isChecked()) {
                isClick = false;
                all_sure_cb.setText(R.string.cancel_select_all);
            }
        }
    }

    private void endActivity() {
        ArrayList<HrorgsModel> baseNodeBeans = adapter.getClick();
        StringBuilder ids = new StringBuilder();
        if (!ListUtils.isEmpty(baseNodeBeans)) {
            for (HrorgsModel b : baseNodeBeans) {
                ids.append(b.getId() + ",");
            }
        }
        if (ids.length() > 0) ids.deleteCharAt(ids.length() - 1);
        int numbers = getFriendsByErpDB(ids.toString());
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("data", baseNodeBeans);
        intent.putExtra("number", numbers);
        setResult(0x20, intent);
        finish();
    }


    /**
     * 通过数据库获取数据显示
     *
     * @return 查询到数据列表
     */
    private int getFriendsByErpDB(String ids) {
        DBManager manager = new DBManager();
        int number = manager.getEmployeeNumber(ids);
        manager.closeDB();
        return number;
    }


    private class SeachAdapter extends BaseAdapter {
        private List<Node> search = new ArrayList<>();

        public SeachAdapter(List<Node> search) {
            this.search = search;
        }

        public List<Node> getSearch() {
            return search;
        }

        public void setSearch(List<Node> search) {
            this.search = search;
        }

        @Override
        public int getCount() {
            return ListUtils.isEmpty(search) ? 0 : search.size();
        }

        @Override
        public Object getItem(int position) {
            return search.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Node n = search.get(position);
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                view = LayoutInflater.from(ct).inflate(R.layout.item_select_depat, null);
                holder.node_cb = (CheckBox) view.findViewById(R.id.node_cb);
                holder.node_value = (TextView) view.findViewById(R.id.node_value);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.node_cb.setChecked(n.isChecked());
            holder.node_cb.setFocusable(false);
            holder.node_cb.setClickable(false);
            holder.node_value.setText(n.getName());
            return view;
        }

        public void setClicked(boolean isClicked) {
            for (int i = 0; i < search.size(); i++)
                search.get(i).setChecked(isClicked);
            notifyDataSetChanged();
        }

        class ViewHolder {
            CheckBox node_cb;
            TextView node_value;
        }
    }

}

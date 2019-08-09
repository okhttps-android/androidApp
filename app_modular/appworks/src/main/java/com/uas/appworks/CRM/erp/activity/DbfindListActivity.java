package com.uas.appworks.CRM.erp.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.model.Employees;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.FlexJsonUtil;
import com.core.widget.ClearEditText;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :LiuJie 2015年7月28日 上午9:37:24
 * @注释:查找
 */
public class DbfindListActivity extends BaseActivity {
    private EmptyLayout mEmptyLayout;

    private PullToRefreshListView lv_employee;
    private EmployeeAdatper adapter;
    private List<Employees> lists;
    private ClearEditText et_Search;
    private  ModeCallback mCallback;//多选模式
    private Context ct;
    private ImageView iv_DeleteText;
    private List<Employees> selectedItems = new ArrayList<>();
    private final static int SUCCESS_LOADDATA = 1;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SUCCESS_LOADDATA:
                    progressDialog.dismiss();
                    if (StringUtil.isEmpty(msg.getData().getString("result"))){
                        ToastMessage(getResources().getString(R.string.networks_out));
                        return;
                    }
                    Map<Object, Object> kMap = FlexJsonUtil.fromJson(msg.getData().getString("result"));
                    lists = FlexJsonUtil.fromJsonArray(kMap.get("data").toString(), Employees.class);
                    if (lists == null) {
                        mEmptyLayout.showError();
                    } else if (lists.size() == 0) {
                        mEmptyLayout.showEmpty();
                    } else {
                        System.out.println("data:" + kMap.get("data").toString());
                        adapter = new EmployeeAdatper(ct, lists);
                        lv_employee.setAdapter(adapter);

                    }
                    break;
                case LOAD_EMPLOYEES_SUCCESS:
                    progressDialog.dismiss();
                    String result = msg.getData().getString("result");
                    Log.i(TAG, result);
                    JSONObject object = JSON.parseObject(result);
                    JSONArray aJsonArray = object.getJSONArray("employees");
                    List<Employees> employees_query = new ArrayList<>();
                    for (int i = 0; i < aJsonArray.size(); i++) {
                        Employees em = new Employees();
                        em.setEm_id(aJsonArray.getJSONObject(i).getIntValue("em_id"));
                        em.setEm_code(aJsonArray.getJSONObject(i).getString("em_code"));
                        em.setEm_name(aJsonArray.getJSONObject(i).getString("em_name"));
                        employees_query.add(em);
                    }
                    adapter = new EmployeeAdatper(ct, employees_query);
                    lv_employee.setAdapter(adapter);
                    break;
                default:
                    break;
            }
        }

        ;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    public void initView() {
        setContentView(R.layout.act_list_dbfind_employee);
        iv_DeleteText = (ImageView) findViewById(R.id.iv_DeleteText);
        et_Search = (ClearEditText) findViewById(R.id.et_Search);
        lv_employee = (PullToRefreshListView) findViewById(R.id.lv_employee);
        ct = this;
     setTitle("选择同事");
        
        Intent intent=getIntent();
        int listViewMode=ListView.CHOICE_MODE_SINGLE;
        if (intent!=null){
            listViewMode= intent.getIntExtra("listViewMode",ListView.CHOICE_MODE_SINGLE);
        }
        mCallback = new ModeCallback();
        lv_employee.setMode(PullToRefreshBase.Mode.DISABLED);
        lv_employee.getRefreshableView().setChoiceMode(listViewMode);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            lv_employee.getRefreshableView().setMultiChoiceModeListener(mCallback);
        }
        lv_employee.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EmployeeAdatper.Store store = (EmployeeAdatper.Store) view.getTag();
                Intent intent = new Intent();
                intent.putExtra("en_name", store.en_name.getText().toString());
                intent.putExtra("en_code", store.en_no.getText().toString());
//				intent.putExtra("en_depart", store.en_depart.getText().toString());
                DbfindListActivity.this.setResult(2, intent);
                DbfindListActivity.this.finish();
            }
        });

        et_Search.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 && !StringUtil.isEmpty(s.toString())) {
                    loadEmployees(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEmptyLayout = new EmptyLayout(this, lv_employee.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);

    }

    public void initData() {
        progressDialog.show();
        if (CommonUtil.isNetWorkConnected(this)) {
            String url = CommonUtil.getAppBaseUrl(ct) + "common/dbfind.action";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("caller", "Ask4leave");
            params.put("which", "form");
            params.put("field", "va_emcode");
            params.put("condition", "1=1");
            params.put("page", "1");
            params.put("pageSize", "100");
            params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(
                    ct, url,
                    params,
                    handler, headers, SUCCESS_LOADDATA, null, null, "get");
        } else {
            progressDialog.dismiss();
            mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
            mEmptyLayout.showError();
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class ModeCallback implements ListView.MultiChoiceModeListener {
        private View mMultiSelectActionBarView;
        private TextView mSelectedCount;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_action_bar, menu);
            if (mMultiSelectActionBarView == null) {
                mMultiSelectActionBarView = LayoutInflater.from(ct)
                        .inflate(R.layout.list_multi_select_actionbar, null);
                mSelectedCount =
                        (TextView) mMultiSelectActionBarView.findViewById(R.id.selected_conv_count);
            }
            mode.setCustomView(mMultiSelectActionBarView);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            String values = getValuesSelect();
            selectedItems.clear();
            if (values.length() > 1) {
                Log.i(TAG, values.substring(0, values.length() - 1));
                Intent intent = new Intent();
                intent.putExtra("employees", values.substring(0, values.length() - 1));
                DbfindListActivity.this.setResult(1, intent);
                DbfindListActivity.this.finish();

            }

        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            switch (lv_employee.getRefreshableView().getCheckedItemCount()) {
                default:
                    manageClickedVIew(position - 1, checked);
                    break;
            }
            updateSeletedCount();
            mode.invalidate();
            adapter.notifyDataSetChanged();
        }

        private void manageClickedVIew(int i, boolean checked) {
            if (checked) {
                selectedItems.add((Employees) adapter.getItem(i));
            } else {
                selectedItems.remove((Employees) adapter.getItem(i));
            }
        }

        public void updateSeletedCount() {
            mSelectedCount.setText(Integer.toString(lv_employee.getRefreshableView().getCheckedItemCount()));
        }


        public String getValuesSelect() {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < selectedItems.size(); i++) {
                String name = selectedItems.get(i).getEm_name();
                sb.append(name + ",");
            }
            return sb.toString();
        }

    }


    private final static int LOAD_EMPLOYEES_SUCCESS = 2;

    public void loadEmployees(String em_name) {
        progressDialog.show();
        if (CommonUtil.isNetWorkConnected(this)) {
            String url = CommonUtil.getAppBaseUrl(ct) + "mobile/QueryEmployee.action";
            Map<String, Object> param = new HashMap<String, Object>();
            param.put("em_name", em_name);
            String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
            Log.i("sessionId", "sessionId:" + sessionId);
            param.put("sessionId", sessionId);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            ViewUtil.httpSendRequest(
                    ct, url,
                    param,
                    handler, headers, LOAD_EMPLOYEES_SUCCESS, null, null, "get");
        } else {
            progressDialog.dismiss();
            mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
            mEmptyLayout.showError();
        }

    }

    public class EmployeeAdatper extends BaseAdapter {
        public List<Employees> lists;
        private Context context;

        public EmployeeAdatper(Context ct, List<Employees> lists) {
            this.lists = lists;
            this.context = ct;
        }

        @Override
        public int getCount() {
            return lists != null ? lists.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Store store = null;

            if (view == null) {
                store = new Store();
                view = LayoutInflater.from(context).inflate(R.layout.item_dbfind_employee, null);
                store.en_no = (TextView) view.findViewById(R.id.tv_em_code_value);
                store.en_name = (TextView) view.findViewById(R.id.tv_employee_name_value);
//				store.en_defaultorname = (TextView) view.findViewById(R.id.tv_em_defaultorname_value);
//				store.en_depart = (TextView) view.findViewById(R.id.tv_em_depart_value);
//				store.en_defaultorid = (TextView) view.findViewById(R.id.tv_em_defaultorid_value);
                view.setTag(store);
            } else {
                store = (Store) view.getTag();
            }
            store.en_no.setText(lists.get(position).getEm_code());
            store.en_name.setText(lists.get(position).getEm_name());
//			store.en_depart.setText(lists.get(position).getEm_depart());
//			store.en_defaultorname.setText(lists.get(position).getEm_defaultorname());
            // store.en_defaultorid.setText(lists.get(position).getEm_defaultorid()+"");


            updateBackground(position + 1, view);
            return view;
        }


        public void updateBackground(int position, View view) {
            int backgroundId;
            if (lv_employee.getRefreshableView().isItemChecked(position)) {
//				backgroundId = R.drawable.list_selected_holo_light;
//				Drawable background = ct.getResources().getDrawable(backgroundId);
                view.setBackgroundResource(R.drawable.list_selected_holo_light);
            } else {
//				backgroundId = R.drawable.conversation_item_background_read;
//				Drawable background = ct.getResources().getDrawable(backgroundId);
                view.setBackgroundColor(ct.getResources().getColor(R.color.Transpant));
            }

        }

        public class Store {
            TextView en_no;// 职位
            TextView en_name;// 人员名称
            TextView en_depart;// 部门
            TextView en_defaultorid;// id 组织
            TextView en_defaultorname;// 组织名称
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        putDownInput();
        return super.onOptionsItemSelected(item);
    }

    private void putDownInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_Search.getWindowToken(), 0);
    }

}

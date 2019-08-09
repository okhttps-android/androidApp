package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.ListUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//参会人员添加界面
public class UserSelectActivity extends BaseActivity {
    private static final int WHAT_LOAD = 0x11;
    private PullToRefreshListView listview;
    private ClearEditText search_edit;
    private boolean isSingle = false;
    private boolean isNet = false;
    private MeetSelectAdapter adapter;
    private List<Employees> list = new ArrayList<>();
    private List<String> selectedList = new ArrayList<>();  //存储已选择参会人员集合数据
    private String selectedStrs;  //已选择参会人员转换为数组存储
    private JSONArray array;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("result");
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            if (StringUtil.isEmpty(message)) return;
            switch (msg.what) {
                case WHAT_LOAD:
                    array = JSON.parseObject(message).getJSONArray("datas");//获取到数据
                    Log.d("handleMessage: ", array.toString());
                    if (array == null || array.size() <= 0) return;
                    Employees entity = null;
                    String code = null;//em_code
                    String name = null;//em_name
                    String company = null;//em_name
                    String position = null;//em_name
                    int imId;//em_department->em_post
                    JSONObject object = null;
                    for (int i = 0; i < array.size(); i++) {
                        object = array.getJSONObject(i);
                        entity = new Employees();
                        entity.setClick(false);
                        code = object.containsKey("em_code") ? object.getString("em_code") : "";
                        name = object.containsKey("em_name") ? object.getString("em_name") : "";
                        imId = object.containsKey("em_imid") ? object.getIntValue("em_imid") : 0;
                        company = array.getJSONObject(i).getString("em_department") == null ? "" : array.getJSONObject(i).getString("em_department");
                        position = (array.getJSONObject(i).getString("em_post") == null ? "" : array.getJSONObject(i).getString("em_post"));
                        entity.setEm_code(code);
                        entity.setEm_name(name);
                        entity.setEm_id(imId);
                        entity.setEm_defaultorname(company);
                        entity.setEm_position(position);
                        list.add(entity);
                    }
                    if (list.size() > 0) {
                        if (adapter == null) {
                            adapter = new MeetSelectAdapter(list);
                            listview.setAdapter(adapter);
                        } else
                            adapter.setUsers(list);
                    }else{
                        mEmptyLayout.showEmpty();
                    }

                    //判断selectList不为空
                    if (selectedList != null && selectedList.size() != 0) {
                        //遍历所有人员将其姓名与前一次所选人员（默认存入的为姓名）对比，如果重复则将该参会人员设置为已勾选
                        for (int i = 0; i < list.size(); i++) {
                            for (int j = 0; j < selectedList.size(); j++) {
                                if (list.get(i).getEm_name().equals(selectedList.get(j))) {
                                    list.get(i).setClick(true);
                                    break;
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case 0x12:
                    if (StringUtil.isEmpty(message)) return;
                    Map<Object, Object> kMap = FlexJsonUtil.fromJson(message);
                    list = FlexJsonUtil.fromJsonArray(kMap.get("data").toString(), Employees.class);
                    if (ListUtils.isEmpty(list)) return;
                    if (list.size() > 0) {
                        if (adapter == null) {
                            adapter = new MeetSelectAdapter(list);
                            listview.setAdapter(adapter);
                        } else
                            adapter.setUsers(list);
                    }else{
                        mEmptyLayout.showEmpty();
                    }
                    if (selectedList != null && selectedList.size() != 0) {
                        for (int i = 0; i < list.size(); i++) {
                            for (int j = 0; j < selectedList.size(); j++) {
                                if (list.get(i).getEm_name().equals(selectedList.get(j))) {
                                    list.get(i).setClick(true);
                                    break;
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (getIntent() != null && getIntent().getIntExtra("reid", -1) != -1) {
//            setTheme(getSharedPreferences("cons", MODE_PRIVATE).getInt("theme",
//                    getIntent().getIntExtra("reid", -1)));
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet_select);
        listview = (PullToRefreshListView) findViewById(R.id.listview);
        search_edit = (ClearEditText) findViewById(R.id.search_edit);
        isSingle = getIntent().getBooleanExtra("single", false);
        //是否获取网络数据
        isNet = getIntent().getBooleanExtra("net", false);

        selectedStrs = getIntent().getStringExtra("users"); //
        if (selectedStrs != null) {
            String[] split = selectedStrs.split(",");
            for (int i = 0; i < split.length; i++) {
                selectedList.add(split[i]);
            }
            Log.d("selected", selectedList.toString());
        }
        initView();
        init();
    }
private EmptyLayout mEmptyLayout;
    private void initView() {
       setTitle(R.string.select_user);
//        View view = LayoutInflater.from(this).inflate(R.layout.view_empty, null);
//        listview.setEmptyView(view);
        adapter = new MeetSelectAdapter();
        listview.setAdapter(adapter);
        listview.setMode(PullToRefreshBase.Mode.DISABLED);

        mEmptyLayout = new EmptyLayout(this, listview.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("暂无数据");

        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String strChche = editable.toString();
                if (list == null || list.size() <= 0) return;
                ArrayList<Employees> chche = new ArrayList<>();
                for (Employees e : list) {
                    String text=e.getEm_name() + ","+e.getEm_defaultorname()+ ","+ e.getEm_position();
                    if (!StringUtil.isEmpty(text)&&text.contains(strChche)) {
                        chche.add(e);
                    }
                }
                adapter.setUsers(chche);
            }
        });
        listview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                init();
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Employees select = adapter.getUsers().get(i - 1 < 0 ? 0 : (i - 1));
                if (isSingle) {//单选
                    if (adapter.getUsers() == null) return;
                    Employees entity = select;
                    Intent intent = new Intent();
                    intent.putExtra("data", entity);
                    setResult(0x11, intent);
                    finish();
                } else {//默认多选
                    if (adapter.getUsers() == null) return;
                    select.setClick(!select.isClick());
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    int pot = -1;

    private void setClickSingle(int item) {
        int i = item - 1 < 0 ? 0 : (item - 1);
        if (adapter == null || adapter.getUsers() == null) return;
        List<Employees> lists = adapter.getUsers();
        if (pot != -1 && lists.size() > pot)//被选择过
            lists.get(pot).setClick(false);
        lists.get(i).setClick(true);
        adapter.setUsers(lists);
        pot = i;
    }

    //正则
    private static boolean getResult(String text, String str) {
        Pattern p = Pattern.compile(str);
        Matcher m = p.matcher(text);
        return m.find();
    }

    private void selectOK() {
        boolean isme = getIntent().getBooleanExtra("isme", false);
        //选择人员成功操作
        ArrayList<Employees> list = new ArrayList<>();

        if (adapter == null || adapter.getUsers() == null || adapter.getUsers().size() <= 0) {
            finish();
            return;

        }
        String name = CommonUtil.getSharedPreferences(ct, "erp_username");
        for (int i = 0; i < adapter.getUsers().size(); i++) {
            if (!isme && !StringUtil.isEmpty(name) && name.equals(adapter.getUsers().get(i).getEm_code())) {
                list.add(adapter.getUsers().get(i));
                continue;
            }
            if (adapter.getUsers().get(i).isClick())
                list.add(adapter.getUsers().get(i));
        }
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.putExtra("data", list);
            setResult(0x11, intent);
            finish();
        } else {
            new AlertDialog
                    .Builder(mContext)
                    .setTitle("温馨提示")
                    .setMessage("\t请选择参会人员")
                    .setPositiveButton("确定", null)
                    .show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            search_edit.setText("");
            putDownInput();
            selectOK();
        } else {
            if (item.getItemId() == android.R.id.home) {
                putDownInput();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isSingle)
            getMenuInflater().inflate(R.menu.menu_visit_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void init() {
        if (isNet) {
            getDataByNet();
        } else {
            getEmployeeByNet();
        }
    }

    private void getEmployeeByNet() {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "common/dbfind.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", "Ask4leave");
        params.put("which", "form");
        params.put("field", "va_emcode");
        params.put("condition", "1=1");
        params.put("page", "1");
        params.put("pageSize", "1000");
        params.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, 0x12, null, null, "get");
    }

    private void getDataByNet() {
        progressDialog.show();
        //获取网络数据
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "mobile/crm/getstaffmsg.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, WHAT_LOAD, null, null, "get");
    }


    class MeetSelectAdapter extends BaseAdapter {
        private List<Employees> users;

        public List<Employees> getUsers() {
            return users;
        }

        public void setUsers(List<Employees> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        public MeetSelectAdapter(List<Employees> users) {
            this.users = users;
        }

        public MeetSelectAdapter() {
        }

        @Override
        public int getCount() {
            return users == null ? 0 : users.size();
        }

        @Override
        public Object getItem(int i) {
            return users == null ? 0 : users.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                view = LayoutInflater.from(ct).inflate(R.layout.meet_select_item, null);
                holder.select_scb = (CheckBox) view.findViewById(R.id.select_scb);
                holder.id_tv = (TextView) view.findViewById(R.id.id_tv);
                holder.name_tv = (TextView) view.findViewById(R.id.name_tv);
                holder.head_img = (ImageView) view.findViewById(R.id.head_img);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.select_scb.setChecked(users.get(i).isClick());
            holder.select_scb.setFocusable(false);
            holder.select_scb.setClickable(false);
            String str = (StringUtil.isEmpty(users.get(i).getEm_position()) ? "" : (" | " + users.get(i).getEm_position()));
            holder.id_tv.setText(users.get(i).getEm_defaultorname() + str);
            holder.name_tv.setText("姓名:" + users.get(i).getEm_name());
            return view;
        }

        class ViewHolder {
            CheckBox select_scb;
            TextView name_tv, id_tv;
            ImageView head_img;
        }
    }

    private void putDownInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search_edit.getWindowToken(), 0);
    }
}

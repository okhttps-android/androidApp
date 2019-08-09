package com.uas.appworks.CRM.erp.activity;

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
import com.core.utils.helper.AvatarHelper;
import com.core.widget.ClearEditText;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by FANGlh on 2016/10/24.
 * @注释:商机分配同事选择
 */

public class DbfindList2Activity extends BaseActivity{
    private static final int WHAT_LOAD = 0x11;
    private PullToRefreshListView listview;
    private ClearEditText search_edit;
    private boolean isSingle = true;
    private boolean isNet = false;
    private DistributionSelectAdapter adapter;
    private List<Employees> list = new ArrayList<>();
    private JSONArray array;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("result");
            if (progressDialog.isShowing())
                progressDialog.dismiss();
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
                            adapter = new DistributionSelectAdapter(list);
                            listview.setAdapter(adapter);
                        } else
                            adapter.setUsers(list);
                    }

                    break;
                case 1:
                    Map<Object, Object> kMap = FlexJsonUtil.fromJson(msg.getData().getString("result"));
                    list = FlexJsonUtil.fromJsonArray(kMap.get("data").toString(), Employees.class);
                    if (ListUtils.isEmpty(list)) return;
                    if (list.size() > 0) {
                        if (adapter == null) {
                            adapter = new DistributionSelectAdapter(list);
                            listview.setAdapter(adapter);
                        } else
                            adapter.setUsers(list);
                    }
                    break;
            }
        }
    };
    private String selectName;
    private String selectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent() != null && getIntent().getIntExtra("reid", -1) != -1) {
            setTheme(getSharedPreferences("cons", MODE_PRIVATE).getInt("theme",
                    getIntent().getIntExtra("reid", -1)));
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet_select);

        ViewUtils.inject(this);
        //是否获取网络数据
        isNet = getIntent().getBooleanExtra("net", false);
        initView();
        init();
    }
    public void initView(){
        View view = LayoutInflater.from(this).inflate(R.layout.view_empty, null);

        listview = (PullToRefreshListView) findViewById(R.id.listview);
        search_edit = (ClearEditText) findViewById(R.id.search_edit);
        listview.setEmptyView(view);
      setTitle("选择同事");
        adapter = new DistributionSelectAdapter();
        listview.setAdapter(adapter);
        listview.setMode(PullToRefreshBase.Mode.DISABLED);
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
                    try{
                        if (getResult(e.getEm_name() + e.getEm_defaultorname() + e.getEm_position(), strChche)) {
                            chche.add(e);
                        }
                    }catch (PatternSyntaxException r){
                        r.printStackTrace();
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
                  if (isSingle) {  // 已默认单选
                      setClickSingle(i);
                      DistributionSelectAdapter.ViewHolder viewHolder = (DistributionSelectAdapter.ViewHolder) view.getTag();
                      selectName = viewHolder.name_tv.getText().toString();
                      selectId = viewHolder.encode;

                  } else {
                      if (adapter.getUsers() == null) return;
                      select.setClick(!select.isClick());
                      adapter.notifyDataSetChanged();
                  }
              }
          });
    }
    int pot=-1;

    /**
     * 单选方法封装
     * @param item
     */
    private void setClickSingle(int item){
        int i =item -1<0 ? 0 :(item-1);
        if(adapter == null || adapter.getUsers() == null) return;

        List<Employees> lists = adapter.getUsers();
        if(pot !=-1&&lists.size()>pot) //被选择过
        {
            lists.get(pot).setClick(false);
        }
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
    private void init() {
        if (isNet) {
            getDataByNet();
        } else {
            getEmployeeByNet();
        }
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
        ViewUtil.httpSendRequest(ct, url, params, handler, headers, 1, null, null, "get");
    }

    class DistributionSelectAdapter extends  BaseAdapter{

        private List<Employees> users;

        public List<Employees> getUsers() {
            return users;
        }

        public void setUsers(List<Employees> users) {
            this.users = users;
            notifyDataSetChanged();
        }

        public DistributionSelectAdapter(List<Employees> users) {
            this.users = users;
        }

        public DistributionSelectAdapter() {
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
                view = LayoutInflater.from(ct).inflate(R.layout.business_distribution_item, null);
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
            holder.name_tv.setText(users.get(i).getEm_name());
            holder.encode=users.get(i).getEm_code();
            if (users.get(i).getEm_id() == 0){
                holder.head_img.setImageResource(R.drawable.avatar_normal);
            }else {
                AvatarHelper.getInstance().display(users.get(i).getEm_id() + "", holder.head_img, true, true);
            }
            return view;
        }

        class ViewHolder {
            CheckBox select_scb;
            TextView name_tv, id_tv;
            ImageView head_img;
            String encode;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            selectOK();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_emp_select, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void selectOK() {

        //选择人员成功操作
        ArrayList<Employees> list = new ArrayList<>();

        if (adapter == null || adapter.getUsers() == null || adapter.getUsers().size() <= 0) {
            finish();
            return;
        }
        if(selectName!=null&&selectId!=null){
            Intent intent = new Intent();
            intent.putExtra("en_name", selectName);
            intent.putExtra("en_code", selectId);
            DbfindList2Activity.this.setResult(2, intent);
            DbfindList2Activity.this.finish();
            finish();
        }else {
            ToastMessage("请选择分配人员");
        }

    }
}

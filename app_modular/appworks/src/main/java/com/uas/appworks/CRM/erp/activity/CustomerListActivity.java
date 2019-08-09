package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.Employees;
import com.core.model.EmployeesEntity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.CRM.erp.model.Business;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @功能:客户列表
 * @author:Arisono
 * @param:
 * @return:
 */
public class CustomerListActivity extends BaseActivity implements View.OnClickListener {
    private BussinessDetailAdapter mAdapter;

    private ImageView iv_head;
    private TextView tv_change;
    private TextView tv_count_unend;
    private TextView tv_count_end;
    private TextView tv_count_total;
    private TextView tv_name;
    private TextView tv_department;
    private TextView tv_position;
    private PullToRefreshListView mlist;
    private ArrayList<Business> mData = new ArrayList<Business>();
    private Context ct;
    private int page = 1;
    private int type = 0;
    private int falg = 0;//是否启用预录入客户

    private EmptyLayout mEmptyLayout;
    private DBManager dbManager;

    private final static int FLAG_ADD_CUSTOMER = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);
        initIds();
        initView();
        initData();
        initListener();
    }

    private void initIds() {

        iv_head = (ImageView) findViewById(R.id.iv_head);
        tv_change = (TextView) findViewById(R.id.tv_change);
        tv_count_unend = (TextView) findViewById(R.id.tv_count_unend);
        tv_count_end = (TextView) findViewById(R.id.tv_count_end);
        tv_count_total = (TextView) findViewById(R.id.tv_count_total);
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_department = (TextView) findViewById(R.id.tv_department);
        tv_position = (TextView) findViewById(R.id.tv_position);
        mlist = (PullToRefreshListView) findViewById(R.id.list_business);

    }

    private void initView() {
        ct = this;
       setTitle(getString(R.string.crmmain_client));

        if (CommonUtil.isNetWorkConnected(ct)) {
            loadParams(3);
        } else {
            ToastUtil.showToast(ct, R.string.networks_out);
        }

        mAdapter = new BussinessDetailAdapter(ct, jsonArray);
        mlist.setAdapter(mAdapter);

        mEmptyLayout = new EmptyLayout(this, mlist.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
    }

    private void initListener() {
        tv_count_unend.setOnClickListener(this);
        tv_count_end.setOnClickListener(this);
        tv_count_total.setOnClickListener(this);
        mlist.setMode(PullToRefreshBase.Mode.BOTH);
        mlist.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                sendHttpResquest(Constants.HTTP_SUCCESS_INIT, 0, page, isSelected, "");
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                sendHttpResquest(Constants.HTTP_SUCCESS_INIT, 0, ++page, isSelected, "");
            }
        });
        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BussinessDetailAdapter.ViewHolder viewHolder = (BussinessDetailAdapter.ViewHolder) view.getTag();
                if (viewHolder.tv_auditstatus.getText().toString().equals("已审核")) {
                    startActivity(new Intent(ct, CustomerDetailActivity.class).putExtra("code", viewHolder.tv_code
                            .getText().toString()).putExtra("falg", falg));
                } else {
                    startActivityForResult(new Intent(ct, CustomerAddActivity.class)
                                    .putExtra("type", 1)
                                    .putExtra("status", viewHolder.tv_auditstatus.getText().toString()).
                                            putExtra("code", viewHolder.tv_code
                                                    .getText().toString())
                            , FLAG_ADD_CUSTOMER);
                }

            }
        });
    }


    public void changeOnChange(View v) {
        if (v.getId() == R.id.tv_change) {
            showPopupWindow(v);
        }
    }

    private void initData() {
        em_code = CommonUtil.getSharedPreferences(ct, "erp_username");
        if (CommonUtil.isNetWorkConnected(ct)) {
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, kind, page, 0, "");
        } else {
            ToastUtil.showToast(ct, R.string.networks_out);
        }
        dbManager = new DBManager(ct);
        List<EmployeesEntity> db = dbManager.select_getEmployee(
                new String[]{CommonUtil.getSharedPreferences(ct, "erp_master"),
                        CommonUtil.getSharedPreferences(ct, "erp_username")}
                , "whichsys=? and em_code=? ");
        if (!ListUtils.isEmpty(db)) {
            tv_position.setText(db.get(0).getEM_POSITION());
            tv_department.setText(db.get(0).getEM_DEFAULTORNAME() + "|");
            AvatarHelper.getInstance().displayAvatar(String.valueOf(db.get(0).getEm_IMID()), iv_head, false);
            if (!StringUtil.isEmpty(db.get(0).getEM_NAME())) {
                tv_name.setText(db.get(0).getEM_NAME());
            } else {
                tv_name.setText(MyApplication.getInstance().mLoginUser.getNickName());
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbManager.closeDB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_customer_top, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.crm_data_add) {
            if (falg == 0) {
                startActivity(new Intent(ct, CustomerAddActivity.class).putExtra("type", 1));//正式
            } else {
                startActivity(new Intent(ct, CustomerAddActivity.class).putExtra("type", 0));//预录
            }
        } else if (item.getItemId() == R.id.crm_data_find) {
            startActivity(new Intent(ct, CustomerListSelectActivity.class));
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_count_unend) {
            tv_count_total.setSelected(false);
            tv_count_end.setSelected(false);
            tv_count_unend.setSelected(true);
            type = 0;
            jsonArray.clear();
//                Crouton.makeText(this,"切换成功", 0xffff8888, 10000).show();
            page = 1;
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, kind, page, 0, "");
        } else if (v.getId() == R.id.tv_count_end) {
            tv_count_total.setSelected(false);
            tv_count_end.setSelected(true);
            tv_count_unend.setSelected(false);
            type = 1;
            page = 1;
            jsonArray.clear();
//                Crouton.makeText(this,"切换成功", Style.CONFIRM).show();
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, kind, page, 0, "");
        } else if (v.getId() == R.id.tv_count_total) {
            tv_count_total.setSelected(true);
            tv_count_end.setSelected(false);
            tv_count_unend.setSelected(false);
            jsonArray.clear();
            type = 2;
            page = 1;
//                Crouton.makeText(this,"切换成功", Style.CONFIRM).show();
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, kind, page, 0, "");
        }
    }

    private class BussinessDetailAdapter extends BaseAdapter {
        private Context ct;
        private JSONArray mdata = new JSONArray();
        private LayoutInflater inflater;

        public BussinessDetailAdapter(Context ct, JSONArray data) {
            this.ct = ct;
            this.mdata = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mdata.size();
        }

        @Override
        public JSONObject getItem(int position) {
            return mdata.getJSONObject(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_customer_main, null);
                holder = new ViewHolder();
                holder.tv_title = (TextView) convertView.findViewById(R.id.tv_cu_title);
                holder.tv_state = (TextView) convertView.findViewById(R.id.tv_cu_state);
                holder.tv_money = (TextView) convertView.findViewById(R.id.tv_cu_money);
                holder.tv_leader = (TextView) convertView.findViewById(R.id.tv_cu_leader);
                holder.tv_date = (TextView) convertView.findViewById(R.id.tv_crm_business_date);
                holder.tv_datetv = (TextView) convertView.findViewById(R.id.tv_crm_business_datetv);
                holder.tv_step = (TextView) convertView.findViewById(R.id.tv_cu_step);
                holder.tv_code = (TextView) convertView.findViewById(R.id.tv_cu_code);
                holder.tv_auditstatus = (TextView) convertView.findViewById(R.id.tv_cu_auditstatus);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tv_title.setText(mdata.getJSONObject(position).getString("cu_name"));
            holder.tv_money.setText(mdata.getJSONObject(position).getString("count"));
            holder.tv_step.setText(mdata.getJSONObject(position).getString("cu_nichestep"));
            holder.tv_date.setText(mdata.getJSONObject(position).getString("cu_lastdate"));
            holder.tv_auditstatus.setText(mdata.getJSONObject(position).getString("cu_auditstatus"));
            holder.tv_datetv.setText(getString(R.string.last_followup_time));
            holder.tv_leader.setText(mdata.getJSONObject(position).getString("cu_sellername"));//cu_contact
            holder.tv_code.setText(mdata.getJSONObject(position).getString("cu_code"));
            if (type == 0) {
                holder.tv_state.setVisibility(View.VISIBLE);
                holder.tv_state.setText(getString(R.string.client_Not_traded));
            } else if (type == 1) {
                holder.tv_state.setVisibility(View.VISIBLE);
                holder.tv_state.setText(getString(R.string.client_Deal_done));
            } else if (type == 2) {
                /*if (position <= (unEndSize - 1)) {
                    holder.tv_state.setText("未成交");
                } else {
                    holder.tv_state.setText("已成交");
                }*/
                holder.tv_state.setVisibility(View.GONE);
            }
            return convertView;
        }


        class ViewHolder {
            TextView tv_code;
            TextView tv_auditstatus;
            TextView tv_state;
            TextView tv_title;
            TextView tv_money;
            TextView tv_step;
            TextView tv_leader;
            TextView tv_datetv;
            TextView tv_date;
        }
    }


    private PopupWindow popupWindow = null;

    public void showPopupWindow(View parent) {
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
                    switch (position) {
                        case 0:
                            isSelected = 0;
                            page = 1;
                            kind = 0;
                            tv_name.setText(MyApplication.getInstance().mLoginUser.getNickName());
                            initData();
                            popupWindow.dismiss();
                            break;
                        case 1:
                            Intent intent = new Intent(ct, UserSelectActivity.class);
                            intent.putExtra("single", true);
                            intent.putExtra("net", true);
                            startActivityForResult(intent, 0x11);
                            popupWindow.dismiss();
                            break;
                     /*   case 2:
                            popupWindow.dismiss();
                            break;*/

                    }
                }
            });
            popupWindow = new PopupWindow(view, windowManager.getDefaultDisplay().getWidth() / 3, windowManager.getDefaultDisplay().getHeight() / 3);
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(CustomerListActivity.this, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        popupWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);
    }


    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.client_myself));
        list.add(map);

        map = new HashMap<String, Object>();
        map.put("item_name", getString(R.string.client_subordinate));
        list.add(map);

   /*     map = new HashMap<String, Object>();
        map.put("item_name", "取消");
        list.add(map);*/


        return list;
    }

    JSONArray jsonArray = new JSONArray();
    private int unEndSize = 0;
    private int endSize = 0;
    private int totalSize = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    ToastMessage(getString(R.string.common_up_finish));
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    JSONArray json = JSON.parseObject(msg.getData().getString("result")).getJSONArray("customers");
                    JSONArray json0 = JSON.parseObject(msg.getData().getString("result")).getJSONArray("customers0");
                    JSONArray json1 = JSON.parseObject(msg.getData().getString("result")).getJSONArray("customers1");
                    if (json0 != null && json1 != null) {
                        if (page == 1) {
                            if (json0.size() > 0) {
                                unEndSize = json0.getJSONObject(0).getIntValue("count");
                            } else unEndSize = 0;
                            if (json.size() > 0) {
                                totalSize = json.getJSONObject(0).getIntValue("count");
                            } else totalSize = 0;
                            if (json1.size() > 0) {
                                endSize = json1.getJSONObject(0).getIntValue("count");
                            } else endSize = 0;
                            CommonUtil.textSpanForStyle(tv_count_unend, unEndSize + getString(R.string.client_Not_traded), String.valueOf(unEndSize),
                                    ct.getResources().getColor(R.color.yellow_home));
                            CommonUtil.textSpanForStyle(tv_count_end, endSize + getString(R.string.client_Deal_done), String.valueOf(endSize),
                                    ct.getResources().getColor(R.color.yellow_home));
                            CommonUtil.textSpanForStyle(tv_count_total, totalSize + getString(R.string.client_All_customers), String.valueOf(totalSize),
                                    ct.getResources().getColor(R.color.yellow_home));
                        }

                    }
                    switch (type) {
                        case 0:
                            json = json0;
                            break;
                        case 1:
                            json = json1;
                            break;
                        case 2:

                            break;
                    }
                    if (page == 1) jsonArray.clear();
                    jsonArray.addAll(json);
                    mAdapter.notifyDataSetChanged();
                    if (ListUtils.isEmpty(jsonArray))
                        mEmptyLayout.showEmpty();
                    mlist.onRefreshComplete();
                    progressDialog.dismiss();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    break;
                case 3:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    falg = Integer.valueOf(JSON.parseObject(msg.getData().getString("result")).getString("isStart"));
                    break;
            }
        }
    };

    private void sendHttpResquest(int what, int kind, int page, int isSelected, String emplist) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getCustomerDetail.action";
        Map<String, Object> params = new HashMap<>();
        params.put("emcode", em_code);
        params.put("page", page);
        params.put("pageSize", "10");
        params.put("isSelected", isSelected);
        params.put("emplist", emplist);
        params.put("type", "2");
        params.put("kind", kind);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private int isSelected = 0;
    private int kind;
    private String em_code;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x11 && 0x11 == resultCode && data != null) {
            Employees entity = data.getParcelableExtra("data");
            if (entity == null || entity.getEm_code() == null) return;
            isSelected = 1;
            page = 1;
            kind = 0;
            em_code = entity.getEm_code();
            tv_name.setText(entity.getEm_name());
            tv_department.setText(entity.getEm_defaultorname() + "->");
            tv_position.setText(entity.getEm_position());
            AvatarHelper.getInstance().displayAvatar(String.valueOf(entity.getEm_id()), iv_head, false);
            sendHttpResquest(Constants.HTTP_SUCCESS_INIT, kind, page, isSelected, em_code);
        }

        if (requestCode == FLAG_ADD_CUSTOMER && resultCode == CustomerAddActivity.RESULT_CUSTOMER_LIST) {
            initData();
        }
    }

    private void loadParams(int what) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/ifuseprecustomer.action";
        Map<String, Object> params = new HashMap<>();
        params.put("currentsystem=", CommonUtil.getSharedPreferences(ct, "erp_master"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }
}

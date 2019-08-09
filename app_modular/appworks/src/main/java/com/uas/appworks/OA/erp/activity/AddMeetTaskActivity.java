package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appworks.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AddMeetTaskActivity extends BaseActivity {
    private PullToRefreshListView listView;
    private boolean isPower = true;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("result");
            switch (msg.what) {
                case 0x15:
                    JSONObject object = JSON.parseObject(message);
                    if (object.containsKey("data") && object.getJSONArray("data").size() > 0) {
                        adapter.setArray(object.getJSONArray("data"));
                    }
                    break;
                default:
                    if (JSONUtil.validate(message)) {
                        ToastUtil.showToast(ct, JSON.parseObject(message).getString("exceptionInfo"));
                    } else {
                        ToastUtil.showToast(ct, StringUtil.getChinese(message));
                    }
                    if (StringUtil.isInclude(message, "查看列表的权限")) {
                        isPower = false;
                    }
                    break;
            }

        }
    };
    private String meetName;
    private AddMeetTaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meet_task);
        setTitle(R.string.meet_task);
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.add_item == item.getItemId()) {
            Intent intent = new Intent("com.modular.task.TaskAddErpActivity");
            intent.putExtra("reid", R.style.OAThemeMeet);
            intent.putExtra("type", 2);
            intent.putExtra("isPower", true);
            intent.putExtra("meetname", meetName);
            startActivityForResult(intent, 0x11);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_icon, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x11 && resultCode == 0x20) {
            if (!isPower)
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast(ct, R.string.not_power_check_bill);
                    }
                }, 1000);
            else
                loadNetData();
        } else if (requestCode == 0x21 && resultCode == 0x20) {
            loadNetData();
        }
    }

    private void initView() {
        listView = (PullToRefreshListView) findViewById(R.id.listview);
        listView.setEmptyView(R.layout.view_empty);
        adapter = new AddMeetTaskAdapter();
        listView.setAdapter(adapter);
        meetName = getIntent().getStringExtra("name");
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JSONArray array = adapter.getArray();
                JSONObject object = array.getJSONObject(i - 1 < 0 ? 0 : (i - 1));
                Intent intent = new Intent(ct, DetailTaskActivity.class);
                intent.putExtra("description", object.containsKey("description") ? object.getString("description") : null);
                intent.putExtra("duration", object.containsKey("duration") ? object.getString("duration") : null);
                intent.putExtra("status", object.containsKey("ra_status") ? object.getString("ra_status") : null);
                intent.putExtra("taskname", object.containsKey("ra_taskname") ? object.getString("ra_taskname") : null);//名称
                intent.putExtra("taskemcode", object.containsKey("recorder") ? object.getString("recorder") : null);//发起人
                intent.putExtra("tasktime", object.containsKey("ra_startdate") ? object.getString("ra_startdate") : null);//发起时间
                intent.putExtra("endtime", object.containsKey("ra_enddate") ? object.getString("ra_enddate") : null);
                intent.putExtra("performer", object.containsKey("resourcename") ? object.getString("resourcename") : "未填写");//处理人名字
                intent.putExtra("taskcode", object.containsKey("ra_resourcecode") ? object.getString("ra_resourcecode") : null);//处理人编号
                intent.putExtra("ra_taskid", object.containsKey("ra_taskid") ? object.getString("ra_taskid") : null);//取回复内容id
                intent.putExtra("taskid", object.containsKey("ra_id") ? object.getString("ra_id") : null);//编号
                startActivityForResult(intent, 0x21);
            }
        });
        loadNetData();
    }

    private void loadNetData() {
        String url = CommonUtil.getSharedPreferences(this, "erp_baseurl") + "common/datalist/data.action";
        String emcode = CommonUtil.getSharedPreferences(ct, "erp_username");
        String caller = "ResourceAssignment";
        String name = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(name)) {
            name = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        //添加限制   or ( =" + "'" + emcode + "')  recorder
        String condition = "(ra_resourcecode='" + emcode + "'  or " + "recorder=" + "'" + name + "'" + ") and " + "(taskorschedule='MTask' and " + "ra_taskname='" + meetName + "') ";
        final Map<String, Object> param = new HashMap<>();
        param.put("caller", caller);
        param.put("emcode", emcode);
        param.put("page", 1);
        param.put("pageSize", 100);
        param.put("condition", condition);
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x15, null, null, "get");
    }

    class AddMeetTaskAdapter extends BaseAdapter {
        private JSONArray array;

        public AddMeetTaskAdapter() {
        }

        public AddMeetTaskAdapter(JSONArray array) {
            this.array = array;
        }

        public JSONArray getArray() {
            return array;
        }

        public void setArray(JSONArray array) {
            this.array = array;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return array == null ? 0 : array.size();
        }

        @Override
        public Object getItem(int i) {
            return array.get(i);
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
                view = LayoutInflater.from(ct).inflate(R.layout.item_activity_meettask, null);
                holder.head_img = (TextView) view.findViewById(R.id.head_img);
                holder.name_tv = (TextView) view.findViewById(R.id.name_tv);
                holder.time_tv = (TextView) view.findViewById(R.id.time_tv);
                holder.theme_tv = (TextView) view.findViewById(R.id.theme_tv);
                holder.status_tv = (TextView) view.findViewById(R.id.status_tv);
                holder.date_tv = (TextView) view.findViewById(R.id.date_tv);
                holder.headler_tv = (TextView) view.findViewById(R.id.headler_tv);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            JSONObject object = array.getJSONObject(i);
            bindData(holder, object);
            //绑定数据
            return view;
        }

        private void bindData(ViewHolder holder, JSONObject object) {
            holder.name_tv.setText(object.containsKey("recorder") ? object.getString("recorder") : "");
            holder.theme_tv.setText(object.containsKey("description") ? object.getString("description") : "");
            holder.status_tv.setText(object.containsKey("ra_status") ? object.getString("ra_status") : "");
            String name = object.containsKey("ra_resourcename") ? object.getString("ra_resourcename") : "";
            holder.headler_tv.setText(getResources().getString(R.string.resourcer) + ":" + (StringUtil.isEmpty(name) ? "无" : name));
            if (name != null && name.length() > 0) {
                String str = name.substring(0, 1);
                holder.head_img.setText(str == null ? "" : str);
            }
            holder.time_tv.setText(object.containsKey("ra_taskname") ? object.getString("ra_taskname") : "");
            holder.date_tv.setText(object.containsKey("ra_startdate") ? object.getString("ra_startdate") : "" + "-" + (object.containsKey("ra_enddate") ? object.getString("ra_enddate") : ""));
        }

        class ViewHolder {
            TextView head_img;
            TextView name_tv, time_tv, theme_tv, date_tv, headler_tv, status_tv;
        }
    }

}

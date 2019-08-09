package com.xzjmyk.pm.activity.ui.erp.activity.crm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.R;

import java.text.DecimalFormat;

/**
 * 我的下属和遗忘客户更多界面
 * type: 1:我的下属   2:遗忘客户
 */
public class SubsActivity extends BaseActivity {
    private ListView list;
    private int type = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subs);
        LinearLayout top = (LinearLayout) findViewById(R.id.top);
        list = (ListView) findViewById(R.id.list);
        if (getIntent() == null) return;
        type = getIntent().getIntExtra("type", -1);
        if (type <= 0) return;
        String title = "UU互联";
        if (type == 2) {
            title = "遗忘客户";
            top.setVisibility(View.GONE);
        } else if (type == 1) {
            title = "我的下属";
            top.setVisibility(View.VISIBLE);
        }
        setTitle(title);
        String chche = getIntent().getStringExtra("data");
        if (!StringUtil.isEmpty(chche) && JSONUtil.validate(chche)) {
            JSONArray array = JSON.parseArray(chche);
            if (array == null) return;
            list.setAdapter(new Adapter(array));
        }
    }

    class Adapter extends BaseAdapter {
        private JSONArray array;

        public Adapter(JSONArray array) {
            this.array = array;
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
                view = LayoutInflater.from(ct).inflate(R.layout.item_subs, null);
                holder.name = (TextView) view.findViewById(R.id.name);
                holder.plan = (TextView) view.findViewById(R.id.plan);
                holder.salea = (TextView) view.findViewById(R.id.salea);
                holder.subs = (LinearLayout) view.findViewById(R.id.subs);

                holder.custs = (LinearLayout) view.findViewById(R.id.custs);
                holder.name_tv = (TextView) view.findViewById(R.id.name_tv);
                holder.time = (TextView) view.findViewById(R.id.time);
                holder.day = (TextView) view.findViewById(R.id.day);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            if (type == 1) {
                holder.subs.setVisibility(View.VISIBLE);
                holder.custs.setVisibility(View.GONE);
                JSONObject object = array.getJSONObject(i);
                holder.name.setText(getValue(object, "CUSTOMERCOUNT"));
                holder.plan.setText(getFloatValue(object, ("RANK")) + "/" + getFloatValue(object, ("TOPCOUNT")));
                holder.salea.setText(getFloatValue(object, ("FIRSTBFCOUNT")) + "/" + getFloatValue(object, ("ACTUALPROFIT")));
            } else if (type == 2) {
                JSONArray arrays = array.getJSONArray(i);
                holder.subs.setVisibility(View.GONE);
                holder.custs.setVisibility(View.VISIBLE);
                holder.name_tv.setText("客戶：" + getValues(arrays, 0));
                String time = getValues(arrays, 1);
                holder.time.setText(time);
                if (time.length() > 5)//容cuo
                {
                    int j = (int) ((System.currentTimeMillis() - DateFormatUtil.str2Long(time, DateFormatUtil.YMD)) / (60 * 1000 * 24 * 60));
                    holder.day.setText("距离上次跟进" + j + "天");
                }
            }
            return view;
        }

        private String getValues(JSONArray array, int i) {
            return array.getString(i) == null ? "" : array.getString(i);
        }

        private String getValue(JSONObject object, String key) {
            if (!object.containsKey(key) || StringUtil.isEmpty(object.getString(key))) return "";
            return object.getString(key);
        }

        private String getFloatValue(JSONObject object, String key) {
            if (!object.containsKey(key) || StringUtil.isEmpty(object.getString(key)))
                return "0.0";
            float f = object.getFloatValue(key);
            if (f < 1f) {
                return f + "";
            }
            DecimalFormat df = new DecimalFormat(".##");
            return df.format(f);
        }

        class ViewHolder {
            LinearLayout subs;
            TextView name, plan, salea;
            LinearLayout custs;
            TextView name_tv, time, day;


        }
    }
}

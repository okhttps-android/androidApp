package com.xzjmyk.pm.activity.ui.erp.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;

/**
 * Created by gongpm on 2016/7/1.
 */
public class OaRegisterFragment extends Fragment {
    private ListView listView;

    public synchronized static OaRegisterFragment getInstance(JSONArray data) {
        OaRegisterFragment fragment = new OaRegisterFragment();
        Bundle bundle = new Bundle();
        bundle.putString("DATA", data.toString());
        fragment.setArguments(bundle);
        return fragment;
    }

    //TODO 以上为测试、待优
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_oa, container, false);
        Bundle bundle = getArguments();
        String data = bundle.getString("DATA");
        JSONArray array = null;
        if (JSONUtil.validate(data)) {
            array = JSON.parseArray(data);
        } else {
            array = new JSONArray();
        }
        initView(view, array);
        return view;
    }

    private void initView(View view, JSONArray array) {
        listView = (ListView) view.findViewById(R.id.listview);
        if (array == null || array.size() <= 0) return;
        MeetSelectAdapter adapter = new MeetSelectAdapter(array);
        listView.setAdapter(adapter);

    }

    class MeetSelectAdapter extends BaseAdapter {
        private JSONArray json;

        public MeetSelectAdapter(JSONArray json) {
            this.json = json;
        }

        @Override
        public int getCount() {
            return json == null ? 0 : json.size();
        }

        @Override
        public Object getItem(int i) {
            return json == null ? 0 : json.get(i);
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
                view = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.meet_select_item, null);
                holder.select_scb = (CheckBox) view.findViewById(R.id.select_scb);
                holder.id_tv = (TextView) view.findViewById(R.id.id_tv);
                holder.name_tv = (TextView) view.findViewById(R.id.name_tv);
                holder.head_img = (ImageView) view.findViewById(R.id.head_img);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            holder.select_scb.setVisibility(View.GONE);
            holder.select_scb.setFocusable(false);
            holder.select_scb.setClickable(false);
            JSONObject object = json.getJSONObject(i);
            String name = (object.containsKey("EM_NAME") && !StringUtil.isEmpty(object.getString("EM_NAME"))) ? object.getString("EM_NAME") : "";
            String position = "";
            if (object.containsKey("EM_DEPART") && !StringUtil.isEmpty(object.getString("EM_DEPART")) &&
                    object.containsKey("EM_DEFAULTHSNAME") && !StringUtil.isEmpty(object.getString("EM_DEFAULTHSNAME"))) {
                position = object.getString("EM_DEPART") + " | " + object.getString("EM_DEFAULTHSNAME");
            } else {
                position = ((object.containsKey("EM_DEPART") && !StringUtil.isEmpty(object.getString("EM_DEPART"))) ? object.getString("EM_DEPART") : "") +
                        ((object.containsKey("EM_DEPART") && !StringUtil.isEmpty(object.getString("EM_DEPART"))) ? object.getString("EM_DEPART") : "");
            }
            holder.name_tv.setText(name);
            holder.id_tv.setText(position);
            return view;
        }

        class ViewHolder {
            CheckBox select_scb;
            TextView name_tv, id_tv;
            ImageView head_img;
        }
    }


}

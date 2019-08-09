package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.MissionModel;
import com.core.net.http.ViewUtil;
import com.core.utils.TimeUtils;
import com.core.widget.ClearEditText;
import com.core.widget.listener.EditChangeListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.xzjmyk.pm.activity.view.crouton.Style;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DestinationActivity extends BaseActivity {

    @ViewInject(R.id.listView)
    private PullToRefreshListView listView;
    private int pager = 1;
    private List<MissionModel> allList;//所有数据搜索
    private List<MissionModel> showList;//适配器数据
    private DestinAdapter adapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.getData().getString("result");
            listView.onRefreshComplete();
            boolean isJSON = StringUtil.isEmpty(message) ? false : JSONUtil.validate(message);
            switch (msg.what) {
                case 0x11:
                    if (!isJSON) return;
                    try {
                        JSONArray array = JSON.parseObject(message).getJSONArray("data");
                        if (pager == 1) {//刷新
                            showList.clear();
                            allList.clear();
                        }
                        handleDada(array);
                    } catch (Exception e) {

                    }
                    break;
            }

        }
    };
    private ClearEditText search_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);
        ViewUtils.inject(this);
        initView();
        initEvent();
    }

    private void initEvent() {
//        search_edit.addTextChangedListener(new EditChangeListener() {
//            @Override
//            public void afterTextChanged(Editable editable) {
//                String str = editable.toString().replaceAll(" ", "");
//                showList.clear();
//                for (MissionModel e : allList) {
//                    if (isinclude(str, e)) {
//                        showList.add(e);
//                    }
//                }
//                if (!StringUtil.isEmpty(str) && ListUtils.isEmpty(showList)) {
//                    MissionModel empty = new MissionModel();
//                    empty.setCompanyName(str);
//                    empty.setStatus(2);
//                    showList.add(empty);
//                }
//                adapter.notifyDataSetChanged();
//            }
//        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int item = (i - 2) <= 0 ? 0 : (i - 2);
                MissionModel entity = showList.get(item);
                if (entity == null) return;
                if (entity.getStatus() != 2) {//正常公司
                    Intent intent = new Intent();
                    intent.putExtra("data", entity);
                    setResult(0x20, intent);
                    finish();
                }
            }
        });
        listView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                pager = 1;
                if (search_edit != null)
                    search_edit.getText().clear();
//                search_edit.setText("");
                loadOutAddress();
            }
        });
    }

    private void initView() {
        allList = new ArrayList<>();
        showList = new ArrayList<>();
//        listView.setEmptyView(R.layout.view_empty);
        listView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        adapter = new DestinAdapter();
        listView.setAdapter(adapter);
        setHeader();
        loadOutAddress();
    }

    private void setHeader() {
        View headview = LayoutInflater.from(ct).inflate(R.layout.search_header, null);
        listView.getRefreshableView().addHeaderView(headview);
        search_edit = (ClearEditText) headview.findViewById(R.id.search_edit);
        search_edit.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable.toString().replaceAll(" ", "");
                str = str.replaceAll("\n", "");
                showList.clear();
                for (MissionModel e : allList) {
                    if (isinclude(str, e)) {
                        showList.add(e);
                    }
                }
                if (!StringUtil.isEmpty(str) && ListUtils.isEmpty(showList)) {
                    MissionModel empty = new MissionModel();
                    empty.setCompanyName(str);
                    empty.setStatus(2);
                    showList.add(empty);
                }
                adapter.notifyDataSetChanged();
            }
        });

    }


    //从服务器获取数据
    private void loadOutAddress() {
        if (!MyApplication.getInstance().isNetworkActive()) {
            ViewUtil.ToastMessage(ct, ct.getResources().getString(R.string.networks_out), Style.holoRedLight, 2000);
            return;
        }
        //获取网络数据
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "mobile/getOutAddressDate.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("condition", "1=1");
        param.put("pageIndex", pager);
        param.put("pageSize", 1000);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, handler, headers, 0x11, null, null, "post");
    }


    //判断输入的字符是否在里面
    private boolean isinclude(String str, MissionModel e) {
        String text = e.getCompanyAddr() + e.getCompanyName();
        return StringUtil.isInclude(text, str);
    }

    //处理请求下来的数据
    private void handleDada(JSONArray array) {
        if (ListUtils.isEmpty(array)) return;
        MissionModel entity = null;
        JSONObject object = null;
        for (int i = 0; i < array.size(); i++) {
            try {
                entity = new MissionModel();
                object = array.getJSONObject(i);
                entity.setCompanyAddr(object.getString("MD_ADDRESS"));
                entity.setVisitcount(object.getInteger("MD_VISITCOUNT"));
                entity.setCompanyName(object.getString("MD_COMPANY"));
                entity.setId(object.getInteger("MD_ID"));
                float longitude = object.getFloatValue("MD_LONGITUDE");
                float latitude = object.getFloatValue("MD_LATITUDE");
                if (longitude != 0 && latitude != 0) {
                    entity.setLatLng(new LatLng(latitude, longitude));
                }
                if (object.getInteger("MD_VISITTIME") != null) {
                    try {
                        entity.setVisitTime(TimeUtils.f_long_2_str(object.getLong("MD_VISITTIME")));
                    } catch (Exception e) {
                        Log.i("gongpengming", "e.getMessage" + e.getMessage());

                    }
                }
                showList.add(entity);
                allList.add(entity);
            } catch (Exception e) {

            }
        }
        adapter.notifyDataSetChanged();
    }

    //适配器
    class DestinAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return ListUtils.isEmpty(showList) ? 0 : showList.size();
        }

        @Override
        public Object getItem(int i) {
            return showList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            BaseViewHolder holder = null;
            final MissionModel entity = showList.get(i);
            if (view == null) {
                holder = new BaseViewHolder();
                view = LayoutInflater.from(ct).inflate(R.layout.item_aims_base, null);
                holder.company_tv = (TextView) view.findViewById(R.id.company_tv); //公司名称
                holder.companyaddr_tv = (TextView) view.findViewById(R.id.companyaddr_tv); //公司地址
                holder.visit_num_tv = (TextView) view.findViewById(R.id.visit_num_tv);//拜访次数
                holder.last_time_tv = (TextView) view.findViewById(R.id.last_time_tv); //上次拜访时间
                holder.long_tv = (TextView) view.findViewById(R.id.long_tv);//距离
                holder.content = (RelativeLayout) view.findViewById(R.id.content);
                holder.empty = (LinearLayout) view.findViewById(R.id.empty);
                holder.add_tv = (TextView) view.findViewById(R.id.add_tv);
                holder.add_rl = (RelativeLayout) view.findViewById(R.id.add_rl);
                view.setTag(holder);
            } else {
                holder = (BaseViewHolder) view.getTag();
            }
            if (entity.getStatus() == 2) {
                holder.content.setVisibility(View.GONE);
                holder.empty.setVisibility(View.VISIBLE);
            } else {
                holder.content.setVisibility(View.VISIBLE);
                holder.empty.setVisibility(View.GONE);
            }
            String company = entity.getCompanyName();
            if (!StringUtil.isEmpty(company) && entity.getStatus() == 2) {
                String text = "将 " + company;
                int start = text.indexOf(company.charAt(0));
                SpannableStringBuilder style = new SpannableStringBuilder(text);
                style.setSpan(new TextAppearanceSpan(MyApplication.getInstance(), R.style.text_color),
                        start, company.length() + start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.add_tv.setText(style);
                holder.add_rl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent();
                        intent.putExtra("data", entity);
                        setResult(0x20, intent);
                        finish();
                    }
                });
            }
            if (entity.getStatus() != 2) {
                holder.company_tv.setText(getNotNull(entity.getCompanyName()));
                holder.companyaddr_tv.setText(getNotNull(entity.getCompanyAddr()));
                holder.visit_num_tv.setText("总拜访 " + entity.getVisitcount() + "次");
                holder.last_time_tv.setText("上次拜访 " + getLastime(entity.getVisitTime()));
                holder.long_tv.setText(getKm(LocationDistanceUtils.distanceMeStr(entity.getLatLng())) + "km");
            }
            return view;
        }

        class BaseViewHolder {
            RelativeLayout content, add_rl;
            LinearLayout empty;
            TextView company_tv, //公司名称
                    companyaddr_tv, //公司地址
                    visit_num_tv,//拜访次数
                    last_time_tv, //上次拜访时间
                    long_tv;//距离

            TextView add_tv;


        }

        private String getLastime(String time) {
            if (StringUtil.isEmpty(time)) return "";
            try {
                return TimeUtils.s_long_2_str(TimeUtils.f_str_2_long(time));
            } catch (Exception e) {
                return "";
            }
        }
    }

    private String getKm(String dis) {
        if (StringUtil.isEmpty(dis)) return String.valueOf(0);
        try {
            DecimalFormat fnum = new DecimalFormat("##0.00");
            String dd = fnum.format(Float.valueOf(dis) / 1000);
            return dd;
        } catch (ClassCastException e) {
            return String.valueOf(0);
        } catch (Exception e) {
            return String.valueOf(0);
        }
    }

    private String getNotNull(String str) {
        return StringUtil.isEmpty(str) ? "" : str;
    }
}

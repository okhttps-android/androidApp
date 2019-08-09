package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.BaiduMapUtil;
import com.core.utils.TimeUtils;
import com.core.utils.helper.AvatarHelper;
import com.core.utils.time.wheel.OASigninPicker;
import com.core.widget.EmptyLayout;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.applocation.UasLocationHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class OutLogListActivity extends BaseActivity {
    @ViewInject(R.id.rili_tv)
    private TextView rili_tv;
    @ViewInject(R.id.name_tv)
    private TextView name_tv;
    @ViewInject(R.id.num_tv)
    private TextView num_tv;
    @ViewInject(R.id.prot_tv)
    private TextView prot_tv;
    @ViewInject(R.id.head_img)
    private ImageView head_img;
    @ViewInject(R.id.mapView)
    private MapView mapView;
    @ViewInject(R.id.listview)
    private ListView listview;

    private EmptyLayout mEmptyLayout;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            String message = (String) msg.getData().get("result");
            if (msg.what == 0x13) {
                adapter.setJson(JSON.parseObject(message).getJSONArray("listdata"));
            }
        }
    };

    private String date = null;
    private SigninAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oalist);
        ViewUtils.inject(this);
        init();
        initView();
    }

    private void init() {
        date = DateFormatUtil.long2Str(DateFormatUtil.YMD);
    }

    private void initView() {
        mEmptyLayout = new EmptyLayout(ct, listview);
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        rili_tv.setText(StringUtil.isEmpty(date) ? "" : date);
        String name = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(name))
            name = MyApplication.getInstance().mLoginUser.getNickName();
        name_tv.setText(StringUtil.isEmpty(name) ? "" : name);
        prot_tv.setText("");
        double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();
        BaiduMapUtil.getInstence().setMapViewPoint(mapView, new LatLng(latitude, longitude), true);
        AvatarHelper.getInstance().displayAvatar(MyApplication.getInstance().mLoginUser.getUserId(), head_img, true);
        JSONArray array = getIntent().getParcelableExtra("data");
        adapter = new SigninAdapter();
        listview.setAdapter(adapter);
        if (array == null || array.size() <= 0) {
            loadLog(date);
        } else {
            adapter.setJson(array);
            listview.setAdapter(adapter);
        }
        rili_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateDialog();
            }
        });

    }

    private void showDateDialog() {
        OASigninPicker picker = new OASigninPicker(this);
        picker.setRange(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                if (!time.equals(date)) {
                    date = time;
                    rili_tv.setText(date);
                    loadLog(date);
                }
            }
        });
        picker.show();
    }

    //获取打卡记录 date:yyyy-MM-dd
    private void loadLog(String date) {
        progressDialog.show();
        //获取网络数据
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "mobile/common/list.action";
        String emcode = CommonUtil.getSharedPreferences(ct, "erp_username");
        final Map<String, Object> param = new HashMap<>();
        param.put("currentMaster", CommonUtil.getSharedPreferences(ct, "erp_master"));
        param.put("page", 1);
        param.put("pageSize", 1000);
        param.put("emcode", emcode);
        param.put("condition", "mo_mancode='" + emcode + "' and to_char(mo_signtime,'yyyy-MM-dd')='" + date + "'");
        param.put("caller", "Mobile_outsign");
        param.put("sessionId", CommonUtil.getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x13, null, null, "get");
    }


    class SigninAdapter extends BaseAdapter {
        private JSONArray json;

        public SigninAdapter() {
        }

        public JSONArray getJson() {
            return json;
        }

        public void setJson(JSONArray json) {
            prot_tv.setText("本日签到 " + json.size() + " 次");
            this.json = json;
            if (json == null || json.size() <= 0) mEmptyLayout.showEmpty();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return json == null ? 0 : json.size();
        }

        @Override
        public Object getItem(int i) {
            return json.get(i);
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
                view = LayoutInflater.from(ct).inflate(R.layout.item_outoffice_list, null);
                holder.time = (TextView) view.findViewById(R.id.time);
                holder.day = (TextView) view.findViewById(R.id.day);
                holder.location = (TextView) view.findViewById(R.id.location);
                holder.addr = (TextView) view.findViewById(R.id.addr);
                holder.remark = (TextView) view.findViewById(R.id.remark);
                holder.image = (ImageView) view.findViewById(R.id.image);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            final JSONObject object = json.getJSONObject(i);
            String str = getStrByJson(object, "mo_signtime");
            if (!StringUtil.isEmpty(str)) {
                holder.time.setText(DateFormatUtil.long2Str(TimeUtils.f_str_2_long(str), "HH:mm"));
                holder.day.setText(DateFormatUtil.long2Str(TimeUtils.f_str_2_long(str), "MM/dd"));
            }
            holder.location.setText(getStrByJson(object, "mo_company"));
            holder.addr.setText(getStrByJson(object, "mo_address"));
            holder.remark.setText(getStrByJson(object, "mo_remark"));
            int reId = 0;
            try {
                reId = object.containsKey("mo_attachid") ? object.getIntValue("mo_attachid"):0;
            } catch (Exception e) {
            }
            if (reId>1000){
                holder.image.setVisibility(View.VISIBLE);
                final String imageUrl=getImageUrl(object.getInteger("mo_attachid"));
                LogUtil.i("imageUrl="+imageUrl);
                ImageLoader.getInstance().displayImage(imageUrl, holder.image);
                holder.image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent("com.modular.tool.SingleImagePreviewActivity");
                        intent.putExtra(AppConstant.EXTRA_IMAGE_URI, imageUrl);
                        ct.startActivity(intent);
                    }
                });
            }else{
                holder.image.setVisibility(View.GONE);
            }
            return view;
        }

        private String getStrByJson(JSONObject object, String key) {
            if (object.containsKey(key)) {
                return object.getString(key) == null ? "" : object.getString(key);
            }
            return "";
        }

        private String getImageUrl(int id) {
            return CommonUtil.getAppBaseUrl(ct) + "common/downloadbyId.action?id=" + id + "&sessionId=" +
                    CommonUtil.getSharedPreferences(ct, "sessionId") +
                    "&sessionUser=" + CommonUtil.getSharedPreferences(ct, "erp_username") +
                    "&master=" + CommonUtil.getSharedPreferences(ct, "erp_master");
        }

        class ViewHolder {
            TextView day,//日期
                    time,//时间
                    location,//位置名称
                    addr,//位置详细
                    remark;//备注
            ImageView image;//图片

        }
    }
}

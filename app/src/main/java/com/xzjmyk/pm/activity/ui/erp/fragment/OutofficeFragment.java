package com.xzjmyk.pm.activity.ui.erp.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.AppConfig;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.net.http.ViewUtil;
import com.core.utils.BaiduMapUtil;
import com.core.utils.TimeUtils;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.view.Activity.SearchLocationActivity;
import com.core.widget.view.model.SearchPoiParam;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.applocation.UasLocationHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.base.EasyFragment;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.OutLogListActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.OutSigninOKActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.OutofficeActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.OutofficeSetActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.xzjmyk.pm.activity.util.oa.CommonUtil.getSharedPreferences;

/**
 * 1.0x11 获取打卡记录列表
 * 2.0x12 获取判断用户是否是管理员接口
 * 3.0x13 获取考勤设置接口
 * 4.0x21 requestCode
 * Created by gongpm on 2016/7/14.
 */
public class OutofficeFragment extends EasyFragment implements View.OnClickListener {
    private LatLng point;//位置
    private int sigNum;//签到次数
    @ViewInject(R.id.name_img)
    private ImageView name_img;//头像
    @ViewInject(R.id.name_tv)
    private TextView name_tv;//名字
    @ViewInject(R.id.com_tv)
    private TextView com_tv;
    @ViewInject(R.id.num_tv)
    private TextView num_tv; //签到次数
    @ViewInject(R.id.click_btn)
    private Button click_btn;//点击签到
    @ViewInject(R.id.location_tv)
    private TextView out_add_name_tv;//地点名称
    @ViewInject(R.id.addr_tv)
    private TextView out_add_t_tv;//地址
    @ViewInject(R.id.mapView)
    private MapView mapView;
    @ViewInject(R.id.date_tv)
    private TextView date_tv;
    @ViewInject(R.id.do_trim)
    private TextView do_trim;
    private OutofficeActivity ct;
    private JSONArray json = null;
    private boolean adminStatus = false;
    private boolean isAddress = false;
    private boolean isImage = false;
    private int distance = 0;
    boolean isRetuen = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setLocation();
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ct.progressDialog.dismiss();
            String message = msg.getData().getString("result");
            JSONObject object = null;
            switch (msg.what) {
                case 0x11:
                    object = JSON.parseObject(message);
                    if (object.containsKey("listdata")) {
                        json = object.getJSONArray("listdata");
                        sigNum = json.size();
                        num_tv.setText(Html.fromHtml("<u>" + sigNum + "</u>"));
                    }
                    break;

                case 0x13:
                    object = JSON.parseObject(message);
                    if (object.containsKey("success") && object.getBoolean("success")) {
                        isAddress = object.containsKey("isAddress") ? ("1".equals(object.getString("isAddress")) ? true : false) : false;
                        isImage = object.containsKey("isImage") ? ("1".equals(object.getString("isImage")) ? true : false) : false;
                        distance = object.containsKey("distance") ? Integer.parseInt(object.getString("distance")) : 0;
                        if (isAddress)
                            do_trim.setVisibility(View.VISIBLE);
                        else
                            do_trim.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_outoffice;
    }


    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(ct).unregisterReceiver(receiver);
        super.onDestroy();
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.num_tv:
                Intent list = new Intent(ct, OutLogListActivity.class);
                list.putExtra("data", json);
                startActivity(list);
                break;
            case R.id.do_trim:
                intent = new Intent(ct, SearchLocationActivity.class);
                SearchPoiParam poiParam = new SearchPoiParam();
                poiParam.setType(1);
                poiParam.setTitle(MyApplication.getInstance().getResources().getString(R.string.unoffice));
                poiParam.setRadius(distance);
                poiParam.setResultCode(SearchPoiParam.DEFAULT_RESULTCODE);
                poiParam.setDistanceTag(MyApplication.getInstance().getResources().getString(R.string.rice));
                intent.putExtra("data", poiParam);
                ct.startActivityForResult(intent, 0x21);
                break;
            case R.id.click_btn:
                intent = new Intent(getActivity(), OutSigninOKActivity.class);
                intent.putExtra("addr", out_add_t_tv.getText().toString());
                intent.putExtra("com", out_add_name_tv.getText().toString());
                intent.putExtra("isImage", isImage);
                if (json != null)
                    intent.putExtra("list", json);
                startActivityForResult(intent, 0x21);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.title) {
            Intent intent = new Intent(ct, OutofficeSetActivity.class);
            intent.putExtra("isAddress", isAddress);
            intent.putExtra("isImage", isImage);
            intent.putExtra("distance", distance);
            startActivityForResult(intent, 0x21);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (ct.getIntent() != null && adminStatus)//管理员
            inflater.inflate(R.menu.menu_outoffice, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (0x21 == requestCode) {
            isRetuen = true;
            if (resultCode ==SearchPoiParam.DEFAULT_RESULTCODE) {
                PoiInfo info = data.getParcelableExtra(SearchPoiParam.DEFAULT_RESULTKEY);
                if (info != null) {
                    out_add_name_tv.setText(info.name);
                    out_add_t_tv.setText(info.address);
                    point = info.location;
                    BaiduMapUtil.getInstence().setMapViewPoint(mapView, point, true);
                }
            } else if (resultCode == 0x12) {
                boolean isOK = data.getBooleanExtra("result", false);
                if (isOK) {
                    sigNum++;
                    num_tv.setText(Html.fromHtml("<u>" + sigNum + "</u>"));
                }
            } else if (resultCode == 0x21) {
                isImage = data.getBooleanExtra("isImage", false);
                isAddress = data.getBooleanExtra("isAddress", false);
                distance = data.getIntExtra("distance", 100);
                if (isAddress)
                    do_trim.setVisibility(View.VISIBLE);
                else
                    do_trim.setVisibility(View.GONE);
            } else if (resultCode == 0x20) {
                if (data != null) {
                    boolean isAuto = data.getBooleanExtra("isAuto", false);
                    if (!isAuto) return;
                    ct.finish();
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ct = (OutofficeActivity) context;

    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        setHasOptionsMenu(true);
        ViewUtils.inject(getmRootView());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_LOCATION_CHANGE);
        LocalBroadcastManager.getInstance(ct).registerReceiver(receiver, filter);
        adminStatus = ct.getIntent() == null ? false : ct.getIntent().getBooleanExtra(AppConfig.IS_ADMIN, false);
        initview();
        initData();
    }

    private void initview() {
        num_tv.setOnClickListener(this);
        date_tv.setText(DateFormatUtil.long2Str("yyyy年MM月dd日") +
                " " + CalendarUtil.getWeek(System.currentTimeMillis()) +
                " " + DateFormatUtil.long2Str("HH:mm"));
        num_tv.setText("" + sigNum);
        String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        AvatarHelper.getInstance().displayAvatar(loginUserId, name_img, true);
        String name = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(name))
            name = MyApplication.getInstance().mLoginUser.getNickName();
        name_tv.setText(name);
        com_tv.setText(CommonUtil.getSharedPreferences(ct, "erp_commpany"));
        setLocation();
        do_trim.setOnClickListener(this);
        click_btn.setOnClickListener(this);
        if (isAddress)
            do_trim.setVisibility(View.VISIBLE);
        else
            do_trim.setVisibility(View.GONE);
         UasLocationHelper.getInstance().requestLocation();
    }

    private void setLocation() {
        String name = UasLocationHelper.getInstance().getUASLocation().getName();
        String addr = UasLocationHelper.getInstance().getUASLocation().getAddress();
        double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();
        out_add_name_tv.setText(name);
        out_add_t_tv.setText(addr);
        point = new LatLng(latitude, longitude);
        BaiduMapUtil.getInstence().setMapViewPoint(mapView, point);
    }

    private void initData() {
        loadLog();
        getSet();
        saveSet();
    }

    private void getSet() {
        Map<String, Object> param = new HashMap<>();
        param.put("code", "isImage,isAddress,distance");
        net("mobile/getconfigs.action", param, 0x13);
    }

    //获取外勤设置
    private void getSet(int i) {
        Map<String, Object> param = new HashMap<>();
        net("mobile/getOutSet.action", param, 0x20);

    }

    //保存外测试
    private void saveSet() {
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "lp");
        Map<String, Object> formStore = new HashMap<>();
        formStore.put("Mo_id", 0);
        formStore.put("mo_distance", 1000);//距离
        formStore.put("Mo_visittime", TimeUtils.f_long_2_str(System.currentTimeMillis()));//拜访时间
        param.put("formStore", JSONUtil.map2JSON(formStore));
        net("mobile/update.action", param, 0x20);
    }


    //获取打卡记录 date:yyyy-MM-dd
    private void loadLog() {
        if (emcode == null)
            emcode = getSharedPreferences(ct, "erp_username");
        final Map<String, Object> param = new HashMap<>();
        param.put("currentMaster", getSharedPreferences(ct, "erp_master"));
        param.put("page", 1);
        param.put("pageSize", 1000);
        param.put("condition", "mo_mancode='" + emcode + "' and to_char(mo_signtime,'yyyy-MM-dd')='" + DateFormatUtil.long2Str(DateFormatUtil.YMD) + "'");
        param.put("caller", "Mobile_outsign");
        net("mobile/common/list.action", param, 0x11);
    }

    String baseUrl = null;
    String emcode = null;

    private void net(String action, Map<String, Object> param, int what) {
        ct.progressDialog.show();
        if (baseUrl == null)
            baseUrl = getSharedPreferences(ct, "erp_baseurl");
        if (emcode == null)
            emcode = getSharedPreferences(ct, "erp_username");
        String url = baseUrl + action;

        param.put("emcode", emcode);
        param.put("sessionId", getSharedPreferences(ct, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, what, null, null, "get");
    }


}

package com.uas.appworks.OA.erp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.MyListView;
import com.core.widget.view.Activity.SearchLocationActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.model.SearchPoiParam;
import com.lidroid.xutils.ViewUtils;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.applocation.UasLocationHelper;
import com.uas.appworks.OA.erp.model.OfficeAddressBean;
import com.uas.appworks.OA.platform.model.PlatSignAddressBean;
import com.uas.appworks.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by FANGlh on 2017/1/16.
 * function: 打卡2.0办公地址设置
 */

public class OfficeAddressSettingsActivity extends BaseActivity {
    private static final int SIGN_RANGE_REQUESTCODE = 0x117;
    private static final int GET_SIGN_DDRESSS = 11901;
    private static final int SAVE_ADDRESS_REQUEST = 12001;
    private static final int WORK_RANGE_REQUESTCODE = 12002;
    private static final int ADDRESS_DELETE_REQUEST = 12101;
    private static final int UPDATE_VALUE_REQUEST = 2181;
    private static final int GET_PLAT_SIGN_ADDRESS = 0307;
    private static final int DELETE_PLAT_SIGN_ADDRESS = 3101;
    private static final int SAVE_PLAT_OFFICE_ADDRESS = 3102;
    private TextView sign_range;  //打卡范围
    private TextView work_range;  //办公范围
    private MyListView address_lv;  //地址列表
    private RelativeLayout add_address;  // + 添加办公地址
    private Button save_btn;   // 保存
    private OfficeAddressAdapter myadapter;
    private int search_rangge = 0;
    private final int ADDRESS_CHANGE = 0x14;
    private List<String> address_names;  //办公地址简称
    private List<String> address_location; //办公地址详细
    private List<String> address_longitude; //经度
    private List<String> address_latitud; //纬度
    private List<String> address_id; //地址id
    private String caller = "comAddressSet";
    private int saved_num = 0;
    private int save_address_size;
    private int default_sign_range = 300; //默认打卡范围
    private int default_work_range = 500; //默认办公范围
    private int delete_position = 0;      //记录删除的位置position
    private OfficeAddressBean mOfficeAddressBean;
    private int listdada_size;
    private Boolean ok = true;  // 防止按多次删除出现问题，OK为true 才可以进行删除请求

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_SIGN_DDRESSS:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String get_sign_address_result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("get_sign_address_result", get_sign_address_result);
                            doShowSignAddress(get_sign_address_result);
                        }
                    }
                    break;
                case UPDATE_VALUE_REQUEST:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String update_value_result = msg.getData().getString("result");
                            Log.i("update_value_result", update_value_result);
                            ToastMessage(getString(R.string.os_range_notice1));
                            doSaveAddress();
                        }
                    }
                    break;
                case SAVE_ADDRESS_REQUEST:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String save_address_result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("save_address_result", save_address_result);
                            if (saved_num == address_names.size() - 1) {// 有待优化
                                progressDialog.dismiss();
                                Toast.makeText(ct, getString(R.string.os_address_notice1), Toast.LENGTH_SHORT).show();
                                save_btn.setEnabled(true);
                                finish();
                            }
                            saved_num++;
                        }
                    }
                    break;
                case ADDRESS_DELETE_REQUEST:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            int length = address_names.size();
                            if (delete_position < length) {
                                address_names.remove(delete_position);
                                address_location.remove(delete_position);
                                address_longitude.remove(delete_position);
                                address_latitud.remove(delete_position);
                                address_id.remove(delete_position);
                                listdada_size--;
                                myadapter.notifyDataSetChanged();
                                ToastMessage(getString(R.string.delete_succeed_notice1));
                                progressDialog.dismiss();
                            }
                        }
                        ok = true;
                    } else {
                        ToastMessage(getString(R.string.delete_failed_notice1));
                    }
                    break;
                //平台部分
                case GET_PLAT_SIGN_ADDRESS:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            Log.i("plat_sign_office", result);
//                            ToastMessage("请求成功");
                            doShowPlatSignAddress(result);
                        }
                    }
                    break;
                case SAVE_PLAT_OFFICE_ADDRESS:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("result");
                            LogUtil.prinlnLongMsg("save_address_result", result);
                            if (saved_num == address_names.size() - 1) {// 有待优化
                                progressDialog.dismiss();
                                Toast.makeText(ct, getString(R.string.save_ok), Toast.LENGTH_SHORT).show();
                                save_btn.setEnabled(true);
                                finish();
                            }
                            saved_num++;
                        }
                    }
                    break;
                case DELETE_PLAT_SIGN_ADDRESS:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            String result = msg.getData().getString("");
                            int length = address_names.size();
                            if (delete_position < length) {
                                address_names.remove(delete_position);
                                address_location.remove(delete_position);
                                address_longitude.remove(delete_position);
                                address_latitud.remove(delete_position);
                                address_id.remove(delete_position);
                                listdada_size--;
                                myadapter.notifyDataSetChanged();
                                ToastMessage(getString(R.string.delete_succeed_notice1));
                                progressDialog.dismiss();
                            }

                        }
                        ok = true;
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                    }
                    break;
            }
        }
    };


    private Boolean platform;
    private List<PlatSignAddressBean.DataBean> mPlatSignData;
    private TextView title_tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.office_address_settings);
        ViewUtils.inject(this);
        initView();
        initData();
        clickEvent();
    }

    private void initView() {
        progressDialog.show();
        setTitle(getString(R.string.office_addr_setting));
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;

        sign_range = (TextView) findViewById(R.id.of_add_set_sign_range);
        work_range = (TextView) findViewById(R.id.of_add_set_work_range_tv);
        address_lv = (MyListView) findViewById(R.id.of_add_set_address_lv);
        add_address = (RelativeLayout) findViewById(R.id.of_add_set_addaddress_rl);
        save_btn = (Button) findViewById(R.id.of_add_set_save_bt);

        address_names = new ArrayList<>();
        address_location = new ArrayList<>();
        address_latitud = new ArrayList<>();
        address_longitude = new ArrayList<>();
        address_id = new ArrayList<>();

        myadapter = new OfficeAddressAdapter();
        sign_range.setText(default_sign_range + getString(R.string.rice));
        work_range.setText(default_work_range + getString(R.string.rice));

        if (platform) {
            mPlatSignData = new ArrayList<>();
            myadapter.setmPlatSignData(mPlatSignData);
        } else {
            mOfficeAddressBean = new OfficeAddressBean();
            myadapter.setmOfficeAddressBean(mOfficeAddressBean);
        }

        address_lv.setAdapter(myadapter);

    }

    private void initData() {
        //初次进来获取考勤地址设置接口请求
        if (!MyApplication.getInstance().isNetworkActive()) {
            ToastMessage(getString(R.string.networks_out));
            progressDialog.dismiss();
            return;
        } else {
            if (!platform) {
                String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "/mobile/getcomaddressset.action";
                Map<String, Object> param = new HashMap<>();
                LinkedHashMap headers = new LinkedHashMap();
                headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
                ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, GET_SIGN_DDRESSS, null, null, "post");
            } else {
                //  平台办公地址获取
                String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().sign_get_office_url;
                Map<String, Object> param = new HashMap<>();
                param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
                param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
                LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
                ViewUtil.httpSendRequest(this, url, param, handler, headers, GET_PLAT_SIGN_ADDRESS, null, null, "get");
            }
        }

    }

    private void doShowPlatSignAddress(String result) {
        try {
            JSONObject resultJsonObject = new JSONObject(result);
            if (resultJsonObject != null) {
                JSONArray resultArray = resultJsonObject.getJSONArray("data");
                mPlatSignData = JSON.parseArray(resultArray.toString(), PlatSignAddressBean.DataBean.class);
                if (!ListUtils.isEmpty(mPlatSignData)) {
                    listdada_size = mPlatSignData.size();
                    for (int i = 0; i < listdada_size; i++) {
                        if (!StringUtil.isEmpty(mPlatSignData.get(i).getShortname())) {
                            address_names.add(mPlatSignData.get(i).getShortname());
                        } else {
                            address_names.add("");
                        }
                        if (!StringUtil.isEmpty(mPlatSignData.get(i).getCs_workaddr())) {
                            address_location.add(mPlatSignData.get(i).getCs_workaddr());
                        } else {
                            address_location.add("");
                        }

                        if (mPlatSignData.get(i).getCs_latitude() != -1) {
                            address_longitude.add(mPlatSignData.get(i).getCs_latitude() + "");
                        } else {
                            address_longitude.add("");
                        }

                        if (mPlatSignData.get(i).getCs_longitude() != -1) {
                            address_latitud.add(mPlatSignData.get(i).getCs_longitude() + "");
                        } else {
                            address_latitud.add("");
                        }

                        if (mPlatSignData.get(i).getCs_id() != 0) {
                            address_id.add(mPlatSignData.get(i).getCs_id() + "");
                        } else {
                            address_id.add("");
                        }
                    }
                    myadapter.setAddress_names(address_names);
                    myadapter.notifyDataSetChanged();

                    if (mPlatSignData.get(0).getCs_validrange() != -1.0) {
                        sign_range.setText(mPlatSignData.get(0).getCs_validrange() + getString(R.string.rice));
                    } else {
                        sign_range.setText(default_sign_range + getString(R.string.rice));
                    }

                    if (mPlatSignData.get(0).getCs_innerdistance() != -1.0) {
                        work_range.setText(mPlatSignData.get(0).getCs_innerdistance() + getString(R.string.rice));
                    } else {
                        work_range.setText(default_work_range + getString(R.string.rice));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    private void doShowSignAddress(String get_sign_address_result) {
        try {
            JSONObject resultJsonObject = new JSONObject(get_sign_address_result);
            if (resultJsonObject != null) {
                mOfficeAddressBean = JSON.parseObject(resultJsonObject.toString(), OfficeAddressBean.class);
                if (!ListUtils.isEmpty(mOfficeAddressBean.getListdata())) {
                    listdada_size = mOfficeAddressBean.getListdata().size();
                    for (int i = 0; i < listdada_size; i++) {
                        if (!StringUtil.isEmpty(mOfficeAddressBean.getListdata().get(i).getCS_SHORTNAME())) {
                            address_names.add(mOfficeAddressBean.getListdata().get(i).getCS_SHORTNAME());
                        } else {
                            address_names.add("");
                        }

                        if (!StringUtil.isEmpty(mOfficeAddressBean.getListdata().get(i).getCS_WORKADDR())) {
                            address_location.add(mOfficeAddressBean.getListdata().get(i).getCS_WORKADDR());
                        } else {
                            address_location.add("");
                        }

                        if (!StringUtil.isEmpty(mOfficeAddressBean.getListdata().get(i).getCS_LATITUDE())) {
                            address_longitude.add(mOfficeAddressBean.getListdata().get(i).getCS_LATITUDE());
                        } else {
                            address_longitude.add("");
                        }
                        if (!StringUtil.isEmpty(mOfficeAddressBean.getListdata().get(i).getCS_LONGITUDE())) {
                            address_latitud.add(mOfficeAddressBean.getListdata().get(i).getCS_LONGITUDE());
                        } else {
                            address_latitud.add("");
                        }
                        if (mOfficeAddressBean.getListdata().get(i).getCS_ID() != 0) {
                            address_id.add(mOfficeAddressBean.getListdata().get(i).getCS_ID() + "");
                        } else {
                            address_id.add("");
                        }
                    }
                    myadapter.setAddress_names(address_names);
                    myadapter.notifyDataSetChanged();

                    if (!StringUtil.isEmpty(mOfficeAddressBean.getListdata().get(0).getCS_VALIDRANGE())
                            && CommonUtil.getNumByString(mOfficeAddressBean.getListdata().get(0).getCS_VALIDRANGE()) > 0) {
                        sign_range.setText(mOfficeAddressBean.getListdata().get(0).getCS_VALIDRANGE() + getString(R.string.rice));
                    } else {
                        sign_range.setText(default_sign_range + getString(R.string.rice));
                    }

                    if (mOfficeAddressBean.getListdata().get(0).getCS_INNERDISTANCE() > 0) {
                        work_range.setText(mOfficeAddressBean.getListdata().get(0).getCS_INNERDISTANCE() + getString(R.string.rice));
                    } else {
                        work_range.setText(default_work_range + getString(R.string.rice));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressDialog.dismiss();
    }

    private void clickEvent() {
        sign_range.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] distance = getResources().getStringArray(R.array.sign_distance);
                int requestCode = SIGN_RANGE_REQUESTCODE;
                doSelectRange(distance, requestCode);
            }
        });

        work_range.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] distance = getResources().getStringArray(R.array.work_distance);
                int requestCode = WORK_RANGE_REQUESTCODE;
                doSelectRange(distance, requestCode);
            }
        });

        add_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAddress();
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (address_names.size() > 0) {
                    if (MyApplication.getInstance().isNetworkActive()) {
                        doUpdateValue();
                        progressDialog.show();
                        save_btn.setEnabled(false);
                    } else {
                        ToastMessage(getString(R.string.common_notlinknet));
                    }

                } else {
                    ToastMessage(getString(R.string.add_address_first));
                }
            }
        });
    }

    private void doSavePlatAddress() {
        save_address_size = address_names.size();
        for (int i = 0; i < save_address_size; i++) {
            Map<String, Object> formStoreMap = new HashMap<>();
            formStoreMap.put("SHORTNAME", address_names.get(i)); //办公地址简称
            if (!StringUtil.isEmpty(address_id.get(i))) {
                formStoreMap.put("CS_ID", CommonUtil.getNumByString(address_id.get(i))); //办公地址ID
            } else {
                formStoreMap.put("CS_ID", ""); //办公地址ID
            }
            formStoreMap.put("CS_WORKADDR", address_location.get(i)); //办公地址详细
            //TODO 按要求先反过来  update by Bitliker
            formStoreMap.put("CS_LONGITUDE", Double.valueOf(address_latitud.get(i)).doubleValue()); // 经度
            formStoreMap.put("CS_LATITUDE", Double.valueOf(address_longitude.get(i)).doubleValue()); // 纬度
            formStoreMap.put("CS_VALIDRANGE", Double.valueOf(CommonUtil.getNumByString(sign_range.getText().toString())).doubleValue()); //打卡范围
            formStoreMap.put("CS_INNERDISTANCE", Double.valueOf(CommonUtil.getNumByString(work_range.getText().toString())).doubleValue()); //办公范围
            formStoreMap.put("enuu", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu")).longValue());
            formStoreMap.put("emcode", Long.valueOf(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu")).longValue());
            String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().save_plat_office_address_url;
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("formStore", formStore);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
            ViewUtil.httpSendRequest(this, url, params, handler, headers, SAVE_PLAT_OFFICE_ADDRESS, null, null, "post");
        }
    }

    private void doUpdateValue() {
        if (!platform) {
            Map<String, Object> formStoreMap = new HashMap<>();
            formStoreMap.put("CS_VALIDRANGE", CommonUtil.getNumByString(sign_range.getText().toString())); //打卡范围
            formStoreMap.put("CS_INNERDISTANCE", CommonUtil.getNumByString(work_range.getText().toString())); //办公范围
            String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/mobile/updatecomaddressset.action";
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("caller", caller);
            params.put("formStore", formStore);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(this, url, params, handler, headers, UPDATE_VALUE_REQUEST, null, null, "post");
        } else {
            //  平台距离更新,地址保存一个接口实现
            doSavePlatAddress();
        }

    }

    private void doSelectRange(String[] distance, int requestCode) {
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        for (String e : distance) {
            bean = new SelectBean();
            bean.setName(e);
            bean.setClick(false);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", getString(R.string.select_range));
        startActivityForResult(intent, requestCode);
    }

    private void doSaveAddress() {
        save_address_size = address_names.size();
        String emname = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName();
        }

        for (int i = 0; i < save_address_size; i++) {
            Map<String, Object> formStoreMap = new HashMap<>();
            formStoreMap.put("CS_RECORDER", emname);//录入人姓名
            formStoreMap.put("CS_SHORTNAME", address_names.get(i)); //办公地址简称
            formStoreMap.put("CS_WORKADDR", address_location.get(i)); //办公地址详细
            formStoreMap.put("CS_LONGITUDE", address_latitud.get(i)); // 经度  TODO 这里ERp系统一直算反了，所以这里也反着存进去
            formStoreMap.put("CS_LATITUDE", address_longitude.get(i)); // 纬度
            formStoreMap.put("CS_VALIDRANGE", CommonUtil.getNumByString(sign_range.getText().toString())); //打卡范围
            formStoreMap.put("CS_INNERDISTANCE", CommonUtil.getNumByString(work_range.getText().toString())); //办公范围

            String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "/mobile/savecomaddressset.action";
            HashMap<String, Object> params = new HashMap<>();
            String formStore = JSON.toJSONString(formStoreMap);
            params.put("caller", caller);
            params.put("formStore", formStore);
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(this, url, params, handler, headers, SAVE_ADDRESS_REQUEST, null, null, "post");
        }
    }


    private void searchAddress() {
        search_rangge = CommonUtil.getNumByString(sign_range.getText().toString());
        Intent intent = new Intent(ct, SearchLocationActivity.class);
        SearchPoiParam poiParam = new SearchPoiParam();
        poiParam.setType(2);
        poiParam.setTitle(getString(R.string.add_office_address));
        poiParam.setRadius(search_rangge);
        poiParam.setContrastLatLng(UasLocationHelper.getInstance().getUASLocation().getLocation());
        poiParam.setResultCode(ADDRESS_CHANGE);
        poiParam.setDistanceTag("m");
        intent.putExtra("data", poiParam);
        startActivityForResult(intent, ADDRESS_CHANGE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        myadapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (resultCode == 0x20) {
            SelectBean b = data.getParcelableExtra("data");
            if (b == null) return;
            String selected_distance = StringUtil.isEmpty(b.getName()) ? "" : b.getName();
            if (requestCode == SIGN_RANGE_REQUESTCODE) {
                sign_range.setText(selected_distance);
            } else if (requestCode == WORK_RANGE_REQUESTCODE) {
                work_range.setText(selected_distance);
            }
        }
        if (requestCode == ADDRESS_CHANGE && resultCode == ADDRESS_CHANGE) {//地址微调
            PoiInfo poi = data.getParcelableExtra("resultKey");
            if (poi == null) return;
            // 将每次点击地址返回的值add到自定义列表中，
            address_lv.setVisibility(View.VISIBLE);
            address_location.add(poi.address);
            address_latitud.add(String.valueOf(poi.location.latitude));
            address_longitude.add(String.valueOf(poi.location.longitude));
            address_id.add("");
            LogUtil.prinlnLongMsg("getpoi", JSON.toJSONString(poi));

            doEditShortName(poi); //编辑地址简称
        }
    }

    private PopupWindow popupWindow = null;

    private void doEditShortName(final PoiInfo poi) {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.item_select_aim_pop, null);

        // 设置按钮的点击事件
        final EditText company_et = (EditText) contentView.findViewById(R.id.company_et);
        TextView address_tv = (TextView) contentView.findViewById(R.id.address_tv);
        title_tv = (TextView) contentView.findViewById(R.id.title_tv);
        title_tv.setText(getString(R.string.edit_office_address_name));
        company_et.setText(poi.name);
        address_tv.setText(getString(R.string.detail_address) + ":" + poi.address);
        Editable etext = company_et.getText();
        Selection.setSelection(etext, etext.length());
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 300);
        h_screen = DisplayUtil.sp2px(this, 180);
        contentView.findViewById(R.id.goto_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                address_names.add(poi.name);
                myadapter.setAddress_names(address_names);
                myadapter.notifyDataSetChanged();
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String com = company_et.getText().toString();
                if (!StringUtil.isEmpty(com)) {
                    address_names.add(com);
                } else {
                    address_names.add(poi.name);
                }
                myadapter.setAddress_names(address_names);
                myadapter.notifyDataSetChanged();
                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_round_bg));
        // 设置好参数之后再show
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(0.4f);
    }

    private void setbg(float alpha) {
        setBackgroundAlpha(this, alpha);
        if (popupWindow == null) return;
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(OfficeAddressSettingsActivity.this, 1f);
            }
        });
    }

    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    //地点显示列表适配器
    public class OfficeAddressAdapter extends BaseAdapter {
        private OfficeAddressBean mOfficeAddressBean;
        private List<String> address_names;
        private int mPosition;
        private List<PlatSignAddressBean.DataBean> mPlatSignData;

        public List<PlatSignAddressBean.DataBean> getmPlatSignData() {
            return mPlatSignData;
        }

        public void setmPlatSignData(List<PlatSignAddressBean.DataBean> mPlatSignData) {
            this.mPlatSignData = mPlatSignData;
        }

        public OfficeAddressBean getmOfficeAddressBean() {
            return mOfficeAddressBean;
        }

        public void setmOfficeAddressBean(OfficeAddressBean mOfficeAddressBean) {
            this.mOfficeAddressBean = mOfficeAddressBean;
        }

        public List<String> getAddress_names() {
            return address_names;
        }

        public void setAddress_names(List<String> address_names) {
            this.address_names = address_names;
        }

        @Override
        public int getCount() {
            return address_names == null ? 0 : address_names.size();
        }

        @Override
        public Object getItem(int position) {
            int itemposition = 0;
            if (platform) {
                itemposition = mPlatSignData == null ? 0 : mPlatSignData.size();
            } else {
                itemposition = mOfficeAddressBean.getListdata() == null ? 0 : mOfficeAddressBean.getListdata().size();
            }
            return itemposition;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.office_address_item, null);
                viewHolder = new ViewHolder();
                viewHolder.item_address_tv = (TextView) convertView.findViewById(R.id.item_address_tv);
                viewHolder.office_address_delete_im = (ImageView) convertView.findViewById(R.id.item_address_delete_im);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // 带回地址的显示
            if (!ListUtils.isEmpty(address_names) && !TextUtils.isEmpty(address_names.get(position))) {
                viewHolder.item_address_tv.setText(address_names.get(position));
            }
            if (position == address_names.size() - 1) {
                progressDialog.dismiss();
            }
            // 地址的删除操作
            final ViewHolder finalViewHolder = viewHolder;
            viewHolder.office_address_delete_im.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupWindowHelper.showAlart(OfficeAddressSettingsActivity.this,
                            getString(R.string.common_notice), getString(R.string.delete_address_notice1),
                            new PopupWindowHelper.OnSelectListener() {
                                @Override
                                public void select(boolean selectOk) {
                                    if (selectOk) {
                                        if (MyApplication.getInstance().isNetworkActive()) {
                                            mPosition = position;
                                            progressDialog.show();
                                            finalViewHolder.office_address_delete_im.setEnabled(false);
                                            if (mPosition < listdada_size) {
                                                doDeleteAddress(mPosition, ok);
                                                finalViewHolder.office_address_delete_im.setEnabled(true);
                                            } else {
                                                if (mPosition < address_names.size()) {
                                                    address_names.remove(mPosition);
                                                    address_location.remove(mPosition);
                                                    address_longitude.remove(mPosition);
                                                    address_latitud.remove(mPosition);
                                                    address_id.remove(mPosition);
                                                    myadapter.notifyDataSetChanged();
                                                    finalViewHolder.office_address_delete_im.setEnabled(true);
                                                    progressDialog.dismiss();
                                                }

                                            }
                                        } else {
                                            ToastMessage(getString(R.string.common_notlinknet));
                                        }
                                    }
                                }
                            });
                }
            });
            return convertView;
        }

        private void doDeleteAddress(int mPosition, Boolean ok) {
            if (!ok) return;
            delete_position = mPosition;
            if (!platform) {
                String url = CommonUtil.getAppBaseUrl(getApplicationContext()) + "/mobile/deletecomaddressset.action";
                Map<String, Object> param = new HashMap<>();
                param.put("caller", caller);
                param.put("id", CommonUtil.getNumByString(address_id.get(delete_position).toString()));
                LinkedHashMap headers = new LinkedHashMap();
                headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getApplicationContext(), "sessionId"));
                ViewUtil.httpSendRequest(getApplicationContext(), url, param, handler, headers, ADDRESS_DELETE_REQUEST, null, null, "post");
            } else {
                String url = ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().delete_plat_office_address_url;
                Map<String, Object> param = new HashMap<>();
                param.put("id", CommonUtil.getNumByString(address_id.get(delete_position).toString()));
                param.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
                param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
                LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                headers.put("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
                ViewUtil.httpSendRequest(mContext, url, param, handler, headers, DELETE_PLAT_SIGN_ADDRESS, null, null, "post");
            }
            ok = false;
        }

        class ViewHolder {
            TextView item_address_tv;
            ImageView office_address_delete_im;
        }
    }
}

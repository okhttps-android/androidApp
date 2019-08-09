package com.xzjmyk.pm.activity.ui.erp.activity.secretary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.common.data.TextUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.widget.NScrollerGridView;
import com.core.widget.view.Activity.SearchLocationActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.model.SearchPoiParam;
import com.core.widget.view.model.SelectAimModel;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.core.widget.view.selectcalendar.bean.DataState;
import com.core.xmpp.model.AddAttentionResult;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.booking.model.BookingModel;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appcontact.ui.activity.ContactsActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @desc:小秘书新增界面
 * @author：Arison on 2017/6/22
 */
public class BookingAddActivity extends SupportToolBarActivity implements View.OnClickListener {

    private TextView tvBookObject;
    private RelativeLayout companyAddRl;
    private RelativeLayout rlObject;
    private TextView tvBookTimes;
    private RelativeLayout remarkRl;
    private RelativeLayout topic_rl;
    private EditText tv_book_topic;
    private TextView tvBookAddress;
    private double latitude;
    private double longitude;
    private String imId;
    private String startTime;
    private String endTime;
    private EditText et_book_content;
    BookingModel model;
    private Button bt_commit;
    @ViewInject(R.id.gv_topic)
    private NScrollerGridView gv_topic;
    List<DataState> dataStates;
    GridDataAdapter adapter;
    private String[] mTypes;
    private String phone;
    private String mWhichPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_add);
        setTitle(getString(R.string.booking_add));
        tvBookObject = findViewById(R.id.tv_book_object);
        companyAddRl = findViewById(R.id.company_add_rl);
        tvBookTimes = findViewById(R.id.tv_book_times);
        remarkRl = findViewById(R.id.remark_rl);
        rlObject = findViewById(R.id.rl_object);

        topic_rl = findViewById(R.id.topic_rl);
        tvBookAddress = findViewById(R.id.tv_book_address);
        et_book_content = findViewById(R.id.et_book_content);

        tv_book_topic = findViewById(R.id.tv_book_topic);
        gv_topic = findViewById(R.id.gv_topic);
        mTypes = getResources().getStringArray(R.array.booking_topics);
        tv_book_topic.setText(mTypes[0]);
        bt_commit = findViewById(R.id.submit_btn);
        findViewById(R.id.submit_btn).setOnClickListener(this);
        rlObject.setOnClickListener(this);
        companyAddRl.setOnClickListener(this);
        remarkRl.setOnClickListener(this);
        topic_rl.setOnClickListener(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            model = getIntent().getExtras().getParcelable("model");
            mWhichPage = getIntent().getStringExtra("whichPage");
            if (model != null) {
                tvBookObject.setText(model.getAb_bman());
                tvBookTimes.setText(model.getAb_starttime().substring(0, 10) + " " + model.getAb_starttime().substring(11, 16) + "-"
                        + model.getAb_endtime().substring(11, 16));
                tvBookAddress.setText(model.getAb_address());
                et_book_content.setText(model.getAb_content());

                longitude = Double.valueOf(model.getAb_longitude());
                latitude = Double.valueOf(model.getAb_latitude());
                imId = model.getAb_bmanid();

                startTime = model.getAb_starttime();
                endTime = model.getAb_endtime();

                ((Button) findViewById(R.id.submit_btn)).setText(getString(R.string.booking_change));
            }
        }

        dataStates = new ArrayList<>();
        for (String str : mTypes) {
            DataState dataState = new DataState();
            dataState.setValue(str);
            dataStates.add(dataState);
        }

        adapter = new GridDataAdapter(this, dataStates);
        gv_topic.setAdapter(adapter);

        gv_topic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridDataAdapter.ViewModle modle = (GridDataAdapter.ViewModle) view.getTag();
                tv_book_topic.setText(modle.tv_text.getText().toString());
                tv_book_topic.setSelection(modle.tv_text.getText().toString().length());

            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.topic_rl:
                selectType();
                break;
            case R.id.submit_btn:
                bt_commit.setEnabled(false);
                if (((Button) view).getText().equals(getString(R.string.app_button_commit))) {
                    getBookingTime();
                } else {
                    updateBooking();
                }
                break;
            case R.id.company_add_rl://时段
                if (!StringUtil.isEmpty(tvBookObject.getText().toString())) {
                    startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                                    .putExtra("startDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                                    .putExtra("endDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                                    .putExtra("hasMenu", false)
                                    .putExtra("imId", imId)
                                    .putExtra("type", 1)
                            , 0x24);
                } else {
                    ToastMessage("请先选择预约对象！");
                }
                break;
            case R.id.remark_rl://地址
                Intent intent = new Intent(ct, SearchLocationActivity.class);
                SearchPoiParam poiParam = new SearchPoiParam();
                poiParam.setType(2);
                poiParam.setTitle("地图搜索");
                poiParam.setRadius(500);
                //poiParam.setContrastLatLng(new LatLng(companyLocation.getLocation().longitude, companyLocation.getLocation().latitude));
                poiParam.setResultCode(0x23);
                poiParam.setDistanceTag(MyApplication.getInstance().getResources().getString(R.string.rice));
                intent.putExtra("data", poiParam);
                startActivityForResult(intent, 0x23);

                break;
            case R.id.rl_object://对象
                if (model != null) {
                    ToastMessage("预约对象不能更改！");
                } else {
                    intent = new Intent(this, ContactsActivity.class);
//                    SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
//                            .setTitle(getString(R.string.booking_object_name))
//                            .setSingleAble(true)
//                            .setSelectCode(null);
//                    intent.putExtra(OAConfig.MODEL_DATA, bean);
//                    intent.putExtra("isMenuShuffle",true);
                    intent.putExtra("type", 1);
                    intent.putExtra("title", getString(R.string.booking_object_name));
                    startActivityForResult(intent, 0x01);
                }
                break;
        }
    }

    private PopupWindow popupWindow;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        switch (requestCode) {
            case 0x23://地址
                PoiInfo poi = data.getParcelableExtra("resultKey");
                if (poi == null) return;
                tvBookAddress.setText(poi.address + poi.name);
                latitude = poi.location.latitude;
                longitude = poi.location.longitude;
                if (popupWindow != null) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                SelectAimModel chcheAimModel = new SelectAimModel();
                chcheAimModel.setLatLng(poi.location);
                chcheAimModel.setName(poi.address);
                chcheAimModel.setAddress(poi.name);
                chcheAimModel.setType(3);
                popupWindow = PopupWindowHelper.create(this, getString(com.modular.booking.R.string.perfect_company_name), chcheAimModel, new PopupWindowHelper.OnClickListener() {
                    @Override
                    public void result(SelectAimModel model) {
                        tvBookAddress.setText(model.getName());
                    }
                }, new PopupWindowHelper.OnFindLikerListener() {
                    @Override
                    public void click(String licker) {
                    }
                });

                break;
            case 0x24://时间
                startTime = data.getStringExtra("startDate");
                endTime = data.getStringExtra("endDate");
                String displayDate = startTime.substring(11, 16) + "-" + endTime.substring(11, 16);
                tvBookTimes.setText(displayDate);
                break;
            case 0x01://对象
                ContactsModel model = data.getParcelableExtra("data");
                imId = model.getImid();
                phone = model.getPhone();
                tvBookObject.setText(model.getName());
                break;
            case 0x11:
                SelectBean b = data.getParcelableExtra("data");
                if (b != null) {
                    tv_book_topic.setText(b.getName());
                }
                break;
        }


    }

    public void getBookingTime() {
        if (!CommonUtil.isNetWorkConnected(ct)) {
            ToastMessage(getString(R.string.networks_out));
            bt_commit.setEnabled(true);
            return;
        }
        if (StringUtil.isEmpty(tvBookObject.getText().toString())) {
            ToastMessage("请选择预约对象！");
            bt_commit.setEnabled(true);
            return;
        }
        if (StringUtil.isEmpty(tvBookTimes.getText().toString())) {
            ToastMessage("请选择预约时间段！");
            bt_commit.setEnabled(true);
            return;
        }
        if (StringUtil.isEmpty(tvBookAddress.getText().toString())) {
            ToastMessage("请选择预约地址！");
            bt_commit.setEnabled(true);
            return;
        }
        if (isEmoji(et_book_content.getText().toString()) || isEmoji(tvBookObject.getText().toString())) {
            ToastMessage("不支持表情符号的输入！");
            bt_commit.setEnabled(true);
            return;
        }

        if (StringUtil.isEmpty(tv_book_topic.getText().toString())) {
            ToastMessage("请选择预约地址！");
            bt_commit.setEnabled(true);
            return;
        }

        if (StringUtil.isEmpty(com.core.utils.CommonUtil.getName())) {
            ToastMessage("当前登录人姓名为空，不能新增预约");
            return;
        }
//        showLoading();
        String content = "";
        content = et_book_content.getText().toString();
        content = content.replace("'", "''");
        String json = "{" +
                "\"ab_bman\":\"" + tvBookObject.getText().toString() + "\"," +
                "\"ab_bmanid\":\"" + imId + "\"," +
                "\"ab_btelephone\":\"" + phone + "\"," +
                "\"ab_starttime\":\"" + startTime + "\"," +
                "\"ab_endtime\":\"" + endTime + "\"," +
                "\"ab_recordid\":\"" + MyApplication.getInstance().mLoginUser.getUserId() + "\"," +
                "\"ab_recordman\":\"" + com.core.utils.CommonUtil.getName() + "\"," +
                "\"ab_content\":\"" + content + "\"," +
                "\"ab_type\":\"" + tv_book_topic.getText().toString() + "\"," +
                "\"ab_confirmstatus\":\"待确认\"," +
                "\"ab_address\":\"" + tvBookAddress.getText().toString() + "\"," +
                "\"ab_longitude\":\"" + longitude + "\",\n" +
                "\"ab_latitude\":\"" + latitude + "\"" +
                "}\n";
        String url = Constants.IM_BASE_URL() + "user/appSaveBooking";
        Map<String, Object> params = new HashMap<>();
        params.put("token", MyApplication.getInstance().mAccessToken);
        params.put("map", json);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x01, null, null, "post");
    }

    public boolean isEmoji(String string) {
        @SuppressLint("WrongConstant")
        Pattern p = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]",
                Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(string);
        return m.find();
    }

    public void updateBooking() {
        if (!CommonUtil.isNetWorkConnected(ct)) {
            ToastMessage(getString(R.string.networks_out));
            bt_commit.setEnabled(true);
            return;
        }
        if (StringUtil.isEmpty(tvBookObject.getText().toString())) {
            ToastMessage("请选择预约对象！");
            bt_commit.setEnabled(true);
            return;
        }
        if (StringUtil.isEmpty(tvBookTimes.getText().toString())) {
            ToastMessage("请选择预约时间段！");
            bt_commit.setEnabled(true);
            return;
        }
        if (StringUtil.isEmpty(tvBookAddress.getText().toString())) {
            ToastMessage("请选择预约地址！");
            bt_commit.setEnabled(true);
            return;
        }
        if (isEmoji(et_book_content.getText().toString()) || isEmoji(tvBookObject.getText().toString())) {
            ToastMessage("不支持表情符号的输入！");
            bt_commit.setEnabled(true);
            return;
        }
        if (StringUtil.isEmpty(tv_book_topic.getText().toString())) {
            ToastMessage("请选择预约地址！");
            bt_commit.setEnabled(true);
            return;
        }
//        showLoading();
        String content = "";
        content = et_book_content.getText().toString();
        content = content.replace("'", "''");
        String json = "{" +
                "\"ab_bman\":\"" + tvBookObject.getText().toString() + "\"," +
                "\"ab_bmanid\":\"" + imId + "\"," +

                "\"ab_starttime\":\"" + startTime + "\"," +
                "\"ab_endtime\":\"" + endTime + "\"," +
                "\"ab_recordid\":\"" + model.getAb_recordid() + "\"," +
                "\"ab_recordman\":\"" + model.getAb_recordman() + "\"," +
                "\"ab_content\":\"" + content + "\"," +
                "\"ab_type\":\"" + tv_book_topic.getText().toString() + "\"," +
                "\"ab_confirmstatus\":\"未确认\"," +

                "\"ab_address\":\"" + tvBookAddress.getText().toString() + "\"," +
                "\"ab_longitude\":\"" + longitude + "\",\n" +
                "\"ab_latitude\":\"" + latitude + "\"" +
                "}\n";
        String url = Constants.IM_BASE_URL() + "user/appUpdateBooking";
        Map<String, Object> params = new HashMap<>();
        params.put("token", MyApplication.getInstance().mAccessToken);
        params.put("map", json);
        params.put("id", model.getAb_id());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x02, null, null, "post");
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            dimssLoading();
            switch (msg.what) {
                case 0x01:
                    try {
                        String resutl = JSON.parseObject(msg.getData().getString("result")).getString("result");
                        if (resutl.equals("true")) {
                            ToastMessage("预约成功!");
                            if (!StringUtil.isEmpty(imId)) {
                                invite(phone, "6e554e51-08de-443c-9b6c-f0d6d0d07bb4");
                                invite(phone, "fd4ac30e-b176-4410-ac0e-e39c8b71dfe0");
                            } else {
                                invite(phone, "8636ba7f-a1b4-4062-8571-782035101167");
                            }
                            if (!TextUtils.isEmpty(mWhichPage) && mWhichPage.equals("ScheduleActivity")) {
                                setResult(0x11);
                                finish();
                            } else {
                                startActivity(new Intent(BookingAddActivity.this, BookingListActivity.class)
                                        .putExtra("curDate", startTime)
                                        .putExtra("whichPage", mWhichPage));
                            }

                        } else {
                            ToastMessage("预约失败！");
                            bt_commit.setEnabled(true);
                        }

//                        dimssLoading();
                    } catch (Exception e) {
                        ToastMessage("预约失败！");
                        bt_commit.setEnabled(true);
                    }
                case 0x02:
                    try {
                        if (JSON.parseObject(msg.getData().getString("result")).getString("result").equals("true")) {
                            ToastMessage(getString(R.string.make_adeal_success));

                            startActivity(new Intent(BookingAddActivity.this, BookingListActivity.class)
                                    .putExtra("curDate", model.getAb_starttime())
                            );
                        } else {
                            ToastMessage(getString(R.string.make_adeal_failed));
                        }
                    } catch (Exception e) {
                        ToastMessage(getString(R.string.make_adeal_failed));
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    ToastMessage(getString(R.string.make_adeal_failed));
                    bt_commit.setEnabled(true);
                    break;
                case Constants.APP_NOTNETWORK:
                    ToastMessage(msg.getData().getString("result"));
                    bt_commit.setEnabled(true);
                    break;
            }
        }


    };

    private void selectType() {
        ArrayList<SelectBean> formBeaan = new ArrayList<>();
        SelectBean selectBean;
        for (int i = 0; i < mTypes.length; i++) {
            selectBean = new SelectBean();
            selectBean.setName(mTypes[i]);
            formBeaan.add(selectBean);
        }
        Intent intent = new Intent();
        intent.setClass(this, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putExtra("title", "主题");
        intent.putParcelableArrayListExtra("data", formBeaan);
        startActivityForResult(intent, 0x11);
    }


    public class GridDataAdapter extends BaseAdapter {

        private Context ct;
        private List<DataState> mData = new ArrayList<>();
        private LayoutInflater inflater;
        private int selected = -1;

        GridDataAdapter(Context ct, List<DataState> data) {
            this.ct = ct;
            this.mData = data;
            this.inflater = LayoutInflater.from(ct);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public int getSelected() {
            return selected;
        }

        public void setSelected(int selected) {
            this.selected = selected;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridDataAdapter.ViewModle modle = null;

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_simple_text, parent, false);
                modle = new GridDataAdapter.ViewModle();
                modle.tv_text = (TextView) convertView.findViewById(R.id.tv_text);
                convertView.setTag(modle);
            } else {
                modle = (GridDataAdapter.ViewModle) convertView.getTag();
            }
            modle.tv_text.setText(mData.get(position).getValue());
            if (mData.get(position).isClicked()) {
                modle.clicked = true;
                modle.tv_text.setTextColor(mContext.getResources().getColor(R.color.black));
                modle.tv_text.setBackgroundResource(R.drawable.bg_select_blue);
                if (selected == position) {
                    modle.tv_text.setSelected(true);
                    modle.tv_text.setTextColor(mContext.getResources().getColor(R.color.white));
                } else {
                    modle.tv_text.setSelected(false);
                }
            } else {
                modle.clicked = false;
                modle.tv_text.setTextColor(mContext.getResources().getColor(R.color.light_gray));
                modle.tv_text.setBackgroundResource(R.drawable.bg_select_red);
                modle.tv_text.setSelected(false);
            }
            return convertView;
        }

        class ViewModle {
            TextView tv_text;
            boolean clicked;
        }
    }

    private void invite(String user, final String modeid) {
        final String name = CommonUtil.getName();
        final String phone = user.trim().replaceAll(" ", "");
        if (!StringUtil.isMobileNumber(phone)) {
            // showToast(, R.color.load_submit);
            showToast("选择人员电话号码为空或是格式不正确");
            return;
        }
        StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
                Request.Method.POST, "http://message.ubtob.com/sms/send", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                // dimssLoading();
            }
        }, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
            @Override
            public void onResponse(ObjectResult<AddAttentionResult> result) {
                showToast("短信发送成功");

            }
        }, AddAttentionResult.class, null) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                String param = "{\"receiver\":\"" + phone + "\",\"params\":[\"" + name + "\"],\"templateId\":\"" + modeid + "\"}";
                LogUtil.i("param=" + param);
                return param.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        MyApplication.getInstance().getFastVolley().addDefaultRequest("Volley", request);
    }
}
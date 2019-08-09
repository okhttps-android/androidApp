package com.modular.booking.activity.business;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.SelectEmUser;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonUtil;
import com.core.widget.NScrollerGridView;
import com.core.widget.view.Activity.SearchLocationActivity;
import com.core.widget.view.model.SearchPoiParam;
import com.core.widget.view.model.SelectAimModel;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.core.widget.view.selectcalendar.bean.DataState;
import com.core.xmpp.model.AddAttentionResult;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.booking.R;
import com.modular.booking.model.BookingModel;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appcontact.ui.activity.ContactsActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc:商务预约新增界面
 * @author：Arison on 2017/9/7
 */
public class BBookingAddActivity extends SupportToolBarActivity implements View.OnClickListener {
    private TextView tvBookObject;
    private RelativeLayout companyAddRl;
    private RelativeLayout rlObject;
    private TextView tvBookTimes;
    private RelativeLayout remarkRl;
    private RelativeLayout topic_rl;
    private RelativeLayout rl_company;
    private EditText tv_book_topic;
    private TextView tvBookAddress;
    private TextView tv_book_company;
    private double latitude;
    private double longitude;
    private String imId;
    private String startTime;
    private String endTime;

    private EditText et_book_content;
    BookingModel model;
    private Button bt_commit;
    private NScrollerGridView gv_topic;
    List<DataState> dataStates;
    GridDataAdapter adapter;
    private String phone;
    private String[] mTypes;
    private String mWhichPage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbooking_add);
        initView();
        initEvent();
    }

    private void initEvent() {
        findViewById(R.id.submit_btn).setOnClickListener(this);
        rlObject.setOnClickListener(this);
        companyAddRl.setOnClickListener(this);
        remarkRl.setOnClickListener(this);
        rl_company.setOnClickListener(this);
        topic_rl.setOnClickListener(this);

        gv_topic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GridDataAdapter.ViewModle modle = (GridDataAdapter.ViewModle) view.getTag();
                tv_book_topic.setText(modle.tv_text.getText().toString());
                tv_book_topic.setSelection(modle.tv_text.getText().toString().length());

            }
        });
    }

    public void initView() {
        setTitle(getString(R.string.booking_business));

        tvBookObject = (TextView) findViewById(R.id.tv_book_object);
        companyAddRl = (RelativeLayout) findViewById(R.id.company_add_rl);
        tvBookTimes = (TextView) findViewById(R.id.tv_book_times);
        remarkRl = (RelativeLayout) findViewById(R.id.remark_rl);
        rlObject = (RelativeLayout) findViewById(R.id.rl_object);
        rl_company = (RelativeLayout) findViewById(R.id.rl_company);
        topic_rl = (RelativeLayout) findViewById(R.id.topic_rl);

        tv_book_company = (TextView) findViewById(R.id.tv_book_company);
        tvBookAddress = (TextView) findViewById(R.id.tv_book_address);
        et_book_content = (EditText) findViewById(R.id.et_book_content);

        tv_book_topic = (EditText) findViewById(R.id.tv_book_topic);
        gv_topic = (NScrollerGridView) findViewById(R.id.gv_topic);
        bt_commit = (Button) findViewById(R.id.submit_btn);
        mTypes = getResources().getStringArray(R.array.booking_topics);
        tv_book_topic.setText(mTypes[0]);

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
                tv_book_topic.setText(model.getAb_type());
                tv_book_company.setText(getIntent().getExtras().getString("companys"));
                imId = getIntent().getExtras().getString("bmanid");
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


    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.submit_btn) {
            if ("提交".equals(((Button) v).getText().toString())) {
                save();
            } else if ("变更".equals(((Button) v).getText().toString())) {
                update();
            }
        } else if (i == R.id.topic_rl) {

        } else if (i == R.id.company_add_rl) {
            //时间
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
        } else if (i == R.id.remark_rl) {
            //地址
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
        } else if (i == R.id.rl_object) {
            if (model != null) {
                ToastMessage("预约对象不能更改！");
            } else {
                //"com.modular.contact.ContactsActivity"
                Intent intent = new Intent(this, ContactsActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("title", getString(R.string.booking_object_name));
                intent.putExtra("isSingleSelect", false);
                intent.putParcelableArrayListExtra("models", models);//状态回传
                startActivityForResult(intent, 0x01);
            }
        } else if (i == R.id.rl_company) {
            Intent intent = new Intent(this, BBCompanyListActivity.class);
            startActivityForResult(intent, 0x02);
        }
    }

    ArrayList<ContactsModel> models;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (data == null) return;
        switch (requestCode) {
            case 0x01://对象
                models = data.getParcelableArrayListExtra("data");
                //拿到多个预约对象，需要下拉他们的企业信息
                StringBuilder names = new StringBuilder("");
                StringBuilder imids = new StringBuilder("");
                StringBuilder phones = new StringBuilder("");
                for (int i = 0; i < models.size(); i++) {
                    if (i == models.size() - 1) {
                        names.append(models.get(i).getName());
                        imids.append(models.get(i).getImid());
                        phones.append(models.get(i).getPhone());
                    } else {
                        names.append(models.get(i).getName() + ",");
                        imids.append(models.get(i).getImid() + ",");
                        phones.append(models.get(i).getPhone() + ",");
                    }
                }
                //根据手机号进行公司查询
                comboxGetCompany(phones.toString());
                if (StringUtil.isEmpty(tvBookObject.getText().toString())) {
                    tvBookObject.setText(names.toString());
                } else {
                    tvBookObject.setText(names.toString());
                    //  tvBookObject.setText(tvBookObject.getText().toString()+","+names.toString());
                }
                break;
            case 0x02://企业
                //逻辑说明
                //预约对象有人，累加（重复的暂时不考虑）
                //企业有已选择，累加（重复的暂时不考虑）
                ArrayList<SelectEmUser> selectEmUsers = data.getParcelableArrayListExtra("data");
                if (!ListUtils.isEmpty(selectEmUsers)) {
                    StringBuilder nameBuilder = new StringBuilder("");
                    StringBuilder companyBuilder = new StringBuilder("");
                    StringBuilder imidBuilder = new StringBuilder("");
                    StringBuilder phoneBuilder = new StringBuilder("");
                    for (int i = 0; i < selectEmUsers.size(); i++) {
                        if (i == selectEmUsers.size() - 1) {
                            companyBuilder.append(selectEmUsers.get(i).getEmCode());
                            imidBuilder.append(selectEmUsers.get(i).getImId());
                            phoneBuilder.append(selectEmUsers.get(i).getDepart());
                            nameBuilder.append(selectEmUsers.get(i).getEmName());
                        } else {
                            companyBuilder.append(selectEmUsers.get(i).getEmCode() + ",");
                            imidBuilder.append(selectEmUsers.get(i).getImId() + ",");
                            phoneBuilder.append(selectEmUsers.get(i).getDepart() + ",");
                            nameBuilder.append(selectEmUsers.get(i).getEmName() + ",");
                        }

                    }

                    String bookObject = tvBookObject.getText().toString();
                    if (StringUtil.isEmpty(bookObject)) {
                        bookObject = bookObject + nameBuilder;
                        imId = imId + imidBuilder.toString();
                        phone = phone + phoneBuilder.toString();
                        tv_book_company.setText(companyBuilder.toString());
                    } else {
                        bookObject = bookObject + "," + nameBuilder;
                        imId = imId + "," + imidBuilder.toString();
                        phone = phone + "," + phoneBuilder.toString();
                        tv_book_company.setText(tv_book_company.getText().toString() + "," + companyBuilder.toString());
                    }
                    tvBookObject.setText(bookObject);
                }
                break;
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
                popupWindow = PopupWindowHelper.create(this, getString(R.string.perfect_company_name), chcheAimModel, new PopupWindowHelper.OnClickListener() {
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
            case 0x11://主题

                break;
        }
    }

    private PopupWindow popupWindow;

    //提交
    public void save() {
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

        if (StringUtil.isEmpty(tv_book_topic.getText().toString())) {
            ToastMessage("请选择预约主题！");
            bt_commit.setEnabled(true);
            return;
        }
        //前面四个值是连带出来的
        String map = "{" +
                "\"ab_bman\":\"" + tvBookObject.getText().toString() + "\"," +
                "\"ab_bmanid\":\"" + imId + "\"," +
                "\"ab_bcompany\":\"" + tv_book_company.getText().toString() + "\"," +
                "\"ab_btelephone\":\"" + phone + "\"," +
                "\"ab_starttime\":\"" + startTime + "\"," +
                "\"ab_endtime\":\"" + endTime + "\"," +
                "\"ab_recordid\":\"" + MyApplication.getInstance().mLoginUser.getUserId() + "\"," +
                "\"ab_recordman\":\"" + CommonUtil.getName() + "\"," +
//                "\"ab_content\":\"" + content + "\"," +
                "\"ab_type\":\"" + tv_book_topic.getText().toString() + "\"," +
                "\"ab_confirmstatus\":\"未确认\"," +
                "\"ab_address\":\"" + tvBookAddress.getText().toString() + "\"," +
                "\"ab_longitude\":\"" + longitude + "\",\n" +
                "\"ab_latitude\":\"" + latitude + "\"" +
                "}\n";
        // showLoading();
        LogUtil.d("HttpLogs", "map:" + map);
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appSaveBusiness")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .add("map", map)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                //dimssLoading();
                LogUtil.d("", "" + o.toString());
                if (JSONUtil.validate(o.toString())) {
                    String success = JSON.parseObject(o.toString()).getString("result");
                    if ("true".equals(success)) {
                        ToastMessage("预约计划生成！");
                        try {
                            if (!StringUtil.isEmpty(phone)) {
                                for (int i = 0; i < phone.split(",").length; i++) {
                                    LogUtil.d("HttpLogs", "phone:" + phone.split(",")[i]);
                                    LogUtil.d("HttpLogs", "imid:" + imId.split(",")[i]);
                                    if ("0".equals(imId.split(",")[i]) || StringUtil.isEmpty(imId.split(",")[i])) {
                                        invite(phone.split(",")[i], "6e554e51-08de-443c-9b6c-f0d6d0d07bb4");
                                        invite(phone.split(",")[i], "fd4ac30e-b176-4410-ac0e-e39c8b71dfe0");
                                    } else {
                                        invite(phone.split(",")[i], "8636ba7f-a1b4-4062-8571-782035101167");
                                    }

                                }
                            }
                        } catch (Exception e) {

                        }
                        Intent intent = new Intent("com.modular.booking.BookingListActivity");
                        intent.putExtra("curDate", startTime);
                        intent.putExtra("whichPage", mWhichPage);
                        startActivity(intent);
                    }
                }
            }
        }));


    }

    //变更
    public void update() {
        String map = "{" +
                "\"ab_bman\":\"" + tvBookObject.getText().toString() + "\"," +
                "\"ab_bmanid\":\"" + imId + "\"," +
                "\"ab_bcompany\":\"" + tv_book_company.getText().toString() + "\"," +
//                "\"ab_btelephone\":\"" + phone + "\"," +
                "\"ab_starttime\":\"" + startTime + "\"," +
                "\"ab_endtime\":\"" + endTime + "\"," +
                "\"ab_recordid\":\"" + MyApplication.getInstance().mLoginUser.getUserId() + "\"," +
                "\"ab_recordman\":\"" + CommonUtil.getName() + "\"," +
//                "\"ab_content\":\"" + content + "\"," +
                "\"ab_type\":\"" + tv_book_topic.getText().toString() + "\"," +
                "\"ab_confirmstatus\":\"未确认\"," +
                "\"ab_address\":\"" + tvBookAddress.getText().toString() + "\"," +
                "\"ab_longitude\":\"" + longitude + "\",\n" +
                "\"ab_latitude\":\"" + latitude + "\"" +
                "}\n";
        LogUtil.d("HttpLogs", "map:" + map);
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("/user/appUpdateBusiness")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .add("map", map)
                .add("token", MyApplication.getInstance())
                .add("id", model.getAb_id())
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (JSONUtil.validate(o.toString())) {
                    String success = JSON.parseObject(o.toString()).getString("result");
                    if ("true".equals(success)) {
                        ToastMessage("预约计划变更成功！");
                        Intent intent = new Intent("com.modular.booking.BookingListActivity");

                        intent.putExtra("curDate", startTime);
                        startActivity(intent);
                    }
                }
            }
        }));
    }


    public void comboxGetCompany(String phones) {
        HttpClient httpClient = new HttpClient.Builder(Constants.ACCOUNT_CENTER_HOST).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .isDebug(true)
                .url("api/userspace/userInfos")
                .add("tel", phones)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    LogUtil.d("ResponseText", "o:" + o.toString());
                    if (JSONUtil.validate(o.toString())) {
                        JSONArray datas = JSON.parseObject(o.toString()).getJSONArray("listdata");
                        if (!ListUtils.isEmpty(datas)) {
                            StringBuilder companyBuilder = new StringBuilder("");
                            StringBuilder imidBuilder = new StringBuilder("");
                            StringBuilder phoneBuilder = new StringBuilder("");
                            StringBuilder nameBuilder = new StringBuilder("");
                            for (int i = 0; i < datas.size(); i++) {
                                JSONObject object = datas.getJSONObject(i);
                                if (i == datas.size() - 1) {
                                    companyBuilder.append(object.getString("company"));
                                    imidBuilder.append(StringUtil.isEmpty(object.getString("imid")) == true ? "0" : object.getString("imid"));
                                    phoneBuilder.append(object.getString("usertel"));
                                    nameBuilder.append(object.getString("username"));
                                } else {
                                    companyBuilder.append(object.getString("company") + ",");
                                    if (StringUtil.isEmpty(object.getString("imid"))) {
                                        imidBuilder.append("0" + ",");
                                    } else {
                                        imidBuilder.append(object.getString("imid") + ",");
                                    }
                                    phoneBuilder.append(object.getString("usertel") + ",");
                                    nameBuilder.append(object.getString("username") + ",");
                                }
                            }

//                            if (StringUtil.isEmpty(tv_book_company.getText().toString())){
                            tv_book_company.setText(companyBuilder.toString());
                            imId = imidBuilder.toString();
                            phone = phoneBuilder.toString();
//                            }else{
//                                tv_book_company.setText(tv_book_company.getText().toString()+","+companyBuilder.toString());
//                                imId =imId+","+ imidBuilder.toString();
//                                phone = phone+","+phoneBuilder.toString();
//                                imId= imId.replace("null","");
//                                phone = phone.replace("null","");
//                            }


                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
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
            showToast("选择人员电话号码为空或是格式不正确");
            return;
        }
        StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
                Request.Method.POST, "http://message.ubtob.com/sms/send", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                //  dimssLoading();
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

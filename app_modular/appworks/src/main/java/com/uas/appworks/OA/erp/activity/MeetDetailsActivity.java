package com.uas.appworks.OA.erp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.search.core.PoiInfo;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.widget.view.Activity.SearchLocationActivity;
import com.core.widget.view.model.SearchPoiParam;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.applocation.UasLocationHelper;
import com.uas.appworks.OA.erp.model.MeetEntity;
import com.uas.appworks.OA.erp.model.MeetingDocBean;
import com.uas.appworks.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MeetDetailsActivity extends BaseActivity implements View.OnClickListener {
    private TextView name_tv;
    private TextView addr_tv;
    private TextView sub_tv;
    private TextView tag_tv;
    private TextView user_tv;
    private TextView oa_meeting_start;
    private TextView oa_meeting_end_date;
    private TextView oa_meeting_end;
    private Button click_btn;
    private TextView oa_meeting_start_date;
    private TextView end_meet_tv;
    private TextView status_tv;
    private TextView location_tv;
    private TextView num_tv;
    private final int whatLoad = 0x11, SIGNNIN = 0x12, ENDMEET = 0x13;
    private int ma_id;
    private String ma_code;
    private String emname;
    private boolean isPlay = true;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            String message = (String) msg.getData().get("result");
            switch (msg.what) {
                case whatLoad:
//                    updataUI();
                    break;
                case ENDMEET:
                    ToastUtil.showToast(ct, R.string.meet_success_end);
                    status_tv.setText(R.string.ended);
                    setSigninAble(false);
                    end_meet_tv.setVisibility(View.GONE);
                    break;
                case SIGNNIN:
                    ToastUtil.showToast(ct, R.string.meet_signin_success);
                    isPlay = false;
                    click_btn.setText(R.string.signined);
                    setSigninAble(false);
                    loadNumData();
                    break;
                case 0x14:
                    JSONObject numlist = JSON.parseObject(message).getJSONObject("participants");
                    String code = CommonUtil.getSharedPreferences(ct, "erp_username");
                    int confirmed = 0, unconfirmed = 0;
                    if (numlist != null && numlist.getJSONArray("confirmed") != null) {//签到人数
                        array = numlist.getJSONArray("confirmed");
                        if (array != null && array.size() > 0) {
                            confirmed = array.size();
                            for (int i = 0; i < confirmed; i++)
                                if (array.getJSONObject(i).containsKey("EM_CODE") && code.equals(array.getJSONObject(i).getString("EM_CODE"))) {
                                    isPlay = false;
                                    click_btn.setText(R.string.signined);
                                    setSigninAble(false);
                                    break;
                                }
                        }
                    }
                    if (numlist != null && numlist.getJSONArray("unconfirmed") != null) {//未签到人数
                        array.addAll(numlist.getJSONArray("unconfirmed"));
                        unconfirmed = numlist.getJSONArray("unconfirmed").size();
                    }
                    if ((confirmed + unconfirmed) > 0) {
                        num_tv.setText(getResources().getString(R.string.signin_number) + ":" + confirmed + "/" + (confirmed + unconfirmed));
                    }
                    break;
            }
        }
    };
    private MeetEntity entity;
    private int item;
    private JSONArray array;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            endActivity();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        endActivity();
    }

    private void endActivity() {
        Intent data = new Intent();
        data.putExtra("item", item);
        data.putExtra("data", true);
        setResult(0x15, data);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet_details);
        emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        entity = getIntent().getParcelableExtra("data");
        item = getIntent().getIntExtra("item", -1);
        if (entity != null) {
            ma_id = entity.getMa_id();
            ma_code = entity.getMa_code();
        }
        loadNumData();
        initView();
    }

    private void setSigninAble(boolean b) {
        if (b) {
            click_btn.setBackgroundResource(R.drawable.selector_confirm_bg);
        } else {
            click_btn.setBackgroundResource(R.drawable.shape_corner_blue_unable_bg);
            click_btn.setClickable(false);
            click_btn.setFocusable(false);
            click_btn.setPadding(20, 20, 20, 20);
        }
    }

    private void showExitDialog() {
        PopupWindowHelper.showAlart(this,
                getString(R.string.meet_manage), getString(R.string.to_end_meet)
                , new PopupWindowHelper.OnSelectListener() {
                    @Override
                    public void select(boolean selectOk) {
                        if (selectOk) {
                            doNetSubmit(ENDMEET);
                        }
                    }
                });
    }

    private void initView() {
        initIDs();
        location_tv.setText(UasLocationHelper.getInstance().getUASLocation().getAddress());
        findViewById(R.id.meet_save).setOnClickListener(this);
        findViewById(R.id.num_tag).setOnClickListener(this);
        findViewById(R.id.end_meet_tv).setOnClickListener(this);
        findViewById(R.id.click_btn).setOnClickListener(this);
        findViewById(R.id.location_tag).setOnClickListener(this);
        findViewById(R.id.add_task_ll).setOnClickListener(this);
        if (entity == null) return;
        //设置会议 地点、介绍、发起人、标签
        addr_tv.setText(entity.getMa_mrname() == null ? getString(R.string.common_noinput) : entity.getMa_mrname());
        sub_tv.setText(entity.getMa_remark() == null ? "" : entity.getMa_remark());
        user_tv.setText(entity.getMa_recorder() == null ? "" : entity.getMa_recorder());
        tag_tv.setText(StringUtil.isEmpty(entity.getMa_tag()) ? getString(R.string.common_noinput) : entity.getMa_tag());
        //设置会议状态
        String str = entity.getMa_stage();
        if (StringUtil.isEmpty(str)) {
            if (TimeUtils.f_str_2_long(entity.getMa_starttime()) > System.currentTimeMillis())
                str = getResources().getString(R.string.not_bigan);
            else
                str = getResources().getString(R.string.doing);
        }

        if (str != null) {
            int statusColor = R.color.meeting_end_status;
            if (getResources().getString(R.string.doing).equals(str)) {
                statusColor = R.color.meeting_start_status;
            } else {
                statusColor = R.color.meeting_end_status;
                if ("已结束".equals(str)) {
                    setSigninAble(false);
                }
            }

            status_tv.setTextColor(getResources().getColor(statusColor));
            status_tv.setText(str);
        } else {
            status_tv.setText("");
        }


        //设置开始和结束时间的显示
        name_tv.setText(entity.getMa_theme() == null ? "" : entity.getMa_theme());
        long startLongTime = TimeUtils.f_str_2_long(entity.getMa_starttime());
        long endLongTime = TimeUtils.f_str_2_long(entity.getMa_endtime());
        oa_meeting_start.setText(DateFormatUtil.long2Str(startLongTime, "MM月dd日"));
        oa_meeting_end.setText(DateFormatUtil.long2Str(endLongTime, "MM月dd日"));
        oa_meeting_start_date.setText(DateFormatUtil.long2Str(startLongTime, "HH:mm"));
        oa_meeting_end_date.setText(DateFormatUtil.long2Str(endLongTime, "HH:mm"));
        if (!StringUtil.isEmpty(emname) && entity.getMa_recorder() != null
                && !emname.equals(entity.getMa_recorder())
                || "已结束".equals(status_tv.getText().toString().trim())) {
            end_meet_tv.setVisibility(View.GONE);
        } else {
            end_meet_tv.setVisibility(View.VISIBLE);
        }
    }

    private void initIDs() {

        name_tv = (TextView) findViewById(R.id.name_tv);
        addr_tv = (TextView) findViewById(R.id.addr_tv);
        sub_tv = (TextView) findViewById(R.id.sub_tv);
        tag_tv = (TextView) findViewById(R.id.tag_tv);
        user_tv = (TextView) findViewById(R.id.user_tv);
        oa_meeting_start = (TextView) findViewById(R.id.oa_meeting_start);
        oa_meeting_end_date = (TextView) findViewById(R.id.oa_meeting_end_date);
        oa_meeting_end = (TextView) findViewById(R.id.oa_meeting_end);
        click_btn = (Button) findViewById(R.id.click_btn);
        oa_meeting_start_date = (TextView) findViewById(R.id.oa_meeting_start_date);
        end_meet_tv = (TextView) findViewById(R.id.end_meet_tv);
        status_tv = (TextView) findViewById(R.id.status_tv);
        location_tv = (TextView) findViewById(R.id.location_tv);
        num_tv = (TextView) findViewById(R.id.num_tv);
    }

    String baseUrl;

    //获取会议详情接口（获取到的数据太少，先不用）
    private void loadDetailsData() {
        progressDialog.show();
        //获取网络数据
        if (baseUrl == null) {
            baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        }
        String url = null;
        url = baseUrl + "mobile/common/getPanel.action";
        String em_code = CommonUtil.getSharedPreferences(ct, "erp_username");
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        final Map<String, Object> param = new HashMap<>();
        String caller = "Meetingroomapply";
        param.put("caller", caller);
        param.put("emcode", em_code);
        param.put("formCondition", "ma_id=" + ma_id);
        param.put("gridCondition", "ma_id=" + ma_id);
        param.put("sessionId", sessionId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, whatLoad, null, null, "post");
    }

    //获取网络数据
    private void loadNumData() {
        progressDialog.show();
        //获取网络数据
        if (baseUrl == null) {
            baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        }
        String url = baseUrl + "mobile/crm/getMeetingDetailParticipants.action";
        String em_code = CommonUtil.getSharedPreferences(ct, "erp_username");
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        final Map<String, Object> param = new HashMap<>();
        String caller = "Meetingroomapply";
        param.put("caller", caller);
        param.put("emcode", em_code);
        param.put("formCondition", "ma_id=" + ma_code);
        param.put("gridCondition", "ma_id=" + ma_code);
        param.put("ma_code", ma_code);
        param.put("sessionId", sessionId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, 0x14, null, null, "post");
    }


    /**
     * 提交接口（结束会议、会议签到、为与会成员加 日程）
     *
     * @param type 接口类型  签到 SIGNNIN、结束会议：ENDMEET
     */
    private void doNetSubmit(int type) {
        progressDialog.show();
        //获取网络数据
        if (baseUrl == null) {
            baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        }
        String urlSub = "";
        switch (type) {
            case SIGNNIN:
                urlSub = "mobile/oa/meeting/meetingSignInMobile.action";//会议签到
                break;
            case ENDMEET:
                urlSub = "mobile/crm/updateMeeting.action";//结束会议
                break;
        }
//        emcode改成em_code ，ma_id改成ma_code
        String url = baseUrl + urlSub;
        String em_code = CommonUtil.getSharedPreferences(ct, "erp_username");
        String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
        final Map<String, Object> param = new HashMap<>();
        String caller = "Meetingroomapply";
        if (type == ENDMEET) {
            param.put("emcode", em_code);
            param.put("ma_id", ma_id);
        } else {
            param.put("caller", caller);
            param.put("em_code", em_code);
            param.put("ma_code", ma_id);
        }
        param.put("sessionId", sessionId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, handler, headers, type, null, null, "post");
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        if (view.getId() == R.id.num_tag) {
            intent = new Intent("com.modular.metting.appworks.OARegisterActivity");
            intent.putExtra("code", ma_code);
            intent.putExtra("id", ma_code);
            startActivity(intent);
        } else if (view.getId() == R.id.meet_save) {
            if (!user_tv.getText().toString().trim().equals(emname)) {
                ToastUtil.showToast(ct, R.string.not_power_edit_tag);
                return;
            }
            StringBuilder name = new StringBuilder();
            StringBuilder emcome = new StringBuilder();
            if (array != null && array.size() > 0) {
                for (int i = 0; i < array.size(); i++) {
                    name.append(";" + array.getJSONObject(i).getString("EM_NAME"));
                    emcome.append(";employee#" + array.getJSONObject(i).getString("EM_ID"));
                }
                name.delete(0, 1);
                emcome.delete(0, 1);
            }
            MeetingDocBean bean = new MeetingDocBean();
            intent = new Intent(ct, SaveMeetActivity.class);
            bean.setMd_recorder(user_tv.getText().toString());
            bean.setMd_recorderdate("");
            bean.setMd_status("在录入");
            bean.setMd_title(name_tv.getText().toString());
            bean.setMd_mtname("");
            bean.setMd_meetingname(name_tv.getText().toString());
            bean.setMd_meetingcode(ma_code);
            bean.setMd_mrcode(entity.getMa_mrcode());
            bean.setMd_mrname(entity.getMa_mrname());
            bean.setMd_starttime(entity == null ? TimeUtils.f_long_2_str(System.currentTimeMillis()) : entity.getMa_starttime());
            bean.setMd_statuscode("ENTERING");
            bean.setMd_endtime(entity == null ? TimeUtils.f_long_2_str(System.currentTimeMillis()) : entity.getMa_endtime());
            bean.setMd_group(name.toString());//数据返回
            bean.setMd_attachs("");
            bean.setMd_contents("");
            bean.setMd_groupid(emcome.toString());//数据返回
            intent.putExtra("data", bean);
            intent.putExtra("code", ma_code);
            startActivity(intent);
        } else if (view.getId() == R.id.click_btn) {
            if (isPlay) {
                if ("已结束".equals(status_tv.getText().toString().trim())) {
                    ToastUtil.showToast(ct, R.string.meet_ended_not_signin);
                } else {
                    doNetSubmit(SIGNNIN);
                }
            } else {
                ToastUtil.showToast(ct, R.string.already_signined);
            }
        } else if (view.getId() == R.id.location_tag) {
            intent = new Intent(ct, SearchLocationActivity.class);
            SearchPoiParam poiParam = new SearchPoiParam();
            poiParam.setType(1);
            poiParam.setTitle(getResources().getString(R.string.unoffice));
            poiParam.setRadius(300);
            poiParam.setContrastLatLng(UasLocationHelper.getInstance().getUASLocation().getLocation());
            poiParam.setResultCode(0x20);
            poiParam.setDistanceTag("m");
            intent.putExtra("data", poiParam);
            startActivityForResult(intent, 0x20);
        } else if (view.getId() == R.id.add_task_ll) {
            intent = new Intent(ct, AddMeetTaskActivity.class);
            intent.putExtra("name", StringUtil.getTextRexHttp(name_tv) + StringUtil.getMessage(ma_code));
            startActivity(intent);
        } else if (view.getId() == R.id.end_meet_tv) {
            if (!user_tv.getText().toString().trim().equals(emname)) {
                ToastUtil.showToast(ct, R.string.not_power_end_meet);
                return;
            }
            showExitDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        if (requestCode == 0x20 && resultCode == 0x20) {
            PoiInfo poi = data.getParcelableExtra("resultKey");
            if (poi == null) return;
            location_tv.setText(poi.address == null ? "" : poi.address);
        }

    }
}

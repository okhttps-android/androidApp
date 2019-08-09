package com.uas.appworks.OA.erp.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.EasyFragment;
import com.core.model.WorkModel;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.WorkHandlerUtil;
import com.core.widget.crouton.Crouton;
import com.core.widget.view.oacalender.CalenderView;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class AttendanceFragment extends EasyFragment implements OnHttpResultListener {
    private CalenderView calender;
    private ScrollView month_sv;
    private ListView day_lv;
    private TextView date_tv;

    private TextView l1;
    private TextView l2;
    private TextView l3;
    private TextView l4;
    private TextView l5;
    private TextView l6;

    private TextView r1;
    private TextView r2;
    private TextView r3;
    private TextView r4;
    private TextView r5;
    private TextView r6;

    private WorkAdapter adapter;
    private Date selectDate = null;
    private String newHHmm;


    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_attendance;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (!createView) return;
        ViewUtils.inject(getmRootView());
        initView();
        initEvent();
    }

    private void initView() {

        calender = (CalenderView) findViewById(R.id.calender);
        month_sv = (ScrollView) findViewById(R.id.month_sv);
        day_lv = (ListView) findViewById(R.id.day_lv);
        date_tv = (TextView) findViewById(R.id.date_tv);
        l1 = (TextView) findViewById(R.id.l1);
        l2 = (TextView) findViewById(R.id.l2);
        l3 = (TextView) findViewById(R.id.l3);
        l4 = (TextView) findViewById(R.id.l4);
        l5 = (TextView) findViewById(R.id.l5);
        l6 = (TextView) findViewById(R.id.l6);
        r1 = (TextView) findViewById(R.id.r1);
        r2 = (TextView) findViewById(R.id.r2);
        r3 = (TextView) findViewById(R.id.r3);
        r4 = (TextView) findViewById(R.id.r4);
        r5 = (TextView) findViewById(R.id.r5);
        r6 = (TextView) findViewById(R.id.r6);



        newHHmm = DateFormatUtil.long2Str(System.currentTimeMillis(), "HH:mm");
        date_tv.setText(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy年MM月"));
        loadByNet(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyyMM"));
        View emptyView = LayoutInflater.from(ct).inflate(R.layout.erp_empty_view, null);
        day_lv.setEmptyView(emptyView);
    }


    private void initEvent() {
        calender.setOnDateChangeListener(new CalenderView.OnDateSelectListener() {
            @Override
            public void selected(boolean isClickAgen, Date date) {
                selectDate = date;
                showMessage(isClickAgen);
            }
        });

        calender.setOnMonthChangeListener(new CalenderView.OnMonthChangeListener() {
            @Override
            public void selected(String yyyyMM) {
                long time = DateFormatUtil.str2Long(yyyyMM, "yyyyMM");
                if (time != 0)
                    date_tv.setText(DateFormatUtil.long2Str(time, "yyyy年MM月"));
                loadByNet(yyyyMM);
            }
        });
    }

    /**
     * 显示日历下面数据
     *
     * @param isMonth 是否显示全月   全月数据  、当天数据
     */
    private void showMessage(boolean isMonth) {
        if (isMonth) {
            month_sv.setVisibility(View.VISIBLE);
            day_lv.setVisibility(View.GONE);
        } else {
            month_sv.setVisibility(View.GONE);
            day_lv.setVisibility(View.VISIBLE);
            loadWorkData(selectDate.getTime());
        }
    }


    private void loadByNet(String date) {
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        param.put("yearmonth", date);
        Request request = new Request.Bulider()
                .setUrl("mobile/getPersonAttend.action")
                .setWhat(0x11)
                .setParam(param)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    public void loadWorkData(long time) {
        if (ct instanceof BaseActivity) {
            ((BaseActivity) ct).progressDialog.show();
        }
        Map<String, Object> param = new HashMap<>();
        param.put("date", DateFormatUtil.long2Str(time,"yyyyMMdd"));
        param.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username"));
        Bundle bundle = new Bundle();
        bundle.putLong("time", time);
        Request request = new Request.Bulider()
                .setUrl("mobile/getWorkDate.action")
                .setWhat(0x12)
                .setParam(param)
                .setBundle(bundle)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);

    }

    private void loadLog(ArrayList<WorkModel> models, Bundle bundle) {
        long time = 0;
        if (bundle != null) time = bundle.getLong("time");
        String date = TimeUtils.s_long_2_str(time);
        //获取网络数据
        Map<String, Object> param = new HashMap<>();
        String code = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
        param.put("currentMaster", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        param.put("page", 1);
        param.put("pageSize", 100);
        param.put("condition", "cl_emcode='" + code + "' and to_char(cl_time,'yyyy-MM-dd')='" + date + "'");
        param.put("caller", "CardLog");
        param.put("emcode", code);
        param.put("master", CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
        bundle.putParcelableArrayList("models", models);
        bundle.putLong("time", time);
        Request request = new Request.Bulider()
                .setUrl("mobile/oa/workdata.action")
                .setWhat(0x13)
                .setParam(param)
                .setBundle(bundle)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, this);
    }

    /**
     * 处理打卡签到
     */
    private void handlerWorkData(JSONObject object, Bundle bundle) throws Exception {
        boolean isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        ArrayList<WorkModel> models = WorkHandlerUtil.handlerWorkData(object, isB2b);
        if (ListUtils.isEmpty(models)) {
            setDate2Adapter(null);
        }
        loadLog(models, bundle);
    }

    /**
     * 处理打卡签到列表，建议在线程钟使用
     *
     * @param logModels 获取班次信息时候的数据
     * @throws Exception
     */
    private void handlerWorkLog(JSONObject object, final ArrayList<WorkModel> logModels) throws Exception {
        ArrayList<WorkModel> models = WorkHandlerUtil.handlerWorkLog(object, logModels);
        setDate2Adapter(models);
    }


    private void handlerDate(JSONObject object) {
        float achuqin = JSONUtil.getFloat(object, "achuqin");
        int atime =JSONUtil.getInt(object, "atime");
        DecimalFormat df = new DecimalFormat(".##");
        String atimes = null;
        if (achuqin != 0)
            atimes = df.format(atime / achuqin);
        else atimes = "0";
        String achuqins = null;
        if (achuqin == (int) achuqin) achuqins = String.valueOf((int) achuqin);
        else achuqins = String.valueOf(achuqin);
        l1.setText(getString(R.string.ychuqin) + "  " + JSONUtil.getText(object, "ychuqin") + " 天");
        r1.setText(getString(R.string.achuqin) + "  " + achuqins + " 天");
        l2.setText(getString(R.string.nday) + "  " + JSONUtil.getText(object, "nday") + " 天");
        r2.setText(getString(R.string.sign_actualtime) + "  " + atimes + " " + getString(R.string.sign_hour_day));
        l3.setText(getString(R.string.sign_late) + "  " + JSONUtil.getText(object, "latecount") + " " + getString(R.string.sign_Times));
        r3.setText(getString(R.string.sign_leave) + "  " + JSONUtil.getText(object, "earlycount") + " " + getString(R.string.sign_Times));
        l4.setText(getString(R.string.sign_Absenteeism) + "  " + JSONUtil.getText(object, "noncount") + " " + getString(R.string.sign_Times));
        r4.setText(getString(R.string.signcard) + "  " + JSONUtil.getText(object, "signcard") + " " + getString(R.string.sign_Times));
        l5.setText(getString(R.string.leave) + "  " + JSONUtil.getText(object, "qjdaty") + " 天");
        r5.setText(getString(R.string.overtime) + "  " + JSONUtil.getText(object, "overtime") + " 次/2小时");
        l6.setText(getString(R.string.outdays) + "  " + JSONUtil.getText(object, "outdays") + " 天");
        r6.setText(getString(R.string.oaoutplan_title) + " " + JSONUtil.getText(object, "outcount") + " 天/3次");
    }

    private void setDate2Adapter(ArrayList<WorkModel> models) {
        if (adapter == null) {
            adapter = new WorkAdapter();
            adapter.setModels(models);
            day_lv.setAdapter(adapter);
        } else {
            adapter.setModels(models);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (!isJSON) return;
        JSONObject object = JSON.parseObject(message);
        switch (what) {
            case 0x11:
                handlerDate(object.getJSONArray("listdata").getJSONObject(0));
                break;
            case 0x12:
                try {
                    handlerWorkData(object, bundle);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 0x13:
                ArrayList<WorkModel> models = null;
                if (bundle != null) models = bundle.getParcelableArrayList("models");
                try {
                    handlerWorkLog(object, models);
                } catch (Exception e) {
                    e.printStackTrace();

                }
                if (ct instanceof BaseActivity) {
                    ((BaseActivity) ct).progressDialog.dismiss();
                }
                break;
        }

    }


    private int getNull(JSONObject object, String key) {
        if (object == null || !object.containsKey(key)) return 0;
        return object.getIntValue(key);

    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        if (!StringUtil.isEmpty(message))
            Crouton.showToast(ct, message, R.color.load_warning);
    }


    class WorkAdapter extends BaseAdapter {
        private List<WorkModel> models;
        private boolean isBeforeTaday;
        private boolean isTaday;

        public void setModels(List<WorkModel> models) {
            isBeforeTaday = selectDate.before(new Date());
            isTaday = selectDate.equals(new Date());
            LogUtil.i("isAfterTaday=" + isBeforeTaday);
            LogUtil.i("isTaday=" + isTaday);
            this.models = models;
        }

        @Override
        public int getCount() {
            return ListUtils.isEmpty(models) ? 0 : models.size();
        }

        @Override
        public Object getItem(int i) {
            return models.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHoder hoder = null;
            if (view == null) {
                view = LayoutInflater.from(ct).inflate(R.layout.item_attendance, null);
                hoder = new ViewHoder();
                hoder.title_tv = (TextView) view.findViewById(R.id.title_tv);
                hoder.work_tv = (TextView) view.findViewById(R.id.work_tv);
                hoder.work_tag_tv = (TextView) view.findViewById(R.id.work_tag_tv);
                hoder.off_tv = (TextView) view.findViewById(R.id.off_tv);
                hoder.off_tag_tv = (TextView) view.findViewById(R.id.off_tag_tv);
                view.setTag(hoder);
            } else {
                hoder = (ViewHoder) view.getTag();
            }
            initItemView(hoder, i);

            return view;
        }

        private void initItemView(ViewHoder hoder, int position) {
            WorkModel model = models.get(position);
            hoder.title_tv.setText(getString(R.string.sign_flights) + (position + 1) + " " + model.getWorkTime() + "-" + model.getOffTime());
            hoder.work_tv.setText(StringUtil.isEmpty(model.getWorkSignin()) ? (isBeforeTaday ? getString(R.string.no_) : "") : model.getWorkSignin());
            hoder.off_tv.setText(StringUtil.isEmpty(model.getOffSignin()) ? (isBeforeTaday ? getString(R.string.no_) : "") : model.getOffSignin());
            String workTag = "";


            if (StringUtil.isEmpty(model.getWorkSignin())) {
                if (isBeforeTaday || (isTaday && newHHmm.compareTo(model.getWorkend()) > 0))
                    workTag = getString(R.string.missing_card);
            } else {
                if (model.getWorkSignin().compareTo(model.getWorkTime()) > 0 && model.getWorkSignin().compareTo(model.getWorkend()) <= 0)
                    workTag = getLastTime(model.getWorkSignin(), model.getWorkTime(), true);
            }
            hoder.work_tag_tv.setText(workTag);

            String offTag = "";
            if (!StringUtil.isEmpty(model.getOffSignin())) {
                if (model.getOffSignin().compareTo(model.getOffStart()) > 0 && model.getOffSignin().compareTo(model.getOffTime()) < 0)
                    offTag = getLastTime(model.getOffSignin(), model.getOffTime(), false);
            } else {
                if (isBeforeTaday || (isTaday && newHHmm.compareTo(model.getWorkend()) > 0))
                    offTag = getString(R.string.missing_card);
            }
            hoder.off_tag_tv.setText(offTag);
        }

        private String getLastTime(String signin, String time, boolean isWork) {
            long lastTime = TimeUtils.f_str_2_long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + signin + ":00");
            long thisTime = TimeUtils.f_str_2_long(DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + time + ":00");
            float distance = Math.abs((lastTime - thisTime) / 1000);//秒
            int h = (int) (distance / 3600);
            int m = (int) (distance % 3600) / 60;
            return (isWork ? getString(R.string.sign_late) : getString(R.string.sign_leave)) + h + getString(R.string.hour) + m + getString(R.string.minute);
        }

        class ViewHoder {
            TextView title_tv, work_tv, work_tag_tv, off_tv, off_tag_tv;
        }
    }

}

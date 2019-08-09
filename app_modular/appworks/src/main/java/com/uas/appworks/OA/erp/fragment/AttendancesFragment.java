package com.uas.appworks.OA.erp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.base.EasyFragment;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.OASigninPicker;
import com.core.widget.MyListView;
import com.lidroid.xutils.ViewUtils;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.activity.TeamsAttShowActivity;
import com.uas.appworks.OA.erp.model.AttenddancesBean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 团队考勤
 * 团队
 */
public class AttendancesFragment extends EasyFragment implements View.OnClickListener {
    private MyListView attendances_mylv;
    private TextView start_date_tv;
    private TextView end_date_tv;
    private TextView all_hardworking_tv;
    private TextView hardworking_first_tv;
    private TextView hardworking_second_tv;
    private TextView hardworking_third_tv;
    private AttenddancesBean mAttenddancesBean;
    private AttenddancesAdapter madapter;
    private String yearmonth = DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyyMM");

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_attendances;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (!createView) return;
        ViewUtils.inject(getmRootView());
        initView();
        initData(yearmonth);
    }

    private void initData(String yearmonth) {
        Map<String, Object> param = new HashMap<>();
        param.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        param.put("yearmonth", yearmonth);
        Request request = new Request.Bulider()
                .setMode(Request.Mode.GET)
                .setUrl("mobile/getTeamAttend.action")
                .setParam(param)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, new OnHttpResultListener() {
            @Override
            public void result(int what, boolean isJSON, String message, Bundle bundle) {
                if (!isJSON) return;
                handleData(message);
            }

            @Override
            public void error(int what, String message, Bundle bundle) {
                if (!StringUtil.isEmpty(message))
                    ToastUtil.showToast(ct, message);
            }
        });

    }

    private void initView() {

        attendances_mylv = (MyListView) findViewById(R.id.attendances_mylv);
        start_date_tv = (TextView) findViewById(R.id.start_date_tv);
        end_date_tv = (TextView) findViewById(R.id.end_date_tv);
        all_hardworking_tv = (TextView) findViewById(R.id.all_hardworking_tv);
        hardworking_first_tv = (TextView) findViewById(R.id.hardworking_first_tv);
        hardworking_second_tv = (TextView) findViewById(R.id.hardworking_second_tv);
        hardworking_third_tv = (TextView) findViewById(R.id.hardworking_third_tv);

        start_date_tv.setOnClickListener(this);
        end_date_tv.setOnClickListener(this);
        all_hardworking_tv.setOnClickListener(this);
        hardworking_first_tv.setOnClickListener(this);
        hardworking_second_tv.setOnClickListener(this);
        hardworking_third_tv.setOnClickListener(this);
        madapter = new AttenddancesAdapter();
        Date date = new Date(System.currentTimeMillis());
        String start_date_year = CalendarUtil.getYear(date) + "";
        int month = CalendarUtil.getMonth(date);
        String start_date_month = "";
        if (month < 10) {
            start_date_month = "0" + month;
        } else {
            start_date_month = month + "";
        }
        start_date_tv.setText(start_date_year + "-" + start_date_month + "-01");
        end_date_tv.setText(DateFormatUtil.long2Str(DateFormatUtil.YMD));
    }


    private void handleData(String message) {
        LogUtil.prinlnLongMsg("attendances_data", message);
        if (!StringUtil.isEmpty(message)) {
            mAttenddancesBean = JSON.parseObject(message, AttenddancesBean.class);
            madapter.setmAttenddancesBean(mAttenddancesBean);
            LogUtil.prinlnLongMsg("mAttenddancesBean", JSON.toJSONString(mAttenddancesBean));
            attendances_mylv.setAdapter(madapter);

            if (!StringUtil.isEmpty(mAttenddancesBean.getDatas().get(0).getEmname())) {
                hardworking_first_tv.setText(mAttenddancesBean.getDatas().get(0).getEmname());
            } else {
                hardworking_first_tv.setText("1");
            }

            if (!StringUtil.isEmpty(mAttenddancesBean.getDatas().get(1).getEmname())) {
                hardworking_second_tv.setText(mAttenddancesBean.getDatas().get(1).getEmname());
            } else {
                hardworking_second_tv.setText("2");
            }
            if (!StringUtil.isEmpty(mAttenddancesBean.getDatas().get(2).getEmname())) {
                hardworking_third_tv.setText(mAttenddancesBean.getDatas().get(2).getEmname());
            } else {
                hardworking_third_tv.setText("3");
            }

        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_date_tv){
            doSelectStartDate();
        }else if (v.getId() == R.id.end_date_tv){
            doSelectEndDate();
        }else if (v.getId() == R.id.all_hardworking_tv){
            startActivity(new Intent(getActivity(), TeamsAttShowActivity.class));
        }
    }

    private void doSelectEndDate() {
        OASigninPicker picker = new OASigninPicker(getActivity());
        picker.setRange(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                end_date_tv.setText(time);
            }
        });
        picker.show();
    }

    private void doSelectStartDate() {
        OASigninPicker picker = new OASigninPicker(getActivity());
        picker.setRange(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                start_date_tv.setText(time);

                //当选择了开始时间后截止时间默认变成所选开始时间那年那月的最后一天
                int currentyear = CalendarUtil.getYear();
                int current_month = CalendarUtil.getMonth();  //当前月份
                if (current_month == CommonUtil.getNumByString(month) && currentyear == CommonUtil.getNumByString(year)) {
                    end_date_tv.setText(DateFormatUtil.long2Str(DateFormatUtil.YMD));
                } else {
                    int monthdays = CalendarUtil.getCurrentDateDays(CommonUtil.getNumByString(year), CommonUtil.getNumByString(month));
                    end_date_tv.setText(year + "-" + month + "-" + monthdays);
                }
                yearmonth = year + month;
                initData(yearmonth);
                madapter.notifyDataSetChanged();
            }
        });
        picker.show();
    }


    //团队考勤统计适配器
    public class AttenddancesAdapter extends BaseAdapter {
        private AttenddancesBean mAttenddancesBean;

        public AttenddancesBean getmAttenddancesBean() {
            return mAttenddancesBean;
        }

        public void setmAttenddancesBean(AttenddancesBean mAttenddancesBean) {
            this.mAttenddancesBean = mAttenddancesBean;
        }

        @Override
        public int getCount() {
            return mAttenddancesBean.getDatas() == null ? 0 : mAttenddancesBean.getDatas().size();
        }

        @Override
        public Object getItem(int position) {
            return mAttenddancesBean.getDatas().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(ct).inflate(R.layout.item_attendances, null);
                viewHolder = new ViewHolder();
                viewHolder.attendances_name_tv = (TextView) convertView.findViewById(R.id.attendances_name_tv);
                viewHolder.attendances_late_tv = (TextView) convertView.findViewById(R.id.attendances_late_tv);
                viewHolder.attendances_leaveearly_tv = (TextView) convertView.findViewById(R.id.attendances_leaveearly_tv);
                viewHolder.attendances_absenteeism_tv = (TextView) convertView.findViewById(R.id.attendances_absenteeism_tv);
                viewHolder.attendances_workhours_tv = (TextView) convertView.findViewById(R.id.attendances_workhours_tv);
                viewHolder.attendances_trend_iv = (ImageView) convertView.findViewById(R.id.attendances_trend_iv);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.attendances_name_tv.setText(mAttenddancesBean.getDatas().get(position).getEmname());
            viewHolder.attendances_late_tv.setText("" + mAttenddancesBean.getDatas().get(position).getLatecount() + getString(R.string.sign_Times));
            viewHolder.attendances_leaveearly_tv.setText("" + mAttenddancesBean.getDatas().get(position).getEarlycount() + getString(R.string.sign_Times));
            viewHolder.attendances_absenteeism_tv.setText("" + mAttenddancesBean.getDatas().get(position).getNoncount() + getString(R.string.sign_Times));
            viewHolder.attendances_workhours_tv.setText("" + mAttenddancesBean.getDatas().get(position).getAtime() + getString(R.string.hour));

            if (CommonUtil.getNumByString(mAttenddancesBean.getDatas().get(position).getAtime()) < 8) {
                viewHolder.attendances_trend_iv.setImageResource(R.drawable.down);
            } else {
                viewHolder.attendances_trend_iv.setImageResource(R.drawable.up);
            }
            return convertView;
        }

        class ViewHolder {
            TextView attendances_name_tv;
            TextView attendances_late_tv;
            TextView attendances_leaveearly_tv;
            TextView attendances_absenteeism_tv;
            TextView attendances_workhours_tv;
            ImageView attendances_trend_iv;
        }
    }

}

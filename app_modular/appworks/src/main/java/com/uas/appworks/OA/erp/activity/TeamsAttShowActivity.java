package com.uas.appworks.OA.erp.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.time.wheel.DatePicker;
import com.uas.appworks.R;
import com.uas.appworks.OA.erp.model.AttenddancesBean;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by FANGlh on 2017/2/18.
 * function:
 */
public class TeamsAttShowActivity extends BaseActivity {

    private static final int TEAMS_ATTENDANCES_REQUEST = 2017021801;
    private ListView plv;
    private AttenddancesBean mAttenddancesBean;
    private String myearmonth = DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyyMM");//默认当前月
    private List<String> teams_section; //员工部门职位 eg：产品规划部>熊短小
    private TeamsAttShowAdapter myadapter;
    private DBManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.teams_att_show);
        initView();
        initData(myearmonth);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            
            switch (msg.what){
                case TEAMS_ATTENDANCES_REQUEST:
                    if (msg.getData() != null){
                        String teams_attendances_result = msg.getData().getString("result");
                        LogUtil.prinlnLongMsg("teams_attendances_result",teams_attendances_result);
                        doDateHandle(teams_attendances_result);
                    }
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                        progressDialog.dismiss();
                    }
                    break;
            }
        }
    };

    private void doDateHandle(String teams_attendances_result) {

        try{

            if (!StringUtil.isEmpty(teams_attendances_result)){
                mAttenddancesBean = JSON.parseObject(teams_attendances_result,AttenddancesBean.class);
                LogUtil.prinlnLongMsg("mAttenddancesBean",JSON.toJSONString(mAttenddancesBean));
                //根据mAttenddancesBean中的emcode 从im中查找:部门>职位并add到teams_section中
                int datasize = mAttenddancesBean.getDatas().size();
                for (int i = 0; i < datasize; i++) {
                    String em_code = mAttenddancesBean.getDatas().get(i).getEmcode();
                    if (!StringUtil.isEmpty(em_code)){
                        try {
                            List<EmployeesEntity> db = manager.select_getEmployee(
                                    new String[]{CommonUtil.getSharedPreferences(ct, "erp_master"),
                                            em_code}
                                    , "whichsys=? and em_code=? ");

                            if (!ListUtils.isEmpty(db)) {
                                teams_section.add(db.get(0).getEM_DEFAULTORNAME() + ">" + db.get(0).getEM_POSITION());
                            }else {
                                teams_section.add("");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        teams_section.add("");
                    }
                }
                myadapter.setmAttenddancesBean(mAttenddancesBean);
                myadapter.setTeams_section(teams_section);
                myadapter.notifyDataSetChanged();
                LogUtil.prinlnLongMsg("teams_section",teams_section + "");
                progressDialog.dismiss();
            } else {
                ToastMessage("resultJsonObject == null");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void initData(String myearmonth) {
        String url = CommonUtil.getSharedPreferences(ct, "erp_baseurl") + "mobile/getTeamAttend.action";
        HashMap<String, Object> params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        params.put("yearmonth", myearmonth);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(this, url, params, mHandler, headers, TEAMS_ATTENDANCES_REQUEST, null, null, "post");
    }

    private void initView() {
        progressDialog.show();
        plv = (ListView) findViewById(R.id.teams_att_show_plv);
        teams_section = new ArrayList<>();
        myadapter = new TeamsAttShowAdapter();
        plv.setAdapter(myadapter);
        manager = new DBManager(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.more_attendances_date, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.more_dates){
            DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH);
            picker.setRange(2016, 2017);
            picker.setSelectedItem(
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH) + 1);
            picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
                @Override
                public void onDatePicked(String year, String month) {
                    myearmonth =  year + "-" + month;
                    ToastMessage(myearmonth);
                    if (!ListUtils.isEmpty(teams_section)) {
                        teams_section.clear();
                    }
                    initData(myearmonth);
                }
            });
            picker.show();
        }
        return super.onOptionsItemSelected(item);
    }


    //适配器
    public class TeamsAttShowAdapter extends BaseAdapter{
        private AttenddancesBean mAttenddancesBean;
        private List<String> teams_section;

        public AttenddancesBean getmAttenddancesBean() {
            return mAttenddancesBean;
        }

        public void setmAttenddancesBean(AttenddancesBean mAttenddancesBean) {
            this.mAttenddancesBean = mAttenddancesBean;
        }

        public List<String> getTeams_section() {
            return teams_section;
        }

        public void setTeams_section(List<String> teams_section) {
            this.teams_section = teams_section;
        }

        @Override
        public int getCount() {
            return mAttenddancesBean == null ? 0 :mAttenddancesBean.getDatas().size();
        }

        @Override    public Object getItem(int position) {
            return mAttenddancesBean.getDatas().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = View.inflate(ct,R.layout.item_teams_att_show,null);
                viewHolder.teams_member_number = (TextView) convertView.findViewById(R.id.item_teams_member_number_tv);
                viewHolder.teams_member_name = (TextView) convertView.findViewById(R.id.item_teams_member_name_tv);
                viewHolder.teams_member_Section = (TextView) convertView.findViewById(R.id.item_teams_member_Section_tv);
                viewHolder.average_working_hours = (TextView) convertView.findViewById(R.id.item_teams_member_average_working_hours_tv);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            int number = position + 1;
            if (number < 4){
                viewHolder.teams_member_number.setTextColor(mContext.getResources().getColor(R.color.no_approval));
            }else if (number < 7 && number >=4){
                viewHolder.teams_member_number.setTextColor(mContext.getResources().getColor(R.color.blue));
            }else {
                viewHolder.teams_member_number.setTextColor(mContext.getResources().getColor(R.color.approval));
            }
            if (number < 10){
                viewHolder.teams_member_number.setText("0" + number);
            }else {
                viewHolder.teams_member_number.setText(number + "");
            }
            viewHolder.teams_member_name.setText(mAttenddancesBean.getDatas().get(position).getEmname());
            if (!ListUtils.isEmpty(teams_section) && !StringUtil.isEmpty(teams_section.get(position))){
                viewHolder.teams_member_Section.setText(teams_section.get(position));
            }else {
                viewHolder.teams_member_Section.setText("");
            }
            viewHolder.average_working_hours.setText(getString(R.string.sign_Average_hours) +":" + mAttenddancesBean.getDatas().get(position).getAtime() + getString(R.string.hour));

            return convertView;
        }
        class ViewHolder {
            TextView teams_member_number;
            TextView teams_member_name;
            TextView teams_member_Section;
            TextView average_working_hours;
        }
    }
}

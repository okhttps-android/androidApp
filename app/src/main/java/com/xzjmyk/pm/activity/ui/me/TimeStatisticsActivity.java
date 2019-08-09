package com.xzjmyk.pm.activity.ui.me;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.preferences.PreferenceUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.common.data.ListUtils;
import com.core.widget.MyListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/2/9.
 * function:统计显示UU登入、登出、以及被杀死的时间记录
 */
public class TimeStatisticsActivity extends BaseActivity {
    @ViewInject(R.id.login_in_lv)
    private MyListView login_in_lv;
    @ViewInject(R.id.login_exit_lv)
    private MyListView login_exit_lv;
    @ViewInject(R.id.killed_lv)
    private MyListView killed_lv;
    @ViewInject(R.id.delete_loogin_in_time)
    private TextView delete_loogin_in_time;
    @ViewInject(R.id.delete_loogin_exit_time)
    private TextView delete_loogin_exit_time;
    @ViewInject(R.id.delete_killed_time)
    private TextView delete_killed_time;

    public static List<String> login_in_times = new ArrayList<>();
    public static List<String> login_exit_times = new ArrayList<>();
    public static List<String> killed_times = new ArrayList<>();
    private LoginInTimeAdapter mloginInTimeAdapter;
    private LoginExitAdapter mloginExitAdapter;
    private KilledTimeAdapter mkilledTimeAdapter;
    public static String Login_In = "LOGIN_IN";
    public static String Login_Exit = "LOGIN_EXIT";
    public static String Killed = "KILLED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_statistics);
        ViewUtils.inject(this);
        initView();
        initData();
        clickEvent();
    }

    private void clickEvent() {
        delete_loogin_in_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.putString(TimeStatisticsActivity.Login_In,"");
                login_in_times.remove(login_in_times);
                mloginInTimeAdapter.notifyDataSetChanged();
            }
        });
        delete_loogin_exit_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.putString(TimeStatisticsActivity.Login_Exit,"");
                login_exit_times.remove(login_exit_times);
                mloginInTimeAdapter.notifyDataSetChanged();
            }
        });
        delete_killed_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.putString(TimeStatisticsActivity.Killed, "");
                killed_times.remove(killed_times);
                mkilledTimeAdapter.notifyDataSetChanged();
            }
        });

    }

    private void initView() {
        login_in_times = new ArrayList<>();
        mloginInTimeAdapter = new LoginInTimeAdapter();
        login_in_lv.setAdapter(mloginInTimeAdapter);

        login_exit_times = new ArrayList<>();
        mloginExitAdapter = new LoginExitAdapter();
        login_exit_lv.setAdapter(mloginExitAdapter);

        killed_times = new ArrayList<>();
        mkilledTimeAdapter = new KilledTimeAdapter();
        killed_lv.setAdapter(mkilledTimeAdapter);
    }

    private void initData() {
        String getloginintime = PreferenceUtils.getString(MyApplication.getInstance(),TimeStatisticsActivity.Login_In);
        if (!TextUtils.isEmpty(getloginintime)){
            String[] split = getloginintime.split(",");
            for (int i = 0; i < split.length; i++) {
                login_in_times.add(split[i]);
            }
            Log.i("login_in_times",login_in_times + "");
            if (!ListUtils.isEmpty(login_in_times)){
                mloginInTimeAdapter.setLogin_in_times(login_in_times);
                mloginInTimeAdapter.notifyDataSetChanged();
            }
        }


        String getloginexittime = PreferenceUtils.getString(MyApplication.getInstance(),TimeStatisticsActivity.Login_Exit);
        if (!TextUtils.isEmpty(getloginexittime)){
            String[] split = getloginexittime.split(",");
            for (int i = 0; i < split.length; i++) {
                login_exit_times.add(split[i]);
            }
            Log.i("login_exit_times",login_exit_times + "");
            if (!ListUtils.isEmpty(login_exit_times)){
                mloginExitAdapter.setLogin_exit_times(login_exit_times);
                mloginExitAdapter.notifyDataSetChanged();
            }
        }


        String getkilledtime = PreferenceUtils.getString(MyApplication.getInstance(),TimeStatisticsActivity.Killed);
        if (!TextUtils.isEmpty(getkilledtime)){
            String[] split = getkilledtime.split(",");
            for (int i = 0; i < split.length; i++) {
                killed_times.add(split[i]);
            }

            Log.i("killed_times",killed_times +"");
            if (!ListUtils.isEmpty(killed_times)){
                mkilledTimeAdapter.setKilled_times(killed_times);
                mkilledTimeAdapter.notifyDataSetChanged();
            }
        }

    }


    public class LoginInTimeAdapter extends BaseAdapter{
        private List<String> login_in_times;

        public List<String> getLogin_in_times() {
            return login_in_times;
        }

        public void setLogin_in_times(List<String> login_in_times) {
            this.login_in_times = login_in_times;
        }

        @Override
        public int getCount() {
            return login_in_times == null ? 0 :login_in_times.size();
        }

        @Override
        public Object getItem(int position) {
            return login_in_times.get(position);
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
                convertView =  View.inflate(mContext, R.layout.item_comdoc_am,null);
                viewHolder.docmainmsg_list = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
                viewHolder.docmainmsg_value = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            int nums = position + 1;
            viewHolder.docmainmsg_list.setText("UU登入时间" + nums);
            if (!TextUtils.isEmpty(getLogin_in_times().get(position))){
                viewHolder.docmainmsg_value.setText(getLogin_in_times().get(position));
            }
            return convertView;
        }

        class ViewHolder{
            TextView docmainmsg_list;
            TextView docmainmsg_value;
        }
    }

    public class LoginExitAdapter extends BaseAdapter{
        private List<String> login_exit_times;

        public List<String> getLogin_exit_times() {
            return login_exit_times;
        }

        public void setLogin_exit_times(List<String> login_exit_times) {
            this.login_exit_times = login_exit_times;
        }

        @Override
        public int getCount() {
            return login_exit_times == null ? 0 : login_exit_times.size();
        }

        @Override
        public Object getItem(int position) {
            return login_exit_times.get(position);
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
                convertView =  View.inflate(mContext, R.layout.item_comdoc_am,null);
                viewHolder.docmainmsg_list = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
                viewHolder.docmainmsg_value = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            int nums = position + 1;
            viewHolder.docmainmsg_list.setText("UU登出时间" +  nums);
            if (!TextUtils.isEmpty(getLogin_exit_times().get(position))){
                viewHolder.docmainmsg_value.setText(getLogin_exit_times().get(position));
            }
            return convertView;
        }

        class ViewHolder{
            TextView docmainmsg_list;
            TextView docmainmsg_value;
        }
    }

    public class KilledTimeAdapter extends BaseAdapter{
        private List<String> killed_times;

        public List<String> getKilled_times() {
            return killed_times;
        }

        public void setKilled_times(List<String> killed_times) {
            this.killed_times = killed_times;
        }

        @Override
        public int getCount() {
            return killed_times == null ? 0 : killed_times.size();
        }

        @Override
        public Object getItem(int position) {
            return killed_times.get(position);
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
                convertView =  View.inflate(mContext, R.layout.item_comdoc_am,null);
                viewHolder.docmainmsg_list = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
                viewHolder.docmainmsg_value = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            int nums = position + 1;
            viewHolder.docmainmsg_list.setText("UU被杀死时间" +  nums);
            if (!TextUtils.isEmpty(getKilled_times().get(position))){
                viewHolder.docmainmsg_value.setText(getKilled_times().get(position));
            }
            return convertView;
        }

        class ViewHolder{
            TextView docmainmsg_list;
            TextView docmainmsg_value;
        }
    }
}

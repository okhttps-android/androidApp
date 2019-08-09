package com.uas.appme.pedometer.view;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.dao.DBManager;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.CircleImageView;
import com.core.widget.MyListView;
import com.lidroid.xutils.ViewUtils;
import com.uas.appme.R;
import com.uas.appme.pedometer.bean.StepEntity;
import com.uas.appme.pedometer.db.StepDataDao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FANGlh on 2017/4/13.
 * function:
 */
public class NewStepListActivity extends SupportToolBarActivity {
    private UuStepNumAdapter muuStepNumAdapter;
    private StepDataDao stepDataDao;
    private List<StepEntity> stepEntityList;
    private MyListView step_num_lv;
    private TextView steps_history_tv;
    private LinearLayout step_ll;
    private DBManager manager;
    private CircleImageView step_photo_im;
    private TextView step_name_tv;
    private LinearLayout step_em_ll;
    private String em_name = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_emname");
    private String em_code;
    private Boolean platform;

    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newstep_data);
        ViewUtils.inject(this);
        initView();
        initData();
        showNotice();
    }
    private void initView() {

        step_num_lv = (MyListView) findViewById(R.id.step_num_lv);
        steps_history_tv = (TextView) findViewById(R.id.steps_history_tv);
        step_ll = (LinearLayout) findViewById(R.id.steps_history_ll);
        step_photo_im = (CircleImageView) findViewById(R.id.step_photo_im);
        step_name_tv = (TextView) findViewById(R.id.step_name_tv);
        step_em_ll = (LinearLayout) findViewById(R.id.step_em_ll);

        muuStepNumAdapter = new UuStepNumAdapter();
        stepEntityList = new ArrayList<>();
        steps_history_tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        step_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotice();
            }
        });
        step_ll.setVisibility(View.GONE);
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        manager = new DBManager(this);

    }
    private void showNotice() {
        new AlertDialog.Builder(mContext)
                .setTitle(getString(R.string.uu_friend_notice))
                .setMessage(getString(R.string.uu_love_notice))
                .setPositiveButton(getString(R.string.i_known),null)
                .show();
    }


    private void initData() {
        //获取数据库
        stepDataDao = new StepDataDao(this);
        stepEntityList.clear();
        stepEntityList.addAll(stepDataDao.getAllDatas());
        Log.i("stepEntityList", stepEntityList + "");

        if (!ListUtils.isEmpty(stepEntityList)){
            muuStepNumAdapter.setStepEntityList(stepEntityList);
            step_num_lv.setAdapter(muuStepNumAdapter);
        }

        if (platform) {
            em_code = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "b2b_uu");
        }
        else {
            em_code = CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_username");
        }
        step_name_tv.setText(em_name + "");
        String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();

        AvatarHelper.getInstance().display(loginUserId, step_photo_im, true, false);
    }

    public class UuStepNumAdapter extends BaseAdapter {
        private List<StepEntity> stepEntityList;
        public List<StepEntity> getStepEntityList() {return stepEntityList;}
        public void setStepEntityList(List<StepEntity> stepEntityList) {this.stepEntityList = stepEntityList;}
        @Override
        public int getCount() {
            return stepEntityList == null ? 0 : stepEntityList.size();
        }
        @Override
        public Object getItem(int position) {
            return stepEntityList.get(position);
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
                convertView =  View.inflate(mContext, R.layout.item_uustep_nums,null);
                viewHolder.docmainmsg_list = (TextView) convertView.findViewById(R.id.item_comdoc_am_list_tv);
                viewHolder.docmainmsg_value = (TextView) convertView.findViewById(R.id.item_comdoc_am_value_tv);
                convertView.setTag(viewHolder);

            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
//            viewHolder.docmainmsg_list.setText(stepEntityList.get(getCount() - position - 1).getCurDate());
            viewHolder.docmainmsg_list.setText( TimeUtils.s_long_2_str(DateFormatUtil.str2Long(stepEntityList.get(getCount() - position - 1).getCurDate(), "yyyy年MM月dd日")));
            viewHolder.docmainmsg_value.setText(stepEntityList.get(getCount() - position - 1).getSteps());
            return convertView;
        }
        class ViewHolder{
            TextView docmainmsg_list;
            TextView docmainmsg_value;
        }
    }
}

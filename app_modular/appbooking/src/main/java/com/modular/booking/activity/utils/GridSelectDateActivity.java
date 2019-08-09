package com.modular.booking.activity.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.model.SelectBean;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.activity.SingleImagePreviewActivity;
import com.modular.booking.R;
import com.modular.booking.adapter.ItemRoomsSelectAdapter;
import com.modular.booking.model.SBMenuModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @desc:功能界面 表格选择界面
 * 自定义头部
 * @author：Arison on 2017/11/1
 */
public class GridSelectDateActivity extends OABaseActivity {
    ItemRoomsSelectAdapter itemAdapter;
    private GridView gvTopic;
    private List<SBMenuModel> menuModels = new ArrayList<>();
    ArrayList<SelectBean> selectBeens = new ArrayList<SelectBean>();
    private TextView tvTopDate;
    private ImageView back;
    private TextView tvVStart;
    private TextView tvVAfter;
    private String companyId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_select);
        gvTopic = (GridView) findViewById(R.id.gv_topic);
        initView();
        initEvent();
    }

    private void initView() {
        View view = LayoutInflater.from(ct).inflate(R.layout.action_service_room, null);
        tvTopDate = (TextView) view.findViewById(R.id.tvTopDate);
        back = (ImageView) view.findViewById(R.id.book_service_search_back);
        tvVStart = (TextView) view.findViewById(R.id.tvVStart);
        tvVAfter = (TextView) view.findViewById(R.id.tvVAfter);
        
        ActionBar bar = this.getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setBackgroundDrawable(getResources().getDrawable(R.color.antionbarcolor));
        bar.setCustomView(view);
      
        tvVStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date =tvTopDate.getText().toString();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(DateFormatUtil.getDate4StrDate(date, "yyyy-MM-dd HH:mm"));
                calendar.add( Calendar.DATE,-1);//把日期往后增加一天.整数往后推,负数往前移动 
                System.out.println(DateFormatUtil.getStrDate4Date(calendar.getTime(), "yyyy-MM-dd HH:mm"));
                String curTime= DateFormatUtil.getStrDate4Date(calendar.getTime(), "yyyy-MM-dd HH:mm");
                if (curTime.compareTo(DateFormatUtil.getStrDate4Date(new Date(),"yyyy-MM-dd HH:mm"))<0){
                    ToastMessage("不能选择过去的时间");
                    return;
                }
                tvTopDate.setText(curTime);
                getBookingDataState(curTime,ServiceUtils.getCodeDateByService(curTime));
            }
        });
        tvVAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date =tvTopDate.getText().toString();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(DateFormatUtil.getDate4StrDate(date, "yyyy-MM-dd HH:mm"));
                calendar.add( Calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动 
                System.out.println(DateFormatUtil.getStrDate4Date(calendar.getTime(), "yyyy-MM-dd HH:mm"));
                String curTime= DateFormatUtil.getStrDate4Date(calendar.getTime(), "yyyy-MM-dd HH:mm");
                tvTopDate.setText(curTime);
                getBookingDataState(curTime,ServiceUtils.getCodeDateByService(curTime));
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (getIntent() != null) {
            selectBeens = getIntent().getParcelableArrayListExtra("data");
           setTitle(getIntent().getStringExtra("title"));
            companyId=getIntent().getStringExtra("companyId");
            String date =getIntent().getStringExtra("date");
            tvTopDate.setText(date);
            getBookingDataState(date,ServiceUtils.getCodeDateByService(date));
            LogUtil.d(TAG,JSON.toJSONString(selectBeens));
            if (!ListUtils.isEmpty(selectBeens)) {
                for (int i = 0; i < selectBeens.size(); i++) {
                    SelectBean selectBean = selectBeens.get(i);
                    com.alibaba.fastjson.JSONObject object = JSON.parseObject(selectBean.getJson());
                    SBMenuModel menuModel = new SBMenuModel();
                    menuModel.setUrl(object.getString("st_imageurl"));
                    if (StringUtil.isEmpty(object.getString("st_name"))) {
                        menuModel.setTitle(object.getString("sm_username"));//医生
                    } else {
                        menuModel.setTitle(object.getString("st_name"));
                    }
                    menuModel.setCode(object.getString("sm_userid"));
                    menuModel.setData(object.toJSONString());
                    menuModels.add(menuModel);
                }
            }
        }
        LogUtil.d("GridSelectActivity", "models:" + JSON.toJSONString(menuModels));
        itemAdapter = new ItemRoomsSelectAdapter(mContext, menuModels);
        gvTopic.setAdapter(itemAdapter);

        itemAdapter.setmOnBookClickListener(new ItemRoomsSelectAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(View view, int position) {
                SBMenuModel menuModel = menuModels.get(position);
                LogUtil.d(TAG, "按钮点击事件!");
                ToastMessage("" + menuModel.getDesc() + menuModel.getTitle());

                if (menuModel.isBooking()) {
                    ToastMessage("包间已满，不可预订！");
                } else {
                    setResult(0x21, new Intent().putExtra("data", menuModel)
                    .putExtra("date",tvTopDate.getText().toString()));
                    finish();
                }
            }
        });

        itemAdapter.setmOnImageClickListener(new ItemRoomsSelectAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {
                SBMenuModel menuModel = menuModels.get(position);
                LogUtil.d(TAG, "图片点击事件!");
                String loginUserId = menuModel.getUrl();
                Intent intent = new Intent(activity, SingleImagePreviewActivity.class);
                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, loginUserId);
                startActivity(intent);
            }
        });
    }

    private static final String TAG = "GridSelectActivity";

    public void initEvent() {
        tvTopDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出日期
                startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                        .putExtra("hasMenu", false)
                        .putExtra("bookType", "0")
                        .putExtra("type", 3), 0x01);
            }
        });
        /*gvTopic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ItemRoomsSelectAdapter.ViewHolder menuModel = (ItemRoomsSelectAdapter.ViewHolder) view.getTag();
                menuModel.tvBookAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        ToastMessage("点击事件！");
                        LogUtil.d(TAG, "按钮点击事件!");
                        ToastMessage("" + menuModel.model.getDesc() + menuModel.model.getTitle());

                        if (menuModel.model.isBooking()) {
                            ToastMessage("包间已满，不可预订！");
                        } else {
                            setResult(0x21, new Intent().putExtra("data", menuModel.model));
                            finish();
                        }
                    }
                });
//                
                menuModel.ivItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LogUtil.d(TAG, "图片点击事件!");
                        String loginUserId = menuModel.model.getUrl();
                        Intent intent = new Intent(activity, SingleImagePreviewActivity.class);
                        intent.putExtra(AppConstant.EXTRA_IMAGE_URI, loginUserId);
                        startActivity(intent);
                    }
                });


            }
        });*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        switch (requestCode) {
            case 0x01:
                try {
                    String startTime = data.getStringExtra("startDate");
                    String displayDate = startTime;
                    tvTopDate.setText(displayDate);
                    getBookingDataState(displayDate
                    ,ServiceUtils.getCodeDateByService(displayDate));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    public void getBookingDataState(String date, final int state) {
        date=DateFormatUtil.getStrDate4Date(DateFormatUtil.getDate4StrDate(date, "yyyy-MM-dd"), "yyyyMMdd");
        LogUtil.d(TAG, "date:" + date+" +state:"+state);
        //http://113.105.74.135:8092/user/appServiceBusytime?yearmonth=20171109&token=8a07b517fdd94248b7f00d120ab29502&commonid=0&type=餐饮&companyid=10002&client=Android%20Client
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appServiceBusytime")
                .add("yearmonth", date)
                .add("commonid", "0")
                .add("type","餐饮")
                .add("companyid",companyId)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(), new ResultSubscriber<Object>(new ResultListener<Object>() {

            @Override
            public void onResponse(Object o) {
                try {
                    LogUtil.d(TAG, o.toString());
                    LogUtil.d(TAG,JSON.toJSONString(menuModels));
                    JSONArray reslut = JSON.parseObject(o.toString()).getJSONArray("reslut");
               
                    if (reslut != null) {
                        for (int i = 0; i < reslut.size(); i++) {
                            JSONObject object = reslut.getJSONObject(i);
                            //名字
                            String sb_spname=object.getString("sb_spname");
                            //时间状态 1，2，3
                            String  sb_endtime=object.getString("sb_endtime");

                            for (int j = 0; j <menuModels.size() ; j++) {
                                SBMenuModel menuModel=menuModels.get(j);
                                if (menuModel.getTitle().equals(sb_spname)){
                                    if (sb_endtime.equals(String.valueOf(state))){
                                        menuModel.setBooking(true);//已经预约
                                    }else{
                                        menuModel.setBooking(false);//未被预约
                                    }
                                }
                            }
                            
                        }
                        if (reslut.size()==0){
                            //{"reslut":[]}  全部可以预约
                            for (int i = 0; i <menuModels.size() ; i++) {
                                menuModels.get(i).setBooking(false);
                            }
                        }
                        LogUtil.d(TAG,JSON.toJSONString(menuModels));
                        itemAdapter.notifyDataSetChanged();
                    }else{
                      
                        //{"reslut":[]}  全部可以预约
                        for (int i = 0; i <menuModels.size() ; i++) {
                            menuModels.get(i).setBooking(false);
                        }
                        LogUtil.d(TAG,"[]:"+JSON.toJSONString(menuModels));
                        itemAdapter.notifyDataSetChanged();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }));
    }
}

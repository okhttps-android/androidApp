package com.modular.booking.activity.services;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.PermissionUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.model.SelectBean;
import com.core.net.utils.NetUtils;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.NScrollerGridView;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.ListViewInScroller;
import com.core.widget.view.SwitchView;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.core.xmpp.model.AddAttentionResult;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.booking.R;
import com.modular.booking.activity.utils.GridSelectActivity;
import com.modular.booking.activity.utils.GridSelectDateActivity;
import com.modular.booking.activity.utils.ShoppingCart;
import com.modular.booking.adapter.ItemFoodStateAdapter;
import com.modular.booking.adapter.ItemFoodsDishlistAdapter;
import com.modular.booking.model.SBListModel;
import com.modular.booking.model.SBMenuModel;
import com.modular.booking.model.SeatsStateModel;
import com.modular.booking.model.ShoppingEntity;
import com.modular.booking.widget.AddSubUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.common.data.JSONUtil.getJSONArray;

public class BServiceAddActivity extends OABaseActivity implements View.OnClickListener {
  
    private Button submit_btn;
    
    private EditText et_book_notes, et_book_phone, et_book_name;//预约人的备注、电话、姓名
    //餐饮
    private TextView tv_food_times, tv_food_rooms, tv_food_peoples;//用餐时间、需要包厢、用餐人数
    //美容美发
    private TextView tv_hair_times, tv_hair_rooms;//服务时间、服务项目
    //运动
    private TextView tv_sport_time, tv_sport_rooms, tv_sport_peoples;//运动时间、运动项目、运动人数
    //医院挂号
    private TextView tv_hospital_rooms, tag_hospital_doctor, tv_hospital_time;//科室、医生、时段
    //会所
    private TextView tv_club_technician, tv_club_time, tv_club_peoples;//技师、时间、人数
    //KTV
    private TextView tv_ktv_times, tv_ktv_rooms, tv_ktv_peoples;//时间、房间、人数
    //抬头信息
    private CircleImageView iv_header;
    private ImageView max_img;
    private TextView tv_title, tv_sub;
    private SBListModel model;
    private boolean submiting = false;
    private String sb_userid, sb_username;
    private String serviceId;//服务id
    private String serviceName;//服务名称
    private String dataService;//详情数据
    private boolean isHasPerson;//是否指定了人员
    private AddSubUtils addSubUtils;
    private String sb_sex="0";//默认女士
    
    //餐饮
    private TextView tvMSeatsName,tvMSeatsNum,tvZSeatsName,tvZSeatsNum,tvDSeatsName,tvDSeatsNum;
    private TextView tv_food_seats;
    private SwitchView sv_food_rooms;
    private boolean foodCheckRooms=false;
    private TextView tvMSeatsTitle,tvZSeatsTitle,tvDSeatsTitle;
    private NScrollerGridView gvMSeats,gvZSeats,gvDSeats;
    private LinearLayout llPanelWait;
    private LinearLayout llPanelBusiness;
    private TextView tvSeatsLeft;
    private TextView tvSeatsRight;
    private RelativeLayout notes_rl;
    private LinearLayout ll_seats_panel;
    private RelativeLayout food_seats_rl;
    private RelativeLayout food_dishs_rl;

    ItemFoodsDishlistAdapter itemFoodsDishlistAdapter;
    ListViewInScroller mDishList;
    private TextView tv_food_dishs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bservice_add);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        initView();
        initData();
    }

    private RadioGroup rg_sex;
    
    private void initView() {
        rg_sex = (RadioGroup)findViewById(R.id.rg_sex);
        submit_btn = (Button) findViewById(R.id.submit_btn);
        et_book_notes = (EditText) findViewById(R.id.et_book_notes);
        et_book_phone = (EditText) findViewById(R.id.et_book_phone);
        et_book_name = (EditText) findViewById(R.id.et_book_name);

        tv_food_times = (TextView) findViewById(R.id.tv_food_times);
        tv_food_rooms = (TextView) findViewById(R.id.tv_food_rooms);
        tv_food_peoples = (TextView) findViewById(R.id.tv_food_peoples);

        tv_hair_rooms = (TextView) findViewById(R.id.tv_hair_rooms);
        tv_hair_times = (TextView) findViewById(R.id.tv_hair_times);

        tv_sport_peoples = (TextView) findViewById(R.id.tv_sport_peoples);
        tv_sport_rooms = (TextView) findViewById(R.id.tv_sport_rooms);
        tv_sport_time = (TextView) findViewById(R.id.tv_sport_time);

        tv_hospital_rooms = (TextView) findViewById(R.id.tv_hospital_rooms);
        tag_hospital_doctor = (TextView) findViewById(R.id.tag_hospital_doctor);
        tv_hospital_time = (TextView) findViewById(R.id.tv_hospital_time);

        tv_club_technician = (TextView) findViewById(R.id.tv_club_technician);
        tv_club_time = (TextView) findViewById(R.id.tv_club_time);
        tv_club_peoples = (TextView) findViewById(R.id.tv_club_peoples);

        tv_ktv_times = (TextView) findViewById(R.id.tv_ktv_times);
        tv_ktv_rooms = (TextView) findViewById(R.id.tv_ktv_rooms);
        tv_ktv_peoples = (TextView) findViewById(R.id.tv_ktv_peoples);

        iv_header = (CircleImageView) findViewById(R.id.iv_header);
        max_img = (ImageView) findViewById(R.id.max_img);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_sub = (TextView) findViewById(R.id.tv_sub);

        tv_food_seats=(TextView) findViewById(R.id .tv_food_seats);
        tvMSeatsName=(TextView) findViewById(R.id .tvMSeatsName);
        tvMSeatsNum=(TextView) findViewById(R.id .tvMSeatsNum);

        tvZSeatsName=(TextView) findViewById(R.id .tvZSeatsName);
        tvZSeatsNum=(TextView) findViewById(R.id .tvZSeatsNum);

        tvDSeatsName=(TextView) findViewById(R.id .tvDSeatsName);
        tvDSeatsNum=(TextView) findViewById(R.id .tvDSeatsNum);

        sv_food_rooms=(SwitchView)findViewById(R.id.sv_food_rooms);
        
        tvMSeatsTitle=(TextView)findViewById(R.id.tvMSeatsTitle);
         gvMSeats=(NScrollerGridView)findViewById(R.id.gvMSeats);
        tvZSeatsTitle=(TextView)findViewById(R.id.tvZSeatsTitle);
         gvZSeats=(NScrollerGridView)findViewById(R.id.gvZSeats);
        tvDSeatsTitle=(TextView)findViewById(R.id.tvDSeatsTitle);
         gvDSeats=(NScrollerGridView)findViewById(R.id.gvDSeats);
        food_dishs_rl=findViewById(R.id.food_dishs_rl);
     //   food_dishs_rl.setEnabled(true);
        mDishList=findViewById(R.id.lv_dish);
        tv_food_dishs=findViewById(R.id.tv_food_dishs);

        tvSeatsLeft=findViewById(R.id.tvSeatsLeft);
        tvSeatsRight=findViewById(R.id.tvSeatsRight);
        llPanelWait=(LinearLayout)findViewById(R.id.llPanelWait);
        llPanelBusiness=(LinearLayout)findViewById(R.id.llPanelBusiness);
        notes_rl =(RelativeLayout)findViewById(R.id.notes_rl);
        ll_seats_panel=(LinearLayout)findViewById(R.id.ll_seats_panel) ;
        food_seats_rl=(RelativeLayout)findViewById(R.id.food_seats_rl);
        
        submit_btn.setOnClickListener(this);
        et_book_name.setText(CommonUtil.getName());
        et_book_phone.setText(MyApplication.getInstance().mLoginUser.getTelephone());
        
        tv_sub.setOnClickListener(this);
        tv_title.setOnClickListener(this);
        food_dishs_rl.setOnClickListener(this);

        addSubUtils = findViewById(R.id.add_sub);
        addSubUtils.setBuyMax(999)       // 最大购买数，默认为int的最大值
                .setInventory(999)       // 库存，默认为int的最大值
                .setCurrentNumber(1)    // 设置当前数，默认为1
                .setStep(1)             // 步长，默认为1
                .setBuyMin(1)           // 购买的最小值，默认为1
                .setOnWarnListener(new AddSubUtils.OnWarnListener() {
                    @Override
                    public void onWarningForInventory(int inventory) {
                        // Toast.makeText(mContext, "当前库存:" + inventory, Toast.LENGTH_SHORT).show();
                        tv_food_peoples.setText(inventory);
//                        tv_food_peoples.setVisibility(View.GONE);
                    }

                    @Override
                    public void onWarningForBuyMax(int max) {
                        //  Toast.makeText(mContext, "超过最大购买数:" + max, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWarningForBuyMin(int min) {
                        // Toast.makeText(mContext, "低于最小购买数:" + min, Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnChangeValueListener(new AddSubUtils.OnChangeValueListener() {
                    @Override
                    public void onChangeValue(int value, int position) {
                        tv_food_peoples.setText(String.valueOf(value));
                        tv_food_peoples.setVisibility(View.GONE);
                        if (!StringUtil.isEmpty(tv_food_times.getText().toString())) {
                            searchSeatNumbers(tv_food_times.getText().toString(), model.getCompanyid());
                        }
                    }
                });
        tv_food_peoples.setText("1");
        
        rg_sex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton)findViewById(rg_sex.getCheckedRadioButtonId());
              //  ToastMessage("id:"+radioButton.getId()+"text:"+radioButton.getText().toString());
                if (radioButton.getText().toString().equals("先生")){
                    sb_sex="0";
                }else if(radioButton.getText().toString().equals("女士")){
                    sb_sex="1";
                }
            }
        });
        sv_food_rooms.setChecked(false);
        sv_food_rooms.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                //ToastMessage("isChecked:"+isChecked);
                foodCheckRooms=isChecked;
                        
            }
        });
        
        tvSeatsLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvSeatsLeft.setTextColor(mContext.getResources().getColor(R.color.blue_seats_num));
                tvSeatsRight.setTextColor(mContext.getResources().getColor(R.color.gray));
                llPanelBusiness.setVisibility(View.GONE);
                llPanelWait.setVisibility(View.VISIBLE);
            }
        });
        tvSeatsRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvSeatsLeft.setTextColor(mContext.getResources().getColor(R.color.gray));
                tvSeatsRight.setTextColor(mContext.getResources().getColor(R.color.blue_seats_num));
                llPanelBusiness.setVisibility(View.VISIBLE);
                llPanelWait.setVisibility(View.GONE);
                try {
                    if (!isEdited){
                        getSteatListStates(JSON.parseObject(dataService).getString("sb_companyid"));//获取餐饮类别状态信息
                    }else{
                        getSteatListStates(model.getCompanyid());//获取餐饮类别状态信息
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private boolean isEdited = true;

    private void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            model = intent.getParcelableExtra("model");
            isEdited = intent.getBooleanExtra("isEdited", true);
            isHasPerson = intent.getBooleanExtra("isHasPerson", false);
            dataService = intent.getStringExtra("dataService");
            //两个字段是从选择人员界面选过来的
            sb_userid = intent.getStringExtra("sb_userid");
            sb_username = intent.getStringExtra("sb_username");
            LogUtil.d(TAG, "sb_userid:" + sb_userid + "  sb_username:" + sb_username + " dataService:" + dataService);
            LogUtil.prinlnLongMsg("model",JSON.toJSONString(model));
            if (model != null) {
                LogUtil.i(model.toString());
                initType(model.getType());
                if (isEdited) {
                    submit_btn.setText("立即预约");
                    if (!StringUtil.isEmpty(sb_userid)) {
                        AvatarHelper.getInstance().displayAvatar(sb_userid, iv_header, true);
                        AvatarHelper.getInstance().displayAvatar(sb_userid, max_img, true);
                        tv_title.setText(sb_username);
                        if ("10005".equals(model.getIndustrycode())) {
                            tv_sub.setText(getString(R.string.booking_serivce_technician) + " " + model.getAddress());
                        } else {
                            tv_sub.setText(getString(R.string.booking_serivce_teacher) + " " + model.getAddress());
                        }
                    } else {
                        AvatarHelper.getInstance().display(model.getUrl(), iv_header, true);
                        AvatarHelper.getInstance().display(model.getUrl(), max_img, true);
                        tv_title.setText(model.getName());
                        tv_sub.setText(model.getAddress());
                    }

                } else {
                    et_book_phone.setOnClickListener(this);
                    JSONObject object = JSON.parseObject(dataService);
                    String sb_status = object.getString("sb_status");
                    String sb_imageurl = object.getString("sc_imageurl");
                    sb_userid = object.getString("sb_userid");
                    sb_username = object.getString("sb_username");
                    //已取消，已确认，已结束
                    if ("已取消".equals(sb_status)||"已结束".equals(sb_status)) {
                        submit_btn.setVisibility(View.GONE);
                    } else if ("已确认".equals(sb_status)){
                        if (!StringUtil.isEmpty(dataService)) {
                            String recordId = object.getString("sb_recordid");
                            if (MyApplication.getInstance().mLoginUser.getUserId().equals(recordId)) {
                                submit_btn.setVisibility(View.VISIBLE);
                                submit_btn.setText("取消");
                            } else {
                                submit_btn.setVisibility(View.VISIBLE);
                                submit_btn.setText("释放");
                            }
                        }
                    }
                    
                 
                    if (!StringUtil.isEmpty(sb_userid)) {
                        AvatarHelper.getInstance().displayAvatar(sb_userid, iv_header, true);
                        AvatarHelper.getInstance().displayAvatar(sb_userid, max_img, true);
                        tv_title.setText(sb_username);
                        if ("10005".equals(model.getIndustrycode())) {
                            tv_sub.setText(getString(R.string.booking_serivce_technician) + " " + model.getAddress());

                        } else {
                            tv_sub.setText(getString(R.string.booking_serivce_teacher) + " " + model.getAddress());
                        }
                    } else {
                        tv_title.setText(model.getName());
                        tv_sub.setText(model.getAddress());
                        AvatarHelper.getInstance().display(sb_imageurl, iv_header, true);
                        AvatarHelper.getInstance().display(sb_imageurl, max_img, true);
                    }

                    
                }

               // setTitle(model.getName());
               setTitle("预约");

            } else {
                //TODO 由于该界面必须要转进来的对象，如果model缺失，应返回
            }
        } else {
            //TODO 由于该界面必须要转进来的对象，如果model缺失，应返回
        }
    }


    private TextView getTVByType(String type, int requestCode) {
        if (type == null) return null;

        switch (type) {
            case "餐饮"://餐饮
                switch (requestCode) {
                    case NUMBER_SELECT://人数选择
                        return tv_food_peoples;

                    case TIME_SELECT://时间选择
                        return tv_food_times;
                    default:
                        return tv_food_rooms;
                }
            case "美容美发"://  美容美发
                switch (requestCode) {
                    case SERVICE_SELECT:
                        return tv_hair_rooms;
                    case TIME_SELECT://时间选择
                    default:
                        return tv_hair_times;
                }
            case "运动健身":
                switch (requestCode) {
                    case SERVICE_SELECT:
                        return tv_sport_rooms;
                    case NUMBER_SELECT://人数选择
                        return tv_sport_peoples;
                    case TIME_SELECT://时间选择
                    default:
                        return tv_sport_time;
                }
            case "医疗":
                switch (requestCode) {
                    case MAN_SELECT:
                        return tag_hospital_doctor;
                    case SERVICE_SELECT:
                        return tv_hospital_rooms;
                    case TIME_SELECT://时间选择
                    default:
                        return tv_hospital_time;
                }
            case "会所":
                switch (requestCode) {
                    case SERVICE_SELECT://人数选择
                        return tv_club_technician;
                    case NUMBER_SELECT://人数选择
                        return tv_club_peoples;
                    case TIME_SELECT://时间选择
                    default:
                        return tv_club_time;
                }
            case "KTV":
                switch (requestCode) {
                    case SERVICE_SELECT://服务选择包间选择
                        return tv_ktv_rooms;
                    case NUMBER_SELECT://人数选择
                        return tv_ktv_peoples;
                    case TIME_SELECT://时间选择
                    default:
                        return tv_ktv_times;
                }
        }
        return null;
    }


    private final int FOOD_ROOMS = 1;
    
    private final int TIME_SELECT = 11//时间选择（包含时间段、时间点）
            , NUMBER_SELECT = 12//人数选择（1~10以上）
            , SERVICE_SELECT = 13//服务项目
            , MAN_SELECT = 14//医生选择
            ,DISHS_SELECT=15//菜品选择
            ;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id==R.id. food_dishs_rl){
            try {
                startActivityForResult(
                        new Intent(BServiceAddActivity.this,DishSelectActivity.class)
                        .putExtra("tvTitle",model.getName())
                        .putExtra("tvSub",model.getAddress())
                        .putExtra("headImgUrl",model.getUrl())
                        ,DISHS_SELECT);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else if (id == R.id.tv_title) {
            try {
                if (!StringUtil.isEmpty(dataService)) {
                    showDialog(JSON.parseObject(dataService).getString("sc_introduce"));
                } else {
                    showDialog(model.getIntroduce());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (id == R.id.tv_sub) {
            //地图定位(需要经纬度)
            try {
                if (model != null) {
                    if (!StringUtil.isEmpty(model.getLatitude())) {
                        LatLng latLng = new LatLng(Double.valueOf(model.getLatitude()),
                                Double.valueOf(model.getLongitude()));
                        Intent intent = new Intent("com.modular.appworks.NavigationActivity");
                        intent.putExtra("toLocation", latLng);
                        startActivityForResult(intent, 0x23);
                    } else {
                        LatLng latLng = new LatLng(Double.valueOf(JSON.parseObject(dataService).getString("sc_latitude")),
                                Double.valueOf(JSON.parseObject(dataService).getString("sc_longitude")));
                        Intent intent = new Intent("com.modular.appworks.NavigationActivity");
                        intent.putExtra("toLocation", latLng);
                        startActivityForResult(intent, 0x23);
                    }
                } else {
                    LatLng latLng = new LatLng(Double.valueOf(JSON.parseObject(dataService).getString("sc_latitude")),
                            Double.valueOf(JSON.parseObject(dataService).getString("sc_longitude")));
                    Intent intent = new Intent("com.modular.appworks.NavigationActivity");
                    intent.putExtra("toLocation", latLng);
                    startActivityForResult(intent, 0x23);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.et_book_phone) {
            //打电话
            phoneAction(et_book_phone.getText().toString());
        }

        //选择时间（用餐时间、唱歌时间....）
        if (id == R.id.tv_food_times
                || id == R.id.tv_hair_times
                || id == R.id.tv_sport_time
                || id == R.id.tv_hospital_time
                || id == R.id.tv_club_time
                || id == R.id.tv_ktv_times
                ) {
            //TODO 这个类太长了，修改困难，
            //TODO 现在的需求是
            //TODO  1.当model.getBookType()==0的时候，只能选择开始时间，没有结束时间，当model.getBookType()==1的时候，可选开始时间和结束时间，时间间隔是 30分钟
            //TODO  2.需要传进去商家的id或是ktv包厢的id，在里面请求，判断商家繁忙时刻 5.11、获取商家服务繁忙时间段
            String serviceParam = null;
            if (!StringUtil.isEmpty(sb_userid) || "10001".equals(model.getIndustrycode())) {
                if ("10001".equals(model.getIndustrycode())) {
                    serviceParam = serviceId;
                } else {
                    serviceParam = sb_userid;
                }
            } else {
                serviceParam = serviceName;
            }
            LogUtil.d(TAG, "serviceParam:" + serviceParam);
            startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                            .putExtra("startDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("endDate", DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))
                            .putExtra("hasMenu", false)
                            .putExtra("imId", model.getImid())
                            .putExtra("bookType", model.getBookType())//TODO 根据1\0 判断是否有开始时间和结束时间
                            .putExtra("companyId", model.getCompanyid())
                            .putExtra("businessType", model.getType())
                            .putExtra("startTime", model.getStarttime())
                            .putExtra("endTime", model.getEndtime())
                            .putExtra("serviceId", StringUtil.isEmpty(serviceParam) == true ? "" : serviceParam)
                            .putExtra("type", 3)
                    , TIME_SELECT);
        }
        //人数选择（1~10以上）
        else if (
            //id == R.id.tv_food_people||
                id == R.id.tv_ktv_peoples
                        || id == R.id.tv_club_peoples
                        || id == R.id.tv_sport_peoples) {
            ArrayList<SelectBean> formBeaan = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                formBeaan.add(new SelectBean(i == 10 ? "10人以上" : i + "人"));
            }
            Intent intent = new Intent(this, SelectActivity.class)
                    .putExtra("type", 2)
                    .putExtra("title", getString(R.string.service_food_peoples))
                    .putParcelableArrayListExtra("data", formBeaan);
            startActivityForResult(intent, NUMBER_SELECT);
        }
        //是否包间（餐饮时候独有）
//		else if (id == R.id.tv_food_rooms) {
//			ArrayList<SelectBean> formBeaan = new ArrayList<>();
//			formBeaan.add(new SelectBean("是"));
//			formBeaan.add(new SelectBean("否"));
//			Intent intent = new Intent(this, SelectActivity.class)
//					.putExtra("type", 2)
//					.putExtra("title", getString(R.string.service_food_rooms))
//					.putParcelableArrayListExtra("data", formBeaan);
//			startActivityForResult(intent, FOOD_ROOMS);
//		}

        //服务项目
        else if (id == R.id.tv_hair_rooms
                || id == R.id.tv_sport_rooms
                || id == R.id.tv_hospital_rooms
                || id == R.id.tv_club_technician
                || id == R.id.tv_ktv_rooms
                || id == R.id.tv_food_rooms) {
            if (id == R.id.tv_food_rooms && TextUtils.isEmpty(tv_food_times.getText())) {
                ToastUtil.showToast(this, "请先选择用餐时间");
            } else {
                loadStoreService(v.getContentDescription());
            }
        }

        //医疗
        else if (id == R.id.tag_hospital_doctor) {//医生
            Object tag = tv_hospital_rooms.getTag(R.id.tag_id);
            if (tag != null) {
                LogUtil.i("tag=" + tag.toString());
                if (tag instanceof String) {
                    loadServiceMan(tag.toString(), getString(R.string.service_hospital_rooms));
                }
            } else {
                ToastUtil.showToast(ct, "请先选择科室");
            }
        } else if (id == R.id.submit_btn) {
            if (((Button) v).getText().equals("立即预约")) {
                save();
            } else if (((Button) v).getText().equals("取消")) {
                if (!StringUtil.isEmpty(dataService)) {
                    JSONObject object = JSON.parseObject(dataService);
                    String recordId = object.getString("sb_recordid");
                    if (MyApplication.getInstance().mLoginUser.getUserId().equals(recordId)) {
                        showActionDialog("取消");
                    } else {
                        ToastMessage("商家不可取消预约！");
                    } 
                }
            }else if(((Button) v).getText().equals("释放")){
                if (!StringUtil.isEmpty(dataService)) {
                    JSONObject object = JSON.parseObject(dataService);
                    String recordId = object.getString("sb_recordid");
                    if (MyApplication.getInstance().mLoginUser.getUserId().equals(recordId)) {
//                        cancle(String.valueOf(model.getId()));
                        ToastMessage("只有商家可以释放！");
                    } else {
                        //商家释放
                        showActionDialog("释放");
                    }
                }
            }
        }
    }

    private static final String TAG = "BServiceAddActivity";

    public void cancle() {
        LogUtil.d(TAG, "id:" + String.valueOf(model.getId()));
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appCancelService")
                .add("id", String.valueOf(model.getId()))
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.POST)
                .build(), new ResultSubscriber<Object>(new ResultListener<Object>() {

            @Override
            public void onResponse(Object o) {
                try {
                    if (JSONUtil.validate(o.toString())) {
                        if ("true".equals(JSON.parseObject(o.toString()).getString("result"))) {
                            ToastMessage("取消成功！");
                            //取消  通知商家取消  模板7
                            invite(JSON.parseObject(dataService).getString("sc_telephone"), "584f32ed-a24e-4818-99bf-191a5aa0f061");
                            Intent intent = new Intent("com.modular.booking.BookingListActivity");
                            intent.putExtra("curDate", model.getEndtime() == null ? "" : model.getEndtime());
                            startActivity(intent);
                        }
                    } else {
                        ToastMessage("操作失败！");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastMessage("操作失败！");
                }
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        TextView tv = getTVByType(model.getType(), requestCode);
        String tvMessage = null;
        switch (requestCode) {
            case FOOD_ROOMS://是否包间
                String name = "";
                SelectBean bean = data.getParcelableExtra("data");
                if (bean != null && bean.getName() != null) {
                    name = bean.getName();
                }
                if (tv == null) {
                    tv = tv_food_rooms;
                }
                tv.setText(name);
                break;
            case TIME_SELECT://时间段设置
                if ("0".equals(model.getBookType())) {//只能开始时间
                    String startTime = data.getStringExtra("startDate");
                    String displayDate = startTime;
//					tv.setTag(R.id.tag_id2, endTime);
//					tv.setTag(R.id.tag_id, startTime);
                    tv.setText(displayDate);
                    if (model.getIndustrycode().equals("10003")){
                        searchSeatNumbers(displayDate,model.getCompanyid());
                    }
                } else {
                    String startTime = data.getStringExtra("startDate");
                    String endTime = data.getStringExtra("endDate");
                    String displayDate = startTime + "-" + endTime.substring(11, 16);
                    tv.setTag(R.id.tag_id2, endTime);
                    tv.setTag(R.id.tag_id, startTime);
                    tv.setText(displayDate);
                }
                break;
            case NUMBER_SELECT://人数选择
                String number = "";
                SelectBean numberBean = data.getParcelableExtra("data");
                if (numberBean != null && numberBean.getName() != null) {
                    number = numberBean.getName();
                }
                tv.setText(number.substring(0, number.length() - 1));
                break;
            case SERVICE_SELECT:
                try {
                    SBMenuModel menuModel = data.getParcelableExtra("data");
                   if (!StringUtil.isEmpty( data.getStringExtra("date"))){
                       //餐饮包间
                       tv_food_times.setText(data.getStringExtra("date"));
                   }
                    JSONObject object = JSON.parseObject(menuModel.getData());
                    //sm_userid
                    //sm_username
                    tvMessage = object.getString("st_name");
                    serviceId = object.getString("st_id");
                    serviceName = object.getString("st_name");
                    if (tv != null) {
                        tv.setTag(R.id.tag_id, serviceId);
                        tv.setTag(R.id.tag_id2, serviceName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MAN_SELECT:
                try {
                    SBMenuModel menuModel = data.getParcelableExtra("data");
                    JSONObject object = JSON.parseObject(menuModel.getData());
                    //sm_userid
                    //sm_username
                    if (model.getIndustrycode().equals("10001")) {
                        //医生
                        tvMessage = object.getString("sm_username");
                        serviceId = object.getString("sm_userid");
                        serviceName = object.getString("sm_username");
                    } else {
                        //其它
                        tvMessage = object.getString("st_name");
                        serviceId = object.getString("st_id");
                        serviceName = object.getString("st_name");
                    }

                    if (tv != null) {
                        tv.setTag(R.id.tag_id, serviceId);
                        tv.setTag(R.id.tag_id2, serviceName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                SelectBean service = data.getParcelableExtra("data");
//                if (service != null && service.getName() != null) {
//                    LogUtil.i("service=" + JSON.toJSONString(service));
//                    tvMessage = service.getName();
//                    serviceId = service.getFields();
//                    serviceName = service.getName();
//                    if (tv != null) {
//                        tv.setTag(R.id.tag_id, serviceId);
//                        tv.setTag(R.id.tag_id2, serviceName);
//                    }
//                }
                break;
            case DISHS_SELECT:
                List<ShoppingEntity>  shoppingEntities=  ShoppingCart.getInstance().getShoppingList();
                LogUtil.d(TAG,"data:"+JSON.toJSONString(shoppingEntities));
                itemFoodsDishlistAdapter=new ItemFoodsDishlistAdapter(mContext,shoppingEntities);
                mDishList.setAdapter(itemFoodsDishlistAdapter);
                
                CommonUtil.textSpanForStyle(tv_food_dishs,"合计: "+ShoppingCart.getInstance().getTotalQuantity()+"份 ￥"+ShoppingCart.getInstance().getTotalPrice(),ShoppingCart.getInstance().getTotalQuantity()+"份 ￥"+ShoppingCart.getInstance().getTotalPrice(),ct.getResources().getColor(R.color.blue_seats_num));
                break;
        }
        if (tvMessage != null && tv != null) {
            if (tv == tv_hospital_rooms) {
                tag_hospital_doctor.setText("");
            }
            tv.setText(tvMessage);
        }
    }

    //load 服务项目
    public void loadStoreService(final CharSequence title) {
        progressDialog.show();
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL())
                .isDebug(BaseConfig.isDebug()).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("/user/appStoreService")
                .add("companyid", model.getCompanyid())
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.i(o.toString());
                if (JSONUtil.validateJSONObject(o.toString())) {
                    JSONArray array = getJSONArray(o.toString(), "result");
                    SelectBean bean = null;
                    ArrayList<SelectBean> selectBeens = new ArrayList<SelectBean>();
//                    if (ListUtils.isEmpty(array)) {
//                        array = JSONUtil.getJSONArray(TestString.STORE_SERVICE, "result");
//                    }
                    if (!ListUtils.isEmpty(array)) {
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            bean = new SelectBean();
                            int id = JSONUtil.getInt(object, "st_id");
                            String name = JSONUtil.getText(object, "st_name");
                            if (StringUtil.isEmpty(name)) continue;
                            bean.setId(id);
                            bean.setFields(String.valueOf(id));
                            bean.setName(name);
                            bean.setObject(object.toJSONString());
                            bean.setJson(object.toJSONString());
                            selectBeens.add(bean);
                        }
                    }
                    if (!ListUtils.isEmpty(selectBeens)) {
                        if (model.getIndustrycode().equals("10003")) {
                       
                            Intent intent = new Intent(ct, GridSelectDateActivity.class)
                                    .putExtra("type", 2)
                                    .putExtra("title", title)
                                    .putExtra("companyId",model.getCompanyid())
                                    .putExtra("date",tv_food_times.getText().toString())
                                    .putParcelableArrayListExtra("data", selectBeens);
                            startActivityForResult(intent, SERVICE_SELECT);
                        } else {
                            Intent intent = new Intent(ct, GridSelectActivity.class)
                                    .putExtra("type", 2)
                                    .putExtra("title", title)
                                    .putParcelableArrayListExtra("data", selectBeens);
                            startActivityForResult(intent, SERVICE_SELECT);
                        }

                    } else {
                        showToast("该商家当前无可选服务", R.color.load_error);
                    }
                }
                progressDialog.dismiss();
            }
        }));
    }

    //load 选择服务人员
    public void loadServiceMan(String serviceId, final String title) {
        progressDialog.show();
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(BaseConfig.isDebug()).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreman")
                .add("companyid", model.getCompanyid())
                .add("serviceid", serviceId)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.i(o.toString());
                if (JSONUtil.validateJSONObject(o.toString())) {
                    JSONArray array = getJSONArray(o.toString(), "result");
                    SelectBean bean = null;
                    ArrayList<SelectBean> selectBeens = new ArrayList<SelectBean>();
                    //TODO 测试数据
//                    if (ListUtils.isEmpty(array)) {
//                        array = JSONUtil.getJSONArray(TestString.STORE_SERVICE, "result");
//                    }
                    if (!ListUtils.isEmpty(array)) {
                        for (int i = 0; i < array.size(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            bean = new SelectBean();
                            int id = JSONUtil.getInt(object, "sm_userid");
                            String name = JSONUtil.getText(object, "sm_username");
                            if (StringUtil.isEmpty(name)) continue;
                            bean.setId(id);
                            bean.setFields(String.valueOf(id));
                            bean.setName(name);
                            bean.setJson(object.toJSONString());
                            selectBeens.add(bean);
                        }
                    }
                    if (!ListUtils.isEmpty(selectBeens)) {
                        Intent intent = new Intent(ct, GridSelectActivity.class)
                                .putExtra("type", 2)
                                .putExtra("title", title)
                                .putParcelableArrayListExtra("data", selectBeens);
                        startActivityForResult(intent, MAN_SELECT);
                    } else {
                        showToast("该商家当前无可选" + title, R.color.load_error);
                    }
                }
                progressDialog.dismiss();
            }
        }));
    }


    private synchronized void save() {
        if (!NetUtils.isNetWorkConnected(ct)) {
            showToast(R.string.networks_out, R.color.load_error);
            return;
        }
        if (submiting) {
            return;
        }
        submiting = true;
        progressDialog.show();
        final Map<String, Object> map = getServiceMap();
        if (map == null) {
            LogUtil.i("map == null");
            submiting = false;
            progressDialog.dismiss();
            return;
        } else {
            map.put("sb_sex",sb_sex);
            LogUtil.i("map=1" + JSONUtil.map2JSON(map));
            LogUtil.i("map=2" + JSON.toJSONString(map));
            
//			if (1 == 1) {
//				submiting = false;
//				progressDialog.dismiss();
//				return;`
//			}
        }


     List<ShoppingEntity> shoppingEntities=  ShoppingCart.getInstance().getShoppingList();
        List<Map<String,Object>>  shopMap=new ArrayList<>();
        if(!ListUtils.isEmpty(shoppingEntities)){
            for (int i = 0; i <shoppingEntities.size(); i++) {
                //sd_name 菜名，sd_price价格，sd_number数量，sd_url
                ShoppingEntity entity=shoppingEntities.get(i);
                Map<String,Object> mapNode=new HashMap<String,Object>();
                mapNode.put("sd_name",entity.getName());
                mapNode.put("sd_price",entity.getUnitPrice());
                mapNode.put("sd_number",entity.getQuantity());
                mapNode.put("sd_url","");
                shopMap.add(mapNode);
            }
        }
            LogUtil.d(TAG,"save shop map:"+JSON.toJSONString(shopMap));
        final String startTime = map.get("sb_endtime").toString();
        new HttpClient
                .Builder(Constants.IM_BASE_URL())
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("/user/appSaveService")
                        .add("map", JSONUtil.map2JSON(map))
                        .add("mapdetail",JSON.toJSONString(shopMap))
                        .add("token", MyApplication.getInstance().mAccessToken)
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new ResultListener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        try {
                            LogUtil.d("result", "result:" + o.toString());
                            progressDialog.dismiss();
                            submiting = true;
                            //{"result":"true"}
                            if (JSONUtil.validate(o.toString())) {
                                String result = JSON.parseObject(o.toString()).getString("result");
                                if ("true".equals(result)) {
                                    //发送短信
                                    invite(model.getPhone(), "a65d09f6-9273-4380-b13b-0413c6fb5f76");
                                    Intent intent = new Intent("com.modular.booking.BookingListActivity");
                                    intent.putExtra("curDate", startTime == null ? "" : startTime);
                                    startActivity(intent);
                                } else {
                                    ToastMessage(getString(R.string.save_failed));
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            submiting = true;
                        }

                    }
                }));
    }

    private Map<String, Object> getServiceMap() {
        Map<String, Object> map = getMapFormType();
        if (map == null) {
            LogUtil.i("getServiceMap==nulll");
            return map;
        }
        map.put("sb_recordid", MyApplication.getInstance().getLoginUserId());//当前用户ID
        if (TextUtils.isEmpty(et_book_name.getText())) {
            showToast("姓名为必填项", R.color.load_error);
            return null;
        } else {
            map.put("sb_recordor", StringUtil.getTextRexHttp(et_book_name));//姓名
        }
        if (TextUtils.isEmpty(et_book_phone.getText())) {
            showToast("电话为必填项", R.color.load_error);
            return null;
        } else {
            map.put("sb_telephone", StringUtil.getTextRexHttp(et_book_phone));//姓名
        }
        if (TextUtils.isEmpty(et_book_notes.getText())) {
//            showToast("备注为必填项", R.color.load_error);
//            return null;
        } else {
            map.put("sb_remark", StringUtil.getTextRexHttp(et_book_notes));//姓名
        }

        map.put("sb_industry", model.getIndustrycode());
        map.put("sb_companyid", StringUtil.toHttpString(model.getCompanyid()));//商家ID
        map.put("sb_companyname", StringUtil.toHttpString(model.getName()));//商家名称
        map.put("sb_address", StringUtil.toHttpString(model.getAddress()));//地址
        return map;
    }

    private Map<String, Object> getMapFormType() {
        Map<String, Object> map = new HashMap<>();
        switch (model.getType()) {
            case "餐饮"://餐饮st_id
                if (TextUtils.isEmpty(tv_food_peoples.getText())) {
                    showToast("用餐人数为必填项", R.color.load_error);
                    return null;
                } else {
                    map.put("sb_person", StringUtil.getTextRexHttp(tv_food_peoples));//姓名
                }
                if (TextUtils.isEmpty(tv_food_times.getText())) {
                    showToast("用餐时间为必填项", R.color.load_error);
                    return null;
                } else {
                    //sb_enddate的格式是yyyy-MM-dd HH:mm:ss
                    setTime(map, tv_food_times);
                }
                //选包间和选桌位
//                if (!TextUtils.isEmpty(tv_food_rooms.getText())) {
                    if (!TextUtils.isEmpty(tv_food_rooms.getText())){
                        //选包间
                        map.put("sb_spname", tv_food_rooms.getText());
                    }else{
                        if (tv_food_seats.getTag(R.id.tag_id2)!=null){
                            //选桌位
                            if (((Integer)tv_food_seats.getTag(R.id.tag_id2))>0) {
                                map.put("sb_stname", tv_food_seats.getTag(R.id.tag_id));
                            }else{
                                ToastMessage("已无剩余桌位！");
                                return null;
                            }
                        }else{
                            tv_food_rooms.setHint(R.string.common_select);
                            ToastMessage("请选择包间！");
                            return null;
                        }
                    }
                  
//                }
                break;
            case "美容美发":
                if (TextUtils.isEmpty(tv_hair_times.getText())) {
                    showToast("服务时间为必填项", R.color.load_error);
                    return null;
                } else {
                    setTime(map, tv_hair_times);
                }
                if (TextUtils.isEmpty(tv_hair_rooms.getText()) || tv_hair_rooms.getTag(R.id.tag_id) == null) {
                    showToast("服务项目为必填项", R.color.load_error);
                    return null;
                } else {
                    map.put("sb_stname", StringUtil.getTextRexHttp(tv_hair_rooms));//服务项目名称
                    map.put("sb_stid", tv_hair_rooms.getTag(R.id.tag_id));//服务项目ID
                }

                if (!StringUtil.isEmpty(sb_userid)) {
                    map.put("sb_userid", sb_userid);//技师ID
                }
                if (!StringUtil.isEmpty(sb_username)) {
                    map.put("sb_username", sb_username);//技师名字
                }
                map.put("sb_person", 1);//人数
                break;
            case "运动健身":
                if (TextUtils.isEmpty(tv_sport_time.getText())) {
                    showToast("运动时间为必填项", R.color.load_error);
                    return null;
                } else {
                    //sb_enddate的格式是yyyy-MM-dd HH:mm:ss
                    setTime(map, tv_sport_time);
                }
                if (TextUtils.isEmpty(tv_sport_peoples.getText())) {
//                    showToast("运动人数为必填项", R.color.load_error);
//                    return null;
                } else {
                    map.put("sb_person", StringUtil.getFirstInt(tv_sport_peoples.getText().toString(), 1));//人数
                }
                if (TextUtils.isEmpty(tv_sport_rooms.getText())) {
                    showToast("运动项目为必填项", R.color.load_error);
                    return null;
                } else {
                    map.put("sb_spname", StringUtil.getTextRexHttp(tv_sport_rooms));//场地名称
                }
                break;
            case "医疗":
                if (TextUtils.isEmpty(tv_hospital_rooms.getText())) {
                    showToast("科室为必填项", R.color.load_error);
                    return null;
                } else {
                    map.put("sb_stname", StringUtil.getTextRexHttp(tv_hospital_rooms));//服务项目名称
                    map.put("sb_stid", tv_hospital_rooms.getTag(R.id.tag_id));//服务项目ID
                }
                //|| tag_hospital_doctor.getTag(R.id.tag_id) == null
                if (TextUtils.isEmpty(tag_hospital_doctor.getText())) {
                    showToast("医生为必填项", R.color.load_error);
                    return null;
                } else {
                    map.put("sb_username", StringUtil.getTextRexHttp(tag_hospital_doctor));//服务项目名称
                    map.put("sb_userid", tag_hospital_doctor.getTag(R.id.tag_id));//服务项目ID
                }
                if (TextUtils.isEmpty(tv_hospital_time.getText())) {
                    showToast("时间为必填项", R.color.load_error);
                    return null;
                } else {
                    setTime(map, tv_hospital_time);
                }
                //|| tv_hospital_rooms.getTag(R.id.tag_id) == null


                break;
            case "会所":
                if (TextUtils.isEmpty(tv_club_technician.getText()) || tv_club_technician.getTag(R.id.tag_id) == null) {
                    showToast("服务项目为必填项", R.color.load_error);
                    return null;
                } else {
                    map.put("sb_stname", StringUtil.getTextRexHttp(tv_club_technician));//服务项目名称
                    map.put("sb_stid", tv_club_technician.getTag(R.id.tag_id));//服务项目ID
                }
                if (TextUtils.isEmpty(tv_club_time.getText())) {
                    showToast("服务时段为必填项", R.color.load_error);
                    return null;
                } else {
                    setTime(map, tv_club_time);
                }
                if (TextUtils.isEmpty(tv_club_peoples.getText())) {
//                    showToast("人数为必填项", R.color.load_error);
//                    return null;
                } else {
                    map.put("sb_person", StringUtil.getFirstInt(tv_club_peoples.getText().toString(), 1));//人数
                }


                if (!StringUtil.isEmpty(sb_userid)) {
                    map.put("sb_userid", sb_userid);//技师ID
                }
                if (!StringUtil.isEmpty(sb_username)) {
                    map.put("sb_username", sb_username);//技师名字
                }
                break;
            case "KTV":
                if (TextUtils.isEmpty(tv_ktv_rooms.getText()) || tv_ktv_rooms.getTag(R.id.tag_id) == null) {
                    showToast("包房预定为必填项", R.color.load_error);
                    return null;
                } else {
                    map.put("sb_spname", StringUtil.getTextRexHttp(tv_ktv_rooms));//服务项目名称
                }
                if (TextUtils.isEmpty(tv_ktv_times.getText())) {
                    showToast("欢唱时段为必填项", R.color.load_error);
                    return null;
                } else {
                    setTime(map, tv_ktv_times);
                }
                if (TextUtils.isEmpty(tv_ktv_peoples.getText())) {
//                    showToast("人数为必填项", R.color.load_error);
//                    return null;
                } else {
                    map.put("sb_person", StringUtil.getFirstInt(tv_ktv_peoples.getText().toString(), 1));//人数
                }

                break;
        }

        return map;
    }


    private void setTime(Map<String, Object> map, TextView tv) {
        //这个需要与选择时段相对应，有model确定选择的是时间点还是时段
        if (tv == null || TextUtils.isEmpty(tv.getText())) {
            return;
        }
        String message = tv.getText().toString();
        if ("1".equals(model.getBookType())) {//时段
            Object start = tv.getTag(R.id.tag_id);
            Object end = tv.getTag(R.id.tag_id2);
            if (start != null && start instanceof String) {
                map.put("sb_starttime", start.toString() + ":00");
            }
            if (end != null && end instanceof String) {
                map.put("sb_endtime", end.toString() + ":00");
            }
        } else {//时间点
            map.put("sb_endtime", message + ":00");

        }
    }


    private void initType(String type) {
        if (type == null) return;
        JSONObject data = null;
        if (dataService != null) {
            data = JSON.parseObject(dataService);
        }
        if (!isEdited) {
            food_dishs_rl.setEnabled(false);
            et_book_phone.setText(data.getString("sb_telephone"));
            et_book_name.setText(data.getString("sb_recordor"));
            if ("0".equals(data.getString("sb_sex"))){
                rg_sex.check(rg_sex.getChildAt(0).getId());
            }else if("1".equals(data.getString("sb_sex"))){
                rg_sex.check(rg_sex.getChildAt(1).getId());
            }
            if (!StringUtil.isEmpty(data.getString("sb_remark"))){
                et_book_notes.setText(data.getString("sb_remark"));
                notes_rl.setVisibility(View.VISIBLE);
            }else{
                notes_rl.setVisibility(View.GONE);
            }
            et_book_phone.setKeyListener(null);
            et_book_name.setEnabled(false);
            et_book_notes.setEnabled(false);
        }
        switch (type) {
            case "餐饮"://餐饮
                findViewById(R.id.ll_food).setVisibility(View.VISIBLE);
                if (isEdited) {
                    searchSeatNumbers(DateFormatUtil.getStrDate4Date(new Date(),"yyyy-MM-dd HH:ss"),model.getCompanyid());
                    tv_food_times.setOnClickListener(this);
                    tv_food_rooms.setOnClickListener(this);
                    tv_food_peoples.setOnClickListener(this);
                    tv_food_peoples.setVisibility(View.GONE);
                    addSubUtils.setVisibility(View.VISIBLE);
                    tvSeatsRight.setVisibility(View.GONE);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_food_times.setText(data.getString("sb_endtime"));
                    } else {
                        tv_food_times.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    searchSeatNumbers(tv_food_times.getText().toString(),data.getString("sb_companyid"));
                    if(StringUtil.isEmpty(data.getString("sb_spname"))){
                        tv_food_rooms.setText(" ");
                    }else{
                        tv_food_rooms.setText(data.getString("sb_spname"));
                    }
                    tv_food_rooms.setText(data.getString("sb_stname"));
                    tv_food_peoples.setText(data.getString("sb_person"));
                    tv_food_peoples.setVisibility(View.VISIBLE);
                    addSubUtils.setVisibility(View.GONE);
                    if (!StringUtil.isEmpty(dataService)) {
                        JSONObject object = JSON.parseObject(dataService);
                        String recordId = object.getString("sb_recordid");
                        if (MyApplication.getInstance().mLoginUser.getUserId().equals(recordId)) {
                            tvSeatsRight.setVisibility(View.GONE);
                        } else {
                            tvSeatsRight.setVisibility(View.VISIBLE);  //商家释放
                        }
                    }
                }
                break;
            case "美容美发"://  美容美发
                //sb_starttime
                findViewById(R.id.ll_hair).setVisibility(View.VISIBLE);
                if (isEdited) {
                    tv_hair_rooms.setOnClickListener(this);
                    tv_hair_times.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_hair_times.setText(data.getString("sb_endtime"));
                    } else {
                        tv_hair_times.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_hair_rooms.setText(data.getString("sb_stname"));
                }
                break;
            case "运动健身":
                findViewById(R.id.ll_sport).setVisibility(View.VISIBLE);

                if (isEdited) {
                    tv_sport_peoples.setOnClickListener(this);
                    tv_sport_rooms.setOnClickListener(this);
                    tv_sport_time.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_sport_time.setText(data.getString("sb_endtime"));
                    } else {
                        tv_sport_time.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_sport_rooms.setText(data.getString("sb_spname"));
                    tv_sport_peoples.setText(data.getString("sb_person"));
                }
                break;
            case "医疗":
                findViewById(R.id.ll_hospital).setVisibility(View.VISIBLE);
                if (isEdited) {
                    tv_hospital_rooms.setOnClickListener(this);
                    tag_hospital_doctor.setOnClickListener(this);
                    tv_hospital_time.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_hospital_time.setText(data.getString("sb_endtime"));
                    } else {
                        tv_hospital_time.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_hospital_rooms.setText(data.getString("sb_stname"));
                    tag_hospital_doctor.setText(data.getString("sb_username"));
                }
                break;
            case "会所":
                findViewById(R.id.ll_club).setVisibility(View.VISIBLE);
                tv_club_peoples.setVisibility(View.GONE);
                if (isEdited) {
                    tv_club_technician.setOnClickListener(this);
                    tv_club_time.setOnClickListener(this);
                    tv_club_peoples.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_club_time.setText(data.getString("sb_endtime"));
                    } else {
                        tv_club_time.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_club_technician.setText(data.getString("sb_stname"));
                    tv_club_peoples.setText(data.getString("sb_person"));
                }
                break;
            case "KTV":
                findViewById(R.id.ll_ktv).setVisibility(View.VISIBLE);
                tv_ktv_peoples.setVisibility(View.GONE);
                if (isEdited) {
                    tv_ktv_times.setOnClickListener(this);
                    tv_ktv_rooms.setOnClickListener(this);
                    tv_ktv_peoples.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_ktv_times.setText(data.getString("sb_endtime"));
                    } else {
                        tv_ktv_times.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_ktv_rooms.setText(data.getString("sb_spname"));
                    tv_ktv_peoples.setText(data.getString("sb_person"));
                }
                break;
            case "10001"://医院
                findViewById(R.id.ll_hospital).setVisibility(View.VISIBLE);
                if (isEdited) {
                    tv_hospital_rooms.setOnClickListener(this);
                    tag_hospital_doctor.setOnClickListener(this);
                    tv_hospital_time.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_hospital_time.setText(data.getString("sb_endtime"));
                    } else {
                        tv_hospital_time.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_hospital_rooms.setText(data.getString("sb_stname"));
                    tag_hospital_doctor.setText(data.getString("sb_username"));

                    tv_hospital_time.setCompoundDrawables(null,null,null,null);
                    tv_hospital_rooms.setCompoundDrawables(null,null,null,null);
                }
                break;
            case "10002"://运动健身
                findViewById(R.id.ll_sport).setVisibility(View.VISIBLE);
                tv_sport_peoples.setVisibility(View.GONE);
                if (isEdited) {
                    tv_sport_peoples.setOnClickListener(this);
                    tv_sport_rooms.setOnClickListener(this);
                    tv_sport_time.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_sport_time.setText(data.getString("sb_endtime"));
                    } else {
                        tv_sport_time.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_sport_rooms.setText(data.getString("sb_spname"));
                    tv_sport_peoples.setText(data.getString("sb_person"));

                    tv_sport_rooms.setCompoundDrawables(null,null,null,null);
                    tv_sport_peoples.setCompoundDrawables(null,null,null,null);
                }
                break;
            case "10003"://餐饮
                findViewById(R.id.ll_food).setVisibility(View.VISIBLE);
                if (isEdited) {
                    searchSeatNumbers(DateFormatUtil.getStrDate4Date(new Date(),"yyyy-MM-dd HH:ss"),model.getCompanyid());
                    tv_food_times.setOnClickListener(this);
                    tv_food_rooms.setOnClickListener(this);
                    tv_food_peoples.setOnClickListener(this);
                    tv_food_peoples.setVisibility(View.GONE);
                    addSubUtils.setVisibility(View.VISIBLE);
                    tvSeatsRight.setVisibility(View.GONE);
               
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_food_times.setText(data.getString("sb_endtime"));
                    } else {
                        tv_food_times.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }

                    searchSeatNumbers(tv_food_times.getText().toString(),data.getString("sb_companyid"));
                    if(StringUtil.isEmpty(data.getString("sb_spname"))){
                        tv_food_rooms.setText(" ");
                    }else{
                        tv_food_rooms.setText(data.getString("sb_spname"));
                    }
                    tv_food_peoples.setText(data.getString("sb_person"));
                    tv_food_peoples.setVisibility(View.VISIBLE);
                    addSubUtils.setVisibility(View.GONE);
                    if (!StringUtil.isEmpty(dataService)) {
                        JSONObject object = JSON.parseObject(dataService);
                        String recordId = object.getString("sb_recordid");
                        if (MyApplication.getInstance().mLoginUser.getUserId().equals(recordId)) {
                            tvSeatsRight.setVisibility(View.GONE);
                        } else {
                            tvSeatsRight.setVisibility(View.VISIBLE);  //商家释放
                        }
                    }

                    tv_food_seats .setCompoundDrawables(null,null,null,null);
                    tv_food_peoples .setCompoundDrawables(null,null,null,null);
                    tv_food_times .setCompoundDrawables(null,null,null,null);
                    tv_food_rooms.setCompoundDrawables(null,null,null,null);
                }
                break;
            case "10004"://美容美发
                findViewById(R.id.ll_hair).setVisibility(View.VISIBLE);
                if (isEdited) {
                    tv_hair_rooms.setOnClickListener(this);
                    tv_hair_times.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_hair_times.setText(data.getString("sb_endtime"));
                    } else {
                        tv_hair_times.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_hair_rooms.setText(data.getString("sb_stname"));
                    tv_hair_times.setCompoundDrawables(null,null,null,null);
                    tv_hair_rooms. setCompoundDrawables(null,null,null,null);
                }
                break;
            case "10005"://会所
                findViewById(R.id.ll_club).setVisibility(View.VISIBLE);
                tv_club_peoples.setVisibility(View.GONE);
                if (isEdited) {
                    tv_club_technician.setOnClickListener(this);
                    tv_club_time.setOnClickListener(this);
                    tv_club_peoples.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_club_time.setText(data.getString("sb_endtime"));
                    } else {
                        tv_club_time.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_club_technician.setText(data.getString("sb_stname"));
                    tv_club_peoples.setText(data.getString("sb_person"));

                    tv_club_technician. setCompoundDrawables(null,null,null,null);
                    tv_club_peoples.setCompoundDrawables(null,null,null,null);
                }
                break;
            case "10006"://ktv
                findViewById(R.id.ll_ktv).setVisibility(View.VISIBLE);
                tv_ktv_peoples.setVisibility(View.GONE);
                if (isEdited) {
                    tv_ktv_times.setOnClickListener(this);
                    tv_ktv_rooms.setOnClickListener(this);
                    tv_ktv_peoples.setOnClickListener(this);
                } else {
                    if (StringUtil.isEmpty(data.getString("sb_starttime"))) {
                        tv_ktv_times.setText(data.getString("sb_endtime"));
                    } else {
                        tv_ktv_times.setText(data.getString("sb_starttime").substring(0, 10) + " "
                                + data.getString("sb_starttime").substring(11, 16) + "-" + data.getString("sb_endtime").substring(11, 16));
                    }
                    tv_ktv_rooms.setText(data.getString("sb_spname"));
                    tv_ktv_peoples.setText(data.getString("sb_person"));
                    tv_ktv_rooms.setCompoundDrawables(null,null,null,null);
                    tv_ktv_peoples. setCompoundDrawables(null,null,null,null);
                    tv_ktv_times.setCompoundDrawables(null,null,null,null);
                }
                break;

        }
    }


    private void invite(String user, final String modeid) {
        LogUtil.d(TAG, "短信手机号：" + user + "  模板：" + modeid);
        final String name = CommonUtil.getName();
        final String phone = user.trim().replaceAll(" ", "");
        if (!StringUtil.isMobileNumber(phone)) {
            showToast("选择人员电话号码为空或是格式不正确", R.color.load_submit);
            return;
        }
        StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
                Request.Method.POST, "http://message.ubtob.com/sms/send", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                dimssLoading();
            }
        }, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
            @Override
            public void onResponse(ObjectResult<AddAttentionResult> result) {
                //  showToast("短信发送成功", R.color.load_submit);
                //ToastUtil.showToast(MyApplication.getInstance(),"短信发送成功");
                //   Toast.makeText(MyApplication.getInstance(),"短信发送成功",Toast.LENGTH_SHORT).show();

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


    public void phoneAction(final String phone) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.dialog_confim_phone))
                .content(getString(R.string.dialog_phone) + ":" + phone)
                .positiveText(getString(R.string.dialog_phone_action))
                .negativeText(getString(R.string.common_cancel))
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        // 用intent启动拨打电话
                        if (PermissionUtil.lacksPermissions(ct, Manifest.permission.CALL_PHONE)) {
                            PermissionUtil.requestPermission(activity, PermissionUtil.DEFAULT_REQUEST, Manifest.permission.CALL_PHONE);
                        } else {
                            if (!StringUtil.isMobileNumber(phone)) {
                                ToastUtil.showToast(ct, R.string.phone_number_format_error);
                                return;
                            }
                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                ToastUtil.showToast(ct, R.string.not_system_permission);
                                return;
                            }
                            activity.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
                        }

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                }).build();

        dialog.show();
    }

    public void showDialog(String content) {
        if (StringUtil.isEmpty(content)){
            content="暂无商家介绍！";
        }
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .content(content)
                .positiveText("知道了")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .build();
        dialog.show();

    }
    
    //展示取消  释放
    public void  showActionDialog(final String action){
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(getString(R.string.app_dialog_title))
                .content("您是否要进行"+action+"操作？")
                .positiveText(getString(R.string.sure))
                .negativeText(getString(R.string.common_cancel))
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        dialog.dismiss();
                        if (action.equals("释放")){
                            release();
                        }
                        if (action.equals("取消")){
                            cancle();
                        }
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                       dialog.dismiss();
                    }
                }).build();
        dialog.show();
                
    }
     
    //商家释放预约接口
    public void release(){

        Map<String,Object> map=new HashMap<>();
        if (!StringUtil.isEmpty(dataService)){
            JSONObject object=JSON.parseObject(dataService);
            map.put("sb_status","已结束");
            map.put("sb_id",object.getString("sb_id"));
         }
          LogUtil.d(TAG,JSON.toJSONString(map));
          HttpClient httpClient=new HttpClient.Builder(Constants.IM_BASE_URL()).build();
                 httpClient.Api().send(new HttpClient.Builder()
                 .url("user/appServiceUpdate")
                 .add("map",JSON.toJSONString(map))
                 .add("token",MyApplication.getInstance().mAccessToken)
                 .method(Method.POST)
                 .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {
          
                     @Override
                     public void onResponse(Object o) {
                         try {
                             if (JSONUtil.validate(o.toString())) {
                                 if ("true".equals(JSON.parseObject(o.toString()).getString("result"))) {
                                     ToastMessage("取消成功！");
                                     Intent intent = new Intent("com.modular.booking.BookingListActivity");
                                     intent.putExtra("curDate", model.getEndtime() == null ? "" : model.getEndtime());
                                     startActivity(intent);
                                 }
                             } else {
                                 ToastMessage("操作失败！");
                             }
                         } catch (Exception e) {
                             e.printStackTrace();
                             ToastMessage("操作失败！");
                         }
                     }
                 }));
    }
    
    
    //人数默认是1  小桌
    //选择时间
    //查询桌子余量
    public void searchSeatNumbers(String yearmonth,String companyid){
        //companyid,userid,yearmonth,token
        yearmonth=DateFormatUtil.getStrDate4Date(DateFormatUtil.getDate4StrDate(yearmonth,"yyyy-MM-dd HH:ss"),"yyyyMMdd");
        String peopleNumber=tv_food_peoples.getText().toString();
        String aType="小桌";
        if (Integer.valueOf(peopleNumber)>2&&Integer.valueOf(peopleNumber)<=5){
            //中
            aType="中桌";
        }else  if(Integer.valueOf(peopleNumber)>5){
            //大
            aType="大桌";
        }else{
            //小
            aType="小桌";
        }
          final String  asType=aType;
          LogUtil.i(TAG,"座位："+asType+"yearmonth:"+yearmonth+"  companyid:"+companyid+" peopleNumber:"+peopleNumber+" id:"+model.getId());
          HttpClient httpClient=new HttpClient.Builder(Constants.IM_BASE_URL()).build();
                 httpClient.Api().send(new HttpClient.Builder()
                 .url("/user/appLineList")
                 .add("companyid",companyid)
                 .add("userid",MyApplication.getInstance().mLoginUser.getUserId())
                 .add("yearmonth",yearmonth)
                 .add("id",model.getId())
                 .add("token",MyApplication.getInstance().mAccessToken)
                 .method(Method.POST)
                 .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {
          
                     @Override
                     public void onResponse(Object o) {
                         LogUtil.prinlnLongMsg("appLineList",o.toString());
                         dimssLoading();
                         try {
                             LogUtil.d(TAG,o.toString());
                             boolean isEnter=false;
                             JSONArray deskbook=JSON.parseObject(o.toString()).getJSONArray("deskbook");
                             JSONArray desklist=JSON.parseObject(o.toString()).getJSONArray("desklist");
                             JSONArray detaillist=JSON.parseObject(o.toString()).getJSONArray("detaillist");
                             
                             if (!ListUtils.isEmpty(deskbook)){
                                 CommonUtil.textSpanForStyle(tvMSeatsNum,"前方"+"0"+"桌","0",ct.getResources().getColor(R.color.blue_seats_num));
                                 CommonUtil.textSpanForStyle(tvZSeatsNum,"前方"+"0"+"桌","0",ct.getResources().getColor(R.color.blue_seats_num));
                                 CommonUtil.textSpanForStyle(tvDSeatsNum,"前方"+"0"+"桌","0",ct.getResources().getColor(R.color.blue_seats_num));
                                 for (int i = 0; i <deskbook.size() ; i++) {
                                     JSONObject object=deskbook.getJSONObject(i);
                                     String number= object.getString("number");//预约量
                                     String king=object.getString("kind");//类型
                                     String booknumber=object.getString("as_booknumber");//总预约量
                                     String deskcode=object.getString("deskcode");//桌位编号
                                     Integer bookednumber=Integer.valueOf(booknumber)-Integer.valueOf(number);
                                         if (king.equals("小桌")){
                                             CommonUtil.textSpanForStyle(tvMSeatsNum,"前方"+number+"桌",number,ct.getResources().getColor(R.color.blue_seats_num));
                                         }
                                         if (king.equals("中桌")){
                                             CommonUtil.textSpanForStyle(tvZSeatsNum,"前方"+number+"桌",number,ct.getResources().getColor(R.color.blue_seats_num));
                                         }
                                         if (king.equals("大桌")){
                                             CommonUtil.textSpanForStyle(tvDSeatsNum,"前方"+number+"桌",number,ct.getResources().getColor(R.color.blue_seats_num));
                                         }
                                    if (asType.equals(king)){
                                        if (bookednumber<=0){
                                            bookednumber=0;
                                           // ToastMessage("已无剩余桌量！");
                                        }
                                        tv_food_seats.setTag(R.id.tag_id,deskcode);
                                        tv_food_seats.setTag(R.id.tag_id2,bookednumber);//桌位数量
                                        CommonUtil.textSpanForStyle(tv_food_seats,"仅剩"+bookednumber+"桌",String.valueOf(bookednumber),ct.getResources().getColor(R.color.blue_seats_num));
                                        isEnter=false;
                                    }else{
                                       isEnter=true;//置空，下面循环会进入指定逻辑代码
                                    }
                                     //tv_food_seats.setText("仅剩"+bookednumber+"桌");
                                    
                                 }
                             }
                             if (desklist!=null){
                                 if (desklist.size()==0){
                                     tv_food_rooms.setHint(R.string.common_select);
                                     ll_seats_panel.setVisibility(View.GONE);
                                     food_seats_rl.setVisibility(View.GONE);
                                 }else{
                                     ll_seats_panel.setVisibility(View.VISIBLE);
                                     food_seats_rl.setVisibility(View.VISIBLE);
                                 }
                                 for (int i = 0; i <desklist.size() ; i++) {
                                     JSONObject object=desklist.getJSONObject(i);
                                     String as_type= object.getString("as_type");
                                     String as_number=object.getString("as_number");
                                     String as_remark=object.getString("as_remark");
                                     String as_booknumber=object.getString("as_booknumber");
                                     Integer bookNum=Integer.valueOf(as_booknumber)-Integer.valueOf(as_number);
                                     String as_deskcode=object.getString("as_deskcode");
                                    if (ListUtils.isEmpty(deskbook)) {
                                        if (as_type.equals(asType)||isEnter) {
                                            //tv_food_seats.setText("仅剩"+as_number+"桌");
                                            if (bookNum<=0){
                                                bookNum=0;
                                               // ToastMessage("已无剩余桌量！");
                                            }
                                            CommonUtil.textSpanForStyle(tv_food_seats,"仅剩"+bookNum+"桌",String.valueOf(bookNum),ct.getResources().getColor(R.color.blue_seats_num));
                                            LogUtil.d(TAG,"as_number:"+as_number+" as_deskcode:"+as_deskcode);
                                           // tv_food_seats.setTag(0,as_number);
                                            tv_food_seats.setTag(R.id.tag_id,as_deskcode+"01");//桌位编号
                                            tv_food_seats.setTag(R.id.tag_id2,bookNum);//桌位数量
                                       }
                                        LogUtil.d(TAG,"桌:"+as_type+"：前方"+as_number+"桌");
                                        if ("小桌".equals(as_type)){
                                            tvMSeatsName.setText("小桌("+as_remark+")");
                                            // CommonUtil.textSpanForStyle( tvMSeatsName,"小桌("+as_remark+")",as_remark,ct.getResources().getColor(R.color.yellow_home));
                                            //tvMSeatsNum.setText("前方"+as_number+"桌");
                                            LogUtil.d(TAG,"小桌：前方"+as_number+"桌");
                                            CommonUtil.textSpanForStyle(tvMSeatsNum,"前方"+"0"+"桌","0",ct.getResources().getColor(R.color.blue_seats_num));
                                        }
                                        if ("中桌".equals(as_type)){
                                            tvZSeatsName.setText("中桌("+as_remark+")");
                                            //tvZSeatsNum.setText("前方"+as_number+"桌");
                                            CommonUtil.textSpanForStyle(tvZSeatsNum,"前方"+"0"+"桌","0",ct.getResources().getColor(R.color.blue_seats_num));
                                        }
                                        if ("大桌".equals(as_type)){
                                            tvDSeatsName.setText("大桌("+as_remark+")");
                                            // tvDSeatsNum.setText("前方"+as_number+"桌");
                                            CommonUtil.textSpanForStyle(tvDSeatsNum,"前方"+"0"+"桌","0",ct.getResources().getColor(R.color.blue_seats_num));
                                        }
                                    }
                                 }
                             }else{
                                 tv_food_rooms.setHint(R.string.common_select);
                                 ll_seats_panel.setVisibility(View.GONE);
                                 food_seats_rl.setVisibility(View.GONE);
                             }
                             
                             
                             if (!ListUtils.isEmpty(detaillist)){
                                 List<ShoppingEntity>  shoppingEntities= new ArrayList<>();
                                 int totalNum=0;
                                 int totalPrice=0;
                                 for (int i = 0; i <detaillist.size() ; i++) {
                                     JSONObject object=detaillist.getJSONObject(i);
                                     ShoppingEntity model=new ShoppingEntity();
                                     model.setName(object.getString("sd_name"));
                                     model.setQuantity(Integer.valueOf(object.getString("sd_number")));
                                     totalNum=totalNum+Integer.valueOf(object.getString("sd_number"));
                                     model.setUnitPrice(Integer.valueOf(object.getString("sd_price")));
                                     Integer totalModePrice=    Integer.valueOf(object.getString("sd_price"))*Integer.valueOf(object.getString("sd_number"));
                                     totalPrice=totalPrice+totalModePrice;
                                     shoppingEntities.add(model);
                                 }

                                 itemFoodsDishlistAdapter=new ItemFoodsDishlistAdapter(mContext,shoppingEntities);
                                 mDishList.setAdapter(itemFoodsDishlistAdapter);

                                 CommonUtil.textSpanForStyle(tv_food_dishs,"合计: "+totalNum+"份 ￥"+totalPrice,totalNum+"份 ￥"+totalPrice,ct.getResources().getColor(R.color.blue_seats_num));
                             }else{
                                 
                             }
                             
                         } catch (Exception e) {
                             e.printStackTrace();
                         }


                     }
                 }));
    }
    
    
    //获取列表状态
    public void getSteatListStates(String companyId){
        LogUtil.d(TAG,"model companyid："+model.getCompanyid());
          HttpClient httpClient=new HttpClient.Builder(Constants.IM_BASE_URL()).build();
                 httpClient.Api().send(new HttpClient.Builder()
                 .url("user/appDeskDetail")
                 .add("companyid",companyId)
                 .add("token",MyApplication.getInstance().mAccessToken)
                 .method(Method.POST)
                 .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {
          
                     @Override
                     public void onResponse(Object o) {
                         try {
                             LogUtil.d(TAG,o.toString());
                             JSONArray jsonArray=JSON.parseObject(o.toString()).getJSONArray("result");
                             List< SeatsStateModel> seatsM=new ArrayList<>();
                             List< SeatsStateModel> seatsZ=new ArrayList<>();
                             List< SeatsStateModel> seatsD=new ArrayList<>();
                             for (int i = 0; i <jsonArray.size() ; i++) {
                                 JSONObject object=jsonArray.getJSONObject(i);
                                 String ad_deskcode=object.getString("ad_deskcode");
                                 String ad_desktype=object.getString("ad_desktype");
                                 String ad_status=object.getString("ad_status");
                                 String ad_bookid=object.getString("ad_bookid");
                                 
                                 SeatsStateModel model=new SeatsStateModel();
                                 model.setAd_deskcode(ad_deskcode.substring(1,ad_deskcode.length()));
                                 model.setAd_id(ad_deskcode);
                                 model.setAd_status(ad_status);
                                 model.setAd_bookid(ad_bookid);
                                 model.setAd_companyid(object.getString("ad_companyid"));
                                 
                                if ("小桌".equals(ad_desktype)){
                                    seatsM.add(model);
                                }else if("中桌".equals(ad_desktype)){
                                    seatsZ.add(model);
                                }else{
                                    seatsD.add(model);
                                }
                                
                             }
                             
                             tvMSeatsTitle.setText("小桌(1-2人,共"+seatsM.size()+"桌)");
                             tvZSeatsTitle.setText("中桌(3-5人,共"+seatsZ.size()+"桌)");
                             tvDSeatsTitle.setText("大桌(6人以上,共"+seatsD.size()+"桌)");
                             gvMSeats.setAdapter(new ItemFoodStateAdapter(mContext,seatsM));
                             gvDSeats.setAdapter(new ItemFoodStateAdapter(mContext,seatsD));
                             gvZSeats.setAdapter(new ItemFoodStateAdapter(mContext,seatsZ));
                             //ni
                             
                             gvMSeats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                 @Override
                                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                     ItemFoodStateAdapter.ViewHolder viewHolder= (ItemFoodStateAdapter.ViewHolder) view.getTag();
                                     LogUtil.d(TAG,"model:"+JSON.toJSONString(viewHolder.model));
                                     steatAction(viewHolder.model.getAd_id(),viewHolder.model.getAd_status(),viewHolder.model.getAd_bookid());
                                 }
                             });
                             
                             gvDSeats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                 @Override
                                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                     ItemFoodStateAdapter.ViewHolder viewHolder= (ItemFoodStateAdapter.ViewHolder) view.getTag();
                                     LogUtil.d(TAG,"model:"+JSON.toJSONString(viewHolder.model));
                                     steatAction(viewHolder.model.getAd_id(),viewHolder.model.getAd_status(),viewHolder.model.getAd_bookid());
                                 }
                             });
                             
                             gvZSeats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                 @Override
                                 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                     ItemFoodStateAdapter.ViewHolder viewHolder= (ItemFoodStateAdapter.ViewHolder) view.getTag();
                                     LogUtil.d(TAG,"model:"+JSON.toJSONString(viewHolder.model));
                                     steatAction(viewHolder.model.getAd_id(),viewHolder.model.getAd_status(),viewHolder.model.getAd_bookid());
                                 }
                             });
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                 }));
    }
    
    //释放操作，锁定操作
    public void steatAction(String deskcode,String actionType,String bookid){
        showLoading();
        //companyid 公司ID,bookid 服务预约ID,deskcode 桌位号,type 0代表释放操作，1代表锁定操作,token
          if ("0".equals(actionType)){
              actionType="1";
          }else if("1".equals(actionType)){
              actionType="0";
          }

        final  String sb_companyid=JSON.parseObject(dataService).getString("sb_companyid");
        final String sb_id=JSON.parseObject(dataService).getString("sb_id");
        HttpClient httpClient=new HttpClient.Builder(Constants.IM_BASE_URL()).build();
                 httpClient.Api().send(new HttpClient.Builder()
                 .url("user/appStoreRelease")
                 .add("companyid", sb_companyid)
                 .add("bookid",bookid)
                 .add("deskcode",deskcode)
                 .add("type",actionType)
                 .add("token",MyApplication.getInstance().mAccessToken)
                 .method(Method.POST)
                 .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {
          
                     @Override
                     public void onResponse(Object o) {
                         //{"result":"true"}
                         try {
                             dimssLoading();
                             LogUtil.d(TAG,o.toString());
                             if (JSONUtil.validate(o.toString())){
                               String result=  JSON.parseObject(o.toString()).getString("result");
                               if (result.equals("true")){
                                   getSteatListStates(sb_companyid);
                               }else{
                                   ToastMessage("操作失败！");
                               }
                             }
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                 }));
    }
}

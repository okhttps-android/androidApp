package com.modular.booking.activity.business;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.model.Friend;
import com.core.model.OAConfig;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.model.XmppMessage;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.CustomerScrollView;
import com.core.widget.MyListView;
import com.core.xmpp.CoreService;
import com.core.xmpp.ListenerManager;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.ChatMessageListener;
import com.core.xmpp.model.ChatMessage;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.booking.R;
import com.modular.booking.adapter.ItemListTypeAdapter;
import com.modular.booking.model.BookingModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import rx.Observable;
import rx.functions.Action1;

import static com.core.utils.HeightUtils.setListViewHeightBasedOnChildren1;

/**
 * @desc:商务预约详情
 * @author：Arison on 2017/9/11
 */
public class BBookingDetailActivity extends OABaseActivity implements View.OnClickListener {

    private MyListView mListDetail;
    private RelativeLayout rvTop;
    private LinearLayout llLeft;
    private CircleImageView ivMe;
    private TextView tvMe;
    private ImageView ivResultInfo;
    private LinearLayout llRight;
    private CircleImageView ivTarget;
    private TextView tvTarget;
    private TextView tvTime;
    private TextView tvAddress;
    private TextView tvContent;
    private TextView tv_topic;
    private ImageView ivResult;
    private LinearLayout ll_bottom;

    private ItemListTypeAdapter mAdapter;
    private BookingModel model;
    private boolean isMenuShuffle = false;
    private Animation animation;
    private boolean isShared;
    private LinearLayout ll_refuse;
    private TextView tv_title;
    private TextView tv_sender;
    private CoreService mService;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((CoreService.CoreServiceBinder) service).getService();

        }
    };
    private CustomerScrollView sv_top;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbooking_detail);
        bindService(CoreService.getIntent(), mConnection, BIND_AUTO_CREATE);
        initView();
        initEvent();
        initData();
    }

    Menu mMenu;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        Log.e("isMenuShuffle", isMenuShuffle + "");
        if (isMenuShuffle) {
            menu.findItem(R.id.app_about).setVisible(true);
        } else {
            menu.findItem(R.id.app_about).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.app_about) {
            Intent intent = new Intent("com.modular.main.SelectCollisionActivity");
            SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                    .setTitle(getString(R.string.select_share_friend))
                    .setSingleAble(false);
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            startActivityForResult(intent, 0x02);

        } else if (i == android.R.id.home) {
            onBackPressed();

        }
        return true;
    }

    public void initView() {
        setTitle("商务预约");
        sv_top = (CustomerScrollView) findViewById(R.id.sv_top);
        mListDetail = (MyListView) findViewById(R.id.mListDetail);
        rvTop = (RelativeLayout) findViewById(R.id.rv_top);
        llLeft = (LinearLayout) findViewById(R.id.ll_left);
        ivMe = (CircleImageView) findViewById(R.id.iv_me);
        ll_refuse = (LinearLayout) findViewById(R.id.ll_refuse);
        tvMe = (TextView) findViewById(R.id.tv_me);
        tv_sender = (TextView) findViewById(R.id.tv_sender);
        ivResultInfo = (ImageView) findViewById(R.id.iv_resultInfo);
        llRight = (LinearLayout) findViewById(R.id.ll_right);
        ivTarget = (CircleImageView) findViewById(R.id.iv_target);
        tvTarget = (TextView) findViewById(R.id.tv_target);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvAddress = (TextView) findViewById(R.id.tv_address);
        tvContent = (TextView) findViewById(R.id.tv_content);
        tv_topic = (TextView) findViewById(R.id.tv_topic);
        tv_title = (TextView) findViewById(R.id.tv_title);
        ivResult = (ImageView) findViewById(R.id.iv_result);
        ll_bottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.modular.appworks.NavigationActivity");
                intent.putExtra("toLocation", new LatLng(Float.valueOf(model.getAb_latitude()),Float.valueOf(model.getAb_longitude())));
                startActivityForResult(intent, 0x23);
            }
        });
        animation = AnimationUtils.loadAnimation(this, R.anim.anim_translate_bookingprogress);
        if (getIntent() != null && getIntent().getExtras() != null) {
            isShared = getIntent().getExtras().getBoolean("isShared");
            model = getIntent().getExtras().getParcelable("model");
            if ("个人".equals(model.getKind())) {
                updateUi(isShared);
            } else {
                getApiData();
            }

        }

    }

    private void updateUi(boolean isShared) {
        ll_bottom.setVisibility(View.VISIBLE);
        sv_top.setVisibility(View.VISIBLE);
        tvTime.setText(model.getAb_starttime().substring(0, 10) + " " + model.getAb_starttime().substring(11, 16) + "-" +
                model.getAb_endtime().substring(11, 16));
        tvAddress.setText(model.getAb_address());
        if (!StringUtil.isEmpty(model.getAd_reason())) {
            ll_refuse.setVisibility(View.VISIBLE);
            tvContent.setText(model.getAd_reason());
        } else {
            ll_refuse.setVisibility(View.GONE);
        }
        tv_topic.setText(model.getAb_type());
        AvatarHelper.getInstance().display(model.getAb_bmanid(), ivTarget, true, true);
        AvatarHelper.getInstance().display(model.getAb_recordid(), ivMe, true, true);
        ivResultInfo.clearAnimation();
        ll_bottom.setVisibility(View.GONE);
        tv_sender.setText(model.getAb_recordman());
        if ("已拒绝".equals(model.getAb_confirmstatus())) {
            ivResult.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_jujue1));
            ivResultInfo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_jujue));
        }
        if ("未确认".equals(model.getAb_confirmstatus()) || "待确认".equals(model.getAb_confirmstatus())) {
            ivResult.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_weiqueren));
            ivResultInfo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_send));
            //开启动画
            ll_bottom.setVisibility(View.VISIBLE);
            ivResultInfo.startAnimation(animation);
            if (MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_recordid())) {
                //发起人
                ((Button) findViewById(R.id.bt_change)).setText("变更");
                ((Button) findViewById(R.id.bt_change)).setTextColor(getResources().getColor(R.color.white));
                ((Button) findViewById(R.id.bt_cancle)).setText("取消");
            } else {
                //被预约人
                ((Button) findViewById(R.id.bt_change)).setText("确认");
                ((Button) findViewById(R.id.bt_cancle)).setText("拒绝");
            }
        }
        if ("已确认".equals(model.getAb_confirmstatus())) {
            ivResult.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_yiqueren));
            ivResultInfo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_queren1));
            isMenuShuffle = true;
            if (model.getAb_sharestatus().equals("已共享")) {
                isMenuShuffle = true;
            }
            if (MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_recordid())) {
                ((Button) findViewById(R.id.bt_change)).setText("变更");
                ((Button) findViewById(R.id.bt_cancle)).setText("取消");
                ll_bottom.setVisibility(View.VISIBLE);

            } else {
                // ll_bottom.setVisibility(View.VISIBLE);
                ((Button) findViewById(R.id.bt_change)).setText("变更");
                ((Button) findViewById(R.id.bt_cancle)).setText("取消");
                ((Button) findViewById(R.id.bt_change)).setVisibility(View.GONE);
                ll_bottom.setVisibility(View.VISIBLE);
            }
        }
        if ("已取消".equals(model.getAb_confirmstatus())) {
            ivResult.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_quxiao3));
            ivResultInfo.setImageDrawable(mContext.getResources().getDrawable(R.drawable.icon_cancle3));

        }
        //共享状态
        if (isShared) {
            tvTarget.setText(model.getAb_bman());
            tvMe.setText(model.getAb_recordman());
            ll_bottom.setVisibility(View.GONE);
        } else {
            if (MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_recordid())) {
                //我是发起人
                tvMe.setText(getString(R.string.me));
                tvTarget.setText(model.getAb_bman());
            } else {
                //被约 
                tvTarget.setText(getString(R.string.me));
                tvMe.setText(model.getAb_recordman());
            }
        }

        if (model.getAb_starttime().compareTo(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS)) < 0) {
            ll_bottom.setVisibility(View.GONE);
            isMenuShuffle = false;
        }

    }

    public void initEvent() {
        findViewById(R.id.bt_change).setOnClickListener(this);
        findViewById(R.id.bt_cancle).setOnClickListener(this);
    }

    public void initData() {

        mAdapter = new ItemListTypeAdapter(mContext);
        mListDetail.setAdapter(mAdapter);
    }

    private String companys;
    private String bmanid;

    public void getApiData() {
        showLoading();
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).build();
        httpClient.Api()
                .send(new HttpClient.Builder().url("user/appBusinessDetail")
                        .add("id", model.getAb_id())
                        .add("userid", MyApplication.getInstance().mLoginUser.getUserId())
                        .add("token", MyApplication.getInstance().mAccessToken)
                        .build(), new ResultSubscriber<>(new ResultListener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        dimssLoading();
                        LogUtil.d("ResponseText", " onResponse o:" + o.toString());
                        //mapdetail
                        if (JSONUtil.validate(o.toString())) {
                            String detail = JSON.parseObject(o.toString()).getString("mapdetail");
                            String baseInfo = JSON.parseObject(o.toString()).getString("map");
                            JSONArray baseArray = JSON.parseArray(baseInfo);
                            JSONObject baseObject = baseArray.getJSONObject(0);

                            model = JSON.parseObject(baseObject.toJSONString(), BookingModel.class);

//                            tvTime.setText(model.getAb_starttime().substring(0,10)+" "+model.getAb_starttime().substring(11,16)
//                            +"-"+model.getAb_endtime().substring(11,16));
//                            tv_topic.setText(model.getAb_content());
//                            tvAddress.setText(model.getAb_address());
//                            rvTop.setVisibility(View.GONE);
                            updateUi(isShared);
                            JSONArray array = JSON.parseArray(detail);
                            if (!ListUtils.isEmpty(array)) {
                                List<JSONObject> datas = new ArrayList<>();
                                StringBuilder companysBuilder = new StringBuilder("");
                                StringBuilder bmanidBuilder = new StringBuilder("");

                                for (int i = 0; i < array.size(); i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    companysBuilder.append(object.getString("ad_bcompany") + ",");
                                    bmanidBuilder.append(object.getString("ad_bmanid") + ",");
                                    datas.add(object);
                                }
                                companys = companysBuilder.toString();
                                bmanid = bmanidBuilder.toString();
                                mAdapter.setObjects(datas);
                                mAdapter.notifyDataSetChanged();
                                if (mAdapter.getCount() == 0) {
                                    //个人预约界面保持一致
                                    tv_title.setVisibility(View.GONE);
                                    mListDetail.setVisibility(View.GONE);
                                    rvTop.setVisibility(View.VISIBLE);
                                    tvMe.setText(model.getAb_recordman());
                                    tvTarget.setText(model.getAb_bman());
                                    AvatarHelper.getInstance().display(model.getAb_bmanid(), ivTarget, true, true);
                                    AvatarHelper.getInstance().display(model.getAb_recordid(), ivMe, true, true);
                                } else {
                                    tv_title.setVisibility(View.VISIBLE);
                                    rvTop.setVisibility(View.GONE);
                                    mListDetail.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                }));
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_change) {
            updateBookingState((Button) v);
        } else if (i == R.id.bt_cancle) {
            updateBookingState((Button) v);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        switch (requestCode) {
            case 0x02:
                try {
                    List<SelectEmUser> employeesList = data.getParcelableArrayListExtra("data");
                    LogUtil.d("Test", JSON.toJSONString(employeesList));
                    StringBuilder stringBuilder = new StringBuilder("");
                    for (int i = 0; i < employeesList.size(); i++) {
                        if (i == employeesList.size() - 1) {
                            stringBuilder.append(employeesList.get(i).getImId());
                        } else {
                            stringBuilder.append(employeesList.get(i).getImId());
                            stringBuilder.append(",");
                        }
                    }
                    LogUtil.d("Test", stringBuilder.toString());
                    LogUtil.d("Test", model.getAb_id());
                    if (!StringUtil.isEmpty(stringBuilder.toString()) && !StringUtil.isEmpty(model.getAb_id())) {
                        shareBooking(model.getAb_id(), stringBuilder.toString());
                    } else {
                        ToastMessage("共享失败！");
                    }
                } catch (Exception e) {

                }
                break;
        }
    }

    public void shareBooking(String id, String imids) {
        showLoading();
        //map包括planids 个人计划id,bplanids商务计划id,userids共享人员imid，多个id用逗号连接
        String map = "{\"planids\":\"" + "" + "\",\"bplanids\":\"" + id + "\",\"userids\":\"" + imids + "\"}";
        LogUtil.d("HttpLogs", "map:" + map);
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("/user/appBatchShare")
                .add("token", MyApplication.getInstance().mAccessToken)
                .add("map", map)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                dimssLoading();
                if (JSONUtil.validate(o.toString())) {
                    String result = JSON.parseObject(o.toString()).getString("result");
                    if ("success".equals(result)) {
                        ToastMessage("分享成功！");
                    }
                }
            }
        }));

    }

    //发送IM消息
    public void sendMuiltMsg(String content) {
        if (!StringUtil.isEmpty(bmanid)) {
            String[] barray = bmanid.split(",");
            for (int i = 0; i < barray.length; i++) {
                //barray[i]=100263,Ab_recordid:109079,Ab_recordman:陈爱平
                LogUtil.d("XmppApp", "barray[i]=" + barray[i] + ",Ab_recordid:" + model.getAb_recordid() + ",Ab_recordman:"
                        + model.getAb_recordman());
//                sendMessage(model.getAb_recordid(),model.getAb_recordman(),barray[i],content);
                if (MyApplication.getInstance().getLoginUserId().equals(model.getAb_recordid())) {
                    sendMessage(MyApplication.getInstance().mLoginUser.getUserId(),
                            MyApplication.getInstance().mLoginUser.getNickName(), barray[i], content);
                } else {
                    sendMessage(MyApplication.getInstance().mLoginUser.getUserId(),
                            MyApplication.getInstance().mLoginUser.getNickName(), model.getAb_recordid(), content);
                }

            }
        }
    }

    public void updateBookingState(Button button) {
        // 发起人  可以变更和取消
        //被预约人  不能变更和取消
        String map = "{\"ab_confirmstatus\":\"已确认\",\"ab_bmanid\":\"" + model.getAb_bmanid() + "\"}";
        String time = model.getAb_starttime().substring(0, 16) + "-" + model.getAb_endtime().substring(11, 16);
        if (button.getText().equals("变更")) {
            String content = model.getAb_recordman() + "变更了您" + time + "的预约计划";
            sendMuiltMsg(content);
            Bundle bundle = new Bundle();
            bundle.putParcelable("model", model);
            if (!StringUtil.isEmpty(companys)) {
                bundle.putString("companys", companys.substring(0, companys.length() - 1));
            }
            if (!StringUtil.isEmpty(bmanid)) {
                bundle.putString("bmanid", bmanid.substring(0, bmanid.length() - 1));
            }
            startActivity(new Intent(mContext, BBookingAddActivity.class)
                    .putExtras(bundle));
            return;
        }
        if (button.getText().equals("确认")) {
            String content = model.getAb_bman() + "确认了您" + time + "的预约计划";
            //   sendMessage(model.getAb_bmanid(),model.getAb_bman(),model.getAb_recordid(),content);
            String action = "已确认";
            map = "{\"ad_confirmstatus\":\"" + action + "\",\"ad_bmanid\":\"" + MyApplication.getInstance().getLoginUserId() + "\",\"ad_sharestatus\":\"" + "未共享" + "\"}";
            sendMuiltMsg(content);
        }
        if (button.getText().equals("拒绝")) {
            String content = model.getAb_bman() + "拒绝了您" + time + "的预约计划";
            sendMuiltMsg(content);
            // sendMessage(model.getAb_bmanid(),model.getAb_bman(),model.getAb_recordid(),content);
            showPopupWindow(button);
            return;
        }
        if (button.getText().equals("取消")) {
            if (MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_bmanid())) {
                // String content=model.getAb_bman()+"取消了您"+time+"的预约计划";
                ToastMessage("您不能进行取消操作！");
                return;
                //    sendMessage(model.getAb_bmanid(),model.getAb_bman(),model.getAb_recordid(),content);
            } else {
                String content = model.getAb_recordman() + "取消了您" + time + "的预约计划";
                sendMuiltMsg(content);
                //   sendMessage(model.getAb_recordid(),model.getAb_recordman(),model.getAb_bmanid(),content);
            }
//            String action="已取消";
//            map="{\"ad_confirmstatus\":\""+action+"\",\"ad_bmanid\":\""+MyApplication.getInstance().getLoginUserId()+"\",\"ad_sharestatus\":\"" +"未共享"+ "\"}";

            PopupWindowHelper.showAlart(this,
                    getString(R.string.app_dialog_title), "您确定要取消预约计划?"
                    , new PopupWindowHelper.OnSelectListener() {
                        @Override
                        public void select(boolean selectOk) {
                            if (selectOk) {
                                showLoading();
                                String action = "已取消";
                                String map = "{\"ad_confirmstatus\":\"" + action + "\",\"ad_bmanid\":\"" + MyApplication.getInstance().getLoginUserId() + "\",\"ad_sharestatus\":\"" + "未共享" + "\"}";
                                actionOrder(model.getAb_id(), map);
                            } else {
                                dimssLoading();
                            }
                        }
                    });
            return;
        }
        actionOrder(model.getAb_id(), map);
    }


    //确认和取消,拒绝
    public void actionOrder(String id, String map) {
        LogUtil.d("HttpLogs", "id:" + id);
        LogUtil.d("HttpLogs", "map:" + map);
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("/user/appDoBusiness")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                .add("token", MyApplication.getInstance().mAccessToken)
                .add("map", map)
                .add("id", id)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                dimssLoading();
                if (JSONUtil.validate(o.toString())) {
                    String result = JSON.parseObject(o.toString()).getString("result");
                    if ("true".equals(result)) {
                        ToastMessage("操作成功!");
                        Observable.timer(2, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
                            @Override
                            public void call(Long aLong) {
                                startActivity(new Intent("com.modular.booking.BookingListActivity")
                                        .putExtra("curDate", model.getAb_starttime()));
                                finish();
                            }
                        });
                    }
                } else {
                    ToastMessage("接口异常");
                }
            }
        }));

    }


    private PopupWindow popupWindow = null;

    public void showPopupWindow(View parent) {
        dimssLoading();
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = (ListView) view.findViewById(R.id.mList);
            final SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    getPopData(),
                    R.layout.item_pop_list,
                    new String[]{"item_name"}, new int[]{R.id.tv_item_name});
            plist.setAdapter(adapter);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    position = position + 1;
                    String map = "";
                    String action = "已拒绝";
                    switch (position) {
                        case 1:
                            break;
                        case 2:
                            map = "{\"ad_confirmstatus\":\"" + action + "\",\"ad_reason\":\"工作繁忙\",\"ad_bmanid\":\"" + MyApplication.getInstance().getLoginUserId() + "\",\"ad_sharestatus\":\"" + "未共享" + "\"}";
                            //ab_reason  工作繁忙
                            actionOrder(model.getAb_id(), map);
                            break;
                        case 3:
                            //ab_reason  没有必要
                            map = "{\"ad_confirmstatus\":\"" + action + "\",\"ad_reason\":\"沒有必要\",\"ad_bmanid\":\"" + MyApplication.getInstance().getLoginUserId() + "\",\"ad_sharestatus\":\"" + "未共享" + "\"}";
                            actionOrder(model.getAb_id(), map);
                            break;
                        case 4:
                            //ab_reason  其它
                            popupWindow.dismiss();
                            showEditerDialog(model.getAb_id());
                            break;
                    }
                    popupWindow.dismiss();

                }
            });

            popupWindow = new PopupWindow(view, parent.getWidth(), setListViewHeightBasedOnChildren1(plist) + DisplayUtil.dip2px(this, 10));
        }
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        DisplayUtil.backgroundAlpha(this, 0.5f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(activity, 1f);
            }
        });
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, location[0],
                location[1] - popupWindow.getHeight() - 5);
    }


    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String[] lists = getResources().getStringArray(R.array.booking_reject);
        for (String str : lists) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("item_name", str);
            list.add(map);
        }
        return list;
    }


    private void showEditerDialog(final String id) {
        final PopupWindow window = new PopupWindow(ct);
        View view = LayoutInflater.from(ct).inflate(R.layout.item_select_remark_pop, null);
        window.setContentView(view);
        TextView title = (TextView) view.findViewById(R.id.title_tv);
        TextView company_tag = (TextView) view.findViewById(R.id.company_tag);
        title.setText("请输入拒绝理由！");
        company_tag.setText("");
        final EditText company_et = (EditText) view.findViewById(R.id.company_et);
        view.findViewById(R.id.ok_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = company_et.getText().toString();
                if (StringUtil.isEmpty(message))
                    ToastUtil.showToast(ct, R.string.sure_input_valid);
                else {
                    String map = "{\"ad_confirmstatus\":\"" + "已拒绝" + "\",\"ad_reason\":\"" + message + "\",\"ad_bmanid\":\"" + MyApplication.getInstance().getLoginUserId() + "\",\"ad_sharestatus\":\"" + "未共享" + "\"}";
                    actionOrder(id, map);
                }
            }
        });
        view.findViewById(R.id.not_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                window.dismiss();
            }
        });
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(BBookingDetailActivity.this, 1f);
            }
        });
        window.setBackgroundDrawable(ct.getResources().getDrawable(R.drawable.pop_round_bg));
        window.setTouchable(true);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        PopupWindowHelper.setPopupWindowHW(this, window);
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void sendMessage(final String ownerId, String ownerName, final String objectId, final String text) {
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TEXT);
        message.setContent(text);
        message.setFromUserName(ownerName);
        message.setFromUserId(ownerId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        if (interprect(ownerId, objectId, message)) {
            return;
        }
        Log.i("wang", "send message:" + JSON.toJSONString(message));
        // mHasSend = true;
        Log.d("roamer", "开始发送消息,ChatBottomView的回调 sendmessage");
        message.setPacketId(UUID.randomUUID().toString().replaceAll("-", ""));
        ChatMessageDao.getInstance().saveNewSingleChatMessage(ownerId, objectId, message);
        if (message.getType() == XmppMessage.TYPE_VOICE || message.getType() == XmppMessage.TYPE_IMAGE
                || message.getType() == XmppMessage.TYPE_VIDEO || message.getType() == XmppMessage.TYPE_FILE) {
            if (!message.isUpload()) {
                Log.d("roamer", "去更新服务器的数据");
                // UploadEngine.uploadImFile(mFriend.getUserId(), message, mUploadResponse);

            } else {
                Log.d("roamer", "sendChatMessage....");
                mService.sendChatMessage(objectId, message);
            }
        } else {
            Log.d("roamer", "sendChatMessage");
            mService.sendChatMessage(objectId, message);

        }
    }

    /**
     * 拦截发送的消息
     *
     * @param message
     */
    public boolean interprect(String ownerId, String objectId, ChatMessage message) {
        int len = 0;
        List<Friend> mBlackList = FriendDao.getInstance().getAllBlacklists(ownerId);
        if (mBlackList != null) {
            for (Friend friend : mBlackList) {
                if (friend.getUserId().equals(objectId)) {
                    Toast.makeText(mContext, "已经加入黑名单,无法发送消息", Toast.LENGTH_SHORT).show();
                    len++;
                }
            }
        }
        Log.d("wang", "....kkkkk");
        if (len != 0) {
            // finish();
            ListenerManager.getInstance().notifyMessageSendStateChange(ownerId, objectId,
                    message.get_id(), ChatMessageListener.MESSAGE_SEND_FAILED);
            return true;
        }
        return false;
    }
}

package com.xzjmyk.pm.activity.ui.erp.activity.secretary;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
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

import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.model.LatLng;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.HttpImp;
import com.core.base.OABaseActivity;
import com.core.model.Friend;
import com.core.model.OAConfig;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.model.XmppMessage;
import com.core.net.http.ViewUtil;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.xmpp.CoreService;
import com.core.xmpp.ListenerManager;
import com.core.xmpp.dao.ChatMessageDao;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.ChatMessageListener;
import com.core.xmpp.model.ChatMessage;
import com.modular.apputils.utils.PopupWindowHelper;
import com.modular.booking.model.BookingModel;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.SelectCollisionActivity;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.core.utils.HeightUtils.setListViewHeightBasedOnChildren1;


/**
  * @desc:预约详情
  * @author：Arison on 2017/6/22
  */
public class BookingDetailActivity extends OABaseActivity implements HttpImp,View.OnClickListener {

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
    private LinearLayout ll_refuse;

    BookingModel model;
    private boolean isMenuShuffle=false;
    private CoreService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);
        bindService(CoreService.getIntent(), mConnection, BIND_AUTO_CREATE);
        initView();
    }

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
    private void initView() {
        setTitle(getString(R.string.booking_title));
        
        rvTop = (RelativeLayout) findViewById(R.id.rv_top);
        llLeft = (LinearLayout) findViewById(R.id.ll_left);
        ll_refuse = (LinearLayout) findViewById(R.id.ll_refuse);
        ivMe = (CircleImageView) findViewById(R.id.iv_me);
        tvMe = (TextView) findViewById(R.id.tv_me);
        ivResultInfo = (ImageView) findViewById(R.id.iv_resultInfo);
        llRight = (LinearLayout) findViewById(R.id.ll_right);
        ivTarget = (CircleImageView) findViewById(R.id.iv_target);
        tvTarget = (TextView) findViewById(R.id.tv_target);
        tvTime = (TextView) findViewById(R.id.tv_time);
        tvAddress = (TextView) findViewById(R.id.tv_address);
        tvContent = (TextView) findViewById(R.id.tv_content);
        tv_topic= (TextView) findViewById(R.id.  tv_topic);
        ivResult = (ImageView) findViewById(R.id.iv_result);
        ll_bottom= (LinearLayout) findViewById(R.id.ll_bottom);
        findViewById(R.id.bt_change).setOnClickListener(this);
        findViewById(R.id.bt_cancle).setOnClickListener(this);
        tvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.modular.appworks.NavigationActivity");
                intent.putExtra("toLocation", new LatLng(Float.valueOf(model.getAb_latitude()),Float.valueOf(model.getAb_longitude())));
                startActivityForResult(intent, 0x23);
            }
        });
        Animation animation= AnimationUtils.loadAnimation(this, R.anim.anim_translate_bookingprogress);
        
        
        if (getIntent()!=null){
            model=getIntent().getExtras().getParcelable("model");
            boolean isShared=getIntent().getExtras().getBoolean("isShared");
            tvTime.setText(model.getAb_starttime().substring(0,10)+" "+model.getAb_starttime().substring(11,16)+"-"+
                    model.getAb_endtime().substring(11,16));
            tvAddress.setText(model.getAb_address());
          
            tv_topic.setText(model.getAb_type());
            if (!StringUtil.isEmpty(model.getAb_reason())){
                ll_refuse.setVisibility(View.VISIBLE);
                tvContent.setText(model.getAb_reason());
            }else{
                ll_refuse.setVisibility(View.GONE);
            }
            AvatarHelper.getInstance().display(model.getAb_bmanid(), ivTarget, true, true);
            AvatarHelper.getInstance().display(model.getAb_recordid(), ivMe, true, true);
            ivResultInfo.clearAnimation();
            ll_bottom.setVisibility(View.GONE);
         
            if ("已拒绝".equals(model.getAb_confirmstatus())){
                ivResult.setImageDrawable(mContext. getResources().getDrawable(R.drawable.icon_jujue1));
                ivResultInfo.setImageDrawable(mContext. getResources().getDrawable(R.drawable.icon_jujue));
            }
            if("未确认".equals(model.getAb_confirmstatus())){
                ivResult.setImageDrawable(mContext. getResources().getDrawable(R.drawable.icon_weiqueren));
                ivResultInfo.setImageDrawable(mContext. getResources().getDrawable(R.drawable.icon_send));
                //开启动画
                ll_bottom.setVisibility(View.VISIBLE);
                ivResultInfo.startAnimation(animation);
                if( MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_recordid())){
                    ((Button)findViewById(R.id.bt_change)).setText("变更");
                    ((Button)findViewById(R.id.bt_change)).setTextColor(getResources().getColor(R.color.white));
                    ((Button)findViewById(R.id.bt_cancle)).setText("取消");
                }else{
                    ((Button)findViewById(R.id.bt_change)).setText("确认");
                    ((Button)findViewById(R.id.bt_cancle)).setText("拒绝");
                }
            }
            if("已确认".equals(model.getAb_confirmstatus())){
                ivResult.setImageDrawable(mContext. getResources().getDrawable(R.drawable.icon_yiqueren));
                ivResultInfo.setImageDrawable(mContext. getResources().getDrawable(R.drawable.icon_queren1));
                isMenuShuffle=true;
                if (model.getAb_sharestatus().equals("已共享")){
                    isMenuShuffle=true;
                }
                if( MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_recordid())){
                    ((Button)findViewById(R.id.bt_change)).setText("变更");
                    ((Button)findViewById(R.id.bt_cancle)).setText("取消");
                    ll_bottom.setVisibility(View.VISIBLE);
                  
                }else{
                   // ll_bottom.setVisibility(View.VISIBLE);
                    ((Button)findViewById(R.id.bt_change)).setText("变更");
                    ((Button)findViewById(R.id.bt_cancle)).setText("取消");
                    ((Button)findViewById(R.id.bt_change)).setVisibility(View.GONE);
                    ll_bottom.setVisibility(View.VISIBLE);
                }
            }
            if("已取消".equals(model.getAb_confirmstatus())){
                ivResult.setImageDrawable(mContext. getResources().getDrawable(R.drawable.icon_quxiao3));
                ivResultInfo.setImageDrawable(mContext. getResources().getDrawable(R.drawable.icon_cancle3));
               
            }
            //共享状态
            if (isShared){
                tvTarget.setText(model.getAb_bman());
                tvMe.setText(model.getAb_recordman());
                ll_bottom.setVisibility(View.GONE);
            }else{
                if ( MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_recordid())){
                    //我是发起人
                    tvMe.setText(getString(R.string.me));
                    tvTarget.setText(model.getAb_bman());
                }else{
                    //被约 
                    tvTarget.setText(getString(R.string.me));
                    tvMe.setText(model.getAb_recordman());
                }
            }
        
            if(model.getAb_starttime().compareTo(DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS))<0){
                ll_bottom.setVisibility(View.GONE);
                isMenuShuffle=false;
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_change:
                updateBookingState((Button)view);
                break;
            case R.id.bt_cancle:
                updateBookingState((Button)view);
                break;
        }
    }

    
    Menu mMenu;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu=menu;
        Log.e("isMenuShuffle",isMenuShuffle + "");
        if(isMenuShuffle)
        {
            menu.findItem(R.id.app_about).setVisible(true);
        }else
        {
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
        switch (item.getItemId()) {
            case R.id.app_about:
               Intent intent = new Intent(ct, SelectCollisionActivity.class);
                SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                        .setTitle(getString(R.string.select_share_friend))
                        .setSingleAble(false);
                intent.putExtra(OAConfig.MODEL_DATA, bean);
                startActivityForResult(intent, 0x02);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }
    
    public void updateBookingState(Button button){
        String map="{\"ab_confirmstatus \":\"已确认\",\"ab_bmanid\":\""+model.getAb_bmanid()+"\"}";
        String time=model.getAb_starttime().substring(0,16)+"-"+model.getAb_endtime().substring(11,16);
       if (button.getText().equals("变更")){
           if (MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_bmanid())){
               String content=model.getAb_bman()+"变更了您"+time+"的预约计划";
               sendMessage(model.getAb_bmanid(),model.getAb_bman(),model.getAb_recordid(),content);
           }else{
               String content=model.getAb_recordman()+"变更了您"+time+"的预约计划";
               sendMessage(model.getAb_recordid(),model.getAb_recordman(),model.getAb_bmanid(),content);
           }
          
           Bundle bundle=new Bundle();
           bundle.putParcelable("model",model);
           startActivity(new Intent(mContext,BookingAddActivity.class)
                   .putExtras(bundle));
           return;
       }
        if (button.getText().equals("拒绝")){
            String content=model.getAb_bman()+"拒绝了您"+time+"的预约计划";
            sendMessage(model.getAb_bmanid(),model.getAb_bman(),model.getAb_recordid(),content);
            showPopupWindow(button);
            return;
        }
        showLoading();
        if (button.getText().equals("确认")){
            String content=model.getAb_bman()+"确认了您"+time+"的预约计划";
            sendMessage(model.getAb_bmanid(),model.getAb_bman(),model.getAb_recordid(),content);
            map="{\"ab_confirmstatus \":\"已确认\",\"ab_bmanid\":\""+model.getAb_bmanid()+"\"}";
    
        }
 
        if (button.getText().equals("取消")){
            if (MyApplication.getInstance().mLoginUser.getUserId().equals(model.getAb_bmanid())){
                String content=model.getAb_bman()+"取消了您"+time+"的预约计划";
                sendMessage(model.getAb_bmanid(),model.getAb_bman(),model.getAb_recordid(),content);
            }else{
                String content=model.getAb_recordman()+"取消了您"+time+"的预约计划";
                sendMessage(model.getAb_recordid(),model.getAb_recordman(),model.getAb_bmanid(),content);
            }
            new MaterialDialog.Builder(ct)
                    .title(MyApplication.getInstance().getString(R.string.app_dialog_title))
                    .content("您确定要取消预约计划?")
                    .positiveText(MyApplication.getInstance().getString(R.string.app_dialog_ok))
                    .negativeText(MyApplication.getInstance().getString(R.string.app_dialog_close))
                    .autoDismiss(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            dialog.dismiss();
                            String   map="{\"ab_confirmstatus\":\"已取消\",\"ab_sharestatus\":\"未共享\",\"ab_bmanid\":\"" + model.getAb_bmanid() + "\"}";
                            updateBooking(map);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                           dialog.dismiss();
                            dimssLoading();
                        }
                    }).show();
            
            return;
        }
        
        updateBooking(map);
    }

    private void updateBooking(String map) {
        String url = Constants.IM_BASE_URL() + "user/appUpdateBooking";
        Map<String, Object> params = new HashMap<>();
        params.put("token", MyApplication.getInstance().mAccessToken);
        params.put("map", map);
        params.put("id", model.getAb_id());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x01, null, null, "post");
    }

    private Handler mHandler=new Handler(){
             @Override
             public void handleMessage(Message msg) {
                 dimssLoading();
                 switch (msg.what){
                     case 0x01:
                         try {
                             if (JSON.parseObject(msg.getData().getString("result")).getString("result").equals("true")){
                                 ToastMessage(getString(R.string.make_adeal_success));
                                 startActivity(new Intent(BookingDetailActivity.this,BookingListActivity.class)
                                 .putExtra("curDate",model.getAb_starttime()));
                             }else{
                                 ToastMessage(getString(R.string.make_adeal_failed));
                             }
                         } catch (Exception e) {
                             ToastMessage(getString(R.string.make_adeal_failed));
                         }
                         break;
                     case 0x02:
                         try {
                             if (JSON.parseObject(msg.getData().getString("result")).getString("result").equals("true")){
                                 ToastMessage(getString(R.string.make_adeal_success));
                                // mMenu .findItem(R.id.app_about).setVisible(false);
                             }else{
                                 ToastMessage(getString(R.string.make_adeal_failed));
                               //  mMenu .findItem(R.id.app_about).setVisible(true);
                             }
                         } catch (Exception e) {
                             ToastMessage(getString(R.string.make_adeal_failed));
                            // mMenu .findItem(R.id.app_about).setVisible(true);
                         }
                         break;
                     case Constants.APP_SOCKETIMEOUTEXCEPTION:
                         ToastMessage(getString(R.string.make_adeal_failed));
                         break;
                 }
             }
         };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data==null)return;
        switch (requestCode){
            case 0x02:
                try {
                    List<SelectEmUser> employeesList = data.getParcelableArrayListExtra("data");
                    LogUtil.d("Test",JSON.toJSONString(employeesList));
                    StringBuilder stringBuilder=new StringBuilder("");
                    for(int i=0;i<employeesList.size();i++){
                        if (i==employeesList.size()-1){
                            stringBuilder.append(employeesList.get(i).getImId());
                        }else{
                            stringBuilder.append(employeesList.get(i).getImId());
                            stringBuilder.append(",");
                        }
                    }
                    LogUtil.d("Test",stringBuilder.toString());
                    LogUtil.d("Test",model.getAb_id());
                    if (!StringUtil.isEmpty(stringBuilder.toString())&&!StringUtil.isEmpty(model.getAb_id())){
                        shareBooking(model.getAb_id(),stringBuilder.toString());
                    }else {
                        ToastMessage("共享失败！");
                    }
                } catch (Exception e) {
                    
                }
                break;
        }
    }
    
    
    public void shareBooking(String id,String imids){
        showLoading();
        String map="{\"ab_bmanid\":\"" + model.getAb_bmanid() + "\",\"as_planid\":\""+id+"\",\"as_userids\":\""+imids+"\"}";
        LogUtil.d("HttpLogs","map:"+map);
        String url = Constants.IM_BASE_URL() + "user/appSharePlan";
        Map<String, Object> params = new HashMap<>();
        params.put("token", MyApplication.getInstance().mAccessToken);
        params.put("map", map);
//        params.put("id", model.getAb_id());
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x02, null, null, "post");
    }



    private void sendMessage( final String ownerId,String ownerName, final String objectId,  final String text) {
        ChatMessage message = new ChatMessage();
        message.setType(XmppMessage.TYPE_TEXT);
        message.setContent(text);
        message.setFromUserName(ownerName);
        message.setFromUserId(ownerId);
        message.setTimeSend(CalendarUtil.getSecondMillion());
        if (interprect(ownerId,objectId,message)) {
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
    public boolean interprect(String ownerId,String objectId,ChatMessage message) {
        int len = 0;
       List<Friend> mBlackList = FriendDao.getInstance().getAllBlacklists(ownerId);
        if (mBlackList!=null) {
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }


    private PopupWindow popupWindow = null;

    public void showPopupWindow(View parent) {
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
                    String  map="";
                    switch (position) {
                        case 1:
            
                            break;
                        case 2:
                         //ab_reason  工作繁忙
                            map="{\"ab_confirmstatus\":\"已拒绝\",\"ab_reason\":\"工作繁忙\",\"ab_bmanid\":\""+model.getAb_bmanid()+"\"}";
                            showLoading();
                            updateBooking(map);
                            break;
                        case 3:
                            //ab_reason  没有必要
                            map="{\"ab_confirmstatus\":\"已拒绝\",\"ab_reason\":\"没有必要\",\"ab_bmanid\":\""+model.getAb_bmanid()+"\"}";
                            showLoading();
                            updateBooking(map);
                            break;
                        case 4:
                            //ab_reason  其它
                        
                            showEditerDialog();
                            break;
                    }
                  ;
           
                    
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

    private void showEditerDialog( ) {
        final PopupWindow window = new PopupWindow(ct);
        View view = LayoutInflater.from(ct).inflate(com.modular.booking.R.layout.item_select_remark_pop, null);
        window.setContentView(view);
        TextView title = (TextView) view.findViewById(com.modular.booking.R.id.title_tv);
        TextView company_tag = (TextView) view.findViewById(com.modular.booking.R.id.company_tag);
        title.setText("请输入拒绝理由！");
        company_tag.setText("");
        final EditText company_et = (EditText) view.findViewById(com.modular.booking.R.id.company_et);
        view.findViewById(com.modular.booking.R.id.ok_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = company_et.getText().toString();
                if (StringUtil.isEmpty(message))
                    ToastUtil.showToast(ct, com.modular.booking.R.string.sure_input_valid);
                else {
                    String  map="{\"ab_confirmstatus\":\"已拒绝\",\"ab_reason\":\""+message+"\",\"ab_bmanid\":\""+model.getAb_bmanid()+"\"}";
                    showLoading();
                    updateBooking(map);
                }
            }
        });
        view.findViewById(com.modular.booking.R.id.not_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                window.dismiss();
            }
        });
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(BookingDetailActivity.this, 1f);
            }
        });
        window.setBackgroundDrawable(ct.getResources().getDrawable(com.modular.booking.R.drawable.pop_round_bg));
        window.setTouchable(true);
        window.setOutsideTouchable(false);
        window.setFocusable(true);
        PopupWindowHelper.setPopupWindowHW(this, window);
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }
}

package com.uas.appcontact.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.data.CalendarUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.ui.ProgressDialogUtil;
import com.common.ui.ViewHolder;
import com.core.app.AppConfig;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.EasyFragment;
import com.core.broadcast.MucgroupUpdateUtil;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.Friend;
import com.core.model.HrorgsEntity;
import com.core.net.http.ViewUtil;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.xmpp.CoreService;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.model.Area;
import com.core.xmpp.model.MucRoom;
import com.core.app.AppConstant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appcontact.R;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AutoCreateChatFragment extends EasyFragment {
    private PullToRefreshListView mPullToRefreshListView;
    private List<MucRoom> mMucRooms;
    private MucRoomAdapter mAdapter;
    private int mPageIndex = 0;
    private BaseActivity mActivity;
    private boolean mNeedUpdate = true;
    private Context mContext;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MucgroupUpdateUtil.ACTION_UPDATE)) {
                if (isResumed()) {
                    requestData(true);
                } else {
                    mNeedUpdate = true;
                }
            }
        }
    };

    public AutoCreateChatFragment() {
        mMucRooms = new ArrayList<MucRoom>();
        mAdapter = new MucRoomAdapter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getActivity().registerReceiver(mUpdateReceiver,
                MucgroupUpdateUtil.getUpdateActionFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_icon, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_item) {
            startActivity(new Intent("com.modular.groupchat.SelectContactsActivity" ));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.layout_pullrefresh_list;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
        }
    }

    @SuppressLint("InflateParams")
    private void initView() {
        mContext = getActivity();
        mContext.bindService(CoreService.getIntent(), mServiceConnection, Context.BIND_AUTO_CREATE);
        mConfig = MyApplication.getInstance().getConfig();

        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mPullToRefreshListView.setAdapter(mAdapter);

        View emptyView = LayoutInflater.from(getActivity()).inflate(
                R.layout.view_empty, null);
        mPullToRefreshListView.setEmptyView(emptyView);

        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);

        mPullToRefreshListView
                .setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
                    @Override
                    public void onPullDownToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        requestData(true);
                    }

                    @Override
                    public void onPullUpToRefresh(
                            PullToRefreshBase<ListView> refreshView) {
                        requestData(false);
                    }
                });


        mPullToRefreshListView.getRefreshableView().setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        MucRoom room = mMucRooms.get((int) id);
                        String loginUserId = MyApplication.getInstance().mLoginUser
                                .getUserId();
                        Friend friend = FriendDao.getInstance().getFriend(
                                loginUserId, room.getJid());
                        if (friend == null) {// friend为null，说明之前没加入过该房间，那么调用接口加入
                            // 将房间作为一个好友存到好友表
                            joinRoom(room, loginUserId);
                        } else {
                            interMucChat(room.getJid(), room.getName());
                        }

                    }
                });
    }

    private void interMucChat(String roomJid, String roomName) {
        Intent intent = new Intent("com.modular.message.MucChatActivity");
        intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomName);
        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
        startActivity(intent);
    }

    private void joinRoom(final MucRoom room, final String loginUserId) {
        Log.d("roamer", "joinRoom");
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", room.getId());
        if (room.getUserId() == loginUserId)
            params.put("type", "1");
        else
            params.put("type", "2");
        final ProgressDialog dialog = ProgressDialogUtil.init(getActivity(),
                null, getString(R.string.please_wait));
        ProgressDialogUtil.show(dialog);

        StringJsonArrayRequest<Void> request = new StringJsonArrayRequest<Void>(
                mActivity.mConfig.ROOM_JOIN, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(getActivity());
                ProgressDialogUtil.dismiss(dialog);
            }
        }, new StringJsonArrayRequest.Listener<Void>() {
            @Override
            public void onResponse(ArrayResult<Void> result) {
                boolean success = Result.defaultParser(getActivity(),
                        result, true);
                if (success) {
                    Friend friend = new Friend();// 将房间也存为好友
                    friend.setOwnerId(loginUserId);
                    friend.setUserId(room.getJid());
                    friend.setNickName(room.getName());
                    friend.setDescription(room.getDesc());
                    friend.setRoomFlag(1);
                    friend.setRoomId(room.getId());
                    friend.setRoomCreateUserId(room.getUserId());
                    // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
                    friend.setTimeSend(CalendarUtil.getSecondMillion());
                    friend.setStatus(Friend.STATUS_FRIEND);
                    FriendDao.getInstance()
                            .createOrUpdateFriend(friend);//创建或者更新好友...

                    interMucChat(room.getJid(), room.getName());
                }
                ProgressDialogUtil.dismiss(dialog);
            }
        }, Void.class, params);
        mActivity.addDefaultRequest(request);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        requestData(true);
        if (mNeedUpdate) {
            mNeedUpdate = false;
            mPullToRefreshListView.post(new Runnable() {
                @Override
                public void run() {
                    autoCreateManageGruop();
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (BaseActivity) getActivity();
    }

    private void requestData(final boolean isPullDwonToRefersh) {
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", String.valueOf(mPageIndex));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        if (mActivity.mConfig.ROOM_LIST_HIS == null)
            return;
        StringJsonArrayRequest<MucRoom> request = new StringJsonArrayRequest<MucRoom>(
                mActivity.mConfig.ROOM_LIST_HIS, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(getActivity());
                mPullToRefreshListView.onRefreshComplete();
            }
        }, new StringJsonArrayRequest.Listener<MucRoom>() {
            @Override
            public void onResponse(ArrayResult<MucRoom> result) {

                boolean success = Result.defaultParser(getActivity(),
                        result, true);
                if (success) {
                    mPageIndex++;
                    if (isPullDwonToRefersh) {
                        mMucRooms.clear();
                    }
                    List<MucRoom> datas = result.getData();
                    //只展示类别是1的管理群
                    List<MucRoom> dataTemp = new ArrayList<>();
                    if (datas != null && datas.size() > 0) {
                        for (int i = 0; i < datas.size(); i++) {
                            if (datas.get(i).getCategory() == 1) {
                                dataTemp.add(datas.get(i));
                            }
                        }
                        mMucRooms.addAll(dataTemp);
                    }
                    mAdapter.notifyDataSetChanged();
                    if (isPullDwonToRefersh) {

                    }
                }
                mPullToRefreshListView.onRefreshComplete();
            }
        }, MucRoom.class, params);
        mActivity.addDefaultRequest(request);
    }

    public class MucRoomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMucRooms.size();
        }

        @Override
        public Object getItem(int position) {
            return mMucRooms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(
                        R.layout.row_muc_room, parent, false);
            }
            ImageView avatar_img = ViewHolder.get(convertView, R.id.avatar_img);
            TextView nick_name_tv = ViewHolder.get(convertView,
                    R.id.nick_name_tv);
            TextView content_tv = ViewHolder.get(convertView, R.id.content_tv);
            TextView time_tv = ViewHolder.get(convertView, R.id.time_tv);

            final MucRoom room = mMucRooms.get(position);
            AvatarHelper.getInstance().displayAvatar(room.getUserId(),
                    avatar_img, true);

            nick_name_tv.setText(room.getName());
            time_tv.setText(TimeUtils.getFriendlyTimeDesc(getActivity(),
                    (int) room.getCreateTime()));
            content_tv.setText(room.getDesc());
            return convertView;
        }

    }


    /**
     * 创建管理群
     * by gongpm
     */
    private void autoCreateManageRoom() {
        final DBManager db = new DBManager(mContext);
        final String master = CommonUtil.getSharedPreferences(mContext, "erp_master");
        String emCode = CommonUtil.getSharedPreferences(mContext, "erp_username");
        final List<HrorgsEntity> hlist = db.queryHrorgList(new String[]{master, emCode}, "whichsys=? and or_headmancode=?");
        if (hlist != null && hlist.size() > 0) { //本地有缓存
            if (hlist.get(0).getOr_remark() == 1) {//已经创建群了
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        create(db, master, hlist.get(0));
                    }
                }).start();

                return;
            } else {//还没有创建群
                //TODO 创建群操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        create(db, master, hlist.get(0));
                    }
                }).start();
            }
        } else {  //本地无缓冲
            //TODO  本地无缓存 应该去企业架构获取还存数据
        }
    }

    private void create(DBManager db, String master, HrorgsEntity hrorgsEntity) {
        String roomdesc = "本群为管理群！";

        List<String> inviteUsers = new ArrayList<String>();//群组人员

        int or_subof = hrorgsEntity.getOr_subof();
        int or_id = hrorgsEntity.getOr_id();
        String or_name = CommonUtil.getSharedPreferences(getActivity(), "erp_commpany") + "-" + hrorgsEntity.getOr_name();//群名字
        String or_headmanname = hrorgsEntity.getOr_name();//领导人名字  本群创建者
        String or_headmancode = hrorgsEntity.getOr_headmancode();
        int im_id = 0;
//        int im_id = getEmployeeIMID(db, master, or_headmancode);
//        inviteUsers.add(String.valueOf(im_id));//本群领导人IMID
//            //获取上级部门
//            if (!StringUtil.isEmpty(or_headmanname) && or_subof != 0) {
//                //获取上级领导人的名字以及IMID
//                HrorgsEntity suplist = db.select_getRootData(new String[]{String.valueOf(or_subof), master}, "or_id=? and whichsys=?");
//                if (suplist != null) {
//                    String parentName = suplist.getOr_headmanname();
//                    String parentCode = suplist.getOr_headmancode();
//                    im_id = getEmployeeIMID(db, master, parentCode);
//                    inviteUsers.add(String.valueOf(im_id));////上级领导人IMID
//                }
//            }

        //分部门大于等于1才创建群
        List<HrorgsEntity> chrildlist = db.queryHrorgList(new String[]{String.valueOf(or_id), master}, "or_subof=? and whichsys=?");
        if (!ListUtils.isEmpty(chrildlist)) {
            if (chrildlist.size() >= 1) {
                for (int j = 0; j < chrildlist.size(); j++) {
                    HrorgsEntity chrild = chrildlist.get(j);
                    String chrild_headmancode = chrild.getOr_headmancode();
                    String chrild_headmanname = chrild.getOr_headmanname();
                    if (StringUtil.isEmpty(chrild_headmancode) || StringUtil.isEmpty(chrild_headmanname))
                        continue;
                    im_id = getEmployeeIMID(db, master, chrild_headmancode);
                    if (im_id != 0)
                        inviteUsers.add(String.valueOf(im_id));//分部门IMID
                }
                inviteUsers = removeDuplicate(inviteUsers);//去除重复数据
                createGroupChat(or_headmanname, or_name, null, roomdesc, inviteUsers, or_id);
            }
        }


    }

    /**
     * @desc:自定创建管理群
     * @author：Administrator on 2016/5/13 11:27
     * //遍历组织架构
     * 部门人员人数小于2的群不创建
     * 默认把部门的上级部门的领导人拉进来做群主
     * 群内人数等于：上级部门领导人   小于2的群不创建
     * 组织架构叶子节点的不创建群，比如移动终端群不创建，只创建产品研发群，群里面只放领导人；
     * 根据组织架构表中的or_headmancode 来获取员工表中的员工信息（IMID）
     * 怎样判断创建群的操作已经结束？
     */
    boolean is = false;

    public void autoCreateManageGruop() {
        DBManager db = new DBManager(mContext);
        String master = CommonUtil.getSharedPreferences(mContext, "erp_master");
        String emCode = CommonUtil.getSharedPreferences(mContext, "erp_username");
        //查找本地数据库，管理群存在，则不创建
        if (StringUtil.isEmpty(master)) return;
        List<HrorgsEntity> hlist = db.queryHrorgList(new String[]{master, emCode}, "whichsys=? and or_headmancode=?");
        if (!ListUtils.isEmpty(hlist)) {
            String roomdesc = "本群为管理群！";
            for (int i = 0; i < hlist.size(); i++) {
                if (hlist.get(i).getOr_remark() == 1)
                    continue;
                List<String> inviteUsers = new ArrayList<String>();//群组人员
                int or_id = hlist.get(i).getOr_id();
                String or_name = CommonUtil.getSharedPreferences(getActivity(), "erp_commpany") + "-" + hlist.get(i).getOr_name();//群名字
                String or_headmanname = hlist.get(i).getOr_name();//领导人名字  本群创建者
                String or_headmancode = hlist.get(i).getOr_headmancode();
                if (StringUtil.isEmpty(or_headmanname) || StringUtil.isEmpty(or_headmancode))
                    continue;
                int im_id = getEmployeeIMID(db, master, or_headmancode);
                inviteUsers.add(String.valueOf(im_id));//本群领导人IMID

                //获取上级部门
//                if (!StringUtil.isEmpty(or_headmanname) && or_subof != 0) {
//                    //获取上级领导人的名字以及IMID
//                    HrorgsEntity suplist = db.select_getRootData(new String[]{String.valueOf(or_subof), master}, "or_id=? and whichsys=?");
//                    if (suplist != null) {
//                        String parentName = suplist.getOr_headmanname();
//                        String parentCode = suplist.getOr_headmancode();
//                        im_id = getEmployeeIMID(db, master, parentCode);
//                        inviteUsers.add(String.valueOf(im_id));////上级领导人IMID
//                    }
//                }
                //分部门大于等于1才创建群
                List<HrorgsEntity> chrildlist = db.queryHrorgList(new String[]{String.valueOf(or_id), master}, "or_subof=? and whichsys=?");
                if (!ListUtils.isEmpty(chrildlist)) {
                    if (chrildlist.size() >= 1) {
                        for (int j = 0; j < chrildlist.size(); j++) {
                            HrorgsEntity chrild = chrildlist.get(j);
                            String chrild_headmancode = chrild.getOr_headmancode();
                            String chrild_headmanname = chrild.getOr_headmanname();
                            if (StringUtil.isEmpty(chrild_headmancode) || StringUtil.isEmpty(chrild_headmanname))
                                continue;
                            im_id = getEmployeeIMID(db, master, chrild_headmancode);
                            if (im_id != 0)
                                inviteUsers.add(String.valueOf(im_id));//分部门IMID
                        }
                        inviteUsers = removeDuplicate(inviteUsers);//去除重复数据
                        createGroupChat(or_headmanname, or_name, null, roomdesc, inviteUsers, or_id);
                    }
                }
            }
        } else {

        }
    }

    private int getEmployeeIMID(DBManager db, String master, String or_headmancode) {
        List<EmployeesEntity> elist = db.select_getEmployee(new String[]{or_headmancode, master}, "EM_CODE=? and WHICHSYS=?");
        if (elist != null && elist.size() > 0) {
            int em_imid = elist.get(0).getEm_IMID();
            return em_imid;
        } else {
            return 0;
        }
    }

    /**
     * @注释：去除重复数据
     */
    public List<String> removeDuplicate(List<String> list) {
        HashSet<String> h = new HashSet<String>(list);
        list.clear();
        list.addAll(h);
        return list;
    }


    public ProgressDialog mProgressDialog;
    public AppConfig mConfig;
    private CoreService mXmppService;

    private String roomJid;

    /**
     * @desc:房间名字：组织架构名字；房间描述
     * @author：Administrator on 2016/5/16 9:35
     */
    private void createGroupChat(String nickName, String roomName, String roomSubject, String roomDesc,
                                 List<String> inviteUsers, final int or_id) {
        try {
            roomJid = mXmppService.createMucRoom(nickName, roomName, roomSubject, roomDesc);
        } catch (NullPointerException e) {
            roomJid = mXmppService.createMucRoom(nickName, roomName, "", roomDesc);
        }
        if (TextUtils.isEmpty(roomJid)) {
            ToastUtil.showToast(mContext, R.string.create_room_failed);
            return;
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("jid", roomJid);
        params.put("name", roomName);
        params.put("category", "1");
        params.put("desc", roomDesc);
        params.put("countryId", String.valueOf(Area.getDefaultCountyId()));// 国家Id

        Area area = Area.getDefaultProvince();
        if (area != null) {
            params.put("provinceId", String.valueOf(area.getId()));// 省份Id
        }
        area = Area.getDefaultCity();
        if (area != null) {
            params.put("cityId", String.valueOf(area.getId()));// 城市Id
            area = Area.getDefaultDistrict(area.getId());
            if (area != null) {
                params.put("areaId", String.valueOf(area.getId()));// 城市Id
            }
        }
        UASLocation mUASLocation = UasLocationHelper.getInstance().getUASLocation();
        double latitude = mUASLocation.getLatitude();
        double longitude =mUASLocation.getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));
        params.put("text", JSON.toJSONString(inviteUsers));
        ProgressDialogUtil.show(mProgressDialog);
        StringJsonObjectRequest<MucRoom> request = new StringJsonObjectRequest<MucRoom>(mConfig.ROOM_ADD,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError arg0) {
                        ProgressDialogUtil.dismiss(mProgressDialog);
                        ToastUtil.showErrorNet(mContext);
                    }
                }, new StringJsonObjectRequest.Listener<MucRoom>() {
            @Override
            public void onResponse(ObjectResult<MucRoom> result) {
                boolean parserResult = Result.defaultParser(mContext, result, true);
                if (parserResult && result.getData() != null) {
                    mPullToRefreshListView.setPullDownRefreshing(200);//加载列表
                    updateCreateFalgToERP(or_id);
                }
                ProgressDialogUtil.dismiss(mProgressDialog);
            }
        }, MucRoom.class, params);
        String HASHCODE = Integer.toHexString(this.hashCode()) + "@";
        MyApplication.getInstance().getFastVolley().addDefaultRequest(HASHCODE, request);
    }


    /**
     * @desc:自动创建群后，避免重复，需要更新标识
     * @author：Administrator on 2016/5/17 16:59
     */
    public void updateCreateFalgToERP(int or_id) {
        String url = CommonUtil.getAppBaseUrl(mContext) + "/mobile/update_hrorgmobile.action";
        Map<String, Object> params = new HashMap<>();
        params.put("orid", or_id);
        params.put("kind", 1);
        params.put("sessionId", CommonUtil.getSharedPreferences(mContext, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));
        ViewUtil.httpSendRequest(mContext, url, params, handler, headers, 1, null, null, "get");
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String result = msg.getData().getString("result");
                    Log.i("Arison", "result:" + result);

                    if (JSONUtil.validate(result)) {
                        String or_id = JSON.parseObject(result).getString("or_id");
                        if (!StringUtil.isEmpty(or_id)) {
                            DBManager db = new DBManager(mContext);
                            String master = CommonUtil.getSharedPreferences(mContext, "erp_master");
                            db.updateHrogrRemark(Integer.valueOf(or_id), 1, master);
                            ViewUtil.ToastMessage(mContext, "系统创建您的管理群成功！");
                        }
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    result = msg.getData().getString("result");
                    ViewUtil.ToastMessage(mContext, result);
                    break;
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mXmppService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mXmppService = ((CoreService.CoreServiceBinder) service).getService();
        }
    };
}

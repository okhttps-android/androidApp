package com.xzjmyk.pm.activity.ui.groupchat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.data.CalendarUtil;
import com.common.data.ListUtils;
import com.common.data.NumberUtils;
import com.common.file.PropertiesUtil;
import com.common.system.DisplayUtil;
import com.common.ui.ImageUtil;
import com.common.ui.ProgressDialogUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.UserDao;
import com.core.model.Friend;
import com.core.model.User;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.ClearEditText;
import com.core.xmpp.CoreService;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.model.Area;
import com.core.xmpp.model.MucRoom;
import com.core.xmpp.model.MucRoomSimple;
import com.core.xmpp.utils.CardcastUiUpdateUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.uas.applocation.UasLocationHelper;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.broadcast.MucgroupUpdateUtil;
import com.xzjmyk.pm.activity.ui.erp.net.HttpUtil;
import com.xzjmyk.pm.activity.ui.message.MucChatActivity;
import com.xzjmyk.pm.activity.util.im.Constants;
import com.xzjmyk.pm.activity.util.imageloader.BitmapUtil;
import com.xzjmyk.pm.activity.view.HorizontalListView;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.xzjmyk.pm.activity.ui.groupchat
 * @作者:王阳
 * @创建时间: 2015年10月16日 上午11:29:13
 * @描述: 选择联系人创建群的界面
 * @SVN版本号: $Rev$
 * @修改人: $Author$
 * @修改时间: $Date$
 * @修改的内容: 增加房间名和房间描述的字数限制
 */
public class SelectContactsActivity extends BaseActivity {

    private final int LAST_ICON = -1;

    private RecyclerView mListView;
    private HorizontalListView mHorizontalListView;
    private Button mOkBtn;

    private List<Friend> mFriendList;
    private RecycAdapter mAdapter;
    private List<Integer> mSelectPositions;
    private HorListViewAdapter mHorAdapter;
    public ProgressDialog mProgressDialog;

    private String mLoginUserId;
    private boolean mBind;
    private CoreService mXmppService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        mFriendList = new ArrayList<Friend>();
        mAdapter = new RecycAdapter();
        mSelectPositions = new ArrayList<>();
        mSelectPositions.add(LAST_ICON);// 增加一个虚线框的位置
        mHorAdapter = new HorListViewAdapter();
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        initView();

        mBind = bindService(CoreService.getIntent(), mServiceConnection, BIND_AUTO_CREATE);
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBind) {
            unbindService(mServiceConnection);
        }
    }

    private void initView() {
        setTitle(R.string.select_contacts);
        mListView = (RecyclerView) findViewById(R.id.list_view);
        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_list_view);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        mListView.setAdapter(mAdapter);
        mHorizontalListView.setAdapter(mHorAdapter);
        //TODO     mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
        //TODO
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//                if (hasSelected(position)) {
//                    removeSelect(position);
//                } else {
//                    addSelect(position);
//                }
//            }
//        });

        mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position == mSelectPositions.size() - 1) {
                    return;
                }
                mSelectPositions.remove(position);
                mAdapter.notifyDataSetChanged();
                mHorAdapter.notifyDataSetInvalidated();
//TODO                mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
            }
        });

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCreateGroupChatDialog();
            }
        });

        mProgressDialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait), false, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Friend> userInfos = FriendDao.getInstance().getAllContacts(mLoginUserId);
        //排除自己
        if (!ListUtils.isEmpty(userInfos)) {
            for (int i = 0; i < userInfos.size(); i++) {
                if (userInfos.get(i).getUserId().equals(MyApplication.getInstance().mLoginUser.getUserId())) {
                    userInfos.remove(userInfos.get(i));
                }
            }
        }
        if (userInfos != null) {
            mFriendList.clear();
            mFriendList.addAll(userInfos);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void addSelect(int position) {
        if (!hasSelected(position)) {
            mSelectPositions.add(0, position);
            mAdapter.notifyDataSetChanged();
            mHorAdapter.notifyDataSetInvalidated();
            //TODO         mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
        }
    }

    private boolean hasSelected(int position) {
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i) == position) {
                return true;
            } else if (i == mSelectPositions.size() - 1) {
                return false;
            }
        }
        return false;
    }

    private void removeSelect(int position) {
        mSelectPositions.remove(Integer.valueOf(position));
        mAdapter.notifyDataSetChanged();
        mHorAdapter.notifyDataSetInvalidated();
        //TODO    mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
    }


    private class RecycAdapter extends RecyclerView.Adapter<RecycAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AvatarHelper.getInstance().displayAvatar(mFriendList.get(position).getUserId(), holder.avatarImg, true);
            holder.userNameTv.setText(mFriendList.get(position).getNickName());
            if (mSelectPositions.contains(Integer.valueOf(position))) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);

            }
        }

        @Override
        public int getItemCount() {
            return mFriendList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView avatarImg;
            TextView userNameTv;
            CheckBox checkBox;

            public ViewHolder(View itemView) {
                super(itemView);
                avatarImg = (ImageView) itemView.findViewById(R.id.avatar_img);
                userNameTv = (TextView) itemView.findViewById(R.id.user_name_tv);
                checkBox = (CheckBox) itemView.findViewById(R.id.check_box);
            }
        }
    }


//    private class ListViewAdapter extends BaseAdapter {
//        @Override
//        public int getCount() {
//            return mFriendList.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return mFriendList.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts, parent, false);
//            }
//            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
//            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
//            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);
//            AvatarHelper.getInstance().displayAvatar(mFriendList.get(position).getUserId(), avatarImg, true);
//            userNameTv.setText(mFriendList.get(position).getNickName());
//            checkBox.setChecked(false);
//            if (mSelectPositions.contains(Integer.valueOf(position))) {
//                checkBox.setChecked(true);
//            }
//            return convertView;
//        }
//
//    }

    private class HorListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mSelectPositions.size();
        }

        @Override
        public Object getItem(int position) {
            return mSelectPositions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(mContext);
                int size = DisplayUtil.dip2px(mContext, 37);
                AbsListView.LayoutParams param = new AbsListView.LayoutParams(size, size);
                convertView.setLayoutParams(param);
            }
            ImageView imageView = (ImageView) convertView;
            int selectPosition = mSelectPositions.get(position);
            if (selectPosition == -1) {
                imageView.setImageResource(R.drawable.dot_avatar);
            } else {
                if (selectPosition >= 0 && selectPosition < mFriendList.size()) {
                    AvatarHelper.getInstance().displayAvatar(mFriendList.get(selectPosition).getUserId(), imageView,
                            true);
                }
            }
            return convertView;
        }
    }

    /**
     * @功能:创建群对话框
     * @author:Arisono
     * @param:
     * @return:
     */
    private void showCreateGroupChatDialog() {
        if (mXmppService == null || !mXmppService.isMucEnable()) {
            ToastUtil.showToast(mContext, R.string.service_start_failed);
            return;
        }
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_create_muc_room, null);
        final ClearEditText roomNameEdit = (ClearEditText) rootView.findViewById(R.id.room_name_edit);
        TextView tvName = (TextView) rootView.findViewById(R.id.creater_name);
        TextView tvNum = (TextView) rootView.findViewById(R.id.tv_num);
        User user = UserDao.getInstance().getUserByUserId(mLoginUserId);
        tvNum.setText(mSelectPositions.size() + "/1000");
        tvName.setText(MyApplication.getInstance().mLoginUser.getNickName());
        // final EditText roomSubjectEdit = (EditText)
        // rootView.findViewById(R.id.room_subject_edit);
        final ClearEditText roomDescEdit = (ClearEditText) rootView.findViewById(R.id.room_desc_edit);
        final Button sure_btn = (Button) rootView.findViewById(R.id.sure_btn);

//        ToastUtil.addEditTextNumChanged(SelectContactsActivity.this, roomNameEdit, 8);// 设置EditText的字数限制
//        ToastUtil.addEditTextNumChanged(SelectContactsActivity.this, roomDescEdit, 20);
        final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.create_room).setView(rootView)
                .create();

        sure_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomName = roomNameEdit.getText().toString().trim();//防止空,或者输入空格
                if (TextUtils.isEmpty(roomName) || roomName.length() > 8) {
                    ToastUtil.showToast(mContext, R.string.room_name_empty_error);
                    return;
                }

                // String roomSubject = roomSubjectEdit.getText().toString();
                String roomDesc = roomDescEdit.getText().toString();
                if (TextUtils.isEmpty(roomName)) {
                    ToastUtil.showToast(mContext, R.string.room_des_empty_error);
                    return;
                }
                createGroupChat(roomName, null, roomDesc);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void createGroupChat(final String roomName, String roomSubject, final String roomDesc) {
        String nickName = MyApplication.getInstance().mLoginUser.getNickName();
        final String roomJid = mXmppService.createMucRoom(nickName, roomName, roomSubject, roomDesc);
        if (TextUtils.isEmpty(roomJid)) {
            ToastUtil.showToast(mContext, R.string.create_room_failed);
            return;
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("jid", roomJid);
        params.put("name", roomName);
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

        double latitude = UasLocationHelper.getInstance().getUASLocation().getLatitude();
        double longitude = UasLocationHelper.getInstance().getUASLocation().getLongitude();
        if (latitude != 0)
            params.put("latitude", String.valueOf(latitude));
        if (longitude != 0)
            params.put("longitude", String.valueOf(longitude));

        final List<String> inviteUsers = new LinkedList<String>();
        // 邀请好友
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i) == -1) {
                continue;
            }
            String userId = mFriendList.get(mSelectPositions.get(i)).getUserId();
            inviteUsers.add(userId);
        }
        //开启线程处理合成群头像操作
        final Thread uploadImageTask = new Thread(new Runnable() {
            @Override
            public void run() {
                uploadIsEnd = false;//开始上传任务
                photoId = NumberUtils.generateNumber2();//八位不重复随机数
                inviteUsers.add(0, MyApplication.getInstance().mLoginUser.getUserId());
                image = createChatImage(inviteUsers);
                Message msg = mhandler.obtainMessage();
                msg.getData().putString("roomjId", roomJid);
                msg.what = uploadFile;
                mhandler.sendMessage(msg);
            }
        });
        uploadImageTask.start();
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
                    while (uploadImageTask.isAlive()) {
                        //上传线程正在运行
                        // Log.i("Arison","SelectContactsActivity:onResponse:440:上传是否结束："+uploadIsEnd);
                    }
//                    while (!uploadIsEnd){
//                        //上传线程正在运行
//                        Log.i("Arison","SelectContactsActivity:onResponse:440:上传是否结束："+uploadIsEnd);
//                    }

                    createRoomSuccess(result.getData().getId(), roomJid, roomName, roomDesc, photoId);
                    ProgressDialogUtil.dismiss(mProgressDialog);
                }

            }
        }, MucRoom.class, params);
        addDefaultRequest(request);
    }

    private final int uploadFile = 1;
    File image;
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case uploadFile:
                    String roomjId = msg.getData().getString("roomjId");
                    Log.i("Arison", "SelectContactsActivity:handleMessage:466:handler uploadFile=" + uploadFile);
                    uploadAvatar(image, roomjId);
                    break;
            }

        }
    };


    private void createRoomSuccess(String roomId, String roomJid, String roomName, String roomDesc, String photoId) {
        Log.i("Arison", "photoId=" + photoId);
        Friend friend = new Friend();// 将房间也存为好友
        friend.setOwnerId(mLoginUserId);
        friend.setUserId(roomJid);
        friend.setNickName(roomName);
        friend.setDescription(roomDesc);
        friend.setRoomFlag(1);
        friend.setRoomId(roomId);
//        friend.setRoomCreateUserId(mLoginUserId);
        friend.setRoomCreateUserId(photoId);
        // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
        friend.setTimeSend(CalendarUtil.getSecondMillion() - 5);
        friend.setStatus(Friend.STATUS_FRIEND);
        FriendDao.getInstance().createOrUpdateFriend(friend);
        // 更新名片盒（可能需要更新）
        CardcastUiUpdateUtil.broadcastUpdateUi(this);
        // 更新群聊界面
        MucgroupUpdateUtil.broadcastUpdateUi(this);
//        MsgBroadcast.broadcastMsgUiUpdate(this);

        MucRoomSimple mucRoomSimple = new MucRoomSimple();
        mucRoomSimple.setId(roomId);
        mucRoomSimple.setJid(roomJid);
        mucRoomSimple.setName(roomName);
        mucRoomSimple.setDesc(roomDesc);
        mucRoomSimple.setUserId(mLoginUserId);
        mucRoomSimple.setTimeSend(CalendarUtil.getSecondMillion() - 5);
        String reason = JSON.toJSONString(mucRoomSimple);
        Log.d("roamer", "reason:" + reason);
        // 邀请好友
        String[] noticeFriendList = new String[mSelectPositions.size()];
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i) == -1) {
                continue;
            }
            String firendUserId = mFriendList.get(mSelectPositions.get(i)).getUserId();
            noticeFriendList[i] = firendUserId;
            mXmppService.invite(roomJid, firendUserId, reason);
        }

        Intent intent = new Intent(this, MucChatActivity.class);
        intent.putExtra(AppConstant.EXTRA_USER_ID, roomJid);
        intent.putExtra(AppConstant.EXTRA_NICK_NAME, roomName);
        intent.putExtra(AppConstant.EXTRA_IS_GROUP_CHAT, true);
        intent.putExtra(Constants.GROUP_JOIN_NOTICE, noticeFriendList);
        startActivity(intent);
        finish();
    }

    /**
     * @功能:合成九宫图头像--耗时操作
     * @author:Arisono
     * @param:
     * @return:
     */
    private File createChatImage(List<String> inviteUsers) {
        File file = null;
        if (!ListUtils.isEmpty(inviteUsers)) {
            int size = inviteUsers.size() > 9 ? 9 : inviteUsers.size();
            List<MyBitmapEntity> mEntityList = getBitmapEntitys(size);
            Bitmap mBitmaps[] = new Bitmap[size];
            for (int i = 0; i < size; i++) {
                String url = AvatarHelper.getAvatarUrl(inviteUsers.get(i), false);
                Bitmap nextBitmap = null;
                String filepath = HttpUtil.download(url, com.common.file.FileUtils.getSDRoot() + "/uu/chat/head" + i + ".png");
                Log.i("Arison", "SelectContactsActivity:createChatImage:493:filepath:" + filepath);
                nextBitmap = ImageUtil.compressBitmapWithFilePath(filepath, 300, 300);
                if (nextBitmap == null) {
                    nextBitmap = ImageUtil.compressBitmapWithResources(this, R.drawable.avatar_normal, 300, 300);
                }
                Bitmap tempBitmap = ThumbnailUtils.extractThumbnail(nextBitmap, (int) mEntityList
                        .get(i).width, (int) mEntityList.get(i).width);
                Log.i("Arison", "createChatImage:499:tempBitmap:" + url);
                Log.i("Arison", "createChatImage:499:tempBitmap:" + nextBitmap);
                mBitmaps[i] = tempBitmap;
            }

            Bitmap combineBitmap = BitmapUtil.getCombineBitmaps(mEntityList, mBitmaps);

            try {
                file = BitmapUtil.saveFile(combineBitmap, "chatImage.png");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }


    private void uploadAvatar(File file, final String roomjId) {
        if (!file.exists()) {// 文件不存在
            return;
        }
        RequestParams params = new RequestParams();
//        final String loginUserId = MyApplication.getInstance().mLoginUser.getUserId();
//        long time=System.currentTimeMillis();
        params.put("userId", photoId);//群主id+当前系统时间
        try {
            params.put("file1", file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(MyApplication.getInstance().getConfig().AVATAR_UPLOAD_URL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                boolean success = false;
                if (arg0 == 200) {
                    Result result = null;
                    try {
                        result = JSON.parseObject(new String(arg2), Result.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (result != null && result.getResultCode() == Result.CODE_SUCCESS) {
                        success = true;
                    }
                }
                if (success) {
                    Log.i("Arison", "SelectContactsActivity:onSuccess:567:" + "上次头像成功！");
                    ToastUtil.showToast(SelectContactsActivity.this, R.string.upload_avatar_success);
                    //更新服务器
                    updateIMChatImageId(roomjId, photoId);
                } else {
                    Log.i("Arison", "SelectContactsActivity:onSuccess:567:" + "上次头失败！");
                    ToastUtil.showToast(SelectContactsActivity.this, R.string.upload_avatar_failed);
                }
                finish();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                ToastUtil.showToast(SelectContactsActivity.this, R.string.upload_avatar_failed);
                Log.i("Arison", "SelectContactsActivity:onSuccess:567:" + "上次头失败！");
            }
        });
    }

    private String photoId;
    private static boolean uploadIsEnd = true;

    /**
     * @功能:上传文件后，需要上传更新图像id
     * @author:Arisono
     * @param:
     * @return:
     */
    public void updateIMChatImageId(String roomjId, String photoId) {
        String url = MyApplication.getInstance().getConfig().apiUrl + "room/setRelationGroupPhoto";
        final String requestTag = "loginManagerSystem";
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", roomjId);
        params.put("photoid", photoId);
        StringJsonObjectRequest<String> mRequest = new StringJsonObjectRequest<String>(
                Request.Method.GET, url,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                    }
                },
                new StringJsonObjectRequest.Listener<String>() {
                    @Override
                    public void onResponse(ObjectResult<String> result) {
                        Log.i("Arison", "更新头像id result:" + result.getResultData());
                    }
                }, String.class, params, true);
        mRequest.setTag(requestTag);
        addDefaultRequest(mRequest);
    }


    public static class MyBitmapEntity {
        public float x;
        public float y;
        public float width;
        public float height;
        public static int devide = 1;
        public int index = -1;

        @Override
        public String toString() {
            return "MyBitmap [x=" + x + ", y=" + y + ", width=" + width
                    + ", height=" + height + ", devide=" + devide + ", index="
                    + index + "]";
        }
    }


    private List<MyBitmapEntity> getBitmapEntitys(int count) {
        List<MyBitmapEntity> mList = new LinkedList<MyBitmapEntity>();
        String value = PropertiesUtil.readData(this, String.valueOf(count),
                R.raw.data);
        String[] arr1 = value.split(";");
        int length = arr1.length;
        for (int i = 0; i < length; i++) {
            String content = arr1[i];
            String[] arr2 = content.split(",");
            MyBitmapEntity entity = null;
            for (int j = 0; j < arr2.length; j++) {
                entity = new MyBitmapEntity();
                entity.x = Float.valueOf(arr2[0]);
                entity.y = Float.valueOf(arr2[1]);
                entity.width = Float.valueOf(arr2[2]);
                entity.height = Float.valueOf(arr2[3]);
            }
            mList.add(entity);
        }
        return mList;
    }
}

package com.xzjmyk.pm.activity.ui.message;

import android.annotation.SuppressLint;
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
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.NumberUtils;
import com.common.file.PropertiesUtil;
import com.common.system.DisplayUtil;
import com.common.ui.ImageUtil;
import com.common.ui.ProgressDialogUtil;
import com.common.ui.ViewHolder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.model.Friend;
import com.core.xmpp.model.MucRoomSimple;
import com.core.xmpp.utils.CardcastUiUpdateUtil;
import com.core.broadcast.MsgBroadcast;
import com.xzjmyk.pm.activity.broadcast.MucgroupUpdateUtil;
import com.core.xmpp.dao.FriendDao;
import com.core.utils.helper.AvatarHelper;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.net.HttpUtil;
import com.xzjmyk.pm.activity.ui.groupchat.SelectContactsActivity;
import com.core.utils.ToastUtil;
import com.xzjmyk.pm.activity.util.imageloader.BitmapUtil;
import com.xzjmyk.pm.activity.view.HorizontalListView;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.xmpp.CoreService;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AddContactsActivity extends BaseActivity {

    private final int LAST_ICON = -1;

    private ListView mListView;
    private HorizontalListView mHorizontalListView;
    private Button mOkBtn;

    private List<Friend> mFriendList;
    private ListViewAdapter mAdapter;
    private List<Integer> mSelectPositions;
    private HorListViewAdapter mHorAdapter;

    private int Id;
    private String mRoomId;
    private String mRoomJid;
    private String mRoomDes;
    private String mRoomName;
    private List<String> mExistIds;

    private String mLoginUserId;

    private boolean mXmppBind;
    private CoreService mCoreService;
    private String photoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);
        if (getIntent() != null) {
            Id = getIntent().getIntExtra("id", 0);
            mRoomId = getIntent().getStringExtra("roomId");
            mRoomJid = getIntent().getStringExtra("roomJid");
            mRoomDes = getIntent().getStringExtra("roomDes");
            mRoomName = getIntent().getStringExtra("roomName");
            String ids = getIntent().getStringExtra("exist_ids");
            mExistIds = JSON.parseArray(ids, String.class);
        }

        mFriendList = new ArrayList<Friend>();
        mAdapter = new ListViewAdapter();
        mSelectPositions = new ArrayList<Integer>();
        mSelectPositions.add(LAST_ICON);// 增加一个虚线框的位置
        mHorAdapter = new HorListViewAdapter();

        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        initView();
        // 绑定服务
        mXmppBind = bindService(CoreService.getIntent(), mXmppServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mXmppBind) {
            unbindService(mXmppServiceConnection);
        }
    }

    private ServiceConnection mXmppServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCoreService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCoreService = ((CoreService.CoreServiceBinder) service).getService();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Friend> userInfos = FriendDao.getInstance().getAllContacts(mLoginUserId);
        if (userInfos != null) {
            mFriendList.clear();
            for (int i = 0; i < userInfos.size(); i++) {
                boolean isIn = isExist(userInfos.get(i));
                if (isIn) {
                    userInfos.remove(i);
                    i--;
                } else {
                    mFriendList.add(userInfos.get(i));
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 是否存在已经在那个房间的好友
     *
     * @param
     * @return
     */
    private boolean isExist(Friend friend) {
        for (int i = 0; i < mExistIds.size(); i++) {
            if (mExistIds.get(i) == null) {
                continue;
            }
            if (friend.getUserId().equals(mExistIds.get(i))) {
                return true;
            }
        }
        return false;
    }

    private void initView() {
        setTitle(R.string.select_contact);
        mListView = (ListView) findViewById(R.id.list_view);
        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_list_view);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        mListView.setAdapter(mAdapter);
        mHorizontalListView.setAdapter(mHorAdapter);
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (hasSelected(position)) {
                    removeSelect(position);
                } else {
                    addSelect(position);
                }
            }
        });

        mHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if (position == mSelectPositions.size() - 1) {
                    return;
                }
                mSelectPositions.remove(position);
                mAdapter.notifyDataSetInvalidated();
                mHorAdapter.notifyDataSetInvalidated();
                mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
            }
        });

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteFriend();
            }
        });

        mProgressDialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait));
    }

    public ProgressDialog mProgressDialog;

    private void addSelect(int position) {
        if (!hasSelected(position)) {
            mSelectPositions.add(0, position);
            mAdapter.notifyDataSetInvalidated();
            mHorAdapter.notifyDataSetInvalidated();
            mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
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

    @SuppressLint("StringFormatMatches")
    private void removeSelect(int position) {
        mSelectPositions.remove(Integer.valueOf(position));
        mAdapter.notifyDataSetInvalidated();
        mHorAdapter.notifyDataSetInvalidated();
        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));
    }

    private class ListViewAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mFriendList.size();
        }

        @Override
        public Object getItem(int position) {
            return mFriendList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_contacts, parent, false);
            }
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);
            CheckBox checkBox = ViewHolder.get(convertView, R.id.check_box);

            AvatarHelper.getInstance().displayAvatar(mFriendList.get(position).getUserId(), avatarImg, true);
            userNameTv.setText(mFriendList.get(position).getNickName());
            checkBox.setChecked(false);
            if (mSelectPositions.contains(Integer.valueOf(position))) {
                checkBox.setChecked(true);
            }
            return convertView;
        }

    }

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
                    AvatarHelper.getInstance().displayAvatar(mFriendList.get(selectPosition).getUserId(), imageView, true);
                }
            }
            return convertView;

        }

    }

    /**
     * 邀请好友
     * 原成员小于等于9需要重新上传图片的逻辑
     */
    private void inviteFriend() {
        if (mSelectPositions.size() <= 1) {
            setResult(RESULT_OK, new Intent());
            finish();
            return;
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", mRoomId);

        final List<String> inviteUsers = new ArrayList<String>();
        // 邀请好友
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i) == -1) {
                continue;
            }
            String userId = mFriendList.get(mSelectPositions.get(i)).getUserId();
            inviteUsers.add(userId);
        }

        //开启线程处理合成群头像操作
        final Thread uploadImageTask =
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//						uploadIsEnd=false;//开始上传任务
                        photoId = NumberUtils.generateNumber2();//八位不重复随机数
                        /*inviteUsers.add(0, MyApplication.getInstance().mLoginUser.getUserId());*/
                        inviteUsers.addAll(0, mExistIds);
                        image = createChatImage(inviteUsers);
                        if (image == null) return;
                        Message msg = mhandler.obtainMessage();
                        msg.getData().putString("roomjId", mRoomJid);
                        msg.what = uploadFile;
                        mhandler.sendMessage(msg);
                        uploadImageTaskOk = true;
                    }
                });

        uploadImageTask.start();

        params.put("text", JSON.toJSONString(inviteUsers));

        ProgressDialogUtil.show(mProgressDialog);
        StringJsonObjectRequest<Void> request = new StringJsonObjectRequest<Void>(mConfig.ROOM_MEMBER_UPDATE, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ProgressDialogUtil.dismiss(mProgressDialog);
                ToastUtil.showErrorNet(mContext);
            }
        }, new StringJsonObjectRequest.Listener<Void>() {
            @Override
            public void onResponse(ObjectResult<Void> result) {
                boolean parserResult = Result.defaultParser(mContext, result, true);
                if (parserResult) {
//					while (uploadImageTask.isAlive()){	//上传线程正在运行
//						//Log.i("Arison","SelectContactsActivity:onResponse:440:上传是否结束："+uploadImageTask.isAlive());
//					}
                    requestOk = true;
                    createRoomSuccess(mRoomId, mRoomJid, mRoomName, mRoomDes, photoId);
//					inviteFriendSuccess();
                }
            }
        }, Void.class, params);
        addDefaultRequest(request);
    }

    private boolean uploadImageTaskOk = false;
    private boolean requestOk = false;

    private void createRoomSuccess(String roomId, String roomJid, String roomName, String roomDesc, String photoId) {
        if (!uploadImageTaskOk || !requestOk) return;
        ProgressDialogUtil.dismiss(mProgressDialog);
        Log.i("Arison", "AddContactsActivity:createRoomSuccess:403:mLoginUserId=" +
                mLoginUserId + "roomId=" + roomId + "roomJid=" + roomJid + "id:" + Id);
        Friend friend = new Friend();// 将房间也存为好友
        friend.set_id(Id);
        friend.setOwnerId(mLoginUserId);
        friend.setUserId(roomJid);
        friend.setNickName(roomName);
        friend.setDescription(roomDesc);
        friend.setContent("更新头像成功！");
        friend.setRoomFlag(1);
        friend.setRoomId(roomId);
//        friend.setRoomCreateUserId(mLoginUserId);
        Log.i("Arison", "AddContactsActivity:createRoomSuccess:411:photoId=" + photoId);
        friend.setRoomCreateUserId(photoId);
        // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
        friend.setTimeSend(CalendarUtil.getSecondMillion());
        friend.setStatus(Friend.STATUS_FRIEND);
        boolean isUpdateSuce = FriendDao.getInstance().createOrUpdateFriend(friend);
        Log.i("Arison", "AddContactsActivity:createRoomSuccess:417:isUpdateSuce=" + isUpdateSuce);
        // 更新名片盒（可能需要更新）
        CardcastUiUpdateUtil.broadcastUpdateUi(this);
        // 更新群聊界面
        MucgroupUpdateUtil.broadcastUpdateUi(this);
        MsgBroadcast.broadcastMsgUiUpdate(this);

        MucRoomSimple mucRoomSimple = new MucRoomSimple();
        mucRoomSimple.setId(roomId);
        mucRoomSimple.setJid(roomJid);
        mucRoomSimple.setName(roomName);
        mucRoomSimple.setDesc(roomDesc);
        mucRoomSimple.setUserId(mLoginUserId);
        mucRoomSimple.setTimeSend(CalendarUtil.getSecondMillion());
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
            mCoreService.invite(roomJid, firendUserId, reason);
        }
        setResult(RESULT_OK, new Intent());
        finish();
    }

    private void inviteFriendSuccess() {
        MucRoomSimple mucRoomSimple = new MucRoomSimple();
        mucRoomSimple.setId(mRoomId);
        mucRoomSimple.setJid(mRoomJid);
        mucRoomSimple.setName(mRoomName);
        mucRoomSimple.setDesc(mRoomDes);
        mucRoomSimple.setUserId(mLoginUserId);
        mucRoomSimple.setTimeSend(CalendarUtil.getSecondMillion());
        String reason = JSON.toJSONString(mucRoomSimple);
        // 邀请好友
        for (int i = 0; i < mSelectPositions.size(); i++) {
            if (mSelectPositions.get(i) == -1) {
                continue;
            }
            String firendUserId = mFriendList.get(mSelectPositions.get(i)).getUserId();

            mCoreService.invite(mRoomJid, firendUserId, reason);
            /*Intent broadcast=new Intent(Constants.CHAT_MESSAGE_DELETE_ACTION);
            broadcast.putExtra(Constants.GROUP_JOIN_NOTICE_FRIEND_ID,firendUserId);
			broadcast.putExtra(AppConstant.EXTRA_USER_ID, mRoomJid);
			this.sendBroadcast(broadcast);*/
        }
        setResult(RESULT_OK);

        finish();
    }


    /**
     * @功能:合成九宫图头像--耗时操作
     * @author:Arisono
     * @param:
     * @return:
     */
    private File createChatImage(List<String> inviteUsers) {
        long startTime = System.currentTimeMillis();
        Log.i("Arison", "SelectContactsActivity:createChatImage:473:开始合成图片:" + DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS));
        File file = null;
        if (!ListUtils.isEmpty(inviteUsers)) {
            int size = inviteUsers.size() > 9 ? 9 : inviteUsers.size();
            List<SelectContactsActivity.MyBitmapEntity> mEntityList = getBitmapEntitys(size);
            Bitmap mBitmaps[] = new Bitmap[size];
            for (int i = 0; i < size; i++) {
                if (System.currentTimeMillis() - startTime > 15 * 1000 && size - i > 2) {
                    ProgressDialogUtil.dismiss(mProgressDialog);
                    ToastUtil.showToast(ct, R.string.networks_out);
                    return file;
                }
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
        Log.i("Arison", "SelectContactsActivity:createChatImage:473:合成图片结束:" + DateFormatUtil.long2Str(DateFormatUtil.YMD_HMS) + ";file=" + file);
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
                    ToastUtil.showToast(AddContactsActivity.this, R.string.upload_avatar_success);
                    //更新服务器
                    updateIMChatImageId(roomjId, photoId);
                } else {
                    Log.i("Arison", "SelectContactsActivity:onSuccess:567:" + "上次头失败！");
                    ToastUtil.showToast(AddContactsActivity.this, R.string.upload_avatar_failed);
                }
                finish();
            }

            @Override
            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                ToastUtil.showToast(AddContactsActivity.this, R.string.upload_avatar_failed);
                Log.i("Arison", "SelectContactsActivity:onSuccess:567:" + "上次头失败！");
            }
        });
    }

    /**
     * @功能:上传文件后，需要上传更新图像id
     * @author:Arisono
     * @param:
     * @return:
     */
    public void updateIMChatImageId(String roomjId, String photoId) {
//        this.uploadIsEnd=true;
        String url = MyApplication.getInstance().getConfig().apiUrl + "room/setRelationGroupPhoto";
        final String requestTag = "loginManagerSystem";
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("roomId", roomjId);
        params.put("photoid", photoId);
//		final ProgressDialog dialog = ProgressDialogUtil.init(mContext, null, getString(R.string.please_wait), true);
//		ProgressDialogUtil.show(dialog);
        StringJsonObjectRequest<String> mRequest = new StringJsonObjectRequest<String>(
                Request.Method.GET, url,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
//						ProgressDialogUtil.dismiss(dialog);
//						ToastUtil.showErrorNet(mContext);
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


    private List<SelectContactsActivity.MyBitmapEntity> getBitmapEntitys(int count) {
        List<SelectContactsActivity.MyBitmapEntity> mList = new LinkedList<SelectContactsActivity.MyBitmapEntity>();
        String value = PropertiesUtil.readData(this, String.valueOf(count),
                R.raw.data);
        String[] arr1 = value.split(";");
        int length = arr1.length;
        for (int i = 0; i < length; i++) {
            String content = arr1[i];
            String[] arr2 = content.split(",");
            SelectContactsActivity.MyBitmapEntity entity = null;
            for (int j = 0; j < arr2.length; j++) {
                entity = new SelectContactsActivity.MyBitmapEntity();
                entity.x = Float.valueOf(arr2[0]);
                entity.y = Float.valueOf(arr2[1]);
                entity.width = Float.valueOf(arr2[2]);
                entity.height = Float.valueOf(arr2[3]);
            }
            mList.add(entity);
        }
        return mList;
    }

    private final int uploadFile = 1;
    private File image;
    private Handler mhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case uploadFile:
                    createRoomSuccess(mRoomId, mRoomJid, mRoomName, mRoomDes, photoId);
                    String roomjId = msg.getData().getString("roomjId");
                    Log.i("Arison", "SelectContactsActivity:handleMessage:466:handler uploadFile=" + uploadFile);
                    uploadAvatar(image, roomjId);

                    break;
            }

        }
    };
}

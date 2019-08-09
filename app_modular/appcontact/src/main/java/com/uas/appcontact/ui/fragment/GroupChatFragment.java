package com.uas.appcontact.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.StringUtil;
import com.common.ui.ProgressDialogUtil;
import com.common.ui.ViewHolder;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.EasyFragment;
import com.core.broadcast.MucgroupUpdateUtil;
import com.core.model.Friend;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.model.MucRoom;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appcontact.R;
import com.uas.appcontact.ui.activity.CommonFragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupChatFragment extends EasyFragment {
    private PullToRefreshListView mPullToRefreshListView;
    private List<MucRoom> mMucRooms;
    private MucRoomAdapter mAdapter;
    private int mPageIndex = 0;
    private BaseActivity mActivity;
    private boolean mNeedUpdate = true;
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
    private int imStatus;

    public GroupChatFragment() {
        mMucRooms = new ArrayList<>();

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
            LogUtil.i("onOptionsItemSelected");
//            startActivity(new Intent(getActivity(), SelectContactsActivity.class));
            startActivity(new Intent("com.modular.groupchat.CreateGroupActivity"));
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
            imStatus = ((CommonFragmentActivity) getActivity()).getImStatus();
            initView();
            init();
        }
    }

    private void init() {
        for (int i = 0; i < mMucRooms.size(); i++) {
            mMucRooms.get(i).getJid();
        }

    }

    boolean b = false;


    @SuppressLint("InflateParams")
    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        View emptyView = LayoutInflater.from(getActivity()).inflate(
                R.layout.view_empty, null);
        mPullToRefreshListView.setEmptyView(emptyView);

        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(
                    PullToRefreshBase<ListView> refreshView) {
                requestData(true);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestData(false);
            }
        });

        /**@注释：点击进入房间的逻辑 */
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
                    friend.setRoomCreateUserId(StringUtil.isEmpty(room.getSubject()) ? room.getUserId() : room.getSubject());
                    // timeSend作为取群聊离线消息的标志，所以要在这里设置一个初始值
                    friend.setTimeSend(CalendarUtil.getSecondMillion());
                    friend.setStatus(Friend.STATUS_FRIEND);
                    FriendDao.getInstance().createOrUpdateFriend(friend);//创建或者更新好友...

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
        if (mNeedUpdate) {
            mNeedUpdate = false;
            mPullToRefreshListView.post(new Runnable() {
                @Override
                public void run() {
                    if (imStatus != 3) {
                        //Crouton.makeText(ct, R.string.service_start_failed);
                        ToastUtil.showToast(ct, R.string.service_start_failed);
                        return;
                    }
                    mPullToRefreshListView.setPullDownRefreshing(200);
                }
            });
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (BaseActivity) getActivity();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0x11) {
            requestData(true);
        }
    }

    private void requestData(final boolean isPullDwonToRefersh) {
        if (imStatus != 3) {
          //  Crouton.makeText(ct, R.string.service_start_failed, 2000);
            ToastUtil.showToast(ct, R.string.service_start_failed);
            return;
        }
        if (isPullDwonToRefersh) {
            mPageIndex = 0;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("pageIndex", String.valueOf(mPageIndex));
        params.put("pageSize", String.valueOf(AppConfig.PAGE_SIZE));
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        if (AppConfig.DEBUG) {
            Log.i("Arison", "mAccessToken:" + MyApplication.getInstance().mAccessToken);
            Log.i("Arison", "PAGE_SIZE:" + String.valueOf(AppConfig.PAGE_SIZE));
            Log.i("Arison", "mPageIndex:" + String.valueOf(mPageIndex));
        }
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
                    if (datas != null && datas.size() > 0) {
                        for (int i = 0; i < datas.size(); i++) {
                            if (datas.get(i).getCategory() == 1) {

                            } else {
                                mMucRooms.add(datas.get(i));
                            }
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
                if (!b) {
                    b = true;
//                    uploadAvatar();
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
            if (!StringUtil.isEmpty(room.getSubject())) {
                //合成图像下载
                AvatarHelper.getInstance().displayAvatarPng(room.getSubject(),
                        avatar_img, false);
            } else {
                AvatarHelper.getInstance().displayAvatar(room.getUserId(),
                        avatar_img, false);
            }


            nick_name_tv.setText(room.getName());
            time_tv.setText(TimeUtils.getFriendlyTimeDesc(getActivity(),
                    (int) room.getCreateTime()));
            content_tv.setText(room.getDesc());
            return convertView;
        }

    }

}

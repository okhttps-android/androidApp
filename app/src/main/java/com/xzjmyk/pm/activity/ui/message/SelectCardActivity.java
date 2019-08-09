package com.xzjmyk.pm.activity.ui.message;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.common.ui.ProgressDialogUtil;
import com.common.ui.ViewHolder;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.model.Friend;
import com.core.xmpp.dao.FriendDao;
import com.core.utils.helper.AvatarHelper;
import com.core.base.BaseActivity;
import com.core.xmpp.CoreService;

import java.util.ArrayList;
import java.util.List;

public class SelectCardActivity extends BaseActivity {

    private final int LAST_ICON = -1;

    private ListView mListView;
//    private HorizontalListView mHorizontalListView;
    private Button mOkBtn;

    private List<Friend> mFriendList;
    private ListViewAdapter mAdapter;
    private List<Integer> mSelectPositions;
//    private HorListViewAdapter mHorAdapter;
    public ProgressDialog mProgressDialog;

    private String mLoginUserId;
    private boolean mBind;
    private CoreService mXmppService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_card);
        mFriendList = new ArrayList<Friend>();
        mAdapter = new ListViewAdapter();
        mSelectPositions = new ArrayList<Integer>();
        mSelectPositions.add(LAST_ICON);// 增加一个虚线框的位置

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
        mListView = (ListView) findViewById(R.id.list_view);
//        mHorizontalListView = (HorizontalListView) findViewById(R.id.horizontal_list_view);
        mOkBtn = (Button) findViewById(R.id.ok_btn);
        mListView.setAdapter(mAdapter);

        mOkBtn.setText(getString(R.string.add_chat_ok_btn, mSelectPositions.size() - 1));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Friend card=mFriendList.get(position);
                Intent intent=new Intent();
                intent.putExtra("card",card);
                SelectCardActivity.this.setResult(RESULT_OK,intent);
                SelectCardActivity.this.finish();
                if (hasSelected(position)) {

//                    removeSelect(position);
                } else {
//                    addSelect(position);
                }
            }
        });


        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //TODO  返回数据
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
        if (userInfos != null) {
            mFriendList.clear();
            mFriendList.addAll(userInfos);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void addSelect(int position) {
        if (!hasSelected(position)) {
            mSelectPositions.add(0, position);
            mAdapter.notifyDataSetInvalidated();
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

    private void removeSelect(int position) {
        mSelectPositions.remove(Integer.valueOf(position));
        mAdapter.notifyDataSetInvalidated();
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_select_card, parent, false);
            }
            ImageView avatarImg = ViewHolder.get(convertView, R.id.avatar_img);
            TextView userNameTv = ViewHolder.get(convertView, R.id.user_name_tv);

            AvatarHelper.getInstance().displayAvatar(mFriendList.get(position).getUserId(), avatarImg, true);
            userNameTv.setText(mFriendList.get(position).getNickName());

            return convertView;
        }

    }

}

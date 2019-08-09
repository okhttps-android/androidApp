package com.xzjmyk.pm.activity.ui.message;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.common.ui.ProgressDialogUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.Friend;
import com.core.utils.sortlist.BaseComparator;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.core.utils.sortlist.SideBar;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.model.ChatMessage;
import com.core.app.AppConstant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appcontact.adapter.FriendSortAdapter;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.util.im.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @项目名称: SkWeiChat-Baidu
 * @包名: com.xzjmyk.pm.activity.ui.message
 * @作者:王阳
 * @创建时间: 2015年10月21日 下午4:30:23
 * @描述: 选择联系人转发消息
 * @SVN版本号: $Rev: 2144 $
 * @修改人: $Author: luorc $
 * @修改时间: $Date: 2015-10-23 14:34:14 +0800 (Fri, 23 Oct 2015) $
 * @修改的内容: TODO
 */
public class SelectNewContactsActivity extends BaseActivity implements OnClickListener {
    private PullToRefreshListView mPullToRefreshListView;
    private TextView mTextDialog;
    private SideBar mSideBar;
    private ProgressDialog mProgressDialog;
    private List<BaseSortModel<Friend>> mSortFriends;
    private BaseComparator<Friend> mBaseComparator;
    private FriendSortAdapter mAdapter;
    private String mLoginUserId;
    private Handler mHandler = new Handler();
    private ChatMessage message;
    public static final String BROADCOAST_SENDMESSAGE_ACTION = "broadcoast_sendmessage_action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newchat_person_selected);
        mSortFriends = new ArrayList<BaseSortModel<Friend>>();
        mBaseComparator = new BaseComparator<Friend>();
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        message = (ChatMessage) getIntent().getParcelableExtra(Constants.INSTANT_MESSAGE);

        initView();
    }

    private void initView() {
        setTitle(R.string.select_contact);
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        View headView = View.inflate(this, R.layout.item_headview_creategroup_chat, null);
        mPullToRefreshListView.getRefreshableView().addHeaderView(headView);
        headView.setOnClickListener(this);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setTextView(mTextDialog);

        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mPullToRefreshListView.getRefreshableView().setSelection(position);
                }
            }

            @Override
            public void onTouchingUp() {

            }
        });

        mAdapter = new FriendSortAdapter(this, mSortFriends);

        mPullToRefreshListView.setMode(Mode.PULL_FROM_START);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);

        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadData();
            }
        });

        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend = mSortFriends.get((int) id).getBean();
                /*
				 * Intent intent = null; if
				 * (friend.getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)) {//
				 * 新朋友消息 intent = new Intent(SelectContactsActivity.this,
				 * NewFriendActivity.class); } else if
				 * (friend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {//
				 * 新朋友消息 intent = new Intent(SelectContactsActivity.this,
				 * ChatActivity.class); intent.putExtra(ChatActivity.FRIEND,
				 * friend); } else { intent = new
				 * intent.putExtra(AppConstant.EXTRA_USER_ID,
				 * friend.getUserId()); } startActivity(intent);
				 */
                showPopuWindow(view, friend);

            }

        });

        mPullToRefreshListView.getRefreshableView()
                .setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        BaseSortModel<Friend> sortFriend = mSortFriends.get((int) id);
                        if (sortFriend == null || sortFriend.getBean() == null) {
                            return false;
                        }
                        String userId = sortFriend.getBean().getUserId();
                        if (userId.equals(Friend.ID_SYSTEM_MESSAGE) || userId.equals(Friend.ID_NEW_FRIEND_MESSAGE)) {
                            return false;
                        }
                        // showLongClickOperationDialog(sortFriend);
                        return true;
                    }
                });

        mProgressDialog = ProgressDialogUtil.init(this, null, getString(R.string.please_wait));
    }

    InstantMessageConfirm menuWindow;

    private void showPopuWindow(View view, Friend friend) {
        menuWindow = new InstantMessageConfirm(SelectNewContactsActivity.this, new ClickListener(message, friend),
                friend);
        // 显示窗口
        menuWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    /**
     * 事件的监听
     */
    class ClickListener implements OnClickListener {
        private ChatMessage message;
        private Friend friend;

        public ClickListener(ChatMessage message, Friend friend) {
            this.message = message;
            this.friend = friend;
        }

        @Override
        public void onClick(View v) {
            menuWindow.dismiss();
            switch (v.getId()) {
                case R.id.btn_send:// copy文字
				/*
				 * Intent broadcast=new Intent(BROADCOAST_SENDMESSAGE_ACTION);
				 * Bundle bundle=new Bundle();
				 * bundle.putParcelable(Constants.INSTANT_MESSAGE, message);
				 * broadcast.putExtras(bundle); sendBroadcast(broadcast);
				 */
                    Intent intent = new Intent(SelectNewContactsActivity.this, ChatActivity.class);
                    intent.putExtra(AppConstant.FRIEND, friend);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.INSTANT_MESSAGE, message);
                    intent.putExtras(bundle);
                    // intent.putExtra(Constants.INSTANT_SEND,"send");
                    startActivity(intent);
                    SelectNewContactsActivity.this.finish();
                    break;
                case R.id.btn_cancle:// 取消

                    break;
                default:
                    break;
            }
        }
    }

    public void update() {
        if (isRestricted()) {// TODO 这里不完善,需要改
            loadData();
        } else {
            mNeedUpdate = true;
        }
    }

    private boolean mNeedUpdate = true;

    @Override
    public void onResume() {
        super.onResume();
        if (mNeedUpdate) {
            loadData();
            mNeedUpdate = false;
        }
    }

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                final List<Friend> friends = FriendDao.getInstance().getFriends(mLoginUserId);
                long delayTime = 200 - (startTime - System.currentTimeMillis());// 保证至少200ms的刷新过程
                if (delayTime < 0) {
                    delayTime = 0;
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSortFriends.clear();
                        mSideBar.clearExist();
                        if (friends != null && friends.size() > 0) {
                            for (int i = 0; i < friends.size(); i++) {
                                BaseSortModel<Friend> mode = new BaseSortModel<Friend>();
                                mode.setBean(friends.get(i));
                                setSortCondition(mode);
                                mSortFriends.add(mode);
                            }
                            Collections.sort(mSortFriends, mBaseComparator);
                        }
                        mAdapter.notifyDataSetInvalidated();
                        mPullToRefreshListView.onRefreshComplete();
                    }
                }, delayTime);
            }
        }).start();
    }

    private final void setSortCondition(BaseSortModel<Friend> mode) {
        Friend friend = mode.getBean();
        if (friend == null) {
            return;
        }
        String name = friend.getShowName();
        String wholeSpell = PingYinUtil.getPingYin(name);
        if (!TextUtils.isEmpty(wholeSpell)) {
            String firstLetter = Character.toString(wholeSpell.charAt(0));
            mSideBar.addExist(firstLetter);
            mode.setWholeSpell(wholeSpell);
            mode.setFirstLetter(firstLetter);
            mode.setSimpleSpell(PingYinUtil.converterToFirstSpell(name));
        } else {// 如果全拼为空，理论上是一种错误情况，因为这代表着昵称为空
            mode.setWholeSpell("#");
            mode.setFirstLetter("#");
            mode.setSimpleSpell("#");
        }
    }

    /**
     * 点击headView的回调
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rl_headview_instant_group:
                Intent intent = new Intent(SelectNewContactsActivity.this, SelectNewGroupInstantActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable(Constants.INSTANT_MESSAGE, message);
                intent.putExtras(bundle);
                startActivity(intent);
                SelectNewContactsActivity.this.finish();
                break;

            default:
                break;
        }
    }

}

package com.xzjmyk.pm.activity.ui.erp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.AttentionUser;
import com.core.model.EmployeesEntity;
import com.core.model.Friend;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.utils.ToastUtil;
import com.core.utils.sortlist.BaseComparator;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.core.utils.sortlist.SideBar;
import com.core.widget.ClearEditText;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.OnCompleteListener;
import com.core.app.AppConstant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appme.other.activity.BasicInfoActivity;
import com.uas.appcontact.adapter.FriendSortAdapter;
import com.xzjmyk.pm.activity.ui.message.ChatActivity;
import com.uas.appcontact.ui.activity.NewFriendActivity;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.base.EasyFragment;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pengminggong on 2016/11/7.
 */

public class UUFriendFragment extends EasyFragment {
    private PullToRefreshListView mPullToRefreshListView;
    private TextView mTextDialog;
    private SideBar mSideBar;
    private List<BaseSortModel<Friend>> mSortFriends;
    private List<BaseSortModel<Friend>> allFriends;
    private BaseComparator<Friend> mBaseComparator;
    private FriendSortAdapter mAdapter;
    private String mLoginUserId;
    private Handler mHandler = new Handler();
    private boolean mNeedUpdate = true;
    private DBManager manager;
    private boolean isPeculiar = false;
    private ClearEditText search_edit;



    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_contact;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x20 && resultCode == 0x20) {
            mAdapter.setisRefeshed(true);
            upDataFriend();
            loadData();

        }
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {

        if (isPeculiar)
            ((BaseActivity) getActivity()).setTitle("常用联系人");
        else
            ((BaseActivity) getActivity()).setTitle("UU好友");
        mSortFriends = new ArrayList<>();
        allFriends = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        initView();
    }

    private void upDataFriend() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        Log.i("url", "相互关注：" + MyApplication.getInstance().getConfig().FRIENDS_ATTENTION_LIST);
        StringJsonArrayRequest<AttentionUser> request = new StringJsonArrayRequest<AttentionUser>(
                MyApplication.getInstance().getConfig().FRIENDS_ATTENTION_LIST, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(ct);
            }
        }, new StringJsonArrayRequest.Listener<AttentionUser>() {
            @Override
            public void onResponse(ArrayResult<AttentionUser> result) {
                boolean success = Result.defaultParser(ct, result, false);
                if (success) {
                    FriendDao.getInstance().addAttentionUsers(mHandler, MyApplication.getInstance().mLoginUser.getUserId(), result.getData(),
                            new OnCompleteListener() {
                                @Override
                                public void onCompleted() {
                                    mPullToRefreshListView.getRefreshableView();
                                }
                            });
                }
            }
        }, AttentionUser.class, params);
        MyApplication.getInstance().getFastVolley().addDefaultRequest(HASHCODE, request);
    }


    private void initView() {
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        mSideBar.setTextView(mTextDialog);
//        mPullToRefreshListView.setEmptyView(R.layout.view_empty);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                // 该字母首次出现的位置
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    int count = mPullToRefreshListView.getRefreshableView().getHeaderViewsCount();
                    mPullToRefreshListView.getRefreshableView().setSelection(position + count);
                }
                if ("↑".equals(s)) {
                    mPullToRefreshListView.getRefreshableView().setSelection(1);
                }
            }

            @Override
            public void onTouchingUp() {
                //linear_top_menu.setVisibility(View.VISIBLE);
            }
        });
        setHeaderView();
        mAdapter = new FriendSortAdapter(ct, mSortFriends);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshListView.getRefreshableView().setAdapter(mAdapter);
        mPullToRefreshListView.setAdapter(mAdapter);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mAdapter.setisRefeshed(true);
                upDataFriend();
                loadData();
            }
        });

        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private Friend friend;   //l临时对象

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    friend = mSortFriends.get((int) id).getBean();
                } catch (ArrayIndexOutOfBoundsException e) {
                    return;
                }
                Intent intent = null;
                if (friend.getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)) {// 新朋友消息
                    intent = new Intent(ct, NewFriendActivity.class);
                } else if (friend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {// 新朋友消息
                    intent = new Intent(ct, ChatActivity.class);
                    intent.putExtra(AppConstant.FRIEND, friend);

                } else {
                    intent = new Intent(ct, BasicInfoActivity.class);
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    intent.putExtra(AppConstant.EXTRA_NICK_CODE, friend.getPhone());
                    intent.putExtra("friend", friend);
                }
                startActivityForResult(intent, 0x20);
            }
        });
    }

    /*设置头文件*/
    private void setHeaderView() {
        View headview = LayoutInflater.from(ct).inflate(R.layout.header_fragment_contact, null);
        mPullToRefreshListView.getRefreshableView().addHeaderView(headview);
        headview.findViewById(R.id.click_ll).setVisibility(View.GONE);
        search_edit = (ClearEditText) headview.findViewById(R.id.search_edit);
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable == null ? "" : editable.toString();
                upDataBySearch(str);
                mAdapter.setData(mSortFriends);
            }
        });
    }

    /**
     * 当搜索框有字时候获取
     *
     * @param str 搜索框文字
     */
    private void upDataBySearch(String str) {
        if (ListUtils.isEmpty(allFriends)) return;
        mSortFriends.clear();
        for (BaseSortModel<Friend> e : allFriends) {
            String text = e.getBean().getShowName() + e.getBean().getDepart() + e.getBean().getPosition() + e.getBean().getPhone();
            if (StringUtil.isInclude(text, str)) {
                mSortFriends.add(e);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNeedUpdate) {
            loadData();
            mNeedUpdate = false;
        }
    }

    List<Friend> friends;//临时变量

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                if (mLoginUserId == null)
                    mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                if (isPeculiar) {
                    friends = FriendDao.getInstance().getFriends(mLoginUserId, "clickNum", 10);
                } else
                    friends = FriendDao.getInstance().getFriends(mLoginUserId);
                if (friends != null)
                    Log.i("wang", "friends size()=" + friends.size());
                long delayTime = 200 - (startTime - System.currentTimeMillis());// 保证至少200ms的刷新过程
                if (delayTime < 0) {
                    delayTime = 0;
                }
                getFriendByErp(friends);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        search_edit.setText("");
                        mSortFriends.clear();
                        allFriends.clear();
                        mSideBar.clearExist();
                        mSideBar.addExist("↑");
                        mSideBar.addExist("↑");
                        mSideBar.addExist("↑");
                        if (friends != null && friends.size() > 0) {
                            for (int i = 0; i < friends.size(); i++) {
                                BaseSortModel<Friend> mode = new BaseSortModel<Friend>();
                                mode.setBean(friends.get(i));
                                setSortCondition(mode);
                                mSortFriends.add(mode);
                                allFriends.add(mode);
                            }
                            Collections.sort(mSortFriends, mBaseComparator);
                            Collections.sort(allFriends, mBaseComparator);
                        }
                        mAdapter.notifyDataSetInvalidated();
                        mPullToRefreshListView.onRefreshComplete();
                    }
                }, delayTime);
            }
        }).start();
    }

    private void getFriendByRep(EmployeesEntity e, Friend friend) {
        friend.setOwnerId(mLoginUserId);
        friend.setNickName(e.getEM_NAME());
        friend.setPhone(e.getEM_MOBILE());
        friend.setDepart(e.getEM_DEPART());
        friend.setPosition(e.getEM_POSITION());
        friend.setEmCode(e.getEM_CODE());
        friend.setPrivacy(e.getEM_EMAIL());
        friend.setCompanyId(0);
        friend.setRoomFlag(0);// 0朋友 1群组
        friend.setStatus(Friend.STATUS_UNKNOW);
    }

    private void getFriendByErp(List<Friend> friends) {
        if (manager == null)
            manager = new DBManager(MyApplication.getInstance());
        String master = CommonUtil.getSharedPreferences(ct, "erp_master");
        List<EmployeesEntity> emList = null;
        if (!StringUtil.isEmpty(master)) {
            emList = manager.select_getEmployee(new String[]{master}, "whichsys=?");
        }
        if (ListUtils.isEmpty(emList)) return;
        if (!ListUtils.isEmpty(friends)) {
            for (int i = 0; i < friends.size(); i++) {
                String id = friends.get(i).getUserId();
                for (int j = 0; j < emList.size(); j++) {
                    if (id.equals(String.valueOf(emList.get(j).getEm_IMID()))) {
                        getFriendByRep(emList.get(j), friends.get(i));
                        break;
                    }
                }
            }
        }
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

    String HASHCODE = Integer.toHexString(this.hashCode()) + "@";

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
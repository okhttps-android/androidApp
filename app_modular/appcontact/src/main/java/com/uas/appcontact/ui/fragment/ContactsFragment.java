package com.uas.appcontact.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.thread.ThreadUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.fragment.SupportToolBarFragment;
import com.core.broadcast.MsgBroadcast;
import com.core.model.EmployeesEntity;
import com.core.model.Friend;
import com.core.net.http.http.OAHttpHelper;
import com.core.utils.CommonUtil;
import com.core.utils.sortlist.BaseComparator;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.core.utils.sortlist.SideBar;
import com.core.widget.CustomProgressDialog;
import com.core.widget.VoiceSearchView;
import com.core.widget.listener.EditChangeListener;
import com.modular.apputils.manager.ContactsManager;
import com.uas.appcontact.R;
import com.uas.appcontact.adapter.FriendSortAdapter;
import com.uas.appcontact.listener.ImStatusListener;
import com.uas.appcontact.ui.activity.CommonFragmentActivity;
import com.uas.appcontact.ui.activity.CompanyContactsActivity;
import com.uas.appcontact.ui.activity.ContactsActivity;
import com.uas.appcontact.ui.activity.MyFriendActivity;
import com.uas.appcontact.ui.activity.NewFriendActivity;
import com.uas.appcontact.ui.activity.PlatContactAddPeopleActivity;
import com.uas.appcontact.ui.activity.UserSearchActivity;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by Bitliker on 2017/9/1.
 */

public class ContactsFragment extends SupportToolBarFragment
        implements SideBar.OnTouchingLetterChangedListener
        , PullToRefreshView.OnRefreshListener
        , AdapterView.OnItemClickListener
        , View.OnClickListener
        , ContactsManager.OnEmployListener {

    private StickyListHeadersListView refreshListView;
    private PullToRefreshView mPullToRefreshView;


    private TextView dialogTV;
    private TextView uuRedTag;
    private SideBar sidebar;
    private VoiceSearchView voiceSearchView;

    private BaseComparator comparator;
    private List<BaseSortModel<Friend>> allDatas;
    private List<BaseSortModel<Friend>> showDatas;
    private FriendSortAdapter adapter;
    private String userId;
    private ImStatusListener mListener;
    private boolean isB2b;
    private CustomProgressDialog progressDialog;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.app.home.update") || intent.getAction().equals(MsgBroadcast.ACTION_MSG_COMPANY_UPDATE)) {//账套变更，重新刷新数据，初始化任务
                loadData();
            }
        }
    };

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_contacts;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter inputFilter = new IntentFilter();
        inputFilter.addAction(MsgBroadcast.ACTION_MSG_COMPANY_UPDATE);
        inputFilter.addAction("com.app.home.update");
        LocalBroadcastManager.getInstance(ct).registerReceiver(receiver, inputFilter);
        setHasOptionsMenu(true);
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(ct).unregisterReceiver(receiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_nearby, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.search) {
            if (!isB2b) {
                ct.startActivity(new Intent(getActivity(), UserSearchActivity.class));
            } else {
                showPopupWindow();
            }
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            initView();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ImStatusListener) {
            mListener = (ImStatusListener) context;
        }
    }

    private void initView() {
        setTitle(R.string.contact_title);
        progressDialog = CustomProgressDialog.createDialog(getContext());
        showDatas = new ArrayList<>();
        allDatas = new ArrayList<>();
        userId = MyApplication.getInstance().getLoginUserId();
        refreshListView = (StickyListHeadersListView) findViewById(R.id.pull_refresh_list);
        mPullToRefreshView = (PullToRefreshView) findViewById(R.id.contact_ptrv);
        dialogTV = (TextView) findViewById(R.id.dialogTV);
        sidebar = (SideBar) findViewById(R.id.sidebar);
        sidebar.setTextView(dialogTV);
        sidebar.setOnTouchingLetterChangedListener(this);
        refreshListView.setOnItemClickListener(this);
        comparator = new BaseComparator();
        initHeaderView();
        adapter = new FriendSortAdapter(ct, showDatas);
        refreshListView.setAdapter(adapter);
        mPullToRefreshView.setOnRefreshListener(this);
        loadData();
    }

    private void showPopupWindow() {
        View viewContext = LayoutInflater.from(getActivity()).inflate(R.layout.contact_add_menu, null);
        final PopupWindow setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        viewContext.findViewById(R.id.add_friend_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ct.startActivity(new Intent(getActivity(), UserSearchActivity.class));
                if (setWindow != null) {
                    setWindow.dismiss();
                }
            }
        });
        viewContext.findViewById(R.id.add_people_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ct.startActivity(new Intent(getActivity(), PlatContactAddPeopleActivity.class));
                if (setWindow != null) {
                    setWindow.dismiss();
                }
            }
        });
        setWindow.setAnimationStyle(R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(ct.getResources().getDrawable(R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(getActivity(), 1f);
            }
        });
        setWindow.showAtLocation(getActivity().getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(getActivity(), 0.4f);
    }

    private void initHeaderView() {
        View headview = LayoutInflater.from(ct).inflate(R.layout.header_fragment_contact, null);
        refreshListView.addHeaderView(headview);
        voiceSearchView = (VoiceSearchView) headview.findViewById(R.id.voiceSearchView);
        headview.findViewById(R.id.company).setOnClickListener(this);//企业结构
        headview.findViewById(R.id.group).setOnClickListener(this);//商务群
        headview.findViewById(R.id.uu_friend).setOnClickListener(this);//我的好友
        headview.findViewById(R.id.peculiar_tv).setOnClickListener(this);//我的好友
        uuRedTag = (TextView) headview.findViewById(R.id.tv_uu_new);
        uuRedTag.setVisibility(CommonUtil.getSharedPreferencesBoolean(ct, Constants.SET_UU_NEW, false) ? View.GONE : View.VISIBLE);
        voiceSearchView.addTextChangedListener(new EditChangeListener() {
            @Override
            public void afterTextChanged(Editable editable) {
                String str = editable == null ? "" : editable.toString();
                upDataBySearch(str);
                adapter.setData(showDatas);
            }
        });
    }


    private void showLoading() {
        if (mPullToRefreshView != null) {
            if (!mPullToRefreshView.isRefreshing() && !isHidden()) {
//                progressDialog.show();
            }
        }
    }//显示刷新

    void dimssLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (mPullToRefreshView != null) {
            if (mPullToRefreshView.isRefreshing()) {
                mPullToRefreshView.setRefreshing(false);
            }
        }
    }//关闭刷新

    private void loadData() {
        if (allDatas != null) {
            allDatas.clear();
        }
        if (showDatas != null) {
            showDatas.clear();
        }
        showLoading();
        ContactsManager.getInstance().loadContact(this);
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        // 该字母首次出现的位置
        int position = adapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            refreshListView.setSelection(position);
        }
        if ("↑".equals(s)) {
            refreshListView.setSelection(0);
        }
    }

    @Override
    public void onTouchingUp() {

    }

    @Override
    public void onRefresh() {
        if ("1".equals(CommonUtil.getUserRole())) {
            mPullToRefreshView.setRefreshing(false);
            dimssLoading();
        } else {
            ContactsManager.getInstance().loadContactByNet(this);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        List<BaseSortModel<Friend>> list = adapter.getmSortFriends();
        if (!ListUtils.isEmpty(list)) {
            Friend friend = list.get((int) id).getBean();
            clickFriend(friend);
        }
    }

    private void clickFriend(Friend friend) {
        Intent intent = null;
        if (friend.getUserId().equals(Friend.ID_NEW_FRIEND_MESSAGE)) {// 新朋友消息
            intent = new Intent(getActivity(), NewFriendActivity.class);
        } else if (friend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {// 新朋友消息
            intent = new Intent("com.modular.message.ChatActivity");
            intent.putExtra("friend", friend);
        } else {
            intent = new Intent("com.modular.basic.BasicInfoActivity");
            intent.putExtra(AppConstant.EXTRA_NICK_CODE, friend.getPhone());
            intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
            intent.putExtra(AppConstant.EXTRA_NICK_NAME, friend.getNickName());
            intent.putExtra(AppConstant.EXTRA_EM_CODE, friend.getEmCode());
            intent.putExtra("friend", friend);
        }
        ct.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        int id = view.getId();
        if (id == R.id.company) {
            intent = new Intent(ct, CompanyContactsActivity.class);
            ct.startActivity(intent);
        } else if (id == R.id.group) {
            intent = new Intent(ct, CommonFragmentActivity.class);
            intent.putExtra("imStatus", mListener == null ? 0 : mListener.getmImStatus());
            intent.putExtra("type", Constants.TYPE_CHAT_All);
            ct.startActivity(intent);
        } else if (id == R.id.uu_friend) {
            ct.startActivity(new Intent(ct, ContactsActivity.class));
            uuRedTag.setVisibility(View.GONE);
            CommonUtil.setSharedPreferences(MyApplication.getInstance(), Constants.SET_UU_NEW, true);
        } else if (id == R.id.peculiar_tv) {
            intent = new Intent(ct, MyFriendActivity.class);
            intent.putExtra("isPeculiar", true);
            ct.startActivity(intent);
        }
    }


    @Override
    public void callback(List<EmployeesEntity> employees) {
        try {
            if (ListUtils.isEmpty(employees)) {
                dimssLoading();
            } else {
                final List<BaseSortModel<Friend>> friends = getFriendsByErpDB(employees);
                ThreadUtil.getInstance().addTask(new Runnable() {
                    @Override
                    public void run() {
                        handlerData(friends);
                    }
                });
            }
        } catch (Exception e) {
            dimssLoading();
        }
    }

    /**
     * 想获取到的人员列表数据整合为Friend数据列表
     *
     * @return 查询到数据列表
     */
    private List<BaseSortModel<Friend>> getFriendsByErpDB(List<EmployeesEntity> emList) throws Exception {
        if (ListUtils.isEmpty(emList)) return null;
        List<BaseSortModel<Friend>> list = new ArrayList<>();
        for (EmployeesEntity e : emList) {
            list.add(getFriendByErp(e));
        }
        if (ListUtils.isEmpty(list)) {
            list = new ArrayList<>();
        } else {
            Collections.sort(list, comparator);
        }
        return list;
    }


    private void handlerData(List<BaseSortModel<Friend>> friends) {
        allDatas = friends;
        if (voiceSearchView == null || TextUtils.isEmpty(voiceSearchView.getText())) {//没有搜索数据
            showDatas = friends;
        } else {//
            String str = voiceSearchView.getText().toString();
            upDataBySearch(str);
        }
        OAHttpHelper.getInstance().post(new Runnable() {
            @Override
            public void run() {
                mPullToRefreshView.setRefreshing(false);
                if (adapter == null) return;
                adapter.setData(showDatas);
                dimssLoading();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mPullToRefreshView.setRefreshing(false, false);
    }

    /**
     * 将Rep转化为Friend对象
     *
     * @param e 员工对象
     * @return
     */
    private BaseSortModel<Friend> getFriendByErp(EmployeesEntity e) {
        Friend friend = new Friend();
        friend.setTimeCreate((int) (System.currentTimeMillis() / 1000));
        friend.setOwnerId(userId);
        friend.setUserId(String.valueOf(e.getEm_IMID()));
        friend.setNickName(e.getEM_NAME());
        friend.setPhone(e.getEM_MOBILE());
        friend.setDepart(e.getEM_DEPART());
        friend.setPosition(e.getEM_POSITION());
        friend.setEmCode(e.getEM_CODE());
        friend.setPrivacy(e.getEM_EMAIL());
        friend.setCompanyId(0);
        friend.setRoomFlag(0);// 0朋友 1群组
        friend.setStatus(Friend.STATUS_FRIEND);
        BaseSortModel<Friend> mode = new BaseSortModel<>();
        mode.setBean(friend);
        setSortCondition(mode);
        return mode;
    }

    /*设置当前mode所在的索引值*/
    private void setSortCondition(BaseSortModel<Friend> mode) {
        Friend friend = mode.getBean();
        if (friend == null) {
            return;
        }
        String name = friend.getShowName();
        String wholeSpell = PingYinUtil.getPingYin(name);
        if (!StringUtil.isEmpty(wholeSpell)) {
            try {
                String firstLetter = Character.toString(wholeSpell.charAt(0));
                sidebar.addExist(firstLetter);
                mode.setWholeSpell(wholeSpell);
                mode.setFirstLetter(firstLetter);
                mode.setSimpleSpell(PingYinUtil.converterToFirstSpell(name));
            } catch (NullPointerException e) {

            }
        } else {// 如果全拼为空，理论上是一种错误情况，因为这代表着昵称为空
            mode.setWholeSpell("#");
            mode.setFirstLetter("#");
            mode.setSimpleSpell("#");
        }
    }

    /**
     * 当搜索框有字时候获取
     *
     * @param str 搜索框文字
     */
    private void upDataBySearch(String str) {
        if (ListUtils.isEmpty(allDatas)) return;
        showDatas = new ArrayList<>();
        for (BaseSortModel<Friend> e : allDatas) {
            String text = e.getBean().getShowName() + e.getBean().getPhone();
            if (StringUtil.isInclude(text, str)) {
                showDatas.add(e);
            }
        }
    }

}

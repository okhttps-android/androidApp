package com.uas.appcontact.ui.activity;

import android.app.ProgressDialog;
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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.thread.ThreadPool;
import com.common.ui.ProgressDialogUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.AttentionUser;
import com.core.model.EmployeesEntity;
import com.core.model.Friend;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.core.net.volley.ArrayResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonArrayRequest;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.VoiceSearchView;
import com.core.utils.sortlist.BaseComparator;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.core.utils.sortlist.SideBar;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.listener.OnCompleteListener;
import com.core.app.AppConstant;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.uas.appcontact.R;
import com.uas.appcontact.adapter.UUFriendSortAdapter;
import com.uas.appcontact.db.TopContactsDao;
import com.uas.appcontact.model.TopContacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 123 on 2016/4/7.
 */
public class MyFriendActivity extends BaseActivity {
    private PullToRefreshListView mPullToRefreshListView;
    private TextView mTextDialog;
    private SideBar mSideBar;
    private ProgressDialog mProgressDialog;
    private List<BaseSortModel<Friend>> mSortFriends;
    private List<BaseSortModel<Friend>> allFriends;
    private BaseComparator<Friend> mBaseComparator;
    private UUFriendSortAdapter mAdapter;
    private String mLoginUserId;
    private Handler mHandler = new Handler();
    private boolean mNeedUpdate = true;
    private DBManager manager;
    private boolean isPeculiar = false;//是否是常用联系人
    private VoiceSearchView voiceSearchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_contact_friend);
        isPeculiar = getIntent().getBooleanExtra("isPeculiar", false);
        if (isPeculiar)
           setTitle(getString(R.string.contact_often));
        else
          setTitle(getString(R.string.contact_friend));
        mSortFriends = new ArrayList<>();
        allFriends = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
        initView();

    }

    private void initView() {
        progressDialog.show();
        mPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.pull_refresh_list);
        mTextDialog = (TextView) findViewById(R.id.text_dialog);
        mSideBar = (SideBar) findViewById(R.id.sidebar);
        voiceSearchView = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        mSideBar.setTextView(mTextDialog);

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
                    mPullToRefreshListView.getRefreshableView().setSelection(0);
                }
            }

            @Override
            public void onTouchingUp() {
                //linear_top_menu.setVisibility(View.VISIBLE);
            }
        });
        // setHeaderView();
        loadData();
       /* mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mAdapter.setisRefeshed(true);
                upDataFriend();
                loadData();
            }
        });*/
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                progressDialog.show();
                mAdapter.setisRefeshed(true);
                upDataFriend();
                loadData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

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
                    intent = new Intent(MyFriendActivity.this, NewFriendActivity.class);
                } else if (friend.getUserId().equals(Friend.ID_SYSTEM_MESSAGE)) {// 新朋友消息
                    intent = new Intent("com.modular.message.ChatActivity");
                    intent.putExtra("friend", friend);

                } else {
                    intent = new Intent("com.modular.basic.BasicInfoActivity");
                    intent.putExtra(AppConstant.EXTRA_USER_ID, friend.getUserId());
                    intent.putExtra(AppConstant.EXTRA_NICK_CODE, friend.getPhone());
                    intent.putExtra("friend", friend);
                }
                startActivityForResult(intent, 0x20);
            }
        });

        mProgressDialog = ProgressDialogUtil.init(MyFriendActivity.this, null, getString(R.string.please_wait));

        voiceSearchView.addTextChangedListener(new TextWatcher() {
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
            }
        });
    }

    /*设置头文件*/
    private void setHeaderView() {
        View headview = LayoutInflater.from(ct).inflate(R.layout.header_fragment_contact, null);
        mPullToRefreshListView.getRefreshableView().addHeaderView(headview);
        headview.findViewById(R.id.click_ll).setVisibility(View.GONE);
        voiceSearchView = (VoiceSearchView) headview.findViewById(R.id.voiceSearchView);
    }

    /**
     * 当搜索框有字时候获取
     *
     * @param str 搜索框文字
     */
    private void upDataBySearch(String str) {
        if (ListUtils.isEmpty(allFriends)) return;
        mAdapter.getmSortFriends().clear();
        for (BaseSortModel<Friend> e : allFriends) {
            String text = e.getBean().getShowName() + e.getBean().getDepart() + e.getBean().getPosition() + e.getBean().getPhone();
            if (StringUtil.isInclude(text, str)) {
                mAdapter.getmSortFriends().add(e);
            }
        }
        LogUtil.prinlnLongMsg("mSortFriends", JSON.toJSONString(mAdapter.getmSortFriends()));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mNeedUpdate) {
//            loadData();
            mNeedUpdate = false;
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    List<Friend> friends;//临时变量

    private void loadData() {
//        if (isPeculiar) loadContactByERpNet();
//        else
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                if (mLoginUserId == null) {
                    mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                }
                friends = new ArrayList<>();
                if (isPeculiar) {
                    List<TopContacts> topContacts = TopContactsDao.api().getTopContacts();
                    if (topContacts != null) {
                        for (TopContacts e : topContacts) {
                            Friend f = new Friend();
                            f.setNickName(e.getName());
                            f.setPhone(e.getPhone());
                            f.setUserId(e.getUserId());
                            f.setOwnerId(e.getOwnerId());
                            f.setEmCode(e.getEmCode());
                            f.setTimeSend((int) (e.getLastTime() / 1000));
                            friends.add(f);
                        }
                    }
//                    friends = FriendDao.getInstance().getFriends(mLoginUserId, "clickNum", 10);
                } else {
                    friends = FriendDao.getInstance().getFriends(mLoginUserId);// 取所有好友
                }
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
                        handlerFriends(friends);
                        LogUtil.prinlnLongMsg("hifriends", JSON.toJSONString(friends));
                    }
                }, delayTime);
            }
        });
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


    private void loadContactByERpNet() {
        String url = "mobile/crm/getContactPerson.action";
        Map<String, Object> param = new HashMap<>();
        param.put("page", 1);
//        param.put("condition", "1=1");
        param.put("size", 1000);
        Request request = new Request.Bulider()
                .setWhat(0)
                .setUrl(url)
                .setParam(param)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request, new OnHttpResultListener() {
            @Override
            public void result(int what, boolean isJSON, String message, Bundle bundle) {
                if (!isJSON) return;
                JSONObject object = JSON.parseObject(message);
                JSONArray listdata = object.getJSONArray("datalist");
                if (ListUtils.isEmpty(listdata)) return;
                JSONObject o = null;
                Friend friend = null;
                List<Friend> friends = new ArrayList<Friend>();
                for (int i = 0; i < listdata.size(); i++) {
                    o = listdata.getJSONObject(i);
                    String name = JSONUtil.getText(o, "ct_name");
                    String ct_job = JSONUtil.getText(o, "ct_job");
                    String ct_cucode = JSONUtil.getText(o, "ct_cucode");
                    String ct_cuname = JSONUtil.getText(o, "ct_cuname");
                    friend = new Friend();
                    friend.setEmCode(ct_cucode);
                    friend.setRemarkName(ct_cuname);
                    friend.setNickName(name);
                    friend.setPosition(ct_job);
                    friend.setUserId(Friend.ID_SYSTEM_MESSAGE);
                    friends.add(friend);
                }
                handlerFriends(friends);
                LogUtil.prinlnLongMsg("hifriends", JSON.toJSONString(friend));
            }

            @Override
            public void error(int what, String message, Bundle bundle) {

            }
        });

    }


    private void handlerFriends(List<Friend> friends) {
        voiceSearchView.setText("");
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
            Collections.sort(mSortFriends, new Comparator<BaseSortModel<Friend>>() {
                @Override
                public int compare(BaseSortModel<Friend> o1, BaseSortModel<Friend> o2) {
                    Friend f1 = o1.getBean();
                    Friend f2 = o2.getBean();
                    return f2.getClickNum() - f1.getClickNum();
                }
            });
            LogUtil.prinlnLongMsg("mSortFriends", JSON.toJSONString(mSortFriends));
            Collections.sort(allFriends, new Comparator<BaseSortModel<Friend>>() {
                @Override
                public int compare(BaseSortModel<Friend> o1, BaseSortModel<Friend> o2) {
                    Friend f1 = o1.getBean();
                    Friend f2 = o2.getBean();
                    return f2.getClickNum() - f1.getClickNum();
                }
            });
            LogUtil.prinlnLongMsg("allFriends", JSON.toJSONString(allFriends));
//            Collections.sort(mSortFriends, mBaseComparator);
//            Collections.sort(allFriends, mBaseComparator);
        }
        mAdapter = new UUFriendSortAdapter(MyFriendActivity.this, mSortFriends);
        this.friends = friends;
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
        mPullToRefreshListView.setAdapter(mAdapter);
        mPullToRefreshListView.onRefreshComplete();
        progressDialog.dismiss();
        mPullToRefreshListView.setEmptyView(R.layout.view_empty);
    }

    private void getFriendByErp(List<Friend> friends) {
        if (manager == null)
            manager = new DBManager();
        String master = CommonUtil.getSharedPreferences(ct, "erp_master");
        List<EmployeesEntity> emList = null;
        if (!StringUtil.isEmpty(master)) {
            emList = manager.select_getEmployee(new String[]{master}, "whichsys=?");
        }
        if (ListUtils.isEmpty(emList)) return;
        if (friends.isEmpty()) return;
        for (EmployeesEntity e : emList) {
            int em_imid = e.getEm_IMID();
            if (em_imid == 0) continue;
            for (int i = 0; i < friends.size(); i++) {
                String id = friends.get(i).getUserId();
                if (id.equals(mLoginUserId)) {//当前用户，去除
                    friends.remove(i);
                    break;
                }
                if (String.valueOf(em_imid).equals(id)) {
                    getFriendByRep(e, friends.get(i));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x20 && resultCode == 0x20) {
//            mAdapter.setisRefeshed(true);
//            upDataFriend();
//            loadData();
        }
    }

    private void upDataFriend() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        Log.i("url", "相互关注：" + MyApplication.getInstance().getConfig().FRIENDS_ATTENTION_LIST);
        StringJsonArrayRequest<AttentionUser> request = new StringJsonArrayRequest<AttentionUser>(
                MyApplication.getInstance().getConfig().FRIENDS_ATTENTION_LIST, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                ToastUtil.showErrorNet(MyFriendActivity.this);
            }
        }, new StringJsonArrayRequest.Listener<AttentionUser>() {
            @Override
            public void onResponse(ArrayResult<AttentionUser> result) {
                LogUtil.d("Test", "result:" + result);
                boolean success = Result.defaultParser(MyFriendActivity.this, result, false);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}

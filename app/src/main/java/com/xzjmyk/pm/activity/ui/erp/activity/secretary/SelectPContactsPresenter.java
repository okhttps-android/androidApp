package com.xzjmyk.pm.activity.ui.erp.activity.secretary;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.thread.ThreadPool;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.Friend;
import com.core.model.NewFriendMessage;
import com.core.model.User;
import com.core.model.XmppMessage;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.volley.ObjectResult;
import com.core.net.volley.Result;
import com.core.net.volley.StringJsonObjectRequest;
import com.core.utils.ToastUtil;
import com.core.utils.sortlist.BaseComparator;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.core.xmpp.CoreService;
import com.core.xmpp.dao.FriendDao;
import com.core.xmpp.dao.NewFriendDao;
import com.core.xmpp.model.AddAttentionResult;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.uas.appcontact.model.contacts.Contacts;
import com.uas.appcontact.utils.ContactsUtils;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.presenter.imp.ISelectActiveView;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Arison on 2017/6/19.
 */

public class SelectPContactsPresenter {

    private ISelectActiveView iSelectActiveView;
    private List<BaseSortModel<SelectEmUser>> allList = new ArrayList<>();
    private BaseComparator<SelectEmUser> mBaseComparator;
    private SelectCollisionTurnBean selectBean;
    private CoreService mXmppService;
    private boolean mBind;
    private  Activity activity;

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

    public SelectPContactsPresenter(ISelectActiveView iSelectActiveView) {
        if (iSelectActiveView == null) throw new NullPointerException("iSelectActiveView is Null");
        this.iSelectActiveView = iSelectActiveView;
        this.activity=((Activity) iSelectActiveView);
    }


    public synchronized void start(SelectCollisionTurnBean selectBean, int type) {
        if (selectBean == null) {
            new NullPointerException("Intent cannot be Null");
        }
        this.selectBean = selectBean;

        mBind = activity.bindService(CoreService.getIntent(), mServiceConnection, activity.BIND_AUTO_CREATE);
       
        allList = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        switch (type) {
            case 0:
                initdata();
                break;
            case 1:
                loadDataForFriend();
                break;
        }

    }
    
    private void destory(){
        if (mBind) {
            activity.unbindService(mServiceConnection);
        }
    }

    private synchronized void loadDataForFriend() {
        iSelectActiveView.showLoading();
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String mLoginUserId = MyApplication.getInstance().mLoginUser.getUserId();
                List<Friend> friends = FriendDao.getInstance().getFriends(mLoginUserId);
                //  friends.clear();
                handlerFriendData(friends);
            }
        });
    }

    private synchronized void handlerFriendData(List<Friend> employees) {
        if (ListUtils.isEmpty(employees)) {
            OAHttpHelper.getInstance().post(new Runnable() {
                @Override
                public void run() {
                    showModel(null);
                    iSelectActiveView.dimssLoading();
                }
            });
            return;
        }
        SelectEmUser user = null;
        List<SelectEmUser> users = new ArrayList<>();
        for (Friend e : employees) {
            if (e.getUserId().equals(MyApplication.getInstance().mLoginUser.getUserId())) {
                continue;
            }
            user = new SelectEmUser();
            user.setDefaultorid(0);
            user.setDepart(e.getDepart());
            user.setEmName(e.getNickName());
            user.setImId(Integer.valueOf(e.getUserId()));
            user.setEmCode(e.getEmCode());
            user.setPosition(e.getPosition());
            user.setEmId(e.get_id());
            users.add(user);
        }
        setUser2Sort(users, false);
    }

    private void initdata() {
        List<SelectEmUser> selects = selectBean.getSelectList();
        if (!StringUtil.isEmpty(selectBean.getSureText()))
            iSelectActiveView.showSureText(selectBean.getSureText());
        if (ListUtils.isEmpty(selects))
            loadDataInThread();
        else
            setUser2Sort(selects, true);
    }

    public void onClickStatus(SelectEmUser user, int position, String message) {
        LogUtil.i("onClickStatus");
        if (StringUtil.isEmpty(message)) return;
        if (MyApplication.getInstance().getString(R.string.add).equals(message)) {
            addUser(user, position);
        } else if (MyApplication.getInstance().getString(R.string.invite).equals(message)) {
            invite(user, position);
        }
    }

    public void changeChecked(boolean b, List<BaseSortModel<SelectEmUser>> listData) {
        if (ListUtils.isEmpty(listData)) {
        } else {
            for (int i = 0; i < listData.size(); i++) {
                listData.get(i).setClick(b);
            }
            iSelectActiveView.showModel(listData);
            ArrayList<SelectEmUser> chche = getClickNumber();
            iSelectActiveView.showNumber(chche.size());
        }
    }

    public void search(Editable s) {
        if (ListUtils.isEmpty(allList)) return;
        if (s == null || StringUtil.isEmpty(s.toString())) {
            iSelectActiveView.showModel(allList);
            return;
        }
        List<BaseSortModel<SelectEmUser>> chche = new ArrayList<>();
        for (BaseSortModel<SelectEmUser> u : allList) {
            String text = getTextForSearch(u);
            if (StringUtil.isInclude(text, s.toString()))
                chche.add(u);
        }
        iSelectActiveView.showModel(chche);
    }

    /**
     * 确定
     *
     * @param ct
     */
    public void sure(BaseActivity ct) {
        if (ListUtils.isEmpty(allList)) {
            iSelectActiveView.showToast(R.string.not_data_to_select_number, R.color.load_error);
            return;
        }
        ArrayList<SelectEmUser> chche = selectBean.isReBackSelect() ? getClickNumber() : getNoClickNumber();
        Intent intent = new Intent();
        if (chche instanceof ArrayList)
            intent.putParcelableArrayListExtra("data", chche);
        ct.setResult(0x20, intent);
        ct.finish();
    }

    public void sureSingle(BaseActivity ct, SelectEmUser user) {

        Intent intent = new Intent();
        intent.putExtra("data", user);
        ct.setResult(0x20, intent);
        ct.finish();
    }

    private ArrayList<SelectEmUser> getClickNumber() {
        ArrayList<SelectEmUser> chche = new ArrayList<>();
        for (BaseSortModel<SelectEmUser> e : allList) {
            if (e.isClick()) chche.add(e.getBean());
        }
        return chche;
    }

    private ArrayList<SelectEmUser> getNoClickNumber() {
        ArrayList<SelectEmUser> chche = new ArrayList<>();
        for (BaseSortModel<SelectEmUser> e : allList) {
            if (!e.isClick()) chche.add(e.getBean());
        }
        return chche;
    }

    /*开启线程获取数据*/
    private void loadDataInThread() {
        iSelectActiveView.showLoading();
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<Contacts> contactsList = ContactsUtils.getContacts();
                    OAHttpHelper.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            ThreadPool.getThreadPool().addTask(new Runnable() {
                                @Override
                                public void run() {
                                    handlerLoadData(contactsList);
                                }
                            });
                        }
                    });
                } catch (Exception e) {
                    if (e != null)
                        Log.i("gongpengming", "loadDataInThread  " + e.getMessage());
                }
            }

        });
    }


    private void handerError(String message) {
        iSelectActiveView.dimssLoading();
        if (StringUtil.isEmpty(message))
            iSelectActiveView.showToast(message, R.color.load_warning);
    }

    private synchronized void handlerLoadData(List<Contacts> employees) {
        if (ListUtils.isEmpty(employees)) return;//TODO 请求下来时候数据为空
        SelectEmUser user = null;
        List<SelectEmUser> users = new ArrayList<>();
        for (Contacts e : employees) {
            user = new SelectEmUser();
            user.setDefaultorid(0);
            user.setDepart("");
            user.setEmName(e.getName());
            user.setImId(0);
            user.setEmCode("");
            user.setPosition(e.getPhone() != null ? e.getPhone() : "");
            user.setEmId(0);
            users.add(user);
        }
        setUser2Sort(users, false);
    }

    int numSelect;

    private void setUser2Sort(List<SelectEmUser> list, boolean isClickAll) {
        if(!ListUtils.isEmpty(allList))allList.clear();
        numSelect = 0;
        for (SelectEmUser e : list) {
            BaseSortModel<SelectEmUser> mode = new BaseSortModel<>();
            if (isSelected(isClickAll, selectBean.getSelectCode(), e.getEmCode())) {
                numSelect++;
                mode.setClick(true);
            } else {
                mode.setClick(isClickAll);
                if (isClickAll) numSelect++;
            }
            mode.setBean(e);
            setSortCondition(mode);
            allList.add(mode);
        }
        Collections.sort(allList, mBaseComparator);
        OAHttpHelper.getInstance().post(new Runnable() {
            @Override
            public void run() {
                appMobileContact(allList);
            }
        });

    }

    private boolean isSelected(boolean isClickAll, String SelectCode, String emCode) {
        return !isClickAll && !StringUtil.isEmpty(SelectCode) && StringUtil.isInclude(SelectCode, emCode);
    }

    /**
     * 对特定成员进行排序
     *
     * @param mode
     */
    private final void setSortCondition(BaseSortModel<SelectEmUser> mode) {
        SelectEmUser employeesEntity = mode.getBean();
        if (employeesEntity == null) {
            return;
        }
        String name = employeesEntity.getEmName();
        String wholeSpell = PingYinUtil.getPingYin(name);
        if (!TextUtils.isEmpty(wholeSpell)) {
            String firstLetter = Character.toString(wholeSpell.charAt(0));
            iSelectActiveView.addExist(firstLetter);
            mode.setWholeSpell(wholeSpell);
            mode.setFirstLetter(firstLetter);
            mode.setSimpleSpell(PingYinUtil.converterToFirstSpell(name));
        } else {// 如果全拼为空，理论上是一种错误情况，因为这代表着昵称为空
            mode.setWholeSpell("#");
            mode.setFirstLetter("#");
            mode.setSimpleSpell("#");
        }
    }


    private String getTextForSearch(BaseSortModel<SelectEmUser> u) {
        return getByNull(u.getBean().getEmName()) + getByNull(u.getBean().getTag()) + getByNull(u.getBean().getDepart());
    }

    private String getByNull(String str) {
        return StringUtil.isEmpty(str) ? "" : str;
    }

    public void appMobileContact(final List<BaseSortModel<SelectEmUser>> models) {
        if (ListUtils.isEmpty(models)) return;
        String myUserId = MyApplication.getInstance().mLoginUser.getUserId();
        StringBuilder builder = new StringBuilder();
        for (BaseSortModel<SelectEmUser> u : models) {
            SelectEmUser user = u.getBean();
            if (!StringUtil.isEmpty(user.getPosition()))
                builder.append(user.getPosition().trim().replaceAll(" ", "") + ",");
        }
        StringUtil.removieLast(builder);
        Map<String, String> params = new HashMap<>();
        params.put("token", MyApplication.getInstance().mAccessToken);
        params.put("userid", myUserId);
        params.put("telephones", builder.toString());
        LogUtil.d("Test1"," mConfig.APP_MOBILE_CONTACT=" + MyApplication.getInstance().getConfig().APP_QUER_YUSER);
        LogUtil.d("Test1","map=" + JSONUtil.map2JSON(params));
        StringJsonObjectRequest<String> request = new StringJsonObjectRequest<String>(
                MyApplication.getInstance().getConfig().APP_QUER_YUSER, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
//                ToastUtil.showErrorNet(MyApplication.getInstance());
                showModel(models);
            }
        }, new StringJsonObjectRequest.Listener<String>() {
            @Override
            public void onResponse(ObjectResult<String> result) {
                String message = result.toString();
                if (!StringUtil.isEmpty(message)) {
                    hanlderAppQueryuserInThread(message);
                }
            }
        }, String.class, params);
        MyApplication.getInstance().getFastVolley().addDefaultRequest("Volley", request);
    }

    private void hanlderAppQueryuserInThread(String message) {
        if (JSONUtil.validate(message)) {
            JSONObject object = JSON.parseObject(message);
            String resultData = object.getString("resultData");
            object = object.parseObject(resultData);
            String user = object.getString("user");
            JSONArray array = JSON.parseArray(user);
            LogUtil.i("array=" + array);
            if (!ListUtils.isEmpty(array)) {
                JSONObject o = null;
                for (int i = 0; i < array.size(); i++) {
                    o = array.getJSONObject(i);
                    String telephone = JSONUtil.getText(o, "telephone");
                    String isfriend = JSONUtil.getText(o, "isfriend");
                    int _id = JSONUtil.getInt(o, "_id");
                    for (BaseSortModel<SelectEmUser> baseSortModel : allList) {
                        if (isSamePerson(baseSortModel.getBean(), telephone)) {
                            baseSortModel.getBean().setTag(isfriend);
                            baseSortModel.getBean().setImId(_id);
                        }
                    }
                }
            }
        }
        //计算全部数据完成
        showModel(allList);
    }

    private boolean isSamePerson(SelectEmUser user, String telephone) {
        if (user == null || StringUtil.isEmpty(telephone) || StringUtil.isEmpty(user.getPosition()))
            return false;
        if (user.getPosition().replaceAll(" ", "").equals(telephone))
            return true;
        return false;
    }

    private void showModel(List<BaseSortModel<SelectEmUser>> allList) {
        iSelectActiveView.dimssLoading();
        iSelectActiveView.showNumber(numSelect);
        if (allList != null) {
            iSelectActiveView.isAllClicked(numSelect == allList.size());
        }
        iSelectActiveView.showModel(allList);

    }


    //TODO 添加好友
    private void addUser(final SelectEmUser user, final int position) {
        LogUtil.i("addUser");
        if (user == null) {
            return;
        }
        iSelectActiveView.showLoading();
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("access_token", MyApplication.getInstance().mAccessToken);
        params.put("toUserId", String.valueOf(user.getImId()));
        StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
                MyApplication.getInstance().getConfig().FRIENDS_ATTENTION_ADD, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                iSelectActiveView.dimssLoading();
                ToastUtil.showErrorNet(activity);
            }
        }, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
            @Override
            public void onResponse(ObjectResult<AddAttentionResult> result) {
                iSelectActiveView.dimssLoading();
                boolean success = Result.defaultParser(MyApplication.getInstance(), result, true);
                String message = result.toString();
                LogUtil.d("Test","success=" + success);
                LogUtil.d("Test","message=" + message);
                if (success) {
                    iSelectActiveView.showToast(R.string.add_attention_succ, R.color.load_submit);
                    if (ListUtils.getSize(allList) > position && allList.get(position).getBean().getImId() == user.getImId()) {
                        //进行确认是同一个人，防止线程出现的适配器和管理器数据不同步问题
                        allList.get(position).getBean().setTag("1");
                        // 添加为关注
                        User mUser=new User();
                        mUser.setUserId(String.valueOf(user.getImId()));//已经开通了UU IM的人
                        mUser.setNickName(user.getEmName());//手机通讯录的名字
                        // 发送推送的消息
                        NewFriendMessage mess = NewFriendMessage.createWillSendMessage(
                                MyApplication.getInstance().mLoginUser, XmppMessage.TYPE_FRIEND, null, mUser);
                        mXmppService.sendNewFriendMessage(mUser.getUserId(), mess);

                        // 添加为好友
                        NewFriendDao.getInstance().ascensionNewFriend(mess, Friend.STATUS_FRIEND);
                 
                        showModel(allList);
                    }
                }
            }
        }, AddAttentionResult.class, params);
        MyApplication.getInstance().getFastVolley().addDefaultRequest("Volley", request);
    }

    private void invite(SelectEmUser user, int position) {
        LogUtil.i("invite");
        final String name = CommonUtil.getName();
        final String phone = user.getPosition().trim().replaceAll(" ", "");
        if (!StringUtil.isMobileNumber(phone)) {
            iSelectActiveView.showToast("选择人员电话号码为空或是格式不正确", R.color.load_submit);
            return;
        }
        StringJsonObjectRequest<AddAttentionResult> request = new StringJsonObjectRequest<AddAttentionResult>(
                Request.Method.POST, "http://message.ubtob.com/sms/send", new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError arg0) {
                iSelectActiveView.dimssLoading();
            }
        }, new StringJsonObjectRequest.Listener<AddAttentionResult>() {
            @Override
            public void onResponse(ObjectResult<AddAttentionResult> result) {
                String message = result.toString();
                iSelectActiveView.showToast("短信发送成功", R.color.load_submit);
                LogUtil.i("message=" + message);
            }
        }, AddAttentionResult.class, null) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                String param = "{\"receiver\":\"" + phone + "\",\"params\":[\"" + name + "\"],\"templateId\":\"4b60e18b-de2e-410f-9de1-819265d9e636\"}";
                LogUtil.i("param=" + param);
                return param.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };
        MyApplication.getInstance().getFastVolley().addDefaultRequest("Volley", request);
    }

}

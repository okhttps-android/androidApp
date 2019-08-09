package com.xzjmyk.pm.activity.ui.erp.presenter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.thread.ThreadPool;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.EmployeesEntity;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.net.http.http.OAHttpHelper;
import com.core.utils.CommonInterface;
import com.core.utils.CompanyHandlerInfoUtil;
import com.core.utils.sortlist.BaseComparator;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appcontact.db.ContactsDao;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.presenter.imp.ISelectActiveView;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Bitliker on 2017/2/14.
 */

public class SelectCollisionPresenter {
    private ISelectActiveView iSelectActiveView;
    private List<BaseSortModel<SelectEmUser>> allList;
    private BaseComparator<SelectEmUser> mBaseComparator;
    private Activity mActivity;
    private SelectCollisionTurnBean selectBean;

    public SelectCollisionPresenter(ISelectActiveView iSelectActiveView) {
        if (iSelectActiveView == null) new NullPointerException("iSelectActiveView is Null");
        this.iSelectActiveView = iSelectActiveView;
        this.mActivity = (Activity) iSelectActiveView;
    }

    public void start(SelectCollisionTurnBean selectBean) {
        if (selectBean == null) {
            new NullPointerException("Intent cannot be Null");
        }
        this.selectBean = selectBean;
        allList = new ArrayList<>();
        mBaseComparator = new BaseComparator<>();
        initdata();
    }

    private void initdata() {
        List<SelectEmUser> selects = selectBean.getSelectList();
        if (!StringUtil.isEmpty(selectBean.getSureText()))
            iSelectActiveView.showSureText(selectBean.getSureText());
        if (ListUtils.isEmpty(selects)) {
            if (selectBean.getTitle().equals(StringUtil.getMessage(R.string.select_share_friend))) {//分享好友
                loadShareFriendInThread(); //企业架构+UU好友
            } else {
                switch (selectBean.getResultCode()) {
                    case 0x001:
                        loadBookingCPersons(selectBean.getTitle());
                        break;
                    default:
                        loadDataInThread(); //企业架构
                        break;
                }
            }
        } else {
            setUser2Sort(selects, true);
        }
    }

    public void changeChecked(boolean b, List<BaseSortModel<SelectEmUser>> listData) {
        if (ListUtils.isEmpty(listData)) {
            //TODO
        } else {
            for (int i = 0; i < listData.size(); i++) {
                listData.get(i).setClick(b);
            }
            iSelectActiveView.showModel(listData);
            ArrayList<SelectEmUser> chche = getClickNumber();
            iSelectActiveView.showNumber(chche.size());
        }
    }

    public void search(Editable s) throws Exception {
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


    private void loadShareFriendInThread() {
        iSelectActiveView.showLoading();
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                List<ContactsModel> models = ContactsDao.getInstance().findByShare();
                if (!ListUtils.isEmpty(models)) {
                    handlerLoadShareData(models);
                } else {
                    //联网加载
                }
            }
        });
    }

    /*开启线程获取数据*/
    private void loadDataInThread() {
        iSelectActiveView.showLoading();
        ThreadPool.getThreadPool().addTask(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<EmployeesEntity> employees = getEmListByDB();
                    OAHttpHelper.getInstance().post(new Runnable() {
                        @Override
                        public void run() {
                            ThreadPool.getThreadPool().addTask(new Runnable() {
                                @Override
                                public void run() {
                                    if (ListUtils.isEmpty(employees))
                                        loadDataByNet();
                                    else handlerLoadData(employees);
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

    private List<EmployeesEntity> getEmListByDB() throws Exception {
        DBManager manager = new DBManager();
        String master = CommonUtil.getMaster();
        List<EmployeesEntity> emList = null;
        if (!StringUtil.isEmpty(master)) {
            emList = manager.select_getEmployee(new String[]{master}, "whichsys=?");
        }
        manager.closeDB();
        return emList;
    }

    private void loadDataByNet() {
        CommonInterface.getInstance().loadCompanyData(new CommonInterface.OnResultListener() {
            @Override
            public void result(@NonNull boolean success, @NonNull int what, @Nullable String message) {
                try {
                    if (success) {
                        JSONObject object = JSON.parseObject(message);
                        List<EmployeesEntity> employees = CompanyHandlerInfoUtil.getEmployees(object);
                        handlerLoadData(employees);
                    } else handerError(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void handerError(String message) {
        iSelectActiveView.dimssLoading();
        if (StringUtil.isEmpty(message))
            iSelectActiveView.showToast(message, R.color.load_warning);
    }


    private void handlerLoadShareData(List<ContactsModel> employees) {
        if (ListUtils.isEmpty(employees)) return;//TODO 请求下来时候数据为空
        List<SelectEmUser> users = new ArrayList<>();
        SelectEmUser user = null;
        for (ContactsModel e : employees) {
            user = new SelectEmUser();
            user.setEmName(e.getName());
            user.setImId(Integer.valueOf(e.getImid()));
            users.add(user);
        }
        setUser2Sort(users, false);
    }

    private void handlerLoadData(List<EmployeesEntity> employees) {

        if (ListUtils.isEmpty(employees)) return;//TODO 请求下来时候数据为空
        SelectEmUser user = null;
        List<SelectEmUser> users = new ArrayList<>();
        for (EmployeesEntity e : employees) {
            user = new SelectEmUser();
            user.setDefaultorid(e.getEm_defaultorid());
            user.setDepart(e.getEM_DEPART());
            user.setEmName(e.getEM_NAME());
            user.setImId(e.getEm_IMID());
            user.setEmCode(e.getEM_CODE());
            user.setPosition(e.getEM_POSITION());
            user.setEmId(e.getEM_ID());
            users.add(user);
        }
        setUser2Sort(users, false);
    }

    int numSelect;

    private void setUser2Sort(List<SelectEmUser> list, boolean isClickAll) {
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
            //判断界面类型决定是否排序
            if (!selectBean.getTitle().equals(mActivity.getString(R.string.select_share_friend))) {
                setSortCondition(mode);
            }
            allList.add(mode);
        }
        if (!selectBean.getTitle().equals(mActivity.getString(R.string.select_share_friend))) {
            Collections.sort(allList, mBaseComparator);
        }

        //计算全部数据完成
        OAHttpHelper.getInstance().post(new Runnable() {
            @Override
            public void run() {
                iSelectActiveView.dimssLoading();
                iSelectActiveView.showNumber(numSelect);
                iSelectActiveView.isAllClicked(numSelect == allList.size());
                iSelectActiveView.showModel(allList);
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


    public void loadBookingCPersons(String name) {
        iSelectActiveView.showLoading();
        //https://account.ubtob.com/api/userspace/userSpaceDetail/keyword?keyword=优软科技有限&pageNumber=1
        HttpClient httpClient = new HttpClient.Builder("https://account.ubtob.com").build();
        httpClient.Api().send(new HttpClient.Builder()
                        .url("api/userspace/employees")
                .add("name",name)
                        .add("pageNumber","1")
                .method(Method.GET)
                        .build(),
                new ResultSubscriber<>(new ResultListener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        iSelectActiveView.dimssLoading();
                        LogUtil.d("ResultSubscriber",""+o.toString());
                        if (JSONUtil.validate(o.toString())){
                            JSONArray jsonArray=JSON.parseObject(o.toString()).getJSONArray("listdata");
                            if (!ListUtils.isEmpty(jsonArray)){
                                SelectEmUser user = null;
                                List<SelectEmUser> users = new ArrayList<>();
                                for (int i=0;i<jsonArray.size();i++){
                                    user=new SelectEmUser();
                                    user.setImId(Integer.valueOf(jsonArray.getJSONObject(i).getString("imid")));
                                    user.setEmName(jsonArray.getJSONObject(i).getString("username"));//员工名
                                   // user.setPosition(jsonArray.getJSONObject(i).getString("address"));//公司
                                    user.setEmCode(selectBean.getTitle());//公司名
                                    user.setDepart(jsonArray.getJSONObject(i).getString("usertel"));//手机号
                                    users.add(user);
                                }
                                setUser2Sort(users,false);
                            }
                        }
                    }
                }));

    }
}

package com.uas.appworks.crm3_0.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.PermissionUtil;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.core.utils.sortlist.BaseComparator;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.core.utils.sortlist.SideBar;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Tags;
import com.uas.appcontact.model.contacts.Contacts;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appcontact.utils.ContactsUtils;
import com.uas.appworks.R;
import com.uas.appworks.crm3_0.adapter.ContactLocalSortAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalContactsListFragment extends ViewPagerLazyFragment implements OnSmartHttpListener,SideBar.OnTouchingLetterChangedListener {
    private static final String TAG = "LocalContactsListFragment";
//    private ContactsLocalAdapter adapter;
//    private PullToRefreshListView mListView;
    private List<ContactsModel> models = new ArrayList<>();
    
    
    StickyListHeadersListView refreshListView;
    private BaseComparator comparator;
    private List<BaseSortModel<ContactsModel>> allDatas=new ArrayList<>();
    ContactLocalSortAdapter mAdapter;
    SideBar sideBar;
    TextView dialogTV;
   
//    private int page;

    public static  LocalContactsListFragment newInstance(int tabItem) {
        Bundle args = new Bundle();
        args.putInt("tabItem", tabItem);
        LocalContactsListFragment fragment = new   LocalContactsListFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onSuccess(int what, String message, Tags tag) throws Exception {
        
    }

    @Override
    public void onFailure(int what, String message, Tags tag) throws Exception {

    }

    @Override
    protected void LazyData() {
           initView();
           
           initData();
    }

 

    private void initView() {
        refreshListView =  findViewById(R.id.mListView);
        mAdapter = new ContactLocalSortAdapter(ct, allDatas);
        mAdapter.setFrameLayout(getContentView());
        refreshListView.setAdapter(mAdapter);

        dialogTV = findViewById(R.id.dialogTV);
        sideBar = findViewById(R.id.sidebar);
        sideBar.setTextView(dialogTV);
        sideBar.setOnTouchingLetterChangedListener(this);
        comparator = new BaseComparator();

    }

    private void initData() {
//        adapter=new ContactsLocalAdapter(ct,models);
//        mListView.setAdapter(adapter);
        String[] permissions = {Manifest.permission.READ_CONTACTS};
        if (PermissionUtil.lacksPermissions(ct, permissions)) {
            PermissionUtil.requestPermission(getActivity(), PermissionUtil.DEFAULT_REQUEST, permissions);
        } else {
            loadLocalContacts();
            isHasPermiss = true;
            LogUtil.d(TAG, "有权限@....");
        }
      
    }
    
    
    private void loadLocalContacts(){
        List<Contacts> contacts = ContactsUtils.getContacts1();
        LogUtil.d(TAG, JSON.toJSONString(contacts));
        if (contacts != null) {
            for (Contacts entity : contacts) {
                ContactsModel model = new ContactsModel();
                model.setImid("0");
                model.setName(StringUtil.isEmpty(entity.getName()) ? entity.getNickname() : entity.getName());
                model.setType(3);
                model.setEmail("");
                model.setOwnerId(MyApplication.getInstance().mLoginUser.getUserId());
                model.setPhone(entity.getPhone());
                model.setWhichsys(CommonUtil.getSharedPreferences(MyApplication.getInstance(), "erp_master"));
                model.setCompany("");
                models.add(model);
            }
        }
        
        allDatas.clear();
        allDatas= getAllDatas(models);
        mAdapter.setData(allDatas);
    }

    @Override
    protected String getBaseUrl() {
        return null;
    }

    @Override
    protected int inflater() {
        return R.layout.fragment_local_contacts_list;
    }

    boolean isHasPermiss = false;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtil.DEFAULT_REQUEST) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                isHasPermiss = false;
                loadLocalContacts();
                LogUtil.d(TAG, "第一次没有权限....");
            } else {
                isHasPermiss = true;
                loadLocalContacts();
                LogUtil.d(TAG, "之后就有权限....");
            }
        }
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = mAdapter.getPositionForSection(s.charAt(0));
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


    private List<BaseSortModel<ContactsModel>> getAllDatas(List<ContactsModel> emList) {
        if (ListUtils.isEmpty(emList)) return null;
        List<BaseSortModel<ContactsModel>> list = new ArrayList<>();
        for (ContactsModel e : emList) {

            BaseSortModel<ContactsModel> mode = new BaseSortModel<>();
            mode.setBean(e);

            ContactsModel friend = mode.getBean();
            if (friend == null) {
                break;
            }
            String name = friend.getName();
            String wholeSpell = PingYinUtil.getPingYin(name);
            if (!StringUtil.isEmpty(wholeSpell)) {
                try {
                    String firstLetter = Character.toString(wholeSpell.charAt(0));
                    sideBar.addExist(firstLetter);
                    mode.setWholeSpell(wholeSpell);
                    mode.setFirstLetter(firstLetter);
                    mode.setSimpleSpell(PingYinUtil.converterToFirstSpell(name));
                } catch (Exception o) {
                    o.printStackTrace();
                }
            } else {// 如果全拼为空，理论上是一种错误情况，因为这代表着昵称为空
                mode.setWholeSpell("#");
                mode.setFirstLetter("#");
                mode.setSimpleSpell("#");
            }

            list.add(mode);
        }
        if (ListUtils.isEmpty(list)) {
            list = new ArrayList<>();
        } else {
            Collections.sort(list, comparator);
        }
        return list;
    }
}

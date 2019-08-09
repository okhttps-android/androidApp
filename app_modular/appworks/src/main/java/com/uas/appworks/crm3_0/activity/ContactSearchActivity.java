package com.uas.appworks.crm3_0.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.LogUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.widget.MenuVoiceSearchView;
import com.uas.appcontact.model.contacts.Contacts;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appcontact.utils.ContactsUtils;
import com.uas.appworks.R;
import com.uas.appworks.crm3_0.adapter.ContactsLocalAdapter;
import com.uas.appworks.crm3_0.fragment.ContactsListFragment;
import com.uas.appworks.crm3_0.model.ContactsBean;

import java.util.ArrayList;
import java.util.List;

public class ContactSearchActivity extends BaseActivity implements ContactsLocalAdapter.ResultItemsInface {

    private ListView lv_contact;
    private ListView lv_local;
    private MenuVoiceSearchView mVoiceSearchView;
    private ContactsLocalAdapter adapter;
    ContactsListFragment.ItemContactsMeAdapter iAdapter;
    private List<ContactsModel> models = new ArrayList<>();
    List<ContactsBean> datas=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        iAdapter=new ContactsListFragment().new ItemContactsMeAdapter(ct,datas);
        adapter=new ContactsLocalAdapter(mContext,models);
        
        lv_contact=findViewById(R.id.lv_contact);
        lv_local=findViewById(R.id.lv_local);
        
        
        lv_contact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ContactsListFragment.ItemContactsMeAdapter.ViewHolder viewHolder= (ContactsListFragment.ItemContactsMeAdapter.ViewHolder) view.getTag();
                if (StringUtil.isEmpty(viewHolder.bean.getCompanyName())){
                    viewHolder.bean.setCompanyName("未填写");
                }
                if (StringUtil.isEmpty(viewHolder.bean.getDepartment())){
                    viewHolder.bean.setDepartment("未填写");
                }
                if (StringUtil.isEmpty(viewHolder.bean.getPosition())){
                    viewHolder.bean.setPosition("未填写");
                }
                if (StringUtil.isEmpty(viewHolder.bean.getNotes())){
                    viewHolder.bean.setNotes("未填写");
                }
                startActivity(new Intent(mContext, ContactsDetialActivity.class)
                        .putExtra("model",viewHolder.bean));
             
            }
        });
        
        
        
        mVoiceSearchView = findViewById(R.id.mVoiceSearchView);
        findViewById(R.id.backImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        findViewById(R.id.addImg).setVisibility(View.GONE);
      


        mVoiceSearchView.getSearch_edit().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!StringUtil.isEmpty(editable.toString())){
                    //我的联系人
                    HttpClient httpClient = new HttpClient.Builder("https://mobile.ubtob.com:8443/linkman/")
                            .isDebug(true)
                            .build();
                    httpClient.Api().send(new HttpClient.Builder("mobile/contactList")
                            .add("key",editable.toString())
                            .add("imid", MyApplication.getInstance().getLoginUserId())
                            .method(Method.GET)
                            .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {

                        @Override
                        public void onResponse(Object o) {
                            try {
                                datas.clear();
//                                datas= JSON.parseObject(JSON.parseObject(o.toString()).getJSONArray("data").toJSONString()
//                                        ,new TypeReference<List<ContactsBean>>(){});
                                datas.addAll(JSON.parseObject(JSON.parseObject(o.toString()).getJSONArray("data").toJSONString()
                                        ,new TypeReference<List<ContactsBean>>(){}));
                                lv_contact.setAdapter(iAdapter);
                                iAdapter.notifyDataSetChanged();
                                LogUtil.d(TAG,JSON.toJSONString(datas));
                            }catch (Exception e){

                            }
                        }
                    }));
                    
                    //本地通讯录
                    //主要权限问题
                    models.clear();
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
                            if (model.getName().contains(editable.toString())||model.getPhone().equals(editable.toString())){
                                models.add(model);
                            }
                        }
                    }
                    LogUtil.d(TAG,"models:"+ JSON.toJSONString( models));
                    lv_local.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    
                    
                }
            }
        });
    }

    @Override
    public int getToolBarId() {
        return R.id.cycleCountToolBar;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_contact_search;
    }

    @Override
    public void onResultForItems(View view, ContactsModel model, int position) {
        
    }
}

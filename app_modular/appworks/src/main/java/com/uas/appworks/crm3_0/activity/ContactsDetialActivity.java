package com.uas.appworks.crm3_0.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.file.FileUtils;
import com.common.system.DisplayUtil;
import com.common.system.SystemUtil;
import com.common.ui.ImageUtil;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.utils.ToastUtil;
import com.core.utils.helper.AvatarHelper;
import com.core.widget.view.ListViewInScroller;
import com.core.widget.view.SwitchView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appcontact.model.contacts.ContactsModel;
import com.uas.appcontact.ui.activity.ContactsActivity;
import com.uas.appworks.R;
import com.uas.appworks.activity.SchedulerCreateActivity;
import com.uas.appworks.crm3_0.inface.OnItemsButtonAddInface;
import com.uas.appworks.crm3_0.model.ContactsBean;
import com.uas.appworks.datainquiry.Constants;
import com.uas.appworks.model.Schedule;
import com.uas.appworks.utils.PhoneContactsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.card.payment.CardIOActivity;


/**
 * @desc:联系人新增+动态表单界面
 * @author：Arison on 2018/9/11
 */
public class ContactsDetialActivity extends BaseActivity implements OnItemsButtonAddInface {

    private static final String TAG = "ContactsAddActivity";
  
    ListViewInScroller mPhones;
    ListViewInScroller mTels;
    ListViewInScroller mEmails;

    ItemsContactLocalAddAdapter adapterPhone;
    ItemsContactLocalAddAdapter adapterTel;
    ItemsContactLocalAddAdapter adapterEmails;

    List<ListItems> pData = new ArrayList<>();
    List<ListItems> tData = new ArrayList<>();
    List<ListItems> eData = new ArrayList<>();

    ScrollView svContent;
    RelativeLayout rlImages;
    TextView tvUpload;
    ImageView ivUpload;
    LinearLayout llBasic;
    TextView tvTitle;
    TextView tvSex;
    TextView tvAge;
    TextView tvPosition;
    TextView tvDepart;
    TextView tvNotes;
    TextView tvIsMarked;
    SwitchView etIsMarked;
    SwitchView etIsAddLocal;
    LinearLayout llBottom;
    TextView tvUnmanger;
    TextView tvManged;
    TextView tvTimeout;
    TextView tvTranstered;
    
    ImageView ivPerson;
    
    
    int orderId = 0;
    boolean isMarked = true;
    boolean isAddLocal = true;
    boolean isEditEnable=true;
    private ImageView iv_header;
    private static final int UPDATE_SEX = 6;
    private String imgUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_add);
        initView();
        initData();
    }
 
    ContactsBean model;
    private void initView() {
        super.setTitle("我的联系人");
   
        if (getIntent()!=null){
           model=getIntent().getParcelableExtra("model");
        }
        LogUtil.d(TAG,"model:"+JSON.toJSONString(model));
        
        mPhones = findViewById(R.id.lv_phones);
        mTels = findViewById(R.id.lv_tels);
        mEmails = findViewById(R.id.lv_emails);
        
        

        
        ivPerson=findViewById(R.id.iv_person);
        svContent = (ScrollView) findViewById(R.id.sv_content);
        rlImages = (RelativeLayout) findViewById(R.id.rl_images);
        tvUpload = (TextView) findViewById(R.id.tv_upload);
        ivUpload = (ImageView) findViewById(R.id.iv_upload);
        llBasic = (LinearLayout) findViewById(R.id.ll_basic);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvSex = (TextView) findViewById(R.id.tv_sex);
        tvAge = (TextView) findViewById(R.id.tv_age);
        tvPosition = (TextView) findViewById(R.id.tv_position);
        tvDepart = (TextView) findViewById(R.id.tv_depart);
        tvNotes = (TextView) findViewById(R.id.tv_notes);
        tvIsMarked = (TextView) findViewById(R.id.tv_isMarked);
        etIsMarked = (SwitchView) findViewById(R.id.et_isMarked);
        etIsAddLocal=findViewById(R.id.et_isAddLocal);

        llBottom = (LinearLayout) findViewById(R.id.ll_bottom);
        tvUnmanger = (TextView) findViewById(R.id.tv_unmanger);
        tvManged = (TextView) findViewById(R.id.tv_manged);
        tvTimeout = (TextView) findViewById(R.id.tv_timeout);
        tvTranstered = (TextView) findViewById(R.id.tv_transtered);
        iv_header = findViewById(R.id.iv_header);


        llBottom.setVisibility(View.VISIBLE);
        tvTranstered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupWindow(view);
            }
        });
        
        etIsMarked.setChecked(true);
        etIsMarked.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked) {
                    isMarked = true;
                } else {
                    isMarked = false;
                }
            }
        });
        
        etIsAddLocal.setChecked(true);
        etIsAddLocal.setOnCheckedChangeListener(new SwitchView.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean isChecked) {
                if (isChecked){
                    isAddLocal=true;
                }else{
                    isAddLocal=false;
                }
            }
        });


        findViewById(R.id.tv_unmanger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phone=  pData.get(0).getValue();
                SystemUtil.phoneAction(mContext,phone);
            }
        });



      findViewById(R.id.tv_manged).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              Schedule mSchedule = new Schedule(false);
              mSchedule.setTitle("@"+getEtName().getText().toString());
              mSchedule.setRemarks("@"+getEtName().getText().toString());
              mSchedule.setDetails("@"+getEtName().getText().toString());
              startActivity(new Intent(ct, SchedulerCreateActivity.class)
                      .putExtra(Constants.Intents.ENABLE, true)
                      .putExtra(Constants.Intents.MODEL, mSchedule));
          }
      });
      
      findViewById(R.id.tv_timeout).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              if (!ListUtils.isEmpty(pData)){
                  String phones = "";
                  for (int i = 0; i < pData.size(); i++) {
                      if (!StringUtil.isEmpty(pData.get(i).getValue())) {
                          if (i == pData.size() - 1) {
                              phones = phones + pData.get(i).getValue();
                          } else {
                              phones = phones + pData.get(i).getValue() + "/";
                          }
                      }else{
                          ToastMessage("请输入手机号！");
                          return;
                      }
                  }
              }
              startActivity(new Intent(mContext,BillInputBindActivity.class)
                      .putExtra(com.core.app.Constants.Intents.CALLER, "Contact")
                      .putExtra(com.core.app.Constants.Intents.TITLE, "客户联系人")
                      .putExtra("phone",pData.get(0).getValue())
                      .putExtra("name",getEtName().getText().toString())
                      .putExtra(com.core.app.Constants.Intents.ID, 0));
          }
      });
        
        ivPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ContactsActivity.class);
                intent.putExtra("type", 1);
                intent.putExtra("title", "联系人");
                startActivityForResult(intent, 0x01);
            }
        });
        
        tvUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardIOActivity.activityStart(ContactsDetialActivity.this, 0xFF00C5DC, 100);
            }
        });

        getEtSex().setKeyListener(null);
        getEtSex().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.modular.me.UpdateSexActivity");
                intent.putExtra("sex", getEtSex().getText().toString().trim());
                startActivityForResult(intent, UPDATE_SEX);
            }
        });
        //编辑状态
        if (model!=null){
            //默认是编辑状态
            isEditEnable=false;
            
            getEtName().setKeyListener(null);
            getEtAge().setKeyListener(null);
            getEtCompany().setKeyListener(null);
            getEtDepart().setKeyListener(null);
            getEtPosition().setKeyListener(null);
            getEtNotes().setKeyListener(null);
            getEtSex().setEnabled(false);
            getEtSex().setFocusable(false);
            etIsMarked.setEnabled(false);
            etIsAddLocal.setEnabled(false);
            ivPerson.setEnabled(false);
            
            
            
            orderId=model.getLinkmanId();
            getEtName().setText(model.getName());
            getEtCompany().setText(model.getCompanyName());
            LogUtil.d(TAG,"model.getImageUrl:"+model.getImageUrl());
          if (!StringUtil.isEmpty(model.getImageUrl())&&!"\"null\"".equals(model.getImageUrl())){
              AvatarHelper.getInstance().display("https://mobile.ubtob.com:8443/linkman/"+model.getImageUrl(),iv_header,false);
              tvUpload.setVisibility(View.GONE);
           }else{
              tvUpload.setText("未上传名片");
              tvUpload.setEnabled(false);
           }
            
            if ("1".equals(model.getSex())){
                getEtSex().setText("女");
            }else{
                getEtSex().setText("男");
            }
            getEtAge().setText(model.getAge()+"");
            getEtPosition().setText(model.getPosition());
            getEtDepart().setText(model.getDepartment());
            getEtNotes().setText(model.getNotes());
            if (model.getIsDMakers()==0){
                etIsMarked.setChecked(false);
            }else{
                etIsMarked.setChecked(true);
            }
            
            //解析手机号码，座机号码，邮件号

            String phones[]= new String[0];
            String tels[]= new String[0];
            String emails[]= new String[0];
            try {
                phones = model.getPhone().split("/");
                tels = model.getTel().split("/");
                emails = model.getEmail().split("/");
            } catch (Exception e) {
                e.printStackTrace();
            }

            pData.clear();
            for (int i=0;i<phones.length;i++){
                if (i==phones.length-1){
                    ListItems items = new ListItems();
                    items.setName("手机号");
                    items.setHink("请输入");
                    items.setValue(phones[i]);
                    items.setAction("新增");
                    pData.add(items);
                }else{
                    ListItems items = new ListItems();
                    items.setName("手机号");
                    items.setHink("请输入");
                    items.setValue(phones[i]);
                    items.setAction("删除");
                    pData.add(items);
                }
            }
            
            
             
            tData.clear();
            for (int i=0;i<tels.length;i++){
                if(i==tels.length-1){
                    ListItems items = new ListItems();
                    items.setName("座机号");
                    items.setHink("请输入");
                    items.setValue(tels[i]);
                    items.setAction("新增");
                    tData.add(items);
                }else{
                    ListItems items = new ListItems();
                    items.setName("座机号");
                    items.setHink("请输入");
                    items.setValue(tels[i]);
                    items.setAction("删除");
                    tData.add(items);
                }
              
            }

             eData.clear();
            for (int i=0;i<emails.length;i++){
                if (i==emails.length-1){
                    ListItems items = new ListItems();
                    items.setName("Email");
                    items.setHink("请输入");
                    items.setValue(emails[i]);
                    items.setAction("新增");
                    eData.add(items);
                }else{
                    ListItems items = new ListItems();
                    items.setName("Email");
                    items.setHink("请输入");
                    items.setValue(emails[i]);
                    items.setAction("删除");
                    eData.add(items);
                }
           
            }

            
            if (adapterPhone==null){
                adapterPhone = new ItemsContactLocalAddAdapter(mContext, pData, this);
                adapterPhone.setTagId(0);
                mPhones.setAdapter(adapterPhone);
            }else{
                adapterPhone.notifyDataSetChanged();
            }
            if (adapterTel==null){
                adapterTel = new ItemsContactLocalAddAdapter(mContext, tData, this);
                adapterTel.setNeed(false);
                adapterTel.setTagId(1);
                mTels.setAdapter(adapterTel);
            }else{
                adapterTel.notifyDataSetChanged();
            }

            if (adapterEmails==null){
                adapterEmails = new ItemsContactLocalAddAdapter(mContext, eData, this);
                adapterEmails.setNeed(false);
                adapterEmails.setTagId(2);
                mEmails.setAdapter(adapterEmails);
            }else{
                adapterEmails.notifyDataSetChanged();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null != data && data.hasExtra(CardIOActivity.EXTRA_CAPTURED_CARD_IMAGE)) {
            byte[] byteArrayExtra = data.getByteArrayExtra(CardIOActivity.EXTRA_CAPTURED_CARD_IMAGE);
            int length = byteArrayExtra.length;
            System.out.println("压缩前的大小====" + (length / 1024f / 1024f) + " M");
//            tv.setText((length / 1024f / 1024f) + " M");
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArrayExtra, 0, length);
            iv_header.setImageBitmap(bitmap);
            
            tvUpload.setVisibility(View.INVISIBLE);
            ivUpload.setVisibility(View.INVISIBLE);
             File file=  ImageUtil.saveBitmapFile(bitmap, FileUtils.getSDRoot()+"/uu/linkheadtop.png");
             upload(file);
            //  bitmapBase64 = bitmapToBase64(bitmap);
        }


        if (requestCode == UPDATE_SEX && data != null) {
          String  mSex = data.getStringExtra("newsex");
            if (mSex != null) {
                getEtSex().setText(mSex);
//                if (mSex.equals("男")) {
//                    mTempData.setSex(1);
//                } else if (mSex.equals("女")) {
//                    mTempData.setSex(0);
//                }
//                updateData();
            }
        }
        if(requestCode==0x01){
            try {
                ContactsModel model = data.getParcelableExtra("data");
                getEtName().setText(model.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void initData() {
         if (model==null){
             ListItems items = new ListItems();
             items.setName("手机号");
             items.setHink("请输入");

             pData.add(items);

             ListItems tItems = new ListItems();
             tItems.setName("座机号");
             tItems.setHink("请输入");
             tData.add(tItems);

             ListItems eTems = new ListItems();
             eTems.setName("Email");
             eTems.setHink("请输入");
             eData.add(eTems);
         }
        adapterPhone = new ItemsContactLocalAddAdapter(mContext, pData, this);
        adapterPhone.setTagId(0);
        adapterTel = new ItemsContactLocalAddAdapter(mContext, tData, this);
        adapterTel.setNeed(false);
        adapterTel.setTagId(1);
        adapterEmails = new ItemsContactLocalAddAdapter(mContext, eData, this);
        adapterEmails.setNeed(false);
        adapterEmails.setTagId(2);
        mPhones.setAdapter(adapterPhone);

        mTels.setAdapter(adapterTel);

        mEmails.setAdapter(adapterEmails);

    }
    
    
    private void upload(File file){
        LogUtil.d(TAG,"file:"+file.getAbsolutePath());
        HttpClient httpClient = new HttpClient.Builder("https://mobile.ubtob.com:8443/linkman/")
                .isDebug(true)
                .build();
        httpClient.Api().uploads(new HttpClient.Builder("mobile/upload")
        .add("file",file)
        .method(Method.POST)
        .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object s) {
                LogUtil.d(TAG, JSON.toJSONString("result:"+s));
              imgUrl=  JSON.parseObject(s.toString()).getString("data");
            }
        }));
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (model!=null){
            menu.findItem(R.id.sure).setTitle("编辑");
        }else{
            menu.findItem(R.id.sure).setTitle("保存");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back_sure, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.sure) {
            if (MyApplication.getInstance().isNetworkActive()) {
               // saveData(orderId);
                startActivity(new Intent(mContext, ContactsAddActivity.class)
                        .putExtra("model",model));
            } else {
                ToastUtil.showToast(ct, R.string.networks_out);
            }
        }
        return true;
    }


    public void saveData( int Id) {
        //检查必填项
        if (StringUtil.isEmpty(getEtName().getText().toString())) {
            ToastMessage("请输入名字！");
            return;
        }
        if (StringUtil.isEmpty(getEtSex().getText().toString())) {
            ToastMessage("请输入性别！");
            return;
        }
//        if (StringUtil.isEmpty(getEtAge().getText().toString())) {
//            ToastMessage("请输入年龄！");
//            return;
//        }

        String phones = "";
        String tels = "";
        String emails = "";

        for (int i = 0; i < pData.size(); i++) {
            if (!StringUtil.isEmpty(pData.get(i).getValue())) {
                if (i == pData.size() - 1) {
                    phones = phones + pData.get(i).getValue();
                } else {
                    phones = phones + pData.get(i).getValue() + "/";
                }
            }else{
                ToastMessage("请输入手机号！");
                return;
            }
        }

        for (int i = 0; i < tData.size(); i++) {
            if (!StringUtil.isEmpty(tData.get(i).getValue())) {
                if (i == tData.size() - 1) {
                    tels = tels + tData.get(i).getValue();
                } else {
                    tels = tels + tData.get(i).getValue() + "/";
                }
            }
        }

        for (int i = 0; i < eData.size(); i++) {
            if (!StringUtil.isEmpty(eData.get(i).getValue())) {
                if (i == eData.size() - 1) {
                    emails = emails + eData.get(i).getValue();
                } else {
                    emails = emails + eData.get(i).getValue() + "/";
                }
            }
        }

        int isMarks = 0;
        if (isMarked) {
            isMarks = 1;
        } else {
            isMarks = 0;
        }
        int sexTag=0;
        if ("男".equals(getEtSex().getText().toString())){
            sexTag=0;
        }else{
            sexTag=1;
        }
        
        if(model!=null){
            imgUrl=model.getImageUrl();
        }
  
        
        
        if (orderId!=0){
            String jsonData = "{" +
                    "\"linkmanId\":" + model.getLinkmanId() + "," +
                    "\"imid\":" + MyApplication.getInstance().mLoginUser.getUserId() + "," +
                    "\"companyName\":\"" + getEtCompany().getText().toString() + "\"," +
                    "\"name\":\"" + getEtName().getText().toString() + "\"" +
                    ",\"sex\":" + sexTag + "," +
                    "\"age\":" + getEtAge().getText().toString() + "," +
                    "\"position\":\"" + getEtPosition().getText().toString() + "\"," +
                    "\"department\":\"" + getEtDepart().getText().toString() + "\"," +
                    "\"brithday\":\"2018-05-06\"," +
                    "\"isDMakers\":" + isMarks + "," +
                    "\"notes\":\"" + getEtNotes().getText().toString() + "\"," +
                    "\"phone\":\"" + phones + "\"," +
                    "\"email\":\"" + emails + "\"," +
                    "\"tel\":\"" + tels + "\"," +
                    "\"imageUrl\":\""+imgUrl+"\"" +
                    "}";
            LogUtil.d(TAG,"jsonData:"+JSON.toJSONString(jsonData));
            HttpClient httpClient = new HttpClient.Builder("https://mobile.ubtob.com:8443/linkman/")
                    .isDebug(true)
                    .build();
            httpClient.Api().send(new HttpClient.Builder("mobile/contactUpdate")
                    .add("jsonFile",jsonData)
                    .method(Method.POST)
                    .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {

                @Override
                public void onResponse(Object o) {
                    LogUtil.d(TAG,JSON.toJSONString(o));

                    try {
                        orderId=JSON.parseObject(o.toString()).getInteger("data");
                        ToastMessage("更新成功");
                    }catch (Exception e){

                    }
                }
            }));
        }else{
            String jsonData = "{\"imid\":" + MyApplication.getInstance().mLoginUser.getUserId() + "," +
                    "\"companyName\":\"" +getEtCompany().getText().toString()+ "\"," +
                    "\"name\":\"" + getEtName().getText().toString() + "\"" +
                    ",\"sex\":" + sexTag + "," +
                    "\"age\":" + getEtAge().getText().toString() + "," +
                    "\"position\":\"" + getEtPosition().getText().toString() + "\"," +
                    "\"department\":\"" + getEtDepart().getText().toString() + "\"," +
                    "\"brithday\":\"2018-05-06\"," +
                    "\"isDMakers\":" + isMarks + "," +
                    "\"notes\":\"" + getEtNotes().getText().toString() + "\"," +
                    "\"phone\":\"" + phones + "\"," +
                    "\"email\":\"" + emails + "\"," +
                    "\"tel\":\"" + tels + "\"," +
                    "\"imageUrl\":\""+imgUrl+"\"" +
                    "}";
            //
            LogUtil.d(TAG, "jsonData:" + jsonData);
            HttpClient httpClient = new HttpClient.Builder("https://mobile.ubtob.com:8443/linkman/")
                    .isDebug(true)
                    .build();
            httpClient.Api().send(new HttpClient.Builder("mobile/contactAdd")
                    .add("jsonFile",jsonData)
                    .method(Method.POST)
                    .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {

                @Override
                public void onResponse(Object o) {
                    LogUtil.d(TAG,JSON.toJSONString(o));

                    try {
                        orderId=JSON.parseObject(o.toString()).getInteger("data");
                        ToastMessage("保存成功，单据ID："+orderId);
                        startActivity(new Intent(mContext,ContactsListActivity.class));
                        finish();
                    }catch (Exception e){

                    }
                }
            }));
        }
        
        if (isAddLocal){
            if (!ListUtils.isEmpty(pData)) {
                List<String> tPhones = new ArrayList<>();
                for (int i = 0; i < pData.size(); i++) {
                    tPhones.add(pData.get(i).getValue());
                }
                String email="";
                if (!ListUtils.isEmpty(eData)){
                     email=eData.get(0).getValue();
                }
                //同步到手机通讯录
                LogUtil.d(TAG,"保存到本地通讯录："+JSON.toJSONString(tPhones));
                try {
                    PhoneContactsUtils.deleteContact(this,getEtName().getText().toString());
                    PhoneContactsUtils.addContact(this, getEtName().getText().toString(),tPhones,email,getEtCompany().getText().toString(),
                            getEtPosition().getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
   
        
    }

    @Override
    public void onItemsClick(View view, int id, int position, Object object) {
        switch (id) {
            case 0:
                ListItems model = (ListItems) object;
                if ("删除".equals(model.getAction())) {
                    pData.remove(model);

                } else {
                    //判别是否是删除操作，还是新增操作
                    ListItems items = new ListItems();
                    items.setName("手机号");
                    items.setHink("请输入");
                    items.setAction("新增");
                    pData.add(items);

                    for (int i = 0; i < pData.size(); i++) {
                        if (i == pData.size() - 1) {
                            pData.get(i).setAction("新增");
                        } else {
                            pData.get(i).setAction("删除");

                        }
                    }

                }
                adapterPhone.notifyDataSetChanged();
                break;
            case 1:
                ListItems tItems =  (ListItems) object;
                if ("删除".equals(tItems.getAction())) {
                    tData.remove(tItems);

                } else {
                    //判别是否是删除操作，还是新增操作
                    ListItems items = new ListItems();
                    items.setName("座机号");
                    items.setHink("请输入");
                    items.setAction("新增");
                    tData.add(items);

                    for (int i = 0; i <  tData.size(); i++) {
                        if (i ==  tData.size() - 1) {
                            tData.get(i).setAction("新增");
                        } else {
                            tData.get(i).setAction("删除");

                        }
                    }

                }

                adapterTel.notifyDataSetChanged();
                break;


            case 2:
                ListItems eTems =  (ListItems) object;
                if ("删除".equals( eTems.getAction())) {
                    eData.remove( eTems);

                } else {
                    //判别是否是删除操作，还是新增操作
                    ListItems items = new ListItems();
                    items.setName("Email");
                    items.setHink("请输入");
                    items.setAction("新增");
                    eData.add(items);

                    for (int i = 0; i < eData.size(); i++) {
                        if (i == eData.size() - 1) {
                            eData.get(i).setAction("新增");
                        } else {
                            eData.get(i).setAction("删除");

                        }
                    }

                }
                adapterEmails.notifyDataSetChanged();

                break;
        }
    }


    class ListItems {

        String name;
        String hink;
        String value;
        String action;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getHink() {
            return hink;
        }

        public void setHink(String hink) {
            this.hink = hink;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }


    public class ItemsContactLocalAddAdapter extends BaseAdapter {

        private List<ListItems> objects = new ArrayList<ListItems>();

        private Context context;
        private LayoutInflater layoutInflater;
        private int tagId;
        private OnItemsButtonAddInface onItemsButtonAddInface;
        private boolean isNeed = true;

        public boolean isNeed() {
            return isNeed;
        }

        public void setNeed(boolean need) {
            isNeed = need;
        }

        public ItemsContactLocalAddAdapter(Context context, List<ListItems> datas) {
            this.context = context;
            this.objects = datas;
            this.layoutInflater = LayoutInflater.from(context);
        }

        public ItemsContactLocalAddAdapter(Context context, List<ListItems> datas, OnItemsButtonAddInface listener) {
            this.context = context;
            this.objects = datas;
            this.onItemsButtonAddInface = listener;
            this.layoutInflater = LayoutInflater.from(context);
        }

        public int getTagId() {
            return tagId;
        }

        public void setTagId(int tagId) {
            this.tagId = tagId;
        }

        public List<ListItems> getObjects() {
            return objects;
        }

        public void setObjects(List<ListItems> objects) {
            this.objects = objects;
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public ListItems getItem(int position) {
            return objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (true) {
                convertView = layoutInflater.inflate(R.layout.items_contact_local_add, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            initializeViews((ListItems) getItem(position), (ViewHolder) convertView.getTag(), position);
            return convertView;
        }

        private void initializeViews(final ListItems object, ViewHolder holder, final int position) {
            holder.tvName.setText(object.getName());
            holder.etName.setHint(object.getHink());
            holder.etName.setText(object.getValue());

            if ("删除".equals(object.getAction())) {
                holder.tvAdd.setText("删除");
            } else {
                holder.tvAdd.setText("新增");
            }

            holder.tvAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemsButtonAddInface.onItemsClick(view, tagId, position, object);
                }
            });

            holder.etName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


                    // pData.get(position).setValue(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    object.setValue(editable.toString());
                }
            });
            if (isNeed) {
                holder.tv_mark.setVisibility(View.VISIBLE);
            } else {
                holder.tv_mark.setVisibility(View.GONE);
            }
        }


        protected class ViewHolder {
            private TextView tvName, tv_mark;
            private EditText etName;
            private TextView tvAdd;

            public ViewHolder(View view) {
                tvName = (TextView) view.findViewById(R.id.tv_name);
                tv_mark = view.findViewById(R.id.tv_mark);
                etName = (EditText) view.findViewById(R.id.et_name);
                tvAdd = (TextView) view.findViewById(R.id.tv_add);
            }
        }
    }

    private EditText getEtCompany(){return (EditText)findViewById(R.id.et_company);} 
    
    private EditText getEtName() {
        return (EditText) findViewById(R.id.et_name);
    }

    private EditText getEtSex() {
        return (EditText) findViewById(R.id.et_sex);
    }

    private EditText getEtAge() {
        return (EditText) findViewById(R.id.et_age);
    }

    private EditText getEtPosition() {
        return (EditText) findViewById(R.id.et_position);
    }

    private EditText getEtDepart() {
        return (EditText) findViewById(R.id.et_depart);
    }

    private EditText getEtNotes() {
        return (EditText) findViewById(R.id.et_notes);
    }



    private PopupWindow popupWindow = null;

    public void showPopupWindow(View parent) {
        View view = null;
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = (ListView) view.findViewById(R.id.mList);
            SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    getPopData(),
                    R.layout.item_pop_list,
                    new String[]{"item_name"}, new int[]{R.id.tv_item_name});
            plist.setAdapter(adapter);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                        //mobile/contactDelete

                            HttpClient httpClient = new HttpClient.Builder("https://mobile.ubtob.com:8443/linkman/")
                                    .isDebug(true)
                                    .build();
                            httpClient.Api().send(new HttpClient.Builder("mobile/contactDelete")
                                    .add("linkmanId",orderId)
                                    .method(Method.POST)
                                    .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {

                                @Override
                                public void onResponse(Object o) {
                                    LogUtil.d(TAG,JSON.toJSONString(o));

                                    try {
                                        ToastMessage("删除成功，单据ID："+orderId);
                                        startActivity(new Intent(mContext,ContactsListActivity.class));
                                        finish();
                                    }catch (Exception e){

                                    }
                                }
                            }));
                            break;
                        case 1:

                            break;
                        case 3:
                            
                            
                            break;
                        case 2:

                            break;
                    }
                    closePoppupWindow();
                }
            });
            popupWindow = new PopupWindow(view, parent.getWidth(), 2*parent.getHeight());
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(mContext, 1f);
            }
        });
        DisplayUtil.backgroundAlpha(this, 0.5f);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
       // popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        //popupWindow.showAsDropDown(parent, windowManager.getDefaultDisplay().getWidth(), 0);

        int width = popupWindow.getWidth();
        popupWindow.setWidth(width + DisplayUtil.dip2px(ct, 10));
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        popupWindow.showAtLocation(parent.findViewById(R.id.tv_transtered), Gravity.NO_GRAVITY, location[0],
                location[1] - popupWindow.getHeight()/2);
    }

    private void closePoppupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
            popupWindow = null;
        }
    }


    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<>();
        map = new HashMap<String, Object>();
        map.put("item_name", "删除");
        list.add(map);
//        map = new HashMap<String, Object>();
//        map.put("item_name","分享");
//        list.add(map);
        return list;
    }
}

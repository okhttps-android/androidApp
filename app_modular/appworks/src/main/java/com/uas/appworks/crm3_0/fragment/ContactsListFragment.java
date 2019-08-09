package com.uas.appworks.crm3_0.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.SystemUtil;
import com.core.app.MyApplication;
import com.core.utils.CommonUtil;
import com.core.utils.NotifyUtils;
import com.core.utils.sortlist.BaseComparator;
import com.core.utils.sortlist.BaseSortModel;
import com.core.utils.sortlist.PingYinUtil;
import com.core.utils.sortlist.SideBar;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Tags;
import com.uas.appworks.R;
import com.uas.appworks.crm3_0.activity.ContactDynamicAddActivity;
import com.uas.appworks.crm3_0.activity.ContactsDetialActivity;
import com.uas.appworks.crm3_0.adapter.ContactSortAdapter;
import com.uas.appworks.crm3_0.adapter.DynamicAdapter;
import com.uas.appworks.crm3_0.model.ColumnModel;
import com.uas.appworks.crm3_0.model.ContactsBean;
import com.uas.appworks.crm3_0.model.ItemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
   * @desc:联系人列表
   * @author：Arison on 2018/9/13
   */
public class ContactsListFragment extends ViewPagerLazyFragment implements OnSmartHttpListener
,SideBar.OnTouchingLetterChangedListener{

  
//    private PullToRefreshListView mListView;
    private int tabItem;
    List<ContactsBean> datas;
    ItemContactsMeAdapter adapter;
    

    StickyListHeadersListView refreshListView;
    private BaseComparator comparator;
    private List<BaseSortModel<ContactsBean>> allDatas=new ArrayList<>();
    ContactSortAdapter mAdapter;
    private static final String TAG = "ContactsListFragment";
    private int page;

    SideBar sideBar;
    TextView dialogTV;


    DynamicAdapter dynamicAdapter;
    private List<Object> mData=new ArrayList<>();

    public static  ContactsListFragment newInstance(int tabItem) {
        Bundle args = new Bundle();
        args.putInt("tabItem", tabItem);
        ContactsListFragment fragment = new  ContactsListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void LazyData() {
        initView();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (refreshListView!=null) {
                   initData();
                }
            }
        }, 200);
    }

    private void initView() {
        tabItem = getArguments().getInt("tabItem", 0);
        LogUtil.d(TAG,"layzData():"+tabItem);

        refreshListView =  findViewById(R.id.mListView);
        refreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (tabItem){
                    case 1:
                        final ContactSortAdapter.ViewHolder viewHolder= (ContactSortAdapter.ViewHolder) view.getTag();
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
                        getActivity().startActivity(new Intent(getActivity(), ContactsDetialActivity.class)
                                .putExtra("model",viewHolder.bean));
                        break;

                    case 2:
                        DynamicAdapter.ViewHolder model= (DynamicAdapter.ViewHolder) view.getTag();
                        Toast.makeText(getActivity(),model.columnModel.getId(),Toast.LENGTH_LONG).show();
                        startActivity(new Intent(getActivity(),
                                ContactDynamicAddActivity.class)
                                .putExtra("caller", "Contact")
                                .putExtra("title", "客户联系人")
                                .putExtra("status", "在录入")
                                .putExtra("id", Integer.valueOf(model.columnModel.getId())));
                        break;
                }

            }
        });
        
        mAdapter = new ContactSortAdapter(ct, allDatas);
        mAdapter.setFrameLayout(getContentView());
        refreshListView.setAdapter(mAdapter);
        
        dialogTV = (TextView) findViewById(R.id.dialogTV);
        sideBar = (SideBar) findViewById(R.id.sidebar);
        sideBar.setTextView(dialogTV);
        sideBar.setOnTouchingLetterChangedListener(this);
        comparator = new BaseComparator();
        
//        mListView = findViewById(R.id.mListView);
//        mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
//            @Override
//            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
//                LogUtil.d(TAG,"下拉刷新.....");
//                page = 1;
//                initData();
//            }
//
//            @Override
//            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
//                LogUtil.d(TAG,"加载更多.....");
//                page++;
//                loadMore();
//            }
//        });
        
        
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//          
//
//            }
//        });
        
    }
    
    private void initData(){
        switch (tabItem){
            case 1:
                //我的联系人-不分页
                progressDialog.show();
                HttpClient httpClient = new HttpClient.Builder("https://mobile.ubtob.com:8443/linkman/")
                        .isDebug(true)
                        .build();
                httpClient.Api().send(new HttpClient.Builder("mobile/contactList")
                        .add("key","")
                        .add("imid", MyApplication.getInstance().getLoginUserId())
                        .method(Method.GET)
                        .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        try {
                            allDatas.clear();
                            datas= JSON.parseObject(JSON.parseObject(o.toString()).getJSONArray("data").toJSONString()
                                    ,new TypeReference<List<ContactsBean>>(){});
                            
                            allDatas=getAllDatas(datas);
                            mAdapter.setData(allDatas);
                            progressDialog.dismiss();
//                            adapter=new ItemContactsMeAdapter(getActivity(),datas);
//                            mListView.setAdapter(adapter);
//                            LogUtil.d(TAG,JSON.toJSONString(datas));
                        }catch (Exception e){

                        }
                    }
                }));
                
                String hex="{\"contacts\":[{\"companyName\":\"深圳市优软科技有限公司\",\"name\":\"张三\",\"sex\":\"男\",\"age\":25,\"position\":\"财务经理\",\"department\":\"财务部\",\"brithday\":\"1992-08-17\",\"phone\":\"13266699268\",\"isDMakers\":1,\"notes\":\"特殊客户\"},{\"companyName\":\"深圳市优软科技有限公司\",\"name\":\"张三\",\"sex\":\"男\",\"age\":25,\"position\":\"财务经理\",\"department\":\"财务部\",\"brithday\":\"1992-08-17\",\"phone\":\"13266699268\",\"isDMakers\":1,\"notes\":\"特殊客户\"}]}";
               
                break;
            case 2:
                //mobile/common/list.action
                 httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(MyApplication.getInstance()))
                        .isDebug(true)
                        .build();
                httpClient.Api().send(new HttpClient.Builder("mobile/common/list.action")
                        .add("page","1")
                        .add("pageSize","100")
                        .add("condition","1=1")
                        .add("caller", "Contact")
                        .add("sessionId",CommonUtil.getSharedPreferences(MyApplication.getInstance(), "sessionId"))
                        .method(Method.POST)
                        .build(),new ResultSubscriber<Object>(new ResultListener<Object>() {

                    @Override
                    public void onResponse(Object o) {
                        try {
                           //  LogUtil.prinlnLongMsg(TAG,"result:"+o.toString());
                            mData= getData(o.toString());
                            LogUtil.prinlnLongMsg(TAG,"result:"+JSON.toJSONString( mData));
                            dynamicAdapter=new DynamicAdapter(getActivity(),mData);
//                            mListView.setAdapter(dynamicAdapter);
                        }catch (Exception e){
                             e.printStackTrace();
                        }
                    }
                }));


//                hex="{\"contacts\":[{\"companyName\":\"深圳市优软科技有限公司\",\"name\":\"张三\",\"sex\":\"男\",\"age\":25,\"position\":\"财务经理\",\"department\":\"财务部\",\"brithday\":\"1992-08-17\",\"phone\":\"13266699268\",\"isDMakers\":1,\"notes\":\"特殊客户\"},{\"companyName\":\"深圳市优软科技有限公司\",\"name\":\"张三\",\"sex\":\"男\",\"age\":25,\"position\":\"财务经理\",\"department\":\"财务部\",\"brithday\":\"1992-08-17\",\"phone\":\"13266699268\",\"isDMakers\":1,\"notes\":\"特殊客户\"}]}";
//                datas= JSON.parseObject(JSON.parseObject(hex).getJSONArray("contacts").toJSONString()
//                        ,new TypeReference<List<ContactsBean>>(){});
//                adapter=new ItemContactsMeAdapter(getActivity(),datas);
//                mListView.setAdapter(adapter);
//                LogUtil.d(TAG,JSON.toJSONString(datas));
                break;
        }
//        mListView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mListView.onRefreshComplete();
//            }
//        },1000);
    }
    
    
    public void loadMore(){
        switch (tabItem){
            case 1:
//                String hex="{\"contacts\":[{\"companyName\":\"深圳市优软科技有限\",\"name\":\"张三\",\"sex\":\"男\",\"age\":25,\"position\":\"财务经理\",\"department\":\"财务部\",\"brithday\":\"1992-08-17\",\"phone\":\"13266699268\",\"isDMakers\":1,\"notes\":\"特殊客户\"},{\"companyName\":\"深圳市优软科司\",\"name\":\"张三\",\"sex\":\"男\",\"age\":25,\"position\":\"财务经理\",\"department\":\"财务部\",\"brithday\":\"1992-08-17\",\"phone\":\"13266699268\",\"isDMakers\":1,\"notes\":\"特殊客户\"}]}";
//                List<ContactsBean> datasNew= JSON.parseObject(JSON.parseObject(hex).getJSONArray("contacts").toJSONString()
//                        ,new TypeReference<List<ContactsBean>>(){});
//                datas.addAll(datasNew);
//                adapter.notifyDataSetChanged();
//                LogUtil.d(TAG,JSON.toJSONString(datas));
                break;
            case 2:
//                hex="{\"contacts\":[{\"companyName\":\"深圳市科技有限公司\",\"name\":\"张\",\"sex\":\"男\",\"age\":25,\"position\":\"财务经理\",\"department\":\"财务部\",\"brithday\":\"1992-08-17\",\"phone\":\"13266699268\",\"isDMakers\":1,\"notes\":\"特客户\"},{\"companyName\":\"深圳市优软科技有限公司\",\"name\":\"张三\",\"sex\":\"男\",\"age\":25,\"position\":\"财务经理\",\"department\":\"财务部\",\"brithday\":\"1992-08-17\",\"phone\":\"13266699268\",\"isDMakers\":1,\"notes\":\"特殊户\"}]}";
//                datasNew= JSON.parseObject(JSON.parseObject(hex).getJSONArray("contacts").toJSONString()
//                        ,new TypeReference<List<ContactsBean>>(){});
//                datas.addAll(datasNew);
//                adapter.notifyDataSetChanged();
//                LogUtil.d(TAG,JSON.toJSONString(datas));
                break;
        }

//        mListView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mListView.onRefreshComplete();
//            }
//        },1000);
    }
    

    @Override
    protected String getBaseUrl() {
        return CommonUtil.getAppBaseUrl(getContext());
    }

    @Override
    protected int inflater() {
        return R.layout.fragment_contacts_me;
    }

    


    @Override
    public void onSuccess(int what, String message, Tags tag) throws Exception {
        
    }

    @Override
    public void onFailure(int what, String message, Tags tag) throws Exception {

    }

    @Override
    public void onTouchingLetterChanged(String s) {
        LogUtil.d(TAG,"搜索关键字:"+s);
        // 该字母首次出现的位置
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


    public class ItemContactsMeAdapter extends BaseAdapter {

        private List<ContactsBean> objects = new ArrayList<ContactsBean>();

        private Context context;
        private LayoutInflater layoutInflater;

        public ItemContactsMeAdapter(Context context,List<ContactsBean> cb) {
            this.context = context;
            this.objects=cb;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        @Override
        public ContactsBean getItem(int position) {
            return objects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_contacts_me, null);
                convertView.setTag(new ViewHolder(convertView));
            }
            initializeViews((ContactsBean)getItem(position), (ViewHolder) convertView.getTag());
            return convertView;
        }

        private void initializeViews(final ContactsBean object, ViewHolder holder) {
         holder.tvName.setText(object.getName());
         holder.tvCompanyName.setText(object.getCompanyName());
         if (!StringUtil.isEmpty(object.getPhone())){
             holder.tvPhone.setText(object.getPhone().split("/")[0]);
         }
         holder.tvPosition.setText(object.getPosition());
         holder.bean=object;
         
         if (StringUtil.isEmpty(object.getPhone()))return;
         final String phone=object.getPhone().split("/")[0];
         holder.ivIcon.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (!StringUtil.isEmpty(phone)) {
                     String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
                     Pattern regex = Pattern.compile(check);
                     Matcher matcher = regex.matcher(phone);
                     if (matcher.matches()) {
                         if (context instanceof Activity){
                             SystemUtil.phoneAction(context, phone);
                         }else{
                             SystemUtil.phoneAction(getActivity(), phone);
                         }
                        
                     } else {
                         NotifyUtils.ToastMessage(getActivity(),getString(R.string.not_format_phone));
                     }
                 } else {
                     NotifyUtils.ToastMessage(getActivity(),getString(R.string.not_phone));
                 }
             }
         });
         holder.tvPhone.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (!StringUtil.isEmpty(phone)) {
                     String check = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
                     Pattern regex = Pattern.compile(check);
                     Matcher matcher = regex.matcher(phone);
                     if (matcher.matches()) {
                         if (context instanceof Activity){
                             SystemUtil.phoneAction(context, phone);
                         }else{
                             SystemUtil.phoneAction(getActivity(), phone);
                         }
                     } else {
                         NotifyUtils.ToastMessage(getActivity(),getString(R.string.not_format_phone));
                     }
                 } else {
                     NotifyUtils.ToastMessage(getActivity(),getString(R.string.not_phone));
                 }
             }
         });
        }

        public class ViewHolder {
            private TextView tvName;
            private TextView tvPosition;
            private TextView tvCompanyName;
            private TextView tvPhone;
            private ImageView ivIcon;
            public ContactsBean bean;

            public ViewHolder(View view) {
                tvName = (TextView) view.findViewById(R.id.tv_name);
                tvPosition = (TextView) view.findViewById(R.id.tv_position);
                tvCompanyName = (TextView) view.findViewById(R.id.tv_company_name);
                tvPhone = (TextView) view.findViewById(R.id.tv_phone);
                ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
            }
        }
    }


    private List<Object> getData(String json){
        List<Object> data=new ArrayList<>();
        JSONArray listdata= JSON.parseObject(json).getJSONArray("listdata");
        JSONArray columns= JSON.parseObject(json).getJSONArray("columns");
        String keyField=JSON.parseObject(json).getString("keyField");
        String pfField=JSON.parseObject(json).getString("pfField");
        List<ColumnModel> keys=new ArrayList<>();
        for (int i=0;i<columns.size();i++){
            JSONObject object=columns.getJSONObject(i);
            ColumnModel columnModel=new ColumnModel();
            columnModel.setDataIndex(object.getString("dataIndex"));
            columnModel.setCaption(object.getString("caption"));
            columnModel.setWidth(object.getInteger("width"));
            keys.add(columnModel);
        }
        for (int j=0;j<listdata.size();j++){
            JSONObject object=listdata.getJSONObject(j);
            data.add("分割线");
            String orderId="";
            for (int m=0;m<keys.size();m++){
                if (keys.get(m).getDataIndex().equals(keyField)){
                    orderId= object.getString(keys.get(m).getDataIndex());
                }
            }
            for (int k=0;k<keys.size();k++){
                    ItemModel itemModel=new ItemModel();
                    itemModel.setKey(keys.get(k).getCaption());//
                    itemModel.setId(orderId);
                    itemModel.setValue(object.getString(keys.get(k).getDataIndex()));//
                    itemModel.setGroupId(String.valueOf(j));
                 
              data.add(itemModel);
            }
        }
        return data;
    }


    private List<BaseSortModel<ContactsBean>> getAllDatas(List<ContactsBean> emList) {
        if (ListUtils.isEmpty(emList)) return null;
        List<BaseSortModel<ContactsBean>> list = new ArrayList<>();
        for (ContactsBean e : emList) {
            
            BaseSortModel<ContactsBean> mode = new BaseSortModel<>();
            mode.setBean(e);

            ContactsBean friend = mode.getBean();
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

package com.modular.booking.activity.business;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.base.BaseActivity;
import com.core.model.OAConfig;
import com.core.model.SelectBean;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.utils.CommonUtil;
import com.core.widget.VoiceSearchView;
import com.core.widget.EmptyLayout;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.impl.RetrofitImpl;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.booking.R;

import java.util.ArrayList;
import java.util.List;

/**
  * @desc:查询公司列表
  * @author：Arison on 2017/9/8
  */
public class BBCompanyListActivity extends BaseActivity {
    
    private ListView list;
    private List<SelectBean> formBean=new ArrayList<>();//数据来源
    private VoiceSearchView search_edit;
    private ListAdapter adapter;
    private EmptyLayout emptyLayout;
    private boolean isSingle=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bbcompany_list);
        initView();
        initEvent();
    }
    
    public void initView(){
     setTitle("企业");
        list = (ListView) findViewById(R.id.listview);
        search_edit = (VoiceSearchView) findViewById(R.id.voiceSearchView);
        emptyLayout = new EmptyLayout(ct, list);
        emptyLayout.setShowLoadingButton(false);
        emptyLayout.setShowEmptyButton(false);
        emptyLayout.setShowErrorButton(false);
        emptyLayout.setEmptyViewRes(R.layout.view_empty);
        adapter = new ListAdapter(formBean);
        SelectBean selectBean=new SelectBean();
        selectBean.setName(CommonUtil.getSharedPreferences(mContext,"erp_commpany"));
        formBean.add(selectBean);
        list.setAdapter(adapter);
    }
    
    public void initEvent(){
       // search_edit.setText(CommonUtil.getSharedPreferences(mContext,"erp_commpany"));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<SelectBean> formBeaans=  adapter.getFormBeaan();
                if (!ListUtils.isEmpty(formBeaans)){
                    String name= null;
                    try {
                        String json=formBeaans.get(position).getJson();
                        name = JSON.parseObject(json).getString("company");
                    } catch (Exception e) {
                        name=formBeaans.get(position).getName();
                    }
                    
                    Intent intent = new Intent("com.modular.main.SelectCollisionActivity");
                    SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                            .setResultCode(0x001)
                            .setTitle(name)
                            .setSingleAble(false);
                    intent.putExtra(OAConfig.MODEL_DATA, bean);
                    startActivityForResult(intent, 0x01);
                }
            }
        });
        search_edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!StringUtil.isEmpty(editable.toString())){
                    searchBykey(editable.toString());
                }else{
                    formBean.clear();
                    if (adapter!=null) {
                        adapter.notifyDataSetChanged();
                    }
                }
                
            }
        });
    }

    class ListAdapter extends BaseAdapter {
        private List<SelectBean> formBeaan;

        public ListAdapter() {
        }

        public ListAdapter(List<SelectBean> formBeaan) {
            this.formBeaan = formBeaan;
        }

        public void setFormBeaan(List<SelectBean> formBeaan) {
            this.formBeaan = formBeaan;
        }

        public List<SelectBean> getFormBeaan() {
            return formBeaan;
        }

        @Override
        public int getCount() {
            return formBeaan == null ? 0 : formBeaan.size();
        }

        @Override
        public Object getItem(int i) {
            return formBeaan.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        //临时变量
        SelectBean chche = null;

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
           ListAdapter.ViewHoler holer = null;
            if (view == null) {
                view = LayoutInflater.from(ct).inflate(R.layout.select_list_item, null);
                holer = new ListAdapter.ViewHoler();
                holer.select_scb = (CheckBox) view.findViewById(R.id.select_scb);
                holer.name_tv = (TextView) view.findViewById(R.id.name_tv);
                view.setTag(holer);
            } else {
                holer = (ListAdapter.ViewHoler) view.getTag();
            }
            chche = formBeaan.get(i);
            holer.name_tv.setText(StringUtil.isEmpty(chche.getName()) ? "" : chche.getName());
            holer.select_scb.setChecked(chche.isClick());
            if (isSingle) {
                holer.select_scb.setVisibility(View.GONE);
            } else {
                holer.select_scb.setFocusable(false);
                holer.select_scb.setClickable(false);
            }
            return view;
        }

        class ViewHoler {
            CheckBox select_scb;
            TextView name_tv;
        }
    }
    
    
    public void searchBykey(String key){
        LogUtil.d("ResultSubscriber","key:"+key);
        HttpClient httpClient=new HttpClient.Builder("https://account.ubtob.com/")
                .httpBase(RetrofitImpl.getInstance())
                .build();

        httpClient.Api().send(new HttpClient.Builder("api/userspace/userSpaceDetail/keyword")
                .add("keyword",key)
                .add("pageNumber","1")
                .method(Method.GET)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("ResultSubscriber","result:"+ o);
                if (JSONUtil.validate(o.toString())){
                    formBean.clear();
                JSONArray jsonArray= JSON.parseObject(o.toString()).getJSONArray("listdata");
                if (ListUtils.isEmpty(jsonArray)){
                    emptyLayout.showEmpty();
                    adapter.notifyDataSetChanged();
                    return;
                }
                for(int i=0;i<jsonArray.size();i++){
                    JSONObject jsonObject=jsonArray.getJSONObject(i);
                    SelectBean selectBean=new SelectBean();
                    selectBean.setName(jsonObject.getString("company"));
                    selectBean.setJson(jsonObject.toJSONString());
                    formBean.add(selectBean);
                }
                if (adapter!=null){
                    adapter.notifyDataSetChanged();
                }
                }
                
            }
        }));
                
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data==null)return;
        switch (requestCode){
            case 0x01:
            ArrayList<SelectEmUser> selectEmUsers=new ArrayList<>();
            if (data.getParcelableArrayListExtra("data")==null){
                setResult(0x02,new Intent().putParcelableArrayListExtra("data",selectEmUsers));
                finish();
            }else{
                selectEmUsers=data.getParcelableArrayListExtra("data") ;
                setResult(0x02,new Intent().putParcelableArrayListExtra("data",selectEmUsers));
                finish();   
            }
            break;
        }
    }
}

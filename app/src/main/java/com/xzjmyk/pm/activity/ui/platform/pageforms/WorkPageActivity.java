package com.xzjmyk.pm.activity.ui.platform.pageforms;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.api.wxapi.ApiConfig;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.widget.EmptyLayout;
import com.core.widget.VoiceSearchView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.WorkExtraActivity;
import com.xzjmyk.pm.activity.ui.erp.activity.oa.OAActivity;
import com.xzjmyk.pm.activity.ui.platform.adapter.PagesModelAdapter;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
  * @desc:加班单列表
  * @author：Arison on 2017/3/7
  */
public class WorkPageActivity extends BaseActivity {
    @ViewInject(R.id.lv_sale_list)
    private PullToRefreshListView mlistview;
    @ViewInject(R.id.voiceSearchView)
    private VoiceSearchView voiceSearchView;
    private EmptyLayout mEmptyLayout;

    private List<PagesModel> mDatas=new ArrayList<>();
    private PagesModelAdapter mAdapter;
    private int page=1;

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case  Constants.HTTP_SUCCESS_INIT:
                    try {
                        String result=msg.getData().getString("result");
                        JSONArray array= JSON.parseObject(result).getJSONArray("listdata");
                        for (int i=0;i<array.size();i++){
                            JSONObject object=array.getJSONObject(i);
                            PagesModel model=new PagesModel();
                            model.setModeJson(object.toJSONString());
                            model.setEndTime(DateFormatUtil.long2Str(object.getJSONArray("workovertimedet").getJSONObject(0)
                                    .getLong("wod_enddate"), DateFormatUtil.YMD_HMS));
                            model.setCode(object.getString("wo_code"));
                            model.setId(object.getString("wo_id"));
                            model.setStartTime(DateFormatUtil.long2Str(object.getJSONArray("workovertimedet").getJSONObject(0)
                                    .getLong("wod_startdate"), DateFormatUtil.YMD_HMS));
                            model.setState(object.getString("wo_status"));
                            mDatas.add(model);
                        }
                        if (mAdapter!=null){
                            mAdapter.notifyDataSetChanged();
                            if (mAdapter.getCount()==0){
                                mEmptyLayout.showEmpty();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mlistview.onRefreshComplete();
                    break;
                default:
                    if (msg.getData() != null) {
                        if (!StringUtil.isEmpty(msg.getData().getString("result"))) {
                            ToastMessage(msg.getData().getString("result"));
                        }
                    }
                    break;
            }
        }
    };
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();

    }

    private void initData() {
        mAdapter=new PagesModelAdapter(mDatas,ct);
        mAdapter.setmEmptyLayout(mEmptyLayout);
        mAdapter.setType(2);
        mlistview.setAdapter(mAdapter);
        getPageData(page);
    }

    private String formwhere;
    private void initView() {

        Intent intent = getIntent();
        formwhere = intent.getStringExtra("ADDUI");
        setContentView(R.layout.act_sale_select_list);
        ViewUtils.inject(this);
        mEmptyLayout = new EmptyLayout(this, mlistview.getRefreshableView());
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowLoadingButton(false);
        mlistview.setMode(PullToRefreshBase.Mode.BOTH);
        mlistview.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                page = 1;
                if (!ListUtils.isEmpty(mDatas)) {
                    mDatas.clear();
                }
                getPageData(page);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                page++;
                getPageData(page);
            }
        });

        mlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPosition = (int) parent.getItemIdAtPosition(position);
                //PagesModelAdapter.ViewModel model= (PagesModelAdapter.ViewModel) view.getTag();
                if (mDatas.size()<1) return;
                String data = mDatas.get(position - 1).getModeJson();
                JSONObject root = JSON.parseObject(data);
                JSONObject map = new JSONObject(true);
//                map.put("录入人", root.getString("wo_recorder"));
//                map.put("单据状态", root.getString("wo_status"));
                map.put(getString(R.string.doc_id), root.getString("wo_code"));
                map.put(getString(R.string.overwork_purpose), root.getString("wo_worktask"));
                LogUtil.d(map.toJSONString());
                JSONArray array = root.getJSONArray("workovertimedet");
                JSONArray detail = new JSONArray();
                for (int i = 0; i < array.size(); i++) {
                    JSONObject model = array.getJSONObject(i);
                    JSONObject temp = new JSONObject(true);
                    temp.put(getString(R.string.overwork_hours), model.get("wod_count"));
                    temp.put(getString(R.string.end_time), DateFormatUtil.long2Str(model.getLong("wod_enddate"), DateFormatUtil.YMD_HMS));
                    temp.put(getString(R.string.start_time), DateFormatUtil.long2Str(model.getLong("wod_startdate"), DateFormatUtil.YMD_HMS));
                    detail.add(temp);

                }
                if ("在录入".equals(root.getString("wo_status"))) {
                    startActivityForResult(new Intent(getApplicationContext(), WorkExtraActivity.class)
                            .putExtra("data", map.toString())
                            .putExtra("detailJson", detail.toJSONString())
                            .putExtra("mkeyValue", root.getIntValue("wo_id"))
                            .putExtra("submittype", "resubmit")
                            .putExtra("wod_id",array.getJSONObject(0).getInteger("wod_id"))
                            , 0x333);

                    finish();
                } else {
                    startActivityForResult(new Intent(ct, FormDetailActivity.class)
                            .putExtra("data", map.toString())
                            .putExtra("detail", detail.toJSONString())
                            .putExtra("title", getString(R.string.overtime_doc) + getString(R.string.doc_detail))
                            .putExtra("mkeyValue", root.getIntValue("wo_id"))
                            .putExtra("whichpage", 3)
                            .putExtra("status", root.getString("wo_status"))
                            .putExtra("docemname",root.getString("wo_recorder"))
                            , 0x333);
                }

                Log.i("WorkPagectivity.this",JSON.toJSONString(detail));

            }
        });

        voiceSearchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mAdapter != null) {
                    if (ListUtils.isEmpty(mDatas)){
                        mEmptyLayout.showEmpty();
                        return;
                    }else {
                        String strChche = editable.toString().replace(" ", "");//去除空格
                        strChche = strChche.replace(" ", " ");//去除空格
                        List<PagesModel> chche=new ArrayList<>();
                        if (StringUtil.isEmpty(strChche.toString())) {
                            mAdapter.getFilter().filter("");
                        } else {
                            mAdapter.getFilter().filter(strChche.toString());
                        }
                        Log.i("strChche", strChche + "null?");
                    }

                }
            }
        });


    }
   

    public void getPageData(int page){
        //emcode=1000003217&enuu=10030994&pageNumber=1&pageSize=10
        String url= ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().list_workOvertime;
        Map<String, Object> params = new HashMap<>();
        params.put("pageNumber", page);
        params.put("pageSize", "10");
        params.put("emcode", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
        params.put("enuu", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "companyEnUu"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "get");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (!StringUtil.isEmpty(formwhere) && "ADDUI".equals(formwhere)){
                startActivity(new Intent(getApplicationContext(),OAActivity.class));
            }
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!StringUtil.isEmpty(formwhere) && "ADDUI".equals(formwhere)){
            startActivity(new Intent(getApplicationContext(), OAActivity.class));
        }
        finish();
        super.onBackPressed();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0x333 &&resultCode == 0x328){
            mDatas.remove(mPosition);
            mAdapter.notifyDataSetChanged();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}

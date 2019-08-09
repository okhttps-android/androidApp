package com.uas.appworks.OA.erp.activity.form;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.view.MyStickyGridHeadersGridView;
import com.lidroid.xutils.ViewUtils;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;
import com.uas.appworks.OA.erp.adapter.StickyGridAdapter;
import com.core.widget.view.selectcalendar.bean.Data;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @desc:配置字段 分组表格
 * @author：Arison on 2016/11/23
 */
public class DataFormFieldActivity extends BaseActivity implements AdapterView.OnItemClickListener,
        StickyGridHeadersGridView.OnHeaderClickListener, StickyGridHeadersGridView.OnHeaderLongClickListener {

    private MyStickyGridHeadersGridView mGridView;
    private MyStickyGridHeadersGridView mDGridView;
    private Button bt_save;
    private LinearLayout ll_hide;
    private LinearLayout ll_add;
    private StickyGridAdapter mAdapter;
    private StickyGridAdapter mDadapter;
    private String master;//添加账套，由于审批流部分存在不同账套的问题

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_form_field);
        ViewUtils.inject(this);
        setTitle("编辑字段");
        initView();

    }

    private void initView() {
        Intent intent = getIntent();
        mGridView = (MyStickyGridHeadersGridView) findViewById(R.id.asset_grid);
        mDGridView = (MyStickyGridHeadersGridView) findViewById(R.id.asset_grid_delete);
        bt_save = (Button) findViewById(R.id.bt_save);
        ll_hide = (LinearLayout) findViewById(R.id.ll_hide);
        ll_add = (LinearLayout) findViewById(R.id.ll_add);

        if (intent != null) {
            List<Data> fields = (List) intent.getParcelableArrayListExtra("fields");//传递数据
            List<Data> fieldsDis = (List) intent.getParcelableArrayListExtra("fieldsDis");//传递数据
            caller = intent.getStringExtra("caller");
            master = intent.getStringExtra("master");
            initData(fields, fieldsDis);
        } else {
            initData(null, null);
        }

        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                commit(); ;
            }
        });
    }

    private void initData(List<Data> fields, List<Data> fieldsDis) {
        if (ListUtils.isEmpty(fields) && ListUtils.isEmpty(fieldsDis)) {
            List<Data> hasHeaderIdList = new ArrayList<>();
//            int groupId = 1;
//            for (int i = 0; i < 25; i++) {
//                Data item = new Data();
//                item.setName("字段" + i);
//                item.setGroupId(groupId);
//                item.setGroup("组名" + groupId);
//                if (i % 5 == 0) {
//                    groupId++;
//                }
//                hasHeaderIdList.add(item);
//            }

            mAdapter = new StickyGridAdapter(DataFormFieldActivity.this, hasHeaderIdList, mGridView);
            mGridView.setAdapter(mAdapter);
            mGridView.setAreHeadersSticky(false);//空值悬停
            mGridView.setOnItemClickListener(this);

            mDadapter = new StickyGridAdapter(DataFormFieldActivity.this, hasHeaderIdList, mDGridView);
            mDGridView.setAdapter(mDadapter);
            mDGridView.setAreHeadersSticky(false);//空值悬停

            mDGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                }
            });

            View emptyView = LayoutInflater.from(this).inflate(R.layout.erp_empty_view, null);
            emptyView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            emptyView.setVisibility(View.GONE);
            ((ViewGroup) mGridView.getParent()).addView(emptyView);
//            ((ViewGroup)mDGridView.getParent()).addView(emptyView);

            mGridView.setEmptyView(emptyView);
//            mDGridView.setEmptyView(emptyView);

            bt_save.setVisibility(View.GONE);
            ll_add.setVisibility(View.GONE);
            ll_hide.setVisibility(View.GONE);
        } else {
            mAdapter = new StickyGridAdapter(DataFormFieldActivity.this, fields, mGridView);
            mGridView.setAdapter(mAdapter);
            mGridView.setAreHeadersSticky(false);//空值悬停
            mGridView.setOnItemClickListener(this);

            mDadapter = new StickyGridAdapter(DataFormFieldActivity.this, fieldsDis, mDGridView);
            mDGridView.setAdapter(mDadapter);
            mDGridView.setAreHeadersSticky(false);//空值悬停
            mDGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // ToastMessage(mDadapter.getHasHeaderIdList().get(position).getName() + "被点击！");
                    if (mDadapter.getHasHeaderIdList().get(position).isSelected()) {
                        mDadapter.getHasHeaderIdList().get(position).setIsSelected(false);
                    } else {
                        mDadapter.getHasHeaderIdList().get(position).setIsSelected(true);
                    }
                    mDadapter.notifyDataSetChanged();
                }
            });
            if (ListUtils.isEmpty(fields)) {
                ll_add.setVisibility(View.GONE);
                ll_hide.setVisibility(View.VISIBLE);
            }
            if (ListUtils.isEmpty(fieldsDis)) {
                ll_add.setVisibility(View.VISIBLE);
                ll_hide.setVisibility(View.GONE);
            }
            bt_save.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onHeaderClick(AdapterView<?> adapterView, View view, long l) {

    }

    @Override
    public boolean onHeaderLongClick(AdapterView<?> adapterView, View view, long l) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // ToastMessage(mAdapter.getHasHeaderIdList().get(position).getName() + "被点击！");
        if (mAdapter.getHasHeaderIdList().get(position).isSelected()) {
            mAdapter.getHasHeaderIdList().get(position).setIsSelected(false);
        } else {
            mAdapter.getHasHeaderIdList().get(position).setIsSelected(true);
        }
        mAdapter.notifyDataSetChanged();

    }


    private String formStore = "";
    private String gridStore = "";
    private String caller;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.HTTP_SUCCESS_INIT:
                    LogUtil.d(msg.getData().getString("result"));
                    ToastMessage("提交成功！");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setResult(0x25);
                            finish();
                        }
                    }, 3000);
                    break;
            }
        }
    };


    /*
    * 界面配更新接口：
    mobile/configUpdate.action
    参数：caller,formStore,gridStore,其中formStore,gridStore多个字段的值用逗号隔开
    例如：ERP/mobile/configUpdate.action?caller=FeePlease!CCSQ&formStore=fp_auditman,fp_date&gridStore=fpd_code,fpd_id
    */
    public void commit() {
        List<Data> mSource = mAdapter.getHasHeaderIdList();
        List<Data> mDSource = mDadapter.getHasHeaderIdList();
        HashMap<String, Integer> resultMap = new HashMap<>();
//        LogUtil.d(JSON.toJSONString(mSource));
//        LogUtil.d(JSON.toJSONString(mDSource));
        formStore = "";
        gridStore = "";
        if (!ListUtils.isEmpty(mSource)) {
            for (int i = 0; i < mSource.size(); i++) {
                String formStr = "";
                String girdStr = "";
                if (mSource.get(i).getGroupId() == 0) {
                    if (mSource.get(i).isSelected()) {
                        formStr = mSource.get(i).getField();
                    }
                    if (StringUtil.isEmpty(formStr)) continue;
//                    if (i==mSource.size()-1){
//                        formStore+=formStr;
//                    }else{
                    formStore += formStr + ",";
                    resultMap.put(formStr, -1);//准备显示
//                    }
                } else {
                    if (mSource.get(i).isSelected()) {
                        girdStr = mSource.get(i).getField();
                    }
                    if (StringUtil.isEmpty(girdStr)) continue;
//                    if(i==mSource.size()-1){
//                        gridStore+=girdStr;
//                    }else{
                    gridStore += girdStr + ",";
                    resultMap.put(girdStr, -1);//准备显示
//                    } 
                }
            }
        }


        if (!ListUtils.isEmpty(mDSource)) {
            for (int i = 0; i < mDSource.size(); i++) {
                String formStr = "";
                String girdStr = "";
                if (mDSource.get(i).getGroupId() == 0) {
                    if (mDSource.get(i).isSelected()) {
                        formStr = mDSource.get(i).getField();
                    }
                    if (StringUtil.isEmpty(formStr)) continue;
//                    if (i==mSource.size()-1){
//                        formStore+=formStr;
//                    }else{
                    formStore += formStr + ",";
                    resultMap.put(formStr, 0);//准备隐藏
//                    }
                } else {
                    if (mDSource.get(i).isSelected()) {
                        girdStr = mDSource.get(i).getField();
                    }
                    if (StringUtil.isEmpty(girdStr)) continue;
//                    if(i==mSource.size()-1){
//                        gridStore+=girdStr;
//                    }else{
                    gridStore += girdStr + ",";
                    resultMap.put(girdStr, 0);//准备隐藏
//                    } 
                }
            }
        }


//        LogUtil.d(formStore);
//        LogUtil.d(gridStore);
        LogUtil.d(JSON.toJSONString(resultMap));
        if (!StringUtil.isEmpty(formStore))
            formStore = formStore.substring(0, formStore.length() - 1);
        if (!StringUtil.isEmpty(gridStore))
            gridStore = gridStore.substring(0, gridStore.length() - 1);
        if (StringUtil.isEmpty(gridStore) && StringUtil.isEmpty(formStore)) {
            ToastMessage("当前没有选中项");
            return;
        }
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/common/updatemobiledefault.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", caller);
        params.put("master", master);
        params.put("formStore", JSON.toJSONString(resultMap));
        // params.put("gridStore", gridStore);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
    }


}

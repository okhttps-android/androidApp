package com.modular.booking.activity.shares;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.OAConfig;
import com.core.model.SelectCollisionTurnBean;
import com.core.model.SelectEmUser;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.booking.R;
import com.modular.booking.adapter.MuiltBookAdapter;
import com.modular.booking.model.BookingModel;

import java.util.ArrayList;
import java.util.List;

public class MuiltSelectShareActivity extends SupportToolBarActivity {
    private ArrayList<BookingModel> mDatas = new ArrayList<>();
    private MuiltBookAdapter mAdapter;
    private PullToRefreshListView mlist;
    private EmptyLayout emptyLayout;
    private static final String TAG = "MuiltSelectShareActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shares_list);
        initView();
    }

    private void initView() {
        setTitle("预约共享");
        mlist = (PullToRefreshListView) findViewById(R.id.shareList);
        emptyLayout = new EmptyLayout(ct, mlist.getRefreshableView());
        if (getIntent() != null) {
            mDatas = getIntent().getParcelableArrayListExtra("model");
            mAdapter = new MuiltBookAdapter(mContext, mDatas);
            mAdapter.setTime(true);
            mlist.setAdapter(mAdapter);
            if (mAdapter.getCount()==0){
                emptyLayout.setEmptyMessage(getString(R.string.book_empty));
                emptyLayout.showEmpty();
            }
        }

        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MuiltBookAdapter.ViewHolder viewHolder = (MuiltBookAdapter.ViewHolder) view.getTag();
                boolean isChecked = !viewHolder.checkBox.isChecked();
                mAdapter.getItem(position - 1).setClick(isChecked);
                mAdapter.notifyDataSetChanged();


            }
        });

    
     
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.app_about) {
            Intent intent = new Intent("com.modular.main.SelectCollisionActivity");
            SelectCollisionTurnBean bean = new SelectCollisionTurnBean()
                    .setTitle(getString(R.string.select_share_friend))
                    .setSingleAble(false);
            intent.putExtra(OAConfig.MODEL_DATA, bean);
            startActivityForResult(intent, 0x02);
        }

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;
        switch (requestCode) {
            case 0x02:
                try {
                    List<SelectEmUser> employeesList = data.getParcelableArrayListExtra("data");
                    LogUtil.d("Test", JSON.toJSONString(employeesList));
                    StringBuilder stringBuilder = new StringBuilder("");
                    for (int i = 0; i < employeesList.size(); i++) {
                        if (i == employeesList.size() - 1) {
                            stringBuilder.append(employeesList.get(i).getImId());
                        } else {
                            stringBuilder.append(employeesList.get(i).getImId());
                            stringBuilder.append(",");
                        }
                    }
                    LogUtil.d("Test", stringBuilder.toString());
                    List<BookingModel> models = mAdapter.getDatas();
                    StringBuilder planids = new StringBuilder("");
                    StringBuilder bplanids = new StringBuilder("");
                    for (int i = 0; i < models.size(); i++) {
                        if (models.get(i).isClick) {
                            if ("个人".equals(models.get(i).getKind())) {
                                planids.append(models.get(i).getAb_id() + ",");
                            } else {
                                bplanids.append(models.get(i).getAb_id() + ",");
                            }
                        }
                    }
//                  
                    if (!StringUtil.isEmpty(stringBuilder.toString())) {
                        String p="";
                        String b="";
                        if (!StringUtil.isEmpty(planids.toString())){
                            p=planids.substring(0,planids.length()-1);
                        }
                        if (!StringUtil.isEmpty(bplanids.toString())){
                           b= bplanids.substring(0,bplanids.length()-1);
                        }
                        shareBooking(p, b, stringBuilder.toString());
                    } else {
                        ToastMessage("共享失败！");
                    }
                } catch (Exception e) {

                }
                break;
        }
    }


    public void shareBooking(String planids, String bplanids, String imids) {
        String map = "{\"planids\":\"" + planids + "\",\"bplanids\":\"" + bplanids + "\",\"userids\":\"" + imids + "\"}";
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build();
        httpClient.Api().send(new HttpClient.Builder()
                .url("/user/appBatchShare")
                .add("token", MyApplication.getInstance().mAccessToken)
                .add("map", map)
                .method(Method.POST)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
//                dimssLoading();
                if (JSONUtil.validate(o.toString())){
                    String result=JSON.parseObject(o.toString()).getString("result");
                    if ("success".equals(result)){
                        ToastMessage("分享成功！");
                    }
                }
            }
        }));

    }
}

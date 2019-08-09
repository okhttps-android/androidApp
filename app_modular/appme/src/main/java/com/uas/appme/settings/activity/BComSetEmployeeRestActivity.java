package com.uas.appme.settings.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.CalendarUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.SupportToolBarActivity;
import com.core.model.SelectBean;
import com.core.utils.CommonUtil;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.OASigninPicker;
import com.core.widget.MyListView;
import com.core.widget.view.Activity.SelectActivity;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;
import com.uas.appme.pedometer.utils.TimeUtil;
import com.uas.appme.settings.model.ComRestBean;
import com.uas.appme.settings.model.PersonSetingBean;

import java.util.ArrayList;
import java.util.List;

import static com.common.data.JSONUtil.getJSONArray;

/**
 * Created by FANGlh on 2017/10/12.
 * function:
 */

public class BComSetEmployeeRestActivity extends SupportToolBarActivity implements View.OnClickListener{
    private MyListView mComList;
    private List<ComRestBean> mList;  //进行保存的员工休息数据列表
    private ComRestAdapter myAdapter;
    private String current_date;
    private List<String> serviceMans;
    private int click_positon;
    private ArrayList<SelectBean> selectBeens;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_bcom_setting_activity);
        initView();
        loadServiceMan();
//        initData();
    }

    private void initData() {

        //获取商家服务人员
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreman")
//                .add("companyid", 201)
                .add("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("serviceid", 0)
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appStoreman", o.toString()+"");
                try {
                    PersonSetingBean  mServicePersonList = JSON.parseObject(o.toString(),PersonSetingBean.class);
                    handleServerMan(mServicePersonList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }
    private String sc_industry;
    private String sc_industrycode;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bsetting_more, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.booking_set_list){
            startActivity(new Intent(ct,BSetComRestListActivity.class)
                    .putExtra("sc_industry",sc_industry)
                    .putExtra("sc_industrycode",sc_industrycode)
                    .putExtra("type","eomployee"));
        }
        return super.onOptionsItemSelected(item);
    }
    private void handleServerMan(PersonSetingBean mServicePersonList) {
        if (mServicePersonList == null || ListUtils.isEmpty(mServicePersonList.getResult())) return;
        for (int i=0; i<mServicePersonList.getResult().size();i++)
            serviceMans.add(mServicePersonList.getResult().get(i).getSm_username());

    }


    private void initView() {
        mList = new ArrayList<>();
        mComList = (MyListView) findViewById(R.id.com_list);
        findViewById(R.id.add_new_rl).setOnClickListener(this);
        myAdapter = new ComRestAdapter(this);
        myAdapter.setModelList(mList);
        mComList.setAdapter(myAdapter);
        findViewById(R.id.save_bt).setOnClickListener(this);
        serviceMans = new ArrayList<>();

        //初始化今天日期
        String CURRENT_DATE = TimeUtil.getCurrentDate();
        current_date = TimeUtils.s_long_2_str(DateFormatUtil.str2Long(CURRENT_DATE, "yyyy年MM月dd日"));

        ComRestBean model = new ComRestBean();
        model.setSf_companyid(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
        model.setSf_username("");
        model.setSf_companyname(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_commpany"));
        model.setSf_userid("");
        model.setSf_date("");
        mList.add(model);
        myAdapter.notifyDataSetChanged();

        //接收商家类型
        sc_industry = getIntent().getStringExtra("sc_industry");
        sc_industrycode = getIntent().getStringExtra("sc_industrycode");

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_new_rl){
            ComRestBean model = new ComRestBean();
            model.setSf_companyid(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
            model.setSf_username("");
            model.setSf_companyname(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_commpany"));
            model.setSf_userid("");
            model.setSf_date("");
            mList.add(model);
            myAdapter.notifyDataSetChanged();
        }else if (v.getId() == R.id.save_bt){
            if (!CommonUtil.isNetWorkConnected(ct)) {
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }
            LogUtil.prinlnLongMsg("mList", JSON.toJSONString(mList));
            if (ListUtils.isEmpty(mList)) return;
            for (int i = 0; i < mList.size(); i++) {
                if (StringUtil.isEmpty(mList.get(i).getSf_username()) || StringUtil.isEmpty(mList.get(i).getSf_date())){
                    ToastMessage(getString(R.string.input_name_date));
                    break;
                }
                if (i==mList.size()-1)
                    doSave(mList);
            }
        }
    }

    private void doSave(List<ComRestBean> mList) {
        Boolean canSave = true;
        if (mList.size() > 1){
            for (int i = 0; i < mList.size(); i++) {
                for (int j = i+1;j < mList.size();j++){
                    if (mList.get(i).getSf_username().equals(mList.get(j).getSf_username()) &&
                            mList.get(i).getSf_date().equals(mList.get(j).getSf_date())){
                        ToastMessage(getString(R.string.hava_save_detail));
                        LogUtil.prinlnLongMsg("fanglh",mList.get(i).getSf_username()+","+
                                mList.get(j).getSf_username() +","+
                                mList.get(i).getSf_date()+","+
                                mList.get(j).getSf_date());
                        canSave = false;
                        break;
                    }
                }
            }
        }

        if (!canSave) return;
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appBatchMrest")
                .add("map",JSON.toJSONString(mList))
                .add("token",MyApplication.getInstance().mAccessToken)
                .connectTimeout(10000)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) {
                    progressDialog.dismiss();
                    return;
                }
                LogUtil.prinlnLongMsg("appBatchMrest", o.toString()+"");
                if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")) {
                    Toast.makeText(ct,getString(R.string.common_save_success),Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ct,BSetComRestListActivity.class)
                            .putExtra("sc_industry",sc_industry)
                            .putExtra("sc_industrycode",sc_industrycode)
                            .putExtra("type","eomployee"));
                    finish();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Object t) {
                progressDialog.dismiss();
                ToastMessage(getString(R.string.too_long_to_http));
            }
        }));

    }

    private class ComRestAdapter extends BaseAdapter{
        private Context mContext;
        private List<ComRestBean> modelList;

        public List<ComRestBean> getModelList() {
            return modelList;
        }

        public void setModelList(List<ComRestBean> modelList) {
            this.modelList = modelList;
        }

        public ComRestAdapter(Context mContext){
            this.mContext = mContext;
        }
        @Override
        public int getCount() { return ListUtils.isEmpty(modelList) ? 0 : modelList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView =  View.inflate(mContext, R.layout.com_rest_item,null);
                viewHolder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
                viewHolder.date_tv = (TextView) convertView.findViewById(R.id.date_tv);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if (!ListUtils.isEmpty(modelList)){
                viewHolder.name_tv.setText(modelList.get(position).getSf_username()+"");
                viewHolder.date_tv.setText(modelList.get(position).getSf_date()+"");
            }

            viewHolder.name_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click_positon = position;
                    if (ListUtils.isEmpty(selectBeens)) {
                        doInputName(position);
                    } else{
                            Intent intent = new Intent(ct, SelectActivity.class)
                                    .putExtra("type", 2)
                                    .putExtra("title", getString(R.string.select_user))
                                    .putParcelableArrayListExtra("data", selectBeens);
                            startActivityForResult(intent, 0x02);

                       /* ArrayList<SelectBean> beans = new ArrayList<>();
                        SelectBean bean = null;
                        for (String e : serviceMans) {
                            bean = new SelectBean();
                            bean.setName(e);
                            bean.setClick(false);
                            beans.add(bean);
                        }
                        Intent intent = new Intent(ct, SelectActivity.class);
                        intent.putExtra("type", 2);
                        intent.putParcelableArrayListExtra("data", beans);
                        intent.putExtra("mTitle", "人员选择");
                        startActivityForResult(intent, 0x01);*/
                    }
                }
            });

            viewHolder.date_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doSelectEndDate(position);
                }
            });
            return convertView;
        }

        class ViewHolder{
            TextView name_tv;
            TextView date_tv;
        }
    }
    //load 选择服务人员
    private String companyid;
    public void loadServiceMan() {
        if (!CommonUtil.isNetWorkConnected(ct)){
            ToastMessage(getString(R.string.common_notlinknet));
            return;
        }
        progressDialog.show();
        new HttpClient.Builder(Constants.IM_BASE_URL())
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url("/user/appStoreman")
                        .add("companyid", StringUtil.isEmpty(companyid) ? CommonUtil.getSharedPreferences(ct, "erp_uu") : companyid)
                        .add("serviceid", "0")
                        .add("token", MyApplication.getInstance().mAccessToken)
                        .method(Method.GET)
                        .build(), new ResultSubscriber<>(new ResultListener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        LogUtil.prinlnLongMsg("appStoreman", o.toString()+"");
                        if (JSONUtil.validateJSONObject(o.toString())) {
                            JSONArray array = getJSONArray(o.toString(), "result");
                            SelectBean bean = null;
                            selectBeens = new ArrayList<SelectBean>();

                            if (!ListUtils.isEmpty(array)) {
                                for (int i = 0; i < array.size(); i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    bean = new SelectBean();
                                    int id = JSONUtil.getInt(object, "sm_id");
                                    String name = JSONUtil.getText(object, "sm_username");
                                    bean.setId(id);
                                    bean.setFields(String.valueOf(id));
                                    bean.setName(name);
                                    bean.setJson(object.toJSONString());
                                    selectBeens.add(bean);
                                }
                            } else {
                                ToastUtil.showToast(ct, "当前公司还没有设置员工");
                            }

                        }
                        progressDialog.dismiss();
                    }
                }));
    }
    private PopupWindow popupWindow = null;
    private void doInputName(final int position) {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.item_edit_location_pop, null);

        // 设置按钮的点击事件
        TextView title_tv = (TextView) contentView.findViewById(R.id.title_tv);
        final EditText editname_et = (EditText) contentView.findViewById(R.id.editname_et);
        TextView cancel_tv = (TextView) contentView.findViewById(R.id.cancel_tv);
        TextView sure_tv = (TextView) contentView.findViewById(R.id.sure_tv);
        title_tv.setText(getString(R.string.input_person_name));

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 300);
        h_screen = DisplayUtil.dip2px(this, 145);

        contentView.findViewById(R.id.cancel_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StringUtil.isEmpty(editname_et.getText().toString())){
                    ToastMessage(getString(R.string.input_person_name));
                    return;
                }else {
                    mList.get(position).setSf_username(editname_et.getText().toString());
                    myAdapter.notifyDataSetChanged();
                }
                popupWindow.dismiss();
            }
        });
        popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable(getResources().getDrawable(com.uas.appworks.R.drawable.pop_round_bg));
        // 设置好参数之后再show
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        setbg(0.4f);
    }
    private void setbg(float alpha) {
        setBackgroundAlpha(this, alpha);
        if (popupWindow == null) return;
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                setBackgroundAlpha(BComSetEmployeeRestActivity.this, 1f);
            }
        });
    }

    /**
     * 设置页面的透明度
     * 兼容华为手机（在个别华为手机上 设置透明度会不成功）
     *
     * @param bgAlpha 透明度   1表示不透明
     */
    public void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && 0x02 == requestCode) {
            SelectBean bean = data.getParcelableExtra("data");
            if (bean != null) {
                String json = bean.getJson();
                if (JSONUtil.validateJSONObject(json)) {
                    JSONObject object = JSON.parseObject(json);
                    LogUtil.prinlnLongMsg("fanglh1",JSON.toJSONString(object));
                    if (object != null) {
                        mList.get(click_positon).setSf_username(JSONUtil.getText(object, "sm_username"));
                        mList.get(click_positon).setSf_userid(JSONUtil.getText(object, "sm_userid"));
                        myAdapter.notifyDataSetChanged();
                    }
                    LogUtil.prinlnLongMsg("fanglh2",JSON.toJSONString(mList));
                }
            }
        }
    }
    private void doSelectEndDate(final int pos) {
        OASigninPicker picker = new OASigninPicker(this);
        picker.setRange(CalendarUtil.getYear()+1, CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                if (current_date.compareTo(time) > 0) {
                    ToastMessage(getString(R.string.cannot_select_beforedate));
                    return;
                }else {
                    if (JSON.toJSONString(mList).contains(time)){
                        ToastMessage(getString(R.string.relax_time_rapeat));
                        return;
                    }
                    mList.get(pos).setSf_date(time);
                    myAdapter.notifyDataSetChanged();
                }
            }
        });
        picker.show();
    }
}

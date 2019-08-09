package com.uas.appme.settings.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.base.BaseActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.time.wheel.DatePicker;
import com.core.widget.MyListView;
import com.core.widget.handwritedemo.LinePathView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.uas.appme.R;
import com.uas.appme.settings.adapter.WagesDetailsAdapter;
import com.uas.appworks.OA.erp.model.KVMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by FANGlh on 2017/11/10.
 * function:
 */
public class WagesDetailsActivity extends BaseActivity implements View.OnClickListener {
    private String checkYear;
    private String checkMonth;
    private MyListView mWagesLv;
    private Button mBtnSignature;
    private PopupWindow setWindow = null;//
    private LinePathView pathView;
    public static String plainpath= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sign.png";
    private ArrayList<KVMode> kvModeList;
    private JSONObject salaryObject;
    private WagesDetailsAdapter myAdapter;
    private String sl_id = null;
    private Boolean platform;
    private ImageView clear_im;
    private String master;
    private String emcode;
    private EditText msg_et;
    private ImageView received_im;
    private String password;
    private String vecode;
    private String phone;
    private TextView wageDateTv;
    private LinearLayout noWageLl;
    private Boolean haveData = true;
    private TextView announcement_tv;
    private int needSignature = 0;
    private JSONObject dataObject;
    private int sl_result = -1;
    private String sl_remark;
    private LinearLayout error_ll;
    private TextView error_tv;
    private boolean errorgone = false;
    private LinearLayout wages_ll;
    private LinearLayout announcement_ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wages_details_activity);
        platform = ApiUtils.getApiModel() instanceof ApiPlatform;
        initView();

    }


    private void showDatas(String result) {
        LogUtil.prinlnLongMsg("resultWage",result);
        haveData = true;
        wages_ll.setVisibility(View.VISIBLE);
        try {
            JSONObject salaryObject = JSON.parseObject(result).getJSONObject("salary");
            if (salaryObject != null){
                JSONArray configsArray = salaryObject.getJSONArray("configs");
                JSONObject dataObject = salaryObject.getJSONObject("data");
                if ( dataObject == null || dataObject.size() == 0 ||  ListUtils.isEmpty(configsArray)) {
                    showEnty();
                    return;
                }
                String gonggao = dataObject.getString("sl_text");
                if (!StringUtil.isEmpty(gonggao)){
                    announcement_ll.setVisibility(View.VISIBLE);
                    announcement_tv.setText("通知公告："+gonggao);
                } else if (gonggao == null)
                    announcement_ll.setVisibility(View.GONE);


                sl_id = dataObject.getString("sl_id");
                if (!ListUtils.isEmpty(configsArray) && dataObject.size() > 0){
                    for (int i = 0; i < configsArray.size(); i++) {
                        String key = configsArray.getJSONObject(i).getString("Caption");
                        String field = configsArray.getJSONObject(i).getString("Field");
                        if (!StringUtil.isEmpty(key) && !StringUtil.isEmpty(field)){
                            String value = dataObject.getString(field);
                            if(!StringUtil.isEmpty(value)){
                                KVMode kvMode = new KVMode(key,value);
                                kvModeList.add(kvMode);
                            }
                        }

                        if (i == configsArray.size() -1){
                            myAdapter.setModeList(kvModeList);
                            myAdapter.notifyDataSetChanged();
                            LogUtil.prinlnLongMsg("fanglh",JSON.toJSONString(kvModeList));
                            if (ListUtils.isEmpty(kvModeList))
                                showEnty();
                            else {
                                wages_ll.setVisibility(View.VISIBLE);
                                noWageLl.setVisibility(View.GONE);
                                showView(dataObject);
                            }
                        }
                    }

                }else{
                   showEnty();
                }
            }else{
                showEnty();
            }
       } catch (Exception e) {
            e.printStackTrace();
            showEnty();
        }
}

    private void showView(JSONObject dataObject) {

        sl_result = dataObject.getInteger("sl_result");
        sl_remark = dataObject.getString("sl_remark");

        if (!StringUtil.isEmpty(sl_remark) && sl_result == -1){
            error_ll.setVisibility(View.VISIBLE);
            error_tv.setText(sl_remark);
        }else {
            error_ll.setVisibility(View.GONE);
        }


       if (sl_result == 0 && !ListUtils.isEmpty(kvModeList)
               && StringUtil.isEmpty(sl_remark))
            mBtnSignature.setVisibility(View.VISIBLE);
        else
            mBtnSignature.setVisibility(View.GONE);

        if (sl_result == 1 & !ListUtils.isEmpty(kvModeList))
            received_im.setVisibility(View.VISIBLE);
        else
            received_im.setVisibility(View.GONE);
        invalidateOptionsMenu();
        needSignature = dataObject.getInteger("sl_signature");
        if (needSignature != 1)
            mBtnSignature.setText("确认签收");
        else
            mBtnSignature.setText("签字");
    }

    private void showEnty() {
        haveData = false;
        wages_ll.setVisibility(View.GONE);
        received_im.setVisibility(View.GONE);
        noWageLl.setVisibility(View.VISIBLE);
        mBtnSignature.setVisibility(View.GONE);
        announcement_ll.setVisibility(View.GONE);
        error_ll.setVisibility(View.GONE);
        Log.i("getEmSalary","showEnty");
        invalidateOptionsMenu();
    }

    private void initView() {
                emcode = CommonUtil.getEmcode();
//        emcode = "U0747";
        master = CommonUtil.getSharedPreferences(ct, "erp_master");

        setTitle(getString(R.string.salary_search));

        mWagesLv = (MyListView) findViewById(R.id.wages_lv);
        wages_ll = (LinearLayout) findViewById(R.id.wages_ll);
        mBtnSignature = (Button) findViewById(R.id.btn_signature); mBtnSignature.setOnClickListener(this);
        kvModeList = new ArrayList<>();
        myAdapter = new WagesDetailsAdapter(this);

        //设置适配器
        mWagesLv.setAdapter(myAdapter);

        received_im = (ImageView) findViewById(R.id.received_im);
        wageDateTv = (TextView) findViewById(R.id.wage_date_tv);
        wageDateTv.setOnClickListener(this);
        noWageLl = (LinearLayout) findViewById(R.id.wage_nodata_ll);
        announcement_tv = (TextView) findViewById(R.id.announcement);
        announcement_ll = (LinearLayout) findViewById(R.id.announcement_ll);
        error_ll = (LinearLayout) findViewById(R.id.error_ll);
        error_tv = (TextView) findViewById(R.id.error_tv);

//        findViewById(R.id.h_sign_tv).setOnClickListener(this);
        Intent hh =  getIntent();
        String result = hh.getStringExtra("WageDatas");
        checkYear = hh.getStringExtra("checkYear");
        checkMonth = hh.getStringExtra("checkMonth");
        String title = checkYear + "年" + checkMonth + "月"+"工资";
        if (!StringUtil.isEmpty(checkYear) && !StringUtil.isEmpty(checkMonth))
            wageDateTv.setText(title);
        showDatas(result);

        password = hh.getStringExtra("password");
        vecode = hh.getStringExtra("vecode");
        phone = hh.getStringExtra("phone");

    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(resultCode==101 && requestCode == 101){
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 2;
//            Bitmap bm = BitmapFactory.decodeFile(plainpath, options);
//            s_image.setImageBitmap(bm);
//        }
//
//    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_signature){
            if (!haveData){
                ToastMessage(getString(R.string.cannot_sign));
                return;
            }
            if (needSignature == 1)
                showSiganWindow();
            else
                doNoSignatureSubmit();
        }else if (v.getId() == R.id.clear_im){
            pathView.clear();
        }else if (v.getId() == R.id.cancel_tv){
            pathView.clear();
            closePopupWindow();
        } else if (v.getId() == R.id.submit_btn){
            if (pathView.getTouched())
            {
                if (!CommonUtil.isNetWorkConnected(ct)){
                    ToastMessage(getString(R.string.common_notlinknet));
                    return;
                }

                try {
                    pathView.save(plainpath,false,10);  //将图片路径保存到plainpath中，并获取Bimap对象
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap pathBm = BitmapFactory.decodeFile(plainpath);
                    doNewSubmit(pathBm, plainpath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(this,getString(R.string.not_sign)+"~", Toast.LENGTH_SHORT).show();
            }
        }else if (v.getId() == R.id.sure_tv){
            if (StringUtil.isEmpty(msg_et.getText().toString())){
                ToastMessage(getString(R.string.input_your_error));
                return;
            }
            if (!CommonUtil.isNetWorkConnected(ct)){
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }
            doErrorMsgHandle();
        }else if (v.getId() == R.id.wage_date_tv){
            DatePicker picker = new DatePicker(this, DatePicker.YEAR_MONTH);
            picker.setRange(CommonUtil.getNumByString(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy"))-2, CommonUtil.getNumByString(DateFormatUtil.long2Str(System.currentTimeMillis(), "yyyy")));
            picker.setSelectedItem(
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH) + 1);
            picker.setOnDatePickListener(new DatePicker.OnYearMonthPickListener() {
                @Override
                public void onDatePicked(String year, String month) {
                    String myearmonth =  year + "年" + month+"月";
                    checkYear = year;
                    checkMonth = month;
                    wageDateTv.setText(myearmonth+getString(R.string.salary));
                    searchWage();
                }
            });
            picker.show();
        }else if (v.getId() == R.id.cancel_error_tv){
            popupWindow.dismiss();
        }
    }

    private void searchWage() {
        if (!CommonUtil.isNetWorkConnected(ct)){
            ToastMessage(getString(R.string.common_notlinknet));
            return;
        }
        if (!ListUtils.isEmpty(kvModeList)) {
            kvModeList.clear();
            myAdapter.notifyDataSetChanged();
        }

        errorgone = false;
                HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//        HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/salary/getEmSalary.action")
                .add("emcode", emcode)
                .add("password",password)
                .add("phone",phone)
                .add("vecode",vecode)
                .add("date",checkYear+"-"+checkMonth)
                .add("master",master)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("getEmSalary", o.toString()+"");
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                if (o.toString().contains("success") && JSON.parseObject(o.toString()).getBoolean("success")){
                   showDatas(o.toString());
                }else if (o.toString().contains("reason")){
                    ToastMessage(JSON.parseObject(o.toString()).getString("reason"));
                }else if (o.toString().contains("exceptionInfo")){
                    ToastMessage(JSON.parseObject(o.toString()).getString("exceptionInfo"));
                }

            }
        }));
    }

    private void doErrorMsgHandle() {
        HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//        HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/salary/salaryWrong.action")
                .add("sl_id",sl_id)
                .add("emcode",emcode)
                .add("msg",msg_et.getText().toString())
                .add("result","0")
                .add("master", master)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("salaryWrong", o.toString()+"");
                if (o.toString().contains("success") && JSON.parseObject(o.toString()).getBoolean("success")){
                    ToastMessage(getString(R.string.send_error_ok));
                    mBtnSignature.setVisibility(View.GONE);
                    error_ll.setVisibility(View.VISIBLE);
                    error_tv.setText(msg_et.getText().toString());
                    sl_result = -1;
                    invalidateOptionsMenu();
                    popupWindow.dismiss();
                }

            }
        }));
    }

    private void doNewSubmit(Bitmap pathBm, final String plainpath) {
//        HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
        String url = CommonUtil.getAppBaseUrl(ct)+"mobile/salary/salaryBack.action";
        RequestParams params = new RequestParams();
        params.addBodyParameter("emcode", emcode);
        params.addBodyParameter("sl_id",sl_id);
        params.addBodyParameter("result","true");
        params.addQueryStringParameter("master", master);
        Log.i("urlparams",plainpath+"");
        if (needSignature == 1 && !StringUtil.isEmpty(plainpath)){
            params.addBodyParameter("img",new File(plainpath));
        }else {
//            params.addHeader("Content-Type","multipart/form-data");
            params.addBodyParameter("img","");
        }
//        else
//            params.addBodyParameter("img",new File());
        final HttpUtils http = new HttpUtils();
        Log.i("urlparams",url+params+"");

        http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                if (needSignature == 1)
                    ViewUtil.ToastMessage(ct, getString(R.string.sending_picture)+"...");
            }
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                if (needSignature == 1)
                    ViewUtil.ToastMessage(ct, getString(R.string.Uploaded_successfully));
                Log.i("doNewSubmit",JSON.parseObject(responseInfo.result).toJSONString());
                if (JSONUtil.validate(responseInfo.result) && JSON.parseObject(responseInfo.result).getBoolean("success")) {
                    Toast.makeText(ct,getString(R.string.ss_send_success),Toast.LENGTH_LONG).show();
                    received_im.setVisibility(View.VISIBLE);
                    mBtnSignature.setVisibility(View.GONE);
                    errorgone = true;
                    invalidateOptionsMenu();
                    closePopupWindow();
                }
                progressDialog.dismiss();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        finish();
                    }
                },1000);
            }

            @Override
            public void onFailure(HttpException error, String msg) {
                ViewUtil.ToastMessage(ct, getString(R.string.common_save_failed) + msg);
                Log.i("doNewSubmit",error+","+msg);
                progressDialog.dismiss();
            }
        });
    }

    //不需要签名情况下的签收
    private void doNoSignatureSubmit() {
        if (!CommonUtil.isNetWorkConnected(ct)){
            ToastMessage(getString(R.string.common_notlinknet));
            return;
        }
        HttpClient httpClient = new HttpClient.Builder(CommonUtil.getAppBaseUrl(ct)).isDebug(true).build(true);
//        HttpClient httpClient = new HttpClient.Builder("http://192.168.253.58:8080/ERP/").isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("mobile/salary/salaryBackNoSignature.action\n")
                .add("emcode", emcode)
                .add("sl_id",sl_id)
                .add("result",true)
                .add("master", master)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("salaryBack", o.toString()+"");
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                if (JSONUtil.validate(o.toString()) && o.toString().contains("success") && JSON.parseObject(o.toString()).getBoolean("success")) {
                    Toast.makeText(ct,getString(R.string.ss_send_success),Toast.LENGTH_LONG).show();
                    received_im.setVisibility(View.VISIBLE);
                    mBtnSignature.setVisibility(View.GONE);
                    errorgone = true;
                    invalidateOptionsMenu();
                    closePopupWindow();
                }else {
                    ViewUtil.ToastMessage(ct, getString(R.string.common_save_failed));
                }
                progressDialog.dismiss();
            }
        }));

    }
    private byte[] getBitmapByte(Bitmap bitmap){   //将bitmap转化为二进制字节流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
    private void showSiganWindow() {
        if (setWindow == null) initPopupWindow();
        setWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
//        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.sigature_pop, null);

        viewContext.findViewById(R.id.submit_btn).setOnClickListener(this);
        viewContext.findViewById(R.id.clear_im).setOnClickListener(this);
        viewContext.findViewById(R.id.cancel_tv).setOnClickListener(this);
//        viewContext.findViewById(R.id.hscreen_btn).setOnClickListener(this);
        clear_im = (ImageView) viewContext.findViewById(R.id.clear_im);

        pathView = (LinePathView) viewContext.findViewById(R.id.sigature_view);
        pathView.setPaintWidth(5);

        setWindow = new PopupWindow(viewContext,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, true);
        setWindow.setOutsideTouchable(true);
        setWindow.setAnimationStyle(com.uas.appworks.R.style.MenuAnimationFade);
        setWindow.setBackgroundDrawable(mContext.getResources().getDrawable(com.uas.appworks.R.drawable.bg_popuwin));
        setWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
    }
    private void closePopupWindow() {
        if (setWindow != null)
            setWindow.dismiss();
        DisplayUtil.backgroundAlpha(this, 1f);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if(haveData && StringUtil.isEmpty(sl_remark) && sl_result == 0 && !errorgone)
            getMenuInflater().inflate(R.menu.wage_error, menu);
        else
            menu.clear();

        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.wage_error, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.wage_error){
           showErrorWindow();
        }
        return super.onOptionsItemSelected(item);
    }

    private PopupWindow popupWindow;
    private void showErrorWindow() {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.input_error_msg, null);

        // 设置按钮的点击事件
        msg_et = (EditText) contentView.findViewById(R.id.msg_et);
        contentView.findViewById(R.id.sure_tv).setOnClickListener(this);
        contentView.findViewById(R.id.cancel_error_tv).setOnClickListener(this);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(this, 300);
        h_screen = DisplayUtil.dip2px(this, 250);

        popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow = new PopupWindow(contentView,
                w_screen, h_screen,true);
        popupWindow.setAnimationStyle(R.style.MenuAnimationFade);
        popupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                closePopupWindow();
            }
        });
        popupWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

}

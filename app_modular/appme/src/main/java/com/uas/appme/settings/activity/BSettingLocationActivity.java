package com.uas.appme.settings.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.system.PermissionUtil;
import com.common.ui.CameraUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.SupportToolBarActivity;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.MyListView;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.appme.R;
import com.uas.appme.settings.model.BSetLocationBean;
import com.umeng.socialize.utils.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by FANGlh on 2017/10/12.
 * function:
 */

public class BSettingLocationActivity extends SupportToolBarActivity implements View.OnClickListener{
    private MyListView mComList;
    private List<BSetLocationBean> mList;  //进行保存的员工休息数据列表
    private BSetLocationAdapter myAdapter;
    private String sc_industry;
    private String sc_industrycode;
    private Boolean update = false; //false 为正常录入，true为更新
    private Button save_bt;
    private RelativeLayout add_new_rl;
    private String st_id;
    private String updateData;
    private Button deleteBtn;
    private ArrayList<String> mPhotoList;
    private int image_position; // 点击点击照片添加/更换 记住的position
    private Uri mNewPhotoUri;// 拍照和图库 获得图片的URI
    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;// 图库
    private Boolean updateChangeP = false; //更新是否更换了照片，若没换
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_bcom_setting_activity);
        progressDialog.show();
        initView();
        judgeWhichType();
        String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};
        for (String permission : permissions) {
            if (PermissionUtil.lacksPermissions(ct, permission)) {
                PermissionUtil.requestPermission(this, PermissionUtil.DEFAULT_REQUEST, permission);
            }
        }
    }

    private void judgeWhichType() {
        if (!CommonUtil.isNetWorkConnected(ct)){
            ToastMessage(getString(R.string.common_notlinknet));
            initData();
            return;
        }
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appCompanyType")
                .add("companyid", CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"))
                .add("token",MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(),new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) return;
                LogUtil.prinlnLongMsg("appCompanyType", o.toString()+"");

                //{"result":[{"sc_industry":"医疗","sc_industrycode":"10001"}]}
                JSONArray array = JSON.parseObject(o.toString()).getJSONArray("result");
                if (ListUtils.isEmpty(array)) {
                    progressDialog.dismiss();
                    return;
                }
                JSONObject object = array.getJSONObject(0);
                if (object == null) return;
                sc_industry = object.getString("sc_industry");
                sc_industrycode = object.getString("sc_industrycode");
                Log.i("fanglh1",sc_industry+"");
                initData();
            }
        }));

    }
    private void initData() {

        updateData = getIntent().getStringExtra("updateData");
        if (!StringUtil.isEmpty(updateData) &&  JSONUtil.validate(updateData) ){  // 更新状态则不显示 + 新增按钮，且保存改为更新
            deleteBtn.setVisibility(View.VISIBLE);
            update = true;
            save_bt.setText(getString(R.string.common_update_button));
            add_new_rl.setVisibility(View.GONE);
           setTitle(getString(R.string.update_room));

            st_id = JSON.parseObject(updateData).getString("st_id");
            BSetLocationBean model = new BSetLocationBean();
            model.setSt_companyname(JSON.parseObject(updateData).getString("st_companyname"));
            model.setSt_companyid(JSON.parseObject(updateData).getString("st_companyid"));
            model.setSt_siid(JSON.parseObject(updateData).getString("st_siid"));
            model.setSt_name(JSON.parseObject(updateData).getString("st_name"));
            model.setSt_servicetime(JSON.parseObject(updateData).getString("st_servicetime"));
            model.setSt_imageurl(JSON.parseObject(updateData).getString("st_imageurl"));
            mList.add(model);
            myAdapter.setIndustry(sc_industry);
            myAdapter.notifyDataSetChanged();
        }else {
            deleteBtn.setVisibility(View.GONE);
            BSetLocationBean model = new BSetLocationBean();
            model.setSt_companyname(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_commpany"));
            model.setSt_companyid(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
            model.setSt_siid(StringUtil.isEmpty(sc_industrycode) ? "0" : sc_industrycode);
            model.setSt_name("");
            model.setSt_servicetime("0");
            model.setSt_imageurl("");
//            model.setSt_id("0");
            mList.add(model);
            myAdapter.setIndustry(sc_industry);
            myAdapter.notifyDataSetChanged();
        }
        Log.i("fanglh",sc_industry+"");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        },2000);
    }

    private void initView() {
        mList = new ArrayList<>();
        mComList = (MyListView) findViewById(R.id.com_list);
        add_new_rl = (RelativeLayout) findViewById(R.id.add_new_rl);
        add_new_rl.setOnClickListener(this);
        myAdapter = new BSetLocationAdapter(this);
        myAdapter.setModelList(mList);
        mComList.setAdapter(myAdapter);
        save_bt = (Button) findViewById(R.id.save_bt);
        save_bt.setOnClickListener(this);

        deleteBtn = (Button)findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(this);

        mPhotoList = new ArrayList<>();
        myAdapter.setPhotoList(mPhotoList);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (StringUtil.isEmpty(updateData)) {
            getMenuInflater().inflate(R.menu.bsetting_more, menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }
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
                    .putExtra("type","place"));
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_new_rl){
            LogUtil.prinlnLongMsg("mPhotoList",JSON.toJSONString(mPhotoList));
            if(!ListUtils.isEmpty(mList)){
                if (StringUtil.isEmpty(mList.get(mList.size()-1).getSt_name())){
                    ToastMessage(getString(R.string.input_name1));
                    return;
                }
                if (mPhotoList.size() < mList.size()){
                    ToastMessage(getString(R.string.add_img));
                    return;
                }
            }

            BSetLocationBean model = new BSetLocationBean();
            model.setSt_companyname(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_commpany"));
            model.setSt_companyid(CommonUtil.getSharedPreferences(MyApplication.getInstance(),"erp_uu"));
            model.setSt_siid(StringUtil.isEmpty(sc_industrycode) ? "0" : sc_industrycode);
            model.setSt_name("");
            model.setSt_servicetime("0");
            model.setSt_imageurl("");
//            model.setSt_id("0");
            mList.add(model);
            myAdapter.notifyDataSetChanged();
        }else if (v.getId() == R.id.save_bt){
            LogUtil.prinlnLongMsg("mList", JSON.toJSONString(mList));
            if (ListUtils.isEmpty(mList)) return;
            if (StringUtil.isEmpty(mList.get(mList.size()-1).getSt_name())){
                ToastMessage(getString(R.string.input_name1));
                return;
            }
            if (!CommonUtil.isNetWorkConnected(this)) {
                ToastMessage(getString(R.string.common_notlinknet));
                return;
            }else {
                if (update && !StringUtil.isEmpty(st_id)){
                    if (updateChangeP)   // 更换过照片
                        doJudegeCNow_p();//开始上传第一张图片
                    else
                        doUpdate();
                }
                else
                    doSaveJudge(mList);
            }

        }else if (v.getId() == R.id.deleteBtn){
            doDelete();
        }
    }

    private void doDelete() {
        if (!CommonUtil.isNetWorkConnected(ct)){
            ToastMessage(getString(R.string.common_notlinknet));
            return;
        }
        String keyfield = "st_id";;
        String tablename = "ServiceType";
        String id = st_id;

        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appStoreDel")
                .add("keyfield", keyfield)
                .add("id",id)
                .add("tablename",tablename)
                .add("token", MyApplication.getInstance().mAccessToken)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                LogUtil.prinlnLongMsg("appStoreDel", o.toString() + "");
                try {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")){
                        Toast.makeText(ct,getString(R.string.delete_all_succ),Toast.LENGTH_LONG).show();
                        setResult(20);
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    private void doUpdate() {
        if (StringUtil.isEmpty(mList.get(0).getSt_name())){
            ToastMessage(getString(R.string.input_name1));
           return;
        }else {
            Map<String,Object> param = new HashMap<>();
            param.put("st_id",st_id);
            param.put("st_name",mList.get(0).getSt_name());
            param.put("st_siid",mList.get(0).getSt_siid());
            param.put("st_companyid",mList.get(0).getSt_companyid());
            param.put("st_imageurl",mList.get(0).getSt_imageurl());
            param.put("st_servicetime",mList.get(0).getSt_servicetime());
            progressDialog.show();save_bt.setEnabled(false);

            HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);
            httpClient.Api().send(new HttpClient.Builder()
                    .url("user/appServiceSet")
                    .add("map",JSON.toJSONString(param))
                    .add("token",MyApplication.getInstance().mAccessToken)
                    .method(Method.POST)
                    .connectTimeout(10000)
                    .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
                @Override
                public void onResponse(Object o) {
                    if (!JSONUtil.validate(o.toString()) || o == null) return;
                    LogUtil.prinlnLongMsg("appServiceSet", o.toString()+"");
                    if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")) {
                        Toast.makeText(ct,getString(R.string.update_success),Toast.LENGTH_LONG).show();
                        setResult(20);
                        finish();
                    }
                    progressDialog.dismiss();save_bt.setEnabled(true);
                }

                @Override
                public void onFailure(Object t) {
                    progressDialog.dismiss();save_bt.setEnabled(true);
                }
            }));

        }

    }

    private void doSaveJudge(List<BSetLocationBean> mList) {
        if (mPhotoList.size() < mList.size()){
            ToastMessage(getString(R.string.add_img));
            return;
        }
        for (int i = 0; i < mList.size(); i++) {
            if (StringUtil.isEmpty(mList.get(i).getSt_name())){
                ToastMessage(getString(R.string.input_name1));
                break;
            }else if (i==mList.size()-1){
                if (updateChangeP)   // 更换过照片
                    doJudegeCNow_p();//开始上传第一张图片
                else
                    doSaveDatasList();
            }
        }


    }
    private int now_p = 0;
    private void sendPictureRequest(String path) {
        if (StringUtil.isEmpty(path)) return;
        File waterBitmapToFile=  new File(path);
        if (!waterBitmapToFile.isFile()){
            return;
        }else {
            com.lidroid.xutils.http.RequestParams params = new com.lidroid.xutils.http.RequestParams();
            params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(ct, "erp_master"));
            params.addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            params.addBodyParameter("userid", MyApplication.getInstance().mLoginUser.getUserId());
            params.addBodyParameter("file1", waterBitmapToFile);
            String url = "http://113.105.74.140:8080/upload/UploadServlet";
            final HttpUtils http = new HttpUtils();
            http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
                @Override
                public void onStart() {
                    progressDialog.show();
                    ViewUtil.ToastMessage(ct, getString(com.uas.appworks.R.string.sending_picture)+"...");
                }
                @Override
                public void onLoading(long total, long current, boolean isUploading) {
                    if (isUploading) {
                    } else {
                    }
                }

                @Override
                public void onSuccess(ResponseInfo<String> responseInfo) {
                    try {
                    if (JSONUtil.validate(responseInfo.result) && responseInfo.result.contains("success") &&
                            JSON.parseObject(responseInfo.result).getBoolean("success")) {
                        LogUtil.prinlnLongMsg("UploadServlet", responseInfo.result + "");
                            JSONObject object = JSON.parseObject(responseInfo.result);
                            JSONObject dataobject = object.getJSONObject("data");
                            if (dataobject == null) {
                                doJudegeCNow_p();
                                return;
                            }
                            JSONArray imagearray = dataobject.getJSONArray("images");
                            if (ListUtils.isEmpty(imagearray)) {
                                doJudegeCNow_p();
                                return;
                            }
                            String oUrl = imagearray.getJSONObject(0).getString("oUrl");
                            if (!StringUtil.isEmpty(oUrl))
                                mList.get(now_p).setSt_imageurl(oUrl);
                            else
                                mList.get(now_p).setSt_imageurl("");

                            now_p++;
                            LogUtil.prinlnLongMsg("mList",JSON.toJSONString(mList));
                            doJudegeCNow_p();


                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(HttpException error, String msg) {
                    now_p++;
                    doJudegeCNow_p();
                    ViewUtil.ToastMessage(ct, getString(com.uas.appworks.R.string.common_save_failed) + msg);
                    progressDialog.dismiss();
                }
            });
        }
    }

    //判断当前上传了几张照片，进行是否上传数据操作
    private void doJudegeCNow_p() {
        if (now_p == mList.size()){
            if (update && !StringUtil.isEmpty(st_id))
                doUpdate();
            else
                doSaveDatasList();
        } else{
            if (now_p < mPhotoList.size())
                sendPictureRequest(mPhotoList.get(now_p));
        }

    }

    private int getID(String chche) {
        if (StringUtil.isEmpty(chche)) return 0;
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(chche);
        if (m.find()) {
            return Integer.parseInt(m.group(0));
        }
        return -1;
    }
    private void doSaveDatasList() {
        progressDialog.show();save_bt.setEnabled(false);
        HttpClient httpClient = new HttpClient.Builder(Constants.IM_BASE_URL()).isDebug(true).build(true);

//        httpClient.getHeaders().remove("Content-Type");

        httpClient.Api().send(new HttpClient.Builder()
                .url("user/appBatchPlace")
                .add("map",JSON.toJSONString(mList))
                .add("token",MyApplication.getInstance().mAccessToken)
                .connectTimeout(10000)
                .method(Method.POST)
                .build(),new ResultSubscriber<>(new Result2Listener<Object>() {
            @Override
            public void onResponse(Object o) {
                if (!JSONUtil.validate(o.toString()) || o == null) {
                    progressDialog.dismiss();save_bt.setEnabled(true );
                    return;
                }
                LogUtil.prinlnLongMsg("appBatchPlace", o.toString()+"");
                if (o.toString().contains("result") && JSON.parseObject(o.toString()).getBooleanValue("result")) {
                    Toast.makeText(ct,getString(R.string.common_save_success),Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ct,BSetComRestListActivity.class)
                            .putExtra("sc_industry",sc_industry)
                            .putExtra("sc_industrycode",sc_industrycode)
                            .putExtra("type","place"));
                    finish();
                }
                progressDialog.dismiss();save_bt.setEnabled(true);
            }

            @Override
            public void onFailure(Object t) {
                progressDialog.dismiss();save_bt.setEnabled(true);
            }
        }));
    }

    private class BSetLocationAdapter extends BaseAdapter{
        private Context mContext;
        private List<BSetLocationBean> modelList;
        private ArrayList<String> photoList;
        private String industry;

        public String getIndustry() {
            return industry;
        }

        public void setIndustry(String industry) {
            this.industry = industry;
        }

        public ArrayList<String> getPhotoList() {
            return photoList;
        }

        public void setPhotoList(ArrayList<String> photoList) {
            this.photoList = photoList;
        }

        public List<BSetLocationBean> getModelList() {
            return modelList;
        }

        public void setModelList(List<BSetLocationBean> modelList) {
            this.modelList = modelList;
        }

        public BSetLocationAdapter(Context mContext){
            this.mContext = mContext;
        }
        @Override
        public int getCount() {
            return ListUtils.isEmpty(modelList) ? 0 : modelList.size();
        }

        @Override
        public Object getItem(int position) {
            return modelList.get(position);
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
                convertView =  View.inflate(mContext, R.layout.com_location_input_item,null);
                viewHolder.name_tv = (FormEditText) convertView.findViewById(R.id.name_tv);
                viewHolder.image_im = (ImageView) convertView.findViewById(R.id.image_im);
                viewHolder.sTime_rl = (RelativeLayout) convertView.findViewById(R.id.service_time_rl);
                viewHolder.sTime_et = (FormEditText)convertView.findViewById(R.id.service_time_et);
                viewHolder.delete_tv = (TextView) convertView.findViewById(R.id.delete_tv);
                convertView.setTag(viewHolder);
                viewHolder.name_tv.setFocusable(false);
                viewHolder.name_tv.setKeyListener(null);
                viewHolder.sTime_et.setFocusable(false);
                viewHolder.sTime_et.setKeyListener(null);
            }else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if("会所".equals(industry))
                viewHolder.sTime_rl.setVisibility(View.VISIBLE);
            else
                viewHolder.sTime_rl.setVisibility(View.GONE);

            viewHolder.name_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doInputName(1,position);
                }
            });
            viewHolder.sTime_et.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doInputName(2,position);
                }
            });
      /*     viewHolder.name_tv.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    String strChche =  s.toString().replace(" ", "");//去除空格
                    strChche = strChche.replace(" ", " ");//去除空格
                    if(StringUtil.isEmpty(strChche.toString())) return;
                    mList.get(position).setSt_name(strChche.toString());
                    myAdapter.notifyDataSetChanged();
                }
            });*/

            viewHolder.image_im.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    image_position = position;  //点击的图片位置
                    if (PermissionUtil.lacksPermissions(ct, Manifest.permission.CAMERA)) {
                        PermissionUtil.requestPermission(BSettingLocationActivity.this,PermissionUtil.DEFAULT_REQUEST,Manifest.permission.CAMERA);
                        ToastUtil.showToast(ct,R.string.not_camera_permission);
                    } else {
                        showSelectPictureDialog();//添加
                    }
                }
            });
            viewHolder.delete_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (modelList.size() > 1){
                        mList.remove(position);
                        if (mPhotoList.size() > position)
                            mPhotoList.remove(position);

                        notifyDataSetChanged();
                    }
                }
            });

//            if (modelList.size() == 1) viewHolder.delete_tv.setVisibility(View.GONE);
//            else viewHolder.delete_tv.setVisibility(View.VISIBLE);

            //显示名称
            if (!ListUtils.isEmpty(modelList) && !StringUtil.isEmpty(modelList.get(position).getSt_name()))
                viewHolder.name_tv.setText(modelList.get(position).getSt_name()+"");
            else
                viewHolder.name_tv.setText(null);

            //显示照片
            if (update && !updateChangeP){
                ImageLoader.getInstance().displayImage(modelList.get(position).getSt_imageurl(),viewHolder.image_im);
            }else {
                if (!ListUtils.isEmpty(photoList) && position < photoList.size() &&
                        !StringUtil.isEmpty(photoList.get(position)))
                    ImageLoader.getInstance().displayImage(Uri.fromFile(new File(photoList.get(position))).toString(), viewHolder.image_im);
                else
                    viewHolder.image_im.setImageResource(R.drawable.add_picture); //设置为默认头像不然会被复用
            }



            //显示服务时间/分钟
            if (!ListUtils.isEmpty(modelList) && !StringUtil.isEmpty(modelList.get(position).getSt_servicetime())){
                viewHolder.sTime_et.setText(modelList.get(position).getSt_servicetime());
            }else{
                viewHolder.sTime_et.setText(null);
            }
            return convertView;
        }
        class ViewHolder{
            FormEditText name_tv;
            ImageView image_im;
            RelativeLayout sTime_rl;
            FormEditText sTime_et;
            TextView delete_tv;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    String path = mNewPhotoUri.getPath();
                    addPhotoList(path);
                } else {
                    ToastUtil.showToast(this, com.uas.appworks.R.string.c_take_picture_failed);
                }
            }
        }else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 传统单选方式，选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK){
                if (data != null && data.getData() != null){
                    String path= CameraUtil.getImagePathFromUri(this, data.getData());
                    addPhotoList(path);
                }
            }
        }
    }

    private void addPhotoList(String path) {
        if (image_position > mPhotoList.size() -1){
            mPhotoList.add(path);
        }else if (image_position <= mPhotoList.size() -1){
            mPhotoList.set(image_position,path); //替换
        }
        myAdapter.notifyDataSetChanged();
        Log.i("files0x01",path);
        updateChangeP = true;
    }

    private void showSelectPictureDialog() {
        String[] items = new String[]{getString(com.uas.appworks.R.string.c_take_picture), getString(com.uas.appworks.R.string.c_photo_album)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            try {
                                takePhoto();
                            } catch (Exception e) {
                                String message=e.getMessage();
                                if (!StringUtil.isEmpty(message)&&message.contains("Permission")){
                                    ToastUtil.showToast(ct,R.string.not_system_permission);
                                }
                            }
                        } else {
                            CameraUtil.pickImageSimple(BSettingLocationActivity.this, REQUEST_CODE_PICK_PHOTO);  //传统单选方式
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void takePhoto() {
        try {
            mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().getLoginUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
            CameraUtil.captureImage(this, mNewPhotoUri, REQUEST_CODE_CAPTURE_PHOTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PopupWindow popupWindow = null;
    private void doInputName(final int type, final int position) {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(ct).inflate(
                R.layout.item_edit_location_pop, null);

        // 设置按钮的点击事件
        final EditText editname_et = (EditText) contentView.findViewById(R.id.editname_et);
        final EditText editname_et2 = (EditText) contentView.findViewById(R.id.editname_et2);
        TextView title_tv = (TextView) contentView.findViewById(R.id.title_tv);
        editname_et.setText(mList.get(position).getSt_name()+"");
        editname_et2.setText(mList.get(position).getSt_servicetime()+"");
        DisplayMetrics dm = getResources().getDisplayMetrics();
        if (type==1) {
            editname_et.setVisibility(View.VISIBLE);
            editname_et2.setVisibility(View.GONE);
            title_tv.setText(getString(R.string.please_input_room));
        } else if(type==2) {
            editname_et.setVisibility(View.GONE);
            editname_et2.setVisibility(View.VISIBLE);
            title_tv.setText(getString(R.string.please_input_time));
        }
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
                if (type==1){
                    if (StringUtil.isEmpty(editname_et.getText().toString())){
                        ToastMessage(getString(R.string.please_input_room));
                        return;
                    }else {
                        mList.get(position).setSt_name(editname_et.getText().toString());
                        myAdapter.notifyDataSetChanged();
                    }
                }else if (type==2){
                        mList.get(position).setSt_servicetime(
                                StringUtil.isEmpty(editname_et2.getText().toString()) ? "0" :editname_et2.getText().toString());
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
                setBackgroundAlpha(BSettingLocationActivity.this, 1f);
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
}

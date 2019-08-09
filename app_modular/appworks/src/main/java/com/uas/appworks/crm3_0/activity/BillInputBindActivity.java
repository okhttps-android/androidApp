package com.uas.appworks.crm3_0.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.CalendarUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.file.FileUtils;
import com.common.ui.CameraUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.OABaseActivity;
import com.core.model.SelectBean;
import com.core.utils.StatusBarUtil;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.modular.apputils.R;
import com.modular.apputils.activity.BillDetailsActivity;
import com.modular.apputils.activity.BillListActivity;
import com.modular.apputils.activity.SelectNetAcitivty;
import com.modular.apputils.adapter.BillAdapter;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.model.BillListConfig;
import com.modular.apputils.presenter.BillPresenter;
import com.modular.apputils.presenter.imp.IBill;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 动态表单单据录入界面，后续会尽量关闭修改，添加拓展功能
 */
public class BillInputBindActivity extends OABaseActivity implements IBill, BillAdapter.OnAdapterListener {
    public final int REQUESTCODE_C = 0x11;
    public final int REQUESTCODE_DB_FIND = 0x12;
    public final int REQUESTCODE_ENCLOSURE = 0x13;
    public final int REQUESTCODE_C_NET = 0x14;
    public final int REQUESTCODE_ENCLOSURE_LOW = 0x15;
    public final int REQUEST_CODE_CAPTURE_PHOTO = 0x16;

    public RecyclerView mRecyclerView;
    public BillPresenter mBillPresenter;
    public BillAdapter mBillAdapter;
    private String mListCondition;
    public int selectPosition = -1;//当前选择调转界面的index，本来不想添加全局变量的，没有办法
    private HashMap<String, String> dbFindCondition;
    private String phone;
    private String name;

    //TODO 重写方法实现自定义的 BillPresenter,必须继承于BillPresenter
    public BillPresenter newBillPresenter() {
        return new BillPresenter(this, this);
    }

    //TODO 重写方法实现自定义的 BillAdapter,必须继承于BillAdapter
    public BillAdapter newBillAdapter(List<BillGroupModel> groupModels) {
        return new BillAdapter(ct, groupModels, this);
    }

    //TODO  重写方法实现自定义的 提交完成后处理
    public void commitSuccess(final int keyValue, String code) {
        ToastMessage("提交成功！");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mContext == null) return;
                //使用新界面
                startActivity(new Intent(ct, BillDetailsActivity.class)
                        .putExtra(Constants.Intents.CALLER, mBillPresenter.getFormCaller())
                        .putExtra(Constants.Intents.TITLE, getToolBarTitle())
                        .putExtra(Constants.Intents.ID, keyValue));
//                startActivity(new Intent("com.modular.form.erp.activity.CommonDocDetailsActivity")
//                        .putExtra("caller", mBillPresenter.getFormCaller())
//                        .putExtra("keyValue", keyValue)
//                        .putExtra("update", "1")
//                        .putExtra("title", getToolBarTitle())
//                        .putExtra("statusKey", mBillPresenter.getStatusField())
//                        .putExtra("status", "已提交"));
                finish();
                overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
            }
        }, 2000);
    }

    //TODO  重写方法实现自定义的初始化
    public void init() {
        Intent intent = getIntent();
        if (intent != null) {
            Serializable mSerializable = intent.getSerializableExtra(Constants.Intents.DB_FIND_CONDITION);
            if (mSerializable != null && mSerializable instanceof HashMap) {
                dbFindCondition = (HashMap<String, String>) mSerializable;
            }
        }
        mBillPresenter = newBillPresenter();
        mBillPresenter.start(getIntent());
        if (getIntent() != null) {
            mListCondition = getIntent().getStringExtra(Constants.Intents.LIST_CONDITION);
            phone=getIntent().getStringExtra("phone");
            name=getIntent().getStringExtra("name");
        }
        if (TextUtils.isEmpty(mListCondition)) {
            mListCondition = "1=1";
        }

      
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_input);
        initView();
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bill_input, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.saveAndSubmit) {
            mBillPresenter.saveAndSubmit(mBillAdapter.getBillGroupModels());
        } else if (item.getItemId() == R.id.list) {
            toDataFormList();
        }
        return super.onOptionsItemSelected(item);
    }

    public void toDataFormList() {
        ArrayList<BillListConfig> billListConfigs = new ArrayList<>();
        BillListConfig mBillListConfig = new BillListConfig();
        mBillListConfig.setTitle("");
        mBillListConfig.setCaller(mBillPresenter.getFormCaller());
        mBillListConfig.setCondition(mListCondition);
        billListConfigs.add(mBillListConfig);
        startActivity(new Intent(ct, BillListActivity.class)
                .putExtra(Constants.Intents.CONFIG, billListConfigs)
                .putExtra(Constants.Intents.TITLE, getToolBarTitle())
                .putExtra(Constants.Intents.DETAILS_CLASS, BillDetailsActivity.class)
        );
//        startActivity(new Intent("com.modular.work.OA.erp.activity.form.FormListSelectActivity")
//                .putExtra("caller", mBillPresenter.getFormCaller())
//                .putExtra("statusKey", mBillPresenter.getStatusField())//传状态key
//                .putExtra("title", getToolBarTitle()));

    }


    private void initView() {
        mRecyclerView = findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(ct, LinearLayout.VERTICAL));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            final View mRootLL = findViewById(R.id.mRootLL);
            mRootLL.post(new Runnable() {
                @Override
                public void run() {
                    int top = StatusBarUtil.getStatusBarHeight(ct);
                    mRootLL.setTranslationY(-top);
                }
            });
        }
    }

    @Override
    public void setAdapter(List<BillGroupModel> groupModels) {
        if (mBillAdapter == null) {
            mBillAdapter = newBillAdapter(groupModels);
            mRecyclerView.setAdapter(mBillAdapter);
        } else {
            mBillAdapter.setBillGroupModels(groupModels);
            mBillAdapter.notifyDataSetChanged();
        }

        updateItemsValue(name,phone);
    }


    @Override
    public void updateFileOk() {
        dimssLoading();
        mBillPresenter.saveAndSubmit(mBillAdapter.getBillGroupModels());
    }

    @Override
    public void addTopLayout(View ...view) {

    }

    @Override
    public void toSelect(int position, BillGroupModel.BillModel model) {
        if (TextUtils.isEmpty(model.getType())) return;
        if (!ListUtils.isEmpty(model.getLocalDatas())) {
            //本地数据不为空的情况下
            selectByLocal(position, model);
        } else {
            //本地数据为空，获取网络数据
            switch (model.getType()) {
                case "C"://单项选择
                    getComboValue(position, model);
                    break;
                case "D"://日期选择
                case "T"://时间选择
                    showDateDialog(!model.getCaption().contains("生日"), position);
                    break;
                case "SF":
                case "DF":
                    //DBFind 选择
                    findBydbFind(model);
                    break;
                case "MF":
                    break;

            }
        }
    }

    @Override
    public void toEnclosureSelect(int position, BillGroupModel.BillModel model) {
        selectPosition = position;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            toSelectEnclosure();
        } else {
            showSelectPictureDialog(model);
        }
    }


    private void toSelectEnclosure() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//无类型限制
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUESTCODE_ENCLOSURE);
    }

    private void toSelectEnclosure(BillGroupModel.BillModel model) {
        Intent intent = new Intent(ct, ImgFileListActivity.class);
        intent.putExtra("MAX_SIZE", 9);
        intent.putExtra("CURRENT_SIZE", ListUtils.getSize(model.getLocalDatas()));
        startActivityForResult(intent, REQUESTCODE_ENCLOSURE_LOW);
    }

    private void showSelectPictureDialog(final BillGroupModel.BillModel model) {
        String[] items = new String[]{getString(R.string.c_take_picture), getString(R.string.c_photo_album)};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this).setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            try {
                                takePhoto();
                            } catch (Exception e) {
                                String message = e.getMessage();
                                if (!StringUtil.isEmpty(message) && message.contains("Permission")) {
                                    ToastUtil.showToast(ct, R.string.not_system_permission);
                                }
                            }
                        } else {
                            toSelectEnclosure(model);
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private Uri mNewPhotoUri;

    private void takePhoto() throws Exception {
        Uri mNewPhotoUri = CameraUtil.getOutputMediaFileUri(mContext, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
        if (mNewPhotoUri != null) {
            CameraUtil.captureImage((Activity) ct,
                    mNewPhotoUri,
                    REQUEST_CODE_CAPTURE_PHOTO);
        } else {
            ToastUtil.showToast(this, "uri is null");
        }
    }

    public void getComboValue(int position, BillGroupModel.BillModel model) {
        HashMap param = new HashMap<>();
        param.put("caller", mBillPresenter.getFormCaller());
        param.put("field", model.getField());
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "combdatas");
        intent.putExtra("showKey", "DISPLAY");
        intent.putExtra("action", "mobile/common/getComboValue.action");
        intent.putExtra("title", model.getCaption());
        intent.putExtra("id", position);//需要把zum
        startActivityForResult(intent, REQUESTCODE_C_NET);
    }

    /**
     * 选择日期
     */
    public void showDateDialog(final boolean needTime, final int position) {
        DateTimePicker picker = new DateTimePicker(this, needTime ? DateTimePicker.HOUR_OF_DAY : DateTimePicker.YEAR_MONTH_DAY);
        picker.setRange(CalendarUtil.getYear() - 100, CalendarUtil.getYear() + 20);
        if (needTime) {
            picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay(), CalendarUtil.getHour(), CalendarUtil.getMinute());
        } else {
            picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
        }
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                String date = year + "-" + month + "-" + day;
                String dateTime = date + " " + hour + ":" + minute + ":00";
                mBillAdapter.updateBillModelValues(position, needTime ? dateTime : date, dateTime);
            }
        });

        picker.show();
    }

    /**
     * dbfind 查找
     *
     * @param model
     */
    public void findBydbFind(BillGroupModel.BillModel model) {
        String gridCaller = "";
        String fieldKey = model.getField();
        BillGroupModel mGroupModel = mBillAdapter.getBillGroupModel(model.getGroupIndex());
        boolean isForm = mGroupModel == null || mGroupModel.isForm();
        if (!isForm && !StringUtil.isEmpty(model.getFindFunctionName())) {
            String[] mFindFunctionNames = model.getFindFunctionName().split("\\|");
            if (mFindFunctionNames != null && mFindFunctionNames.length > 1) {
                gridCaller = mFindFunctionNames[0];
//                fieldKey = mFindFunctionNames[1];
            }
        }
        String mCondition = "";
        if (dbFindCondition != null && dbFindCondition.containsKey(fieldKey)) {
            mCondition = dbFindCondition.get(fieldKey);
        }
        startActivityForResult(new Intent(ct, SelectNetAcitivty.class)
                        .putExtra("fieldKey", fieldKey)
                        .putExtra("caller", mBillPresenter.getFormCaller())
                        .putExtra("gCaller", gridCaller)
                        .putExtra("mCondition", mCondition)
                        .putExtra("isDevice", false)
                        .putExtra("isForm", isForm)
                        .putExtra("groupId", model.getGroupIndex())
                , REQUESTCODE_DB_FIND);
    }

    /**
     * 选择本地数据选项
     *
     * @param position 索引
     * @param model    点击对象
     */
    public void selectByLocal(int position, BillGroupModel.BillModel model) {
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean;
        for (BillGroupModel.LocalData localData : model.getLocalDatas()) {
            bean = new SelectBean();
            bean.setJson(localData.display);
            bean.setShowName(localData.value);
            bean.setName(localData.value);
            bean.setIndex(position);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", model.getCaption());
        startActivityForResult(intent, REQUESTCODE_C);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        switch (requestCode) {
            case REQUESTCODE_C_NET:
                SelectBean mSelectBeanC = data.getParcelableExtra("data");
                int position = data.getIntExtra("id", -1);
                if (mSelectBeanC != null && position >= 0) {
                    String display = StringUtil.isEmpty(mSelectBeanC.getName()) ? "" : mSelectBeanC.getName();
                    String value = StringUtil.isEmpty(mSelectBeanC.getName()) ? "" : mSelectBeanC.getName();
                    mBillAdapter.updateBillModelValues(position, value, display);
                }
                break;
            case REQUESTCODE_C:
                mSelectBeanC = data.getParcelableExtra("data");
                if (mSelectBeanC != null) {
                    position = mSelectBeanC.getIndex();
                    String value = StringUtil.isEmpty(mSelectBeanC.getShowName()) ? "" : mSelectBeanC.getShowName();
                    String display = StringUtil.isEmpty(mSelectBeanC.getJson()) ? "" : mSelectBeanC.getJson();
                    mBillAdapter.updateBillModelValues(position, value, display);
                }
                break;
            case REQUESTCODE_DB_FIND:
                String json = data.getStringExtra("data");
                int groupIndex = data.getIntExtra("groupId", 0);
                handlerSelectDbFind(JSON.parseObject(json), groupIndex);
                break;
            case 0x22:
                SelectBean d = data.getParcelableExtra("data");
                if (d == null) return;
                String name = StringUtil.isEmpty(d.getName()) ? "" : d.getName();
                String jsonObject = StringUtil.isEmpty(d.getJson()) ? "" : d.getJson();
                LogUtil.i("gong", "json=" + jsonObject);
                mBillPresenter.getEmnameByReturn(name, jsonObject);
                break;
            case REQUESTCODE_ENCLOSURE:
                if (resultCode == Activity.RESULT_OK) {
                    String path;
                    Uri uri = data.getData();
                    if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                        path = uri.getPath();
                    } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                        path = FileUtils.getPath(this, uri);
                    } else {//4.4以下下系统调用方法
                        path = FileUtils.getRealPathFromURI(this, uri);
                    }
                    LogUtil.i("gong", "uri=" + uri.toString());
                    File file = null;
                    if (!TextUtils.isEmpty(path)) {
                        file = new File(path);
                    }
                    if (file != null && file.exists() && file.isFile()) {
                        String value = file.getName();
                        String display = path;
                        mBillAdapter.addBillModelData(selectPosition, value, display);
                        selectPosition = -1;
                    }

                }
            case REQUEST_CODE_CAPTURE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    if (mNewPhotoUri != null) {
                        String value = mNewPhotoUri.getPath();
                        String display = value;
                        mBillAdapter.addBillModelData(selectPosition, value, display);
                        selectPosition = -1;
                    } else {
                        ToastUtil.showToast(this, R.string.c_take_picture_failed);
                    }
                }
                break;
            case REQUESTCODE_ENCLOSURE_LOW:
                List<String> filePaths = data.getStringArrayListExtra("files");
                if (!ListUtils.isEmpty(filePaths)) {
                    File file = null;
                    List<BillGroupModel.LocalData> localDatas = new ArrayList<>();
                    for (String filePath : filePaths) {
                        if (!TextUtils.isEmpty(filePath)) {
                            file = new File(filePath);
                        }
                        if (file != null && file.exists() && file.isFile()) {
                            BillGroupModel.LocalData localData = new BillGroupModel.LocalData();
                            localData.value = file.getName();
                            localData.display = filePath;
                            localDatas.add(localData);
                        }
                    }
                    mBillAdapter.addBillModelData(selectPosition, localDatas);
                    selectPosition = -1;
                }
                break;
        }
    }

    protected void handlerSelectDbFind(JSONObject object, int groupId) {
        try {
            mBillPresenter.setmId(Integer.valueOf(object.getString("cu_id")));
            LogUtil.d("Arison", "groupId:" + groupId + " object:" + object.toJSONString());
            BillGroupModel mBillGroupModel = mBillAdapter.getBillGroupModel(groupId);
            if (mBillGroupModel != null) {
                if (!ListUtils.isEmpty(mBillGroupModel.getShowBillFields())) {
                    for (BillGroupModel.BillModel e : mBillGroupModel.getShowBillFields()) {
                        if (object.containsKey(e.getField())) {
                            e.setValue(JSONUtil.getText(object, e.getField()));
                        }
                    }
                }
                if (!ListUtils.isEmpty(mBillGroupModel.getHideBillFields())) {
                    for (BillGroupModel.BillModel e : mBillGroupModel.getHideBillFields()) {
                        if (object.containsKey(e.getField())) {
                            e.setValue(JSONUtil.getText(object, e.getField()));
                        }
                    }
                }
            }
            mBillAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



//    关联客户
    protected void updateItemsValue(String name,String phone) {
        try {
            List<BillGroupModel>  groupModels=  mBillAdapter.getBillGroupModels();
            if (groupModels.size()>2){
                for (int i=2;i<groupModels.size();i++){
                    groupModels.remove(i);
                }
            }

            BillGroupModel mBillGroupModel=groupModels.get(1);//第一个明细
            LogUtil.d("arison","BillGroupModel:"+JSON.toJSONString(mBillGroupModel));
            if (!ListUtils.isEmpty(mBillGroupModel.getShowBillFields())) {
                for (BillGroupModel.BillModel e : mBillGroupModel.getShowBillFields()) {
                    if ("ct_name".equals(e.getField())) {
                        e.setValue(name);
                    }
                    if ("ct_mobile".equals(e.getField())) {
                        e.setValue(phone);
                    }
                }
    
                for (BillGroupModel.BillModel e : mBillGroupModel.getHideBillFields()) {
                    if ("ct_id".equals(e.getField())) {
                        e.setValue("0");
                    }
                    //ct_cuid
    //                if ("ct_mobile".equals(e.getField())) {
    //                    e.setValue("13266699268");
    //                }
                }
            }

            mBillAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

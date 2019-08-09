package com.modular.apputils.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import com.core.utils.CommonUtil;
import com.core.utils.StatusBarUtil;
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.modular.apputils.R;
import com.modular.apputils.adapter.BillAdapter;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.model.BillListConfig;
import com.modular.apputils.presenter.BillPresenter;
import com.modular.apputils.presenter.imp.IBill;
import com.modular.apputils.widget.WrapContentLinearLayoutManager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 动态表单单据录入界面，后续会尽量关闭修改，添加拓展功能
 */
public class BillInputActivity extends OABaseActivity implements IBill, BillAdapter.OnAdapterListener {
    public final int REQUESTCODE_C = 0x11;
    public final int REQUESTCODE_DB_FIND = 0x12;
    public final int REQUESTCODE_ENCLOSURE = 0x13;
    public final int REQUESTCODE_C_NET = 0x14;
    public final int REQUESTCODE_ENCLOSURE_LOW = 0x15;
    public final int REQUEST_CODE_CAPTURE_PHOTO = 0x16;
    public final int REQUEST_CODE_SELECT_MORE = 0x17;

    public RecyclerView mRecyclerView;
    public BillPresenter mBillPresenter;
    public BillAdapter mBillAdapter;
    public FrameLayout mTopLayout;
    public TabLayout mTabLayout;
    private String mListCondition;
    private RecyclerView.AdapterDataObserver mAdapterDataObserver;
    public int selectPosition = -1;//当前选择调转界面的index，本来不想添加全局变量的，没有办法
    private HashMap<String, String> dbFindCondition;

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
            if (!CommonUtil.isRepeatClick(3000)) {
                mBillPresenter.saveAndSubmit(mBillAdapter.getBillGroupModels());
            }
        } else if (item.getItemId() == R.id.list) {
            if (!CommonUtil.isRepeatClick()) {
                toDataFormList();
            }
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
        mTopLayout = findViewById(R.id.bill_input_top_fl);
        mRecyclerView = findViewById(R.id.mRecyclerView);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(ct,
                LinearLayoutManager.VERTICAL, false));
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
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //控制顶部TabLayout是否显示
                if (mBillAdapter != null && mBillAdapter.mTabIndex != -1) {
                    RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
                    if (layoutManager instanceof WrapContentLinearLayoutManager && mTopLayout != null) {
                        WrapContentLinearLayoutManager linearLayoutManager = (WrapContentLinearLayoutManager) layoutManager;
                        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                        if (firstVisibleItemPosition >= mBillAdapter.mTabIndex) {
                            mTopLayout.setVisibility(View.VISIBLE);
                            mTopLayout.requestLayout();
                        } else {
                            mTopLayout.setVisibility(View.GONE);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void setAdapter(List<BillGroupModel> groupModels) {
        if (mBillAdapter == null) {
            mBillAdapter = newBillAdapter(groupModels);
            mRecyclerView.setAdapter(mBillAdapter);
            mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    if (mBillAdapter.mTabIndex != -1 && mBillAdapter.mTabPosition != -1
                            && mTabLayout != null && mTabLayout.getSelectedTabPosition() != mBillAdapter.mTabPosition) {
                        mTabLayout.getTabAt(mBillAdapter.mTabPosition).select();
                    }
                }
            };
            mBillAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        } else {
            mBillAdapter.setBillGroupModels(groupModels);
            mBillAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void updateFileOk() {
        dimssLoading();
        mBillPresenter.saveAndSubmit(mBillAdapter.getBillGroupModels());
    }

    @Override
    public void addTopLayout(View... view) {
        if (view != null && view.length > 0) {
            mTopLayout.addView(view[0]);

            if (view.length > 1 && view[1] instanceof TabLayout) {
                mTabLayout = (TabLayout) view[1];
                if (mTabLayout != null) {
                    mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {
                            int tabPosition = tab.getPosition();
                            if (mBillAdapter != null && mBillAdapter.mTabIndex != -1
                                    && tabPosition != mBillAdapter.mTabPosition) {
                                mBillAdapter.switchTabIndex(tabPosition);
                                mRecyclerView.scrollToPosition(mBillAdapter.mTabIndex);
//                                WrapContentLinearLayoutManager mLayoutManager =
//                                        (WrapContentLinearLayoutManager) mRecyclerView.getLayoutManager();
//                                mLayoutManager.scrollToPositionWithOffset(mBillAdapter.mTabIndex, 0);
                            }
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });
                }
            }
        } else {
            mTopLayout.setVisibility(View.GONE);
            mTopLayout.removeAllViews();
            mTopLayout.invalidate();
        }
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
                case "DT"://时间选择
                    showDateDialog(true, position);
                    break;
                case "D"://日期选择
                case "T"://时间选择
                    showDateDialog(false, position);
                    break;
                case "SF":
                case "DF":
                    //DBFind 选择
                    findBydbFind(model);
                    break;
                case "MF":
                    //多选
                    findMore(position, model);
                    break;

            }
        }
    }

    private void findMore(int position, BillGroupModel.BillModel model) {
        HashMap param = new HashMap<>();
        String[] fields = new String[]{"sa_custname"};
        BillGroupModel mGroupModel = mBillAdapter.getBillGroupModel(model.getGroupIndex());
        boolean isForm = mGroupModel == null || mGroupModel.isForm();

        param.put("caller", mBillPresenter.getFormCaller());
        param.put("page", "1");
        param.put("which", "form");
        param.put("condition", "1=1");
        param.put("pageSize", "1000");
        param.put("field", model.getField());
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("isSingle", false);
        intent.putExtra("reid", R.style.OAThemeMeet);
        intent.putExtras(bundle);
        intent.putExtra("key", "combdatas");
        intent.putExtra("showKey", model.getField());
        intent.putExtra("fields", fields);
        intent.putExtra("action", "common/dbfind.action");
        intent.putExtra("title", model.getCaption());
        intent.putExtra("id", model.getGroupIndex());
        intent.putExtra("isForm", isForm);
        startActivityForResult(intent, REQUEST_CODE_SELECT_MORE);
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
                mBillAdapter.updateBillModelValues(position, needTime ? dateTime : date, needTime ? dateTime : date);
            }
        });
        picker.setOnCancelListener(new DateTimePicker.OnCancelListener() {
            @Override
            public void onCancel() {
                mBillAdapter.updateBillModelValues(position, "", "");
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
        findBydbFind(model, "");
    }

    public void findBydbFind(BillGroupModel.BillModel model, String condition) {
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
        if (TextUtils.isEmpty(condition)) {
            if (dbFindCondition != null && dbFindCondition.containsKey(fieldKey)) {
                mCondition = dbFindCondition.get(fieldKey);
            }
        } else {
            mCondition = condition;
        }

        startActivityForResult(new Intent(ct, SelectNetAcitivty.class)
                        .putExtra("fieldKey", fieldKey)
                        .putExtra("caller", mGroupModel == null ? mBillPresenter.getFormCaller()
                                : mGroupModel.getBillCaller())
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
            case REQUEST_CODE_SELECT_MORE:
                if (resultCode == 0x21) {//多选框
                    int groupIndex = data.getIntExtra("id", 0);
                    boolean isForm = data.getBooleanExtra("isForm", true);
                    ArrayList<SelectBean> muliData = data.getParcelableArrayListExtra("data");
                    JSONObject object = null;
                    if (!ListUtils.isEmpty(muliData)) {
                        object = JSON.parseObject(muliData.get(0).getJson());
                        String dbFind = muliData.get(0).getDbfinds();
                        for (int i = 1; i < muliData.size(); i++) {
                            SelectBean b = muliData.get(i);
                            JSONObject bObject = JSON.parseObject(b.getJson());
                            for (Map.Entry<String, Object> entry : bObject.entrySet()) {
                                String oldText = object.getString(entry.getKey());
                                object.put(entry.getKey(), oldText + "#" + entry.getValue());
                            }
                        }
                        if (!TextUtils.isEmpty(dbFind)) {
                            JSONArray fbFindArray = JSON.parseArray(dbFind);
                            if (!ListUtils.isEmpty(fbFindArray)) {
                                for (int i = 0; i < fbFindArray.size(); i++) {
                                    String dbGridField = JSONUtil.getText(fbFindArray.getJSONObject(i), "dbGridField");
                                    String field = JSONUtil.getText(fbFindArray.getJSONObject(i), "field");
                                    if (object.containsKey(dbGridField)) {
                                        object.put(field, object.get(dbGridField));
                                        object.remove(dbGridField);
                                    }
                                }
                            }
                        }
                    }
//                    mBillAdapter.updateBillModelValues(id, values, values);
                    LogUtil.i("gong", "object=" + object);
                    LogUtil.i("gong", "groupIndex=" + groupIndex);
                    handlerSelectDbFind(object, groupIndex, isForm);
                }
                break;
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
                boolean isForm = data.getBooleanExtra("isForm", true);
                handlerSelectDbFind(JSON.parseObject(json), groupIndex, isForm);
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

    protected void handlerSelectDbFind(JSONObject object, int groupId, boolean isForm) {
        if (object == null) {
            return;
        }
        if (isForm) {
            List<BillGroupModel.BillModel> formBillModels = mBillAdapter.getFormBillModels();

            if (!ListUtils.isEmpty(formBillModels)) {
                for (BillGroupModel.BillModel billModel : formBillModels) {
                    if (object.containsKey(billModel.getField())) {
                        billModel.setValue(JSONUtil.getText(object, billModel.getField()));
                    }
                }
            }
        } else {
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
        }

        mBillAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBillAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
    }
}

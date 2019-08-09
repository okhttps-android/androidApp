package com.modular.apputils.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

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
import com.core.utils.ToastUtil;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.modular.apputils.R;
import com.modular.apputils.adapter.BillDetailsAdapter;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.presenter.BillDetailsPresenter;
import com.modular.apputils.presenter.imp.IBillDetails;
import com.modular.apputils.widget.BillUpdatePopup;
import com.modular.apputils.widget.VeriftyDialog;
import com.module.recyclerlibrary.ui.refresh.EmptyRecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillDetailsActivity extends OABaseActivity implements IBillDetails, BillUpdatePopup.OnUpdateSelectListener {
    public final int REQUESTCODE_C = 0x21;
    public final int REQUESTCODE_DB_FIND = 0x22;
    public final int REQUESTCODE_ENCLOSURE = 0x23;
    public final int REQUESTCODE_C_NET = 0x24;
    public final int REQUESTCODE_ENCLOSURE_LOW = 0x25;
    public final int REQUEST_CODE_CAPTURE_PHOTO = 0x26;
    public final int REQUEST_CODE_SELECT_MORE = 0x27;

    protected RecyclerView mRecyclerView;
    protected BillDetailsPresenter mBillDetailsPresenter;
    protected String status;
    protected BillDetailsAdapter mBillDetailsAdapter;
    protected boolean isMe;//是否是自己的单据
    protected BillUpdatePopup mBillUpdatePopup;
    public int selectPosition = -1;
    private List<BillGroupModel> mUpdateGroupModels = new ArrayList<>();

    public BillDetailsAdapter newBillDetailsAdapter(List<BillGroupModel> groupModels) {
        return new BillDetailsAdapter(ct, groupModels);
    }

    public BillDetailsPresenter newBillDetailsPresenter() {
        return new BillDetailsPresenter(ct, this);
    }

    public int getLayoutId() {
        return R.layout.activity_bill_details;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        mBillDetailsPresenter = newBillDetailsPresenter();
        Intent intent = getIntent();
        if (intent != null) {
            isMe = intent.getBooleanExtra(Constants.Intents.MY_DOIT, false);
        }
        initView();
        mBillDetailsPresenter.start(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*if (isMe && (TextUtils.isEmpty(status) || !status.equals("已审核"))) {
            getMenuInflater().inflate(R.menu.menu_input_edit, menu);
        }*/

        if (isMe) {
            if ("已审核".equals(status) || "已转入".equals(status)) {
                if (mBillDetailsAdapter != null && !ListUtils.isEmpty(mBillDetailsAdapter.getUpdateBillModels())) {
                    getMenuInflater().inflate(com.modular.apputils.R.menu.menu_input_update, menu);
                }
            } else {
                getMenuInflater().inflate(com.modular.apputils.R.menu.menu_input_edit, menu);
            }
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.edit == item.getItemId()) {
            new VeriftyDialog.Builder(ct)
                    .setTitle(getString(R.string.app_name))
                    .setContent("是否确定反提交该单据?")
                    .build(new VeriftyDialog.OnDialogClickListener() {
                        @Override
                        public void result(boolean clickSure) {
                            if (clickSure) {
                                mBillDetailsPresenter.unSubmit(status);
                            }
                        }
                    });
        } else if (item.getItemId() == R.id.update) {
            if (mBillDetailsAdapter != null && !ListUtils.isEmpty(mBillDetailsAdapter.getUpdateBillModels())) {
                try {
                    mUpdateGroupModels = CommonUtil.deepCopy(mBillDetailsAdapter.getBillGroupModels());
                } catch (Exception e) {
                    mUpdateGroupModels = mBillDetailsAdapter.getBillGroupModels();
                    e.printStackTrace();
                }
                mBillUpdatePopup.setGroupModels(mUpdateGroupModels).showPopupWindow();
            } else {
                toast(getString(R.string.no_fields_to_update));
            }
        }
        return super.onOptionsItemSelected(item);
    }


    protected void initView() {
        EmptyRecyclerView mEmptyRecyclerView = findViewById(R.id.mEmptyRecyclerView);
        mRecyclerView = mEmptyRecyclerView.getRecyclerView();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ct));

        mBillUpdatePopup = new BillUpdatePopup(mContext, this);
    }


    @Override
    public void updateStatus(String status) {
        if (status == null) {
            supportInvalidateOptionsMenu();
        } else {
            this.status = status;
            if (status.equals("已审核")) {
                supportInvalidateOptionsMenu();
            }
        }
    }

    @Override
    public void setAdapter(List<BillGroupModel> groupModels) {
        if (mBillDetailsAdapter == null) {
            mBillDetailsAdapter = newBillDetailsAdapter(groupModels);
            mRecyclerView.setAdapter(mBillDetailsAdapter);
        } else {
            mBillDetailsAdapter.updateGroupModels(groupModels);
            mBillDetailsAdapter.notifyDataSetChanged();
        }
        if (!ListUtils.isEmpty(mLocalDatas)) {
            setFilePaths(mLocalDatas);
        }
    }

    private List<BillGroupModel.LocalData> mLocalDatas;

    @Override
    public void setFilePaths(List<BillGroupModel.LocalData> mLocalDatas) {
        this.mLocalDatas = mLocalDatas;
        if (mBillDetailsAdapter != null) {
            List<BillGroupModel.BillModel> mBillModels = mBillDetailsAdapter.getShowBillModels();
            if (mBillModels != null) {
                for (int i = 0; i < mBillModels.size(); i++) {
                    BillGroupModel.BillModel mBillModel = mBillModels.get(i);
                    if (mBillModel.getType().equals("FF")) {
                        mBillModel.setLocalDatas(mLocalDatas);
                        mBillDetailsAdapter.notifyItemChanged(i);
                    }
                }
            }
        }
    }

    @Override
    public void updateDetail(List<BillGroupModel> mGroupModels) {
        setAdapter(mGroupModels);
    }

    @Override
    public void updateFileOk() {
        dimssLoading();
        mBillDetailsPresenter.saveAndSubmit(mBillDetailsAdapter.getBillGroupModels());
    }

    @Override
    public void onUpdateSelect(int position, BillGroupModel.BillModel model) {
        if (TextUtils.isEmpty(model.getType())) {
            return;
        }
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

    @Override
    public void onUpdateEnclosure(int position, BillGroupModel.BillModel model) {
        selectPosition = position;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            toSelectEnclosure();
        } else {
            showSelectPictureDialog(model);
        }
    }

    @Override
    public void onUpdateConfirm(List<BillGroupModel> billGroupModels, List<BillGroupModel.BillModel> updateBillModels) {
        mUpdateGroupModels = billGroupModels;
        mBillDetailsPresenter.saveAndSubmit(billGroupModels);
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
        String[] items = new String[]{getString(com.modular.apputils.R.string.c_take_picture), getString(com.modular.apputils.R.string.c_photo_album)};
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
                                    ToastUtil.showToast(ct, com.modular.apputils.R.string.not_system_permission);
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

    private void findMore(int position, BillGroupModel.BillModel model) {
        HashMap param = new HashMap<>();
        String[] fields = new String[]{"sa_custname"};
        BillGroupModel mGroupModel = mBillDetailsAdapter.getBillGroupModel(model.getGroupIndex());
        boolean isForm = mGroupModel == null || mGroupModel.isForm();

        param.put("caller", mBillDetailsPresenter.getCaller());
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
        intent.putExtra("reid", com.modular.apputils.R.style.OAThemeMeet);
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

    public void getComboValue(int position, BillGroupModel.BillModel model) {
        HashMap param = new HashMap<>();
        param.put("caller", mBillDetailsPresenter.getCaller());
        param.put("field", model.getField());
        Bundle bundle = new Bundle();
        bundle.putSerializable("param", param);
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 1);
        intent.putExtra("reid", com.modular.apputils.R.style.OAThemeMeet);
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
                mBillUpdatePopup.updateBillModelValues(position, needTime ? dateTime : date, needTime ? dateTime : date);
            }
        });
        picker.setOnCancelListener(new DateTimePicker.OnCancelListener() {
            @Override
            public void onCancel() {
                mBillUpdatePopup.updateBillModelValues(position, "", "");
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
        BillGroupModel mGroupModel = mBillDetailsAdapter.getBillGroupModel(model.getGroupIndex());
        boolean isForm = mGroupModel == null || mGroupModel.isForm();
        if (!isForm && !StringUtil.isEmpty(model.getFindFunctionName())) {
            String[] mFindFunctionNames = model.getFindFunctionName().split("\\|");
            if (mFindFunctionNames != null && mFindFunctionNames.length > 1) {
                gridCaller = mFindFunctionNames[0];
//                fieldKey = mFindFunctionNames[1];
            }
        }
        String mCondition = "";
        /*if (dbFindCondition != null && dbFindCondition.containsKey(fieldKey)) {
            mCondition = dbFindCondition.get(fieldKey);
        }*/
        startActivityForResult(new Intent(ct, SelectNetAcitivty.class)
                        .putExtra("fieldKey", fieldKey)
                        .putExtra("caller", mGroupModel == null ? mBillDetailsPresenter.getCaller()
                                : mGroupModel.getBillCaller())
                        .putExtra("gCaller", gridCaller)
                        .putExtra("mCondition", mCondition)
                        .putExtra("isDevice", false)
                        .putExtra("isForm", isForm)
                        .putExtra("groupId", model.getGroupIndex())
                , REQUESTCODE_DB_FIND);
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
                    mBillUpdatePopup.updateBillModelValues(position, value, display);
                }
                break;
            case REQUESTCODE_C:
                mSelectBeanC = data.getParcelableExtra("data");
                if (mSelectBeanC != null) {
                    position = mSelectBeanC.getIndex();
                    String value = StringUtil.isEmpty(mSelectBeanC.getShowName()) ? "" : mSelectBeanC.getShowName();
                    String display = StringUtil.isEmpty(mSelectBeanC.getJson()) ? "" : mSelectBeanC.getJson();
                    mBillUpdatePopup.updateBillModelValues(position, value, display);
                }
                break;
            case REQUESTCODE_DB_FIND:
                String json = data.getStringExtra("data");
                int groupIndex = data.getIntExtra("groupId", 0);
                boolean isForm = data.getBooleanExtra("isForm", true);
                handlerSelectDbFind(JSON.parseObject(json), groupIndex, isForm);
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
                        mBillUpdatePopup.getBillUpdateAdapter().addBillModelData(selectPosition, value, display);
                        selectPosition = -1;
                    }

                }
            case REQUEST_CODE_CAPTURE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    if (mNewPhotoUri != null) {
                        String value = mNewPhotoUri.getPath();
                        String display = value;
                        mBillUpdatePopup.getBillUpdateAdapter().addBillModelData(selectPosition, value, display);
                        selectPosition = -1;
                    } else {
                        ToastUtil.showToast(this, com.modular.apputils.R.string.c_take_picture_failed);
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
                    mBillUpdatePopup.getBillUpdateAdapter().addBillModelData(selectPosition, localDatas);
                    selectPosition = -1;
                }
                break;
        }
    }

    protected void handlerSelectDbFind(JSONObject object, int groupId, boolean isForm) {
        if (isForm) {
            List<BillGroupModel.BillModel> formBillModels = mBillUpdatePopup.getBillUpdateAdapter().getFormBillModels();

            if (!ListUtils.isEmpty(formBillModels)) {
                for (BillGroupModel.BillModel billModel : formBillModels) {
                    if (object.containsKey(billModel.getField())) {
                        billModel.setValue(JSONUtil.getText(object, billModel.getField()));
                    }
                }
            }
        } else {
            BillGroupModel mBillGroupModel = mBillUpdatePopup.getBillUpdateAdapter().getBillGroupModel(groupId);
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

        mBillUpdatePopup.getBillUpdateAdapter().notifyDataSetChanged();
    }

}
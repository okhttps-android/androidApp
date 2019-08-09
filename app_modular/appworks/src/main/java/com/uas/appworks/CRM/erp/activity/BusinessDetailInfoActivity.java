package com.uas.appworks.CRM.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRouteLine;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.common.LogUtil;
import com.common.config.VersionUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.MissionModel;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.BaiduMapUtil;
import com.core.utils.CommonUtil;
import com.core.utils.OnGetDrivingRouteResult;
import com.core.utils.TimeUtils;
import com.core.utils.ToastUtil;
import com.core.widget.MyListView;
import com.core.widget.crouton.Style;
import com.core.widget.view.steps.StepsView;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.applocation.UasLocationHelper;
import com.uas.applocation.model.UASLocation;
import com.uas.applocation.utils.LocationDistanceUtils;
import com.uas.appworks.CRM.erp.adapter.StickyListAdapter;
import com.uas.appworks.CRM.erp.model.SimpleData;
import com.uas.appworks.OA.erp.activity.MissionActivity;
import com.uas.appworks.OA.erp.model.EmployeesModel;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

import static com.alibaba.fastjson.JSON.parseObject;
import static com.core.utils.HeightUtils.setListViewHeightBasedOnChildren1;


/**
 * @功能:商机详情
 * @author:Arisono
 * @param:
 * @return:
 */
public class BusinessDetailInfoActivity extends BaseActivity implements View.OnClickListener {

    private static final int LOAD_JIEDUAN = 0x21;
    private static final int LOAD_STAGEPOINT = 0x22;
    private static final int LOAD_NICHEHOUSE = 0x278;
    private static final int DELETE_NICHEHOUSE = 0x279;//删除商机
    private final int LOAD_COMTACT = 0x280;//获取联系人接口
    private Button bt_fenpei;
    private Button bt_qiang;
    private Button bt_manage_date;
    private Button bt_zhuanyi;
    private Button bt_manage_go;
    private LinearLayout crm_ll_followup;
    private LinearLayout crm_ll_qiang;
    private LinearLayout crm_ll_manage;
    private TextView ll_card_num;
    private View ll_card_line;
    private TextView bc_from;
    private TextView bc_remark;
    private TextView bc_recorder;
    private TextView bc_recorddate;
    private TextView bc_code;
    private TextView bc_description;
    private TextView bc_nichehouse;
    private TextView bc_type;
    private TextView bc_currentprocess;
    private TextView bc_lastdate;
    private TextView bc_doman;
    private TextView bc_custname;
    private TextView bc_address;
    private TextView bc_contact;
    private TextView bc_position;
    private TextView bt_tel;
    private MyListView myListView;
    private StepsView mStepsView;
    private StickyListHeadersListView mList;

    private StickyListAdapter mAapter;
    private int type = 0;
    private int id = 0;
    private String formCondition;
    private String gridCondition;
    private String en_code;

    private String[] labels;
    private LatLng destination = null;

    private final static int BUSINESS_FAILURE_REQUEST = 99;
    private final static int BUSINESS_FOLLOW_REQUEST = 100;
    private ContactAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_detail_info);
        initIDS();
        initView();
        initListener();
    }

    private void initIDS() {
        bt_fenpei = (Button) findViewById(R.id.bt_fenpei);
        bt_qiang = (Button) findViewById(R.id.bt_qiang);
        bt_manage_date = (Button) findViewById(R.id.bt_manage_date);
        bt_zhuanyi = (Button) findViewById(R.id.bt_zhuanyi);
        bt_manage_go = (Button) findViewById(R.id.bt_manage_go);
        crm_ll_followup = (LinearLayout) findViewById(R.id.crm_ll_followup);
        crm_ll_qiang = (LinearLayout) findViewById(R.id.crm_ll_qiang);
        crm_ll_manage = (LinearLayout) findViewById(R.id.crm_ll_manage);
        ll_card_num = (TextView) findViewById(R.id.ll_card_num);
        ll_card_line = findViewById(R.id.ll_card_line);
        bc_from = (TextView) findViewById(R.id.bc_from);
        bc_remark = (TextView) findViewById(R.id.bc_remark);
        bc_recorder = (TextView) findViewById(R.id.bc_recorder);
        bc_recorddate = (TextView) findViewById(R.id.bc_recorddate);
        bc_code = (TextView) findViewById(R.id.bc_code);
        bc_description = (TextView) findViewById(R.id.bc_description);
        bc_nichehouse = (TextView) findViewById(R.id.bc_nichehouse);
        bc_type = (TextView) findViewById(R.id.bc_type);
        bc_currentprocess = (TextView) findViewById(R.id.bc_currentprocess);
        bc_lastdate = (TextView) findViewById(R.id.bc_lastdate);
        bc_doman = (TextView) findViewById(R.id.bc_doman);
        bc_custname = (TextView) findViewById(R.id.bc_custname);
        bc_address = (TextView) findViewById(R.id.bc_address);
        bc_contact = (TextView) findViewById(R.id.bc_contact);
        bc_position = (TextView) findViewById(R.id.bc_position);
        bt_tel = (TextView) findViewById(R.id.bt_tel);
        myListView = (MyListView) findViewById(R.id.myListView);
        mStepsView = (StepsView) findViewById(R.id.stepsView);
        mList = (StickyListHeadersListView) findViewById(R.id.lv_business);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (type == 0 && VersionUtil.canShowCrm2_0() && !CommonUtil.isBiteman())
            getMenuInflater().inflate(R.menu.menu_delete_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete) {
            PopupWindowHelper.showAlart(this, null, getString(R.string.sure_delete_bc), new PopupWindowHelper.OnSelectListener() {
                @Override
                public void select(boolean selectOk) {
                    if (selectOk)
                        deleteBusinessChance();
                }
            });
        } else if (item.getItemId() == R.id.edit) {
            ArrayList<EmployeesModel> models = new ArrayList<>();
            if (contactAdapter == null || ListUtils.isEmpty(contactAdapter.getModels())) {
                EmployeesModel model = new EmployeesModel();
                model.setEmployeeNames(StringUtil.getTextRexHttp(bc_contact));
                model.setEmployeecode(StringUtil.getTextRexHttp(bt_tel));
                models.add(model);
            } else {
                models = (ArrayList<EmployeesModel>) contactAdapter.getModels();
            }
            startActivity(new Intent(ct, AddBusinessActivity.class)
                    .putExtra("isUpdata", true)
                    .putExtra("id", id)
                    .putExtra("code", bt_code)
                    .putExtra("company", StringUtil.getTextRexHttp(bc_custname))
                    .putExtra("companyAdd", StringUtil.getTextRexHttp(bc_address))
                    .putExtra("remark", StringUtil.getTextRexHttp(bc_description))
                    .putExtra("businessStage", StringUtil.getTextRexHttp(bc_currentprocess))
                    .putExtra("businessLibrary", StringUtil.getTextRexHttp(bc_nichehouse))
                    .putParcelableArrayListExtra("contact", models));
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        setTitle(getString(R.string.business_detail));
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getIntExtra("type", 0);
            formCondition = intent.getStringExtra("formCondition");
            gridCondition = intent.getStringExtra("gridCondition");
            id = intent.getIntExtra("id", 0);
            LogUtil.i("id=" + id);
        }
        switch (type) {
            case 0:
                //跟进
                crm_ll_followup.setVisibility(View.VISIBLE);
                crm_ll_qiang.setVisibility(View.GONE);
                crm_ll_manage.setVisibility(View.GONE);
                break;
            case 1:
                //抢
                crm_ll_followup.setVisibility(View.GONE);
                crm_ll_qiang.setVisibility(View.VISIBLE);
                crm_ll_manage.setVisibility(View.GONE);
                break;
            case 2:
                //分配
                crm_ll_followup.setVisibility(View.GONE);
                crm_ll_qiang.setVisibility(View.GONE);
                crm_ll_manage.setVisibility(View.VISIBLE);
                break;
            default:
                crm_ll_followup.setVisibility(View.GONE);
                crm_ll_qiang.setVisibility(View.GONE);
                crm_ll_manage.setVisibility(View.GONE);
                break;
        }

    }

    private void initListener() {
        bt_manage_date.setOnClickListener(this);
        bt_manage_go.setOnClickListener(this);
        findViewById(R.id.stage_follow).setOnClickListener(this);
        findViewById(R.id.add_mission).setOnClickListener(this);
        bt_qiang.setOnClickListener(this);
        bt_fenpei.setOnClickListener(this);
        bt_zhuanyi.setOnClickListener(this);

    }

    private void initData() {
        sendHttpResquest(Constants.HTTP_SUCCESS_INIT, formCondition, gridCondition);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_manage_go) {
            showPopupWindow(v);
        } else if (v.getId() == R.id.stage_follow) {
            startActivityForResult(new Intent(BusinessDetailInfoActivity.this, BusinessLessActivity.class)
                    .putExtra("type", 2)
                    .putExtra("code", bt_code)
                    .putExtra("process", bt_process)
                    .putExtra("doman", bt_doman), BUSINESS_FOLLOW_REQUEST);
        } else if (v.getId() == R.id.bt_manage_date) {
            isEnableClick(bt_code, 5);
        } else if (v.getId() == R.id.add_mission) {
            if (destination == null) {
                ToastUtil.showToast(ct, getString(R.string.crmmain_business) + getString(R.string.location_error_try_agen));
                return;
            }
            float dis = LocationDistanceUtils.distanceMe(destination);
            setEndTime(destination, dis);
            progressDialog.show();
        } else if (v.getId() == R.id.bt_qiang) {
            progressDialog.show();
            String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/ifoverrecv.action";
            Map<String, Object> stringMap = new HashMap<String, Object>();
            stringMap.put("emcode", CommonUtil.getSharedPreferences(ct, "erp_username"));
            stringMap.put("type", "0");
            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
            ViewUtil.httpSendRequest(ct, url, stringMap, mHandler, headers, CAN_QIANG_NOT, null, null, "post");
        } else if (v.getId() == R.id.bt_fenpei) {
            startActivityForResult(new Intent(ct, DbfindList2Activity.class), 1);
        } else if (v.getId() == R.id.bt_zhuanyi) {
            startActivity(new Intent(ct, BusinessTransferActivity.class).putExtra("code", bt_code).putExtra("name", bc_nichehouse.getText().toString().trim()));
        }
    }


    private PopupWindow popupWindow = null;

    public void showPopupWindow(View parent) {
        View view = null;
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.pop_crm_list, null);
            ListView plist = (ListView) view.findViewById(R.id.mList);
            final SimpleAdapter adapter = new SimpleAdapter(
                    this,
                    getPopData(),
                    R.layout.item_pop_list,
                    new String[]{"item_name"}, new int[]{R.id.tv_item_name});
            plist.setAdapter(adapter);
            plist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    position = position + 1;
                    switch (position) {
                        case 1:
                            popupWindow.dismiss();
                            final String nichehouse = StringUtil.getTextRexHttp(bc_nichehouse);
                            if (/*存在商机库已选择*/!StringUtil.isEmpty(nichehouse)) {
                                PopupWindowHelper.showAlart(BusinessDetailInfoActivity.this,
                                        getString(R.string.common_dialog_title), getString(R.string.sure_business_release),
                                        new PopupWindowHelper.OnSelectListener() {
                                            @Override
                                            public void select(boolean selectOk) {
                                                if (selectOk) {
                                                    sendHttpResquestRelease(nichehouse, BUSINESS_RELEASE);
                                                }
                                            }
                                        });
                            } else
                                loadNichehouse();
                            break;
                        case 2:
                            startActivityForResult(new Intent(BusinessDetailInfoActivity.this, BusinessLessActivity.class)
                                    .putExtra("type", 1)
                                    .putExtra("code", bt_code)
                                    .putExtra("process", bt_process)
                                    .putExtra("doman", bt_doman), BUSINESS_FAILURE_REQUEST);
                            break;
                        case 3:
                            progressDialog.show();
                            String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/isTurnToCustomer.action";
                            Map<String, Object> params = new HashMap<>();
                            params.put("bc_code", bc_code.getText().toString());
                            LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
                            headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
                            ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x10, null, null, "post");
                            break;
                        case 4:
                            startActivity(new Intent(BusinessDetailInfoActivity.this,
                                    BusinessSelectCustomerActivity.class)
                                    .putExtra("code", bt_code)
                            );
                            break;
                    }
                }
            });

            popupWindow = new PopupWindow(view, parent.getWidth(), setListViewHeightBasedOnChildren1(plist) + DisplayUtil.dip2px(this, 10));
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);
        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        DisplayUtil.backgroundAlpha(this, 0.5f);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(BusinessDetailInfoActivity.this, 1f);
            }
        });
        int width = popupWindow.getWidth();
        popupWindow.setWidth(width + DisplayUtil.dip2px(ct, 10));
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        popupWindow.showAtLocation(parent.findViewById(R.id.bt_manage_go), Gravity.NO_GRAVITY, location[0],
                location[1] - popupWindow.getHeight() - 5);

    }

    /*处理商机库并显示选择*/
    private void handlerAndShowNichehouse(JSONArray array) {
        if (ListUtils.isEmpty(array)) {
            sendHttpResquestRelease(null, BUSINESS_RELEASE);
            return;
        }
        final PopupWindow window = new PopupWindow(ct);
        final View view = LayoutInflater.from(ct).inflate(R.layout.pop_radio_list, null);
        window.setContentView(view);
        PopupWindowHelper.setPopupWindowHW(this, window);
        window.setFocusable(true);
        final ListView list = (ListView) view.findViewById(R.id.pop_list);
        final PopListAdapter adapter = new PopListAdapter(getNichehouseData(array));
        list.setAdapter(adapter);
        view.findViewById(R.id.goto_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
            }
        });
        view.findViewById(R.id.sure_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window.dismiss();
                for (SelectBean e : adapter.getBeanList()) {
                    if (e.isClick()) {
                        sendHttpResquestRelease(e.getName(), BUSINESS_RELEASE);
                        return;
                    }
                }
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PopListAdapter adapter = (PopListAdapter) parent.getAdapter();
                for (int i = 0; i < adapter.getBeanList().size(); i++) {
                    adapter.getBeanList().get(i).setClick(i == position);
                }
                adapter.notifyDataSetChanged();
            }
        });
        window.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_round_bg));
        window.showAtLocation(view, Gravity.CENTER, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
        window.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                DisplayUtil.backgroundAlpha(BusinessDetailInfoActivity.this, 1f);
            }
        });
    }

    private List<SelectBean> getNichehouseData(JSONArray array) {
        List<SelectBean> list = new ArrayList<>();
        SelectBean bean = null;
        JSONObject object = null;
        for (int i = 0; i < array.size(); i++) {
            object = array.getJSONObject(i);
            bean = new SelectBean();
            bean.setName(object.getString("BD_NAME"));
            bean.setObject(object.getString("BD_PROP"));
            list.add(bean);
        }
        return list;
    }


    private List<Map<String, Object>> getPopData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String[] lists = getResources().getStringArray(R.array.crm_dialog_followup);
        for (String str : lists) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("item_name", str);
            list.add(map);
        }
        return list;
    }

    private final int BUSINESS_QIANG = 2;
    private final int BUSINESS_FENPEI = 3;
    private final int BUSINESS_RELEASE = 4;
    private final int CAN_QIANG_NOT = 30;

    private String bt_doman;//跟进人
    private String bt_code;//编号
    private String bt_process;//商机阶段
    private JSONObject root = null;//数据源
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
            String message = msg.getData().getString("result");
            if (!JSONUtil.validate(message)) {
                ToastUtil.showToast(ct, message);
                return;
            }
            JSONObject object = parseObject(message);
            switch (msg.what) {
                case LOAD_NICHEHOUSE:
                    handlerAndShowNichehouse(object.getJSONArray("combos"));
                    break;
                case Constants.HTTP_SUCCESS_INIT:
                    progressDialog.dismiss();
                    root = object.getJSONObject("panelData");
                    if (root != null) {
                        bc_from.setText(JSONUtil.getText(root, "bc_from"));
                        bc_nichehouse.setText(JSONUtil.getText(root, "bc_nichehouse"));
                        bt_tel.setText(JSONUtil.getText(root, "bc_tel"));
                        bc_recorddate.setText(JSONUtil.getText(root, "bc_recorddate"));
                        bc_lastdate.setText(JSONUtil.getText(root, "bc_lastdate"));
                        bc_recorder.setText(JSONUtil.getText(root, "bc_recorder"));
                        bc_remark.setText(JSONUtil.getText(root, "bc_remark"));
                        bc_doman.setText(JSONUtil.getText(root, "bc_doman"));
                        bc_code.setText(JSONUtil.getText(root, "bc_code"));
                        bc_address.setText(JSONUtil.getText(root, "bc_address"));
                        bc_custname.setText(JSONUtil.getText(root, "bc_custname"));
                        bc_position.setText(JSONUtil.getText(root, "bc_position"));
                        bc_contact.setText(JSONUtil.getText(root, "bc_contact"));
                        bc_description.setText(JSONUtil.getText(root, "bc_description"));
                        bc_currentprocess.setText(JSONUtil.getText(root, "bc_currentprocess"));
                        bc_type.setText(JSONUtil.getText(root, "bc_type"));
                        bt_doman = JSONUtil.getText(root, "bc_doman");
                        bt_code = JSONUtil.getText(root, "bc_code");
                        bt_process = JSONUtil.getText(root, "bc_currentprocess");
                        double longitude = JSONUtil.getDouble(root, "bc_longitude");
                        double latitude = JSONUtil.getDouble(root, "bc_latitude");
                        if (latitude > 0 && longitude > 0)
                            destination = new LatLng(latitude, longitude);
                        addContent(JSONUtil.getJSONArray(object, "panelItems"), root);
                    }

                    loadContact();
                    loadJieDuan();
                    loadStagePoints();
                    break;
                case BUSINESS_QIANG:
                    showToast("成功抢得此商机，\n" +
                            "请到商机跟进中查看商机!\n");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 5000);
//                    startActivity(new Intent(ct, BusinessDetailActivty.class).putExtra("bt_type",1));
                    break;
                case BUSINESS_FENPEI:
                    showToast("分配商机成功！");
                    crm_ll_followup.setVisibility(View.GONE);
                    crm_ll_qiang.setVisibility(View.GONE);
                    crm_ll_manage.setVisibility(View.GONE);
                    //                   finish();
//                    startActivity(new Intent(ct, BusinessDetailActivty.class).putExtra("bt_type",2));
                    break;
                case BUSINESS_RELEASE:
                    showToast("商机释放成功！");
                    progressDialog.dismiss();
                    popupWindow.dismiss();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 2000);
                    // jumpToStateActivity();
                    break;
                case CAN_QIANG_NOT:

                    String isok = object.getString("isok");
                    if ("0".equals(isok)) {
                        Log.i("qiang", isok);
                        sendHttpBusinessQiang(BUSINESS_QIANG, bt_code, bt_doman);
                    } else if ("1".equals(isok)) {
                        Log.i("qiang", isok);
                        progressDialog.dismiss();
                        showToast("您已超出最大获取商机数！");
                    }
                case 5:
                    progressDialog.dismiss();
                    JSONArray items = object.getJSONArray("result");
                    if (items != null) {
                        if (items.size() > 0) {
                            showToast("不能重复添加到日程！");
                        } else {
                            Intent intent = new Intent("com.modular.appworks.TaskAddActivity");
                            intent.putExtra("type", 1);
                            intent.putExtra("from", "BusinessDetailInfo");
                            intent.putExtra("data", bt_code);
                            intent.putExtra("bc_doman", bt_doman);
                            intent.putExtra("bc_custname", StringUtil.getTextRexHttp(bc_custname));
                            startActivityForResult(intent, 0x11);
                        }
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    String exceptionInfo = object.getString("exceptionInfo");
                    ToastUtil.showToast(ct, exceptionInfo);
                    break;
                case LOAD_JIEDUAN:
                    progressDialog.dismiss();
                    Log.i(TAG, "handleMessage:" + msg.getData().getString("result"));
                    try {
                        JSONArray array = object.getJSONArray("stages");
                        labels = new String[array.size()];
                        if (!array.isEmpty()) {
                            for (int i = 0; i < array.size(); i++) {
                                //  lists.add(array.getJSONObject(i).getString("BS_NAME"));
                                labels[i] = array.getJSONObject(i).getString("BS_NAME");
                            }
                        }

                        if (labels != null) {
                            int position = 0;
                            for (int i = 0; i < labels.length; i++) {
                                if (labels[i].equals(bc_currentprocess.getText().toString())) {
                                    position = i;
                                }
                            }
                            LogUtil.d(JSON.toJSONString(labels));
                            LogUtil.d(JSON.toJSONString(position));
                            LogUtil.d(bc_currentprocess.getText().toString());
                            mStepsView.setCompletedPosition(position % labels.length)
                                    .setLabels(labels)
                                    .setBarColorIndicator(
                                            ct.getResources().getColor(R.color.light_gray))
                                    .setProgressColorIndicator(ct.getResources().getColor(R.color.orange))
                                    .setLabelColorIndicator(ct.getResources().getColor(R.color.light_gray))
                                    .drawView();
                        }
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                        ToastMessage("扔出异常！");
                    }
                    break;
                case LOAD_STAGEPOINT:
                    progressDialog.dismiss();
                    try {
                        JSONArray data = object.getJSONArray("data");
                        int size = data.size();
                        List<SimpleData> simpleDatas = new ArrayList<>();
                        JSONArray value = data.getJSONObject(0).getJSONArray("value");
                        for (int j = 0; j < value.size(); j++) {
                            for (int i = 0; i < data.size(); i++) {
                                SimpleData simpleData = new SimpleData();
                                simpleData.setLeft(data.getJSONObject(i).getString("caption"));
                                simpleData.setRight(data.getJSONObject(i).getJSONArray("value").get(j).toString());
                                simpleData.setGroupId(j);
                                simpleDatas.add(simpleData);
                            }
                        }
                        if (ListUtils.isEmpty(simpleDatas)) {
                            findViewById(R.id.stage_need_tv).setVisibility(View.GONE);
                        }
                        mAapter = new StickyListAdapter(mContext, simpleDatas);
                        mList.setAdapter(mAapter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case DELETE_NICHEHOUSE://删除商机
                    ToastUtil.showToast(ct, getString(R.string.delete_business_ok));
                    finish();
                    break;
                case 0x10:
                    progressDialog.dismiss();
                    boolean success = object.getBoolean("success");
                    if (success) {
                        startActivity(new Intent(BusinessDetailInfoActivity.this, CustomerAddActivity.class)
                                .putExtra("companyname", bc_custname.getText().toString().trim())
                                .putExtra("companyaddress", bc_address.getText().toString().trim())
                                .putExtra("contact", bc_contact.getText().toString().trim())
                                .putExtra("position", bc_position.getText().toString().trim())
                                .putExtra("bc_code", bt_code)
                                .putExtra("id", id)
                                .putExtra("json", root)
                                .putExtra("telephone", bt_tel.getText().toString().trim()));
                    } else {
//                      ToastMessage("该商机已转过客户！"); 
                        ViewUtil.ToastMessage(mContext, "该商机已转过客户！", Style.holoRedLight, 2000);
                    }
                    break;
                case LOAD_COMTACT:
                    JSONArray datalist = JSONUtil.getJSONArray(object, "datalist");
                    if (ListUtils.isEmpty(datalist)) return;
                    List<EmployeesModel> models = new ArrayList<>();
                    for (int i = 0; i < datalist.size(); i++) {
                        JSONObject contact = datalist.getJSONObject(i);
                        String name = JSONUtil.getText(contact, "ct_name");
                        String phone = JSONUtil.getText(contact, "ct_mobile");
                        models.add(new EmployeesModel().setEmployeecode(phone).setEmployeeNames(name));
                    }
                    if (!ListUtils.isEmpty(models)) {
                        contactAdapter = new ContactAdapter();
                        contactAdapter.setModels(models);
                        myListView.setAdapter(contactAdapter);
                    }
                    break;
                default:
                    exceptionInfo = object.getString("exceptionInfo");
                    ToastUtil.showToast(ct, exceptionInfo);
                    break;
            }
        }
    };

    private void addContent(JSONArray panelItems, JSONObject object) {
        if (!ListUtils.isEmpty(panelItems) && object != null) {
            String contact = null, phone = null;
            for (int i = 0; i < panelItems.size(); i++) {
                JSONObject item = panelItems.getJSONObject(i);
                if (JSONUtil.getText(item, "caption").equals("联系人")) {
                    contact = JSONUtil.getText(object, JSONUtil.getText(item, "field"));
                }
                if (JSONUtil.getText(item, "caption").equals("电话")) {
                    phone = JSONUtil.getText(object, JSONUtil.getText(item, "field"));
                }
            }
            if (TextUtils.isEmpty(contact)&&!TextUtils.isEmpty(phone)){
                //添加联系人
                List<EmployeesModel> models = new ArrayList<>();
                models.add(new EmployeesModel().setEmployeecode(phone).setEmployeeNames(contact));
                if (!ListUtils.isEmpty(models)) {
                    contactAdapter = new ContactAdapter();
                    contactAdapter.setModels(models);
                    myListView.setAdapter(contactAdapter);
                }
            }
        }
    }

    private void jumpToStateActivity() {
        Intent intent = new Intent();
        intent.setClass(BusinessDetailInfoActivity.this, BusinessStateActivity.class);
        startActivity(intent);
    }

    private void loadNichehouse() {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getNichehouse.action";
        Map<String, Object> params = new HashMap<>();
        params.put("condition", "1=1");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, LOAD_NICHEHOUSE, null, null, "post");
    }

    private void sendHttpResquest(int what, String formCondition, String gridCondition) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/common/getPanel.action";
        Map<String, Object> params = new HashMap<>();
        params.put("caller", "BusinessChance");
        if (type == 0) formCondition = "bc_id" + formCondition;
        params.put("formCondition", formCondition);
        params.put("gridCondition", gridCondition);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private void sendHttpBusinessQiang(int what, String bc_code, String bc_doman) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/updateBusinessChanceDoman.action";
        Map<String, Object> params = new HashMap<>();
        params.put("bc_code", bc_code);
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        params.put("type", "0");
        params.put("bc_doman", emname);
        params.put("bc_domancode", CommonUtil.getSharedPreferences(ct, "erp_username"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    private void loadContact() {
        if (StringUtil.isEmpty(bt_code)) return;
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getContactPerson.action";
        Map<String, Object> param = new HashMap<>();
        param.put("page", 1);
        param.put("condition", "ct_sourcecode='" + bt_code + "'");
        param.put("size", 1000);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, LOAD_COMTACT, null, null, "post");
    }

    /**
     * @desc:商机释放
     * @author：Arison on 2016/7/25
     */
    public void sendHttpResquestRelease(String nichehouse, int what) {
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/updateBusinessChanceType.action";
        progressDialog.show();
        Map<String, Object> params = new HashMap<>();
        params.put("bc_code", bt_code);
        if (!StringUtil.isEmpty(nichehouse)) params.put("bc_nichehouse", nichehouse);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }


    /*删除商机*/
    public void deleteBusinessChance() {
        String url = CommonUtil.getAppBaseUrl(ct) + "crm/chance/deleteBusinessChance.action";
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("caller", "BusinessChance");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, DELETE_NICHEHOUSE, null, null, "post");
    }


    public void isEnableClick(String bt_code, int what) {
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getSchedule.action";
        progressDialog.show();
        Map<String, Object> params = new HashMap<>();
        params.put("bccode", bt_code);
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        params.put("emname", emname);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (data == null) {
                    return;
                }
                bt_doman = data.getStringExtra("en_name");
                en_code = data.getStringExtra("en_code");
                sendHttpBusinessFenpei(BUSINESS_FENPEI, bt_code, bt_doman, en_code);
                break;
            case BUSINESS_FAILURE_REQUEST:
                if (data == null) {
                    return;
                }
                this.finish();
                break;
            case BUSINESS_FOLLOW_REQUEST:
                if (data == null) {
                    return;
                }
                this.finish();
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendHttpBusinessFenpei(int what, String bt_code, String bt_doman, String en_code) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/updateBusinessChanceDoman.action";
        Map<String, Object> params = new HashMap<>();
        params.put("bc_code", bt_code);
        String emname = CommonUtil.getSharedPreferences(ct, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }
        params.put("type", "1");
        params.put("bc_doman", bt_doman);
        params.put("bc_domancode", en_code);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, what, null, null, "post");
    }


    /**
     * @desc:加载阶段
     * @author：Arison on 2016/7/20
     */
    public void loadJieDuan() {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "/mobile/crm/getBusinessChanceStage.action";
        Map<String, Object> params = new HashMap<>();
        params.put("condition", "1=1");
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, LOAD_JIEDUAN, null, null, "post");
    }


    public void loadStagePoints() {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "mobile/crm/getStagePoints.action";
        Map<String, Object> params = new HashMap<>();
        params.put("bccode", bc_code.getText().toString());
        params.put("currentStep", bc_currentprocess.getText().toString());
        params.put("master", CommonUtil.getSharedPreferences(mContext, "erp_master"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, LOAD_STAGEPOINT, null, null, "post");
    }

    /*商机库适配器*/
    private class PopListAdapter extends BaseAdapter {

        private List<SelectBean> beanList;

        public PopListAdapter(List<SelectBean> beanList) {
            this.beanList = beanList;
        }

        public List<SelectBean> getBeanList() {
            return beanList;
        }

        public void setBeanList(List<SelectBean> beanList) {
            this.beanList = beanList;
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(beanList);
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(ct).inflate(R.layout.pop_item_list, null);
                holder.item_cb = (CheckBox) convertView.findViewById(R.id.item_cb);
                holder.name_tv = (TextView) convertView.findViewById(R.id.name_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String name = beanList.get(position).getName();
            holder.name_tv.setText(StringUtil.isEmpty(name) ? "" : name);
            holder.item_cb.setChecked(beanList.get(position).isClick());
            holder.item_cb.setFocusable(false);
            holder.item_cb.setClickable(false);
            return convertView;
        }

        class ViewHolder {
            CheckBox item_cb;
            TextView name_tv;

        }
    }

    /**
     * 获取距离
     *
     * @param location
     * @param dis
     * @update by 2017/1/11
     */
    private void setEndTime(LatLng location, final double dis) {
        BaiduMapUtil.getInstence().getDrivingRoute(UasLocationHelper.getInstance().getUASLocation().getLocation(), location,
                new OnGetDrivingRouteResult() {
                    @Override
                    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
                        List<DrivingRouteLine> list = drivingRouteResult.getRouteLines();
                        if (ListUtils.isEmpty(list)) {
                            if (dis != 0) {
                                setVoisitTime((long) (dis / 3));
                            } else
                                setVoisitTime(800);
                            return;
                        }
                        int minTime = 0;
                        for (DrivingRouteLine e : list) {
                            if (minTime == 0 || minTime > e.getDuration()) {
                                minTime = e.getDuration();
                            }
                        }
                        setVoisitTime(minTime);
                    }
                });

    }

    /**
     * @param time 秒
     * @update by 2017/1/11
     */
    private void setVoisitTime(long time) {
        progressDialog.dismiss();
        String realTime = TimeUtils.f_long_2_str(System.currentTimeMillis() + (time * 1000));
        UASLocation locationHelper = UasLocationHelper.getInstance().getUASLocation();
        MissionModel mission = new MissionModel();
        mission.setStatus(0);
        mission.setCompanyName(bc_custname.getText().toString());
        mission.setCompanyAddr(bc_address.getText().toString());
        mission.setDistance(LocationDistanceUtils.distanceMe(destination));
        mission.setLatLng(destination);
        mission.setVisitTime(realTime);//预计到达时间
        mission.setLocation(locationHelper.getName());
        mission.setType(1);
        mission.setRecorddate(TimeUtils.f_long_2_str(System.currentTimeMillis()));
        startActivity(new Intent(ct, MissionActivity.class).putExtra("model", mission));
    }

    /*联系人适配器*/
    private class ContactAdapter extends BaseAdapter {
        List<EmployeesModel> models;

        public void setModels(List<EmployeesModel> models) {
            this.models = models;
            int size = ListUtils.getSize(models);
            if (size <= 1) {
                ll_card_num.setVisibility(View.GONE);
                ll_card_line.setVisibility(View.GONE);
            } else {
                ll_card_num.setVisibility(View.VISIBLE);
                ll_card_line.setVisibility(View.VISIBLE);
                ll_card_num.setText(size + getString(R.string.crm_ge));
            }
        }

        public List<EmployeesModel> getModels() {
            return models;
        }

        @Override
        public int getCount() {
            return ListUtils.getSize(models);
        }

        @Override
        public Object getItem(int position) {
            return models.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(ct).inflate(R.layout.item_name_phone, null);
                holder.contact_title = (TextView) convertView.findViewById(R.id.contact_title);
                holder.contact_tv = (TextView) convertView.findViewById(R.id.contact_tv);
                holder.bt_tel = (TextView) convertView.findViewById(R.id.bt_tel);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            EmployeesModel model = models.get(position);
            holder.contact_title.setText(getString(R.string.common_Contact_person) + " " + (position + 1));
            holder.contact_tv.setText(StringUtil.getMessage(model.getEmployeeNames()));
            holder.bt_tel.setText(StringUtil.getMessage(model.getEmployeecode()));
            return convertView;
        }

        class ViewHolder {
            TextView contact_title, contact_tv, bt_tel;
        }

    }
}


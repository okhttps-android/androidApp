package com.uas.appworks.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.data.RegexUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.ui.CameraUtil;
import com.common.ui.ImageUtil;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.AppConstant;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.model.SelectBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.utils.pictureselector.ComPictureAdapter;
import com.core.utils.time.wheel.DateTimePicker;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.MultiImagePreviewActivity;
import com.core.widget.view.Activity.SelectActivity;
import com.core.widget.view.ListViewInScroller;
import com.core.widget.view.MyGridView;
import com.core.widget.view.model.SelectAimModel;
import com.core.widget.view.selectcalendar.SelectCalendarActivity;
import com.core.widget.view.selectcalendar.bean.Data;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;
import com.modular.apputils.utils.PopupWindowHelper;
import com.uas.appworks.OA.erp.activity.CommonDocDetailsActivity;
import com.uas.appworks.OA.erp.activity.form.DataFormFieldActivity;
import com.uas.appworks.OA.erp.activity.form.FormListSelectActivity;
import com.uas.appworks.OA.erp.model.form.GroupData;
import com.uas.appworks.R;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @desc:动态表单界面
 * @author：Arison on 2016/11/8
 */
public class CommonDataFormActivity extends BaseActivity implements View.OnClickListener {
    boolean isLeave = true;
    private ListViewInScroller lv_datas;
    private TextView tv_item_add;
    private Button click_btn;
    private Button btn_update;
    private LinearLayout ll_item_add;
    private LinearLayout ll_top;

    private String caller = "";

    private DataAdapter mAdapter;

    private final int load_success = 2;
    private ArrayList<Data> datas = new ArrayList<Data>();
    private ArrayList<GroupData> mDatas = new ArrayList<>();//适配器装载数据
    private ArrayList<GroupData> mDatasNet = new ArrayList<>();//网络数据

    private JSONArray grids;
    private String formidkey = "";
    private String detailkey_id = "";
    private String detailkey_did = "";
    private GroupData groupData;//明细项


    //判断日期需要用到的字段key
    private String startDate = "";
    private String endDate = "";
    private String d_startDate = "";
    private String d_endDate = "";

    //日期需要的value 重新提交用到，记住时间状态
    private String startDate_v = "";
    private String endDate_v = "";
    private String d_startDate_v = "";
    private String d_endDate_v = "";

    private int formGroupSize;

    private int va_id;
    private String mServeId;
    private String mOperateId;

    private String sessionId;
    private StringBuilder mPhotoIds;
    private int mUploadImgCount = 0;

    private String mOperateUrl;

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            String result = msg.getData().getString("result");
            // result="{\"data\":{\"formdetail\":[{\"fd_id\":157540,\"fd_detno\":2,\"mfd_isdefault\":-1,\"fd_caption\":\"单据编号\",\"fd_readonly\":\"T\",\"fd_type\":\"SS\",\"fd_group\":null,\"fd_field\":\"wo_code\",\"fd_value\":\"\"},{\"fd_id\":161499,\"fd_detno\":3,\"mfd_isdefault\":0,\"fd_caption\":\"所属公司\",\"fd_readonly\":\"F\",\"fd_type\":\"SF\",\"fd_group\":null,\"fd_field\":\"wo_cop\",\"fd_value\":\"\"},{\"fd_id\":151264,\"fd_detno\":4,\"mfd_isdefault\":-1,\"fd_caption\":\"人员类型\",\"fd_readonly\":\"F\",\"fd_type\":\"C\",\"fd_group\":null,\"fd_field\":\"wo_mankind\",\"fd_value\":\"\"},{\"fd_id\":154512,\"fd_detno\":5,\"mfd_isdefault\":-1,\"fd_caption\":\"状态\",\"fd_readonly\":\"T\",\"fd_type\":\"SS\",\"fd_group\":null,\"fd_field\":\"wo_status\",\"fd_value\":\"\"},{\"fd_id\":185101,\"fd_detno\":6,\"mfd_isdefault\":-1,\"fd_caption\":\"录入人\",\"fd_readonly\":\"T\",\"fd_type\":\"SS\",\"fd_group\":null,\"fd_field\":\"wo_emcode\",\"fd_value\":\"\"},{\"fd_id\":154513,\"fd_detno\":7,\"mfd_isdefault\":-1,\"fd_caption\":null,\"fd_readonly\":\"T\",\"fd_type\":\"SS\",\"fd_group\":null,\"fd_field\":\"wo_recorder\",\"fd_value\":\"\"},{\"fd_id\":185103,\"fd_detno\":8,\"mfd_isdefault\":0,\"fd_caption\":\"岗位名称\",\"fd_readonly\":\"T\",\"fd_type\":\"SS\",\"fd_group\":null,\"fd_field\":\"wo_job\",\"fd_value\":\"\"},{\"fd_id\":185102,\"fd_detno\":9,\"mfd_isdefault\":0,\"fd_caption\":\"组织名称\",\"fd_readonly\":\"T\",\"fd_type\":\"SS\",\"fd_group\":null,\"fd_field\":\"wo_hrorg\",\"fd_value\":\"\"},{\"fd_id\":185104,\"fd_detno\":10,\"mfd_isdefault\":-1,\"fd_caption\":\"部门名称\",\"fd_readonly\":\"F\",\"fd_type\":\"SS\",\"fd_group\":null,\"fd_field\":\"wo_depart\",\"fd_value\":\"\"},{\"fd_id\":154514,\"fd_detno\":11,\"mfd_isdefault\":-1,\"fd_caption\":\"录入时间\",\"fd_readonly\":\"T\",\"fd_type\":\"D\",\"fd_group\":null,\"fd_field\":\"wo_recorddate\",\"fd_value\":\"\"},{\"fd_id\":154515,\"fd_detno\":12,\"mfd_isdefault\":-1,\"fd_caption\":\"备注\",\"fd_readonly\":\"F\",\"fd_type\":\"SS\",\"fd_group\":null,\"fd_field\":\"wo_remark\",\"fd_value\":\"\"},{\"fd_id\":148960,\"fd_detno\":13,\"mfd_isdefault\":-1,\"fd_caption\":\"工作任务\",\"fd_readonly\":\"F\",\"fd_type\":\"TA\",\"fd_group\":null,\"fd_field\":\"wo_worktask\",\"fd_value\":\"\"}],\"gridetail\":[{\"dg_caption\":\"关联id\",\"dg_type\":\"N\",\"dg_value\":\"\",\"mdg_isdefault\":0,\"gd_id\":141384,\"dg_group\":0,\"dg_sequence\":1,\"dg_field\":\"wod_woid\",\"dg_logictype\":null},{\"dg_caption\":\"id\",\"dg_type\":\"N\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":141383,\"dg_group\":1,\"dg_sequence\":2,\"dg_field\":\"wod_id\",\"dg_logictype\":null},{\"dg_caption\":\"序号\",\"dg_type\":\"N\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":142913,\"dg_group\":1,\"dg_sequence\":3,\"dg_field\":\"wod_detno\",\"dg_logictype\":\"detno\"},{\"dg_caption\":\"员工工号\",\"dg_type\":\"S\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":142914,\"dg_group\":0,\"dg_sequence\":4,\"dg_field\":\"wod_empcode\",\"dg_logictype\":\"necessaryField\"},{\"dg_caption\":\"员工姓名\",\"dg_type\":\"S\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":142915,\"dg_group\":0,\"dg_sequence\":5,\"dg_field\":\"wod_empname\",\"dg_logictype\":null},{\"dg_caption\":\"部门\",\"dg_type\":\"S\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":142119,\"dg_group\":0,\"dg_sequence\":6,\"dg_field\":\"wod_jiax4\",\"dg_logictype\":null},{\"dg_caption\":\"加班类型\",\"dg_type\":\"C\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":143902,\"dg_group\":0,\"dg_sequence\":7,\"dg_field\":\"wod_type\",\"dg_logictype\":null},{\"dg_caption\":\"起始时间\",\"dg_type\":\"D\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":142110,\"dg_group\":0,\"dg_sequence\":9,\"dg_field\":\"wod_startdate\",\"dg_logictype\":\"necessaryField\"},{\"dg_caption\":\"截止时间\",\"dg_type\":\"D\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":142111,\"dg_group\":0,\"dg_sequence\":10,\"dg_field\":\"wod_enddate\",\"dg_logictype\":\"necessaryField\"},{\"dg_caption\":\"当天加班时数\",\"dg_type\":\"N\",\"dg_value\":\"\",\"mdg_isdefault\":-1,\"gd_id\":142120,\"dg_group\":1,\"dg_sequence\":11,\"dg_field\":\"wod_count\",\"dg_logictype\":null}]},\"success\":true,\"sessionId\":\"9712A59DDFDA3B6BC2DA733099603507\"}";
            progressDialog.dismiss();
            switch (msg.what) {
                case load_success:
                    if (!ListUtils.isEmpty(mDatas)) mDatas.clear();
                    if (!ListUtils.isEmpty(mDatasNet)) mDatasNet.clear();
                    if (StringUtil.isEmpty(result)) return;
                    LogUtil.prinlnLongMsg("result:", result);
                    JSONObject resultObject = JSON.parseObject(result);
                    mOperateId = JSONUtil.getText(resultObject, "operateid");
                    mOperateUrl = JSONUtil.getText(resultObject, "operateurl");
                    JSONArray forms = resultObject.getJSONObject("data").getJSONArray("formdetail");
                    sessionId = resultObject.getString("sessionId");
                    grids = resultObject.getJSONObject("data").getJSONArray("gridetail");
                    //主表分组
                    HashSet<String> set = new HashSet<String>();
                    HashSet<Integer> detail = new HashSet<Integer>();
                    List<Integer> detailist = new ArrayList<Integer>();
                    if (!ListUtils.isEmpty(forms)) {
                        for (int i = 0; i < forms.size(); i++) {
                            JSONObject items = forms.getJSONObject(i);
                            //控制分组数据下无数据，不显示  标识号等于
//                            if(items.getIntValue("mfd_isdefault")==-1){
                            set.add(items.getString("fd_group"));
//                            }

                        }
                        //过滤分组
                        Iterator<String> iter = set.iterator();
                        formGroupSize = set.size();
                        int formGroupId = -1;
                        while (iter.hasNext()) {
                            formGroupId++;
                            LogUtil.d("主表分组ID:" + formGroupId);
                            GroupData groupData = new GroupData();//分组
                            GroupData groupDataNet = new GroupData();//传输给配置界面
                            ArrayList<Data> itemData = new ArrayList<>();
                            ArrayList<Data> itemDataNet = new ArrayList<>();//传输给配置界面
                            String value = iter.next();

                            for (int i = 0; i < forms.size(); i++) {
                                JSONObject items = forms.getJSONObject(i);
                                Data data = new Data();
                                data.setName(items.getString("fd_caption"));
                                data.setReadonly(items.getString("fd_readonly"));
                                data.setField(items.getString("fd_field"));
                                data.setDetno(items.getIntValue("fd_detno"));
                                data.setFd_logictype(items.getString("fd_logictype"));
                                data.setFd_defaultvalue(items.getString("fd_defaultvalue"));
                                data.setMaxlength(String.valueOf(items.getIntValue("fd_maxlength")));
                                data.setFdid(items.getIntValue("fd_id"));
                                data.setValue(items.getString("fd_value"));

                                if (startDate.equals(items.getString("fd_field"))) {
                                    if (StringUtil.isEmpty(items.getString("fd_value"))) {
                                        data.setValue(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));
                                    }
                                    startDate_v = data.getValue();
                                }
                                if (endDate.equals(items.getString("fd_field"))) {
                                    if (StringUtil.isEmpty(items.getString("fd_value"))) {
                                        data.setValue(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));
                                    }
                                    endDate_v = data.getValue();
                                }

                                data.setGroupId(formGroupId);
                                data.setGroup(items.getString("fd_group"));
                                data.setIsDefault(items.getIntValue("mfd_isdefault"));
                                data.setIsNeed(items.getIntValue("mfd_isdefault"));
                                data.setType(items.getString("fd_type"));
                                if (!StringUtil.isEmpty(value)) {
                                    if (value.equals(items.getString("fd_group"))) {
                                        if (data.getIsNeed() == -1) {
                                            itemData.add(data);
                                        } else {
                                            if (data.getIsNeed() == 0) {
                                                itemDataNet.add(data);
                                            }
                                        }

                                    }
                                } else {
                                    //组名未空的情况
                                    if (items.getString("fd_group") == value) {
                                        if (data.getIsNeed() == -1) {
                                            itemData.add(data);
                                        } else {
                                            if (data.getIsNeed() == 0) {
                                                itemDataNet.add(data);
                                            }
                                        }
                                    }
                                }
                            }
                            //默认空字符不显示主form分组名字
                            if (!StringUtil.isEmpty(value)) {
                                groupData.setName("");
                                groupDataNet.setName("");//传输给配置界面
                            } else {
                                groupData.setName("");
                                groupDataNet.setName("");//传输给配置界面
                            }

                            groupData.setDatas(itemData);
                            groupDataNet.setDatas(itemDataNet);//传输给配置界面
                            mDatasNet.add(groupDataNet);//传输给配置界面
                            mDatas.add(groupData);//添加分组
                            //System.out.println(value);
                            // Lg.d(JSON.toJSONString(mDatas));
                        }

                        //明细项
                        if (!ListUtils.isEmpty(grids)) {
                            //明細分組--获取组名
                            for (int i = 0; i < grids.size(); i++) {
                                JSONObject items = grids.getJSONObject(i);
                                if (!detail.contains(items.getInteger("dg_group"))) {
                                    detailist.add(items.getInteger("dg_group"));
                                    detail.add(items.getInteger("dg_group"));
                                }

                            }
                            //分组大于1开始分组
                            if (detail.size() > 1) {
                                //需要分組
                                groupData = new GroupData();//分组
                                //过滤分组
                                Iterator<Integer> iter_detail = detailist.iterator();
                                while (iter_detail.hasNext()) {
                                    Integer detailId = iter_detail.next();
                                    LogUtil.d(TAG, "明细表分组id：" + detailId + "");
                                    GroupData detailGroup = new GroupData();
                                    GroupData groupDataNet = new GroupData();
                                    ArrayList<Data> itemData = new ArrayList<>();
                                    ArrayList<Data> itemDataNet = new ArrayList<>();
                                    //临时变量
                                    String key_id = "";
                                    String key_did = "";
                                    HashMap<Integer, String> values = new HashMap<>();
                                    for (int i = 0; i < grids.size(); i++) {
                                        JSONObject items = grids.getJSONObject(i);
                                        Data data = new Data();
//                                        if (detailkey_id.equals(items.getString("dg_field"))) {
//                                            //明细id
//                                            key_id = items.getString("dg_value");
//                                            values.put(items.getInteger("dg_group"), key_id);
//                                            Lg.d("明细id:" + key_id + "groupid:" + items.getInteger("dg_group"));
//                                        }
                                        if (detailkey_did.equals(items.getString("dg_field"))) {
                                            //关联id
                                            if (StringUtil.isEmpty(items.getString("dg_value"))) {
                                                key_did = String.valueOf(formid);
                                            } else {
                                                key_did = items.getString("dg_value");
                                            }

                                        }
                                        data.setMaxlength(String.valueOf(items.getIntValue("dg_maxlength")));
                                        data.setName(items.getString("dg_caption"));
                                        data.setReadonly(items.getString("dg_logictype"));
                                        data.setField(items.getString("dg_field"));
                                        data.setDetno(items.getIntValue("dg_sequence"));
                                        data.setFdid(items.getIntValue("gd_id"));
                                        data.setValue(items.getString("dg_value"));
                                        if (d_startDate.equals(items.getString("dg_field"))) {
                                            if (StringUtil.isEmpty(items.getString("dg_value"))) {
                                                data.setValue(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));
                                            }
                                            d_startDate_v = data.getValue();
                                        }

                                        if (d_endDate.equals(items.getString("dg_field"))) {
                                            if (StringUtil.isEmpty(items.getString("dg_value"))) {
                                                data.setValue(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));
                                            }
                                            d_endDate_v = data.getValue();
                                        }

                                        data.setIsDefault(items.getIntValue("mdg_isdefault"));
                                        data.setIsNeed(items.getIntValue("mdg_isdefault"));
                                        data.setType(items.getString("dg_type"));
                                        // data.setGroupId(items.getInteger("dg_group"));
                                        data.setGroupId(items.getInteger("dg_group") + (formGroupSize - 1));
                                        data.setGroup("明细项" + items.getInteger("dg_group"));
                                        if (detailId == items.getInteger("dg_group")) {
                                            //同类分组

                                            if (data.getIsNeed() == -1) {
                                                //逃逸筛选的数据组
                                                LogUtil.d("group:" + detailId + " key_id:" + values.get(detailId) + " key_did:" + key_did);
                                                data.setDetailId(values.get(detailId));
                                                data.setDetailDid(key_did);
                                                itemData.add(data);
                                            } else {
                                                if (items.getInteger("dg_group") == 1) {
                                                    if (data.getIsNeed() == 0) {
                                                        data.setGroup("明细项");
                                                        itemDataNet.add(data);
                                                    }
                                                }

                                            }
                                            //更新的时候一定要加上明细项id
                                            if (detailkey_id.equals(items.getString("dg_field"))) {
                                                //明细id
                                                key_id = items.getString("dg_value");
                                                values.put(items.getInteger("dg_group"), key_id);
                                                if (!ListUtils.isEmpty(itemData)) {
                                                    // itemData.get(itemData.size() - 1).setDetailId(key_id);
                                                    itemData.get(0).setDetailId(key_id);

                                                    LogUtil.d("明细id:" + key_id + "groupid:" + items.getInteger("dg_group"));
                                                }
                                            }
                                            //
                                        } else {
                                            //不同类分组
                                        }

                                    }

                                    detailGroup.setDatas(itemData);
                                    detailGroup.setId(detailId);
                                    detailGroup.setName("明细项" + detailId);

                                    groupDataNet.setDatas(itemDataNet);

                                    mDatas.add(detailGroup);
                                    mDatasNet.add(groupDataNet);

                                }
                                //updateItemAdd();
                            } else {
                                //不需要分組---减少循环
                                groupData = new GroupData();//分组
                                GroupData groupDataNet = new GroupData();
                                ArrayList<Data> itemData = new ArrayList<>();
                                ArrayList<Data> itemDataNet = new ArrayList<>();
                                //临时变量
                                String key_id = "";
                                String key_did = "";
                                for (int i = 0; i < grids.size(); i++) {

                                    JSONObject items = grids.getJSONObject(i);
                                    Data data = new Data();
//                                    if (detailkey_id.equals(items.getString("dg_field"))) {
//                                        //明细id
//                                        key_id = items.getString("dg_value");
//                                    }
                                    if (detailkey_did.equals(items.getString("dg_field"))) {
                                        //主表id
                                        if (StringUtil.isEmpty(items.getString("dg_value"))) {
                                            key_did = String.valueOf(formid);
                                        } else {
                                            key_did = items.getString("dg_value");
                                        }
                                    }
                                    data.setMaxlength(String.valueOf(items.getIntValue("dg_maxlength")));
                                    data.setName(items.getString("dg_caption"));
                                    data.setField(items.getString("dg_field"));
                                    data.setReadonly(items.getString("dg_logictype"));
                                    data.setValue(items.getString("dg_value"));
                                    if (d_startDate.equals(items.getString("dg_field"))) {
                                        if (StringUtil.isEmpty(items.getString("dg_value"))) {
                                            data.setValue(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));
                                        }
                                        d_startDate_v = data.getValue();
                                    }

                                    if (d_endDate.equals(items.getString("dg_field"))) {
                                        if (StringUtil.isEmpty(items.getString("dg_value"))) {
                                            data.setValue(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));
                                        }
                                        d_endDate_v = data.getValue();
                                    }

                                    data.setDetno(items.getIntValue("dg_sequence"));
                                    data.setFdid(items.getIntValue("gd_id"));
                                    data.setIsDefault(items.getIntValue("mdg_isdefault"));
                                    data.setIsNeed(items.getIntValue("mdg_isdefault"));
                                    data.setType(items.getString("dg_type"));
                                    //  data.setGroupId(1);
                                    data.setGroupId(1 + (formGroupSize - 1));
                                    if (data.getIsNeed() == -1) {
                                        //逃逸筛选的数据组
                                        data.setDetailId(key_id);
                                        data.setDetailDid(key_did);//主表id
                                        data.setGroup("明细项1");
                                        itemData.add(data);
                                    } else {
                                        if (data.getIsNeed() == 0) {
                                            data.setGroup("明细项");
                                            itemDataNet.add(data);
                                        }
                                    }
                                    //获取明细id  重新提交
                                    //更新的时候一定要加上明细项id
                                    if (detailkey_id.equals(items.getString("dg_field"))) {
                                        //明细id
                                        key_id = items.getString("dg_value");
                                        if (!ListUtils.isEmpty(itemData)) {
//                                            itemData.get(itemData.size() - 1).setDetailId(key_id);
                                            itemData.get(0).setDetailId(key_id);
                                            LogUtil.d("明细id:" + key_id + "groupid:" + items.getInteger("dg_group"));
                                        }
                                    }


                                }
                                groupData.setId((mDatas.size() - (formGroupSize - 1)));
                                groupData.setDatas(itemData);
                                groupData.setName("明细项" + (mDatas.size() - (formGroupSize - 1)));

                                groupDataNet.setDatas(itemDataNet);//传输给配置界面

                                mDatas.add(groupData);
                                mDatasNet.add(groupDataNet);//传输给配置界面

                                // updateItemAdd();

                            }
                            updateItemAdd();
                        } else {
                            if (ll_item_add != null) ll_item_add.setVisibility(View.GONE);
                        }

                        if (mAdapter == null) {
                            if (mContext != null) {
                                mAdapter = new DataAdapter(mContext, mDatas);
                                lv_datas.setAdapter(mAdapter);
                            }
                        } else {
                            mAdapter.setmListData(mDatas);
                            mAdapter.notifyDataSetChanged();
                        }

                        if (formid == 0) {
                            if (btn_update != null) btn_update.setVisibility(View.GONE);
                            if (click_btn != null) click_btn.setVisibility(View.VISIBLE);
                        } else {
                            if (btn_update != null) btn_update.setVisibility(View.VISIBLE);
                            if (click_btn != null) click_btn.setVisibility(View.GONE);
                        }
                        if (mDatas.size() == 0) {
                            click_btn.setVisibility(View.GONE);
                        }
                    } else {
                        click_btn.setVisibility(View.GONE);
                    }


                    break;
                case Constants.HTTP_SUCCESS_INIT:
                    Boolean success = null;
                    try {
                        success = JSON.parseObject(result).getBoolean("success");
                        if ("ExtraWork$".equals(caller)) {
                            va_id = JSON.parseObject(result).getIntValue("wod_id");
                        } else {
                            //rp_id
                            //  va_id = JSON.parseObject(result).getIntValue(formidkey);
                            va_id = JSON.parseObject(result).getIntValue("rp_id");
                        }
                        if (success) {
                            judgeApprovers(va_id);
                        } else {
                            ToastMessage("接口数据非法！");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        ToastMessage(result);
                    }
                    break;
                case 0x20:
                    updata();
                    break;
                case 0x21:
                    try {
                        LogUtil.d("result:" + result);
                        success = JSON.parseObject(result).getBoolean("success");
                        if (success) {
                            judgeApprovers(formid);
//                            mHandler.postDelayed(new Runnable() {
//                                @Override
//                                public void run() {
//                                    startActivity(new Intent(DataFormDetailActivity.this, CommonDocDetailsActivity.class)
//                                            .putExtra("caller", caller)
//                                            .putExtra("keyValue", formid)
//                                            .putExtra("update", "1")
//                                            .putExtra("status", "已提交"));
//
//                                    finish();
//                                    overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
//                                }
//                            }, 2000);

                        } else {
                            if (JSONUtil.validate(result)) {
                                ToastMessage(JSON.parseObject(result).getString("exceptionInfo"));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                    }
                    break;
                case 0x15:
                    try {
                        LogUtil.d("result:" + result);
                        // success = JSON.parseObject(result).getBoolean("success");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 0x16:
                    commitSuccess();
                    break;
                case 0x14:
                    if (!StringUtil.isEmpty(result) && JSONUtil.validate(result)) {
                        JSONObject object = JSON.parseObject(result);
                        if (object.containsKey("assigns")) {
                            JSONArray array = JSON.parseObject(result).getJSONArray("assigns");
                            JSONObject o = array.getJSONObject(0);
                            String noid = "";
                            if (o != null && o.containsKey("JP_NODEID")) {
                                noid = o.getString("JP_NODEID");
                            }
                            JSONArray data = null;
                            if (o != null && o.containsKey("JP_CANDIDATES")) {
                                data = o.getJSONArray("JP_CANDIDATES");
                            }
                            if (!StringUtil.isEmpty(noid) && data != null && data.size() > 0)
                                sendToSelect(noid, data);
                        } else {
                            commitSuccess();
                        }
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    btn_update.setEnabled(true);
                    click_btn.setEnabled(true);
                    if (JSONUtil.validate(result)) {
                        ToastMessage(JSON.parseObject(result).getString("exceptionInfo"));
                    } else {
                        ToastMessage(result);
                    }
                    break;

            }
        }
    };
    private int currentGroupId;//当前点击的分组
    private ArrayList<String> mPhotoList = new ArrayList<String>();

    private void updateItemAdd() {
        if (ll_item_add != null) {
            if (caller.equals("FeePlease!CCSQ") || caller.equals("Workovertime")) {
                ll_item_add.setVisibility(View.GONE);
            } else {
                ll_item_add.setVisibility(View.VISIBLE);
            }
        }
    }

    private void commitSuccess() {
        if (formid == 0) {
            ToastMessage("提交成功！");
        } else {
            ToastMessage("更新成功！");
        }
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mContext == null) return;
                LogUtil.d("当前线程是：" + CommonUtil.isMainThread());
                if (formid == 0) {
                    startActivity(new Intent(mContext,
                            CommonCityIndustryDetailsActivity.class)
                            .putExtra("title", getToolBarTitle().toString())
                            .putExtra("keyValue", va_id)
                            .putExtra("serve_id", mServeId));
//                    startActivity(new Intent(mContext, CommonDocDetailsActivity.class)
//                            .putExtra("caller", caller)
//                            .putExtra("keyValue", va_id)
//                            .putExtra("status", "已提交"));
                } else {
                    startActivity(new Intent(mContext, CommonDocDetailsActivity.class)
                            .putExtra("caller", caller)
                            .putExtra("keyValue", formid)
                            .putExtra("update", "1")
                            .putExtra("status", "已提交"));
                }
                finish();
                overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
            }
        }, 2000);
    }

    private boolean isload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_form_detail);
        initView();
        initListener();

        initData();
    }

    private String status;

    private void initView() {
        ViewUtils.inject(this);
        Intent intent = getIntent();
        initIds();
        formid = 0;
        if (intent != null) {
            mServeId = intent.getStringExtra("serve_id");

            formid = intent.getIntExtra("id", 0);
            status = intent.getStringExtra("status");
            if (!StringUtil.isEmpty(intent.getStringExtra("caller"))) {
                caller = intent.getStringExtra("caller");
                if ("Ask4Leave".equals(caller)) {
                    formidkey = "va_id";
                    startDate = "va_startime";
                    endDate = "va_endtime";
                   setTitle("请假单");
                } else if ("SpeAttendance".equals(caller)) {
                    startDate = "sa_appdate";
                    endDate = "sa_enddate";
                    formidkey = "sa_id";
                  setTitle("特殊考勤");
                } else if ("Workovertime".equals(caller) || "ExtraWork$".equals(caller)) {
                    startDate = "wod_startdate";
                    endDate = "wod_enddate";
                    d_startDate = "wod_startdate";
                    d_endDate = "wod_enddate";
                    if ("ExtraWork$".equals(caller)) {
                        formidkey = "wod_id";//主表id  ---适应接口频繁调整
                    } else {
                        formidkey = "wo_id";//主表id
                    }
                    detailkey_id = "wod_id";//明细id
                    detailkey_did = "wod_woid";//主表id
                   setTitle("加班单");
                } else if ("FeePlease!CCSQ".equals(caller) || "FeePlease!CCSQ!new".equals(caller)) {
                    startDate = "fp_prestartdate";
                    endDate = "fp_preenddate";
                    d_startDate = "fpd_date1";
                    d_endDate = "fpd_date2";

                    formidkey = "fp_id";
                    detailkey_id = "fpd_id";//明细id
                    detailkey_did = "fpd_fpid";
                   setTitle("出差单");
                } else if ("StandbyApplication".equals(caller)) {
                    formidkey = "sa_id";
                    detailkey_id = "sad_id";//明细id
                    detailkey_did = "sad_said";//关联id 主表
                    setTitle("备用机申请单");
                } else if ("MaterielApply".equals(caller)) {
                    formidkey = "ama_id";
                    detailkey_id = "amad_id";//明细id
                    detailkey_did = "amad_amaid";//关联id 主表
                  setTitle("物料申请单");
                } else if ("MainTain".equals(caller)) {
                    formidkey = "mt_id";
                    detailkey_id = "mtd_id";//明细id
                    detailkey_did = "mtd_mtid";//关联id 主表
                   setTitle("维修申请单");
                }

            }
            if (!StringUtil.isEmpty(intent.getStringExtra("title"))) {
               setTitle(intent.getStringExtra("title"));
            }
            LogUtil.d("更新caller：" + caller);
            LogUtil.d("更新id：" + formid);
        }
        // mAdapter = new DataAdapter(mContext, mDatas);
        //给该layout设置监听，监听其布局发生变化事件
//        findViewById(R.id.sv_top).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            int heightDiff;
//
//            @Override
//            public void onGlobalLayout() {
//                //比较Activity根布局与当前布局的大小
//                if (heightDiff != 0) {
//                    int updatehight = findViewById(R.id.sv_top).getRootView().getHeight() - findViewById(R.id.sv_top).getHeight();
//
//                    if (updatehight != heightDiff) {
//                        if (updatehight > heightDiff) {
//                            //弹起键盘
//                            Lg.d("updatehight:" + updatehight + " heightDiff:" + heightDiff + "isLeave:" + isLeave);
//                            Lg.d("弹起键盘");
//                        } else {
//                            //隐藏键盘
//                            Lg.d("updatehight:" + updatehight + " heightDiff:" + heightDiff + "isLeave:" + isLeave);
//                            Lg.d("----隐藏键盘");
//                            isLeave = true;
//                            if (mAdapter != null)
//                                mAdapter.notifyDataSetChanged();
//                            isload = true;
//                        }
//                    }
//                    heightDiff = updatehight;
//                } else {
//                    heightDiff = findViewById(R.id.sv_top).getRootView().getHeight() - findViewById(R.id.sv_top).getHeight();
//                }
//            }
//        });
    }

    private void initIds() {
        lv_datas = (ListViewInScroller) findViewById(R.id.lv_datas);
        tv_item_add = (TextView) findViewById(R.id.tv_item_add);
        click_btn = (Button) findViewById(R.id.click_btn);
        btn_update = (Button) findViewById(R.id.btn_update);
        ll_item_add = (LinearLayout) findViewById(R.id.ll_item_add);
        ll_top = (LinearLayout) findViewById(R.id.ll_top);
    }

    private void initListener() {
        ll_item_add.setOnClickListener(this);
        click_btn.setOnClickListener(this);
        btn_update.setOnClickListener(this);
    }

    int formid;

    private void initData() {
        loadData(formid);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if ("USOFTSYS".equals(CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "erp_master"))) {
            if (PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false)) {
                menu.findItem(R.id.oa_signin_set).setVisible(false);
            } else {
                menu.findItem(R.id.oa_signin_set).setVisible(false);
            }
        } else {
            if (PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false)) {
                menu.findItem(R.id.oa_signin_set).setVisible(false);
            } else {
                menu.findItem(R.id.oa_signin_set).setVisible(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signin_set, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.oa_signin_set) {
            ArrayList<Data> fields = new ArrayList<>();
            ArrayList<Data> fieldsDis = new ArrayList<>();
            LogUtil.d(JSON.toJSONString(mDatasNet));
            for (int i = 0; i < mDatasNet.size(); i++) {
                GroupData group = mDatasNet.get(i);
                for (int j = 0; j < group.getDatas().size(); j++) {
                    if (group.getDatas().get(j).getIsDefault() == 0) {//隐藏的字段
                        fields.add(group.getDatas().get(j));
                    }
                }

            }
            for (int i = 0; i < mDatas.size(); i++) {
                GroupData group = mDatas.get(i);
                for (int j = 0; j < group.getDatas().size(); j++) {
                    if (group.getDatas().get(j).getIsDefault() == -1) {//显示的字段
                        fieldsDis.add(group.getDatas().get(j));
                    }
                }

            }
            startActivityForResult(new Intent(this, DataFormFieldActivity.class)
                            .putParcelableArrayListExtra("fields", fields)
                            .putParcelableArrayListExtra("fieldsDis", fieldsDis)
                            .putExtra("caller", caller),
                    0x25);
        } else if (item.getItemId() == R.id.oa_leave) {
            startActivity(new Intent(mContext, CommonFormListActivity.class)
                    .putExtra("serveId", mServeId)
                    .putExtra("caller", caller)
                    .putExtra("title", getToolBarTitle().toString()));
            if (formid != 0) {
                finish();
            }
        } else if (item.getItemId() == android.R.id.home) {
            if ("在录入".equals(status)) {
                FormListSelectActivity.reload = false;
            } else if ("已提交".equals(status)) {
                FormListSelectActivity.reload = true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadData(int id) {
        progressDialog.show();
        if (!CommonUtil.isNetWorkConnected(mContext)) {
            ToastMessage("网络未连接！");
        }
        if (caller == null) {
            caller = "Ask4Leave";
        }
//        String url = CommonUtil.getCityBaseUrl(ct) + "api/serve/config.action";
        String url = CommonUtil.getCityBaseUrl(ct) + "mobile/common/getConfig.action";

        Map<String, Object> params = new HashMap<>();
        params.put("serve_id", mServeId);
//        params.put("client_type", "cc");
//        params.put("access_token", "123456");
//        params.put("sessionId", CommonUtil.getB2BUid(this));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", CommonUtil.getB2BUid(this));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, load_success, null, null, "get");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_item_add) {
            addGridItemData();
        } else if (v.getId() == R.id.click_btn) {
            if (formid == 0) {
                commit();
            }
        } else if (v.getId() == R.id.btn_update) {
            updata();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        click_btn.setEnabled(true);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * @desc:需要优化的适配器
     * @author：Arison on 2016/12/2
     */
    public class DataAdapter extends BaseAdapter {
        private boolean isProtectState = false;
        private Context ct;
        private LayoutInflater inflater;
        private static final int TYPE_CATEGORY_ITEM = 0;//分类
        private static final int TYPE_ITEM = 1;//分类下的item
        private ArrayList<GroupData> mListData;
        private ArrayList<Data> datas;
        private ArrayList<Integer> groupIndex;
        private String currentEditField;

        //在初始化的的时候，把分类处理完整
        public DataAdapter(Context ct, ArrayList<GroupData> mdata) {
            this.ct = ct;
            this.mListData = mdata;
            this.inflater = LayoutInflater.from(ct);
            groupIndex = new ArrayList<>();
            datas = new ArrayList<>();
            int index = 0;
            for (int i = 0; i < mdata.size(); i++) {
                groupIndex.add(index);
                Data gData = new Data();
                gData.setGroup(mdata.get(i).getName());
                gData.setGroupId(mdata.get(i).getId());
                int size = mdata.get(i).getDatas().size();
                if (size != 0) {
                    gData.setDetailId(mdata.get(i).getDatas().get(0).getDetailId());
                    gData.setDetailDid(mdata.get(i).getDatas().get(0).getDetailDid());
                }
                datas.add(gData);
                datas.addAll(mdata.get(i).getDatas());
                index = index + mdata.get(i).getDatas().size() + 1;
            }
        }

        @Override
        public int getCount() {
            return datas == null ? 0 : datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas == null ? null : datas.get(position);
        }


        @Override
        public int getItemViewType(int position) {
            if (groupIndex.contains(position)) {
                return TYPE_CATEGORY_ITEM;
            } else {
                return TYPE_ITEM;
            }
        }

        public boolean isProtectState() {
            return isProtectState;
        }

        public void setIsProtectState(boolean isProtectState) {
            this.isProtectState = isProtectState;
        }

        /**
         * @desc:判断是否是最后一个元素
         * @author：Arison on 2016/11/15
         */

        public HashMap<String, Object> isLastItem(int position) {
            HashMap<String, Object> results = new HashMap<>();
            boolean falg = false;
            // 异常情况处理
            if (null == mListData || position < 0 || position > getCount()) {
                return null;
            }
            int categroyFirstIndex = 0;
            for (int i = 0; i < mListData.size(); i++) {
                int size = mListData.get(i).getItemCount();
                int categoryIndex = position - categroyFirstIndex;
                if (i == mListData.size() - 1) {
                    // 在当前分类中的索引值
                    // item在当前分类内
                    if (categoryIndex < size) {
                        if (categoryIndex == size - 1) {
                            results.put("categoryIndex", categoryIndex);
                            results.put("groupIndex", i);
                            results.put("isLastItem", true);
                            results.put("isGroupLastItem", true);
                            return results;
                        } else {
                            results.put("isLastItem", false);
                            results.put("categoryIndex", categoryIndex);
                            results.put("groupIndex", i);
                            results.put("isGroupLastItem", false);
                            return results;
                        }
                    }
                    // 索引移动到当前分类结尾，即下一个分类第一个元素索引

                } else {
                    if (categoryIndex < size) {
                        if (categoryIndex == size - 1) {
                            results.put("isLastItem", false);
                            results.put("isGroupLastItem", true);
                            results.put("categoryIndex", categoryIndex);
                            results.put("groupIndex", i);
                            return results;
                        } else {
                            results.put("isLastItem", false);
                            results.put("isGroupLastItem", false);
                            results.put("categoryIndex", categoryIndex);
                            results.put("groupIndex", i);
                            return results;
                        }
                    }
                }
                categroyFirstIndex += size;
            }

            return results;
        }

        public ArrayList<GroupData> getmListData() {
            return mListData;
        }

        public void setmListData(ArrayList<GroupData> mdata) {
            if (mdata == null) return;
            this.mListData = mdata;
            groupIndex = new ArrayList<>();
            datas = new ArrayList<>();
            int index = 0;
            LogUtil.d(JSON.toJSONString(mdata));
            for (int i = 0; i < mdata.size(); i++) {
                groupIndex.add(index);
                Data gData = new Data();
                gData.setGroup(mdata.get(i).getName());
                gData.setGroupId(mdata.get(i).getId());
                int size = mdata.get(i).getDatas().size();
                if (size != 0) {
                    gData.setDetailId(mdata.get(i).getDatas().get(0).getDetailId());
                    gData.setDetailDid(mdata.get(i).getDatas().get(0).getDetailDid());
                }
                datas.add(gData);
                datas.addAll(mdata.get(i).getDatas());
                index = index + mdata.get(i).getDatas().size() + 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int itemViewType = getItemViewType(position);
            switch (itemViewType) {
                case TYPE_CATEGORY_ITEM:
                    convertView = inflater.inflate(R.layout.listview_item_header, null);
                    final TextView textView = (TextView) convertView.findViewById(R.id.tv_title);
                    TextView item_add = (TextView) convertView.findViewById(R.id.iv_item_add);
                    RelativeLayout header = (RelativeLayout) convertView.findViewById(R.id.rl_item_header);
                    final Data itemValue = ((Data) getItem(position));
                    // final  int id= getGroupPosition(position);
                    textView.setText("");//itemValue.getGroup()
                    try {
                        if (StringUtil.isEmpty(itemValue.getGroup())) {
                            header.setVisibility(View.GONE);
                        } else {
                            // header.setVisibility(View.GONE);
                            if (itemValue.getGroup().contains("明细") && !itemValue.getGroup().equals("明细项1")) {
                                item_add.setVisibility(View.VISIBLE);
                            } else {
                                item_add.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    item_add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog
                                    .Builder(mContext)
                                    .setTitle("温馨提示")
                                    .setMessage("\t\t确认删除该明细项?")
                                    .setNegativeButton("取消", null)
                                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            LogUtil.d(JSON.toJSONString(itemValue));
                                            deleteGridItemData(itemValue.getGroupId(), itemValue.getDetailId());
                                            notifyDataSetChanged();
                                        }
                                    }).show();
                        }
                    });
                    break;
                case TYPE_ITEM:
                    ViewModel model;
                    LinearLayout select_img_layout;
                    MyGridView grid_view;
                    if (true) {
                        convertView = inflater.inflate(R.layout.listitem_form_edit, parent, false);
                        model = new ViewModel();
                        model.text = (TextView) convertView.findViewById(R.id.tv_less_key);
                        model.editText = (EditText) convertView.findViewById(R.id.tv_less_value);
                        model.ll_moment = (LinearLayout) convertView.findViewById(R.id.ll_moment);
                        model.editText.setBackgroundResource(R.color.transparent);
                        grid_view = (MyGridView) convertView.findViewById(R.id.grid_view);
                        select_img_layout = (LinearLayout) convertView.findViewById(R.id.select_img_layout);
                        convertView.setTag(model);
                    } else {
                        //缓存问题
                        // model= (ViewModel) convertView.getTag();
                    }
                    final Data data = (Data) getItem(position);
                    HashMap<String, Object> results = isLastItem(position);
                    final Integer categoryIndex = (Integer) results.get("categoryIndex");
                    model.text.setText(data.getName());

                    if ("F".equals(data.getReadonly()) ||
                            "necessaryField".equals(data.getReadonly())) {
                        model.editText.setHint("请输入(必填)");
                    } else {
                        model.editText.setHint("请输入");
                    }
                    if (data.getType().equals("D") ||
                            data.getType().equals("C") ||
                            data.getType().equals("MF") ||
                            data.getType().equals("SF") ||
                            data.getType().equals("DF") ||
                            data.getType().equals("RG") ||
                            data.getType().equals("CBG")) {
                        model.editText.setKeyListener(null);
                        model.editText.setFocusable(false);
                        model.editText.setHint("请选择(必选)");
                    }
                    //附件上传
                    if (data.getType().equals("FF")) {
                        model.editText.setKeyListener(null);
                        model.editText.setFocusable(false);
                        model.editText.setHint("请上传(必选)");


                        cAdapter = new ComPictureAdapter(mContext);
                        cAdapter.setmPhotoList(mPhotoList);
                        cAdapter.setMaxSiz(9);
                        grid_view.setAdapter(cAdapter);
                        grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                int viewType = cAdapter.getItemViewType(position);
                                if (viewType == 1) {
                                    showSelectPictureDialog();//添加
                                } else {
                                    showPictureActionDialog(position); //删除
                                }
                            }
                        });
                        select_img_layout.setVisibility(View.VISIBLE);
                    }

                    if (data.isEditing()) {
                        model.editText.setEnabled(true);
                    } else {
                        model.editText.setEnabled(false);
                        model.editText.setHint("不可编辑");
                    }

                    if (data.getType().equals("N")) {
                        model.editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                    }
                    final EditText editText = model.editText;

                    editText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (data.getType().equals("D")) {
                                boolean hasMenu = false;
                                if ("Ask4Leave".equals(caller)) hasMenu = true;
                                Log.d("date:", startDate_v + "," + endDate_v);
                                if ("Ask4Leave".equals(caller)
                                        || "FeePlease!CCSQ".equals(caller)
                                        || "Workovertime".equals(caller)
                                        || "ExtraWork$".equals(caller)
                                        || "FeePlease!CCSQ!new".equals(caller)) {
                                    startActivityForResult(new Intent(mContext, SelectCalendarActivity.class)
                                                    .putExtra("startDate", startDate_v)
                                                    .putExtra("endDate", endDate_v)
                                                    .putExtra("hasMenu", hasMenu)
                                                    .putExtra("id", categoryIndex)
                                                    .putExtra("field", data.getField())
                                                    .putExtra("object", data)
                                                    .putExtra("caller", caller)
                                            , 0x30);
                                } else {
                                    showDateDialog(ct, (EditText) v, data);
                                }

                            }
                            //下拉选择
                            if (data.getType().equals("C")) {
                                HashMap param = new HashMap<>();
                                param.put("caller", caller);
                                param.put("field", data.getField());
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("param", param);
                                Intent intent = new Intent(ct, SelectActivity.class);
                                intent.putExtra("type", 1);
                                intent.putExtra("reid", R.style.OAThemeMeet);
                                intent.putExtras(bundle);
                                intent.putExtra("key", "combdatas");
                                intent.putExtra("showKey", "DISPLAY");
                                //mobile/common/getCombo.action
                                intent.putExtra("action", "mobile/common/getComboValue.action");//
                                intent.putExtra("title", data.getName());
                                intent.putExtra("id", categoryIndex);//需要把zum
                                intent.putExtra("object", data);
                                startActivityForResult(intent, categoryIndex);
                                isLeave = true;
                            }
                            //单选，多选处理
                            if (data.getType().equals("MF") || data.getType().equals("SF")
                                    || data.getType().equals("RG") ||
                                    data.getType().equals("CBG")) {
                                ArrayList<SelectBean> formBeaan = new ArrayList<>();
                                String logicType = data.getFd_logictype();
                                String[] items = logicType.split(";");
                                for (int i = 0; i < items.length; i++) {
                                    SelectBean selectBean = new SelectBean();
                                    selectBean.setName(items[i]);
                                    formBeaan.add(selectBean);
                                }
                                Intent intent = new Intent(ct, SelectActivity.class);
                                intent.putExtra("type", 2);//本地数据加载
                                if (data.getType().equals("SF") || data.getType().equals("RG")) {
                                    intent.putExtra("isSingle", true);
                                } else {
                                    intent.putExtra("isSingle", false);
                                }
                                intent.putParcelableArrayListExtra("data", formBeaan);
                                intent.putExtra("reid", R.style.OAThemeMeet);
                                intent.putExtra("key", "combdatas");
                                intent.putExtra("showKey", data.getField());
                                intent.putExtra("action", "common/dbfind.action");
                                intent.putExtra("title", data.getName());
                                intent.putExtra("id", categoryIndex);
                                intent.putExtra("object", data);
                                startActivityForResult(intent, categoryIndex);
                                isLeave = true;
                            }
                            //连带操作
                            if (data.getType().equals("DF")) {
                                if ("fpd_d5".equals(data.getField())) {
                                    //出差单 客户名称联动操作特殊处理
                                    Intent intent = new Intent("com.modular.form.SelectAimActivity")
                                            .putExtra("groupId", data.getGroupId());
                                    currentGroupId = data.getGroupId();
                                    startActivityForResult(intent, 0x23);
                                } else {
                                    HashMap param = new HashMap<>();
                                    String[] fields = new String[]{"sa_custname"};
                                    if (caller.equals("StandbyApplication")) {

                                    }
                                    if (caller.equals("MaterielApply")) {
                                        fields = new String[]{"amad_spec", "amad_sysname", "amad_unit"};
                                    }
                                    param.put("which", "form");
                                    param.put("caller", caller);
                                    if (caller.equals("MainTain")) {
                                        fields = new String[]{"mtd_proname", "mtd_remark", "mtd_unit"};
                                        param.put("caller", "sProduct");
                                        param.put("which", "grid");
                                    }
                                    param.put("page", "1");
                                    param.put("condition", "1=1");
                                    param.put("pageSize", "30");
                                    param.put("field", data.getField());
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("param", param);
                                    Intent intent = new Intent(ct, SelectActivity.class);
                                    intent.putExtra("type", 1);
                                    intent.putExtra("reid", R.style.OAThemeMeet);
                                    intent.putExtras(bundle);
                                    intent.putExtra("key", "combdatas");
                                    intent.putExtra("showKey", data.getField());
                                    intent.putExtra("fields", fields);
                                    intent.putExtra("action", "common/dbfind.action");
                                    intent.putExtra("title", data.getName());
                                    intent.putExtra("id", categoryIndex);
                                    intent.putExtra("object", data);
                                    startActivityForResult(intent, categoryIndex);
                                    isLeave = true;
                                }

                            }

                            //上传附件
                            if (data.getType().equals("FF")) {
                                showSelectPictureDialog();
                            }


                        }
                    });


                    editText.addTextChangedListener(new TextWatcher() {
                        private String text = "";

                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                            text = s.toString();
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            currentEditField = data.getName();
                            String type = data.getType();
                            String length = data.getMaxlength();
                            if ("0".equals(length)) length = "300";
                            if ("N".equals(type) && !StringUtil.isEmpty(s.toString())) {
                                boolean falg = true;
                                CommonUtil.counter = 0;
                                if (s.toString().length() >= 2
                                        && s.toString().substring(s.toString().length() - 1, s.toString().length())
                                        .equals(".") && CommonUtil.countStr(s.toString(), ".") == 1) {
                                    falg = false;
                                }
                                if (!RegexUtil.checkRegex(s.toString(), "^[0-9]+(.[0-9]{1,2})?$") && falg) {
                                    editText.setText(text);
                                } else {
                                    data.setValue(s.toString());
                                }
                            } else {
                                if (!data.getType().equals("SF") && !data.getType().equals("MF") && !data.getType().equals("C") && !"D".equals(type)
                                        && (s.toString().length() > Integer.valueOf(length))) {
                                    ToastMessage(data.getName() + "超出限制字符长度" + Integer.valueOf(length));
                                } else {
                                    data.setValue(s.toString());
                                }
                            }

                        }
                    });
                    if (!StringUtil.isEmpty(data.getValue()) && isLeave) {
                        model.editText.setText(data.getValue());
                    }
                    break;
            }

            return convertView;
        }

        class ViewModel {
            TextView text;
            EditText editText;
            LinearLayout ll_moment;
        }
    }

    /**
     * @desc:日期选择控件---底部弹窗
     * @author：Arison on 2017/2/13
     */
    private void showDateDialog(Context ct, final EditText tv, final Data field) {
        DateTimePicker picker = new DateTimePicker((Activity) ct, DateTimePicker.HOUR_OF_DAY);
        picker.setRange(2000, 2030);
        if (!StringUtil.isEmpty(field.getValue())) {
            //记住时间
            String date = field.getValue();
            int year = Integer.valueOf(date.substring(0, 4));
            int month = Integer.valueOf(date.substring(5, 7));
            int day = Integer.valueOf(date.substring(8, 10));
            int hour = Integer.valueOf(date.substring(11, 13));
            int minute = Integer.valueOf(date.substring(14, 16));
            picker.setSelectedItem(year, month, day, hour, minute);
        } else {
            //赋值当前系统时间
            picker.setSelectedItem(Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH) + 1,
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE));
        }
        picker.setOnDateTimePickListener(new DateTimePicker.OnYearMonthDayTimePickListener() {
            @Override
            public void onDateTimePicked(String year, String month, String day, String hour, String minute) {
                tv.setText(year + "-" + month + "-" + day + " " + hour + ":" + minute + ":00");
                if (caller.equals("Workovertime")) {//加班单
                    if (d_startDate.equals(field.getField())) {
                        d_startDate_v = tv.getText().toString();
                    }
                    if (d_endDate.equals(field.getField())) {
                        d_endDate_v = tv.getText().toString();
                    }
                    //判断两者时间不为空
                    distance(d_startDate_v, d_endDate_v, field.getGroupId());
                }
            }
        });
        picker.show();
    }

    /**
     * @desc:计算加班时间
     * @author：Arison on 2017/3/16
     */
    private void distance(String start, String end, int groupId) {
        if (!StringUtil.isEmpty(start) && !StringUtil.isEmpty(end)) {
            //计算加班时数
            List<Data> tempLists = mAdapter.getmListData().get(groupId).getDatas();
            int wod_count = 0;
            for (int i = 0; i < tempLists.size(); i++) {
                Data tempData = tempLists.get(i);
                if (tempData.getField().equals("wod_count")) {
                    wod_count = i + 1;
                }
            }
            Date startDate = null;
            Date endDate = null;
            try {
                startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse(start);
                endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .parse(end);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            double result = (double) (DateFormatUtil.getDifferenceNum(startDate, endDate, 1)) / 60;
            LogUtil.d("当天加班时数：" + result);
            if (wod_count == 0) return;
            mAdapter.getmListData().get(groupId).getDatas().get(wod_count - 1).setValue(new DecimalFormat("0.0").format(result));
            mAdapter.notifyDataSetChanged();
        }
    }


    /**
     * @desc:调转界面返回参数数据
     * @author：Arison on 2016/11/14
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mAdapter != null) mAdapter.setIsProtectState(true);
        switch (resultCode) {
            case 0x20:
                if (data == null) return;
                SelectBean b = new SelectBean();
                if (data.getParcelableExtra("data") instanceof SelectBean) {
                    b = data.getParcelableExtra("data");
                }
                if (requestCode == b.getId() && resultCode == 0x20) {
                    //if (b == null || StringUtil.isEmpty(b.getJson())) return;
                    if (true) {
                        Data vaules = JSON.parseObject(b.getObject(), Data.class);
                        String value = null;
                        if ("C".equals(vaules.getType())) {
                            value = JSON.parseObject(b.getJson()).getString("VALUE");
                            muiltCallerMainTain(vaules, value);//维修申请单特殊处理
                        }
                        if ("SF".equals(vaules.getType()) || "DF".equals(vaules.getType())
                                || "RG".equals(vaules.getType()) || "CBG".equals(vaules.getType())) {
                            mulitSelectUpdate(b, vaules);//联动字段选择
                        }
                        mulitAsk4Leave(b, vaules, value);//请假单特殊处理
                        mAdapter.notifyDataSetChanged();
                    }
                } else if (requestCode == 0x22) {
                    SelectBean d = data.getParcelableExtra("data");
                    if (d == null) return;
                    String name = StringUtil.isEmpty(d.getName()) ? "" : d.getName();
                    getEmnameByReturn(name);
                } else if (requestCode == 0x23) {
                    SelectAimModel entity = data.getParcelableExtra("data");
                    PopupWindowHelper.create(this, getString(R.string.perfect_company_name), entity, new PopupWindowHelper.OnClickListener() {
                        @Override
                        public void result(SelectAimModel model) {
                            //出差单 写死固定参数1
                            String cu_name = model.getName();
                            String cu_address = model.getAddress();
                            int sa_custname = 0;
                            int sa_custaddress = 0;
                            LogUtil.d("currentGroup:" + currentGroupId);
                            List<Data> tempLists = mAdapter.getmListData().get(currentGroupId).getDatas();
                            for (int i = 0; i < tempLists.size(); i++) {
                                Data tempData = tempLists.get(i);
                                if (tempData.getField().equals("fpd_d5")) {
                                    sa_custname = i + 1;
                                }
                                if (tempData.getField().equals("fpd_d6")) {
                                    sa_custaddress = i + 1;
                                }
                            }
                            if (sa_custname>0){
                                mAdapter.getmListData().get(currentGroupId).getDatas().get(sa_custname - 1).setValue(cu_name);
                            }
                            if (sa_custaddress>0){
                                mAdapter.getmListData().get(currentGroupId).getDatas().get(sa_custaddress - 1).setValue(cu_address);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
                break;
            case 0x21://多选
                if (data == null) return;
                ArrayList<SelectBean> muliData = data.getParcelableArrayListExtra("data");
                int id = data.getIntExtra("id", 0);
                LogUtil.d("HttpLogs", "muliData:" + JSON.toJSONString(muliData));
                LogUtil.d("HttpLogs", "多选项：" + mAdapter.getmListData().get(0).getDatas().get(id - 1).getFd_logictype());
                String fd_logictype = mAdapter.getmListData().get(0).getDatas().get(id - 1).getFd_logictype();
                if (requestCode == id && resultCode == 0x21) {//多选框
                    StringBuilder values = new StringBuilder("");
                    String logicArray[] = fd_logictype.split(";");
                    for (int i = 0; i < logicArray.length; i++) {
                        String logicName = logicArray[i];
                        String isSelected = "0";
                        for (int j = 0; j < muliData.size(); j++) {
                            String selectName = muliData.get(j).getName();
                            if (selectName.equals(logicName)) {
                                isSelected = "1";
                            }
                        }
                        values.append(isSelected + ";");
                    }

                    LogUtil.d("HttpLogs", "values:" + values.toString());

                    StringBuilder str = new StringBuilder("");
                    if (!ListUtils.isEmpty(muliData)) {
                        for (int i = 0; i < muliData.size(); i++) {
                            LogUtil.d(muliData.get(i).getName());
                            if (i == (muliData.size() - 1)) {
                                str.append(muliData.get(i).getName());
                            } else {
                                str.append(muliData.get(i).getName() + ",");
                            }

                        }
                    }
                    LogUtil.d("HttpLogs", "vaules:CN:" + str.toString());
                    mAdapter.getmListData().get(0).getDatas().get(id - 1).setValue(str.toString());
                    mAdapter.getmListData().get(0).getDatas().get(id - 1).setDisplayValue(values.toString());
                    //Toast.makeText(ct, "下拉项：" + datas.get(id - 1).getName() + "返回值：" + str.toString(), Toast.LENGTH_LONG).show();
                    // mAdapter.get
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case 0x25:
                mAdapter = null;
                initData();
                break;
            case 0x11:
                try {
                    parseResultByField(data);
                } catch (Exception e) {

                }
                break;
        }

        if (requestCode == 0x01 && resultCode == 0x02 && data != null) {
            mPhotoList.addAll(data.getStringArrayListExtra("files"));
            Log.i("files0x01", data.getStringArrayListExtra("files").toString());
            Log.i("mPhotoList", mPhotoList.toString());
            doImageFiltering(mPhotoList);
//            uploadFile();
//            mAdapter.notifyDataSetInvalidated();
        }

        if (requestCode == REQUEST_CODE_CAPTURE_PHOTO) {// 拍照返回
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    mPhotoList.add(mNewPhotoUri.getPath());
                    cAdapter.notifyDataSetInvalidated();
//                    uploadFile();
                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_PHOTO) {// 选择一张图片,然后立即调用裁减
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    LogUtil.d("uri:", JSON.toJSONString(data.getData()));
                    String path = CameraUtil.getImagePathFromUri(this, data.getData());
                    mPhotoList.add(path);
//                    uploadFile();
                    cAdapter.notifyDataSetInvalidated();
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }
    }

    private void parseResultByField(Intent data) {
        int id;
        Data model = data.getParcelableExtra("object");
        id = data.getIntExtra("id", 0);
        String startDate = data.getStringExtra("startDate");
        String endDate = data.getStringExtra("endDate");

        startDate = startDate + ":00";
        endDate = endDate + ":00";

        startDate_v = startDate;//赋值给全局变量
        endDate_v = endDate;//赋值给全局变量

        String field = data.getStringExtra("field");
        LogUtil.d("id=" + id);
        LogUtil.d("groupId=" + model.getGroupId());
        if ("Ask4Leave".equals(caller)) {
            //va_startime
            //va_endtime
            int va_alldays = 0;
            int va_alltimes = 0;
            List<Data> tempLists = mAdapter.getmListData().get(model.getGroupId()).getDatas();
            for (int i = 0; i < tempLists.size(); i++) {
                Data tempData = tempLists.get(i);
                if (tempData.getField().equals("va_startime")) {
                    va_alldays = i + 1;
                }
                if (tempData.getField().equals("va_endtime")) {
                    va_alltimes = i + 1;
                }
            }
            mAdapter.getmListData().get(model.getGroupId()).getDatas().get(va_alldays - 1).setValue(startDate);
            mAdapter.getmListData().get(model.getGroupId()).getDatas().get(va_alltimes - 1).setValue(endDate);
        }
        if ("Workovertime".equals(caller) || "ExtraWork$".equals(caller)) {
            int va_alldays = 0;
            int va_alltimes = 0;

            List<Data> tempLists = mAdapter.getmListData().get(model.getGroupId()).getDatas();
            for (int i = 0; i < tempLists.size(); i++) {
                Data tempData = tempLists.get(i);
                if (tempData.getField().equals("wod_startdate")) {
                    va_alldays = i + 1;
                }
                if (tempData.getField().equals("wod_enddate")) {
                    va_alltimes = i + 1;
                }
            }
            mAdapter.getmListData().get(model.getGroupId()).getDatas().get(va_alldays - 1).setValue(startDate);
            mAdapter.getmListData().get(model.getGroupId()).getDatas().get(va_alltimes - 1).setValue(endDate);

            distance(startDate, endDate, model.getGroupId());

        }


        if ("FeePlease!CCSQ".equals(caller) || "FeePlease!CCSQ!new".equals(caller)) {
            int va_alldays = 0;
            int va_alltimes = 0;
            //fp_preenddate
            //fp_prestartdate
            if ("fp_preenddate".equals(field) || "fp_prestartdate".equals(field)) {
                List<Data> tempLists = mAdapter.getmListData().get(model.getGroupId()).getDatas();
                for (int i = 0; i < tempLists.size(); i++) {
                    Data tempData = tempLists.get(i);
                    if (tempData.getField().equals("fp_preenddate")) {
                        va_alldays = i + 1;
                    }
                    if (tempData.getField().equals("fp_prestartdate")) {
                        va_alltimes = i + 1;
                    }
                }
                mAdapter.getmListData().get(model.getGroupId()).getDatas().get(va_alltimes - 1).setValue(startDate);
                mAdapter.getmListData().get(model.getGroupId()).getDatas().get(va_alldays - 1).setValue(endDate);
            }
            if ("fpd_date1".equals(field) || "fpd_date2".equals(field)) {
                List<Data> tempLists = mAdapter.getmListData().get(model.getGroupId()).getDatas();
                for (int i = 0; i < tempLists.size(); i++) {
                    Data tempData = tempLists.get(i);
                    if (tempData.getField().equals("fpd_date1")) {
                        va_alldays = i + 1;
                    }
                    if (tempData.getField().equals("fpd_date2")) {
                        va_alltimes = i + 1;
                    }
                }
                mAdapter.getmListData().get(model.getGroupId()).getDatas().get(va_alldays - 1).setValue(startDate);
                mAdapter.getmListData().get(model.getGroupId()).getDatas().get(va_alltimes - 1).setValue(endDate);
            }
        }
    }

    private void muiltCallerMainTain(Data vaules, String value) {
        if (caller.equals("MainTain")) {
            int sa_custname = 0;
            int md_row = 0;
            List<Data> tempLists = mAdapter.getmListData().get(vaules.getGroupId()).getDatas();
            for (int i = 0; i < tempLists.size(); i++) {
                Data tempData = tempLists.get(i);
                if (tempData.getField().equals("mt_ckcode")) {
                    sa_custname = i + 1;
                }
                if (tempData.getField().equals("mt_row")) {
                    md_row = i + 1;
                }
            }
            if (vaules.getField().equals("mt_type")) {
                if (value.equals("back")) {
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(sa_custname - 1).setValue("");
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(sa_custname - 1).setIsEditing(false);

                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(md_row - 1).setValue("");
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(md_row - 1).setIsEditing(false);
                } else {
//                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(sa_custname - 1).setDisplayValue("请选择(必选)");
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(sa_custname - 1).setIsEditing(true);

//                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(md_row - 1).setDisplayValue("请选择(必选)");
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(md_row - 1).setIsEditing(true);
                }
            }
        }
        //
    }

    private void mulitAsk4Leave(SelectBean b, Data vaules, String value) {
        if (ListUtils.isEmpty(grids)) {
            //主
            mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(b.getId() - 1).setValue(b.getName());
            if ("Ask4Leave".equals(caller)) {
                //va_alldays   天
                //va_alltimes  时
                int va_alldays = 0;
                int va_alltimes = 0;
                List<Data> tempLists = mAdapter.getmListData().get(vaules.getGroupId()).getDatas();
                for (int i = 0; i < tempLists.size(); i++) {
                    Data tempData = tempLists.get(i);
                    if (tempData.getField().equals("va_alldays")) {
                        va_alldays = i + 1;
                    }
                    if (tempData.getField().equals("va_alltimes")) {
                        va_alltimes = i + 1;
                    }
                }
                LogUtil.d("va_alldays:" + va_alldays);
                LogUtil.d("va_alltimes:" + va_alltimes);
                if ("按小时".equals(b.getName())) {//4
                    //天的编辑框不可编辑，并赋值为0
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(va_alldays - 1).setValue("0");
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(va_alldays - 1).setIsEditing(false);
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(va_alltimes - 1).setIsEditing(true);
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(va_alltimes - 1).setValue("");
                } else if ("按天".equals(b.getName())) {//5
                    //小时的编辑框不可编辑，并赋值为0
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(va_alltimes - 1).setValue("0");
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(va_alltimes - 1).setIsEditing(false);
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(va_alldays - 1).setIsEditing(true);
                    mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(va_alldays - 1).setValue("");
                }
            }
            if (value != null) {
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(b.getId() - 1).setDisplayValue(value);
            }
        } else {
            //明细
            if (b.getName().contains("(")) {
                b.setName(b.getName().split("\\(")[0]);//索菱联动定 特殊处理
            }
            mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(b.getId() - 1).setValue(b.getName());
            if (value != null) {
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(b.getId() - 1).setDisplayValue(value);
            }
        }
    }

    /**
     * @desc:联动选择
     * @author：Arison on 2017/4/10
     */
    private void mulitSelectUpdate(SelectBean b, Data vaules) {
        JSONObject jdata = JSON.parseObject(b.getJson());

        Data tdata = JSON.parseObject(b.getObject(), Data.class);
        mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(b.getId() - 1).setValue(b.getName());

        if (caller.equals("StandbyApplication")) {
            String cu_name = jdata.getString("cu_name");
            int sa_custname = 0;
            List<Data> tempLists = mAdapter.getmListData().get(vaules.getGroupId()).getDatas();
            for (int i = 0; i < tempLists.size(); i++) {
                Data tempData = tempLists.get(i);
                if (tempData.getField().equals("sa_custname")) {
                    sa_custname = i + 1;
                }
            }
            mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(sa_custname - 1).setValue(cu_name);
        }
        if (caller.equals("MaterielApply")) {
            int sa_custname = 0;
            int amad_spec = 0;
            int amad_sysname = 0;
            int amad_unit = 0;
            List<Data> tempLists = mAdapter.getmListData().get(vaules.getGroupId()).getDatas();
            for (int i = 0; i < tempLists.size(); i++) {
                Data tempData = tempLists.get(i);
                if (tempData.getField().equals("ama_customer")) {
                    sa_custname = i + 1;
                }
                if (tempData.getField().equals("amad_spec")) {
                    amad_spec = i + 1;
                }
                if (tempData.getField().equals("amad_sysname")) {
                    amad_sysname = i + 1;
                }
                if (tempData.getField().equals("amad_unit")) {
                    amad_unit = i + 1;
                }
            }
            String pr_spec = null;
            String pr_detail = null;
            String pr_unit = null;
            String cu_name = null;
            if (tdata.getField().equals("amad_makecode")) {
                pr_spec = jdata.getString("pr_spec");
                pr_detail = jdata.getString("pr_detail");
                pr_unit = jdata.getString("pr_unit");
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(amad_spec - 1).setValue(pr_spec);
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(amad_sysname - 1).setValue(pr_detail);
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(amad_unit - 1).setValue(pr_unit);
            }
            if (tdata.getField().equals("ama_text2")) {
                cu_name = jdata.getString("cu_name");
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(sa_custname - 1).setValue(cu_name);
            }
        }
        if (caller.equals("MainTain")) {
            int mt_custname = 0;
            int mt_text2 = 0;
            int mtd_proname = 0;
            int mtd_remark = 0;
            int mtd_unit = 0;
            int mtd_row = 0;
            List<Data> tempLists = mAdapter.getmListData().get(vaules.getGroupId()).getDatas();
            for (int i = 0; i < tempLists.size(); i++) {
                Data tempData = tempLists.get(i);
                if (tempData.getField().equals("mt_custname")) {
                    mt_custname = i + 1;
                }
                if (tempData.getField().equals("mt_row")) {
                    mtd_row = i + 1;
                }
                if (tempData.getField().equals("mt_text2")) {
                    mt_text2 = i + 1;
                }
                if (tempData.getField().equals("mtd_proname")) {
                    mtd_proname = i + 1;
                }
                if (tempData.getField().equals("mtd_remark")) {
                    mtd_remark = i + 1;
                }
                if (tempData.getField().equals("mtd_unit")) {
                    mtd_unit = i + 1;
                }
            }
            String td_proname = null;
            String td_remark = null;
            String td_unit = null;
            String td_custname = null;
            String td_text2 = null;
            String td_row = null;
            if (tdata.getField().equals("mtd_procode")) {
                td_proname = jdata.getString("pr_spec");
                td_remark = jdata.getString("pr_detail");
                td_unit = jdata.getString("pr_unit");
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(mtd_proname - 1).setValue(td_proname);
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(mtd_remark - 1).setValue(td_remark);
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(mtd_unit - 1).setValue(td_unit);
            }
            if (tdata.getField().equals("mt_custcode")) {
                td_custname = jdata.getString("cu_name");
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(mt_custname - 1).setValue(td_custname);
            }
            if (tdata.getField().equals("mt_text1")) {
                td_text2 = jdata.getString("cu_servicename");
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(mt_text2 - 1).setValue(td_text2);
            }
            if (tdata.getField().equals("mt_ckcode")) {
                td_row = jdata.getString("sod_row");
                mAdapter.getmListData().get(vaules.getGroupId()).getDatas().get(mtd_row - 1).setValue(td_row);
            }
        }
    }


    /**
     * @desc:添加
     * @author：Arison on 2016/11/16
     */
    public void addGridItemData() {
        LogUtil.d(JSON.toJSONString(grids));
        if (!ListUtils.isEmpty(grids)) {//存在明细表
//            if (formid != 0) {
//                if ("Ask4Leave".equals(caller)) {
//                    formidkey = "va_id";
//                } else if ("SpeAttendance".equals(caller)) {
//                    formidkey = "sa_id";
//                } else if ("Workovertime".equals(caller)) {
//                    formidkey = "wo_id";
//                    detailkey_id = "wod_id";
//                    detailkey_did = "wod_woid";
//                } else if ("FeePlease!CCSQ".equals(caller)) {
//                    formidkey = "fp_id";
//                    detailkey_id = "fpd_id";
//                    detailkey_did = "fpd_fpid";
//                }
//            }
            GroupData groupData = new GroupData();//分组
            ArrayList<Data> itemData = new ArrayList<>();
            //id可能会被删除，所以不能简单取mDatas.size()
            int groupId = 0;
            if (mDatas.size() >= 2) {
                groupId = mDatas.get(mDatas.size() - 1).getId() + 1;//获取最后一组的id 然后累进加一
            } else {
                groupId = mDatas.size();
            }
            String key_id = "";
            String key_did = "";
            Integer groupid;
            for (int i = 0; i < grids.size(); i++) {//弄掉循环
                JSONObject items = grids.getJSONObject(i);
                Data data = new Data();
                if (detailkey_did.equals(items.getString("dg_field"))) {
                    //关联id
                    key_did = items.getString("dg_value");
                }
                if (d_startDate.equals(items.getString("dg_field")) ||
                        d_endDate.equals(items.getString("dg_field"))) {
                    if (StringUtil.isEmpty(items.getString("dg_value"))) {
                        data.setValue(DateFormatUtil.getStrDate4Date(new Date(), "yyyy-MM-dd HH:mm:ss"));
                    } else {
                        data.setValue(items.getString("dg_value"));
                    }
                }
                data.setName(items.getString("dg_caption"));
                data.setField(items.getString("dg_field"));
                data.setDetno(items.getIntValue("dg_sequence"));
                data.setFdid(items.getIntValue("gd_id"));
                data.setReadonly(items.getString("dg_logictype"));
                data.setIsDefault(items.getIntValue("mdg_isdefault"));
                data.setIsNeed(items.getIntValue("mdg_isdefault"));
                data.setType(items.getString("dg_type"));
                data.setMaxlength(String.valueOf(items.getIntValue("dg_maxlength")));
                LogUtil.d("新增分组id:" + (groupId + (formGroupSize - 1)));
                //data.setGroupId(groupId+(formGroupSize-1));//设置组id
                data.setGroupId(groupId + (formGroupSize - 1));//设置组id
                groupid = items.getInteger("dg_group");
                if (formid == 0) {
                    //录入
                    if (data.getIsNeed() == -1) {
                        data.setDetailDid(key_did);//更新时候，只添加关联id
                        data.setGroup("明细项" + (mDatas.size() - (formGroupSize - 1)));
                        itemData.add(data);
                    }
                } else {
                    //更新  只取固定组名  1
                    if (groupid == 1) {
                        if (data.getIsNeed() == -1) {
                            data.setDetailDid(String.valueOf(formid));//更新时候，只添加关联id---主表ID
                            data.setGroup("明细项" + (mDatas.size() - (formGroupSize - 1)));
                            data.setDetailId("");
                            itemData.add(data);
                        }
                    }

                }


            }
            groupData.setDatas(itemData);
            groupData.setId(groupId);
            groupData.setName("明细项" + groupId);
            LogUtil.d("添加id:" + groupData.getId() + "");
            mDatas.add(groupData);
            if (mAdapter == null) {
                mAdapter = new DataAdapter(mContext, mDatas);
                lv_datas.setAdapter(mAdapter);
            } else {
                //空处理
                mAdapter.setmListData(mDatas);
                mAdapter.notifyDataSetChanged();
            }
        }

    }

    /**
     * @desc:删除
     * @author：Arison on 2016/11/16
     */
    public void deleteGridItemData(int groupId, String detailId) {
        LogUtil.d("delete group:" + groupId);
        if (!ListUtils.isEmpty(mDatas)) {
            int deteId = 0;
            boolean hasNext = false;
            for (int i = 0; i < mDatas.size(); i++) {
                if (groupId == mDatas.get(i).getId()) {
                    deteId = i;
                    hasNext = true;
                }
                //删除某项，自动补数处理
                if (hasNext) {
                    mDatas.get(i).setName("明细项" + (mDatas.get(i).getId() - 1));
                    mDatas.get(i).setId(mDatas.get(i).getId() - 1);
                }

            }
            //判断明细项是否有id，没有id不执行网络请求
            if (!StringUtil.isEmpty(detailId)) {
                deteItemRequest(detailId);
            }


            mDatas.remove(deteId);
            mAdapter.setmListData(mDatas);
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * @desc:删除明细项请求
     * @author：Arison on 2017/2/21
     */
    public void deteItemRequest(String id) {
        progressDialog.show();
        String url = CommonUtil.getAppBaseUrl(ct) + "common/deleteDetail.action";
        //caller=MeetingRoom&gridcaller=MeetingRoom&condition=eq_id=16545
        Map<String, Object> params = new HashMap<>();
        params.put("caller", caller);
        params.put("condition", detailkey_id + "=" + id);
        params.put("gridcaller", caller);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x15, null, null, "post");
    }

    /**
     * @desc:提交操作---数据校验
     * @author：Arison on 2016/11/17
     */
    public void commit() {
        mPhotoIds = new StringBuilder("");
        mUploadImgCount = 0;
        for (int i = 0; i < mPhotoList.size(); i++) {
            String path = mPhotoList.get(i);
            uploadFile(path, i);
        }
//        commitForm();
    }

    private void commitForm() {
        StringBuffer forStore = new StringBuffer("{");
        StringBuffer gridStore = new StringBuffer("[");
        mDatas = mAdapter.getmListData();
        //判断是否有明细项
        LogUtil.d(JSON.toJSONString(mAdapter.getmListData()));
        if (groupData != null) {//存在明细
            ArrayList<Data> items = new ArrayList<>();
            for (int i = 0; i < formGroupSize; i++) {//存在主表多个分组
                items.addAll(mAdapter.getmListData().get(i).getDatas());
            }
            if (!ListUtils.isEmpty(items)) {
                String start = "";
                String startName = "";
                String end = "";
                for (int i = 0; i < items.size(); i++) {
                    Data item = items.get(i);
                    String key = item.getField();
                    String value = item.getValue();
                    String disValue = item.getDisplayValue();
                    if (!StringUtil.isEmpty(disValue)) value = disValue;//下拉字段
                    if (StringUtil.isEmpty(value)) value = "";
                    if (("F".equals(item.getReadonly()) && StringUtil.isEmpty(value))
                            || ("necessaryField".equals(item.getReadonly()) && StringUtil.isEmpty(value))) {
                        progressDialog.dismiss();
                        ToastMessage(item.getName() + "不能为空！");
                        return;
                    }


                    //日期格式判断
                    if (!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)) {
                        if (key.equals(startDate)) {
                            start = value;
                            startName = item.getName();
                        }
                        if (key.equals(endDate)) {
                            end = value;
                            //判断日期
                            if (!StringUtil.isEmpty(start) && !StringUtil.isEmpty(end)) {
                                if (end.compareTo(start) <= 0) {
                                    progressDialog.dismiss();
                                    ToastMessage(item.getName() + "不能小于或等于" + startName);
                                    return;
                                }
                            }

                        }
                    } else {
                        LogUtil.d("startDate:" + startDate + "endDate:" + endDate);
                    }

                    if (i == items.size() - 1) {
                        if (caller.equals("FeePlease!CCSQ")) {
                            forStore.append("\"" + "fp_auditdate" + "\":\"" + "" + "\",");
                        }
                        forStore.append("\"" + key + "\":\"" + value + "\"}");
                    } else {
                        forStore.append("\"" + key + "\":\"" + value + "\",");
                    }


                }
            }
            LogUtil.d("forStore:" + forStore.toString());
            //屏蔽第一项
            String d_start = "";
            String startName = "";
            String d_end = "";
            for (int i = formGroupSize; i < mAdapter.getmListData().size(); i++) {
                items = mAdapter.getmListData().get(i).getDatas();
                if (!ListUtils.isEmpty(items)) {
                    gridStore.append("{");
                    for (int j = 0; j < items.size(); j++) {
                        Data item = items.get(j);
                        String key = item.getField();
                        String value = item.getValue();
                        String disValue = item.getDisplayValue();
                        if (!StringUtil.isEmpty(disValue)) value = disValue;//下拉字段
                        if (StringUtil.isEmpty(value)) value = "";
                        if (("F".equals(item.getReadonly()) && StringUtil.isEmpty(value))
                                || ("necessaryField".equals(item.getReadonly()) && StringUtil.isEmpty(value))) {
                            progressDialog.dismiss();
                            ToastMessage(item.getName() + "不能为空！");
                            return;
                        }

                        //日期格式判断
                        if (!StringUtil.isEmpty(d_startDate) && !StringUtil.isEmpty(d_endDate)) {
                            if (key.equals(d_startDate)) {
                                d_start = value;
                                startName = item.getName();
                            }
                            if (key.equals(d_endDate)) {
                                d_end = value;
                                //判断日期
                                if (!StringUtil.isEmpty(d_start) && !StringUtil.isEmpty(d_end)) {
                                    if (d_end.compareTo(d_start) <= 0) {
                                        progressDialog.dismiss();
                                        ToastMessage(item.getName() + "不能小于或等于" + startName);
                                        return;
                                    }
                                }

                            }
                        }


                        if (j == items.size() - 1) {
                            gridStore.append("\"" + key + "\":\"" + value + "\"");
                        } else {
                            gridStore.append("\"" + key + "\":\"" + value + "\",");
                        }

                    }
                    if (i == mAdapter.getmListData().size() - 1) {
                        gridStore.append("}");
                    } else {
                        gridStore.append("},");
                    }

                }
            }
            gridStore.append("]");
            LogUtil.d("girdStore:" + gridStore.toString());
            saveData(forStore.toString(), gridStore.toString());
        } else {
            ArrayList<Data> items = new ArrayList<>();
            for (int i = 0; i < formGroupSize; i++) {//存在多个主表分组
                items.addAll(mAdapter.getmListData().get(i).getDatas());
            }

            if (!ListUtils.isEmpty(items)) {
                String start = "";
                String startName = "";
                String end = "";
                for (int i = 0; i < items.size(); i++) {
                    Data item = items.get(i);
                    String key = item.getField();
                    String value = item.getValue();
                    String disValue = item.getDisplayValue();
                    String type = item.getType();
                    if (!StringUtil.isEmpty(disValue)) value = disValue;//下拉字段
                    if (StringUtil.isEmpty(value)) value = "";
                    if (("F".equals(item.getReadonly()) && StringUtil.isEmpty(value))
                            || ("necessaryField".equals(item.getReadonly()) && StringUtil.isEmpty(value))) {
                        progressDialog.dismiss();
                        ToastMessage(item.getName() + "不能为空！");
                        return;
                    }
                    //日期格式判断
                    if (!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)) {
                        if (key.equals(startDate)) {
                            start = value;
                            startName = item.getName();
                        }
                        if (key.equals(endDate)) {
                            end = value;
                            //判断日期
                            if (!StringUtil.isEmpty(start) && !StringUtil.isEmpty(end)) {
                                if (end.compareTo(start) <= 0) {
                                    ToastMessage(item.getName() + "不能小于或等于" + startName);
                                    return;
                                }
                            }

                        }
                    } else {
                        LogUtil.d("startDate:" + startDate + "endDate:" + endDate);
                    }

                    if ("FF".equals(type)) {
                        String photoIds = mPhotoIds.toString();
                        Log.d("ffids", "ffids=" + photoIds);
                        if (photoIds.length() >= 1) {
                            photoIds.substring(0, photoIds.length() - 1);
                        }
                        Log.d("ffids", "ffids=" + photoIds);
                        forStore.append("\"" + key + "\":\"" + photoIds + "\"}");
                    } else if (i == items.size() - 1) {
                        forStore.append("\"" + key + "\":\"" + value + "\"}");
                    } else {
                        forStore.append("\"" + key + "\":\"" + value + "\",");
                    }

                }
            } else {
                forStore.append("}");
            }
            LogUtil.d("forStore:" + forStore.toString());

            saveData(forStore.toString(), "{}");
        }
    }

    /**
     * @desc:保存提交
     * @author：Arison on 2016/11/21
     */
    public void saveData(String formStore, String gridStore) {
        if ("{}".equals(formStore) && "{}".equals(gridStore)) {
            ToastMessage("界面字段为空，不能提交！");
            return;
        }
        if ("[]".equals(gridStore)) {
            ToastMessage("单据明细必填字段缺失，请联系管理员！");
            if (!caller.equals("MainTain")) {
                return;
            }
        }
        if ("{}".equals(formStore) || "{".equals(formStore)) {
            ToastMessage("单据主表必填字段缺失，请联系管理员！");
            return;
        }
        progressDialog.show();
        click_btn.setEnabled(false);
        //  api/serve/save.action?data=?&operate_id=?&access_token=?
        String url = CommonUtil.getCityBaseUrl(ct) + mOperateUrl;
        Map<String, Object> params = new HashMap<>();
        params.put("data", formStore);
        params.put("operate_id", mOperateId);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", CommonUtil.getB2BUid(this));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, Constants.HTTP_SUCCESS_INIT, null, null, "post");
    }


    public void updata() {
        mDatas = mAdapter.getmListData();
        // Lg.d("提交操作：" + JSON.toJSONString(mDatas));
        StringBuffer forStore = new StringBuffer("{");
        StringBuffer gridStore = new StringBuffer("[");
        //判断是否有明细项
        if (groupData != null) {//存在明细
            ArrayList<Data> items = new ArrayList<>();
            for (int i = 0; i < formGroupSize; i++) {//存在多个主表分组
                items.addAll(mAdapter.getmListData().get(i).getDatas());
            }
            if (!ListUtils.isEmpty(items)) {
                String start = "";
                String startName = "";
                String end = "";
                for (int i = 0; i < items.size(); i++) {
                    Data item = items.get(i);
                    String key = item.getField();
                    String value = item.getValue();
                    if (StringUtil.isEmpty(value)) value = "";
                    if (("F".equals(item.getReadonly()) && StringUtil.isEmpty(value))
                            || ("necessaryField".equals(item.getReadonly()) && StringUtil.isEmpty(value))) {
                        ToastMessage(item.getName() + "不能为空！");
                        return;
                    }

                    //日期格式判断
                    if (!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)) {
                        if (key.equals(startDate)) {
                            start = value;
                            startName = item.getName();
                        }
                        if (key.equals(endDate)) {
                            end = value;
                            //判断日期
                            if (!StringUtil.isEmpty(start) && !StringUtil.isEmpty(end)) {
                                if (end.compareTo(start) <= 0) {
                                    ToastMessage(item.getName() + "不能小于或等于" + startName);
                                    return;
                                }
                            }

                        }
                    } else {
                        LogUtil.d("startDate:" + startDate + "endDate:" + endDate);
                    }

                    if (i == items.size() - 1) {
                        forStore.append("\"" + key + "\":\"" + value + "\",")
                                .append("\"" + formidkey + "\":\"" + formid + "\"}");
                    } else {
                        forStore.append("\"" + key + "\":\"" + value + "\",");
                    }

                }
            }
            LogUtil.prinlnLongMsg("update", "forStore:" + forStore.toString());
            //屏蔽第一项
            LogUtil.d("formGroupSize:" + formGroupSize);
            for (int i = formGroupSize; i < mAdapter.getmListData().size(); i++) {
                items = mAdapter.getmListData().get(i).getDatas();
                if (!ListUtils.isEmpty(items)) {
                    //屏蔽第一项
                    String d_start = "";
                    String startName = "";
                    String d_end = "";
                    gridStore.append("{");
                    String detailId = "";//明细表id
                    for (int j = 0; j < items.size(); j++) {
                        Data item = items.get(j);
                        String key = item.getField();
                        String value = item.getValue();
                        if (!StringUtil.isEmpty(item.getDetailId())) {
                            detailId = item.getDetailId();
                        }
                        String detailDid = String.valueOf(formid);
                        if (StringUtil.isEmpty(value)) value = "";
                        if (("F".equals(item.getReadonly()) && StringUtil.isEmpty(value))
                                || ("necessaryField".equals(item.getReadonly()) && StringUtil.isEmpty(value))) {
                            ToastMessage(item.getName() + "不能为空！");
                            return;
                        }

                        //日期格式判断
                        if (!StringUtil.isEmpty(d_startDate) && !StringUtil.isEmpty(d_endDate)) {
                            if (key.equals(d_startDate)) {
                                d_start = value;
                                startName = item.getName();
                            }
                            if (key.equals(d_endDate)) {
                                d_end = value;
                                //判断日期
                                if (!StringUtil.isEmpty(d_start) && !StringUtil.isEmpty(d_end)) {
                                    if (d_end.compareTo(d_start) <= 0) {
                                        ToastMessage(item.getName() + "不能小于或等于" + startName);
                                        return;
                                    }
                                }

                            }
                        } else {
                            LogUtil.d("d_startDate:" + d_startDate + "d_endDate:" + d_endDate);
                        }

                        if (j == items.size() - 1) {
                            gridStore.append("\"" + key + "\":\"" + value + "\",")
                                    .append("\"" + detailkey_id + "\":\"" + detailId + "\",")//主表id
                                    .append("\"" + detailkey_did + "\":\"" + detailDid + "\"");//明细表id
                        } else {
                            gridStore.append("\"" + key + "\":\"" + value + "\",");
                        }

                    }
                    if (i == mAdapter.getmListData().size() - 1) {
                        gridStore.append("}");
                    } else {
                        gridStore.append("},");
                    }

                }
            }
            gridStore.append("]");
            LogUtil.prinlnLongMsg("update", "girdStore:" + gridStore.toString());
            updateData(forStore.toString(), gridStore.toString());
        } else {
            ArrayList<Data> items = new ArrayList<>();
            for (int i = 0; i < formGroupSize; i++) {//存在多个主表分组
                items.addAll(mAdapter.getmListData().get(i).getDatas());
            }
            if (!ListUtils.isEmpty(items)) {
                String start = "";
                String startName = "";
                String end = "";
                for (int i = 0; i < items.size(); i++) {
                    Data item = items.get(i);
                    String key = item.getField();
                    String value = item.getValue();
                    if (StringUtil.isEmpty(value)) value = "";
                    if (("F".equals(item.getReadonly()) && StringUtil.isEmpty(value))
                            || ("necessaryField".equals(item.getReadonly()) && StringUtil.isEmpty(value))) {
                        ToastMessage(item.getName() + "不能为空！");
                        return;
                    }
                    //日期格式判断
                    if (!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)) {
                        if (key.equals(startDate)) {
                            start = value;
                            startName = item.getName();
                        }
                        if (key.equals(endDate)) {
                            end = value;
                            //判断日期
                            if (!StringUtil.isEmpty(start) && !StringUtil.isEmpty(end)) {
                                if (end.compareTo(start) <= 0) {
                                    ToastMessage(item.getName() + "不能小于或等于" + startName);
                                    return;
                                }
                            }

                        }
                    } else {
                        LogUtil.d("startDate:" + startDate + "endDate:" + endDate);
                    }
                    if (i == items.size() - 1) {
                        forStore.append("\"" + key + "\":\"" + value + "\",")
                                .append("\"" + formidkey + "\":\"" + formid + "\"}");
                    } else {
                        forStore.append("\"" + key + "\":\"" + value + "\",");
                    }

                }
            }
            LogUtil.prinlnLongMsg("update", "forStore:" + forStore.toString());

            updateData(forStore.toString(), "[]");
        }


    }

    /**
     * @desc:更新接口
     * @author：Arison on 2016/11/22
     */
    public void updateData(String formStore, String gridStore) {
        if ("{}".equals(formStore) && "{}".equals(gridStore)) {
            ToastMessage("界面字段为空，不能提交！");
            return;
        }

        if ("{}".equals(formStore) || "{".equals(formStore)) {
            ToastMessage("单据主表必填字段缺失，请联系管理员！");
            return;
        }
        progressDialog.show();
        btn_update.setEnabled(false);

        String url = null;
        Map<String, Object> params = new HashMap<>();
        if ("ExtraWork$".equals(caller)) {
            url = CommonUtil.getAppBaseUrl(ct) + "/mobile/oa/ExtraWorkUpdateAndSubmit.action";
            params.put("id", String.valueOf(formid));
        } else {
            url = CommonUtil.getAppBaseUrl(ct) + "/mobile/commonUpdate.action";
            params.put("keyid", String.valueOf(formid));
        }
        params.put("caller", caller);
        params.put("formStore", formStore);
        params.put("gridStore", gridStore);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(ct, "sessionId"));
        ViewUtil.httpSendRequest(ct, url, params, mHandler, headers, 0x21, null, null, "post");
    }


    /**
     * 监听Back键按下事件,方法2:
     * 注意:
     * 返回值表示:是否能完全处理该事件
     * 在此处返回false,所以会继续传播该事件.
     * 在具体项目中此处的返回值视情况而定.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if ("在录入".equals(status)) {
                //不刷新列表
                FormListSelectActivity.reload = false;
                return super.onKeyDown(keyCode, event);
            } else if ("已提交".equals(status)) {
                //刷新列表---必须要刷新列表
                FormListSelectActivity.reload = true;
                return super.onKeyDown(keyCode, event);
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }


    private void judgeApprovers(int id) {
        LogUtil.d("id:" + id + "  caller:" + caller);
        String url = CommonUtil.getCityBaseUrl(mContext) + "api/serve/getMultiNodeAssigns.action";
        Map<String, Object> param = new HashMap<>();
        param.put("caller", "RepairApply");
        param.put("id", id);
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", CommonUtil.getB2BUid(mContext));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, 0x14, null, null, "post");
    }

    private String noid;

    //提交动作，增加判断节点是否有多人的情况
    private void selectApprovers(String emName) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                click_btn.setEnabled(false);
            }
        }, 50);
        String url = CommonUtil.getAppBaseUrl(mContext) + "api/serve/takeOverTask.action";
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", emName);
        params.put("nodeId", noid);
        param.put("_noc", 1);
        param.put("params", JSONUtil.map2JSON(params));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", CommonUtil.getB2BUid(mContext));
        ViewUtil.httpSendRequest(ct, url, param, mHandler, headers, 0x16, null, null, "post");
    }

    /**
     * @param noid
     * @param data
     */
    private void sendToSelect(String noid, JSONArray data) {
        this.noid = noid;
        ArrayList<SelectBean> beans = new ArrayList<>();
        SelectBean bean = null;
        for (int i = 0; i < data.size(); i++) {
            bean = new SelectBean();
            bean.setName(data.getString(i));
            bean.setClick(false);
            beans.add(bean);
        }
        Intent intent = new Intent(ct, SelectActivity.class);
        intent.putExtra("type", 2);
        intent.putParcelableArrayListExtra("data", beans);
        intent.putExtra("title", "选择审批人");
        startActivityForResult(intent, 0x22);
    }

    private void getEmnameByReturn(String text) {
        if (StringUtil.isEmpty(text)) return;
        Pattern pattern = Pattern.compile("(?<=\\()(.+?)(?=\\))");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String name = matcher.group();
            if (!StringUtil.isEmpty(name)) {
                selectApprovers(name);
                return;
            }
        }
//        if (!matcher.find()){
//            selectApprovers(text);
//        }
    }


    private void showSelectPictureDialog() {
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
//                            selectPhoto();
                            Intent intent = new Intent();
                            intent.putExtra("MAX_SIZE", 9);
                            intent.putExtra("CURRENT_SIZE", mPhotoList == null ? 0 : mPhotoList.size());
                            intent.setClass(ct, ImgFileListActivity.class);
                            startActivityForResult(intent, 0x01);
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private static final int REQUEST_CODE_CAPTURE_PHOTO = 1;// 拍照
    private static final int REQUEST_CODE_PICK_PHOTO = 2;// 图库
    private static Uri mNewPhotoUri;// 拍照和图库 获得图片的URI
    ComPictureAdapter cAdapter;

    private void takePhoto() throws Exception {
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(mContext, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
        LogUtil.d("uri:" + mNewPhotoUri);
        if (mNewPhotoUri != null) {
            CameraUtil.captureImage(CommonDataFormActivity.this,
                    mNewPhotoUri,
                    REQUEST_CODE_CAPTURE_PHOTO);
        } else {
            ToastUtil.showToast(this, "uri is null");
        }
    }

    private void doImageFiltering(ArrayList<String> mPhotoList) {
        for (int i = 0; i < mPhotoList.size(); i++) {
            File file = new File(mPhotoList.get(i).toString());
            if (!file.isFile()) {
//                mPhotoList.remove(i);
                Toast.makeText(ct, "第" + (i + 1) + "张图片格式不对，可能会上传失败，建议更换", Toast.LENGTH_LONG).show();
            }
            if (i == mPhotoList.size() - 1) {
                mAdapter.notifyDataSetInvalidated();
            }
        }
    }

    private Boolean platform = ApiUtils.getApiModel() instanceof ApiPlatform;

    //上传文件
    private void uploadFile(String path, final int position) {
//        String path = mPhotoList.get(0).toString();
        File waterBitmapToFile = new File(path);
        File file = ImageUtil.compressBitmapToFile(path, 100, 360, 480);
        RequestParams params = new RequestParams();
//        params.addQueryStringParameter("master", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "erp_master"));
//        params.addHeader("Cookie", "JSESSIONID=" + ApiConfig.getInstance(ApiUtils.getApiModel()).getmApiBase().getCookie());
        params.addHeader("Cookie", CommonUtil.getB2BUid(this));
//        params.addBodyParameter("em_code", CommonUtil.getSharedPreferences(MyApplication.getInstance().getApplicationContext(), "b2b_uu"));
//        params.addBodyParameter("sessionId", sessionId);
//        params.addBodyParameter("sessionUser", "U0807");
//        params.addBodyParameter("access_token", "12321");
//        params.addBodyParameter("client_type", "cc");
//        params.addBodyParameter("type", "common");
        params.addBodyParameter("file", file == null ? waterBitmapToFile : file);
        Log.d("uploadfile", params.toString());
        String url = CommonUtil.getCityBaseUrl(ct) + "api/serve/uploadAttach.action";
        final HttpUtils http = new HttpUtils();
        http.send(HttpRequest.HttpMethod.POST, url, params, new RequestCallBack<String>() {
            @Override
            public void onStart() {
                progressDialog.show();
                ViewUtil.ToastMessage(ct, getString(R.string.sending_picture) + "...");
            }

            @Override
            public void onLoading(long total, long current, boolean isUploading) {
                if (isUploading) {

                } else {

                }
            }

            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                mUploadImgCount++;
                String result = responseInfo.result;
                Log.d("updateresponseInfo", result);
                if (JSONUtil.validate(result) && JSON.parseObject(result).getBoolean("success")) {
                    JSONObject resultObject = JSON.parseObject(result);
                    String phontId = JSONUtil.getText(resultObject, "id");
                    mPhotoIds.append(phontId + ";");
                }
                if (mUploadImgCount == mPhotoList.size()) {
                    //上传操作
                    commitForm();
                }
            }


            @Override
            public void onFailure(HttpException error, String msg) {
                mUploadImgCount++;
                ViewUtil.ToastMessage(ct, getString(R.string.common_save_failed) + msg);
                progressDialog.dismiss();
            }
        });

    }


    private void showPictureActionDialog(final int position) {

//        ToastMessage("删除的位置是：position:"+position);
        String[] items = new String[]{getString(R.string.look_over), getString(R.string.common_delete)};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this).setTitle(R.string.pictures)
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {// 查看
                            Intent intent = new Intent(CommonDataFormActivity.this, MultiImagePreviewActivity.class);
                            intent.putExtra(AppConstant.EXTRA_IMAGES, mPhotoList);
                            intent.putExtra(AppConstant.EXTRA_POSITION, position);
                            intent.putExtra(AppConstant.EXTRA_CHANGE_SELECTED, false);
                            startActivity(intent);
                        } else {// 删除
                            try {
                                cAdapter.getmPhotoList().remove(position);
                                cAdapter.notifyDataSetChanged();
                                mAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

}

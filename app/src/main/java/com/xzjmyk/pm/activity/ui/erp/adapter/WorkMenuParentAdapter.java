package com.xzjmyk.pm.activity.ui.erp.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.common.LogUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUAS;
import com.core.api.wxapi.ApiUtils;
import com.core.app.AppConfig;
import com.core.app.Constants;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.view.MyGridView;
import com.uas.appworks.model.bean.WorkMenuBean;
import com.xzjmyk.pm.activity.R;

import java.util.ArrayList;
import java.util.List;

public class WorkMenuParentAdapter extends BaseAdapter {
    private List<WorkMenuBean> objects = new ArrayList<WorkMenuBean>();
    private Context context;
    private LayoutInflater layoutInflater;
    private WorkMenuChildAdapter mWorkMenuChildAdapter;
    private OnAddFuncClickListener mOnAddFuncClickListener;
    private Resources mResources;
    private boolean isB2b = false;

    public WorkMenuParentAdapter(Context context, List<WorkMenuBean> objects) {
        this.context = context;
        this.objects = objects;
        this.layoutInflater = LayoutInflater.from(context);
        isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
        mResources = context.getResources();
    }

    public void setOnAddFuncClickListener(OnAddFuncClickListener onAddFuncClickListener) {
        mOnAddFuncClickListener = onAddFuncClickListener;
    }

    public List<WorkMenuBean> getObjects() {
        return objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public WorkMenuBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_work_parent_layout, null);
            convertView.setTag(new ViewHolder(convertView));
        }
        initializeViews((WorkMenuBean) getItem(position), (ViewHolder) convertView.getTag());
        return convertView;
    }

    private void initializeViews(WorkMenuBean object, ViewHolder holder) {
        if (object.isLocalModule()) {
            try {
                holder.workParentTitleTv.setText(mResources.getIdentifier(object.getModuleName(), "string", context.getPackageName()));
            } catch (Exception e) {
                holder.workParentTitleTv.setText(object.getModuleName());
            }
        } else {
            holder.workParentTitleTv.setText(object.getModuleName());
        }
        List<WorkMenuBean.ModuleListBean> typeList = object.getModuleList();
        List<WorkMenuBean.ModuleListBean> typeListBeen = new ArrayList<>();
        for (int i = 0; i < typeList.size(); i++) {
            WorkMenuBean.ModuleListBean moduleListBean = typeList.get(i);
            if (!moduleListBean.isHide()) {
                typeListBeen.add(moduleListBean);
            }
        }
        if (typeListBeen.size() == 0) {
            WorkMenuBean.ModuleListBean moduleListBean = new WorkMenuBean.ModuleListBean();
            moduleListBean.setIsHide(false);
            moduleListBean.setIsLocalMenu(true);
            moduleListBean.setMenuActivity("com.modular.work.WorkFuncSetActivity");
            moduleListBean.setMenuTag(object.getModuleTag());
            moduleListBean.setMenuIcon("add_picture");
            moduleListBean.setMenuName("str_work_add_func");
            moduleListBean.setMenuUrl("");

            typeListBeen.add(moduleListBean);
        }
        mWorkMenuChildAdapter = new WorkMenuChildAdapter(context, typeListBeen);
        holder.workParentGv.setAdapter(mWorkMenuChildAdapter);

        holder.workParentGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String menuActivity = typeListBeen.get(position).getMenuActivity();
                    String menuIcon = typeListBeen.get(position).getMenuIcon();
                    String caller = typeListBeen.get(position).getCaller();

                    LogUtil.d("workMenuActivity", menuActivity);
                    if ("com.modular.work.WorkFuncSetActivity".equals(menuActivity)) {
                        if (mOnAddFuncClickListener != null) {
                            mOnAddFuncClickListener.onAddFuncClick(view, position);
                        }
                    } else {
                        Intent intent = getWorkIntent(typeListBeen.get(position));
                        if (intent != null) {
                            context.startActivity(intent);
                        }
                    }
                } catch (Exception e) {
                    Log.e("workException", e.getMessage());
                    ToastUtil.showToast(context, "该功能正在完善中");
                }
            }
        });
    }

    private static final String TAG = "WorkMenuParentAdapter";

    private Intent getWorkIntent(WorkMenuBean.ModuleListBean moduleListBean) {
        String menuActivity = moduleListBean.getMenuActivity();
        String menuIcon = moduleListBean.getMenuIcon();
        String caller = moduleListBean.getCaller();

        Intent intent = new Intent(menuActivity);

        switch (menuActivity) {
            case "com.modular.work.WorkActivity":
                boolean isAdmin = PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false);
                //上传管理员状态
                intent.putExtra(AppConfig.IS_ADMIN, isAdmin);
                break;
            case "com.modular.work.MissionActivity":
                boolean is = PreferenceUtils.getBoolean(AppConfig.AUTO_MISSION, false);
                if (isB2b || is) {
                    intent = new Intent("com.modular.work.MissionActivity");
                    intent.putExtra("flag", 1);
                } else {
                    intent = new Intent("com.modualr.appworks.OutofficeActivity");
                }
                isAdmin = PreferenceUtils.getBoolean(AppConfig.IS_ADMIN, false);
                //上传管理员状态
                intent.putExtra(AppConfig.IS_ADMIN, isAdmin);
                break;
            case "com.modular.form.TravelDataFormDetailActivity":
                if (ApiUtils.getApiModel() instanceof ApiUAS) {
                    String travelCaller = CommonUtil.getSharedPreferences(context, Constants.WORK_TRAVEL_CALLER_CACHE);
                    if (!TextUtils.isEmpty(travelCaller) && "FeePlease!CCSQ!new".equals(travelCaller)) {
                        intent.putExtra("caller", travelCaller);
                        break;
                    }
                }
                intent.setAction("com.modular.form.DataFormDetailActivity");
            case "com.modular.form.DataFormDetailActivity":
                if ("ic_work_special_attendance".equals(menuIcon)) {
                    intent.putExtra("caller", "SpeAttendance");
                } else if ("ic_work_overtime_request".equals(menuIcon)) {
                    if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                        intent = new Intent("com.modular.plat.WorkExtraActivity");
                    } else if (ApiUtils.getApiModel() instanceof ApiUAS) {
                        String overtimeCaller = CommonUtil.getSharedPreferences(context, Constants.WORK_OVERTIME_CALLER_CACHE);
                        if (StringUtil.isEmpty(overtimeCaller)) {
                            intent.putExtra("caller", "Workovertime");
                        } else {
                            intent.putExtra("caller", overtimeCaller);
                        }
                    }
                } else if ("ic_work_leave_request".equals(menuIcon)) {
                    if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                        intent = new Intent("com.modular.oa.PlatLeaveAddActivity");
                    } else if (ApiUtils.getApiModel() instanceof ApiUAS) {
                        intent.putExtra("caller", "Ask4Leave");
                    }
                } else if ("ic_work_travel_request".equals(menuIcon)) {
                    if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                        intent = new Intent("com.modular.plat.TravelActivity");
                    } else if (ApiUtils.getApiModel() instanceof ApiUAS) {
                        String travelCaller = CommonUtil.getSharedPreferences(context, Constants.WORK_TRAVEL_CALLER_CACHE);
                        if (StringUtil.isEmpty(travelCaller)) {
                            intent.putExtra("caller", "FeePlease!CCSQ!new");
                        } else {
                            intent.putExtra("caller", travelCaller);
                        }
                    }
                } else {
                    if ("ProdInOut!Sale".equals(caller)){
                        intent = new Intent("com.modular.work.OA.erp.activity.form.FormListSelectActivity");
                    }
                    intent.putExtra("caller", caller);
                    String title = null;
                    if (moduleListBean.isLocalMenu()) {
                        try {
                            title = context.getString(mResources.getIdentifier(moduleListBean.getMenuName(), "string", context.getPackageName()));
                        } catch (Exception e) {
                            title = moduleListBean.getMenuName();
                        }
                    } else {
                        title = moduleListBean.getMenuName();
                    }
                    intent.putExtra("title", title);
                }
                break;

            case "com.modular.oa.StatisticsActivity":
                PreferenceUtils.putInt(Constants.NEW_FUNCTION_NOTICE, 1);
                break;
            case "com.modular.oa.ExpenseReimbursementActivity":
                PreferenceUtils.putInt(Constants.NEW_EXPENSE_REIMBURSEMENT_NOTICE, 1);
                break;
            case "com.modular.work.OAActivity":
                if ("ic_work_customer_visit".equals(menuIcon)) {
                    intent.putExtra("type", 1);
                }
                break;
            case "com.modular.apputils.activity.SimpleWebActivity":
                //  case "com.modular.main.WebViewCommActivity":
                if ("ic_work_b2b_commerce".equals(menuIcon)) {
                    String phone = com.xzjmyk.pm.activity.util.oa.CommonUtil.getSharedPreferences(context, "user_phone");
                    String password = com.xzjmyk.pm.activity.util.oa.CommonUtil.getSharedPreferences(context, "user_password");
                    String b_enuu = com.xzjmyk.pm.activity.util.oa.CommonUtil.getSharedPreferences(context, "erp_uu");
                    String url = "";
                    if (StringUtil.isEmpty(b_enuu)) {
                        url = "http://b2b.usoftchina.com/authen?b_username=" + phone + "&b_password=" + password;
                    } else {
                        url = "http://b2b.usoftchina.com/authen?b_username=" + phone + "&b_password=" + password + "&b_enuu=" + b_enuu;
                    }
                    intent.putExtra("url", url);
                    intent.putExtra("p", context.getString(R.string.work_business_me));
                    intent.putExtra("cookie", true);
                } else if ("ic_work_usoft_mall".equals(menuIcon)) {
                    String accountToken = CommonUtil.getAccountToken(context);
                    String enuu = CommonUtil.getEnuu(context);
                    String url = Constants.ACCOUNT_CENTER_HOST +
                            "agency?token=" + accountToken + "&appId=mall&spaceUU=" + enuu
                            + "&returnURL=https://mall.usoftchina.com/"
                            + "&baseURL=https://mall.usoftchina.com/newLogin/other";
                    intent.putExtra("url", url);
                    intent.putExtra("p", context.getString(R.string.work_business_find));
                    intent.putExtra("cookie", true);
                } else if ("ic_work_uu_education".equals(menuIcon)) {
                    String url = "http://www.i-ronge.com/?referee=uuhl666666";
                    intent.putExtra("url", url);
                    intent.putExtra("p", context.getString(R.string.str_work_uu_education));
                }
                break;
            case "com.modular.work.CommonDataFormActivity":
                intent.putExtra("serve_id", caller);
                String title = null;
                if (moduleListBean.isLocalMenu()) {
                    try {
                        title = context.getString(mResources.getIdentifier(moduleListBean.getMenuName(), "string", context.getPackageName()));
                    } catch (Exception e) {
                        title = moduleListBean.getMenuName();
                    }
                } else {
                    title = moduleListBean.getMenuName();
                }
                intent.putExtra("title", title);
                break;
            case "com.modular.crm.ClientActivity":
                if ("ZT".equals(CommonUtil.getMaster())) {
                    ToastUtil.showToast(context, "应贵公司要求，暂不开放");
                    return null;
                }
                break;
            default:
                break;
        }
        return intent;
    }

    protected class ViewHolder {
        private TextView workParentTitleTv;
        private MyGridView workParentGv;

        public ViewHolder(View view) {
            workParentTitleTv = (TextView) view.findViewById(R.id.work_parent_title_tv);
            workParentGv = (MyGridView) view.findViewById(R.id.work_parent_gv);
        }
    }

    public interface OnAddFuncClickListener {
        void onAddFuncClick(View view, int position);
    }
}

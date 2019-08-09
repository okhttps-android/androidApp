package com.xzjmyk.pm.activity.ui.erp.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.fragment.BaseMVPFragment;
import com.core.net.http.http.OAHttpHelper;
import com.core.utils.StatusBarUtil;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnMultiPurposeListener;
import com.uas.appworks.model.bean.WorkMenuBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.uas.appworks.view.WorkPlatView;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.adapter.WorkMenuParentAdapter;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 工作台主页面
 * @date 2017/11/12 16:45
 */

public class WorkPlatFragment extends BaseMVPFragment<WorkPlatPresenter> implements WorkPlatView {
    private final int LOAD_ORDERS_CALLER = 0x02;
    private final int OBTAIN_APP_CITY_INDUSTRY_SERVICE = 0x04;

    private ListView mWorkMenuListView;
    private List<WorkMenuBean> mWorkMenuBeans;
    private WorkMenuParentAdapter mWorkMenuParentAdapter;
    private boolean isB2b;
    private TextView mSetTextView, mMasterTextView;
    private List<WorkMenuBean> mCacheMenuTypeBeans;
    private String mUserId, mCompanyName, mMaster;
    private Resources mResources;
    private RefreshLayout mRefreshLayout;
    private ImageView mHeaderImageView, mFuncImageView, mHeaderWordImageView;
    private TextView mHeaderTimeTextView;
    private Toolbar mToolbar;
    private RelativeLayout mHeaderRelativeLayout;
    private int mHeaderHeight;

    @Override
    protected WorkPlatPresenter initPresenter() {
        return new WorkPlatPresenter();
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_work;
    }

    @Override
    protected void initViews() {
        if (isFirstLoad) {
            isB2b = ApiUtils.getApiModel() instanceof ApiPlatform;
            mWorkMenuListView = $(R.id.work_menu_lv);
            mRefreshLayout = $(R.id.fragment_work_refreshLayout);
            mHeaderImageView = $(R.id.work_plat_main_header_iv);
            mHeaderRelativeLayout = $(R.id.work_plat_main_header_rl);
            mHeaderWordImageView = $(R.id.work_plat_main_header_word_iv);
            mHeaderTimeTextView = $(R.id.work_plat_main_header_time_tv);
            mMasterTextView = $(R.id.work_plat_master_name_tv);
            mFuncImageView = $(R.id.work_plat_func_set_iv);
            mToolbar = $(R.id.work_plat_main_toolbar);
            StatusBarUtil.setPaddingSmart(mContext, mToolbar);

            ViewGroup.LayoutParams layoutParams = mHeaderRelativeLayout.getLayoutParams();
            mHeaderHeight = layoutParams.height;

            View setItem = LayoutInflater.from(mContext).inflate(R.layout.layout_work_plat_set, null);
            mSetTextView = (TextView) setItem.findViewById(R.id.work_plat_set_tv);
            mWorkMenuListView.addFooterView(setItem);

            mCacheMenuTypeBeans = new ArrayList<>();
            mWorkMenuBeans = new ArrayList<>();
            mWorkMenuParentAdapter = new WorkMenuParentAdapter(mContext, mWorkMenuBeans);
            mWorkMenuListView.setAdapter(mWorkMenuParentAdapter);
        }
        if (mResources == null) {
            mResources = mContext.getResources();
        }
    }

    @Override
    protected void initEvents() {
        if (mSetTextView != null) {
            mSetTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult("com.modular.work.WorkModuleSortActivity", Constants.WORK_MODULE_SORT_REQUEST);
                }
            });
        }

        if (mWorkMenuParentAdapter != null) {
            mWorkMenuParentAdapter.setOnAddFuncClickListener(new WorkMenuParentAdapter.OnAddFuncClickListener() {
                @Override
                public void onAddFuncClick(View view, int position) {
                    startActivityForResult("com.modular.work.WorkFuncSetActivity", Constants.WORK_FUNC_SET);
                }
            });
        }

        if (mFuncImageView != null) {
            mFuncImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivityForResult("com.modular.work.WorkFuncSetActivity", Constants.WORK_FUNC_SET);
                }
            });
        }

        if (mRefreshLayout != null) {
            mRefreshLayout.setOnMultiPurposeListener(new OnMultiPurposeListener() {
                @Override
                public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {

                }

                @Override
                public void onRefresh(RefreshLayout refreshlayout) {

                }

                @Override
                public void onLoadMore(RefreshLayout refreshlayout) {

                }

                @Override
                public void onHeaderPulling(RefreshHeader header, float percent, int offset, int headerHeight, int extendHeight) {
                    /*if (percent >= 1f) {
                        mHeaderTimeTextView.setVisibility(View.VISIBLE);
                    } else {
                        mHeaderTimeTextView.setVisibility(View.GONE);
                    }
                    if (percent >= 1.3f) {
                        mRefreshLayout.finishRefresh(0);
                        if (!CommonUtil.isRepeatClick()) {
                            Intent intent = new Intent();
                            intent.setClass(mContext, TimeHelperActivity.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.anim_activity_top_in, R.anim.anim_activity_bottom_out);
                        }
                    }*/
                    ViewGroup.LayoutParams layoutParams = mHeaderRelativeLayout.getLayoutParams();
                    layoutParams.height = (int) (mHeaderHeight * (1 + percent));
                    mHeaderRelativeLayout.setLayoutParams(layoutParams);

                    mHeaderWordImageView.setAlpha(percent > 1f ? 1f : percent);
                }

                @Override
                public void onHeaderReleasing(RefreshHeader header, float percent, int offset, int headerHeight, int extendHeight) {
                    /*if (percent >= 1f) {
                        mHeaderTimeTextView.setVisibility(View.VISIBLE);
                    } else {
                        mHeaderTimeTextView.setVisibility(View.GONE);
                    }*/
                    ViewGroup.LayoutParams layoutParams = mHeaderRelativeLayout.getLayoutParams();
                    layoutParams.height = (int) (mHeaderHeight * (1 + percent));
                    mHeaderRelativeLayout.setLayoutParams(layoutParams);

                    mHeaderWordImageView.setAlpha(percent > 1f ? 1f : percent);
                }

                @Override
                public void onHeaderStartAnimator(RefreshHeader header, int headerHeight, int extendHeight) {

                }

                @Override
                public void onHeaderFinish(RefreshHeader header, boolean success) {
                    mHeaderRelativeLayout.requestLayout();
                    mHeaderWordImageView.requestLayout();
                }

                @Override
                public void onHeaderReleased(RefreshHeader refreshHeader, int i, int i1) {

                }

                @Override
                public void onFooterReleased(RefreshFooter refreshFooter, int i, int i1) {

                }

                @Override
                public void onFooterPulling(RefreshFooter footer, float percent, int offset, int footerHeight, int extendHeight) {

                }

                @Override
                public void onFooterReleasing(RefreshFooter footer, float percent, int offset, int footerHeight, int extendHeight) {

                }

                @Override
                public void onFooterStartAnimator(RefreshFooter footer, int footerHeight, int extendHeight) {

                }

                @Override
                public void onFooterFinish(RefreshFooter footer, boolean success) {

                }
            });
        }

    }

    private Runnable uiRunnable = new Runnable() {
        @Override
        public void run() {
            doInUi();
        }
    };

    private void doInUi() {
        getLocalMenu();
        loadOrdersCaller();
        obtainCityIndustryService();
    }

    @Override
    protected void initDatas() {
        if (isFirstLoad) {
            doInUi();
        } else {
            OAHttpHelper.getInstance().postDelayed(uiRunnable, 250);
        }

    }

    private void getLocalMenu() {
        String role = com.core.utils.CommonUtil.getUserRole();
        String userId = MyApplication.getInstance().mLoginUser.getUserId();
        if (role.equals("1")) {
            //个人用户
            if (userId != null) {
                if (!userId.equals(mUserId)) {
                    mPresenter.uasRequest(mContext, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
                }
            } else {
                mPresenter.uasRequest(mContext, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
            }
            mUserId = userId;
            mCompanyName = "";
            mMaster = "";
            mMasterTextView.setText(getString(R.string.work_title));
        } else if (role.equals("3")) {
            //b2b用户
            String companyName = com.core.utils.CommonUtil.getSharedPreferences(mContext, "companyName");
            if (companyName != null && userId != null) {
                if ((!userId.equals(mUserId) || !companyName.equals(mCompanyName))) {
                    mPresenter.uasRequest(mContext, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
                }
            } else {
                mPresenter.uasRequest(mContext, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
            }
            mUserId = userId;
            mCompanyName = companyName;
            mMaster = "";
            mMasterTextView.setText(getString(R.string.work_title));
        } else if (role.equals("2")) {
            //ERP用户
            String erp_company = com.core.utils.CommonUtil.getSharedPreferences(mContext, "erp_commpany");
            String erp_master = com.core.utils.CommonUtil.getSharedPreferences(mContext, "erp_master");
            if (erp_company != null && erp_master != null && userId != null) {
                if ((!userId.equals(mUserId) || !erp_company.equals(mCompanyName) || !erp_master.equals(mMaster))) {
                    mPresenter.uasRequest(mContext, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
                }
            } else {
                mPresenter.uasRequest(mContext, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
            }
            mUserId = userId;
            mCompanyName = erp_company;
            mMaster = erp_master;
            String master_ch = CommonUtil.getSharedPreferences(mContext, "Master_ch");
            mMasterTextView.setText(TextUtils.isEmpty(master_ch) ? getString(R.string.work_title) : master_ch);
        }
    }

    private void obtainCityIndustryService() {
        Map<String, Object> params = new HashMap<>();
        params.put("kind", "app");
        params.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));

        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));

        HttpParams request = new HttpParams.Builder()
                .flag(OBTAIN_APP_CITY_INDUSTRY_SERVICE)
//                .url("api/serve/mainPage/getServices.action")
                .url("mobile/common/getServices.action")
                .setHeaders(header)
                .setParams(params)
                .method(Method.GET)
                .build();
        mPresenter.uasRequest(mContext, request);
    }

    public void setTitle(String title) {
        if (mMasterTextView != null) {
            mMasterTextView.setText(title);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.WORK_MODULE_SORT_REQUEST || resultCode == Constants.WORK_FUNC_SET) {
            mPresenter.uasRequest(mContext, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
        }
    }

    private void loadOrdersCaller() {
        if (isB2b) {
            return;
        }
        String url = "mobile/oa/getoaconifg.action";
        HttpParams request = new HttpParams.Builder()
                .url(url)
                .addParam("master", CommonUtil.getMaster())
                .addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(BaseConfig.getContext(), "sessionId"))
                .flag(LOAD_ORDERS_CALLER)
                .method(Method.GET)
                .build();
        mPresenter.uasRequest(mContext, request);
    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void requestSuccess(int what, Object object) {
        if (object != null) {
            Log.d("worksuccess", object.toString());
        }
        if (what == Constants.LOAD_WORK_MENU_CACHE) {
            mCacheMenuTypeBeans = (List<WorkMenuBean>) object;
            mWorkMenuBeans.clear();
            for (int i = 0; i < mCacheMenuTypeBeans.size(); i++) {
                WorkMenuBean workMenuBean = mCacheMenuTypeBeans.get(i);
                if (workMenuBean.isModuleVisible()) {
                    mWorkMenuBeans.add(workMenuBean);
                }

            }
            mWorkMenuParentAdapter.notifyDataSetChanged();
        } else if (what == LOAD_ORDERS_CALLER) {
            saveOrdersCaller(object);
        } else if (what == OBTAIN_APP_CITY_INDUSTRY_SERVICE) {
            String result = object.toString();
            if (JSONUtil.validate(result)) {
                analyzeCityIndustry(result);
            }
        }
    }

    private void analyzeCityIndustry(String result) {
        JSONObject resultObject = JSON.parseObject(result);
        JSONArray configArray = resultObject.getJSONArray("configs");
        if (configArray != null) {
            List<WorkMenuBean> netWorkMenuBeans = new ArrayList<>();
            for (int i = 0; i < configArray.size(); i++) {
                JSONObject configObject = configArray.getJSONObject(i);
                if (configObject != null) {
                    WorkMenuBean workMenuBean = new WorkMenuBean();
                    workMenuBean.setModuleVisible(true);
                    workMenuBean.setIsLocalModule(false);
                    workMenuBean.setModuleName(JSONUtil.getText(configObject, "st_name"));
                    // TODO: 2018/3/16 待修改
                    workMenuBean.setModuleTag(JSONUtil.getText(configObject, "st_tag"));
                    workMenuBean.setModuleId(JSONUtil.getText(configObject, "st_id"));

                    JSONArray serviceArray = configObject.getJSONArray("serves");
                    if (serviceArray != null && serviceArray.size() > 0) {
                        List<WorkMenuBean.ModuleListBean> moduleListBeans = new ArrayList<>();
                        for (int j = 0; j < serviceArray.size(); j++) {
                            JSONObject serviceObject = serviceArray.getJSONObject(j);
                            if (serviceObject != null) {
                                WorkMenuBean.ModuleListBean moduleListBean = new WorkMenuBean.ModuleListBean();
                                moduleListBean.setMenuUrl("");
                                moduleListBean.setMenuName(JSONUtil.getText(serviceObject, "sv_name"));
                                moduleListBean.setMenuActivity("com.modular.form.DataFormDetailActivity");
                                // TODO: 2018/3/16 待修改
                                moduleListBean.setMenuTag(JSONUtil.getText(serviceObject, "sv_tag"));
                                moduleListBean.setIsLocalMenu(false);
                                moduleListBean.setIsHide(false);
                                moduleListBean.setCaller(JSONUtil.getText(serviceObject, "sv_caller"));

                                JSONObject logourlObject = serviceObject.getJSONObject("sv_logourl");
                                if (logourlObject != null) {
                                    moduleListBean.setMenuIcon(JSONUtil.getText(logourlObject, "mobile"));
                                }
                                moduleListBeans.add(moduleListBean);
                            }
                        }
                        workMenuBean.setModuleList(moduleListBeans);

                        netWorkMenuBeans.add(workMenuBean);
                    }
                }
            }

            //循环遍历网络数据，更新本地的网络应用，添加新增的网络应用
            for (WorkMenuBean netWorkMenuBean : netWorkMenuBeans) {
                boolean isExist = false;
                String netModuleTag = netWorkMenuBean.getModuleTag();
                List<WorkMenuBean.ModuleListBean> netModuleList = netWorkMenuBean.getModuleList();
                for (WorkMenuBean cacheWorkMenuBean : mCacheMenuTypeBeans) {
                    String cacheModuleTag = cacheWorkMenuBean.getModuleTag();
                    List<WorkMenuBean.ModuleListBean> cacheModuleList = cacheWorkMenuBean.getModuleList();
                    //如果缓存数据中存在网络返回的模块，则根据网络数据更新缓存
                    if (cacheModuleTag.equals(netModuleTag)) {
                        isExist = true;
                        cacheWorkMenuBean.setModuleId(netWorkMenuBean.getModuleId());
                        cacheWorkMenuBean.setModuleTag(netWorkMenuBean.getModuleTag());
                        for (WorkMenuBean.ModuleListBean netModuleListBean : netModuleList) {
                            boolean isFuncExist = false;
                            String netMenuTag = netModuleListBean.getMenuTag();
                            for (WorkMenuBean.ModuleListBean cacheModuleListBean : cacheModuleList) {
                                String cacheMenuTag = cacheModuleListBean.getMenuTag();
                                if (cacheMenuTag.equals(netMenuTag)) {
                                    isFuncExist = true;
                                    if (!cacheModuleListBean.isLocalMenu()) {
                                        cacheModuleListBean.setCaller(netModuleListBean.getCaller());
                                        cacheModuleListBean.setMenuActivity(netModuleListBean.getMenuActivity());
                                        cacheModuleListBean.setMenuTag(netModuleListBean.getMenuTag());
                                        cacheModuleListBean.setMenuIcon(netModuleListBean.getMenuIcon());
                                        cacheModuleListBean.setMenuName(netModuleListBean.getMenuName());
                                        cacheModuleListBean.setMenuUrl(netModuleListBean.getMenuUrl());
                                    }
                                    break;
                                }
                            }
                            //如果缓存中不存在，说明是新增的网络引用，则直接添加到缓存中
                            if (!isFuncExist) {
                                cacheModuleList.add(netModuleListBean);
                            }
                        }

                        //循环缓存数据和网络数据，将缓存中多余的网络应用去掉
                        for (int i = cacheModuleList.size() - 1; i >= 0; i--) {
                            WorkMenuBean.ModuleListBean cacheModuleListBean = cacheModuleList.get(i);
                            if (!cacheModuleListBean.isLocalMenu()) {
                                boolean isCacheExist = false;
                                String cacheMenuTag = cacheModuleListBean.getMenuTag();
                                for (WorkMenuBean.ModuleListBean netModuleListBean : netModuleList) {
                                    String netMenuTag = netModuleListBean.getMenuTag();
                                    if (cacheMenuTag.equals(netMenuTag)) {
                                        isCacheExist = true;
                                        break;
                                    }
                                }
                                if (!isCacheExist) {
                                    cacheModuleList.remove(i);
                                }
                            }
                        }

                        break;
                    }
                }
                //如果缓存数据中不存在，说明是网络数据新增的模块，则直接添加进缓存中
                if (!isExist) {
                    mCacheMenuTypeBeans.add(netWorkMenuBean);
                }
            }

            //循环遍历缓存数据和网络数据，如果缓存数据中存在网络数据中不存在的模块，则需要去除
            for (int i = mCacheMenuTypeBeans.size() - 1; i >= 0; i--) {
                WorkMenuBean cacheWorkMenuBean = mCacheMenuTypeBeans.get(i);
                if (!cacheWorkMenuBean.isLocalModule()) {
                    boolean isCacheExist = false;
                    String cacheModuleTag = cacheWorkMenuBean.getModuleTag();
                    for (WorkMenuBean netWorkMenuBean : netWorkMenuBeans) {
                        String netModuleTag = netWorkMenuBean.getModuleTag();
                        if (cacheModuleTag.equals(netModuleTag)) {
                            isCacheExist = true;
                            break;
                        }
                    }
                    if (!isCacheExist) {
                        mCacheMenuTypeBeans.remove(i);
                    }
                }
            }

            String resultJson = JSON.toJSONString(mCacheMenuTypeBeans);
            com.core.utils.CommonUtil.setUniqueSharedPreferences(mContext, Constants.WORK_MENU_CACHE, resultJson);

            mPresenter.uasRequest(mContext, new HttpParams.Builder().flag(Constants.LOAD_WORK_MENU_CACHE).build());
        }
    }

    private void saveOrdersCaller(Object object) {
        try {
            String result = object.toString();
            if (JSONUtil.validate(result)) {
                JSONObject resultObject = JSON.parseObject(result);
                JSONArray listdata = JSONUtil.getJSONArray(resultObject, "listdata");
                if (ListUtils.isEmpty(listdata)) {
                    return;
                }
                for (int i = 0; i < listdata.size(); i++) {
                    JSONObject jb = listdata.getJSONObject(i);
                    String mo_caller = jb.getString("MO_CALLER");
                    if ("加班申请".equals(jb.getString("MO_NAME"))) {
                        CommonUtil.setSharedPreferences(mContext, Constants.WORK_OVERTIME_CALLER_CACHE, mo_caller);
                    }
                    if ("出差申请".equals(jb.getString("MO_NAME"))) {
                        CommonUtil.setSharedPreferences(mContext, Constants.WORK_TRAVEL_CALLER_CACHE, mo_caller);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void requestError(int what, String errorMsg) {
//        if (errorMsg != null && errorMsg.length() <= 40) {
//            toast(errorMsg);
//        }
    }
}

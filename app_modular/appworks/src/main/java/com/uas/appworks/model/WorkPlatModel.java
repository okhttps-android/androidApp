package com.uas.appworks.model;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.config.BaseConfig;
import com.core.api.wxapi.ApiPlatform;
import com.core.api.wxapi.ApiUtils;
import com.core.app.Constants;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.google.gson.Gson;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.WorkMenuBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2017/11/9 14:33
 */

public class WorkPlatModel implements IWorkPlatModel {
    private List<WorkMenuBean> mMenuTypeBeen;
    private Resources mResources;

    @Override
    public void uasRequest(Context context, final HttpParams httpParams, final HttpCallback workCallback) {
        final int what = httpParams.getFlag();
        if (what == Constants.LOAD_WORK_MENU_CACHE) {
            loadWorkMenuCache(context, workCallback, what);
        } else {
            if (!CommonUtil.isNetWorkConnected(context)) {
                if (workCallback != null) {
                    try {
                        workCallback.onFail(what, context.getString(R.string.networks_out));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastUtil.showToast(context, R.string.networks_out);
                }
                return;
            }
            String appBaseUrl = null;
            if (ApiUtils.getApiModel() instanceof ApiPlatform) {
                appBaseUrl = new ApiPlatform().getBaseUrl();
            } else {
                appBaseUrl = CommonUtil.getAppBaseUrl(BaseConfig.getContext());
            }
            if (TextUtils.isEmpty(appBaseUrl)) {
                if (workCallback != null) {
                    try {
                        workCallback.onFail(what, context.getString(R.string.host_null_please_login_retry));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ToastUtil.showToast(context, R.string.host_null_please_login_retry);
                }
                return;
            }
            HttpRequest.getInstance().sendRequest(appBaseUrl, httpParams, workCallback);
        }

    }

    @Override
    public void cityRequest(Context context, final HttpParams httpParams, final HttpCallback workCallback) {
        if (!CommonUtil.isNetWorkConnected(context)) {
            if (workCallback != null) {
                try {
                    workCallback.onFail(httpParams.getFlag(), context.getString(R.string.networks_out));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ToastUtil.showToast(context, R.string.networks_out);
            }
            return;
        }
        String appBaseUrl = CommonUtil.getCityBaseUrl(BaseConfig.getContext());
        HttpRequest.getInstance().sendRequest(appBaseUrl, httpParams, workCallback);
    }

    private void loadWorkMenuCache(final Context context, final HttpCallback workCallback, final int what) {
        mResources = context.getResources();
        String cacheJson = CommonUtil.getUniqueSharedPreferences(context, Constants.WORK_MENU_CACHE);
        String localJson = null;
        String role = CommonUtil.getUserRole();
        if (role.equals("1")) {//个人用户
            localJson = CommonUtil.getAssetsJson(context, "work_menu_personal.json");
        } else if (role.equals("3")) {//b2b用户
            localJson = CommonUtil.getAssetsJson(context, "work_menu_b2b.json");
        } else if (role.equals("2")) {//ERP用户
            localJson = CommonUtil.getAssetsJson(context, "work_menu.json");
        }
        List<WorkMenuBean> localWorkMenuBeans = null;
        if (localJson != null) {
            localWorkMenuBeans = getWorkMenuBeans(localJson);
        }
        if (cacheJson != null) {
            mMenuTypeBeen = new ArrayList<>();
            List<WorkMenuBean> cacheWorkMenuBeans = getWorkMenuBeans(cacheJson);

            // TODO: 2017/11/15 循环逻辑待优化
            //循环遍历缓存数据，目的是用本地文件来更新缓存数据
            for (WorkMenuBean cacheWorkMenuBean : cacheWorkMenuBeans) {
                String cacheModuleTag = cacheWorkMenuBean.getModuleTag();
                List<WorkMenuBean.ModuleListBean> cacheTypeList = cacheWorkMenuBean.getModuleList();
                boolean isWorkMenuExist = false;

                //先循环本地文件，如果是本地的模块，则按照本地文件更新缓存中的属性
                for (WorkMenuBean localWorkMenuBean : localWorkMenuBeans) {
                    String localModuleTag = localWorkMenuBean.getModuleTag();
                    List<WorkMenuBean.ModuleListBean> localModuleList = localWorkMenuBean.getModuleList();
                    if (localModuleTag.equals(cacheModuleTag)) {
                        //本地模块存在，按本地模块更新缓存数据
                        isWorkMenuExist = true;
                        WorkMenuBean workMenuBean = new WorkMenuBean();
                        workMenuBean.setIsLocalModule(true);
                        workMenuBean.setModuleName(localWorkMenuBean.getModuleName());
                        workMenuBean.setModuleTag(localWorkMenuBean.getModuleTag());
                        workMenuBean.setModuleVisible(cacheWorkMenuBean.isModuleVisible());

                        List<WorkMenuBean.ModuleListBean> resultModuleListBeans = new ArrayList<>();

                        for (WorkMenuBean.ModuleListBean cacheModuleListBean : cacheTypeList) {
                            boolean isModuleListExist = false;
                            String cacheMenuIcon = cacheModuleListBean.getMenuIcon();
                            for (WorkMenuBean.ModuleListBean localModuleListBean : localModuleList) {
                                String localMenuIcon = localModuleListBean.getMenuIcon();
                                if (localMenuIcon.equals(cacheMenuIcon)) {
                                    //本地应用存在，按本地应用更新缓存数据
                                    isModuleListExist = true;
                                    WorkMenuBean.ModuleListBean resultModuleListBean = new WorkMenuBean.ModuleListBean();
                                    resultModuleListBean.setIsHide(cacheModuleListBean.isHide());
                                    resultModuleListBean.setIsLocalMenu(localModuleListBean.isLocalMenu());
                                    resultModuleListBean.setMenuActivity(localModuleListBean.getMenuActivity());
                                    resultModuleListBean.setMenuTag(localModuleListBean.getMenuTag());
                                    resultModuleListBean.setMenuIcon(localModuleListBean.getMenuIcon());
                                    resultModuleListBean.setMenuName(localModuleListBean.getMenuName());
                                    resultModuleListBean.setMenuUrl(localModuleListBean.getMenuUrl());

                                    resultModuleListBeans.add(resultModuleListBean);
                                    break;
                                }
                            }
                            //如果本地应用不存在，则直接添加
                            if (!isModuleListExist && !cacheModuleListBean.isLocalMenu()) {
                                resultModuleListBeans.add(cacheModuleListBean);
                            }
                        }
                        for (WorkMenuBean.ModuleListBean localModuleListBean : localModuleList) {
                            boolean isExist = false;
                            String localMenuIcon = localModuleListBean.getMenuIcon();
                            for (WorkMenuBean.ModuleListBean moduleListBean : resultModuleListBeans) {
                                String menuIcon = moduleListBean.getMenuIcon();
                                if (localMenuIcon.equals(menuIcon)) {
                                    isExist = true;
                                    break;
                                }
                            }
                            //本地文件中有缓存中不存在的模块，则将新增模块添加入模块列表最后一项
                            if (!isExist) {
                                resultModuleListBeans.add(localModuleListBean);
                            }
                        }
                        workMenuBean.setModuleList(resultModuleListBeans);

                        mMenuTypeBeen.add(workMenuBean);
                        break;
                    }
                }

                //如果本地文件中不存在，并且不是本地模块，则说明是网络模块，直接添加
                if (!isWorkMenuExist && !cacheWorkMenuBean.isLocalModule()) {
                    mMenuTypeBeen.add(cacheWorkMenuBean);
                }
            }
            //循环遍历本地菜单，目的是想缓存数据中添加新增的本地应用
            for (WorkMenuBean localWorkMenuBean : localWorkMenuBeans) {
                boolean isExist = false;
                String localModuleTag = localWorkMenuBean.getModuleTag();
                for (WorkMenuBean workMenuBean : mMenuTypeBeen) {
                    String moduleTag = workMenuBean.getModuleTag();
                    if (localModuleTag.equals(moduleTag)) {
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    mMenuTypeBeen.add(localWorkMenuBean);
                }
            }

            CommonUtil.setUniqueSharedPreferences(context, Constants.WORK_MENU_CACHE, JSON.toJSONString(mMenuTypeBeen));
            if (mMenuTypeBeen == null) {
                try {
                    workCallback.onFail(what, "工作菜单缓存数据为空");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    workCallback.onSuccess(what, JSON.toJSONString(mMenuTypeBeen));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            CommonUtil.setUniqueSharedPreferences(context, Constants.WORK_MENU_CACHE, localJson);
            mMenuTypeBeen = localWorkMenuBeans;
            if (mMenuTypeBeen == null) {
                try {
                    workCallback.onFail(what, "工作菜单缓存数据为空");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    workCallback.onSuccess(what, localJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @NonNull
    private List<WorkMenuBean> getWorkMenuBeans(String menuJson) {
        List<WorkMenuBean> localWorkMenuBeans;
        Gson mGson = new Gson();
        localWorkMenuBeans = new ArrayList<>();
        JSONArray objects = JSON.parseArray(menuJson);
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.getJSONObject(i);
            WorkMenuBean menuTypeBean = mGson.fromJson(jsonObject.toString(), WorkMenuBean.class);
            localWorkMenuBeans.add(menuTypeBean);
        }
        return localWorkMenuBeans;
    }

    @Override
    public List<WorkMenuBean> getWorkData() {
        return mMenuTypeBeen;
    }

}

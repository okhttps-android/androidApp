package com.uas.appworks.activity.businessManage.businessMineList;

import android.content.Context;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.core.base.presenter.BaseMvpPresenter;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.uas.appworks.model.bean.BusinessMineChildBean;
import com.uas.appworks.model.bean.CommonColumnsBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/11 17:25
 */
public class BusinessMineListPresenterImpl extends BaseMvpPresenter<BusinessMineListContract.IBusinessMineListView>
        implements BusinessMineListContract.IBusinessMineListPresenter, HttpCallback {
    private final int REQUEST_BUSINESS_MINE_LIST = 0x01;

    @Override
    public void onMvpAttachView(BusinessMineListContract.IBusinessMineListView view, Bundle savedInstanceState) {
        super.onMvpAttachView(view, savedInstanceState);
    }

    @Override
    public void onMvpResume() {
        super.onMvpResume();
    }

    @Override
    public void getBusinessMineList(Context context, Map<String, Object> params) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/common/list.action")
                        .method(Method.GET)
                        .flag(REQUEST_BUSINESS_MINE_LIST)
                        .setParams(params)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void onSuccess(int flag, Object o) throws Exception {
        try {
            getMvpView().hideLoading();
            switch (flag) {
                case REQUEST_BUSINESS_MINE_LIST:
                    String result = o.toString();
                    LogUtil.prinlnLongMsg("raoMineSuc", result);
                    List<BusinessMineChildBean> businessMineChildBeans = new ArrayList<>();
                    List<CommonColumnsBean> commonColumnsBeans = new ArrayList<>();
                    if (!JSONUtil.validate(result)) {
                        getMvpView().requestListSuccess(businessMineChildBeans, commonColumnsBeans);
                        return;
                    }
                    JSONObject resultObject = JSON.parseObject(result);
                    if (resultObject == null) {
                        getMvpView().requestListSuccess(businessMineChildBeans, commonColumnsBeans);
                        return;
                    }
                    JSONArray columnArray = resultObject.getJSONArray("columns");
                    if (columnArray == null) {
                        getMvpView().requestListSuccess(businessMineChildBeans, commonColumnsBeans);
                        return;
                    }
                    for (int i = 0; i < columnArray.size(); i++) {
                        JSONObject columnObject = columnArray.getJSONObject(i);
                        if (columnObject != null) {
                            CommonColumnsBean commonColumnsBean = new CommonColumnsBean();

                            commonColumnsBean.setDataIndex(JSONUtil.getText(columnObject, "dataIndex"));
                            commonColumnsBean.setCaption(JSONUtil.getText(columnObject, "caption"));
                            commonColumnsBean.setWidth(JSONUtil.getInt(columnObject, "width"));
                            commonColumnsBean.setType(JSONUtil.getText(columnObject, "type"));
                            commonColumnsBean.setFormat(JSONUtil.getText(columnObject, "format"));
                            commonColumnsBean.setRender(JSONUtil.getText(columnObject, "render"));

                            commonColumnsBeans.add(commonColumnsBean);
                        }
                    }
                    JSONArray listDataArray = resultObject.getJSONArray("listdata");
                    if (listDataArray == null) {
                        getMvpView().requestListSuccess(businessMineChildBeans, commonColumnsBeans);
                        return;
                    }

                    for (int i = 0; i < listDataArray.size(); i++) {
                        JSONObject listDataObject = listDataArray.getJSONObject(i);
                        if (listDataObject != null) {
                            BusinessMineChildBean businessMineParent = new BusinessMineChildBean();
                            businessMineParent.setItemType(BusinessMineChildBean.BUSINESS_MINE_PARENT);
                            businessMineChildBeans.add(businessMineParent);

                            for (int j = 0; j < commonColumnsBeans.size(); j++) {
                                CommonColumnsBean commonColumnsBean = commonColumnsBeans.get(j);
                                if (commonColumnsBean != null) {
                                    BusinessMineChildBean businessMineChild = new BusinessMineChildBean();
                                    String dataIndex = commonColumnsBean.getDataIndex();
                                    businessMineChild.setCaption(commonColumnsBean.getCaption());
                                    businessMineChild.setDataIndex(dataIndex);
                                    businessMineChild.setValue(JSONUtil.getText(listDataObject, dataIndex));
                                    businessMineChild.setItemType(BusinessMineChildBean.BUSINESS_MINE_CHILD);
                                    businessMineChild.setId(JSONUtil.getInt(listDataObject, "bc_id"));
                                    businessMineChild.setBcType(JSONUtil.getText(listDataObject, "bc_type"));
                                    businessMineChild.setStageCode(JSONUtil.getText(listDataObject, "bc_currentprocesscode"));
                                    businessMineChild.setBcCode(JSONUtil.getText(listDataObject, "bc_code"));
                                    businessMineChild.setBcDescription(JSONUtil.getText(listDataObject, "bc_description"));

                                    businessMineChildBeans.add(businessMineChild);
                                }
                            }
                        }
                    }
                    getMvpView().requestListSuccess(businessMineChildBeans, commonColumnsBeans);

                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            getMvpView().hideLoading();
            getMvpView().requestListFail("数据解析异常");
        }
    }

    @Override
    public void onFail(int flag, String failStr) throws Exception {
        try {
            getMvpView().hideLoading();
            switch (flag) {
                case REQUEST_BUSINESS_MINE_LIST:
                    getMvpView().requestListFail(failStr);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            getMvpView().requestListFail(failStr);
        }

    }
}

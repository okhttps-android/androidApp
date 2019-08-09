package com.uas.appworks.activity.businessManage.businessStage;

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
import com.uas.appworks.model.bean.BusinessStageBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/12 17:56
 */
public class BusinessStagePresenterImpl extends BaseMvpPresenter<BusinessStageContract.IBusinessStageView>
        implements BusinessStageContract.IBusinessStagePresenter, HttpCallback {
    private final int REQUEST_BUSINESS_STAGE = 2;

    @Override
    public void onMvpAttachView(BusinessStageContract.IBusinessStageView view, Bundle savedInstanceState) {
        super.onMvpAttachView(view, savedInstanceState);
    }

    @Override
    public void onMvpResume() {
        super.onMvpResume();
    }

    @Override
    public void requestStageList(Context context) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/getBusinessChanceStage.action")
                        .flag(REQUEST_BUSINESS_STAGE)
                        .method(Method.POST)
                        .addParam("master", CommonUtil.getMaster())
                        .addParam("sessionId", CommonUtil.getSharedPreferences(context, "sessionId"))
                        .addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"))
                        .build(), this);
    }

    @Override
    public void onSuccess(int flag, Object o) throws Exception {
        getMvpView().hideLoading();
        switch (flag) {
            case REQUEST_BUSINESS_STAGE:
                try {
                    String result = o.toString();
                    LogUtil.prinlnLongMsg("raoStageSuc", result);
                    List<BusinessStageBean> businessStageBeans = new ArrayList<>();
                    if (!JSONUtil.validate(result)) {
                        getMvpView().requestStageSuccess(businessStageBeans);
                        return;
                    }
                    JSONObject resultObject = JSON.parseObject(result);
                    if (resultObject == null) {
                        getMvpView().requestStageSuccess(businessStageBeans);
                        return;
                    }
                    JSONArray stageArray = resultObject.getJSONArray("stages");
                    if (stageArray == null) {
                        getMvpView().requestStageSuccess(businessStageBeans);
                        return;
                    }
                    for (int i = 0; i < stageArray.size(); i++) {
                        JSONObject stageObject = stageArray.getJSONObject(i);
                        if (stageObject != null) {
                            BusinessStageBean businessStageBean = new BusinessStageBean();
                            businessStageBean.setBS_ID(JSONUtil.getInt(stageObject, "BS_ID"));
                            businessStageBean.setBS_CODE(JSONUtil.getText(stageObject, "BS_CODE"));
                            businessStageBean.setBS_NAME(JSONUtil.getText(stageObject, "BS_NAME"));
                            businessStageBean.setBS_REMARK(JSONUtil.getText(stageObject, "BS_REMARK"));
                            businessStageBean.setBS_RELATIVEITEM(JSONUtil.getText(stageObject, "BS_RELATIVEITEM"));
                            businessStageBean.setBS_COLOR(JSONUtil.getText(stageObject, "BS_COLOR"));
                            businessStageBean.setBS_DETNO(JSONUtil.getInt(stageObject, "BS_DETNO"));
                            businessStageBean.setBS_DAYS(JSONUtil.getInt(stageObject, "BS_DAYS"));
                            businessStageBean.setBS_ACTIVEDATE(JSONUtil.getInt(stageObject, "BS_ACTIVEDATE"));
                            businessStageBean.setBS_POINT(JSONUtil.getText(stageObject, "BS_POINT"));
                            businessStageBean.setBS_POINTFLAG(JSONUtil.getText(stageObject, "BS_POINTFLAG"));
                            businessStageBean.setBS_POINTDETNO(JSONUtil.getText(stageObject, "BS_POINTDETNO"));
                            businessStageBean.setBS_CANTURNCUST(JSONUtil.getText(stageObject, "BS_CANTURNCUST"));
                            businessStageBean.setBS_TYPE(JSONUtil.getText(stageObject, "BS_TYPE"));
                            businessStageBean.setBS_ENDSTATUS(JSONUtil.getText(stageObject, "BS_ENDSTATUS"));
                            businessStageBean.setBS_ENDNODE(JSONUtil.getText(stageObject, "BS_ENDNODE"));
                            businessStageBean.setBS_EQUITYEDGE(JSONUtil.getText(stageObject, "BS_EQUITYEDGE"));

                            businessStageBeans.add(businessStageBean);
                        }
                    }
                    getMvpView().requestStageSuccess(businessStageBeans);
                } catch (Exception e) {
                    getMvpView().requestStageSuccess(new ArrayList<BusinessStageBean>());
                }
                break;
        }
    }

    @Override
    public void onFail(int flag, String failStr) throws Exception {
        getMvpView().hideLoading();
        switch (flag) {
            case REQUEST_BUSINESS_STAGE:
                getMvpView().requestStageFail(failStr);
                break;
        }
    }
}

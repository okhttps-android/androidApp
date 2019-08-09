package com.uas.appworks.activity.businessManage.businessChangeStage;

import android.content.Context;
import android.os.Bundle;

import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.base.presenter.BaseMvpPresenter;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.uas.appworks.model.bean.BusinessStageBean;
import com.uas.appworks.model.bean.ChangeStageBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/22 11:30
 */
public class BusinessChangeStagePresenterImpl extends BaseMvpPresenter<BusinessChangeStageContract.IBusinessChangeStageView>
        implements BusinessChangeStageContract.IBusinessChangeStagePresenter, HttpCallback {
    private static final int CHANGE_BUSINESS_STAGE = 895;
    private static final int CHANGE_UPDATE_SCHEDULE = 896;

    @Override
    public void onMvpAttachView(BusinessChangeStageContract.IBusinessChangeStageView view, Bundle savedInstanceState) {
        super.onMvpAttachView(view, savedInstanceState);
    }


    @Override
    public void onMvpResume() {
        super.onMvpResume();
    }


    @Override
    public void requestChangeStage(Context context, List<ChangeStageBean> changeStageBeans, BusinessStageBean currentStage, BusinessStageBean nextStage, String remarks, String bc_code) {
        getMvpView().showLoading("");

        String remark = StringUtil.toHttpString(remarks);
        String gridSoreData = "{\n";
        for (int i = 0; i < changeStageBeans.size(); i++) {
            String value = changeStageBeans.get(i).getValue() == null ? "" : changeStageBeans.get(i).getValue();
            gridSoreData = gridSoreData + "\"BCD_COLUMN" + changeStageBeans.get(i).getStageKey()
                    + "\":\"" + value + "\",\n";
        }
        String emname = CommonUtil.getSharedPreferences(context, "erp_emname");
        if (StringUtil.isEmpty(emname)) {
            emname = MyApplication.getInstance().mLoginUser.getNickName().trim();
        }

        String gridSore = gridSoreData +
                "\"bcd_bccode\":\"" + bc_code + "\",\n" +
                "\"bcd_remark\":\"" + remark + "\",\n" +
                "\"bcd_leader\":\"" + "" + "\",\n" +
                "\"bcd_date\":\"" + "" + "\",\n" +
                "\"bcd_bsname\":\"" + nextStage.getBS_NAME() + "\",\n" +
                "\"bcd_bscode\":\"" + nextStage.getBS_CODE() + "\",\n" +
                "\"bcd_id\":\"" + nextStage.getBS_ID() + "\",\n" +
                "\"bcd_type\":\"" + nextStage.getBS_TYPE() + "\",\n" +
                "\"bcd_oldstep\":\"" + currentStage.getBS_NAME() + "\",\n" +
                "\"bcd_oldstepcode\":\"" + currentStage.getBS_CODE() + "\",\n" +
                "\"bcd_man\":\"" + emname + "\"" +
                "}";

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/updatebusinessChanceData.action")
                        .method(Method.POST)
                        .flag(CHANGE_BUSINESS_STAGE)
                        .addParam("gridStore", gridSore)
                        .addParam("caller", "BusinessChanceData")
                        .addParam("sessionId", CommonUtil.getSharedPreferences(context, "sessionId"))
                        .addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"))
                        .build(), this);
    }

    @Override
    public void requestUpdataSchedule(Context context, String bc_code) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/updateSchedule.action")
                        .method(Method.POST)
                        .flag(CHANGE_UPDATE_SCHEDULE)
                        .addParam("code", bc_code)
                        .addParam("master", CommonUtil.getMaster())
                        .addParam("sessionId", CommonUtil.getSharedPreferences(context, "sessionId"))
                        .addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"))
                        .build(), this);
    }

    @Override
    public void onSuccess(int flag, Object o) throws Exception {
        getMvpView().hideLoading();
        switch (flag) {
            case CHANGE_BUSINESS_STAGE:
                getMvpView().changeStageSuccess();
                break;
            case CHANGE_UPDATE_SCHEDULE:
                getMvpView().updateScheduleSuccess();
                break;
            default:
                break;
        }
    }

    @Override
    public void onFail(int flag, String failStr) throws Exception {
        getMvpView().hideLoading();

        getMvpView().requestFail(flag, failStr);
    }
}

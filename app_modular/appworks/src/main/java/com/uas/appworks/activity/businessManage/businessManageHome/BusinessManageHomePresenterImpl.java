package com.uas.appworks.activity.businessManage.businessManageHome;

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
import com.uas.appworks.model.bean.BusinessOverTimeBean;
import com.uas.appworks.model.bean.BusinessRankBean;
import com.uas.appworks.model.bean.BusinessRecordBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 14:44
 */
public class BusinessManageHomePresenterImpl extends BaseMvpPresenter<BusinessManageHomeContract.IBusinessManageHomeView>
        implements BusinessManageHomeContract.IBusinessManageHomePresenter, HttpCallback {
    public static final int REQUEST_BUSINESS_RECORD = 58;
    public static final int REQUEST_BUSINESS_DATA = 59;
    public static final int REQUEST_BUSINESS_OVERTIME = 60;
    public static final int REQUEST_BUSINESS_RANK = 61;
    public static final int REQUEST_BUSINESS_ALL = 62;
    public static final int REQUEST_OPTION_LIST1 = 63;
    public static final int REQUEST_OPTION_LIST2 = 64;

    @Override
    public void onMvpAttachView(BusinessManageHomeContract.IBusinessManageHomeView view, Bundle savedInstanceState) {
        super.onMvpAttachView(view, savedInstanceState);
    }

    @Override
    public void onMvpResume() {
        super.onMvpResume();
    }

    @Override
    public void getBusinessData(Context context, String dataTime) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/businessDataByMonth.action")
                        .flag(REQUEST_BUSINESS_DATA)
                        .method(Method.GET)
                        .addParam("dataTime", dataTime)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void getBusinessRecord(Context context, String salesmanCode, int pageIndex, int pageSize) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/businessRecord.action")
                        .flag(REQUEST_BUSINESS_RECORD)
                        .method(Method.GET)
                        .addParam("salesmanCode", salesmanCode)
                        .addParam("pageIndex", pageIndex)
                        .addParam("pageSize", pageSize)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void getBusinessOvertime(Context context, String salesmanCode, int pageIndex, int pageSize) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/businessOvertime.action")
                        .flag(REQUEST_BUSINESS_OVERTIME)
                        .method(Method.GET)
                        .addParam("salesmanCode", salesmanCode)
                        .addParam("pageIndex", pageIndex)
                        .addParam("pageSize", pageSize)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void getBusinessRank(Context context, int pageIndex, int pageSize) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/businessRank.action")
                        .flag(REQUEST_BUSINESS_RANK)
                        .method(Method.GET)
                        .addParam("pageIndex", pageIndex)
                        .addParam("pageSize", pageSize)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void getBusinessAll(Context context, String dataTime, String salesmanCode) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/businessOppHome.action")
                        .flag(REQUEST_BUSINESS_ALL)
                        .method(Method.GET)
                        .addParam("dataTime", dataTime)
                        .addParam("salesmanCode", salesmanCode)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void getOptionList(Context context, int flag, String caller, String code) {
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("ma/setting/config.action")
                        .flag(flag)
                        .method(Method.GET)
                        .addParam("caller", caller)
                        .addParam("code", code)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void onSuccess(int flag, Object o) throws Exception {
        getMvpView().hideLoading();
        String result = o.toString();
        switch (flag) {
            case REQUEST_BUSINESS_DATA:
                LogUtil.prinlnLongMsg("raoDataSuc", result);
                getMvpView().requestDataSuccess(result);
                break;
            case REQUEST_BUSINESS_RECORD:
                try {
                    LogUtil.prinlnLongMsg("raoRecordSuc", result);
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray dataArray = resultObject.getJSONArray("data");
                    List<BusinessRecordBean> businessRecordBeans = analysisRecordJson(dataArray);
                    getMvpView().requestRecordSuccess(businessRecordBeans);
                } catch (Exception e) {
                    getMvpView().requestRecordSuccess(new ArrayList<BusinessRecordBean>());
                }
                break;
            case REQUEST_BUSINESS_OVERTIME:
                try {
                    LogUtil.prinlnLongMsg("raoOvertimeSuc", result);
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray dataArray = resultObject.getJSONArray("data");
                    List<BusinessOverTimeBean> businessOverTimeBeans = analysisOvertimeJson(dataArray);
                    getMvpView().requestOvertimeSuccess(businessOverTimeBeans);
                } catch (Exception e) {
                    getMvpView().requestOvertimeSuccess(new ArrayList<BusinessOverTimeBean>());
                }
                break;
            case REQUEST_BUSINESS_RANK:
                try {
                    LogUtil.prinlnLongMsg("raoRankSuc", result);
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray dataArray = resultObject.getJSONArray("data");
                    List<BusinessRankBean> businessRankBeans = analysisRankJson(dataArray);
                    getMvpView().requestRankSuccess(businessRankBeans);
                } catch (Exception e) {
                    getMvpView().requestRankSuccess(new ArrayList<BusinessRankBean>());
                }
                break;
            case REQUEST_BUSINESS_ALL:
                try {
                    LogUtil.prinlnLongMsg("raoAllSuc", result);
                    JSONObject resultObject = JSON.parseObject(result);

                    JSONObject panelObject = resultObject.getJSONObject("panel");
                    String dataJson = "";
                    if (panelObject != null) {
                        dataJson = panelObject.toJSONString();
                    }
                    JSONArray recordArray = resultObject.getJSONArray("scales");
                    JSONArray overtimeArray = resultObject.getJSONArray("timeOutBusiness");
                    JSONArray ranksArray = resultObject.getJSONArray("ranks");
                    List<BusinessRecordBean> businessRecordBeans = analysisRecordJson(recordArray);
                    List<BusinessOverTimeBean> businessOverTimeBeans = analysisOvertimeJson(overtimeArray);
                    List<BusinessRankBean> businessRankBeans = analysisRankJson(ranksArray);
                    getMvpView().requestAllSuccess(dataJson, businessRecordBeans, businessOverTimeBeans, businessRankBeans);
                } catch (Exception e) {
                    getMvpView().requestAllSuccess("", new ArrayList<BusinessRecordBean>(), new ArrayList<BusinessOverTimeBean>(), new ArrayList<BusinessRankBean>());
                }
                break;
            case REQUEST_OPTION_LIST1:
            case REQUEST_OPTION_LIST2:
                getMvpView().requestOptionSuccess(flag, result);
                break;
            default:
                break;
        }
    }

    private List<BusinessRankBean> analysisRankJson(JSONArray dataArray) {
        try {
            List<BusinessRankBean> businessRankBeans = new ArrayList<>();
            if (dataArray == null || dataArray.size() == 0) {
                return businessRankBeans;
            }
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                if (dataObject != null) {
                    BusinessRankBean businessRankBean = new BusinessRankBean();
                    businessRankBean.setFnum(JSONUtil.getInt(dataObject, "fnum"));
                    businessRankBean.setSnun(JSONUtil.getInt(dataObject, "snun"));
                    businessRankBean.setSrates(JSONUtil.getText(dataObject, "srates"));
                    businessRankBean.setName(JSONUtil.getText(dataObject, "name"));
                    businessRankBean.setRank(JSONUtil.getText(dataObject, "rank"));
                    businessRankBean.setBnum(JSONUtil.getInt(dataObject, "bnum"));
                    businessRankBean.setNnum(JSONUtil.getInt(dataObject, "nnum"));

                    businessRankBeans.add(businessRankBean);
                }
            }
            Collections.sort(businessRankBeans);
            return businessRankBeans;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<BusinessOverTimeBean> analysisOvertimeJson(JSONArray dataArray) {
        try {
            List<BusinessOverTimeBean> businessOverTimeBeans = new ArrayList<>();
            if (dataArray == null || dataArray.size() == 0) {
                return businessOverTimeBeans;
            }
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                if (dataObject != null) {
                    BusinessOverTimeBean businessOverTimeBean = new BusinessOverTimeBean();
                    businessOverTimeBean.setStepName(JSONUtil.getText(dataObject, "stepName"));
                    businessOverTimeBean.setName(JSONUtil.getText(dataObject, "name"));
                    businessOverTimeBean.setLastetime(JSONUtil.getLong(dataObject, "lastetime"));
                    businessOverTimeBean.setMan(JSONUtil.getText(dataObject, "man"));

                    businessOverTimeBeans.add(businessOverTimeBean);
                }
            }
            return businessOverTimeBeans;
        } catch (Exception e) {
            return new ArrayList<>();
        }

    }

    private List<BusinessRecordBean> analysisRecordJson(JSONArray dataArray) {
        try {
            List<BusinessRecordBean> businessRecordBeans = new ArrayList<>();
            if (dataArray == null || dataArray.size() == 0) {
                return businessRecordBeans;
            }
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                if (dataObject != null) {
                    BusinessRecordBean businessRecordBean = new BusinessRecordBean();
                    businessRecordBean.setName(JSONUtil.getText(dataObject, "name"));
                    businessRecordBean.setTime(JSONUtil.getLong(dataObject, "time"));
                    businessRecordBean.setMan(JSONUtil.getText(dataObject, "man"));
                    businessRecordBean.setInfo(JSONUtil.getText(dataObject, "info"));

                    businessRecordBeans.add(businessRecordBean);
                }
            }
            return businessRecordBeans;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void onFail(int flag, String failStr) throws Exception {
        getMvpView().hideLoading();
        switch (flag) {
            case REQUEST_BUSINESS_RECORD:
                LogUtil.prinlnLongMsg("raoRecordFail", failStr);
                getMvpView().requestFail(flag, failStr);
                break;
            case REQUEST_BUSINESS_DATA:
                LogUtil.prinlnLongMsg("raoDataFail", failStr);
                getMvpView().requestFail(flag, failStr);
                break;
            case REQUEST_BUSINESS_OVERTIME:
                LogUtil.prinlnLongMsg("raoOvertimeFail", failStr);
                getMvpView().requestFail(flag, failStr);
                break;
            case REQUEST_BUSINESS_RANK:
                LogUtil.prinlnLongMsg("raoRankFail", failStr);
                getMvpView().requestFail(flag, failStr);
                break;
            case REQUEST_BUSINESS_ALL:
                LogUtil.prinlnLongMsg("raoAllFail", failStr);
                getMvpView().requestFail(flag, failStr);
                break;
            case REQUEST_OPTION_LIST1:
                getMvpView().requestFail(flag, failStr);
                break;
            default:
                break;
        }
    }
}

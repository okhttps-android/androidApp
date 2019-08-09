package com.uas.appworks.activity.businessManage.businessDetailActivity;

import android.content.Context;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.core.base.presenter.BaseMvpPresenter;
import com.core.model.SelectEmUser;
import com.core.utils.CommonUtil;
import com.me.network.app.base.HttpCallback;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.HttpRequest;
import com.me.network.app.http.Method;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.BusinessStageBean;
import com.uas.appworks.model.bean.CommonFormBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/18 14:09
 */
public class BusinessDetailPresenterImpl extends BaseMvpPresenter<BusinessDetailContract.BusinessDetailView>
        implements BusinessDetailContract.BusinessDetailPresenter, HttpCallback {
    private Context mContext;

    public static final int REQUEST_BUSINESS_DETAIL = 744;
    public static final int REQUEST_BUSINESS_STAGE = 745;
    public static final int REQUEST_CHANGE_DOMAN = 746;
    public static final int REQUEST_SCHEDULE_LIST = 747;
    public static final int REQUEST_BUSINESS_TYPE = 748;
    public static final int REQUEST_CAN_RECEIVE = 749;
    public static final int REQUEST_BUSINESS_RECEIVE = 750;
    public static final int REQUEST_BUSINESS_DISTRIBUTION = 751;

    @Override
    public void onMvpAttachView(BusinessDetailContract.BusinessDetailView view, Bundle savedInstanceState) {
        super.onMvpAttachView(view, savedInstanceState);
    }

    @Override
    public void onMvpResume() {
        super.onMvpResume();
    }


    @Override
    public void requestMainDetail(Context context, int ID, String caller) {
        mContext = context;
        getMvpView().showLoading("");
        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/getformandgriddetail.action")
                        .method(Method.GET)
                        .flag(REQUEST_BUSINESS_DETAIL)
                        .addParam("caller", caller)
                        .addParam("condition", "1=1")
                        .addParam("id", ID)
                        .addParam("sessionId", CommonUtil.getSharedPreferences(context, "sessionId"))
                        .addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"))
                        .build(), this);
    }

    @Override
    public void requestStageList(Context context) {
        mContext = context;
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
    public void changeDoman(Context context, SelectEmUser user, String bc_code) {
        mContext = context;
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/updateBusinessChanceDoman.action")
                        .method(Method.POST)
                        .flag(REQUEST_CHANGE_DOMAN)
                        .addParam("bc_code", bc_code)
                        .addParam("bc_doman", user.getEmName())
                        .addParam("bc_domancode", user.getEmCode())
                        .addParam("type", 1)
                        .addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"))
                        .build(), this);
    }

    @Override
    public void requestScheduleList(Context context, String bc_code, String emname) {
        mContext = context;
        getMvpView().showLoading("");

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/getSchedule.action")
                        .method(Method.POST)
                        .flag(REQUEST_SCHEDULE_LIST)
                        .addParam("bccode", bc_code)
                        .addParam("emname", emname)
                        .addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"))
                        .build(), this);
    }

    @Override
    public void requestBusinessType(Context context, int bc_id, String admincode) {
        mContext = context;
        getMvpView().showLoading("");
        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/businessChanceButtonShow.action")
                        .method(Method.POST)
                        .flag(REQUEST_BUSINESS_TYPE)
                        .addParam("bcid", bc_id)
                        .addParam("admincode", admincode)
                        .addHeader("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(context, "sessionId"))
                        .build(), this);
    }

    @Override
    public void canBusinessReceive(Context context) {
        mContext = context;
        getMvpView().showLoading("");
        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/ifoverrecv.action")
                        .method(Method.POST)
                        .flag(REQUEST_CAN_RECEIVE)
                        .addParam("emcode", CommonUtil.getEmcode())
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void requestBusinessReceive(Context context, int type, String bc_code, String bc_doman, String bc_domancode) {
        mContext = context;
        getMvpView().showLoading("");

        int what = REQUEST_BUSINESS_RECEIVE;
        if (type == 0) {
            what = REQUEST_BUSINESS_RECEIVE;
        } else if (type == 1) {
            what = REQUEST_BUSINESS_DISTRIBUTION;
        }

        HttpRequest.getInstance().sendRequest(CommonUtil.getAppBaseUrl(context),
                new HttpParams.Builder()
                        .url("mobile/crm/updateBusinessChanceDoman.action")
                        .method(Method.POST)
                        .flag(what)
                        .addParam("bc_code", bc_code)
                        .addParam("type", type)
                        .addParam("bc_doman", bc_doman)
                        .addParam("bc_domancode", bc_domancode)
                        .addHeader("Cookie", CommonUtil.getErpCookie(context))
                        .build(), this);
    }

    @Override
    public void onSuccess(int flag, Object o) throws Exception {
        getMvpView().hideLoading();
        switch (flag) {
            case REQUEST_BUSINESS_DETAIL:
                try {
                    analysisBusinessDetail(o);
                } catch (Exception e) {
                    getMvpView().requestMainDetailSuccess(new ArrayList<CommonFormBean>(), new ArrayList<CommonFormBean>());
                }
                break;
            case REQUEST_BUSINESS_STAGE:
                try {
                    analysisBusinessStage(o);
                } catch (Exception e) {
                    getMvpView().requestStageSuccess(new ArrayList<BusinessStageBean>());
                }
                break;
            case REQUEST_CHANGE_DOMAN:
                getMvpView().changeDomanSuccess();
                break;
            case REQUEST_SCHEDULE_LIST:
                try {
                    String result = o.toString();
                    LogUtil.prinlnLongMsg("raoScheduleSuc", result);
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray resultArray = resultObject.getJSONArray("result");
                    getMvpView().requestScheduleListSuccess(resultArray);
                } catch (Exception e) {
                    getMvpView().requestScheduleListSuccess(new JSONArray());
                }
                break;
            case REQUEST_BUSINESS_TYPE:
                try {
                    String result = o.toString();
                    LogUtil.prinlnLongMsg("raoTypeSuc", result);
                    JSONObject resultObject = JSON.parseObject(result);
                    boolean isGra = JSONUtil.getBoolean(resultObject, "grabBc");
                    boolean isDistribution = JSONUtil.getBoolean(resultObject, "distributionBc");
                    getMvpView().requestBusinessTypeSuccess(isGra, isDistribution);
                } catch (Exception e) {
                    getMvpView().requestBusinessTypeSuccess(false, false);
                }
                break;
            case REQUEST_CAN_RECEIVE:
                try {
                    String result = o.toString();
                    if (!JSONUtil.validate(result)) {
                        getMvpView().requestFail(REQUEST_CAN_RECEIVE, result);
                        return;
                    }
                    JSONObject resultObject = JSON.parseObject(result);
                    String isOk = JSONUtil.getText(resultObject, "isok");
                    if ("0".equals(isOk)) {
                        getMvpView().canBusinessReceiveSuccess();
                    } else if ("1".equals(isOk)) {
                        getMvpView().requestFail(REQUEST_CAN_RECEIVE, mContext.getString(R.string.business_limit));
                    }
                } catch (Exception e) {
                    getMvpView().requestFail(REQUEST_CAN_RECEIVE, "请求异常");
                }
                break;
            case REQUEST_BUSINESS_RECEIVE:
                getMvpView().requestBusinessReceiveSuccess(0);
                break;
            case REQUEST_BUSINESS_DISTRIBUTION:
                getMvpView().requestBusinessReceiveSuccess(1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onFail(int flag, String failStr) throws Exception {
        getMvpView().hideLoading();
        LogUtil.prinlnLongMsg("raoDetailFail", failStr);
        getMvpView().requestFail(flag, failStr);
    }

    private void analysisBusinessStage(Object o) {
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
    }

    private void analysisBusinessDetail(Object o) {
        String result = o.toString();
        LogUtil.prinlnLongMsg("raoDetailSuc", result);
        List<CommonFormBean> allFormBeans = new ArrayList<>();
        List<CommonFormBean> visibleFormBeans = new ArrayList<>();
        if (!JSONUtil.validate(result)) {
            getMvpView().requestMainDetailSuccess(allFormBeans, visibleFormBeans);
            return;
        }
        JSONObject resultObject = JSON.parseObject(result);
        if (resultObject == null) {
            getMvpView().requestMainDetailSuccess(allFormBeans, visibleFormBeans);
            return;
        }
        JSONObject dataObject = resultObject.getJSONObject("data");
        if (dataObject == null) {
            getMvpView().requestMainDetailSuccess(allFormBeans, visibleFormBeans);
            return;
        }
        JSONArray formdetailArray = dataObject.getJSONArray("formdetail");
        if (formdetailArray == null || formdetailArray.size() == 0) {
            getMvpView().requestMainDetailSuccess(allFormBeans, visibleFormBeans);
            return;
        }
        for (int i = 0; i < formdetailArray.size(); i++) {
            JSONObject formdetailObject = formdetailArray.getJSONObject(i);
            if (formdetailObject != null) {
                CommonFormBean commonFormBean = new CommonFormBean();

                commonFormBean.setCaption(JSONUtil.getText(formdetailObject, "fd_caption"));
                commonFormBean.setMcaption(JSONUtil.getText(formdetailObject, "mfd_caption"));
                commonFormBean.setField(JSONUtil.getText(formdetailObject, "fd_field"));
                commonFormBean.setValue(JSONUtil.getText(formdetailObject, "fd_value"));
                commonFormBean.setMaxlength(JSONUtil.getInt(formdetailObject, "fd_maxlength"));
                commonFormBean.setDetno(JSONUtil.getDouble(formdetailObject, "fd_detno"));
                commonFormBean.setType(JSONUtil.getText(formdetailObject, "fd_type"));
                commonFormBean.setReadonly(JSONUtil.getText(formdetailObject, "fd_readonly"));
                commonFormBean.setId(JSONUtil.getInt(formdetailObject, "fd_id"));
                commonFormBean.setItemType(CommonFormBean.COMMON_FORM_CONTENT_ITEM);
                int mfd_isdefault = JSONUtil.getInt(formdetailObject, "mfd_isdefault");
                commonFormBean.setIsdefault(mfd_isdefault);
                String fd_group = JSONUtil.getText(formdetailObject, "fd_group");
                commonFormBean.setGroup(fd_group);

                allFormBeans.add(commonFormBean);
            }
        }

        Collections.sort(allFormBeans, new Comparator<CommonFormBean>() {
            @Override
            public int compare(CommonFormBean o1, CommonFormBean o2) {
                if (o1.getDetno() - o2.getDetno() > 0) {
                    return 1;
                } else if (o1.getDetno() - o2.getDetno() < 0) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        Map<String, List<CommonFormBean>> stringListMap = new LinkedHashMap<>();
        for (int i = 0; i < allFormBeans.size(); i++) {
            CommonFormBean commonFormBean = allFormBeans.get(i);
            String fd_group = commonFormBean.getGroup();
            if (commonFormBean.getIsdefault() == -1) {
                if (stringListMap.containsKey(fd_group)) {
                    List<CommonFormBean> cacheFormBeans = stringListMap.get(fd_group);
                    if (cacheFormBeans != null) {
                        cacheFormBeans.add(commonFormBean);
                    }
                    stringListMap.put(fd_group, cacheFormBeans);
                } else {
                    List<CommonFormBean> mapFormBeans = new ArrayList<>();
                    mapFormBeans.add(commonFormBean);
                    stringListMap.put(fd_group, mapFormBeans);
                }
            }
        }

        Iterator<Map.Entry<String, List<CommonFormBean>>> iterator = stringListMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<CommonFormBean>> next = iterator.next();
            List<CommonFormBean> value = next.getValue();

            CommonFormBean commonFormBean = new CommonFormBean();
            commonFormBean.setItemType(CommonFormBean.COMMON_FORM_GRAY_LINE);
            visibleFormBeans.add(commonFormBean);
            visibleFormBeans.addAll(value);
        }

        getMvpView().requestMainDetailSuccess(allFormBeans, visibleFormBeans);
    }
}

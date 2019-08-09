package com.uas.appworks.utils;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.hmac.Md5Util;
import com.core.app.MyApplication;
import com.core.net.utils.NetUtils;
import com.core.utils.CommonUtil;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.modular.apputils.R;
import com.uas.appworks.OA.platform.model.BusinessTravel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Bitlike on 2018/3/23.
 */

public class TravelUtils {

    public static void reserve(Context ct,String cusCode, String appkey, String appSceret, BusinessTravel model) {
        if (model.getType() == BusinessTravel.UNKOWN || BusinessTravel.TITLE == model.getType()) {
            showSelect(ct,cusCode, appkey, appSceret, model);
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("traverorderno", "RES56884");
        map.put("product", model.getProduct());
        map.put("costname", "成本中心");
        map.put("proname", "所属项目");
        String from = "";
        String fromcode = "";
        String arrive = "";
        String arrivecode = "";
        switch (model.getType()) {
            case BusinessTravel.AIR:
                from = model.getAirStarting();
                fromcode = model.getAirStartingCode();
                arrive = model.getAirDestination();
                arrivecode = model.getAirDestinationCode();
                break;
            case BusinessTravel.TRAIN:
                from = model.getTrainStarting();
                fromcode = model.getTrainStartingCode();
                arrive = model.getTrainDestination();
                arrivecode = model.getTrainDestinationCode();
                break;
            case BusinessTravel.HOTEL:
                from = model.getHotelCity();
                fromcode = model.getHotelCityCode();
                break;
        }
        if (TextUtils.isEmpty(fromcode)) {
            from = "";
        }
        if (TextUtils.isEmpty(arrivecode)) {
            arrive = "";
        }

        Map<String, String> routeMap = new HashMap<>();
        routeMap.put("from", from);
        routeMap.put("fromcode", fromcode);
        routeMap.put("arrive", arrive);
        routeMap.put("arrivecode", arrivecode);
        routeMap.put("startdate", DateFormatUtil.long2Str(model.getStartTime(), DateFormatUtil.YMD));
        routeMap.put("arrivedate", DateFormatUtil.long2Str(model.getEndTime(), DateFormatUtil.YMD));
        routeMap.put("isCanModify", "1");
        map.put("route", routeMap);

        //个人中心
        Map<String, String> otherJSON = new HashMap<>();
        otherJSON.put("fpid", model.getFpId() == null ? "" : model.getFpId());
        Map<String, String> custinfoMap = new HashMap<>();
        custinfoMap.put("backUrl", CommonUtil.getAppBaseUrl(ct));
        custinfoMap.put("isNeedPush", "1");
        custinfoMap.put("cusCode", /*URY*/CommonUtil.getMaster());
        custinfoMap.put("emCode", CommonUtil.getEmcode());
        custinfoMap.put("outOrderno", model.getId() <= 0 ? "-1" : String.valueOf(model.getId()));
        map.put("otherContent", StringUtil.toHttpString(JSONUtil.map2JSON(otherJSON)));
        map.put("custinfo", custinfoMap);
        String p = JSONUtil.map2JSON(map);
        LogUtil.i("p=" + p);
        if (NetUtils.isNetWorkConnected(ct)) {
            String baseUrl = null;
            if (BaseConfig.isDebug()) {
                appkey = "y8gd87dsdkencgzk394k7s5c78io35c";
                appSceret = "e212e142a5c9e0590eefb7d9f1bc91d7";
                baseUrl = "http://124.254.45.234:8082/oa/caslogin/";
            } else {
                baseUrl = "http://h5.auvgo.com/oa/caslogin/";
                if (TextUtils.isEmpty(appkey)) {
                    appkey = "fjdsfnvg6523fsgjkff879fidsf";
                }
                if (TextUtils.isEmpty(appSceret)) {
                    appSceret = "9891ca5330271eba81ec1332e740c210";
                }
            }
            String username = CommonUtil.getEmcode();
            String data = appkey + username.toUpperCase() + appSceret;
            String key = Md5Util.toMD5(appSceret).toUpperCase();
            String sign = Md5Util.toMD5(key + data);
            StringBuilder urlBuilder = new StringBuilder(baseUrl);
            urlBuilder.append(appkey + "/");
            urlBuilder.append(username + "/");
            urlBuilder.append(sign);
            try {
                urlBuilder.append("?p=" + URLEncoder.encode(p, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            LogUtil.i(urlBuilder.toString());
            IntentUtils.linkCommonWeb(ct, urlBuilder.toString(), "行旅国际", "", "", false, false, false);
        } else {
            ToastUtil.showToast(ct, R.string.networks_out);
        }
    }

    public static void showSelect(final Context ct,final String cusCode, final String appkey, final String appSceret, final BusinessTravel model) {
        List<String> items = new ArrayList<>();
        items.add("火车票");
        items.add("飞机票");
        items.add("住宿");
        MaterialDialog mDialog = new MaterialDialog.Builder(ct)
                .title("选择预订类型")
                .items(items)
                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        LogUtil.i("text=" + text);
                        switch (text.toString()) {
                            case "火车票":
                                model.setType(BusinessTravel.TRAIN);
                                break;
                            case "住宿":
                                model.setType(BusinessTravel.HOTEL);
                                break;
                            case "飞机票":
                                model.setType(BusinessTravel.AIR);
                                break;
                        }
                        reserve(ct,cusCode, appkey, appSceret, model);
                        return true;
                    }
                }).positiveText(MyApplication.getInstance().getString(com.uas.appworks.R.string.common_sure)).show();

        mDialog.show();
    }


}

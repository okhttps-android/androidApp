package com.xzjmyk.pm.activity.ui.erp.presenter;

import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.modular.appmessages.model.SubscriptionMessage;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.presenter.imp.ISubscriptionView;
import com.common.data.ListUtils;
import com.core.net.http.http.OAHttpHelper;
import com.core.net.http.http.OnHttpResultListener;
import com.core.net.http.http.Request;
import com.xzjmyk.pm.activity.view.crouton.Style;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.fastjson.JSON.parseObject;

/**
 * Created by Bitliker on 2017/3/17.
 */

public class SubscriptionPresenter implements OnHttpResultListener {
    private final int LOAD = 0x11;
    private BaseActivity ct;
    private ISubscriptionView iSubscription;


    public SubscriptionPresenter(BaseActivity ct, ISubscriptionView iSubscription) {
        if (iSubscription == null)
            new NullPointerException("iSubscription==null");
        this.ct = ct;
        this.iSubscription = iSubscription;
    }

    public void start() {

    }

    /**
     * 获取网络数据
     *
     * @param isRefresh 是否是下拉刷新
     * @param showTime  获取的日期时间戳
     */
    private void loadDataByNet(boolean isRefresh, long showTime) {
        if (!MyApplication.getInstance().isNetworkActive()) {
            iSubscription.showToast(R.string.networks_out, Style.holoRedLight);
            return;
        }
        Map<String, Object> param = new HashMap<>();
        param.put("count", 100);
        param.put("condition", "where to_char(createdate_,'yyyymmdd')='" +  DateFormatUtil.long2Str(showTime,"yyyyMMdd")  + "'");
        Bundle bundle = new Bundle();
        bundle.putBoolean("isRefresh", isRefresh);
        Request request=new Request.Bulider()
                .setWhat(LOAD)
                .setUrl("common/desktop/subs/getSubs.action")
                .setBundle(bundle)
                .setParam(param)
                .setMode(Request.Mode.GET)
                .bulid();
        OAHttpHelper.getInstance().requestHttp(request,this);
    }

    private void loadDatByDB(boolean isRefresh, long showTime) {

    }

    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (isJSON) {
            JSONObject object = JSON.parseObject(message);
            if (object.getBoolean("success")) {
                JSONArray array = parseObject(message).getJSONArray("data");
                if (ListUtils.isEmpty(array)) {
                    iSubscription.showToast("暂无数据", R.color.load_submit);
                    return;
                } else {
                    handlerDataByLoad(JSON.parseArray(object.getJSONArray("data").toJSONString(), SubscriptionMessage.class));
                }
            } else {
                iSubscription.showToast(message == null ? "" : StringUtil.getChinese(message), R.color.load_error);
            }
        }
    }


    @Override
    public void error(int what,  String message, Bundle bundle) {

    }

    private void handlerDataByLoad(List<SubscriptionMessage> data) {


    }

}

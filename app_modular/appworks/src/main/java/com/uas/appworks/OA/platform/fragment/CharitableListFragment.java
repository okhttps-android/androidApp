package com.uas.appworks.OA.platform.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.preferences.PreferenceUtils;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.app.MyApplication;
import com.core.base.BaseActivity;
import com.core.base.EasyFragment;
import com.core.net.utils.NetUtils;
import com.core.utils.IntentUtils;
import com.core.utils.ToastUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.Method;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultListener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnPlayListener;
import com.modular.apputils.utils.playsdk.AliPlay;
import com.modular.apputils.utils.playsdk.WxPlay;
import com.modular.apputils.widget.CustomerBanner;
import com.modular.apputils.widget.VeriftyDialog;
import com.uas.appworks.OA.platform.activity.CharitSearchActivity;
import com.uas.appworks.OA.platform.adapter.AutoPlayPagerAdapter;
import com.uas.appworks.OA.platform.adapter.CharitableAdapter;
import com.uas.appworks.OA.platform.adapter.TypeAdapter;
import com.uas.appworks.OA.platform.model.Carousel;
import com.uas.appworks.OA.platform.model.CharitModel;
import com.uas.appworks.R;
import com.uas.appworks.widget.SelectPlayPop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Bitlike on 2017/11/7.
 */

public class CharitableListFragment extends EasyFragment implements OnPlayListener {
    private HttpClient httpClient = new HttpClient.Builder(Constants.charitBaseUrl())
            .connectTimeout(5000)
            .readTimeout(5000)
            .isDebug(true).build();
    private CustomerBanner banner;
    private RecyclerView typeGv, typeGv2;
    private BaseActivity baseActivity;
    private PullToRefreshListView refreshListView;

    private boolean playing = false;
    private LinearLayout moneyAmountLL;

    private VeriftyDialog mVeriftyDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            baseActivity = (BaseActivity) context;
        }
    }

    @Override
    public void onPause() {
        if (mVeriftyDialog != null) {
            mVeriftyDialog.dismiss();
            mVeriftyDialog = null;
        }
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void showPlyEnd() {
        mVeriftyDialog = new VeriftyDialog.Builder(getActivity())
                .setCanceledOnTouchOutside(false)
                .setContent("感谢您的爱心!")
                .setShowCancel(false)
                .build(new VeriftyDialog.OnDialogClickListener() {
                    @Override
                    public void result(boolean clickSure) {
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search, menu);
        MenuItem item = menu.getItem(0);
        if (item != null) {
            LogUtil.i("item!=null");
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    onOptionsItemSelected(menuItem);
                    return false;
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        LogUtil.i("onOptionsItemSelected=");
        if (item.getItemId() == R.id.search) {
            ct.startActivity(new Intent(ct, CharitSearchActivity.class).
                    putExtra("type", 1)
                    .putExtra("title", "项目搜索"));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected int inflateLayoutId() {
        return R.layout.fragment_charitable_list;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, boolean createView) {
        if (createView) {
            setHasOptionsMenu(true);
            initView();
        }
    }

    private void initView() {
        refreshListView = (PullToRefreshListView) findViewById(R.id.refreshListView);
        refreshListView.getRefreshableView().addHeaderView(getHandlerView());
        refreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        refreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadData();
            }
        });
        setContantData(null);
        loadData();
    }

    public View getHandlerView() {
        View handlerView = LayoutInflater.from(ct).inflate(R.layout.handler_charit_list, null);
        moneyAmountLL = (LinearLayout) handlerView.findViewById(R.id.moneyAmountLL);
        banner = (CustomerBanner) handlerView.findViewById(R.id.banner);
        typeGv = (RecyclerView) handlerView.findViewById(R.id.typeGv);
        typeGv2 = (RecyclerView) handlerView.findViewById(R.id.typeGv2);
        return handlerView;
    }

    private void loadData() {
        if (NetUtils.isNetWorkConnected(ct)) {
            baseActivity.progressDialog.show();
            loadProjects("全部");
            loadIndexData();
        } else {
            String appIndex = PreferenceUtils.getString("appIndex");
            String projects = PreferenceUtils.getString("projects");
            try {
                if (!StringUtil.isEmpty(appIndex)) {
                    handlerIndex(appIndex);
                }
                if (!StringUtil.isEmpty(projects)) {
                    handlerProjects("", projects);
                }
            } catch (Exception e) {
            }
            ToastUtil.showToast(ct, getString(R.string.networks_out));
        }

    }


    public void loadIndexData() {
        baseActivity.progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("appIndex")
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (o != null) {
                        handlerIndex(o.toString());
                    }
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                        ToastUtil.showToast(ct, e.getMessage());
                    }

                }
                refreshListView.onRefreshComplete();
                baseActivity.progressDialog.dismiss();
            }
        }));
    }

    private void handlerIndex(String message) throws Exception {
        JSONObject object = JSON.parseObject(message);
        JSONArray carouselList = JSONUtil.getJSONArray(object, "carouselList");
        JSONArray allArea = JSONUtil.getJSONArray(object, "allArea");
        try {
            String totality = JSONUtil.getText(object, "totality");
            int dian = totality.indexOf(".");
            String showTop = "";
            if (dian != -1) {
                showTop = totality.substring(0, dian);
            } else {
                showTop = totality.substring(0, totality.length());
            }
            int textSize = DisplayUtil.sp2px(ct, 6);
            moneyAmountLL.removeAllViews();//清空布局
            for (int i = 0; i < showTop.length(); i++) {
                char c = showTop.charAt(i);
                TextView textView = new TextView(ct);
                textView.setTextSize(textSize);
                textView.setText(String.valueOf(c));
                textView.setBackgroundResource(R.drawable.text_frame_hint_bg);
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.BLACK);
                moneyAmountLL.addView(textView);
            }
        } catch (Exception e) {
            if (e != null) {
                LogUtil.i("e=" + e.getMessage());
            }

        }
        JSONObject o;
        List<Carousel> list = new ArrayList<>();
        for (int i = 0; i < carouselList.size(); i++) {
            o = carouselList.getJSONObject(i);
            Carousel c = new Carousel(JSONUtil.getInt(o, "id"),
                    JSONUtil.getText(o, "body"),
                    JSONUtil.getText(o, "pictureUrl"));
            LogUtil.i("c=" + JSON.toJSONString(c));
            list.add(c);
        }
        setBannerData(list);
        List<String> areaList = new ArrayList<>();
        List<String> areaList2 = new ArrayList<>();
        allArea.add(0, "全部");
        for (int i = 0; i < allArea.size(); i++) {
            String type = allArea.getString(i);
            if (!StringUtil.isEmpty(type)) {
                if (i < 4) {
                    areaList.add(type);
                } else {
                    areaList2.add(type);
                }
            }
        }
        setTypeData(areaList, areaList2);
        PreferenceUtils.putString("appIndex", message);
    }


    private void loadProjects(final String area) {
        baseActivity.progressDialog.show();
        httpClient.Api().send(new HttpClient.Builder()
                .url("projects")
                .add("area", area)
                .method(Method.GET)
                .build(), new ResultSubscriber<>(new ResultListener<Object>() {
            @Override
            public void onResponse(Object o) {
                try {
                    if (o != null) {
                        handlerProjects(area == null ? "" : area, o.toString());
                    }
                } catch (Exception e) {
                    if (e != null) {
                        LogUtil.i("e=" + e.getMessage());
                    }
                }
                refreshListView.onRefreshComplete();
                baseActivity.progressDialog.dismiss();
            }
        }));
    }

    private void handlerProjects(String area, String message) throws Exception {
        LogUtil.prinlnLongMsg("gong", "message=" + message);
        JSONObject object = JSON.parseObject(message);
        JSONArray projectList = JSONUtil.getJSONArray(object, "projectList");
        String projectJSON = projectList.toJSONString();
        List<CharitModel> models = JSON.parseArray(projectJSON, CharitModel.class);
        setContantData(models);
        PreferenceUtils.putString("projects", message);
    }


    private AutoPlayPagerAdapter autoPlayPagerAdapter;

    private void setBannerData(final List<Carousel> models) {
        if (autoPlayPagerAdapter == null) {
            autoPlayPagerAdapter = new AutoPlayPagerAdapter(ct, models, new AutoPlayPagerAdapter.ItemClickListener() {
                @Override
                public void clickItem(Carousel carousel) {
                    IntentUtils.linkCommonWeb(ct,
                            Constants.BASE_CHARIT_PROJECT_URL + carousel.getId()
                                    + "/" + MyApplication.getInstance().getLoginUserId()
                            , StringUtil.getMessage(R.string.charitable)
                            , carousel.getImageUrl(), carousel.getText());
                }
            });
            banner.setAdapter(autoPlayPagerAdapter);
        } else {
            autoPlayPagerAdapter.setList(models);
        }
    }

    private TypeAdapter typeAdapter;
    private TypeAdapter typeAdapter2;

    private void setTypeData(final List<String> areaList, final List<String> areaList2) {
        typeAdapter = new TypeAdapter(ct, areaList, 0, new TypeAdapter.ChangeListener() {
            @Override
            public void change(String type) {
                typeAdapter2.clear();
                loadProjects(type);
            }
        });
        typeAdapter2 = new TypeAdapter(ct, areaList2, -1, new TypeAdapter.ChangeListener() {
            @Override
            public void change(String type) {
                typeAdapter.clear();
                loadProjects(type);
            }
        });

        typeGv.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager layoutManager = new LinearLayoutManager(ct);
        layoutManager.setOrientation(OrientationHelper.HORIZONTAL);
        typeGv.setLayoutManager(layoutManager);
        typeGv.setAdapter(typeAdapter);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(ct);
        layoutManager2.setOrientation(OrientationHelper.HORIZONTAL);
        typeGv2.setItemAnimator(new DefaultItemAnimator());
        typeGv2.setLayoutManager(layoutManager2);
        typeGv2.setAdapter(typeAdapter2);
//            GridLayoutManager layoutManager = new GridLayoutManager(ct, 782);
//            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
//                @Override
//                public int getSpanSize(int position) {
//                    switch (position) {
//                        case 0:
//                            return 124;
//                        case 1:
//                            return 208;
//                        case 2:
//                            return 225;
//                        case 3:
//                            return 225;
//                        case 4:
//                            return 309;
//                        case 6:
//                            return 225;
//                        case 7:
//                            return 124;
//                        default:
//                            return 124;
//                    }
////
//                }
//            });
    }

    private CharitableAdapter charitableAdapter;

    private void setContantData(List<CharitModel> models) {
//        if (charitableAdapter == null) {
        charitableAdapter = new CharitableAdapter(ct, models, new CharitableAdapter.MyClickListener() {
            @Override
            public void myOnClick(int position, View v) {
                CharitModel model = charitableAdapter.getModels(position);
                if (!model.isEnded()) {
                    SelectPlayPop.showPlay(getActivity(), charitableAdapter.getModels(position), new SelectPlayPop.OnSureListener() {
                        @Override
                        public void sure(double num, int type, CharitModel model) {
                            if (type == Constants.FLAG.WEIXIN_PAY) {
                                LogUtil.i("选择了微信支付");
                            } else {
                                LogUtil.i("选择了支付宝支付");
                            }
                            loadOrderInfo(num, model, type);
                        }
                    });
                }


            }
        });
        refreshListView.setAdapter(charitableAdapter);
        refreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (charitableAdapter != null) {
                    CharitModel model = charitableAdapter.getModels((int) l);
                    if (model != null) {
                        IntentUtils.linkCommonWeb(ct, Constants.BASE_CHARIT_PROJECT_URL
                                        + model.getId() + "/" + MyApplication.getInstance().getLoginUserId()
                                , StringUtil.getMessage(R.string.charitable)
                                , model.getMobileImg(), model.getName());
                    }
                }
            }
        });
//        } else {
//            charitableAdapter.setModels(models);
//        }
    }


    private void sendToPay(String orderInfo, int type) {
        if (type == Constants.FLAG.WEIXIN_PAY) {
            WxPlay.api().wxPay(getActivity(), orderInfo, this);
        } else {
            AliPlay.api().alipay(getActivity(), orderInfo, this);
        }
    }

    @Override
    public void onSuccess(String resultStatus, String resultInfo) {
//        ToastUtil.showToast(ct, "支付成功");
        playing = false;
    }

    @Override
    public void onFailure(String resultStatus, String resultInfo) {
        ToastUtil.showToast(ct, "支付失败");
        playing = false;
    }

    private void loadOrderInfo(Double amount, CharitModel model, final int type) {
        if (!NetUtils.isNetWorkConnected(ct)) {
            ToastUtil.showToast(ct, R.string.networks_out);
            return;
        }
        if (model == null) {
            ToastUtil.showToast(ct, "内部错误");
            return;
        }
        if (playing) {
            ToastUtil.showToast(ct, "目前正在支付，请稍后！！！！");
            return;
        }
        playing = true;
        baseActivity.progressDialog.show();
        String payUrl = "alipay/appPay";
        Map<String, Object> map = new HashMap<>();
        map.put("projectName", model.getName());
        map.put("amount", amount);
        map.put("proId", model.getId());
        map.put("imid", MyApplication.getInstance().getLoginUserId());
        if (type == Constants.FLAG.WEIXIN_PAY) {
            map.put("way", "微信");
            payUrl = "wxpay/appPay";
        } else {
            map.put("way", "支付宝");
            payUrl = "alipay/appPay";
        }

        String json = JSON.toJSONString(map);
        LogUtil.i("json=" + json);
        String url = "http://lj.ubtob.com/";
        new HttpClient.Builder(url)
                .isDebug(BaseConfig.isDebug())
                .build()
                .Api()
                .send(new HttpClient.Builder()
                        .url(payUrl)
                        .add("jsonStr", json)
                        .method(Method.POST)
                        .build(), new ResultSubscriber<>(new Result2Listener<Object>() {
                    @Override
                    public void onResponse(Object o) {
                        String message = o.toString();
                        LogUtil.i("message=" + message);
//						EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
                        if (type == Constants.FLAG.WEIXIN_PAY) {
                            playing = false;
                        }
                        sendToPay(message, type);
                        baseActivity.progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Object t) {
                        try {
                            baseActivity.progressDialog.dismiss();
                            playing = false;
                            JSONObject failObject = JSON.parseObject(t.toString());
                            String errorStr = JSONUtil.getText(failObject, "error");
                            ToastUtil.showToast(ct, errorStr);
                        } catch (Exception e) {

                        }
                    }
                }));
    }
}

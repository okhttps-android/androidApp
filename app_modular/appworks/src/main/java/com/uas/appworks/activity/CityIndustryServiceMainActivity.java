package com.uas.appworks.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.data.JSONUtil;
import com.core.base.activity.BaseMVPActivity;
import com.core.utils.CommonUtil;
import com.core.utils.SpanUtils;
import com.core.utils.StatusBarUtil;
import com.core.widget.RecycleViewDivider;
import com.me.imageloader.ImageLoaderUtil;
import com.me.network.app.base.HttpParams;
import com.me.network.app.http.Method;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.scwang.smartrefresh.layout.listener.SimpleMultiPurposeListener;
import com.scwang.smartrefresh.layout.util.DensityUtil;
import com.uas.appworks.R;
import com.uas.appworks.adapter.CityIndustryCircleFuncAdapter;
import com.uas.appworks.adapter.CityIndustryNewsListAdapter;
import com.uas.appworks.model.bean.CityIndustryAnnounceBean;
import com.uas.appworks.model.bean.CityIndustryServiceBean;
import com.uas.appworks.presenter.WorkPlatPresenter;
import com.uas.appworks.view.WorkPlatView;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe 智慧产城首页
 * @date 2017/11/25 16:03
 */

public class CityIndustryServiceMainActivity extends BaseMVPActivity<WorkPlatPresenter> implements WorkPlatView {
    private final int ANNOUNCE_LOOP_FLAG = 0x01;
    private final int GET_DEFAULT_SERVICE = 0x02;
    private final int GET_LOOP_PICS = 0x03;

    //    private AppBarLayout mAppBarLayout;
    private RecyclerView mFuncRecyclerView, mNewsRecyclerView;
    private Toolbar mToolbar;
    private TextSwitcher mAnnounceTextSwitcher;
    private Banner mBanner;
    private NestedScrollView mNestedScrollView;
    private RefreshLayout mRefreshLayout;
    private ImageView mBackImageView;
    private FrameLayout mToastLayout;
    private View mFuncLine;
    private LinearLayout mAreaLinearLayout;
    private ImageView mAreaImageView;

    private List<CityIndustryAnnounceBean> mCityIndustryAnnounceBeans;
    private int mSwitcherCount = 0;
    private CityIndustryCircleFuncAdapter mCityIndustryFuncAdapter;
    private List<CityIndustryServiceBean> mServesBeans;
    private CityIndustryNewsListAdapter mCityIndustryNewsListAdapter;
    private int mScrollY = 0;
    private SpanUtils mSpanUtils;
    private boolean isAreaSpread = false, isLoopPicsFinish = false, isServiceFinish = false;
    private Animation mSpreadAnimation, mFoldAnimation;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                // 广告
                case ANNOUNCE_LOOP_FLAG:
                    int size = mCityIndustryAnnounceBeans.size();
                    mSpanUtils = new SpanUtils();
                    SpannableStringBuilder announce = mSpanUtils.append(mCityIndustryAnnounceBeans.get(mSwitcherCount % size).getTitle() + "  ").setForegroundColor(Color.RED)
                            .append(mCityIndustryAnnounceBeans.get(mSwitcherCount % size).getContent()).setForegroundColor(Color.BLACK).create();
                    mAnnounceTextSwitcher.setText(announce);
                    mSwitcherCount++;
                    if (mSwitcherCount == size) {
                        mSwitcherCount = 0;
                    }
                    mHandler.sendEmptyMessageDelayed(ANNOUNCE_LOOP_FLAG, 3000);
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mBanner.startAutoPlay();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBanner.stopAutoPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main_city_industry_service;
    }

    @Override
    protected void initView() {
        StatusBarUtil.immersive(this, 0x00000000, 0.0f);
        mToolbar = $(R.id.city_industry_service_main_toolbar);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        StatusBarUtil.setPaddingSmart(this, mToolbar);

        mBackImageView = $(R.id.city_industry_service_main_back_iv);
        mFuncRecyclerView = $(R.id.city_industry_service_main_func_rv);
        mNewsRecyclerView = $(R.id.city_industry_service_main_news_rv);
        mAnnounceTextSwitcher = $(R.id.city_industry_service_main_announce_ts);
        mBanner = $(R.id.city_industry_service_main_banner);
        mNestedScrollView = $(R.id.city_industry_service_main_nsv);
        mRefreshLayout = $(R.id.city_industry_service_main_refreshlayout);
        mFuncLine = $(R.id.city_industry_service_main_func_line);
        mToastLayout = $(R.id.city_industry_service_main_toast);
        mAreaLinearLayout = $(R.id.city_industry_service_main_area_ll);
        mAreaImageView = $(R.id.city_industry_service_main_area_iv);
        mSpreadAnimation = AnimationUtils.loadAnimation(ct, R.anim.anim_rotate_button_spread);
        mFoldAnimation = AnimationUtils.loadAnimation(ct, R.anim.anim_rotate_button_fold);

        mBanner.setImageLoader(new BannerImageLoader());

        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        viewPool.setMaxRecycledViews(0, 10);

        mFuncRecyclerView.setRecycledViewPool(viewPool);
        mFuncRecyclerView.setNestedScrollingEnabled(false);
        mFuncRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFuncRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));

        mNewsRecyclerView.setRecycledViewPool(viewPool);
        mNewsRecyclerView.setNestedScrollingEnabled(false);
        mNewsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mNewsRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light)));
        mNewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mServesBeans = new ArrayList<>();
        mCityIndustryFuncAdapter = new CityIndustryCircleFuncAdapter(this, mServesBeans);
        mFuncRecyclerView.setAdapter(mCityIndustryFuncAdapter);

        mCityIndustryNewsListAdapter = new CityIndustryNewsListAdapter(this);
        mNewsRecyclerView.setAdapter(mCityIndustryNewsListAdapter);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return new WorkPlatPresenter();
    }

    @Override
    protected void initEvent() {
        mBackImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            private int lastScrollY = 0;
            private int h = DensityUtil.dp2px(144);
            private int color = ContextCompat.getColor(getApplicationContext(), R.color.antionbarcolor) & 0x00ffffff;

            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (lastScrollY < h) {
                    scrollY = Math.min(h, scrollY);
                    mScrollY = scrollY > h ? h : scrollY;
                    mToolbar.setBackgroundColor(((255 * (mScrollY) / h) << 24) | color);
                }
                lastScrollY = scrollY;
            }
        });

        mRefreshLayout.setOnMultiPurposeListener(new SimpleMultiPurposeListener() {
            @Override
            public void onHeaderPulling(RefreshHeader header, float percent, int offset, int headerHeight, int extendHeight) {
                mToolbar.setAlpha(1 - Math.min(percent, 1));
            }

            @Override
            public void onHeaderReleasing(RefreshHeader header, float percent, int offset, int footerHeight, int extendHeight) {
                mToolbar.setAlpha(1 - Math.min(percent, 1));
            }
        });

        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(final RefreshLayout refreshLayout) {
                if (CommonUtil.isNetWorkConnected(ct)) {
                    getDefaultService();
                    getLoopPics();
                } else {
                    defaultServiceError(getString(R.string.networks_out));
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(0);
                    }
                }
            }
        });

        mCityIndustryFuncAdapter.setOnItemClickListener(new CityIndustryCircleFuncAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position == (mServesBeans.size() - 1)) {
                    Intent intent = new Intent();
                    intent.setClass(CityIndustryServiceMainActivity.this, CityIndustryFuncSetActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent("com.modular.work.CommonDataFormActivity");
                    intent.putExtra("serve_id", mServesBeans.get(position).getSv_id() + "");
                    intent.putExtra("title", mServesBeans.get(position).getSv_name() + "");
                    mContext.startActivity(intent);
                }
            }
        });

        mAreaLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isAreaSpread) {
                    mAreaImageView.startAnimation(mSpreadAnimation);
                    isAreaSpread = true;
                } else {
                    mAreaImageView.startAnimation(mFoldAnimation);
                    isAreaSpread = false;
                }
            }
        });

        mBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                toast(position + "", mToastLayout);
            }
        });
    }

    @Override
    protected void initData() {
        mRefreshLayout.autoRefresh(400, 1, 1f);

        initTextSwitcher();
    }

    private void getDefaultService() {
        Map<String, Object> params = new HashMap<>();
        params.put("kind", "cc");

        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("Cookie", CommonUtil.getB2BUid(this));

        HttpParams request = new HttpParams.Builder()
                .url("api/serve/getDefaultServices.action")
                .flag(GET_DEFAULT_SERVICE)
                .setParams(params)
                .setHeaders(header)
                .method(Method.GET)
                .build();
        mPresenter.cityRequest(this, request);
    }

    private void getLoopPics() {
        LinkedHashMap<String, Object> header = new LinkedHashMap<>();
        header.put("Cookie", CommonUtil.getB2BUid(this));

        HttpParams request = new HttpParams.Builder()
                .url("api/serve/mainPage/getRecyclePics.action")
                .flag(GET_LOOP_PICS)
                .setHeaders(header)
                .method(Method.GET)
                .build();
        mPresenter.cityRequest(this, request);
    }


    private void initTextSwitcher() {
        mCityIndustryAnnounceBeans = new ArrayList<>();
        mCityIndustryAnnounceBeans.add(new CityIndustryAnnounceBean.Builder()
                .title("[园区公告]")
                .content("企业为员工打造全新潼湖小镇")
                .build());
        mCityIndustryAnnounceBeans.add(new CityIndustryAnnounceBean.Builder()
                .title("[园区活动]")
                .content("下载UU互联有机会赢iPhoneX")
                .build());
        mCityIndustryAnnounceBeans.add(new CityIndustryAnnounceBean.Builder()
                .title("[园区新闻]")
                .content("智慧产城项目隆重开启")
                .build());

        mAnnounceTextSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            // 这里用来创建内部的视图，这里创建TextView，用来显示文字
            @Override
            public View makeView() {
                TextView textView = new TextView(getApplicationContext());
                textView.setSingleLine();
                textView.setTextSize(14);//字号
                textView.setTextColor(Color.parseColor("#000000"));
                textView.setEllipsize(TextUtils.TruncateAt.END);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.CENTER_VERTICAL;
                textView.setLayoutParams(params);
                return textView;
            }
        });

        mAnnounceTextSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = (mCityIndustryAnnounceBeans.size() + mSwitcherCount - 1) % mCityIndustryAnnounceBeans.size();
                toast(mCityIndustryAnnounceBeans.get(position).getTitle(), mToastLayout);
            }
        });

        mAnnounceTextSwitcher.setInAnimation(getApplicationContext(), R.anim.announce_enter_bottom);
        mAnnounceTextSwitcher.setOutAnimation(getApplicationContext(), R.anim.announce_leave_top);
        mHandler.sendEmptyMessage(ANNOUNCE_LOOP_FLAG);
    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void requestSuccess(int what, Object object) {
        LogUtil.d("industryservice", "result->" + object.toString());
        switch (what) {
            case GET_DEFAULT_SERVICE:
                analyzeDefaultSerivce(object);
                break;
            case GET_LOOP_PICS:
                isLoopPicsFinish = true;
                if (mRefreshLayout.isRefreshing() && isServiceFinish) {
                    mRefreshLayout.finishRefresh(0);
                }
                String result = object.toString();
                if (JSONUtil.validate(result)) {
                    JSONObject resultObject = JSON.parseObject(result);
                    JSONArray serveArray = resultObject.getJSONArray("serve");
                    if (serveArray != null) {
                        List<String> imgList = new ArrayList<>();
                        for (int i = 0; i < serveArray.size(); i++) {
                            JSONObject serveObject = serveArray.getJSONObject(i);
                            if (serveObject != null) {
                                String url = JSONUtil.getText(serveObject, "url");
                                imgList.add(url);
                            }
                        }
                        mBanner.update(imgList);
                        mBanner.start();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void analyzeDefaultSerivce(Object object) {
        isServiceFinish = true;
        if (mRefreshLayout.isRefreshing() && isLoopPicsFinish) {
            mRefreshLayout.finishRefresh(0);
        }
        String result = object.toString();
        Log.d("citydefaultservice", result);
        if (JSONUtil.validate(result)) {
            mServesBeans.clear();
            JSONObject resultObject = JSON.parseObject(result);
            JSONArray serviceArray = resultObject.getJSONArray("serves");
            if (serviceArray != null) {
                for (int i = 0; i < serviceArray.size(); i++) {
                    JSONObject serviceObject = serviceArray.getJSONObject(i);
                    if (serviceObject != null) {
                        CityIndustryServiceBean cityIndustryServiceBean = new CityIndustryServiceBean();
                        cityIndustryServiceBean.setSv_id(JSONUtil.getInt(serviceObject, "sv_id"));
                        cityIndustryServiceBean.setSv_name(JSONUtil.getText(serviceObject, "sv_name"));
                        cityIndustryServiceBean.setSv_logourl(JSONUtil.getText(serviceObject, "sv_logourl"));

                        mServesBeans.add(cityIndustryServiceBean);
                    }
                }
            }

            CityIndustryServiceBean cityIndustryServiceBean = new CityIndustryServiceBean();
            cityIndustryServiceBean.setSv_name("全部服务");
            cityIndustryServiceBean.setSv_logourl("moreMenu");
            mServesBeans.add(cityIndustryServiceBean);
        }
        if (mServesBeans.size() == 0) {
            CityIndustryServiceBean cityIndustryServiceBean = new CityIndustryServiceBean();
            cityIndustryServiceBean.setSv_name("全部服务");
            cityIndustryServiceBean.setSv_logourl("moreMenu");
            mServesBeans.add(cityIndustryServiceBean);
        }

        mFuncRecyclerView.setVisibility(View.VISIBLE);
        mFuncLine.setVisibility(View.VISIBLE);
        mCityIndustryFuncAdapter.notifyDataSetChanged();
    }

    @Override
    public void requestError(int what, String errorMsg) {
        switch (what) {
            case GET_DEFAULT_SERVICE:
                isServiceFinish = true;
                if (mRefreshLayout.isRefreshing() && isLoopPicsFinish) {
                    mRefreshLayout.finishRefresh(0);
                }
                defaultServiceError(errorMsg);
                break;
            case GET_LOOP_PICS:
                isLoopPicsFinish = true;
                if (mRefreshLayout.isRefreshing() && isServiceFinish) {
                    mRefreshLayout.finishRefresh(0);
                }
                break;
            default:
                break;
        }
    }

    private void defaultServiceError(String errorMsg) {
        mServesBeans.clear();
        CityIndustryServiceBean cityIndustryServiceBean = new CityIndustryServiceBean();
        cityIndustryServiceBean.setSv_name("全部服务");
        cityIndustryServiceBean.setSv_logourl("moreMenu");
        mServesBeans.add(cityIndustryServiceBean);

        mFuncRecyclerView.setVisibility(View.VISIBLE);
        mFuncLine.setVisibility(View.VISIBLE);
        mCityIndustryFuncAdapter.notifyDataSetChanged();
        toast(errorMsg, mToastLayout);
    }

    private class BannerImageLoader extends ImageLoader {
        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ImageLoaderUtil.getInstance().loadImage(path.toString(), imageView);
        }
    }
}

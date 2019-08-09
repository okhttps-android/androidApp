package com.modular.appmessages.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.LogUtil;
import com.common.data.StringUtil;
import com.core.utils.CommonUtil;
import com.core.widget.CustomProgressDialog;
import com.me.network.app.http.HttpClient;
import com.me.network.app.http.rx.Result2Listener;
import com.me.network.app.http.rx.ResultSubscriber;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;

/**
 * 时间：2017/10/30 17:18
 * 功能介绍：viewpager 懒加载
 */
public abstract class ViewPagerLazyFragment extends Fragment {
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    protected View rootView;
    protected AppCompatActivity ct;

    private boolean isVisible = false;//当前Fragment是否可见
    private boolean isInitView = false;//是否与View建立起映射关系
    private boolean isFirstLoad = true;//是否是第一次加载数据

    public CustomProgressDialog progressDialog;
    private HttpClient httpClient;

    /*防止内存重启出现重叠*/
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null)
            outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ct = (AppCompatActivity) context;
    }

    public View getRootView() {
        return rootView;
    }

    @Nullable
    public final <T extends View> T findViewById(int id) {
        if (rootView == null) {
            return null;
        } else {
            return rootView.findViewById(id);
        }
    }

    /*防止内存重启*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (rootView != null) {//有缓存,不创建
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
        } else {
            rootView = inflater.inflate(inflater(), container, false);
        }
        isInitView = true;
        progressDialog = CustomProgressDialog.createDialog(getContext());
        initHttpConfig();
        lazyLoadData();
        return rootView;
    }

    private void initHttpConfig() {
        String baseUrl = getBaseUrl();
        if (!StringUtil.isEmpty(baseUrl) && baseUrl.endsWith("/") && baseUrl.startsWith("http")) {
            httpClient = new HttpClient.Builder(baseUrl).isDebug(true)
                    .connectTimeout(5000)
                    .readTimeout(5000)
                    .build();
        }
    }

    @Override
    public void startActivity(Intent intent) {
        if (ct != null) {
            ct.startActivity(intent);
        } else {
            super.startActivity(intent);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            isVisible = true;
            lazyLoadData();
        } else {
            isVisible = false;
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void lazyLoadData() {
        if (isFirstLoad) {
        } else {
        }
        if (!isFirstLoad || !isVisible || !isInitView) {
            return;
        }
        LazyData();
        isFirstLoad = false;
    }

    public boolean isShowing() {
        return isVisible;
    }

    public void requestCompanyHttp(Parameter.Builder builder, final OnSmartHttpListener onHttpListener) {
        if (builder != null) {
            String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
            String emCode = CommonUtil.getEmcode();
            builder.addSuperParams("sessionId", sessionId);
            builder.addSuperParams("master", CommonUtil.getMaster());
            builder.addSuperParams("sessionUser", emCode);
            builder.addSuperHeaders("sessionUser", emCode);
            builder.addSuperHeaders("Cookie", "JSESSIONID=" + sessionId);
            requestHttp(builder, onHttpListener);
        }
    }

    public void requestHttp(Parameter.Builder builder, final OnSmartHttpListener onHttpListener) {
        if (httpClient != null && builder != null) {
            HttpClient.Builder httpBuilder = new HttpClient.Builder();
            final Parameter parameter = builder.builder();
            httpBuilder.addParams(parameter.getParams())
                    .addHeaders(parameter.getHeaders())
                    .method(parameter.getMode())
                    .url(parameter.getUrl())
                    .isDebug(parameter.showLog());
            if (parameter.autoProgress()) {
                progressDialog.show();
            }
            HttpClient mHttpClient = httpBuilder.build();
            httpClient.Api().send(mHttpClient, new ResultSubscriber<>(new Result2Listener<Object>() {
                @Override
                public void onResponse(Object o) {
                    if (onHttpListener != null) try {
                        Tags tags = parameter.getTag();
                        int record = 0;
                        if (tags != null) {
                            record = tags.getRecord();
                        }
                        String message = o.toString();
                        if (parameter.showLog()) {
                            LogUtil.prinlnLongMsg("SmartHttp", "onResponse=" + message);
                        }
                        onHttpListener.onSuccess(record, message, tags);
                    } catch (Exception e) {
                        LogUtil.i("gong", "onResponse=" + e.getMessage());
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(Object t) {
                    if (onHttpListener != null) try {
                        Tags tags = parameter.getTag();
                        int record = 0;
                        if (tags != null) {
                            record = tags.getRecord();
                        }
                        String message = t.toString();
                        if (parameter.showLog()) {
                            LogUtil.prinlnLongMsg("SmartHttp", "onFailure=" + message);
                        }
                        onHttpListener.onFailure(record, message, tags);
                    } catch (Exception e) {
                        LogUtil.i("oooo=" + e.getMessage());
                        e.printStackTrace();
                    }
                    progressDialog.dismiss();
                }
            }));
        }
    }

    /**
     * 加载要显示的数据
     */
    protected abstract void LazyData();

    protected abstract String getBaseUrl();

    protected abstract int inflater();


}

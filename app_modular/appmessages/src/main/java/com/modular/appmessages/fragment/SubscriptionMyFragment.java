package com.modular.appmessages.fragment;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.core.app.Constants;
import com.core.dao.DBManager;
import com.core.model.PersonalSubscriptionBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.modular.appmessages.R;
import com.modular.appmessages.activity.SubscribeDetailActivity;
import com.modular.appmessages.adapter.MySubscriptionAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 我的订阅
 * Created by RaoMeng on 2016/9/5.
 */
public class SubscriptionMyFragment extends BaseFragment {
    private PullToRefreshListView mPullListView;
    private List<PersonalSubscriptionBean> mPersonalSubscriptionBeans;
    private MySubscriptionAdapter mMySubscriptionAdapter;
    private EmptyLayout mEmptyLayout;
    private static int mClickedPosition = -1;
    private final static int GET_MY_SUBSCRIPTION = 12;
    private static int SUBSCRIBE_MY_DETAIL_REQUEST = 45;

    private DBManager mDbManager;
    private String currentMaster;
    private String currentUser;

    @Override
    protected int getLayout() {
        return R.layout.fragment_my_subscription;
    }

    @Override
    protected void initViews() {
        mDbManager = new DBManager(getActivity());
        currentMaster = CommonUtil.getSharedPreferences(getActivity(), "erp_master");
        currentUser = CommonUtil.getSharedPreferences(getActivity(), "erp_username");
        mPersonalSubscriptionBeans = new ArrayList<>();
        mPullListView = (PullToRefreshListView) root.findViewById(R.id.my_subscripton_smlv);
        mMySubscriptionAdapter = new MySubscriptionAdapter(getActivity(), mPersonalSubscriptionBeans);
        mEmptyLayout = new EmptyLayout(getActivity(), mPullListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage(getString(R.string.subscribe_nodata));
    }

    @Override
    protected void initEvents() {
        mPullListView.setAdapter(mMySubscriptionAdapter);

        mPullListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        mPullListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (CommonUtil.isNetWorkConnected(getActivity().getApplicationContext())) {
                    mPersonalSubscriptionBeans.clear();
                    sendSubscriptionRequest();
                } else {
                    if (mPullListView.isRefreshing()) {
                        mPullListView.onRefreshComplete(500);
                    }
                    ToastUtil.showToast(getActivity(),getString(R.string.common_notlinknet));
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                Toast.makeText(getActivity(), "refresh up success", Toast.LENGTH_SHORT).show();
                mPullListView.onRefreshComplete();
            }
        });

        mPullListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mClickedPosition = position;
                Intent intent = new Intent();
                intent.setClass(getActivity(), SubscribeDetailActivity.class);
                intent.putExtra("flag", "my");
                intent.putExtra("subId", mPersonalSubscriptionBeans.get(position - 1).getNUM_ID());
                intent.putExtra("subTitle", mPersonalSubscriptionBeans.get(position - 1).getTITLE());
                intent.putExtra("isApplied", mPersonalSubscriptionBeans.get(position - 1).getISAPPLED());
                startActivityForResult(intent, SUBSCRIBE_MY_DETAIL_REQUEST);
            }
        });
    }

    @Override
    protected void initDatas() {
        if (CommonUtil.isNetWorkConnected(getActivity().getApplicationContext())) {
            progressDialog.show();
            sendSubscriptionRequest();
        } else {
            List<PersonalSubscriptionBean> dbPersonalSubscriptionBeans = mDbManager.queryFromMySubs(new String[]{currentMaster, currentUser}, "subs_master=? and subs_username=? ");
            if (dbPersonalSubscriptionBeans != null) {
                mPersonalSubscriptionBeans.clear();
                mPersonalSubscriptionBeans.addAll(dbPersonalSubscriptionBeans);
                mMySubscriptionAdapter.notifyDataSetChanged();
                if (mPersonalSubscriptionBeans.size() == 0) {
                    mEmptyLayout.showEmpty();
                }
            } else {
                mEmptyLayout.setErrorMessage(getString(R.string.common_notlinknet));
                mEmptyLayout.showError();
            }
        }
    }

    /**
     * 获取当前用户的订阅号
     */
    private void sendSubscriptionRequest() {
        String subsUrl = CommonUtil.getAppBaseUrl(getActivity()) + "common/charts/getPersonalSubs.action";
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", CommonUtil.getSharedPreferences(getActivity(), "erp_username"));

        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getActivity(), "sessionId"));
        ViewUtil.httpSendRequest(getActivity(), subsUrl, params, mHandler, headers, GET_MY_SUBSCRIPTION, null, null, "post");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SUBSCRIBE_MY_DETAIL_REQUEST && resultCode == 44) {
            mDbManager.deleteFromMySubs(mPersonalSubscriptionBeans.get(mClickedPosition - 1));
            mPersonalSubscriptionBeans.remove(mClickedPosition - 1);
            mMySubscriptionAdapter.notifyDataSetChanged();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(getActivity()==null){
                return;
            }
            switch (msg.what) {
                case GET_MY_SUBSCRIPTION:
                    progressDialog.dismiss();
                    if (mDbManager.getDb().isOpen()) {
                        mDbManager.deleteMasterMySubs(currentMaster, currentUser);
                    }
                    String resultJson = msg.getData().getString("result");

                    try {
                        if (resultJson != null) {
                            JSONObject resultObject = new JSONObject(resultJson);
                            JSONArray resultArray = resultObject.getJSONArray("datas");
                            if (resultArray != null) {
                                if (resultArray.length() == 0) {
                                    mEmptyLayout.showEmpty();
                                    if (mPullListView.isRefreshing()) {
                                        mPullListView.onRefreshComplete();
                                        ToastUtil.showToast(getActivity(),getString(R.string.subscribe_nodata));
                                    }
                                } else {
                                    for (int i = 0; i < resultArray.length(); i++) {
                                        JSONObject currentObject = resultArray.getJSONObject(i);
                                        PersonalSubscriptionBean personalSubscriptionBean = new PersonalSubscriptionBean();
                                        personalSubscriptionBean.setNUM_ID(currentObject.optInt("NUM_ID"));
                                        personalSubscriptionBean.setTITLE(currentObject.optString("TITLE_"));
                                        personalSubscriptionBean.setKIND(currentObject.optString("KIND_"));
                                        personalSubscriptionBean.setTYPE(currentObject.optString("TYPE_"));
                                        personalSubscriptionBean.setISAPPLED(currentObject.optInt("ISAPPLIED_"));
                                        personalSubscriptionBean.setMASTER(currentMaster);
                                        personalSubscriptionBean.setUSERNAME(currentUser);
                                        String s = currentObject.optString("IMG_");
                                        if ("null".equals(currentObject.optString("IMG_")) || "".equals(currentObject.optString("IMG_"))) {
                                            personalSubscriptionBean.setIMG(new byte[0]);
                                        } else {
                                            personalSubscriptionBean.setIMG(Base64.decode(currentObject.optString("IMG_"), Base64.DEFAULT));
                                        }
                                        mPersonalSubscriptionBeans.add(personalSubscriptionBean);
                                    }
                                    mMySubscriptionAdapter.notifyDataSetChanged();
                                    if (mDbManager.getDb().isOpen()) {
                                        mDbManager.saveListToMySubs(mPersonalSubscriptionBeans);
                                    }
                                    if (mPullListView.isRefreshing()) {
                                        mPullListView.onRefreshComplete();
                                        ToastUtil.showToast(getActivity(),getString(R.string.common_refresh_finish));
                                    }
                                }
                            }
                        }

                        if (mPullListView.isRefreshing()) {
                            mPullListView.onRefreshComplete();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    ToastUtil.showToast(getActivity(),msg.getData().getString("result"));
                    if (mPullListView.isRefreshing()) {
                        mPullListView.onRefreshComplete();
                    }
                    break;

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbManager.closeDB();
    }
}

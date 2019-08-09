package com.modular.appmessages.fragment;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.preferences.SharedUtil;
import com.common.system.DisplayUtil;
import com.core.app.Constants;
import com.core.dao.DBManager;
import com.core.model.SubscriptionNumber;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.EmptyLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.modular.appmessages.R;
import com.modular.appmessages.activity.SubscribeDetailActivity;
import com.modular.appmessages.adapter.AllSubscriptionAdapter;
import com.modular.appmessages.adapter.SubsTypeAdapter;
import com.modular.appmessages.model.AllSubscriptonKindMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 所有订阅
 * Created by RaoMeng on 2016/9/5.
 */

public class SubscriptionAllFragment extends BaseFragment {
    private PullToRefreshListView mPullToRefreshListView;
    private AllSubscriptionAdapter mAllSubscriptionAdapter;
    private EmptyLayout mEmptyLayout;
    private int rbPos = 0;
    private List<String> keyStrings = new ArrayList<>();//当前显示的订阅类
    private List<String> mAllKeyStrings = new ArrayList<>();//所有订阅类
    private final static int SUBSCRIBE_DETAIL_REQUEST = 33;
    private static int mClickedPos = -1;
    private String currentMaster = "";//当前账套
    private String currentUser = "";//当前账号
    private DBManager mDbManager;
    private PopupWindow mDeletePopupWindow;

    private ListView mTypeListView;
    private SubsTypeAdapter mSubsTypeAdapter;

    //适配器数据
    private List<AllSubscriptonKindMessage> mAllSubscriptonKindMessages;
    private List<SubscriptionNumber> mSubscriptionNumbers;
    //本地缓存数据
    private List<AllSubscriptonKindMessage> mDbAllSubscriptonKindMessages;
    private List<SubscriptionNumber> mDbSubscriptionNumbers;
    //网络获取数据
    private List<AllSubscriptonKindMessage> mNetAllSubscriptonKindMessages;
    private List<SubscriptionNumber> mNetSubscriptionNumbers;

    private String mBufferKey = null;

    @Override
    protected int getLayout() {
        return R.layout.fragment_all_subscription;
    }

    @Override
    protected void initViews() {
        mDbManager = new DBManager(getActivity());
        currentMaster = CommonUtil.getSharedPreferences(getActivity(), "erp_master");
        currentUser = CommonUtil.getSharedPreferences(getActivity(), "erp_username");
        mPullToRefreshListView = (PullToRefreshListView) root.findViewById(R.id.subscription_all_ptlv);
        mSubscriptionNumbers = new ArrayList<>();
        mAllSubscriptonKindMessages = new ArrayList<>();
        mAllSubscriptionAdapter = new AllSubscriptionAdapter(getActivity(), mSubscriptionNumbers);

        mDbAllSubscriptonKindMessages = new ArrayList<>();
        mDbSubscriptionNumbers = new ArrayList<>();
        mNetAllSubscriptonKindMessages = new ArrayList<>();
        mNetSubscriptionNumbers = new ArrayList<>();

        mTypeListView = (ListView) root.findViewById(R.id.subscription_all_type_lv);
        mSubsTypeAdapter = new SubsTypeAdapter(getActivity(), keyStrings);
        mEmptyLayout = new EmptyLayout(getActivity(), mPullToRefreshListView.getRefreshableView());
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setEmptyMessage("没有任何可订阅号");
    }

    @Override
    protected void initEvents() {
        mTypeListView.setAdapter(mSubsTypeAdapter);
        mPullToRefreshListView.setAdapter(mAllSubscriptionAdapter);
        mPullToRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (CommonUtil.isNetWorkConnected(getActivity().getApplicationContext())) {
//                    progressDialog.setCancelable(false);
//                    rbPos = 0;
                    sendAllSubscriptionRequest();
                } else {
                    if (mPullToRefreshListView.isRefreshing()) {
                        mPullToRefreshListView.onRefreshComplete(500);
                    }
                    ToastUtil.showToast(getActivity(), getString(R.string.common_notlinknet));
                }
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                ToastUtil.showToast(getActivity(), "上拉加载" );
                mPullToRefreshListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshListView.onRefreshComplete();

                    }
                }, 1000);
            }
        });
        mPullToRefreshListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mClickedPos = position;
                Intent intent = new Intent();
                intent.setClass(getActivity(), SubscribeDetailActivity.class);
                intent.putExtra("flag", "all");
                intent.putExtra("subId", mAllSubscriptionAdapter.getmSubscriptionNumbers().get(position - 1).getId());
                intent.putExtra("subTitle", mAllSubscriptionAdapter.getmSubscriptionNumbers().get(position - 1).getTitle());
                intent.putExtra("subStatus", mAllSubscriptionAdapter.getmSubscriptionNumbers().get(position - 1).getStatus());
                startActivityForResult(intent, SUBSCRIBE_DETAIL_REQUEST);
            }
        });

        mPullToRefreshListView.getRefreshableView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                View popView = View.inflate(getActivity(), R.layout.pop_subs_delete, null);
                TextView deleteTv = (TextView) popView.findViewById(R.id.pop_subs_delete_tv);
                deleteTv.setText(R.string.subscription_number);
                deleteTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAllSubscriptionAdapter.getmSubscriptionNumbers().get(position - 1).setRemoved(1);
                        mDbManager.updateAllSubs(mAllSubscriptionAdapter.getmSubscriptionNumbers().get(position - 1));
                        mAllSubscriptionAdapter.getmSubscriptionNumbers().remove(position - 1);

                        mAllSubscriptionAdapter.notifyDataSetChanged();
                        mAllSubscriptonKindMessages.get(rbPos).setSubscriptionNumbers(mAllSubscriptionAdapter.getmSubscriptionNumbers());

                        if (mAllSubscriptionAdapter.getmSubscriptionNumbers().size() == 0) {
                            mAllSubscriptonKindMessages.remove(rbPos);
                            keyStrings.remove(rbPos);
                            if (keyStrings.size() == 0) {
                                mSubsTypeAdapter.notifyDataSetChanged();
                                mTypeListView.setVisibility(View.GONE);
                                mEmptyLayout.showEmpty();
                                mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                                mAllSubscriptionAdapter.notifyDataSetChanged();
                            } else {
                                rbPos = 0;
                                mBufferKey = keyStrings.get(0);
                                mTypeListView.setVisibility(View.VISIBLE);
                                mSubsTypeAdapter.notifyDataSetChanged();
                                mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                                mAllSubscriptionAdapter.getmSubscriptionNumbers().addAll(mAllSubscriptonKindMessages.get(rbPos).getSubscriptionNumbers());
                                mAllSubscriptionAdapter.notifyDataSetChanged();
                            }
                            initKeyStrings();

                        }
                        closeDeletePopupWindow();
                    }
                });

                mDeletePopupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                mDeletePopupWindow.setOutsideTouchable(true);
                mDeletePopupWindow.setFocusable(true);
                mDeletePopupWindow.setBackgroundDrawable(new BitmapDrawable());
                mDeletePopupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                DisplayUtil.backgroundAlpha(getActivity(), 0.5f);
                mDeletePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        closeDeletePopupWindow();
                    }
                });

                return true;
            }
        });

        mTypeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSubsTypeAdapter.setSelectItem(position);
                mSubsTypeAdapter.notifyDataSetChanged();
                mBufferKey = keyStrings.get(position);
                rbPos = position;
                if (mAllSubscriptonKindMessages.get(rbPos).getSubscriptionNumbers().size() != 0) {
                    mAllSubscriptionAdapter = new AllSubscriptionAdapter(getActivity(), mAllSubscriptonKindMessages.get(rbPos).getSubscriptionNumbers());
                    mPullToRefreshListView.getRefreshableView().setAdapter(mAllSubscriptionAdapter);
                }
            }
        });
        mTypeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                View popView = View.inflate(getActivity(), R.layout.pop_subs_delete, null);
                TextView deleteTv = (TextView) popView.findViewById(R.id.pop_subs_delete_tv);
                deleteTv.setText(R.string.subscription_class);
                deleteTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<SubscriptionNumber> tempSubs = mAllSubscriptonKindMessages.get(position).getSubscriptionNumbers();
                        for (int i = 0; i < tempSubs.size(); i++) {
                            tempSubs.get(i).setRemoved(1);
                        }
                        mDbManager.updateListAllSubs(tempSubs);
//                        mDbManager.deleteListFromAllSubs(mAllSubscriptonKindMessages.get(position).getSubscriptionNumbers());
                        mAllSubscriptonKindMessages.remove(position);
                        keyStrings.remove(position);
                        if (keyStrings.size() == 0) {
                            mSubsTypeAdapter.notifyDataSetChanged();
                            mTypeListView.setVisibility(View.GONE);
                            mEmptyLayout.showEmpty();
                            mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                            mAllSubscriptionAdapter.notifyDataSetChanged();
                        } else {
                            if (rbPos != 0) {
                                rbPos = 0;
                                mBufferKey = keyStrings.get(0);
                                mTypeListView.setVisibility(View.VISIBLE);
                                mSubsTypeAdapter.notifyDataSetChanged();

                                mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                                mAllSubscriptionAdapter.getmSubscriptionNumbers().addAll(mAllSubscriptonKindMessages.get(rbPos).getSubscriptionNumbers());

                                mAllSubscriptionAdapter.notifyDataSetChanged();
                            }
                        }
                        initKeyStrings();
                        closeDeletePopupWindow();
                    }
                });

                mDeletePopupWindow = new PopupWindow(popView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                mDeletePopupWindow.setOutsideTouchable(true);
                mDeletePopupWindow.setFocusable(true);
                mDeletePopupWindow.setBackgroundDrawable(new BitmapDrawable());
                mDeletePopupWindow.showAtLocation(getActivity().getWindow().getDecorView(), Gravity.CENTER, 0, 0);
                DisplayUtil.backgroundAlpha(getActivity(), 0.5f);
                mDeletePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        closeDeletePopupWindow();
                    }
                });

                return true;
            }
        });
    }

    @Override
    protected void initDatas() {
        if (CommonUtil.isNetWorkConnected(getActivity().getApplicationContext())) {
            //每次初始化页面都需要访问网络数据用来更新本地缓存
            progressDialog.show();
            sendAllSubscriptionRequest();
        } else {
            //没有网络时，取本地数据库数据
            getDbSubsData();
        }

    }

    private void initKeyStrings() {
        StringBuilder keyStringBuilder = null;
        if (keyStrings.size() != 0) {
            keyStringBuilder = new StringBuilder();
            for (int i = 0; i < keyStrings.size(); i++) {
                keyStringBuilder.append("," + keyStrings.get(i));

                if (i == rbPos) {
                    mSubsTypeAdapter.setSelectItem(i);
                    mSubsTypeAdapter.notifyDataSetChanged();
                }
            }
            if (keyStringBuilder.length() > 2) {
                keyStringBuilder.delete(0, 1);
            }
            SharedUtil.putString(currentMaster + currentUser + "subs", keyStringBuilder.toString());
        } else {
            SharedUtil.putString(currentMaster + currentUser + "subs", null);
        }

    }

    private void closeDeletePopupWindow() {
        if (mDeletePopupWindow != null) {
            mDeletePopupWindow.dismiss();
            mDeletePopupWindow = null;
            DisplayUtil.backgroundAlpha(getActivity(), 1f);

        }

    }

    /**
     * 获取本地数据库全部订阅
     */
    public void getDbSubsData() {
        mAllSubscriptonKindMessages.clear();
        String cacheKeys = SharedUtil.getString(currentMaster + currentUser + "subs");
        if (cacheKeys != null) {
            String[] cacheKeysArray = cacheKeys.split(",");
            keyStrings.clear();
            for (int i = 0; i < cacheKeysArray.length; i++) {
                keyStrings.add(cacheKeysArray[i]);
            }
        }
//        rbPos = 0;
        List<SubscriptionNumber> dbSubscriptionNumbers
                = mDbManager.queryFromAllSubs(new String[]{currentMaster, currentUser}, "subs_master=? and subs_username=? ");
        if (dbSubscriptionNumbers == null || dbSubscriptionNumbers.size() == 0) {
            mEmptyLayout.showEmpty();
        } else {
            if (keyStrings.size() == 0) {
                mSubsTypeAdapter.notifyDataSetChanged();
                mTypeListView.setVisibility(View.GONE);
                mEmptyLayout.showEmpty();
                mSubscriptionNumbers.clear();
                mAllSubscriptionAdapter.notifyDataSetChanged();
            } else {
                mTypeListView.setVisibility(View.VISIBLE);
                mSubsTypeAdapter.notifyDataSetChanged();
                for (int i = 0; i < keyStrings.size(); i++) {
                    AllSubscriptonKindMessage tempSubscriptonKindMessage = new AllSubscriptonKindMessage();
                    List<SubscriptionNumber> tempSubscriptionNumbers = new ArrayList<>();
                    String currentKey = keyStrings.get(i);
                    for (int j = 0; j < dbSubscriptionNumbers.size(); j++) {
                        SubscriptionNumber currentSubscriptionNumber = dbSubscriptionNumbers.get(j);
                        if (currentSubscriptionNumber.getType().equals(currentKey)
                                && currentSubscriptionNumber.getRemoved() != 1
                                && currentSubscriptionNumber.getStatus() != 1) {
                            tempSubscriptionNumbers.add(currentSubscriptionNumber);
                        }
                    }
                    tempSubscriptonKindMessage.setSubscriptionKind(currentKey);
                    tempSubscriptonKindMessage.setSubscriptionNumbers(tempSubscriptionNumbers);
                    mAllSubscriptonKindMessages.add(tempSubscriptonKindMessage);
                    if (mBufferKey != null && mBufferKey.equals(keyStrings.get(i))) {
                        rbPos = i;
                    }
                }
                mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                mAllSubscriptionAdapter.getmSubscriptionNumbers().addAll(mAllSubscriptonKindMessages.get(rbPos).getSubscriptionNumbers());
                Log.d("allsubscription:", mSubscriptionNumbers.toString());
                mAllSubscriptionAdapter.notifyDataSetChanged();
            }
            initKeyStrings();
        }
    }


    /**
     * 获取网络数据全部订阅
     */
    public void sendAllSubscriptionRequest() {
        //取出本地缓存数据
        mDbSubscriptionNumbers = mDbManager.queryFromAllSubs(new String[]{currentMaster, currentUser}, "subs_master=? and subs_username=? ");
        String allSubsUrl = CommonUtil.getAppBaseUrl(getActivity()) + "common/charts/getApplySubs.action";
        Map<String, Object> params = new HashMap<>();
        params.put("em_code", CommonUtil.getSharedPreferences(getActivity(), "erp_username"));

        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(getActivity(), "sessionId"));
        ViewUtil.httpSendRequest(getActivity(), allSubsUrl, params, mHandler, headers, GET_ALL_SUBSCRIPTION, null, null, "post");
    }

    private final static int GET_ALL_SUBSCRIPTION = 44;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_ALL_SUBSCRIPTION:
                    mAllSubscriptonKindMessages.clear();
                    if (mDbManager.getDb().isOpen()) {
                        mDbManager.deleteMasterAllSubs(currentMaster, currentUser);
                    }
                    String resultJson = msg.getData().getString("result");
                    if (resultJson == null) {
                        progressDialog.dismiss();
                        if (mPullToRefreshListView.isRefreshing()) {
                            mPullToRefreshListView.onRefreshComplete();
                        }
                        mTypeListView.setVisibility(View.GONE);
                        mEmptyLayout.showEmpty();
                        mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                        mAllSubscriptionAdapter.notifyDataSetChanged();
                        return;
                    }
                    try {
                        JSONObject resultObject = new JSONObject(resultJson);
                        JSONArray datasArray = resultObject.getJSONArray("datas");
                        JSONObject datasObject = datasArray.getJSONObject(0);
                        Iterator<String> iterator = datasObject.keys();
                        if (!iterator.hasNext()) {
                            SharedUtil.putString(currentMaster + currentUser + "subs", null);
                            SharedUtil.putString(currentMaster + currentUser + "allsubs", null);
                            mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                            mAllSubscriptionAdapter.notifyDataSetChanged();
                            mEmptyLayout.showEmpty();
                            if (mPullToRefreshListView.isRefreshing()) {
                                mPullToRefreshListView.onRefreshComplete();
                                ToastUtil.showToast(getActivity(), "没有未订阅数据");
                            }
                            keyStrings.clear();
                            mAllKeyStrings.clear();
                            progressDialog.dismiss();
                            mSubsTypeAdapter.notifyDataSetChanged();
                            mTypeListView.setVisibility(View.GONE);
                            return;
                        }

                        keyStrings.clear();
                        mAllKeyStrings.clear();
                        while (iterator.hasNext()) {
                            String key = iterator.next().toString();
                            Log.d("allsubskeys: ", key);
                            mAllKeyStrings.add(key);
                            AllSubscriptonKindMessage subscriptonKindMessage = new AllSubscriptonKindMessage();
                            subscriptonKindMessage.setSubscriptionKind(key);
                            mNetSubscriptionNumbers = new ArrayList<>();
                            List<SubscriptionNumber> subscriptionNumbers = new ArrayList<>();
                            JSONArray subsArray = datasObject.getJSONArray(key);
                            Log.d("allsubsarray: ", subsArray.toString());

                            for (int i = 0; i < subsArray.length(); i++) {
                                JSONObject subsObject = subsArray.getJSONObject(i);
                                SubscriptionNumber subscriptionNumber = new SubscriptionNumber();
                                subscriptionNumber.setId(subsObject.optInt("id"));
                                subscriptionNumber.setTitle(subsObject.optString("title"));
                                subscriptionNumber.setKind(subsObject.optString("kind"));
                                subscriptionNumber.setStatus(subsObject.optInt("status"));
                                subscriptionNumber.setType(key);
                                subscriptionNumber.setMaster(currentMaster);
                                subscriptionNumber.setUsername(currentUser);
                                subscriptionNumber.setRemoved(0);
                                if ("null".equals(subsObject.optString("img")) || "".equals(subsObject.optString("img"))) {
                                    subscriptionNumber.setImg(new byte[0]);
                                } else {
                                    subscriptionNumber.setImg(Base64.decode(subsObject.optString("img"), Base64.DEFAULT));
                                }
                                mNetSubscriptionNumbers.add(subscriptionNumber);
                            }
                            if (mDbSubscriptionNumbers == null || mDbSubscriptionNumbers.size() == 0) {
                                //如果本地数据为空，说明是第一次请求网络数据，直接将获取的数据存入数据库
                                mDbManager.saveListToAllSubs(mNetSubscriptionNumbers);
                            } else {
                                //如果本地数据不为空，说明之前存储过本地数据，则将网络数据中的removed属性更新为本地存储的数据
                                for (int i = 0; i < mNetSubscriptionNumbers.size(); i++) {
                                    for (int j = 0; j < mDbSubscriptionNumbers.size(); j++) {
                                        if (mNetSubscriptionNumbers.get(i).getId() == mDbSubscriptionNumbers.get(j).getId()) {
                                            mNetSubscriptionNumbers.get(i).setRemoved(mDbSubscriptionNumbers.get(j).getRemoved());
                                        }
                                    }
                                }
                                if (mDbManager.getDb().isOpen()) {
                                    mDbManager.saveListToAllSubs(mNetSubscriptionNumbers);
                                }
                            }

                            //遍历更新removed属性的列表，筛选出stautus不等于1且removed不等于1的数据
                            for (int i = 0; i < mNetSubscriptionNumbers.size(); i++) {
                                if (mNetSubscriptionNumbers.get(i).getStatus() != 1 && mNetSubscriptionNumbers.get(i).getRemoved() != 1) {
                                    subscriptionNumbers.add(mNetSubscriptionNumbers.get(i));
                                }
                            }

                            if (subscriptionNumbers.size() != 0) {
                                keyStrings.add(key);
                                subscriptonKindMessage.setSubscriptionNumbers(subscriptionNumbers);
                                mSubsTypeAdapter.notifyDataSetChanged();
                                mAllSubscriptonKindMessages.add(subscriptonKindMessage);
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (mAllKeyStrings.size() == 0) {
                        SharedUtil.putString(currentMaster + currentUser + "allsubs", null);
                    } else {
                        StringBuilder allKeyString = new StringBuilder();
                        for (int i = 0; i < mAllKeyStrings.size(); i++) {
                            allKeyString.append("," + mAllKeyStrings.get(i));
                        }
                        if (allKeyString.length() > 2) {
                            allKeyString.delete(0, 1);
                        }
                        SharedUtil.putString(currentMaster + currentUser + "allsubs", allKeyString.toString());
                    }
                    if (keyStrings.size() == 0) {
                        mTypeListView.setVisibility(View.GONE);
                        mEmptyLayout.showEmpty();
                        mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                        mAllSubscriptionAdapter.notifyDataSetChanged();
                    } else {
                        if (mBufferKey != null) {
                            for (int i = 0; i < keyStrings.size(); i++) {
                                if (mBufferKey.equals(keyStrings.get(i))) {
                                    rbPos = i;
                                }
                            }
                        }
                        mTypeListView.setVisibility(View.VISIBLE);
                        mSubsTypeAdapter.notifyDataSetChanged();
                        mAllSubscriptionAdapter.getmSubscriptionNumbers().clear();
                        mAllSubscriptionAdapter.getmSubscriptionNumbers().addAll(mAllSubscriptonKindMessages.get(rbPos).getSubscriptionNumbers());
                        Log.d("allsubscription:", mAllSubscriptionAdapter.getmSubscriptionNumbers().toString());
                        mAllSubscriptionAdapter.notifyDataSetChanged();
                    }
                    initKeyStrings();
                    progressDialog.setCancelable(true);
                    progressDialog.dismiss();

                    if (mPullToRefreshListView.isRefreshing()) {
                        mPullToRefreshListView.onRefreshComplete();
                        if (getActivity() != null)
                            ToastUtil.showToast(getActivity(), "刷新成功");
                    }
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    if (msg.getData().getString("result") != null)
                        if (getActivity() != null)
                            ToastUtil.showToast(getActivity(), msg.getData().getString("result"));
                    if (mPullToRefreshListView.isRefreshing()) {
                        mPullToRefreshListView.onRefreshComplete();
                    } else {
                        getDbSubsData();
                    }
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBSCRIBE_DETAIL_REQUEST && resultCode == 9 && data != null) {
            int statu = data.getIntExtra("status", 0);
            if (mClickedPos != -1) {
                mAllSubscriptionAdapter.getmSubscriptionNumbers().get(mClickedPos - 1).setStatus(statu);
                mDbManager.updateAllSubs(mAllSubscriptionAdapter.getmSubscriptionNumbers().get(mClickedPos - 1));
                mAllSubscriptionAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDbManager.closeDB();
    }
}

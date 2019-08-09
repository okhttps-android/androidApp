package com.modular.appmessages.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.common.data.ListUtils;
import com.core.app.Constants;
import com.core.base.BaseActivity;
import com.core.dao.DBManager;
import com.core.model.SubscriptionNumber;
import com.core.utils.CommonUtil;
import com.core.widget.EmptyLayout;
import com.modular.appmessages.R;
import com.modular.appmessages.adapter.AllRemovedSubsAdapter;
import com.modular.appmessages.model.AllSubscriptonKindMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 未订阅号整理页面
 * Created by RaoMeng on 2016/10/28.
 */
public class ManageAllSubscriptionActivity extends BaseActivity {
    private DBManager mDbManager;
    private String currentMaster;//当前账套
    private String currentUser;//当前账号
    private List<SubscriptionNumber> dbSubscriptionNumbers;//数据库数据
    private List<SubscriptionNumber> mSubscriptionNumbers;//被移除的订阅数据
    private List<Object> keyStrings;
    private List<Object> removedKeyStrings;

    private ListView mRemovedListView;
    private EmptyLayout mEmptyLayout;
    private List<AllSubscriptonKindMessage> mAllSubscriptonKindMessages;
    private AllRemovedSubsAdapter mAllRemovedSubsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.subscribe_unsetting));
        setContentView(R.layout.activity_manage_all_subs);

        initViews();
        initEvents();
        initDatas();

    }

    private void initDatas() {
        Intent intent = new Intent();
        setResult(Constants.RESULT_MANAGE_ALL_SUBSCRIPTION, intent);
    }

    private void initEvents() {
        if (dbSubscriptionNumbers != null && dbSubscriptionNumbers.size() != 0) {
            for (int i = 0; i < dbSubscriptionNumbers.size(); i++) {
                SubscriptionNumber subscriptionNumber = dbSubscriptionNumbers.get(i);
                if (subscriptionNumber.getRemoved() == 1 && subscriptionNumber.getStatus() != 1){
                    mSubscriptionNumbers.add(subscriptionNumber);
                }
            }
            if (mSubscriptionNumbers == null || mSubscriptionNumbers.size() == 0){
                mEmptyLayout.showEmpty();
            }else {
                for (int i = 0; i < mSubscriptionNumbers.size(); i++) {
                    removedKeyStrings.add(mSubscriptionNumbers.get(i).getType());
                }
                keyStrings = ListUtils.getSingleElement(removedKeyStrings);

                for (int i = 0; i < keyStrings.size(); i++) {
                    List<SubscriptionNumber> tempSubscriptionNumbers = null;
                    String key = keyStrings.get(i).toString();
                    AllSubscriptonKindMessage subscriptonKindMessage = new AllSubscriptonKindMessage();
                    subscriptonKindMessage.setSubscriptionKind(key);
                    tempSubscriptionNumbers = new ArrayList<>();
                    for (int j = 0; j < mSubscriptionNumbers.size(); j++) {
                        SubscriptionNumber tempSubscriptionNumber = mSubscriptionNumbers.get(j);
                        if (tempSubscriptionNumber.getType().equals(key)){
                            tempSubscriptionNumbers.add(tempSubscriptionNumber);
                        }
                    }
                    if (tempSubscriptionNumbers != null && tempSubscriptionNumbers.size() != 0){
                        subscriptonKindMessage.setSubscriptionNumbers(tempSubscriptionNumbers);
                    }
                    mAllSubscriptonKindMessages.add(subscriptonKindMessage);
                }
                mAllRemovedSubsAdapter.notifyDataSetChanged();
            }

        }else {
            mEmptyLayout.showEmpty();
        }

    }

    private void initViews() {
        mRemovedListView = (ListView) findViewById(R.id.manage_all_subs_lv);
        mSubscriptionNumbers = new ArrayList<>();
        mAllSubscriptonKindMessages = new ArrayList<>();

        mAllRemovedSubsAdapter = new AllRemovedSubsAdapter(mAllSubscriptonKindMessages,this);
        mRemovedListView.setAdapter(mAllRemovedSubsAdapter);
        mEmptyLayout = new EmptyLayout(this, mRemovedListView);
        mEmptyLayout.setShowLoadingButton(false);
        mEmptyLayout.setShowErrorButton(false);
        mEmptyLayout.setShowEmptyButton(false);
        mEmptyLayout.setEmptyMessage(getString(R.string.subscribe_hide_nodata));
        keyStrings = new ArrayList<>();
        removedKeyStrings = new ArrayList<>();
        mDbManager = new DBManager(this);
        currentMaster = CommonUtil.getSharedPreferences(this, "erp_master");
        currentUser = CommonUtil.getSharedPreferences(this, "erp_username");
        dbSubscriptionNumbers =
                mDbManager.queryFromAllSubs(new String[]{currentMaster, currentUser}, "subs_master=? and subs_username=? ");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbManager.closeDB();
    }
}

package com.uas.appworks.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.DateFormatUtil;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.base.presenter.BasePresenter;
import com.uas.appworks.R;
import com.uas.appworks.adapter.B2BDetailListAdapter;
import com.uas.appworks.model.bean.B2BDetailListBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe 已注册企业详情页
 * @date 2018/3/25 18:10
 */

public class RegisterDetailActivity extends BaseMVPActivity {
    private RecyclerView mRecyclerView;
    private List<B2BDetailListBean> mListBeanList;
    private B2BDetailListAdapter mListAdapter;
    private String mEnterpriseInfo;
    private int mEnterpriseFlag = -1;

    @Override
    protected int getLayout() {
        return R.layout.activity_register_detail;
    }

    @Override
    protected void initView() {
       setTitle(R.string.enterprise_register_detail);

        Intent intent = getIntent();
        if (intent != null) {
            mEnterpriseInfo = intent.getStringExtra(Constants.FLAG.REGISTERED_ENTERPRISE_INFO);
            mEnterpriseFlag = intent.getIntExtra(Constants.FLAG.REGISTERED_ENTERPRISE_FLAG, -1);
        }
        mRecyclerView = $(R.id.register_detail_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(false);

        mListBeanList = new ArrayList<>();
        mListAdapter = new B2BDetailListAdapter(this, mListBeanList);
        mRecyclerView.setAdapter(mListAdapter);
    }

    @Override
    protected BasePresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {

    }

    @Override
    protected void initData() {
        if (mEnterpriseInfo != null) {
            if (JSONUtil.validate(mEnterpriseInfo)) {
                if (mEnterpriseFlag != -1) {
                    String enName = "", enAddress = "", enCorporation = "", adminName = "", enTel = "", registerDate = "", inviteUserName = "", inviteEnName = "";
                    if (mEnterpriseFlag == Constants.FLAG.REGISTERED_DETAIL) {
                        try {
                            JSONArray resultArray = JSON.parseArray(mEnterpriseInfo);
                            if (resultArray != null && resultArray.size() > 0) {
                                JSONObject resultObject = resultArray.getJSONObject(0);
                                if (resultObject != null) {
                                    enName = JSONUtil.getText(resultObject, "enName");
                                    enAddress = JSONUtil.getText(resultObject, "enAddress");
                                    enCorporation = JSONUtil.getText(resultObject, "enCorporation");
                                    adminName = JSONUtil.getText(resultObject, "adminName");
                                    enTel = JSONUtil.getText(resultObject, "enTel");
                                    long date = resultObject.getLongValue("date");
                                    registerDate = "";
                                    if (date != 0) {
                                        registerDate = DateFormatUtil.long2Str(date, DateFormatUtil.YMD);
                                    }
                                    inviteUserName = JSONUtil.getText(resultObject, "inviteUserName");
                                    inviteEnName = JSONUtil.getText(resultObject, "inviteEnName");
                                }
                            }
                        } catch (Exception e) {

                        }
                    } else if (mEnterpriseFlag == Constants.FLAG.REGISTERED_LIST) {
                        try {
                            JSONObject enterpriseObject = JSON.parseObject(mEnterpriseInfo);
                            if (enterpriseObject != null) {
                                enName = JSONUtil.getText(enterpriseObject, "vendname");
                                enAddress = JSONUtil.getText(enterpriseObject, "enAddress");
                                enCorporation = JSONUtil.getText(enterpriseObject, "enCorporation");
                                adminName = JSONUtil.getText(enterpriseObject, "adminName");
                                enTel = JSONUtil.getText(enterpriseObject, "enTel");
                                long date = enterpriseObject.getLongValue("registerDate");
                                registerDate = "";
                                if (date != 0) {
                                    registerDate = DateFormatUtil.long2Str(date, DateFormatUtil.YMD);
                                }
                                inviteUserName = JSONUtil.getText(enterpriseObject, "inviteUserName");
                                inviteEnName = JSONUtil.getText(enterpriseObject, "inviteEnName");

                            }
                        } catch (Exception e) {

                        }
                    }
                    mListBeanList.add(createListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.enterprise_name), enName));
                    mListBeanList.add(createListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.caption_enterprise_address), enAddress));
                    mListBeanList.add(createListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.caption_enterprise_corporation), enCorporation));
                    mListBeanList.add(createListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.caption_enterprise_admin), adminName));
                    mListBeanList.add(createListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.str_contact_number), enTel));
                    mListBeanList.add(createListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.str_register_date), registerDate));
                    mListBeanList.add(createListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.caption_inviter), inviteUserName));
                    mListBeanList.add(createListBean(B2BDetailListBean.TYPE_DETAIL_TEXT_WHITE, getString(R.string.caption_invite_enterprise), inviteEnName));

                    mListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private B2BDetailListBean createListBean(int itemType,
                                             String caption,
                                             String value) {
        B2BDetailListBean b2BDetailListBean = new B2BDetailListBean();

        b2BDetailListBean.setItemType(itemType);
        b2BDetailListBean.setCaption(caption);
        b2BDetailListBean.setValue(value);

        return b2BDetailListBean;
    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }
}

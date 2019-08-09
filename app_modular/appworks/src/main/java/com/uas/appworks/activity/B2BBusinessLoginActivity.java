package com.uas.appworks.activity;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.common.data.JSONUtil;
import com.core.app.Constants;
import com.core.base.activity.BaseMVPActivity;
import com.core.utils.CommonUtil;
import com.core.widget.RecycleViewDivider;
import com.module.recyclerlibrary.listener.OnRecyclerItemClickListener;
import com.uas.appworks.R;
import com.uas.appworks.adapter.B2BBusinessCompanyAdapter;
import com.uas.appworks.model.bean.B2BCompanyBean;
import com.uas.appworks.presenter.WorkPlatPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe B2B商务登录页面
 * @date 2018/1/15 15:41
 */

public class B2BBusinessLoginActivity extends BaseMVPActivity {

    private RecyclerView mRecyclerView;
    private List<B2BCompanyBean> mB2BCompanyBeans;
    private B2BBusinessCompanyAdapter mB2BBusinessCompanyAdapter;

    @Override
    protected int getLayout() {
        return R.layout.activity_b2b_business_login;
    }

    @Override
    protected void initView() {
       setTitle(R.string.str_work_b2b_commerce);

        mRecyclerView = $(R.id.b2b_business_login_company_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.addItemDecoration(new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light)));

        mB2BCompanyBeans = new ArrayList<>();
        mB2BBusinessCompanyAdapter = new B2BBusinessCompanyAdapter(mContext, mB2BCompanyBeans);
        mRecyclerView.setAdapter(mB2BBusinessCompanyAdapter);
    }

    @Override
    protected WorkPlatPresenter initPresenter() {
        return null;
    }

    @Override
    protected void initEvent() {
        mRecyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(mRecyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder vh) {
                B2BCompanyBean b2BCompanyBean = mB2BCompanyBeans.get(vh.getLayoutPosition());
                CommonUtil.setSharedPreferences(mContext, Constants.CACHE.B2B_BUSINESS_ENUU, b2BCompanyBean.getEnuu());

                Intent intent = new Intent();
                intent.setClass(mContext, B2BBusinessMainActivity.class);
                intent.putExtra(Constants.FLAG.B2B_COMPANY_BEAN, b2BCompanyBean);
                startActivity(intent);
                B2BBusinessLoginActivity.this.finish(false);
            }
        });
    }

    @Override
    protected void initData() {
        getB2BCompanys();
    }

    private void getB2BCompanys() {
        String companyJson = CommonUtil.getSharedPreferences(this, "loginJson");
        if (JSONUtil.validate(companyJson)) {
            JSONArray companyArray = JSON.parseArray(companyJson);
            if (companyArray != null && companyArray.size() > 0) {
                for (int i = 0; i < companyArray.size(); i++) {
                    JSONObject companyObject = companyArray.getJSONObject(i);
                    if (companyObject != null) {
                        String platform = JSONUtil.getText(companyObject, "platform");
                        if ("B2B".equals(platform)) {
                            JSONArray spacesArray = companyObject.getJSONArray("spaces");
                            if (spacesArray != null && spacesArray.size() > 0) {
                                for (int j = 0; j < spacesArray.size(); j++) {
                                    JSONObject spacesObject = spacesArray.getJSONObject(j);
                                    if (spacesObject != null) {
                                        B2BCompanyBean b2BCompanyBean = new B2BCompanyBean();
                                        b2BCompanyBean.setId(JSONUtil.getInt(spacesObject, "id"));
                                        b2BCompanyBean.setEnuu(JSONUtil.getText(spacesObject, "enuu"));
                                        b2BCompanyBean.setBusinessCode(JSONUtil.getText(spacesObject, "businessCode"));
                                        b2BCompanyBean.setName(JSONUtil.getText(spacesObject, "name"));

                                        mB2BCompanyBeans.add(b2BCompanyBean);
                                    }
                                }
                                mB2BBusinessCompanyAdapter.notifyDataSetChanged();
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void showLoading(String loadStr) {

    }

    @Override
    public void hideLoading() {

    }
}

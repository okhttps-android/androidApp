package com.uas.appworks.activity.businessManage.businessChangeStage;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.core.base.activity.MvpBaseActivity;
import com.core.widget.ClearEditText;
import com.uas.appworks.R;
import com.uas.appworks.adapter.ChangeStageAdapter;
import com.uas.appworks.model.bean.BusinessStageBean;
import com.uas.appworks.model.bean.ChangeStageBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/22 11:26
 */
public class BusinessChangeStageActivity extends MvpBaseActivity<BusinessChangeStageContract.IBusinessChangeStagePresenter>
        implements BusinessChangeStageContract.IBusinessChangeStageView {

    private TextView mStageTextView;
    private BusinessStageBean mNextBusinessStage, mCurrentBusinessStage;
    private String mWhichPage = "", mBcCode;
    private RecyclerView mRecyclerView;
    private TextView mConfirmButton;
    private ClearEditText mRemarkEditText;
    private List<ChangeStageBean> mChangeStageBeans;
    private ChangeStageAdapter mChangeStageAdapter;

    @Override
    protected int getLayout() {
        return R.layout.activity_business_change_stage;
    }

    @Override
    protected void initView() {
        Intent intent = getIntent();
        if (intent != null) {
            mNextBusinessStage = (BusinessStageBean) intent.getSerializableExtra("nextStage");
            mCurrentBusinessStage = (BusinessStageBean) intent.getSerializableExtra("currentStage");
            mWhichPage = intent.getStringExtra("whichPage");
            mBcCode = intent.getStringExtra("bc_code");
        }
        if (mNextBusinessStage == null) {
            mNextBusinessStage = new BusinessStageBean();
        }
        if (mCurrentBusinessStage == null) {
            mCurrentBusinessStage = new BusinessStageBean();
        }

        mConfirmButton = $(R.id.business_change_stage_confirm_btn);
        mStageTextView = $(R.id.business_change_stage_current_stage_tv);
        mRemarkEditText = $(R.id.business_change_stage_remarks_et);
        mRecyclerView = $(R.id.business_change_stage_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mChangeStageBeans = new ArrayList<>();
        mChangeStageAdapter = new ChangeStageAdapter(mChangeStageBeans);

        if ("businessDetail".equals(mWhichPage)) {
            setTitle("填写商机记录");
        } else {
            setTitle("商机阶段详情");
            mRecyclerView.setAdapter(mChangeStageAdapter);
        }
    }

    @Override
    protected BusinessChangeStageContract.IBusinessChangeStagePresenter initPresenter() {
        return new BusinessChangeStagePresenterImpl();
    }

    @Override
    protected void initEvent() {
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<ChangeStageBean> stageAdapterData = mChangeStageAdapter.getData();
                Log.e("raoStages", stageAdapterData.toString());
                String remarks = mRemarkEditText.getText().toString();
                if (TextUtils.isEmpty(remarks)) {
                    toast("阶段处理结果不能为空");
                    return;
                }
                if ("businessStage".equals(mWhichPage)) {
                    for (int i = 0; i < stageAdapterData.size(); i++) {
                        ChangeStageBean changeStageBean = stageAdapterData.get(i);
                        if (changeStageBean.getIsRequired() == 1 && TextUtils.isEmpty(changeStageBean.getValue())) {
                            toast("阶段要点：" + changeStageBean.getName() + "不能为空");
                            return;
                        }
                    }
                }
                mPresenter.requestChangeStage(mContext, stageAdapterData, mCurrentBusinessStage, mNextBusinessStage, remarks, mBcCode);
            }
        });
    }

    @Override
    protected void initData() {
        if (mNextBusinessStage != null) {
            mStageTextView.setText(mNextBusinessStage.getBS_NAME());

            String bs_posint = mNextBusinessStage.getBS_POINT();
            String bs_poiniflag = mNextBusinessStage.getBS_POINTFLAG();
            String bs_pointdeino = mNextBusinessStage.getBS_POINTDETNO();
            if (!StringUtil.isEmpty(bs_posint) && !StringUtil.isEmpty(bs_poiniflag)
                    && !StringUtil.isEmpty(bs_pointdeino)) {
                String[] names = bs_posint.split("#");
                String[] flags = bs_poiniflag.split("#");
                String[] keys = bs_pointdeino.split("#");
                for (int j = 0; j < names.length; j++) {
                    ChangeStageBean changeStageBean = new ChangeStageBean();
                    changeStageBean.setName(names[j]);
                    if (flags.length > j) {
                        changeStageBean.setIsRequired(Integer.valueOf(flags[j]));
                    }
                    if (keys.length > j) {
                        changeStageBean.setStageKey(keys[j]);
                    }
                    changeStageBean.setValue("");
                    mChangeStageBeans.add(changeStageBean);
                }

                if ("businessStage".equals(mWhichPage)) {
                    mChangeStageAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void showLoading(String loadStr) {
        progressDialog.show();
    }

    @Override
    public void hideLoading() {
        progressDialog.dismiss();
    }

    @Override
    public void changeStageSuccess() {
        if ("businessDetail".equals(mWhichPage)) {
            toast("商机记录提交成功");
        } else {
            toast("商机阶段变更成功");
        }
        mPresenter.requestUpdataSchedule(this, mBcCode);
    }

    @Override
    public void updateScheduleSuccess() {
        toast("更新商机状态成功");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!BusinessChangeStageActivity.this.isDestroyed()) {
                    finish();
                }
            }
        }, 1500);
    }

    @Override
    public void requestFail(int flag, String failStr) {
        toast(failStr);
    }
}

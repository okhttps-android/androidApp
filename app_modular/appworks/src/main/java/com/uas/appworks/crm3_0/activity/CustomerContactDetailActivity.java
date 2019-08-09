package com.uas.appworks.crm3_0.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.common.system.SystemUtil;
import com.core.app.Constants;
import com.core.utils.CommonUtil;
import com.me.network.app.http.Method;
import com.modular.apputils.activity.BillDetailsActivity;
import com.modular.apputils.adapter.BillDetailsAdapter;
import com.modular.apputils.adapter.BillListAdapter;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.model.BillJump;
import com.modular.apputils.model.BillListGroupModel;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.UUHttpHelper;
import com.modular.apputils.widget.VeriftyDialog;
import com.uas.appworks.R;
import com.uas.appworks.activity.SchedulerCreateActivity;
import com.uas.appworks.model.Schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomerContactDetailActivity extends BillDetailsActivity {

    private String phone;
    private int mContactId;

    @Override
    public int getLayoutId() {
        return R.layout.activity_customer_contact_details;
    }

    @Override
    protected void initView() {
        super.initView();
        findViewById(R.id.callBtn).setOnClickListener(mOnClickListener);
        findViewById(R.id.scheduleBtn).setOnClickListener(mOnClickListener);
        findViewById(R.id.deleteBtn).setOnClickListener(mOnClickListener);
        findViewById(R.id.shareBtn).setOnClickListener(mOnClickListener);

        if (getIntent() != null) {
            List<BillListGroupModel.BillListField> fields = getIntent().getParcelableArrayListExtra(Constants.Intents.BILL_LIST_FIELD_FORWARD);
            setAdapter2(fields);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (com.modular.apputils.R.id.edit == item.getItemId()) {
            HashMap<String, String> dbfindCondition = new HashMap<>();
            dbfindCondition.put("cu_code", "cu_sellercode='" + CommonUtil.getEmcode() + "'");
            String mCaller = "Contact";
            String mTitle = "客户联系人";
            Class clazz = CustomerContactActivity.class;
            startActivity(new Intent(ct, clazz)
                    .putExtra(Constants.Intents.CALLER, mCaller)
                    .putExtra(Constants.Intents.TITLE, mTitle)
                    .putExtra(Constants.Intents.ID, mBillDetailsPresenter.getId())
                    .putExtra(Constants.Intents.MY_DOIT, true)
                    .putExtra(Constants.Intents.DB_FIND_CONDITION, dbfindCondition));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setAdapter(List<BillGroupModel> groupModels) {

    }

    public void setAdapter2(List<BillListGroupModel.BillListField> fields) {
        MAdapter mMAdapter = new MAdapter(fields);
        mRecyclerView.setAdapter(mMAdapter);
        if (fields != null) {
            for (BillListGroupModel.BillListField field : fields) {
                if (field.getCaption().equals("ID")) try {
                    mContactId = Integer.valueOf(field.getValue());
                } catch (Exception e) {

                }
                else {
                    if (field.getCaption().equals("电话")) {
                        phone = field.getValue();
                    }
                }
            }
        }
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.callBtn) {
                if (!TextUtils.isEmpty(phone)) {
                    SystemUtil.phoneAction(ct, phone);
                }
            } else if (view.getId() == R.id.scheduleBtn) {
                String mRemarks = "";
                Schedule mSchedule = new Schedule(Schedule.TYPE_UU);
                mSchedule.setRemarks(mRemarks);
                mSchedule.setTag("客户联系人日程");
                startActivityForResult(new Intent(ct, SchedulerCreateActivity.class)
                        .putExtra(com.uas.appworks.datainquiry.Constants.Intents.ENABLE, true)
                        .putExtra(com.uas.appworks.datainquiry.Constants.Intents.MODEL, mSchedule), 0x11);
            } else if (view.getId() == R.id.deleteBtn) {
                new VeriftyDialog.Builder(ct)
                        .setTitle(getString(R.string.prompt_title))
                        .setContent("是否确认删除当前明细表")
                        .build(new VeriftyDialog.OnDialogClickListener() {
                            @Override
                            public void result(boolean clickSure) {
                                if (clickSure) {
                                    deleteDetail();
                                }
                            }
                        });
            } else if (view.getId() == R.id.shareBtn) {
            }
        }
    };

    public void deleteDetail() {
        showLoading();
        new UUHttpHelper(CommonUtil.getAppBaseUrl(ct))
                .requestCompanyHttp(new Parameter.Builder()
                                .url("common/deleteDetail.action")
                                .addParams("caller", mBillDetailsPresenter.getCaller())
                                .addParams("gridcaller", mBillDetailsPresenter.getCaller())
                                .mode(Method.POST)
                                .addParams("condition", "ct_id=" + mContactId)
                        , new OnSmartHttpListener() {
                            @Override
                            public void onSuccess(int what, String message, Tags tag) throws Exception {
                                showToast("删除成功");
                                dimssLoading();
                                setResult(RESULT_OK);
                                finish();
                            }

                            @Override
                            public void onFailure(int what, String message, Tags tag) throws Exception {
                                if (JSONUtil.validateJSONObject(message)) {
                                    showToast(JSONUtil.getText(message, "exceptionInfo"));
                                } else {
                                    showToast(message);
                                }
                                dimssLoading();
                            }
                        });
    }

    class MAdapter extends RecyclerView.Adapter<MAdapter.ViewHolder> {
        private List<BillListGroupModel.BillListField> fields;

        public MAdapter(List<BillListGroupModel.BillListField> fields) {
            this.fields = fields;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new ViewHolder(LayoutInflater.from(ct).inflate(com.modular.apputils.R.layout.item_bill_details, viewGroup, false));

        }

        @Override
        public void onBindViewHolder(ViewHolder mViewHolder, int position) {
            BillListGroupModel.BillListField field = fields.get(position);
            mViewHolder.captionTv.setText(field.getCaption());
            mViewHolder.valuesTv.setText(field.getValue());
            if (field.getCaption().contains("手机") || field.getCaption().contains("电话")) {
                mViewHolder.valuesTv.setTag(com.modular.apputils.R.id.tag_key, 1);
                mViewHolder.valuesTv.setTag(field.getValue());
                mViewHolder.valuesTv.setOnClickListener(mOnClickListener);
            } else {
                mViewHolder.valuesTv.setOnClickListener(null);
            }
            mViewHolder.titleTv.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return ListUtils.getSize(fields);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private TextView titleTv;
            private TextView captionTv;
            private TextView valuesTv;

            public ViewHolder(View itemView) {
                super(itemView);
                titleTv = (TextView) itemView.findViewById(com.modular.apputils.R.id.titleTv);
                captionTv = (TextView) itemView.findViewById(com.modular.apputils.R.id.captionTv);
                valuesTv = (TextView) itemView.findViewById(com.modular.apputils.R.id.valuesTv);
            }
        }
    }
}

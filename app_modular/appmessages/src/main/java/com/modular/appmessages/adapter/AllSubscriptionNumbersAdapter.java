package com.modular.appmessages.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.core.app.Constants;
import com.core.model.SubscriptionNumber;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.CustomProgressDialog;
import com.modular.appmessages.R;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RaoMeng on 2016/9/7.
 */
public class AllSubscriptionNumbersAdapter extends BaseAdapter {
    private Context mContext;
    private List<SubscriptionNumber> mSubscriptionNumbers;
    private int clickPosition;
    private int status;
    protected CustomProgressDialog progressDialog;

    public AllSubscriptionNumbersAdapter(Context mContext, List<SubscriptionNumber> mSubscriptionNumbers) {
        this.mContext = mContext;
        this.mSubscriptionNumbers = mSubscriptionNumbers;

        progressDialog = CustomProgressDialog.createDialog(mContext);
    }

    @Override
    public int getCount() {
        return mSubscriptionNumbers.size();
    }

    @Override
    public Object getItem(int position) {
        return mSubscriptionNumbers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null){
            convertView = View.inflate(mContext, R.layout.layout_all_subscription_number_list,null);
            viewHolder = new ViewHolder();

            viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.all_subscription_list_name_tv);
            viewHolder.applyTextView = (TextView) convertView.findViewById(R.id.all_subscription_apply_tv);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.nameTextView.setText(mSubscriptionNumbers.get(position).getTitle());
        status = mSubscriptionNumbers.get(position).getStatus();

        if (status == 1){
            viewHolder.nameTextView.setTextColor(mContext.getResources().getColor(R.color.gray));
            viewHolder.nameTextView.setBackground(mContext.getResources().getDrawable(R.drawable.shape_subscribe_selet_bg));
            viewHolder.applyTextView.setText("已订阅");
            viewHolder.applyTextView.setEnabled(false);
        }else if (status == 3){
            viewHolder.nameTextView.setTextColor(mContext.getResources().getColor(R.color.white));
            viewHolder.nameTextView.setBackground(mContext.getResources().getDrawable(R.drawable.shape_subscribe_bg));
            CommonUtil.textUnderlineForStyle(viewHolder.applyTextView,"申请","申请");
            viewHolder.applyTextView.setText("申请");
            viewHolder.applyTextView.setEnabled(true);
        }else if (status == 2){
            viewHolder.nameTextView.setTextColor(mContext.getResources().getColor(R.color.gray));
            viewHolder.nameTextView.setBackground(mContext.getResources().getDrawable(R.drawable.shape_subscribe_selet_bg));
            viewHolder.applyTextView.setText(R.string.subscribe_requested);
            viewHolder.applyTextView.setEnabled(false);
        }

        viewHolder.applyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.setClass(mContext, ApplySubscribeActivity.class);
//                mContext.startActivity(intent);

                clickPosition = position;
                sendApplySubs(clickPosition);
            }
        });

        return convertView;
    }

    /**
     * 申请订阅
     */
    private void sendApplySubs(int position) {
        progressDialog.show();
        String applyUrl = CommonUtil.getAppBaseUrl(mContext) + "common/charts/vastAddSubsApply.action";
        Map<String,Object> params = new HashMap<>();
        params.put("ids",mSubscriptionNumbers.get(position).getId());
        params.put("caller","VastAddSubsApply");

        LinkedHashMap<String,Object> headers = new LinkedHashMap<>();
        headers.put("Cookie","JSESSIONID="+ CommonUtil.getSharedPreferences(mContext,"sessionId"));
        ViewUtil.httpSendRequest(mContext,applyUrl,params,mHandler,headers,APPLY_SUBSCRIPTION,null,null,"post");
    }

    class ViewHolder{
        TextView nameTextView;
        TextView applyTextView;
    }

    private final static int APPLY_SUBSCRIPTION = 101;
    private Handler mHandler = new Handler(){
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case APPLY_SUBSCRIPTION:
                    progressDialog.dismiss();
                    Log.d("applysubscription", msg.getData().getString("result"));
                    CommonUtil.imageToast(mContext,R.drawable.ic_apply_submit_success,"",2000);
                    mSubscriptionNumbers.get(clickPosition).setStatus(2);
                    notifyDataSetChanged();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                   ToastUtil.showToast(mContext,msg.getData().getString("result"));
                    break;
            }
        }
    };
}

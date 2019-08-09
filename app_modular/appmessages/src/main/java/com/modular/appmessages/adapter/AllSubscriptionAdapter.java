package com.modular.appmessages.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.ui.ImageUtil;
import com.core.app.Constants;
import com.core.dao.DBManager;
import com.core.model.SubscriptionNumber;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.utils.ToastUtil;
import com.core.widget.CustomProgressDialog;
import com.modular.appmessages.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RaoMeng on 2016/9/7.
 */
public class AllSubscriptionAdapter extends BaseAdapter {
    private Context mContext;
    private List<SubscriptionNumber> mSubscriptionNumbers;
    private List<Integer> mImageIds;
    private int clickPosition;
    protected CustomProgressDialog progressDialog;
    private DBManager mDbManager;


    public AllSubscriptionAdapter(Context mContext, List<SubscriptionNumber> mSubscriptionNumbers) {
        this.mContext = mContext;
        this.mSubscriptionNumbers = mSubscriptionNumbers;
        mImageIds = new ArrayList<>();
        mDbManager = new DBManager(mContext);
        progressDialog = CustomProgressDialog.createDialog(mContext);
//        mImageIds.add(R.drawable.ic_subscription_icon1);
//        mImageIds.add(R.drawable.ic_subscription_icon2);
//        mImageIds.add(R.drawable.ic_subscription_icon3);
//        mImageIds.add(R.drawable.ic_subscription_icon4);
//        mImageIds.add(R.drawable.ic_subscription_icon5);
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.layout_all_subscription_list, null);
            viewHolder = new ViewHolder();

            viewHolder.subImageView = (ImageView) convertView.findViewById(R.id.all_subscribe_iv);
            viewHolder.subNameTextView = (TextView) convertView.findViewById(R.id.all_subscribe_title_tv);
            viewHolder.applyTextView = (TextView) convertView.findViewById(R.id.all_subscribe_apply_tv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (mSubscriptionNumbers.get(position).getImg() != null) {
            byte[] imgBytes = mSubscriptionNumbers.get(position).getImg();
            if (imgBytes.length != 0) {
                Bitmap bitmap = ImageUtil.compressBitmapWithByte(imgBytes,40,40);
                viewHolder.subImageView.setImageBitmap(bitmap);
            }else {
                viewHolder.subImageView.setImageResource(R.drawable.ic_subscription_number);
            }
        } else {
            viewHolder.subImageView.setImageResource(R.drawable.ic_subscription_number);
        }

        viewHolder.subNameTextView.setText(mSubscriptionNumbers.get(position).getTitle());
        if (mSubscriptionNumbers.get(position).getStatus() == 1) {
            viewHolder.applyTextView.setText("已订阅");
            viewHolder.applyTextView.setTextColor(Color.GRAY);
            viewHolder.applyTextView.setEnabled(false);
        } else if (mSubscriptionNumbers.get(position).getStatus() == 3) {
            CommonUtil.textUnderlineForStyle(viewHolder.applyTextView,"申请","申请");
            viewHolder.applyTextView.setText(R.string.subscribe_detail_commit);
            viewHolder.applyTextView.setTextColor(Color.BLUE);
            viewHolder.applyTextView.setEnabled(true);
        } else if (mSubscriptionNumbers.get(position).getStatus() == 2) {
            viewHolder.applyTextView.setText(R.string.subscribe_requested);
            viewHolder.applyTextView.setTextColor(Color.GRAY);
            viewHolder.applyTextView.setEnabled(false);
        }

        viewHolder.applyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        Map<String, Object> params = new HashMap<>();
        params.put("ids", mSubscriptionNumbers.get(position).getId());
        params.put("caller", "VastAddSubsApply");

        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));
        ViewUtil.httpSendRequest(mContext, applyUrl, params, mHandler, headers, APPLY_SUBSCRIPTION, null, null, "post");
    }


    private final static int APPLY_SUBSCRIPTION = 101;
    private Handler mHandler = new Handler() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case APPLY_SUBSCRIPTION:
                    progressDialog.dismiss();
                    Log.d("applysubscription", msg.getData().getString("result"));
                    CommonUtil.imageToast(mContext, R.drawable.ic_apply_submit_success, "", 2000);
                    mSubscriptionNumbers.get(clickPosition).setStatus(2);
                    mDbManager.updateAllSubs(mSubscriptionNumbers.get(clickPosition));
                    notifyDataSetChanged();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    ToastUtil.showToast(mContext, msg.getData().getString("result"));
                    break;
            }
        }
    };

    class ViewHolder {
        ImageView subImageView;
        TextView subNameTextView;
        TextView applyTextView;
    }

    public List<SubscriptionNumber> getmSubscriptionNumbers() {
        return mSubscriptionNumbers;
    }

    public void setmSubscriptionNumbers(List<SubscriptionNumber> mSubscriptionNumbers) {
        this.mSubscriptionNumbers = mSubscriptionNumbers;
    }
}

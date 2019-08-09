package com.modular.appmessages.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.system.DisplayUtil;
import com.common.ui.ImageUtil;
import com.core.app.Constants;
import com.core.dao.DBManager;
import com.core.model.PersonalSubscriptionBean;
import com.core.net.http.ViewUtil;
import com.core.utils.CommonUtil;
import com.core.widget.CustomProgressDialog;
import com.core.widget.crouton.Crouton;
import com.modular.appmessages.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by RaoMeng on 2016/9/1.
 */
public class MySubscriptionAdapter extends BaseAdapter {
    private Context mContext;
    private List<PersonalSubscriptionBean> mPersonalSubscriptionBeans;
    private List<Integer> mImageIds;
    protected CustomProgressDialog progressDialog;
    private final static int REMOVE_MY_SUBSCRIPTION = 13;
    private int mCanclePosition;
    private PopupWindow mCancleWindow;
    private DBManager mDbManager;

    public MySubscriptionAdapter(Context mContext, List<PersonalSubscriptionBean> mPersonalSubscriptionBeans) {
        this.mContext = mContext;
        this.mPersonalSubscriptionBeans = mPersonalSubscriptionBeans;
        mDbManager = new DBManager(mContext);
        progressDialog = CustomProgressDialog.createDialog(mContext);
        mImageIds = new ArrayList<>();
//        mImageIds.add(R.drawable.ic_subscription_icon1);
//        mImageIds.add(R.drawable.ic_subscription_icon2);
//        mImageIds.add(R.drawable.ic_subscription_icon3);
//        mImageIds.add(R.drawable.ic_subscription_icon4);
//        mImageIds.add(R.drawable.ic_subscription_icon5);
    }

    @Override
    public int getCount() {
        return mPersonalSubscriptionBeans.size();
    }

    @Override
    public Object getItem(int i) {
        return mPersonalSubscriptionBeans.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null){
            view = View.inflate(mContext, R.layout.layout_my_subscribe_list, null);
            viewHolder = new ViewHolder();
            viewHolder.subscribeIv = (ImageView) view.findViewById(R.id.my_subscribe_iv);
            viewHolder.titleTv = (TextView) view.findViewById(R.id.my_subscribe_title_tv);
            viewHolder.applyTv = (TextView) view.findViewById(R.id.my_subscribe_apply_tv);
            view.setTag(viewHolder);

        }else {
            viewHolder = (ViewHolder) view.getTag();
        }

        if (mPersonalSubscriptionBeans.get(i).getIMG() != null){
            byte[] imgBytes = mPersonalSubscriptionBeans.get(i).getIMG();
            if (imgBytes.length != 0){
                Bitmap bitmap = ImageUtil.compressBitmapWithByte(imgBytes,40,40);
                viewHolder.subscribeIv.setImageBitmap(bitmap);
            }else {
                viewHolder.subscribeIv.setImageResource(R.drawable.ic_subscription_number);
            }
        }else {
            viewHolder.subscribeIv.setImageResource(R.drawable.ic_subscription_number);
        }

        viewHolder.titleTv.setText(mPersonalSubscriptionBeans.get(i).getTITLE());
        if (mPersonalSubscriptionBeans.get(i).getISAPPLED() == -1){
            viewHolder.applyTv.setText(R.string.unsubscribe);
            viewHolder.applyTv.setTextColor(Color.BLUE);
            viewHolder.applyTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View cancleView = View.inflate(mContext, R.layout.pop_cancle_my_subscribe, null);
                    TextView cancleTextView = (TextView) cancleView.findViewById(R.id.cancel_subscribe_cancle_tv);
                    TextView contineTextView = (TextView) cancleView.findViewById(R.id.cancel_subscribe_contine_tv);

                    cancleTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCanclePosition = i;
                            sendRemoveRequest(mCanclePosition);
                            closeWarningPopupWindow();
                        }
                    });

                    contineTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            closeWarningPopupWindow();
                        }
                    });

                    mCancleWindow = new PopupWindow(cancleView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
                    mCancleWindow.setAnimationStyle(R.style.MenuAnimationFade);
                    mCancleWindow.showAtLocation(viewGroup, Gravity.BOTTOM, 0, 0);
                    DisplayUtil.backgroundAlpha(mContext, 0.5f);

                    mCancleWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            closeWarningPopupWindow();
                        }
                    });
                }
            });
        }else if (mPersonalSubscriptionBeans.get(i).getISAPPLED() == 0){
            viewHolder.applyTv.setText(R.string.unsubscribe_able);
            viewHolder.applyTv.setTextColor(Color.GRAY);
        }

        return view;
    }


    /**
     * 取消订阅
     */
    private void sendRemoveRequest(int position) {
        progressDialog.show();
        String subsUrl = CommonUtil.getAppBaseUrl(mContext) + "common/charts/removeSubsMans.action";
        Map<String, Object> params = new HashMap<>();
        params.put("emcode", CommonUtil.getSharedPreferences(mContext, "erp_username"));
        params.put("numIds", mPersonalSubscriptionBeans.get(position).getNUM_ID());

        LinkedHashMap headers = new LinkedHashMap();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(mContext, "sessionId"));
        ViewUtil.httpSendRequest(mContext, subsUrl, params, mHandler, headers, REMOVE_MY_SUBSCRIPTION, null, null, "post");

    }

    private void closeWarningPopupWindow() {
        if (mCancleWindow != null) {
            mCancleWindow.dismiss();
            mCancleWindow = null;
            DisplayUtil.backgroundAlpha(mContext, 1f);

        }

    }
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case REMOVE_MY_SUBSCRIPTION:
                    progressDialog.dismiss();
                    Crouton.makeText(mContext, "取消订阅成功");
                    mDbManager.deleteFromMySubs(mPersonalSubscriptionBeans.get(mCanclePosition));
                    mPersonalSubscriptionBeans.remove(mCanclePosition);
                    notifyDataSetChanged();
                    break;
                case Constants.APP_SOCKETIMEOUTEXCEPTION:
                    progressDialog.dismiss();
                    Crouton.makeText(mContext, msg.getData().getString("result"), 1500);
                    break;
            }
        }
    };

    class ViewHolder{
        ImageView subscribeIv;
        TextView titleTv;
        TextView applyTv;
    }
}

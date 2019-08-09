package com.xzjmyk.pm.activity.util.dialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.common.LogUtil;
import com.common.system.DisplayUtil;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.secretary.BookingDetailActivity;
import com.xzjmyk.pm.activity.ui.erp.model.QSCModel;
import com.modular.booking.model.BookingModel;
import com.common.data.ListUtils;
import com.core.widget.MyListView;
import com.core.app.MyActivityManager;

import java.util.ArrayList;

/**
 * Created by FANGlh on 2017/8/7.
 * function:
 */

public class QSComShowPpUtils {
    public QSComShowPpUtils() {
    }
    public static void qSComShowPp(final int code, ArrayList<QSCModel> qscModelsList, final String realList){
        View contentView = LayoutInflater.from(MyActivityManager.getCurrentActivity()).inflate(R.layout.com_show_menu, null);
        DisplayMetrics dm =MyActivityManager.getCurrentActivity().getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        int h_screen = dm.heightPixels;
        w_screen = DisplayUtil.dip2px(MyActivityManager.getCurrentActivity(), 350);
        h_screen = DisplayUtil.dip2px(MyActivityManager.getCurrentActivity(), 400);
        final PopupWindow popupWindow = new PopupWindow(contentView, w_screen, h_screen, true);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(false);
        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        popupWindow.setBackgroundDrawable( MyActivityManager.getCurrentActivity().getResources().getDrawable(R.drawable.pop_round_bg));
        // 设置好参数之后再show
        popupWindow.showAtLocation(contentView, Gravity.CENTER, 0, 0);
        QSearchPpwindowUtils.setbg(MyActivityManager.getCurrentActivity(),popupWindow,0.4f);
        TextView title_tv = (TextView) contentView.findViewById(R.id.title);
        initTitle(code,title_tv);

        MyListView mlist = (MyListView) contentView.findViewById(R.id.qs_common_mlist);

        mlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                enterItemJudge(code,position,realList,view);
                popupWindow.dismiss();
            }
        });
        LogUtil.prinlnLongMsg("qscModelsList1",JSON.toJSONString(qscModelsList));
        Log.d("CurrentActivity()",MyActivityManager.getCurrentActivity()+"null");

        ComShowAdapter mAdapter = new ComShowAdapter(MyApplication.getInstance());
        mAdapter.setModels(qscModelsList);
        mlist.setAdapter(mAdapter);
    }

    private static void initTitle(int code, TextView title_tv) {
        String title = "";
        switch (code){
            case 0x01:
                title = "我的预约";
                break;

            default:
        }
        title_tv.setText(title+"");
    }

    private static void enterItemJudge(int code, int position, String realList, View view) {
        switch (code){
            case 0x01:
                try {
                    JSONArray array = JSON.parseArray(realList);
                    JSONObject object = array.getJSONObject(position);
                    BookingModel model = new BookingModel();
                    model.setAb_address(object.getString("ab_address"));
                    model.setAb_bman(object.getString("ab_bman"));
                    model.setAb_bmanid(object.getString("ab_bmanid"));
                    model.setAb_confirmstatus(object.getString("ab_confirmstatus"));
                    model.setAb_content(object.getString("ab_content"));
                    model.setAb_endtime(object.getString("ab_endtime"));
                    model.setAb_id(object.getString("ab_id"));
                    model.setAb_latitude(object.getString("ab_latitude"));
                    model.setAb_longitude(object.getString("ab_longitude"));
                    model.setAb_recorddate(object.getString("ab_recorddate"));
                    model.setAb_recordid(object.getString("ab_recordid"));
                    model.setAb_recordman(object.getString("ab_recordman"));
                    model.setAb_sharestatus(object.getString("ab_sharestatus"));
                    model.setAb_starttime(object.getString("ab_starttime"));
                    model.setAb_type(object.getString("ab_type"));
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("model", model);
                    bundle.putBoolean("isShared", true);
                    MyActivityManager.getCurrentActivity().startActivity(
                            new Intent(MyActivityManager.getCurrentActivity(), BookingDetailActivity.class)
                            .putExtras(bundle)
                    );
                }catch (JSONException e){
                    e.printStackTrace();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case 0x02:
                break;
                default:
        }
    }

    private static class ComShowAdapter extends BaseAdapter{
        private ArrayList<QSCModel> models;
        private Context mContext;
        public ComShowAdapter(Context mContext){
            this.mContext = mContext;
        }
        public ArrayList<QSCModel> getModels() {
            return models;
        }
        public void setModels(ArrayList<QSCModel> models) {
            this.models = models;
            LogUtil.prinlnLongMsg("qscModelsList2",JSON.toJSONString(models)+"null?");
        }
        @Override
        public int getCount() {
            return ListUtils.isEmpty(getModels()) ? 0 : getModels().size();
        }
        @Override
        public Object getItem(int position) {
            return models.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            QsViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new QsViewHolder();
                convertView = View.inflate(mContext,R.layout.com_show_ppitem,null);
                viewHolder.key1 = (TextView) convertView.findViewById(R.id.key1Tv);
                viewHolder.key2 = (TextView) convertView.findViewById(R.id.key2Tv);
                viewHolder.key3 = (TextView) convertView.findViewById(R.id.key3Tv);
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (QsViewHolder) convertView.getTag();
            }

            viewHolder.key1.setText(getModels().get(position).getKey1()+"");
            viewHolder.key2.setText(getModels().get(position).getKey2()+"");
            viewHolder.key3.setText(getModels().get(position).getKey3()+"");
            return convertView;
        }
        class QsViewHolder {
            TextView key1;
            TextView key2;
            TextView key3;
        }
    }
}

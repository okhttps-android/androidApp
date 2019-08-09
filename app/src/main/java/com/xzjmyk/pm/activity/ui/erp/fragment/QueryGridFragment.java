package com.xzjmyk.pm.activity.ui.erp.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.data.StringUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.xzjmyk.pm.activity.ui.erp.activity.SaleSelectActivity;
import com.xzjmyk.pm.activity.ui.erp.model.ImageInfo;
import com.xzjmyk.pm.activity.ui.erp.model.Querys;
import com.xzjmyk.pm.activity.ui.erp.net.HttpClient;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.app.Constants;
import com.core.utils.FlexJsonUtil;
import com.xzjmyk.pm.activity.ui.erp.view.CustomProgressDialog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author LiuJie
 * @功能:查询界面
 */
@SuppressWarnings("deprecation")
public class QueryGridFragment extends Fragment implements OnClickListener {
    @ViewInject(R.id.gv_home_display)
    private GridView gv_disGridView;
    private GirdDisplayAdapter gv_adapter;
    private String url;
    private ArrayList<ImageInfo> data = new ArrayList<ImageInfo>(); // 菜单数据
    private final static int SUCCESS_LOGINAPP = 0;
    @ViewInject(R.id.view_empty)
    private RelativeLayout emptyLayout;
    @ViewInject(R.id.buttonEmpty)
    private Button tryBut;
    @ViewInject(R.id.textViewMessage)
    private TextView data_msg;
    private CustomProgressDialog progressDialog;
    private Context ct;

    private int[] imgID={
            R.drawable.query1,R.drawable.query2,R.drawable.query3,R.drawable.query4,R.drawable.query5,R.drawable.query6,
            R.drawable.query7,R.drawable.query8,R.drawable.query9,R.drawable.query10,R.drawable.query11,R.drawable.query12,
            R.drawable.query13
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_grid_find_layout, container,
                false);
        ViewUtils.inject(this, view);
        ct = getActivity();
        progressDialog = CustomProgressDialog.createDialog(ct);
        initData();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonEmpty:
                loadData();
                break;

            default:
                break;
        }
    }

    private void initData() {
        progressDialog.show();
        tryBut.setOnClickListener(this);
        if (CommonUtil.isNetWorkConnected(ct)) {
            if (!StringUtil.isEmpty(CommonUtil.getAppBaseUrl(ct))) {
                LoginERPTask(CommonUtil.getAppBaseUrl(ct),
                        CommonUtil.getSharedPreferences(ct, "erp_master"),
                        CommonUtil.getSharedPreferences(ct, "user_phone"),
                        CommonUtil.getSharedPreferences(ct, "user_password"));
            }
        } else {
            progressDialog.dismiss();
            data_msg.setText(getString(R.string.common_notlinknet));
            emptyLayout.setVisibility(View.VISIBLE);
        }
    }

    public void loadData() {
        if (CommonUtil.isNetWorkConnected(ct)) {
            url = CommonUtil.getAppBaseUrl(ct)
                    + "mobile/common/getMobileQuerys.action";
            String sessionId = CommonUtil.getSharedPreferences(ct, "sessionId");
            Map<String, String> params = new HashMap<String, String>();
            params.put("sessionId", sessionId);
            startHandlerThread(url, params, handler, 1);
        } else {
            emptyLayout.setVisibility(View.VISIBLE);
            data_msg.setText(getString(R.string.common_notlinknet));
            gv_disGridView.setVisibility(View.GONE);
        }
    }

    public void setListener() {
        gv_disGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (!StringUtil.isEmpty(CommonUtil.getAppBaseUrl(ct))) {
                    if (data.get(position).getCaller() != null) {
                        Intent it_scale = new Intent(ct,
                                SaleSelectActivity.class);
                        it_scale.putExtra("caller", data.get(position)
                                .getCaller());
                        System.out.println("title+:"
                                + data.get(position).getImageMsg());
                        it_scale.putExtra("title", data.get(position)
                                .getImageMsg());
                        ct.startActivity(it_scale);
                    }
                } else {
                    //ViewUtil.ShowBasicNoTitle(ct);
                }
            }
        });
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String result = msg.getData().getString("result");
                    Log.i("json", "queryGrid" + result);
                    Map<String, Object> rMap = FlexJsonUtil.fromJson(result);
                    List<Querys> querys = new ArrayList<Querys>();
                    querys = FlexJsonUtil.fromJsonArray(
                            FlexJsonUtil.toJson(rMap.get("querys")), Querys.class);
                    data.clear();
                    if (querys != null) {
                        for (int i = 0; i < querys.size(); i++) {
                            data.add(new ImageInfo(querys.get(i).getMq_title(),
                                    R.drawable.home_image_12, 0, 0, querys.get(i)
                                    .getMq_caller()));
                        }
                        if (3 - querys.size() % 3 == 1) {
                            data.add(new ImageInfo("", R.color.white, 0, 0, null));
                        }
                        if (3 - querys.size() % 3 == 2) {
                            data.add(new ImageInfo("", R.color.white, 0, 0, null));
                            data.add(new ImageInfo("", R.color.white, 0, 0, null));
                        }
                    } else {
                        // 数据为空
                    }

                    if (gv_adapter == null) {
                        gv_adapter = new GirdDisplayAdapter(ct, data);
                        gv_disGridView.setAdapter(gv_adapter);
                    } else {
                        gv_adapter.notifyDataSetChanged();
                    }

                    if (gv_adapter.getCount() == 0) {
                        emptyLayout.setVisibility(View.VISIBLE);
                        data_msg.setText("暂时没有数据！");
                        data_msg.setTextColor(ct.getResources().getColor(
                                R.color.gray_light));
                        tryBut.setVisibility(View.GONE);
                    } else {
                        System.out.println("data size:" + gv_adapter.getCount());
                        emptyLayout.setVisibility(View.GONE);
                    }
                    /** 注释：调间距的 */
                    setListener();
                    progressDialog.dismiss();
                    break;
                case SUCCESS_LOGINAPP:
                    rMap = FlexJsonUtil.fromJson(msg.getData().getString("result"));
                    String sessionId;
                    try {
                        sessionId = rMap.get("sessionId").toString();
                        CommonUtil.setSharedPreferences(ct, "sessionId", sessionId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // ViewUtil.ShowMessageTitle(FindGridActivity.this,
                        // "登录失败！");
                    }
                    loadData();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public void LoginERPTask(String url, String master, String username,
                             String password) {
        progressDialog.show();
        url = url + "mobile/login.action";
        Map<String, String> params = new HashMap<String, String>();
        String accountToken = CommonUtil.getSharedPreferences(ct, Constants.CACHE.ACCOUNT_CENTER_TOKEN);
        params.put("token", accountToken);
//        params.put("username", username);
//        params.put("password", password);
        params.put("master", master);
        startHandlerThread(url, params, handler, SUCCESS_LOGINAPP);
    }

    public class GirdDisplayAdapter extends BaseAdapter {
        public Context context;
        public ArrayList<ImageInfo> data;

        public GirdDisplayAdapter(Context context, ArrayList<ImageInfo> data) {
            this.context = context;
            this.data = data;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Griditems iGriditems = null;
            if (convertView == null) {
                iGriditems = new Griditems();
                convertView = LayoutInflater.from(context).inflate(
                        R.layout.act_grid_find_item, null);
                iGriditems.gird_image = (ImageView) convertView
                        .findViewById(R.id.iv_gird_image);
                iGriditems.gird_msg_count = (TextView) convertView
                        .findViewById(R.id.tv_gird_msg_count);
                iGriditems.gird_msg = (TextView) convertView
                        .findViewById(R.id.tv_gird_msg);
                iGriditems.gird_rl = (RelativeLayout) convertView
                        .findViewById(R.id.rl_grid_find_item);
//				iGriditems.badge = new BadgeView(context,
//						iGriditems.gird_msg_count);

                convertView.setTag(iGriditems);
            } else {
                iGriditems = (Griditems) convertView.getTag();
            }
            if (data.get(position).badgeCount != 0) {
//				iGriditems.badge
//						.setText(String.valueOf(data.get(position).badgeCount));
//				iGriditems.badge.setBadgeBackgroundColor(Color
//						.parseColor("red"));
//				iGriditems.badge.setTextColor(Color.WHITE);
//				iGriditems.badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT); // 默认值
//				iGriditems.badge.show();
            }
            /** @注释：logo */
            //TODO 图片数据
            if (position<imgID.length)
            iGriditems.gird_image.setImageResource(imgID[position]);
            else
                iGriditems.gird_image.setImageResource(data.get(position).imageId);
            iGriditems.gird_rl
                    .setBackgroundResource(R.drawable.ry_setting_selector);
            iGriditems.gird_rl.getBackground().setAlpha(250);
            /** @注释：title */
            iGriditems.gird_msg.setText(data.get(position).imageMsg);
            return convertView;
        }
        public final class Griditems {
            //BadgeView badge;
            TextView gird_msg_count;
            ImageView gird_image;
            TextView gird_msg;
            RelativeLayout gird_rl;
        }
    }

    public void startHandlerThread(final String url,
                                   final Map<String, String> params, final Handler handler,
                                   final int what) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                if (CommonUtil.isNetWorkConnected(ct)) {
                    /** @注释：处理网络请求返回结果 */
                    String result = getDataFromServer(url, params);
                    Message message = new Message();
                    if (result != null) {
                        Bundle bundle = new Bundle();
                        bundle.putString("result", result);
                        message.setData(bundle);
                        message.what = what;
                        // handler_net.sendMessage(message);
                        handler.sendMessage(message);
                    } else {
                        // handler_net.sendEmptyMessage(LOAD_EXCEPTION);
                    }
                } else {
                    // handler_net.sendEmptyMessage(LOAD_NOTNETWORK);
                }
            }
        }).start();
    }

    public String getDataFromServer(String url, Map<String, String> params) {
        String result = null;
        HttpClient hClient = new HttpClient();
        try {
            result = hClient.sendPostRequest(url, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class
                    .getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}

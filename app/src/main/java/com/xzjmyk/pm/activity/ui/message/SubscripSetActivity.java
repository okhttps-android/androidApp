package com.xzjmyk.pm.activity.ui.message;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.xzjmyk.pm.activity.ui.erp.entity.SubscipTypeEntity;
import com.core.net.http.ViewUtil;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.core.app.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 申请订阅
 * by gongpm
 */
public class SubscripSetActivity extends BaseActivity {
    private ListView listview;
    private String baseUrl;
    private List<SubscipTypeEntity> entities;
    private SubscripSetAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscrip_set);
        initView();
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.listview);
        baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        init();
    }

    private void init() {
        adapter = new SubscripSetAdapter();
        listview.setAdapter(adapter);
        loadNetData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save) {
            saveData();

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_visit_save, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String message = (String) msg.getData().get("result");
            if (msg.what == 2) {
                JSONObject object = null;
                try {
                    object = new JSONObject(message);
                    JSONArray array = object.getJSONArray("subsNums");
                    entities = JSON.parseArray(array.toString(), SubscipTypeEntity.class);
                    bindData(entities);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else if (Constants.APP_SOCKETIMEOUTEXCEPTION == msg.what) {
                //错误信息

            } else if (msg.what == 3) {

            }
        }
    };

    private void bindData(List<SubscipTypeEntity> entities) {
        adapter.notifyDataSetChanged();
    }

    private ArrayList<Integer> ints = new ArrayList();//保存被选中的id
    private StringBuffer addBuffer = new StringBuffer();
    private StringBuffer removeBuffer = new StringBuffer();
    private int addType = 1, removeType = 2;

    //保存时候提交订阅选择
    private void saveData() {
        if (entities != null && entities.size() > 0)
            for (int i = 0; i < entities.size(); i++) {
                if (entities.get(i).isClicked()) {//被选中
                    if (addBuffer.length() > 0) {
                        addBuffer.append("," + entities.get(i).getId());
                    } else {
                        addBuffer.append(entities.get(i).getId());
                    }
//                    for (int j = 0; j < ints.size(); j++) {
//                        if (ints.get(j) != entities.get(i).getId()) {
//                            if (addBuffer.length() > 0) {
//                                addBuffer.append("," + entities.get(i).getId());
//                            } else {
//                                addBuffer.append(entities.get(i).getId());
//                            }
//                        }
//                    }
                } else {//未选中
                    for (int j = 0; j < ints.size(); j++) {
                        if (ints.get(j) == entities.get(i).getId()) {//当未选中的有以前已经选中的
                            if (addBuffer.length() > 0) {
                                removeBuffer.append("," + entities.get(i).getId());
                            } else {
                                removeBuffer.append(entities.get(i).getId());
                            }
                        }
                    }
                }
            }
        if (addBuffer.length() > 0) {
            upSubdate(addType);
        }
        if (removeBuffer.length() > 0) {
            upSubdate(removeType);
        }
    }

    //修改订阅状态 type：添加还是取消
    public void upSubdate(int type) {
        if (baseUrl == null || baseUrl.length() <= 0) {
            baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        }
        String urlsub;
        String Ids;
        if (type == addType) {
            urlsub = "common/charts/addSubsMans.action";
            Ids = addBuffer.toString();
        } else {
            urlsub = "common/charts/removeSubsMans.action";
            Ids = removeBuffer.toString();
        }
        //获取网络数据
        String empId = CommonUtil.getSharedPreferences(this, "erp_username");
        String url = baseUrl + urlsub;
        final Map<String, Object> param = new HashMap<>();
        param.put("numIds", Ids);
        param.put("empId", empId);
        param.put("sessionId", CommonUtil.getSharedPreferences(this, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, handler, headers, 3, null, null, "post");
    }

    //获取接口数据
    public void loadNetData() {
        if (baseUrl == null || baseUrl.length() <= 0) {
            baseUrl = CommonUtil.getSharedPreferences(this, "erp_baseurl");
        }
        //获取网络数据
        String url = baseUrl + "common/charts/getSubsNums.action";
        final Map<String, Object> param = new HashMap<>();
        param.put("condition", "1=1");
        param.put("sessionId", CommonUtil.getSharedPreferences(this, "sessionId"));
        LinkedHashMap<String, Object> headers = new LinkedHashMap<>();
        headers.put("Cookie", "JSESSIONID=" + CommonUtil.getSharedPreferences(this, "sessionId"));
        ViewUtil.httpSendRequest(this, url, param, handler, headers, 2, null, null, "get");
    }

    class SubscripSetAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (entities == null) {
                return 0;
            }
            return entities.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (view == null) {
                holder = new ViewHolder();
                view = LayoutInflater.from(SubscripSetActivity.this).inflate(R.layout.item_subscripset, null);
                holder.id_tv = (TextView) view.findViewById(R.id.id_tv);
                holder.title_tv = (TextView) view.findViewById(R.id.title_tv);
                holder.kind_img = (ImageView) view.findViewById(R.id.kind_img);
                holder.select_rb = (CheckBox) view.findViewById(R.id.select_rb);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            final SubscipTypeEntity entity = entities.get(i);
            bingAdapterData(entity, holder);
            holder.select_rb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    entities.get(i).setClicked(b);

                }
            });
            return view;
        }

        private void bingAdapterData(final SubscipTypeEntity entity, ViewHolder holder) {
            holder.select_rb.setChecked(entity.isClicked());
            holder.id_tv.setText(entity.getId() + "");
            holder.title_tv.setText(entity.getTitle());
            int resource = -1;
            if ("private".equals(entity.getKind())) {
                resource = R.drawable.sub_private;
            } else {
                resource = R.drawable.sub_public;
            }
            holder.kind_img.setImageResource(resource);

        }

        class ViewHolder {
            TextView id_tv, title_tv;
            ImageView kind_img;
            CheckBox select_rb;
        }
    }


}

package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/26.
 */
public class ErpMenActivity extends BaseActivity {
    @ViewInject(R.id.lv_tool)
    private ListView lv_tool;
    List<String> menus;
    Context ct;
    private DetailItemAdapter adapter;
    private int[] imgID={
            R.drawable.query1,R.drawable.query2,R.drawable.query3,R.drawable.query4,R.drawable.query5,R.drawable.query6,
            R.drawable.query7,R.drawable.query8,R.drawable.query9,R.drawable.query10,R.drawable.query11,R.drawable.query12,
            R.drawable.query13
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_tool_layout);
        ViewUtils.inject(this);
        ct = this;
        initData();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_crm, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.btn_girdView:
//              
//                break;
//            case R.id.btn_listView:
//
//                break;
//            
//        }
//        return true;
//    }

    private void initData() {
        setTitle("考勤单据");
        menus = new ArrayList<String>();
       // menus.add("销售订单");
       // menus.add("报价单");
        menus.add("请假单");
        menus.add("出差单");
        menus.add("加班申请");
        menus.add("特殊考勤");
        menus.add("查询与统计");
       // menus.add("请假单2");

        adapter = new DetailItemAdapter(this, menus);
        lv_tool.setAdapter(adapter);

//        lv_tool.setOnItemLongClickListener(new OnItemLongClickListener() {
//
//            @Override
//            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//                MaterialDialog dialog=new MaterialDialog.Builder(ct)
//                        .items(R.array.menus)
//                        .itemsCallback(new MaterialDialog.ListCallback() {
//
//                            @Override
//                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
//                                switch (which) {
//                                    case 0:
//                                        String imageInfo=(String)adapter.getItem(position);
//                                        adapter.getMenus().remove(position);
//                                        adapter.getMenus().add(0,imageInfo);
//                                        adapter.notifyDataSetChanged();
//                                        break;
//                                    case 1:
//                                        adapter.getMenus().remove(position);
//                                        adapter.notifyDataSetChanged();
//                                        break;
//                                    default:
//                                        break;
//                                }
//                            }
//                        }).build();
//                dialog.show();
//                return false;
//            }
//        });

        lv_tool.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DetailItemAdapter.ListItem item = (DetailItemAdapter.ListItem) view.getTag();
                String tag = item.menu_name.getText().toString();
                int falg = 0;
                if ("销售订单".equals(tag)) {
                    falg = 0;
                }
                if ("报价单".equals(tag)) {
                    falg = 1;
                }
                if ("请假单".equals(tag)) {
                    falg = 2;
                }
                if ("出差单".equals(tag)) {
                    falg = 3;
                }
                if ("加班申请".equals(tag)) {
                    falg = 4;
                }
                if ("特殊考勤".equals(tag)) {
                    falg = 5;
                }
                if ("查询与统计".equals(tag)) {
                    falg = 6;
                }

                switch (falg) {
                    case 0:
//                        if (CommonUtil.getSharedPreferencesBoolean(ct,"b2b_login")) {
//                            Intent b2bIntent=new Intent(ct,MenuFirstActivity.class);
//                            b2bIntent.putExtra("status", "我的单据");
//                            ct.startActivity(b2bIntent);
//                        }else{
//                            new MaterialDialog.Builder(ct)
//                                    .mTitle("信息提示")
//                                    .content("您未开通B2B平台！").build().show();
//                        }
                        break;
                    case 1:
//                        if (CommonUtil.getSharedPreferencesBoolean(ct,"b2b_login")) {
//                            Intent b2bIntent=new Intent(ct,MenuFirstActivity.class);
//                            b2bIntent.putExtra("status", "我的报价");
//                            ct.startActivity(b2bIntent);
//                        }else{
//                            new MaterialDialog.Builder(ct)
//                                    .mTitle("信息提示")
//                                    .content("您未开通B2B平台！").build().show();
//                        }
                        break;
                    case 2:
                        Intent it_scale = new Intent(ct,
                                SaleSelectActivity.class);
                        it_scale.putExtra("caller", "Ask4Leave");
                        it_scale.putExtra("title", "请假单查询");
                        it_scale.putExtra("from", "SignMain");
                        startActivity(it_scale);
                        break;
                    case 3:
                        it_scale = new Intent(ct,
                                SaleSelectActivity.class);
                        it_scale.putExtra("caller", "FeePlease!CCSQ");
                        it_scale.putExtra("title", "出差单查询");
                        it_scale.putExtra("from", "SignMain");
                        startActivity(it_scale);
                        break;
                    case 4:
                        it_scale = new Intent(ct,
                                SaleSelectActivity.class);
                        it_scale.putExtra("caller", "Workovertime");
                        it_scale.putExtra("title", "加班申请查询");
                        it_scale.putExtra("from", "SignMain");
                        startActivity(it_scale);
                        break;
                    case 5:
                        it_scale = new Intent(ct,
                                SaleSelectActivity.class);
                        it_scale.putExtra("caller", "SpeAttendance");
                        it_scale.putExtra("title", "特殊考勤查询");
                        it_scale.putExtra("from", "SignMain");
                        startActivity(it_scale);
                        break;

                    case 6:
                        it_scale = new Intent(ct,
                                QueryActivity.class);
                        startActivity(it_scale);
                        break;
                    default:
                        break;
                }


            }
        });
    }


    public class DetailItemAdapter extends BaseAdapter {
        private Context ct;
        private List<String> menus = new ArrayList<>();
        private LayoutInflater inflater;

        public DetailItemAdapter(Context ct, List<String> menus) {

            this.ct = ct;
            this.menus = menus;
            this.inflater = LayoutInflater.from(ct);
        }


        public List<String> getMenus() {
            return menus;
        }


        public void setMenus(List<String> menus) {
            this.menus = menus;
        }


        @Override
        public int getCount() {
            return menus.size();
        }

        @Override
        public Object getItem(int position) {
            return menus.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItem item = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.item_weixin_listview, parent, false);
                item = new ListItem();
                item.menu_name = (TextView) convertView.findViewById(R.id.tv_menu_name);
//                item.badge=new BadgeView(ct, item.menu_name);
                convertView.setTag(item);
            } else {

                item = (ListItem) convertView.getTag();
            }
            if (position % 2 == 0) {
                convertView.setBackgroundColor(getResources().getColor(R.color.item_color1));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.item_color2));
            }
            item.menu_name.setText(menus.get(position));
//            item.badge.setText(String.valueOf(position));
//            item.badge.setBadgeBackgroundColor(Color
//                    .parseColor("red"));
//            item.badge.setTextColor(Color.WHITE);
//            item.badge.setBadgePosition(BadgeView.POSITION_TOP_LEFT);
            //item.badge.show();
            return convertView;
        }

        class ListItem {
            TextView menu_name;
//            BadgeView badge;
        }


    }
}

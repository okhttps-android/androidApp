package com.xzjmyk.pm.activity.ui.erp.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.core.model.Hrorgs;
import com.core.widget.CustomerListView;

import java.util.List;

public class CompanyActivity extends BaseActivity {
    public static final int LIST_LEFT_MODE = 0;
    public static final int LIST_RIGHT_MODE = 1;
    private SwipeRefreshLayout refresh_top;
    private LinearLayout ll_content;
    private HorizontalScrollView hv_head_view;
    private RadioGroup rg_nav_content;
    private CustomerListView lv_saff_list, lv_left_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company);
        init();
        initView();
    }

    private void initView() {
        refresh_top = (SwipeRefreshLayout) findViewById(R.id.refresh_top);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        hv_head_view = (HorizontalScrollView) findViewById(R.id.hv_head_view);
        rg_nav_content = (RadioGroup) findViewById(R.id.rg_nav_content);
        lv_saff_list = (CustomerListView) findViewById(R.id.lv_saff_list);
        lv_left_list = (CustomerListView) findViewById(R.id.lv_left_list);

        refresh_top.setColorSchemeResources(R.color.blue, R.color.yellow, R.color.grey, R.color.red);
        refresh_top.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO 刷新
            }
        });
        lv_saff_list.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    refresh_top.setEnabled(true);
                else
                    refresh_top.setEnabled(false);
            }
        });



    }

    private void init() {


    }


    public class DetailItemAdapter extends BaseAdapter {
        private Context ct;
        private LayoutInflater inflater;
        private List<Hrorgs.HrorgItem> lists;
        private List<Hrorgs.Employee> employees;
        private int selectPosition;
        private int typeMode = LIST_RIGHT_MODE;


        public DetailItemAdapter(Context ct, List<Hrorgs.HrorgItem> items) {
            this.ct = ct;
            this.inflater = LayoutInflater.from(ct);
            this.lists = items;
        }

        public DetailItemAdapter(Context ct, Hrorgs object) {
            this.ct = ct;
            this.inflater = LayoutInflater.from(ct);
            this.employees = object.getEmployees();
            this.lists = object.getHrorgs();
        }

        public void setMode(int typeMode) {
            this.typeMode = typeMode;
        }

        @Override
        public int getCount() {
            if (lists != null && employees != null) {
                return lists.size() + employees.size();
            } else if (lists != null) {
                return lists.size();
            } else if (employees != null) {
                return employees.size();
            }
            return 0;
        }

        public List<Hrorgs.Employee> getEmployees() {
            return employees;
        }

        public void setEmployees(List<Hrorgs.Employee> employees) {
            this.employees = employees;
        }

        @Override
        public Object getItem(int position) {
            if (lists != null && employees != null) {
                if (position < lists.size()) {
                    return lists.get(position);
                } else {
                    return employees.get(position);
                }
            } else if (lists != null) {
                return lists.get(position);
            } else if (employees != null) {
                return employees.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setSelectPosition(int position) {
            this.selectPosition = position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ModelItem item = null;
            if (convertView == null) {
                item = new ModelItem();

                convertView = inflater.inflate(R.layout.item_staff_tree, parent, false);
                item.iv_falg = (ImageView) convertView.findViewById(R.id.iv_item_falg);
                item.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                item.tv_type = (TextView) convertView.findViewById(R.id.tv_name_type);
                item.iv_enter = (ImageView) convertView.findViewById(R.id.iv_item_into);
                convertView.setTag(item);
            } else {
                item = (ModelItem) convertView.getTag();
            }
            // 三种情况
            if (lists != null && employees != null) {
                if (position < lists.size()) {
                    item.tv_name.setText(lists.get(position).getOr_name());
                    item.or_id = lists.get(position).getOr_id();
                    item.isleaf = lists.get(position).getOr_isleaf();
                    item.tv_type.setText("部门");
                    item.tv_type.setVisibility(View.INVISIBLE);
                } else {
                    item.tv_name.setText(employees.get(position - lists.size()).em_name);
                    item.or_id = employees.get(position - lists.size()).getEm_id();
                    item.isleaf = 1;
                    item.imid = employees.get(position - lists.size()).getEm_imid();
                    // item.iv_enter.setVisibility(View.GONE);
                    item.tv_type.setText("联系人");
                    item.tv_type.setVisibility(View.INVISIBLE);
                }

            } else if (lists != null) {
                item.tv_name.setText(lists.get(position).getOr_name());
                item.or_id = lists.get(position).getOr_id();
                item.isleaf = lists.get(position).getOr_isleaf();
                item.tv_type.setText("部门");
                item.tv_type.setVisibility(View.INVISIBLE);
            } else if (employees != null) {
                item.tv_name.setText(employees.get(position).em_name);
                item.or_id = employees.get(position).getEm_id();
                item.isleaf = 1;
                // item.tv_name.setTextColor(ct.getResources().getColor(R.color.gray));
                item.tv_type.setText("联系人");
                item.imid = employees.get(position).getEm_imid();
                item.tv_type.setVisibility(View.INVISIBLE);
            }
            item.iv_falg.setVisibility(View.GONE);
            if (typeMode == LIST_LEFT_MODE) {
                if (position == selectPosition) {
                    convertView.setBackgroundResource(R.drawable.tongcheng_all_bg01);
                } else {
                    convertView.setBackgroundColor(Color.parseColor("#f4f4f4"));
                }
                item.iv_enter.setVisibility(View.GONE);
            } else {
                convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            return convertView;
        }

        class ModelItem {

            public int imid;
            public int or_id;
            public int isleaf;
            public TextView tv_type;
            public ImageView iv_falg;
            public TextView tv_name;
            public ImageView iv_enter;
        }
    }
}

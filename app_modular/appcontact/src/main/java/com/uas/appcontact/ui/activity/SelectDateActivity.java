package com.uas.appcontact.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.core.app.ActionBackActivity;
import com.uas.appcontact.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 选择日期
 *
 * @author Dean Tao
 * @version 1.0
 */
public class SelectDateActivity extends ActionBackActivity {
    static class Day {
        private String name;// 名称
        private int id;// 天数

        public Day(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }

    private List<Day> mDays;

    public void initData() {
        mDays = new ArrayList<Day>();
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [0], 0));
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [1], 1));
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [2], 2));
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [3], 3));
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [4], 7));
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [5], 14));
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [6], 30));
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [7], 42));
        mDays.add(new Day(getResources().getStringArray(R.array.array_select_date)
                [8], 60));
    }

    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.select_date_title);
        setContentView(R.layout.activity_simple_list);
        initData();
        initView();

    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.list_view);

        mListView.setAdapter(new ConstantAdapter());
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Day day = mDays.get(position);
                result(day.getId(), day.getName());
            }
        });
    }

    public final String EXTRA_CONSTANT_NAME = "constant_name";
    public final String EXTRA_CONSTANT_ID = "constant_id";

    private void result(int id, String name) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_CONSTANT_ID, id);
        intent.putExtra(EXTRA_CONSTANT_NAME, name);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private class ConstantAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDays.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.row_constant_select, parent, false);
            }
            TextView textView = (TextView) convertView;
            textView.setText(mDays.get(position).getName());
            return convertView;
        }

    }

}

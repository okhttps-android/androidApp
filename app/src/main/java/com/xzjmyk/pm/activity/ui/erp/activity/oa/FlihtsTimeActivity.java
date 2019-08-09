package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.DateFormatUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.base.OABaseActivity;
import com.core.model.SelectBean;
import com.core.utils.time.wheel.TimePicker;
import com.core.widget.view.Activity.SelectActivity;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.uas.appworks.OA.erp.model.FlightsTimeModel;
import com.uas.appworks.OA.erp.utils.MostLinearLayoutManager;
import com.xzjmyk.pm.activity.R;
import com.modular.apputils.widget.RecycleViewDivider;
import com.xzjmyk.pm.activity.view.crouton.Crouton;

import java.util.ArrayList;
import java.util.List;

public class FlihtsTimeActivity extends OABaseActivity implements View.OnClickListener {

    @ViewInject(R.id.listView)
    private RecyclerView listView;
    @ViewInject(R.id.time_tv)
    private TextView time_tv;
    @ViewInject(R.id.munber_tv)
    private TextView munber_tv;

    private List<Bean> beanList;
    private MyAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flihts_time);
        ViewUtils.inject(this);
        setTitle(R.string.work_time_setting);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) return;
        if (0x21 == requestCode) {
            SelectBean bean = data.getParcelableExtra("data");
            munber_tv.setText(bean.getName());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.munber_tv:
                String[] str = getResources().getStringArray(R.array.one_five_hour);
                ArrayList<SelectBean> beans = new ArrayList<>();
                SelectBean bean = null;
                for (String e : str) {
                    bean = new SelectBean();
                    bean.setName(e);
                    bean.setClick(false);
                    beans.add(bean);
                }
                Intent intent = new Intent(ct, SelectActivity.class);
                intent.putExtra("type", 2);
                intent.putParcelableArrayListExtra("data", beans);
                intent.putExtra("title", getString(R.string.earliest_work_time));
                startActivityForResult(intent, 0x21);
                break;
            case R.id.click_btn:
                FlightsTimeModel model = canPush();
                if (model != null) {
                    intent = new Intent();
                    String time = munber_tv.getText().toString();
                    model.setEarlyTime(StringUtil.getFirstInt(time, 1));
                    intent.putExtra("data", model);
                    setResult(0x20, intent);
                    finish();
                }
                break;
            case R.id.add_tv:
                if (ListUtils.isEmpty(beanList) || beanList.size() < 3)
                    addEmpty();
                else Crouton.showToast(ct, R.string.most_input_three_time, R.color.load_warning);
                break;
        }
    }


    private void initView() {
        listView.setLayoutManager(new MostLinearLayoutManager(ct));
        RecycleViewDivider viewDivider = new RecycleViewDivider(this, LinearLayout.HORIZONTAL, 1, getResources().getColor(R.color.gray_light));
        listView.addItemDecoration(viewDivider);
        findViewById(R.id.munber_tv).setOnClickListener(this);
        findViewById(R.id.click_btn).setOnClickListener(this);
        findViewById(R.id.add_tv).setOnClickListener(this);
        initData();
    }


    private void initData() {
        beanList = new ArrayList<>();
        FlightsTimeModel model = getIntent().getParcelableExtra("model");
        if (model != null) {
            if (!StringUtil.isEmpty(model.getWd_ondutyone()) && !StringUtil.isEmpty(model.getWd_offdutyone())) {
                Bean b = new Bean();
                b.startTime = model.getWd_ondutyone();
                b.endTime = model.getWd_offdutyone();
                beanList.add(b);
            }
            if (!StringUtil.isEmpty(model.getWd_ondutytwo()) && !StringUtil.isEmpty(model.getWd_offdutytwo())) {
                Bean b = new Bean();
                b.startTime = model.getWd_ondutytwo();
                b.endTime = model.getWd_offdutytwo();
                beanList.add(b);
            }
            if (!StringUtil.isEmpty(model.getWd_ondutythree()) && !StringUtil.isEmpty(model.getWd_offdutythree())) {
                Bean b = new Bean();
                b.startTime = model.getWd_ondutythree();
                b.endTime = model.getWd_offdutythree();
                beanList.add(b);
            }
            int earlyTime = model.getEarlyTime();
            if (earlyTime != 0) {
                munber_tv.setText(getString(R.string.before_work) + earlyTime + getString(R.string.hour));
            }
        } else {
            Bean b = new Bean();
            b.startTime = "08:00";
            b.endTime = "12:00";
            beanList.add(b);
            b = new Bean();
            b.startTime = "13:30";
            b.endTime = "18:00";
            beanList.add(b);
        }
        setAdapter();
    }

    private FlightsTimeModel canPush() {
        if (ListUtils.isEmpty(beanList)) {
            Crouton.showToast(ct, R.string.not_null_work_time, R.color.load_warning);
            return null;
        }
        if (TextUtils.isEmpty(munber_tv.getText())) {
            Crouton.showToast(ct, R.string.select_earliest_work_time, R.color.load_warning);
            return null;
        }
        FlightsTimeModel model = new FlightsTimeModel();
        for (int i = 0; i < beanList.size(); i++) {
            String start = beanList.get(i).startTime;
            String end = beanList.get(i).endTime;
            if (StringUtil.isEmpty(start) || StringUtil.isEmpty(end)) {
                if (StringUtil.isEmpty(start)) {
                    Crouton.showToast(ct, R.string.work_not_complete, R.color.load_warning);
                } else if (StringUtil.isEmpty(end)) {
                    Crouton.showToast(ct, R.string.off_not_complete, R.color.load_warning);
                }
                return null;
            }
            if (i == 0) {
                model.setWd_ondutyone(start);
                model.setWd_offdutyone(end);
            } else if (i == 1) {
                model.setWd_ondutytwo(start);
                model.setWd_offdutytwo(end);
            } else if (i == 2) {
                model.setWd_ondutythree(start);
                model.setWd_offdutythree(end);
            }
            for (int j = 0; j < beanList.size(); j++) {
                if (j == i) continue;
                String cstart = beanList.get(j).startTime;
                String cend = beanList.get(j).endTime;
                if (StringUtil.isEmpty(cstart) && StringUtil.isEmpty(cend)) continue;
                if ((start.compareTo(cstart) >= 0 && start.compareTo(cend) <= 0)
                        || (end.compareTo(cstart) >= 0 && end.compareTo(cend) <= 0)) {
                    //当开始时间和结束时间有冲突
                    Crouton.showToast(ct, R.string.time_include_time, R.color.load_warning);
                    return null;
                }
                //跨天情况
                if (cstart.compareTo(cend) > 0 && (checkInter(start, cstart, cend) || checkInter(end, cstart, cend))
                        ) {
                    //当开始时间和结束时间有冲突
                    Crouton.showToast(ct, R.string.time_include_time, R.color.load_warning);
                    return null;
                }
            }
        }
        return model;
    }

    private boolean checkInter(String time, String start, String end) {
        if ((time.compareTo(start) > 0 && time.compareTo("24:00") < 0) || (time.compareTo("00:00") > 0 && time.compareTo(end) < 0))
            return true;
        return false;
    }


    private void setAdapter() {
        if (adapter == null) {
            adapter = new MyAdapter();
            listView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        setTime();
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ct).inflate(R.layout.item_flight_time, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Bean bean = beanList.get(position);
            holder.title_tv.setText(getString(R.string.work_up_time) + (position + 1));
            holder.start_tv.setText(bean.startTime);
            holder.end_tv.setText(bean.endTime);

            int reId = position == 0 ? R.drawable.daka_bianji : R.drawable.delete;
            holder.img.setImageResource(reId);
            initEvent(holder, position);
        }

        private void initEvent(ViewHolder holder, final int position) {
            holder.start_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        showDateDialog(position, true);
                    } catch (Exception e) {

                    }
                }
            });
            holder.end_tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (StringUtil.isEmpty(beanList.get(position).startTime)) {
                            Crouton.showToast(ct, R.string.please_input_start_time, R.color.load_warning);
                            return;
                        }
                        showDateDialog(position, false);
                    } catch (Exception e) {

                    }
                }
            });
            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position == 0) return;
                    beanList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, beanList.size());
                    setTime();
                }
            });
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getItemCount() {
            return (ListUtils.isEmpty(beanList)) ? 0 : beanList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView title_tv, start_tv, end_tv;
            ImageView img;

            public ViewHolder(View view) {
                super(view);
                title_tv = (TextView) view.findViewById(R.id.title_tv);
                start_tv = (TextView) view.findViewById(R.id.start_tv);
                end_tv = (TextView) view.findViewById(R.id.end_tv);
                img = (ImageView) view.findViewById(R.id.img);
            }
        }
    }


    private void addList(Bean b) {
        if (b == null) return;
        if (beanList == null) beanList = new ArrayList<>();
        beanList.add(b);
        setAdapter();
    }

    /**
     * 获取最后时间
     *
     * @param position 当前索引
     * @param isWork   是否上班时间、（否为下班时间）
     * @return hm
     */
    private String getLastTime(int position, boolean isWork) {
        String lastTime = "";

        if (beanList.size() > position) {
            if (isWork) lastTime = beanList.get(position).startTime;
            else lastTime = beanList.get(position).endTime;
            if (!StringUtil.isEmpty(lastTime)) return lastTime;
        }

        if (position == 0) {
            if (isWork) return "00:00";
            else return beanList.get(position).startTime;
        } else {
            for (int i = position - 1; i >= 0; i--) {
                Bean b = beanList.get(i);
                if (!StringUtil.isEmpty(b.endTime)) {
                    lastTime = b.endTime;
                    break;
                } else if (!StringUtil.isEmpty(b.startTime)) {
                    lastTime = b.startTime;
                    break;
                }
            }
        }
        return lastTime;
    }


    //计算合计工时
    private void setTime() {
        int time = 0;
        for (Bean m : beanList) {
            String start = m.startTime;
            String end = m.endTime;
            if (StringUtil.isEmpty(start) || StringUtil.isEmpty(end)) continue;
            try {
                time += DateFormatUtil.getDifferSS(start, end);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int min = time / 60;//获取分钟
        int h = min / 60;//获取小时
        int m = min % 60;//获取分钟
        String finalTime = null;
        if (h > 0) {
            finalTime = h + getString(R.string.hour) + (m == 0 ? "" : (m + getString(R.string.minute)));
        } else {
            finalTime = m + getString(R.string.minute);
        }
        time_tv.setText(finalTime);
    }

    private void addEmpty() {
        addList(new Bean());
    }


    /**
     * 选择时间
     *
     * @param position 第几个item
     * @param isStart  是否上班时间
     */
    private void showDateDialog(int position, boolean isStart) {
        //获取最后时间
        String hm = getLastTime(position, isStart);
        int hh = 8;
        int minth = 30;
        if (!StringUtil.isEmpty(hm)) {
            try {
                String[] hhmm = hm.split(":");
                hh = Integer.valueOf(hhmm[0]);
                minth = Integer.valueOf(hhmm[1]);
            } catch (Exception e) {

            }
        }
        showDateDialog(position, isStart, hh, minth);
    }


    private void showDateDialog(final int position, final boolean isStart, int hh, int minth) {
        TimePicker picker = new TimePicker(this, TimePicker.HOUR_OF_DAY);
        picker.setSelectedItem(hh, minth);
        picker.setOnTimePickListener(new TimePicker.OnTimePickListener() {
            @Override
            public void onTimePicked(String hour, String minute) {
                String string = hour + ":" + minute;
                if (isStart) {
                    beanList.get(position).startTime = string;
                    setTime();
                } else {
//                    if (StringUtil.isEmpty(beanList.get(position).startTime) ||
//                            beanList.get(position).startTime.compareTo(string) > 0) {
//                        Crouton.showToast(ct, "下班时间不能小于上班时间", R.color.load_warning);
//                        return;
//                    }
                    beanList.get(position).endTime = string;
                    setTime();
                }
                adapter.notifyItemChanged(position);
            }
        });
        picker.show();

    }


    private class Bean {
        public String startTime = "";
        public String endTime = "";
        public boolean isToday = true;
    }
}

package com.uas.appworks.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ProgressBar;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.common.data.DateFormatUtil;
import com.uas.appworks.R;
import com.uas.appworks.model.bean.TimeHelperBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/8/23 11:26
 */
public class TimeHelperAdapter extends BaseQuickAdapter<TimeHelperBean, BaseViewHolder> {

    private int mTimePosition = 0;
    private int mTimeProgress = 0;

    private OnTimeClickListener mOnTimeClickListener;

    public void setOnTimeClickListener(OnTimeClickListener onTimeClickListener) {
        mOnTimeClickListener = onTimeClickListener;
    }

    public void setTimeProgress(int timePosition, int timeProgress) {
        mTimePosition = timePosition;
        mTimeProgress = timeProgress;

        notifyDataSetChanged();
    }

    public TimeHelperAdapter(@Nullable List<TimeHelperBean> timeHelperBeans) {
        super(R.layout.item_list_time_helper, timeHelperBeans);
    }

    @Override
    protected void convert(BaseViewHolder helper, TimeHelperBean item) {
//        helper.setIsRecyclable(false);
        final int adapterPosition = helper.getAdapterPosition();
        int scheduleType = item.getScheduleType();
        if (adapterPosition % 2 == 0) {
            helper.setVisible(R.id.item_time_helper_order_right_ll, false);
            helper.setVisible(R.id.item_time_helper_schedule_right_ll, false);
            helper.setVisible(R.id.item_time_helper_meeting_right_ll, false);
            helper.setVisible(R.id.item_time_helper_outwork_right_ll, false);
            helper.setVisible(R.id.item_time_helper_trip_right_ll, false);

            helper.setVisible(R.id.item_time_helper_order_left_ll, false);
            helper.setVisible(R.id.item_time_helper_schedule_left_ll, false);
            helper.setVisible(R.id.item_time_helper_meeting_left_ll, false);
            helper.setVisible(R.id.item_time_helper_outwork_left_ll, false);
            helper.setVisible(R.id.item_time_helper_trip_left_ll, false);

            helper.setVisible(R.id.item_time_helper_time_left, false);
            helper.setVisible(R.id.item_time_helper_currenttime_left, false);
            helper.setVisible(R.id.item_time_helper_currenttime_right, false);
            helper.setVisible(R.id.item_time_helper_time_right, true);

            String startTime = item.getStartTime();
            try {
                long startLong = DateFormatUtil.str2Long(item.getStartTime(), DateFormatUtil.YMD_HMS);
                startTime = DateFormatUtil.long2Str(startLong, DateFormatUtil.HM);
            } catch (Exception e) {

            }
            helper.setText(R.id.item_time_helper_time_right, startTime);

            if (adapterPosition == mTimePosition) {
                helper.setVisible(R.id.item_time_helper_currenttime_right, true);
                helper.setText(R.id.item_time_helper_currenttime_right,
                        DateFormatUtil.long2Str(System.currentTimeMillis(), DateFormatUtil.HM));
            }

            switch (scheduleType) {
                case TimeHelperBean.TYPE_TIME_HELPER_ORDER:
                    helper.setVisible(R.id.item_time_helper_order_left_ll, true);
                    helper.setText(R.id.item_time_helper_order_left_tv, item.getRemarks());
                    break;
                case TimeHelperBean.TYPE_TIME_HELPER_SCHEDULE:
                    helper.setVisible(R.id.item_time_helper_schedule_left_ll, true);
                    helper.setText(R.id.item_time_helper_schedule_left_tv, item.getRemarks());
                    break;
                case TimeHelperBean.TYPE_TIME_HELPER_MEETING:
                    helper.setVisible(R.id.item_time_helper_meeting_left_ll, true);
                    helper.setText(R.id.item_time_helper_meeting_left_tv, item.getRemarks());
                    break;
                case TimeHelperBean.TYPE_TIME_HELPER_OUTWORK:
                    helper.setVisible(R.id.item_time_helper_outwork_left_ll, true);
                    helper.setText(R.id.item_time_helper_outwork_left_tv, item.getRemarks());
                    break;
                case TimeHelperBean.TYPE_TIME_HELPER_TRIP:
                    helper.setVisible(R.id.item_time_helper_trip_left_ll, true);
                    helper.setText(R.id.item_time_helper_trip_left_tv, item.getRemarks());
                    break;
            }
        } else {
            helper.setVisible(R.id.item_time_helper_order_right_ll, false);
            helper.setVisible(R.id.item_time_helper_schedule_right_ll, false);
            helper.setVisible(R.id.item_time_helper_meeting_right_ll, false);
            helper.setVisible(R.id.item_time_helper_outwork_right_ll, false);
            helper.setVisible(R.id.item_time_helper_trip_right_ll, false);

            helper.setVisible(R.id.item_time_helper_order_left_ll, false);
            helper.setVisible(R.id.item_time_helper_schedule_left_ll, false);
            helper.setVisible(R.id.item_time_helper_meeting_left_ll, false);
            helper.setVisible(R.id.item_time_helper_outwork_left_ll, false);
            helper.setVisible(R.id.item_time_helper_trip_left_ll, false);

            helper.setVisible(R.id.item_time_helper_time_right, false);
            helper.setVisible(R.id.item_time_helper_currenttime_left, false);
            helper.setVisible(R.id.item_time_helper_currenttime_right, false);
            helper.setVisible(R.id.item_time_helper_time_left, true);

            String startTime = item.getStartTime();
            try {
                long startLong = DateFormatUtil.str2Long(item.getStartTime(), DateFormatUtil.YMD_HMS);
                startTime = DateFormatUtil.long2Str(startLong, DateFormatUtil.HM);
            } catch (Exception e) {

            }
            helper.setText(R.id.item_time_helper_time_left, startTime);

            if (adapterPosition == mTimePosition) {
                helper.setVisible(R.id.item_time_helper_currenttime_left, true);
                helper.setText(R.id.item_time_helper_currenttime_left,
                        DateFormatUtil.long2Str(System.currentTimeMillis(), DateFormatUtil.HM));
            }

            switch (scheduleType) {
                case TimeHelperBean.TYPE_TIME_HELPER_ORDER:
                    helper.setVisible(R.id.item_time_helper_order_right_ll, true);
                    helper.setText(R.id.item_time_helper_order_right_tv, item.getRemarks());
                    break;
                case TimeHelperBean.TYPE_TIME_HELPER_SCHEDULE:
                    helper.setVisible(R.id.item_time_helper_schedule_right_ll, true);
                    helper.setText(R.id.item_time_helper_schedule_right_tv, item.getRemarks());
                    break;
                case TimeHelperBean.TYPE_TIME_HELPER_MEETING:
                    helper.setVisible(R.id.item_time_helper_meeting_right_ll, true);
                    helper.setText(R.id.item_time_helper_meeting_right_tv, item.getRemarks());
                    break;
                case TimeHelperBean.TYPE_TIME_HELPER_OUTWORK:
                    helper.setVisible(R.id.item_time_helper_outwork_right_ll, true);
                    helper.setText(R.id.item_time_helper_outwork_right_tv, item.getRemarks());
                    break;
                case TimeHelperBean.TYPE_TIME_HELPER_TRIP:
                    helper.setVisible(R.id.item_time_helper_trip_right_ll, true);
                    helper.setText(R.id.item_time_helper_trip_right_tv, item.getRemarks());
                    break;
            }
        }

        if (adapterPosition < mTimePosition) {
            helper.setBackgroundRes(R.id.item_time_helper_indicate, R.drawable.shape_time_helper_indicate_checked);
            ((ProgressBar) helper.getView(R.id.item_time_helper_progress)).setProgress(100);
        } else if (adapterPosition == (mTimePosition) && mTimeProgress > 0) {
            helper.setBackgroundRes(R.id.item_time_helper_indicate, R.drawable.shape_time_helper_indicate_checked);
            ((ProgressBar) helper.getView(R.id.item_time_helper_progress)).setProgress(mTimeProgress);
        } else {
            helper.setBackgroundRes(R.id.item_time_helper_indicate, R.drawable.shape_time_helper_indicate);
            ((ProgressBar) helper.getView(R.id.item_time_helper_progress)).setProgress(0);
        }

        helper.setOnClickListener(R.id.item_time_helper_order_left_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_order_right_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_schedule_left_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_schedule_right_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_meeting_left_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_meeting_right_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_outwork_left_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_outwork_right_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_trip_left_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
        helper.setOnClickListener(R.id.item_time_helper_trip_right_ll, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeClick(adapterPosition);
            }
        });
    }

    private void timeClick(int position) {
        if (mOnTimeClickListener != null) {
            mOnTimeClickListener.onTimeClick(position);
        }
    }

    public interface OnTimeClickListener {
        void onTimeClick(int position);
    }
}


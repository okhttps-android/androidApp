package com.uas.appworks.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.data.CalendarUtil;
import com.uas.appworks.datainquiry.bean.SchemeConditionBean;
import com.core.utils.time.wheel.OASigninPicker;
import com.core.widget.view.MyGridView;
import com.uas.appworks.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by RaoMeng on 2017/8/14.
 * 数据查询列表页面高级查询配置选项适配器
 */
public class DeviceQueryConditionAdapter extends BaseAdapter {
    private List<SchemeConditionBean> objects = new ArrayList<SchemeConditionBean>();

    private Context context;
    private LayoutInflater layoutInflater;
    private boolean strChanged = true, numChanged1 = true, numChanged2 = true;
    private int mTouchPosition = -1, mTouchIndex = -1;

    public DeviceQueryConditionAdapter(Context context, List<SchemeConditionBean> objects) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public void resetTouchPosition() {
        mTouchPosition = -1;
        mTouchIndex = -1;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public SchemeConditionBean getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        if (convertView == null) {
        convertView = layoutInflater.inflate(R.layout.item_list_data_inquiry_exact_query, null);
        convertView.setTag(new ViewHolder(convertView));
//        }
        initializeViews((SchemeConditionBean) getItem(position), (ViewHolder) convertView.getTag(), position);
        return convertView;
    }

    private void initializeViews(final SchemeConditionBean object, final ViewHolder holder, int position) {
        holder.captionTv.setText(object.getCaption());
        holder.captionTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        switch (object.getType()) {

            case "N":
                holder.stringEdittext.setVisibility(View.GONE);
                holder.numEdittextLl.setVisibility(View.VISIBLE);
                holder.textviewLl.setVisibility(View.GONE);
                holder.gridview.setVisibility(View.GONE);
                List<SchemeConditionBean.Property> mProperties = object.getProperties();

                holder.numEdittext1.setTag(position);
                holder.numEdittext2.setTag(position);

                holder.numEdittext1.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!numChanged1 && object.getProperties().size() == 2) {
                            object.getProperties().get(0).setDisplay(s.toString());
                            object.getProperties().get(0).setValue(s.toString());
                        }
                    }
                });

                holder.numEdittext2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!numChanged2 && object.getProperties().size() == 2) {
                            object.getProperties().get(1).setDisplay(s.toString());
                            object.getProperties().get(1).setValue(s.toString());
                        }
                    }
                });

                holder.numEdittext1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            mTouchPosition = (int) holder.numEdittext1.getTag();
                            mTouchIndex = 1;
                        }
                    }
                });

                holder.numEdittext2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {
                            mTouchPosition = (int) holder.numEdittext2.getTag();
                            mTouchIndex = 2;
                        }
                    }
                });

                numChanged1 = true;
                numChanged2 = true;
                if (mProperties != null && mProperties.size() == 2) {
                    holder.numEdittext1.setText(mProperties.get(0).getDisplay());
                    numChanged1 = false;

                    holder.numEdittext2.setText(mProperties.get(1).getDisplay());
                    numChanged2 = false;
                }

                if (mTouchPosition == position) {
                    if (mTouchIndex == 1) {
                        holder.numEdittext1.requestFocus();
                        holder.numEdittext1.setSelection(holder.numEdittext1.getText().length());
                    } else if (mTouchIndex == 2) {
                        holder.numEdittext2.requestFocus();
                        holder.numEdittext2.setSelection(holder.numEdittext2.getText().length());
                    } else {
                        holder.numEdittext1.clearFocus();
                        holder.numEdittext2.clearFocus();
                    }
                } else {
                    holder.numEdittext1.clearFocus();
                    holder.numEdittext2.clearFocus();
                }
                break;
            case "D":
            case "CD":
            case "YM":
            case "YMV":
                holder.stringEdittext.setVisibility(View.GONE);
                holder.numEdittextLl.setVisibility(View.GONE);
                holder.textviewLl.setVisibility(View.VISIBLE);
                holder.gridview.setVisibility(View.GONE);

                mProperties = object.getProperties();
                final String type = object.getType();

                if (mProperties != null && mProperties.size() == 2) {
                    holder.textview1.setText(mProperties.get(0).getDisplay());
                    holder.textview2.setText(mProperties.get(1).getDisplay());
                }

                holder.textview1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String time1 = holder.textview1.getText().toString();
                        String[] times1 = time1.split("-");
                        if (("YM".equals(type) || "YMV".equals(type)) && time1.length() == 6) {
                            showDateDialog(time1.substring(0, 4), time1.substring(4, 6), null, holder.textview1, type, object, 0);
                        } else if (times1.length >= 3) {
                            showDateDialog(times1[0], times1[1], times1[2], holder.textview1, type, object, 0);
                        } else {
                            showDateDialog(null, null, null, holder.textview1, type, object, 0);
                        }
                    }
                });

                holder.textview2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String time2 = holder.textview2.getText().toString();
                        String[] times2 = time2.split("-");
                        if (("YM".equals(type) || "YMV".equals(type)) && time2.length() == 6) {
                            showDateDialog(time2.substring(0, 4), time2.substring(4, 6), null, holder.textview2, type, object, 1);
                        } else if (times2.length >= 3) {
                            showDateDialog(times2[0], times2[1], times2[2], holder.textview2, type, object, 1);
                        } else {
                            showDateDialog(null, null, null, holder.textview2, type, object, 1);
                        }
                    }
                });

                holder.calendar1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String time1 = holder.textview1.getText().toString();
                        String[] times1 = time1.split("-");
                        if (("YM".equals(type) || "YMV".equals(type)) && time1.length() == 6) {
                            showDateDialog(time1.substring(0, 4), time1.substring(4, 6), null, holder.textview1, type, object, 0);
                        } else if (times1.length >= 3) {
                            showDateDialog(times1[0], times1[1], times1[2], holder.textview1, type, object, 0);
                        } else {
                            showDateDialog(null, null, null, holder.textview1, type, object, 0);
                        }
                    }
                });

                holder.calendar2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String time2 = holder.textview2.getText().toString();
                        String[] times2 = time2.split("-");
                        if (("YM".equals(type) || "YMV".equals(type)) && time2.length() == 6) {
                            showDateDialog(time2.substring(0, 4), time2.substring(4, 6), null, holder.textview2, type, object, 1);
                        } else if (times2.length >= 3) {
                            showDateDialog(times2[0], times2[1], times2[2], holder.textview2, type, object, 1);
                        } else {
                            showDateDialog(null, null, null, holder.textview2, type, object, 1);
                        }
                    }
                });
                break;
            case "CBG":
            case "C":
            case "EC":
            case "R":
                mProperties = object.getProperties();

                if (mProperties != null) {
                    holder.stringEdittext.setVisibility(View.GONE);
                    holder.numEdittextLl.setVisibility(View.GONE);
                    holder.textviewLl.setVisibility(View.GONE);
                    holder.gridview.setVisibility(View.VISIBLE);

                    ItemGridDeviceQueryExactAdapter gridDeviceQueryExactAdapter
                            = new ItemGridDeviceQueryExactAdapter(context, mProperties);
                    holder.gridview.setAdapter(gridDeviceQueryExactAdapter);

                    holder.gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            List<SchemeConditionBean.Property> properties = object.getProperties();
                            for (int i = 0; i < properties.size(); i++) {
                                if (i == position) {
                                    properties.get(i).setState(!properties.get(i).isState());
                                }
                            }
//                            notifyDataSetChanged();
                            if (holder.gridview.getAdapter() != null) {
                                ((ItemGridDeviceQueryExactAdapter) holder.gridview.getAdapter()).notifyDataSetChanged();
                            }
                        }
                    });
                } else {
                    holder.stringEdittext.setVisibility(View.VISIBLE);
                    holder.numEdittextLl.setVisibility(View.GONE);
                    holder.textviewLl.setVisibility(View.GONE);
                    holder.gridview.setVisibility(View.GONE);

                    object.setType("S");
                    ArrayList<SchemeConditionBean.Property> propertyList = new ArrayList<>();
                    SchemeConditionBean.Property property = new SchemeConditionBean.Property();
                    propertyList.add(property);
                    object.setProperties(propertyList);

                    holder.stringEdittext.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            if (!strChanged && object.getProperties().size() == 1) {
                                object.getProperties().get(0).setDisplay(s.toString());
                                object.getProperties().get(0).setValue(s.toString());
                            }
                        }
                    });

                    strChanged = true;
                    if (propertyList != null && propertyList.size() == 1) {
                        holder.stringEdittext.setText(propertyList.get(0).getDisplay());
                        strChanged = false;
                    }
                }

                break;
            case "S":
            default:
                holder.stringEdittext.setVisibility(View.VISIBLE);
                holder.numEdittextLl.setVisibility(View.GONE);
                holder.textviewLl.setVisibility(View.GONE);
                holder.gridview.setVisibility(View.GONE);
                mProperties = object.getProperties();

                holder.stringEdittext.setTag(position);

                holder.stringEdittext.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!strChanged && object.getProperties().size() == 1) {
                            object.getProperties().get(0).setDisplay(s.toString());
                            object.getProperties().get(0).setValue(s.toString());
                        }
                    }
                });

                holder.stringEdittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus)
                            mTouchPosition = (int) v.getTag();
                    }
                });

                strChanged = true;
                if (mProperties != null && object.getProperties().size() == 1) {
                    holder.stringEdittext.setText(mProperties.get(0).getDisplay());
                    strChanged = false;
                }

                if (mTouchPosition == position) {
                    holder.stringEdittext.requestFocus();
                    holder.stringEdittext.setSelection(holder.stringEdittext.getText().length());
                } else {
                    holder.stringEdittext.clearFocus();
                }
                break;
        }
    }

    private void showDateDialog(String year, String month, String day, final TextView tv, String type, final SchemeConditionBean object, final int i) {
        boolean haveDay = true;
        if (("YM".equals(type) || "YMV".equals(type))) {
            haveDay = false;
        }
        OASigninPicker picker = new OASigninPicker((Activity) context, 2000, 2030, haveDay);
        picker.setRange(2030, 12, 31);
        try {
            if (haveDay && !TextUtils.isEmpty(day)) {
                picker.setSelectedItem(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));
            } else {
                picker.setSelectedItem(Integer.parseInt(year), Integer.parseInt(month));
            }
        } catch (Exception e) {
            if (haveDay) {
                picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth(), CalendarUtil.getDay());
            } else {
                picker.setSelectedItem(CalendarUtil.getYear(), CalendarUtil.getMonth());
            }
        }

        picker.setOnDateTimePickListener(new OASigninPicker.OnDateTimePickListener() {
            @Override
            public void setTime(String year, String month, String day) {
                String time = year + "-" + month + "-" + day;
                if (TextUtils.isEmpty(day)) {
                    time = year + month;
                }
                tv.setText(time);
                if (object.getProperties().size() == 2) {
                    object.getProperties().get(i).setDisplay(time);
                    object.getProperties().get(i).setValue(time);
                }
            }
        });
        picker.show();
    }

    protected class ViewHolder {
        private EditText stringEdittext;
        private TextView captionTv;
        private LinearLayout numEdittextLl;
        private EditText numEdittext1;
        private EditText numEdittext2;
        private LinearLayout textviewLl;
        private TextView textview1;
        private ImageView calendar1;
        private TextView textview2;
        private ImageView calendar2;
        private MyGridView gridview;

        public ViewHolder(View view) {
            stringEdittext = (EditText) view.findViewById(R.id.item_data_inquiry_exact_string_edittext);
            captionTv = (TextView) view.findViewById(R.id.item_data_inquiry_exact_caption_tv);
            numEdittextLl = (LinearLayout) view.findViewById(R.id.item_data_inquiry_exact_num_edittext_ll);
            numEdittext1 = (EditText) view.findViewById(R.id.item_data_inquiry_num_exact_edittext1);
            numEdittext2 = (EditText) view.findViewById(R.id.item_data_inquiry_num_exact_edittext2);
            textviewLl = (LinearLayout) view.findViewById(R.id.item_data_inquiry_exact_textview_ll);
            textview1 = (TextView) view.findViewById(R.id.item_data_inquiry_exact_textview1);
            calendar1 = (ImageView) view.findViewById(R.id.item_data_inquiry_exact_calendar1);
            textview2 = (TextView) view.findViewById(R.id.item_data_inquiry_exact_textview2);
            calendar2 = (ImageView) view.findViewById(R.id.item_data_inquiry_exact_calendar2);
            gridview = (MyGridView) view.findViewById(R.id.item_data_inquiry_exact_gridview);
        }
    }
}

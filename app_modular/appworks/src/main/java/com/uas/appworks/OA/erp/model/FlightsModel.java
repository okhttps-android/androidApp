package com.uas.appworks.OA.erp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.common.data.DateFormatUtil;
import com.common.data.StringUtil;
import com.core.app.MyApplication;
import com.core.utils.TimeUtils;
import com.uas.appworks.R;


/**
 * Created by Bitliker on 2017/1/17.
 */

public class FlightsModel implements Parcelable {
    private int id;              //id
    private String code;//编号
    private int type;         //类型  1、正常班次（可有排版 可删除）  2.默认班次 （有排版  无删除） 3.自由排班
    private String name;     //规则名称
    private int count;         //成员个数
    private int departments;         //成员个数
    private String day;    //日期   星期一，星期二

    private FlightsTimeModel timeModel;  //时间，包含最早上班 和总时间 和上班日期
    private EmployeesModel employeesModel;
    private EmployeesModel hrorgsModel;

    public FlightsModel() {
    }


    protected FlightsModel(Parcel in) {
        id = in.readInt();
        code = in.readString();
        type = in.readInt();
        name = in.readString();
        count = in.readInt();
        departments = in.readInt();
        day = in.readString();
        timeModel = in.readParcelable(FlightsTimeModel.class.getClassLoader());
        employeesModel = in.readParcelable(EmployeesModel.class.getClassLoader());
        hrorgsModel = in.readParcelable(EmployeesModel.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(code);
        dest.writeInt(type);
        dest.writeString(name);
        dest.writeInt(count);
        dest.writeInt(departments);
        dest.writeString(day);
        dest.writeParcelable(timeModel, flags);
        dest.writeParcelable(employeesModel, flags);
        dest.writeParcelable(hrorgsModel, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FlightsModel> CREATOR = new Creator<FlightsModel>() {
        @Override
        public FlightsModel createFromParcel(Parcel in) {
            return new FlightsModel(in);
        }

        @Override
        public FlightsModel[] newArray(int size) {
            return new FlightsModel[size];
        }
    };

    public int getDepartments() {
        return departments;
    }

    public void setDepartments(int departments) {
        this.departments = departments;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public FlightsTimeModel getTimeModel() {
        return timeModel;
    }

    public void setTimeModel(FlightsTimeModel timeModel) {
        this.timeModel = timeModel;
    }

    public EmployeesModel getEmployeesModel() {
        return employeesModel;
    }

    public void setEmployeesModel(EmployeesModel employeesModel) {
        this.employeesModel = employeesModel;
    }

    public EmployeesModel getHrorgsModel() {
        return hrorgsModel;
    }

    public void setHrorgsModel(EmployeesModel hrorgsModel) {
        this.hrorgsModel = hrorgsModel;
    }

    /**
     * 通过转过来的2,3,4转变成周一、
     *
     * @return
     */
    public String getWeek() {
        try {
            if (StringUtil.isEmpty(day)) return "";
            StringBuilder builder = new StringBuilder("周");
            String[] d = day.split(",");
            for (String e : d) {
                builder.append(getWeek(Integer.valueOf(e)));
            }
            StringUtil.removieLast(builder);
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String getWeek(int day) {
        String str = "";
        switch (day) {
            case 1:
                str = "一";
                break;
            case 2:
                str = "二";
                break;
            case 3:
                str = "三";
                break;
            case 4:
                str = "四";
                break;
            case 5:
                str = "五";
                break;
            case 6:
                str = "六";
                break;
            case 7:
                str = "日";
                break;
        }
        return str + "、";
    }

    /**
     * 通过FlightsTimeModel对象判断相关的时间显示
     *
     * @return
     */
    public String getTime() {
        StringBuilder builder = new StringBuilder();
        if (!StringUtil.isEmpty(timeModel.getWd_ondutyone())) {
            builder.append(timeModel.getWd_ondutyone() + "-" + timeModel.getWd_offdutyone() + " ");
        }
        if (!StringUtil.isEmpty(timeModel.getWd_ondutytwo())) {
            builder.append(timeModel.getWd_ondutytwo() + "-" + timeModel.getWd_offdutytwo() + " ");
        }
        if (!StringUtil.isEmpty(timeModel.getWd_ondutythree())) {
            builder.append(timeModel.getWd_ondutythree() + "-" + timeModel.getWd_offdutythree() + " ");
        }

        //计算小时
        float allTime = 0;//分钟
        try {
            allTime = getAllTime() / 60;
        } catch (Exception e) {
            e.printStackTrace();
        }
        float h = allTime / 60f;
        String hour = float2String(h);
        builder.append(hour + MyApplication.getInstance().getString(R.string.hour));
        return builder.toString();
    }

    // 班次时间显示，不显示总时数
    public String getTimeTable() {
        StringBuilder builder = new StringBuilder();
        if (!StringUtil.isEmpty(timeModel.getWd_ondutyone())) {
            builder.append(timeModel.getWd_ondutyone() + "-" + timeModel.getWd_offdutyone() + " ");
        }
        if (!StringUtil.isEmpty(timeModel.getWd_ondutytwo())) {
            builder.append(timeModel.getWd_ondutytwo() + "-" + timeModel.getWd_offdutytwo() + " ");
        }
        if (!StringUtil.isEmpty(timeModel.getWd_ondutythree())) {
            builder.append(timeModel.getWd_ondutythree() + "-" + timeModel.getWd_offdutythree() + " ");
        }
        return builder.toString();
    }

    private String float2String(float hour) {
        if (hour == 0) return "0";
        String h = String.valueOf(hour);
        if (!StringUtil.isEmpty(h) && h.length() > 2) {
            h = h.substring(0, hour < 10 ? 3 : 4);
        }
        if (h.indexOf(".") > 0) {
            //正则表达
            h = h.replaceAll("0+?$", "");//去掉后面无用的零
            h = h.replaceAll("[.]$", "");//如小数点后面全是零则去掉小数点
        }
        return h;
    }

    public int getAllTime() throws Exception {
        int time = 0;
        if (!StringUtil.isEmpty(timeModel.getWd_ondutyone()) && !StringUtil.isEmpty(timeModel.getWd_offdutyone())) {
            time += DateFormatUtil.getDifferSS(timeModel.getWd_ondutyone(), timeModel.getWd_offdutyone());
        }
        if (!StringUtil.isEmpty(timeModel.getWd_ondutytwo()) && !StringUtil.isEmpty(timeModel.getWd_offdutytwo())) {
            time += DateFormatUtil.getDifferSS(timeModel.getWd_ondutytwo(), timeModel.getWd_offdutytwo());
        }
        if (!StringUtil.isEmpty(timeModel.getWd_ondutythree()) && !StringUtil.isEmpty(timeModel.getWd_offdutythree())) {
            time += DateFormatUtil.getDifferSS(timeModel.getWd_ondutythree(), timeModel.getWd_offdutythree());
        }
        return time;
    }

    public long getTime(String start, String end) {
        String startTime = DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + start + ":00";
        String endTime = DateFormatUtil.long2Str(DateFormatUtil.YMD) + " " + end + ":00";
        return TimeUtils.f_str_2_long(endTime) - TimeUtils.f_str_2_long(startTime);
    }
}

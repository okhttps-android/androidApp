package com.uas.appworks.OA.erp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 员工对象和部门对象   只要编号和名字   别的不要
 * Created by Bitliker on 2017/1/22.
 */

public class EmployeesModel implements Parcelable {
    private String employeeNames;
    private String employeecode;

    public EmployeesModel() {

    }

    protected EmployeesModel(Parcel in) {
        employeeNames = in.readString();
        employeecode = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(employeeNames);
        dest.writeString(employeecode);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EmployeesModel> CREATOR = new Creator<EmployeesModel>() {
        @Override
        public EmployeesModel createFromParcel(Parcel in) {
            return new EmployeesModel(in);
        }

        @Override
        public EmployeesModel[] newArray(int size) {
            return new EmployeesModel[size];
        }
    };

    public String getEmployeeNames() {
        return employeeNames;
    }

    public EmployeesModel setEmployeeNames(String employeeNames) {
        this.employeeNames = employeeNames;
        return this;
    }

    public String getEmployeecode() {
        return employeecode;
    }

    public EmployeesModel setEmployeecode(String employeecode) {
        this.employeecode = employeecode;
        return this;
    }
}

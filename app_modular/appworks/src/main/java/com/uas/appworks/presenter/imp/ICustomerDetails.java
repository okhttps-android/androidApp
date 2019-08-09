package com.uas.appworks.presenter.imp;

import com.core.base.HttpImp;
import com.modular.apputils.model.BillGroupModel;
import com.modular.apputils.presenter.imp.IBillDetails;
import com.uas.appworks.model.CustomerBindBill;

import java.util.ArrayList;
import java.util.List;

public interface  ICustomerDetails extends IBillDetails {
    void setBottomDatas(ArrayList<CustomerBindBill> mCusBusiness, ArrayList<CustomerBindBill> mCusContacts, ArrayList<CustomerBindBill> mCusReport
            , ArrayList<CustomerBindBill> mCusAddress);
}

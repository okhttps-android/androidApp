package com.uas.appworks.activity.businessManage.businessManageHome;

import android.content.Context;

import com.core.base.presenter.MvpPresenter;
import com.core.base.view.MvpView;
import com.uas.appworks.model.bean.BusinessOverTimeBean;
import com.uas.appworks.model.bean.BusinessRankBean;
import com.uas.appworks.model.bean.BusinessRecordBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/10 14:36
 */
public class BusinessManageHomeContract {

    public interface IBusinessManageHomeView extends MvpView {
        void requestDataSuccess(String resultJson);

        void requestRecordSuccess(List<BusinessRecordBean> businessRecordBeans);

        void requestOvertimeSuccess(List<BusinessOverTimeBean> businessOverTimeBeans);

        void requestRankSuccess(List<BusinessRankBean> businessRankBeans);

        void requestAllSuccess(String resultJson, List<BusinessRecordBean> businessRecordBeans,
                               List<BusinessOverTimeBean> businessOverTimeBeans, List<BusinessRankBean> businessRankBeans);

        void requestOptionSuccess(int flag, String resultJson);

        void requestFail(int flag, String failStr);
    }

    public interface IBusinessManageHomePresenter extends MvpPresenter<IBusinessManageHomeView> {
        void getBusinessData(Context context, String dataTime);

        void getBusinessRecord(Context context, String salesmanCode, int pageIndex, int pageSize);

        void getBusinessOvertime(Context context, String salesmanCode, int pageIndex, int pageSize);

        void getBusinessRank(Context context, int pageIndex, int pageSize);

        void getBusinessAll(Context context, String dataTime, String salesmanCode);

        void getOptionList(Context context, int flag, String caller, String code);
    }
}

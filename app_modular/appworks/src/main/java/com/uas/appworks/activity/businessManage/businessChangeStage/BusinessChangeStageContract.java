package com.uas.appworks.activity.businessManage.businessChangeStage;

import android.content.Context;

import com.core.base.presenter.MvpPresenter;
import com.core.base.view.MvpView;
import com.uas.appworks.model.bean.BusinessStageBean;
import com.uas.appworks.model.bean.ChangeStageBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/22 11:27
 */
public class BusinessChangeStageContract {
    interface IBusinessChangeStageView extends MvpView {
        void changeStageSuccess();

        void updateScheduleSuccess();

        void requestFail(int flag, String failStr);
    }

    interface IBusinessChangeStagePresenter extends MvpPresenter<IBusinessChangeStageView> {
        void requestChangeStage(Context context, List<ChangeStageBean> changeStageBeans, BusinessStageBean currentStage, BusinessStageBean nextStage, String remarks, String bc_code);

        void requestUpdataSchedule(Context context, String bc_code);
    }
}

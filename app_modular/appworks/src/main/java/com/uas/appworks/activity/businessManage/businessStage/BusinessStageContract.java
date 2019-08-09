package com.uas.appworks.activity.businessManage.businessStage;

import android.content.Context;

import com.core.base.presenter.MvpPresenter;
import com.core.base.view.MvpView;
import com.uas.appworks.model.bean.BusinessStageBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/12 17:54
 */
public class BusinessStageContract {

    public interface IBusinessStageView extends MvpView {
        void requestStageSuccess(List<BusinessStageBean> businessStageBeans);

        void requestStageFail(String failStr);
    }

    public interface IBusinessStagePresenter extends MvpPresenter<IBusinessStageView> {
        void requestStageList(Context context);
    }
}

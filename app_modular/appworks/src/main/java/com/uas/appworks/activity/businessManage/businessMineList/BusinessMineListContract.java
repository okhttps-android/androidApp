package com.uas.appworks.activity.businessManage.businessMineList;

import android.content.Context;

import com.core.base.presenter.MvpPresenter;
import com.core.base.view.MvpView;
import com.uas.appworks.model.bean.BusinessMineChildBean;
import com.uas.appworks.model.bean.CommonColumnsBean;

import java.util.List;
import java.util.Map;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/11 17:23
 */
public class BusinessMineListContract {

    public interface IBusinessMineListView extends MvpView {
        void requestListSuccess(List<BusinessMineChildBean> businessMineBeans, List<CommonColumnsBean> commonColumnsBeans);

        void requestListFail(String failMsg);
    }

    public interface IBusinessMineListPresenter extends MvpPresenter<IBusinessMineListView> {
        void getBusinessMineList(Context context, Map<String, Object> params);
    }
}

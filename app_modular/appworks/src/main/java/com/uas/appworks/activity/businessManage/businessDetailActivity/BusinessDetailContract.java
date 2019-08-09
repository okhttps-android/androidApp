package com.uas.appworks.activity.businessManage.businessDetailActivity;

import android.content.Context;

import com.alibaba.fastjson.JSONArray;
import com.core.base.presenter.MvpPresenter;
import com.core.base.view.MvpView;
import com.core.model.SelectEmUser;
import com.uas.appworks.model.bean.BusinessStageBean;
import com.uas.appworks.model.bean.CommonFormBean;

import java.util.List;

/**
 * @author RaoMeng
 * @describe
 * @date 2018/9/18 14:08
 */
public class BusinessDetailContract {

    interface BusinessDetailView extends MvpView {
        void requestMainDetailSuccess(List<CommonFormBean> allDetailList, List<CommonFormBean> visibleDetailList);

        void requestStageSuccess(List<BusinessStageBean> businessStageBeans);

        void changeDomanSuccess();

        void requestScheduleListSuccess(JSONArray jsonArray);

        void requestBusinessTypeSuccess(boolean isGra, boolean isDis);

        void canBusinessReceiveSuccess();

        void requestBusinessReceiveSuccess(int type);

        void requestFail(int flag, String failStr);
    }

    interface BusinessDetailPresenter extends MvpPresenter<BusinessDetailView> {
        void requestMainDetail(Context context, int ID, String caller);

        void requestStageList(Context context);

        void changeDoman(Context context, SelectEmUser user, String bc_code);

        void requestScheduleList(Context context, String bc_code, String emname);

        void requestBusinessType(Context context, int bc_id, String admincode);

        void canBusinessReceive(Context context);

        void requestBusinessReceive(Context context, int type, String bc_code, String bc_doman, String bc_domancode);
    }
}

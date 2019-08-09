package com.uas.appworks.OA.erp.model;

import com.core.base.HttpImp;
import com.core.model.MissionModel;
import com.core.model.SelectBean;

import java.util.List;

/**
 * Created by Bitliker on 2016/12/19.
 */

public interface IMission extends HttpImp {

    void showModels(List<MissionModel> models);

    void showFinds(List<SelectBean> models);

    void changModelStatus(int status, int postion);

    void faceSign(MissionModel missionModel);

    void faceUpload(MissionModel mission, String faceBase64);
}

package com.uas.appworks.OA.erp.presenter;

import android.content.Context;
import android.content.Intent;

import com.core.model.MissionModel;

import java.util.List;

/**
 * Created by Bitliker on 2016/12/16.
 */

public interface IMissionPresenter {
	/**
	 * 1.从网络下拉数据
	 * 2.将数据保存到数据库
	 * 3.从数据库取出数据
	 */

	void start(Intent intent);//1.从网上下拉数据   保存数据


	void submit(List<MissionModel> models);

	void sign(List<MissionModel> models);

	void signinMission(MissionModel model,String faceBase64);

	void finder(String licker);

	void uploadFace(Context context, MissionModel mission, String faceBase64);
}

package com.modular.apputils.listener;

import java.io.Serializable;

/**
 * Created by Bitlike on 2017/11/13.
 */

public interface OnPlayListener   extends Serializable {

	void onSuccess(String resultStatus,String resultInfo);
	void onFailure(String resultStatus,String resultInfo);
}

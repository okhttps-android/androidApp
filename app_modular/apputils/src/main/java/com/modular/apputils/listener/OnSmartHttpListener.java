package com.modular.apputils.listener;


import com.modular.apputils.network.Tags;

/**
 * Created by Bitliker on 2017/9/29.
 */

public interface OnSmartHttpListener {

	void onSuccess(int what,String message, Tags tag) throws Exception;

	void onFailure(int what,String message, Tags tag) throws Exception;


}

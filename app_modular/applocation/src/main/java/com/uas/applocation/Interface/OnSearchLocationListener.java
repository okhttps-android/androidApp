package com.uas.applocation.Interface;

import com.uas.applocation.model.UASLocation;

import java.util.List;

public interface OnSearchLocationListener {
    void onCallBack(boolean isSuccess, List<UASLocation> locations);
}

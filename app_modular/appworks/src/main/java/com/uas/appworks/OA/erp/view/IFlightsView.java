package com.uas.appworks.OA.erp.view;

import com.core.base.HttpImp;
import com.uas.appworks.OA.erp.model.FlightsModel;

import java.util.List;


/**
 * Created by Bitliker on 2017/1/16.
 */

public interface IFlightsView extends HttpImp {
    void showModel(List<FlightsModel> models);
    void deleteModel(int position);
}

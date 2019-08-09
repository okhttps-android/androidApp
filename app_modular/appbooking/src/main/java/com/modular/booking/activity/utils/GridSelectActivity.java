package com.modular.booking.activity.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.alibaba.fastjson.JSON;
import com.common.LogUtil;
import com.common.data.ListUtils;
import com.common.data.StringUtil;
import com.core.app.AppConstant;
import com.core.base.OABaseActivity;
import com.core.model.SelectBean;
import com.modular.apputils.activity.SingleImagePreviewActivity;
import com.modular.booking.R;
import com.modular.booking.adapter.ItemRoomsSelectAdapter;
import com.modular.booking.model.SBMenuModel;

import java.util.ArrayList;
import java.util.List;

/**
  * @desc:功能界面  表格选择界面
  * @author：Arison on 2017/11/1
  */
public class GridSelectActivity extends OABaseActivity{
    ItemRoomsSelectAdapter itemAdapter;
    private GridView gvTopic;
    private Context mContext;
    private List<SBMenuModel> menuModels=new ArrayList<>();
    ArrayList<SelectBean> selectBeens = new ArrayList<SelectBean>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_select);
        gvTopic = (GridView) findViewById(R.id.gv_topic);
        mContext=this;
        initView();
        initEvent();
    }
    
    private void initView(){
        if (getIntent()!=null){
           selectBeens=getIntent().getParcelableArrayListExtra("data");
          setTitle(getIntent().getStringExtra("title"));
           if (!ListUtils.isEmpty(selectBeens)){
               for (int i = 0; i < selectBeens.size(); i++) {
                   SelectBean selectBean=selectBeens.get(i);
                   com.alibaba.fastjson.JSONObject object= JSON.parseObject(selectBean.getJson()) ;
                   SBMenuModel menuModel=new SBMenuModel();
                   menuModel.setUrl(object.getString("st_imageurl"));
                   if (StringUtil.isEmpty(object.getString("st_name"))){
                       menuModel.setTitle(object.getString("sm_username"));//医生
                   }else{
                       menuModel.setTitle(object.getString("st_name"));
                   }
                   menuModel.setCode(object.getString("sm_userid"));
                   menuModel.setData(object.toJSONString());
                   menuModels.add(menuModel);
               }
           }
        }
        LogUtil.prinlnLongMsg("GridSelectActivity","models:"+JSON.toJSONString(menuModels));
        itemAdapter = new ItemRoomsSelectAdapter(mContext, menuModels);
        gvTopic.setAdapter(itemAdapter);
    }
    private static final String TAG = "GridSelectActivity";
    public void initEvent(){
        itemAdapter.setmOnBookClickListener(new ItemRoomsSelectAdapter.OnBookClickListener() {
            @Override
            public void onBookClick(View view, int position) {
                SBMenuModel menuModel = menuModels.get(position);
                LogUtil.d(TAG, "按钮点击事件!");
                ToastMessage("" + menuModel.getDesc() + menuModel.getTitle());

                if (menuModel.isBooking()) {
                    ToastMessage("包间已满，不可预订！");
                } else {
                    setResult(0x21, new Intent().putExtra("data", menuModel));
                    finish();
                }
            }
        });

        itemAdapter.setmOnImageClickListener(new ItemRoomsSelectAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(View view, int position) {
                SBMenuModel menuModel = menuModels.get(position);
                LogUtil.d(TAG, "图片点击事件!");
                String loginUserId = menuModel.getUrl();
                Intent intent = new Intent(activity, SingleImagePreviewActivity.class);
                intent.putExtra(AppConstant.EXTRA_IMAGE_URI, loginUserId);
                startActivity(intent);
            }
        });
//        gvTopic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                 final ItemRoomsSelectAdapter.ViewHolder menuModel = (ItemRoomsSelectAdapter.ViewHolder) view.getTag();
//                menuModel.tvBookAction.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
////                        ToastMessage("点击事件！");
//                        LogUtil.d(TAG,"按钮点击事件!");
//                        ToastMessage(""+menuModel.model.getDesc()+menuModel.model.getTitle());
//
//                        if (menuModel.model.isBooking()){
//                            ToastMessage("包间已满，不可预订！");
//                        }else{
//                            setResult(0x21,new Intent().putExtra("data",menuModel.model));
//                            finish();
//                        }
//                    }
//                });
////                
//                menuModel.ivItem.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        LogUtil.d(TAG,"图片点击事件!");
//                        String loginUserId =menuModel.model.getUrl();
//                        Intent intent = new Intent(activity, SingleImagePreviewActivity.class);
//                        intent.putExtra(AppConstant.EXTRA_IMAGE_URI, loginUserId);
//                        startActivity(intent);
//                    }
//                });
//               
//               
//            }
//        });
       
    }
}

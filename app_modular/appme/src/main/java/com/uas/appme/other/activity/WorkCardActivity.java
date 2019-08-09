package com.uas.appme.other.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.aip.excep.utils.Base64Util;
import com.common.LogUtil;
import com.common.config.BaseConfig;
import com.common.data.JSONUtil;
import com.common.data.ListUtils;
import com.core.base.BaseActivity;
import com.core.utils.CommonUtil;
import com.me.imageloader.ImageLoaderUtil;
import com.modular.apputils.listener.OnSmartHttpListener;
import com.modular.apputils.network.Parameter;
import com.modular.apputils.network.Tags;
import com.modular.apputils.utils.ImageViewUtils;
import com.modular.apputils.utils.TestStr;
import com.modular.apputils.utils.UUHttpHelper;
import com.uas.appme.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class WorkCardActivity extends BaseActivity {
    private ImageView logoIv;
    private TextView companyNameTv;
    private TextView nameTv;
    private TextView emCodeTv;
    private TextView positionTv;
    private CircleImageView headIv;

    private UUHttpHelper mUUHttpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_card);
        initView();
    }

    private void initView() {
        mUUHttpHelper = new UUHttpHelper(CommonUtil.getAppBaseUrl(ct));
        logoIv = (ImageView) findViewById(R.id.logoIv);
        companyNameTv = (TextView) findViewById(R.id.companyNameTv);
        nameTv = (TextView) findViewById(R.id.nameTv);
        emCodeTv = (TextView) findViewById(R.id.emCodeTv);
        positionTv = (TextView) findViewById(R.id.positionTv);
        headIv = (CircleImageView) findViewById(R.id.headIv);


        loadData();
    }

    private void loadData() {
        mUUHttpHelper.requestCompanyHttp(new Parameter.Builder()
                        .url("mobile/oa/getLabourCardInfor.action")
                        .addParams("emcode", CommonUtil.getEmcode())
                , new OnSmartHttpListener() {
                    @Override
                    public void onSuccess(int what, String message, Tags tag) throws Exception {
                        JSONObject object = JSON.parseObject(message);
                        JSONArray mLabourCardInfors = JSONUtil.getJSONArray(object, "LabourCardInfor");
                        if (!ListUtils.isEmpty(mLabourCardInfors)) {
                            handlerData(mLabourCardInfors.getJSONObject(0));
                        }else{
                            handlerData(new JSONObject());
                        }
                    }

                    @Override
                    public void onFailure(int what, String message, Tags tag) throws Exception {
                        handlerData(new JSONObject());
                    }
                });

        //TODO 测试数据
        if (BaseConfig.isDebug()) {
            JSONObject object = JSON.parseObject(TestStr.LABOUR_CARD_INFOR);
            JSONArray mLabourCardInfors = JSONUtil.getJSONArray(object, "LabourCardInfor");
            if (!ListUtils.isEmpty(mLabourCardInfors)) {
                handlerData(mLabourCardInfors.getJSONObject(0));
            }
        }
    }


    private void handlerData(JSONObject mLabourCardInfor) {
        String em_cop = JSONUtil.getText(mLabourCardInfor, "EM_COP");
        String em_photourl = JSONUtil.getText(mLabourCardInfor, "EM_PHOTOURL");
        String em_name = JSONUtil.getText(mLabourCardInfor, "EM_NAME");
        String em_code = JSONUtil.getText(mLabourCardInfor, "EM_CODE");
        String em_sex = JSONUtil.getText(mLabourCardInfor, "EM_SEX");
        String em_mobile = JSONUtil.getText(mLabourCardInfor, "EM_MOBILE");
        String em_position = JSONUtil.getText(mLabourCardInfor, "EM_DEPART");
        String en_logo = JSONUtil.getText(mLabourCardInfor, "EN_LOGO");

        companyNameTv.setText(em_cop);
        nameTv.setText(em_name);
        emCodeTv.setText("ID " + em_code);
        positionTv.setText(em_position);
        positionTv.setText(em_position);
        String headUrl = ImageViewUtils.getErpImageUrl(em_photourl);
        LogUtil.i("gong", "headUrl=" + headUrl);
        ImageLoaderUtil.getInstance().loadImage(headUrl, headIv);
        Bitmap logoMap = Base64Util.base64ToBitmap(en_logo);
        if (logoMap != null) {
            logoIv.setImageBitmap(logoMap);
        }
    }
}

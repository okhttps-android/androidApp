package com.xzjmyk.pm.activity.ui.erp.activity.oa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.andreabaccega.widget.FormEditText;
import com.common.data.JSONUtil;
import com.common.data.StringUtil;
import com.common.system.DisplayUtil;
import com.common.ui.CameraUtil;
import com.common.ui.ImageUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.core.app.MyApplication;
import com.xzjmyk.pm.activity.R;
import com.core.base.BaseActivity;
import com.core.widget.view.model.SelectAimModel;
import com.core.utils.ToastUtil;
import com.core.utils.CommonInterface;
import com.xzjmyk.pm.activity.util.oa.CommonUtil;
import com.modular.apputils.utils.PopupWindowHelper;
import com.core.net.http.http.OnHttpResultListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AddContactsActivity extends BaseActivity implements View.OnClickListener, OnHttpResultListener {

    private final int SELECT_COMPANY = 0x19;
    private final int PICK_PHOTO = 0x20;
    private final int CAPTURE_PHOTO = 0x21;
    @ViewInject(R.id.name_et)
    private FormEditText name_et;
    @ViewInject(R.id.position_et)
    private FormEditText position_et;
    @ViewInject(R.id.mobile_et)
    private FormEditText mobile_et;
    @ViewInject(R.id.company_tv)
    private TextView company_tv;
    @ViewInject(R.id.company_add_tv)
    private TextView company_add_tv;
    @ViewInject(R.id.card_img)
    private ImageView card_img;
    private Uri mNewPhotoUri;
    private PopupWindow imagePopWindow;
    private File imageFile;
    private boolean submiting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        ViewUtils.inject(this);
        initView();
        initEnvet();
    }

    private void initEnvet() {
        findViewById(R.id.submit_btn).setOnClickListener(this);
        findViewById(R.id.company_tv).setOnClickListener(this);
        findViewById(R.id.card_img).setOnClickListener(this);
    }

    private void initView() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit_btn:
                if (canSubmit()) {
                    if (!CommonUtil.isNetWorkConnected(ct)){
                        ToastMessage(getString(R.string.networks_out));
                        break;
                    }
                    submit();
                }
                break;
            case R.id.company_tv:
                startActivityForResult(new Intent(ct, SelectAimActivity.class).putExtra("type", 1), SELECT_COMPANY);
                break;
            case R.id.card_img:
                createImagePopWindow();
                break;
            case R.id.head_take_picture:
                mNewPhotoUri = CameraUtil.getOutputMediaFileUri(ct, MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                CameraUtil.captureImage(AddContactsActivity.this, mNewPhotoUri, CAPTURE_PHOTO);
                cloneImagePopWindow();
                break;
            case R.id.head_select_photos:
                mNewPhotoUri = CameraUtil.getOutputMediaFileUri(ct,MyApplication.getInstance().mLoginUser.getUserId(), CameraUtil.MEDIA_TYPE_IMAGE);
                CameraUtil.pickImageSimple(AddContactsActivity.this, PICK_PHOTO);
                cloneImagePopWindow();
                break;
            case R.id.head_cancel:
                cloneImagePopWindow();
                break;
        }
    }


    private void submit() {
        submiting = true;
        List<Map<String, Object>> formStores = new ArrayList<>();
        formStores.add(CommonInterface.getInstance().getFormStoreContact(
                String.valueOf(company_tv.getTag()),
                StringUtil.getTextRexHttp(name_et),
                StringUtil.getTextRexHttp(mobile_et),
                StringUtil.getTextRexHttp(company_tv),
                String.valueOf(company_tv.getTag()),
                StringUtil.getTextRexHttp(company_add_tv),
                position_et.getText().toString()));
        CommonInterface.getInstance().addContact(formStores, this);
    }

    private boolean canSubmit() {
        if (!name_et.testValidity() || !position_et.testValidity() || !mobile_et.testValidity())
            return false;
        if (submiting) return false;
        return true;
    }


    private void createImagePopWindow() {
        if (imagePopWindow == null) initPopupWindow();
        imagePopWindow.showAtLocation(getWindow().getDecorView().
                findViewById(android.R.id.content), Gravity.BOTTOM, 0, 0);
        DisplayUtil.backgroundAlpha(this, 0.4f);
    }

    private void cloneImagePopWindow() {
        if (imagePopWindow != null) {
            imagePopWindow.dismiss();
            DisplayUtil.backgroundAlpha(AddContactsActivity.this, 1f);
            imagePopWindow = null;
        }
    }

    private void initPopupWindow() {
        View viewContext = LayoutInflater.from(ct).inflate(R.layout.layout_select_head, null);
        viewContext.findViewById(R.id.head_take_picture).setOnClickListener(this);
        viewContext.findViewById(R.id.head_select_photos).setOnClickListener(this);
        viewContext.findViewById(R.id.head_cancel).setOnClickListener(this);
        imagePopWindow = new PopupWindow(viewContext, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        imagePopWindow.setAnimationStyle(R.style.MenuAnimationFade);
        imagePopWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.bg_popuwin));
        imagePopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                cloneImagePopWindow();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_COMPANY && resultCode == 0x20) {
            if (data == null) return;
            SelectAimModel entity = data.getParcelableExtra("data");
            PopupWindowHelper.create(this, getString(R.string.perfect_company_name), entity, new PopupWindowHelper.OnClickListener() {
                @Override
                public void result(SelectAimModel model) {
                    if (JSONUtil.validate(model.getObject())) {
                        JSONObject object = JSON.parseObject(model.getObject());
                        String code = JSONUtil.getText(object, "CU_CODE");
                        String name = JSONUtil.getText(object, "CU_NAME");
                        String address = JSONUtil.getText(object, "CU_ADD1");
                        company_tv.setText(StringUtil.isEmpty(name) ? "" : model.getName());
                        company_add_tv.setText(StringUtil.isEmpty(address) ? "" : model.getAddress());
                        company_tv.setTag(code);
                    }
                }
            });
        } else if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAPTURE_PHOTO) {
                if (mNewPhotoUri != null) {
                    saveImageFile(mNewPhotoUri.getPath());
                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                }
            } else if (requestCode == PICK_PHOTO) {
                if (data != null && data.getData() != null) {
                    saveImageFile(CameraUtil.getImagePathFromUri(ct, data.getData()));
                } else {
                    ToastUtil.showToast(this, R.string.c_photo_album_failed);
                }
            }
        }
    }

    private void saveImageFile(String path) {
        if (StringUtil.isEmpty(path)) return;
        imageFile = ImageUtil.compressBitmapToFile(path, 100, 300, 300);
        if (imageFile != null)
            ImageLoader.getInstance().displayImage(Uri.fromFile(imageFile).toString(), card_img);
    }


    @Override
    public void result(int what, boolean isJSON, String message, Bundle bundle) {
        if (isJSON) {
            switch (what) {
                case CommonInterface.ADD_CONTACT:
                    submiting = false;
                    ToastUtil.showToast(ct, R.string.save_success);
                    name_et.setText("");
                    position_et.setText("");
                    mobile_et.setText("");
                    break;
            }
        }
    }

    @Override
    public void error(int what, String message, Bundle bundle) {
        if (what == CommonInterface.ADD_CONTACT) {
            submiting = false;
            ToastUtil.showToast(ct, R.string.save_failed);
        }
    }
}

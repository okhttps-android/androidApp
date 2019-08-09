package com.core.utils.pictureselector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.common.ui.CameraUtil;
import com.core.app.AppConstant;
import com.core.app.MyApplication;
import com.core.app.R;
import com.core.base.BaseActivity;
import com.core.utils.ToastUtil;
import com.core.widget.view.Activity.ImgFileListActivity;
import com.core.widget.view.Activity.MultiImagePreviewActivity;
import com.core.widget.view.MyGridView;
import com.lidroid.xutils.ViewUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by FANGlh on 2017/7/3.
 * function:
 */

public class PictureSelectorDemo extends BaseActivity {
    private MyGridView grid_view;
    private ArrayList<String> mPhotoList;
    private ComPictureAdapter mAdapter;
    private int Max_Size = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_selector);
        ViewUtils.inject(this);
        initView();
        initClickEvent();
    }

    private void initClickEvent() {
        grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int viewType = mAdapter.getItemViewType(position);
                if (viewType == 1) {
                    showSelectPictureDialog();//第一个
                } else {
                    showPictureActionDialog(position);
                }
            }
        });
    }

        private void showPictureActionDialog(final int position) {
            String[] items = new String[]{getString(R.string.look_over), getString(R.string.common_delete)};
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(R.string.pictures)
                    .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == 0) {// 查看
                                Intent intent = new Intent(ct, MultiImagePreviewActivity.class);
                                intent.putExtra(AppConstant.EXTRA_IMAGES, mPhotoList);
                                intent.putExtra(AppConstant.EXTRA_POSITION, position);
                                intent.putExtra(AppConstant.EXTRA_CHANGE_SELECTED, false);
                                startActivity(intent);
                            } else {// 删除
                                mPhotoList.remove(position);
                                mAdapter.notifyDataSetInvalidated();
                            }
                            dialog.dismiss();
                        }
                    });
            builder.show();
    }

    private void initView() {

        grid_view = (MyGridView) findViewById(R.id.grid_view);
        mPhotoList = new ArrayList<>();
        mAdapter = new ComPictureAdapter(this);
        mAdapter.setmPhotoList(mPhotoList);
        mAdapter.setMaxSiz(Max_Size);
        grid_view.setAdapter(mAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x01 && resultCode == 0x02 && data != null){
           mPhotoList.addAll(data.getStringArrayListExtra("files"));
            Log.i("files0x01",data.getStringArrayListExtra("files").toString());
            Log.i("mPhotoList",mPhotoList.toString());
            doImageFiltering(mPhotoList);
//            mAdapter.notifyDataSetInvalidated();
        }
        if (requestCode == 0x04) {// 拍照返回
            if (resultCode == Activity.RESULT_OK) {
                if (mNewPhotoUri != null) {
                    mPhotoList.add(mNewPhotoUri.getPath());
                    mAdapter.notifyDataSetInvalidated();
                } else {
                    ToastUtil.showToast(this, R.string.c_take_picture_failed);
                }
            }
        }
    }

    private void doImageFiltering(ArrayList<String> mPhotoList) {
        for (int i = 0; i < mPhotoList.size(); i++) {
            File file = new File(mPhotoList.get(i).toString());
            if (!file.isFile() ){
//                mPhotoList.remove(i);
                Toast.makeText(ct,"第"+ (i+1)+"张图片格式不对，可能会上传失败，建议更换",Toast.LENGTH_LONG).show();
            }
            if (i == mPhotoList.size() -1){
                mAdapter.notifyDataSetInvalidated();
            }
        }
    }


    private void showSelectPictureDialog() {
        String[] items = new String[]{getString(R.string.c_take_picture), getString(R.string.c_photo_album)};
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            takePhoto();
                        } else {
                            Intent intent = new Intent();
                            intent.putExtra("MAX_SIZE",Max_Size);
                            intent.putExtra("CURRENT_SIZE",mPhotoList == null ? 0 : mPhotoList.size());
                            intent.setClass(ct,ImgFileListActivity.class);
                            startActivityForResult(intent,0x01);
                        }
                        dialog.dismiss();
                    }
                });
        builder.show();
    }
    private Uri mNewPhotoUri;// 拍照和图库 获得图片的URI
    private void takePhoto() {
        mNewPhotoUri = CameraUtil.getOutputMediaFileUri(this, MyApplication.getInstance().mLoginUser.getUserId(),CameraUtil.MEDIA_TYPE_IMAGE);
        CameraUtil.captureImage(this, mNewPhotoUri, 0x04);
    }
}

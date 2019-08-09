package com.modular.appmessages.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.core.widget.CustomProgressDialog;
import com.modular.appmessages.R;


/**
 * Created by RaoMeng on 2016/8/19.
 */
public abstract class BaseFragment extends Fragment {
    protected View root;//fragment的布局对象
    protected CustomProgressDialog progressDialog;

    //工厂模式
    public static <T extends BaseFragment> T newInstance(Class<T> tClass){
        if (tClass != null){
            try {
                T t = tClass.newInstance();
                return t;
            } catch (java.lang.InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(getLayout(),container,false);
      
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = CustomProgressDialog.createDialog(getActivity());
        progressDialog.setMessage("努力加载中...");
//        progressDialog.setCancelable(false);
        initViews();
        initEvents();
        initDatas();
    }

    protected abstract int getLayout();

    protected abstract void initViews();

    protected abstract void initEvents();

    protected abstract void initDatas();


    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        getActivity().overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
        getActivity().overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
    }

}

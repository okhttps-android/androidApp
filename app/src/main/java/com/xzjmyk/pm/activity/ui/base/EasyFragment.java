package com.xzjmyk.pm.activity.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lidroid.xutils.ViewUtils;
import com.xzjmyk.pm.activity.R;

/**
 * @author Dean Tao
 * @version 1.0
 */
public abstract class EasyFragment extends Fragment {

    private View mRootView;
    protected  boolean isRunable;
    protected Context ct;
    /**
     * 是否缓存视图
     *
     * @return
     */
    protected boolean cacheView() {
        return true;
    }

    /**
     * 指定该Fragment的Layout id
     *
     * @return
     */
    protected abstract int inflateLayoutId();

    /**
     * 代替onCreateView的回调
     *
     * @param savedInstanceState
     * @param createView         是否重新创建了视图，如果是，那么你需要重新findView来初始化子视图的引用等。
     */
    protected abstract void onCreateView(Bundle savedInstanceState, boolean createView);

    public View getmRootView() {
        return mRootView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("roamer", TAG() + " onCreateView");
        boolean createView = true;
        if (cacheView() && mRootView != null) {
            ViewGroup parent = (ViewGroup) mRootView.getParent();
            if (parent != null) {
                parent.removeView(mRootView);
            }
            createView = false;
        } else {
            mRootView = inflater.inflate(inflateLayoutId(), container, false);
            ViewUtils.inject(this, mRootView);
        }
        onCreateView(savedInstanceState, createView);
        return mRootView;
    }
    private MenuItem.OnMenuItemClickListener omOnMenuItemClickListener= new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            onOptionsItemSelected(menuItem);
            return false;
        }
    };
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu!=null){
            for (int i=0;i<menu.size();i++){
                MenuItem item = menu.getItem(i);
                if (item!=null){
                    item.setOnMenuItemClickListener(omOnMenuItemClickListener);
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    public View findViewById(int id) {
        if (mRootView != null) {
            return mRootView.findViewById(id);
        }
        return null;
    }

    public View findViewWithTag(Object tag) {
        if (mRootView != null) {
            return mRootView.findViewWithTag(tag);
        }
        return null;
    }

    public String TAG() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ct=context;
        isRunable=true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("roamer", TAG() + " onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("roamer", TAG() + " onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("roamer", TAG() + " onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("roamer", TAG() + " onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("roamer", TAG() + " onPause");
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d("roamer", TAG() + " onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("roamer", TAG() + " onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("roamer", TAG() + " onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        isRunable=false;
        Log.d("roamer", TAG() + " onDetach");
    }


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

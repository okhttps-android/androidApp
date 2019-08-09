package com.core.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.common.LogUtil;
import com.core.app.R;
import com.lidroid.xutils.ViewUtils;

/**
 * @author Dean Tao
 * @version 1.0
 */
public abstract class EasyFragment extends Fragment {

	private View mRootView;
	public boolean isRunable;
	public Context ct;

	public boolean isVisible;
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if(getUserVisibleHint()) {
			isVisible = true;
		} else {
			isVisible = false;
		}

	}
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
	public View getmRootView() {
		return mRootView;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

	public <T extends View> T  findViewById(int id) {
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
		ct = context;
		isRunable = true;
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
		isRunable = false;
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


	/*处理权限问题*/
	private SparseArray<Runnable> allowablePermissionRunnables;
	private SparseArray<Runnable> disallowablePermissionRunnables;
	private int permissionItem = 0;


	/**
	 * 判断是否缺少权限
	 *
	 * @param permission
	 * @return true 缺少   false  以获取
	 */
	protected boolean lacksPermission(String permission) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return ContextCompat.checkSelfPermission(ct, permission) != PackageManager.PERMISSION_GRANTED;
		} else {
			return false;
		}
	}

	/**
	 * 请求权限，先判断，如果没有权限就去请求
	 * 尽量不将权限请求放在onResume 中，会出现不断循环请求
	 *
	 * @param permission           权限
	 * @param allowableRunnable    当取得权限后执行操作，主线程
	 * @param disallowableRunnable 当用户拒绝权限后执行操作，主线程
	 */
	protected void requestPermission(String permission, Runnable allowableRunnable, Runnable disallowableRunnable) {
		permissionItem++;
		if (allowableRunnable != null) {
			if (allowablePermissionRunnables == null) {
				allowablePermissionRunnables = new SparseArray<>();
			}
			allowablePermissionRunnables.put(permissionItem, allowableRunnable);
		}
		if (disallowableRunnable != null) {
			if (disallowablePermissionRunnables == null) {
				disallowablePermissionRunnables = new SparseArray<>();
			}
			disallowablePermissionRunnables.put(permissionItem, disallowableRunnable);
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			//减少是否拥有权限  
			int checkCallPhonePermission = ContextCompat.checkSelfPermission(ct, permission);
			if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {//没有获取到权限
//				if (!shouldShowRequestPermissionRationale(permission)) {
				//弹出对话框接收权限  
				requestPermissions(new String[]{permission}, permissionItem);
//				} else {
//					ToastUtil.showToast(ct, R.string.not_camera_permission);
//				}
			} else {
				if (allowableRunnable != null) {
					allowableRunnable.run();
				}
			}
		} else {
			if (allowableRunnable != null) {
				allowableRunnable.run();
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (permissions != null) {
			for (String p : permissions) {
				LogUtil.i("permission=" + p);
			}
		}
		if (grantResults != null && grantResults.length > 0) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				if (allowablePermissionRunnables != null) {
					Runnable allowRun = allowablePermissionRunnables.get(requestCode);
					if (allowRun != null) {
						allowRun.run();
					}
				}

			} else {
				if (disallowablePermissionRunnables != null) {
					Runnable disallowRun = disallowablePermissionRunnables.get(requestCode);
					if (disallowRun != null) {
						disallowRun.run();
					}
				}

			}
		}
	}

}
